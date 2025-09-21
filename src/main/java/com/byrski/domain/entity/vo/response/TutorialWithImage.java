package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.TutorialImage;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TutorialWithImage {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Integer skiChoice;
    private String title;
    private String subtitle;
    private String content;
    private String videoUrl;
    private String videoTitle;
    private List<TutorialImage> images;
}
