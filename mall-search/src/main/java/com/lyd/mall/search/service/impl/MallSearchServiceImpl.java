package com.lyd.mall.search.service.impl;

import com.lyd.mall.search.service.MallSearchService;
import com.lyd.mall.search.vo.SearchParam;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.stereotype.Service;

/**
 * @Author Liuyunda
 * @Date 2021/4/8 21:29
 * @Email man021436@163.com
 * @Description: TODO
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {
    /**
     * @Description: 检索的所有参数，返回所有的结果
     * @Param: [searchParam]
     * @return: java.lang.Object
     * @Author: Liuyunda
     * @Date: 2021/4/8
     */
    @Override
    public SearchResponse search(SearchParam searchParam) {

        return null;
    }
}
