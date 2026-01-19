package com.mall.domain.merchant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 商品库存领域模型测试
 *
 * @author mall
 */
class ProductInventoryTest {

    private ProductInventory inventory;

    @BeforeEach
    void setUp() {
        inventory = ProductInventory.builder()
                .merchantId(2001L)
                .sku("PROD001")
                .productName("测试商品")
                .price(new BigDecimal("99.99"))
                .quantity(100)
                .currency("CNY")
                .build();
    }

    @Test
    void testIncreaseInventory_Success() {
        // Given
        Integer quantity = 50;
        Integer originalQuantity = inventory.getQuantity();

        // When
        inventory.increaseInventory(quantity);

        // Then
        assertEquals(originalQuantity + quantity, inventory.getQuantity());
    }

    @Test
    void testIncreaseInventory_InvalidQuantity() {
        // Given
        Integer invalidQuantity = 0;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> inventory.increaseInventory(invalidQuantity));
    }

    @Test
    void testDecreaseInventory_Success() {
        // Given
        Integer quantity = 30;
        Integer originalQuantity = inventory.getQuantity();

        // When
        inventory.decreaseInventory(quantity);

        // Then
        assertEquals(originalQuantity - quantity, inventory.getQuantity());
    }

    @Test
    void testDecreaseInventory_InsufficientInventory() {
        // Given
        Integer quantity = 200;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> inventory.decreaseInventory(quantity));
    }

    @Test
    void testDecreaseInventory_InvalidQuantity() {
        // Given
        Integer invalidQuantity = 0;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> inventory.decreaseInventory(invalidQuantity));
    }

    @Test
    void testHasEnoughInventory_True() {
        // Given
        Integer quantity = 50;

        // When
        boolean result = inventory.hasEnoughInventory(quantity);

        // Then
        assertTrue(result);
    }

    @Test
    void testHasEnoughInventory_False() {
        // Given
        Integer quantity = 200;

        // When
        boolean result = inventory.hasEnoughInventory(quantity);

        // Then
        assertFalse(result);
    }

    @Test
    void testCalculateTotalPrice_Success() {
        // Given
        Integer quantity = 5;

        // When
        BigDecimal totalPrice = inventory.calculateTotalPrice(quantity);

        // Then
        BigDecimal expected = inventory.getPrice().multiply(BigDecimal.valueOf(quantity));
        assertEquals(expected, totalPrice);
    }

    @Test
    void testCalculateTotalPrice_InvalidQuantity() {
        // Given
        Integer invalidQuantity = 0;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> inventory.calculateTotalPrice(invalidQuantity));
    }
}
