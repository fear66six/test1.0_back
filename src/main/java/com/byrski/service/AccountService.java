package com.byrski.service;

import com.byrski.domain.entity.dto.Account;
import com.byrski.domain.entity.vo.request.*;
import com.byrski.domain.entity.vo.response.CheckStudentResponseVo;
import com.byrski.domain.entity.vo.response.UserInfoVo;
import com.byrski.common.exception.ByrSkiException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AccountService extends UserDetailsService {


    /**
     * 通过用户ID获取Account对象。
     * @param userId 用户ID
     * @return Account对象
     */
    Account getByUserId(Integer userId);

    /**
     * 通过用户名或邮箱查找Account对象。
     * @param text 用户名或邮箱
     * @return Account对象
     */
    Account findAccountByUsernameOrEmail(String text);

    /**
     * 通过微信code获取openid。调用微信接口获取openid，并处理可能的错误。
     * @param loginCode 微信登录code
     * @return openid
     * @throws ByrSkiException 获取openid失败时抛出异常
     */
    String getOpenidByCode(String loginCode) throws ByrSkiException;

    /**
     * 通过code获取手机号。调用微信接口获取手机号，并处理可能的错误。
     * @param phoneCode 微信手机号获取code
     * @return 手机号
     */
    String getPhoneNumberByCode(String phoneCode);

    /**
     * 通过openid查找或创建Account对象。如果存在则返回，不存在则创建并保存后返回。
     * @param openid 微信openid
     * @return Account对象
     */
    Account findOrCreateByOpenid(String openid, String phoneNumber);

    /**
     * 通过用户名加载用户详情。实现Spring Security的UserDetailsService接口方法。
     * @param username 用户名
     * @return UserDetails对象
     * @throws UsernameNotFoundException 用户不存在时抛出异常
     */
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    /**
     * 获取用户信息。
     * @return UserInfoVo对象
     */
    UserInfoVo getUserInfo();

    /**
     * 更新用户信息。
     * @param vo 用户信息更新VO
     */
    void updateUserInfo(UserInfoUpdateVo vo);

    /**
     * 检查学生认证状态。
     * @param requestVo 学生认证码
     * @return 学生认证状态
     */
    CheckStudentResponseVo checkStudent(WxLoginRequestVo requestVo);

    void registerEmailVerifyCode(Long userId, EmailVerifyVO vo, String ip) throws Exception;

    void registerEmailAccount(EmailRegisterVO vo) throws Exception;

    void resetPassword(PasswordResetVO vo) throws Exception;
}
