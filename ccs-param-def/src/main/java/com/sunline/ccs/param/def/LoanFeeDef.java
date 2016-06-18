package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ccs.param.def.enums.CalcBaseInd;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.DistributeMethod;
import com.sunline.ccs.param.def.enums.InterestAcruMethod;
import com.sunline.ccs.param.def.enums.InterestAdjMethod;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.param.def.enums.PaymentIntervalUnit;
import com.sunline.ccs.param.def.enums.PrepaymentCalMethod;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ccs.param.def.enums.PrinScheduleMethod;
import com.sunline.ccs.param.def.enums.TierInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;

/**
 * 分期计划计价方式
* @author fanghj
 *
 */
public class LoanFeeDef implements Serializable {

	private static final long serialVersionUID = -1775506664508563628L;

	/**
	 * 分期子产品编号
	 */
	@PropertyInfo(name="子产品编号",length=8)
	public Integer loanFeeDefId;
	
	/**
	 * 分期期数
	 */
	@PropertyInfo(name="分期期数", length=2)
	public Integer initTerm;

	/**
	 * 最小分期金额
	 */
	@PropertyInfo(name="最小允许分期金额", length=15, precision=2)
	public BigDecimal minAmount;
	
	/**
	 * 最大允许分期金额
	 */
	@PropertyInfo(name="最大允许分期金额", length=15, precision=2)
	public BigDecimal maxAmount;
	
	/**
	 * 最大允许分期比例
	 */
	@PropertyInfo(name="总本金可转分期比例", length=12, precision=8)
	public BigDecimal maxAmountRate;
	
	/**
	 * 本金分配方式
	 */
	@PropertyInfo(name="本金分配方式", length=1)
	public DistributeMethod distributeMethod;
	
	/**
	 * 本金分摊方式
	 */
	@PropertyInfo(name="本金分摊方式", length=1)
	public PrinScheduleMethod prinScheduleMethod;
	
	/**
	 * 分期利率表ID
	 */
	@PropertyInfo(name="分期利率表ID", length=20)
	public String intTableOverrideId;
	
	/**
	 * 还款间隔单位
	 */
	@PropertyInfo(name="还款间隔单位",length=1)
	public PaymentIntervalUnit paymentIntervalUnit;
	/**
	 * 还款间隔周期
	 */
	@PropertyInfo(name="还款间隔周期",length=3)
	public Integer paymentIntervalPeriod;
	
	/**
	 * 复利利率
	 */
	@PropertyInfo(name="复利利率(/年)", length=12, precision=8)
	public BigDecimal compoundIntTableId;
	
	/**
	 * 基准利率(/年)
	 */
	@PropertyInfo(name="基准利率(/年)", length=12, precision=8)
	public BigDecimal interestRate;
	
	/**
	 * 计息方式
	 */
	@PropertyInfo(name="计息方式",length=1)
	public InterestAcruMethod interestAcruMethod;
	/**
	 * 计息用利率
	 */
	@PropertyInfo(name="计息利率", length=12, precision=8)
	public BigDecimal intTableId;
	/**
	 * 罚息用利率
	 */
	@PropertyInfo(name="罚息利率(/年)", length=12, precision=8)
	public BigDecimal penaltyIntTableId;
	
	/**
	 * 利率调整方式
	 */
	@PropertyInfo(name="利率调整方式",length = 1)
	public InterestAdjMethod interestAdjMethod;
	
	
	/**
	 * 贷款服务费收取方式                                                             
	 */
	@PropertyInfo(name="贷款服务费收取方式", length=1)              //从 分期手续费收取方式 恢复
	public LoanFeeMethod loanFeeMethod;

	/**
	 * 贷款服务费计算方式
	 */
	@PropertyInfo(name="贷款服务费计算方式", length=1)            //从 分期手续费计算方式 恢复
	public CalcMethod loanFeeCalcMethod;

	
	/**
	 * 贷款服务费固定金额
	 */
	@PropertyInfo(name="贷款服务费固定金额", length=15, precision=2)         //从 分期手续费固定金额 恢复
	public BigDecimal feeAmount;
	
	/**
	 * 贷款服务费收取比例
	 */
	@PropertyInfo(name="贷款服务费收取比例", length=12, precision=8)          //从 分期手续费收取比例 恢复
	public BigDecimal feeRate;
	
	//展期参数
	
	/**
	 * 是否允许展期
	 */
	@PropertyInfo(name="允许展期", length=1)
	public Boolean rescheduleInd;
	
	/**
	 * 展期手续费收取方式
	 */
	@PropertyInfo(name="展期手续费收取方式", length=1)
	public LoanFeeMethod rescheduleFeeMethod;

	/**
	 * 展期手续费计算方式
	 */
	@PropertyInfo(name="展期手续费计算方式", length=1)
	public CalcMethod rescheduleCalcMethod;

	/**
	 * 展期手续费金额
	 */
	@PropertyInfo(name="展期手续费金额", length=15, precision=2)
	public BigDecimal rescheduleFeeAmount;
	
	/**
	 * 展期手续费比例
	 */
	@PropertyInfo(name="展期手续费比例", length=12, precision=8)
	public BigDecimal rescheduleFeeRate;
	
	/**
	 * 允许展期最小金额
	 */
	@PropertyInfo(name="展期最小金额", length=15, precision=2)
	public BigDecimal rescheduleMinAmount;
	
	/**
	 * 允许展期最大金额
	 */
	@PropertyInfo(name="展期最大金额", length=15, precision=2)
	public BigDecimal rescheduleMaxAmount;
	
	//缩期参数
	
	/**
	 * 是否允许缩期
	 */
	@PropertyInfo(name="允许缩期",length=1)
	public Boolean shortedRescInd;
	
	/**
	 * 缩期手续费计算方式
	 */
	@PropertyInfo(name="缩期手续费计算方式", length=1)
	public CalcMethod shortedRescCalcMethod;
	
	/**
	 * 缩期手续费金额
	 */
	@PropertyInfo(name="缩期手续费金额", length=15, precision=2)
	public BigDecimal shortedRescFeeAmount;
	
	/**
	 * 缩期手续费比例
	 */
	@PropertyInfo(name="缩期手续费比例", length=12, precision=8)
	public BigDecimal shortedRescFeeAmountRate;
	
	/**
	 * 缩期最小还款金额
	 */
	@PropertyInfo(name="缩期最小还款金额", length=15,precision=2)
	public BigDecimal shortedMinPmtDue;
	
	/**
	 * 缩期最小金额
	 */
	@PropertyInfo(name="缩期最小金额", length=15,precision=2)
	public BigDecimal shorteRescdMinAmount;
	
    //马上消费金融  start
    
	//保险费参数
    /**
     * 保险费收取方式
     */
    @PropertyInfo(name="保险费收取方式", length=1)
    public LoanFeeMethod insCollMethod;
    /**
     * 保险费计算方式
     */
    @PropertyInfo(name="保险费计算方式", length=1)
    public PrepaymentFeeMethod insCalcMethod;
    /**
     * 保险费率
     */
    @PropertyInfo(name="保险费率", length=12, precision=8)
    public BigDecimal insRate;
    /**
     * 保险费固定金额
     */
    @PropertyInfo(name="保险费固定金额", length=15, precision=2)
    public BigDecimal insAmt;
    
    //寿险计划包参数
    
    /**
     * 寿险计划包收取方式
     */
    @PropertyInfo(name="寿险计划包收取方式", length=1)
    public LoanFeeMethod lifeInsuFeeMethod;
    /**
     * 寿险计划包计算方式
     */
    @PropertyInfo(name="寿险计划包计算方式", length=1)
    public PrepaymentFeeMethod lifeInsuFeeCalMethod;
    /**
     * 寿险计划包费率
     */
    @PropertyInfo(name="寿险计划包收取比例", length=12, precision=8)
    public BigDecimal lifeInsuFeeRate;
    /**
     * 寿险计划包固定金额
     */
    @PropertyInfo(name="寿险计划包固定金额", length=15, precision=2)
    public BigDecimal lifeInsuFeeAmt;
    
    //印花税参数
    
    /**
     * 印花税率
     */
    @PropertyInfo(name="印花税率", length=12, precision=8)
    public BigDecimal stampRate;
    /**
     * 印花税率收取方式
     */
    @PropertyInfo(name="印花税率收取方式", length=1)
    public LoanFeeMethod stampMethod;
    /**
     * 印花税计算方式
     */
    @PropertyInfo(name="印花税计算方式", length=1)
    public PrepaymentFeeMethod stampCalcMethod;
    /**
     * 印花税固定金额
     */
    @PropertyInfo(name="印花税固定金额", length=15, precision=2)
    public BigDecimal stampAMT;
    /**
     * 印花税是否冲减利息
     */
    @PropertyInfo(name="印花税是否冲减利息", length=1)
    public Indicator isOffsetRate;
    /**
     * 印花税是否入客户账
     */
    @PropertyInfo(name="印花税是否入客户账", length=1)
    public Indicator stampCustomInd;
    /**
     * 罚金表ID
     */
    @PropertyInfo(name="罚金表ID", length=6, precision=4)
    public String mulctTableId;
    
    //提前还款参数
    
	/**
	 * 提前还款手续费计算方式
	 */
	@PropertyInfo(name="提前还款手续费计算方式", length=1)
	public PrepaymentFeeMethod prepaymentFeeMethod;
    /**
     * 支持预约提前结清
     */
    @PropertyInfo(name="支持预约提前结清", length=1)
    public Indicator appointEarlySettleEnable;
    /**
     * 预约提前结清提前天数
     */
    @PropertyInfo(name="预约提前结清提前天数", length=3)
    public Integer appointEarlySettleDate;
    
    /**
     * 提前还款信息
     */
    public List<EarlyRepayDef> earlyRepayDefs;
    
    //现金贷二期提前还款添加参数
    /**
     * 提前还款申请扣款提前天数
     */
    @PropertyInfo(name="提前还款申请扣款提前天数",length=2)
    public Integer prepayApplyCutDay;
    
    /**
     * 提前还款计算方式
     */
    @PropertyInfo(name="提前还款计算方式",length=2)
    public PrepaymentCalMethod prepaymentCalMethod;
    //结束
    
    //其他参数
    
    /**
     * 电子资料模板ID
     */
    @PropertyInfo(name="电子资料模板ID", length=3)
    public String riskTableId;
	 /**
	  * 犹豫期(天)
	*/
	@PropertyInfo(name="犹豫期(天)",length=3)
	public Integer hesitationDays;

	/**
	 * 分期手续费收取方式
	 */
	@PropertyInfo(name = "分期手续费收取方式", length = 1)
	public LoanFeeMethod installmentFeeMethod; // 从 贷款服务费收取方式 恢复
	/**
	 * 分期手续费计算方式
	 */
	@PropertyInfo(name = "分期手续费计算方式", length = 1)
	public PrepaymentFeeMethod installmentFeeCalMethod; // 从 贷款服务费计算方式 恢复
	/**
	 * 分期手续费收取比例
	 */
	@PropertyInfo(name = "分期手续费收取比例", length = 12, precision = 8)
	public BigDecimal installmentFeeRate; // 从 贷款服务费收取比例 恢复
	/**
	 * 分期手续费固定金额
	 */
	@PropertyInfo(name = "分期手续费固定金额", length = 15, precision = 2)
	public BigDecimal installmentFeeAmt; // 从 贷款服务费固定金额 恢复

	 //灵活还款计划包费参数
	 
	 /**
	  * 灵活还款计划包费收取方式
	  */
	@PropertyInfo(name="灵活还款计划包费收取方式", length=1)
	public LoanFeeMethod prepayPkgFeeMethod;
	/**
	 * 灵活还款计划包费计算方式
	 */
	@PropertyInfo(name="灵活还款计划包费计算方式", length=1)
	public PrepaymentFeeMethod prepayPkgFeeCalMethod;
	
	/**
	 * 灵活还款计划包费金额
	 */
	@PropertyInfo(name="灵活还款计划包费金额", length=15, precision=2)
	public BigDecimal prepayPkgFeeAmount;
	
	/**
	 * 灵活还款计划包费比例
	 */
	@PropertyInfo(name="灵活还款计划包费比例", length=12, precision=8)
	public BigDecimal prepayPkgFeeAmountRate;
	
	//马上消费金融  end
	
    //以下滞纳金，暂不使用
    /**
	 * 滞纳金收取标志
	 */
	@PropertyInfo(name="滞纳金收取标志",length=1)
	public Indicator lateFeeCharge;
	
	  /**
     * 收取滞纳金最小拖欠期数，小于该值不收取
     */
    @PropertyInfo(name="触发最小拖欠期数", length=1)
    public String minAgeCd;
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
     * 滞纳金计算基准金额指示：
     * T - 用最小还款额剩余部分（total due）
     * L - 用上期最小还款额剩余部分（last due）
     * C - 对当期due收滞纳金
     */
    @PropertyInfo(name="计算基准金额", length=1)
    public CalcBaseInd calcBaseInd;
    /**
     * 计算方式：
     * F - 利用全部金额计算
     * T - 分段计算
     */
    @PropertyInfo(name="计算方式", length=1)
    public TierInd tierInd;
    /**
     * 费率信息
     */
    public List<RateDef> chargeRates;
    /**
     * 分期子产品状态
     */
	@PropertyInfo(name = "分期子产品状态", length = 1)
	public LoanFeeDefStatus loanFeeDefStatus;
	
	/**
     * 免费退货最长天数（包含当天）
     */
    @PropertyInfo(name="免费退货最长天数（包含当天）", length=3)
    public Integer returnMaxDays;
	
	/**
	 * 代收服务费收取比例
	 */
	@PropertyInfo(name="代收服务费收取比例", length=12, precision=8)
	public BigDecimal replaceFeeRate;
	
	/**
	 * 代收服务费固定金额
	 */
	@PropertyInfo(name="代收服务费固定金额", length=15, precision=2)
	public BigDecimal replaceFeeAmt;
	
	/**
	 * 代收服务费收取方式
	 */
	@PropertyInfo(name="代收服务费收取方式", length=1)
	public LoanFeeMethod replaceFeeMethod;

	/**
	 * 代收服务费计算方式
	 */
	@PropertyInfo(name="代收服务费计算方式", length=1)
	public CalcMethod replaceFeeCalMethod;
	
	//兜底产品
	/**
	 * 代收罚息费率
	 */
	@PropertyInfo(name="代收罚息费率",length=15,precision=2)
	public BigDecimal replacePenaltyRate;
	
	/**
	 * 代收提前还款手续费收取方式
	 */
	@PropertyInfo(name="代收提前还款手续费收取方式",length=1)
	public PrepaymentFeeMethod replacePrepaymentFeeMethod;
	
    /**
     * 代收提前还款手续费列表
     */
    public List<ReplaceEarlyRepayDef> replaceEarlyRepayDef;
    
    /**
	 * 是否退还趸交费
	 */
	@PropertyInfo(name="是否退还趸交费",length=1)
	public Indicator premiumReturnInd;
	
	/**
	 * 代收罚金表ID
	 */
	@PropertyInfo(name="代收罚金表ID", length=6, precision=4)
    public String replaceMulctTableId;
	
	//兜底新增
	/**
     * CPD容差
     */
    @PropertyInfo(name="CPD容差", length=15,precision=2)
    public BigDecimal cpdToleLmt;
	
    /**
     * DPD容差
     */
    @PropertyInfo(name="DPD容差", length=15,precision=2)
    public BigDecimal dpdToleLmt;
    
    //5月版本新增
    /**
     * 是否免收宽限期利息
     */
    @PropertyInfo(name="是否免收宽限期利息",length=1)
    public Indicator wavieGraceIntInd;
    
    /**
     * 延期还款申请距离还款日提前天数
     */
    @PropertyInfo(name="延期还款申请距离还款日提前天数",length=2)
    public Integer delayApplyAdvDays;
    
    /**
     * 延期还款申请最大次数
     */
    @PropertyInfo(name="延期还款申请最大次数",length=2)
    public Integer delayApplyMax;

    /**
     * 延期还款每次延期最大期数
     */
    @PropertyInfo(name="延期还款每次延期最大期数",length=2)
    public Integer delayMaxTerm;
    
    /**
     * 延期还款累计延期最大期数
     */
    @PropertyInfo(name="延期还款累计延期最大期数",length=2)
    public Integer delayAccuMaxTerm;
    
    /**
     * 延期还款首次申请足额还款期数
     */
    @PropertyInfo(name="延期还款首次申请足额还款期数",length=2)
    public Integer delayFristApplyTerm;
    
    /**
     * 延期还款再次申请距离上次足额偿还期数
     */
    @PropertyInfo(name="延期还款再次申请距离上次足额偿还期数",length=2)
    public Integer delayApplyAgainTerm;
    
    /**
     * 变更还款日次月生效申请提前天数
     */
    @PropertyInfo(name="变更还款日次月生效申请提前天数",length=2)
    public Integer payDateExpireAdvDays;
    
    /**
     * 变更还款日首次申请足额还款期数
     */
    @PropertyInfo(name="变更还款日首次申请足额还款期数",length=2)
    public Integer payDateFirstApplyTerm;
    
    /**
     * 变更还款日再次申请距离上次足额偿还期数
     */
    @PropertyInfo(name="变更还款日再次申请距离上次足额偿还期数",length=2)
    public Integer payDateApplyAgainTerm;
    
    /**
     * 变更还款日累计变更最大次数
     */
    @PropertyInfo(name="变更还款日累计变更最大次数",length=2)
    public Integer payDateAccuMax;
    
    /**
     * 优惠提前还款申请足额还款期数
     */
    @PropertyInfo(name="优惠提前还款申请足额还款期数",length=2)
    public Integer disPrepaymentApplyTerm;
}
