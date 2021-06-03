package com.lyd.mall.ware.feign;

import com.lyd.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Liuyunda
 * @Date 2021/6/3 22:57
 * @Email man021436@163.com
 * @Description: TODO
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R addrInfo(@PathVariable("id") Long id);
}
