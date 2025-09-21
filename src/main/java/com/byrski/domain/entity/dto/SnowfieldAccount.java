package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@TableName("snowfield_account")
@Builder
public class SnowfieldAccount {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @TableField("snowfield_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;
    @TableField("user_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
}
