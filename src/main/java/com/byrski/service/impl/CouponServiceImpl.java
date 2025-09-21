package com.byrski.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.byrski.common.exception.ByrSkiException;
import com.byrski.domain.entity.dto.*;
import com.byrski.domain.entity.vo.request.CouponAddVo;
import com.byrski.domain.entity.vo.request.CouponBatchIssueVo;
import com.byrski.domain.entity.vo.response.CouponVo;
import com.byrski.domain.entity.vo.response.UserCouponVo;
import com.byrski.domain.entity.vo.response.CouponBatchIssueResultVo;
import com.byrski.domain.entity.vo.response.CouponIssuedUserVo;
import com.byrski.domain.entity.vo.response.CouponIssuePreviewVo;
import com.byrski.domain.entity.vo.request.CouponIssuePreviewCreateVo;
import com.byrski.domain.entity.vo.response.TradeDetailVo;
import com.byrski.domain.entity.vo.request.CouponBatchIssueV2Vo;
import com.byrski.domain.entity.vo.response.CouponBatchIssueV2ResultVo;
import com.byrski.domain.enums.CouponPageReceiveMode;
import com.byrski.domain.entity.dto.CouponUserReceivable;
import java.util.UUID;
import com.byrski.domain.enums.CouponStatus;
import com.byrski.domain.enums.CouponType;
import com.byrski.domain.enums.CouponIssueMethod;
import com.byrski.domain.enums.ReturnCode;
import com.byrski.domain.enums.UserCouponStatus;
import com.byrski.domain.user.LoginUser;
import com.byrski.infrastructure.mapper.impl.*;
import com.byrski.infrastructure.repository.manager.ProductManager;
import com.byrski.service.CouponService;
import com.byrski.common.utils.UserValidationUtils;
import com.byrski.common.utils.ExcelUtils;
import com.byrski.common.validator.CouponValidator;

import com.byrski.common.event.CouponEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

/**
 * 优惠券服务实现类
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class CouponServiceImpl implements CouponService {

    private final CouponMapperService couponMapperService;
    private final UserCouponMapperService userCouponMapperService;
    private final AccountMapperService accountMapperService;
    private final CouponUserReceivableMapperService couponUserReceivableMapperService;
    private final ProductManager productManager;
    private final ActivityTemplateMapperService activityTemplateMapperService;
    private final TradeMapperService tradeMapperService;
    private final UserValidationUtils userValidationUtils;
    private final ExcelUtils excelUtils;
    private final CouponValidator couponValidator;

    private final ApplicationEventPublisher eventPublisher;

    // 预览会话内存存储: previewId -> 预览数据
    private final Map<String, PreviewSession> previewSessions = new ConcurrentHashMap<>();

    public CouponServiceImpl(CouponMapperService couponMapperService,
                           UserCouponMapperService userCouponMapperService,
                           AccountMapperService accountMapperService,
                           CouponUserReceivableMapperService couponUserReceivableMapperService,
                           ProductManager productManager,
                           ActivityTemplateMapperService activityTemplateMapperService,
                           TradeMapperService tradeMapperService,
                           UserValidationUtils userValidationUtils,
                           ExcelUtils excelUtils,
                           CouponValidator couponValidator,

                           ApplicationEventPublisher eventPublisher) {
        this.couponMapperService = couponMapperService;
        this.userCouponMapperService = userCouponMapperService;
        this.accountMapperService = accountMapperService;
        this.couponUserReceivableMapperService = couponUserReceivableMapperService;
        this.productManager = productManager;
        this.activityTemplateMapperService = activityTemplateMapperService;
        this.tradeMapperService = tradeMapperService;
        this.userValidationUtils = userValidationUtils;
        this.excelUtils = excelUtils;
        this.couponValidator = couponValidator;

        this.eventPublisher = eventPublisher;
    }

    @Override
    public Long createCoupon(CouponAddVo couponAddVo) {
        // 验证优惠券名称是否重复
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getName, couponAddVo.getName());
        if (couponMapperService.count(wrapper) > 0) {
            throw new ByrSkiException(ReturnCode.COUPON_NAME_EXISTS);
        }

        // 使用CouponValidator进行统一验证
        couponValidator.validateDiscountValue(couponAddVo.getType(), couponAddVo.getDiscountValue());
        couponValidator.validateCouponTime(couponAddVo.getStartTime(), couponAddVo.getEndTime());

        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(couponAddVo, coupon);
        coupon.setStatus(CouponStatus.DRAFT);
        coupon.setIssuedCount(0);
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setUpdateTime(LocalDateTime.now());
        coupon.setCreatorId(LoginUser.getLoginUserId());

        couponMapperService.save(coupon);
        log.info("创建优惠券成功，ID: {}", coupon.getId());
        return coupon.getId();
    }

    @Override
    public Boolean updateCoupon(Long couponId, CouponAddVo couponAddVo) {
        Coupon coupon = couponMapperService.getById(couponId);
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        // 验证优惠券名称是否重复（排除自己）
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getName, couponAddVo.getName())
                .ne(Coupon::getId, couponId);
        if (couponMapperService.count(wrapper) > 0) {
            throw new ByrSkiException(ReturnCode.COUPON_NAME_EXISTS);
        }

        // 使用CouponValidator进行统一验证
        couponValidator.validateDiscountValue(couponAddVo.getType(), couponAddVo.getDiscountValue());
        couponValidator.validateCouponTime(couponAddVo.getStartTime(), couponAddVo.getEndTime());

        BeanUtils.copyProperties(couponAddVo, coupon);
        coupon.setUpdateTime(LocalDateTime.now());

        boolean result = couponMapperService.updateById(coupon);
        log.info("更新优惠券成功，ID: {}", couponId);
        return result;
    }

    @Override
    public Boolean deleteCoupon(Long couponId) {
        Coupon coupon = couponMapperService.getById(couponId);
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        // 检查是否有用户已领取该优惠券
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getCouponId, couponId);
        if (userCouponMapperService.count(wrapper) > 0) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_AVAILABLE.getCode(), "该优惠券已被用户领取，无法删除");
        }

        boolean result = couponMapperService.removeById(couponId);
        log.info("删除优惠券成功，ID: {}", couponId);
        return result;
    }

    @Override
    public CouponVo getCouponDetail(Long couponId) {
        Coupon coupon = couponMapperService.getById(couponId);
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        return buildCouponVo(coupon);
    }

    @Override
    public List<CouponVo> getCouponList(Integer status, String name) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Coupon::getStatus, CouponStatus.fromCode(status));
        }
        if (StringUtils.hasText(name)) {
            wrapper.like(Coupon::getName, name);
        }
        wrapper.orderByDesc(Coupon::getCreateTime);

        List<Coupon> coupons = couponMapperService.list(wrapper);
        return coupons.stream().map(this::buildCouponVo).collect(Collectors.toList());
    }

    @Override
    public Boolean enableCoupon(Long couponId) {
        Coupon coupon = couponMapperService.getById(couponId);
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        coupon.setStatus(CouponStatus.ACTIVE);
        coupon.setUpdateTime(LocalDateTime.now());

        boolean result = couponMapperService.updateById(coupon);
        log.info("启用优惠券成功，ID: {}", couponId);
        return result;
    }

    @Override
    public Boolean disableCoupon(Long couponId) {
        Coupon coupon = couponMapperService.getById(couponId);
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        coupon.setStatus(CouponStatus.INACTIVE);
        coupon.setUpdateTime(LocalDateTime.now());

        boolean result = couponMapperService.updateById(coupon);
        log.info("停用优惠券成功，ID: {}", couponId);
        return result;
    }

    @Override
    public Object batchIssueCoupon(CouponBatchIssueVo batchIssueVo) {
        log.info("batchIssueCoupon - 开始处理批量发放请求 - issueMethod: {}, pageReceiveMode: {}, needPreview: {}",
                batchIssueVo.getIssueMethod(), batchIssueVo.getPageReceiveMode(), batchIssueVo.getNeedPreview());

        // 验证优惠券
        Coupon coupon = couponMapperService.getById(batchIssueVo.getCouponId());
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new ByrSkiException(ReturnCode.COUPON_INACTIVE);
        }

        // 检查发放数量限制
        if (coupon.getIssueLimit() > 0 &&
            coupon.getIssuedCount() + batchIssueVo.getQuantity() > coupon.getIssueLimit()) {
            throw new ByrSkiException(ReturnCode.COUPON_QUANTITY_EXCEEDED);
        }

        // 根据发放方式处理
        CouponIssueMethod issueMethod = CouponIssueMethod.fromCode(batchIssueVo.getIssueMethod());
        UserValidationUtils.UserValidationResult validationResult;

        if (issueMethod == CouponIssueMethod.PAGE_RECEIVE) {
            log.info("batchIssueCoupon - 进入页面领取分支 - pageReceiveMode: {}", batchIssueVo.getPageReceiveMode());
            // 页面领取方式 - 支持用户ID列表、手机号列表和Excel导入，将优惠券发放到指定用户的receivable列表中
            // 检查页面领取模式
            if (batchIssueVo.getPageReceiveMode() == null) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "页面领取模式不能为空");
            }

            CouponPageReceiveMode pageReceiveMode = CouponPageReceiveMode.fromCode(batchIssueVo.getPageReceiveMode());
            if (pageReceiveMode == null) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "不支持的页面领取模式");
            }

            if (pageReceiveMode == CouponPageReceiveMode.ALL_USERS_COMPETITION) {
                // 全体用户抢票模式
                if (batchIssueVo.getQuantity() == null || batchIssueVo.getQuantity() <= 0) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "全体用户抢票模式需要指定发放数量");
                }
                // 确保用户ID、手机号、Excel数据为空，因为是全体用户
                if ((batchIssueVo.getUserIds() != null && !batchIssueVo.getUserIds().isEmpty()) ||
                        (batchIssueVo.getPhoneNumbers() != null && !batchIssueVo.getPhoneNumbers().isEmpty()) ||
                        (batchIssueVo.getExcelBase64Data() != null && !batchIssueVo.getExcelBase64Data().isEmpty())) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "全体用户抢票模式不需要指定用户ID、手机号或Excel文件");
                }

                // 获取所有用户ID
                List<Long> allUserIds = getAllUserIds();
                if (allUserIds.isEmpty()) {
                    throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(), "没有找到任何用户");
                }

                // 检查用户领取上限
                List<Long> validUserIds = allUserIds.stream()
                        .filter(userId -> {
                            LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
                            wrapper.eq(UserCoupon::getCouponId, coupon.getId())
                                    .eq(UserCoupon::getUserId, userId);
                            return userCouponMapperService.count(wrapper) < coupon.getPerUserLimit();
                        })
                        .collect(Collectors.toList());

                if (validUserIds.isEmpty()) {
                    throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(), "所有用户都已达到领取上限");
                }

                // 直接发放到全体用户的receivable列表中
                CouponBatchIssueV2ResultVo v2Result = executeAllUsersCompetitionToReceivable(coupon, validUserIds, batchIssueVo.getQuantity(), batchIssueVo.getRemark());

                // 转换为V1格式
                return CouponBatchIssueResultVo.builder()
                        .couponId(v2Result.getCouponId())
                        .couponName(v2Result.getCouponName())
                        .successCount(v2Result.getSuccessCount())
                        .skipCount(v2Result.getSkipCount())
                        .invalidUserIdCount(v2Result.getInvalidUserIdCount())
                        .invalidPhoneCount(v2Result.getInvalidPhoneCount())
                        .invalidUserIds(v2Result.getInvalidUserIds())
                        .invalidPhoneNumbers(v2Result.getInvalidPhoneNumbers())
                        .skipReasons(v2Result.getSkipReasons())
                        .successUsers(v2Result.getSuccessUsers().stream()
                                .map(user -> CouponBatchIssueResultVo.UserIssueInfo.builder()
                                        .userId(user.getUserId())
                                        .username(user.getUsername())
                                        .phone(user.getPhone())
                                        .issueTime(LocalDateTime.now().toString())
                                        .build())
                                .collect(Collectors.toList()))
                        .build();
            } else {
                // 指定用户发放模式
                if ((batchIssueVo.getUserIds() == null || batchIssueVo.getUserIds().isEmpty()) &&
                        (batchIssueVo.getPhoneNumbers() == null || batchIssueVo.getPhoneNumbers().isEmpty()) &&
                        (batchIssueVo.getExcelBase64Data() == null || batchIssueVo.getExcelBase64Data().isEmpty())) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "指定用户发放模式需要提供用户ID、手机号或Excel文件");
                }
            }
            List<String> mergedPhones = new ArrayList<>();
            if (batchIssueVo.getPhoneNumbers() != null) {
                mergedPhones.addAll(batchIssueVo.getPhoneNumbers());
            }
            if (batchIssueVo.getExcelBase64Data() != null && batchIssueVo.getFileName() != null) {
                if (!excelUtils.isValidExcelFile(batchIssueVo.getFileName())) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "不支持的文件格式，请上传Excel文件(.xlsx或.xls)");
                }
                List<String> excelPhones;
                try {
                    excelPhones = excelUtils.parsePhoneNumbersFromBase64(batchIssueVo.getExcelBase64Data(),
                            batchIssueVo.getPhoneColumnName());
                } catch (Exception e) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "解析Excel文件失败: " + e.getMessage());
                }
                if (excelPhones != null && !excelPhones.isEmpty()) {
                    mergedPhones.addAll(excelPhones);
                }
            }

            // 验证用户
            validationResult = userValidationUtils.validateUsers(batchIssueVo.getUserIds(), mergedPhones);

            // 记录验证结果
            if (validationResult.getInvalidUserIdCount() > 0) {
                log.warn("页面领取优惠券发放 - 无效用户ID: {}", String.join(", ", validationResult.getInvalidUserIds()));
            }
            if (validationResult.getInvalidPhoneCount() > 0) {
                log.warn("页面领取优惠券发放 - 无效手机号: {}", String.join(", ", validationResult.getInvalidPhoneNumbers()));
            }

            if (!validationResult.hasValidUsers()) {
                throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(),
                    "没有找到有效的用户。无效用户ID: " + validationResult.getInvalidUserIdCount() + "个，无效手机号: " + validationResult.getInvalidPhoneCount() + "个");
            }

            // 检查是否需要预览
            log.info("batchIssueCoupon - 页面领取-指定用户模式 - 检查预览模式 - needPreview: {}, 是否为true: {}, 是否为null: {}",
                    batchIssueVo.getNeedPreview(),
                    Boolean.TRUE.equals(batchIssueVo.getNeedPreview()),
                    batchIssueVo.getNeedPreview() == null);

            if (Boolean.TRUE.equals(batchIssueVo.getNeedPreview()) || batchIssueVo.getNeedPreview() == null) {
                // 创建预览会话
                log.info("batchIssueCoupon - 页面领取-指定用户模式 - 进入预览模式，调用createIssuePreviewFromBatchIssue方法");
                return createIssuePreviewFromBatchIssue(batchIssueVo, coupon, validationResult);
            }

            log.info("batchIssueCoupon - 页面领取-指定用户模式 - 跳过预览模式，直接执行发放逻辑");

            // 创建关联表记录，将优惠券发放到指定用户的receivable列表中
            int successCount = 0;
            List<CouponBatchIssueResultVo.UserIssueInfo> successUsers = new ArrayList<>();

            for (Long userId : validationResult.getValidUserIds()) {
                try {
                    // 检查是否已存在关联记录
                    LambdaQueryWrapper<CouponUserReceivable> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(CouponUserReceivable::getCouponId, coupon.getId())
                            .eq(CouponUserReceivable::getUserId, userId);

                    if (couponUserReceivableMapperService.count(wrapper) > 0) {
                        continue; // 已存在，跳过
                    }

                    // 创建关联记录
                    CouponUserReceivable receivable = CouponUserReceivable.builder()
                            .couponId(coupon.getId())
                            .userId(userId)
                            .receiveMode(CouponPageReceiveMode.SPECIFIC_USERS.getCode())
                            .isReceived(false)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();

                    boolean result = couponUserReceivableMapperService.save(receivable);
                    if (result) {
                        Account account = validationResult.getValidUsers().get(userId);
                        successUsers.add(CouponBatchIssueResultVo.UserIssueInfo.builder()
                                .userId(userId)
                                .username(account != null ? account.getUsername() : "未知")
                                .phone(account != null ? account.getPhone() : "")
                                .build());
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("创建优惠券关联记录失败，用户ID: {}, 优惠券ID: {}", userId, coupon.getId(), e);
                }
            }

            log.info("页面领取类型优惠券已发放到指定用户的receivable列表中，优惠券ID: {}, 成功数量: {}",
                coupon.getId(), successCount);

            return CouponBatchIssueResultVo.builder()
                    .couponId(coupon.getId())
                    .couponName(coupon.getName())
                    .successCount(successCount)
                    .skipCount(validationResult.getValidUserIds().size() - successCount)
                    .invalidUserIdCount(validationResult.getInvalidUserIdCount())
                    .invalidPhoneCount(validationResult.getInvalidPhoneCount())
                    .invalidUserIds(validationResult.getInvalidUserIds())
                    .invalidPhoneNumbers(validationResult.getInvalidPhoneNumbers())
                    .skipReasons(new ArrayList<>())
                    .successUsers(successUsers)
                    .build();

        } else {
            // 批量导入方式 - 支持用户ID列表和手机号列表
            List<String> mergedPhones = new ArrayList<>();
            if (batchIssueVo.getPhoneNumbers() != null) {
                mergedPhones.addAll(batchIssueVo.getPhoneNumbers());
            }
            if (batchIssueVo.getExcelBase64Data() != null && batchIssueVo.getFileName() != null) {
                if (!excelUtils.isValidExcelFile(batchIssueVo.getFileName())) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "不支持的文件格式，请上传Excel文件(.xlsx或.xls)");
                }
                List<String> excelPhones;
                try {
                    excelPhones = excelUtils.parsePhoneNumbersFromBase64(batchIssueVo.getExcelBase64Data(),
                            batchIssueVo.getPhoneColumnName());
                } catch (Exception e) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "解析Excel文件失败: " + e.getMessage());
                }
                if (excelPhones != null && !excelPhones.isEmpty()) {
                    mergedPhones.addAll(excelPhones);
                }
            }

            validationResult = userValidationUtils.validateUsers(batchIssueVo.getUserIds(), mergedPhones);

            // 记录验证结果
            if (validationResult.getInvalidUserIdCount() > 0) {
                log.warn("批量发放优惠券 - 无效用户ID: {}", String.join(", ", validationResult.getInvalidUserIds()));
            }
            if (validationResult.getInvalidPhoneCount() > 0) {
                log.warn("批量发放优惠券 - 无效手机号: {}", String.join(", ", validationResult.getInvalidPhoneNumbers()));
            }

            if (!validationResult.hasValidUsers()) {
                throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(),
                    "没有找到有效的用户。无效用户ID: " + validationResult.getInvalidUserIdCount() + "个，无效手机号: " + validationResult.getInvalidPhoneCount() + "个");
            }

            // 检查是否需要预览
            log.info("batchIssueCoupon - 批量导入方式 - 检查预览模式 - needPreview: {}, 是否为true: {}, 是否为null: {}",
                    batchIssueVo.getNeedPreview(),
                    Boolean.TRUE.equals(batchIssueVo.getNeedPreview()),
                    batchIssueVo.getNeedPreview() == null);

            if (Boolean.TRUE.equals(batchIssueVo.getNeedPreview()) || batchIssueVo.getNeedPreview() == null) {
                // 创建预览会话
                log.info("batchIssueCoupon - 批量导入方式 - 进入预览模式，调用createIssuePreviewFromBatchIssue方法");
                return createIssuePreviewFromBatchIssue(batchIssueVo, coupon, validationResult);
            }

            log.info("batchIssueCoupon - 批量导入方式 - 跳过预览模式，直接执行发放逻辑");
        }

        List<Long> validUserIds = validationResult.getValidUserIds();

        // 批量发放优惠券
        int successCount = 0;
        int skipCount = 0;
        List<String> skipReasons = new ArrayList<>();
        List<CouponBatchIssueResultVo.UserIssueInfo> successUsers = new ArrayList<>();

        for (Long userId : validUserIds) {
            try {
                // 检查用户是否已领取过该优惠券
                LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(UserCoupon::getCouponId, coupon.getId())
                        .eq(UserCoupon::getUserId, userId);

                long currentCount = userCouponMapperService.count(wrapper);
                if (currentCount >= coupon.getPerUserLimit()) {
                    Account account = validationResult.getValidUsers().get(userId);
                    String reason = String.format("用户 %s (%s) 已达到领取上限: %d/%d",
                        userId, account != null ? account.getUsername() : "未知", currentCount, coupon.getPerUserLimit());
                    skipReasons.add(reason);
                    skipCount++;
                    continue;
                }

                // 创建用户优惠券记录
                UserCoupon userCoupon = UserCoupon.builder()
                        .userId(userId)
                        .couponId(coupon.getId())
                        .status(UserCouponStatus.UNUSED)
                        .receiveTime(LocalDateTime.now())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();

                boolean saved = userCouponMapperService.save(userCoupon);
                if (saved) {
                    successCount++;
                    Account account = validationResult.getValidUsers().get(userId);
                    log.info("成功为用户 {} ({}) 发放优惠券 {} [{}]", userId,
                        account != null ? account.getUsername() : "未知", coupon.getName(), issueMethod.getDescription());

                    // 添加到成功用户列表
                    CouponBatchIssueResultVo.UserIssueInfo userInfo = CouponBatchIssueResultVo.UserIssueInfo.builder()
                            .userId(userId)
                            .username(account != null ? account.getUsername() : "未知")
                            .phone(account != null ? account.getPhone() : "")
                            .issueTime(LocalDateTime.now().toString())
                            .build();
                    successUsers.add(userInfo);
                } else {
                    log.error("为用户 {} 发放优惠券失败: 数据库保存失败", userId);
                }
            } catch (Exception e) {
                log.error("为用户 {} 发放优惠券失败: {}", userId, e.getMessage());
                skipCount++;
            }
        }

        // 更新优惠券发放数量
        couponMapperService.update()
                .setSql("issued_count = issued_count + " + successCount)
                .eq("id", coupon.getId())
                .update();

        return CouponBatchIssueResultVo.builder()
                .couponId(coupon.getId())
                .couponName(coupon.getName())
                .successCount(successCount)
                .skipCount(skipCount)
                .invalidUserIdCount(validationResult.getInvalidUserIdCount())
                .invalidPhoneCount(validationResult.getInvalidPhoneCount())
                .invalidUserIds(validationResult.getInvalidUserIds())
                .invalidPhoneNumbers(validationResult.getInvalidPhoneNumbers())
                .skipReasons(skipReasons)
                .successUsers(successUsers)
                .build();
    }

    /**
     * 从批量发放请求创建预览会话
     */

    /**
     * 执行批量发放并返回详细结果
     */
    private Object executeBatchIssueWithDetail(CouponBatchIssueVo batchIssueVo) {
        Coupon coupon = couponMapperService.getById(batchIssueVo.getCouponId());
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new ByrSkiException(ReturnCode.COUPON_INACTIVE);
        }

        // 检查发放数量限制
        if (coupon.getIssueLimit() > 0 &&
            coupon.getIssuedCount() + batchIssueVo.getQuantity() > coupon.getIssueLimit()) {
            throw new ByrSkiException(ReturnCode.COUPON_QUANTITY_EXCEEDED);
        }

        // 根据发放方式处理
        CouponIssueMethod issueMethod = CouponIssueMethod.fromCode(batchIssueVo.getIssueMethod());
        UserValidationUtils.UserValidationResult validationResult;

        if (issueMethod == CouponIssueMethod.PAGE_RECEIVE) {
            log.info("batchIssueCoupon - 进入页面领取分支 - pageReceiveMode: {}", batchIssueVo.getPageReceiveMode());
            // 页面领取方式 - 支持用户ID列表、手机号列表和Excel导入，将优惠券发放到指定用户的receivable列表中
            // 检查页面领取模式
            if (batchIssueVo.getPageReceiveMode() == null) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "页面领取模式不能为空");
            }

            CouponPageReceiveMode pageReceiveMode = CouponPageReceiveMode.fromCode(batchIssueVo.getPageReceiveMode());
            if (pageReceiveMode == null) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "不支持的页面领取模式");
            }

            if (pageReceiveMode == CouponPageReceiveMode.ALL_USERS_COMPETITION) {
                // 全体用户抢票模式
                if (batchIssueVo.getQuantity() == null || batchIssueVo.getQuantity() <= 0) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "全体用户抢票模式需要指定发放数量");
                }
                // 确保用户ID、手机号、Excel数据为空，因为是全体用户
                if ((batchIssueVo.getUserIds() != null && !batchIssueVo.getUserIds().isEmpty()) ||
                        (batchIssueVo.getPhoneNumbers() != null && !batchIssueVo.getPhoneNumbers().isEmpty()) ||
                        (batchIssueVo.getExcelBase64Data() != null && !batchIssueVo.getExcelBase64Data().isEmpty())) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "全体用户抢票模式不需要指定用户ID、手机号或Excel文件");
                }

                // 获取所有用户ID
                List<Long> allUserIds = getAllUserIds();
                if (allUserIds.isEmpty()) {
                    throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(), "没有找到任何用户");
                }

                // 检查用户领取上限
                List<Long> validUserIds = allUserIds.stream()
                        .filter(userId -> {
                            LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
                            wrapper.eq(UserCoupon::getCouponId, coupon.getId())
                                    .eq(UserCoupon::getUserId, userId);
                            return userCouponMapperService.count(wrapper) < coupon.getPerUserLimit();
                        })
                        .collect(Collectors.toList());

                if (validUserIds.isEmpty()) {
                    throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(), "所有用户都已达到领取上限");
                }

                // 直接发放到全体用户的receivable列表中
                CouponBatchIssueV2ResultVo v2Result = executeAllUsersCompetitionToReceivable(coupon, validUserIds, batchIssueVo.getQuantity(), batchIssueVo.getRemark());

                // 转换为V1格式
                return CouponBatchIssueResultVo.builder()
                        .couponId(v2Result.getCouponId())
                        .couponName(v2Result.getCouponName())
                        .successCount(v2Result.getSuccessCount())
                        .skipCount(v2Result.getSkipCount())
                        .invalidUserIdCount(v2Result.getInvalidUserIdCount())
                        .invalidPhoneCount(v2Result.getInvalidPhoneCount())
                        .invalidUserIds(v2Result.getInvalidUserIds())
                        .invalidPhoneNumbers(v2Result.getInvalidPhoneNumbers())
                        .skipReasons(v2Result.getSkipReasons())
                        .successUsers(v2Result.getSuccessUsers().stream()
                                .map(user -> CouponBatchIssueResultVo.UserIssueInfo.builder()
                                        .userId(user.getUserId())
                                        .username(user.getUsername())
                                        .phone(user.getPhone())
                                        .issueTime(LocalDateTime.now().toString())
                                        .build())
                                .collect(Collectors.toList()))
                        .build();
            } else {
                // 指定用户发放模式
                if ((batchIssueVo.getUserIds() == null || batchIssueVo.getUserIds().isEmpty()) &&
                        (batchIssueVo.getPhoneNumbers() == null || batchIssueVo.getPhoneNumbers().isEmpty()) &&
                        (batchIssueVo.getExcelBase64Data() == null || batchIssueVo.getExcelBase64Data().isEmpty())) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "指定用户发放模式需要提供用户ID、手机号或Excel文件");
                }
            }
            List<String> mergedPhones = new ArrayList<>();
            if (batchIssueVo.getPhoneNumbers() != null) {
                mergedPhones.addAll(batchIssueVo.getPhoneNumbers());
            }
            if (batchIssueVo.getExcelBase64Data() != null && batchIssueVo.getFileName() != null) {
                if (!excelUtils.isValidExcelFile(batchIssueVo.getFileName())) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "不支持的文件格式，请上传Excel文件(.xlsx或.xls)");
                }
                List<String> excelPhones;
                try {
                    excelPhones = excelUtils.parsePhoneNumbersFromBase64(batchIssueVo.getExcelBase64Data(),
                            batchIssueVo.getPhoneColumnName());
                } catch (Exception e) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "解析Excel文件失败: " + e.getMessage());
                }
                if (excelPhones != null && !excelPhones.isEmpty()) {
                    mergedPhones.addAll(excelPhones);
                }
            }

            // 验证用户
            validationResult = userValidationUtils.validateUsers(batchIssueVo.getUserIds(), mergedPhones);

            // 记录验证结果
            if (validationResult.getInvalidUserIdCount() > 0) {
                log.warn("页面领取优惠券发放 - 无效用户ID: {}", String.join(", ", validationResult.getInvalidUserIds()));
            }
            if (validationResult.getInvalidPhoneCount() > 0) {
                log.warn("页面领取优惠券发放 - 无效手机号: {}", String.join(", ", validationResult.getInvalidPhoneNumbers()));
            }

            if (!validationResult.hasValidUsers()) {
                throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(),
                    "没有找到有效的用户。无效用户ID: " + validationResult.getInvalidUserIdCount() + "个，无效手机号: " + validationResult.getInvalidPhoneCount() + "个");
            }

            // 检查是否需要预览
            log.info("batchIssueCoupon - 页面领取-指定用户模式 - 检查预览模式 - needPreview: {}, 是否为true: {}, 是否为null: {}",
                    batchIssueVo.getNeedPreview(),
                    Boolean.TRUE.equals(batchIssueVo.getNeedPreview()),
                    batchIssueVo.getNeedPreview() == null);

            if (Boolean.TRUE.equals(batchIssueVo.getNeedPreview()) || batchIssueVo.getNeedPreview() == null) {
                // 创建预览会话
                log.info("batchIssueCoupon - 页面领取-指定用户模式 - 进入预览模式，调用createIssuePreviewFromBatchIssue方法");
                return createIssuePreviewFromBatchIssue(batchIssueVo, coupon, validationResult);
            }

            log.info("batchIssueCoupon - 页面领取-指定用户模式 - 跳过预览模式，直接执行发放逻辑");

            // 创建关联表记录，将优惠券发放到指定用户的receivable列表中
            int successCount = 0;
            List<CouponBatchIssueResultVo.UserIssueInfo> successUsers = new ArrayList<>();

            for (Long userId : validationResult.getValidUserIds()) {
                try {
                    // 检查是否已存在关联记录
                    LambdaQueryWrapper<CouponUserReceivable> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(CouponUserReceivable::getCouponId, coupon.getId())
                            .eq(CouponUserReceivable::getUserId, userId);

                    if (couponUserReceivableMapperService.count(wrapper) > 0) {
                        continue; // 已存在，跳过
                    }

                    // 创建关联记录
                    CouponUserReceivable receivable = CouponUserReceivable.builder()
                            .couponId(coupon.getId())
                            .userId(userId)
                            .receiveMode(CouponPageReceiveMode.SPECIFIC_USERS.getCode())
                            .isReceived(false)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();

                    boolean result = couponUserReceivableMapperService.save(receivable);
                    if (result) {
                        Account account = validationResult.getValidUsers().get(userId);
                        successUsers.add(CouponBatchIssueResultVo.UserIssueInfo.builder()
                                .userId(userId)
                                .username(account != null ? account.getUsername() : "未知")
                                .phone(account != null ? account.getPhone() : "")
                                .build());
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("创建优惠券关联记录失败，用户ID: {}, 优惠券ID: {}", userId, coupon.getId(), e);
                }
            }

            log.info("页面领取类型优惠券已发放到指定用户的receivable列表中，优惠券ID: {}, 成功数量: {}",
                coupon.getId(), successCount);

            return CouponBatchIssueResultVo.builder()
                    .couponId(coupon.getId())
                    .couponName(coupon.getName())
                    .successCount(successCount)
                    .skipCount(validationResult.getValidUserIds().size() - successCount)
                    .invalidUserIdCount(validationResult.getInvalidUserIdCount())
                    .invalidPhoneCount(validationResult.getInvalidPhoneCount())
                    .invalidUserIds(validationResult.getInvalidUserIds())
                    .invalidPhoneNumbers(validationResult.getInvalidPhoneNumbers())
                    .skipReasons(new ArrayList<>())
                    .successUsers(successUsers)
                    .build();

        } else {
            // 批量导入方式 - 支持用户ID列表和手机号列表
            List<String> mergedPhones = new ArrayList<>();
            if (batchIssueVo.getPhoneNumbers() != null) {
                mergedPhones.addAll(batchIssueVo.getPhoneNumbers());
            }
            if (batchIssueVo.getExcelBase64Data() != null && batchIssueVo.getFileName() != null) {
                if (!excelUtils.isValidExcelFile(batchIssueVo.getFileName())) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "不支持的文件格式，请上传Excel文件(.xlsx或.xls)");
                }
                List<String> excelPhones;
                try {
                    excelPhones = excelUtils.parsePhoneNumbersFromBase64(batchIssueVo.getExcelBase64Data(),
                            batchIssueVo.getPhoneColumnName());
                } catch (Exception e) {
                    throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "解析Excel文件失败: " + e.getMessage());
                }
                if (excelPhones != null && !excelPhones.isEmpty()) {
                    mergedPhones.addAll(excelPhones);
                }
            }

            validationResult = userValidationUtils.validateUsers(batchIssueVo.getUserIds(), mergedPhones);

            // 记录验证结果
            if (validationResult.getInvalidUserIdCount() > 0) {
                log.warn("批量发放优惠券 - 无效用户ID: {}", String.join(", ", validationResult.getInvalidUserIds()));
            }
            if (validationResult.getInvalidPhoneCount() > 0) {
                log.warn("批量发放优惠券 - 无效手机号: {}", String.join(", ", validationResult.getInvalidPhoneNumbers()));
            }

            if (!validationResult.hasValidUsers()) {
                throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(),
                    "没有找到有效的用户。无效用户ID: " + validationResult.getInvalidUserIdCount() + "个，无效手机号: " + validationResult.getInvalidPhoneCount() + "个");
            }

            // 检查是否需要预览
            log.info("batchIssueCoupon - 批量导入方式 - 检查预览模式 - needPreview: {}, 是否为true: {}, 是否为null: {}",
                    batchIssueVo.getNeedPreview(),
                    Boolean.TRUE.equals(batchIssueVo.getNeedPreview()),
                    batchIssueVo.getNeedPreview() == null);

            if (Boolean.TRUE.equals(batchIssueVo.getNeedPreview()) || batchIssueVo.getNeedPreview() == null) {
                // 创建预览会话
                log.info("batchIssueCoupon - 批量导入方式 - 进入预览模式，调用createIssuePreviewFromBatchIssue方法");
                return createIssuePreviewFromBatchIssue(batchIssueVo, coupon, validationResult);
            }

            log.info("batchIssueCoupon - 批量导入方式 - 跳过预览模式，直接执行发放逻辑");
        }

        List<Long> validUserIds = validationResult.getValidUserIds();

        // 批量发放优惠券
        int successCount = 0;
        int skipCount = 0;
        List<String> skipReasons = new ArrayList<>();
        List<CouponBatchIssueResultVo.UserIssueInfo> successUsers = new ArrayList<>();

        for (Long userId : validUserIds) {
            try {
                // 检查用户是否已领取过该优惠券
                LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(UserCoupon::getCouponId, coupon.getId())
                        .eq(UserCoupon::getUserId, userId);

                long currentCount = userCouponMapperService.count(wrapper);
                if (currentCount >= coupon.getPerUserLimit()) {
                    Account account = validationResult.getValidUsers().get(userId);
                    String reason = String.format("用户 %s (%s) 已达到领取上限: %d/%d",
                        userId, account != null ? account.getUsername() : "未知", currentCount, coupon.getPerUserLimit());
                    skipReasons.add(reason);
                    skipCount++;
                    continue;
                }

                // 创建用户优惠券记录
                UserCoupon userCoupon = UserCoupon.builder()
                        .userId(userId)
                        .couponId(coupon.getId())
                        .status(UserCouponStatus.UNUSED)
                        .receiveTime(LocalDateTime.now())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();

                boolean saved = userCouponMapperService.save(userCoupon);
                if (saved) {
                    successCount++;
                    Account account = validationResult.getValidUsers().get(userId);
                    log.info("成功为用户 {} ({}) 发放优惠券 {} [{}]", userId,
                        account != null ? account.getUsername() : "未知", coupon.getName(), issueMethod.getDescription());

                    // 添加到成功用户列表
                    CouponBatchIssueResultVo.UserIssueInfo userInfo = CouponBatchIssueResultVo.UserIssueInfo.builder()
                            .userId(userId)
                            .username(account != null ? account.getUsername() : "未知")
                            .phone(account != null ? account.getPhone() : "")
                            .issueTime(LocalDateTime.now().toString())
                            .build();
                    successUsers.add(userInfo);
                } else {
                    log.error("为用户 {} 发放优惠券失败: 数据库保存失败", userId);
                }
            } catch (Exception e) {
                log.error("为用户 {} 发放优惠券失败: {}", userId, e.getMessage());
                skipCount++;
            }
        }

        // 更新优惠券已发放数量
        if (successCount > 0) {
            coupon.setIssuedCount(coupon.getIssuedCount() + successCount);
            couponMapperService.updateById(coupon);
        }

        // 记录详细的发放结果
        log.info("批量发放优惠券完成 - 优惠券ID: {}, 方式: {}, 成功: {}, 跳过: {}, 无效用户ID: {}, 无效手机号: {}",
            coupon.getId(), issueMethod.getDescription(), successCount, skipCount, validationResult.getInvalidUserIdCount(), validationResult.getInvalidPhoneCount());

        if (!skipReasons.isEmpty()) {
            log.info("跳过的原因: {}", String.join("; ", skipReasons));
        }

        // 构建详细结果
        return CouponBatchIssueResultVo.builder()
                .couponId(coupon.getId())
                .couponName(coupon.getName())
                .successCount(successCount)
                .skipCount(skipCount)
                .invalidUserIdCount(validationResult.getInvalidUserIdCount())
                .invalidPhoneCount(validationResult.getInvalidPhoneCount())
                .invalidUserIds(validationResult.getInvalidUserIds())
                .invalidPhoneNumbers(validationResult.getInvalidPhoneNumbers())
                .skipReasons(skipReasons)
                .successUsers(successUsers)
                .build();
    }

    @Override
    public Boolean receiveCoupon(Long couponId, Long userId) {
        // 检查用户是否在可领取列表中
        LambdaQueryWrapper<CouponUserReceivable> receivableWrapper = new LambdaQueryWrapper<>();
        receivableWrapper.eq(CouponUserReceivable::getCouponId, couponId)
                .eq(CouponUserReceivable::getUserId, userId)
                .eq(CouponUserReceivable::getIsReceived, false);

        CouponUserReceivable receivable = couponUserReceivableMapperService.getOne(receivableWrapper);
        if (receivable == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST.getCode(), "您没有权限领取此优惠券");
        }

        Coupon coupon = couponMapperService.getById(couponId);
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new ByrSkiException(ReturnCode.COUPON_INACTIVE);
        }

        // 检查用户是否已领取过该优惠券
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getUserId, userId);

        if (coupon.getPerUserLimit() != null && coupon.getPerUserLimit() > 0
                && userCouponMapperService.count(wrapper) >= coupon.getPerUserLimit()) {
            throw new ByrSkiException(ReturnCode.COUPON_QUANTITY_EXCEEDED.getCode(),
                String.format("您已达到该优惠券的领取上限: %d/%d",
                    userCouponMapperService.count(wrapper), coupon.getPerUserLimit()));
        }

        // 使用乐观锁更新优惠券发放数量
        boolean updated = couponMapperService.update()
                .setSql("issued_count = issued_count + 1")
                .eq("id", couponId)
                .eq("status", CouponStatus.ACTIVE)
                .apply("(issue_limit = 0 OR issued_count < issue_limit)") // 检查数量限制
                .update();

        if (!updated) {
            throw new ByrSkiException(ReturnCode.COUPON_QUANTITY_EXCEEDED);
        }

        // 创建用户优惠券记录
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(UserCouponStatus.UNUSED)
                .receiveTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        boolean result = userCouponMapperService.save(userCoupon);
        if (!result) {
            // 如果保存失败，回滚优惠券发放数量
            couponMapperService.update()
                    .setSql("issued_count = issued_count - 1")
                    .eq("id", couponId)
                    .update();
            throw new ByrSkiException(ReturnCode.COUPON_RECEIVE_FAILED);
        }

        // 更新关联表状态为已领取
        receivable.setIsReceived(true);
        receivable.setReceiveTime(LocalDateTime.now());
        receivable.setUpdateTime(LocalDateTime.now());
        couponUserReceivableMapperService.updateById(receivable);

        // 发布优惠券领取事件
        try {
            eventPublisher.publishEvent(new CouponEvent.CouponReceivedEvent(this, couponId, userId, coupon.getName()));
        } catch (Exception e) {
            log.warn("发布优惠券领取事件失败，用户ID: {}, 优惠券ID: {}", userId, couponId, e);
        }

        log.info("用户 {} 领取优惠券成功，优惠券ID: {}", userId, couponId);
        return result;
    }

    @Override
    public List<UserCouponVo> getUserCouponList(Long userId, Integer status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, UserCouponStatus.fromCode(status));
        }
        wrapper.orderByDesc(UserCoupon::getCreateTime);

        List<UserCoupon> userCoupons = userCouponMapperService.list(wrapper);
        return userCoupons.stream().map(this::buildUserCouponVo).collect(Collectors.toList());
    }

    @Override
    public List<UserCouponVo> getAvailableCoupons(Long userId, String productId, Double amount, Integer participants) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, UserCouponStatus.UNUSED);
        wrapper.orderByDesc(UserCoupon::getCreateTime);

        List<UserCoupon> userCoupons = userCouponMapperService.list(wrapper);

        return userCoupons.stream()
                .filter(userCoupon -> isCouponAvailable(userCoupon.getCouponId(), userId, productId, amount, participants))
                .map(this::buildUserCouponVo)
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponVo> getReceivableCoupons(Long userId) {
        // 优化：使用JOIN查询一次性获取用户可领取的优惠券信息
        // 避免两次数据库查询，提升性能
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.exists("SELECT 1 FROM coupon_user_receivable cur " +
                      "WHERE cur.coupon_id = coupon.id " +
                      "AND cur.user_id = {0} " +
                      "AND cur.is_received = false", userId)
                .eq(Coupon::getStatus, CouponStatus.ACTIVE)
                .le(Coupon::getStartTime, LocalDateTime.now()) // 已生效
                .ge(Coupon::getEndTime, LocalDateTime.now())   // 未过期
                .and(w -> w.eq(Coupon::getIssueLimit, 0)       // 无限制
                        .or()
                        .apply("issued_count < issue_limit"))  // 或未达到发放上限
                .orderByDesc(Coupon::getCreateTime);

        List<Coupon> coupons = couponMapperService.list(wrapper);

        if (coupons.isEmpty()) {
            return new ArrayList<>();
        }

        return coupons.stream()
                .map(this::buildCouponVo)
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否可以领取指定优惠券
     */
    private boolean canUserReceiveCoupon(Coupon coupon, Long userId) {
        // 检查用户是否已达到领取上限
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getCouponId, coupon.getId())
                .eq(UserCoupon::getUserId, userId);

        if (coupon.getPerUserLimit() == null || coupon.getPerUserLimit() <= 0) {
            return true;
        }
        long currentCount = userCouponMapperService.count(wrapper);
        return currentCount < coupon.getPerUserLimit();
    }

    /**
     * 批量查询用户已领取的优惠券数量
     * 优化：避免在循环中逐个查询数据库
     */
    private Map<Long, Integer> getUserCouponCounts(Long couponId, List<Long> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // 使用批量查询替代循环中的单个查询
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getCouponId, couponId)
                .in(UserCoupon::getUserId, userIds);
        
        List<UserCoupon> userCoupons = userCouponMapperService.list(wrapper);
        
        // 按用户ID分组统计数量
        return userCoupons.stream()
                .collect(Collectors.groupingBy(
                    UserCoupon::getUserId,
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
    }

    /**
     * 批量查询用户信息
     * 优化：避免在循环中逐个查询用户信息
     */
    private Map<Long, Account> getUserMapByIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<Account> accounts = accountMapperService.listByIds(userIds);
        return accounts.stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
    }

    /**
     * 批量查询用户是否已有待领取记录
     * 优化：避免在循环中逐个检查
     */
    private Set<Long> getExistingReceivableUserIds(Long couponId, List<Long> userIds) {
        if (userIds.isEmpty()) {
            return new HashSet<>();
        }
        
        LambdaQueryWrapper<CouponUserReceivable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponUserReceivable::getCouponId, couponId)
                .in(CouponUserReceivable::getUserId, userIds);
        
        List<CouponUserReceivable> receivables = couponUserReceivableMapperService.list(wrapper);
        return receivables.stream()
                .map(CouponUserReceivable::getUserId)
                .collect(Collectors.toSet());
    }

    @Override
    public Double useCoupon(Long userCouponId, Long tradeId, Double amount) {
        UserCoupon userCoupon = userCouponMapperService.getById(userCouponId);
        if (userCoupon == null) {
            throw new ByrSkiException(ReturnCode.USER_COUPON_NOT_EXIST);
        }

        if (userCoupon.getStatus() != UserCouponStatus.UNUSED) {
            throw new ByrSkiException(ReturnCode.COUPON_ALREADY_USED);
        }

        Coupon coupon = couponMapperService.getById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        // 获取订单信息进行产品验证
        Trade trade = tradeMapperService.getById(tradeId);
        String productId = null;
        if (trade != null && trade.getProductId() != null) {
            productId = trade.getProductId();
        }

        // 使用统一的验证逻辑
        if (!isCouponAvailable(coupon.getId(), userCoupon.getUserId(), productId, amount, 1)) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_AVAILABLE);
        }

        // 检查产品适用性（isCouponAvailable中已包含，但这里需要更严格的验证）
        if (trade != null && trade.getProductId() != null) {
            if (coupon.getProductId() != null && !coupon.getProductId().equals(trade.getProductId())) {
                throw new ByrSkiException(ReturnCode.COUPON_PRODUCT_NOT_MATCH);
            }
        }

        // 计算优惠金额
        Double discountAmount = calculateDiscount(coupon.getId(), amount);

        // 更新用户优惠券状态
        userCoupon.setStatus(UserCouponStatus.USED);
        userCoupon.setUseTime(LocalDateTime.now());
        userCoupon.setTradeId(tradeId);
        userCoupon.setDiscountAmount(discountAmount);
        userCoupon.setUpdateTime(LocalDateTime.now());

        boolean result = userCouponMapperService.updateById(userCoupon);
        if (!result) {
            throw new ByrSkiException(ReturnCode.COUPON_RECEIVE_FAILED);
        }

        // 发布优惠券使用事件
        try {
            eventPublisher.publishEvent(new CouponEvent.CouponUsedEvent(this, coupon.getId(), userCoupon.getUserId(), tradeId, discountAmount));
        } catch (Exception e) {
            log.warn("发布优惠券使用事件失败，用户优惠券ID: {}, 订单ID: {}", userCouponId, tradeId, e);
        }

        log.info("使用优惠券成功，用户优惠券ID: {}, 优惠金额: {}", userCouponId, discountAmount);
        return discountAmount;
    }

    @Override
    public Boolean refundCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponMapperService.getById(userCouponId);
        if (userCoupon == null) {
            throw new ByrSkiException(ReturnCode.USER_COUPON_NOT_EXIST);
        }

        if (userCoupon.getStatus() != UserCouponStatus.USED) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_AVAILABLE.getCode(), "只有已使用的优惠券才能退还");
        }

        userCoupon.setStatus(UserCouponStatus.UNUSED);
        userCoupon.setUseTime(null);
        userCoupon.setTradeId(null);
        userCoupon.setDiscountAmount(null);
        userCoupon.setUpdateTime(LocalDateTime.now());

        boolean result = userCouponMapperService.updateById(userCoupon);

        // 发布优惠券退还事件
        try {
            Coupon coupon = couponMapperService.getById(userCoupon.getCouponId());
            eventPublisher.publishEvent(new CouponEvent.CouponRefundedEvent(this, coupon.getId(), userCoupon.getUserId(), userCoupon.getTradeId()));
        } catch (Exception e) {
            log.warn("发布优惠券退还事件失败，用户优惠券ID: {}", userCouponId, e);
        }

        log.info("退还优惠券成功，用户优惠券ID: {}", userCouponId);
        return result;
    }

    @Override
    public Boolean isCouponAvailable(Long couponId, Long userId, String productId, Double amount, Integer participants) {
        Coupon coupon = couponMapperService.getById(couponId);
        if (coupon == null) {
            return false;
        }

        // 检查优惠券状态
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            return false;
        }

        // 检查时间有效性
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            return false;
        }

        // 检查产品适用性
        // 如果优惠券绑定了特定产品，则必须匹配；如果未绑定产品，则为通用优惠券
        if (coupon.getProductId() != null && !coupon.getProductId().isEmpty() && !coupon.getProductId().equals(productId)) {
            return false;
        }

        // 检查金额门槛
        if (coupon.getMinAmount() > 0 && amount < coupon.getMinAmount()) {
            return false;
        }

        // 检查人数门槛
        if (coupon.getMinParticipants() > 0 && participants < coupon.getMinParticipants()) {
            return false;
        }

        return true;
    }

    @Override
    public Boolean isUserCouponAvailable(Long userCouponId, Long userId, String productId, Double amount, Integer participants) {
        // 获取用户优惠券信息
        UserCoupon userCoupon = userCouponMapperService.getById(userCouponId);
        if (userCoupon == null) {
            return false;
        }

        // 检查用户优惠券状态
        if (userCoupon.getStatus() != UserCouponStatus.UNUSED) {
            return false;
        }

        // 检查用户ID是否匹配
        if (!userCoupon.getUserId().equals(userId)) {
            return false;
        }

        // 获取优惠券信息
        Coupon coupon = couponMapperService.getById(userCoupon.getCouponId());
        if (coupon == null) {
            return false;
        }

        // 检查优惠券状态
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            return false;
        }

        // 检查时间有效性
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            return false;
        }

        // 检查金额门槛
        if (coupon.getMinAmount() > 0 && amount < coupon.getMinAmount()) {
            return false;
        }

        // 检查参与人数门槛
        if (coupon.getMinParticipants() > 0 && participants < coupon.getMinParticipants()) {
            return false;
        }

        return true;
    }

    @Override
    public Double calculateDiscount(Long couponId, Double amount) {
        Coupon coupon = couponMapperService.getById(couponId);
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        if (coupon.getType() == CouponType.PERCENTAGE) {
            // 百分比打折：amount(元) * discountValue(%) / 100
            return amount * coupon.getDiscountValue() / 100.0;
        } else if (coupon.getType() == CouponType.FIXED_AMOUNT) {
            // 固定金额优惠：取优惠券金额和订单金额的较小值
            return Math.min(coupon.getDiscountValue().doubleValue(), amount);
        }

        return 0.0;
    }

    @Override
    public void updateExpiredCoupons() {
        LocalDateTime now = LocalDateTime.now();

        // 批量更新过期的优惠券状态
        LambdaQueryWrapper<Coupon> expiredCouponWrapper = new LambdaQueryWrapper<>();
        expiredCouponWrapper.eq(Coupon::getStatus, CouponStatus.ACTIVE)
                .lt(Coupon::getEndTime, now);

        List<Coupon> expiredCoupons = couponMapperService.list(expiredCouponWrapper);
        int expiredCouponCount = 0;
        for (Coupon coupon : expiredCoupons) {
            coupon.setStatus(CouponStatus.EXPIRED);
            coupon.setUpdateTime(now);
            if (couponMapperService.updateById(coupon)) {
                expiredCouponCount++;
            }
        }

        // 批量更新过期的用户优惠券状态
        List<Long> expiredCouponIds = expiredCoupons.stream()
                .map(Coupon::getId)
                .collect(Collectors.toList());

        int expiredUserCouponCount = 0;
        if (!expiredCouponIds.isEmpty()) {
            LambdaQueryWrapper<UserCoupon> userCouponWrapper = new LambdaQueryWrapper<>();
            userCouponWrapper.eq(UserCoupon::getStatus, UserCouponStatus.UNUSED)
                    .in(UserCoupon::getCouponId, expiredCouponIds);

            List<UserCoupon> expiredUserCoupons = userCouponMapperService.list(userCouponWrapper);
            for (UserCoupon userCoupon : expiredUserCoupons) {
                userCoupon.setStatus(UserCouponStatus.EXPIRED);
                userCoupon.setUpdateTime(now);
                if (userCouponMapperService.updateById(userCoupon)) {
                    expiredUserCouponCount++;
                }
            }
        }

        // 发布过期事件
        try {
            for (Coupon coupon : expiredCoupons) {
                eventPublisher.publishEvent(new CouponEvent.CouponExpiredEvent(this, coupon.getId(), null));
            }
        } catch (Exception e) {
            log.warn("发布优惠券过期事件失败", e);
        }

        log.info("更新过期优惠券状态完成，过期优惠券数量: {}, 过期用户优惠券数量: {}",
                expiredCouponCount, expiredUserCouponCount);
    }

    // ============ 发放预览相关实现 ============
    @Override
    public CouponIssuePreviewVo createIssuePreview(CouponIssuePreviewCreateVo createVo) {
        // 统计型日志：发放预览开始（仅关键标识）
        log.info("issuePreview:start couponId={}", createVo.getCouponId());

        Coupon coupon = couponMapperService.getById(createVo.getCouponId());
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new ByrSkiException(ReturnCode.COUPON_INACTIVE);
        }

        // allUsers: true 时，忽略自定义导入，分页加载全体有效用户
        if (Boolean.TRUE.equals(createVo.getAllUsers())) {
            // 分页批量加载，避免一次性加载过多
            int page = 1;
            int pageSize = 2000; // 每批 2000，可按需要调整
            List<Long> allUserIds = new ArrayList<>();

            while (true) {
                com.baomidou.mybatisplus.extension.plugins.pagination.Page<Account> p =
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Account> wrapper =
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                // 仅取普通用户
                wrapper.eq(Account::getIdentity, com.byrski.domain.enums.UserIdentity.USER.getCode());
                // 是否排除黑名单（is_active = false 视为黑名单）
                if (createVo.getExcludeBlacklist() == null || Boolean.TRUE.equals(createVo.getExcludeBlacklist())) {
                    wrapper.ne(Account::getIsActive, false);
                }
                // 学生筛选：true 仅学生；false 仅非学生；null 全部
                if (createVo.getIsStudent() != null) {
                    wrapper.eq(Account::getIsStudent, createVo.getIsStudent());
                }
                com.baomidou.mybatisplus.extension.plugins.pagination.Page<Account> result = accountMapperService.page(p, wrapper);
                if (result.getRecords() == null || result.getRecords().isEmpty()) {
                    break;
                }
                for (Account a : result.getRecords()) {
                    if (a != null && a.getId() != null) allUserIds.add(a.getId());
                }
                if (result.getCurrent() * result.getSize() >= result.getTotal()) {
                    break;
                }
                page++;
            }

            // 按每人限额过滤（保持与原有逻辑一致）
            List<Long> validUserIds = allUserIds.stream().filter(uid -> {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCoupon> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.eq(UserCoupon::getCouponId, coupon.getId()).eq(UserCoupon::getUserId, uid);
                long current = userCouponMapperService.count(wrapper);
                Integer limit = coupon.getPerUserLimit();
                return (limit == null || limit <= 0) || current < limit;
            }).collect(java.util.stream.Collectors.toList());

            String previewId = java.util.UUID.randomUUID().toString();
            PreviewSession session = new PreviewSession();
            session.previewId = previewId;
            session.couponId = coupon.getId();
            session.quantity = createVo.getQuantity() == null ? 1 : createVo.getQuantity();
            session.userIds = new java.util.ArrayList<>(validUserIds);
            session.remark = createVo.getRemark();
            previewSessions.put(previewId, session);

            log.info("issuePreview:created(allUsers) previewId={} couponId={} totalUsers={} finalValid={}",
                    previewId, coupon.getId(), allUserIds.size(), validUserIds.size());

            return CouponIssuePreviewVo.builder()
                    .previewId(previewId)
                    .couponId(coupon.getId())
                    .quantity(session.quantity)
                    .totalCount(allUserIds.size())
                    .validCount(validUserIds.size())
                    .userIds(validUserIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.toList()))
                    .remark(createVo.getRemark())
                    .build();
        }

        List<String> phonesFromExcel = new ArrayList<>();
        if (createVo.getExcelBase64Data() != null && createVo.getFileName() != null) {
            if (!excelUtils.isValidExcelFile(createVo.getFileName())) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "不支持的文件格式，请上传Excel文件(.xlsx或.xls)");
            }
            try {
                phonesFromExcel = excelUtils.parsePhoneNumbersFromBase64(createVo.getExcelBase64Data(),
                        createVo.getPhoneColumnName());
            } catch (Exception e) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "解析Excel文件失败: " + e.getMessage());
            }
        }

        // 汇总来源：userIds + phoneNumbers + excel
        List<Long> inputUserIds = createVo.getUserIds() != null ? createVo.getUserIds() : new ArrayList<>();
        List<String> inputPhones = createVo.getPhoneNumbers() != null ? createVo.getPhoneNumbers() : new ArrayList<>();
        if (!phonesFromExcel.isEmpty()) {
            inputPhones.addAll(phonesFromExcel);
        }
        int totalInputs = (createVo.getUserIds() == null ? 0 : createVo.getUserIds().size()) + inputPhones.size();
        // 入参去重（用户ID与手机号）
        inputUserIds = inputUserIds.stream().filter(java.util.Objects::nonNull).distinct().collect(java.util.stream.Collectors.toList());
        inputPhones = inputPhones.stream().filter(java.util.Objects::nonNull).distinct().collect(java.util.stream.Collectors.toList());
        int dedupedInputs = inputUserIds.size() + inputPhones.size();

        UserValidationUtils.UserValidationResult validation;
        if (!inputPhones.isEmpty()) {
            validation = userValidationUtils.validateUsers(inputUserIds, inputPhones);
        } else {
            validation = userValidationUtils.validateUsers(inputUserIds, null);
        }
        int validAfterValidation = validation.getValidUserCount();

        // 过滤掉已达上限的用户
        List<Long> validUserIds = validation.getValidUserIds().stream().filter(uid -> {
            LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserCoupon::getCouponId, coupon.getId()).eq(UserCoupon::getUserId, uid);
            long current = userCouponMapperService.count(wrapper);
            Integer limit = coupon.getPerUserLimit();
            return (limit == null || limit <= 0) || current < limit;
        }).collect(Collectors.toList());
        int validAfterLimit = validUserIds.size();

        String previewId = UUID.randomUUID().toString();
        PreviewSession session = new PreviewSession();
        session.previewId = previewId;
        session.couponId = coupon.getId();
        session.quantity = createVo.getQuantity() == null ? 1 : createVo.getQuantity();
        session.userIds = new ArrayList<>(validUserIds);
        session.remark = createVo.getRemark();
        previewSessions.put(previewId, session);

        // 统计型日志：发放预览创建完成
        log.info("issuePreview:created previewId={} couponId={} inputs={} dedupedInputs={} validated={} finalValid={}",
                previewId, coupon.getId(), totalInputs, dedupedInputs, validAfterValidation, validAfterLimit);

        return CouponIssuePreviewVo.builder()
                .previewId(previewId)
                .couponId(coupon.getId())
                .quantity(session.quantity)
                .totalCount((createVo.getUserIds() == null ? 0 : createVo.getUserIds().size()) + inputPhones.size())
                .validCount(validUserIds.size())
                .invalidUserIdCount(validation.getInvalidUserIdCount())
                .invalidPhoneCount(validation.getInvalidPhoneCount())
                .userIds(validUserIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.toList()))
                .invalidUserIds(validation.getInvalidUserIds())
                .invalidPhoneNumbers(validation.getInvalidPhoneNumbers())
                .remark(createVo.getRemark())
                .build();
    }

    @Override
    public CouponIssuePreviewVo getIssuePreview(String previewId) {
        PreviewSession session = requirePreview(previewId);
        return CouponIssuePreviewVo.builder()
                .previewId(session.previewId)
                .couponId(session.couponId)
                .quantity(session.quantity)
                .totalCount(session.userIds.size())
                .validCount(session.userIds.size())
                .userIds(session.userIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.toList()))
                .remark(session.remark)
                .build();
    }

    @Override
    public CouponIssuePreviewVo getIssuePreview(String previewId, String username, String phone) {
        PreviewSession session = requirePreview(previewId);
        List<Long> filtered = new ArrayList<>(session.userIds);
        // 简化处理：如果需要按用户名/手机号过滤，可在此处根据映射批量查询后过滤
        if ((username != null && !username.isEmpty()) || (phone != null && !phone.isEmpty())) {
            List<Account> accounts = accountMapperService.listByIds(filtered);
            filtered = accounts.stream()
                    .filter(a -> (username == null || a.getUsername().contains(username))
                            && (phone == null || (a.getPhone() != null && a.getPhone().contains(phone))))
                    .map(Account::getId)
                    .collect(Collectors.toList());
        }
        return CouponIssuePreviewVo.builder()
                .previewId(session.previewId)
                .couponId(session.couponId)
                .quantity(session.quantity)
                .totalCount(filtered.size())
                .validCount(filtered.size())
                .userIds(filtered.stream().map(String::valueOf).collect(java.util.stream.Collectors.toList()))
                .remark(session.remark)
                .build();
    }

    @Override
    public CouponIssuePreviewVo removeUserFromIssuePreview(String previewId, Long userId) {
        PreviewSession session = requirePreview(previewId);
        session.userIds.removeIf(id -> id.equals(userId));
        return getIssuePreview(previewId);
    }

    @Override
    public CouponBatchIssueResultVo submitIssuePreview(String previewId) {
        // 统计型日志：提交预览开始
        log.info("issuePreview:submit:start previewId={}", previewId);

        PreviewSession session = requirePreview(previewId);
        Coupon coupon = couponMapperService.getById(session.couponId);
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new ByrSkiException(ReturnCode.COUPON_INACTIVE);
        }

        int totalUsers = session.userIds.size();
        int requestedTotal = totalUsers * session.quantity;
        log.info("issuePreview:submit:plan couponId={} users={} quantityPerUser={} totalRequests={}",
                session.couponId, totalUsers, session.quantity, requestedTotal);

        // 数量上限校验
        if (coupon.getIssueLimit() > 0 && coupon.getIssuedCount() + requestedTotal > coupon.getIssueLimit()) {
            throw new ByrSkiException(ReturnCode.COUPON_QUANTITY_EXCEEDED);
        }

        // 逐个用户发放（沿用批量逻辑）
        int successCount = 0;
        int skipCount = 0;
        List<String> skipReasons = new ArrayList<>();
        List<CouponBatchIssueResultVo.UserIssueInfo> successUsers = new ArrayList<>();

        Map<Long, Account> users = new HashMap<>();
        if (!session.userIds.isEmpty()) {
            List<Account> accounts = accountMapperService.listByIds(session.userIds);
            users = accounts.stream().collect(Collectors.toMap(Account::getId, Function.identity()));
        }

        // 根据发放方式决定创建哪种记录
        log.info("submitIssuePreview - session.issueMethod: {}, session.pageReceiveMode: {}",
                session.issueMethod, session.pageReceiveMode);
        boolean isPageReceive = session.issueMethod != null && session.issueMethod == 1; // 页面领取方式
        log.info("submitIssuePreview - isPageReceive: {}", isPageReceive);

        for (Long uid : session.userIds) {
            // quantity 次发放（通常为1）
            for (int i = 0; i < session.quantity; i++) {
                if (isPageReceive) {
                    // 页面领取方式：创建 CouponUserReceivable 记录
                    LambdaQueryWrapper<CouponUserReceivable> receivableWrapper = new LambdaQueryWrapper<>();
                    receivableWrapper.eq(CouponUserReceivable::getCouponId, coupon.getId())
                            .eq(CouponUserReceivable::getUserId, uid);

                    long receivableCount = couponUserReceivableMapperService.count(receivableWrapper);
                    if (receivableCount > 0) {
                        Account account = users.get(uid);
                        String reason = String.format("用户 %s (%s) 已存在待领取记录",
                                uid, account != null ? account.getUsername() : "未知");
                        skipReasons.add(reason);
                        skipCount++;
                        break;
                    }

                    CouponUserReceivable receivable = CouponUserReceivable.builder()
                            .userId(uid)
                            .couponId(coupon.getId())
                            .receiveMode(session.pageReceiveMode)
                            .isReceived(false)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .remark(session.remark)
                            .build();

                    if (couponUserReceivableMapperService.save(receivable)) {
                        successCount++;
                        Account account = users.get(uid);
                        successUsers.add(CouponBatchIssueResultVo.UserIssueInfo.builder()
                                .userId(uid)
                                .username(account != null ? account.getUsername() : "未知")
                                .phone(account != null ? account.getPhone() : "")
                                .issueTime(LocalDateTime.now().toString())
                                .build());
                    } else {
                        skipCount++;
                    }
                } else {
                    // 批量导入方式：创建 UserCoupon 记录
                    LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(UserCoupon::getCouponId, coupon.getId()).eq(UserCoupon::getUserId, uid);
                    long current = userCouponMapperService.count(wrapper);
                    Integer limit = coupon.getPerUserLimit();
                    if (limit != null && limit > 0 && current >= limit) {
                        Account account = users.get(uid);
                        String reason = String.format("用户 %s (%s) 已达到领取上限: %d/%d",
                                uid, account != null ? account.getUsername() : "未知", current, limit);
                        skipReasons.add(reason);
                        skipCount++;
                        break;
                    }

                    UserCoupon userCoupon = UserCoupon.builder()
                            .userId(uid)
                            .couponId(coupon.getId())
                            .status(UserCouponStatus.UNUSED)
                            .receiveTime(LocalDateTime.now())
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();
                    if (userCouponMapperService.save(userCoupon)) {
                        successCount++;
                        Account account = users.get(uid);
                        successUsers.add(CouponBatchIssueResultVo.UserIssueInfo.builder()
                                .userId(uid)
                                .username(account != null ? account.getUsername() : "未知")
                                .phone(account != null ? account.getPhone() : "")
                                .issueTime(LocalDateTime.now().toString())
                                .build());
                    } else {
                        skipCount++;
                    }
                }
            }
        }

        if (successCount > 0) {
            coupon.setIssuedCount(coupon.getIssuedCount() + successCount);
            couponMapperService.updateById(coupon);
        }
        // 统计型日志：提交完成汇总
        log.info("issuePreview:submit:done couponId={} success={} skip={} totalUsers={} quantityPerUser={} totalRequests={}",
                coupon.getId(), successCount, skipCount, totalUsers, session.quantity, requestedTotal);

        // 提交后删除会话
        previewSessions.remove(previewId);

        return CouponBatchIssueResultVo.builder()
                .couponId(coupon.getId())
                .couponName(coupon.getName())
                .successCount(successCount)
                .skipCount(skipCount)
                .invalidUserIdCount(0)
                .invalidPhoneCount(0)
                .invalidUserIds(new ArrayList<>())
                .invalidPhoneNumbers(new ArrayList<>())
                .skipReasons(skipReasons)
                .successUsers(successUsers)
                .build();
    }

    @Override
    public Boolean discardIssuePreview(String previewId) {
        return previewSessions.remove(previewId) != null;
    }

    @Override
    public Boolean cleanDuplicateReceivableRecords(Long couponId) {
        return null;
    }

    private PreviewSession requirePreview(String previewId) {
        PreviewSession session = previewSessions.get(previewId);
        if (session == null) {
            throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "预览会话不存在或已过期");
        }
        return session;
    }

    private static class PreviewSession {
        String previewId;
        Long couponId;
        String couponName;
        Integer issueMethod;
        Integer pageReceiveMode;
        Integer quantity;
        List<Long> userIds;
        String remark;
        LocalDateTime createTime;
        List<Long> validUserIds;
        Map<Long, Account> validUsers;
        List<String> invalidUserIds;
        List<String> invalidPhoneNumbers;
    }



    /**
     * 构建优惠券VO
     */
    private CouponVo buildCouponVo(Coupon coupon) {
        CouponVo vo = CouponVo.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .type(coupon.getType())
                .discountValue(coupon.getDiscountValue())
                .minAmount(coupon.getMinAmount())
                .minParticipants(coupon.getMinParticipants())
                .productId(coupon.getProductId())
                .activityTemplateId(coupon.getActivityTemplateId())
                .snowfieldId(coupon.getSnowfieldId())
                .status(coupon.getStatus())
                .issueMethod(coupon.getIssueMethod())
                .pageReceiveMode(coupon.getPageReceiveMode())
                .issueLimit(coupon.getIssueLimit())
                .issuedCount(coupon.getIssuedCount())
                .perUserLimit(coupon.getPerUserLimit())
                .startTime(coupon.getStartTime())
                .endTime(coupon.getEndTime())
                .createTime(coupon.getCreateTime())
                .updateTime(coupon.getUpdateTime())
                .creatorId(coupon.getCreatorId())
                .remark(coupon.getRemark())
                .build();

        // 填充扩展信息
        if (coupon.getProductId() != null) {
            Product product = productManager.getProductById(coupon.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
            }
        }

        if (coupon.getActivityTemplateId() != null) {
            ActivityTemplate template = activityTemplateMapperService.getById(coupon.getActivityTemplateId());
            if (template != null) {
                vo.setActivityTemplateName(template.getName());
            }
        }

        if (coupon.getSnowfieldId() != null) {
            // 通过ProductManager获取雪场信息，避免直接依赖SnowfieldMapperService
            try {
                // 这里可以通过产品ID间接获取雪场信息，或者暂时跳过
                // 如果需要雪场信息，建议通过ProductManager或专门的雪场服务获取
                log.debug("雪场信息获取功能待优化，雪场ID: {}", coupon.getSnowfieldId());
            } catch (Exception e) {
                log.warn("获取雪场信息失败，雪场ID: {}", coupon.getSnowfieldId(), e);
            }
        }

        if (coupon.getCreatorId() != null) {
            Account creator = accountMapperService.getById(coupon.getCreatorId());
            if (creator != null) {
                vo.setCreatorName(creator.getUsername());
            }
        }

        return vo;
    }

    /**
     * 构建用户优惠券VO
     */
    private UserCouponVo buildUserCouponVo(UserCoupon userCoupon) {
        UserCouponVo vo = UserCouponVo.builder()
                .id(userCoupon.getId())
                .userId(userCoupon.getUserId())
                .couponId(userCoupon.getCouponId())
                .status(userCoupon.getStatus())
                .receiveTime(userCoupon.getReceiveTime())
                .useTime(userCoupon.getUseTime())
                .tradeId(userCoupon.getTradeId())
                .discountAmount(userCoupon.getDiscountAmount())
                .createTime(userCoupon.getCreateTime())
                .updateTime(userCoupon.getUpdateTime())
                .remark(userCoupon.getRemark())
                .build();

        // 填充优惠券信息
        Coupon coupon = couponMapperService.getById(userCoupon.getCouponId());
        if (coupon != null) {
            vo.setCouponName(coupon.getName());
            vo.setCouponDescription(coupon.getDescription());
            vo.setCouponType(coupon.getType());
            vo.setCouponDiscountValue(coupon.getDiscountValue());
            vo.setCouponStartTime(coupon.getStartTime());
            vo.setCouponEndTime(coupon.getEndTime());
            vo.setMinAmount(coupon.getMinAmount());
        }

        // 填充订单信息
        if (userCoupon.getTradeId() != null) {
            Trade trade = tradeMapperService.getById(userCoupon.getTradeId());
            if (trade != null) {
                vo.setTradeOutTradeNo(trade.getOutTradeNo());
                if (trade.getProductId() != null) {
                    Product product = productManager.getProductById(trade.getProductId());
                    if (product != null) {
                        vo.setProductName(product.getName());
                    }
                }
            }
        }

        return vo;
    }

    /**
     * 处理批量发放优惠券的核心逻辑
     * @param coupon 优惠券
     * @param validUserIds 有效用户ID列表
     * @param validUsers 有效用户映射
     * @param issueMethod 发放方式描述
     * @param quantity 发放数量
     * @return 成功发放数量
     */
    private Integer processBatchIssue(Coupon coupon, List<Long> validUserIds,
                                    Map<Long, Account> validUsers, String issueMethod, Integer quantity) {
        // 批量发放优惠券
        int successCount = 0;
        int skipCount = 0;
        List<String> skipReasons = new ArrayList<>();

        for (Long userId : validUserIds) {
            try {
                // 检查用户是否已领取过该优惠券
                LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(UserCoupon::getCouponId, coupon.getId())
                        .eq(UserCoupon::getUserId, userId);

                long currentCount = userCouponMapperService.count(wrapper);
                if (currentCount >= coupon.getPerUserLimit()) {
                    Account account = validUsers.get(userId);
                    String reason = String.format("用户 %s (%s) 已达到领取上限: %d/%d",
                        userId, account != null ? account.getUsername() : "未知", currentCount, coupon.getPerUserLimit());
                    skipReasons.add(reason);
                    skipCount++;
                    continue;
                }

                // 创建用户优惠券记录
                UserCoupon userCoupon = UserCoupon.builder()
                        .userId(userId)
                        .couponId(coupon.getId())
                        .status(UserCouponStatus.UNUSED)
                        .receiveTime(LocalDateTime.now())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();

                boolean saved = userCouponMapperService.save(userCoupon);
                if (saved) {
                    successCount++;
                    Account account = validUsers.get(userId);
                    log.info("成功为用户 {} ({}) 发放优惠券 {} [{}]", userId,
                        account != null ? account.getUsername() : "未知", coupon.getName(), issueMethod);
                } else {
                    log.error("为用户 {} 发放优惠券失败: 数据库保存失败", userId);
                }
            } catch (Exception e) {
                log.error("为用户 {} 发放优惠券失败: {}", userId, e.getMessage());
                skipCount++;
            }
        }

        // 更新优惠券已发放数量
        if (successCount > 0) {
            coupon.setIssuedCount(coupon.getIssuedCount() + successCount);
            couponMapperService.updateById(coupon);
        }

        // 记录详细的发放结果
        log.info("批量发放优惠券完成 - 优惠券ID: {}, 方式: {}, 成功: {}, 跳过: {}",
            coupon.getId(), issueMethod, successCount, skipCount);

        if (!skipReasons.isEmpty()) {
            log.info("跳过的原因: {}", String.join("; ", skipReasons));
        }

        return successCount;
    }

    @Override
    public List<CouponIssuedUserVo> getCouponIssuedUsers(Long couponId, Integer status, String username, String phone, String startTime, String endTime) {
        // 验证优惠券是否存在
        Coupon coupon = couponMapperService.getById(couponId);
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        log.info("查询优惠券已发放用户，优惠券ID: {}, 状态: {}, 用户名: {}, 手机号: {}, 开始时间: {}, 结束时间: {}", 
                couponId, status, username, phone, startTime, endTime);

        // 根据实际数据情况决定查询哪个表
        List<CouponIssuedUserVo> issuedUsers;
        
        // 先检查 user_coupon 表中是否有数据（批量发放）
        LambdaQueryWrapper<UserCoupon> userCouponWrapper = new LambdaQueryWrapper<>();
        userCouponWrapper.eq(UserCoupon::getCouponId, couponId);
        long userCouponCount = userCouponMapperService.count(userCouponWrapper);
        
        // 再检查 coupon_user_receivable 表中是否有数据（页面领取）
        LambdaQueryWrapper<CouponUserReceivable> receivableWrapper = new LambdaQueryWrapper<>();
        receivableWrapper.eq(CouponUserReceivable::getCouponId, couponId);
        long receivableCount = couponUserReceivableMapperService.count(receivableWrapper);
        
        if (userCouponCount > 0) {
            // 有批量发放数据，查询 user_coupon 表
            issuedUsers = getBatchIssuedUsers(couponId, status, username, phone, startTime, endTime);
        } else if (receivableCount > 0) {
            // 有页面领取数据，查询 coupon_user_receivable 表
            issuedUsers = getPageIssuedUsers(couponId, status, username, phone, startTime, endTime);
        } else {
            // 两个表都没有数据
            issuedUsers = new ArrayList<>();
        }
        log.info("已发放用户数量: {}", issuedUsers.size());
        
        return issuedUsers;
    }

    @Override
    public TradeDetailVo.CouponInfo getCouponInfoForTrade(Long userCouponId) {
        // 获取用户优惠券信息
        UserCoupon userCoupon = userCouponMapperService.getById(userCouponId);
        if (userCoupon == null) {
            return null;
        }

        // 获取优惠券详细信息
        Coupon coupon = couponMapperService.getById(userCoupon.getCouponId());
        if (coupon == null) {
            return null;
        }

        // 构建优惠券信息
        return TradeDetailVo.CouponInfo.builder()
                .couponId(coupon.getId())
                .couponName(coupon.getName())
                .couponDescription(coupon.getDescription())
                .discountAmount(userCoupon.getDiscountAmount())
                .discountType(coupon.getType().getCode())
                .discountRate(coupon.getType() == CouponType.PERCENTAGE ?
                    Double.valueOf(coupon.getDiscountValue()) : null)
                .validStartTime(coupon.getStartTime())
                .validEndTime(coupon.getEndTime())
                .minAmount(coupon.getMinAmount())
                .minUserCount(coupon.getMinParticipants())
                .build();
    }

    @Override
    public Object batchIssueCouponV2(CouponBatchIssueV2Vo batchIssueV2Vo) {
        // 验证请求参数
        batchIssueV2Vo.validate();

        Coupon coupon = couponMapperService.getById(batchIssueV2Vo.getCouponId());
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new ByrSkiException(ReturnCode.COUPON_INACTIVE);
        }

        // 根据发放方式处理
        CouponIssueMethod issueMethod = CouponIssueMethod.fromCode(batchIssueV2Vo.getIssueMethod());

        if (issueMethod == CouponIssueMethod.PAGE_RECEIVE) {
            // 页面领取方式 - 根据模式处理
            CouponPageReceiveMode pageReceiveMode = CouponPageReceiveMode.fromCode(batchIssueV2Vo.getPageReceiveMode());

            if (pageReceiveMode == CouponPageReceiveMode.SPECIFIC_USERS) {
                // 指定用户发放模式 - 使用原有逻辑
                return executeSpecificUsersIssue(batchIssueV2Vo, coupon);
            } else if (pageReceiveMode == CouponPageReceiveMode.ALL_USERS_COMPETITION) {
                // 全体用户抢票模式 - 新逻辑
                return executeAllUsersCompetitionIssue(batchIssueV2Vo, coupon);
            }
        } else {
            // 批量导入方式 - 使用原有逻辑
            CouponBatchIssueVo oldBatchIssueVo = new CouponBatchIssueVo();
            BeanUtils.copyProperties(batchIssueV2Vo, oldBatchIssueVo);
            return batchIssueCoupon(oldBatchIssueVo);
        }

        throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "不支持的发放方式");
    }

    /**
     * 执行指定用户发放模式
     */
    private Object executeSpecificUsersIssue(CouponBatchIssueV2Vo batchIssueV2Vo, Coupon coupon) {
        // 合并所有数据源
        List<String> mergedPhones = new ArrayList<>();
        if (batchIssueV2Vo.getPhoneNumbers() != null) {
            mergedPhones.addAll(batchIssueV2Vo.getPhoneNumbers());
        }
        if (batchIssueV2Vo.getExcelBase64Data() != null && batchIssueV2Vo.getFileName() != null) {
            if (!excelUtils.isValidExcelFile(batchIssueV2Vo.getFileName())) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "不支持的文件格式，请上传Excel文件(.xlsx或.xls)");
            }
            List<String> excelPhones;
            try {
                excelPhones = excelUtils.parsePhoneNumbersFromBase64(batchIssueV2Vo.getExcelBase64Data(),
                        batchIssueV2Vo.getPhoneColumnName());
            } catch (Exception e) {
                throw new ByrSkiException(ReturnCode.PARAM_EXCEPTION.getCode(), "解析Excel文件失败: " + e.getMessage());
            }
            if (excelPhones != null && !excelPhones.isEmpty()) {
                mergedPhones.addAll(excelPhones);
            }
        }

        // 验证用户
        UserValidationUtils.UserValidationResult validationResult =
            userValidationUtils.validateUsers(batchIssueV2Vo.getUserIds(), mergedPhones);

        if (!validationResult.hasValidUsers()) {
            throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(),
                "没有找到有效的用户。无效用户ID: " + validationResult.getInvalidUserIdCount() + "个，无效手机号: " + validationResult.getInvalidPhoneCount() + "个");
        }

        // 如果需要预览，创建预览会话
        if (Boolean.TRUE.equals(batchIssueV2Vo.getNeedPreview()) || batchIssueV2Vo.getNeedPreview() == null) {
            return createIssuePreviewFromBatchIssueV2(batchIssueV2Vo, coupon, validationResult);
        } else {
            // 直接发放
            return executeBatchIssueToUsers(coupon, validationResult.getValidUserIds(), batchIssueV2Vo.getRemark());
        }
    }

    /**
     * 执行全体用户抢票模式
     */
    private Object executeAllUsersCompetitionIssue(CouponBatchIssueV2Vo batchIssueV2Vo, Coupon coupon) {
        // 检查发放数量限制
        if (coupon.getIssueLimit() > 0 &&
            coupon.getIssuedCount() + batchIssueV2Vo.getQuantity() > coupon.getIssueLimit()) {
            throw new ByrSkiException(ReturnCode.COUPON_QUANTITY_EXCEEDED);
        }

        // 获取所有用户ID
        List<Long> allUserIds = getAllUserIds();
        if (allUserIds.isEmpty()) {
            throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(), "没有找到任何用户");
        }

        // 过滤掉已达领取上限的用户
        List<Long> validUserIds = allUserIds.stream().filter(uid -> {
            LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserCoupon::getCouponId, coupon.getId()).eq(UserCoupon::getUserId, uid);
            long current = userCouponMapperService.count(wrapper);
            return current < coupon.getPerUserLimit();
        }).collect(Collectors.toList());

        if (validUserIds.isEmpty()) {
            throw new ByrSkiException(ReturnCode.COUPON_BATCH_IMPORT_FAILED.getCode(), "所有用户都已达到领取上限");
        }

        // 如果需要预览，创建预览会话
        if (Boolean.TRUE.equals(batchIssueV2Vo.getNeedPreview()) || batchIssueV2Vo.getNeedPreview() == null) {
            return createAllUsersCompetitionPreview(batchIssueV2Vo, coupon, validUserIds);
        } else {
            // 直接发放到全体用户的receivable列表中
            return executeAllUsersCompetitionToReceivable(coupon, validUserIds, batchIssueV2Vo.getQuantity(), batchIssueV2Vo.getRemark());
        }
    }

    /**
     * 从批量发放请求创建预览会话
     */
    private CouponIssuePreviewVo createIssuePreviewFromBatchIssue(CouponBatchIssueVo batchIssueVo,
                                                                  Coupon coupon,
                                                                  UserValidationUtils.UserValidationResult validationResult) {
        String previewId = UUID.randomUUID().toString();

        // 立即保存验证结果到局部变量，避免后续调用时数据被修改
        List<Long> validUserIds = new ArrayList<>(validationResult.getValidUserIds());
        Map<Long, Account> validUsers = new HashMap<>(validationResult.getValidUsers());
        List<String> invalidUserIds = new ArrayList<>(validationResult.getInvalidUserIds());
        List<String> invalidPhoneNumbers = new ArrayList<>(validationResult.getInvalidPhoneNumbers());
        int invalidUserIdCount = validationResult.getInvalidUserIdCount();
        int invalidPhoneCount = validationResult.getInvalidPhoneCount();

        // 调试日志
        log.info("创建预览会话 - 优惠券ID: {}, 有效用户数量: {}, 有效用户ID列表: {}",
                coupon.getId(), validUserIds.size(), validUserIds);
        log.info("验证结果详情 - 有效用户映射: {}, 无效用户ID: {}, 无效手机号: {}",
                validUsers.size(), invalidUserIds.size(), invalidPhoneNumbers.size());

        // 创建预览会话
        log.info("createIssuePreviewFromBatchIssue - batchIssueVo.issueMethod: {}, batchIssueVo.pageReceiveMode: {}",
                batchIssueVo.getIssueMethod(), batchIssueVo.getPageReceiveMode());
        PreviewSession session = new PreviewSession();
        session.previewId = previewId;
        session.couponId = coupon.getId();
        session.couponName = coupon.getName();
        session.issueMethod = batchIssueVo.getIssueMethod();
        session.pageReceiveMode = batchIssueVo.getPageReceiveMode();
        log.info("createIssuePreviewFromBatchIssue - session.issueMethod: {}, session.pageReceiveMode: {}",
                session.issueMethod, session.pageReceiveMode);
        session.quantity = batchIssueVo.getQuantity();
        session.remark = batchIssueVo.getRemark();
        session.createTime = LocalDateTime.now();
        session.userIds = new ArrayList<>(validUserIds);
        session.validUserIds = validUserIds;
        session.validUsers = validUsers;
        session.invalidUserIds = invalidUserIds;
        session.invalidPhoneNumbers = invalidPhoneNumbers;

        // 存储预览会话
        previewSessions.put(previewId, session);

        List<String> userIdStrings = validUserIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());

        log.info("预览结果 - 用户ID字符串列表: {}", userIdStrings);

        return CouponIssuePreviewVo.builder()
                .previewId(previewId)
                .couponId(coupon.getId())
                .quantity(batchIssueVo.getQuantity())
                .totalCount(validUserIds.size())
                .validCount(validUserIds.size())
                .invalidUserIdCount(invalidUserIdCount)
                .invalidPhoneCount(invalidPhoneCount)
                .userIds(userIdStrings)
                .invalidUserIds(invalidUserIds)
                .invalidPhoneNumbers(invalidPhoneNumbers)
                .remark(batchIssueVo.getRemark())
                .build();
    }

    /**
     * 创建指定用户发放预览
     */
    private CouponIssuePreviewVo createIssuePreviewFromBatchIssueV2(CouponBatchIssueV2Vo batchIssueV2Vo,
                                                                   Coupon coupon,
                                                                   UserValidationUtils.UserValidationResult validationResult) {
        // 对于页面领取方式，不需要过滤已达上限的用户，因为优惠券会发放到 receivable 表中
        // 用户领取时才检查上限
        List<Long> validUserIds = new ArrayList<>(validationResult.getValidUserIds());

        // 创建预览会话
        String previewId = UUID.randomUUID().toString();
        PreviewSession session = new PreviewSession();
        session.previewId = previewId;
        session.couponId = coupon.getId();
        session.issueMethod = batchIssueV2Vo.getIssueMethod();
        session.pageReceiveMode = batchIssueV2Vo.getPageReceiveMode();
        session.quantity = 1; // 指定用户发放模式，每人1张
        session.userIds = new ArrayList<>(validUserIds);
        session.remark = batchIssueV2Vo.getRemark();
        previewSessions.put(previewId, session);

        return CouponIssuePreviewVo.builder()
                .previewId(previewId)
                .couponId(coupon.getId())
                .quantity(session.quantity)
                .totalCount(validationResult.getValidUserIds().size())
                .validCount(validUserIds.size())
                .invalidUserIdCount(validationResult.getInvalidUserIdCount())
                .invalidPhoneCount(validationResult.getInvalidPhoneCount())
                .userIds(validUserIds.stream().map(String::valueOf).collect(Collectors.toList()))
                .invalidUserIds(validationResult.getInvalidUserIds())
                .invalidPhoneNumbers(validationResult.getInvalidPhoneNumbers())
                .remark(batchIssueV2Vo.getRemark())
                .build();
    }

    /**
     * 创建全体用户抢票预览
     */
    private CouponIssuePreviewVo createAllUsersCompetitionPreview(CouponBatchIssueV2Vo batchIssueV2Vo,
                                                                 Coupon coupon,
                                                                 List<Long> validUserIds) {
        // 创建预览会话
        String previewId = UUID.randomUUID().toString();
        PreviewSession session = new PreviewSession();
        session.previewId = previewId;
        session.couponId = coupon.getId();
        session.quantity = batchIssueV2Vo.getQuantity();
        session.userIds = new ArrayList<>(validUserIds);
        session.remark = batchIssueV2Vo.getRemark();
        previewSessions.put(previewId, session);

        return CouponIssuePreviewVo.builder()
                .previewId(previewId)
                .couponId(coupon.getId())
                .quantity(session.quantity)
                .totalCount(validUserIds.size())
                .validCount(validUserIds.size())
                .invalidUserIdCount(0)
                .invalidPhoneCount(0)
                .userIds(validUserIds.stream().map(String::valueOf).collect(Collectors.toList()))
                .invalidUserIds(new ArrayList<>())
                .invalidPhoneNumbers(new ArrayList<>())
                .remark(batchIssueV2Vo.getRemark())
                .build();
    }

    /**
     * 执行指定用户发放
     */
    private CouponBatchIssueV2ResultVo executeBatchIssueToUsers(Coupon coupon, List<Long> userIds, String remark) {
        int successCount = 0;
        int skipCount = 0;
        List<String> skipReasons = new ArrayList<>();
        List<CouponBatchIssueV2ResultVo.UserIssueInfo> successUsers = new ArrayList<>();

        for (Long userId : userIds) {
            try {
                // 对于页面领取方式，检查是否已经在 receivable 表中
                LambdaQueryWrapper<CouponUserReceivable> receivableWrapper = new LambdaQueryWrapper<>();
                receivableWrapper.eq(CouponUserReceivable::getCouponId, coupon.getId())
                        .eq(CouponUserReceivable::getUserId, userId);

                long receivableCount = couponUserReceivableMapperService.count(receivableWrapper);
                if (receivableCount > 0) {
                    Account account = accountMapperService.getById(userId);
                    String reason = String.format("用户 %s (%s) 已存在待领取记录",
                        userId, account != null ? account.getUsername() : "未知");
                    skipReasons.add(reason);
                    skipCount++;
                    continue;
                }

                // 创建用户可领取记录（页面领取方式）
                CouponUserReceivable receivable = CouponUserReceivable.builder()
                        .userId(userId)
                        .couponId(coupon.getId())
                        .receiveMode(1) // 指定用户发放模式
                        .isReceived(false)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .remark(remark)
                        .build();

                boolean result = couponUserReceivableMapperService.save(receivable);
                if (result) {
                    // 更新优惠券发放数量
                    couponMapperService.update()
                            .setSql("issued_count = issued_count + 1")
                            .eq("id", coupon.getId())
                            .update();

                    Account account = accountMapperService.getById(userId);
                    successUsers.add(CouponBatchIssueV2ResultVo.UserIssueInfo.builder()
                            .userId(userId)
                            .username(account != null ? account.getUsername() : "未知")
                            .phone(account != null ? account.getPhone() : "")
                            .remark(remark)
                            .build());
                    successCount++;
                } else {
                    skipReasons.add("保存用户优惠券记录失败");
                    skipCount++;
                }
            } catch (Exception e) {
                log.error("发放优惠券到用户失败，用户ID: {}, 优惠券ID: {}", userId, coupon.getId(), e);
                skipReasons.add("发放失败: " + e.getMessage());
                skipCount++;
            }
        }

        return CouponBatchIssueV2ResultVo.builder()
                .couponId(coupon.getId())
                .couponName(coupon.getName())
                .pageReceiveMode(CouponPageReceiveMode.SPECIFIC_USERS.getCode())
                .successCount(successCount)
                .skipCount(skipCount)
                .invalidUserIdCount(0)
                .invalidPhoneCount(0)
                .invalidUserIds(new ArrayList<>())
                .invalidPhoneNumbers(new ArrayList<>())
                .skipReasons(skipReasons)
                .successUsers(successUsers)
                .remark(remark)
                .build();
    }

    /**
     * 执行全体用户抢票发放到receivable列表
     */
    private CouponBatchIssueV2ResultVo executeAllUsersCompetitionToReceivable(Coupon coupon,
                                                                              List<Long> validUserIds,
                                                                              Integer quantity,
                                                                      String remark) {
        // 随机选择用户进行发放
        Collections.shuffle(validUserIds);
        List<Long> selectedUserIds = validUserIds.stream()
                .limit(quantity)
                .collect(Collectors.toList());

        int successCount = 0;
        int skipCount = 0;
        List<String> skipReasons = new ArrayList<>();
        List<CouponBatchIssueV2ResultVo.UserIssueInfo> successUsers = new ArrayList<>();

        for (Long userId : selectedUserIds) {
            try {
                // 检查是否已存在关联记录
                LambdaQueryWrapper<CouponUserReceivable> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(CouponUserReceivable::getCouponId, coupon.getId())
                        .eq(CouponUserReceivable::getUserId, userId);

                if (couponUserReceivableMapperService.count(wrapper) > 0) {
                    skipReasons.add("用户已在可领取列表中");
                    skipCount++;
                    continue;
                }

                // 创建关联记录
                CouponUserReceivable receivable = CouponUserReceivable.builder()
                        .couponId(coupon.getId())
                        .userId(userId)
                        .receiveMode(CouponPageReceiveMode.ALL_USERS_COMPETITION.getCode())
                        .isReceived(false)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .remark(remark)
                        .build();

                boolean result = couponUserReceivableMapperService.save(receivable);
                if (result) {
                    Account account = accountMapperService.getById(userId);
                    successUsers.add(CouponBatchIssueV2ResultVo.UserIssueInfo.builder()
                            .userId(userId)
                            .username(account != null ? account.getUsername() : "未知")
                            .phone(account != null ? account.getPhone() : "")
                            .remark(remark)
                            .build());
                    successCount++;
                } else {
                    skipReasons.add("保存关联记录失败");
                    skipCount++;
                }
            } catch (Exception e) {
                log.error("创建优惠券关联记录失败，用户ID: {}, 优惠券ID: {}", userId, coupon.getId(), e);
                skipReasons.add("创建失败: " + e.getMessage());
                skipCount++;
            }
        }

        return CouponBatchIssueV2ResultVo.builder()
                .couponId(coupon.getId())
                .couponName(coupon.getName())
                .pageReceiveMode(CouponPageReceiveMode.ALL_USERS_COMPETITION.getCode())
                .successCount(successCount)
                .skipCount(skipCount)
                .invalidUserIdCount(0)
                .invalidPhoneCount(0)
                .invalidUserIds(new ArrayList<>())
                .invalidPhoneNumbers(new ArrayList<>())
                .skipReasons(skipReasons)
                .successUsers(successUsers)
                .remark(remark)
                .build();
    }

    /**
     * 执行全体用户抢票发放（直接发放到用户账户）
     */
    private CouponBatchIssueV2ResultVo executeAllUsersCompetitionIssue(Coupon coupon,
                                                                      List<Long> validUserIds,
                                                                      Integer quantity,
                                                                      String remark) {
        // 随机选择用户进行发放
        Collections.shuffle(validUserIds);
        List<Long> selectedUserIds = validUserIds.stream()
                .limit(quantity)
                .collect(Collectors.toList());

        int successCount = 0;
        int skipCount = 0;
        List<String> skipReasons = new ArrayList<>();
        List<CouponBatchIssueV2ResultVo.UserIssueInfo> successUsers = new ArrayList<>();

        for (Long userId : selectedUserIds) {
            try {
                // 创建用户优惠券记录
                UserCoupon userCoupon = UserCoupon.builder()
                        .userId(userId)
                        .couponId(coupon.getId())
                        .status(UserCouponStatus.UNUSED)
                        .receiveTime(LocalDateTime.now())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .remark(remark)
                        .build();

                boolean result = userCouponMapperService.save(userCoupon);
                if (result) {
                    // 更新优惠券发放数量
                    couponMapperService.update()
                            .setSql("issued_count = issued_count + 1")
                            .eq("id", coupon.getId())
                            .update();

                    Account account = accountMapperService.getById(userId);
                    successUsers.add(CouponBatchIssueV2ResultVo.UserIssueInfo.builder()
                            .userId(userId)
                            .username(account != null ? account.getUsername() : "未知")
                            .phone(account != null ? account.getPhone() : "")
                            .remark(remark)
                            .build());
                    successCount++;
                } else {
                    skipReasons.add("保存用户优惠券记录失败");
                    skipCount++;
                }
            } catch (Exception e) {
                log.error("发放优惠券到用户失败，用户ID: {}, 优惠券ID: {}", userId, coupon.getId(), e);
                skipReasons.add("发放失败: " + e.getMessage());
                skipCount++;
            }
        }

        return CouponBatchIssueV2ResultVo.builder()
                .couponId(coupon.getId())
                .couponName(coupon.getName())
                .pageReceiveMode(CouponPageReceiveMode.ALL_USERS_COMPETITION.getCode())
                .successCount(successCount)
                .skipCount(skipCount)
                .invalidUserIdCount(0)
                .invalidPhoneCount(0)
                .invalidUserIds(new ArrayList<>())
                .invalidPhoneNumbers(new ArrayList<>())
                .skipReasons(skipReasons)
                .successUsers(successUsers)
                .remark(remark)
                .build();
    }

    @Override
    public List<Long> getAllUserIds() {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getIsActive, true)
                .select(Account::getId);

        List<Account> accounts = accountMapperService.list(wrapper);
        return accounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getUserCouponCounts(Long userId) {
        Map<String, Integer> counts = new HashMap<>();
        
        // 总优惠券数量
        LambdaQueryWrapper<UserCoupon> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(UserCoupon::getUserId, userId);
        counts.put("total", (int) userCouponMapperService.count(totalWrapper));
        
        // 未使用优惠券数量
        LambdaQueryWrapper<UserCoupon> unusedWrapper = new LambdaQueryWrapper<>();
        unusedWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, UserCouponStatus.UNUSED);
        counts.put("unused", (int) userCouponMapperService.count(unusedWrapper));
        
        // 已使用优惠券数量
        LambdaQueryWrapper<UserCoupon> usedWrapper = new LambdaQueryWrapper<>();
        usedWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, UserCouponStatus.USED);
        counts.put("used", (int) userCouponMapperService.count(usedWrapper));
        
        // 已过期优惠券数量
        LambdaQueryWrapper<UserCoupon> expiredWrapper = new LambdaQueryWrapper<>();
        expiredWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, UserCouponStatus.EXPIRED);
        counts.put("expired", (int) userCouponMapperService.count(expiredWrapper));
        
        // 可领取优惠券数量
        LambdaQueryWrapper<CouponUserReceivable> receivableWrapper = new LambdaQueryWrapper<>();
        receivableWrapper.eq(CouponUserReceivable::getUserId, userId)
                .eq(CouponUserReceivable::getIsReceived, false);
        counts.put("receivable", (int) couponUserReceivableMapperService.count(receivableWrapper));
        
        return counts;
    }

    @Override
    public CouponVo getCouponDetailForUser(Long couponId, Long userId) {
        // 1. 获取优惠券基本信息
        Coupon coupon = couponMapperService.getById(couponId);
        if (coupon == null) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_EXIST);
        }

        // 2. 检查优惠券状态 - 必须是已发布状态
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_AVAILABLE.getCode(), "优惠券未发布或已停用");
        }

        // 3. 检查优惠券是否在有效期内
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_AVAILABLE.getCode(), "优惠券尚未生效");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_AVAILABLE.getCode(), "优惠券已过期");
        }

        // 4. 检查优惠券是否还有剩余数量（如果有限制）
        if (coupon.getIssueLimit() > 0 && coupon.getIssuedCount() >= coupon.getIssueLimit()) {
            throw new ByrSkiException(ReturnCode.COUPON_NOT_AVAILABLE.getCode(), "优惠券已发放完毕");
        }

        // 5. 检查用户是否已达到领取上限
        if (coupon.getPerUserLimit() != null && coupon.getPerUserLimit() > 0) {
            LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserCoupon::getCouponId, couponId)
                    .eq(UserCoupon::getUserId, userId);
            long userReceivedCount = userCouponMapperService.count(wrapper);
            if (userReceivedCount >= coupon.getPerUserLimit()) {
                throw new ByrSkiException(ReturnCode.COUPON_NOT_AVAILABLE.getCode(), "您已达到该优惠券的领取上限");
            }
        }

        // 6. 构建并返回优惠券详情
        return buildCouponVo(coupon);
    }

    /**
     * 查询已发放的用户（user_coupon表）
     */
    private List<CouponIssuedUserVo> getBatchIssuedUsers(Long couponId, Integer status, String username, String phone, String startTime, String endTime) {
        // 构建查询条件
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getCouponId, couponId);

        // 状态筛选
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, UserCouponStatus.fromCode(status));
        }

        // 时间范围筛选
        if (StringUtils.hasText(startTime)) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTime);
                wrapper.ge(UserCoupon::getCreateTime, start);
            } catch (Exception e) {
                log.warn("开始时间格式错误: {}", startTime);
            }
        }
        if (StringUtils.hasText(endTime)) {
            try {
                LocalDateTime end = LocalDateTime.parse(endTime);
                wrapper.le(UserCoupon::getCreateTime, end);
            } catch (Exception e) {
                log.warn("结束时间格式错误: {}", endTime);
            }
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(UserCoupon::getCreateTime);

        // 查询用户优惠券列表
        List<UserCoupon> userCoupons = userCouponMapperService.list(wrapper);
        log.info("批量发放查询结果，优惠券ID: {}, 查询到用户优惠券数量: {}", couponId, userCoupons.size());
        
        // 调试：打印查询条件
        log.info("查询条件 - couponId: {}, status: {}, startTime: {}, endTime: {}", 
                couponId, status, startTime, endTime);
        
        // 调试：检查数据库中是否存在该优惠券的用户记录
        LambdaQueryWrapper<UserCoupon> debugWrapper = new LambdaQueryWrapper<>();
        debugWrapper.eq(UserCoupon::getCouponId, couponId);
        long totalCount = userCouponMapperService.count(debugWrapper);
        log.info("数据库中优惠券 {} 的总用户记录数: {}", couponId, totalCount);
        
        // 调试：检查 coupon_user_receivable 表中的记录
        LambdaQueryWrapper<CouponUserReceivable> receivableWrapper = new LambdaQueryWrapper<>();
        receivableWrapper.eq(CouponUserReceivable::getCouponId, couponId);
        long receivableCount = couponUserReceivableMapperService.count(receivableWrapper);
        log.info("数据库中优惠券 {} 的可领取记录数: {}", couponId, receivableCount);
        
        // 调试：检查已领取的记录
        LambdaQueryWrapper<CouponUserReceivable> receivedWrapper = new LambdaQueryWrapper<>();
        receivedWrapper.eq(CouponUserReceivable::getCouponId, couponId)
                      .eq(CouponUserReceivable::getIsReceived, true);
        long receivedCount = couponUserReceivableMapperService.count(receivedWrapper);
        log.info("数据库中优惠券 {} 的已领取记录数: {}", couponId, receivedCount);

        // 获取所有用户ID
        List<Long> userIds = userCoupons.stream()
                .map(UserCoupon::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        final Map<Long, Account> userMap;
        if (!userIds.isEmpty()) {
            List<Account> accounts = accountMapperService.listByIds(userIds);
            userMap = accounts.stream()
                    .collect(Collectors.toMap(Account::getId, Function.identity()));
        } else {
            userMap = new HashMap<>();
        }

        // 获取优惠券信息，确定发放方式
        Coupon coupon = couponMapperService.getById(couponId);
        final String issueMode;
        if (coupon != null && coupon.getIssueMethod() != null) {
            if (coupon.getIssueMethod() == 1) {
                issueMode = "页面领取";
            } else if (coupon.getIssueMethod() == 2) {
                issueMode = "批量发放";
            } else {
                issueMode = "批量发放"; // 默认值
            }
        } else {
            issueMode = "批量发放"; // 默认值
        }

        // 构建结果
        return userCoupons.stream()
                .map(userCoupon -> {
                    Account account = userMap.get(userCoupon.getUserId());

                    // 用户名和手机号筛选
                    if (StringUtils.hasText(username) && account != null) {
                        if (!account.getUsername().toLowerCase().contains(username.toLowerCase())) {
                            return null;
                        }
                    }
                    if (StringUtils.hasText(phone) && account != null) {
                        if (!account.getPhone().contains(phone)) {
                            return null;
                        }
                    }

                    return CouponIssuedUserVo.builder()
                            .userId(userCoupon.getUserId())
                            .username(account != null ? account.getUsername() : "未知用户")
                            .phone(account != null ? account.getPhone() : "")
                            .status(userCoupon.getStatus())
                            .issueMode(issueMode)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 查询页面发放的用户（coupon_user_receivable表）
     */
    private List<CouponIssuedUserVo> getPageIssuedUsers(Long couponId, Integer status, String username, String phone, String startTime, String endTime) {
        // 构建查询条件
        LambdaQueryWrapper<CouponUserReceivable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponUserReceivable::getCouponId, couponId);

        // 时间范围筛选
        if (StringUtils.hasText(startTime)) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTime);
                wrapper.ge(CouponUserReceivable::getCreateTime, start);
            } catch (Exception e) {
                log.warn("开始时间格式错误: {}", startTime);
            }
        }
        if (StringUtils.hasText(endTime)) {
            try {
                LocalDateTime end = LocalDateTime.parse(endTime);
                wrapper.le(CouponUserReceivable::getCreateTime, end);
            } catch (Exception e) {
                log.warn("结束时间格式错误: {}", endTime);
            }
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(CouponUserReceivable::getCreateTime);

        // 查询可领取记录
        List<CouponUserReceivable> receivableRecords = couponUserReceivableMapperService.list(wrapper);
        log.info("页面发放查询结果，优惠券ID: {}, 查询到可领取记录数量: {}", couponId, receivableRecords.size());

        // 获取所有用户ID
        List<Long> userIds = receivableRecords.stream()
                .map(CouponUserReceivable::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        final Map<Long, Account> userMap;
        if (!userIds.isEmpty()) {
            List<Account> accounts = accountMapperService.listByIds(userIds);
            userMap = accounts.stream()
                    .collect(Collectors.toMap(Account::getId, Function.identity()));
        } else {
            userMap = new HashMap<>();
        }

        // 构建结果
        return receivableRecords.stream()
                .map(record -> {
                    Account account = userMap.get(record.getUserId());
                    
                    // 用户名和手机号筛选
                    if (StringUtils.hasText(username) && account != null) {
                        if (!account.getUsername().toLowerCase().contains(username.toLowerCase())) {
                            return null;
                        }
                    }
                    if (StringUtils.hasText(phone) && account != null) {
                        if (!account.getPhone().contains(phone)) {
                            return null;
                        }
                    }
                    
                    // 判断用户优惠券的三种状态
                    UserCouponStatus userCouponStatus;
                    String receiveStatus;
                    
                    if (!record.getIsReceived()) {
                        // 未领取
                        userCouponStatus = UserCouponStatus.UNUSED;
                        receiveStatus = "未领取";
                    } else {
                        // 已领取，需要查询 user_coupon 表获取实际状态
                        LambdaQueryWrapper<UserCoupon> userCouponWrapper = new LambdaQueryWrapper<>();
                        userCouponWrapper.eq(UserCoupon::getCouponId, couponId)
                                .eq(UserCoupon::getUserId, record.getUserId())
                                .orderByDesc(UserCoupon::getCreateTime)
                                .last("LIMIT 1");
                        
                        List<UserCoupon> userCoupons = userCouponMapperService.list(userCouponWrapper);
                        if (!userCoupons.isEmpty()) {
                            UserCoupon userCoupon = userCoupons.get(0);
                            userCouponStatus = userCoupon.getStatus();
                            if (userCouponStatus == UserCouponStatus.USED) {
                                receiveStatus = "已使用";
                            } else {
                                receiveStatus = "已领取未使用";
                            }
                        } else {
                            // 理论上不应该出现这种情况，但为了安全起见
                            userCouponStatus = UserCouponStatus.UNUSED;
                            receiveStatus = "已领取未使用";
                        }
                    }

                    // 状态筛选
                    if (status != null && userCouponStatus.getCode() != status) {
                        return null;
                    }

                    return CouponIssuedUserVo.builder()
                            .userId(record.getUserId())
                            .username(account != null ? account.getUsername() : "未知用户")
                            .phone(account != null ? account.getPhone() : "")
                            .status(userCouponStatus)
                            .issueMode("页面领取")
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 查询页面领取的用户（coupon_user_receivable表）
     */
    private List<CouponIssuedUserVo> getPageReceivedUsers(Long couponId, Integer status, String username, String phone, String startTime, String endTime) {
        // 构建查询条件
        LambdaQueryWrapper<CouponUserReceivable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponUserReceivable::getCouponId, couponId)
                .eq(CouponUserReceivable::getIsReceived, true); // 只查询已领取的

        // 时间范围筛选
        if (StringUtils.hasText(startTime)) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTime);
                wrapper.ge(CouponUserReceivable::getCreateTime, start);
            } catch (Exception e) {
                log.warn("开始时间格式错误: {}", startTime);
            }
        }
        if (StringUtils.hasText(endTime)) {
            try {
                LocalDateTime end = LocalDateTime.parse(endTime);
                wrapper.le(CouponUserReceivable::getCreateTime, end);
            } catch (Exception e) {
                log.warn("结束时间格式错误: {}", endTime);
            }
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(CouponUserReceivable::getCreateTime);

        // 查询可领取记录
        List<CouponUserReceivable> receivableRecords = couponUserReceivableMapperService.list(wrapper);
        log.info("页面领取查询结果，优惠券ID: {}, 查询到可领取记录数量: {}", couponId, receivableRecords.size());

        // 获取所有用户ID
        List<Long> userIds = receivableRecords.stream()
                .map(CouponUserReceivable::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        final Map<Long, Account> userMap;
        if (!userIds.isEmpty()) {
            List<Account> accounts = accountMapperService.listByIds(userIds);
            userMap = accounts.stream()
                    .collect(Collectors.toMap(Account::getId, Function.identity()));
        } else {
            userMap = new HashMap<>();
        }

        // 构建结果
        return receivableRecords.stream()
                .map(record -> {
                    Account account = userMap.get(record.getUserId());
                    
                    // 用户名和手机号筛选
                    if (StringUtils.hasText(username) && account != null) {
                        if (!account.getUsername().toLowerCase().contains(username.toLowerCase())) {
                            return null;
                        }
                    }
                    if (StringUtils.hasText(phone) && account != null) {
                        if (!account.getPhone().contains(phone)) {
                            return null;
                        }
                    }
                    
                    // 页面领取的用户优惠券状态需要从user_coupon表查询
                    UserCouponStatus userCouponStatus = UserCouponStatus.UNUSED;
                    
                    // 查询对应的user_coupon记录
                    LambdaQueryWrapper<UserCoupon> userCouponWrapper = new LambdaQueryWrapper<>();
                    userCouponWrapper.eq(UserCoupon::getCouponId, couponId)
                            .eq(UserCoupon::getUserId, record.getUserId())
                            .orderByDesc(UserCoupon::getCreateTime)
                            .last("LIMIT 1");
                    
                    List<UserCoupon> userCoupons = userCouponMapperService.list(userCouponWrapper);
                    if (!userCoupons.isEmpty()) {
                        UserCoupon userCoupon = userCoupons.get(0);
                        userCouponStatus = userCoupon.getStatus();
                    }

                    // 状态筛选
                    if (status != null && userCouponStatus.getCode() != status) {
                        return null;
                    }

                    return CouponIssuedUserVo.builder()
                            .userId(record.getUserId())
                            .username(account != null ? account.getUsername() : "未知用户")
                            .phone(account != null ? account.getPhone() : "")
                            .status(userCouponStatus)
                            .issueMode("页面领取")
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
