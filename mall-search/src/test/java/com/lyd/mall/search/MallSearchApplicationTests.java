package com.lyd.mall.search;

import com.alibaba.fastjson.JSON;
import com.lyd.mall.search.config.MallElasticsearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
class MallSearchApplicationTests {

    @Data
    static class Accout {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @Autowired
    RestHighLevelClient client;
    @Test
    void contextLoads() {
    }

    /**
     * @Description: 测试存储数据
     * @Param: []
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/3/16
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        // indexRequest.source("username","张三","age",18,"gender","男");
        User user = new User();
        user.setUserName("张三");
        user.setAge(20);
        user.setGender("男");
        String userString = JSON.toJSONString(user);
        indexRequest.source(userString,XContentType.JSON);

        IndexResponse index = client.index(indexRequest, MallElasticsearchConfig.COMMON_OPTIONS);

        System.out.println(index);
    }

    @Test
    public void searchData() throws IOException {
        // 创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("bank");
        // 指定DSL，检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构造检索条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        // 按照年龄聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);
        // 计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);
        System.out.println(searchSourceBuilder.toString());

        searchRequest.source(searchSourceBuilder);

        // 执行检索
        SearchResponse search = client.search(searchRequest, MallElasticsearchConfig.COMMON_OPTIONS);

        // 拿到响应的数据，分析
        System.out.println(search.toString());
        // Map map = JSON.parseObject(search.toString(), Map.class);
        // 获取所有查到的数据
        SearchHits hits = search.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            // Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String sourceAsString = searchHit.getSourceAsString();
            Accout accout = JSON.parseObject(sourceAsString, Accout.class);
            System.out.println(accout);
        }
        // 获取分析
        Aggregations aggregations = search.getAggregations();
        Terms ageAggTearms = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAggTearms.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println(keyAsString+":"+bucket.getDocCount());
        }
        Avg balanceAvgAvg = aggregations.get("balanceAvg");
        System.out.println(balanceAvgAvg.getValue());
    }
    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    public void es(){
        System.out.println(client);
    }

}
