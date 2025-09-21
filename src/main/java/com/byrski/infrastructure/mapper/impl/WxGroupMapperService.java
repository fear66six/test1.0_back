package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.WxGroup;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.WxGroupMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WxGroupMapperService extends ServiceImpl<WxGroupMapper, WxGroup> {

    public WxGroup get(Long wxGroupId) {
        WxGroup wxGroup = this.getById(wxGroupId);
        if (wxGroup == null) {
            throw new ByrSkiException(ReturnCode.WXGROUP_NOT_EXIST);
        }
        return wxGroup;
    }

    /**
     * 根据活动ID获取对应的微信群信息，按照创建时间倒序排序
     * @param activityId 活动ID
     * @return 微信群信息
     */
    public WxGroup getByActivityId(Long activityId) {
        return this.lambdaQuery()
                .eq(WxGroup::getActivityId, activityId)
                .last("LIMIT 1")
                .one();
    }

    public List<WxGroup> getWxGroupListByActivityId(Long id) {
        return this.lambdaQuery().eq(WxGroup::getActivityId, id).list();
    }
}
