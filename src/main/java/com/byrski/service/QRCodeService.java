package com.byrski.service;

import com.byrski.domain.entity.vo.response.RentInfo;

import java.util.List;

// QR码业务接口
public interface QRCodeService {
    String encryptInfo(Long tradeId) throws Exception;
    List<RentInfo> decryptInfo(String encryptedToken) throws Exception;
}