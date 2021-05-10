package com.lyd.mall.product.web;

import com.lyd.mall.product.service.SkuInfoService;
import com.lyd.mall.product.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author Liuyunda
 * @Date 2021/5/10 22:25
 * @Email man021436@163.com
 * @Description: 商品详情
 */
@Slf4j
@Controller
public class ItemController {
    @Autowired
    SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId")Long skuId){
        log.info("准备查询商品："+skuId+"的详情。");
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        return "item";
    }
}
