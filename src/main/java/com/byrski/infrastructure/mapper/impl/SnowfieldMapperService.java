package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.Snowfield;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.SnowfieldMapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Component;

@Component
public class SnowfieldMapperService extends ServiceImpl<SnowfieldMapper, Snowfield> {

    public Snowfield get(Long snowfieldId) {
        Snowfield snowfield = this.getById(snowfieldId);
        if (snowfield == null) {
            throw new ByrSkiException(ReturnCode.SNOWFIELD_NOT_EXIST);
        }
        return snowfield;
    }

    @Options(useGeneratedKeys = true, keyProperty = "id")
    public Snowfield addSnowfield(Snowfield snowfield) {
        this.save(snowfield);
        return snowfield;
    }
}
