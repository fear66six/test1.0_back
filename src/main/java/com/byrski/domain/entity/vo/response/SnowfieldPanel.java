package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.Area;
import com.byrski.domain.entity.dto.Snowfield;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SnowfieldPanel {
    private Snowfield snowfield;
    private Area area;
}
