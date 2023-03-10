package com.java.xinge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.xinge.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author JianXin
 * @Date 2022/12/17 10:57
 * @Github https://github.com/JackyST0
 */

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
