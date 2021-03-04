package com.lyd.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/3/4 21:18
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
