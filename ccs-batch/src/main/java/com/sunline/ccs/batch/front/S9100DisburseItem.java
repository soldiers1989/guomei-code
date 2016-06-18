package com.sunline.ccs.batch.front;

import java.math.BigDecimal;

import com.sunline.ark.support.cstruct.CChar;

/**
 * 批量代付接口
 * @author MengXiang
 *
 */
public class S9100DisburseItem {
	
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
	 * 卡号/折号
	 */
	@CChar(value = 25, order = 300)
	public String cardNo;
	
	/**
	 * 持卡人姓名
	 */
	@CChar(value = 60, order = 400)
	public String name;
	
	/**
	 * 开户行
	 */
	@CChar(value = 50, order = 500)
	public String openBank;
	
	/**
	 * 开户行省份
	 */
	@CChar(value = 20, order = 600)
	public String province;
	
	/**
	 * 开户行城市
	 */
	@CChar(value = 40, order = 700)
	public String city;
	
	/**
	 * 开户支行
	 */
	@CChar(value = 80, order = 800)
	public String subBank;
	
	/**
	 * 交易金额
	 */
	@CChar(value = 12, pointSupported = true, order = 900)
	public BigDecimal txnAmt;
	
	/**
	 * 用途
	 */
	@CChar(value = 25, order = 1000)
	public String purpose;
	
	/**
	 * 付款标志
	 */
	@CChar(value = 2, order = 1100)
	public String flag;
	
}
