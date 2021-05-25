package com.lyd.mallcart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/5/25 20:19
 * @Email man021436@163.com
 * @Description: 购物车
 */
public class Cart {
    private List<CartItem> items;
    private Integer countNum;
    private Integer countType;
    private BigDecimal totalAmount;
    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (items!=null&& items.size()>0){
            for (CartItem item : items) {
                count+= item.getCount();
            }
        }
        return count;
    }


    public Integer getCountType() {
        return items.size();
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amout = new BigDecimal("0");
        if (items!=null&& items.size()>0){
            for (CartItem item : items) {
                amout = amout.add(item.getTotalPrice());
            }
        }
        return amout.subtract(getReduce());
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
