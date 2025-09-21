package com.byrski.service;

import com.byrski.domain.entity.dto.Snowfield;
import com.byrski.domain.entity.vo.response.ActivityDateVo;
import com.byrski.domain.entity.vo.response.SnowfieldBriefVo;
import com.byrski.domain.entity.vo.response.SnowfieldDetailVo;

import java.util.List;
import java.util.Map;

public interface SnowfieldService {

    /**
     * 获取滑雪场简要信息列表
     * @return 滑雪场简要信息列表
     */
    List<SnowfieldBriefVo> listSnowfieldBrief();

    /**
     * 获取滑雪场详细信息，包含该雪场下所有活动的所有门票
     * @param snowfieldId 滑雪场ID
     * @return 滑雪场详细信息
     */
    SnowfieldDetailVo getSnowfieldDetail(Long snowfieldId, List<Long> activityIds);

    List<Snowfield> listSnowfield();

    void updateSnowfield(Snowfield snowfield);

    List<ActivityDateVo> getActivityBeginDate(Long snowfieldId);
}
