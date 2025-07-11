package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    /**
     * 批量插入多个口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);


    /**
     * 根据dish主键删除其口味
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 根据菜品ids删除口味
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);

    /**
     * 根据菜品Id查询相关口味
     * @param DishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{DishId}")
    List<DishFlavor> getByDishId(Long DishId);
}
