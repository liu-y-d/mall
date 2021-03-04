package com.lyd.mall.ware.vo;

import lombok.Data;

/**
 * @Author Liuyunda
 * @Date 2021/3/4 22:31
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
