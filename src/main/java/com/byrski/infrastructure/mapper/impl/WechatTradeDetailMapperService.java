package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.Trade;
import com.byrski.domain.entity.dto.WechatTradeDetail;
import com.byrski.domain.entity.vo.response.PaymentVo;
import com.byrski.infrastructure.mapper.WechatTradeDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WechatTradeDetailMapperService extends ServiceImpl<WechatTradeDetailMapper, WechatTradeDetail> {

    @Autowired
    private TradeMapperService tradeMapperService;

    @Value("${wechat.app-id}")
    private String appId;

    public PaymentVo queryPaymentVo(Long tradeId) {
        WechatTradeDetail wechatTradeDetail = query().eq("trade_id", tradeId).one();
        if (wechatTradeDetail == null) {
            return null;
        }
        
        // 获取订单信息以获取用户数量
        Trade trade = tradeMapperService.getById(tradeId);
        Integer userCount = trade != null ? trade.getUserCount() : 1; // 默认为1
        
        return PaymentVo.builder()
                .appId(appId)
                .timeStamp(wechatTradeDetail.getTimestamp())
                .nonceStr(wechatTradeDetail.getNonceStr())
                .packageVal(wechatTradeDetail.getPackageVal())
                .signType(wechatTradeDetail.getSignType())
                .paySign(wechatTradeDetail.getPaySign())
                .tradeId(tradeId)
                .userCount(userCount)
                .build();
    }
}
