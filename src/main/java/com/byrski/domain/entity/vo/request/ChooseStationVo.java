package com.byrski.domain.entity.vo.request;

import lombok.Data;

@Data
public class ChooseStationVo {

    private Long tradeId;
    private Long stationId;

    // 删除选定上车点时同时传入的bus id数据
//    private Integer busId;

}
