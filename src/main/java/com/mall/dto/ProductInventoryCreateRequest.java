package com.mall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建商品库存请求DTO
 *
 * @author mall
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryCreateRequest {

    /**
     * 商家ID
     */
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    /**
     * 商品SKU
     */
    @NotNull(message = "商品SKU不能为空")
    private String sku;

    /**
     * 商品名称
     */
    @NotNull(message = "商品名称不能为空")
    private String productName;

    /**
     * 商品价格
     */
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    /**
     * 初始库存数量
     */
    @NotNull(message = "初始库存数量不能为空")
    @Min(value = 0, message = "初始库存数量不能为负数")
    private Integer quantity;

    /**
     * 货币类型（可选，默认为CNY）
     */
    private String currency;
}
