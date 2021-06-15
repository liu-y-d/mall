package com.lyd.mall.order.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Configuration;


/**
 * @Author Liuyunda
 * @Date 2021/6/10 22:51
 * @Email man021436@163.com
 * @Description: TODO
 */
@Configuration
public class MySeataConfig {

    @Autowired
    DataSourceProperties dataSourceProperties;
    // @Bean
    // public DataSource dataSource(){
    //     HikariDataSource build = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    //     return new DataSourceProxy(build);
    // }

}
