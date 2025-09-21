package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.School;
import com.byrski.infrastructure.mapper.SchoolMapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Component;

@Component
public class SchoolMapperService extends ServiceImpl<SchoolMapper, School> {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    public School addSchool(School school) {
        this.save(school);
        return school;
    }
}
