package com.lyd.mall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author Liuyunda
 * @Date 2021/6/1 21:52
 * @Email man021436@163.com
 * @Description: TODO
 */
@Controller
public class HelloController {
    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page")String page){
        return page;
    }
}
