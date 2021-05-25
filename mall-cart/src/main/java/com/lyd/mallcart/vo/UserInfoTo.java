package com.lyd.mallcart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @Author Liuyunda
 * @Date 2021/5/25 20:58
 * @Email man021436@163.com
 * @Description: TODO
 */
@ToString
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;
    private boolean tempUser = false;
}
