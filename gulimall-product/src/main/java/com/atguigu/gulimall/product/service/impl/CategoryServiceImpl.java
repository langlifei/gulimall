package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired(required = false)
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
             categoryEntity.getParentCid() == 0
        ).map((menu)->{
            menu.setChildren(getChildrens(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());




        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categories() {
        List<CategoryEntity> parent_cid = categoryDao.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return parent_cid;
    }



    //TODO 采用lettuce作为redis客户端将产生OutOfDirectMemoryError错误。
    @Override
    @Cacheable(value = "category",key = "'cateJson'")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        System.out.println("调用了......");
        return getCatalogJsonWithRedisson();
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonWithRedisson() {
        RLock lock = redisson.getLock("cateJson-Lock");
        lock.lock();
        try {
            //双重检验....
            String cateJson = redisTemplate.opsForValue().get("cateJson");
            if(StringUtils.isEmpty(cateJson)) {
                System.out.println("查询了数据库.......");
                //查询数据库
                Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDB();
                return catalogJsonFromDB;
            }
            return JSON.parseObject(cateJson,new TypeReference<Map<String, List<Catalog2Vo>>>(){});
        }finally {
            lock.unlock();
        }
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonWithRedisDistLock() {
        //为防止删除其他人的锁，为每个进程锁设置一个唯一值
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
        if(lock){
           try {
               System.out.println("查询了数据库.......");
               //查询数据库
               Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDB();
               //序列化为Json格式
               String value = JSON.toJSONString(catalogJsonFromDB);
               //存入缓存
               redisTemplate.opsForValue().set("cateJson",value);
               return catalogJsonFromDB;
           }finally {
               //解锁.....由于解锁过程中可能会出现中断，所以查询自身的锁是否存在以及删除锁应该是原子操作，故使用lua脚本进行解锁
               String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
               Long flag = redisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class), Arrays.asList("lock"), uuid);
           }
        }else{
            //重试，开始自旋重新获取数据.....
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                System.out.println("休眠被打断....");
            }
            return getCatalogJson();
        }
    }


    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDB() {
        List<CategoryEntity> totalList = categoryDao.selectList(null);;
        List<CategoryEntity> level1Categories = getParent_cid(totalList,0l);
        Map<String,List<Catalog2Vo>> map = null;
        if(level1Categories != null){
            map = level1Categories.stream().collect(Collectors.toMap(k->k.getCatId().toString(),v->{
                List<CategoryEntity> catalogEntities = getParent_cid(totalList,v.getCatId());
                List<Catalog2Vo> catalog2Vos = null;
                if(catalogEntities!=null){
                    catalog2Vos = catalogEntities.stream().map(l2->{
                        Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(),null,l2.getCatId().toString(),l2.getName());
                        List<CategoryEntity> level3Catalog = getParent_cid(totalList,l2.getCatId());
                        List<Catalog2Vo.Catalog3Vo> collect = null;
                        if(level3Catalog!=null){
                            collect = level3Catalog.stream().map(l3->{
                                Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(),l3.getCatId().toString(),l3.getName());
                                return catalog3Vo;
                            }).collect(Collectors.toList());
                        }
                        catalog2Vo.setCatalog3List(collect);
                        return catalog2Vo;
                    }).collect(Collectors.toList());
                    return catalog2Vos;
                }
                return null;
            }));
        }
        return map;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> totalList,long parent_cid) {
        List<CategoryEntity> collect = totalList.stream().filter(iter ->
            iter.getParentCid() == parent_cid
        ).collect(Collectors.toList());
        return collect;
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;

    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //2、菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }



}