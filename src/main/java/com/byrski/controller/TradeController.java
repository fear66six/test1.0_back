package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.dto.Room;
import com.byrski.domain.entity.dto.Tutorial;
import com.byrski.domain.entity.vo.request.EncryptedTokenVo;
import com.byrski.domain.entity.vo.request.JoinRoomVo;
import com.byrski.domain.entity.vo.request.OrderVo;
import com.byrski.domain.entity.vo.request.SkiChoiceVo;
import com.byrski.domain.entity.vo.response.*;
import com.byrski.service.QRCodeService;
import com.byrski.service.TradeService;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/trade")
public class TradeController extends AbstractController{

    private final TradeService tradeService;
    private final QRCodeService qrCodeService;

    public TradeController(TradeService tradeService, QRCodeService qrCodeService) {
        this.tradeService = tradeService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/order")
    public RestBean<PaymentVo> makeOrder(@RequestBody OrderVo order) {
        return handleRequest(order, log, new ExecuteCallbackWithResult<>() {
            @Override
            public PaymentVo doInTransactionWithResult(OrderVo order) {
                return tradeService.makeOrder(order);
            }
        });
    }

    @DeleteMapping("/delete")
    public RestBean<Boolean> deleteTrade(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long tradeId) {
                return tradeService.deleteTrade(tradeId);
            }
        });
    }

    @GetMapping("/itinerary/card")
    public RestBean<ItineraryCardVo> getItineraryCard(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public ItineraryCardVo doInTransactionWithResult(Long tradeId) {
                return tradeService.getItineraryCard(tradeId);
            }
        });
    }

    @GetMapping("/itinerary/list")
    public RestBean<ItineraryListVo> getItineraryList() {
        return handleRequest(null, log, new ExecuteCallbackWithResult<Void, ItineraryListVo>() {
            @Override
            public ItineraryListVo doInTransactionWithResult(Void aVoid) {
                return tradeService.getItineraryList();
            }
        });
    }

    @PatchMapping("/itinerary/upgrade")
    public RestBean<Void> upgradeItinerary(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(Long tradeId) {
                tradeService.upgradeItinerary(tradeId);
            }
        });
    }

    /**
     * 获取行程详情，根据用户信息区分返回数据内容
     * @param tradeId 订单id
     * @return 行程详情
     */
    @GetMapping("/itinerary/detail")
    public RestBean<ItineraryDetailVo> getItineraryDetail(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public ItineraryDetailVo doInTransactionWithResult(Long tradeId) {
                return tradeService.getItineraryDetail(tradeId);
            }
        });
    }

    @GetMapping("/itinerary/station/list")
    public RestBean<List<ItineraryDetailVo.StationInfo>> leaderGetItineraryStationInfoList(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public List<ItineraryDetailVo.StationInfo> doInTransactionWithResult(Long tradeId) {
                return tradeService.leaderGetItineraryStationInfoList(tradeId);
            }
        });
    }

    /**
     * 取消订单（支持单用户和多用户订单）
     * @param tradeId 订单id
     * @return 退款信息
     */
    @PostMapping("/cancel")
    public RestBean<Refund> cancel(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Refund doInTransactionWithResult(Long tradeId) {
                return tradeService.cancelTrade(tradeId);
            }
        });
    }

    /**
     * 设置为已经上车
     * @param tradeId 订单id
     * @return 是否成功
     */
    @PatchMapping("/boarded/departure")   // 去程上车
    public RestBean<Void> setDepartureBoarded(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(Long tradeId) {
                tradeService.setDepartureBoarded(tradeId);
            }
        });
    }

    @PatchMapping("/boarded/return")      // 返程上车
    public RestBean<Void> setReturnBoarded(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(Long tradeId) {
                tradeService.setReturnBoarded(tradeId);
            }
        });
    }

    @GetMapping("/guide")
    public RestBean<Tutorial> getTutorial(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Tutorial doInTransactionWithResult(Long tradeId) {
                return tradeService.getTutorial(tradeId);
            }
        });
    }

    @PatchMapping("/guide/next")
    public RestBean<Tutorial> nextTutorial(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Tutorial doInTransactionWithResult(Long tradeId) {
                return tradeService.nextTutorial(tradeId);
            }
        });
    }

    @PatchMapping("/guide/skip")
    public RestBean<Tutorial> skipTutorial(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Tutorial doInTransactionWithResult(Long tradeId) {
                return tradeService.skipTutorial(tradeId);
            }
        });
    }

    @GetMapping("/guide/list")
    public RestBean<List<List<TutorialWithImage>>> getTutorialList(@RequestParam Long tutorialId) {
        return handleRequest(tutorialId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public List<List<TutorialWithImage>> doInTransactionWithResult(Long tutorialId) {
                return tradeService.getTutorialList(tutorialId);
            }
        });
    }

    @GetMapping("/list")
    public RestBean<List<TradeMeta>> getTradeList(@RequestParam(required = false) Integer status) {
        return handleRequest(status, log, new ExecuteCallbackWithResult<>() {
            @Override
            public List<TradeMeta> doInTransactionWithResult(Integer status) {
                return tradeService.getTradeList(status);
            }
        });
    }

    @GetMapping("/detail")
    public RestBean<TradeDetailVo> getTradeDetail(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public TradeDetailVo doInTransactionWithResult(Long tradeId) {
                return tradeService.getTradeDetail(tradeId);
            }
        });
    }



    @GetMapping("/qrcode")
    public RestBean<String> getQRCode(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public String doInTransactionWithResult(Long tradeId) throws Exception {
                return qrCodeService.encryptInfo(tradeId);
            }
        });
    }

    @PostMapping("/qrcode")
    public RestBean<List<RentInfo>> decryptQRCode(@RequestBody EncryptedTokenVo encryptedTokenVo) {
        return handleRequest(encryptedTokenVo, log, new ExecuteCallbackWithResult<>() {
            @Override
            public List<RentInfo> doInTransactionWithResult(EncryptedTokenVo encryptedTokenVo) throws Exception {
                return qrCodeService.decryptInfo(encryptedTokenVo.getEncryptedToken());
            }
        });
    }

    @GetMapping("/checked")
    public RestBean<Boolean> getTicketChecked(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long tradeId) throws Exception {
                return tradeService.getTicketChecked(tradeId);
            }
        });
    }

    @GetMapping("/room/alloc/check")
    public RestBean<CheckRoomVo> checkRoomAlloc(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public CheckRoomVo doInTransactionWithResult(Long tradeId) throws Exception {
                return tradeService.checkRoomAlloc(tradeId);
            }
        });
    }

    @PostMapping("/room/create")
    public RestBean<Room> createRoom(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Room doInTransactionWithResult(Long tradeId) throws Exception {
                return tradeService.createRoom(tradeId);
            }
        });
    }

    @GetMapping("/room/query")
    public RestBean<Room> queryRoom(@RequestParam String code) {
        return handleRequest(code, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Room doInTransactionWithResult(String code) throws Exception {
                return tradeService.queryRoom(code);
            }
        });
    }

    @PostMapping("/room/join")
    public RestBean<Boolean> joinRoom(@RequestBody JoinRoomVo joinRoomVo) {
        return handleRequest(joinRoomVo, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(JoinRoomVo joinRoomVo) throws Exception {
                return tradeService.joinRoom(joinRoomVo);
            }
        });
    }

    @PostMapping("/room/cancel")
    public RestBean<Boolean> cancelRoom(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long tradeId) throws Exception {
                return tradeService.cancelRoom(tradeId);
            }
        });
    }

    @PatchMapping("/ski/choice")
    public RestBean<Void> chooseSki(@RequestBody SkiChoiceVo skiChoiceVo) {
        return handleRequest(skiChoiceVo, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(SkiChoiceVo skiChoiceVo) throws Exception {
                tradeService.chooseSki(skiChoiceVo);
            }
        });
    }

    @PatchMapping("/return/item")
    public RestBean<Void> returnItem(@RequestParam Long tradeId) {
        return handleRequest(tradeId, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(Long tradeId) {
                tradeService.returnItem(tradeId);
            }
        });
    }
}
