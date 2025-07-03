package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
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

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增菜品
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDishes(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        // 新增套餐
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        // 关联套餐中的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    public void updateWithDishes(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 修改套餐信息
        setmealMapper.update(setmeal);
        // 删除套餐原有菜品
        setmealMapper.deleteBySetmealId(setmealDTO.getId());
        // 添加菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getSetmealById(long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        // 封装到VO
        SetmealVO setmealVO = SetmealVO.builder()
                .setmealDishes(setmealDishes)
                .build();
        BeanUtils.copyProperties(setmeal, setmealVO);

        return setmealVO;
    }

    /**
     * 更改套餐状态
     * @param status
     * @param id
     */
    @Override
    public void startAndStop(Integer status, long id) {
        // 判断套餐中是否存在未起售菜品
        List<SetmealDish> dishes = setmealDishMapper.getBySetmealId(id);
        if (dishes != null && !dishes.isEmpty() && StatusConstant.ENABLE.equals(status)) {
            dishes.forEach(setmealDish -> {
               Dish dish = dishMapper.getById(setmealDish.getDishId());
               if (StatusConstant.DISABLE == dish.getStatus()) {
                   throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
               }
            });
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);

        }

    /**
     * 批量删除套餐
     * @param setmealIds
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> setmealIds) {
        if (setmealIds != null && !setmealIds.isEmpty()) {
            setmealIds.forEach(setmealId -> {
                // 起售中的套餐不能删除
                Setmeal setmeal = setmealMapper.getById(setmealId);
                if (StatusConstant.ENABLE == setmeal.getStatus()) {
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
                } else if (StatusConstant.DISABLE == setmeal.getStatus()) {
                    // 删除setmeal_dishes
                    setmealMapper.deleteBySetmealId(setmealId);
                    // 删除setmeal
                    setmealMapper.delete(setmealId);
                }
            });
        }
    }
}
