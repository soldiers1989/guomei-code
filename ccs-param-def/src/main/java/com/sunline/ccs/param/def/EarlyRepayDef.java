package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ark.support.meta.PropertyInfo;

public class EarlyRepayDef implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提前还款列表-当前期数最大值
     */
    @PropertyInfo(name = "提前还款列表-当前期数最大值", length = 3)
    public Integer adCurPeriod;

    /**
     * 提前还款手续费金额
     */
    @PropertyInfo(name = "提前还款手续费金额", length = 15, precision = 2)
    public BigDecimal adFeeAmt;

    /**
     * 提前还款手续费比例
     */
    @PropertyInfo(name = "提前还款手续费比例", length = 6, precision = 4)
    public BigDecimal adFeeScale;

}
