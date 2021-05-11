package com.lyd.mall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.mall.product.dao.AttrGroupDao;
import com.lyd.mall.product.dao.SkuSaleAttrValueDao;
import com.lyd.mall.product.entity.BrandEntity;
import com.lyd.mall.product.service.BrandService;
import com.lyd.mall.product.vo.SkuItemSaleAttrVo;
import com.lyd.mall.product.vo.SpuItemAttrGroupVo;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.UUID;

@SpringBootTest
class MallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;


    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test111() {
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(14L);
        System.out.println(saleAttrsBySpuId);

    }

    @Test
    public void test() {
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(14L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);

    }


    @Test
    public void testRedisson() {
        System.out.println(redissonClient);

    }

    @Test
    public void testRedis() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 保存
        ops.set("hello","world_"+ UUID.randomUUID().toString());
        // 查询
        String hello = ops.get("hello");
        System.out.println("之前保存的数据："+hello);

    }
    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        // brandEntity.setName("华为");
        // brandService.save(brandEntity);
        // System.out.println("保存成功.....");
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("华为牛鼻");
        brandService.updateById(brandEntity);
        BrandEntity byId = brandService.getById(1L);
        System.out.println(byId);
        List<BrandEntity> brand_id = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        brand_id.forEach((item)->{
            System.out.println(item);
        });
    }

}
