package com.byrski.service.impl;

import com.byrski.domain.entity.vo.request.*;
import com.byrski.domain.entity.vo.response.CheckStudentResponseVo;
import com.byrski.domain.entity.vo.response.UserInfoVo;
import com.byrski.domain.enums.EmailVerifyEnums;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.domain.enums.UserIdentity;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.infrastructure.mapper.impl.AccountMapperService;
import com.byrski.domain.entity.dto.Account;
import com.byrski.service.AccountService;
import com.byrski.domain.user.LoginUser;
import com.byrski.common.utils.Const;
import com.byrski.common.utils.InfoUtils;
import com.byrski.common.utils.RedisUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccountServiceImpl implements AccountService {

    @Value("${wechat.app-id}")
    private String APP_ID;
    @Value("${wechat.app-secret}")
    private String APP_SECRET;
    @Value("${wechat.urls.auth-code}")
    private String AUTH_CODE_URL;
    @Value("${wechat.urls.phone-code}")
    private String PHONE_CODE_URL;
    @Value("${wechat.urls.check-student}")
    private String CHECK_STUDENT_URL;


    @Resource
    private AmqpTemplate amqpTemplate;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private AccountMapperService accountMapperService;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private InfoUtils infoUtils;
    @Resource
    private RedisUtils redisUtils;

    @Override
    public Account findOrCreateByOpenid(String openid, String phoneNumber) {
        Account account = accountMapperService.findByOpenid(openid);
        if (account == null) {
            account = new Account(
                    openid,
                    "wx_" + StringUtils.left(openid, 21),
                    phoneNumber,
                    UserIdentity.USER.getCode(),
                    false,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    0,
                    true
            );
            accountMapperService.save(account);
        }
        return account;
    }

    @Override
    public String getOpenidByCode(String code) throws ByrSkiException {
        String requestPath = String.format(
                AUTH_CODE_URL,
                APP_ID, APP_SECRET, code
        );

        String response = restTemplate.getForObject(requestPath, String.class);
        if (response != null) {
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

            if (jsonObject.has("openid")) {
                return jsonObject.get("openid").getAsString();
            }

            if (jsonObject.has("errcode")) {
                int errcode = jsonObject.get("errcode").getAsInt();
                throw new ByrSkiException(errcode, jsonObject.get("errmsg").getAsString());
            }

        }
        throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(), "获取OpenId失败，请检查登录传入的code参数");
    }

    @Override
    public String getPhoneNumberByCode(String phoneCode) {
        String accessToken = getAccessToken();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", phoneCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
        String url = String.format(PHONE_CODE_URL, accessToken);
        try {
            String response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            ).getBody();
            if (response == null) {
                throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(), "获取手机号失败，未收到响应");
            }
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            if (jsonResponse.has("errcode") && jsonResponse.get("errcode").getAsInt()!= 0) {
                int errcode = jsonResponse.get("errcode").getAsInt();
                String errmsg = jsonResponse.has("errmsg")?
                        jsonResponse.get("errmsg").getAsString() : "未知的手机号验证错误信息";
                throw new ByrSkiException(errcode, errmsg);
            }

            if (jsonResponse.has("phone_info")) {
                return jsonResponse.get("phone_info").getAsJsonObject().get("purePhoneNumber").getAsString();
            }
            else {
                throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(), "获取手机号失败，响应体中不包含手机号信息");
            }

        } catch (ByrSkiException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取手机号失败", e);
            throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(), "获取手机号失败，请检查登录传入的phoneCode参数");
        }
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = findAccountByUsernameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException("User not found.");
        }

        return User
                .withUsername(username)
                .password(account.getPassword())
                .build();
    }

    @Override
    public UserInfoVo getUserInfo() {
        return infoUtils.getUserInfoVo();
    }

    @Override
    public void updateUserInfo(UserInfoUpdateVo vo) {
        Account account = accountMapperService.getById(LoginUser.getInstance().getId());
        account.setUsername(vo.getName());
        account.setGender(vo.getGender());
        account.setEmail(vo.getEmail());
        account.setPhone(vo.getPhone());
        account.setHeight(vo.getHeight());
        account.setWeight(vo.getWeight());
        account.setSkiBootsSize(vo.getSkiBootsSize());
        account.setSkiBoard(vo.getSkiBoard());
        account.setSkiLevel(vo.getSkiLevel());
        account.setSkiFavor(vo.getSkiFavor());
        account.setUpdateTime(LocalDateTime.now());
        account.setSchoolId(vo.getSchoolId());

        accountMapperService.updateById(account);
    }

    @Override
    public Account getByUserId(Integer userId) {
        return accountMapperService.getById(userId);
    }

    @Override
    public Account findAccountByUsernameOrEmail(String text) {
        return accountMapperService.findAccountByUsernameOrEmail(text);
    }

    @Override
    public CheckStudentResponseVo checkStudent(WxLoginRequestVo requestVo) {
        String accessToken = getAccessToken();

        Long userId = LoginUser.getInstance().getId();
        Account account = accountMapperService.getById(userId);
        String openid = account.getOpenid();

        // 3. 准备请求数据
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("openid", openid);
        requestBody.put("wx_studentcheck_code", requestVo.getLoginCode());

        // 4. 调用微信API验证学生身份
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        String url = String.format(CHECK_STUDENT_URL, accessToken);
        try {
            String response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            ).getBody();

            if (response == null) {
                throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(), "学生身份验证失败: 未收到响应");
            }
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            // 检查是否有错误
            if (jsonResponse.has("errcode") && jsonResponse.get("errcode").getAsInt() != 0) {
                int errcode = jsonResponse.get("errcode").getAsInt();
                String errmsg = jsonResponse.has("errmsg") ?
                        jsonResponse.get("errmsg").getAsString() : "未知的学生认证错误信息";
                throw new ByrSkiException(errcode, errmsg);
            }

            // 构造返回对象
            CheckStudentResponseVo responseVo = new CheckStudentResponseVo();
            if (jsonResponse.has("bind_status")) {
                responseVo.setBindStatus(jsonResponse.get("bind_status").getAsInt());
            }
            if (jsonResponse.has("is_student")) {
                responseVo.setIsStudent(jsonResponse.get("is_student").getAsBoolean());
            }

            account.setIsStudent(jsonResponse.get("is_student").getAsBoolean());
            accountMapperService.updateById(account);
            return responseVo;

        } catch (ByrSkiException e) {
            throw e;
        } catch (Exception e) {
            log.error("学生身份验证失败: {}", e.getMessage(), e);
            throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(),
                    "学生身份验证失败: " + e.getMessage());
        }
    }

    private String getAccessToken() {
        // 中控服务的 URL
        String centralServiceUrl = "https://gxski.top/central/access-token";

        try {
            // 使用 RestTemplate 发送 GET 请求
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(centralServiceUrl, String.class);

            // 解析 JSON 响应
            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

                if (jsonObject.has("access_token")) {
                    // 成功获取 access token
                    return jsonObject.get("access_token").getAsString();
                } else if (jsonObject.has("error")) {
                    // 处理错误信息
                    String errorMessage = jsonObject.get("error").getAsString();
                    throw new RuntimeException("获取 AccessToken 失败: " + errorMessage);
                }
            }

            // 如果响应为空或格式不正确
            throw new RuntimeException("获取 AccessToken 失败: 响应为空或格式不正确");

        } catch (HttpClientErrorException e) {
            // 处理 HTTP 错误
            log.error("HTTP 错误: 状态码 = {}, 响应 = {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HTTP 错误: " + e.getMessage(), e);

        } catch (ResourceAccessException e) {
            // 处理资源访问错误（如网络问题）
            log.error("资源访问错误: {}", e.getMessage());
            throw new RuntimeException("资源访问错误: " + e.getMessage(), e);

        } catch (Exception e) {
            // 处理其他错误
            log.error("获取 AccessToken 失败: {}", e.getMessage());
            throw new RuntimeException("获取 AccessToken 失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void registerEmailVerifyCode(Long userId, EmailVerifyVO vo, String ip) throws Exception {
        // 防止同一邮箱重复注册
        if (userId != null && EmailVerifyEnums.EMAIL_REGISTER_VERIFY.getType().equals(vo.getType())) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "您似乎已经注册过了，请检查!");
        }


        if (EmailVerifyEnums.EMAIL_REGISTER_VERIFY.getType().equals(vo.getType())) {
            if (accountMapperService.existsAccountByEmail(vo.getEmail())) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "该邮箱已被注册，请更换邮箱或使用此账号登录");
            }
        }

        // 检查待重置密码的账号是否存在
        if (EmailVerifyEnums.EMAIL_RESET_VERIFY.getType().equals(vo.getType())) {
            if (!accountMapperService.existsAccountByEmail(vo.getEmail())) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "该邮箱未注册，请检查!");
            }
        }

        redisUtils.lock(Const.VERIFY_EMAIL_SEND_LOCK + vo.getEmail(), new RedisUtils.LockCallbackWithoutRet() {
            @Override
            protected void executeWithoutRet() {
                if (!redisUtils.lockWithExpire(Const.VERIFY_EMAIL_LIMIT + ip, 60)) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "请求太频繁啦，请稍后再试");
                }
                String verifyCode = RandomStringUtils.randomNumeric(6);
                Map<String, Object> data = Map.of("type", vo.getType(), "email", vo.getEmail(), "code", verifyCode);
                amqpTemplate.convertAndSend("mail", data);
                redisUtils.set(Const.VERIFY_EMAIL_DATA + vo.getEmail(), verifyCode, 180);
            }
        });
    }

    @Override
    public void registerEmailAccount(EmailRegisterVO vo) throws Exception {
        redisUtils.lock(Const.REGISTER_EMAIL_LOCK + vo.getEmail(), new RedisUtils.LockCallbackWithoutRet() {
            @Override
            protected void executeWithoutRet() {
                String email = vo.getEmail();
                String username = vo.getUsername();
                validateEmailVerificationCode(email, vo.getCode());
                if (accountMapperService.existsAccountByEmail(email)) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "该邮箱已被注册，请更换邮箱或使用此账号登录");
                }
                if (accountMapperService.existsAccountByUsername(username)) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "该用户名已被使用，请更换用户名");
                }

                String password = passwordEncoder.encode(vo.getPassword());
                Account account = new Account(username, password, email, "user", LocalDateTime.now());
                if (accountMapperService.save(account)) {
                    redisUtils.delete(Const.VERIFY_EMAIL_DATA + email);
                    log.info("新用户注册：{}", account);
                } else {
                    throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(), "注册失败，请联系管理员");
                }
            }
        });
    }

    private void validateEmailVerificationCode(String email, String code2) {
        String code = redisUtils.get(Const.VERIFY_EMAIL_DATA + email);
        if (code == null) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "请先获取验证码");
        }
        if (!code.equals(code2)) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "验证码错误，请核对后重新输入");
        }
    }

    @Override
    public void resetPassword(PasswordResetVO vo) throws Exception {
        redisUtils.lock(Const.RESET_EMAIL_LOCK + vo.getEmail(), new RedisUtils.LockCallbackWithoutRet() {
            @Override
            protected void executeWithoutRet() {
                String email = vo.getEmail();
                validateEmailVerificationCode(email, vo.getCode());

                Account account = accountMapperService.findAccountByEmail(email);
                if (account == null) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "该邮箱未注册，请检查!");
                }

                String newPassword = passwordEncoder.encode(vo.getPassword());
                account.setPassword(newPassword);
                if (accountMapperService.updateById(account)) {
                    redisUtils.delete(Const.VERIFY_EMAIL_DATA + email);
                    log.info("用户重置密码：{}", account.getUsername());
                } else {
                    throw new ByrSkiException(ReturnCode.OTHER_ERROR.getCode(), "密码重置失败，请联系我们");
                }
            }
        });
    }
}
