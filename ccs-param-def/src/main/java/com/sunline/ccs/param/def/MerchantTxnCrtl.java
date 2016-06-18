package com.sunline.ccs.param.def;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ark.support.meta.PropertyInfo;
import java.io.Serializable;

/**
 * 特殊交易商户控制表
 */
public class MerchantTxnCrtl implements Serializable {

	private static final long serialVersionUID = -3891637558262815293L;

	/**
	 * 商户编号
	 */
	@PropertyInfo(name = "商户编号", length = 15)
	public String merchantId;

	/**
	 * 商户名称
	 */
	@PropertyInfo(name = "商户名称", length = 255)
	public String merchantName;
	/**
	 * 国家代码
	 */
	@PropertyInfo(name = "国家代码", length = 3)
	public String countryCd;
	/**
	 * 收单机构
	 */
	@PropertyInfo(name = "收单机构", length = 30)
	public String acqInstitution;
	/**
	 * 商户简称
	 */
	@PropertyInfo(name = "简称", length = 255)
	public String merchantShortName;

	/**
	 * 商户等级
	 */
	@PropertyInfo(name = "等级", length = 2)
	public String merchantClass;

	/**
	 * 商户类别代码
	 */
	@PropertyInfo(name = "MCC", length = 4)
	public String mcc;
	
	/**
	 * 商户邮编
	 */
	@PropertyInfo(name = "邮政编码", length = 6)
	public String merchantPostcode;
	
	/**
	 * 商户地址
	 */
	@PropertyInfo(name = "地址", length = 255)
	public String merchantAddr;
	
	/**
	 * 商户电话
	 */
	@PropertyInfo(name = "电话", length = 19)
	public String merchantTel;
	
	/**
	 * 商户联系人
	 */
	@PropertyInfo(name = "联系人", length = 255)
	public String merchantContact;
	
	/**
	 * 是否已激活
	 */
	@PropertyInfo(name = "已激活", length = 1)
	public Indicator activateInd;
	
	/**
	 * 是否支持MOTO消费交易
	 */
	@PropertyInfo(name = "支持MOTO消费交易", length = 1)
	public Indicator supportMotoPosInd;
	
	/**
	 * 行内MOTO交易强制验证CVV2
	 * AIC2.7银联升级-修改描述为行内
	 */
	@PropertyInfo(name = "行内MOTO交易强制验证CVV2", length = 1)
	public Indicator forceMotoRetailCvv2Ind;

	/**
	 * 是否支持电子类消费交易
	 */
	@PropertyInfo(name = "支持电子类消费交易", length = 1)
	public Indicator supportEmotoInd;
	
	/**
	 * 是否支持分期消费交易
	 */
	@PropertyInfo(name = "支持分期消费交易", length = 1)
	public Indicator supportLoanInd;
	
	/**
	 * 是否支持大额分期消费交易
	 */
	@PropertyInfo(name = "支持大额分期消费交易", length = 1)
	public Indicator supportSpecloan;
}
