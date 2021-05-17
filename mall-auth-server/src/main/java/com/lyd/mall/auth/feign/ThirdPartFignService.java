package com.lyd.mall.auth.feign;

import com.lyd.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author Liuyunda
 * @Date 2021/5/17 21:35
 * @Email man021436@163.com
 * @Description: TODO
 */
@FeignClient("mall-third-party")
public interface ThirdPartFignService {
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone")String phone, @RequestParam("code")String code);
}
