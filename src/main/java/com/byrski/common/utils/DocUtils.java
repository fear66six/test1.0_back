package com.byrski.common.utils;

import com.byrski.domain.entity.dto.Doc;
import com.byrski.infrastructure.repository.DocRepository;
import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocUtils {

    @Resource
    private DocRepository docRepository;
    @Resource
    private MongoTemplate mongoTemplate;

    public Doc getDocByActivityTemplateIdAndType(Long activityTemplateId, Integer type) {
        return docRepository.findDocsByActivityTemplateIdAndType(activityTemplateId, type).stream().findFirst().orElse(null);
    }

    public long updateDocByActivityTemplateIdAndType(
            Long activityTemplateId,
            Integer type,
            List<Doc.Content> contents
    ) {
        Query query = new Query(Criteria.where("activityTemplateId").is(activityTemplateId)
                .and("type").is(type));
        Update update = new Update().set("contents", contents);
        UpdateResult result = mongoTemplate.upsert(query, update, Doc.class);
        return result.getModifiedCount();
    }

}
