package com.lyd.mallcart.controller;

import com.lyd.mallcart.interceptor.CartInterceptor;
import com.lyd.mallcart.vo.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author Liuyunda
 * @Date 2021/5/25 20:38
 * @Email man021436@163.com
 * @Description: TODO
 */
@Controller
public class CartController {
    @GetMapping("/cart.html")
    public String cartListPage(){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        System.out.println(userInfoTo.toString());
        return "cartList";
    }
}
