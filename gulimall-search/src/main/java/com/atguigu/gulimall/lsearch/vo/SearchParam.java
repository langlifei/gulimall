package com.atguigu.gulimall.lsearch.vo;


import lombok.Data;

import java.util.List;

@Data
public class  SearchParam {

    /**
     *  页面传递的查询参数
     *  catalog3Id=223&keyword=小米&sort=saleCount_asc&hasStock=0/1
     */

    private String keyword;     //页面传递过来的全文匹配关键字
    private Long catalog3Id;    //三级分类id

    /**
     * sort=saleCount_asc/desc 销量
     * sort=skuPrice_asc/desc  价格
     * sort=hotScore_asc/desc  综合
     */

    private String sort;    //排序条件

    /**
     * 过滤的条件有
     * hasStock(是否有货)，skuPrice(价格区间),brandId(品牌ID),attrs(三级分类对应的属性)
     * hasStock=0/1 1表示有货,0表示无货
     * skuPrice=1_500/_500/500_  表示1-500,0-500,500以上
     * brandId=1
     * attrs=2_5寸:6寸,表示catalog3Id下的2号属性选中5寸和6寸
     */

    private Integer hasStock = 1;  //是否只显示有货
    private String skuPrice;   //价格区间查询
    private List<Long> brandId; //按照品牌进行查询,可以多选
    private List<String> attrs; //按照属性进行筛选
    private Integer pageNum = 1 ;    //页码
}
