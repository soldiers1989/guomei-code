package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 分期活动计价方式
* @author fanghj
 *
 */
public class ProgramFeeDef implements Serializable {

	private static final long serialVersionUID = 3499751696200335940L;

	/**
	 * 最小允许分期金额
	 */
	@PropertyInfo(name="最小允许分期金额", length=15, precision=2)
	public BigDecimal minAmount;
	
	/**
	 * 最大允许分期金额
	 */
	@PropertyInfo(name="最大允许分期金额", length=15, precision=2)
	public BigDecimal maxAmount;
	
	/**
	 * 商户手续费率
	 */
	@PropertyInfo(name="商户手续费率", length=7, precision=4)
	public BigDecimal merFeeRate;
	
	/**
	 * 分期手续费比例
	 */
	@PropertyInfo(name="分期手续费比例", length=7, precision=4)
	public BigDecimal feeRate;
	
	/**
	 * 分期手续费金额
	 */
	@PropertyInfo(name="分期手续费金额", length=15, precision=2)
	public BigDecimal feeAmount;
	
	/**
	 * 分期手续费计算方式
	 */
	@PropertyInfo(name="分期手续费计算方式", length=1)
	public CalcMethod loanFeeCalcMethod;
	
	/**
     * 分期手续费收取方式
     */
    @PropertyInfo(name="分期手续费收取方式", length=1)
    public LoanFeeMethod loanFeeMethod;
	
}
