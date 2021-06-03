package com.lyd.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyd.common.utils.PageUtils;
import com.lyd.mall.ware.entity.WareInfoEntity;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-29 00:28:36
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @Description: 根据收货地址计算运费
     * @Param: [addrId]
     * @return: java.math.BigDecimal
     * @Author: Liuyunda
     * @Date: 2021/6/3
     */
    BigDecimal getFare(Long addrId);
}

