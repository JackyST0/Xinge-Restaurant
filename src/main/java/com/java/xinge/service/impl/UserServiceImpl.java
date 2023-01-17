package com.java.xinge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.xinge.entity.User;
import com.java.xinge.mapper.UserMapper;
import com.java.xinge.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @Author JianXin
 * @Date 2022/12/16 8:59
 * @Github https://github.com/JackyST0
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
