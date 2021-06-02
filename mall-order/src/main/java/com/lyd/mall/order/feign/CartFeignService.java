package com.lyd.mall.order.feign;

import com.lyd.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/6/2 21:54
 * @Email man021436@163.com
 * @Description: TODO
 */
@FeignClient("mall-cart")
public interface CartFeignService {
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentCartItems();
}
