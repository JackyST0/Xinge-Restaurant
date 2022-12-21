package com.java.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.reggie.entity.Category;

/**
 * @Author JianXin
 * @Date 2022/12/12 14:30
 * @Github https://github.com/JackyST0
 */
public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
