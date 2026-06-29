package com.sky.service.impl;

import com.sky.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 店铺相关业务层接口实现类
 */
@Service
public class ShopServiceImpl implements ShopService
{
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺状态
     *
     * @param status
     */
    @Override
    public void setStatus(Integer status)
    {
        redisTemplate.opsForValue().set("SHOP_STATUS", status);
    }

    /**
     * 获取店铺状态
     * @return
     */
    @Override
    public Integer getStatus()
    {
        return (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
    }
}
