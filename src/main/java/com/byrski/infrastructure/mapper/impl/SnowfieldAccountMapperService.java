package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.SnowfieldAccount;
import com.byrski.infrastructure.mapper.SnowfieldAccountMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SnowfieldAccountMapperService extends ServiceImpl<SnowfieldAccountMapper, SnowfieldAccount> {
    public List<Long> getMySnowfieldIds(Long userId) {
        return this.lambdaQuery().eq(SnowfieldAccount::getUserId, userId).list().stream().map(SnowfieldAccount::getSnowfieldId).toList();
    }
}
