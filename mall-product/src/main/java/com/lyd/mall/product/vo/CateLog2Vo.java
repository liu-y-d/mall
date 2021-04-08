package com.lyd.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author Liuyunda
 * @Date 2021/3/23 21:02
 * @Email man021436@163.com
 * @Description: DOTO
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CateLog2Vo {
    private String catalog1Id;
    private List<Catalog3Vo> catalog3List;
    private String id;
    private String name;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catalog3Vo{
        private String catalog2Id;
        private String id;
        private String name;
    }
}
