package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品和口味
     * @param dishDTO
     */
    public void saveWithFlavour(DishDTO dishDTO);


    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据ID查询菜品和相关口味
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 修改菜品和口味信息
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 根据分类id获取分类下的所有菜品
     * @param id
     * @return
     */
    List<Dish> getByCID(Long id);

    /**
     * 更改菜品起售状态
     * @param status
     * @param id
     */
    void startAndStop(Integer status, long id);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
