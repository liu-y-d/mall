package com.lyd.mall.search.vo;

import com.lyd.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/4/8 21:44
 * @Email man021436@163.com
 * @Description: TODO
 */
@Data
public class SearchResult {
    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    private List<Integer> pageNavs;

    /**
     * 当前查询的结果，所有涉及的品牌
     */
    private List<BrandVo> brands;

    /**
     * 当前查询的结果，所有涉及的属性
     */
    private List<AttrVo> attrs;

    /**
     * 当前查询的结果，所有涉及的分类
     */
    private List<CatalogVo> catalogs;

    /**
     * 面包屑导航数据
     */
    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
}
