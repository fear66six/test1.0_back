package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.Activity;
import com.byrski.domain.entity.dto.Bus;
import com.byrski.domain.entity.dto.BusMove;
import com.byrski.domain.entity.dto.Trade;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TradeWithDetail {

    private Trade trade;
    private StationWithInfo stationWithInfo;
    private Bus bus;
    private BusMove busMove;
    private UserInfoVo userInfoVo;
    private Activity activity;
    private List<RentInfo> rentInfos;
    private String activityName;
    private RoomTicket roomTicket;
}
