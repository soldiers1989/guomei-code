package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ccs.param.def.enums.CtrlListInd;
import com.sunline.ccs.param.def.enums.ProgramStatus;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 分期活动参数
 */
public class Program implements Serializable {
	
	private static final long serialVersionUID = -4244204850817989286L;

	/**
     * 分期产品ID
     */
    @PropertyInfo(name="分期产品ID", length=4)
	public String loanPlanId;
	
    /**
     * 分期活动编号 
     */
    @PropertyInfo(name="分期活动编号 ", length=30)
	public String programId;
    
    /**
     * 分期活动描述
     */
    @PropertyInfo(name="分期活动描述 ", length=40)
	public String programDesc;
    
    /**
     * 活动生效日期
     */
    @PropertyInfo(name="活动生效日期", length=8)
    public Date programStartDate;
    
    /**
     * 活动终止日期
     */
    @PropertyInfo(name="活动终止日期", length=8)
    public Date programEndDate;
    
    /**
     * 活动状态
     */
    @PropertyInfo(name="活动状态", length=1)
    public ProgramStatus programStatus;
    
    /**
	 * 活动参与最小金额
	 */
	@PropertyInfo(name="分期交易最小金额", length=15, precision=2)
	public BigDecimal programMinAmount;
	
	/**
	 * 活动参与最大金额
	 */
	@PropertyInfo(name="分期交易最大金额", length=15, precision=2)
	public BigDecimal programMaxAmount;
	
	/**
     * 是否允许附卡参与
     */
    @PropertyInfo(name="是否允许附卡参与", length=1)
    public Indicator programSuppInd;
	
	 /**
     * 活动所属分行
     */
    @PropertyInfo(name="活动所属分行", length=9)
    public String programBranch;
	
    /**
     * 分行列表支持属性
     */
    @PropertyInfo(name="分行列表支持属性", length=1)
    public CtrlListInd ctrlBranchInd;
    
    /**
     * 分行列表
     */
    public List<String> ctrlBranchList;
    
    /**
     * 卡产品列表支持属性
     */
    @PropertyInfo(name="卡产品列表支持属性", length=1)
    public CtrlListInd ctrlProdCreditInd;
    
    /**
     * 卡产品列表
     */
    public List<String> ctrlProdCreditList;
    
    /**
     * 活动参与商户列表
     */
    public List<String> programMerList;
    
    /**
     * 分期计划计价方式列表
     * Key - 分期期数
     * Value - 计价方式
     */
    public Map<Integer, ProgramFeeDef> programFeeDef;
    
    /**
     * 分期类型
     */
    @PropertyInfo(name="分期类型", length=1)
    public LoanType loanType;
    
    /**
     * MCC列表支持属性
     */
    @PropertyInfo(name="MCC列表支持属性", length=1)
    public CtrlListInd ctrlMccInd;
    
    /**
     * MCC列表
     */
    public List<String> ctrlMccList;
}
