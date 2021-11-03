package com.atguigu.gulimall.lsearch.service;

import com.atguigu.gulimall.lsearch.vo.SearchParam;
import com.atguigu.gulimall.lsearch.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
