package com.mall.service.impl;

import com.mall.domain.user.UserAccount;
import com.mall.dto.UserAccountDepositRequest;
import com.mall.exception.UserNotFoundException;
import com.mall.mapper.UserAccountMapper;
import com.mall.service.RedisAccountService;
import com.mall.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 用户账户服务实现类
 *
 * @author mall
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountMapper userAccountMapper;
    private final RedisAccountService redisAccountService;

    /**
     * 用户账户充值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAccount deposit(UserAccountDepositRequest request) {
        log.info("用户账户充值请求：userId={}, amount={}", request.getUserId(), request.getAmount());

        // 查找或创建用户账户
        UserAccount account = Optional.ofNullable(userAccountMapper.selectByUserId(request.getUserId()))
                .orElseGet(() -> {
                    log.info("用户账户不存在，创建新账户：userId={}", request.getUserId());
                    UserAccount newAccount = UserAccount.builder()
                            .userId(request.getUserId())
                            .balance(BigDecimal.ZERO)
                            .currency(request.getCurrency() != null ? request.getCurrency() : "CNY")
                            .version(0L)
                            .build();
                    newAccount.initDefaults();
                    userAccountMapper.insert(newAccount);
                    return newAccount;
                });

        // 执行充值
        account.deposit(request.getAmount());

        // 保存账户到数据库
        userAccountMapper.updateById(account);
        
        // 同步更新Redis账户余额
        redisAccountService.initUserAccount(account.getUserId(), account.getBalance());
        
        log.info("用户账户充值成功：userId={}, newBalance={}", account.getUserId(), account.getBalance());

        return account;
    }

    /**
     * 获取用户账户信息
     */
    @Override
    public UserAccount getUserAccount(Long userId) {
        UserAccount account = userAccountMapper.selectByUserId(userId);
        if (account == null) {
            throw new UserNotFoundException("用户账户不存在：userId=" + userId);
        }
        return account;
    }
}
