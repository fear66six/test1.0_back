package com.byrski.common.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.infrastructure.mapper.impl.AccountMapperService;
import com.byrski.common.utils.JwtUtils;
import com.byrski.domain.user.LoginUser;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT授权过滤器，用于验证JWT令牌并设置Spring Security的安全上下文。
 * 该过滤器拦截每个请求，解析JWT令牌，验证令牌有效性，并根据令牌信息设置当前登录用户。
 */
@Component
@Slf4j
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    /**
     * JWT工具类，用于解析和验证JWT令牌。
     */
    @Resource
    private JwtUtils jwtUtils;

    /**
     * 用户数据访问服务，用于获取用户信息。
     */
    @Resource
    private AccountMapperService accountMapperService;

    /**
     * 过滤器处理方法，解析JWT令牌，验证令牌有效性，设置安全上下文，并继续执行过滤器链。
     * @param request  HttpServletRequest对象
     * @param response HttpServletResponse对象
     * @param filterChain FilterChain对象
     * @throws ServletException 如果Servlet操作失败
     * @throws IOException      如果IO操作失败
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().contains("/login") || request.getServletPath().contains("/captcha") || request.getServletPath().contains("/register") || request.getServletPath().contains("/reset")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getServletPath().contains("/api/admin")) {
            try {
                String authorization = request.getHeader("Authorization");
                DecodedJWT jwt = jwtUtils.resolveAdminJwt(authorization);
                if (jwt != null) {
                    UserDetails userDetails = jwtUtils.toAdminDetails(jwt);
                    Long userId = jwtUtils.toAdminId(jwt);
                    LoginUser.initLoginUser(userId); // 初始化登录用户信息
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken); // 设置安全上下文
                }
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                log.error(e.getMessage());
                writeJsonResponse(response, createErrorResponseBody(ReturnCode.UNAUTHORIZED.getCode(), "令牌校验失败"), HttpServletResponse.SC_BAD_REQUEST);
            } finally {
                LoginUser.clearInstance(); // 清理登录用户信息
            }
//            filterChain.doFilter(request, response);
//            return;
        }
        else {
            try {
                String authorization = request.getHeader("Authorization");
                DecodedJWT jwt = jwtUtils.resolveUserJwt(authorization);
                if (jwt != null) {
                    UserDetails userDetails = jwtUtils.toUserDetails(jwt);
                    Long userId = jwtUtils.toId(jwt);
                    LoginUser.initLoginUser(userId); // 初始化登录用户信息
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken); // 设置安全上下文
                }
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                log.error(e.getMessage());
                writeJsonResponse(response, createErrorResponseBody(ReturnCode.UNAUTHORIZED.getCode(), "令牌校验无效"), HttpServletResponse.SC_BAD_REQUEST);
            } finally {
                LoginUser.clearInstance(); // 清理登录用户信息
            }
        }
    }

    /**
     * 创建错误响应的JSON字符串。
     *
     * @param code 错误码。
     * @param msg  错误信息。
     * @return  格式化的JSON字符串，包含错误码和错误信息。
     */
    private String createErrorResponseBody(int code, String msg) {
        return String.format("{\"code\": %d,\"data\": null, \"message\": \"%s\"}", code, msg);
    }

    /**
     * 将JSON字符串写入HttpServletResponse。设置响应内容类型为application/json，字符编码为UTF-8，状态码为指定值。
     *
     * @param response  HttpServletResponse对象。
     * @param json  要写入的JSON字符串。
     * @param status  HTTP状态码。
     * @throws IOException  如果写入过程中发生IO异常。
     */
    private void writeJsonResponse(HttpServletResponse response, String json, int status) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setStatus(status);
        response.getWriter().write(json);
    }
}
