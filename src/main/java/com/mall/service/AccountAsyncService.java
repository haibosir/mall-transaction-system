package com.mall.service;

import com.mall.domain.merchant.MerchantAccount;
import com.mall.domain.user.UserAccount;
import com.mall.mapper.MerchantAccountMapper;
import com.mall.mapper.UserAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 账户异步服务
 * 异步更新数据库账户余额，避免阻塞主流程
 *
 * @author mall
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountAsyncService {

    private final UserAccountMapper userAccountMapper;
    private final MerchantAccountMapper merchantAccountMapper;
    private final RedisAccountService redisAccountService;

    /**
     * 异步更新数据库账户余额（转账）
     *
     * @param userId     用户ID
     * @param merchantId 商家ID
     * @param amount     转账金额
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void updateDatabaseAccountBalance(Long userId, Long merchantId, BigDecimal amount) {
        try {
            log.info("开始异步更新数据库账户余额：userId={}, merchantId={}, amount={}", userId, merchantId, amount);

            // 获取Redis中的余额作为最新值
            BigDecimal userBalance = redisAccountService.getUserBalance(userId);
            BigDecimal merchantBalance = redisAccountService.getMerchantBalance(merchantId);

            if (userBalance == null) {
                log.error("Redis中用户账户不存在，无法更新数据库：userId={}", userId);
                return;
            }

            if (merchantBalance == null) {
                log.error("Redis中商家账户不存在，无法更新数据库：merchantId={}", merchantId);
                return;
            }

            // 更新用户账户余额
            UserAccount userAccount = userAccountMapper.selectByUserId(userId);
            if (userAccount != null) {
                userAccount.setBalance(userBalance);
                userAccountMapper.updateById(userAccount);
                log.info("数据库用户账户余额更新成功：userId={}, balance={}", userId, userBalance);
            } else {
                log.error("数据库用户账户不存在：userId={}", userId);
            }

            // 更新商家账户余额
            MerchantAccount merchantAccount = merchantAccountMapper.selectByMerchantId(merchantId);
            if (merchantAccount != null) {
                merchantAccount.setBalance(merchantBalance);
                merchantAccountMapper.updateById(merchantAccount);
                log.info("数据库商家账户余额更新成功：merchantId={}, balance={}", merchantId, merchantBalance);
            } else {
                log.error("数据库商家账户不存在：merchantId={}", merchantId);
            }

        } catch (Exception e) {
            log.error("异步更新数据库账户余额失败：userId={}, merchantId={}, amount={}, error={}",
                    userId, merchantId, amount, e.getMessage(), e);
            // 注意：这里不进行回滚Redis，因为Redis余额已经是最终状态
        }
    }
}
