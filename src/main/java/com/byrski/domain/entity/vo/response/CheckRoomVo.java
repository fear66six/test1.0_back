package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.Account;
import com.byrski.domain.entity.dto.Room;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckRoomVo {
    private Room room;
    private List<Account> members;
    private Integer status;
}
