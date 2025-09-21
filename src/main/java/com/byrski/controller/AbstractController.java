package com.byrski.controller;

import com.byrski.common.utils.IpBlacklistManager;
import com.byrski.domain.entity.ApiRequestEvent;
import com.byrski.domain.entity.vo.request.UploadImage;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.domain.user.LoginUser;
import com.byrski.common.utils.HttpIpUtils;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.exception.WechatPayException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import com.byrski.domain.entity.RestBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

@Slf4j
public class AbstractController {

    @Autowired
    protected HttpServletRequest httpRequest;
    @Autowired
    protected ApplicationEventPublisher eventPublisher;
    @Autowired
    protected IpBlacklistManager ipBlacklistManager;

    /**
     * 处理请求的通用方法
     *
     * @param request  请求对象
     * @param log      日志记录器
     * @param callback 执行回调
     * @param <R>      请求类型
     * @param <T>      响应类型
     * @return 响应对象
     */
    protected <R, T> RestBean<T> handleRequest(R request, Logger log, ExecuteCallback<R, T> callback) {

        eventPublish();

        String ipAddress = HttpIpUtils.getRemoteIpAddress(httpRequest);
        if (ipBlacklistManager.isIpBlocked(ipAddress)) {
            log.warn("Request from blocked IP: {}", ipAddress);
            return RestBean.failure(ReturnCode.FORBIDDEN.getCode(), "Access denied: Your IP has been blocked");
        }
        if (!ipBlacklistManager.checkAndRecordIpRequest(ipAddress)) {
            log.warn("Request frequency limit exceeded for IP: {}", ipAddress);
            return RestBean.failure(ReturnCode.FORBIDDEN.getCode(), "Too many requests, please try again later");
        }
        if (request instanceof UploadImage uploadImage) {
            log.info(
                    "{}，IP: {}，请求者: {}，请求参数(图片信息): {}",
                    httpRequest.getServletPath(),
                    HttpIpUtils.getRemoteIpAddress(httpRequest),
                    LoginUser.getLoginUserId(),
                    uploadImage.getFileName()
            );
        } else {
            log.info(
                    "{}，IP: {}，请求者: {}，请求参数: {}",
                    httpRequest.getServletPath(),
                    HttpIpUtils.getRemoteIpAddress(httpRequest),
                    LoginUser.getLoginUserId(),
                    request
            );
        }
        RestBean<T> result;
        try {
            T data = callback.doInTransaction(request);
            result = RestBean.success(data);
        } catch (ServiceException e) {
            // 对于微信支付的异常，需要特殊处理
            if (Objects.equals(e.getErrorCode(), "RESOURCE_NOT_EXISTS") || Objects.equals(e.getErrorCode(), "PARAM_ERROR") || Objects.equals(e.getErrorCode(), "ORDER_NOT_EXIST"))
                result = RestBean.failure(ReturnCode.TRADE_NOT_EXIST.getCode(), e.getErrorMessage());
            else
                throw e;
        } catch (MissingServletRequestParameterException e) {
            log.error("缺少必需参数: {}", e.getParameterName());
            result = RestBean.failure(
                    ReturnCode.PARAM_EXCEPTION.getCode(),
                    "缺少必需参数: " + e.getParameterName()
            );
        } catch (MethodArgumentTypeMismatchException e) {
            log.error("参数类型错误: {} 应为 {}", e.getName(), Objects.requireNonNull(e.getRequiredType()).getSimpleName());
            result = RestBean.failure(
                    ReturnCode.PARAM_EXCEPTION.getCode(),
                    "参数类型错误: " + e.getName() + " 应为 " + e.getRequiredType().getSimpleName()
            );
        } catch (MethodArgumentNotValidException e) {
            log.error("参数验证失败: {}", Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());
            result = RestBean.failure(
                    ReturnCode.PARAM_EXCEPTION.getCode(),
                    Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage()
            );
        } catch (DuplicateKeyException e) {
            log.error("重复的数据: {}", e.getCause().getMessage());
            result = RestBean.failure(ReturnCode.PARAM_EXCEPTION.getCode(),
                    "重复的数据: " + e.getCause().getMessage()
            );
        } catch (DataIntegrityViolationException e) {
            log.error("数据完整性错误: {}", e.getCause().getMessage());
            result = RestBean.failure(ReturnCode.PARAM_EXCEPTION.getCode(),
                    "数据完整性错误: " + e.getCause().getMessage()
            );
        }catch (IllegalArgumentException e) {
            log.error("参数错误: {}", e.getMessage());
            result = RestBean.failure(ReturnCode.PARAM_EXCEPTION.getCode(), e.getMessage());
        } catch (ByrSkiException be) {
            String msg = be.getMsg();
            log.error(msg);
            result = RestBean.failure(be.getCode(), msg);
        } catch (WechatPayException e) {
            log.error("微信支付错误: {}", e.getMessage());
            result = RestBean.failure(ReturnCode.OTHER_ERROR.getCode(), e.getMessage());
        }catch (Throwable e) {
            log.error("发生未知错误", e);
            result = RestBean.failure(ReturnCode.OTHER_ERROR.getCode(), e.getMessage());
        }
        log.info("response code:{}, msg:{}, data:{}", result.code(), result.message(), result.data());
        return result;
    }

    /**
     * 发布API请求事件，用于记录请求日志
     */
    private void eventPublish() {
        ApiRequestEvent event = new ApiRequestEvent();
        event.setPath(httpRequest.getServletPath());
        event.setMethod(httpRequest.getMethod());
        event.setIp(HttpIpUtils.getRemoteIpAddress(httpRequest));
        event.setUserId(LoginUser.getLoginUserId());
        event.setTimestamp(System.currentTimeMillis());
        eventPublisher.publishEvent(event);
    }

    /**
     * 定义一个执行回调接口
     *
     * @param <R> 请求类型
     * @param <T> 响应类型
     */
    public interface ExecuteCallback<R, T> {
        T doInTransaction(R request) throws Exception;
    }

    /**
     * 不带返回值的执行回调实现
     *
     * @param <R> 请求类型
     */
    protected abstract static class ExecuteCallbackWithoutResult<R> implements ExecuteCallback<R, Void> {
        public final Void doInTransaction(R request) throws Exception {
            try {
                doInTransactionWithoutResult(request);

            }
            catch (ServiceException e) {
                log.error("ServiceException: {}", e.getMessage());
            }
            return null;
        }

        /** 不带返回值的实现 */
        protected abstract void doInTransactionWithoutResult(R request) throws Exception;
    }

    /**
     * 带返回值的执行回调实现
     *
     * @param <R> 请求类型
     * @param <T> 响应类型
     */
    protected abstract static class ExecuteCallbackWithResult<R, T> implements ExecuteCallback<R, T> {
        public final T doInTransaction(R request) throws Exception {
            return doInTransactionWithResult(request);
        }

        /** 带返回值的实现 */
        protected abstract T doInTransactionWithResult(R request) throws Exception;
    }

    /**
     * 不带请求和返回值的执行回调实现
     */
    protected abstract static class ExecuteCallbackWithoutReqResult implements ExecuteCallback<Void, Void> {
        public final Void doInTransaction(Void request) throws Exception {
            doInTransactionWithoutReqResult();
            return null;
        }

        /** 不带请求和返回值的实现 */
        protected abstract void doInTransactionWithoutReqResult() throws Exception;
    }

    /**
     * 不带请求的执行回调实现
     *
     * @param <T> 响应类型
     */
    protected abstract static class ExecuteCallbackWithoutReq<T> implements ExecuteCallback<Void, T> {
        public final T doInTransaction(Void request) throws Exception {
            return doInTransactionWithoutReq();
        }

        /** 不带请求的实现 */
        protected abstract T doInTransactionWithoutReq() throws Exception;
    }
}
