package com.lyd.mall.product.controller;

import com.lyd.common.utils.PageUtils;
import com.lyd.common.utils.R;
import com.lyd.mall.product.entity.AttrEntity;
import com.lyd.mall.product.entity.AttrGroupEntity;
import com.lyd.mall.product.service.AttrAttrgroupRelationService;
import com.lyd.mall.product.service.AttrGroupService;
import com.lyd.mall.product.service.AttrService;
import com.lyd.mall.product.service.CategoryService;
import com.lyd.mall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author liuyunda
 * @email man021436@163.com
 * @date 2020-12-28 22:35:46
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    AttrAttrgroupRelationService relationService;
    /**
     * @Description: 新建关联关系
     * @Param: [vos]
     * @return: com.lyd.common.utils.R
     * @Author: Liuyunda
     * @Date: 2021/3/1
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos ){
        relationService.saveBath(vos);
        return R.ok();
    }

    /**
     * @Description: 获取分组关联的属性
     * @Param: [attrgroupId]
     * @return: com.lyd.common.utils.R
     * @Author: Liuyunda
     * @Date: 2021/3/1
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data",entities);
    }

    /**
     * @Description: 获取分组未关联的属性
     * @Param: [attrgroupId]
     * @return: com.lyd.common.utils.R
     * @Author: Liuyunda
     * @Date: 2021/3/1
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrUnRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params){
        PageUtils page = attrService.getUnRelationAttr(params,attrgroupId);
        return R.ok().put("page",page);
    }
    /**
     * @Description: 批量删除关联属性
     * @Param: [vos]
     * @return: com.lyd.common.utils.R
     * @Author: Liuyunda
     * @Date: 2021/3/1
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos){
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catelogId") Long catelogId) {
        // PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
