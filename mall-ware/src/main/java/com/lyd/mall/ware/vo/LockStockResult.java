package com.lyd.mall.ware.vo;

import lombok.Data;

/**
 * @Author Liuyunda
 * @Date 2021/6/8 22:20
 * @Email man021436@163.com
 * @Description: TODO
 */
@Data
public class LockStockResult {

    private Long skuId;
    private Integer num;
    private Boolean locked;
}
