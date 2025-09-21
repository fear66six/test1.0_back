package com.byrski.domain.entity.vo.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ActivityDateVo {
    private String date;
    private String day;
    private List<String> activityIds;
}
