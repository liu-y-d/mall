package com.lyd.mall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @Author Liuyunda
 * @Date 2021/5/31 21:32
 * @Email man021436@163.com
 * @Description: TODO
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }


    /**
     * @Description: 定制RabbitTemplate
     * MyRabbitConfig对象创建完成以后，执行这个方法
     * 发送端
     *      1.服务器收到消息就回调
     *          1.spring.rabbitmq.publisher-confirms=true
     *          2.设置确认回调ConfirmCallback
     *      2.消息正确抵达队列回调
     *          1.spring.rabbitmq.publisher-returns=true
     *          2.spring.rabbitmq.template.mandatory=true(只要抵达队列，以异步的方式优先回调我们这个returnConfirm)
     * 消费端确认（保证每一个消息被正确消费，此时才可以broker删除这个消息）
     *
     * @Param: []
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/5/31
     */
    @PostConstruct
    public void initRabbitTemplate(){
        // 设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @Description:只要消息抵达broker，ack就返回true
             * @Param: [correlationData 当前消息的唯一关联数据（消息的唯一id）, ack 代表消息是否成功收到, cause 原因]
             * @return: void
             * @Author: Liuyunda
             * @Date: 2021/5/31
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("correlationData:"+correlationData);
                System.out.println("ack:"+ack);
                System.out.println("cause:"+cause);
            }
        });
        // 设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * @Description: 只要休息奥没有投递给指定的队列，就触发这个失败回调
             * @Param: [message 投递失败的消息详细信息, replyCode 回复的状态码, replyText 回复的文本内容, exchange 当时这个消息发给哪个交换机, routingKey 当时这个消息用哪个路由键]
             * @return: void
             * @Author: Liuyunda
             * @Date: 2021/5/31
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("message:"+message);
                System.out.println("replyCode:"+replyCode);
                System.out.println("replyText:"+replyText);
                System.out.println("exchange:"+exchange);
                System.out.println("routingKey:"+routingKey);
            }
        });
    }

}
