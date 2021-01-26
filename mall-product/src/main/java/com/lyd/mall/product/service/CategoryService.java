package com.lyd.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyd.common.utils.PageUtils;
import com.lyd.mall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-28 21:58:56
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> categoryIds);

    Long[] findCatelogPath(Long catelogId);
}

