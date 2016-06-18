package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ccs.param.def.enums.CalcBaseInd;
import com.sunline.ccs.param.def.enums.TierInd;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 滞纳金参数
 */
public class LatePaymentCharge implements Serializable {

	private static final long serialVersionUID = -540071244404305602L;
	
    /**
     * 滞纳金收取日期，在到期还款日后多少天
     */
    @PropertyInfo(name="延后收取天数", length=2)
    public Integer assessDays;

    /**
     * 滞纳金计算基准金额指示：
     * T - 用最小还款额剩余部分（total due）
     * L - 用上期最小还款额剩余部分（last due）
     * C - 对当期due收滞纳金
     */
    @PropertyInfo(name="计算基准金额", length=1)
    public CalcBaseInd calcBaseInd;

    /**
     * 收取滞纳金最小账龄，小于该账龄不收取
     */
    @PropertyInfo(name="触发最小账龄", length=1)
    public String minAgeCd;

    /**
     * 收取指示，在持卡人账龄下降或者保持不变的情况下是否还要收取滞纳金：
     * Y/N
     */
    @PropertyInfo(name="账龄下降或者保持不变时仍然收取滞纳金", length=1)
    public Boolean assessInd;

    /**
     * 滞纳金免收金额,最小还款额未还部分少于该金额,免收滞纳金
     */
    @PropertyInfo(name="免收最大金额", length=15, precision=2)
    public BigDecimal threshold;

    /**
     * 滞纳金单笔最小金额
     */
    @PropertyInfo(name="单笔最小金额", length=15, precision=2)
    public BigDecimal minCharge;
    
    /**
     * 滞纳金单笔最大金额
     */
    @PropertyInfo(name="单笔最大金额", length=15, precision=2)
    public BigDecimal maxCharge;
    
    public List<RateDef> chargeRates;

    /**
     * 计算方式：
     * F - 利用全部金额计算
     * T - 分段计算
     */
    @PropertyInfo(name="计算方式", length=1)
    public TierInd tierInd;

    /**
     * 年度累计收取滞纳金最大值
     */
    @PropertyInfo(name="年累计最大金额", length=15, precision=2)
    public BigDecimal yearMaxCharge;

    /**
     * 年度收取滞纳金最大次数
     */
    @PropertyInfo(name="年累计最大次数", length=2)
    public Integer yearMaxCnt;
    /**
     * 原名：延迟扣款是否滞纳金回溯
     * 修改时间：2015.11.05 15：05  chenpy
     */
    @PropertyInfo(name="扣款延迟入账是否回溯滞纳金", length=1)
    public Indicator isReviewLateFee;
}
