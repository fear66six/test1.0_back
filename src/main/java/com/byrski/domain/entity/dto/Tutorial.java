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
@Builder
@TableName("tutorial")
public class Tutorial {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @TableField("ski_choice")
    private Integer skiChoice;
    @TableField("title")
    private String title;
    @TableField("subtitle")
    private String subtitle;
    @TableField("content")
    private String content;
    @TableField("video_url")
    private String videoUrl;
    @TableField("video_title")
    private String videoTitle;
}
