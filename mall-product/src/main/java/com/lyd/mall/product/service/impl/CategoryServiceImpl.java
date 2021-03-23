package com.lyd.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.mall.product.dao.CategoryDao;
import com.lyd.mall.product.entity.CategoryEntity;
import com.lyd.mall.product.service.CategoryBrandRelationService;
import com.lyd.mall.product.service.CategoryService;
import com.lyd.mall.product.vo.CateLog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

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
        return level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> categoryIds) {
        // TODO 1.检查当前删除的菜单，是否被别的地方引用
        // 逻辑删除
        baseMapper.deleteBatchIds(categoryIds);
    }

    /**
     * @Description: 根据当前catelogid想上查找父节点路径
     * @Param: [catelogId]
     * @return: java.lang.Long[]
     * @Author: Liuyunda
     * @Date: 2021/1/26
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * @Description: 级联更新所有关联数据
     * @Param: [category]
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/2/1
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
       this.updateById(category);
       categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Override
    public Map<String, List<CateLog2Vo>> getCatalogJson() {
        // 查出所有1级分类
        List<CategoryEntity> level1Categorys = getLevel1Categorys();
        Map<String, List<CateLog2Vo>> listMap = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 每一个一级分类
            List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            List<CateLog2Vo> cateLog2Vos = null;

            if (categoryEntities != null) {
                cateLog2Vos = categoryEntities.stream().map(l2 -> {
                    CateLog2Vo cateLog2Vo = new CateLog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 找三级分类
                    List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
                    if (entities != null){
                        List<CateLog2Vo.Catalog3Vo> collect = entities.stream().map(l3 -> {
                            CateLog2Vo.Catalog3Vo catalog3Vo = new CateLog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        cateLog2Vo.setCatalog3List(collect);
                    }
                    return cateLog2Vo;
                }).collect(Collectors.toList());

            }

            return cateLog2Vos;
        }));
        return listMap;
    }

    /**
     * @Description: 递归找分组id的父节点
     * @Param: [catelogId, paths]
     * @return: java.util.List<java.lang.Long>
     * @Author: Liuyunda
     * @Date: 2021/1/26
     */
    private List<Long> findParentPath (Long catelogId,List<Long> paths) {
        // 1.收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
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