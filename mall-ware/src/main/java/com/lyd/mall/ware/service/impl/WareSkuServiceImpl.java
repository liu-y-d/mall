package com.lyd.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.exception.NoStockException;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.common.utils.R;
import com.lyd.mall.ware.dao.WareSkuDao;
import com.lyd.mall.ware.entity.WareSkuEntity;
import com.lyd.mall.ware.feign.ProductFeignService;
import com.lyd.mall.ware.service.WareSkuService;
import com.lyd.mall.ware.vo.OrderItemVo;
import com.lyd.mall.ware.vo.SkuHasStockVo;
import com.lyd.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)){
            queryWrapper.eq("sku_id",skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 如果没有库存记录就是新增操作
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities==null||wareSkuEntities.size()<=0){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            wareSkuEntity.setSkuName("");
            try {
                R info = productFeignService.info(skuId);
                if ((Integer)info.get("code")!=0){
                    log.error("添加库存：远程获取商品名称失败！");
                }else {
                    Map<String, Object> data = (Map) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e){

            }
            wareSkuDao.insert(wareSkuEntity);
        }else {
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            Long count = this.baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count == null?false:count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());

        return collect;
    }

    /**
     * @Description: 未订单锁库存
     * 默认只要是运行时异常都会回滚
     * @Param: [vo]
     * @return: java.util.List<com.lyd.mall.ware.vo.LockStockResult>
     * @Author: Liuyunda
     * @Date: 2021/6/8
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        // 1.按照下单的收货地址，找到一个就近仓库，锁定库存
        // 1.找到每个商品在那个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(orderItemVo -> {
            Long skuId = orderItemVo.getSkuId();
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(orderItemVo.getCount());
            // 查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());
        // 2.锁定库存
        Boolean allLock = true;
        for (SkuWareHasStock skuWareHasStock : collect) {
            Boolean skuStock = false;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();
            if (wareIds==null || wareIds.size() == 0){
                // 没有任何仓库有这个商品的库存
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                // 成功返回1，否则是0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,skuWareHasStock.getNum());
                if (count == 1){
                    skuStock = true;
                    break;
                }else {
                    // 当前仓库锁失败
                }
            }
            if (!skuStock){
                // 当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }
        // 3.肯定都是锁定成功的
        return true;
    }
    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}