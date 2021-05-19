package com.lyd.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lyd.common.utils.R;
import com.lyd.mall.auth.feign.MemberFeignService;
import com.lyd.mall.auth.vo.MemberResponseVo;
import com.lyd.mall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * @Author Liuyunda
 * @Date 2021/5/19 22:41
 * @Email man021436@163.com
 * @Description: 三方登录
 */
@Slf4j
@Controller
public class OAuth2Controller {


    @Autowired
    MemberFeignService memberFeignService;
    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code") String code) throws Exception {
        // 根据code换区access_token
        HashMap<String, String> map = new HashMap<>();
        map.put("grant_type","authorization_code");
        map.put("code",code);
        map.put("client_id","62f966c9c839298ce99188f08ff2499c1948ae0510e88c935bd19474d68a8b11");
        map.put("redirect_uri","http://auth.mall.com/oauth2.0/gitee/success");
        map.put("client_secret","2df17285b86e907b81121f2e187162e88bd17446010d85a6db19ba0d046cd793");
        RestTemplate restTemplate = new RestTemplate();
        String Url = "https://gitee.com/oauth/token";
        String s = restTemplate.postForObject(Url, map, String.class);
        if (StringUtils.isNotEmpty(s)){
            // 获取到了access_token
            SocialUser socialUser = JSON.parseObject(s, SocialUser.class);
            R r = memberFeignService.oauthLogin(socialUser);
            if ((Integer) r.get("code")==0){
                MemberResponseVo data = r.getData("data", new TypeReference<MemberResponseVo>() {});
                log.info("登录成功：{}",data.toString());
                // 登录成功跳回首页
                return "redirect:http://mall.com";
            }else {
                return "redirect:http://auth.mall.com/login.html";
            }
        }else {
            return "redirect:http://auth.mall.com/login.html";
        }
    }
}
