package com.byrski.common.utils;

import com.byrski.domain.entity.dto.Account;
import com.byrski.infrastructure.mapper.impl.AccountMapperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 用户验证工具类
 */
@Component
@Slf4j
public class UserValidationUtils {

    private final AccountMapperService accountMapperService;

    // 中国手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    public UserValidationUtils(AccountMapperService accountMapperService) {
        this.accountMapperService = accountMapperService;
    }

    /**
     * 验证用户ID列表
     * @param userIds 用户ID列表
     * @return 验证结果
     */
    public UserValidationResult validateUserIds(List<Long> userIds) {
        UserValidationResult result = new UserValidationResult();
        
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }

        for (Long userId : userIds) {
            if (userId == null || userId <= 0) {
                result.addInvalidUserId("无效用户ID: " + userId);
                continue;
            }

            Account account = accountMapperService.getById(userId);
            if (account == null) {
                result.addInvalidUserId("用户不存在: " + userId);
            } else if (!account.getIsActive()) {
                result.addInvalidUserId("用户已被禁用: " + userId + " (" + account.getUsername() + ")");
            } else {
                result.addValidUser(userId, account);
                log.debug("验证用户ID成功: {} ({})", userId, account.getUsername());
            }
        }

        return result;
    }

    /**
     * 验证手机号列表
     * @param phoneNumbers 手机号列表
     * @return 验证结果
     */
    public UserValidationResult validatePhoneNumbers(List<String> phoneNumbers) {
        UserValidationResult result = new UserValidationResult();
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            log.info("phoneValidate:skip empty");
            return result;
        }

        int inputs = phoneNumbers.size();
        // 预清洗+去重
        java.util.Map<String, String> cleanToOriginal = new java.util.HashMap<>();
        for (String phone : phoneNumbers) {
            if (phone == null || phone.trim().isEmpty()) {
                result.addInvalidPhone("手机号为空");
                continue;
            }
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            if (!isValidPhoneNumber(cleanPhone)) {
                result.addInvalidPhone("手机号格式错误: " + phone);
                continue;
            }
            cleanToOriginal.putIfAbsent(cleanPhone, phone);
        }
        int deduped = cleanToOriginal.size();

        // 批量查询，减少 N 次 DB 循环
        java.util.Map<String, Account> phoneToAccount = accountMapperService.findPreferredByPhones(cleanToOriginal.keySet());
        for (java.util.Map.Entry<String, String> entry : cleanToOriginal.entrySet()) {
            String cleanPhone = entry.getKey();
            String original = entry.getValue();
            Account account = phoneToAccount.get(cleanPhone);
            if (account == null) {
                result.addInvalidPhone("手机号未注册: " + original);
            } else if (!Boolean.TRUE.equals(account.getIsActive())) {
                result.addInvalidPhone("手机号用户已被禁用: " + original + " (" + account.getUsername() + ")");
            } else {
                result.addValidUser(account.getId(), account);
            }
        }

        log.info("phoneValidate:done inputs={} deduped={} validUsers={} invalidPhones={}",
                inputs, deduped, result.getValidUserCount(), result.getInvalidPhoneCount());
        return result;
    }

    /**
     * 验证手机号格式
     * @param phone 手机号
     * @return 是否有效
     */
    public boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // 移除所有非数字字符
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        
        // 使用正则表达式验证中国手机号格式
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * 批量验证用户ID和手机号
     * @param userIds 用户ID列表
     * @param phoneNumbers 手机号列表
     * @return 合并的验证结果
     */
    public UserValidationResult validateUsers(List<Long> userIds, List<String> phoneNumbers) {
        UserValidationResult userIdResult = validateUserIds(userIds);
        UserValidationResult phoneResult = validatePhoneNumbers(phoneNumbers);

        // 调试日志
        log.info("validateUsers - userIdResult有效用户数: {}, phoneResult有效用户数: {}", 
                userIdResult.getValidUsers().size(), phoneResult.getValidUsers().size());

        // 合并结果
        UserValidationResult mergedResult = new UserValidationResult();
        
        // 合并有效用户（去重）
        Map<Long, Account> validUsers = new HashMap<>();
        validUsers.putAll(userIdResult.getValidUsers());
        validUsers.putAll(phoneResult.getValidUsers());
        mergedResult.setValidUsers(validUsers);

        // 合并无效信息
        mergedResult.getInvalidUserIds().addAll(userIdResult.getInvalidUserIds());
        mergedResult.getInvalidPhoneNumbers().addAll(phoneResult.getInvalidPhoneNumbers());

        log.info("validateUsers - 合并后有效用户数: {}, 有效用户ID列表: {}", 
                mergedResult.getValidUsers().size(), mergedResult.getValidUserIds());

        return mergedResult;
    }

    /**
     * 用户验证结果
     */
    public static class UserValidationResult {
        private final Map<Long, Account> validUsers = new HashMap<>();
        private final List<String> invalidUserIds = new ArrayList<>();
        private final List<String> invalidPhoneNumbers = new ArrayList<>();

        public void addValidUser(Long userId, Account account) {
            validUsers.put(userId, account);
        }

        public void addInvalidUserId(String reason) {
            invalidUserIds.add(reason);
        }

        public void addInvalidPhone(String reason) {
            invalidPhoneNumbers.add(reason);
        }

        public Map<Long, Account> getValidUsers() {
            return validUsers;
        }

        public List<Long> getValidUserIds() {
            return new ArrayList<>(validUsers.keySet());
        }

        public List<String> getInvalidUserIds() {
            return invalidUserIds;
        }

        public List<String> getInvalidPhoneNumbers() {
            return invalidPhoneNumbers;
        }

        public void setValidUsers(Map<Long, Account> validUsers) {
            this.validUsers.clear();
            this.validUsers.putAll(validUsers);
        }

        public boolean hasValidUsers() {
            return !validUsers.isEmpty();
        }

        public int getValidUserCount() {
            return validUsers.size();
        }

        public int getInvalidUserIdCount() {
            return invalidUserIds.size();
        }

        public int getInvalidPhoneCount() {
            return invalidPhoneNumbers.size();
        }

        @Override
        public String toString() {
            return String.format("UserValidationResult{validUsers=%d, invalidUserIds=%d, invalidPhones=%d}", 
                validUsers.size(), invalidUserIds.size(), invalidPhoneNumbers.size());
        }
    }
}
