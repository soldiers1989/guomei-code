package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.util.Map;

import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ark.support.meta.ReferEnums;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 信用计划模板参数
 */
public class PlanTemplate implements Serializable {

	private static final long serialVersionUID = -4258130560855171282L;

	/**
	 * 主信用计划模板号
	 * 此模板号为计划模板的主键，根据该值获取对应的计划模板
	 */
	@PropertyInfo(name="信用计划模板号", length=6)
	public String planNbr;

    /**
     * 计划类型
     */
    @PropertyInfo(name="计划类型", length=1)
    public PlanType planType;
    
    /**
     * 是否参与还款分配标志
     */
    @PropertyInfo(name="参与还款分配", length=1)
    public Boolean pmtAssignInd;

    /**
     * 计划描述
     */
    @PropertyInfo(name="描述", length=40)
    public String description;
    
    /**
     * 余额成份参数
     */
    @ReferEnums({BucketType.class})
    public Map<BucketType, BucketDef> intParameterBuckets;

    /**
     * 还款优先级
     */
    @PropertyInfo(name="还款优先级", length=3)
    public Integer pmtPriority;
    
    /**
     * XFR计划号
     */
    @PropertyInfo(name="XFR计划号", length=6)
    public String dualXfrPlanNbr;

    /**
     * 多交易共享计划标识
     * 每个交易是否建立单独的信用计划（用于分期的情况，如果按照个贷核算模式，每期计划要有单独的账务处理）：
     * Y/N
     */
    @PropertyInfo(name="交易计入相同计划", length=1)
    public Boolean multSaleInd;
    
    /**
     * 计划保存天数
     */
    @PropertyInfo(name="失效保留天数", length=3)
    public Integer planPurgeDays;
    /**
	 * 罚息计息用利率编号，引用 {@link InterestTable}
	 */
	@PropertyInfo(name="罚息利率表编号", length=6)
	public Integer intTableId;
	
	 /**
     * 是否累计本金基数
     */
    @PropertyInfo(name="是否累计本金基数", length=1)
    public Indicator isAccruPrinSum;
    
    //兜底产品
    /**
     * 代收罚息计息用利率表，引用 {@link InterestTable}
     */
    @PropertyInfo(name="代收罚息利率表编号",length=6)
    public Integer repPenaltyIntId;
}
