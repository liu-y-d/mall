package com.lyd.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.common.vo.MemberResponseVo;
import com.lyd.mall.order.dao.OrderDao;
import com.lyd.mall.order.entity.OrderEntity;
import com.lyd.mall.order.feign.CartFeignService;
import com.lyd.mall.order.feign.MemberFeignService;
import com.lyd.mall.order.interceptor.LoginUserInterceptor;
import com.lyd.mall.order.service.OrderService;
import com.lyd.mall.order.vo.MemberAddressVo;
import com.lyd.mall.order.vo.OrderConfirmVo;
import com.lyd.mall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        //1.远程查询所有的收货地址列表
        List<MemberAddressVo> receiveAddress = memberFeignService.getReceiveAddress(memberResponseVo.getId());
        confirmVo.setAddressVos(receiveAddress);
        //2.远程查询购物车所有选中的购物项
        List<OrderItemVo> cartItems = cartFeignService.getCurrentCartItems();
        confirmVo.setItems(cartItems);
        //3.查询用户的积分
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);
        // todo 防重令牌
        return confirmVo;
    }

}