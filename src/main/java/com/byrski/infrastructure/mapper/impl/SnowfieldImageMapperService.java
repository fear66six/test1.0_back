package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.SnowfieldImage;
import com.byrski.infrastructure.mapper.SnowfieldImageMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SnowfieldImageMapperService extends ServiceImpl<SnowfieldImageMapper, SnowfieldImage> {
    public List<SnowfieldImage> getImagesById(Long snowfieldId) {
        List<SnowfieldImage> list = lambdaQuery().eq(SnowfieldImage::getSnowfieldId, snowfieldId).list();
        for (SnowfieldImage snowfieldImage : list) {
            snowfieldImage.setUrl(snowfieldImage.getUrl());
        }
        return list;
    }
}
