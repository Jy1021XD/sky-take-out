package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 套餐业务实现类
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService
{
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐
     *
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithDishes(SetmealDTO setmealDTO)
    {
        // 插入套餐信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.insert(setmeal);
        // 插入套餐菜品关系信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 获取新增套餐主键
        Long setmealId = setmeal.getId();
        if (setmealDishes != null && !setmealDishes.isEmpty())
        {
            setmealDishes.forEach(setmealDish ->
            {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        // 开启分页
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        // 查询数据
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        // 返回数据
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return;
        }
        // 判断是否有在售套餐
        Long count = setmealMapper.countStatusEnableByIds(ids);
        if (count != 0)
        {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        // 删除套餐数据
        setmealMapper.deleteBatch(ids);
        // 删除套餐菜品关系数据
        setmealDishMapper.deleteBatchBySetmealIds(ids);
    }

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id)
    {
        SetmealVO setmealVO = new SetmealVO();
        // 查询套餐基本信息
        Setmeal setmeal = setmealMapper.selectById(id);
        BeanUtils.copyProperties(setmeal, setmealVO);
        // 查询套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        // 封装返回数据
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 更改套餐状态
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, Long id)
    {
        // 如果操作位起售，需要检查套餐内是否有停售菜品
        if(status == StatusConstant.ENABLE)
        {
            // 查询套餐包含的菜品中停售的数量
            Long count = setmealDishMapper.countDishStatusDisableBySetmealId(id);
            if(count > 0 && count != null)
            {
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithDishes(SetmealDTO setmealDTO)
    {
        Long setmealId = setmealDTO.getId();
        // 套餐id不能为空
        if (setmealId == null)
        {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_NOT_FOUND);
        }

        // 查询套餐是否存在
        Setmeal existingSetmeal = setmealMapper.selectById(setmealId);
        if (existingSetmeal == null)
        {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_NOT_FOUND);
        }

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 修改套餐信息
        setmealMapper.update(setmeal);

        // 处理套餐菜品关系：setmealDishes为null表示不修改，空列表表示清空，非空列表表示替换
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null)
        {
            setmealDishMapper.deleteBySetmealId(setmealId);
            if (!setmealDishes.isEmpty())
            {
                setmealDishes.forEach(dish -> dish.setSetmealId(setmealId));
                setmealDishMapper.insertBatch(setmealDishes);
            }
        }
    }
}
