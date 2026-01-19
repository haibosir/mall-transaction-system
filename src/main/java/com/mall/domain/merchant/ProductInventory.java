package com.mall.domain.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品库存聚合根
 * 负责管理商家的商品库存信息
 *
 * @author mall
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventory {

    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 商品SKU
     */
    private String sku;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer quantity;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 版本号，用于乐观锁
     */
    private Long version;

    /**
     * 增加库存
     *
     * @param quantity 增加的数量
     * @throws IllegalArgumentException 如果数量小于等于0
     */
    public void increaseInventory(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("增加库存数量必须大于0");
        }
        this.quantity = this.quantity + quantity;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 减少库存
     *
     * @param quantity 减少的数量
     * @throws IllegalArgumentException 如果数量小于等于0或库存不足
     */
    public void decreaseInventory(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("减少库存数量必须大于0");
        }
        if (this.quantity < quantity) {
            throw new IllegalArgumentException("库存不足，当前库存：" + this.quantity);
        }
        this.quantity = this.quantity - quantity;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 检查库存是否充足
     *
     * @param quantity 需要检查的数量
     * @return true if inventory >= quantity
     */
    public boolean hasEnoughInventory(Integer quantity) {
        return this.quantity >= quantity;
    }

    /**
     * 计算指定数量的商品总价
     *
     * @param quantity 商品数量
     * @return 总价
     */
    public BigDecimal calculateTotalPrice(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("商品数量必须大于0");
        }
        return this.price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * 初始化默认值
     */
    public void initDefaults() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
        if (quantity == null) {
            quantity = 0;
        }
        if (currency == null || currency.isEmpty()) {
            currency = "CNY";
        }
    }
}
