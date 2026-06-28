package com.sky.service;

import com.sky.dto.SetmealDTO;

/**
 * 套餐管理业务层接口
 */
public interface SetmealService
{
    /**
     * 添加套餐
     * @param setmealDTO
     */
    void saveWithDishes(SetmealDTO setmealDTO);
}
