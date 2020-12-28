package com.lyd.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyd.common.utils.PageUtils;
import com.lyd.mall.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-29 00:28:36
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

