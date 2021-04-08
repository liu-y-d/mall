package com.lyd.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

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
    // @Caching(evict = {
    //         @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
    //         @CacheEvict(value = "category",key = "'getCatalogJson'")
    // })
    @CacheEvict(value = "category",allEntries = true)
    @CachePut // 双写模式
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

        // 同时修改缓存中的数据
        // 删除缓存中的数据，等待下次主动查询进行更新
    }

    @Cacheable(value = "category",key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map<String, List<CateLog2Vo>> getCatalogJson() {
        /**
         * 1.将多次查询数据库改为查询一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        Map<String, List<CateLog2Vo>> listMap = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 每一个一级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            List<CateLog2Vo> cateLog2Vos = null;

            if (categoryEntities != null) {
                cateLog2Vos = categoryEntities.stream().map(l2 -> {
                    CateLog2Vo cateLog2Vo = new CateLog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 找三级分类
                    List<CategoryEntity> entities = getParent_cid(selectList, l2.getCatId());
                    if (entities != null) {
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

    // @Override
    public Map<String, List<CateLog2Vo>> getCatalogJson2() {
        /**
         * 1.空结果缓存，解决缓存穿透问题
         * 2.设置过期时间（加随机值）：解决缓存雪崩
         * 3.加锁，解决缓存击穿问题
         */
        // 加入缓存
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)){
            // 缓存中没有
            Map<String, List<CateLog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();

            return catalogJsonFromDb;
        }
        // 转为对象
        Map<String, List<CateLog2Vo>> result = JSON.parseObject(catalogJson,new TypeReference<Map<String, List<CateLog2Vo>>>(){});
        return result;
    }
    // 从数据库查询并封装分类数据(本地锁)
    public Map<String, List<CateLog2Vo>> getCatalogJsonFromDbWithLocalLock() {
        // 本地锁，锁当前进程，在分布式情况下必须使用分布式锁
        synchronized (this) {
            // 得到锁以后再去缓存中确定一次
            return getDataFromDb();
        }

    }

    /**
     * @Description: 缓存里面的数据如何和数据库保持一致
     * 缓存数据一致性问题
     * 1.双写模式：同时修改缓存中的数据
     * 2.失效模式：删除缓存中的数据，等待下次主动查询进行更新
     * @Param: []
     * @return: java.util.Map<java.lang.String,java.util.List<com.lyd.mall.product.vo.CateLog2Vo>>
     * @Author: Liuyunda
     * @Date: 2021/4/1
     */
    public Map<String, List<CateLog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        RLock catalogJsonLock = redissonClient.getLock("catalogJsonLock");
        catalogJsonLock.lock();
        Map<String, List<CateLog2Vo>> dataFromDb;

        // 加锁成功...执行业务
        try {
            dataFromDb = getDataFromDb();
        }finally {
            catalogJsonLock.unlock();
        }
        return dataFromDb;

    }

    public Map<String, List<CateLog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        // 1.占分布式锁。去redis占坑
        // 设置过期时间，必须和加锁是同步的，原子的
        String token = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", token,300,TimeUnit.SECONDS);
        Map<String, List<CateLog2Vo>> dataFromDb;
        if (lock){
            // 加锁成功...执行业务
            try {
                dataFromDb = getDataFromDb();
            }finally {
                // 获取值对比+对比成功删除 原子操作
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                // 删除锁
                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock")
                        , token);
            }

            return dataFromDb;
        }else {
            // 加锁失败...重试。synchronized()
            // 自旋
            // 休眠100ms
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();

        }
    }

    private Map<String, List<CateLog2Vo>> getDataFromDb() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isNotEmpty(catalogJson)) {
            // 转为对象
            Map<String, List<CateLog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<CateLog2Vo>>>() {
            });
            return result;
        }
        /**
         * 1.将多次查询数据库改为查询一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        Map<String, List<CateLog2Vo>> listMap = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 每一个一级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            List<CateLog2Vo> cateLog2Vos = null;

            if (categoryEntities != null) {
                cateLog2Vos = categoryEntities.stream().map(l2 -> {
                    CateLog2Vo cateLog2Vo = new CateLog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 找三级分类
                    List<CategoryEntity> entities = getParent_cid(selectList, l2.getCatId());
                    if (entities != null) {
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

        // 将查到的数据放到缓存,将对象转为json
        String s = JSON.toJSONString(listMap);
        stringRedisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
        return listMap;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> {
            return item.getParentCid().equals(parentCid);
        }).collect(Collectors.toList());
        return collect;
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