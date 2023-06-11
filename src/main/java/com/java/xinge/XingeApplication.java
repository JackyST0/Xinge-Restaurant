package com.java.xinge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
    public static void main(String[] args) throws UnknownHostException {
//        SpringApplication.run(XingeApplication.class,args);
//        log.info("项目启动成功...");
        ConfigurableApplicationContext application = SpringApplication.run(XingeApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String username = env.getProperty("USERNAME");
        String computername = env.getProperty("COMPUTERNAME");
        String userdomain = env.getProperty("USERDOMAIN");
        String property = System.getProperty("os.name");
        log.info("{},{},{},{}",username,computername,userdomain,property);
        String path = env.getProperty("server.servlet.context-path");
        log.info("\n----------------------------------------------------------\n\t" +
                "Application Xinge-Restaurant is running! Access URLs:\n\t" +
//                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
//                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "Knife4j文档: \thttp://" + ip + ":" + port + "/doc.html\n\t" +
                "前台: \thttp://" + ip + ":" + port + "/front/page/login.html\n\t" +
                "后台: \thttp://" + ip + ":" + port + "/backend/page/login/login.html\n\t" +
                "----------------------------------------------------------");
    }
}

