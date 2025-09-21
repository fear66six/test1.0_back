package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.Station;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.domain.enums.StationStatus;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.StationMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StationMapperService extends ServiceImpl<StationMapper, Station> {
    public List<Station> getByActivityId(Long activityId) {
        return this.lambdaQuery()
                .eq(Station::getActivityId, activityId)
                .ne(Station::getStatus, StationStatus.INVALID.getCode())
                .list();
    }

    public Station getByStationInfoAndActivityId(Long stationInfoId, Long activityId) {
        return this.lambdaQuery()
              .eq(Station::getStationInfoId, stationInfoId)
              .eq(Station::getActivityId, activityId)
              .ne(Station::getStatus, StationStatus.INVALID.getCode())
              .one();
    }

    public Station get(Long stationId) {
        Station station = this.getById(stationId);
        if (station == null) {
            throw new ByrSkiException(ReturnCode.STATION_NOT_EXIST);
        }
        return station;
    }

    public boolean addChoicePeopleNum(Long stationId, Integer num) {
        return this.update().eq("id", stationId).setSql("choice_peoplenum = choice_peoplenum + " + num).update();
    }

    public boolean addChoicePeopleNum(Long stationId) {
        return this.addChoicePeopleNum(stationId, 1);
    }

    public boolean subChoicePeopleNum(Long stationId) {
        return this.update().eq("id", stationId).setSql("choice_peoplenum = choice_peoplenum - 1").update();
    }

    /**
     * 获取某一活动在某一地区的所有站点信息
     * @param activityId 活动id
     * @param areaId 地区id
     * @return 站点信息列表
     */
    public List<Station> getStationsByActivityIdAndAreaId(Long activityId, Long areaId) {
        return this.lambdaQuery()
             .eq(Station::getActivityId, activityId)
             .eq(Station::getAreaId, areaId)
             .list();
    }

    public List<Station> getInvalidStationByActivityId(Integer activityId) {
        return this.lambdaQuery()
                .eq(Station::getActivityId, activityId)
                .apply("choice_peoplenum < target_peoplenum")
                .list();
    }

    /**
     * 将某一活动中所有未达到目标人数的站点状态设置为无效
     * @param activityId 活动id
     * @return 是否更新成功
     */
    public boolean updateInvalidStationByActivityId(Long activityId) {
        return this.lambdaUpdate()
                .eq(Station::getActivityId, activityId)
                .apply("choice_peoplenum < target_peoplenum")
                .set(Station::getStatus, StationStatus.INVALID.getCode())  // 设置状态为无效
                .update();
    }

    /**
     * 获取某一活动中所有站点的人数信息
     * @param activityId 活动id
     * @return 站点id->人数 的映射
     */
    public Map<Long, Integer> getStationPeopleNumByActivityId(Integer activityId) {
        List<Station> list = this.lambdaQuery()
                .eq(Station::getActivityId, activityId)
                .ne(Station::getStatus, StationStatus.INVALID.getCode())
                .list();
        return list.stream().collect(
                java.util.stream.Collectors.toMap(Station::getStationInfoId, Station::getChoicePeopleNum));

    }

    /**
     * 获取某一活动中所有站点的人数信息，按照地区分组
     * @param activityId 活动id
     * @return 地区id->站点id->人数 的映射
     */
    public Map<Long, Map<Long, Integer>> getStationPeopleNumByActivityIdGroupByArea(Long activityId) {
        List<Station> list = this.lambdaQuery()
                .eq(Station::getActivityId, activityId)
                .ne(Station::getStatus, StationStatus.INVALID.getCode())
                .list();

        // 按照areaId分组，然后对每组创建stationId->peopleNum的映射
        return list.stream()
                .collect(Collectors.groupingBy(
                        Station::getAreaId,
                        Collectors.toMap(
                                Station::getId,
                                Station::getChoicePeopleNum
                        )
                ));
    }

    public List<Station> getStationListByActivityId(Long id) {
        return this.lambdaQuery().eq(Station::getActivityId, id).list();
    }

    public List<Long> getStationIdsByStationInfoId(Long stationInfoId) {
        return this.lambdaQuery()
                .eq(Station::getStationInfoId, stationInfoId)
                .list()
                .stream()
                .map(Station::getId)
                .collect(Collectors.toList());
    }

    public Map<Long, Station> getByIds(Set<Long> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return Map.of();
        }
        return this.lambdaQuery()
                .in(Station::getId, stationIds)
                .list()
                .stream()
                .collect(Collectors.toMap(Station::getId, station -> station));
    }
}
