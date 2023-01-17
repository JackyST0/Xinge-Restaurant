package com.java.xinge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.xinge.entity.ShoppingCart;

/**
 * @Author JianXin
 * @Date 2022/12/16 16:50
 * @Github https://github.com/JackyST0
 */
public interface ShoppingCartService extends IService<ShoppingCart> {

    //清空购物车
    public void clean();
}
