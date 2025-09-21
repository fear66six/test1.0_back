package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.TutorialImage;
import com.byrski.infrastructure.mapper.TutorialImageMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TutorialImageMapperService extends ServiceImpl<TutorialImageMapper, TutorialImage> {
    public List<TutorialImage> getByTutorialIdSortByIndex(Long id) {
        return this.lambdaQuery().eq(TutorialImage::getTutorialId, id).orderByAsc(TutorialImage::getNumber).list();
    }
}
