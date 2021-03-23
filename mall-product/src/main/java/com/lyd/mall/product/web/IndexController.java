package com.lyd.mall.product.web;

import com.lyd.mall.product.entity.CategoryEntity;
import com.lyd.mall.product.service.CategoryService;
import com.lyd.mall.product.vo.CateLog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @Author Liuyunda
 * @Date 2021/3/22 23:27
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;
    @GetMapping({"/","/index.html"})
    public String indexPage(Model model) {
        // TODO 1.一级分类
        List<CategoryEntity> categoryEntities =  categoryService.getLevel1Categorys();

        // 视图解析器拼串->classpath:/template/+返回值+。html
        model.addAttribute("categorys",categoryEntities);
        return "index";
    }


    //index/json/catalog.json
    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    public Map<String, Object> getCatalogJson() {
        Map<String, List<CateLog2Vo>> map = categoryService.getCatalogJson();
        return null;
    }
}
