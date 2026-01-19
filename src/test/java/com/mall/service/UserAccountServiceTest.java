package com.mall.service;

import com.mall.domain.user.UserAccount;
import com.mall.dto.UserAccountDepositRequest;
import com.mall.exception.UserNotFoundException;
import com.mall.mapper.UserAccountMapper;
import com.mall.service.impl.UserAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户账户服务测试
 *
 * @author mall
 */
@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountMapper userAccountRepository;

    //    @InjectMocks
//    private UserAccountService userAccountService;
    private UserAccountServiceImpl userAccountService;

    private Long userId;
    private UserAccount existingAccount;

    @Mock
    private RedisAccountService redisAccountService;

    @BeforeEach
    void setUp() {
        userId = 1001L;
        existingAccount = UserAccount.builder()
                .id(1L)
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .currency("CNY")
                .build();
        
        // 初始化服务实现类
        userAccountService = new UserAccountServiceImpl(userAccountRepository, redisAccountService);
    }

    @Test
    void testDeposit_ExistingAccount() {
        // Given
        UserAccountDepositRequest request = UserAccountDepositRequest.builder()
                .userId(userId)
                .amount(new BigDecimal("50.00"))
                .currency("CNY")
                .build();

        when(userAccountRepository.selectByUserId(userId)).thenReturn(existingAccount);
        when(userAccountRepository.updateById(any(UserAccount.class))).thenReturn(1);

        // When
        UserAccount result = userAccountService.deposit(request);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.getBalance());
        verify(userAccountRepository).selectByUserId(userId);
        verify(userAccountRepository).updateById(any(UserAccount.class));
    }

    @Test
    void testDeposit_NewAccount() {
        // Given
        UserAccountDepositRequest request = UserAccountDepositRequest.builder()
                .userId(9999L)
                .amount(new BigDecimal("100.00"))
                .currency("CNY")
                .build();

        when(userAccountRepository.selectByUserId(9999L)).thenReturn(null);
        when(userAccountRepository.insert(any(UserAccount.class))).thenReturn(1);

        // When
        UserAccount result = userAccountService.deposit(request);

        // Then
        assertNotNull(result);
        assertEquals(9999L, result.getUserId());
        assertEquals(new BigDecimal("100.00"), result.getBalance());
        verify(userAccountRepository).selectByUserId(9999L);
        verify(userAccountRepository).insert(any(UserAccount.class));
    }

    @Test
    void testGetUserAccount_Success() {
        // Given
        when(userAccountRepository.selectByUserId(userId)).thenReturn(existingAccount);

        // When
        UserAccount result = userAccountService.getUserAccount(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(userAccountRepository).selectByUserId(userId);
    }

    @Test
    void testGetUserAccount_NotFound() {
        // Given
        when(userAccountRepository.selectByUserId(userId)).thenReturn(null);

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userAccountService.getUserAccount(userId));
        verify(userAccountRepository).selectByUserId(userId);
    }
}
