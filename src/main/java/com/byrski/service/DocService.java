package com.byrski.service;

import com.byrski.domain.entity.dto.Doc;
import com.mongodb.lang.NonNull;

import java.util.List;

public interface DocService {


    Doc insertDoc(Doc doc);

    Doc updateDoc(Doc doc);

    void deleteDoc(@NonNull String id);

    List<Doc> findDocs(Long activityTemplateId, Integer type);
}
