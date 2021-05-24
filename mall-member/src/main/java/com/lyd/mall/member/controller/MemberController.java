package com.lyd.mall.member.controller;

import com.lyd.common.exception.BizCodeEnume;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.R;
import com.lyd.mall.member.entity.MemberEntity;
import com.lyd.mall.member.exception.PhoneExistException;
import com.lyd.mall.member.exception.UsernameExistException;
import com.lyd.mall.member.feign.CouponFeignService;
import com.lyd.mall.member.service.MemberService;
import com.lyd.mall.member.vo.MemberLoginVo;
import com.lyd.mall.member.vo.MemberRegistVo;
import com.lyd.mall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-29 00:05:11
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;


    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo){
        try{
            memberService.regist(vo);
        } catch (PhoneExistException e){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMessage());
        } catch (UsernameExistException e){
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(),BizCodeEnume.USER_EXIST_EXCEPTION.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity!=null){
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMessage());
        }
    }

    @PostMapping("/oauth/login")
    public R oauthLogin(@RequestBody SocialUser vo){
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity!=null){
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMessage());
        }
    }

    /**
     * @Description: 获取会员的优惠券信息
     * @Param: []
     * @return: com.lyd.common.utils.R
     * @Author: Liuyunda
     * @Date: 2020/12/29
     */
    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R memberCoupons = couponFeignService.memberCoupons();

        return R.ok().put("member",memberEntity).put("coupons",memberCoupons.get("coupons"));
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
