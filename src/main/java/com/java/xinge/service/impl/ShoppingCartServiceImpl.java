package com.java.xinge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.xinge.common.BaseContext;
import com.java.xinge.entity.ShoppingCart;
import com.java.xinge.mapper.ShoppingCartMapper;
import com.java.xinge.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author JianXin
 * @Date 2022/12/16 16:50
 * @Github https://github.com/JackyST0
 */

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    public void clean() {
        //SQL:delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);
    }
}
