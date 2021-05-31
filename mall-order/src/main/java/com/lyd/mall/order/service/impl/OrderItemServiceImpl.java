package com.lyd.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.mall.order.dao.OrderItemDao;
import com.lyd.mall.order.entity.OrderItemEntity;
import com.lyd.mall.order.entity.OrderReturnReasonEntity;
import com.lyd.mall.order.service.OrderItemService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

import java.util.Map;

// @RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * @Description: 监听队列
     * 1.原生详细类型Message：消息头消息体
     * 2.T 发送的消息的类型
     * 3.Channel 当前传输数据的通道
     *
     * Queue:可以很多人都来监听。只要收到消息，队列删除消息。而且只能有一个收到此消息
     *      场景
     *          1.订单服务启动多个:同一个消息，只能有一个客户端收到
     *          2.只有一个消息完全处理完，方法运行结束，就可以接收到下一个消息
     *
     * RabbitListener注解：类+方法上（监听哪些队列）
     * RabbitHandler注解：方法上(重载区分不同的消息)
     *
     * @Param: []
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/5/31
     */
    // @RabbitListener(queues = {"hello-java-queue"})
    // @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity context, Channel channel){
        // System.out.println("接收到消息，内容："+message+",类型："+message.getClass());
        System.out.println("接收到消息，内容："+context);
        // try { TimeUnit.SECONDS.sleep(3); }catch (InterruptedException e) { e.printStackTrace(); }
        // System.out.println("消息处理完成");
    }

    // @RabbitHandler
    public void receiveMessage2(String context){
        // System.out.println("接收到消息，内容："+message+",类型："+message.getClass());
        System.out.println("接收到消息，内容："+context);
        // try { TimeUnit.SECONDS.sleep(3); }catch (InterruptedException e) { e.printStackTrace(); }
        // System.out.println("消息处理完成");
    }
}