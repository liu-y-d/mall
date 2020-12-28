package com.lyd.mall.coupon.dao;

import com.lyd.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-28 23:56:22
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
