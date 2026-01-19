package com.mall.service;

import com.mall.domain.transaction.Order;
import com.mall.dto.OrderCreateRequest;

/**
 * 订单服务接口
 *
 * @author mall
 */
public interface OrderService {

    /**
     * 创建订单并完成支付
     *
     * @param request 订单创建请求
     * @return 创建的订单
     */
    Order createOrder(OrderCreateRequest request);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单
     */
    Order getOrderByOrderNo(String orderNo);
}
