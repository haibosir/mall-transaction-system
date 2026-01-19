package com.mall.controller;

import com.mall.dto.ApiResponse;
import com.mall.dto.ProductInventoryAddRequest;
import com.mall.dto.ProductInventoryCreateRequest;
import com.mall.domain.merchant.ProductInventory;
import com.mall.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商家控制器
 * 提供商家相关的REST API
 *
 * @author mall
 */
@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
@Slf4j
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * 创建商品库存
     * POST /api/merchants/{merchantId}/products
     *
     * @param merchantId 商家ID
     * @param request    创建商品库存请求
     * @return 创建的商品库存信息
     */
    @PostMapping("/{merchantId}/products")
    public ApiResponse<ProductInventory> createProduct(@PathVariable Long merchantId,
                                                        @Validated @RequestBody ProductInventoryCreateRequest request) {
        try {
            // 确保路径参数和请求体中的信息一致
            request.setMerchantId(merchantId);
            ProductInventory inventory = merchantService.createProductInventory(request);
            return ApiResponse.success("创建商品成功", inventory);
        } catch (IllegalArgumentException e) {
            log.warn("创建商品失败：{}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("创建商品异常", e);
            return ApiResponse.fail("创建商品失败：" + e.getMessage());
        }
    }

    /**
     * 增加商品库存
     * POST /api/merchants/{merchantId}/products/{sku}/inventory
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @param request    增加库存请求
     * @return 更新后的商品库存信息
     */
    @PostMapping("/{merchantId}/products/{sku}/inventory")
    public ApiResponse<ProductInventory> addInventory(@PathVariable Long merchantId,
                                                       @PathVariable String sku,
                                                       @Validated @RequestBody ProductInventoryAddRequest request) {
        try {
            // 确保路径参数和请求体中的信息一致
            request.setMerchantId(merchantId);
            request.setSku(sku);
            ProductInventory inventory = merchantService.addInventory(request);
            return ApiResponse.success("增加库存成功", inventory);
        } catch (IllegalArgumentException e) {
            log.warn("增加库存失败：{}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("增加库存异常", e);
            return ApiResponse.fail("增加库存失败：" + e.getMessage());
        }
    }

    /**
     * 查询商品库存信息
     * GET /api/merchants/{merchantId}/products/{sku}/inventory
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @return 商品库存信息
     */
    @GetMapping("/{merchantId}/products/{sku}/inventory")
    public ApiResponse<ProductInventory> getInventory(@PathVariable Long merchantId,
                                                       @PathVariable String sku) {
        try {
            ProductInventory inventory = merchantService.getProductInventory(merchantId, sku);
            return ApiResponse.success(inventory);
        } catch (IllegalArgumentException e) {
            log.warn("查询库存失败：{}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("查询库存异常", e);
            return ApiResponse.fail("查询库存失败：" + e.getMessage());
        }
    }
}
