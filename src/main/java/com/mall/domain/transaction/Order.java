package com.mall.domain.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单聚合根
 * 负责管理订单信息和状态
 *
 * @author mall
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 商品单价
     */
    private BigDecimal unitPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     * PENDING: 待支付
     * PAID: 已支付
     * FAILED: 支付失败
     * CANCELLED: 已取消
     */
    private OrderStatus status;

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
     * 标记订单为已支付
     */
    public void markAsPaid() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("只有待支付状态的订单才能标记为已支付");
        }
        this.status = OrderStatus.PAID;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记订单为支付失败
     */
    public void markAsFailed() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("只有待支付状态的订单才能标记为支付失败");
        }
        this.status = OrderStatus.FAILED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 取消订单
     */
    public void cancel() {
        if (this.status == OrderStatus.PAID) {
            throw new IllegalStateException("已支付的订单不能取消");
        }
        this.status = OrderStatus.CANCELLED;
        this.updateTime = LocalDateTime.now();
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
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        if (currency == null || currency.isEmpty()) {
            currency = "CNY";
        }
        if (orderNo == null || orderNo.isEmpty()) {
            orderNo = generateOrderNo();
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        PENDING,    // 待支付
        PAID,       // 已支付
        FAILED,     // 支付失败
        CANCELLED   // 已取消
    }
}
