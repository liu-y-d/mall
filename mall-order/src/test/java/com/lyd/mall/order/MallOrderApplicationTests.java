package com.lyd.mall.order;

import com.lyd.mall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;

@Slf4j
@SpringBootTest
class MallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Test
    public void sendMessageTest(){
        String message = "hello world";
        // 如果发送的消息是个对象，会使用序列化机制，将对象写出去，对象必须实现Serializable
        for (int i = 0; i < 10; i++) {
            if (i%2==0){
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("qqqq-"+i);
                reasonEntity.setStatus(1);
                reasonEntity.setSort(1);
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java", reasonEntity,new CorrelationData(UUID.randomUUID().toString()));
            }else {
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java", message,new CorrelationData(UUID.randomUUID().toString()));
            }

            // log.info("消息发送完成{}",reasonEntity.toString());
        }
    }


    /**
     * @Description: 如何创建exchange、Queue、Binding，如何收发消息
     * @Param: []
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/5/31
     */
    @Test
    void contextLoads() {
        // 使用Amqp进行创建exchange
        DirectExchange directExchange = new DirectExchange("hello-java-exchange",true,false);
        amqpAdmin.declareExchange(directExchange);
        log.info("exchange[{}]创建成功","hello-java-exchange");
        // 使用Amqp进行创建Queue
        Queue queue = new Queue("hello-java-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("queue[{}]创建成功","hello-java-queue");
        // 使用Amqp进行创建Binding
        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE,"hello-java-exchange","hello.java",null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功","hello-java-Binding");

    }

}
