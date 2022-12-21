package com.java.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.reggie.dto.DishDto;
import com.java.reggie.dto.SetmealDto;
import com.java.reggie.entity.Setmeal;

import java.util.List;
import java.util.Set;

/**
 * @Author JianXin
 * @Date 2022/12/12 15:46
 * @Github https://github.com/JackyST0
 */
public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐 ，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 根据id查询套餐信息和对应菜品的关联数据
     * @param id
     * @return
     */
    public SetmealDto getByIdWithDish(Long id);

    /**
     * 更新套餐信息，同时更新菜品的关联数据
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto);
}
