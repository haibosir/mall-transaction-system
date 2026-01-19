package com.mall.mapper;

import com.mall.domain.merchant.MerchantAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商家账户Mapper
 *
 * @author mall
 */
@Mapper
public interface MerchantAccountMapper {

    /**
     * 插入商家账户
     */
    int insert(MerchantAccount merchantAccount);

    /**
     * 根据ID更新商家账户
     */
    int updateById(MerchantAccount merchantAccount);

    /**
     * 根据ID查找商家账户
     */
    MerchantAccount selectById(Long id);

    /**
     * 根据商家ID查找账户
     */
    MerchantAccount selectByMerchantId(Long merchantId);

    /**
     * 根据商家ID查找账户（加悲观锁 FOR UPDATE）
     */
    MerchantAccount selectByMerchantIdForUpdate(@Param("merchantId") Long merchantId);

    /**
     * 查找所有商家账户
     */
    List<MerchantAccount> selectAll();
}
