package com.lyd.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyd.common.utils.PageUtils;
import com.lyd.mall.member.entity.MemberEntity;
import com.lyd.mall.member.exception.PhoneExistException;
import com.lyd.mall.member.exception.UsernameExistException;
import com.lyd.mall.member.vo.MemberRegistVo;

import java.util.Map;

/**
 * 会员
 *
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-29 00:05:11
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkEmailUnique(String email);
    void checkPhoneUnique(String phone) throws PhoneExistException;
    void checkUsernameUnique(String username) throws UsernameExistException;
}

