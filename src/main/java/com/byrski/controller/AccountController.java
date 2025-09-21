package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.vo.request.UserInfoUpdateVo;
import com.byrski.domain.entity.vo.request.WxLoginRequestVo;
import com.byrski.domain.entity.vo.response.CheckStudentResponseVo;
import com.byrski.domain.entity.vo.response.UserInfoVo;
import com.byrski.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class AccountController extends AbstractController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/info")
    public RestBean<UserInfoVo> getUserInfo() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected UserInfoVo doInTransactionWithoutReq() throws Exception {
                return accountService.getUserInfo();
            }
        });
    }

    @PostMapping("/update")
    public RestBean<Void> updateUserInfo(@RequestBody UserInfoUpdateVo vo) {
        return handleRequest(vo, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(UserInfoUpdateVo vo) throws Exception {
                accountService.updateUserInfo(vo);
            }
        });
    }

    @PostMapping("/student")
    public RestBean<CheckStudentResponseVo> checkStudent(@RequestBody WxLoginRequestVo requestVo) {
        return handleRequest(requestVo, log, accountService::checkStudent);
    }


}
