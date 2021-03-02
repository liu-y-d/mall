package com.lyd.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyd.common.to.SkuReductionTo;
import com.lyd.common.utils.PageUtils;
import com.lyd.mall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-28 23:56:22
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

