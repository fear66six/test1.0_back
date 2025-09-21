package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.Tutorial;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.TutorialMapper;
import org.springframework.stereotype.Component;

@Component
public class TutorialMapperService extends ServiceImpl<TutorialMapper, Tutorial> {

    public Tutorial get(Long id) {
        if (id == null) {
            throw new ByrSkiException(ReturnCode.TUTORIAL_ID_NOT_EXIST);
        }
        Tutorial tutorial = this.getById(id);
        if (tutorial == null) {
            throw new ByrSkiException(ReturnCode.TUTORIAL_ID_NOT_EXIST);
        }
        return tutorial;
    }
}
