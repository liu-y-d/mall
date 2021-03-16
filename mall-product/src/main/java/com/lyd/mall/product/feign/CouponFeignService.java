package com.lyd.mall.product.feign;

import com.lyd.common.to.SkuReductionTo;
import com.lyd.common.to.SpuBoundTo;
import com.lyd.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author Liuyunda
 * @Date 2021/3/2 23:10
 * @Email man021436@163.com
 * @Description: DOTO
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {
    /**
     * @Description: 保存积分
     * 远程服务调用@RequestBody将对象转为json，放在请求体里，对方服务接收到请求后，将json再转为接收的参数对象，只要两个对象的属性名一致都可以转
     * 只要json数据模型是兼容的，那么双方服务无需使用同一个to对象
     * @Param: [spuBoundTo]
     * @return: com.lyd.common.utils.R
     * @Author: Liuyunda
     * @Date: 2021/3/2
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
