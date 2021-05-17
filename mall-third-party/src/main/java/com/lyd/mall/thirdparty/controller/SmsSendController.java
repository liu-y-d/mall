package com.lyd.mall.thirdparty.controller;

import com.lyd.common.utils.R;
import com.lyd.mall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Liuyunda
 * @Date 2021/5/17 21:29
 * @Email man021436@163.com
 * @Description: TODO
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    SmsComponent smsComponent;
    /**
     * @Description: 提供给别的服务进行调用
     * @Param: [phone, code]
     * @return: com.lyd.common.utils.R
     * @Author: Liuyunda
     * @Date: 2021/5/17
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone")String phone,@RequestParam("code")String code){
        // smsComponent.sendCode(phone,code);
        System.out.println(code);
        return R.ok();
    }
}
