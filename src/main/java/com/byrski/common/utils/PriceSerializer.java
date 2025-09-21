package com.byrski.common.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

// 自定义序列化器
public class PriceSerializer extends JsonSerializer<Integer> {
    @Override
    public void serialize(Integer value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 将分转换为元，并格式化为字符串
        String priceInYuan = String.format("%.2f", value / 100.0);
        gen.writeString(priceInYuan);
    }
}