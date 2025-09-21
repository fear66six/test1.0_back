package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
@TableName("station_info")
public class StationInfo {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @TableField("school")
    private String school;
    @TableField("campus")
    private String campus;
    @TableField("location")
    private String location;
    @TableField("area_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long areaId;

    public String getPosition() {
        return this.school + this.campus + this.location;
    }
}
