package com.sky.service;

import com.sky.dto.DishDTO;

/**
 * 菜品管理接口
 */
public interface DishService
{
    /**
     * 新增菜品
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);
}
