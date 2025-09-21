package com.byrski.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.byrski.common.config.WechatPayConfig;
import com.byrski.common.utils.Const;
import com.byrski.domain.entity.dto.Product;
import com.byrski.domain.entity.dto.Trade;
import com.byrski.domain.entity.dto.WechatTradeDetail;
import com.byrski.domain.entity.vo.request.TradeVo;
import com.byrski.domain.entity.vo.response.PaymentVo;
import com.byrski.domain.entity.vo.response.RefundVo;
import com.byrski.domain.enums.TradeStatus;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.impl.*;
import com.byrski.infrastructure.repository.manager.ProductManager;
import com.byrski.service.WechatPayService;
import com.byrski.common.utils.PrepayRequestHelper;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class WechatPayServiceImpl implements WechatPayService {

    private final WechatPayConfig wechatPayConfig;
    private final TradeMapperService tradeMapperService;
    private final AmqpTemplate amqpTemplate;
    private final AccountMapperService accountMapperService;
    private final WechatTradeDetailMapperService wechatTradeDetailMapperService;
    private final RentOrderMapperService rentOrderMapperService;
    private final ProductManager productManager;

    public WechatPayServiceImpl(
            WechatPayConfig wechatPayConfig,
            TradeMapperService tradeMapperService,
            AmqpTemplate amqpTemplate,
            AccountMapperService accountMapperService,
            WechatTradeDetailMapperService wechatTradeDetailMapperService,
            RentOrderMapperService rentOrderMapperService, ProductManager productManager) {
        this.wechatPayConfig = wechatPayConfig;
        this.tradeMapperService = tradeMapperService;
        this.amqpTemplate = amqpTemplate;
        this.accountMapperService = accountMapperService;
        this.wechatTradeDetailMapperService = wechatTradeDetailMapperService;
        this.rentOrderMapperService = rentOrderMapperService;
        this.productManager = productManager;
    }

    @Value("${callback.url}")
    private String callbackUrl;

    /**
     * 获取微信支付配置
     * @param tradeVo 订单信息，注意这里tradeId为空，不可使用
     * @param tradeId 订单ID
     * @param originalAmount 原始金额（分）
     * @param discountAmount 优惠金额（分）
     * @param userCount 用户数量
     * @return 微信支付信息
     */
    @Override
    public PaymentVo callWechatPay(TradeVo tradeVo, Long tradeId, Integer originalAmount, Integer discountAmount, Integer userCount) {
        Config config = getConfig();
        JsapiServiceExtension jsapiServiceExtension = getJsapiServiceExtension(config);

        String openid = tradeVo.getOpenid();
        String outTradeNo = tradeVo.getOutTradeNo();
        String description = tradeVo.getDescription();
        Integer total = tradeVo.getTotal();

        // 创建下单请求
        PrepayRequest request = PrepayRequestHelper.createRequest(
                total,
                wechatPayConfig.getAppId(),
                wechatPayConfig.getMerchantId(),
                description,
                outTradeNo,
                openid,
                callbackUrl
        );

        // 下单
        PrepayWithRequestPaymentResponse response = jsapiServiceExtension.prepayWithRequestPayment(request);
        log.info("下单成功，outTradeNo: {}", outTradeNo);
        log.info(response.toString());

        // 将微信支付信息保存到数据库
        saveWechatTradeDetail(response, tradeId);

        // 发送消息到延迟队列，用于自动取消订单
        JSONObject message = new JSONObject();
        message.put("outTradeNo", tradeVo.getOutTradeNo());
        amqpTemplate.convertAndSend(Const.QUEUE_TRADE_TTL, message.toJSONString());

        return PaymentVo.builder()
                .appId(response.getAppId())
                .timeStamp(response.getTimeStamp())
                .nonceStr(response.getNonceStr())
                .packageVal(response.getPackageVal())
                .signType(response.getSignType())
                .paySign(response.getPaySign())
                .tradeId(tradeId)
                .totalAmount(total)
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .userCount(userCount)
                .build();
    }

    private void saveWechatTradeDetail(PrepayWithRequestPaymentResponse response, Long tradeId) {
        String timestamp = response.getTimeStamp();
        String nonceStr = response.getNonceStr();
        String packageVal = response.getPackageVal();
        String signType = response.getSignType();
        String paySign = response.getPaySign();
        wechatTradeDetailMapperService.save(new WechatTradeDetail(
                timestamp,
                nonceStr,
                packageVal,
                signType,
                paySign,
                tradeId
        ));
    }

    @Override
    public void resolveNotify(HttpServletRequest httpRequest, String requestBody) {
        // 解析回调数据
        RequestParam requestParam = getRequestParam(httpRequest, requestBody);
        NotificationConfig config = getConfig();
        NotificationParser parser = new NotificationParser(config);

        // 得到支付订单信息
        Transaction transaction = parser.parse(requestParam, Transaction.class);
        String outTradeNo = transaction.getOutTradeNo();

        // 更新订单状态和支付时间
        Trade trade = tradeMapperService.getByOutTradeNoWithoutAuth(outTradeNo);
        trade.setStatus(TradeStatus.PAID.getCode());
        trade.setPayTime(LocalDateTime.now());
        // 更新rent_order
        rentOrderMapperService.setRentOrdersPaid(trade.getId());

        if (tradeMapperService.updateById(trade)) {
            // 更新用户points和savedMoney
            Product product = productManager.getProductById(trade.getProductId());
            accountMapperService.addPointsAndSavedMoney(trade, product.getOriginalPrice() - product.getPrice());
            log.info("订单支付成功，outTradeNo: {}", outTradeNo);
        } else {
            throw new ByrSkiException(ReturnCode.DATABASE_ERROR);
        }
    }

    @Override
    public PaymentVo queryPaymentVo(Long tradeId) {
        return wechatTradeDetailMapperService.queryPaymentVo(tradeId);
    }

    @Override
    public void resolveRefundNotify(HttpServletRequest httpRequest, String requestBody) {
        // 解析回调数据
        RequestParam requestParam = getRequestParam(httpRequest, requestBody);
        NotificationConfig config = getConfig();
        NotificationParser parser = new NotificationParser(config);

        // 得到退款订单信息
        // 响应数据中为refund_status，而Refund类中设置为status，故自定义解析
        RefundVo refund = parser.parse(requestParam, RefundVo.class);
        if (!tradeMapperService.updateOrderStatus(refund.getOutTradeNo(), TradeStatus.REFUNDED)) {
            throw new ByrSkiException(ReturnCode.DATABASE_ERROR);
        }
    }

    @Override
    public Refund queryRefundStatus(String outRefundNo) {
        Config config = getConfig();
        RefundService service = new RefundService.Builder().config(config).build();

        return queryByOutRefundNo(outRefundNo, service);
    }

    /**
     * 获取微信支付请求参数，用于解析微信支付的回调信息，支付信息被填写在请求头中
     * @return 微信支付配置
     */
    private RequestParam getRequestParam(HttpServletRequest httpRequest, String requestBody) {
        String signature = httpRequest.getHeader("Wechatpay-Signature");
        String nonce = httpRequest.getHeader("Wechatpay-Nonce");
        String timestamp = httpRequest.getHeader("Wechatpay-Timestamp");
        String serialNo = httpRequest.getHeader("Wechatpay-Serial");
        String signatureType = httpRequest.getHeader("Wechatpay-Signature-Type");

        log.info("接收到微信支付回调通知：requestBody={}, signature={}, nonce={}, timestamp={}, serialNo={}, signatureType={}",
                requestBody, signature, nonce, timestamp, serialNo, signatureType);

        return new RequestParam.Builder()
                .serialNumber(serialNo)
                .nonce(nonce)
                .timestamp(timestamp)
                .signature(signature)
                .body(requestBody)
                .build();
    }

    /**
     * 通过商户退款单号查询退款订单
     * @param outRefundNo 商户退款单号
     * @param refundService 退款服务类
     * @return 退款订单
     */
    private Refund queryByOutRefundNo(String outRefundNo, RefundService refundService) {
        QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
        request.setOutRefundNo(outRefundNo);
        return refundService.queryByOutRefundNo(request);
    }

    /**
     * 获取微信支付配置
     * @return 微信支付配置
     */
    private RSAAutoCertificateConfig getConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(wechatPayConfig.getMerchantId())
                .privateKey(wechatPayConfig.getPrivateKey())
                .merchantSerialNumber(wechatPayConfig.getMerchantSerialNumber())
                .apiV3Key(wechatPayConfig.getApiV3Key())
                .build();
    }

    /**
     * 获取JsapiServiceExtension
     * @param config 微信支付配置
     * @return JsapiServiceExtension
     */
    private JsapiServiceExtension getJsapiServiceExtension(Config config) {
        return new JsapiServiceExtension.Builder()
                .config(config)
                .signType("RSA")
                .build();
    }
}
