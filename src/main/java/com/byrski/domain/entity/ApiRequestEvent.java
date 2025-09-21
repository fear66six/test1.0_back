package com.byrski.domain.entity;

import lombok.Data;

@Data
public class ApiRequestEvent {
    private String path;
    private String method;
    private String ip;
    private Long userId;
    private Long timestamp;
}