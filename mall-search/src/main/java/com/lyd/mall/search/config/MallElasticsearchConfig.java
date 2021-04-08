package com.lyd.mall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Liuyunda
 * @Date 2021/3/16 21:47
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Configuration
public class MallElasticsearchConfig {

    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient(){

        HttpHost httpHost = new HttpHost("192.168.56.10", 9200, "http");
        RestClientBuilder http = RestClient.builder(httpHost);
        RestHighLevelClient client = new RestHighLevelClient(http);
        return client;
    }
}
