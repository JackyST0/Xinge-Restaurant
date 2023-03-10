package com.java.xinge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.java.xinge.dto.OrdersDto;
import com.java.xinge.entity.Orders;

import java.util.Map;

/**
 * @Author JianXin
 * @Date 2022/12/17 10:59
 * @Github https://github.com/JackyST0
 */
public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    /**
     * 用户端展示自己的订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    public Page<OrdersDto> page(int page, int pageSize);

    /**
     * 客户端点击再来一单
     * @param map
     */
    public void againSubmit(Map<String,String> map);
}
