package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.ccs.param.def.enums.IntAccumFrom;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 余额成份账务处理参数
* @author fanghj
 *
 */
public class BucketDef implements Serializable{

	private static final long serialVersionUID = 7340284531060868774L;

	/**
	 * 计息用利率编号，引用 {@link InterestTable}
	 */
	@PropertyInfo(name="利率表编号", length=6)
	public Integer intTableId;
	
	/**
	 * 起息日类型
	 */
	@PropertyInfo(name="起息日类型", length=1)
	public IntAccumFrom intAccumFrom;
	
	/**
	 * 是否享受免息期
	 */
	@PropertyInfo(name="享受免息期", length=1)
	public Boolean intWaive;
	
	/**
	 * 是否计入全额应还款金额
	 */
	@PropertyInfo(name="计入全部应还款额", length=1)
	public Boolean graceQualify;
	
	/**
	 * 是否参与超限计算
	 */
	@PropertyInfo(name="参与超限计算", length=1)
	public Boolean overlimitQualify;
	
	/**
	 * 最小还款额计算比例
	 */
	@PropertyInfo(name="最小还款额计算比例", length=7, precision=4)
	public Map<BnpPeriod, BigDecimal> minPaymentRates = new HashMap<BnpPeriod, BigDecimal>();
	
	
}
