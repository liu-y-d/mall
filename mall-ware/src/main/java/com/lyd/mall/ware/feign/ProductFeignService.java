package com.lyd.mall.ware.feign;

import com.lyd.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Liuyunda
 * @Date 2021/3/4 22:57
 * @Email man021436@163.com
 * @Description: DOTO
 */
@FeignClient("mall-gateway")
public interface ProductFeignService {
    @RequestMapping("/api/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId")Long skuId);
}
