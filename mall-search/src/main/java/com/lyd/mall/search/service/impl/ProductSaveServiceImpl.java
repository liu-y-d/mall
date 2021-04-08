package com.lyd.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lyd.common.to.es.SkuEsModel;
import com.lyd.mall.search.config.MallElasticsearchConfig;
import com.lyd.mall.search.constant.EsConstant;
import com.lyd.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Liuyunda
 * @Date 2021/3/18 22:09
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 将数据保存到es
        // 给es中建立一个索引。product
        // 建立好映射关系
        // 给Es中保存数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            // 构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String s = JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, MallElasticsearchConfig.COMMON_OPTIONS);

        // TODO 如果批量错误单独处理
        boolean b = bulk.hasFailures();
        BulkItemResponse[] items = bulk.getItems();
        List<String> collect = Arrays.asList(items).stream().map(item -> {
            String id = item.getId();
            return id;
        }).collect(Collectors.toList());
        log.info("商品上架完成：{},返回数据:{}",collect,bulk.toString());
        return b;
    }
}
