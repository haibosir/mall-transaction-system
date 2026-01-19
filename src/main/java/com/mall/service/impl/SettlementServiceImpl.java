package com.mall.service.impl;

import com.mall.domain.merchant.MerchantAccount;
import com.mall.domain.merchant.ProductInventory;
import com.mall.exception.MerchantNotFoundException;
import com.mall.mapper.MerchantAccountMapper;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.ProductInventoryMapper;
import com.mall.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 商家结算服务实现类
 * 每天定时结算，对库存中卖出的商品价值和商家账户的余额进行匹配
 *
 * @author mall
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementServiceImpl implements SettlementService {

    private final OrderMapper orderMapper;
    private final MerchantAccountMapper merchantAccountMapper;
    private final ProductInventoryMapper productInventoryMapper;

    /**
     * 执行商家结算
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SettlementResult settleMerchant(Long merchantId, LocalDate settlementDate) {
        log.info("开始商家结算：merchantId={}, settlementDate={}", merchantId, settlementDate);

        // 计算时间范围（当天的开始和结束时间）
        LocalDateTime startTime = settlementDate.atStartOfDay();
        LocalDateTime endTime = settlementDate.atTime(LocalTime.MAX);

        // 查询指定日期内的已支付订单总金额
        BigDecimal totalOrderAmount = orderMapper.sumPaidOrderAmountByMerchantAndTimeRange(
                merchantId, startTime, endTime);

        // 获取商家账户余额
        MerchantAccount merchantAccount = merchantAccountMapper.selectByMerchantId(merchantId);
        if (merchantAccount == null) {
            throw new MerchantNotFoundException("商家账户不存在：merchantId=" + merchantId);
        }

        BigDecimal accountBalance = merchantAccount.getBalance();

        // 计算差值
        BigDecimal difference = accountBalance.subtract(totalOrderAmount);

        log.info("商家结算完成：merchantId={}, 订单总金额={}, 账户余额={}, 差值={}",
                merchantId, totalOrderAmount, accountBalance, difference);

        return SettlementResult.builder()
                .merchantId(merchantId)
                .settlementDate(settlementDate)
                .totalOrderAmount(totalOrderAmount)
                .accountBalance(accountBalance)
                .difference(difference)
                .matched(Math.abs(difference.doubleValue()) < 0.01) // 允许0.01的误差
                .build();
    }

    /**
     * 结算所有商家
     */
    @Override
    public void settleAllMerchants(LocalDate settlementDate) {
        log.info("开始结算所有商家：settlementDate={}", settlementDate);

        // 获取所有有商品库存的商家ID
        List<ProductInventory> inventories = productInventoryMapper.selectAll();
        inventories.stream()
                .map(ProductInventory::getMerchantId)
                .distinct()
                .forEach(merchantId -> {
                    try {
                        settleMerchant(merchantId, settlementDate);
                    } catch (Exception e) {
                        log.error("商家结算失败：merchantId={}, error={}", merchantId, e.getMessage(), e);
                    }
                });

        log.info("所有商家结算完成：settlementDate={}", settlementDate);
    }
}
