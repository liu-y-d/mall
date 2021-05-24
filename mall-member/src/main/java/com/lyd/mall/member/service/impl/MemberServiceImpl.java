package com.lyd.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.mall.member.dao.MemberDao;
import com.lyd.mall.member.dao.MemberLevelDao;
import com.lyd.mall.member.entity.MemberEntity;
import com.lyd.mall.member.entity.MemberLevelEntity;
import com.lyd.mall.member.exception.PhoneExistException;
import com.lyd.mall.member.exception.UsernameExistException;
import com.lyd.mall.member.service.MemberService;
import com.lyd.mall.member.vo.MemberLoginVo;
import com.lyd.mall.member.vo.MemberRegistVo;
import com.lyd.mall.member.vo.SocialUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {


    @Autowired
    MemberLevelDao memberLevelDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        MemberLevelEntity memberLevelEntity =  memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());

        // 检查用户名和手机号是否唯一
        this.checkPhoneUnique(vo.getPhone());
        this.checkUsernameUnique(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        // 密码加密存储
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        memberEntity.setNickname(vo.getUserName());
        memberEntity.setPassword(bCryptPasswordEncoder.encode(vo.getPassword()));
        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkEmailUnique(String email) {

    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer mobile = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile>0){
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count>0){
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (memberEntity==null){
            return null;
        }else {
            String passwordInBase = memberEntity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, passwordInBase);
            if (matches){
                return memberEntity;
            }else {
                return null;
            }
        }

    }

    @Override
    public MemberEntity login(SocialUser vo) {
        MemberEntity memberEntity = new MemberEntity();
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, String> param = new HashMap<>();
        param.put("access_token", vo.getAccess_token());
        String forObject = restTemplate.getForObject("https://gitee.com/api/v5/user" + "?access_token={access_token}", String.class, param);
        if (StringUtils.isNotEmpty(forObject)){
            JSONObject jsonObject = JSON.parseObject(forObject);
            memberEntity.setNickname((String) jsonObject.get("name"));
            memberEntity.setSocialUid(jsonObject.get("id").toString());
            QueryWrapper<MemberEntity> eq = new QueryWrapper<MemberEntity>().eq("social_uid", jsonObject.get("id").toString());
            Integer integer = baseMapper.selectCount(eq);
            if (integer>0){
                baseMapper.update(memberEntity,eq);
            }else {
                baseMapper.insert(memberEntity);
            }
            return memberEntity;
        }
        return null;
    }

}