package com.lyd.mall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author Liuyunda
 * @Date 2021/6/6 15:32
 * @Email man021436@163.com
 * @Description: TODO
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
