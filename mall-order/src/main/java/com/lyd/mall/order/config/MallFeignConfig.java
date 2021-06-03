package com.lyd.mall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author Liuyunda
 * @Date 2021/6/2 22:25
 * @Email man021436@163.com
 * @Description: TODO
 */
@Configuration
public class MallFeignConfig {
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 1.RequestContextHolder拿到刚进来的这个请求的数据
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                // 老请求
                HttpServletRequest request = requestAttributes.getRequest();
                if (request != null) {
                    // 2.同步请求头数据，Cookie
                    String cookie = request.getHeader("Cookie");
                    // 给新请求同步了老请求的cookie
                    requestTemplate.header("Cookie",cookie);
                }
            }
        };
    }
}
