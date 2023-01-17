package com.java.xinge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.xinge.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author JianXin
 * @Date 2022/12/16 8:58
 * @Github https://github.com/JackyST0
 */

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
