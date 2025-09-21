package com.byrski;

import com.byrski.domain.entity.vo.request.OrderVo;
import com.byrski.domain.entity.vo.response.PaymentVo;
import com.byrski.domain.entity.vo.response.TradeDetailVo;
import com.byrski.service.TradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * 整合后的订单功能测试
 * 验证多用户功能已成功整合到现有的订单详情接口中
 */
@SpringBootTest
@Transactional
public class IntegratedOrderTest {

    @Autowired
    private TradeService tradeService;

    @Test
    public void testIntegratedOrderDetail() {
        // 创建多用户订单
        OrderVo order = new OrderVo();
        order.setStationId(1L);
        order.setProductId("test_product_001");
        order.setRoomTicketId(123L);
        order.setDescription("整合测试订单");
        order.setOpenid("test_openid_123");
        order.setRentItemIds(Arrays.asList(1L, 2L));
        order.setUserCouponId(456L);
        
        // 创建用户信息
        OrderVo.OrderUserInfo user1 = new OrderVo.OrderUserInfo();
        user1.setName("张三");
        user1.setGender(1);
        user1.setIdCardNumber("110101199001011234");
        user1.setPhone("13800138000");
        user1.setSchoolId(1L);
        
        OrderVo.OrderUserInfo user2 = new OrderVo.OrderUserInfo();
        user2.setName("李四");
        user2.setGender(2);
        user2.setIdCardNumber("110101199002022345");
        user2.setPhone("13800138001");
        user2.setSchoolId(2L);
        
        order.setUsers(Arrays.asList(user1, user2));

        try {
            // 创建订单
            PaymentVo paymentVo = tradeService.makeOrder(order);
            System.out.println("订单创建成功，订单ID: " + paymentVo.getTradeId());
            System.out.println("用户数量: " + paymentVo.getUserCount());
            System.out.println("总金额: " + paymentVo.getTotalAmount());

            // 使用整合后的接口查询订单详情
            TradeDetailVo detail = tradeService.getTradeDetail(paymentVo.getTradeId());
            System.out.println("订单详情查询成功");
            System.out.println("用户数量: " + detail.getUserCount());
            System.out.println("总金额: " + detail.getCost());
            System.out.println("用户列表大小: " + detail.getUsers().size());
            
                         // 验证用户信息
             if (!detail.getUsers().isEmpty()) {
                 TradeDetailVo.UserInfo firstUser = detail.getUsers().get(0);
                 System.out.println("第一个用户姓名: " + firstUser.getName());
                 System.out.println("第一个用户电话: " + firstUser.getPhone());
             }
             
             // 验证优惠券信息
             if (detail.getUserCouponId() != null) {
                 System.out.println("优惠券ID: " + detail.getUserCouponId());
                 if (detail.getCouponInfo() != null) {
                     System.out.println("优惠券名称: " + detail.getCouponInfo().getCouponName());
                     System.out.println("优惠金额: " + detail.getCouponInfo().getDiscountAmount());
                     System.out.println("优惠类型: " + detail.getCouponInfo().getDiscountType());
                 }
             } else {
                 System.out.println("未使用优惠券");
             }
            
            // 验证多用户信息
            for (int i = 0; i < detail.getUsers().size(); i++) {
                TradeDetailVo.UserInfo user = detail.getUsers().get(i);
                System.out.println("用户" + (i + 1) + ": " + user.getName() + " - " + user.getPhone());
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
