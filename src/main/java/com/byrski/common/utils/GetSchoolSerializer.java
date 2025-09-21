package com.byrski.common.utils;

import com.byrski.infrastructure.mapper.impl.SchoolMapperService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GetSchoolSerializer extends JsonSerializer<Long> {

    @Autowired
    private SchoolMapperService schoolMapperService;

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String schoolName = schoolMapperService.getById(value).getName();
        gen.writeString(schoolName);
    }
}
