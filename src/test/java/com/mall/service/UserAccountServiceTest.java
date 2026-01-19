package com.mall.service;

import com.mall.domain.user.UserAccount;
import com.mall.dto.UserAccountDepositRequest;
import com.mall.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

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
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private UserAccountService userAccountService;

    private Long userId;
    private UserAccount existingAccount;

    @BeforeEach
    void setUp() {
        userId = 1001L;
        existingAccount = UserAccount.builder()
                .id(1L)
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .currency("CNY")
                .build();
    }

    @Test
    void testDeposit_ExistingAccount() {
        // Given
        UserAccountDepositRequest request = UserAccountDepositRequest.builder()
                .userId(userId)
                .amount(new BigDecimal("50.00"))
                .currency("CNY")
                .build();

        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.of(existingAccount));
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserAccount result = userAccountService.deposit(request);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.getBalance());
        verify(userAccountRepository).findByUserId(userId);
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void testDeposit_NewAccount() {
        // Given
        UserAccountDepositRequest request = UserAccountDepositRequest.builder()
                .userId(9999L)
                .amount(new BigDecimal("100.00"))
                .currency("CNY")
                .build();

        when(userAccountRepository.findByUserId(9999L)).thenReturn(Optional.empty());
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserAccount result = userAccountService.deposit(request);

        // Then
        assertNotNull(result);
        assertEquals(9999L, result.getUserId());
        assertEquals(new BigDecimal("100.00"), result.getBalance());
        verify(userAccountRepository).findByUserId(9999L);
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void testGetUserAccount_Success() {
        // Given
        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.of(existingAccount));

        // When
        UserAccount result = userAccountService.getUserAccount(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(userAccountRepository).findByUserId(userId);
    }

    @Test
    void testGetUserAccount_NotFound() {
        // Given
        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userAccountService.getUserAccount(userId));
        verify(userAccountRepository).findByUserId(userId);
    }
}
