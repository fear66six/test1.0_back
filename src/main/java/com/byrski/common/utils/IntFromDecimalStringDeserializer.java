package com.byrski.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 兼容整数字符串与小数字符串的整型反序列化器。
 * 示例："50" -> 50；"50.00" -> 50；"20.5" -> 21（四舍五入）
 */
public class IntFromDecimalStringDeserializer extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null) {
            return null;
        }
        text = text.trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            if (text.indexOf('.') >= 0) {
                return (int) Math.round(Double.parseDouble(text));
            }
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            // 回退：尝试按double解析后取整
            try {
                return (int) Math.round(Double.parseDouble(text));
            } catch (Exception ignore) {
                throw ex;
            }
        }
    }
}


