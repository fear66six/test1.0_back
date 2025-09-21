package com.byrski.service;

import com.byrski.domain.entity.vo.request.TradeVo;
import com.byrski.domain.entity.vo.response.PaymentVo;
import com.wechat.pay.java.service.refund.model.Refund;
import jakarta.servlet.http.HttpServletRequest;

public interface WechatPayService {

    /**
     * 调用微信支付业务
     * @param tradeVo 订单信息
     * @param tradeId 订单ID
     * @param originalAmount 原始金额（分）
     * @param discountAmount 优惠金额（分）
     * @param userCount 用户数量
     * @return 支付信息
     */
    PaymentVo callWechatPay(TradeVo tradeVo, Long tradeId, Integer originalAmount, Integer discountAmount, Integer userCount);

    /**
     * 解析微信支付通知
     * @param httpServletRequest 携带RequestParam的原始请求
     * @param requestBody 请求体
     */
    void resolveNotify(HttpServletRequest httpServletRequest, String requestBody);

    /**
     * 查询支付信息
     * @param tradeId 订单id
     * @return 支付信息
     */
    PaymentVo queryPaymentVo(Long tradeId);

    /**
     * 查询退款状态
     * @param outRefundNo 商户退款单号
     */
    Refund queryRefundStatus(String outRefundNo);

    /**
     * 解析退款通知
     * @param httpRequest 携带RequestParam的原始请求
     * @param requestBody 请求体
     */
    void resolveRefundNotify(HttpServletRequest httpRequest, String requestBody);
}
