package com.lyd.mall.ware.feign;

import com.lyd.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author Liuyunda
 * @Date 2021/6/15 22:38
 * @Email man021436@163.com
 * @Description: TODO
 */
@FeignClient("mall-order")
public interface OrderFeignService {
    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
