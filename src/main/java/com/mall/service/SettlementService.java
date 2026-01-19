package com.mall.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商家结算服务接口
 *
 * @author mall
 */
public interface SettlementService {

    /**
     * 执行商家结算
     *
     * @param merchantId     商家ID
     * @param settlementDate 结算日期
     * @return 结算结果信息
     */
    SettlementResult settleMerchant(Long merchantId, LocalDate settlementDate);

    /**
     * 结算所有商家
     *
     * @param settlementDate 结算日期
     */
    void settleAllMerchants(LocalDate settlementDate);

    /**
     * 结算结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class SettlementResult {
        private Long merchantId;
        private LocalDate settlementDate;
        private BigDecimal totalOrderAmount;
        private BigDecimal accountBalance;
        private BigDecimal difference;
        private Boolean matched;
    }
}
