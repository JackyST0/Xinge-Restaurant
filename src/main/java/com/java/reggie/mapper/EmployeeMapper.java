package com.java.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author JianXin
 * @Date 2022/12/8 11:24
 * @Github https://github.com/JackyST0
 */

 @Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
