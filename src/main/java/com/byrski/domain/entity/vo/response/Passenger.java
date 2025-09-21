package com.byrski.domain.entity.vo.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Passenger {
    private String name;
    private Integer gender;
    private String phone;
    private String school;
    private String campus;
    private String location;
    private Boolean boarded;
}
