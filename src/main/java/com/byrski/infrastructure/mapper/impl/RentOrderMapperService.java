package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.RentOrder;
import com.byrski.domain.enums.RentOrderStatus;
import com.byrski.infrastructure.mapper.RentOrderMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RentOrderMapperService extends ServiceImpl<RentOrderMapper, RentOrder> {

    /**
     * 获取订单列表，无视订单状态
     * @param tradeId 订单id
     * @return 订单列表
     */
    public List<RentOrder> getByTradeId(Long tradeId) {
        return this.lambdaQuery()
                .eq(RentOrder::getTradeId, tradeId)
                .list();
    }

    public void setRentOrdersPaid(Long tradeId) {
        List<RentOrder> rentOrders = this.getByTradeId(tradeId);
        setRentOrdersPaid(rentOrders);
    }

    public void setRentOrdersPaid(List<RentOrder> rentOrders) {
        for (RentOrder rentOrder : rentOrders) {
            rentOrder.setStatus(RentOrderStatus.PAID.getCode());
            rentOrder.setPayTime(LocalDateTime.now());
            this.updateById(rentOrder);
        }
    }
}
