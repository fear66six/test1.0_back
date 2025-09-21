package com.byrski.domain.entity.vo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Place {
    @JsonSerialize(using = ToStringSerializer.class)
    private long id;
    private String area;
    private String school;
    private String campus;
    private String location;
    private int choicePeopleNum;
    private int targetPeopleNum;
}
