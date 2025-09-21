package com.byrski.service.impl;

import com.byrski.domain.entity.dto.Doc;
import com.byrski.infrastructure.repository.DocRepository;
import com.byrski.service.DocService;
import com.mongodb.lang.NonNull;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocServiceImpl implements DocService {

    @Resource
    private DocRepository docRepository;

    public Doc insertDoc(Doc doc) {
        return docRepository.save(doc);
    }

    public Doc updateDoc(Doc doc) {
        if (doc.getId() == null) {
            throw new IllegalArgumentException("更新操作中，Doc的id不能为null");
        }
        return docRepository.save(doc);
    }

    public void deleteDoc(@NonNull String id) {
        docRepository.deleteById(id);
    }

    public List<Doc> findDocs(Long activityTemplateId, Integer type) {
        return docRepository.findDocsByActivityTemplateIdAndType(activityTemplateId, type);
    }
}
