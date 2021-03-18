package com.lyd.mall.search.controller;

import com.lyd.common.exception.BizCodeEnume;
import com.lyd.common.to.es.SkuEsModel;
import com.lyd.common.utils.R;
import com.lyd.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/3/18 22:05
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticsearchController {

    @Autowired
    ProductSaveService ProductSaveService;
    /**
     * @Description: 上架商品
     * @Param: [skuEsModels]
     * @return: com.lyd.common.utils.R
     * @Author: Liuyunda
     * @Date: 2021/3/18
     */
    @PostMapping("/product")
    public R productStatsUp(@RequestBody List<SkuEsModel> skuEsModels)  {
        Boolean aBoolean = false;
        try {
            aBoolean = ProductSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticsearchController 商品上架错误：{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if (!aBoolean){
            return R.ok();
        }else {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMessage());
        }
    }
}
