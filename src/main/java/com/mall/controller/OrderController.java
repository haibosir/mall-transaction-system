package com.mall.controller;

import com.mall.dto.ApiResponse;
import com.mall.dto.OrderCreateRequest;
import com.mall.domain.transaction.Order;
import com.mall.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 * 提供订单相关的REST API
 *
 * @author mall
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * POST /api/orders
     *
     * @param request 订单创建请求
     * @return 创建的订单
     */
    @PostMapping
    public ApiResponse<Order> createOrder(@Validated @RequestBody OrderCreateRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return ApiResponse.success("订单创建成功", order);
        } catch (IllegalArgumentException e) {
            log.warn("订单创建失败：{}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("订单创建异常", e);
            return ApiResponse.fail("订单创建失败：" + e.getMessage());
        }
    }

    /**
     * 查询订单
     * GET /api/orders/{orderNo}
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<Order> getOrder(@PathVariable String orderNo) {
        try {
            Order order = orderService.getOrderByOrderNo(orderNo);
            return ApiResponse.success(order);
        } catch (IllegalArgumentException e) {
            log.warn("查询订单失败：{}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("查询订单异常", e);
            return ApiResponse.fail("查询订单失败：" + e.getMessage());
        }
    }
}
