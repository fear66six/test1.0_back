package com.byrski.domain.entity.vo.request;

import com.byrski.domain.entity.BaseTicketEntity;
import lombok.Data;

import java.util.List;

@Data
public class ProductAddVo {
    private String name;
    private String description;
    private Boolean isStudent;
    private List<Long> ticketIds;
}
