package com.byrski.common.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.common.utils.Const;
import com.byrski.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TradeQueueListener {

    @Autowired
    private TradeService tradeService;

    @RabbitListener(queues = Const.QUEUE_TRADE_DEAD)
    public void handleMessage(String message) throws ByrSkiException {
        JSONObject msg = JSON.parseObject(message);
        try {
            tradeService.cancelExpiredTrade(msg.getString("outTradeNo"));
        } catch (Exception e) {
            log.error("取消订单失败，订单号：{}，{}", msg.getString("outTradeNo"), e.getMessage());
        }
    }
}
