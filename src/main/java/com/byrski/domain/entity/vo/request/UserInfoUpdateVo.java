package com.byrski.domain.entity.vo.request;

import lombok.Data;

@Data
public class UserInfoUpdateVo {
    private String name;
    private Integer gender;
    private String email;
    private String phone;
    private Integer height;
    private Integer weight;
    private Double skiBootsSize;
    private Integer skiBoard;
    private Integer skiLevel;
    private Integer skiFavor;
    private Long schoolId;
}
