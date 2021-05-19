package com.lyd.mall.auth.feign;

import com.lyd.common.utils.R;
import com.lyd.mall.auth.vo.SocialUser;
import com.lyd.mall.auth.vo.UserLoginVo;
import com.lyd.mall.auth.vo.UserRegistVo;
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

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth/login")
    R oauthLogin(@RequestBody SocialUser vo);
}
