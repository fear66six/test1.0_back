package com.byrski.domain.entity.vo.request;

import lombok.Data;

@Data
public class WxLoginRequestVo {
    private String loginCode;
    private String phoneCode;
}