package com.lyd.common.exception;

/**
 * @Author Liuyunda
 * @Date 2021/6/8 22:53
 * @Email man021436@163.com
 * @Description: TODO
 */
public class NoStockException extends RuntimeException{
    private Long skuId;
    public NoStockException(){
        super("没有足够的库存了");
    }
    public NoStockException(Long skuId){
        super("商品："+skuId+"，没有足够的库存了");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
