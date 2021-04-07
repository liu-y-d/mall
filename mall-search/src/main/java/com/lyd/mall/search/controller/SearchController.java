package com.lyd.mall.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author Liuyunda
 * @Date 2021/4/7 22:39
 * @Email man021436@163.com
 * @Description: TODO
 */
@Controller
public class SearchController {

    @GetMapping("/list.html")
    public String listPage(){
        return "list";
    }
}
