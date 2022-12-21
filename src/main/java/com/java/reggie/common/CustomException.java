package com.java.reggie.common;

/**
 * @Author JianXin
 * @Date 2022/12/12 17:08
 * @Github https://github.com/JackyST0
 */

/**
 * 自定义业务异常类
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
