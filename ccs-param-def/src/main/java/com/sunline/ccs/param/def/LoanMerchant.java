package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.param.def.enums.RecStatus;
import com.sunline.ark.support.meta.PropertyInfo;

public class LoanMerchant implements Serializable {

	private static final long serialVersionUID = -7046179508095276182L;

	/**
	 * 商户ID
	 */
	@PropertyInfo(name="商户ID", length=15)
    public String merId;
	
	/**
	 * 商户状态
	 */
	@PropertyInfo(name="商户状态", length=1)
    public RecStatus recStatus;
	
	/**
	 * 商户类型
	 */
	@PropertyInfo(name="商户类型(MCC)", length=4)
    public String merType;
	
	/**
	 * 商户名称
	 */
	@PropertyInfo(name="商户名称", length=80)
    public String merName;
	
	/**
	 * 商户省份
	 */
	@PropertyInfo(name="商户省份", length=40)
    public String merState;
	
	/**
	 * 商户城市
	 */
	@PropertyInfo(name="商户城市", length=40)
    public String merCity;
	
	/**
	 * 商户地址
	 */
	@PropertyInfo(name="商户地址", length=200)
    public String merAddr;
	
	/**
	 * 商户的邮政编码
	 */
	@PropertyInfo(name="商户的邮政编码", length=6)
    public String merPstlCd;
	
	/**
	 * 商户电话
	 */
	@PropertyInfo(name="商户电话", length=20)
    public String merPhone;
	
	/**
	 * 商户联系人
	 */
	@PropertyInfo(name="商户联系人", length=80)
    public String merLinkMan;
	
	/**
	 * 是否支持POS分期交易
	 */
	@PropertyInfo(name="支持商户分期", length=1)
    public Indicator posLoanSupportInd;
	
	/**
	 * 是否支持MOTO分期交易
	 */
	@PropertyInfo(name="支持moto电子渠道分期", length=1)
    public Indicator motoLoanSupportInd;
	
	/**
	 * 是否支持网上分期交易
	 */
	@PropertyInfo(name="支持moto电话渠道分期", length=1)
    public Indicator eBankLoanSupportInd;
	
	/**
	 * 是否支持大额分期交易
	 */
	@PropertyInfo(name="支持大额分期", length=1)
    public Indicator macroLoanSupportInd;
	
	/**
	 * 商户允许的单笔最小POS分期消费交易金额
	 */
	@PropertyInfo(name="POS分期最小金额", length=15, precision=2)
    public BigDecimal posLoanSingleAmtMin;
	
	/**
	 * 商户允许的单笔最大POS分期消费交易金额
	 */
	@PropertyInfo(name="POS分期最大金额", length=15, precision=2)
    public BigDecimal posLoanSingleAmtMax;
	
	/**
	 * 商户允许的单笔最小大额分期消费交易金额
	 */
	@PropertyInfo(name="大额分期最小金额", length=15, precision=2)
    public BigDecimal specLoanSingleAmtMin;
	
	/**
	 * 商户允许的单笔最大大额分期消费交易金额
	 */
	@PropertyInfo(name="大额分期最大金额", length=15, precision=2)
    public BigDecimal specLoanSingleAmtMax;
	
	/**
	 * 商户发生POS分期时的收单行收益
	 */
	@PropertyInfo(name="POS分期收单行收益比例", length=15, precision=4)
    public BigDecimal posFeeIssPerc;
	
	/**
	 * 商户发生POS分期时的发卡行收益
	 */
	@PropertyInfo(name="POS分期发卡行收益比例", length=15, precision=4)
    public BigDecimal posFeeAcqPerc;
	
	/**
	 * 商户发生大额分期时的收单行收益
	 */
	@PropertyInfo(name="大额分期收单行收益比例", length=15, precision=4)
    public BigDecimal macroFeeIssPerc;
	
	/**
	 * 商户发生大额分期时的发卡行收益
	 */
	@PropertyInfo(name="大额分期发卡行收益比例", length=15, precision=4)
    public BigDecimal macroFeeAcqPerc;
	
	/**
	 * 商户所属主管分行
	 */
	@PropertyInfo(name="所属主管分行", length=9)
    public String merBranche;
	
	/**
	 * 商户所属品牌
	 */
	@PropertyInfo(name="所属品牌", length=80)
    public String merBrand;
	
	/**
	 * 商户分组
	 */
	@PropertyInfo(name="商户分组", length=4)
    public String merGroup;
	
	/**
	 * 备注
	 */
	@PropertyInfo(name="备注", length=200)
    public String memo;

}
