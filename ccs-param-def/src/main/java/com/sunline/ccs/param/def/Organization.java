package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.GraceIntType;
import com.sunline.ccs.param.def.enums.LimitControlType;
import com.sunline.ccs.param.def.enums.MicroLoanSupport;
import com.sunline.ccs.param.def.enums.NoCardSelfDiscern;
import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ark.support.meta.ReferEnums;

/**
 * 核心系统机构级参数
 */
public class Organization implements Serializable {

	private static final long serialVersionUID = -1600750662181409073L;
    
    /**
     * 基准货币代码
     */
    @PropertyInfo(name="基准货币代码", length = 3)
    public String baseCurrCd;
    
    /**
     * 基准货币小数位
     */
    @PropertyInfo(name="基准货币小数位", length = 1)
    public Integer baseCurrexp;
    
    /**
     * 是否允许双币卡
     */
    @PropertyInfo(name="允许双币卡", length = 1)
    public Boolean dualEnableInd;

    /**
     * 额度控制类型
     * H|highest-最高额度控制
     * S|sum-汇总额度控制
     */
    @PropertyInfo(name="额度控制类型", length = 1)
    public LimitControlType limitControlType;

    /**
     * 最大授信额度
     */
    @PropertyInfo(name="最大授信额度", length=15, precision=2)
    public BigDecimal maxCreditLimit;

    /**
     * 对账单记录保留期限（月）
     */
    @PropertyInfo(name="对账单保留月数", length=2)
    public Integer stmtRtnPrd;

    /**
     * 最大账单日修改次数/年
     */
    @PropertyInfo(name="最大账单日修改次数/年", length = 2)
    public Integer maxCycChange;

    /**
     * 缺省分支行号（卡中心）
     */
    @PropertyInfo(name="缺省分支行号", length=9)
    public String defaultBranchId;

    /**
     * 续卡审核提前月数
     */
    @PropertyInfo(name="续卡审核提前月数", length = 1)
    public Integer rnwlRvwMth;

    /**
     * 超限享受免息期标识
     */
    @PropertyInfo(name="超限免息标识", length=1)
    public GraceIntType overlimitDeferInd;
    
    /**
     * 未全额还款享受免息期标志
     * 如果未对上期账单做全额还款，当期新增交易是否享受免息期
     */
    @PropertyInfo(name="未全额还款免息标识", length=1)
    public GraceIntType nofullpayDeferInd;

    /**
     * 溢缴款信用计划号
     */
    @PropertyInfo(name="溢缴款信用计划号", length=6)
    public String depositPlanNbr;

    /**
     * 会计并账文件级别
     * 用于支持卡中心统一核算以及各分行单独核算两种模式。
     * /// 
     * 0|总行级别
     * 1|一级分行级别
     */
    @PropertyInfo(name="会计并账文件级别", length=1)
    public String glLevel;
  
    /**
     * 信用额度币种折算汇率
     * key - 货币代码
     * value - 折算汇率
     * 目前汇率指折算为RMB的汇率，未来增加货币转换矩阵
     */
    public Map<String, BigDecimal> shareLimitRate;

    /**
     * 月续卡频率
     * 一个月内的续卡次数，不同开卡日期的卡片只在这些日期做续卡
     */
    @PropertyInfo(name="月续卡频率", length=2)
    public Integer renewFrequency;
    
    /**
     * 预销卡持续天数
     * 销卡账户保留多少天后正式销卡
     */
    @PropertyInfo(name="预销卡持续天数", length=2)
    public Integer daysBeforeClose;
    
    /**
     * 临额最大有效月数
     */
    @PropertyInfo(name="临额最大有效月数", length=1)
    public Integer tempLimitMaxMths;
    
    /**
     * 临时额度调整最大比例
     */
    @PropertyInfo(name="临额调整最大比例", length=5, precision=2)
    public BigDecimal tlMaxRt;
    
    /**
     * 利息是否按信用计划入账
     */
    @PropertyInfo(name="利息按信用计划入账", length=1)
    public Boolean intPostOnPlan;
    
    @PropertyInfo(name="是否需要调用反欺诈",length=1)
    public Boolean needRds;
    
    @PropertyInfo(name="无卡自助识别模式", length=1)
    public NoCardSelfDiscern noCardSelfDiscern;
    
    /**
     * 短信或信函的配置
     */
    @ReferEnums({CPSMessageCategory.class})
    public Map<CPSMessageCategory, String> messageTemplates;
    
    @PropertyInfo(name="贷记撤销交易验证otb标志",length=1)
    public Boolean creditVoidOtbCtrlInd;
    
    @PropertyInfo(name="贷记冲正交易验证otb标志",length=1)
    public Boolean creditReverseOtbCtrlInd;
    
    /**
     * 贷记撤销-冲正超限容差
     */
    @PropertyInfo(name="贷记撤销-冲正超限容差" ,length=15, precision=2)
    public BigDecimal crRevFloorLimit;
    
    @PropertyInfo(name="送总账账龄与账龄一致标志" ,length=1)
    public Boolean glAgeCdConsistentWithAgeCd;
    
    /**
     * 支持小额贷款的方式
     */
    @PropertyInfo(name="支持小额贷款的方式", length=1)
	public MicroLoanSupport microLoanSupport;
    
    /**
     * 现金分期放款方式
     */
    @PropertyInfo(name="现金分期放款方式", length=1)
  	public LoanLendWay cashLoanSendMode;
    
    /**
     * 现金分期是否需要审核
     */
    @PropertyInfo(name="现金分期是否需要审核", length=1)
  	public Boolean cashLoanNeedAudit;
}
