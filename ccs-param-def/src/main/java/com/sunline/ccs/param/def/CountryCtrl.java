package com.sunline.ccs.param.def;

import com.sunline.ark.support.meta.PropertyInfo;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 国家授权
 */
public class CountryCtrl implements Serializable{

	private static final long serialVersionUID = 6828401848956690268L;

	/**
     * 国家代码
     */
    @PropertyInfo(name="国家代码", length=3)
    public String countryCode;
   
    /**
     * 是否允许此国家
     */
    @PropertyInfo(name="允许此国家", length=1)
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
