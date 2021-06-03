package com.lyd.mall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author Liuyunda
 * @Date 2021/6/2 21:20
 * @Email man021436@163.com
 * @Description: TODO
 */
public class OrderConfirmVo {
    /**
     * 收货地址
     */
    @Setter
    @Getter
    List<MemberAddressVo> addressVos;

    /**
     * 所有选中的购物项
     */
    @Setter
    @Getter
    List<OrderItemVo> items;

    /**
     * 发票记录。。。
     */

    /**
     * 优惠券
     */
    @Setter
    @Getter
    Integer integration;

    /**
     * 防重令牌
     */
    @Setter
    @Getter
    String orderToken;
    @Setter
    @Getter
    Map<Long,Boolean> stocks;
    public Integer getCount(){
        Integer i = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                i+=item.getCount();
            }
        }
        return i;
    }
    /**
     * 订单总额
     */
    // BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal decimal = new BigDecimal("0");
        if (items!=null){
            for (OrderItemVo item : items) {
                decimal = decimal.add(item.getPrice().multiply(new BigDecimal(item.getCount().toString())));
            }
        }
        return decimal;
    }

    /**
     * 应付价格
     */
    // BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        return getTotal();
    }

}
