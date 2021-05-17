package com.lyd.mall.auth.controller;

import com.lyd.common.constant.AuthServerConstant;
import com.lyd.common.exception.BizCodeEnume;
import com.lyd.common.utils.R;
import com.lyd.mall.auth.feign.ThirdPartFignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author Liuyunda
 * @Date 2021/5/13 21:00
 * @Email man021436@163.com
 * @Description: TODO
 */
@Controller
public class LoginController {
    // @GetMapping("/login.html")
    // public String login(){
    //     return "login";
    // }
    // @GetMapping("/reg.html")
    // public String reg(){
    //     return "reg";
    // }

    @Autowired
    ThirdPartFignService thirdPartFignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone")String phone){

        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotEmpty(redisCode)){
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis()-l<60000){
                // 60内不能再发
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(),BizCodeEnume.SMS_CODE_EXCEPTION.getMessage());
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 5)+"_"+System.currentTimeMillis();

        //redis 缓存验证码
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,10, TimeUnit.MINUTES);
        thirdPartFignService.sendCode(phone, code);
        return R.ok();
    }
}
