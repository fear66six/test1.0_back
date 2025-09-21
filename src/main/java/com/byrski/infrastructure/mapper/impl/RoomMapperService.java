package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.Room;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.RoomMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoomMapperService extends ServiceImpl<RoomMapper, Room> {
    public Room get(Long roomId) {
        Room room = this.getById(roomId);
        if (room == null) {
            throw new ByrSkiException(ReturnCode.ROOM_NOT_EXIST);
        }
        return room;
    }

    public Room getByCode(String code) {
        return lambdaQuery().eq(Room::getCode, code).one();
    }

    public List<Room> getRoomListByActivityId(Long activityId) {
        return lambdaQuery().eq(Room::getActivityId, activityId).list();
    }

    public boolean deleteRoom(Long roomId) {
        return this.removeById(roomId);
    }

    public boolean decreaseMember(Long roomId, Integer peopleNum) {
        return this.lambdaUpdate()
                .eq(Room::getId, roomId)
                .set(Room::getPeopleNum, peopleNum - 1)
                .update();
    }
}
