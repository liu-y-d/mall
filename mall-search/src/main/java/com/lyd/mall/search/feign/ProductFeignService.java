package com.lyd.mall.search.feign;

import com.lyd.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/4/14 20:44
 * @Email man021436@163.com
 * @Description: TODO
 */
@FeignClient("mall-product")
public interface ProductFeignService {
    @GetMapping("/product/attr/info/{attrId}")
    R getAttrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    R brandInfo(@RequestParam("brandIds") List<Long> brandIds);
}
