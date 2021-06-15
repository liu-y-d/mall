package com.lyd.common.to.mq;

import lombok.Data;

/**
 * @Author Liuyunda
 * @Date 2021/6/15 22:06
 * @Email man021436@163.com
 * @Description: TODO
 */
@Data
public class StockDeatilTo {
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 1-已锁定  2-已解锁  3-扣减
     */
    private Integer lockStatus;

}
