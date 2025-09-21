package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.Activity;
import com.byrski.domain.entity.vo.response.FromDate2Date;
import com.byrski.domain.enums.ActivityStatus;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.ActivityMapper;
import com.byrski.domain.enums.ReturnCode;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ActivityMapperService extends ServiceImpl<ActivityMapper, Activity> {

    public FromDate2Date getFromDate2Date(Long activityId) {
        // 执行查询，获取活动数据
        Activity activity = this.get(activityId);
        LocalDate beginDate = activity.getActivityBeginDate();
        LocalDate endDate = activity.getActivityEndDate();

        // 日期和星期的格式化
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM月dd日");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("E", Locale.CHINESE);

        String date = beginDate.format(dateFormatter) + "-" + endDate.format(dateFormatter);
        String day = beginDate.format(dayFormatter) + "-" + endDate.format(dayFormatter);

        // 返回 FromDate2Date 实例
        return new FromDate2Date(date, day);
    }

    public Activity get(Long activityId) {
        Activity activity = this.getById(activityId);
        if (activity == null) {
            throw new ByrSkiException(ReturnCode.ACTIVITY_NOT_EXIST);
        }
        return activity;
    }


    public boolean addCurrentParticipant(Long activityId) {
        return this.update().eq("id", activityId).setSql("current_participant = current_participant + 1").update();
    }

    public boolean subCurrentParticipant(Long activityId) {
        return this.update().eq("id", activityId).setSql("current_participant = current_participant - 1").update();
    }

    /**
     * 获取报名截止日期不早于今天的活动列表
     * @param date 今天的日期
     * @return 活动列表
     */
    public List<Activity> getRegistrationDeadlineActivities(LocalDateTime date) {
        return this.lambdaQuery()
                .le(Activity::getSignupDdlDate, date)  // 小于等于date
                .eq(Activity::getStatus, ActivityStatus.ACTIVE.getCode())            // status为ACTIVE
                .list();
    }

    public List<Activity> getLockedActivities(LocalDateTime date) {
        return this.lambdaQuery()
                .le(Activity::getLockDdlDate, date)  // 小于等于date
                .eq(Activity::getStatus, ActivityStatus.DEADLINE_NOT_LOCKED.getCode())            // status为DEADLINE_NOT_LOCKED
                .list();
    }

    public List<Activity> getBeginActivities(LocalDate date) {
        return this.lambdaQuery()
                .le(Activity::getActivityBeginDate, date)  // 小于等于date
                .eq(Activity::getStatus, ActivityStatus.LOCKED.getCode())            // status为LOCKED
                .list();
    }

    public List<Activity> getHomePageActivities(LocalDate date) {
        List<Activity> list = this.lambdaQuery()
                .ge(Activity::getActivityEndDate, date) // 当前日期早于 activity_end_date
                .in(Activity::getStatus, ActivityStatus.ACTIVE)
                .list();
        if (list.isEmpty()) {
            return this.lambdaQuery()
                    .ne(Activity::getStatus, ActivityStatus.EDITING.getCode())
                    .orderByDesc(Activity::getCreateTime)
                    .list();
        }
        return list;
    }

    @Options(useGeneratedKeys = true, keyProperty = "id")
    public Activity addActivity(Activity activity) {
        this.save(activity);
        return activity;
    }

    public Activity updateActivity(Activity activity) {
        return this.updateById(activity)? activity : null;
    }

    public List<Activity> getActivityListByActivityTemplateId(Long activityTemplateId) {
        return this.lambdaQuery().eq(Activity::getActivityTemplateId, activityTemplateId).list();
    }

    public List<Activity> listByUserId(Long userId) {
        return this.lambdaQuery().eq(Activity::getUserId, userId).list();
    }

    public List<Activity> listBySnowfieldId(Long snowfieldId) {
        return this.lambdaQuery().eq(Activity::getSnowfieldId, snowfieldId).eq(Activity::getStatus, ActivityStatus.ACTIVE.getCode()).list();
    }

    public List<Long> getMyActivityIds(Long userId) {
        return this.lambdaQuery().eq(Activity::getUserId, userId).list().stream().map(Activity::getId).collect(Collectors.toList());
    }

    public Map<Long, Activity> getByIds(Set<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Map.of();
        }
        return this.lambdaQuery().in(Activity::getId, activityIds).list().stream().collect(Collectors.toMap(Activity::getId, activity -> activity));
    }

    public void publishActivity(Long activityId) {
        this.lambdaUpdate().eq(Activity::getId, activityId).set(Activity::getStatus, ActivityStatus.ACTIVE.getCode()).update();
    }

    public List<Activity> getActiveActivityListBySnowfieldId(Long snowfieldId) {
        return this.lambdaQuery().eq(Activity::getSnowfieldId, snowfieldId).in(Activity::getStatus, ActivityStatus.ACTIVE).list();
    }
}
