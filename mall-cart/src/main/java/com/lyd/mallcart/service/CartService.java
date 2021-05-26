package com.lyd.mallcart.service;

import com.lyd.mallcart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @Author Liuyunda
 * @Date 2021/5/25 20:35
 * @Email man021436@163.com
 * @Description: TODO
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;
}
