package com.atguigu.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
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
@TableName("sms_coupon")
public class CouponEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private Integer couponType;
	/**
	 * 
	 */
	private String couponImg;
	/**
	 * 
	 */
	private String couponName;
	/**
	 * 
	 */
	private Integer num;
	/**
	 * 
	 */
	private BigDecimal amount;
	/**
	 * ÿ
	 */
	private Integer perLimit;
	/**
	 * ʹ
	 */
	private BigDecimal minPoint;
	/**
	 * 
	 */
	private Date startTime;
	/**
	 * 
	 */
	private Date endTime;
	/**
	 * ʹ
	 */
	private Integer useType;
	/**
	 * 
	 */
	private String note;
	/**
	 * 
	 */
	private Integer publishCount;
	/**
	 * 
	 */
	private Integer useCount;
	/**
	 * 
	 */
	private Integer receiveCount;
	/**
	 * 
	 */
	private Date enableStartTime;
	/**
	 * 
	 */
	private Date enableEndTime;
	/**
	 * 
	 */
	private String code;
	/**
	 * 
	 */
	private Integer memberLevel;
	/**
	 * 
	 */
	private Integer publish;

}
