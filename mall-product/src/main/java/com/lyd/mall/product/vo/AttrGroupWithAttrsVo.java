package com.lyd.mall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.lyd.mall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/3/2 20:55
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Data
public class AttrGroupWithAttrsVo {
    /**
     * 分组id
     */
    @TableId
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;
    private Integer valueType;

    private List<AttrEntity> attrs;
}
