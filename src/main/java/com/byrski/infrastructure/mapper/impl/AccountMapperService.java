package com.byrski.infrastructure.mapper.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.byrski.domain.entity.dto.Trade;
import com.byrski.domain.enums.UserIdentity;
import com.byrski.infrastructure.mapper.AccountMapper;
import com.byrski.domain.entity.dto.Account;
import com.byrski.domain.user.LoginUser;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AccountMapperService extends ServiceImpl<AccountMapper, Account> {

    public Account findAccountByUsernameOrEmail(String text) {
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one();
    }

    public boolean updateUserDataByTrade(String name, Integer gender, String idCardNumber, String phone, Long schoolId) {
        // 创建更新条件
        LambdaUpdateWrapper<Account> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Account::getId, LoginUser.getLoginUserId());

        // 创建要更新的对象
        Account account = new Account();
        account.setUsername(name);
        account.setGender(gender);
        account.setIdCardNumber(idCardNumber);
        account.setPhone(phone);
        account.setSchoolId(schoolId);

        // 执行更新操作
        return this.update(account, updateWrapper);
    }

    public Account findAccountByEmail(String email) {
        return this.query()
                .eq("email", email)
                .one();
    }

    public boolean existsAccountByEmail(String email) {
        return this.query()
                .eq("email", email)
                .exists();
    }

    public boolean existsAccountByUsername(String username) {
        return this.query()
                .eq("username", username)
                .exists();
    }

    public Account findByOpenid(String openid) {
        return this.query()
                .eq("openid", openid)
                .one();
    }

    public void addPointsAndSavedMoney(Trade trade, int v) {
        Long userId = trade.getUserId();
        this.lambdaUpdate()
                .eq(Account::getId, userId)
                .setSql("points = points + " + 0)
                .setSql("saved_money = saved_money + " + v)
                .update();
    }

    public void subPointsAndSavedMoney(Trade trade, int v) {
        Long userId = trade.getUserId();
        this.lambdaUpdate()
                .eq(Account::getId, userId)
                .setSql("points = points - " + 0)
                .setSql("saved_money = saved_money - " + v)
                .update();
    }

    public Map<Long, Account> getByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return this.query()
                .in("id", userIds)
                .list()
                .stream()
                .collect(Collectors.toMap(Account::getId, account -> account));
    }

    public boolean isLeader(Long id) {
        return this.lambdaQuery().eq(Account::getId, id).eq(Account::getIdentity, UserIdentity.LEADER.getCode()).exists();
    }

    // 根据用户名模糊匹配
    public Set<Long> findIdsByUsernameLike(String username) {
        return this.query()
                .like("username", username)
                .list()
                .stream()
                .map(Account::getId)
                .collect(Collectors.toSet());
    }

    // 根据手机号查找用户：存在重复手机号时不抛异常，优先返回启用账号，其次返回最近更新的账号
    public Account findByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        // 按更新时间倒序，避免 selectOne 在重复数据时抛 TooManyResultsException
        var accounts = this.lambdaQuery()
                .eq(Account::getPhone, phone)
                .orderByDesc(Account::getUpdateTime)
                .list();
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }
        // 优先选择启用的账号
        for (Account a : accounts) {
            if (Boolean.TRUE.equals(a.getIsActive())) {
                return a;
            }
        }
        // 都未启用则返回第一条（最新）
        return accounts.get(0);
    }

    /**
     * 批量按手机号查询账号，返回每个手机号对应的优先账号（优先启用账号，否则按更新时间最新）
     */
    public Map<String, Account> findPreferredByPhones(java.util.Set<String> phones) {
        if (phones == null || phones.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        var list = this.lambdaQuery()
                .in(Account::getPhone, phones)
                .orderByDesc(Account::getUpdateTime)
                .list();
        if (list == null || list.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        // phone -> 最佳账号（先挑启用的；否则取分组中的第一条，即更新时间最新）
        return list.stream().collect(java.util.stream.Collectors.groupingBy(
                Account::getPhone,
                java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        accounts -> {
                            for (Account a : accounts) {
                                if (Boolean.TRUE.equals(a.getIsActive())) {
                                    return a;
                                }
                            }
                            return accounts.get(0);
                        }
                )
        ));
    }
}
