package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ccs.param.def.enums.FirstCardFeeInd;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 杂项费用
 */
public class Fee implements Serializable {
	
	private static final long serialVersionUID = 3209901533187601598L;
    
    /**
     * 描述
     */
    @PropertyInfo(name="描述", length=40)
    public String description;

    /**
     * 入会费
     */
    @PropertyInfo(name="入会费", length=15, precision=2)
    public BigDecimal joiningFee;
    //马上贷，原主卡年费
    /**
     *  随借随还收取年费
     */
    @PropertyInfo(name=" 随借随还收取年费", length=15, precision=2)
    public BigDecimal primCardFee;

    /**
     * 附卡年费
     */
    @PropertyInfo(name="附卡年费", length=15, precision=2)
    public BigDecimal suppCardFee;

    /**
     * 首次年费收取方式
     * I - 发卡收年费
     * S - 首次账单收年费
     * A - 激活收年费
     * T - 首次交易收年费
     */
    @PropertyInfo(name="首次年费收取方式", length=1)
    public FirstCardFeeInd firstCardFeeInd;
    
    /**
     * 首次年费是否减免
     */
    @PropertyInfo(name="首次年费减免", length=1)
    public Boolean firstCardFeeWaiveInd;

    /**
     * 加急费
     */
    @PropertyInfo(name="加急费", length=15, precision=2)
    public BigDecimal urgentFee;

    /**
     * 余额不足罚金（用于约定扣款失败）
     */
    @PropertyInfo(name="余额不足罚金", length=15, precision=2)
    public BigDecimal nsfFee;

    /**
     * 信函固定收费
     */
    @PropertyInfo(name="信函固定收费", length=15, precision=2)
    public BigDecimal letterFee;

    
    /**
     * 免除补打账单费月数
     * 
     * */
    @PropertyInfo(name="免除补打账单费月数", length=2)
    public Integer waiveMonthReprintStmt;

    /**
     * 短信费
     */
    @PropertyInfo(name="短信费", length=15, precision=2)
    public BigDecimal smsFee;

    /**
     * 自定义费用1
     */
    @PropertyInfo(name="自定义费用1", length=15, precision=2)
    public BigDecimal userFee1;

    /**
     * 自定义费用2
     */
    @PropertyInfo(name="自定义费用2", length=15, precision=2)
    public BigDecimal userFee2;

    /**
     * 自定义费用3
     */
    @PropertyInfo(name="自定义费用3", length=15, precision=2)
    public BigDecimal userFee3;

    /**
     * 自定义费用4
     */
    @PropertyInfo(name="自定义费用4", length=15, precision=2)
    public BigDecimal userFee4;

    /**
     * 自定义费用5
     */
    @PropertyInfo(name="自定义费用5", length=15, precision=2)
    public BigDecimal userFee5;

    /**
     * 自定义费用6
     */
    @PropertyInfo(name="自定义费用6", length=15, precision=2)
    public BigDecimal userFee6;
}
