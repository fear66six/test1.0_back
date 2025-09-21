package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.ActivityTemplate;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.ActivityTemplateMapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ActivityTemplateMapperService extends ServiceImpl<ActivityTemplateMapper, ActivityTemplate> {
    public ActivityTemplate get(Long activityTemplateId) {
        ActivityTemplate activityTemplate = this.getById(activityTemplateId);
        if (activityTemplate == null) {
            throw new ByrSkiException(ReturnCode.ACTIVITY_TEMPLATE_NOT_EXIST);
        }
        return activityTemplate;
    }

    public List<ActivityTemplate> getActivityTemplateListBySnowfieldId(Long snowfieldId) {
        return this.lambdaQuery().eq(ActivityTemplate::getSnowfieldId, snowfieldId).list();
    }

    public List<ActivityTemplate> getActivityTemplateListBySnowfieldIds(List<Long> snowfieldIds) {
        return this.lambdaQuery().in(ActivityTemplate::getSnowfieldId, snowfieldIds).list();
    }

    @Options(useGeneratedKeys = true, keyProperty = "activityTemplateId")
    public ActivityTemplate addActivityTemplate(ActivityTemplate activityTemplate) {
        this.save(activityTemplate);
        return activityTemplate;
    }

    public ActivityTemplate updateActivityTemplate(ActivityTemplate activityTemplate) {
        return this.updateById(activityTemplate)? activityTemplate : null;
    }

    public Map<Long, ActivityTemplate> getByIds(Set<Long> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return Map.of();
        }
        return this.lambdaQuery().in(ActivityTemplate::getId, templateIds).list().stream()
                .collect(Collectors.toMap(ActivityTemplate::getId, activityTemplate -> activityTemplate));
    }
}
