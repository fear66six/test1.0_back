package com.byrski.domain.entity.vo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.gson.annotations.SerializedName;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketBriefVo {

    @Pattern(regexp = "(snow|bus|all)")
    private String type;

    @SerializedName("ticketId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ticketId;

    @SerializedName("activityId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityId;

    @SerializedName("activityTemplateId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityTemplateId;

    @SerializedName("snowfieldId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;

    @SerializedName("activityName")
    private String activityName;

    @SerializedName("begin2end")
    private FromDate2Date fromDate2Date;

    @SerializedName("intro")
    private String intro;

    @SerializedName("price")
    private String price;
}
