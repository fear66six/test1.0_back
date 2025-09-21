package com.byrski.domain.entity.vo.response;

import com.byrski.common.utils.PriceSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HomePageVo {

    private User user;
    private List<Activity> activities;

    @Data
    @Builder
    public static class User {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String name;
        private String school;
        private Integer identity;
        private Boolean isStudent;
        private Integer registerDays;
        @JsonSerialize(using = PriceSerializer.class)
        private Integer savedMoney;
        private Integer leadTimes;
        private String intro;
    }

    @Data
    @Builder
    public static class Activity {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long snowfieldId;
        private String snowfieldName;
        private String snowfieldArea;
        private String beginDate;
        private String cover;
        private LocalDateTime createTime;
        @JsonSerialize(using = PriceSerializer.class)
        private Integer minPrice;
    }

}
