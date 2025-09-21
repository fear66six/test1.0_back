package com.byrski.domain.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.byrski.common.utils.GetSchoolSerializer;
import com.byrski.common.utils.PriceSerializer;
import com.byrski.domain.entity.BaseData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("account")
@AllArgsConstructor
@NoArgsConstructor
public class Account implements BaseData {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @TableField("openid")
    private String openid;
    @TableField("username")
    private String username;
    @TableField("password")
    @JsonIgnore
    private String password;
    @TableField("email")
    private String email;
    @TableField("phone")
    private String phone;
    @TableField("id_card_number")
    private String idCardNumber;
    @TableField("profile")
    private String profile;
    @TableField("points")
    private Integer points;
    @TableField("identity")
    private Integer identity;
    @TableField("intro")
    private String intro;
    @TableField("is_student")
    private Boolean isStudent;
    @TableField("is_active")
    private Boolean isActive;
    @TableField("ski_board")
    private Integer skiBoard;
    @TableField("ski_level")
    private Integer skiLevel;
    @TableField("ski_favor")
    private Integer skiFavor;
    @TableField("gender")
    private Integer gender;
    @TableField("height")
    private Integer height;
    @TableField("weight")
    private Integer weight;
    @TableField("foot_length")
    private Integer footLength;
    @TableField("ski_boots_size")
    private Double skiBootsSize;
    @TableField("snowboard_size_1")
    private Integer snowboardBoardSize1;
    @TableField("snowboard_size_2")
    private Integer snowboardBoardSize2;
    @TableField("snowboard_hardness")
    private Integer snowHardness;
    @TableField("skipole_size")
    private Integer skipoleSize;
    @TableField("register_time")
    private LocalDateTime registerTime;
    @TableField(value = "update_time", update = "now()")
    private LocalDateTime updateTime;
    @TableField("school_id")
    @JsonSerialize(using = GetSchoolSerializer.class)
    private Long schoolId;
    @TableField("saved_money")
    @JsonSerialize(using = PriceSerializer.class)
    private Integer savedMoney;
    @TableField("lead_times")
    private Integer leadTimes;


    public Account(String openid, String username, String phone,Integer identity, Boolean isStudent, LocalDateTime registerTime, LocalDateTime updateTime, Integer points, Boolean isActive) {
        this.openid = openid;
        this.phone = phone;
        this.username = username;
        this.identity = identity;
        this.isStudent = isStudent;
        this.registerTime = registerTime;
        this.updateTime = updateTime;
        this.points = points;
        this.isActive = isActive;
    }

    public Account(String username, String password, String email, String user, LocalDateTime date) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.registerTime = date;
    }
}
