package com.lyd.mall.order.web;

import com.lyd.common.exception.NoStockException;
import com.lyd.mall.order.service.OrderService;
import com.lyd.mall.order.vo.OrderConfirmVo;
import com.lyd.mall.order.vo.OrderSubmitVo;
import com.lyd.mall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    /**
     * @Description: 下单功能
     * @Param: [vo]
     * @return: java.lang.String
     * @Author: Liuyunda
     * @Date: 2021/6/6
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){

        try{
            SubmitOrderResponseVo sb = orderService.submitOrder(vo);
            if (sb.getCode() == 0){
                // 下单成功来到支付选择页
                model.addAttribute("submitOrderResponseVo",sb);
                return "pay";
            }else {
                // 下单失败回到订单确认页重新确定订单信息
                String msg = "下单失败；";
                switch (sb.getCode()){
                    case 1:msg += "订单信息过期请刷新再次提交!"; break;
                    case 2:msg += "订单商品价格发生变化请确认后再次提交!";break;
                    case 3:msg += "库存锁定失败，商品库存不足!";break;
                    default:
                }

                redirectAttributes.addFlashAttribute("msg",msg);
                return "redirect:http://order.mall.com/toTrade";
            }
        }catch (Exception e) {
            if (e instanceof NoStockException){
                redirectAttributes.addFlashAttribute("msg",e.getMessage());
            }
            return "redirect:http://order.mall.com/toTrade";
        }

    }
}
