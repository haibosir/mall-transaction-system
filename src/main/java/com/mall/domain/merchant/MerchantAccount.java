package com.mall.domain.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家账户聚合根
 * 负责管理商家的账户余额
 *
 * @author mall
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantAccount {

    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 账户余额
     */
    private BigDecimal balance;

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
     * 收款操作
     *
     * @param amount 收款金额
     * @throws IllegalArgumentException 如果收款金额小于等于0
     */
    public void receive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("收款金额必须大于0");
        }
        this.balance = this.balance.add(amount);
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
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (currency == null || currency.isEmpty()) {
            currency = "CNY";
        }
    }
}
