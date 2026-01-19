package com.mall.service;

import com.mall.domain.user.UserAccount;
import com.mall.dto.UserAccountDepositRequest;

/**
 * 用户账户服务接口
 *
 * @author mall
 */
public interface UserAccountService {

    /**
     * 用户账户充值
     *
     * @param request 充值请求
     * @return 充值后的账户信息
     */
    UserAccount deposit(UserAccountDepositRequest request);

    /**
     * 获取用户账户信息
     *
     * @param userId 用户ID
     * @return 用户账户
     */
    UserAccount getUserAccount(Long userId);
}
