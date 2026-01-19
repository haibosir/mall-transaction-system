package com.mall.service.impl;

import com.mall.domain.merchant.ProductInventory;
import com.mall.dto.ProductInventoryAddRequest;
import com.mall.dto.ProductInventoryCreateRequest;
import com.mall.exception.ProductAlreadyExistsException;
import com.mall.exception.InsufficientInventoryException;
import com.mall.mapper.ProductInventoryMapper;
import com.mall.service.MerchantService;
import com.mall.service.RedisInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商家服务实现类
 *
 * @author mall
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    private final ProductInventoryMapper productInventoryMapper;
    private final RedisInventoryService redisInventoryService;

    /**
     * 创建商品库存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductInventory createProductInventory(ProductInventoryCreateRequest request) {
        log.info("创建商品库存请求：merchantId={}, sku={}, productName={}, price={}, quantity={}",
                request.getMerchantId(), request.getSku(), request.getProductName(),
                request.getPrice(), request.getQuantity());

        // 检查商品是否已存在
        if (productInventoryMapper.selectByMerchantIdAndSku(request.getMerchantId(), request.getSku()) != null) {
            throw new ProductAlreadyExistsException(
                    "商品已存在：merchantId=" + request.getMerchantId() + ", sku=" + request.getSku());
        }

        // 创建商品库存
        ProductInventory inventory = ProductInventory.builder()
                .merchantId(request.getMerchantId())
                .sku(request.getSku())
                .productName(request.getProductName())
                .price(request.getPrice())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .currency(request.getCurrency() != null ? request.getCurrency() : "CNY")
                .version(0L)
                .build();
        inventory.initDefaults();

        // 保存库存到数据库
        productInventoryMapper.insert(inventory);
        
        // 同步库存到Redis
        redisInventoryService.initInventory(
                inventory.getMerchantId(), inventory.getSku(), inventory.getQuantity());
        
        log.info("创建商品库存成功：merchantId={}, sku={}, quantity={}",
                inventory.getMerchantId(), inventory.getSku(), inventory.getQuantity());

        return inventory;
    }

    /**
     * 增加商品库存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductInventory addInventory(ProductInventoryAddRequest request) {
        log.info("增加商品库存请求：merchantId={}, sku={}, quantity={}",
                request.getMerchantId(), request.getSku(), request.getQuantity());

        // 查找商品库存
        ProductInventory inventory = productInventoryMapper
                .selectByMerchantIdAndSku(request.getMerchantId(), request.getSku());
        if (inventory == null) {
            throw new InsufficientInventoryException(
                    "商品库存不存在：merchantId=" + request.getMerchantId() + ", sku=" + request.getSku());
        }

        // 增加数据库库存
        inventory.increaseInventory(request.getQuantity());
        productInventoryMapper.updateById(inventory);
        
        // 同步增加Redis库存
        redisInventoryService.increaseInventory(
                request.getMerchantId(), request.getSku(), request.getQuantity());
        
        log.info("增加商品库存成功：merchantId={}, sku={}, newQuantity={}",
                inventory.getMerchantId(), inventory.getSku(), inventory.getQuantity());

        return inventory;
    }

    /**
     * 获取商品库存信息
     */
    @Override
    public ProductInventory getProductInventory(Long merchantId, String sku) {
        ProductInventory inventory = productInventoryMapper.selectByMerchantIdAndSku(merchantId, sku);
        if (inventory == null) {
            throw new InsufficientInventoryException(
                    "商品库存不存在：merchantId=" + merchantId + ", sku=" + sku);
        }
        return inventory;
    }
}
