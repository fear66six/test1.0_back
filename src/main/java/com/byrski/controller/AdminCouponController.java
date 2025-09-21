package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.vo.request.CouponAddVo;
import com.byrski.domain.entity.vo.request.CouponBatchIssueVo;
import com.byrski.domain.entity.vo.response.CouponVo;
import com.byrski.domain.entity.vo.response.UserCouponVo;
import com.byrski.domain.entity.vo.response.CouponBatchIssueResultVo;
import com.byrski.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.byrski.domain.entity.vo.response.CouponIssuedUserVo;
import com.byrski.domain.entity.vo.request.CouponIssuePreviewCreateVo;
import com.byrski.domain.entity.vo.response.CouponIssuePreviewVo;
import com.byrski.domain.entity.vo.request.CouponBatchIssueV2Vo;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.domain.enums.ReturnCode;

/**
 * 管理员优惠券控制器 - 管理员端接口
 */
@RestController
@RequestMapping("/api/admin/coupon")
@Slf4j
public class AdminCouponController extends AbstractController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    /**
     * 创建优惠券
     */
    @PostMapping("/add")
    public RestBean<Long> createCoupon(@RequestBody CouponAddVo couponAddVo) {
        return handleRequest(couponAddVo, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Long doInTransactionWithResult(CouponAddVo couponAddVo) {
                return couponService.createCoupon(couponAddVo);
            }
        });
    }

    /**
     * 更新优惠券
     */
    @PutMapping("/update/{id}")
    public RestBean<Boolean> updateCoupon(@PathVariable Long id, @RequestBody CouponAddVo couponAddVo) {
        return handleRequest(id, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long id) {
                return couponService.updateCoupon(id, couponAddVo);
            }
        });
    }

    /**
     * 删除优惠券
     */
    @DeleteMapping("/delete/{id}")
    public RestBean<Boolean> deleteCoupon(@PathVariable Long id) {
        return handleRequest(id, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long id) {
                return couponService.deleteCoupon(id);
            }
        });
    }

    /**
     * 获取优惠券详情
     */
    @GetMapping("/detail/{id}")
    public RestBean<CouponVo> getCouponDetail(@PathVariable Long id) {
        return handleRequest(id, log, new ExecuteCallbackWithResult<>() {
            @Override
            public CouponVo doInTransactionWithResult(Long id) {
                return couponService.getCouponDetail(id);
            }
        });
    }

    /**
     * 获取优惠券列表
     */
    @GetMapping("/list")
    public RestBean<List<CouponVo>> getCouponList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String name) {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<CouponVo> doInTransactionWithoutReq() {
                return couponService.getCouponList(status, name);
            }
        });
    }

    /**
     * 启用优惠券
     */
    @PostMapping("/enable/{id}")
    public RestBean<Boolean> enableCoupon(@PathVariable Long id) {
        return handleRequest(id, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long id) {
                return couponService.enableCoupon(id);
            }
        });
    }

    /**
     * 停用优惠券
     */
    @PostMapping("/disable/{id}")
    public RestBean<Boolean> disableCoupon(@PathVariable Long id) {
        return handleRequest(id, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long id) {
                return couponService.disableCoupon(id);
            }
        });
    }

    /**
     * 批量发放优惠券（支持两种页面领取模式）
     * 
     * 功能说明：
     * - 指定用户发放：管理员指定具体用户ID列表，只有这些用户能领取
     * - 全体用户抢票：所有登录小程序的用户都能领取（不超领取上限的前提下）
     * - 支持预览和直接发放，默认使用预览模式
     */
    @PostMapping("/batch-issue")
    public RestBean<Object> batchIssueCoupon(@RequestBody Object requestBody) {
        return handleRequest(requestBody, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Object doInTransactionWithResult(Object requestBody) {
                // 根据请求体类型判断使用哪种逻辑
                if (requestBody instanceof CouponBatchIssueV2Vo) {
                    return couponService.batchIssueCouponV2((CouponBatchIssueV2Vo) requestBody);
                } else if (requestBody instanceof CouponBatchIssueVo) {
                    return couponService.batchIssueCoupon((CouponBatchIssueVo) requestBody);
                } else if (requestBody instanceof Map) {
                    // 处理 Map 类型的请求体（前端发送的 JSON 会被解析为 Map）
                    Map<String, Object> requestMap = (Map<String, Object>) requestBody;
                    
                    // 检查是否包含 pageReceiveMode 字段，如果有则使用 V2 逻辑
                    if (requestMap.containsKey("pageReceiveMode")) {
                        // 转换为 CouponBatchIssueV2Vo
                        CouponBatchIssueV2Vo v2Vo = convertMapToV2Vo(requestMap);
                        return couponService.batchIssueCouponV2(v2Vo);
                    } else {
                        // 转换为 CouponBatchIssueVo
                        CouponBatchIssueVo vo = convertMapToVo(requestMap);
                        return couponService.batchIssueCoupon(vo);
                    }
                } else {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "不支持的请求类型: " + requestBody.getClass().getSimpleName());
                }
            }
        });
    }

    /**
     * 将 Map 转换为 CouponBatchIssueVo
     */
    private CouponBatchIssueVo convertMapToVo(Map<String, Object> requestMap) {
        CouponBatchIssueVo vo = new CouponBatchIssueVo();
        vo.setCouponId(getLongValue(requestMap, "couponId"));
        vo.setIssueMethod(getIntegerValue(requestMap, "issueMethod"));
        vo.setPageReceiveMode(getIntegerValue(requestMap, "pageReceiveMode"));
        vo.setUserIds(getLongListValue(requestMap, "userIds"));
        vo.setPhoneNumbers(getStringListValue(requestMap, "phoneNumbers"));
        vo.setExcelBase64Data(getStringValue(requestMap, "excelBase64Data"));
        vo.setFileName(getStringValue(requestMap, "fileName"));
        vo.setPhoneColumnName(getStringValue(requestMap, "phoneColumnName"));
        vo.setQuantity(getIntegerValue(requestMap, "quantity"));
        vo.setRemark(getStringValue(requestMap, "remark"));
        vo.setNeedPreview(getBooleanValue(requestMap, "needPreview"));
        return vo;
    }

    /**
     * 将 Map 转换为 CouponBatchIssueV2Vo
     */
    private CouponBatchIssueV2Vo convertMapToV2Vo(Map<String, Object> requestMap) {
        CouponBatchIssueV2Vo vo = new CouponBatchIssueV2Vo();
        vo.setCouponId(getLongValue(requestMap, "couponId"));
        vo.setIssueMethod(getIntegerValue(requestMap, "issueMethod"));
        vo.setPageReceiveMode(getIntegerValue(requestMap, "pageReceiveMode"));
        vo.setUserIds(getLongListValue(requestMap, "userIds"));
        vo.setPhoneNumbers(getStringListValue(requestMap, "phoneNumbers"));
        vo.setExcelBase64Data(getStringValue(requestMap, "excelBase64Data"));
        vo.setFileName(getStringValue(requestMap, "fileName"));
        vo.setPhoneColumnName(getStringValue(requestMap, "phoneColumnName"));
        vo.setQuantity(getIntegerValue(requestMap, "quantity"));
        vo.setRemark(getStringValue(requestMap, "remark"));
        vo.setNeedPreview(getBooleanValue(requestMap, "needPreview"));
        return vo;
    }

    // 辅助方法
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Long> getLongListValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream()
                    .map(item -> {
                        if (item instanceof Number) {
                            return ((Number) item).longValue();
                        }
                        if (item instanceof String) {
                            try {
                                return Long.parseLong((String) item);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        }
                        return null;
                    })
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> getStringListValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream()
                    .map(item -> item != null ? item.toString() : null)
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 获取指定用户的优惠券列表
     */
    @GetMapping("/user/{userId}/list")
    public RestBean<List<UserCouponVo>> getUserCouponList(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer status) {
        return handleRequest(userId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public List<UserCouponVo> doInTransactionWithResult(Long userId) {
                return couponService.getUserCouponList(userId, status);
            }
        });
    }

    /**
     * 定时任务：更新过期优惠券状态
     */
    @PostMapping("/update-expired")
    public RestBean<Void> updateExpiredCoupons() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected Void doInTransactionWithoutReq() {
                couponService.updateExpiredCoupons();
                return null;
            }
        });
    }

    /**
     * 获取优惠券已发放用户列表
     */
    @GetMapping("/{couponId}/issued-users")
    public RestBean<List<CouponIssuedUserVo>> getCouponIssuedUsers(
            @PathVariable Long couponId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return handleRequest(couponId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public List<CouponIssuedUserVo> doInTransactionWithResult(Long couponId) {
                return couponService.getCouponIssuedUsers(couponId, status, username, phone, startTime, endTime);
            }
        });
    }

    /**
     * 创建发放预览会话
     */
    @PostMapping("/issue-preview/create")
    public RestBean<CouponIssuePreviewVo> createIssuePreview(@RequestBody CouponIssuePreviewCreateVo createVo) {
        return handleRequest(createVo, log, new ExecuteCallbackWithResult<>() {
            @Override
            public CouponIssuePreviewVo doInTransactionWithResult(CouponIssuePreviewCreateVo req) {
                return couponService.createIssuePreview(req);
            }
        });
    }

    /**
     * 获取发放预览详情
     */
    @GetMapping("/issue-preview/{previewId}")
    public RestBean<CouponIssuePreviewVo> getIssuePreview(@PathVariable String previewId,
                                                          @RequestParam(required = false) String username,
                                                          @RequestParam(required = false) String phone) {
        return handleRequest(previewId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public CouponIssuePreviewVo doInTransactionWithResult(String id) {
                return couponService.getIssuePreview(id, username, phone);
            }
        });
    }

    /**
     * 从预览会话移除用户
     */
    @DeleteMapping("/issue-preview/{previewId}/users/{userId}")
    public RestBean<CouponIssuePreviewVo> removeUserFromIssuePreview(@PathVariable String previewId, @PathVariable Long userId) {
        return handleRequest(previewId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public CouponIssuePreviewVo doInTransactionWithResult(String id) {
                return couponService.removeUserFromIssuePreview(id, userId);
            }
        });
    }

    /**
     * 提交预览会话并发放
     */
    @PostMapping("/issue-preview/{previewId}/submit")
    public RestBean<CouponBatchIssueResultVo> submitIssuePreview(@PathVariable String previewId) {
        return handleRequest(previewId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public CouponBatchIssueResultVo doInTransactionWithResult(String id) {
                return couponService.submitIssuePreview(id);
            }
        });
    }

    /**
     * 清理指定优惠券的重复待领取记录
     */
    @DeleteMapping("/{couponId}/clean-duplicate-records")
    public RestBean<Boolean> cleanDuplicateReceivableRecords(@PathVariable Long couponId) {
        return handleRequest(couponId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long id) {
                return couponService.cleanDuplicateReceivableRecords(id);
            }
        });
    }


    /**
     * 作废预览会话
     */
    @DeleteMapping("/issue-preview/{previewId}")
    public RestBean<Boolean> discardIssuePreview(@PathVariable String previewId) {
        return handleRequest(previewId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(String id) {
                return couponService.discardIssuePreview(id);
            }
        });
    }


    /**
     * 获取所有用户列表（用于全体用户抢票模式）
     */
    @GetMapping("/all-users")
    public RestBean<List<Long>> getAllUserIds() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<Long> doInTransactionWithoutReq() {
                return couponService.getAllUserIds();
            }
        });
    }
}
