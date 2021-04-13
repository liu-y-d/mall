package com.lyd.mall.search.service;

import com.lyd.mall.search.vo.SearchParam;
import com.lyd.mall.search.vo.SearchResult;

/**
 * @Author Liuyunda
 * @Date 2021/4/8 21:28
 * @Email man021436@163.com
 * @Description: TODO
 */
public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
