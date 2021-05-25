package com.lyd.mallcart.service.impl;

import com.lyd.mallcart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
}
