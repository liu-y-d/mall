package com.lyd.mall.order.feign;

import com.lyd.mall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/6/2 21:35
 * @Email man021436@163.com
 * @Description: TODO
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> getReceiveAddress(@PathVariable("memberId") Long memberId);
}
