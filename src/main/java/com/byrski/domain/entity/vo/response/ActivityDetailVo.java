package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.Doc;
import com.byrski.domain.entity.dto.SnowfieldImage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ActivityDetailVo {

    private String location;
    private String cover;
    private List<SnowfieldImage> images;
    private String name;
    private Doc detail;
    private Doc schedule;
    private Doc attention;
    @JsonProperty("isAvailable")
    private boolean isAvailable;
    @JsonProperty("isValid")
    private boolean isValid;
    private LocalDateTime signupDdlDate;
}