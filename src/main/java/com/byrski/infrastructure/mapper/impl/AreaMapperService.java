package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.Area;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.AreaMapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Component;

@Component
public class AreaMapperService extends ServiceImpl<AreaMapper, Area> {

    public String getAreaNameById(Long areaId) {
        return this.lambdaQuery().eq(Area::getId, areaId).one().getAreaName();
    }

    public Area get(Long areaId) {
        Area area = this.getById(areaId);
        if (area == null) {
            throw new ByrSkiException(ReturnCode.AREA_NOT_EXIST);
        }
        return area;
    }

    @Options(useGeneratedKeys = true, keyProperty = "id")
    public Area addArea(Area area) {
        this.save(area);
        return area;
    }
}
