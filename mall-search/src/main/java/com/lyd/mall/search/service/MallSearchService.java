package com.lyd.mall.search.service;

import com.lyd.mall.search.vo.SearchParam;
import org.elasticsearch.action.search.SearchResponse;

/**
 * @Author Liuyunda
 * @Date 2021/4/8 21:28
 * @Email man021436@163.com
 * @Description: TODO
 */
public interface MallSearchService {
    SearchResponse search(SearchParam searchParam);
}
