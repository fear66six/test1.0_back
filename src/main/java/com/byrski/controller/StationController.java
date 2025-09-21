package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.dto.Bus;
import com.byrski.domain.entity.vo.request.ChooseBusVo;
import com.byrski.domain.entity.vo.request.ChooseStationVo;
import com.byrski.domain.entity.vo.response.AlternativeStationVo;
import com.byrski.domain.entity.vo.response.Place;
import com.byrski.service.ActivityService;
import com.byrski.service.StationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/station")
public class StationController extends AbstractController {

    private final ActivityService activityService;
    private final StationService stationService;

    public StationController(
            ActivityService activityService,
            StationService stationService
    ) {
        this.activityService = activityService;
        this.stationService = stationService;
    }


    @GetMapping("/list")
    public RestBean<Map<String, List<Place>>> listStation(@RequestParam Long activityId ) {
        return handleRequest(activityId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Map<String, List<Place>> doInTransactionWithResult(Long activityId) {
                return activityService.listStationByActivityId(activityId);
            }
        });
    }

    @GetMapping("/alternative")
    public RestBean<List<AlternativeStationVo>> getAlternativeBus(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public List<AlternativeStationVo> doInTransactionWithResult(Long tradeId) {
                return stationService.getAlternativeBus(tradeId);
            }
        });
    }

    @PatchMapping("/choose")
    public RestBean<Void> chooseStation(@RequestBody ChooseStationVo chooseStationVo) {
        return handleRequest(chooseStationVo, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            public void doInTransactionWithoutResult(ChooseStationVo chooseStationVo) {
                stationService.chooseStation(chooseStationVo);
            }
        });
    }

    @GetMapping("/bus/list")
    public RestBean<List<Bus>> getStationBusList(@RequestParam Long stationId) {
        return handleRequest(stationId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public List<Bus> doInTransactionWithResult(Long stationId) {
                return stationService.getStationBusList(stationId);
            }
        });
    }

    @PutMapping("/bus/choose")
    public RestBean<Boolean> chooseBus(@RequestBody ChooseBusVo chooseBusVo) {
        return handleRequest(chooseBusVo, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(ChooseBusVo vo) {
                return stationService.chooseBus(vo);
            }
        });
    }
}
