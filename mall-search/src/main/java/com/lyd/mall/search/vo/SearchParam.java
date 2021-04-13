package com.lyd.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/4/8 21:27
 * @Email man021436@163.com
 * @Description: 封装页面所有可能穿过来的查询条件
 */
@Data
public class SearchParam {

    /**
     * 全文匹配关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件
     */
    private String sort;

    /**
     * 是否有货
     */
    private Integer hasStock;

    /**
     * 价格区间
     */
    private String skuPrice;

    /**
     * 品牌id
     */
    private List<Long> brandId;


    /**
     * 按照属性筛选
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

}
