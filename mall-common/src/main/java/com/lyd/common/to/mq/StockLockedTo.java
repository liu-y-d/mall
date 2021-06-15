package com.lyd.common.to.mq;

import lombok.Data;

/**
 * @Author Liuyunda
 * @Date 2021/6/15 21:58
 * @Email man021436@163.com
 * @Description: TODO
 */
@Data
public class StockLockedTo {
    /**
     * 库存工作单id
     */
    private Long id;
    /**
     * 工作单详情
     */
    private StockDeatilTo detail;
}
