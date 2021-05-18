package com.lyd.mall.auth.feign;

import com.lyd.common.utils.R;
import com.lyd.mall.auth.co.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author Liuyunda
 * @Date 2021/5/18 23:24
 * @Email man021436@163.com
 * @Description: TODO
 */
@FeignClient("mall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
     R regist(@RequestBody UserRegistVo vo);
}
