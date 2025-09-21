package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("activity")
@AllArgsConstructor
@NoArgsConstructor
public class Activity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("snowfield_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;

    @TableField("activity_begin_date")
    private LocalDate activityBeginDate;

    @TableField("activity_end_date")
    private LocalDate activityEndDate;

    @TableField("signup_ddl_date")
    private LocalDateTime signupDdlDate;

    @TableField("lock_ddl_date")
    private LocalDateTime lockDdlDate;

    @TableField("status")
    private Integer status;

    @TableField("target_participant")
    private Integer targetParticipant;

    @TableField("current_participant")
    private Integer currentParticipant;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;

    @TableField("activity_template_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityTemplateId;

    @TableField("return_loc")
    private String returnLocation;

    @TableField("return_time")
    private LocalDateTime returnTime;

    @TableField("success_departure")
    private Boolean successDeparture;

    @TableField("user_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
}