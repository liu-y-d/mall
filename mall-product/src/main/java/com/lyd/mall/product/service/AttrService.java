package com.lyd.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyd.common.utils.PageUtils;
import com.lyd.mall.product.entity.AttrEntity;
import com.lyd.mall.product.vo.AttrGroupRelationVo;
import com.lyd.mall.product.vo.AttrRespVo;
import com.lyd.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-28 21:58:56
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroup2Id);

    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getUnRelationAttr(Map<String, Object> params, Long attrgroupId);

    List<Long> selectSearchAttrs(List<Long> attrIds);
}

