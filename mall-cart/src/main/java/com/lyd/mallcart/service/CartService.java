package com.lyd.mallcart.service;

import com.lyd.mallcart.vo.Cart;
import com.lyd.mallcart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @Author Liuyunda
 * @Date 2021/5/25 20:35
 * @Email man021436@163.com
 * @Description: TODO
 */
public interface CartService {
    /**
     * @Description: 添加商品到购物车
     * @Param: [skuId, num]
     * @return: com.lyd.mallcart.vo.CartItem
     * @Author: Liuyunda
     * @Date: 2021/5/26
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * @Description: 获取购物车中的某个购物项
     * @Param: [skuId]
     * @return: com.lyd.mallcart.vo.CartItem
     * @Author: Liuyunda
     * @Date: 2021/5/26
     */
    CartItem getCartItem(Long skuId);

    /**
     * @Description: 获得整个购物车
     * @Param: []
     * @return: com.lyd.mallcart.vo.Cart
     * @Author: Liuyunda
     * @Date: 2021/5/27
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * @Description: 清空购物车
     * @Param: [cartKey]
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/5/27
     */
    void clearCart(String cartKey);

    /**
     * @Description: 切换购物项选中状态
     * @Param: [skuId, check]
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/5/27
     */
    void checkItem(Long skuId, Integer check);

    /**
     * @Description: 改变数量
     * @Param: [skuId, num]
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/5/27
     */
    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);
}
