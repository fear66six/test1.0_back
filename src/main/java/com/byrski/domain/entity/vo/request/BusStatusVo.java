package com.byrski.domain.entity.vo.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BusStatusVo {
    @NotNull
    private Long id;

    @NotNull
    @Pattern(regexp = "(go|arrive|ski|returned|finish)")
    private String type;

}
