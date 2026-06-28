package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 菜品管理实现类
 */
@Slf4j
@Service
public class DishServiceImpl implements DishService
{
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO)
    {
        // 新增菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 保存菜品
        dishMapper.insert(dish);

        // 获取主键
        Long dishId = dish.getId();
        // 新增菜品口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty())
        {
            // 补齐dishId，并批量插入数据
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }


    /**
     * 分页查询菜品
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO)
    {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> dishPage = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(dishPage.getTotal(), dishPage.getResult());
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids)
    {
        if (ids == null || ids.isEmpty()) return;
        // 查看菜品是否有在售
        for (Long id : ids)
        {
            Dish dish = dishMapper.selectById(id);
            if (dish == null)
            {
                throw new DeletionNotAllowedException(MessageConstant.DISH_NOT_FOUND);
            }
            if (Objects.equals(dish.getStatus(), StatusConstant.ENABLE))
            {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 查看菜品是否有套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && !setmealIds.isEmpty())
        {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 删除菜品
        dishMapper.deleteBatchByDishIds(ids);
        // 删除菜品对应口味
        dishFlavorMapper.deleteBatchByDishIds(ids);
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id)
    {
        DishVO dishVO = new DishVO();
        // 查询菜品
        Dish dish = dishMapper.selectById(id);
        BeanUtils.copyProperties(dish, dishVO);
        // 查询菜品口味
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);

        // 封装返回数据
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO)
    {
        Long dishId = dishDTO.getId();
        // 菜品id不能为空
        if (dishId == null)
        {
            throw new DeletionNotAllowedException(MessageConstant.DISH_NOT_FOUND);
        }

        // 查询菜品是否存在
        Dish existingDish = dishMapper.selectById(dishId);
        if (existingDish == null)
        {
            throw new DeletionNotAllowedException(MessageConstant.DISH_NOT_FOUND);
        }

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 修改菜品信息
        dishMapper.update(dish);

        // 处理口味：flavors为null表示不修改口味，空列表表示清空口味，非空列表表示替换口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null)
        {
            dishFlavorMapper.deleteByDishId(dishId);
            if (!flavors.isEmpty())
            {
                flavors.forEach(flavor -> flavor.setDishId(dishId));
                dishFlavorMapper.insertBatch(flavors);
            }
        }
    }

    /**
     * 根据菜品分类id查询菜品列表
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId)
    {
        // 构建Dish对象
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE); // 必须是在售菜品

        return dishMapper.selectList(dish);
    }

    /**
     * 修改菜品状态
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, Long id)
    {
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.update(dish);
    }
}
