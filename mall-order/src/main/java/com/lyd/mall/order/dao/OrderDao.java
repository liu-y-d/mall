package com.lyd.mall.order.dao;

import com.lyd.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-29 00:12:20
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
