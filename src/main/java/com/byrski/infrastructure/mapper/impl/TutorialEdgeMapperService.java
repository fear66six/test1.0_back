package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.TutorialEdge;
import com.byrski.infrastructure.mapper.TutorialEdgeMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TutorialEdgeMapperService extends ServiceImpl<TutorialEdgeMapper, TutorialEdge> {

    public List<Long> next(Long startVertex) {
         return this.lambdaQuery()
                .eq(TutorialEdge::getStartVertex, startVertex)
                .list()
                .stream()
                .map(TutorialEdge::getEndVertex)
                .toList();
    }

    public List<Long> prev(Long endVertex) {
        return this.lambdaQuery()
                .eq(TutorialEdge::getEndVertex, endVertex)
                .list()
                .stream()
                .map(TutorialEdge::getStartVertex)
                .toList();
    }
}
