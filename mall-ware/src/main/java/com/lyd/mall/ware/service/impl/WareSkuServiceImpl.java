package com.lyd.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.exception.NoStockException;
import com.lyd.common.to.mq.StockDeatilTo;
import com.lyd.common.to.mq.StockLockedTo;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.common.utils.R;
import com.lyd.mall.ware.dao.WareSkuDao;
import com.lyd.mall.ware.entity.WareOrderTaskDetailEntity;
import com.lyd.mall.ware.entity.WareOrderTaskEntity;
import com.lyd.mall.ware.entity.WareSkuEntity;
import com.lyd.mall.ware.feign.OrderFeignService;
import com.lyd.mall.ware.feign.ProductFeignService;
import com.lyd.mall.ware.service.WareOrderTaskDetailService;
import com.lyd.mall.ware.service.WareOrderTaskService;
import com.lyd.mall.ware.service.WareSkuService;
import com.lyd.mall.ware.vo.OrderItemVo;
import com.lyd.mall.ware.vo.OrderVo;
import com.lyd.mall.ware.vo.SkuHasStockVo;
import com.lyd.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;


    private void unLockStock(Long skuId,Long wareId,Integer num,Long taskDetailId){
        // 库存解锁
        wareSkuDao.unLockStock(skuId,wareId,num);
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(taskDetailId);
        wareOrderTaskDetailEntity.setLockStatus(2);
        this.wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }
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
     * 库存解锁的场景
     *      1.下订单成功，订单过期没有支付被系统自动取消、被用户手动取消，都要解锁库存
     *      2.下订单成功，库存锁定成功，但是接下来的业务调用失败，导致订单回滚，之前锁定的库存就要自动解锁
     * @Param: [vo]
     * @return: java.util.List<com.lyd.mall.ware.vo.LockStockResult>
     * @Author: Liuyunda
     * @Date: 2021/6/8
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工作单的详情
         * 追溯
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

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
            // 1.如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
            // 2.锁定失败，前面保存的工作单信息就回滚了。发送出去的消息即使要解锁记录，由于去数据库查不到id，所以就不用解锁
            for (Long wareId : wareIds) {
                // 成功返回1，否则是0
                Long count = wareSkuDao.lockSkuStock(skuId,wareId,skuWareHasStock.getNum());
                if (count == 1){
                    skuStock = true;
                    // 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null,skuId,"",skuWareHasStock.getNum(),wareOrderTaskEntity.getId(),wareId,1);
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDeatilTo stockDeatilTo = new StockDeatilTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity,stockDeatilTo);
                    stockLockedTo.setDetail(stockDeatilTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockedTo);
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

    @Override
    public void unlockStock(StockLockedTo to) {
        StockDeatilTo detail = to.getDetail();
        Long detailId = detail.getId();
        // 解锁
        /**
         * 查询数据库关于这个订单的锁定库存信息
         * 如果数据库
         *      有：证明库存锁定成功了
         *          解锁：订单情况
         *              1.没有这个订单。必须解锁
         *              2.如果有订单。不是解锁库存
         *                  看订单状态，如果是已取消，那么就解锁库存
         *                  没取消就不能解锁
         *      没有：库存锁定失败，库存回滚了。这种情况无须解锁
         */
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId != null) {
            Long id = to.getId();
            WareOrderTaskEntity byId1 = wareOrderTaskService.getById(id);
            String orderSn = byId1.getOrderSn();
            // 根据订单订单号查询订单的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if ((Integer) r.get("code") == 0) {
                // 订单数据返回成功
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                if (data == null||data.getStatus() == 4 ) {
                    // 订单已经被取消了或订单不存在，才能解锁库存
                    if (byId.getLockStatus() == 1){
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                // 消息拒绝后重新放入消息队列，让别人继续消费解锁
                throw new RuntimeException("远程服务失败");
            }
        } else {
            // 无需解锁
        }
    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}