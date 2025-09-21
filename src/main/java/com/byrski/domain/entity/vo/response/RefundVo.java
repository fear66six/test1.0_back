package com.byrski.domain.entity.vo.response;

import com.google.gson.annotations.SerializedName;
import com.wechat.pay.java.service.refund.model.*;
import lombok.Data;

import java.util.List;

@Data
public class RefundVo {
    @SerializedName("refund_id")
    private String refundId;
    @SerializedName("out_refund_no")
    private String outRefundNo;
    @SerializedName("transaction_id")
    private String transactionId;
    @SerializedName("out_trade_no")
    private String outTradeNo;
    @SerializedName("user_received_account")
    private String userReceivedAccount;
    @SerializedName("success_time")
    private String successTime;
    @SerializedName("create_time")
    private String createTime;
    @SerializedName("promotion_detail")
    private List<Promotion> promotionDetail;
    @SerializedName("amount")
    private Amount amount;
    @SerializedName("channel")
    private Channel channel;
    @SerializedName("funds_account")
    private FundsAccount fundsAccount;
    @SerializedName("refund_status")
    private Status status;
}
