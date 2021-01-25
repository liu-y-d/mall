package com.lyd.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author Liuyunda
 * @Date 2021/1/25 23:19
 * @Email man021436@163.com
 * @Description: DOTO
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {

    private Set<Integer> set = new HashSet<>();
    /**
     * @Description: 初始化方法
     * @Param: [constraintAnnotation]
     * @return: void
     * @Author: Liuyunda
     * @Date: 2021/1/25
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] values = constraintAnnotation.values();
        for (int value : values) {
            set.add(value);
        }
    }

    /**
     * @Description: 判断是否校验成功
     * @Param: [value, context]
     * @return: boolean
     * @Author: Liuyunda
     * @Date: 2021/1/25
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return set.contains(value);
    }
}
