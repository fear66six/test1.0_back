package com.byrski.service.impl;

import com.byrski.common.exception.ByrSkiException;
import com.byrski.common.utils.InfoUtils;
import com.byrski.common.utils.RentUtils;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.entity.dto.ActivityTemplate;
import com.byrski.domain.entity.dto.Product;
import com.byrski.domain.entity.dto.ServiceButton;
import com.byrski.domain.entity.dto.Snowfield;
import com.byrski.domain.entity.vo.response.ProductDetailVo;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.domain.enums.TicketType;
import com.byrski.infrastructure.mapper.impl.*;
import com.byrski.infrastructure.repository.manager.ProductManager;
import com.byrski.service.ProductService;
import com.byrski.strategy.factory.TicketStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ProductServiceImpl implements ProductService {
    private final ProductManager productManager;
    private final ActivityTemplateMapperService activityTemplateMapperService;
    private final ServiceButtonMapperService serviceButtonMapperService;
    private final InfoUtils infoUtils;
    private final SnowfieldMapperService snowfieldMapperService;
    private final ActivityMapperService activityMapperService;
    private final RentUtils rentUtils;
    private final TicketStrategyFactory ticketStrategyFactory;

    public ProductServiceImpl(ProductManager productManager,
                              ActivityTemplateMapperService activityTemplateMapperService,
                              ServiceButtonMapperService serviceButtonMapperService,
                              InfoUtils infoUtils,
                              SnowfieldMapperService snowfieldMapperService,
                              ActivityMapperService activityMapperService,
                              RentUtils rentUtils,
                              TicketStrategyFactory ticketStrategyFactory) {
        this.productManager = productManager;
        this.activityTemplateMapperService = activityTemplateMapperService;
        this.serviceButtonMapperService = serviceButtonMapperService;
        this.infoUtils = infoUtils;
        this.snowfieldMapperService = snowfieldMapperService;
        this.activityMapperService = activityMapperService;
        this.rentUtils = rentUtils;
        this.ticketStrategyFactory = ticketStrategyFactory;
    }

    /**
     * 获取一个产品的详情，该接口用于在用户下单页面展示数据，需要一并展示该活动下的所有附加内容
     * @param productId 产品id
     * @return 产品详情
     */
    @Override
    public ProductDetailVo getProductDetail(String productId) {
        Product product = productManager.getProductById(productId);
        if (product == null) {
            throw new ByrSkiException(ReturnCode.PRODUCT_NOT_EXIST);
        }
        Long activityId = product.getActivityId();
        Long activityTemplateId = product.getActivityTemplateId();
        Long snowfieldId = product.getSnowfieldId();
        ActivityTemplate activityTemplate = activityTemplateMapperService.get(activityTemplateId);
        Snowfield snowfield = snowfieldMapperService.get(snowfieldId);
        List<ServiceButton> serviceButtons = serviceButtonMapperService.list();
        List<BaseTicketEntity> roomTickets = ticketStrategyFactory.getStrategy(TicketType.ROOM).getTicketsByActivityId(activityId);
        return ProductDetailVo.builder()
                .product(ProductDetailVo.Product.builder()
                        .productId(product.getId())
                        .activityId(activityId)
                        .activityTemplateId(activityTemplateId)
                        .activityName(activityTemplate.getName())
                        .name(product.getName())
                        .description(product.getDescription())
                        .type(product.getType())
                        .isStudent(product.getIsStudent())
                        .price(product.getPrice())
                        .originalPrice(product.getOriginalPrice())
                        .fromDate2Date(activityMapperService.getFromDate2Date(activityId))
                        .cover(snowfield.getCover())
                        .serviceButtons(serviceButtons)
                        .rentInfos(rentUtils.getRentInfoByActivityId(activityId))
                        .roomTickets(roomTickets)
                        .tickets(product.getTickets())
                        .build())
                .user(
                        infoUtils.getUserInfoVo()
                )
                .build();

    }
}
