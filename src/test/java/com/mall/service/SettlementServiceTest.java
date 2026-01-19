package com.mall.service;

import com.mall.domain.merchant.MerchantAccount;
import com.mall.domain.merchant.ProductInventory;
import com.mall.mapper.MerchantAccountMapper;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.ProductInventoryMapper;
import com.mall.service.impl.SettlementServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 结算服务测试
 *
 * @author mall
 */
@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private MerchantAccountMapper merchantAccountMapper;
    @Mock
    private ProductInventoryMapper productInventoryMapper;

    @InjectMocks
    private SettlementServiceImpl settlementService;

    private Long merchantId;
    private LocalDate settlementDate;
    private MerchantAccount merchantAccount;

    @BeforeEach
    void setUp() {
        merchantId = 2001L;
        settlementDate = LocalDate.of(2024, 1, 15);
        merchantAccount = MerchantAccount.builder()
                .id(1L)
                .merchantId(merchantId)
                .balance(new BigDecimal("10000.00"))
                .currency("CNY")
                .build();
    }

    @Test
    void testSettleMerchant_Matched() {
        // Given
        BigDecimal totalOrderAmount = new BigDecimal("5000.00");
        merchantAccount.setBalance(totalOrderAmount);

        when(orderMapper.sumPaidOrderAmountByMerchantAndTimeRange(
                eq(merchantId), any(), any())).thenReturn(totalOrderAmount);
        when(merchantAccountMapper.selectByMerchantId(merchantId))
                .thenReturn(merchantAccount);

        // When
        SettlementService.SettlementResult result = settlementService.settleMerchant(merchantId, settlementDate);

        // Then
        assertNotNull(result);
        assertEquals(merchantId, result.getMerchantId());
        assertEquals(settlementDate, result.getSettlementDate());
        assertEquals(totalOrderAmount, result.getTotalOrderAmount());
        assertEquals(totalOrderAmount, result.getAccountBalance());
        assertEquals(BigDecimal.ZERO, result.getDifference());
        assertTrue(result.getMatched());
    }

    @Test
    void testSettleMerchant_NotMatched() {
        // Given
        BigDecimal totalOrderAmount = new BigDecimal("5000.00");
        BigDecimal accountBalance = new BigDecimal("6000.00");

        merchantAccount.setBalance(accountBalance);

        when(orderMapper.sumPaidOrderAmountByMerchantAndTimeRange(
                eq(merchantId), any(), any())).thenReturn(totalOrderAmount);
        when(merchantAccountMapper.selectByMerchantId(merchantId))
                .thenReturn(merchantAccount);

        // When
        SettlementService.SettlementResult result = settlementService.settleMerchant(merchantId, settlementDate);

        // Then
        assertNotNull(result);
        assertEquals(totalOrderAmount, result.getTotalOrderAmount());
        assertEquals(accountBalance, result.getAccountBalance());
        assertEquals(new BigDecimal("1000.00"), result.getDifference());
        assertFalse(result.getMatched());
    }

    @Test
    void testSettleMerchant_MerchantNotFound() {
        // Given
        when(orderMapper.sumPaidOrderAmountByMerchantAndTimeRange(
                eq(merchantId), any(), any())).thenReturn(BigDecimal.ZERO);
        when(merchantAccountMapper.selectByMerchantId(merchantId))
                .thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> settlementService.settleMerchant(merchantId, settlementDate));
    }

    @Test
    void testSettleAllMerchants() {
        // Given
        List<ProductInventory> inventories = Arrays.asList(
                ProductInventory.builder().merchantId(2001L).sku("SKU1").build(),
                ProductInventory.builder().merchantId(2002L).sku("SKU2").build()
        );

        when(productInventoryMapper.selectAll()).thenReturn(inventories);
        when(orderMapper.sumPaidOrderAmountByMerchantAndTimeRange(anyLong(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(merchantAccountMapper.selectByMerchantId(anyLong()))
                .thenReturn(merchantAccount);

        // When
        settlementService.settleAllMerchants(settlementDate);

        // Then
        verify(productInventoryMapper).selectAll();
        verify(orderMapper, times(2)).sumPaidOrderAmountByMerchantAndTimeRange(anyLong(), any(), any());
    }
}
