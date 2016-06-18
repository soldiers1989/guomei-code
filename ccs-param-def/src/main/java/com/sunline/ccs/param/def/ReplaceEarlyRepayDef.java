package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ark.support.meta.PropertyInfo;

public class ReplaceEarlyRepayDef implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 代收提前还款列表-当前期数最大值
     */
    @PropertyInfo(name = "代收提前还款列表-当前期数最大值", length = 3)
    public Integer replaceAdCurPeriod;

    /**
     * 代收提前还款手续费金额
     */
    @PropertyInfo(name = "代收提前还款手续费金额", length = 15, precision = 2)
    public BigDecimal replaceAdFeeAmt;

    /**
     * 代收提前还款手续费比例
     */
    @PropertyInfo(name = "代收提前还款手续费比例", length = 6, precision = 4)
    public BigDecimal replaceAdFeeScale;

}
