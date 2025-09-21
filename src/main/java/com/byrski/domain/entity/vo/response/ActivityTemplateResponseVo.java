package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.Doc;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ActivityTemplateResponseVo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Integer durationDays;
    private List<Doc.Content> leaderNotice;
    private List<Doc.Content> detail;
    private List<Doc.Content> schedule;
    private List<Doc.Content> attention;
    private String notes;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long snowfieldId;
    private String name;
    private String scheduleLite;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
