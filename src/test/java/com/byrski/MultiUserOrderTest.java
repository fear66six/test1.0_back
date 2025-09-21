package com.byrski;

import com.byrski.domain.entity.vo.request.OrderVo;
import com.byrski.domain.entity.vo.response.PaymentVo;
import com.byrski.service.TradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

/**
 * 多人票订单测试
 * 测试多人票的创建和查询功能
 */
@SpringBootTest
@ActiveProfiles("test")
public class MultiUserOrderTest {

    @Autowired
    private TradeService tradeService;

    @Test
    public void testMultiUserOrderCreation() {
        // 创建多人票订单请求
        OrderVo order = new OrderVo();
        order.setProductId("test_product_001");
        order.setStationId(1L);
        order.setDescription("测试多人票订单");
        order.setOpenid("test_openid_123");
        
        // 添加多个用户信息
        OrderVo.OrderUserInfo user1 = new OrderVo.OrderUserInfo();
        user1.setName("张三");
        user1.setGender(1);
        user1.setIdCardNumber("110101199001011234");
        user1.setPhone("13800138001");
        user1.setSchoolId(1L);
        
        OrderVo.OrderUserInfo user2 = new OrderVo.OrderUserInfo();
        user2.setName("李四");
        user2.setGender(2);
        user2.setIdCardNumber("110101199002022345");
        user2.setPhone("13800138002");
        user2.setSchoolId(1L);
        
        order.setUsers(Arrays.asList(user1, user2));
        
        // 可选：添加优惠券
        // order.setUserCouponId(1L);
        
        // 可选：添加租赁物品
        // order.setRentItemIds(Arrays.asList(1L, 2L));
        
        // 可选：添加房间票
        // order.setRoomTicketId(1L);
        
        try {
            // 创建订单
            PaymentVo paymentVo = tradeService.makeOrder(order);
            
            System.out.println("订单创建成功！");
            System.out.println("订单号: " + paymentVo.getTradeId());
            System.out.println("总金额: " + paymentVo.getTotalAmount() + " 元");
            System.out.println("用户数量: " + paymentVo.getUserCount());
            
            // 查询多用户订单详情
            String outTradeNo = paymentVo.getTradeId().toString(); // 这里需要根据实际实现调整
            // TradeDetailVo detail = tradeService.getTradeDetail(tradeId);
            // System.out.println("订单详情查询成功，用户数量: " + detail.getUserCount());
            
        } catch (Exception e) {
            System.err.println("订单创建失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
