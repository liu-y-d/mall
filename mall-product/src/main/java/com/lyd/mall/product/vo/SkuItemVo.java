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
    /**
     * sku的图片信息 pms_sku_images
     */
    List<SkuImagesEntity> images;
    /**
     * spu的销售属性组合
     */
    List<ItemSaleAttrsVo> saleAttr;
    /**
     * 获取spu的介绍
     */
    SpuInfoDescEntity desc;
    /**
     * 获取spu的规格参数信息
     */
    List<SpuItemAttrGroupVo> groupAttrs;

    @Data
    public static class ItemSaleAttrsVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

    @Data
    public static class SpuItemAttrGroupVo{
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }

    @Data
    public static class SpuBaseAttrVo{
        private String attrName;
        private String attrValues;
    }
}
