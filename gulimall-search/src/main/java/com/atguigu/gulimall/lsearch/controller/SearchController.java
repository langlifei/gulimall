package com.atguigu.gulimall.lsearch.controller;

import com.atguigu.gulimall.lsearch.service.MallSearchService;
import com.atguigu.gulimall.lsearch.vo.SearchParam;
import com.atguigu.gulimall.lsearch.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
