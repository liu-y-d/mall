package com.lyd.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyd.common.utils.PageUtils;
import com.lyd.mall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-29 00:12:20
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

