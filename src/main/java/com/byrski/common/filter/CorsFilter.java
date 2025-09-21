package com.byrski.common.filter;

import com.byrski.common.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * CORS过滤器，用于处理跨域请求。
 *  该过滤器添加必要的CORS头信息到响应中，允许来自指定源的请求。
 *  过滤器顺序由@Order(Const.ORDER_CORS)指定。
 */
@Component
@Order(Const.ORDER_CORS)
public class CorsFilter extends HttpFilter {

    /**
     * 过滤器处理方法，添加CORS头信息并继续执行过滤器链。
     * @param request  HttpServletRequest对象
     * @param response HttpServletResponse对象
     * @param chain    FilterChain对象
     * @throws IOException      如果IO操作失败
     * @throws ServletException 如果Servlet操作失败
     */
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.addCorsHeader(request, response);
        chain.doFilter(request, response);
    }

    /**
     * 添加CORS头信息到响应中。
     *  允许的源取自请求头中的Origin字段，允许的方法和头信息固定。
     * @param request  HttpServletRequest对象
     * @param response HttpServletResponse对象
     */
    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
