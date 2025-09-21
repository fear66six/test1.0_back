package com.byrski.common.utils;

import com.byrski.domain.entity.dto.Account;
import com.byrski.domain.entity.vo.response.LeaderInfoVo;
import com.byrski.domain.entity.vo.response.UserInfoVo;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.domain.enums.UserIdentity;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.impl.AccountMapperService;
import com.byrski.infrastructure.mapper.impl.SchoolMapperService;
import com.byrski.domain.user.LoginUser;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 该类提供了一些信息处理的实用方法，包括获取用户信息和检查用户是否为领队等功能
 */
@Component
public class InfoUtils {

    private final AccountMapperService accountMapperService;
    private final SchoolMapperService schoolMapperService;

    public InfoUtils(AccountMapperService accountMapperService, SchoolMapperService schoolMapperService) {
        this.accountMapperService = accountMapperService;
        this.schoolMapperService = schoolMapperService;
    }

    /**
     * 获取用户信息的方法。
     * 首先通过 AccountMapperService 根据登录用户的 ID 获取用户的账户信息，
     * 然后根据账户信息中的学校 ID 获取学校名称，
     * 最后使用构建器模式构建并返回 UserInfoVo 对象，包含用户的各种信息。
     * @return UserInfoVo 包含用户信息的对象
     */
    public UserInfoVo getUserInfoVo(){
        return this.getUserInfoVoById(LoginUser.getInstance().getId());
    }

    /**
     * 获取用户信息的方法。
     * 首先通过 AccountMapperService 根据登录用户的 ID 获取用户的账户信息，
     * 然后根据账户信息中的学校 ID 获取学校名称，
     * 最后使用构建器模式构建并返回 UserInfoVo 对象，包含用户的各种信息。
     * @return UserInfoVo 包含用户信息的对象
     */
    public UserInfoVo getUserInfoVoById(Long userId){
        Account account = accountMapperService.getById(userId);
        Long schoolId = account.getSchoolId();
        String school = "无学校";
        if (schoolId != null) {
            school = schoolMapperService.getById(schoolId).getName();
        }
        return UserInfoVo.builder()
                .id(account.getId())
                .name(account.getUsername())
                .gender(account.getGender())
                .phone(account.getPhone())
                .idCardNumber(account.getIdCardNumber())
                .school(school)
                .schoolId(schoolId)
                .height(account.getHeight())
                .weight(account.getWeight())
                .skiBootsSize(account.getSkiBootsSize())
                .skiBoard(account.getSkiBoard())
                .skiLevel(account.getSkiLevel())
                .skiFavor(account.getSkiFavor())
                .isStudent(account.getIsStudent())
                .build();
    }

    /**
     * 检查用户是否为领队的方法。
     * 通过 AccountMapperService 根据登录用户的 ID 获取用户的账户信息，
     * 然后检查用户的身份是否为领队，如果不是则抛出异常。
     */
    public void checkLeader() {
        Account account = accountMapperService.getById(LoginUser.getLoginUserId());
        if (!Objects.equals(account.getIdentity(), UserIdentity.LEADER.getCode())) {
            throw new ByrSkiException(ReturnCode.UNAUTHORIZED.getCode(), "您不是领队");
        }
    }

    public LeaderInfoVo getLeaderInfoVoById(Long id) {
        Account account = accountMapperService.getById(id);
        Long schoolId = account.getSchoolId();
        String school = "无学校";
        if (schoolId != null) {
            school = schoolMapperService.getById(schoolId).getName();
        }
        return LeaderInfoVo.builder()
                .id(account.getId())
                .name(account.getUsername())
                .gender(account.getGender())
                .phone(account.getPhone())
                .idCardNumber(account.getIdCardNumber())
                .school(school)
                .intro(account.getIntro())
                .profile(account.getProfile())
                .build();
    }
}
