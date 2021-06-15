package com.lyd.mall.order.config;


import com.lyd.mall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Liuyunda
 * @Date 2021/6/15 20:30
 * @Email man021436@163.com
 * @Description: TODO
 */
@Component
public class MyMQConfig {

    /**
     * @Description: 容器中的Binding ，Queue，Exchange都会自动创建
     * RabbitMQ 只要有，@Bean生成的组件不会覆盖
     * @Param: []
     * @return: org.springframework.amqp.core.Queue
     * @Author: Liuyunda
     * @Date: 2021/6/15
     */
    @Bean
    public Queue orderDelayQueue (){
        /**
         * 死信队列属性
         * x-dead-letter-exchange:order-event-exchange
         * x-dead-letter-routing-key:order.release.order
         * x-message-ttl:60000
         */
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
        return new Queue("order.delay.queue",true,false,false,arguments);
    }
    @Bean
    public Queue orderReleaseOrderQueue (){
        return new Queue("order.release.order.queue",true,false,false);
    }
    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);
    }
    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.create.order",null);
    }
    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE,"order-event-exchange","order.release.order",null);
    }

    @RabbitListener(queues = "order.release.order.queue")
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息：准备关闭订单："+entity.getOrderSn()+",Time:"+new Date());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
