package com.java.xinge.controller;

/**
 * @Author JianXin
 * @Date 2022/12/13 21:37
 * @Github https://github.com/JackyST0
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.java.xinge.common.R;
import com.java.xinge.dto.DishDto;
import com.java.xinge.entity.Category;
import com.java.xinge.entity.Dish;
import com.java.xinge.entity.DishFlavor;
import com.java.xinge.service.CategoryService;
import com.java.xinge.service.DishFlavorService;
import com.java.xinge.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        //清理所有菜品的缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId();
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        //基于stream流方式处理item对象
        List<DishDto> list = records.stream().map((item) -> {
            //创建新的Dto对象
            DishDto dishDto = new DishDto();
            //把item对象的基本属性拷贝到Dto对象中
            BeanUtils.copyProperties(item,dishDto);
            //通过item对象得到分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null){
                //再通过分类对象把分类名称查询出来
                String categoryName = category.getName();
                //最后把分类名称设置给Dto对象
                dishDto.setCategoryName(categoryName);
            }
            //返回Dto
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        //修改菜品时可能会涉及到修改菜品的分类，所以这里应该清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
//        String key = "dish_" + dishDto.getCategoryId();
//        redisTemplate.delete(key);

        return R.success("修改菜品成功");
    }

    /**
     * 批量或单个停售起售菜品
     * @param status;
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> sale(@PathVariable Integer status,@RequestParam List<Long> ids){

        log.info(String.valueOf(ids));
        for (Long id : ids) {
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
            //清理所有菜品的缓存数据
//          Set keys = redisTemplate.keys("dish_*");
//          redisTemplate.delete(keys);

            //清理某个分类下面的菜品缓存数据
            String key = "dish_" + dish.getCategoryId();
            redisTemplate.delete(key);
        }
        return R.success(status == 1 ? "起售成功" : "停售成功");
    }

    /**
     * 批量或单个删除菜品以及对应菜品的口味
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        dishService.deleteWithFlavor(ids);
        return R.success("菜品删除成功");
    }

//    /**
//     * 根据条件查询对应菜品数据
//     * @param dish
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//
//        //构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
//        queryWrapper.eq(dish.getName() != null,Dish::getName,dish.getName());
//        //添加条件，查询状态为1（起售状态）的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }

    /**
     * 根据条件查询对应菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;

        //动态构造key
        String key = "dish_" + dish.getCategoryId();

        //先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null){
            //如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(dish.getName() != null,Dish::getName,dish.getName());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            //创建新的Dto对象
            DishDto dishDto = new DishDto();
            //把item对象的基本属性拷贝到Dto对象中
            BeanUtils.copyProperties(item,dishDto);
            //通过item对象得到分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null){
                //再通过分类对象把分类名称查询出来
                String categoryName = category.getName();
                //最后把分类名称设置给Dto对象
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            //返回Dto
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
