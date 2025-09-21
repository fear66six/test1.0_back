package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.dto.Doc;
import com.byrski.service.DocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/doc")
public class DocController extends AbstractController {

    private final DocService docService;

    public DocController(DocService docService) {
        this.docService = docService;
    }

    @PutMapping
    public RestBean<Doc> insertDoc(@RequestBody Doc doc) {
        return handleRequest(doc, log, docService::insertDoc);
    }

}
