package com.atguigu.gulimall.order.entity;

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
 * @date 2020-10-08 21:11:07
 */
@Data
@TableName("oms_order")
public class OrderEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * member_id
	 */
	private Long memberId;
	/**
	 * 
	 */
	private String orderSn;
	/**
	 * ʹ
	 */
	private Long couponId;
	/**
	 * create_time
	 */
	private Date createTime;
	/**
	 * 
	 */
	private String memberUsername;
	/**
	 * 
	 */
	private BigDecimal totalAmount;
	/**
	 * Ӧ
	 */
	private BigDecimal payAmount;
	/**
	 * 
	 */
	private BigDecimal freightAmount;
	/**
	 * 
	 */
	private BigDecimal promotionAmount;
	/**
	 * 
	 */
	private BigDecimal integrationAmount;
	/**
	 * 
	 */
	private BigDecimal couponAmount;
	/**
	 * 
	 */
	private BigDecimal discountAmount;
	/**
	 * ֧
	 */
	private Integer payType;
	/**
	 * 
	 */
	private Integer sourceType;
	/**
	 * 
	 */
	private Integer status;
	/**
	 * 
	 */
	private String deliveryCompany;
	/**
	 * 
	 */
	private String deliverySn;
	/**
	 * 
	 */
	private Integer autoConfirmDay;
	/**
	 * 
	 */
	private Integer integration;
	/**
	 * 
	 */
	private Integer growth;
	/**
	 * 
	 */
	private Integer billType;
	/**
	 * 
	 */
	private String billHeader;
	/**
	 * 
	 */
	private String billContent;
	/**
	 * 
	 */
	private String billReceiverPhone;
	/**
	 * 
	 */
	private String billReceiverEmail;
	/**
	 * 
	 */
	private String receiverName;
	/**
	 * 
	 */
	private String receiverPhone;
	/**
	 * 
	 */
	private String receiverPostCode;
	/**
	 * ʡ
	 */
	private String receiverProvince;
	/**
	 * 
	 */
	private String receiverCity;
	/**
	 * 
	 */
	private String receiverRegion;
	/**
	 * 
	 */
	private String receiverDetailAddress;
	/**
	 * 
	 */
	private String note;
	/**
	 * ȷ
	 */
	private Integer confirmStatus;
	/**
	 * ɾ
	 */
	private Integer deleteStatus;
	/**
	 * 
	 */
	private Integer useIntegration;
	/**
	 * ֧
	 */
	private Date paymentTime;
	/**
	 * 
	 */
	private Date deliveryTime;
	/**
	 * ȷ
	 */
	private Date receiveTime;
	/**
	 * 
	 */
	private Date commentTime;
	/**
	 * 
	 */
	private Date modifyTime;

}
