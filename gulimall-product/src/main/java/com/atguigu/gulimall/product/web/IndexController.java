package com.atguigu.gulimall.product.web;


import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        List<CategoryEntity> category = categoryService.getLevel1Categories();
        model.addAttribute("categorys",category);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String,List<Catalog2Vo>> getCatalogJson(){
        Map<String,List<Catalog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }
}
