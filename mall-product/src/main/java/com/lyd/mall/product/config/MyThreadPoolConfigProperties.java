package com.lyd.mall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author Liuyunda
 * @Date 2021/5/12 21:57
 * @Email man021436@163.com
 * @Description: TODO
 */
@ConfigurationProperties(prefix = "mall.thread")
@Component
@Data
public class MyThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
