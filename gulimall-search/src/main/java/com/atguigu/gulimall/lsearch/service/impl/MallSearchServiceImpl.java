package com.atguigu.gulimall.lsearch.service.impl;

import com.atguigu.gulimall.lsearch.conf.GulimallElasticSearchConfig;
import com.atguigu.gulimall.lsearch.constant.EsConstant;
import com.atguigu.gulimall.lsearch.service.MallSearchService;
import com.atguigu.gulimall.lsearch.vo.SearchParam;
import com.atguigu.gulimall.lsearch.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Qualifier("esClient")
    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam searchParam) {
        //TODO 1.动态构建出DSL语句
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        //TODO 2.执行DSL查询语句
        SearchResponse searchResponse = null;
        try {
            searchResponse =  client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO 3.组装成返回结果
        SearchResult result = buildSearchResult(searchResponse);
        return null;
    }

    /**
     * 构建查询结果供前端页面进行展示
     * @param searchResponse ES查询的检索结果
     * @return 结果数据
     */
    private SearchResult buildSearchResult(SearchResponse searchResponse) {
        return null;
    }

    /**
     * 构建DSL查询语句
     * @param searchParam 查询参数
     * @return DSL查询语句
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        //TODO 模糊匹配, 过滤(按照属性, 分类, 品牌, 价格区间, 库存) , 排序, 分页, 高亮, 聚合分析
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //TODO 1.模糊匹配, 过滤(按照属性, 分类, 品牌, 价格区间, 库存)
        //1.1模糊匹配
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            MatchQueryBuilder skuTitle = QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword());
            boolQuery.must(skuTitle);
        }
        //1.2.1 分类过滤
        if(searchParam.getCatalog3Id() != null){
            TermQueryBuilder catalogId = QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id());
            boolQuery.filter(catalogId);
        }
        //1.2.2 品牌过滤
        if(searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0){
            TermsQueryBuilder brandIds = QueryBuilders.termsQuery("brandId", searchParam.getBrandId());
            boolQuery.filter(brandIds);
        }
        //1.2.3 属性过滤 attrs=2_5寸:6寸,表示catalog3Id下的2号属性选中5寸和6寸
        if(searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0){
            for(String attr: searchParam.getAttrs()){
                String[] split = attr.split("_");
                BoolQueryBuilder attrBoolQuery = QueryBuilders.boolQuery();
                attrBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                attrBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",split[1].split(":")));
                NestedQueryBuilder attrNestedQuery = QueryBuilders.nestedQuery("attrs",attrBoolQuery, ScoreMode.None);
                boolQuery.filter(attrNestedQuery);
            }
        }
        //1.2.4 库存过滤
        boolQuery.filter(QueryBuilders.termQuery("hasStock",searchParam.getHasStock() == 1));
        //1.2.5 价格区间过滤  skuPrice=1_500/_500/500_  表示1-500,0-500,500以上
        if(!StringUtils.isEmpty(searchParam.getSkuPrice())){
            String[] split = searchParam.getSkuPrice().split("_");
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            if(split.length == 1){
                //只有起始价格
                if(searchParam.getSkuPrice().endsWith("_")){
                    skuPrice.gte(split[0]);
                }else{
                    skuPrice.lte(split[0]);
                }

            }else if(split.length ==  2){
                skuPrice.gte(split[0]);
                skuPrice.lte(split[1]);
            }
            boolQuery.filter(skuPrice);
        }
        //TODO 2.排序, 分页, 高亮
        //TODO 3.聚合分析
        searchSourceBuilder.query(boolQuery);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},  searchSourceBuilder);
        return searchRequest;
    }
}
