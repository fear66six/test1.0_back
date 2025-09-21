package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.vo.request.UserInfoUpdateVo;
import com.byrski.service.AdminService;
import com.byrski.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class GuestController extends AbstractController{

    private final AdminService adminService;
    private final AccountService accountService;
    public GuestController(AdminService adminService, AccountService accountService) {
        this.adminService = adminService;
        this.accountService = accountService;
    }

    @GetMapping("/account/name")
    public RestBean<String> getUserName() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public String doInTransactionWithoutReq() {
                return adminService.getUserName();
            }
        });
    }

    @GetMapping("/account/email")
    public RestBean<String> getUserEmail() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            public String doInTransactionWithoutReq() {
                return adminService.getUserEmail();
            }
        });
    }

    @PostMapping("/account/update")
    public RestBean<Void> updateUserInfo(@RequestBody UserInfoUpdateVo vo) {
        return handleRequest(vo, log, new ExecuteCallbackWithoutResult<>() {
            @Override
            protected void doInTransactionWithoutResult(UserInfoUpdateVo vo) throws Exception {
                accountService.updateUserInfo(vo);
            }
        });
    }

}
