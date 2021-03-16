package com.lyd.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/3/4 22:29
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id;
    private List<PurchaseItemDoneVo> items;
}
