package com.lyd.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @Author Liuyunda
 * @Date 2021/4/1 20:23
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Configuration
public class MyRedissonConfig {

    /**
     * @Description: 所有对redisson的使用都是通过RedissonClient对象
     * @Param: []
     * @return: org.redisson.api.RedissonClient
     * @Author: Liuyunda
     * @Date: 2021/4/1
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        // 创建配置
        Config config = new Config();
        // 安全链接用rediss://
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");
        // 根据config创建出redissonClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;

    }
}
