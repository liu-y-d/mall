package com.lyd.mall.product.feign;

import com.lyd.common.to.es.SkuEsModel;
import com.lyd.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/3/18 22:29
 * @Email man021436@163.com
 * @Description: DOTO
 */
@FeignClient("mall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productStatsUp(@RequestBody List<SkuEsModel> skuEsModels);
}
