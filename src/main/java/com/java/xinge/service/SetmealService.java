package com.java.xinge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.xinge.dto.SetmealDto;
import com.java.xinge.entity.Setmeal;

import java.util.List;

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
