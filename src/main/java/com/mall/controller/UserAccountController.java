package com.mall.controller;

import com.mall.dto.ApiResponse;
import com.mall.dto.UserAccountDepositRequest;
import com.mall.domain.user.UserAccount;
import com.mall.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户账户控制器
 * 提供用户账户相关的REST API
 *
 * @author mall
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserAccountController {

    private final UserAccountService userAccountService;

    /**
     * 用户账户充值
     * POST /api/users/{userId}/accounts/deposit
     *
     * @param userId  用户ID
     * @param request 充值请求
     * @return 充值结果
     */
    @PostMapping("/{userId}/accounts/deposit")
    public ApiResponse<UserAccount> deposit(@PathVariable Long userId,
                                            @Validated @RequestBody UserAccountDepositRequest request) {
        try {
            // 确保路径参数和请求体中的userId一致
            request.setUserId(userId);
            UserAccount account = userAccountService.deposit(request);
            return ApiResponse.success("充值成功", account);
        } catch (IllegalArgumentException e) {
            log.warn("充值失败：{}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("充值异常", e);
            return ApiResponse.fail("充值失败：" + e.getMessage());
        }
    }

    /**
     * 查询用户账户信息
     * GET /api/users/{userId}/accounts
     *
     * @param userId 用户ID
     * @return 用户账户信息
     */
    @GetMapping("/{userId}/accounts")
    public ApiResponse<UserAccount> getAccount(@PathVariable Long userId) {
        try {
            UserAccount account = userAccountService.getUserAccount(userId);
            return ApiResponse.success(account);
        } catch (IllegalArgumentException e) {
            log.warn("查询账户失败：{}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("查询账户异常", e);
            return ApiResponse.fail("查询账户失败：" + e.getMessage());
        }
    }
}
