package com.lyd.mall.order.controller;

import com.lyd.mall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @Author Liuyunda
 * @Date 2021/5/31 22:14
 * @Email man021436@163.com
 * @Description: TODO
 */
@RestController
public class RabbitController {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMq")
    public String sendMessage(@RequestParam(value = "num",defaultValue = "10")Integer num){
        String message = "hello world";
        // 如果发送的消息是个对象，会使用序列化机制，将对象写出去，对象必须实现Serializable
        for (int i = 0; i < num; i++) {
            if (i%2==0){
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("qqqq-"+i);
                reasonEntity.setStatus(1);
                reasonEntity.setSort(1);
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java1", reasonEntity,new CorrelationData(UUID.randomUUID().toString()));
            }else {
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java1", message,new CorrelationData(UUID.randomUUID().toString()));
            }

            // log.info("消息发送完成{}",reasonEntity.toString());
        }
        return "ok";
    }

}
