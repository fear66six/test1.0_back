package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.vo.request.BusStatusVo;
import com.byrski.domain.entity.vo.response.HeadCountVo;
import com.byrski.domain.entity.vo.response.TicketCheckHeadCountVo;
import com.byrski.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leader")
@Slf4j
public class LeaderController extends AbstractController {

    private final TradeService tradeService;

    public LeaderController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/head/count")
    public RestBean<HeadCountVo> getHeadCount(
            @RequestParam Long tradeId,
            @RequestParam String route,
            @RequestParam(required = false, defaultValue = "-1") Long stationId
    ) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public HeadCountVo doInTransactionWithResult(Long tradeId) {
                return tradeService.getHeadCount(tradeId, route, stationId);
            }
        });
    }

    @GetMapping("/ticket/checked")
    public RestBean<TicketCheckHeadCountVo> getCheckedPassenger(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public TicketCheckHeadCountVo doInTransactionWithResult(Long tradeId) {
                return tradeService.getCheckedPassenger(tradeId);
            }
        });
    }

    @PatchMapping("/status")
    public RestBean<Void> updateBusStatus(@RequestBody @Validated BusStatusVo busStatusVo) {
        return handleRequest(busStatusVo, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            public void doInTransactionWithoutResult(BusStatusVo busStatusVo) throws Exception {
                tradeService.updateBusStatus(busStatusVo);
            }
        });
    }

    @PatchMapping("/go")
    public RestBean<Void> go(@RequestParam Long busId, @RequestParam Long stationId) {
        return handleRequest(busId, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            public void doInTransactionWithoutResult(Long busId) throws Exception {
                tradeService.goFinished(busId, stationId);
            }
        });
    }

//    @PatchMapping("/arrive")
//    public RestBean<Void> arrive(@RequestParam Long busId) {
//        return handleRequest(busId, log, new ExecuteCallbackWithoutResult<>() {
//            @Override
//            public void doInTransactionWithoutResult(Long busId) throws Exception {
//                tradeService.arriveFinished(busId);
//            }
//        });
//    }
//
//    @PatchMapping("/ski")
//    public RestBean<Void> ski(@RequestParam Long busId) {
//        return handleRequest(busId, log, new ExecuteCallbackWithoutResult<>() {
//            @Override
//            public void doInTransactionWithoutResult(Long busId) throws Exception {
//                tradeService.skiFinished(busId);
//            }
//        });
//    }
//
//    @PatchMapping("/return")
//    public RestBean<Void> returnFinished(@RequestParam Long busId) {
//        return handleRequest(busId, log, new ExecuteCallbackWithoutResult<>() {
//            @Override
//            public void doInTransactionWithoutResult(Long busId) throws Exception {
//                tradeService.returnFinished(busId);
//            }
//        });
//    }
}
