package com.atguigu.gulimall.coupon.entity;

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
 * @date 2020-10-08 21:00:54
 */
@Data
@TableName("sms_home_adv")
public class HomeAdvEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private String name;
	/**
	 * ͼƬ
	 */
	private String pic;
	/**
	 * 
	 */
	private Date startTime;
	/**
	 * 
	 */
	private Date endTime;
	/**
	 * ״̬
	 */
	private Integer status;
	/**
	 * 
	 */
	private Integer clickCount;
	/**
	 * 
	 */
	private String url;
	/**
	 * 
	 */
	private String note;
	/**
	 * 
	 */
	private Integer sort;
	/**
	 * 
	 */
	private Long publisherId;
	/**
	 * 
	 */
	private Long authId;

}
