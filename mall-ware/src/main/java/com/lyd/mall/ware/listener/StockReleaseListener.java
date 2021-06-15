package com.lyd.mall.ware.listener;

/**
 * @Author Liuyunda
 * @Date 2021/6/15 23:49
 * @Email man021436@163.com
 * @Description: TODO
 */

import com.lyd.common.to.mq.StockLockedTo;
import com.lyd.mall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;
    /**
     * @Description: 库存自动解锁
     *       下订单成功，库存锁定成功，但是接下来的业务调用失败，导致订单回滚，之前锁定的库存就要自动解锁
     *       订单失败，本身是因为锁库存失败
     *
     *       只要解锁库存的消息失败，一定要告诉服务失败
     * @Param: [to, message]
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/6/15
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息");
        try{
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }



    }
}
