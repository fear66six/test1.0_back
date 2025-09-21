package com.byrski.domain.entity.vo.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FromDate2Date {

    @SerializedName("date")
    private String date;

    @SerializedName("day")
    private String day;

}
