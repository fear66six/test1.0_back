package com.byrski.domain.entity.vo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderInfoVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private Integer gender;
    private String phone;
    private String idCardNumber;
    private String school;
    private String intro;
    private String profile;

}
