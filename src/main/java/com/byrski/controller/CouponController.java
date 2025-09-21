package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.vo.response.CouponVo;
import com.byrski.domain.entity.vo.response.UserCouponVo;
import com.byrski.domain.user.LoginUser;
import com.byrski.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器 - 用户端接口
 */
@RestController
@RequestMapping("/api/coupon")
@Slf4j
public class CouponController extends AbstractController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    /**
     * 用户领取优惠券
     */
    @PostMapping("/receive/{couponId}")
    public RestBean<Boolean> receiveCoupon(@PathVariable Long couponId) {
        return handleRequest(couponId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long couponId) {
                return couponService.receiveCoupon(couponId, LoginUser.getLoginUserId());
            }
        });
    }

    /**
     * 获取用户优惠券列表
     */
    @GetMapping("/user/list")
    public RestBean<List<UserCouponVo>> getUserCouponList(
            @RequestParam(required = false) Integer status) {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<UserCouponVo> doInTransactionWithoutReq() {
                return couponService.getUserCouponList(LoginUser.getLoginUserId(), status);
            }
        });
    }

    /**
     * 获取用户可用的优惠券列表
     */
    @GetMapping("/user/available")
    public RestBean<List<UserCouponVo>> getAvailableCoupons(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false, defaultValue = "0") Double amount,
            @RequestParam(required = false, defaultValue = "1") Integer participants) {
        log.info("获取可用优惠券 - 接收参数: productId={}, amount={}, participants={}", productId, amount, participants);
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<UserCouponVo> doInTransactionWithoutReq() {
                return couponService.getAvailableCoupons(LoginUser.getLoginUserId(), productId, amount, participants);
            }
        });
    }

    /**
     * 获取可领取的优惠券列表（页面领取类型）
     */
    @GetMapping("/user/receivable")
    public RestBean<List<CouponVo>> getReceivableCoupons() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected List<CouponVo> doInTransactionWithoutReq() {
                return couponService.getReceivableCoupons(LoginUser.getLoginUserId());
            }
        });
    }

    /**
     * 使用优惠券
     */
    @PostMapping("/use/{userCouponId}")
    public RestBean<Double> useCoupon(
            @PathVariable Long userCouponId,
            @RequestParam Long tradeId,
            @RequestParam @DecimalMin(value = "0.01", message = "订单金额必须大于0") Double amount) {
        return handleRequest(userCouponId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Double doInTransactionWithResult(Long userCouponId) {
                return couponService.useCoupon(userCouponId, tradeId, amount);
            }
        });
    }

    /**
     * 退还优惠券
     */
    @PostMapping("/refund/{userCouponId}")
    public RestBean<Boolean> refundCoupon(@PathVariable Long userCouponId) {
        return handleRequest(userCouponId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long userCouponId) {
                return couponService.refundCoupon(userCouponId);
            }
        });
    }

    /**
     * 检查优惠券是否可用
     */
    @GetMapping("/check/{couponId}")
    public RestBean<Boolean> isCouponAvailable(
            @PathVariable Long couponId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false, defaultValue = "0") Double amount,
            @RequestParam(required = false, defaultValue = "1") Integer participants) {
        return handleRequest(couponId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Boolean doInTransactionWithResult(Long couponId) {
                return couponService.isCouponAvailable(couponId, LoginUser.getLoginUserId(), productId, amount, participants);
            }
        });
    }

    /**
     * 计算优惠金额和最终价格
     */
    @GetMapping("/calculate/{couponId}")
    public RestBean<Map<String, Object>> calculateDiscount(
            @PathVariable Long couponId,
            @RequestParam Double amount) {
        return handleRequest(couponId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public Map<String, Object> doInTransactionWithResult(Long couponId) {
                Double discountAmount = couponService.calculateDiscount(couponId, amount);
                Double finalPrice = amount - discountAmount; // 计算最终价格
                
                Map<String, Object> result = new HashMap<>();
                result.put("originalPrice", amount);
                result.put("discountAmount", discountAmount);
                result.put("finalPrice", finalPrice);
                return result;
            }
        });
    }

    /**
     * 获取用户优惠券数量统计
     */
    @GetMapping("/user/counts")
    public RestBean<Map<String, Integer>> getUserCouponCounts() {
        return handleRequest(null, log, new ExecuteCallbackWithoutReq<>() {
            @Override
            protected Map<String, Integer> doInTransactionWithoutReq() {
                return couponService.getUserCouponCounts(LoginUser.getLoginUserId());
            }
        });
    }

    /**
     * 获取优惠券详情（用户端）
     * 只能查看已发布且有效的优惠券
     */
    @GetMapping("/detail/{id}")
    public RestBean<CouponVo> getCouponDetail(@PathVariable Long id) {
        return handleRequest(id, log, new ExecuteCallbackWithResult<>() {
            @Override
            public CouponVo doInTransactionWithResult(Long id) {
                return couponService.getCouponDetailForUser(id, LoginUser.getLoginUserId());
            }
        });
    }
}
