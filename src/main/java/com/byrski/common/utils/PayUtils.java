package com.byrski.common.utils;

import com.byrski.common.config.WechatPayConfig;
import com.byrski.domain.entity.dto.Product;
import com.byrski.domain.entity.dto.Trade;
import com.byrski.domain.enums.BusinessNoPrefix;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.domain.enums.TradeStatus;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.impl.*;
import com.byrski.infrastructure.repository.manager.ProductManager;
import com.byrski.service.CouponService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.TransactionAmount;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.byrski.common.utils.Const.invalidRefundStatuses;

/**
 * PayUtils 类是一个工具类，用于处理与支付相关的操作，包括订单的取消、退款、数据回退、订单查询、关闭订单等功能。
 * 对外提供两个接口，分别是 cancelTrade 和 closeOrder。
 * 该类使用了微信支付的相关服务和配置，通过注入各种服务和配置对象，实现了与微信支付的集成。
 * 它依赖于多个服务和配置类，如 WechatPayConfig、TradeMapperService、TicketMapperService 等，
 * 并使用了 lombok 的 @Slf4j 注解进行日志记录。
 */
@Component
@Slf4j
public class PayUtils {

    private final WechatPayConfig wechatPayConfig;
    private final TradeMapperService tradeMapperService;
    private final ActivityMapperService activityMapperService;
    private final StationMapperService stationMapperService;
    private final AccountMapperService accountMapperService;
    private final ProductManager productManager;
    private final TradeTicketsMapperService tradeTicketsMapperService;
    private final TicketMapperService ticketMapperService;
    private final CouponService couponService;

    public PayUtils(WechatPayConfig wechatPayConfig, TradeMapperService tradeMapperService, ActivityMapperService activityMapperService, StationMapperService stationMapperService, AccountMapperService accountMapperService, ProductManager productManager, TradeTicketsMapperService tradeTicketsMapperService, TicketMapperService ticketMapperService, CouponService couponService) {
        this.wechatPayConfig = wechatPayConfig;
        this.tradeMapperService = tradeMapperService;
        this.activityMapperService = activityMapperService;
        this.stationMapperService = stationMapperService;
        this.accountMapperService = accountMapperService;
        this.productManager = productManager;
        this.tradeTicketsMapperService = tradeTicketsMapperService;
        this.ticketMapperService = ticketMapperService;
        this.couponService = couponService;
    }

    @Value("${callback.url}")
    private String callbackUrl;

    /**
     * 取消tradeId对应的订单。对于已经付款的trade，调用微信退款接口并更新用户的saved_money和points属性；对于未支付的订单，调用微信的关闭订单接口；对于所有的订单，更新门票销量、活动参与人数、车站选择人数
     * @param tradeId 需要取消的订单id
     * @return 是否取消成功
     */
    public Refund cancelTrade(Long tradeId) {
        return cancelTrade(tradeMapperService.getWithoutOwner(tradeId));
    }

    /**
     * 取消tradeId对应的订单。对于已经付款的trade，调用微信退款接口并更新用户的saved_money和points属性；对于未支付的订单，调用微信的关闭订单接口；对于所有的订单，更新门票销量、活动参与人数、车站选择人数
     * @param trade 需要取消的订单
     * @return 是否取消成功
     */
    public Refund cancelTrade(Trade trade) {
        // 订单状态不可为不允许退款的状态
        if (invalidRefundStatuses.contains(trade.getStatus())) {
            throw new ByrSkiException(ReturnCode.valueOf("TRADE_" + TradeStatus.fromCode(trade.getStatus()).name()));
        }
        return getRefund(trade);
    }

    /**
     * 取消订单，包含退款和关闭订单两个操作；若调用本方法则直接忽略前置的订单状态检查
     * @param trade 订单
     * @return 退款信息
     */
    public Refund getRefund(Trade trade) {
        log.info("取消订单，Id: {}", trade.getId());

        Refund refund = null;
        // 若订单不为未支付状态，则先进行退款流程
        if (trade.getStatus() != TradeStatus.UNPAID.getCode()) {
            tradeMapperService.updateStatus(trade.getId(), TradeStatus.REFUNDING);
            // 回滚支付后更新的数据
            rollbackPaidData(trade);
            // 订单状态不是未支付，先退款
            refund = refund(trade.getId());
            log.info("退款发起：{}", refund);
        } else {
            // 订单为未支付状态，调用关闭订单接口
            closeOrder(trade.getOutTradeNo());
            tradeMapperService.updateStatus(trade.getId(), TradeStatus.CANCELLED);
        }
        // 回滚在下单时就会更新的数据
        rollbackData(trade);
        return refund;
    }

    /**
     * 回退下单之后即更新的对应数据，包括门票销量、活动参与人数、车站选择人数
     * 此方法用于将订单相关的数据进行回退操作，以保证数据的一致性和准确性
     * @param trade 需要回退的订单
     */
    private void rollbackData(Trade trade) {
        Long activityId = trade.getActivityId();
        Long stationId = trade.getStationId();
        String productId = trade.getProductId();
        // 将副产品销量减一
        tradeTicketsMapperService.getTicketIdsByTradeId(trade.getId()).forEach(ticketMapperService::subSale);
        // 将主产品销量减一
        productManager.subSale(productId);
        // 活动参与人数减一
        activityMapperService.subCurrentParticipant(activityId);
        // 车站选择人数减一
        stationMapperService.subChoicePeopleNum(stationId);
        
        // 如果订单使用了优惠券，则退还优惠券
        if (trade.getUserCouponId() != null) {
            try {
                couponService.refundCoupon(trade.getUserCouponId());
                log.info("订单取消时自动退还优惠券成功，订单ID: {}, 用户优惠券ID: {}", trade.getId(), trade.getUserCouponId());
            } catch (Exception e) {
                log.error("订单取消时退还优惠券失败，订单ID: {}, 用户优惠券ID: {}", trade.getId(), trade.getUserCouponId(), e);
                // 优惠券退还失败不应该影响订单取消流程，只记录日志
            }
        }
    }

    /**
     * 回退支付之后才更新的对应数据，包括用户的 saved_money 和 points 属性
     * 此方法用于在订单支付后进行数据回退操作，主要涉及用户的 saved_money 和 points 属性
     * @param trade 需要回退的订单
     */
    private void rollbackPaidData(Trade trade) {
        Product product = productManager.getProductById(trade.getProductId());
        accountMapperService.subPointsAndSavedMoney(trade, product.getOriginalPrice() - product.getPrice());
    }

    /**
     * 生成退款订单并交给微信处理退款
     * 此方法用于生成退款订单，并将其交给微信服务进行退款处理
     * @param tradeId 订单 ID
     * @return 退款信息
     */
    private Refund refund(Long tradeId) {
        Config config = getConfig();
        RefundService service = new RefundService.Builder().config(config).build();
        Trade trade = tradeMapperService.getWithoutOwner(tradeId);
        // 生成退款订单并交给微信处理退款
        return requestWechatRefund(trade.getOutTradeNo(), service);

    }

    /**
     * 向微信发起退款请求
     * @param outTradeNo 商户订单号
     * @param refundService 退款服务类
     * @return 退款订单
     * @throws ByrSkiException 可能抛出的自定义异常
     */
    private Refund requestWechatRefund(String outTradeNo, RefundService refundService) throws ByrSkiException {

        CreateRequest request = new CreateRequest();
        Transaction transaction = queryOrderByOutTradeNo(outTradeNo);
        if (transaction == null) {
            throw new ByrSkiException(ReturnCode.TRADE_NOT_EXIST);
        }
        log.info("Transaction: {}", transaction);
        try {
            TransactionAmount transactionAmount = transaction.getAmount();
            // payerTotal为支付者支付的金额，total为订单金额，在订单未支付时，payerTotal不存在
            Integer total =  transactionAmount.getPayerTotal();
            request.setOutRefundNo(getBusinessNo(BusinessNoPrefix.REFUND));
            AmountReq amountReq = new AmountReq();
            amountReq.setCurrency("CNY");
            amountReq.setRefund(Long.valueOf(total));
            amountReq.setTotal(Long.valueOf(total));

            request.setAmount(amountReq);
            request.setOutTradeNo(outTradeNo);
            request.setNotifyUrl(callbackUrl + "/api/payment/wechat/refund/notify");
            return refundService.create(request);

        } catch (ByrSkiException e) {
            throw e;
        } catch (Exception e) {
            throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(), "微信支付订单退款失败，对应Transaction状态：" + transaction.getTradeState() + "，状态描述：" + transaction.getTradeStateDesc());
        }
    }

    /**
     * 生成订单号，支持不同前缀加时间戳
     * @param businessType 业务类型
     * @return 生成的订单号
     */
    public String getBusinessNo(BusinessNoPrefix businessType) {
        String timestamp = LocalDateTime.now().toString()
                .replace("-", "")
                .replace(":", "")
                .replace(".", "")
                .replace("T", "");
        String result = businessType.getPrefix() + timestamp;
        return result.substring(0, Math.min(result.length(), 32));
    }

    /**
     * 根据订单号查询订单信息
     * @param outTradeNo 订单号
     * @return 订单信息
     */
    private Transaction queryOrderByOutTradeNo(String outTradeNo) {
        Config config = getConfig();
        JsapiServiceExtension jsapiServiceExtension = getJsapiServiceExtension(config);
        // 需要out_trade_no和mchid
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setOutTradeNo(outTradeNo);
        request.setMchid(wechatPayConfig.getMerchantId());
        return jsapiServiceExtension.queryOrderByOutTradeNo(request);
    }

    /**
     * 获取微信支付的配置信息
     * 该方法通过构建 RSAAutoCertificateConfig 对象，使用 WechatPayConfig 中的相关信息，包括商户 ID、私钥、商户序列号和 API V3 密钥
     * 来生成 RSAAutoCertificateConfig 配置对象，用于后续的微信支付相关操作
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
     * 获取 JsapiServiceExtension 对象
     * 该方法通过使用传入的微信支付配置对象，使用构建器模式创建 JsapiServiceExtension 对象
     * 并设置签名类型为 RSA，最终完成对象的构建
     * @param config 微信支付的配置对象
     * @return 构建好的 JsapiServiceExtension 对象
     */
    private JsapiServiceExtension getJsapiServiceExtension(Config config) {
        return new JsapiServiceExtension.Builder()
                .config(config)
                .signType("RSA")
                .build();
    }

    /**
     * 关闭订单，不包含数据库操作，仅调用微信接口
     * 商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；
     * 系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。
     * @param outTradeNo 商户订单号
     */
    public void closeOrder(String outTradeNo) {
        Config config = getConfig();
        JsapiServiceExtension jsapiServiceExtension = getJsapiServiceExtension(config);
        CloseOrderRequest request = new CloseOrderRequest();
        request.setOutTradeNo(outTradeNo);
        request.setMchid(wechatPayConfig.getMerchantId());

        jsapiServiceExtension.closeOrder(request);
    }
}
