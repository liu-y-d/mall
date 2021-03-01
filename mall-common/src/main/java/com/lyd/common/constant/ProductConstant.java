package com.lyd.common.constant;

/**
 * @Author Liuyunda
 * @Date 2021/3/1 21:35
 * @Email man021436@163.com
 * @Description: DOTO
 */
public class ProductConstant {
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),ATTR_TYPE_SALE(0,"销售属性");
        private int code;
        private String msg;
        AttrEnum(int code, String msg){
            this.code = code;
            this.msg = msg;
        }
        public int getCode(){
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
