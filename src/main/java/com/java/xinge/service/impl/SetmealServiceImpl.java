package com.java.xinge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.xinge.common.CustomException;
import com.java.xinge.dto.SetmealDto;
import com.java.xinge.entity.Setmeal;
import com.java.xinge.entity.SetmealDish;
import com.java.xinge.mapper.SetmealMapper;
import com.java.xinge.service.SetmealDishService;
import com.java.xinge.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author JianXin
 * @Date 2022/12/12 15:48
 * @Github https://github.com/JackyST0
 */

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        //保存套餐和菜品的关联关系，操作setmeal_dish，执行insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可用删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if (count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        //删除关系表中的数据---setmeal_dish
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 根据id查询套餐信息和对应菜品的关联数据
     * @param id
     * @return
     */
    public SetmealDto getByIdWithDish(Long id) {
        //查询套餐基本信息，从setmeal表查询
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询当前菜品对应的口味信息，从setmeal_dish表查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    /**
     * 更新套餐信息，同时更新菜品的关联数据
     * @param setmealDto
     */
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新setmeal表基本信息
        this.updateById(setmealDto);

        //清理当前菜品对应口味数据---setmeal_dish表的delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());

        setmealDishService.remove(queryWrapper);

        //添加当前提交过来的口味数据---setmeal_dish表的insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }
}
