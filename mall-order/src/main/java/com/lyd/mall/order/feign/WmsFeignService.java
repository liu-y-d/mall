package com.lyd.mall.order.feign;

import com.lyd.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/6/3 22:30
 * @Email man021436@163.com
 * @Description: TODO
 */
@FeignClient("mall-ware")
public interface WmsFeignService {
    @PostMapping("/ware/waresku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
