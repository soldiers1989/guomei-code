package com.sunline.ccs.param.def;

import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ark.support.meta.PropertyInfo;
import java.io.Serializable;

/**
 * 系统产生交易代码与入账交易代码对应
 */
public class SysTxnCdMapping implements Serializable {

	private static final long serialVersionUID = -8600680279271421622L;

	/**
     * 系统产生交易代码
     */
    @PropertyInfo(name="系统交易码", length=3)
    public SysTxnCd sysTxnCd;

    /**
     * 入账交易代码
     */
    @PropertyInfo(name="入账交易代码", length=4)
    public String txnCd;
}
