package com.java.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.reggie.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author JianXin
 * @Date 2022/12/16 14:53
 * @Github https://github.com/JackyST0
 */

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
