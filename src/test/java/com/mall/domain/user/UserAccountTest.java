package com.mall.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户账户领域模型测试
 *
 * @author mall
 */
class UserAccountTest {

    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        userAccount = UserAccount.builder()
                .userId(1001L)
                .balance(new BigDecimal("100.00"))
                .currency("CNY")
                .build();
    }

    @Test
    void testDeposit_Success() {
        // Given
        BigDecimal amount = new BigDecimal("50.00");
        BigDecimal originalBalance = userAccount.getBalance();

        // When
        userAccount.deposit(amount);

        // Then
        assertEquals(originalBalance.add(amount), userAccount.getBalance());
    }

    @Test
    void testDeposit_InvalidAmount() {
        // Given
        BigDecimal invalidAmount = BigDecimal.ZERO;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userAccount.deposit(invalidAmount));
    }

    @Test
    void testDeposit_NullAmount() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userAccount.deposit(null));
    }

    @Test
    void testDeduct_Success() {
        // Given
        BigDecimal amount = new BigDecimal("30.00");
        BigDecimal originalBalance = userAccount.getBalance();

        // When
        userAccount.deduct(amount);

        // Then
        assertEquals(originalBalance.subtract(amount), userAccount.getBalance());
    }

    @Test
    void testDeduct_InsufficientBalance() {
        // Given
        BigDecimal amount = new BigDecimal("200.00");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userAccount.deduct(amount));
    }

    @Test
    void testDeduct_InvalidAmount() {
        // Given
        BigDecimal invalidAmount = BigDecimal.ZERO;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userAccount.deduct(invalidAmount));
    }

    @Test
    void testHasEnoughBalance_True() {
        // Given
        BigDecimal amount = new BigDecimal("50.00");

        // When
        boolean result = userAccount.hasEnoughBalance(amount);

        // Then
        assertTrue(result);
    }

    @Test
    void testHasEnoughBalance_False() {
        // Given
        BigDecimal amount = new BigDecimal("200.00");

        // When
        boolean result = userAccount.hasEnoughBalance(amount);

        // Then
        assertFalse(result);
    }
}
