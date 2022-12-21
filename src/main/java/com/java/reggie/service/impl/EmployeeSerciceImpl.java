package com.java.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.reggie.entity.Employee;
import com.java.reggie.mapper.EmployeeMapper;
import com.java.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @Author JianXin
 * @Date 2022/12/8 11:27
 * @Github https://github.com/JackyST0
 */

@Service
public class EmployeeSerciceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

}
