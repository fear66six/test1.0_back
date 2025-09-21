package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.vo.response.PaymentVo;
import com.byrski.service.WechatPayService;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/payment/wechat")
public class WechatPayController extends AbstractController {

    private final WechatPayService wechatPayService;

    @Autowired
    public WechatPayController(WechatPayService wechatPayService) {
        this.wechatPayService = wechatPayService;
    }

    /**
     * 查询微信支付的下单参数
     * @param tradeId 订单id，对应新表的tradeId
     * @return PaymentVo，包含支付参数
     */
    @GetMapping("/call/query")
    public RestBean<PaymentVo> query(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            protected PaymentVo doInTransactionWithResult(Long tradeId) throws Exception {
                return wechatPayService.queryPaymentVo(tradeId);
            }
        });
    }

    /**
     * 微信支付通知回调
     * @param requestBody 微信支付通知消息体
     * @return 无
     */
    @PostMapping("/notify")
    public RestBean<Void> notify(@RequestBody String requestBody) {
        return handleRequest(requestBody, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(String requestBody) throws Exception {
                wechatPayService.resolveNotify(httpRequest, requestBody);
            }
        });
    }

    /**
     * 微信支付退款查询
     * @param outRefundNo 商户退款单号
     * @return 无
     */
    @PostMapping("/refund/query")
    public RestBean<Refund> refundQuery(@RequestParam String outRefundNo) {
        return handleRequest(outRefundNo, log, new ExecuteCallbackWithResult<>() {
            @Override
            protected Refund doInTransactionWithResult(String outRefundNo) throws Exception {
                return wechatPayService.queryRefundStatus(outRefundNo);
            }
        });
    }

    /**
     * 微信支付退款通知回调
     * @param requestBody 微信支付退款通知消息体
     * @return 无
     */
    @PostMapping("/refund/notify")
    public RestBean<Void> refundNotify(@RequestBody String requestBody) {
        return handleRequest(requestBody, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(String requestBody) throws Exception {
                wechatPayService.resolveRefundNotify(httpRequest, requestBody);
            }
        });
    }
}
