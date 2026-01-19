package com.mall.service;

import com.mall.domain.merchant.ProductInventory;
import com.mall.dto.ProductInventoryAddRequest;
import com.mall.dto.ProductInventoryCreateRequest;

/**
 * 商家服务接口
 *
 * @author mall
 */
public interface MerchantService {

    /**
     * 创建商品库存
     *
     * @param request 创建商品库存请求
     * @return 创建的商品库存信息
     */
    ProductInventory createProductInventory(ProductInventoryCreateRequest request);

    /**
     * 增加商品库存
     *
     * @param request 增加库存请求
     * @return 更新后的商品库存信息
     */
    ProductInventory addInventory(ProductInventoryAddRequest request);

    /**
     * 获取商品库存信息
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @return 商品库存
     */
    ProductInventory getProductInventory(Long merchantId, String sku);
}
