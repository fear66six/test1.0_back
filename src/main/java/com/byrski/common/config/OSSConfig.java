package com.byrski.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OSSConfig {

    @Bean(name="ossClient")
    OSS ossClient(@Value("${oss.access-key.secret}") String accessKeySecret,
                  @Value("${oss.access-key.id}") String accessKeyId,
                  @Value("${oss.endpoint}") String endpoint) {
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        return new OSSClientBuilder().build("https://" + endpoint, credentialsProvider);
    }
}
