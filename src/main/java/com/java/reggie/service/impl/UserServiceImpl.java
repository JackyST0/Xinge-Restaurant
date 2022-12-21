package com.java.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.reggie.entity.User;
import com.java.reggie.mapper.UserMapper;
import com.java.reggie.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @Author JianXin
 * @Date 2022/12/16 8:59
 * @Github https://github.com/JackyST0
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
