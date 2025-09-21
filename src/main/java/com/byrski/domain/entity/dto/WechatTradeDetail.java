package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@TableName("wechat_trade_detail")
public class WechatTradeDetail {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @TableField("timestamp")
    private String timestamp;
    @TableField("nonce_str")
    private String nonceStr;
    @TableField("package")
    private String packageVal;
    @TableField("sign_type")
    private String signType;
    @TableField("pay_sign")
    private String paySign;
    @TableField("trade_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tradeId;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;

    public WechatTradeDetail(String timestamp, String nonceStr, String packageVal, String signType, String paySign, Long tradeId) {
        this.timestamp = timestamp;
        this.nonceStr = nonceStr;
        this.packageVal = packageVal;
        this.signType = signType;
        this.paySign = paySign;
        this.tradeId = tradeId;
    }

    public WechatTradeDetail(Long id, String timestamp, String nonceStr, String packageVal, String signType, String paySign, Long tradeId) {
        this.id = id;
        this.timestamp = timestamp;
        this.nonceStr = nonceStr;
        this.packageVal = packageVal;
        this.signType = signType;
        this.paySign = paySign;
        this.tradeId = tradeId;
    }

}
