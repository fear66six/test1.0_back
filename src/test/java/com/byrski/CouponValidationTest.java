package com.byrski;

import com.byrski.common.utils.UserValidationUtils;
import com.byrski.domain.entity.dto.Account;
import com.byrski.infrastructure.mapper.impl.AccountMapperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 优惠券用户验证测试
 */
public class CouponValidationTest {

    @Mock
    private AccountMapperService accountMapperService;

    @InjectMocks
    private UserValidationUtils userValidationUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidateUserIds() {
        // 准备测试数据
        List<Long> userIds = Arrays.asList(1L, 2L, 3L, null, -1L);
        
        // 模拟用户数据
        Account validUser1 = createMockAccount(1L, "用户1", "13800138001", true);
        Account validUser2 = createMockAccount(2L, "用户2", "13800138002", true);
        Account inactiveUser = createMockAccount(3L, "禁用用户", "13800138003", false);
        
        // 设置Mock行为
        when(accountMapperService.getById(1L)).thenReturn(validUser1);
        when(accountMapperService.getById(2L)).thenReturn(validUser2);
        when(accountMapperService.getById(3L)).thenReturn(inactiveUser);
        when(accountMapperService.getById(null)).thenReturn(null);
        when(accountMapperService.getById(-1L)).thenReturn(null);
        
        // 执行测试
        UserValidationUtils.UserValidationResult result = userValidationUtils.validateUserIds(userIds);
        
        // 验证结果
        assertEquals(2, result.getValidUserCount());
        assertEquals(3, result.getInvalidUserIdCount());
        assertTrue(result.getValidUserIds().contains(1L));
        assertTrue(result.getValidUserIds().contains(2L));
        assertFalse(result.getValidUserIds().contains(3L));
        
        // 验证无效用户ID的原因
        assertTrue(result.getInvalidUserIds().stream().anyMatch(reason -> reason.contains("无效用户ID: null")));
        assertTrue(result.getInvalidUserIds().stream().anyMatch(reason -> reason.contains("无效用户ID: -1")));
        assertTrue(result.getInvalidUserIds().stream().anyMatch(reason -> reason.contains("用户已被禁用")));
    }

    @Test
    void testValidatePhoneNumbers() {
        // 准备测试数据
        List<String> phoneNumbers = Arrays.asList(
            "13800138001", "13800138002", "13800138003", 
            "invalid_phone", "", null, "12345678901"
        );
        
        // 模拟用户数据
        Account validUser1 = createMockAccount(1L, "用户1", "13800138001", true);
        Account validUser2 = createMockAccount(2L, "用户2", "13800138002", true);
        Account inactiveUser = createMockAccount(3L, "禁用用户", "13800138003", false);
        
        // 设置Mock行为
        when(accountMapperService.findByPhone("13800138001")).thenReturn(validUser1);
        when(accountMapperService.findByPhone("13800138002")).thenReturn(validUser2);
        when(accountMapperService.findByPhone("13800138003")).thenReturn(inactiveUser);
        when(accountMapperService.findByPhone("invalid_phone")).thenReturn(null);
        when(accountMapperService.findByPhone("")).thenReturn(null);
        when(accountMapperService.findByPhone(null)).thenReturn(null);
        when(accountMapperService.findByPhone("12345678901")).thenReturn(null);
        
        // 执行测试
        UserValidationUtils.UserValidationResult result = userValidationUtils.validatePhoneNumbers(phoneNumbers);
        
        // 验证结果
        assertEquals(2, result.getValidUserCount());
        assertEquals(5, result.getInvalidPhoneCount());
        assertTrue(result.getValidUserIds().contains(1L));
        assertTrue(result.getValidUserIds().contains(2L));
        assertFalse(result.getValidUserIds().contains(3L));
        
        // 验证无效手机号的原因
        assertTrue(result.getInvalidPhoneNumbers().stream().anyMatch(reason -> reason.contains("手机号为空")));
        assertTrue(result.getInvalidPhoneNumbers().stream().anyMatch(reason -> reason.contains("手机号格式错误")));
        assertTrue(result.getInvalidPhoneNumbers().stream().anyMatch(reason -> reason.contains("手机号未注册")));
        assertTrue(result.getInvalidPhoneNumbers().stream().anyMatch(reason -> reason.contains("手机号用户已被禁用")));
    }

    @Test
    void testValidateUsersWithDuplicates() {
        // 准备测试数据 - 用户ID和手机号指向同一个用户
        List<Long> userIds = Arrays.asList(1L, 2L);
        List<String> phoneNumbers = Arrays.asList("13800138001", "13800138003"); // 13800138001对应用户1
        
        // 模拟用户数据
        Account user1 = createMockAccount(1L, "用户1", "13800138001", true);
        Account user2 = createMockAccount(2L, "用户2", "13800138002", true);
        Account user3 = createMockAccount(3L, "用户3", "13800138003", true);
        
        // 设置Mock行为
        when(accountMapperService.getById(1L)).thenReturn(user1);
        when(accountMapperService.getById(2L)).thenReturn(user2);
        when(accountMapperService.findByPhone("13800138001")).thenReturn(user1);
        when(accountMapperService.findByPhone("13800138003")).thenReturn(user3);
        
        // 执行测试
        UserValidationUtils.UserValidationResult result = userValidationUtils.validateUsers(userIds, phoneNumbers);
        
        // 验证结果 - 应该去重，只包含3个有效用户
        assertEquals(3, result.getValidUserCount());
        assertTrue(result.getValidUserIds().contains(1L));
        assertTrue(result.getValidUserIds().contains(2L));
        assertTrue(result.getValidUserIds().contains(3L));
    }

    @Test
    void testPhoneNumberFormatValidation() {
        // 测试有效的手机号格式
        assertTrue(userValidationUtils.isValidPhoneNumber("13800138001"));
        assertTrue(userValidationUtils.isValidPhoneNumber("138-0013-8001"));
        assertTrue(userValidationUtils.isValidPhoneNumber("138 0013 8001"));
        assertTrue(userValidationUtils.isValidPhoneNumber("+86 138 0013 8001"));
        
        // 测试无效的手机号格式
        assertFalse(userValidationUtils.isValidPhoneNumber(""));
        assertFalse(userValidationUtils.isValidPhoneNumber(null));
        assertFalse(userValidationUtils.isValidPhoneNumber("1234567890")); // 10位
        assertFalse(userValidationUtils.isValidPhoneNumber("123456789012")); // 12位
        assertFalse(userValidationUtils.isValidPhoneNumber("23800138001")); // 不以1开头
        assertFalse(userValidationUtils.isValidPhoneNumber("invalid"));
    }

    private Account createMockAccount(Long id, String username, String phone, boolean isActive) {
        Account account = new Account();
        account.setId(id);
        account.setUsername(username);
        account.setPhone(phone);
        account.setIsActive(isActive);
        return account;
    }
}
