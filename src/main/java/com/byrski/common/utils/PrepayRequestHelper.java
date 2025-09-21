package com.byrski.common.utils;

import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;

public class PrepayRequestHelper {
    public static PrepayRequest createRequest(
            Integer total,
            String appId,
            String merchantId,
            String description,
            String outTradeNo,
            String openid,
            String callbackUrl

    ) {

        Amount amount = new Amount();
        amount.setTotal(total);

        Payer payer = new Payer();
        payer.setOpenid(openid);

        PrepayRequest request = new PrepayRequest();
        request.setAmount(amount);
        request.setAppid(appId);
        request.setMchid(merchantId);
        request.setDescription(description);
        request.setNotifyUrl(callbackUrl + "/api/payment/wechat/notify");
        request.setOutTradeNo(outTradeNo);
        request.setPayer(payer);

        return request;
    }
}