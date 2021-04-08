package com.lyd.mall.search.service;

import com.lyd.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/3/18 22:08
 * @Email man021436@163.com
 * @Description: DOTO
 */
public interface ProductSaveService {
    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
