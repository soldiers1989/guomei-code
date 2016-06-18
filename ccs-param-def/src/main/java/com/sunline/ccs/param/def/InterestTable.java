package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.util.List;

import com.sunline.ccs.param.def.enums.TierInd;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 利率表，被 {@link PlanTemplate}按ID引用
 */
public class InterestTable implements Serializable {

	private static final long serialVersionUID = -5859826109972302412L;

	/**
     * 主键
     */
    @PropertyInfo(name="本金罚息利率表", length = 6)
    public Integer intTableId;

    /**
     * 描述
     */
    @PropertyInfo(name="描述", length=40)
    public String description;

    /**
     * 计息基准年
     * 360/365/366
     */
    @PropertyInfo(name="计息基准年", length=3)
    public Integer baseYear;

    /**
     * 利息累计计算方式：
     * F - 使用全部金额（full） 
     * T - 采用分段金额（tier）
     */
    @PropertyInfo(name="利息累计计算方式", length=1)
    public TierInd tierInd;

    public List<RateDef> chargeRates;
}
