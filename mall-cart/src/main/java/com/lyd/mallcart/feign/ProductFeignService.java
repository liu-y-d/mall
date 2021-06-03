package com.lyd.mallcart.feign;

import com.lyd.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/5/26 21:28
 * @Email man021436@163.com
 * @Description: TODO
 */
@FeignClient("mall-product")
public interface ProductFeignService {
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    List<String > getSkuSaleAttrValues(@PathVariable("skuId")Long skuId);

    @GetMapping("/product/skuinfo/{skuId}/pric")
    R getPrice(@PathVariable("skuId") Long skuId);
}
