package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        // 构造redis中的key
        String key = "dish_" + categoryId;

        // 查询redis中是否存在菜品数据
        List<DishVO> redisList = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (redisList != null && redisList.size() > 0) {
            // 如果存在，直接返回，不需要进行IO操作
            log.info("使用 redis 缓存");
            return Result.success(redisList);
        }
        // 如果不存在，则查询数据库，并将数据库中的内容放置到redis中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品


        List<DishVO> list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, list);


        return Result.success(list);
    }

}
