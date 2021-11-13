package com.atguigu.gulimall.lsearch.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        SearchResult result = buildSearchResult(searchResponse,searchParam);
        return result;
    }

    /**
     * 构建查询结果供前端页面进行展示
     * @param searchResponse ES查询的检索结果
     * @return 结果数据
     */
    private SearchResult buildSearchResult(SearchResponse searchResponse,SearchParam searchParam) {
        SearchResult searchResult = new SearchResult();
        SearchHits hits = searchResponse.getHits();
        //TODO 1.获取所有命中的商品
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        for (SearchHit hit : hits.getHits()) {
            //将命中的商品转换为字符串
            String sourceAsString = hit.getSourceAsString();
            SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
            if(StringUtils.isNotEmpty(searchParam.getKeyword())){
                String skuTitle = hit.getHighlightFields().get("skuTitle").getFragments()[0].string();
                skuEsModel.setSkuTitle(skuTitle);
            }
            skuEsModels.add(skuEsModel);
        }
        searchResult.setProducts(skuEsModels);
        //TODO 2.获取当前页码信息
        searchResult.setPageNum(searchParam.getPageNum());
        //TODO 3.获取总记录数
        searchResult.setTotal(hits.getTotalHits().value);
        //TODO 4.获取总页码
        searchResult.setTotalPages((int) (searchResult.getTotal() % searchParam.getPageNum() == 0 ? searchResult.getTotal() / searchParam.getPageNum() : (searchResult.getTotal() / searchParam.getPageNum()) + 1));
        Aggregations aggregations = searchResponse.getAggregations();
        //TODO 5.获取品牌的聚合信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = aggregations.get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //获取品牌的ID
            Long brandId = Long.parseLong(bucket.getKeyAsString());
            //获取品牌的名字
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            //获取品牌的图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        searchResult.setBrands(brandVos);
        //TODO 6.获取属性的聚合信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = aggregations.get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //获取属性ID
            attrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            //获取属性名
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            //获取属性值
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(item -> {
                return item.getKeyAsString();
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        searchResult.setAttrs(attrVos);
        //TODO 7.获取分类的聚合信息
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //分类ID
            catalogVo.setCatalogId(Long.parseLong(bucket.getKeyAsString()));
            //分类名字
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVos);
        return searchResult;
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
        searchSourceBuilder.query(boolQuery);
        //TODO 2.排序, 分页, 高亮
        //2.1 排序 sort=saleCount_asc/desc 销量
        if(StringUtils.isNotEmpty(searchParam.getSort())){

            String[] split = searchParam.getSort().split("_");
            //排序字段
            String sortKey = split[0];
            //排序规则
            SortOrder sortRule = split[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(sortKey,sortRule);
        }

        //2.2分页
        searchSourceBuilder.from((searchParam.getPageNum()-1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
       //2.3高亮
        if(StringUtils.isNotEmpty(searchParam.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        //TODO 3.聚合分析
        //3.1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(20);
        //3.1.1 品牌子聚合    1个品牌ID 聚合出一个品牌名以及品牌logo
        TermsAggregationBuilder brand_name_agg = AggregationBuilders.terms("brand_name_agg").field("brandName").size(1);
        TermsAggregationBuilder brand_img_agg = AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1);
        brand_agg.subAggregation(brand_name_agg);
        brand_agg.subAggregation(brand_img_agg);
        searchSourceBuilder.aggregation(brand_agg);
        //3.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        TermsAggregationBuilder catalog_name_agg = AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1);
        catalog_agg.subAggregation(catalog_name_agg);
        searchSourceBuilder.aggregation(catalog_agg);
        //3.3 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(20);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(20));
        attr_agg.subAggregation(attr_id_agg);
        searchSourceBuilder.aggregation(attr_agg);
        String s = searchSourceBuilder.toString();
        System.out.println(s);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},  searchSourceBuilder);
        return searchRequest;
    }
}
