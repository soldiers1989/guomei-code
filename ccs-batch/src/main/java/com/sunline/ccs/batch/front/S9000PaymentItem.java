package com.sunline.ccs.batch.front;

import java.math.BigDecimal;

import com.sunline.ark.support.cstruct.CChar;

/**
 * 批量代扣接口
 * @author zhangqiang
 *
 */
public class S9000PaymentItem {
	
	/**
	 * 渠道流水号
	 */
	@CChar(value = 16, order = 100)
	public Long channelSeq;
	
	/**
	 * 渠道日期
	 */
	@CChar(value = 8, order = 200)
	public String channelDateStr;
	
	/**
	 * 开户行代号
	 */
	@CChar(value = 4, order = 300)
	public String bankId;
	
	/**
	 * 卡折标识
	 */
	@CChar(value = 1, order = 400)
	public String cardType;
	
	/**
	 * 卡号/折号
	 */
	@CChar(value = 25, order = 500)
	public String cardNo;
	
	/**
	 * 持卡人姓名
	 */
	@CChar(value = 40, order = 600)
	public String name;
	
	/**
	 * 证件类型
	 */
	@CChar(value = 2, order = 700)
	public String idType;
	
	/**
	 * 证件号
	 */
	@CChar(value = 25, order = 800)
	public String idNo;
	
	/**
	 * 交易金额
	 */
	@CChar(value = 13, pointSupported = true, order = 900)
	public BigDecimal txnAmt;
	
	/**
	 * 用途
	 */
	@CChar(value = 25, order = 1000)
	public String purpose;
	
	/**
	 * 私有域
	 */
	@CChar(value = 60, order = 1100)
	public String privateField;
	
	/**
	 * 开户行省份
	 */
	@CChar(value = 20, order = 1200)
	public String province;
	
	/**
	 * 开户行城市
	 */
	@CChar(value = 20, order = 1300)
	public String city;
}
