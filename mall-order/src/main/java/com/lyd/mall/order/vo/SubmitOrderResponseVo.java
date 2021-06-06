package com.lyd.mall.order.vo;

import com.lyd.mall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Author Liuyunda
 * @Date 2021/6/6 18:12
 * @Email man021436@163.com
 * @Description: TODO
 */
@Data
public class SubmitOrderResponseVo {

    /**
     * 订单信息
     */
    private OrderEntity order;

    /**
     * 错误状态码 0 代表成功
     */
    private Integer code;
}
