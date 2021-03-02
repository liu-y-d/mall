package com.lyd.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author Liuyunda
 * @Date 2021/3/2 23:16
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
