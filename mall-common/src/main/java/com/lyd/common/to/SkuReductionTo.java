package com.lyd.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/3/2 23:29
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducedPrice;
    private int priceStatus;
    private List<MemberPrice> memberPrices;
}
