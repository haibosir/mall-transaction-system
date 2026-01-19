package com.mall.domain.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单领域模型测试
 *
 * @author mall
 */
class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .userId(1001L)
                .merchantId(2001L)
                .sku("PROD001")
                .productName("测试商品")
                .unitPrice(new BigDecimal("99.99"))
                .quantity(2)
                .totalAmount(new BigDecimal("199.98"))
                .status(Order.OrderStatus.PENDING)
                .currency("CNY")
                .build();
    }

    @Test
    void testMarkAsPaid_Success() {
        // Given
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());

        // When
        order.markAsPaid();

        // Then
        assertEquals(Order.OrderStatus.PAID, order.getStatus());
    }

    @Test
    void testMarkAsPaid_InvalidStatus() {
        // Given
        order.markAsPaid(); // 先标记为已支付

        // When & Then
        assertThrows(IllegalStateException.class, () -> order.markAsPaid());
    }

    @Test
    void testMarkAsFailed_Success() {
        // Given
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());

        // When
        order.markAsFailed();

        // Then
        assertEquals(Order.OrderStatus.FAILED, order.getStatus());
    }

    @Test
    void testMarkAsFailed_InvalidStatus() {
        // Given
        order.markAsPaid(); // 先标记为已支付

        // When & Then
        assertThrows(IllegalStateException.class, () -> order.markAsFailed());
    }

    @Test
    void testCancel_Success() {
        // Given
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());

        // When
        order.cancel();

        // Then
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void testCancel_PaidOrder() {
        // Given
        order.markAsPaid();

        // When & Then
        assertThrows(IllegalStateException.class, () -> order.cancel());
    }
}
