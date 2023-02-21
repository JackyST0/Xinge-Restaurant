package com.java.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author JianXin
 * @Date 2022/12/12 15:43
 * @Github https://github.com/JackyST0
 */

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}