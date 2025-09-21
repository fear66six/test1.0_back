package com.byrski.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byrski.domain.entity.dto.CouponUserReceivable;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券用户可领取关联表 Mapper 接口
 */
@Mapper
public interface CouponUserReceivableMapper extends BaseMapper<CouponUserReceivable> {
}
