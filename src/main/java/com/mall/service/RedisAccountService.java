package com.mall.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Redis账户服务
 * 使用Lua脚本保证账户余额操作的原子性
 *
 * @author mall
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisAccountService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Lua脚本：扣减用户余额并增加商家余额（原子操作）
     * KEYS[1]: 用户账户key (account:user:{userId})
     * KEYS[2]: 商家账户key (account:merchant:{merchantId})
     * ARGV[1]: 扣减金额
     * 返回: 1表示成功，-1表示用户余额不足，-2表示用户账户不存在，-3表示商家账户不存在
     */
    private static final String TRANSFER_AMOUNT_SCRIPT =
            "local userKey = KEYS[1]\n" +
            "local merchantKey = KEYS[2]\n" +
            "local amount = tonumber(ARGV[1])\n" +
            "\n" +
            "local userBalance = redis.call('get', userKey)\n" +
            "if userBalance == false then\n" +
            "    return -2\n" +
            "end\n" +
            "\n" +
            "local userBalanceNum = tonumber(userBalance)\n" +
            "if userBalanceNum < amount then\n" +
            "    return -1\n" +
            "end\n" +
            "\n" +
            "local merchantBalance = redis.call('get', merchantKey)\n" +
            "if merchantBalance == false then\n" +
            "    return -3\n" +
            "end\n" +
            "\n" +
            "redis.call('decrbyfloat', userKey, amount)\n" +
            "redis.call('incrbyfloat', merchantKey, amount)\n" +
            "return 1";

    /**
     * Lua脚本：扣减用户余额
     * KEYS[1]: 用户账户key (account:user:{userId})
     * ARGV[1]: 扣减金额
     * 返回: 扣减后的余额，如果余额不足返回-1，如果账户不存在返回-2
     */
    private static final String DECREASE_USER_BALANCE_SCRIPT =
            "local userKey = KEYS[1]\n" +
            "local amount = tonumber(ARGV[1])\n" +
            "\n" +
            "local userBalance = redis.call('get', userKey)\n" +
            "if userBalance == false then\n" +
            "    return -2\n" +
            "end\n" +
            "\n" +
            "local userBalanceNum = tonumber(userBalance)\n" +
            "if userBalanceNum < amount then\n" +
            "    return -1\n" +
            "end\n" +
            "\n" +
            "local result = userBalanceNum - amount\n" +
            "redis.call('set', userKey, result)\n" +
            "return result";

    /**
     * Lua脚本：增加用户余额
     * KEYS[1]: 用户账户key (account:user:{userId})
     * ARGV[1]: 增加金额
     */
    private static final String INCREASE_USER_BALANCE_SCRIPT =
            "local userKey = KEYS[1]\n" +
            "local amount = tonumber(ARGV[1])\n" +
            "redis.call('incrbyfloat', userKey, amount)\n" +
            "return redis.call('get', userKey)";

    /**
     * Lua脚本：增加商家余额
     * KEYS[1]: 商家账户key (account:merchant:{merchantId})
     * ARGV[1]: 增加金额
     */
    private static final String INCREASE_MERCHANT_BALANCE_SCRIPT =
            "local merchantKey = KEYS[1]\n" +
            "local amount = tonumber(ARGV[1])\n" +
            "redis.call('incrbyfloat', merchantKey, amount)\n" +
            "return redis.call('get', merchantKey)";

    private DefaultRedisScript<Long> transferAmountScript;
    private DefaultRedisScript<Double> decreaseUserBalanceScript;
    private DefaultRedisScript<Double> increaseUserBalanceScript;
    private DefaultRedisScript<Double> increaseMerchantBalanceScript;

    @org.springframework.beans.factory.annotation.Autowired
    public void init() {
        // 初始化转账脚本
        transferAmountScript = new DefaultRedisScript<>();
        transferAmountScript.setScriptText(TRANSFER_AMOUNT_SCRIPT);
        transferAmountScript.setResultType(Long.class);

        // 初始化扣减用户余额脚本
        decreaseUserBalanceScript = new DefaultRedisScript<>();
        decreaseUserBalanceScript.setScriptText(DECREASE_USER_BALANCE_SCRIPT);
        decreaseUserBalanceScript.setResultType(Double.class);

        // 初始化增加用户余额脚本
        increaseUserBalanceScript = new DefaultRedisScript<>();
        increaseUserBalanceScript.setScriptText(INCREASE_USER_BALANCE_SCRIPT);
        increaseUserBalanceScript.setResultType(Double.class);

        // 初始化增加商家余额脚本
        increaseMerchantBalanceScript = new DefaultRedisScript<>();
        increaseMerchantBalanceScript.setScriptText(INCREASE_MERCHANT_BALANCE_SCRIPT);
        increaseMerchantBalanceScript.setResultType(Double.class);
    }

    /**
     * 原子性转账：扣减用户余额并增加商家余额
     *
     * @param userId     用户ID
     * @param merchantId 商家ID
     * @param amount     转账金额
     * @return true if 成功, false if 失败
     * @throws IllegalArgumentException 如果账户不存在或余额不足
     */
    public boolean transferAmount(Long userId, Long merchantId, BigDecimal amount) {
        String userKey = getUserAccountKey(userId);
        String merchantKey = getMerchantAccountKey(merchantId);
        List<String> keys = Arrays.asList(userKey, merchantKey);
        Long result = redisTemplate.execute(transferAmountScript, keys, amount.toString());
        
        if (result == null || result <= 0) {
            if (result != null && result == -1) {
                throw new IllegalArgumentException("用户账户余额不足：userId=" + userId);
            } else if (result != null && result == -2) {
                throw new IllegalArgumentException("用户账户不存在：userId=" + userId);
            } else if (result != null && result == -3) {
                throw new IllegalArgumentException("商家账户不存在：merchantId=" + merchantId);
            } else {
                throw new IllegalArgumentException("转账失败：userId=" + userId + ", merchantId=" + merchantId);
            }
        }
        
        log.info("Redis转账成功：userId={}, merchantId={}, amount={}", userId, merchantId, amount);
        return true;
    }

    /**
     * 初始化用户账户余额到Redis
     *
     * @param userId  用户ID
     * @param balance 余额
     */
    public void initUserAccount(Long userId, BigDecimal balance) {
        String key = getUserAccountKey(userId);
        redisTemplate.opsForValue().set(key, balance.toString());
        log.info("初始化Redis用户账户：key={}, balance={}", key, balance);
    }

    /**
     * 初始化商家账户余额到Redis
     *
     * @param merchantId 商家ID
     * @param balance    余额
     */
    public void initMerchantAccount(Long merchantId, BigDecimal balance) {
        String key = getMerchantAccountKey(merchantId);
        redisTemplate.opsForValue().set(key, balance.toString());
        log.info("初始化Redis商家账户：key={}, balance={}", key, balance);
    }

    /**
     * 获取用户账户余额
     *
     * @param userId 用户ID
     * @return 余额，如果不存在返回null
     */
    public BigDecimal getUserBalance(Long userId) {
        String key = getUserAccountKey(userId);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return new BigDecimal(value.toString());
    }

    /**
     * 获取商家账户余额
     *
     * @param merchantId 商家ID
     * @return 余额，如果不存在返回null
     */
    public BigDecimal getMerchantBalance(Long merchantId) {
        String key = getMerchantAccountKey(merchantId);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return new BigDecimal(value.toString());
    }

    /**
     * 回滚转账：增加用户余额并扣减商家余额
     *
     * @param userId     用户ID
     * @param merchantId 商家ID
     * @param amount     金额
     */
    public void rollbackTransfer(Long userId, Long merchantId, BigDecimal amount) {
        try {
            String userKey = getUserAccountKey(userId);
            String merchantKey = getMerchantAccountKey(merchantId);
            
            List<String> userKeys = Collections.singletonList(userKey);
            redisTemplate.execute(increaseUserBalanceScript, userKeys, amount.toString());
            // 注意：这里需要确保商家余额足够回滚，实际场景中可能需要先检查
            redisTemplate.opsForValue().increment(merchantKey, amount.negate().doubleValue());
            
            log.info("回滚Redis转账：userId={}, merchantId={}, amount={}", userId, merchantId, amount);
        } catch (Exception e) {
            log.error("回滚Redis转账失败：userId={}, merchantId={}, amount={}, error={}",
                    userId, merchantId, amount, e.getMessage(), e);
        }
    }

    /**
     * 获取用户账户key
     */
    private String getUserAccountKey(Long userId) {
        return "account:user:" + userId;
    }

    /**
     * 获取商家账户key
     */
    private String getMerchantAccountKey(Long merchantId) {
        return "account:merchant:" + merchantId;
    }
}
