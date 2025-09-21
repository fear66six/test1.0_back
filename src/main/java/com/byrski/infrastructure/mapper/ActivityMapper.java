package com.byrski.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.byrski.domain.entity.dto.Activity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
}
