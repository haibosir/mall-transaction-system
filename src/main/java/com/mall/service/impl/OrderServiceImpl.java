package com.mall.service.impl;

import com.mall.domain.merchant.MerchantAccount;
import com.mall.domain.merchant.ProductInventory;
import com.mall.domain.transaction.Order;
import com.mall.domain.user.UserAccount;
import com.mall.dto.OrderCreateRequest;
import com.mall.mapper.*;
import com.mall.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 订单服务实现类
 * 负责处理订单创建和支付流程
 * 优化方案：使用Redis + Lua脚本预扣库存，异步更新数据库
 *
 * @author mall
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final UserAccountMapper userAccountMapper;
    private final MerchantAccountMapper merchantAccountMapper;
    private final ProductInventoryMapper productInventoryMapper;
    private final RedisInventoryService redisInventoryService;
    private final InventoryAsyncService inventoryAsyncService;
    private final RedisAccountService redisAccountService;
    private final AccountAsyncService accountAsyncService;

    /**
     * 创建订单并完成支付
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderCreateRequest request) {
        log.info("创建订单请求：userId={}, merchantId={}, sku={}, quantity={}",
                request.getUserId(), request.getMerchantId(), request.getSku(), request.getQuantity());

        // 1. 从数据库查询商品信息（不加锁，只读操作）
        ProductInventory inventory = productInventoryMapper
                .selectByMerchantIdAndSku(request.getMerchantId(), request.getSku());
        if (inventory == null) {
            throw new IllegalArgumentException(
                    "商品不存在：merchantId=" + request.getMerchantId() + ", sku=" + request.getSku());
        }

        // 2. 使用Redis + Lua脚本原子性扣减库存（提高并发性能）
        Long remainingInventory = redisInventoryService.decreaseInventory(
                request.getMerchantId(), request.getSku(), request.getQuantity());
        if (remainingInventory == null || remainingInventory < 0) {
            throw new IllegalArgumentException(
                    "库存不足：merchantId=" + request.getMerchantId() + ", sku=" + request.getSku() + ", quantity=" + request.getQuantity());
        }

        boolean redisInventoryDeducted = true;
        boolean redisAccountTransferred = false;
        try {
            // 3. 计算订单总金额
            BigDecimal totalAmount = inventory.calculateTotalPrice(request.getQuantity());

            // 4. 确保用户账户和商家账户在Redis中存在
            ensureAccountInRedis(request.getUserId(), request.getMerchantId());

            // 5. 使用Redis + Lua脚本原子性转账（扣减用户余额并增加商家余额）
            try {
                redisAccountService.transferAmount(request.getUserId(), request.getMerchantId(), totalAmount);
                redisAccountTransferred = true;
                log.info("Redis账户转账成功：userId={}, merchantId={}, amount={}",
                        request.getUserId(), request.getMerchantId(), totalAmount);
            } catch (IllegalArgumentException e) {
                // 账户不存在或余额不足，尝试初始化账户
                log.warn("Redis转账失败，尝试初始化账户：{}", e.getMessage());
                ensureAccountInRedis(request.getUserId(), request.getMerchantId());
                redisAccountService.transferAmount(request.getUserId(), request.getMerchantId(), totalAmount);
                redisAccountTransferred = true;
            }

            // 6. 创建订单
            Order order = Order.builder()
                    .userId(request.getUserId())
                    .merchantId(request.getMerchantId())
                    .sku(request.getSku())
                    .productName(inventory.getProductName())
                    .unitPrice(inventory.getPrice())
                    .quantity(request.getQuantity())
                    .totalAmount(totalAmount)
                    .currency(inventory.getCurrency())
                    .status(Order.OrderStatus.PENDING)
                    .version(0L)
                    .build();
            order.initDefaults();

            // 7. 标记订单为已支付（Redis转账已成功）
            order.markAsPaid();
            log.info("订单支付成功：orderNo={}, totalAmount={}", order.getOrderNo(), totalAmount);

            // 8. 保存订单
            orderMapper.insert(order);
            log.info("订单创建成功：orderNo={}, status={}", order.getOrderNo(), order.getStatus());

            // 9. 异步更新数据库库存（不阻塞主流程，提高并发性能）
            inventoryAsyncService.decreaseDatabaseInventory(
                    request.getMerchantId(), request.getSku(), request.getQuantity());

            // 10. 异步更新数据库账户余额（不阻塞主流程，提高并发性能）
            accountAsyncService.updateDatabaseAccountBalance(
                    request.getUserId(), request.getMerchantId(), totalAmount);

            return order;
        } catch (Exception e) {
            // 如果Redis库存已扣减但后续流程失败，需要回滚Redis库存
            if (redisInventoryDeducted) {
                try {
                    redisInventoryService.increaseInventory(
                            request.getMerchantId(), request.getSku(), request.getQuantity());
                    log.info("已回滚Redis库存：merchantId={}, sku={}, quantity={}",
                            request.getMerchantId(), request.getSku(), request.getQuantity());
                } catch (Exception ex) {
                    log.error("回滚Redis库存失败：merchantId={}, sku={}, quantity={}, error={}",
                            request.getMerchantId(), request.getSku(), request.getQuantity(), ex.getMessage(), ex);
                }
            }

            // 如果Redis账户已转账但后续流程失败，需要回滚Redis账户
            if (redisAccountTransferred) {
                try {
                    BigDecimal totalAmount = inventory.calculateTotalPrice(request.getQuantity());
                    redisAccountService.rollbackTransfer(
                            request.getUserId(), request.getMerchantId(), totalAmount);
                    log.info("已回滚Redis账户：userId={}, merchantId={}, amount={}",
                            request.getUserId(), request.getMerchantId(), totalAmount);
                } catch (Exception ex) {
                    log.error("回滚Redis账户失败：userId={}, merchantId={}, error={}",
                            request.getUserId(), request.getMerchantId(), ex.getMessage(), ex);
                }
            }
            throw e;
        }
    }

    /**
     * 根据订单号查询订单
     */
    @Override
    public Order getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在：orderNo=" + orderNo);
        }
        return order;
    }

    /**
     * 确保账户在Redis中存在，如果不存在则从数据库加载并初始化
     */
    private void ensureAccountInRedis(Long userId, Long merchantId) {
        // 检查用户账户
        BigDecimal userBalance = redisAccountService.getUserBalance(userId);
        if (userBalance == null) {
            UserAccount userAccount = userAccountMapper.selectByUserId(userId);
            if (userAccount == null) {
                throw new IllegalArgumentException("用户账户不存在：userId=" + userId);
            }
            redisAccountService.initUserAccount(userId, userAccount.getBalance());
        }

        // 检查商家账户
        BigDecimal merchantBalance = redisAccountService.getMerchantBalance(merchantId);
        if (merchantBalance == null) {
            MerchantAccount merchantAccount = merchantAccountMapper.selectByMerchantId(merchantId);
            if (merchantAccount == null) {
                log.info("商家账户不存在，创建新账户：merchantId={}", merchantId);
                merchantAccount = MerchantAccount.builder()
                        .merchantId(merchantId)
                        .balance(BigDecimal.ZERO)
                        .version(0L)
                        .build();
                merchantAccount.initDefaults();
                merchantAccountMapper.insert(merchantAccount);
            }
            redisAccountService.initMerchantAccount(merchantId, merchantAccount.getBalance());
        }
    }
}
