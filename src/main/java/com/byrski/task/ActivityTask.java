package com.byrski.task;

import com.byrski.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class ActivityTask {

    @Autowired
    private ActivityService activityService;

    /**
     * 定时任务，当活动到达第一截止时期时，对活动下的所有订单进行锁定操作；上车点无效的订单会变为待确认上车点
     */
    @Scheduled(cron = "* * 20 * * ?")
    public void activityDeadLineCheck() {
        activityService.activityDeadLineCheck();
    }

    /**
     * 定时任务，当活动到达第二截止时期时，对待确认上车点的订单尝试进行锁定操作；若仍未选择有效上车点，则取消订单，同时开放拼房和选车
     */
//    @Scheduled(cron = "0 30 20 * * ?")
//    public void activityLockedCheck() {
//        activityService.activityLockedCheck();
//    }

    /**
     * 到达活动开始当天，进行自动分车和自动拼房
     */
    @Scheduled(cron = "* * 0 * * ?")
    public void activityStartCheck() {
        activityService.activityBeginCheck();
    }
}
