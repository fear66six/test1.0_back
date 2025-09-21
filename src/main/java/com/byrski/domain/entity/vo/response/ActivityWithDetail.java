package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.Activity;
import com.byrski.domain.entity.dto.ActivityTemplate;
import com.byrski.domain.entity.dto.Area;
import com.byrski.domain.entity.dto.Snowfield;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityWithDetail {
    private Activity activity;
    private ActivityTemplate activityTemplate;
    private Snowfield snowfield;
    private Area area;
}
