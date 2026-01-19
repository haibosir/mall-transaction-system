package com.mall.service;

import com.mall.domain.merchant.ProductInventory;
import com.mall.dto.ProductInventoryAddRequest;
import com.mall.mapper.ProductInventoryMapper;
import com.mall.service.impl.MerchantServiceImpl;

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
 * 商家服务测试
 *
 * @author mall
 */
@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private ProductInventoryMapper productInventoryMapper;
    
    @Mock
    private RedisInventoryService redisInventoryService;

    @InjectMocks
    private MerchantServiceImpl merchantService;

    private Long merchantId;
    private String sku;
    private ProductInventory inventory;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void testAddInventory_Success() {
        // Given
        ProductInventoryAddRequest request = ProductInventoryAddRequest.builder()
                .merchantId(merchantId)
                .sku(sku)
                .quantity(50)
                .build();

        when(productInventoryMapper.selectByMerchantIdAndSku(merchantId, sku))
                .thenReturn(inventory);
        when(productInventoryMapper.insert(any(ProductInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProductInventory result = merchantService.addInventory(request);

        // Then
        assertNotNull(result);
        assertEquals(150, result.getQuantity());
        verify(productInventoryMapper).selectByMerchantIdAndSku(merchantId, sku);
        verify(productInventoryMapper).updateById(any(ProductInventory.class));
    }

    @Test
    void testAddInventory_NotFound() {
        // Given
        ProductInventoryAddRequest request = ProductInventoryAddRequest.builder()
                .merchantId(merchantId)
                .sku(sku)
                .quantity(50)
                .build();

        when(productInventoryMapper.selectByMerchantIdAndSku(merchantId, sku))
                .thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> merchantService.addInventory(request));
        verify(productInventoryMapper).selectByMerchantIdAndSku(merchantId, sku);
        verify(productInventoryMapper, never()).updateById(any(ProductInventory.class));
    }

    @Test
    void testGetProductInventory_Success() {
        // Given
        when(productInventoryMapper.selectByMerchantIdAndSku(merchantId, sku))
                .thenReturn(inventory);

        // When
        ProductInventory result = merchantService.getProductInventory(merchantId, sku);

        // Then
        assertNotNull(result);
        assertEquals(merchantId, result.getMerchantId());
        assertEquals(sku, result.getSku());
        verify(productInventoryMapper).selectByMerchantIdAndSku(merchantId, sku);
    }

    @Test
    void testGetProductInventory_NotFound() {
        // Given
        when(productInventoryMapper.selectByMerchantIdAndSku(merchantId, sku))
                .thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> merchantService.getProductInventory(merchantId, sku));
        verify(productInventoryMapper).selectByMerchantIdAndSku(merchantId, sku);
    }
}
