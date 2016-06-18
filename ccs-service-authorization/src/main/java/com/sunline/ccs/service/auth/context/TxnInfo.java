package com.sunline.ccs.service.auth.context;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
import com.sunline.ppy.dictionary.enums.TransMedium;
import com.sunline.ppy.dictionary.enums.VerifyResult;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.param.def.enums.AutoType;
import com.sunline.ccs.param.def.enums.PinEntryMode;
import com.sunline.ccs.param.def.enums.TransFrom;

/**
 * 
 * @see 类名：TxnInfo
 * @see 描述：交易处理中中间业务信息
 *
 * @see 创建日期：   2015年6月24日下午3:18:23
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class TxnInfo {
	/**
	 * 兑换汇率
	 */
	private BigDecimal conversionRt;
	/**
	 * 负OTB控制金额
	 */
	private BigDecimal crRevFloorLimit;
	/**
	 * [贷记撤销]借记账户负OTB控制
	 */
	private boolean creditVoidOtbCtrlInd = false;

	/**
	 * [贷记冲正]借记账户负OTB控制
	 */
	private boolean creditReverseOtbCtrlInd = false;
	
	/**
	 * 透支支付标识
	 */
	private boolean transferDeditOverdrawValid = false;
	
	/**
	 * 强制验密标识
	 */
	private boolean mustCheckPwd = false;
	
	/**
	 * 交易终端（规则文件中获取）
	 */
	private AuthTransTerminal transTerminal;
	/**
	 * 交易类型 （规则文件中获取）
	 */
	private AuthTransType transType;
	/**
	 * 交易方向 （规则文件中获取）
	 */
	private AuthTransDirection transDirection;
	/**
	 * 原始交易金额 （加载账户币种时设置）
	 */
	private BigDecimal transAmt;
	/**
	 * 原始交易币种 （加载账户币种时设置）
	 */
	private String transCurr;
	/**
	 * 入账交易金额 （加载账户币种时设置）
	 */
	private BigDecimal chbTransAmt;
	/**
	 * 入账交易币种 （加载账户币种时设置）
	 */
	private String chbCurr;
	/**
	 * 交易密码输入能力（规则文件中获取） b022Entrymode=1可输入，其他，不可输入
	 */
	private PinEntryMode pinEntryMode;
	/**
	 * 自助类型 （规则文件中获取）
	 */
	private AutoType autoType;
	/**
	 * 交易介质 （规则文件中获取）
	 */
	private TransMedium transMedium;
	/**
	 * 接入卡组织标示 （规则文件中获取）
	 */
	private InputSource inputSource;

	/**
	 * 交易发起方式 （规则文件中获取）
	 */
	private TransFrom transFrom;
	/**
	 * 客户级可用额度
	 */
	private BigDecimal customerOTB;
	/**
	 * 账户可用额度
	 */
	private BigDecimal accountOTB;
	/**
	 * 取现可用额度
	 */
	private BigDecimal cashOTB;

	/**
	 * 账户溢缴款取现可用额度
	 */
	private BigDecimal depositeCashOTB;

	/**
	 * 人工授权标志
	 */
	private ManualAuthFlag manualAuthFlag = ManualAuthFlag.N;
	/**
	 * 授权码 记录日志时生成？
	 */
	private String authCode;
	/**
	 * 原因码授权结果,所有的都设置初始值为通过的值，程序会在处理过程中不断改这几个值
	 */
	private AuthReason authReason = AuthReason.A000;

	private AuthAction authAction = AuthAction.A;

	private String responsCode = "00";
	/**
	 * 是否电子类交易 （规则文件中获取） 60.2.5 = 电脑互联网输入07 电视机顶盒输入16 手机输入08 或60.2.8 不为00
	 */
	private boolean electronTrans = false;
	/**
	 * 是否远程交易（规则文件中获取） 60.2.5= 电话输入09或17 电脑互联网输入07 电视机顶盒输入16 手机输入08
	 */
	private boolean remoteTrans = false;
	/**
	 * 是否境外免验（规则文件中获取） 60.2.6为0 非免验，60.2.6为1免验 无此域或其他值是为非免验
	 */
	private boolean abroadNoVerify = false;

	/**
	 * 是否银联境外免验
	 */
	private boolean cupXborder = false;

	/**
	 * 是否網銀交易
	 */
	private boolean internetTrans = false;
	/**
	 * 是否本币
	 */
	private boolean baseCurrency = false;
	/**
	 * 交易分类调用程序id
	 */
	private String processId;
	/**
	 * 联机业务处理日期
	 */
	private Date bizDate;
	/**
	 * 原交易业务处理日期
	 */
	private Date origBizDate;
	/**
	 * 检查项
	 */
	private String checkItem;
	/**
	 * 检查列表
	 */
	private List<String> checkList = new ArrayList<String>();
	/**
	 * 单日ATM限笔
	 */
	private Integer dayAtmNbr;
	/**
	 * 单日ATM限额
	 */
	private BigDecimal dayAtmLimit;
	/**
	 * 单日消费限笔
	 */
	private Integer dayRetailNbrLimit;
	/**
	 * 单日消费限额
	 */
	private BigDecimal dayRetailAmtLimit;
	/**
	 * 单日取现限笔
	 */
	private Integer dayCshNbrLimit;
	/**
	 * 单日取现限额
	 */
	private BigDecimal dayCashAmtLimit;
	/**
	 * 单日转出限笔
	 */
	private Integer dayXfroutNbrLimit;
	/**
	 * 单日转出限额
	 */
	private BigDecimal dayXfroutAmtLimit;

	/**
	 * 单日银联境外ATM取现限额
	 */
	private BigDecimal dayCupxbAtmLimit;

	private Map<AuthReason, AuthAction> reasons;

	private String countryCd;

	private String expiryDate;

	/**
	 * 姓名
	 */
	private String acctVerifyName;

	/**
	 * 该交易是否是无卡自助开通
	 */
	private boolean specTranNoCardSelfSvc = false;
	
	/**
	 * 查询是否开通无卡自助
	 */
	private boolean qSpecTranNoCardSvc = false;
	
	/**
	 * 是否返回姓名
	 */
	private boolean nameResp = false;

	/**
	 * 是否返回验证码
	 */
	private boolean dynamicCodeResp = false;
	
	/**
	 * 是否姓名正确(AM用法)
	 */
	private VerifyResult chbNameAMVerifyResult;
	
	/**
	 * 是否姓名正确(NM用法)
	 */
	private VerifyResult chbNameNMVerifyResult;
	/**
	 * 是否收款人姓名正确（行内还款交易）
	 * @since 2.5.0
	 */
	private VerifyResult chbReceiverNameFromBankVerifyResult;
	
	/**
	 * 是否身份证正确
	 */
	private VerifyResult idNbrVerifyResult;
	
	/**
	 * 是否手机号码正确
	 */
	private VerifyResult mobiNbrVerifyResult;
	
	/**
	 * 无卡自助开通标识
	 */
	private Indicator noCardPrzTxnSupportInd = Indicator.N;
	
	/**
	 * 无卡自助手机号码
	 */
	private String noCardPrzTxnMobileNbr;
	
	/**
	 * 消费验证类型 60.2.8为09自主消费10为辅助消费
	 */
	private String authVerifyType;

	private boolean closeSettleAcct = false;

	private BigDecimal LoanOTB;

	private Date sysOlTime;

	private BigDecimal microCreditOTB;

	private Integer cdTerms;
	
	private Integer sixMonCdCount;
	
	private boolean gtLastTermDate;
	
	/**
	 * 是否存在密码
	 */
	 	private BigDecimal origOrigTransAmt;
	private PasswordVerifyResult existPassword;
	
	/**
	 * 验证cvv2 默认为必验
	 * AIC2.7 银联升级
	 */
	private boolean verifCvv2 = true;
	
	public BigDecimal getConversionRt() {
		return conversionRt;
	}
		public BigDecimal getOrigOrigTransAmt() {
		return origOrigTransAmt;
	}
	public void setOrigOrigTransAmt(BigDecimal origOrigTransAmt) {
		this.origOrigTransAmt = origOrigTransAmt;
		}
	public void setConversionRt(BigDecimal conversionRt) {
		this.conversionRt = conversionRt;
	}
	public BigDecimal getCrRevFloorLimit() {
		return crRevFloorLimit;
	}
	public void setCrRevFloorLimit(BigDecimal crRevFloorLimit) {
		this.crRevFloorLimit = crRevFloorLimit;
	}
	public boolean isCreditVoidOtbCtrlInd() {
		return creditVoidOtbCtrlInd;
	}
	public void setCreditVoidOtbCtrlInd(boolean creditVoidOtbCtrlInd) {
		this.creditVoidOtbCtrlInd = creditVoidOtbCtrlInd;
	}
	public boolean isCreditReverseOtbCtrlInd() {
		return creditReverseOtbCtrlInd;
	}
	public void setCreditReverseOtbCtrlInd(boolean creditReverseOtbCtrlInd) {
		this.creditReverseOtbCtrlInd = creditReverseOtbCtrlInd;
	}
	public boolean isTransferDeditOverdrawValid() {
		return transferDeditOverdrawValid;
	}
	public void setTransferDeditOverdrawValid(boolean transferDeditOverdrawValid) {
		this.transferDeditOverdrawValid = transferDeditOverdrawValid;
	}
	public PasswordVerifyResult getExistPassword() {
		return existPassword;
	}
	public void setExistPassword(PasswordVerifyResult existPassword) {
		this.existPassword = existPassword;
	}
	public boolean isMustCheckPwd() {
		return mustCheckPwd;
	}
	public void setMustCheckPwd(boolean mustCheckPwd) {
		this.mustCheckPwd = mustCheckPwd;
	}
	public Integer getCdTerms() {
		return cdTerms;
	}
	public void setCdTerms(Integer cdTerms) {
		this.cdTerms = cdTerms;
	}
	public Integer getSixMonCdCount() {
		return sixMonCdCount;
	}
	public void setSixMonCdCount(Integer sixMonCdCount) {
		this.sixMonCdCount = sixMonCdCount;
	}
	public boolean isGtLastTermDate() {
		return gtLastTermDate;
	}
	public void setGtLastTermDate(boolean gtLastTermDate) {
		this.gtLastTermDate = gtLastTermDate;
	}
	/**
	 * 小额贷额度
	 * @return
	 */
	public BigDecimal getMicroCreditOTB() {
		return microCreditOTB;
	}
	/**
	 * 小额贷额度
	 * @return
	 */
	public void setMicroCreditOTB(BigDecimal microCreditOTB) {
		this.microCreditOTB = microCreditOTB;
	}

	public Date getSysOlTime() {
		return sysOlTime;
	}

	public void setSysOlTime(Date sysOlTime) {
		this.sysOlTime = sysOlTime;
	}

	public BigDecimal getLoanOTB() {
		return LoanOTB;
	}

	public void setLoanOTB(BigDecimal loanOTB) {
		LoanOTB = loanOTB;
	}

	public boolean isCloseSettleAcct() {
		return closeSettleAcct;
	}

	public void setCloseSettleAcct(boolean closeSettleAcct) {
		this.closeSettleAcct = closeSettleAcct;
	}

	public Indicator getNoCardPrzTxnSupportInd() {
		return noCardPrzTxnSupportInd;
	}

	public void setNoCardPrzTxnSupportInd(Indicator noCardPrzTxnSupportInd) {
		this.noCardPrzTxnSupportInd = noCardPrzTxnSupportInd;
	}

	public String getNoCardPrzTxnMobileNbr() {
		return noCardPrzTxnMobileNbr;
	}

	public void setNoCardPrzTxnMobileNbr(String noCardPrzTxnMobileNbr) {
		this.noCardPrzTxnMobileNbr = noCardPrzTxnMobileNbr;
	}

	public VerifyResult getIdNbrVerifyResult() {
		return idNbrVerifyResult;
	}

	public void setIdNbrVerifyResult(VerifyResult idNbrVerifyResult) {
		this.idNbrVerifyResult = idNbrVerifyResult;
	}

	public VerifyResult getMobiNbrVerifyResult() {
		return mobiNbrVerifyResult;
	}

	public void setMobiNbrVerifyResult(VerifyResult mobiNbrVerifyResult) {
		this.mobiNbrVerifyResult = mobiNbrVerifyResult;
	}

	public VerifyResult getChbNameAMVerifyResult() {
		return chbNameAMVerifyResult;
	}

	public void setChbNameAMVerifyResult(VerifyResult chbNameAMVerifyResult) {
		this.chbNameAMVerifyResult = chbNameAMVerifyResult;
	}

	public VerifyResult getChbNameNMVerifyResult() {
		return chbNameNMVerifyResult;
	}

	public void setChbNameNMVerifyResult(VerifyResult chbNameNMVerifyResult) {
		this.chbNameNMVerifyResult = chbNameNMVerifyResult;
	}
	
	public VerifyResult getChbReceiverNameFromBankVerifyResult() {
		return chbReceiverNameFromBankVerifyResult;
	}
	public void setChbReceiverNameFromBankVerifyResult(
			VerifyResult chbReceiverNameFromBankVerifyResult) {
		this.chbReceiverNameFromBankVerifyResult = chbReceiverNameFromBankVerifyResult;
	}
	public boolean isSpecTranNoCardSelfSvc() {
		return specTranNoCardSelfSvc;
	}

	public void setSpecTranNoCardSelfSvc(boolean specTranNoCardSelfSvc) {
		this.specTranNoCardSelfSvc = specTranNoCardSelfSvc;
	}

	public boolean isNameResp() {
		return nameResp;
	}

	public void setNameResp(boolean nameResp) {
		this.nameResp = nameResp;
	}

	public boolean isDynamicCodeResp() {
		return dynamicCodeResp;
	}

	public void setDynamicCodeResp(boolean dynamicCodeResp) {
		this.dynamicCodeResp = dynamicCodeResp;
	}

	public String getAcctVerifyName() {
		return acctVerifyName;
	}

	public void setAcctVerifyName(String acctVerifyName) {
		this.acctVerifyName = acctVerifyName;
	}

	public BigDecimal getDayCupxbAtmLimit() {
		return dayCupxbAtmLimit;
	}

	public void setDayCupxbAtmLimit(BigDecimal dayCupxbAtmLimit) {
		this.dayCupxbAtmLimit = dayCupxbAtmLimit;
	}

	public boolean isCupXborder() {
		return cupXborder;
	}

	public void setCupXborder(boolean cupXborder) {
		this.cupXborder = cupXborder;
	}

	public BigDecimal getDepositeCashOTB() {
		return depositeCashOTB;
	}

	public void setDepositeCashOTB(BigDecimal depositeCashOTB) {
		this.depositeCashOTB = depositeCashOTB;
	}

	public Date getOrigBizDate() {
		return origBizDate;
	}

	public void setOrigBizDate(Date origBizDate) {
		this.origBizDate = origBizDate;
	}

	public String getCountryCd() {
		return countryCd;
	}

	public void setCountryCd(String countryCd) {
		this.countryCd = countryCd;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Map<AuthReason, AuthAction> getReasons() {
		return reasons;
	}

	public void setReasons(Map<AuthReason, AuthAction> reasons) {
		this.reasons = reasons;
	}

	public List<String> getCheckList() {
		return checkList;
	}

	public void setCheckList(List<String> checkList) {
		this.checkList = checkList;
	}

	public Integer getDayAtmNbr() {
		return dayAtmNbr;
	}

	public void setDayAtmNbr(Integer dayAtmNbr) {
		this.dayAtmNbr = dayAtmNbr;
	}

	public BigDecimal getDayAtmLimit() {
		return dayAtmLimit;
	}

	public void setDayAtmLimit(BigDecimal dayAtmLimit) {
		this.dayAtmLimit = dayAtmLimit;
	}

	public Integer getDayRetailNbrLmt() {
		return dayRetailNbrLimit;
	}

	public void setDayRetailNbrLmt(Integer dayRetailNbrLimit) {
		this.dayRetailNbrLimit = dayRetailNbrLimit;
	}

	public BigDecimal getDayRetailAmtLmt() {
		return dayRetailAmtLimit;
	}

	public void setDayRetailAmtLmt(BigDecimal dayRetailAmtLimit) {
		this.dayRetailAmtLimit = dayRetailAmtLimit;
	}

	public Integer getDayCshNbrLimit() {
		return dayCshNbrLimit;
	}

	public void setDayCshNbrLimit(Integer dayCshNbrLimit) {
		this.dayCshNbrLimit = dayCshNbrLimit;
	}

	public BigDecimal getDayCashAmtLmt() {
		return dayCashAmtLimit;
	}

	public void setDayCashAmtLmt(BigDecimal dayCashAmtLimit) {
		this.dayCashAmtLimit = dayCashAmtLimit;
	}

	public Integer getDayXfroutNbrLmt() {
		return dayXfroutNbrLimit;
	}

	public void setDayXfroutNbrLmt(Integer dayXfroutNbrLimit) {
		this.dayXfroutNbrLimit = dayXfroutNbrLimit;
	}

	public BigDecimal getDayXfroutAmtLmt() {
		return dayXfroutAmtLimit;
	}

	public void setDayXfroutAmtLmt(BigDecimal dayXfroutAmtLimit) {
		this.dayXfroutAmtLimit = dayXfroutAmtLimit;
	}

	public Date getBizDate() {
		return bizDate;
	}

	public void setBizDate(Date bizDate) {
		this.bizDate = bizDate;
	}

	public String getCheckItem() {
		return checkItem;
	}

	public void setCheckItem(String checkItem) {
		this.checkItem = checkItem;
	}

	public BigDecimal getCashOTB() {
		return cashOTB;
	}

	public void setCashOTB(BigDecimal cashOTB) {
		this.cashOTB = cashOTB;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public boolean isBaseCurrency() {
		return baseCurrency;
	}

	public void setBaseCurrency(boolean baseCurrency) {
		this.baseCurrency = baseCurrency;
	}

	public BigDecimal getChbTransAmt() {
		return chbTransAmt;
	}

	public void setChbTransAmt(BigDecimal chbTransAmt) {
		this.chbTransAmt = chbTransAmt;
	}

	public String getChbCurr() {
		return chbCurr;
	}

	public void setChbCurr(String chbCurr) {
		this.chbCurr = chbCurr;
	}

	public BigDecimal getAccountOTB() {
		return accountOTB;
	}

	public void setAccountOTB(BigDecimal accountOTB) {
		this.accountOTB = accountOTB;
	}

	public boolean isAbroadNoVerify() {
		return abroadNoVerify;
	}

	public void setAbroadNoVerify(boolean abroadNoVerify) {
		this.abroadNoVerify = abroadNoVerify;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public boolean isElectronTrans() {
		return electronTrans;
	}

	public void setElectronTrans(boolean electronTrans) {
		this.electronTrans = electronTrans;
	}

	public boolean isRemoteTrans() {
		return remoteTrans;
	}

	public void setRemoteTrans(boolean remoteTrans) {
		this.remoteTrans = remoteTrans;
	}

	public ManualAuthFlag getManualAuthFlag() {
		return manualAuthFlag;
	}

	public void setManualAuthFlag(ManualAuthFlag manualAuthFlag) {
		this.manualAuthFlag = manualAuthFlag;
	}

	public AuthReason getAuthReason() {
		return authReason;
	}

	public void setAuthReason(AuthReason authReason) {
		this.authReason = authReason;
	}

	public AuthAction getAuthAction() {
		return authAction;
	}

	public void setAuthAction(AuthAction authAction) {
		this.authAction = authAction;
	}

	public String getResponsCode() {
		return responsCode;
	}

	public void setResponsCode(String responsCode) {
		this.responsCode = responsCode;
	}

	public BigDecimal getCustomerOTB() {
		return customerOTB;
	}

	public void setCustomerOTB(BigDecimal customerOTB) {
		this.customerOTB = customerOTB;
	}

	public PinEntryMode getPinEntryMode() {
		return pinEntryMode;
	}

	public void setPinEntryMode(PinEntryMode pinEntryMode) {
		this.pinEntryMode = pinEntryMode;
	}

	public TransFrom getTransFrom() {
		return transFrom;
	}

	public void setTransFrom(TransFrom transFrom) {
		this.transFrom = transFrom;
	}

	public InputSource getInputSource() {
		return inputSource;
	}

	public void setInputSource(InputSource inputSource) {
		this.inputSource = inputSource;
	}

	public TransMedium getTransMedium() {
		return transMedium;
	}

	public void setTransMedium(TransMedium transMedium) {
		this.transMedium = transMedium;
	}

	public AutoType getAutoType() {
		return autoType;
	}

	public void setAutoType(AutoType autoType) {
		this.autoType = autoType;
	}

	public BigDecimal getTransAmt() {
		return transAmt;
	}

	public void setTransAmt(BigDecimal transAmt) {
		this.transAmt = transAmt;
	}

	public String getTransCurr() {
		return transCurr;
	}

	public void setTransCurr(String transCurr) {
		this.transCurr = transCurr;
	}

	public AuthTransTerminal getTransTerminal() {
		return transTerminal;
	}

	public void setTransTerminal(AuthTransTerminal transTerminal) {
		this.transTerminal = transTerminal;
	}

	public AuthTransType getTransType() {
		return transType;
	}

	public void setTransType(AuthTransType transType) {
		this.transType = transType;
	}

	public AuthTransDirection getTransDirection() {
		return transDirection;
	}

	public void setTransDirection(AuthTransDirection transDirection) {
		this.transDirection = transDirection;
	}

	public boolean isInternetTrans() {
		return internetTrans;
	}

	public void setInternetTrans(boolean internetTrans) {
		this.internetTrans = internetTrans;
	}

	public boolean isqSpecTranNoCardSvc() {
		return qSpecTranNoCardSvc;
	}

	public void setqSpecTranNoCardSvc(boolean qSpecTranNoCardSvc) {
		this.qSpecTranNoCardSvc = qSpecTranNoCardSvc;
	}

	public String getAuthVerifyType() {
		return authVerifyType;
	}

	public void setAuthVerifyType(String authVerifyType) {
		this.authVerifyType = authVerifyType;
	}
	public boolean isVerifCvv2() {
		return verifCvv2;
	}
	public void setVerifCvv2(boolean verifCvv2) {
		this.verifCvv2 = verifCvv2;
	}
	
}
