package com.lyd.mall.member.feign;

import com.lyd.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Liuyunda
 * @Date 2020/12/29 22:32
 * @Email man021436@163.com
 * @Description: DOTO
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {
    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();
}
