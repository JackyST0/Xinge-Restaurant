package com.java.xinge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author JianXin
 * @Date 2022/12/8 10:44
 * @Github https://github.com/JackyST0
 */

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
@EnableCaching //开启Spring Cache注解方式是缓存功能
public class XingeApplication {
    public static void main(String[] args) {
        SpringApplication.run(XingeApplication.class,args);
        log.info("项目启动成功...");
    }
}

