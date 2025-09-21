package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.Room;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomVo {
    private Room room;
    private List<UserInfoVo> members;
    private Integer status;
}
