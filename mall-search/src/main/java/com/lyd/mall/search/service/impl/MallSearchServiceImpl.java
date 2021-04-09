package com.lyd.mall.search.service.impl;

import com.lyd.mall.search.config.MallElasticsearchConfig;
import com.lyd.mall.search.constant.EsConstant;
import com.lyd.mall.search.service.MallSearchService;
import com.lyd.mall.search.vo.SearchParam;
import com.lyd.mall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Author Liuyunda
 * @Date 2021/4/8 21:29
 * @Email man021436@163.com
 * @Description: TODO
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;
    /**
     * @Description: 检索的所有参数，返回所有的结果
     * @Param: [searchParam]
     * @return: java.lang.Object
     * @Author: Liuyunda
     * @Date: 2021/4/8
     */
    @Override
    public SearchResult search(SearchParam searchParam) {
        // 动态构建出查询需要的DSL语句
        SearchResult result = null;
        // 1.准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            // 2.执行检索请求
            SearchResponse response = client.search(searchRequest, MallElasticsearchConfig.COMMON_OPTIONS);
            // 3.分析响应数据，封装成我们需要的格式
            result = buildSearchResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @Description: 准备检索请求
     * 模糊匹配，过滤（属性，分类，品牌，价格区间，库存），排序，分页，高亮，聚合分析
     * @Param: [searchParam]
     * @return: org.elasticsearch.action.search.SearchRequest
     * @Author: Liuyunda
     * @Date: 2021/4/9
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder ssb = new SearchSourceBuilder();
        /**
         * 模糊匹配，过滤（属性，分类，品牌，价格区间，库存）
         */
        // bool query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // must 模糊匹配
        if (StringUtils.isNotEmpty(searchParam.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",searchParam.getKeyword()));
        }
        // filter 三级分类id
        if (searchParam.getCatalog3Id()!=null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",searchParam.getCatalog3Id()));
        }
        // filter 品牌id
        if (searchParam.getBrandId()!=null && searchParam.getBrandId().size()>0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",searchParam.getBrandId()));
        }
        // filter 属性
        if (searchParam.getAttrs()!=null && searchParam.getAttrs().size()>0){
            for (String attrString : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrString.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs",nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }


        // filter 库存
        boolQuery.filter(QueryBuilders.termQuery("hasStock",searchParam.getHasStock()==1));

        // filter 价格区间
        if (StringUtils.isNotEmpty(searchParam.getSkuPrice())){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = searchParam.getSkuPrice().split("_");
            if (s.length==2){
                rangeQuery.gte(s[0]).lte(s[1]);
            }else if (s.length==1){
                if (searchParam.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }
                if (searchParam.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        ssb.query(boolQuery);
        /**
         * 排序，分页，高亮
         */
        // 排序
        if (StringUtils.isNotEmpty(searchParam.getSort())){
            String sort = searchParam.getSort();
            String[] s = sort.split("_");
            SortOrder sortOrder = s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            ssb.sort(s[0], sortOrder);
        }

        // 分页
        ssb.from((searchParam.getPageNum()-1)*EsConstant.PRODUCT_PAGE_SIZE);
        ssb.size(EsConstant.PRODUCT_PAGE_SIZE);

        // 高亮
        if (StringUtils.isNotEmpty(searchParam.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red;'>");
            highlightBuilder.postTags("</b>");
            ssb.highlighter(highlightBuilder);
        }
        /**
         * 聚合分析
         */
        // 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        // 品牌聚合子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        ssb.aggregation(brand_agg);

        // 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        ssb.aggregation(catalog_agg);

        // 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg","attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(1);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        ssb.aggregation(attr_agg);

        System.out.println(ssb.toString());


        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, ssb);
        return searchRequest;
    }

    /**
     * @Description: 构建结果数据
     * @Param: [response]
     * @return: com.lyd.mall.search.vo.SearchResult
     * @Author: Liuyunda
     * @Date: 2021/4/9
     */
    private SearchResult buildSearchResult(SearchResponse response) {
        return null;
    }
}
