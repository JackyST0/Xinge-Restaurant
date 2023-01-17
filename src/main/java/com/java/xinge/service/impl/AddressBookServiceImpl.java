package com.java.xinge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.xinge.entity.AddressBook;
import com.java.xinge.mapper.AddressBookMapper;
import com.java.xinge.service.AddressBookService;
import org.springframework.stereotype.Service;

/**
 * @Author JianXin
 * @Date 2022/12/16 14:54
 * @Github https://github.com/JackyST0
 */

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
