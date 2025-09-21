package com.byrski.domain.entity.vo.request;

import lombok.Data;

@Data
public  class UploadImage {
    private String fileName;
    private String fileType;
    private String base64Data;
}