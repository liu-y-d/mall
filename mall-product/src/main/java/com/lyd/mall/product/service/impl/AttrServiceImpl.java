package com.lyd.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.constant.ProductConstant;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;
import com.lyd.mall.product.dao.AttrAttrgroupRelationDao;
import com.lyd.mall.product.dao.AttrDao;
import com.lyd.mall.product.dao.AttrGroupDao;
import com.lyd.mall.product.dao.CategoryDao;
import com.lyd.mall.product.entity.AttrAttrgroupRelationEntity;
import com.lyd.mall.product.entity.AttrEntity;
import com.lyd.mall.product.entity.AttrGroupEntity;
import com.lyd.mall.product.entity.CategoryEntity;
import com.lyd.mall.product.service.AttrService;
import com.lyd.mall.product.service.CategoryService;
import com.lyd.mall.product.vo.AttrGroupRelationVo;
import com.lyd.mall.product.vo.AttrRespVo;
import com.lyd.mall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrgroupRelationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        // 1、保存基本数据
        this.save(attrEntity);
        // 2、保存关联关系
        if (attr.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()&&attr.getAttrGroupId() != null){
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            attrgroupRelationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type","base".equalsIgnoreCase(type)?ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if (catelogId != 0){
            wrapper.eq("catelog_id",catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and((wrapper1)->{
                wrapper1.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> attrRespVos = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            // 设置分类和分组的名字
            if ("base".equalsIgnoreCase(type)){
                AttrAttrgroupRelationEntity attr_id = attrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attr_id != null && attr_id.getAttrGroupId()!=null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attr_id.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(attrRespVos);
        return pageUtils;
    }

    @Cacheable(value = "attr",key = "'attrInfo:'+#root.args[0]")
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,attrRespVo);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            // 1、设置分组信息
            AttrAttrgroupRelationEntity attrgroupRelationEntity = attrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (!StringUtils.isEmpty(attrgroupRelationEntity)){
                attrRespVo.setAttrGroupId(attrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupRelationEntity.getAttrGroupId());
                if (!StringUtils.isEmpty(attrGroupEntity)){
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        // 2、设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (!StringUtils.isEmpty(categoryEntity)){
            attrRespVo.setCatelogName(categoryEntity.getName());
        }
        attrRespVo.setCatelogPath(catelogPath);
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){
            // 1、修改分组关联

            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());

            Integer count = attrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count>0){
                attrgroupRelationDao.update(attrAttrgroupRelationEntity,new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attr.getAttrId()));
            } else {
                attrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }
    }

    /**
     * @Description: 根据分组id查找关联的所有基本属性
     * @Param: [attrgroupId]
     * @return: java.util.List<com.lyd.mall.product.entity.AttrEntity>
     * @Author: Liuyunda
     * @Date: 2021/3/1
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities = attrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> attrIds = entities.stream().map((attr) -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        if (attrIds==null||attrIds.size()==0){
            return null;
        }
        List<AttrEntity> attrEntities = this.listByIds(attrIds);
        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        // attrgroupRelationDao.delete(new QueryWrapper<>().eq("attr_id",1L).eq("attr_group_id",1L));
        // 批量删除
        List<AttrAttrgroupRelationEntity> collect = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        attrgroupRelationDao.deleteBatchRelation(collect);
    }

    /**
     * @Description: 获取当前分组没有关联的所有属性
     * @Param: [params, attrgroupId]
     * @return: com.lyd.common.utils.PageUtils
     * @Author: Liuyunda
     * @Date: 2021/3/1
     */
    @Override
    public PageUtils getUnRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 1.当前分组只能关联自己所属分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 2.当前分组只能关联别的分组没有引用的属性
        // 3.当前分类下的其他分组，然后在找到这些分组关联的属性，然后从当前分类的所有属性中移除这些属性
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = groupEntities.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());
        List<AttrAttrgroupRelationEntity> entities = attrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attrIds = entities.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds!=null&&attrIds.size()>0){
            attrEntityQueryWrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            attrEntityQueryWrapper.and((w)->{
                w.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), attrEntityQueryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

    /**
     * @Description: 在指定的所有属性集合里挑出检索属性
     * @Param: [attrIds]
     * @return: java.util.List<java.lang.Long>
     * @Author: Liuyunda
     * @Date: 2021/3/17
     */
    @Override
    public List<Long> selectSearchAttrs(List<Long> attrIds) {

        return this.baseMapper.selectSearchAttrs(attrIds);
    }

}