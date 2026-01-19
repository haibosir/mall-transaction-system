package com.mall.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户账户聚合根
 * 负责管理用户的预存现金账户
 *
 * @author mall
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户余额（预存现金）
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
     * 充值操作
     *
     * @param amount 充值金额
     * @throws IllegalArgumentException 如果充值金额小于等于0
     */
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        this.balance = this.balance.add(amount);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 扣款操作
     *
     * @param amount 扣款金额
     * @throws IllegalArgumentException 如果扣款金额小于等于0或余额不足
     */
    public void deduct(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("扣款金额必须大于0");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("账户余额不足");
        }
        this.balance = this.balance.subtract(amount);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 检查余额是否充足
     *
     * @param amount 需要检查的金额
     * @return true if balance >= amount
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
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
