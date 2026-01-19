package com.mall.mapper;

import com.mall.domain.user.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户账户Mapper
 *
 * @author mall
 */
@Mapper
public interface UserAccountMapper {

    /**
     * 插入用户账户
     */
    int insert(UserAccount userAccount);

    /**
     * 根据ID更新用户账户
     */
    int updateById(UserAccount userAccount);

    /**
     * 根据ID查找用户账户
     */
    UserAccount selectById(Long id);

    /**
     * 根据用户ID查找账户
     */
    UserAccount selectByUserId(Long userId);

    /**
     * 根据用户ID查找账户（加悲观锁 FOR UPDATE）
     */
    UserAccount selectByUserIdForUpdate(@Param("userId") Long userId);

    /**
     * 查找所有用户账户
     */
    List<UserAccount> selectAll();
}
