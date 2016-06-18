package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 币种控制
 */
public class CurrencyCtrl implements Serializable{

	private static final long serialVersionUID = -8843073861400189185L;

	/**
	 * 币种数字代码
	 */
	@PropertyInfo(name="币种数字代码", length=3)
	public String currencyCd;

    /**
     * 兑换汇率
     */
    @PropertyInfo(name="兑换汇率", length=9, precision=4)
    public BigDecimal conversionRt;

    /**
     * 是否允许此币种
     */
    @PropertyInfo(name="允许此币种", length=1)
    public Boolean validInd;
    
    /**
     * 本币账户单笔限额
     */
    @PropertyInfo(name="本币账户单笔限额", length=15, precision=2)
    public BigDecimal maxTxnAmtLcl;

    /**
     * 外币账户单笔限额
     */
    @PropertyInfo(name="外币账户单笔限额", length=15, precision=2)
    public BigDecimal maxTxnAmtFrg;
}
