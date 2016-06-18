package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.BlackWhiteCode;
import com.sunline.ppy.dictionary.enums.Day;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ark.support.meta.ReferEnums;

public class TxnFilterRule implements Serializable{

	private static final long serialVersionUID = -5760971682023040818L;

	/**
	 * 交易币种
	 */
	@PropertyInfo(name="交易币种", length=3)
	public String CurrCd;
	
	/**
	 * 交易过滤规则-星期几有效
	 * 非空
	 */
	@ReferEnums({Day.class})
	public List<Day> days;
	
	/**
	 * 交易过滤规则-交易时间段列表
	 * null - 表示所有交易都符合
	 */
	//@PropertyInfo(name="交易时间段")
	public List<TimePeriod> timePeriods;
	
	/**
	 * 交易过滤规则-交易类型列表
	 * null - 表示所有交易都符合
	 */
	//@PropertyInfo(name="交易类型")
	public List<AuthTransType> supportTransTypes;
	
	/**
	 * 交易过滤规则-交易渠道列表
	 * null - 表示所有交易都符合
	 */
	//@PropertyInfo(name="交易渠道")
	public List<AuthTransTerminal> supportTerminals;
	
	/**
	 * 商户终端规则属于黑名单还是白名单
	 */
	@PropertyInfo(name="积分计划类型", length=1)
	public BlackWhiteCode listType;
	
	/**
	 * 交易过滤规则-商户终端号
	 * Map的key - bmp参数Mcc的主键
	 * Map的value - 终端编号，手工输入
	 * null - 表示所有交易都符合
	 */
	//@PropertyInfo(name="商户终端控制规则")
	public Map<String, List<String>> mccTerminals;
	
}
