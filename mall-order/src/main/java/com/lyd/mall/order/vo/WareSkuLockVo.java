package com.lyd.mall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/6/8 22:14
 * @Email man021436@163.com
 * @Description: TODO
 */
@Data
public class WareSkuLockVo {
    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 需要锁住的所有库存信息
     */
    private List<OrderItemVo> locks;
}
