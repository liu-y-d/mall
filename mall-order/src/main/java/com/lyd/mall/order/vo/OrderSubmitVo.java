package com.lyd.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author Liuyunda
 * @Date 2021/6/6 17:51
 * @Email man021436@163.com
 * @Description: 订单提交的数据
 */
@Data
public class OrderSubmitVo {
    /**
     * 收货地址的id
     */
    private Long addrId;

    /**
     * 支付方式
     */
    private Integer payType;

    // 无需提交需要购买的商品去购物车再获取一遍
    // 优惠发票等等
    /**
     * 去重令牌
     */
    private String orderToken;
    /**
     * 应付金额
      */
    private BigDecimal payPrice;

    /**
     * 订单备注
     */
    private String note;
    // 用户信息，直接session中取
}
