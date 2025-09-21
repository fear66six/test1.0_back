package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("service_button")
public class ServiceButton {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("service")
    private String service;

    @TableField("icon")
    private String icon;
}
