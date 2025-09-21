package com.byrski.domain.entity.vo.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

/**
 * 请求微信登录之后返回给前端的凭证信息
 */
@Data
public class LoginCredential {
    @SerializedName("openId")
    private String openId;
    @SerializedName("token")
    private String token;
    @SerializedName("isStudent")
    private Boolean isStudent;
    @SerializedName("identity")
    private Integer identity;
    @SerializedName("expireTime")
    private Date expireTime;
}