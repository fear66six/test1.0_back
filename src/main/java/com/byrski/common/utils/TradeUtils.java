package com.byrski.common.utils;

import com.byrski.domain.enums.BusinessNoPrefix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class TradeUtils {

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
     * 生成一个六位长度的只包含数字的验证码
     * @return 生成的验证码
     */
    public String generateNumericVerificationCode() {
        int code = (int) (Math.random() * 900000) + 100000; // 生成100000到999999之间的随机数
        return String.valueOf(code);
    }
}
