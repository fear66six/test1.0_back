package com.byrski.service;

import com.byrski.domain.entity.dto.Activity;
import com.byrski.domain.entity.dto.ActivityTemplate;
import com.byrski.domain.entity.vo.response.ActivityDetailVo;
import com.byrski.domain.entity.vo.response.HomePageVo;
import com.byrski.domain.entity.vo.response.Place;

import java.util.List;
import java.util.Map;

public interface ActivityService {
    ActivityDetailVo getActivityDetail(Long activityId);

    Map<String, List<Place>> listStationByActivityId(Long activityId);

    List<Activity> getActivityList();

    List<ActivityTemplate> getActivityTemplateList();

    void updateActivityTemplate(ActivityTemplate activityTemplate);

    void updateActivity(Activity activity);

    void activityDeadLineCheck();

    void TestDead(Long activityId);
    void TestBegin(Long activityId);
    void activityLockedCheck();

    void TestLock(Long activityId);

    HomePageVo getHomePage();

    void activityBeginCheck();
}
