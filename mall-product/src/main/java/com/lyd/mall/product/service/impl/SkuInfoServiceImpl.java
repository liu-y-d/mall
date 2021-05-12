package com.lyd.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.mall.product.dao.SkuInfoDao;
import com.lyd.mall.product.entity.SkuImagesEntity;
import com.lyd.mall.product.entity.SkuInfoEntity;
import com.lyd.mall.product.entity.SpuInfoDescEntity;
import com.lyd.mall.product.service.*;
import com.lyd.mall.product.vo.SkuItemSaleAttrVo;
import com.lyd.mall.product.vo.SkuItemVo;
import com.lyd.mall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService descService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and((wapper)->{
               wapper.eq("sku_id",key).or().like("sku_name",key);
            });
        }
        String catalogId = (String) params.get("catalogId");
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            queryWrapper.eq("catalog_id",catalogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(brandId)){
            queryWrapper.eq("brandId",brandId);

        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.ge("price",min);

        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)){
            try {
                BigDecimal bigDecimal = new BigDecimal("0");
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1){
                    queryWrapper.le("price",max);
                }
            } catch (Exception e){

            }



        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);
        List<SkuInfoEntity> list = this.list(queryWrapper);
        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // sku基本信息 pms_sku_info
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(result -> {
            // spu的销售属性组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(result.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        CompletableFuture<Void> spuDescFuture = infoFuture.thenAcceptAsync(res -> {
            // 获取spu的介绍
            SpuInfoDescEntity spuinfo = descService.getById(res.getSpuId());
            skuItemVo.setDesc(spuinfo);
        }, executor);

        CompletableFuture<Void> attrGroupFuture = infoFuture.thenAcceptAsync(res -> {
            // 获取spu的规格参数信息
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);


        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            // sku的图片信息 pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        });

        // 等待所有任务都完成
        CompletableFuture.allOf(saleAttrFuture,spuDescFuture,attrGroupFuture,imagesFuture).get();



        return skuItemVo;
    }

}