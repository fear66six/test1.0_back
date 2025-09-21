package com.byrski.service.impl;

import com.byrski.common.utils.FormatUtils;
import com.byrski.domain.entity.dto.Activity;
import com.byrski.domain.entity.dto.Product;
import com.byrski.domain.entity.dto.Snowfield;
import com.byrski.domain.entity.vo.response.ActivityDateVo;
import com.byrski.domain.entity.vo.response.ProductWithDateVo;
import com.byrski.domain.entity.vo.response.SnowfieldBriefVo;
import com.byrski.domain.entity.vo.response.SnowfieldDetailVo;
import com.byrski.infrastructure.mapper.impl.ActivityMapperService;
import com.byrski.infrastructure.mapper.impl.ActivityTemplateMapperService;
import com.byrski.infrastructure.mapper.impl.SnowfieldMapperService;
import com.byrski.infrastructure.repository.manager.ProductManager;
import com.byrski.service.SnowfieldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class SnowfieldServiceImpl implements SnowfieldService {

    private final SnowfieldMapperService snowfieldMapperService;
    private final ActivityMapperService activityMapperService;
    private final ActivityTemplateMapperService activityTemplateMapperService;
    private final ProductManager productManager;

    public SnowfieldServiceImpl(
            SnowfieldMapperService snowfieldMapperService,
            ActivityMapperService activityMapperService,
            ActivityTemplateMapperService activityTemplateMapperService,
            ProductManager productManager) {
        this.snowfieldMapperService = snowfieldMapperService;
        this.activityMapperService = activityMapperService;
        this.activityTemplateMapperService = activityTemplateMapperService;
        this.productManager = productManager;
    }

    @Override
    public List<SnowfieldBriefVo> listSnowfieldBrief() {
        List<Snowfield> snowfields = snowfieldMapperService.list();
        if (snowfields == null) {
            return null;
        }
        List<SnowfieldBriefVo> snowfieldBriefVos = new ArrayList<>();
        for (Snowfield snowfield : snowfields) {
            SnowfieldBriefVo snowfieldBriefVo = new SnowfieldBriefVo();
            Long snowfieldId = snowfield.getId();
            List<Long> activityIds = activityMapperService.getActiveActivityListBySnowfieldId(snowfieldId).stream()
                    .map(Activity::getId)
                    .toList();
            if (activityIds.isEmpty()) {
                continue;
            }
            Integer minPrice = productManager.getMinProductPriceByActivityIds(activityIds);
            snowfieldBriefVo.setId(snowfieldId);
            snowfieldBriefVo.setName(snowfield.getName());
            snowfieldBriefVo.setCover(snowfield.getCover());
            snowfieldBriefVo.setIntro(snowfield.getIntro());
            snowfieldBriefVo.setMinPrice(minPrice);
            snowfieldBriefVos.add(snowfieldBriefVo);
        }
        return snowfieldBriefVos;
    }

    @Override
    public SnowfieldDetailVo getSnowfieldDetail(Long snowfieldId, List<Long> activityIds) {
        Snowfield snowfield = snowfieldMapperService.get(snowfieldId);
        List<Product> products = new ArrayList<>();
        if (activityIds == null || activityIds.isEmpty()) {
            activityMapperService.getActiveActivityListBySnowfieldId(snowfieldId).forEach(activity -> {
                List<Product> productsByActivityId = productManager.findProductsByActivityId(activity.getId());
                products.addAll(productsByActivityId);
            });
        } else {
            for (Long activityId : activityIds) {
                List<Product> productsByActivityId = productManager.findProductsByActivityId(activityId);
                products.addAll(productsByActivityId);
            }
        }

        List<ProductWithDateVo> productWithDateVos = new ArrayList<>();
        if (products != null && !products.isEmpty()) {
            for (Product product : products) {
                if (product.getDeprecated()) {
                    continue;
                }
                ProductWithDateVo productWithDateVo = ProductWithDateVo.builder()
                        .id(product.getId())
                        .activityId(product.getActivityId())
                        .activityTemplateId(product.getActivityTemplateId())
                        .snowfieldId(product.getSnowfieldId())
                        .activityName(activityTemplateMapperService.get(product.getActivityTemplateId()).getName())
                        .name(product.getName())
                        .description(product.getDescription())
                        .type(product.getType())
                        .isStudent(product.getIsStudent())
                        .price((product.getPrice()))
                        .fromDate2Date(activityMapperService.getFromDate2Date(product.getActivityId()))
                        .tickets(product.getTickets())
                        .build();
                productWithDateVos.add(productWithDateVo);
            }
        }


        return new SnowfieldDetailVo(
                snowfield.getId(),
                snowfield.getName(),
                snowfield.getCover(),
                snowfield.getIntro(),
                snowfield.getOpeningTime(),
                snowfield.getClosingTime(),
                snowfield.getLocation(),
                snowfield.getSlope(),
                productWithDateVos
        );
    }

    @Override
    public List<Snowfield> listSnowfield() {
        return snowfieldMapperService.list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSnowfield(Snowfield snowfield) {
        snowfieldMapperService.updateById(snowfield);
    }

    @Override
    public List<ActivityDateVo> getActivityBeginDate(Long snowfieldId) {
        List<Activity> activities = activityMapperService.listBySnowfieldId(snowfieldId);
        if (activities == null || activities.isEmpty()) {
            return null;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM月dd日");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("E", Locale.CHINESE);

        Map<LocalDate, List<String>> dateToActivityIds = new TreeMap<>();

        for (Activity activity : activities) {
            LocalDate beginDate = activity.getActivityBeginDate();
            dateToActivityIds.computeIfAbsent(beginDate, k -> new ArrayList<>())
                    .add(activity.getId().toString());
        }

        List<ActivityDateVo> result = new ArrayList<>();

        for (Map.Entry<LocalDate, List<String>> entry : dateToActivityIds.entrySet()) {
            LocalDate date = entry.getKey();
            List<String> activityIds = entry.getValue();

            ActivityDateVo vo = ActivityDateVo.builder()
                    .date(date.format(dateFormatter))
                    .day(date.format(dayFormatter))
                    .activityIds(activityIds)
                    .build();

            result.add(vo);
        }

        return result;
    }
}
