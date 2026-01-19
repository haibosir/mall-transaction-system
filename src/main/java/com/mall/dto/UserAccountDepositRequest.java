package com.mall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 用户账户充值请求DTO
 *
 * @author mall
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountDepositRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 充值金额
     */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;

    /**
     * 货币类型（可选，默认为CNY）
     */
    private String currency;
}
