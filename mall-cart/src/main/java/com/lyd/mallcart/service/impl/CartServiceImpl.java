package com.lyd.mallcart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lyd.common.utils.R;
import com.lyd.mallcart.feign.ProductFeignService;
import com.lyd.mallcart.interceptor.CartInterceptor;
import com.lyd.mallcart.service.CartService;
import com.lyd.mallcart.vo.Cart;
import com.lyd.mallcart.vo.CartItem;
import com.lyd.mallcart.vo.SkuInfoVo;
import com.lyd.mallcart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Author Liuyunda
 * @Date 2021/5/25 20:35
 * @Email man021436@163.com
 * @Description: TODO
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    ProductFeignService feignService;
    private final String CART_PREFIX = "mall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)){
            // 1.新商品添加到购物车
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                // 2.远程查询当前要添加的商品信息
                R skuInfo = feignService.getSkuInfo(skuId);
                SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(info.getSkuDefaultImg());
                cartItem.setTitle(info.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(info.getPrice());
            },executor);


            // 2.远程查询Sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = feignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(getSkuInfo,getSkuSaleAttrValues).get();
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }else {
            // 购物车有这个商品，修改数量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(str, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();
        if (userInfo.getUserId()!=null){
            // 1.登录
            String cartKey = CART_PREFIX+userInfo.getUserId();
            BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
            // 如果临时购物车的数据还没有合并
            String tempKey = CART_PREFIX + userInfo.getUserKey();
            List<CartItem> tempCartList = getCartItems(tempKey);
            if (tempCartList!=null) {
                // 1.有数据需要合并
                for (CartItem cartItem : tempCartList) {
                    addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
                // 2.清空临时购物车
                clearCart(tempKey);
            }
            // 获取登录后的购物车的数据(包含合并的临时购物车的数据)
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }else {
            // 2.没登录
            String cartKey = CART_PREFIX+userInfo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        }
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);

    }

    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()==null){
            return null;
        }else {
            List<CartItem> cartItems = getCartItems(CART_PREFIX + userInfoTo.getUserId());
            List<CartItem> collect = cartItems.stream().filter(CartItem::getCheck).map(cartItem -> {
                // 更新最新价格
                R price = feignService.getPrice(cartItem.getSkuId());
                String data = (String) price.get("data");
                cartItem.setPrice(new BigDecimal(data));
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
    }

    /**
     * @Description: 获取到我们要添加的购物车
     * @Param: []
     * @return: org.springframework.data.redis.core.BoundHashOperations<java.lang.String,java.lang.Object,java.lang.Object>
     * @Author: Liuyunda
     * @Date: 2021/5/26
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfo.getUserId()!=null){
            cartKey = CART_PREFIX+userInfo.getUserId();
        }else {
            cartKey = CART_PREFIX+userInfo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

    private List<CartItem> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();
        if (values!=null && values.size()>0){
            List<CartItem> collect = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;

    }
}
