package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.vo.request.EmailRegisterVO;
import com.byrski.domain.entity.vo.request.EmailVerifyVO;
import com.byrski.domain.entity.vo.request.PasswordResetVO;
import com.byrski.service.AccountService;
import com.byrski.domain.user.LoginUser;
import com.byrski.common.utils.HttpIpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthorizeController extends AbstractController{

    @Resource
    AccountService accountService;

    @GetMapping("/captcha")
    public RestBean<Void> getCaptcha(@Validated EmailVerifyVO vo, HttpServletRequest request) {
        return handleRequest(vo, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(EmailVerifyVO vo) throws Exception {
                accountService.registerEmailVerifyCode(LoginUser.getLoginUserId(), vo, HttpIpUtils.getRemoteIpAddress(request));
            }
        });
    }

    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Validated EmailRegisterVO vo) {
        return handleRequest(vo, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(EmailRegisterVO vo) throws Exception {
                accountService.registerEmailAccount(vo);
            }
        });
    }

    @PostMapping("/reset")
    public RestBean<Void> resetPassword(@RequestBody @Validated PasswordResetVO vo) {
        return handleRequest(vo, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(PasswordResetVO vo) throws Exception {
                accountService.resetPassword(vo);
            }
        });
    }
}
