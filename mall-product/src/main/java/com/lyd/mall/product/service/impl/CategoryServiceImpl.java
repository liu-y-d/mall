package com.lyd.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.mall.product.dao.CategoryDao;
import com.lyd.mall.product.entity.CategoryEntity;
import com.lyd.mall.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2。组装成树形结构

        // 2.1 找到所有一级分类
        List<CategoryEntity> level1Menu = entities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((menu)->{
                    menu.setChildren(getChildrens(menu,entities));
                    return menu;
                }).sorted((menuBefore,menuAfter)->{
                    return (menuBefore.getSort()==null?0:menuBefore.getSort())-(menuAfter.getSort()==null?0:menuAfter.getSort());
                }).collect(Collectors.toList());
        return entities;
    }

    @Override
    public void removeMenuByIds(List<Long> categoryIds) {
        // TODO 1.检查当前删除的菜单，是否被别的地方引用
        // 逻辑删除
        baseMapper.deleteBatchIds(categoryIds);
    }

    /**
     * @Description: 递归查找所有菜单的子菜单
     * @Param: [root, all]
     * @return: java.util.List<com.lyd.mall.product.entity.CategoryEntity>
     * @Author: Liuyunda
     * @Date: 2021/1/11
     */
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            // 找子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menuBefore,menuAfter)->{
            // 排序
            return (menuBefore.getSort()==null?0:menuBefore.getSort())-(menuAfter.getSort()==null?0:menuAfter.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}