package com.lyd.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyd.common.utils.PageUtils;
import com.lyd.mall.order.entity.OrderEntity;
import com.lyd.mall.order.vo.OrderConfirmVo;
import com.lyd.mall.order.vo.OrderSubmitVo;
import com.lyd.mall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-29 00:12:20
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @Description: 订单确认页返回的数据
     * @Param: []
     * @return: com.lyd.mall.order.vo.OrderConfirmVo
     * @Author: Liuyunda
     * @Date: 2021/6/2
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);
}

