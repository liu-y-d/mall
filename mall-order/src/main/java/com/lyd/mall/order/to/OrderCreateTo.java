package com.lyd.mall.order.to;

import com.lyd.mall.order.entity.OrderEntity;
import com.lyd.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/6/8 20:17
 * @Email man021436@163.com
 * @Description: TODO
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    /**
     * 订单应付价格
     */
    private BigDecimal payPrice;

    /**
     * 运费
     */
    private BigDecimal fare;
}
