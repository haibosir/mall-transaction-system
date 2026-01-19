package com.mall.mapper;

import com.mall.domain.transaction.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单Mapper
 *
 * @author mall
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     */
    int insert(Order order);

    /**
     * 根据ID更新订单
     */
    int updateById(Order order);

    /**
     * 根据ID查找订单
     */
    Order selectById(Long id);

    /**
     * 根据订单号查找订单
     */
    Order selectByOrderNo(String orderNo);

    /**
     * 根据用户ID查找订单列表
     */
    List<Order> selectByUserId(Long userId);

    /**
     * 根据商家ID查找订单列表
     */
    List<Order> selectByMerchantId(Long merchantId);

    /**
     * 查询指定商家在指定时间范围内的已支付订单总金额
     */
    BigDecimal sumPaidOrderAmountByMerchantAndTimeRange(
            @Param("merchantId") Long merchantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查找所有订单
     */
    List<Order> selectAll();
}
