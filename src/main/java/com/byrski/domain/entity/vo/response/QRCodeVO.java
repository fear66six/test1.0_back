package com.byrski.domain.entity.vo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

// 二维码数据传输对象
@Data
@Builder
public class QRCodeVO {
    // 订单ID
    private String orderId;
    // 加密后的二维码数据
    private String qrCodeData;
    // Base64编码的二维码图片
    private String qrCodeImage;
    // 过期时间戳
    @JsonSerialize(using = ToStringSerializer.class)
    private long expireTime;
}