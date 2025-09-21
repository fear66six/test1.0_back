package com.byrski.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat")
@Data
public class WechatPayConfig {
    private String merchantId;
    private String appId;
    private String privateKey;
    private String merchantSerialNumber;
    private String apiV3Key;
}