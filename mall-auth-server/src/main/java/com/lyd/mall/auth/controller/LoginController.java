package com.lyd.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.lyd.common.constant.AuthServerConstant;
import com.lyd.common.exception.BizCodeEnume;
import com.lyd.common.utils.R;
import com.lyd.mall.auth.co.UserRegistVo;
import com.lyd.mall.auth.feign.MemberFeignService;
import com.lyd.mall.auth.feign.ThirdPartFignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    MemberFeignService memberFeignService;
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
        String codeAppend = code+"_"+System.currentTimeMillis();

        //redis 缓存验证码
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,codeAppend,10, TimeUnit.MINUTES);
        thirdPartFignService.sendCode(phone, code);
        return R.ok();
    }


    /**
     * @Description: 重定向携带数据，利用session原理，将数据放在session中。todo 分布式session问题
     * @Param: [vo, result, redirectAttributes]
     * @return: java.lang.String
     * @Author: Liuyunda
     * @Date: 2021/5/18
     */
    @PostMapping("/regist")
    public String register(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes){
        if (result.hasErrors()){
            Map<String, String> collect = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            // 校验出错转发到注册页
            redirectAttributes.addFlashAttribute("errors",collect);
            return "redirect:http://auth.mal.com/reg.html";
        }
        // 注册调用远程服务注册
        // 校验验证码
        String code = vo.getCode();
        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.isNotEmpty(s)){
            if (code.equals(s.split("_")[0])){
                // 删除验证码
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                // 验证码通过进行注册
                R regist = memberFeignService.regist(vo);
                if ((Integer)regist.get("code")==0){
                    return "redirect:http://auth.mall.com/login.html";
                } else {
                    HashMap<String, String> errors = new HashMap<>();
                    errors.put("msg",regist.getData(new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.mal.com/reg.html";
                }
            }else {
                Map<String, String> collect = new HashMap<>();
                collect.put("code","验证码错误");
                // 校验出错转发到注册页
                redirectAttributes.addFlashAttribute("errors",collect);
                return "redirect:http://auth.mal.com/reg.html";
            }
        }else {
            Map<String, String> collect = new HashMap<>();
            collect.put("code","验证码错误");
            // 校验出错转发到注册页
            redirectAttributes.addFlashAttribute("errors",collect);
            return "redirect:http://auth.mal.com/reg.html";
        }
    }
}
