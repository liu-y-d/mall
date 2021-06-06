package com.lyd.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.common.utils.R;
import com.lyd.common.vo.MemberResponseVo;
import com.lyd.mall.order.constant.OrderConstant;
import com.lyd.mall.order.dao.OrderDao;
import com.lyd.mall.order.entity.OrderEntity;
import com.lyd.mall.order.feign.CartFeignService;
import com.lyd.mall.order.feign.MemberFeignService;
import com.lyd.mall.order.feign.WmsFeignService;
import com.lyd.mall.order.interceptor.LoginUserInterceptor;
import com.lyd.mall.order.service.OrderService;
import com.lyd.mall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WmsFeignService wmsFeignService;

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

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        // 下单，区创建订单，校验令牌，校验价格，锁库存
        // 1.验证令牌，返回0（令牌校验失败）-1
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else retrun 0 end";
        String orderToken = vo.getOrderToken();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        // 原子验证令牌和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), orderToken);
        if (result==0L){
            // 令牌验证失败
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        }else{

        }
        // String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());
        // if (orderToken!=null && orderToken.equals(redisToken)){
        //     // 令牌验证成功
        //
        // }else{
        //     // 不通过
        // }
        return submitOrderResponseVo;
    }

}