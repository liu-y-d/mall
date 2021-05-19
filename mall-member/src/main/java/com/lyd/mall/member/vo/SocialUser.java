package com.lyd.mall.member.vo;

import lombok.Data;

/**
 * @Author Liuyunda
 * @Date 2021/5/19 22:59
 * @Email man021436@163.com
 * @Description: TODO
 */
@Data
public class SocialUser {
    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;
    private String scope;
    private long created_at;
}
