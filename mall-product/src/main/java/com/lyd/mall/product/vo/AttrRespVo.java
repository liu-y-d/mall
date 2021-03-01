package com.lyd.mall.product.vo;

import lombok.Data;

/**
 * @Author Liuyunda
 * @Date 2021/2/2 20:21
 * @Email man021436@163.com
 * @Description: DOTO
 */

@Data
public class AttrRespVo extends AttrVo{
    /**
     * 所属分类名字
     */
    private String catelogName;

    /**
     * 所属分组名字
     */
    private String groupName;

    private Long[] catelogPath;
}
