package com.byrski.domain.entity;

import com.byrski.domain.enums.ReturnCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public record RestBean<T> (int code, T data, String message) {

    // 创建一个配置好的Gson实例，设置序列化null值
    private static final Gson gson = new GsonBuilder()
            .serializeNulls()  // 相当于 FastJson 的 WriteNulls
            .create();

    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(ReturnCode.SUCCESS.getCode(), data, "Request success");
    }

    public static <T> RestBean<T> success() {
        return success(null);
    }

    public static <T> RestBean<T> unAuthorized(String message) {
        return new RestBean<>(ReturnCode.UNAUTHORIZED.getCode(), null, message);
    }

    public static <T> RestBean<T> forbidden(String message) {
        return new RestBean<>(ReturnCode.FORBIDDEN.getCode(), null, message);
    }

    public static <T> RestBean<T> failure(int code, String message) {
        return new RestBean<>(code, null, message);
    }

    public String asJsonString() {
        return gson.toJson(this);
    }
}