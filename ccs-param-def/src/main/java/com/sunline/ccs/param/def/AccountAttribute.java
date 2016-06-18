package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.param.def.enums.AgePmtHierInd;
import com.sunline.ccs.param.def.enums.CycleBaseInd;
import com.sunline.ccs.param.def.enums.DelqDayInd;
import com.sunline.ccs.param.def.enums.DelqTolInd;
import com.sunline.ccs.param.def.enums.DirectDbIndicator;
import com.sunline.ccs.param.def.enums.DownpmtTolInd;
import com.sunline.ccs.param.def.enums.PaymentDueDay;
import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ark.support.meta.ReferEnums;

/**
 * 账户处理属性
 * 主键-accountAttributeId
 */
public class AccountAttribute implements Serializable{

	private static final long serialVersionUID = 2290878920078688409L;

	/**
	 * 账户属性ID
	 */
	@PropertyInfo(name="账户参数标识", length=8)
	public Integer accountAttributeId;
	
	/**
	 * 账户类型
	 */
	@PropertyInfo(name="账户类型", length=1)
	public AccountType accountType;
	
    /**
     * 账户描述
     */
    @PropertyInfo(name="产品描述", length=40)
    public String acctDescription;


	/**
	 * 到期还款日类型
	 */
	@PropertyInfo(name="到期还款日类型", length=1)
    public PaymentDueDay paymentDueDay;

    /**
     * 到期还款日天数：（对于每月固定日期的情况 ）
     */
    @PropertyInfo(name="到期还款日天数", length=2)
    public Integer pmtDueDays;

	/**
     * 到期还款固定日
     * 01 - 28 ： 固定日期 
     * 99 ： 月末
     */
    @PropertyInfo(name="到期还款固定日", length=2)
    public Integer pmtDueDate;
    
    /**
     * 大小月统一到期还款日
     * 启用后不管大月小月，到期还款日期都将维持在同一天
     */
    @PropertyInfo(name="大小月统一到期还款日", length=1)
    public Boolean pmtDueDayFix;
    
    /**
     * 大小月统一月天数
     * 手工输入，输入30则每月按30天计算
     */
    @PropertyInfo(name="大小月统一月天数", length=2)
    public Integer pmtDueDayFixUnit;
    
    /**
     * 到期还款短信/信函提前天数
     */
    @PropertyInfo(name="到期还款提醒提前天数",length=100)
    public String pmtDueLtrPrd;


    /**
     * 到期还款宽限天数
     */
    @PropertyInfo(name="到期还款宽限天数", length=2)
    public Integer pmtGracePrd;

    /**
     * 全额还款容忍度标识：
     */
    @PropertyInfo(name="全额还款容忍度标识", length=1)
    public DownpmtTolInd downpmtTolInd;

	/**
     * 全额还款容差比例
     */
    @PropertyInfo(name="全额还款容差比例", length=7, precision=4)
    public BigDecimal downpmtTolPerc;
    
    /**
     * 全额还款容差金额
     * 如果存放比例值，该值为允许少还的比例，如0.01允许少还1%
     */
    @PropertyInfo(name="全额还款容差金额", length=15, precision=2)
    public BigDecimal downpmtTol;
    
    /**
     * 约定还款日标识
     */
    @PropertyInfo(name="约定还款日标识", length=1)
    public DirectDbIndicator directDbInd;
    
    /**
     * 约定还款提前天数
     */
    @PropertyInfo(name="约定还款提前天数", length=2)
    public Integer directDbDays;
    
    /**
     * 约定还款固定日
     */
    @PropertyInfo(name="约定还款固定日", length=2)
    public Integer directDbDate;

    /**
     * 拖欠处理日标识
     */
    @PropertyInfo(name="拖欠处理日", length=1)
    public DelqDayInd delqDayInd;

    /**
     * 拖欠处理容忍度标志
     */
    @PropertyInfo(name="拖欠容忍计算方式", length=1)
    public DelqTolInd delqTolInd;

    /**
     * 拖欠处理容忍度金额
     */
    @PropertyInfo(name="拖欠处理容忍度金额", length=15, precision=2)
    public BigDecimal delqTol;

    /**
     * 拖欠容忍度比例
     */
    @PropertyInfo(name="拖欠容忍度比例",  length=7, precision=4)
    public BigDecimal delqTolPerc;

    /**
     * 拖欠短信/信函产生标识天数（拖欠之后第多少天产生）
     * 00 - 98 ： 实际天数
     * 99 ： 下个账单日产生
     */
    @PropertyInfo(name="拖欠通知延期天数", length=2)
    public Integer delqLtrPrd;

    /**
     * 是否连续拖欠都输出信函
     */
    @PropertyInfo(name="连续拖欠输出信函", length=1)
    public Boolean ltrOnContDlq;

    /**
     * 账单周期类型：
     */
    @PropertyInfo(name="账单周期类型", length=1)
    public CycleBaseInd cycleBaseInd;
    
    /**
     * 账单合并标识
     * 用法举例： CYCLE_BASE_IND为'M'，
     * CYCLE_BASE_MULT为2，
     * 表明每2个月组成一个账单周期
     */
    @PropertyInfo(name="账单周期乘数", length=1)
    public Integer cycleBaseMult;

    /**
     * 溢缴款免出账单最大金额
     */
    @PropertyInfo(name="溢缴款免出账单最大金额", length=15, precision=2)
    public BigDecimal crMaxbalNoStmt;

    /**
     * 积分账单标识 
     */
    @PropertyInfo(name="仅有积分需出账单", length=1)
    public Boolean stmtOnBpt;

    /**
     * 账单最小阈值，欠款大于等于该值才输出账单
     */
    @PropertyInfo(name="免出账单最大欠款", length=15, precision=2)
    public BigDecimal stmtMinBal;

    /**
     * 临时额度失效提醒天数
     */
    @PropertyInfo(name="临时额度失效提醒天数", length=2)
    public Integer tlExpPrmptPrd;

    /**
     * 缺省授权允许超限比例
     */
    @PropertyInfo(name="默认允许超限比例", length=7, precision=4)
    public BigDecimal ovrlmtRate;

    /**
     * 缺省取现额度比例
     */
    @PropertyInfo(name="默认取现比例", length=7, precision=4)
    public BigDecimal cashLimitRate;

    /**
     * 缺省额度内分期比例
     */
    @PropertyInfo(name="默认额度内分期比例", length=7, precision=4)
    public BigDecimal loanLimitRate;

    /**
     * 催收账龄阀值
     */
    @PropertyInfo(name="入催最小账龄", length=1)
    public String collOnAge;

    /**
     * 超限催收标志
     * Y/N
     */
    @PropertyInfo(name="超限入催", length=1)
    public Boolean collOnOvrlmt;

    /**
     * 首次还款拖欠催收标志
     * collect on first statment delinquncy
     */
    @PropertyInfo(name="首次还款拖欠入催", length=1)
    public Boolean collOnFsDlq;

    /**
     * 入催最小金额阀值
     */
    @PropertyInfo(name="免催最大金额", length=15, precision=2)
    public BigDecimal collMinpmt;
    
    /**
     * 账龄还款冲销优先标识
     * key - 账龄代码
     */
    @ReferEnums(AgePmtHierInd.class)
    public Map<String, AgePmtHierInd> agesPmtHierInd;
    
    /**
     * 账龄对应还款冲销顺序表id
     * key - 账龄代码
     */
    @PropertyInfo(name="账龄对应冲销顺序标识", length=9)
    public Map<String, Integer> agesPmtHierId;
    
    /**
     * 固定额度调整最小间隔日
     */
    @PropertyInfo(name="固定额度调整最小间隔日", length=3)
    public Integer creditLimitAdjustInterval;
    
    //马上消费start
    /**
     * 是否合并账单日
     */
    @PropertyInfo(name="是否合并账单日", length=1)
    public Indicator isMergeBillDay;
    /**
     * 到期还款日跳过月末
     */
    @PropertyInfo(name="到期还款日跳过月末", length=1)
    public Indicator skipEom;
    /**
     * 允许最小还款额
     */
    @PropertyInfo(name="允许最小还款额" , length=15, precision=2)
    public BigDecimal allowMinDueAmt;
    
    //马上消费end
    
    /**
     * 提前还款短信提醒提前天数
     */
    @PropertyInfo(name="提前还款短信提醒提前天数",length=2)
    public Integer prepaymentSmsRemainDays;
}
