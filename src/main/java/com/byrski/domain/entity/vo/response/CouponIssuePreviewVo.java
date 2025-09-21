package com.byrski.domain.entity.vo.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CouponIssuePreviewVo {
    private String previewId;
    private Long couponId;
    private Integer quantity;
    private Integer totalCount;
    private Integer validCount;
    private Integer invalidUserIdCount;
    private Integer invalidPhoneCount;
    private List<String> userIds; // 预览中的有效用户ID（以字符串返回，避免前端JS精度丢失）
    private List<String> invalidUserIds; // 字符串形式便于展示
    private List<String> invalidPhoneNumbers;
    private List<CouponBatchIssueResultVo.UserIssueInfo> successUsers; // 可选预览用户信息
    private String remark;
}


