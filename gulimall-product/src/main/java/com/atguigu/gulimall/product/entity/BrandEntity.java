package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Ʒ
 * 
 * @author zengzhuo
 * @email zengzhuo@gmail.com
 * @date 2020-10-08 20:38:36
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Ʒ
	 */
	@TableId
	private Long brandId;
	/**
	 * Ʒ
	 */
	private String name;
	/**
	 * Ʒ
	 */
	private String logo;
	/**
	 * 
	 */
	private String descript;
	/**
	 * 
	 */
	private Integer showStatus;
	/**
	 * 
	 */
	private String firstLetter;
	/**
	 * 
	 */
	private Integer sort;

}
