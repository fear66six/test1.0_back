package com.byrski.domain.entity.vo.request;

import lombok.Data;

import java.util.List;

/**
 * 创建优惠券发放预览会话请求
 */
@Data
public class CouponIssuePreviewCreateVo {
    private Long couponId;
    private List<Long> userIds;
    private List<String> phoneNumbers;

    // Excel导入可选
    private String excelBase64Data;
    private String fileName;
    private String phoneColumnName;

    private Integer quantity; // 每用户发放数量，默认1
    private String remark;

    // 新增：是否导入全体用户
    private Boolean allUsers;

    // 新增：筛选条件
    // 若不为空，仅筛选学生(true)/非学生(false)
    private Boolean isStudent;
    // 排除黑名单（默认开启）。黑名单等同于 is_active = false
    private Boolean excludeBlacklist;
}


