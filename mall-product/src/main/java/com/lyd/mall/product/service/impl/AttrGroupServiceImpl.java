package com.lyd.mall.product.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.Query;

import com.lyd.mall.product.dao.AttrGroupDao;
import com.lyd.mall.product.entity.AttrGroupEntity;
import com.lyd.mall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        if (catelogId==0){
            // 查询所有
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),new QueryWrapper<AttrGroupEntity>());
            return new PageUtils(page);
        }else{
            // 按照三级分类查
            String key = (String) params.get("key");
            // select * from pms_attr_group where catelog_id = ? and (attr_group_id = key or attr_group_name like %key% )
            QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id",catelogId);
            if (!StringUtils.isEmpty(key)){
                wrapper.and((obj)->{
                    obj.eq("attr_group_id",key).or().like("attr_group_name",key);
                });
            }
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),wrapper);
            return new PageUtils(page);
        }
    }

}