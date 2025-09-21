package com.byrski.domain.entity.vo.request;

import lombok.Data;
import java.util.List;

@Data
public class OrderVo {
    // 公共参数
    private Long stationId;
    private String productId;
    private Long roomTicketId;
    private String description;
    private String openid;
    private List<Long> rentItemIds;
    private Long userCouponId; // 用户选择的优惠券ID
    
    // 用户信息列表
    private List<OrderUserInfo> users;
    
    @Data
    public static class OrderUserInfo {
        private String name;
        private Integer gender;
        private String idCardNumber;
        private String phone;
        private Long schoolId;
    }
}
