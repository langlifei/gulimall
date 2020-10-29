package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.common.valid.ListValue;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDao spuInfoDao;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService imagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * //TODO 高级部分完善
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);



        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(),images);


        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);


        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }


        //5、保存当前spu对应的所有sku信息；

        List<Skus> skus = vo.getSkus();
        if(skus!=null && skus.size()>0){
            skus.forEach(item->{
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                //    private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1）、sku的基本信息；pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2）、sku的图片信息；pms_sku_image
                skuImagesService.saveBatch(imagesEntities);
                //TODO 没有图片路径的无需保存

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // //5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() >0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }



            });
        }






    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        // status=1 and (id=1 or spu_name like xxx)
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        /**
         * status: 2
         * key:
         * brandId: 9
         * catelogId: 225
         */

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        //TODO 1.查出当前spuId对应的所有sku信息,品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        //TODO 1.1根据spuId查出当前所有可以被检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> set = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item->{
            return set .contains(item.getAttrId());
        }).map(item->{
            SkuEsModel.Attrs attr = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item,attr);
            return attr;
        }).collect(Collectors.toList());
        //TODO 1.2根据skus获取到库存信息,组装成<key,value>
        //获取skuIds
        List<Long> skuIds = skus.stream().map(sku -> sku.getSkuId()).collect(Collectors.toList());
        //获取库存信息,发送远程调用,从库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            R vos = wareFeignService.getSkuHasStock(skuIds);
            stockMap = vos.getData(new TypeReference<List<SkuHasStockVo>>(){}).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        }catch (Exception e){
            log.error("库存服务查询异常:原因{}",e);
        }
        //TODO 2.封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku->{
            //TODO 2.1组装需要的数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            //TODO 2.2通过发送远程调用,从库存系统查询是否有库存
            if(finalStockMap != null){
                Boolean hasStock = finalStockMap.get(sku.getSkuId());
                esModel.setHasStock(hasStock!=null?hasStock:false);
            }else{
                esModel.setHasStock(true);
            }
            //TODO 2.3热度评分,默认为0
            esModel.setHotScore(0L);
            //TODO 2.4查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());
            //设置检索属性
            esModel.setAttrs(attrsList);
            //TODO 2.5查询当前spuid对应的所有sku信息,品牌的名字.
            return esModel;
        }).collect(Collectors.toList());
        //TODO 3.将数据发送给es进行保存;gulimall-search
        R r = null;
        try {
            r = searchFeignService.productStatusUp(upProducts);
        }catch (Exception e){
            log.error("ES商品上架错误:{}",e);
        }
        //商品上架成功
        if(r!=null && r.getCode() == 0){
            //TODO 4.改变商品上架的状态
            spuInfoDao.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else{
            //远程调用失败
            //TODO 5.重复调用,实现接口幂等性
        }
    }

}