package com.byrski.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.byrski.domain.entity.dto.Trade;
import com.byrski.domain.entity.vo.response.RentInfo;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.impl.TradeMapperService;
import com.byrski.service.QRCodeService;
import com.byrski.common.utils.InfoUtils;
import com.byrski.common.utils.JwtUtils;
import com.byrski.common.utils.RentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// QR码业务实现类
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class QRCodeServiceImpl implements QRCodeService {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private TradeMapperService tradeMapperService;
    @Autowired
    private RentUtils rentUtils;
    @Autowired
    private InfoUtils infoUtils;

    /**
     * 加密业务实现，查询trade，检查是否已验票，返回加密token
     * @param tradeId 交易ID
     * @return 加密token
     */
    @Override
    public String encryptInfo(Long tradeId) {
        Trade trade = tradeMapperService.getById(tradeId);
        if (trade == null) {
            throw new ByrSkiException(ReturnCode.TRADE_NOT_EXIST);
        }
        if (trade.getTicketCheck()) {
            throw new ByrSkiException(ReturnCode.TRADE_CHECKED);
        }
        return jwtUtils.createTradeIdJwt(tradeId);
    }

    /**
     * 验票业务实现，解密token，获取tradeId，查询trade，检查是否已验票，设置验票状态，返回租借信息
     * @param encryptedToken 加密token
     * @return 租借信息列表
     */
    @Override
    public List<RentInfo> decryptInfo(String encryptedToken) {
        if (encryptedToken == null) {
            throw new ByrSkiException(ReturnCode.TOKEN_EMPTY);
        }
        infoUtils.checkLeader();
        DecodedJWT decodedJWT = jwtUtils.resolveTradeJwt(encryptedToken);
        Long tradeId = jwtUtils.toTradeId(decodedJWT);
        Trade trade = tradeMapperService.getById(tradeId);
        if (trade.getTicketCheck()) {
            throw new ByrSkiException(ReturnCode.TRADE_CHECKED);
        }
        tradeMapperService.setTicketChecked(tradeId);
        return rentUtils.getRentInfoByTradeId(tradeId);
    }


}
