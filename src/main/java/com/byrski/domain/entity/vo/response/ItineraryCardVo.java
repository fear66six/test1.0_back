package com.byrski.domain.entity.vo.response;

import com.byrski.domain.enums.ProductType;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Builder
@Data
public class ItineraryCardVo {
    private String skiResortName;
    private String location;
    private String skiResortPhone;
    private String hotel;
    private String fromArea;
    private String toArea;
    private String stationLocation;
    private String busMoveTime;
    private String name;
    private ProductType type;
    private Integer gender;
    private String phone;
    private String qrCode;
}