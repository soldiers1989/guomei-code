package com.sunline.ccs.param.def;

import com.sunline.ark.support.meta.PropertyInfo;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 账单周期参数表
 */
public class Cycle implements Serializable {

	private static final long serialVersionUID = 5685691660457616667L;

	/**
     * 账单日（有效值：1-28）
     */
    @PropertyInfo(name="账单日", length=2)
    public Integer cycleDay;

    /**
     * 该账单日最大允许账户数
     */
    @PropertyInfo(name="最大允许账户数", length=9)
    public BigDecimal maxAccount;

    /**
     * 系统中该账单日当前账户数
     */
    @PropertyInfo(name="系统中该账单日当前账户数", length=9)
    public BigDecimal accountNo;
}
