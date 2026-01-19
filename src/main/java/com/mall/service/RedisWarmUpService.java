package com.mall.service;

import com.mall.domain.merchant.MerchantAccount;
import com.mall.domain.merchant.ProductInventory;
import com.mall.domain.user.UserAccount;
import com.mall.mapper.MerchantAccountMapper;
import com.mall.mapper.ProductInventoryMapper;
import com.mall.mapper.UserAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis预热服务
 * 在项目启动时将数据库中的数据加载到Redis
 *
 * @author mall
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisWarmUpService implements CommandLineRunner {

    private final ProductInventoryMapper productInventoryMapper;
    private final UserAccountMapper userAccountMapper;
    private final MerchantAccountMapper merchantAccountMapper;
    private final RedisInventoryService redisInventoryService;
    private final RedisAccountService redisAccountService;

    @Override
    public void run(String... args) {
        log.info("开始Redis预热...");
        try {
            warmUpInventory();
            warmUpUserAccounts();
            warmUpMerchantAccounts();
            log.info("Redis预热完成");
        } catch (Exception e) {
            log.error("Redis预热失败", e);
        }
    }

    /**
     * 预热商品库存
     */
    private void warmUpInventory() {
        try {
            log.info("开始预热商品库存到Redis...");
            List<ProductInventory> inventories = productInventoryMapper.selectAll();
            int count = 0;
            for (ProductInventory inventory : inventories) {
                redisInventoryService.initInventory(
                        inventory.getMerchantId(),
                        inventory.getSku(),
                        inventory.getQuantity());
                count++;
            }
            log.info("商品库存预热完成，共预热 {} 条记录", count);
        } catch (Exception e) {
            log.error("预热商品库存失败", e);
        }
    }

    /**
     * 预热用户账户余额
     */
    private void warmUpUserAccounts() {
        try {
            log.info("开始预热用户账户余额到Redis...");
            List<UserAccount> userAccounts = userAccountMapper.selectAll();
            int count = 0;
            for (UserAccount account : userAccounts) {
                redisAccountService.initUserAccount(account.getUserId(), account.getBalance());
                count++;
            }
            log.info("用户账户余额预热完成，共预热 {} 条记录", count);
        } catch (Exception e) {
            log.error("预热用户账户余额失败", e);
        }
    }

    /**
     * 预热商家账户余额
     */
    private void warmUpMerchantAccounts() {
        try {
            log.info("开始预热商家账户余额到Redis...");
            List<MerchantAccount> merchantAccounts = merchantAccountMapper.selectAll();
            int count = 0;
            for (MerchantAccount account : merchantAccounts) {
                redisAccountService.initMerchantAccount(account.getMerchantId(), account.getBalance());
                count++;
            }
            log.info("商家账户余额预热完成，共预热 {} 条记录", count);
        } catch (Exception e) {
            log.error("预热商家账户余额失败", e);
        }
    }
}
