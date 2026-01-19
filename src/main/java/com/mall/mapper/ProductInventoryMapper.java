package com.mall.mapper;

import com.mall.domain.merchant.ProductInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存Mapper
 *
 * @author mall
 */
@Mapper
public interface ProductInventoryMapper {

    /**
     * 插入商品库存
     */
    int insert(ProductInventory productInventory);

    /**
     * 根据ID更新商品库存
     */
    int updateById(ProductInventory productInventory);

    /**
     * 根据ID查找商品库存
     */
    ProductInventory selectById(Long id);

    /**
     * 根据商家ID和SKU查找商品库存
     */
    ProductInventory selectByMerchantIdAndSku(@Param("merchantId") Long merchantId, @Param("sku") String sku);

    /**
     * 根据商家ID和SKU查找商品库存（加悲观锁 FOR UPDATE）
     */
    ProductInventory selectByMerchantIdAndSkuForUpdate(@Param("merchantId") Long merchantId, @Param("sku") String sku);

    /**
     * 根据商家ID查找所有商品库存
     */
    List<ProductInventory> selectByMerchantId(Long merchantId);

    /**
     * 查找所有商品库存
     */
    List<ProductInventory> selectAll();
}
