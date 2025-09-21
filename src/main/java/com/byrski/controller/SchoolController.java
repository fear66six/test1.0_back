package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.dto.School;
import com.byrski.service.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/school")
public class SchoolController extends AbstractController {

    private final SchoolService schoolService;

    public SchoolController (SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    /**
     * 获取学校简要信息列表
     *
     * @return 学校简要信息列表
     */
    @GetMapping("/list")
    public RestBean<List<School>> listSchool() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<School> doInTransactionWithoutReq() throws Exception {
                return schoolService.listSchool();
            }
        });
    }

}
