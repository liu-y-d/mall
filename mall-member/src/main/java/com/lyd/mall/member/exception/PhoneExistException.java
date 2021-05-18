package com.lyd.mall.member.exception;

/**
 * @Author Liuyunda
 * @Date 2021/5/18 22:55
 * @Email man021436@163.com
 * @Description: TODO
 */
public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号存在");
    }
}
