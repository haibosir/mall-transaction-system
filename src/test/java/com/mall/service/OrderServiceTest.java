package com.mall.service;

import com.mall.domain.merchant.MerchantAccount;
import com.mall.domain.merchant.ProductInventory;
import com.mall.domain.transaction.Order;
import com.mall.domain.user.UserAccount;
import com.mall.dto.OrderCreateRequest;
import com.mall.mapper.MerchantAccountMapper;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.ProductInventoryMapper;
import com.mall.mapper.UserAccountMapper;
import com.mall.service.impl.UserAccountServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 订单服务测试
 *
 * @author mall
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UserAccountServiceImpl userAccountService;
    @Mock
    private UserAccountMapper userAccountMapper;
    @Mock
    private MerchantAccountMapper merchantAccountMapper;
    @Mock
    private ProductInventoryMapper productInventoryMapper;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Long userId;
    private Long merchantId;
    private String sku;
    private ProductInventory inventory;
    private UserAccount userAccount;
    private MerchantAccount merchantAccount;
    private OrderCreateRequest request;

    @BeforeEach
    void setUp() {
        userId = 1001L;
        merchantId = 2001L;
        sku = "PROD001";

        inventory = ProductInventory.builder()
                .id(1L)
                .merchantId(merchantId)
                .sku(sku)
                .productName("测试商品")
                .price(new BigDecimal("99.99"))
                .quantity(100)
                .currency("CNY")
                .build();

        userAccount = UserAccount.builder()
                .id(1L)
                .userId(userId)
                .balance(new BigDecimal("1000.00"))
                .currency("CNY")
                .build();

        merchantAccount = MerchantAccount.builder()
                .id(1L)
                .merchantId(merchantId)
                .balance(new BigDecimal("5000.00"))
                .currency("CNY")
                .build();

        request = OrderCreateRequest.builder()
                .userId(userId)
                .merchantId(merchantId)
                .sku(sku)
                .quantity(2)
                .build();
    }

    @Test
    void testCreateOrder_Success() {
        // Given
        when(productInventoryMapper.selectByMerchantIdAndSkuForUpdate(merchantId, sku))
                .thenReturn(inventory);
        when(userAccountMapper.selectByUserIdForUpdate(userId))
                .thenReturn(userAccount);
        when(merchantAccountMapper.selectByMerchantIdForUpdate(merchantId))
                .thenReturn(merchantAccount);

        when(productInventoryMapper.updateById(any(ProductInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userAccountMapper.updateById(any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(merchantAccountMapper.updateById(any(MerchantAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.insert(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertNotNull(result);
        assertEquals(Order.OrderStatus.PAID, result.getStatus());
        assertEquals(2, result.getQuantity());
        assertEquals(new BigDecimal("199.98"), result.getTotalAmount());

        // 验证库存被扣减
        verify(productInventoryMapper).updateById(argThat(inv -> inv.getQuantity() == 98));
        // 验证用户账户被扣款
        verify(userAccountMapper).updateById(argThat(acc -> acc.getBalance().equals(new BigDecimal("800.02"))));
        // 验证商家账户被加款
        verify(merchantAccountMapper).updateById(argThat(acc -> acc.getBalance().equals(new BigDecimal("5199.98"))));
        // 验证订单被保存
        verify(orderMapper).insert(any(Order.class));
    }

    @Test
    void testCreateOrder_ProductNotFound() {
        // Given
        when(productInventoryMapper.selectByMerchantIdAndSkuForUpdate(merchantId, sku))
                .thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
        verify(productInventoryMapper).selectByMerchantIdAndSkuForUpdate(merchantId, sku);
        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    void testCreateOrder_InsufficientInventory() {
        // Given
        inventory.setQuantity(1);
        when(productInventoryMapper.selectByMerchantIdAndSkuForUpdate(merchantId, sku))
                .thenReturn(inventory);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
        verify(productInventoryMapper).selectByMerchantIdAndSkuForUpdate(merchantId, sku);
        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    void testCreateOrder_InsufficientBalance() {
        // Given
        userAccount.setBalance(new BigDecimal("100.00"));
        when(productInventoryMapper.selectByMerchantIdAndSkuForUpdate(merchantId, sku))
                .thenReturn(inventory);
        when(userAccountMapper.selectByUserIdForUpdate(userId))
                .thenReturn(userAccount);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
        verify(productInventoryMapper).selectByMerchantIdAndSkuForUpdate(merchantId, sku);
        verify(userAccountMapper).selectByUserIdForUpdate(userId);
        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    void testCreateOrder_NewMerchantAccount() {
        // Given
        when(productInventoryMapper.selectByMerchantIdAndSkuForUpdate(merchantId, sku))
                .thenReturn(inventory);
        when(userAccountMapper.selectByUserIdForUpdate(userId))
                .thenReturn(userAccount);
        when(merchantAccountMapper.selectByMerchantIdForUpdate(merchantId))
                .thenReturn(null);

        when(productInventoryMapper.updateById(any(ProductInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userAccountMapper.updateById(any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(merchantAccountMapper.updateById(any(MerchantAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.insert(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertNotNull(result);
        assertEquals(Order.OrderStatus.PAID, result.getStatus());
        // 验证新建了商家账户
        verify(merchantAccountMapper).updateById(any(MerchantAccount.class));
    }

    @Test
    void testGetOrderByOrderNo_Success() {
        // Given
        String orderNo = "ORD123456";
        Order order = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .merchantId(merchantId)
                .build();

        when(orderMapper.selectByOrderNo(orderNo)).thenReturn(order);

        // When
        Order result = orderService.getOrderByOrderNo(orderNo);

        // Then
        assertNotNull(result);
        assertEquals(orderNo, result.getOrderNo());
        verify(orderMapper).selectByOrderNo(orderNo);
    }

    @Test
    void testGetOrderByOrderNo_NotFound() {
        // Given
        String orderNo = "ORD123456";
        when(orderMapper.selectByOrderNo(orderNo)).thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderByOrderNo(orderNo));
        verify(orderMapper).selectByOrderNo(orderNo);
    }
}
