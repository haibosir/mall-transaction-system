package com.mall.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * Redis库存服务
 * 使用Lua脚本保证库存扣减的原子性
 *
 * @author mall
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisInventoryService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Lua脚本：扣减库存
     * KEYS[1]: 库存key (inventory:{merchantId}:{sku})
     * ARGV[1]: 扣减数量
     * 返回: 扣减后的库存数量，如果库存不足返回-1
     */
    private static final String DECREASE_INVENTORY_SCRIPT =
            "local current = redis.call('get', KEYS[1])\n" +
            "if current == false then\n" +
            "    return -1\n" +
            "end\n" +
            "local num = tonumber(current)\n" +
            "local decrease = tonumber(ARGV[1])\n" +
            "if num < decrease then\n" +
            "    return -1\n" +
            "end\n" +
            "local result = num - decrease\n" +
            "redis.call('set', KEYS[1], result)\n" +
            "return result";

    private DefaultRedisScript<Long> decreaseInventoryScript;

    @PostConstruct
    public void init() {
        decreaseInventoryScript = new DefaultRedisScript<>();
        decreaseInventoryScript.setScriptText(DECREASE_INVENTORY_SCRIPT);
        decreaseInventoryScript.setResultType(Long.class);
    }

    /**
     * 初始化商品库存到Redis
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @param quantity   库存数量
     */
    public void initInventory(Long merchantId, String sku, Integer quantity) {
        String key = getInventoryKey(merchantId, sku);
        redisTemplate.opsForValue().set(key, quantity);
        log.info("初始化Redis库存：key={}, quantity={}", key, quantity);
    }

    /**
     * 扣减库存（原子操作）
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @param quantity   扣减数量
     * @return 扣减后的库存数量，如果库存不足返回-1
     */
    public Long decreaseInventory(Long merchantId, String sku, Integer quantity) {
        String key = getInventoryKey(merchantId, sku);
        List<String> keys = Collections.singletonList(key);
        Long result = redisTemplate.execute(decreaseInventoryScript, keys, quantity);
        log.info("Redis库存扣减：key={}, quantity={}, result={}", key, quantity, result);
        return result;
    }

    /**
     * 增加库存
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @param quantity   增加数量
     */
    public void increaseInventory(Long merchantId, String sku, Integer quantity) {
        String key = getInventoryKey(merchantId, sku);
        redisTemplate.opsForValue().increment(key, quantity);
        log.info("Redis库存增加：key={}, quantity={}", key, quantity);
    }

    /**
     * 获取库存
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @return 库存数量，如果不存在返回null
     */
    public Integer getInventory(Long merchantId, String sku) {
        String key = getInventoryKey(merchantId, sku);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return Integer.valueOf(value.toString());
    }

    /**
     * 检查库存是否充足
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @param quantity   需要检查的数量
     * @return true if 库存充足
     */
    public boolean hasEnoughInventory(Long merchantId, String sku, Integer quantity) {
        Integer current = getInventory(merchantId, sku);
        return current != null && current >= quantity;
    }

    /**
     * 设置库存
     *
     * @param merchantId 商家ID
     * @param sku        商品SKU
     * @param quantity   库存数量
     */
    public void setInventory(Long merchantId, String sku, Integer quantity) {
        String key = getInventoryKey(merchantId, sku);
        redisTemplate.opsForValue().set(key, quantity);
    }

    /**
     * 获取库存key
     */
    private String getInventoryKey(Long merchantId, String sku) {
        return "inventory:" + merchantId + ":" + sku;
    }
}
