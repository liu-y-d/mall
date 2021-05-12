package com.lyd.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/5/11 22:02
 * @Email man021436@163.com
 * @Description: TODO
 */
@ToString
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
