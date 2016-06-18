package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ark.support.meta.ReferEnums;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.CashLoanLimitType;
import com.sunline.ccs.param.def.enums.CreditType;
import com.sunline.ccs.param.def.enums.LoanVerifyType;
import com.sunline.ccs.param.def.enums.PinBlockResetInd;
import com.sunline.ccs.param.def.enums.SendMessageCardType;
import com.sunline.ppy.dictionary.enums.CardFetchMethod;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;

/**
 * 卡产品贷记属性参数
 */
public class ProductCredit implements Serializable {

    private static final long serialVersionUID = 8168965691382009925L;

    /**
     * 产品代码
     */
    @PropertyInfo(name = "产品代码", length = 6)
    public String productCd;

    /**
     * 本币入账币种
     */
    @PropertyInfo(name = "本币入账币种", length = 3)
    public String postCurrCd;

    /**
     * 外币入账币种
     */
    @PropertyInfo(name = "外币入账币种", length = 3)
    public String dualCurrCd;

    /**
     * 缺省消费凭密标识 Y/N
     */
    @PropertyInfo(name = "缺省消费凭密", length = 1)
    public Boolean purchasePinInd;

    /**
     * 卡片寄送方式
     */
    @PropertyInfo(name = "卡片寄送方式", length = 1)
    public CardFetchMethod cardMailMethodInd;

    /**
     * 交易密码错误限次
     */
    @PropertyInfo(name = "密码错误限次", length = 2)
    public Integer pinTry;

    /**
     * 交易密码锁定释放标识
     */
    @PropertyInfo(name = "密码锁定释放标识", length = 1)
    public PinBlockResetInd pinBlockResetInd;

    /**
     * 查询密码错误限次
     */
    @PropertyInfo(name = "查询密码限次", length = 2)
    public Integer maxInqPinTry;

    /**
     * 查询密码锁定释放标识
     */
    @PropertyInfo(name = "查询密码锁定释放标识", length = 1)
    public PinBlockResetInd inqPinBlockResetInd;

    /**
     * CVV错误限次
     */
    @PropertyInfo(name = "CVV错误限次", length = 2)
    public Integer cvvTry;

    /**
     * 
     * CVV2错误限次
     */
    @PropertyInfo(name = "CVV2错误限次", length = 2)
    public Integer cvv2Try;

    /**
     * ICVV错误限次
     */
    @PropertyInfo(name = "ICVV错误限次", length = 2)
    public Integer icvvTry;

    /**
     * CVV锁定释放标识
     */
    @PropertyInfo(name = "CVV锁定释放标识", length = 1)
    public PinBlockResetInd cvvBlockResetInd;

    /**
     * ICVV锁定释放标识
     */
    @PropertyInfo(name = "ICVV锁定释放标识", length = 1)
    public PinBlockResetInd icvvBlockResetInd;

    /**
     * CVV2锁定释放标识
     */
    @PropertyInfo(name = "CVV2锁定释放标识", length = 1)
    public PinBlockResetInd cvv2BlockResetInd;

    /**
     * 转正式挂失短信提醒天数
     */
    @PropertyInfo(name = "转正式挂失短信提醒天数", length = 3)
    public Integer lostToSendMessageDays;

    /**
     * 转正式挂失换卡天数
     */
    @PropertyInfo(name = "转正式挂失换卡天数", length = 3)
    public Integer lostToChangeCardDays;

    /**
     * 授权匹配容忍比例
     */
    @PropertyInfo(name = "授权匹配容忍比例", length = 7, precision = 4)
    public BigDecimal athMatchTolRt;

    /**
     * 预授权完成容忍比例
     */
    @PropertyInfo(name = "预授权完成容忍比例", length = 7, precision = 4)
    public BigDecimal preathCompTolRt;

    /**
     * 预授权保留最长时间
     */
    @PropertyInfo(name = "预授权保留天数", length = 2)
    public Integer preathRtnPrd;

    /**
     * 预授权是否允许隔日撤销
     */
    @PropertyInfo(name = "是否允许隔日撤销", length = 1)
    public Indicator oldPreAuthVoidSuppInd;

    /**
     * 未匹配授权额度冻结天数
     */
    @PropertyInfo(name = "未匹配借记授权额度冻结天数", length = 3)
    public Integer unmatchDbRtnPrd;

    /**
     * 未匹配贷记授权额度冻结天数
     */
    @PropertyInfo(name = "未匹配贷记授权额度冻结天数", length = 3)
    public Integer unmatchCrRtnPrd;

    /**
     * 缺省账单日
     */
    @PropertyInfo(name = "缺省账单日", length = 2)
    public Integer dfltCycleDay;

    /**
     * OTB是否包含未匹配还款
     */
    @PropertyInfo(name = "未匹配还款提升可用额度", length = 1)
    public Boolean otbInclCrath;

    /**
     *     原名：   延迟扣款是否利息回溯
     *     修改时间2015.11.05 15：04  chenpy
     */
    @PropertyInfo(name="扣款延迟入账是否回溯利息", length=1)
    public Indicator isReviewInt;
    
    /**
     * OTB是否包含溢缴款
     */
    @PropertyInfo(name = "溢缴款提升可用额度", length = 1)
    public Boolean otbInclCrbal;

    /**
     * OTB是否包含争议金额
     */
    @PropertyInfo(name = "争议金额占用可用额度", length = 1)
    public Boolean otbInclDspt;

    /**
     * OTB是否包含分期付款 Y/N
     */
    @PropertyInfo(name = "分期金额占用可用额度", length = 1)
    public Boolean otbInclFrzn;

    /**
     * 取现可用额度是否包含未匹配还款
     */
    @PropertyInfo(name = "未匹配还款提升取现可用额度", length = 1)
    public Boolean cotbInclCrath;

    /**
     * 取现可用额度是否包含溢缴款
     */
    @PropertyInfo(name = "溢缴款提升取现可用额度", length = 1)
    public Boolean cotbInclCrbal;

    /**
     * 超限费
     */
    // @PropertyInfo(name="超限费")
    public OverlimitCharge overlimitCharge;

    /**
     * 外币超限费
     */
    // @PropertyInfo(name="外币超限费")
    public OverlimitCharge dualOverlimitCharge;

    /**
     * 滞纳金
     */
    // @PropertyInfo(name="滞纳金")
    public LatePaymentCharge latePaymentCharge;

    /**
     * 外币滞纳金
     */
    // @PropertyInfo(name="外币滞纳金")
    public LatePaymentCharge dualLatePaymentCharge;

    /**
     * 杂项费用
     */
    // @PropertyInfo(name="杂项费用")
    public Fee fee;

    /**
     * 外币杂项费用
     */
    // @PropertyInfo(name="外币杂项费用")
    public Fee dualFee;

    /**
     * 默认动账短信阈值
     */
    @PropertyInfo(name = "默认动账短信阈值", length = 15, precision = 2)
    public BigDecimal defaultSmsAmt;

    /**
     * 本币账户属性引用ID
     */
    @PropertyInfo(name = "本币账户参数标识", length = 8)
    public Integer accountAttributeId;

    /**
     * 外币账户属性引用ID
     */
    @PropertyInfo(name = "外币账户参数标识", length = 8)
    public Integer dualAccountAttributeId;

    /**
     * 单日ATM限笔
     */
    @PropertyInfo(name = "单日ATM限笔", length = 8)
    public Integer dayAtmNbr;

    /**
     * 单日ATM限额
     */
    @PropertyInfo(name = "单日ATM限额", length = 13)
    public BigDecimal dayAtmLimit;

    /**
     * 单日消费限笔
     */
    @PropertyInfo(name = "单日消费限笔", length = 8)
    public Integer dayRetailNbrLimit;

    /**
     * 单日消费限额
     */
    @PropertyInfo(name = "单日消费限额", length = 13)
    public BigDecimal dayRetailAmtLimit;

    /**
     * 单日取现限笔
     */
    @PropertyInfo(name = "单日取现限笔", length = 8)
    public Integer dayCashNbrLimit;

    /**
     * 单日取现限额
     */
    @PropertyInfo(name = "单日取现限额", length = 13)
    public BigDecimal dayCashAmtLimit;

    /**
     * 单日转出限笔
     */
    @PropertyInfo(name = "单日转出限笔", length = 8)
    public Integer dayXfroutNbrLimit;

    /**
     * 单日转出限额
     */
    @PropertyInfo(name = "单日转出限额", length = 13)
    public BigDecimal dayXfroutAmtLimit;

    /**
     * 单日银联境外ATM取现限额
     */
    @PropertyInfo(name = "单日银联境外ATM取现限额", length = 13)
    public BigDecimal dayCupxbAtmLimit;

    /**
     * 分期标识 该产品是否支持分期付款
     */
    @PropertyInfo(name = "支持分期付款", length = 1)
    public Boolean loanSuppInd;

    /**
     * 专项分期标识 该产品是否支持专项分期
     */
    @PropertyInfo(name = "支持专项分期", length = 1)
    public Boolean specLoanSuppInd;

    /**
     * 客服费用收取指示 key-客服服务代码
     */
    public Map<String, CustomerServiceFee> customerServiceFee;

    /**
     * 卡产品支持的分期计划列表 key-分期代码 指向参数LoanPlan
     */
    @PropertyInfo(name = "分期计划模板列表", length = 6)
    public Map<LoanType, String> loanPlansMap;

    /**
     * 自动消费转分期
     */
    @PropertyInfo(name = "自动消费转分期", length = 1)
    public Boolean autoLoanR;

    /**
     * 消费转分期产品
     */
    @PropertyInfo(name = "消费转分期产品", length = 40)
    public String loanRDesc;

    /**
     * 最低转分期消费金额
     */
    @PropertyInfo(name = "最低转分期消费金额", length = 13)
    public BigDecimal minAutoLoanRAmt;

    /**
     * 分期总期数
     */
    @PropertyInfo(name = "分期总期数", length = 3)
    public Integer autoLoanRInitTerm;

    /**
     * 分期手续费收取方式
     */
    @PropertyInfo(name = "分期手续费收取方式", length = 1)
    public LoanFeeMethod autoLoanRFeeMethod;

    /**
     * 信用计划模板列表 key - 信用计划类型 value - 该类型对应的信用计划模板编号(PlanTemplate主键PlanNbr)
     */
    @PropertyInfo(name = "信用计划模板列表", length = 6)
    public Map<PlanType, String> planNbrList;

    /**
     * 交易费用清单 key - 入账交易代码(TxnCd主键) value - 费用收取清单
     */
    public Map<String, List<TxnFee>> txnFeeList;

    /**
     * 是否允许透支转出
     */
    @PropertyInfo(name = "是否允许透支转出", length = 1)
    public Boolean transferDeditOverdrawValid;
    /**
     * 是否允许多笔额度内现金分期
     */
    @PropertyInfo(name = "是否允许多笔额度内现金分期", length = 1)
    public Indicator multiCashLoanInd;

    @PropertyInfo(name = "额度内现金分期最大笔数", length = 4)
    public Integer maxCashLoanCnt;
    /**
     * 是否允许多笔额度外现金分期
     */
    @PropertyInfo(name = "是否允许多笔额度外现金分期", length = 1)
    public Indicator multiSpecCashLoanInd;

    @PropertyInfo(name = "额度外现金分期最大笔数", length = 4)
    public Integer maxSpecCashLoanCnt;
    /**
     * 额度内现金分期使用的额度类型
     */
    @PropertyInfo(name = "额度内现金分期使用的额度类型", length = 1)
    public CashLoanLimitType cashLoanLimitType;

    @ReferEnums({CPSMessageCategory.class})
    public Map<CPSMessageCategory, String> messageTemplates;

    /**
     * 最大逾期期数
     */
    @PropertyInfo(name = "最大逾期期数", length = 4)
    public Integer maxCd;

    /**
     * 六期内不允许超过的逾期期数
     */
    @PropertyInfo(name = "六期内不允许超过的逾期期数", length = 4)
    public Integer latestCdCount;

    /**
     * 是否检查授信有效期
     */
    @PropertyInfo(name = "是否检查授信有效期", length = 1)
    public Indicator creditExpiryDateInd;

    /**
     * 到期提前报表月数
     */
    @PropertyInfo(name = "到期提前报表月数", length = 2)
    public Integer prvPeriodOfCrExpDate;

    /**
     * 到期提前提示标志
     */
    @PropertyInfo(name = "到期提前提示标志", length = 1)
    public Indicator prvReportOfCrExpDateInd;

    /**
     * 当日到期报表控制标志
     */
    @PropertyInfo(name = "当日到期报表控制标志", length = 1)
    public Indicator crExpiredReportInd;

    /**
     * 当日到期审核结果报表
     */
    @PropertyInfo(name = "当日到期审核结果报表", length = 1)
    public Indicator crReApproveReportInd;

    /**
     * 卡片到期是否需要人工审批
     */
    @PropertyInfo(name = "卡片到期是否需要人工审批", length = 1)
    public Indicator expiryAutoCheck;

    /**
     * 启用机构层短信模板
     */
    @PropertyInfo(name = "启用机构层短信模板", length = 1)
    public Indicator useOrgMessageTemplate;

    /**
     * 短信发送卡号方式
     */
    @PropertyInfo(name = "短信发送卡号方式")
    public SendMessageCardType sendMessageCardType;

    /**
     * 约定扣款(未逾期时)持续补扣天数
     */
    @PropertyInfo(name = "约定扣款持续补扣天数", length = 2)
    public Integer keepingDDDays;

    /**
     * 逾期后持续扣款标志
     */
    @PropertyInfo(name = "逾期后持续扣款标志", length = 2)
    public Indicator ddAfterDelin;

    /**
     * 末期后持续扣款标志
     */
    @PropertyInfo(name = "末期后持续扣款标志", length = 2)
    public Indicator ddAfterLastTerm;

    /**
     * 未激活可交易
     */
    @PropertyInfo(name = "未激活可交易", length = 1)
    public Boolean inactiveTrade;

    /**
     * 指定POS分期开卡
     */
    @PropertyInfo(name = "指定POS分期开卡", length = 1)
    public Boolean setupLoanP;

    /**
     * 商户号
     */
    @PropertyInfo(name = "商户号", length = 15)
    public String setupLoanPMerchantNo;

    /**
     * 商户MCC
     */
    @PropertyInfo(name = "商户类型(MCC)", length = 4)
    public String setupLoanPMerchantMCC;

    /**
     * 分期活动编号
     */
    @PropertyInfo(name = "分期活动编号", length = 30)
    public String setupLoanPProgramId;

    /**
     * 分期期数
     */
    @PropertyInfo(name = "分期期数", length = 3)
    public Integer setupLoanPInitTerm;

    /**
     * 商品名址信息
     */
    @PropertyInfo(name = "商品名址信息", length = 80)
    public String setupLoanPCommodityNameAddr;

    /**
     * 分期手续费收取方式
     */
    @PropertyInfo(name = "分期手续费收取方式", length = 1)
    public LoanFeeMethod setupLoanPFeeMethod;

    // 马上消费金融 start

    /**
     * 所属金融机构
     */
    @PropertyInfo(name = "所属金融机构", length = 8)
    public String financeOrgNo;
    /**
     * 预理赔起始天数
     */
    @PropertyInfo(name = "预理赔起始天数", length = 3)
    public Integer preClaimStartDays;
    /**
     * 预理赔终止天数
     */
    @PropertyInfo(name = "预理赔终止天数", length = 3)
    public Integer preClaimEndDays;
    /**
     * 理赔天数
     */
    @PropertyInfo(name = "理赔天数", length = 3)
    public Integer claimsDays;

    /**
     * 是否循环额度
     */
    @PropertyInfo(name = "是否循环额度", length = 1)
    public Indicator circleAble;

    /**
     * 默认分期类型
     */
    @PropertyInfo(name = "默认分期类型", length = 5)
    public LoanType defaultLoanType;
    // 马上消费金融 end
    // 马上贷
//    /**
//     * 是否自动放款
//     */
//    @PropertyInfo(name = "是否自动放款", length = 1)
//    public Indicator autoDCAmtLimit;
    
    /**
     * 放款方式
     * O-实时放款;B-批量放款
     */
    @PropertyInfo(name = "放款方式", length = 1)
    public CreditType batchDCInd;
    /**
     * 提款有效期(天)
     */
    @PropertyInfo(name = "提款有效期(天)", length = 3)
    public Integer dormentDays;
    /**
     * 放款审核方式
     * P-人工;O-自动
     */
    @PropertyInfo(name = "放款审核方式", length = 1)
    public LoanVerifyType loanVerifyMethod;
    
    /**
     * 自动放款阀值
     */
    @PropertyInfo(name = "自动放款阀值", length = 15,precision = 2)
    public BigDecimal autoDCAmtLimit; 
    
    /**
     * 提款最小金额
     */
    @PropertyInfo(name = "提款最小金额", length = 15,precision = 2)
    public BigDecimal withdrawLowlimit;
    
    /**
     * 还款最小金额
     */
    @PropertyInfo(name = "还款最小金额", length = 15,precision = 2)
    public BigDecimal repayLowlimit;
    
    //兜底产品
    
    /**
     * 代收滞纳金
     */
    public LatePaymentCharge replaceLatePaymentCharge;
    
    /**
     * 是否代偿
     */
    @PropertyInfo(name = "是否代偿", length = 1)
    public Indicator needCompensate;
    
    /**
     * 代偿逾期天数
     */
    @PropertyInfo(name = "代偿逾期天数", length = 2)
    public Integer overdueToCompstDays;
    
    /**
     * 是否免除还款日后一天到宽限日间累计的正常利息
     */
    @PropertyInfo(name = "是否免除还款日后一天到宽限日间累计的正常利息", length = 1)
    public Indicator isGracedayIntWaive;

}
