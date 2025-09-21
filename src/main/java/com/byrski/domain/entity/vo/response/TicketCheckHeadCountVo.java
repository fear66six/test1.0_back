package com.byrski.domain.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketCheckHeadCountVo {

    private Integer uncheckedPassengerNum;
    private Integer checkedPassengerNum;
    private Integer totalPassengerNum;
    private List<Entity> uncheckedPassengers;
    private List<Entity> totalPassengers;

    @Data
    @Builder
    public static class Entity {
        private String name;
        private Integer gender;
        private String phone;
        private String position;
        private Boolean checked;
    }

}
