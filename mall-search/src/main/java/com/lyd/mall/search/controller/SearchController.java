package com.lyd.mall.search.controller;

import com.lyd.mall.search.service.MallSearchService;
import com.lyd.mall.search.vo.SearchParam;
import com.lyd.mall.search.vo.SearchResult;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author Liuyunda
 * @Date 2021/4/7 22:39
 * @Email man021436@163.com
 * @Description: TODO
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model){
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result",result);
        return "list";
    }
}
