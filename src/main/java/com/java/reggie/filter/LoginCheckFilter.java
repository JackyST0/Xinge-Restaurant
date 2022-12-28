package com.java.reggie.filter;

/**
 * @Author JianXin
 * @Date 2022/12/8 16:14
 * @Github https://github.com/JackyST0
 */

import com.alibaba.fastjson.JSON;
import com.java.reggie.common.BaseContext;
import com.java.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.AnnotatedArrayType;

/**
 * 检查用户是否已经完成登录
 */
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        //1.获取本次请求的URI
        String requestURI=request.getRequestURI();

        log.info("拦截到请求：{}",requestURI);

        //定义不需要处理的请求路径
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg", //移动端发送短信
                "/user/login", //移动端登录
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };

        //2.判断本次请求是否需要处理
        boolean check =check(urls,requestURI);

        //3.如果不需要处理，则直接放行
        if (check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //4-1.判断管理端登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee")!=null){
            log.info("用户已登录，用户id为{}",request.getSession().getAttribute("employee"));

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        //4-2.判断移动端登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user")!=null){
            log.info("用户已登录，用户id为{}",request.getSession().getAttribute("user"));

            Long UserId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(UserId);

            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登录");
        //5.如果未登陆则返回未登录状态，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match=PATH_MATCHER.match(url,requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
