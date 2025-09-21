package com.byrski.infrastructure.repository;

import com.byrski.domain.entity.dto.Doc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.lang.NonNull;
import java.util.List;

public interface DocRepository extends MongoRepository<Doc, String> {
    void deleteById(@NonNull String id);

    List<Doc> findDocsByActivityTemplateIdAndType(@NonNull Long activityTemplateId, @NonNull Integer type);

    List<Doc> findByActivityTemplateId(@NonNull Long activityTemplateId);

    List<Doc> findByType(@NonNull Integer type);

    @Query("{'activityTemplateId': ?0, 'type': ?1}")
    List<Doc> queryByActivityTemplateIdAndType(@NonNull Long activityTemplateId, @NonNull Integer type);

    @Query("{'activityTemplateId': ?0, 'type': ?1}")
    @Update("{'$set': {'contents': ?2}}")
    long updateDocByActivityTemplateIdAndType(
            @NonNull Long activityTemplateId,
            @NonNull Integer type,
            List<Doc.Content> contents
    );
}