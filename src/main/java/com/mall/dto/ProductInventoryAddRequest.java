package com.mall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 商品库存增加请求DTO
 *
 * @author mall
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryAddRequest {

    /**
     * 商家ID（路径参数，可留空）
     */
    private Long merchantId;

    /**
     * 商品SKU（路径参数，可留空）
     */
    private String sku;

    /**
     * 增加的数量
     */
    @NotNull(message = "增加数量不能为空")
    @Min(value = 1, message = "增加数量必须大于0")
    private Integer quantity;
}
