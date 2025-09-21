package com.byrski.service.impl;

import com.byrski.domain.entity.dto.School;
import com.byrski.infrastructure.mapper.impl.SchoolMapperService;
import com.byrski.service.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SchoolServiceImpl implements SchoolService {

    private final SchoolMapperService schoolMapperService;

    public SchoolServiceImpl (SchoolMapperService schoolMapperService) {
        this.schoolMapperService = schoolMapperService;
    }

    @Override
    public List<School> listSchool() {
        return schoolMapperService.lambdaQuery()
                .orderByAsc(School::getId)
                .list();
    }
}
