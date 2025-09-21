package com.byrski.common.filter;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.dto.Account;
import com.byrski.domain.entity.vo.request.WxLoginRequestVo;
import com.byrski.domain.entity.vo.response.LoginCredential;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.domain.enums.UserIdentity;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.service.AccountService;
import com.byrski.common.utils.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class WxLoginFilter extends OncePerRequestFilter {

    @Resource
    private AccountService accountService;
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    // TODO: 替换为你自己的小程序 AppID 和 AppSecret
    private static final String APPID = "wxe9b791b5423c1bbf";
    private static final String SECRET = "b7f7fdd0bf4c523e496bca5322dc7dad";

    private String cachedAccessToken = null;
    private long accessTokenExpireTime = 0;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        if ("/api/auth/login".equals(request.getServletPath()) && "POST".equals(request.getMethod())) {
            try {
                WxLoginRequestVo loginRequest = getWxLoginRequestVo(request);

                // step1: jscode2session -> openid
                String openid = getOpenIdFromCode(loginRequest.getLoginCode());

                // step2: getuserphonenumber -> phoneNumber
                String phoneNumber = getPhoneNumberFromCode(loginRequest.getPhoneCode());

                // step3: 查询或创建用户
                Account account = accountService.findOrCreateByOpenid(openid, phoneNumber);

                // step4: 生成 JWT & 设置认证
                UserDetails userDetails = createUserDetails(account);
                String token = jwtUtils.createUserJwt(userDetails, account.getId(), account.getUsername());

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                // step5: 返回登录凭证
                LoginCredential vo = account.asViewObject(LoginCredential.class, v -> {
                    v.setOpenId(openid);
                    v.setIdentity(account.getIdentity());
                    v.setExpireTime(jwtUtils.expireTokenTime());
                    v.setToken(token);
                });

                log.info("用户 {} 登录成功, openId: {}", account.getUsername(), openid);
                writeJsonResponse(response, RestBean.success(vo).asJsonString(), HttpServletResponse.SC_OK);

            } catch (ByrSkiException e) {
                log.error("登录失败: {}", e.getMessage());
                writeJsonResponse(response,
                        createErrorResponseBody(e.getCode(), e.getMessage()),
                        HttpServletResponse.SC_BAD_REQUEST);
            }
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 通过 loginCode 获取 openid
     */
    private String getOpenIdFromCode(String loginCode) {
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session"
                    + "?appid=" + APPID
                    + "&secret=" + SECRET
                    + "&js_code=" + loginCode
                    + "&grant_type=authorization_code";

            String resp = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(resp);

            if (json.has("errcode")) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(),
                        "jscode2session失败: " + json.toString());
            }

            return json.get("openid").asText();
        } catch (Exception e) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(),
                    "获取openid失败: " + e.getMessage());
        }
    }

    /**
     * 通过 phoneCode 获取手机号
     */
    private String getPhoneNumberFromCode(String phoneCode) {
        try {
            String accessToken = getAccessToken();

            String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = "{\"code\":\"" + phoneCode + "\"}";
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            JsonNode json = objectMapper.readTree(resp.getBody());

            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(),
                        "获取手机号失败: " + json.toString());
            }

            return json.get("phone_info").get("phoneNumber").asText();
        } catch (Exception e) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(),
                    "解密手机号失败: " + e.getMessage());
        }
    }

    /**
     * 获取 access_token，带缓存
     */
    private String getAccessToken() {
        long now = System.currentTimeMillis();
        if (cachedAccessToken != null && now < accessTokenExpireTime) {
            return cachedAccessToken;
        }
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/token"
                    + "?grant_type=client_credential"
                    + "&appid=" + APPID
                    + "&secret=" + SECRET;

            String resp = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(resp);

            if (json.has("errcode")) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(),
                        "获取access_token失败: " + json.toString());
            }

            cachedAccessToken = json.get("access_token").asText();
            int expiresIn = json.get("expires_in").asInt();
            accessTokenExpireTime = now + (expiresIn - 200) * 1000L; // 提前200秒过期

            return cachedAccessToken;
        } catch (Exception e) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(),
                    "获取access_token失败: " + e.getMessage());
        }
    }

    private WxLoginRequestVo getWxLoginRequestVo(HttpServletRequest request) throws IOException {
        if (request.getContentLengthLong() == 0) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "请求体不能为空");
        }
        WxLoginRequestVo loginRequest = objectMapper.readValue(request.getInputStream(), WxLoginRequestVo.class);
        if (StringUtils.isBlank(loginRequest.getLoginCode())) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "loginCode 参数不能为空");
        }
        if (StringUtils.isBlank(loginRequest.getPhoneCode())) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "phoneCode 参数不能为空");
        }
        return loginRequest;
    }

    private UserDetails createUserDetails(Account account) {
        return User.builder()
                .username(account.getUsername())
                .password("")
                .authorities(UserIdentity.USER.getRole())
                .build();
    }

    private String createErrorResponseBody(int code, String msg) {
        return String.format("{\"code\": %d,\"data\": null, \"message\": \"%s\"}", code, msg);
    }

    private void writeJsonResponse(HttpServletResponse response, String json, int status) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(status);
        response.getWriter().write(json);
    }
}
