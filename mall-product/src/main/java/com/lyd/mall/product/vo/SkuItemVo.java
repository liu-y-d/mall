package com.lyd.mall.product.vo;

import com.lyd.mall.product.entity.SkuImagesEntity;
import com.lyd.mall.product.entity.SkuInfoEntity;
import com.lyd.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/5/10 22:34
 * @Email man021436@163.com
 * @Description: TODO
 */
@Data
public class SkuItemVo {
    /**
     * sku基本信息 pms_sku_info
     */
    SkuInfoEntity info;

    boolean hasStock = true;

    /**
     * sku的图片信息 pms_sku_images
     */
    List<SkuImagesEntity> images;
    /**
     * spu的销售属性组合
     */
    List<SkuItemSaleAttrVo> saleAttr;
    /**
     * 获取spu的介绍
     */
    SpuInfoDescEntity desc;
    /**
     * 获取spu的规格参数信息
     */
    List<SpuItemAttrGroupVo> groupAttrs;
}
