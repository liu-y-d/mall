package com.lyd.mall.order.web;

import com.lyd.mall.order.service.OrderService;
import com.lyd.mall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

/**
 * @Author Liuyunda
 * @Date 2021/6/2 20:30
 * @Email man021436@163.com
 * @Description: TODO
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;
    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest request) throws ExecutionException, InterruptedException {
        // 展示订单确认页信息
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirm",confirmVo);
        return "confirm";
    }
}
