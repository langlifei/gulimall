package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author zengzhuo
 * @email zengzhuo@gmail.com
 * @date 2020-10-08 20:38:36
 */
@Data
@TableName("pms_attr_attrgroup_relation")
public class AttrAttrgroupRelationEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private Long attrId;
	/**
	 * 
	 */
	private Long attrGroupId;
	/**
	 * 
	 */
	private Integer attrSort;

}
