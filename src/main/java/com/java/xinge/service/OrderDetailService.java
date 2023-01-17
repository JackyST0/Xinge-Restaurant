package com.java.xinge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.xinge.entity.OrderDetail;

import java.util.List;

/**
 * @Author JianXin
 * @Date 2022/12/17 11:00
 * @Github https://github.com/JackyST0
 */
public interface OrderDetailService extends IService<OrderDetail> {

    //抽离的一个方法，通过订单id查询订单明细，得到一个订单明细的集合
    //这里抽离出来是为了避免在stream中遍历的时候直接使用构造条件来查询导致eq叠加，从而导致后面查询的数据都是null
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId);
}
