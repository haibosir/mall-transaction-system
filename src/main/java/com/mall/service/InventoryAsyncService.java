package com.mall.service;

import com.mall.domain.merchant.ProductInventory;
import com.mall.mapper.ProductInventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存异步服务
 * 异步更新数据库库存，避免阻塞主流程
 *
 * @author mall
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryAsyncService {

    private final ProductInventoryMapper productInventoryMapper;
    private final RedisInventoryService redisInventoryService;

    /**
     * 异步扣减数据库库存
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @param quantity   扣减数量
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void decreaseDatabaseInventory(Long merchantId, String sku, Integer quantity) {
        try {
            log.info("开始异步更新数据库库存：merchantId={}, sku={}, quantity={}", merchantId, sku, quantity);

            ProductInventory inventory = productInventoryMapper.selectByMerchantIdAndSku(merchantId, sku);
            if (inventory == null) {
                log.error("商品库存不存在，无法更新：merchantId={}, sku={}", merchantId, sku);
                // 回滚Redis库存
                redisInventoryService.increaseInventory(merchantId, sku, quantity);
                return;
            }

            // 扣减数据库库存
            inventory.decreaseInventory(quantity);
            productInventoryMapper.updateById(inventory);

            log.info("数据库库存更新成功：merchantId={}, sku={}, quantity={}, newQuantity={}",
                    merchantId, sku, quantity, inventory.getQuantity());
        } catch (Exception e) {
            log.error("异步更新数据库库存失败：merchantId={}, sku={}, quantity={}, error={}",
                    merchantId, sku, quantity, e.getMessage(), e);
            // 回滚Redis库存
            try {
                redisInventoryService.increaseInventory(merchantId, sku, quantity);
                log.info("已回滚Redis库存：merchantId={}, sku={}, quantity={}", merchantId, sku, quantity);
            } catch (Exception ex) {
                log.error("回滚Redis库存失败：merchantId={}, sku={}, quantity={}, error={}",
                        merchantId, sku, quantity, ex.getMessage(), ex);
            }
        }
    }

    /**
     * 异步增加数据库库存
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @param quantity   增加数量
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void increaseDatabaseInventory(Long merchantId, String sku, Integer quantity) {
        try {
            log.info("开始异步增加数据库库存：merchantId={}, sku={}, quantity={}", merchantId, sku, quantity);

            ProductInventory inventory = productInventoryMapper.selectByMerchantIdAndSku(merchantId, sku);
            if (inventory == null) {
                log.error("商品库存不存在，无法更新：merchantId={}, sku={}", merchantId, sku);
                return;
            }

            // 增加数据库库存
            inventory.increaseInventory(quantity);
            productInventoryMapper.updateById(inventory);

            log.info("数据库库存增加成功：merchantId={}, sku={}, quantity={}, newQuantity={}",
                    merchantId, sku, quantity, inventory.getQuantity());
        } catch (Exception e) {
            log.error("异步增加数据库库存失败：merchantId={}, sku={}, sku={}, quantity={}, error={}",
                    merchantId, sku, quantity, e.getMessage(), e);
        }
    }
}
