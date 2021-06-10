package com.lyd.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.exception.NoStockException;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.common.utils.R;
import com.lyd.common.vo.MemberResponseVo;
import com.lyd.mall.order.constant.OrderConstant;
import com.lyd.mall.order.dao.OrderDao;
import com.lyd.mall.order.entity.OrderEntity;
import com.lyd.mall.order.entity.OrderItemEntity;
import com.lyd.mall.order.enume.OrderStatusEnum;
import com.lyd.mall.order.feign.CartFeignService;
import com.lyd.mall.order.feign.MemberFeignService;
import com.lyd.mall.order.feign.ProductFeignService;
import com.lyd.mall.order.feign.WmsFeignService;
import com.lyd.mall.order.interceptor.LoginUserInterceptor;
import com.lyd.mall.order.service.OrderItemService;
import com.lyd.mall.order.service.OrderService;
import com.lyd.mall.order.to.OrderCreateTo;
import com.lyd.mall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {


    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> getAddress = CompletableFuture.runAsync(() -> {
            //1.远程查询所有的收货地址列表
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> receiveAddress = memberFeignService.getReceiveAddress(memberResponseVo.getId());
            confirmVo.setAddressVos(receiveAddress);
        }, executor);

        CompletableFuture<Void> getCart = CompletableFuture.runAsync(() -> {
            //2.远程查询购物车所有选中的购物项
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> cartItems = cartFeignService.getCurrentCartItems();
            confirmVo.setItems(cartItems);
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R skusHasStock = wmsFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = skusHasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data!=null){
                Map<Long, Boolean> collect1 = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(collect1);
            }
        },executor);
        //3.查询用户的积分
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 4.防重令牌
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResponseVo.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(getAddress,getCart).get();

        return confirmVo;
    }
    // 本地事务在分布式系统下，只能控制住自己的回滚，控制不了其他服务的回滚
    // 分布式事务：最大原因，网络问题。

    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        submitVoThreadLocal.set(vo);
        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        submitOrderResponseVo.setCode(0);
        // 下单，区创建订单，校验令牌，校验价格，锁库存
        // 1.验证令牌，返回0（令牌校验失败）-1
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        // 原子验证令牌和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), orderToken);
        if (result==0L){
            // 令牌验证失败
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        }else{
            // 令牌验证成功
            // 下单：去创建订单，验证令牌，验价格，锁库存
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue())<0.01) {
                // 金额对比成功
                // todo 保存订单
                saveOrder(order);
                // 锁库存，只要有异常回滚订单数据
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map(orderItemEntity -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(orderItemEntity.getSkuId());
                    orderItemVo.setCount(orderItemEntity.getSkuQuantity());
                    orderItemVo.setTitle(orderItemEntity.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(orderItemVos);
                // todo 远程锁库存
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if (((Integer) r.get("code")) == 0) {
                    // 锁成功
                    submitOrderResponseVo.setOrder(order.getOrder());
                    // todo 远程扣减积分
                    int i = 10/0;
                    return submitOrderResponseVo;
                }else {
                    // 锁定失败
                    throw new NoStockException();
                    // submitOrderResponseVo.setCode(3);
                    // return submitOrderResponseVo;
                }
            }else{
                submitOrderResponseVo.setCode(2);
                return submitOrderResponseVo;
            }

        }
    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder(){

        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1.生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

        // 2.获取到所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        // 3.计算价格、积分等相关数据
        computePrice(orderEntity,itemEntities);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(itemEntities);
        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal giftIntegration = new BigDecimal("0.0");
        BigDecimal giftGrowth = new BigDecimal("0.0");
        // 叠加订单项的总额
        for (OrderItemEntity itemEntity : itemEntities) {
            BigDecimal realAmount = itemEntity.getRealAmount();
            coupon = coupon.add(itemEntity.getCouponAmount());
            integration = integration.add(itemEntity.getIntegrationAmount());
            promotion = promotion.add(itemEntity.getPromotionAmount());
            total = total.add(realAmount);
            giftIntegration = giftIntegration.add(new BigDecimal(itemEntity.getGiftIntegration().toString()));
            giftGrowth = giftGrowth.add(new BigDecimal(itemEntity.getGiftGrowth().toString()));
        }
        // 1.订单价格相关的数据
        orderEntity.setTotalAmount(total);
        // 设置应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setGrowth(giftGrowth.intValue());
        orderEntity.setIntegration(giftIntegration.intValue());
        orderEntity.setDeleteStatus(0);
    }

    /**
     * @Description: 构建订单
     * @Param: [orderSn]
     * @return: com.lyd.mall.order.entity.OrderEntity
     * @Author: Liuyunda
     * @Date: 2021/6/8
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(memberResponseVo.getId());
        // 获取收货地址信息
        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo data = fare.getData(new TypeReference<FareVo>() {
        });
        // 设置运费信息
        entity.setFreightAmount(data.getFare());
        // 设置收货人信息
        entity.setReceiverCity(data.getAddress().getCity());
        entity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
        entity.setReceiverName(data.getAddress().getName());
        entity.setReceiverPhone(data.getAddress().getPhone());
        entity.setReceiverPostCode(data.getAddress().getPostCode());
        entity.setReceiverProvince(data.getAddress().getProvince());
        entity.setReceiverRegion(data.getAddress().getRegion());

        // 订单的状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        return entity;
    }

    /**
     * @Description: 构建所有订单项数据
     * @Param: []
     * @return: java.util.List<com.lyd.mall.order.entity.OrderItemEntity>
     * @Author: Liuyunda
     * @Date: 2021/6/8
     * @param orderSn
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // 最后确定每个购物项的价格
        List<OrderItemVo> cartItems = cartFeignService.getCurrentCartItems();
        if (cartItems != null && cartItems.size()>0) {
            List<OrderItemEntity> itemEntities = cartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);;
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * @Description: 构建单个订单项数据
     * @Param: [item]
     * @return: com.lyd.mall.order.entity.OrderItemEntity
     * @Author: Liuyunda
     * @Date: 2021/6/8
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 订单号
        // 商品的SPU信息
        Long skuId = item.getSkuId();
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setCategoryId(data.getCatalogId());
        // 商品的sku信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuAttrsVals(String.join(";",item.getSkuAttr()));
        orderItemEntity.setSkuQuantity(item.getCount());
        // 商品的优惠信息（不做）
        // 积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());

        // 订单项的价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        // 当前订单项的实际金额
        BigDecimal orign = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        orign = orign.subtract(orderItemEntity.getPromotionAmount()).subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(orign);

        return orderItemEntity;
    }

}