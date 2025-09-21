package com.byrski.common.utils;

import com.byrski.domain.entity.dto.RentItem;
import com.byrski.domain.entity.dto.RentOrder;
import com.byrski.domain.entity.vo.response.RentInfo;
import com.byrski.infrastructure.mapper.impl.ActivityMapperService;
import com.byrski.infrastructure.mapper.impl.ActivityTemplateMapperService;
import com.byrski.infrastructure.mapper.impl.RentItemMapperService;
import com.byrski.infrastructure.mapper.impl.RentOrderMapperService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RentUtils {
    private final RentOrderMapperService rentOrderMapperService;
    private final RentItemMapperService rentItemMapperService;
    private final ActivityMapperService activityMapperService;
    private final ActivityTemplateMapperService activityTemplateMapperService;

    public RentUtils(RentOrderMapperService rentOrderMapperService, RentItemMapperService rentItemMapperService, ActivityMapperService activityMapperService, ActivityTemplateMapperService activityTemplateMapperService) {
        this.rentOrderMapperService = rentOrderMapperService;
        this.rentItemMapperService = rentItemMapperService;
        this.activityMapperService = activityMapperService;
        this.activityTemplateMapperService = activityTemplateMapperService;
    }

    public List<RentInfo> getRentInfoByTradeId(Long tradeId) {
        List<RentOrder> rentOrders = rentOrderMapperService.getByTradeId(tradeId);
        List<RentInfo> rentInfos = new ArrayList<>();
        if (rentOrders != null && !rentOrders.isEmpty()) {
            for (RentOrder rentOrder : rentOrders) {
                RentItem rentItem = rentItemMapperService.getById(rentOrder.getRentItemId());
                RentInfo rentInfo = RentInfo.builder()
                        .rentOrderId(rentOrder.getId())
                        .rentItemId(rentItem.getId())
                        .name(rentItem.getName())
                        .price(rentItem.getPrice())
                        .deposit(rentItem.getDeposit())
                        .days(rentOrder.getRentDay())
                        .build();
                rentInfos.add(rentInfo);
            }
        }
        return rentInfos;
    }

    public Map<Long, List<RentInfo>> getRentInfoMapByTradeIds(List<Long> tradeIds) {
        if (tradeIds == null || tradeIds.isEmpty()) {
            return new HashMap<>();
        }

        return tradeIds.stream()
                .collect(Collectors.toMap(
                        tradeId -> tradeId,
                        this::getRentInfoByTradeId
                ));
    }

    public List<RentInfo> getRentInfoByActivityId(Long activityId) {
        List<RentItem> rentItems = rentItemMapperService.getByActivityId(activityId);
        List<RentInfo> rentInfos = new ArrayList<>();
        Integer days = activityTemplateMapperService.get(activityMapperService.get(activityId).getActivityTemplateId()).getDurationDays();
        if (rentItems!= null &&!rentItems.isEmpty()) {
            for (RentItem rentItem : rentItems) {
                RentInfo rentInfo = RentInfo.builder()
                        .rentItemId(rentItem.getId())
                        .name(rentItem.getName())
                        .price(rentItem.getPrice())
                        .deposit(rentItem.getDeposit())
                        .days(days)
                        .build();
                rentInfos.add(rentInfo);
            }
        }
        return rentInfos;
    }
}
