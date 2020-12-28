package com.lyd.mall.product.dao;

import com.lyd.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-28 21:58:56
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
