package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author zengzhuo
 * @email zengzhuo@gmail.com
 * @date 2020-10-08 21:11:07
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
