package com.byrski.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byrski.domain.entity.dto.WechatTradeDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WechatTradeDetailMapper extends BaseMapper<WechatTradeDetail> {
}
