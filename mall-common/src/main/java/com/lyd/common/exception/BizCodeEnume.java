package com.lyd.common.exception;

/**
 * @Author Liuyunda
 * @Date 2021/1/25 22:53
 * @Email man021436@163.com
 * @Description: 五位状态码：前两位代表业务 场景（10:通用，11：商品，12：订单，13：购物车，14：物流），后三位代表错误代码（001：位置错误）
 */
public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败！"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架出现异常！");

    private Integer code;
    private String message;
    BizCodeEnume(Integer code,String message){
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
