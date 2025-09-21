package com.byrski.service;

import com.byrski.domain.entity.vo.request.CouponAddVo;
import com.byrski.domain.entity.vo.request.CouponBatchIssueVo;
import com.byrski.domain.entity.vo.request.CouponBatchIssueV2Vo;
import com.byrski.domain.entity.vo.response.CouponVo;
import com.byrski.domain.entity.vo.response.UserCouponVo;
import com.byrski.domain.entity.vo.response.CouponBatchIssueResultVo;
import com.byrski.domain.entity.vo.response.CouponIssuedUserVo;
import com.byrski.domain.entity.vo.request.CouponIssuePreviewCreateVo;
import com.byrski.domain.entity.vo.response.CouponIssuePreviewVo;
import com.byrski.domain.entity.vo.response.TradeDetailVo;

import java.util.List;
import java.util.Map;

/**
 * 优惠券服务接口
 */
public interface CouponService {

    /**
     * 创建优惠券
     * @param couponAddVo 优惠券添加请求
     * @return 优惠券ID
     */
    Long createCoupon(CouponAddVo couponAddVo);

    /**
     * 更新优惠券
     * @param couponId 优惠券ID
     * @param couponAddVo 优惠券更新请求
     * @return 是否成功
     */
    Boolean updateCoupon(Long couponId, CouponAddVo couponAddVo);

    /**
     * 删除优惠券
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    Boolean deleteCoupon(Long couponId);

    /**
     * 获取优惠券详情
     * @param couponId 优惠券ID
     * @return 优惠券详情
     */
    CouponVo getCouponDetail(Long couponId);

    /**
     * 获取优惠券列表
     * @param status 状态筛选
     * @param name 名称模糊搜索
     * @return 优惠券列表
     */
    List<CouponVo> getCouponList(Integer status, String name);

    /**
     * 启用优惠券
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    Boolean enableCoupon(Long couponId);

    /**
     * 停用优惠券
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    Boolean disableCoupon(Long couponId);

    /**
     * 批量发放优惠券（支持预览和直接发放）
     * 
     * 功能说明：
     * - 当 needPreview=true 或不传时：创建预览会话，返回预览信息
     * - 当 needPreview=false 时：直接发放，返回详细发放结果
     * 
     * @param batchIssueVo 批量发放请求
     * @return 预览结果或详细发放结果
     */
    Object batchIssueCoupon(CouponBatchIssueVo batchIssueVo);

    /**
     * 用户领取优惠券
     * @param couponId 优惠券ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean receiveCoupon(Long couponId, Long userId);

    /**
     * 获取用户优惠券列表
     * @param userId 用户ID
     * @param status 状态筛选
     * @return 用户优惠券列表
     */
    List<UserCouponVo> getUserCouponList(Long userId, Integer status);

    /**
     * 获取用户可用的优惠券列表
     * @param userId 用户ID
     * @param productId 产品ID
     * @param amount 订单金额
     * @param participants 参与人数
     * @return 可用优惠券列表
     */
    List<UserCouponVo> getAvailableCoupons(Long userId, String productId, Double amount, Integer participants);

    /**
     * 获取可领取的优惠券列表（页面领取类型）
     * @param userId 用户ID
     * @return 可领取的优惠券列表
     */
    List<CouponVo> getReceivableCoupons(Long userId);

    /**
     * 使用优惠券
     * @param userCouponId 用户优惠券ID
     * @param tradeId 订单ID
     * @param amount 订单金额
     * @return 优惠金额
     */
    Double useCoupon(Long userCouponId, Long tradeId, Double amount);

    /**
     * 退还优惠券
     * @param userCouponId 用户优惠券ID
     * @return 是否成功
     */
    Boolean refundCoupon(Long userCouponId);

    /**
     * 检查优惠券是否可用
     * @param couponId 优惠券ID
     * @param userId 用户ID
     * @param productId 产品ID
     * @param amount 订单金额
     * @param participants 参与人数
     * @return 是否可用
     */
    Boolean isCouponAvailable(Long couponId, Long userId, String productId, Double amount, Integer participants);
    
    /**
     * 检查用户优惠券是否可用（通过userCouponId）
     * @param userCouponId 用户优惠券ID
     * @param userId 用户ID
     * @param productId 产品ID
     * @param amount 订单金额
     * @param participants 参与人数
     * @return 是否可用
     */
    Boolean isUserCouponAvailable(Long userCouponId, Long userId, String productId, Double amount, Integer participants);

    /**
     * 计算优惠金额
     * @param couponId 优惠券ID
     * @param amount 订单金额
     * @return 优惠金额
     */
    Double calculateDiscount(Long couponId, Double amount);

    /**
     * 定时任务：更新过期优惠券状态
     */
    void updateExpiredCoupons();

    /**
     * 获取优惠券已发放用户列表
     * @param couponId 优惠券ID
     * @param status 用户优惠券状态筛选
     * @param username 用户名模糊搜索
     * @param phone 手机号模糊搜索
     * @param startTime 开始时间（领取时间范围）
     * @param endTime 结束时间（领取时间范围）
     * @return 已发放用户列表
     */
    List<CouponIssuedUserVo> getCouponIssuedUsers(Long couponId, Integer status, String username, String phone, String startTime, String endTime);

    /**
     * 创建发放预览会话（支持用户ID/手机号/Excel导入），返回预览ID与列表
     */
    CouponIssuePreviewVo createIssuePreview(CouponIssuePreviewCreateVo createVo);

    /**
     * 获取预览会话详情
     */
    CouponIssuePreviewVo getIssuePreview(String previewId);

    /**
     * 获取预览会话详情（支持按用户名/手机号搜索）
     * @param previewId 预览会话ID
     * @param username 用户名模糊查询
     * @param phone 手机号模糊查询
     */
    CouponIssuePreviewVo getIssuePreview(String previewId, String username, String phone);

    /**
     * 从预览会话中移除指定用户
     */
    CouponIssuePreviewVo removeUserFromIssuePreview(String previewId, Long userId);

    /**
     * 提交预览会话，按剩余名单发放
     */
    CouponBatchIssueResultVo submitIssuePreview(String previewId);

    /**
     * 作废预览会话
     */
    Boolean discardIssuePreview(String previewId);

    /**
     * 清理指定优惠券的重复待领取记录
     * 用于解决重复发放导致的问题
     */
    Boolean cleanDuplicateReceivableRecords(Long couponId);

    
    /**
     * 获取订单详情中的优惠券信息
     * @param userCouponId 用户优惠券ID
     * @return 优惠券详细信息
     */
    TradeDetailVo.CouponInfo getCouponInfoForTrade(Long userCouponId);

    /**
     * 批量发放优惠券 V2版本（支持两种页面领取模式）
     * 
     * 功能说明：
     * - 指定用户发放：管理员指定具体用户ID列表，只有这些用户能领取
     * - 全体用户抢票：所有登录小程序的用户都能领取（不超领取上限的前提下）
     * 
     * @param batchIssueV2Vo 批量发放请求V2
     * @return 发放结果
     */
    Object batchIssueCouponV2(CouponBatchIssueV2Vo batchIssueV2Vo);

    /**
     * 获取所有用户列表（用于全体用户抢票模式）
     * @return 所有用户ID列表
     */
    List<Long> getAllUserIds();

    /**
     * 获取用户优惠券数量统计
     * @param userId 用户ID
     * @return 优惠券数量统计信息
     */
    Map<String, Integer> getUserCouponCounts(Long userId);

    /**
     * 获取优惠券详情（用户端）
     * 只能查看已发布且有效的优惠券
     * @param couponId 优惠券ID
     * @param userId 用户ID
     * @return 优惠券详情
     */
    CouponVo getCouponDetailForUser(Long couponId, Long userId);
}
