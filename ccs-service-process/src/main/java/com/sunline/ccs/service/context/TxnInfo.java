package com.sunline.ccs.service.context;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.msentity.TNMTrustLoanSchedReqSubPlan;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 
 * @see 类名：TxnInfo
 * @see 描述：交易处理中中间业务信息
 *
 * @author wangz
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class TxnInfo {
	
	/**
	 * 联机标识  默认Y-联机
	 */
	private Indicator onlineFlag = Indicator.Y;
	
	/**
	 * 保单号
	 */
	private String guarantyid;
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
	 * 联机业务处理日期
	 */
	private Date bizDate;
	/**
	 * 原交易业务处理日期
	 */
	private Date origBizDate;

	/**
	 * 国家代码
	 */
	private String countryCd;

	private String expiryDate;

	/**
	 * 姓名
	 */
	private String acctVerifyName;

	private Date sysOlTime;

	private BigDecimal microCreditOTB;

	private Integer cdTerms;
	
	private Integer sixMonCdCount;
	
	private boolean gtLastTermDate;
	
	private BigDecimal origTransAmt;
	
	private String origServiceSn;
	
	private String origAcqId;
	
	private String origMerId;
	
	private String origAuthTxnTerminal;
	
	private String origServiceId;
	
	private String origRefNbr;
	
	private String origMerOrderId;
	
	//是否预约结清日到达
	private boolean isBookingDate = false;
	
	//在途贷记
	private BigDecimal onWayCrAmt;
	
	//贷款用途
	private LoanUsage loanUsage;
	
	//终端类型
	private String subTerminalType;
	
	//贷款注册状态
	private LoanRegStatus loanRegStatus;
	
	//贷款动作
	private LoanAction loanAction;
	
	//订单状态
	private OrderStatus orderStatus;
	
	//交易状态
	private AuthTransStatus authTransStatus;
	
	
	
	private String responsCode = "";
	//描述
	private String responsDesc = "";
	
	//外部交易时间  yyyyMMddHHmmss
	private String requestTime;
	
	//外部交易流水号 YG+yyyyMMddHHmmss+6位随机数
	private String serviceSn;
	
	//贷款流水号
	private String loanNo;
	
	//退、还款流水号
	private String reNo;
	
	//交易报文类型
	private String mti;
	
	//交易处理码
	private String procCode;
	
	//渠道来源
	private InputSource inputSource;
	
	//卡号
	private String cardNo;
	
	//支付前置返回码
	private String payRespCode;
	
	//支付前置返回描述
	private String payRespMessage;
	
	//支付前置返回交易状态
	private String payStatus;
	
	//支付前置业务返回码 （如代付 代扣查询 交易）
	private String payOthRespCode;
	

	//支付前置业务返回描述
	private String payOthRespMessage;
	
	//预约日期
	private Date bookingDate;
	
	//预约类型 1-试算  2-申请
	private String bookingType;
	
	//产品号
	private String productCd;
	
	/**
	 * 货币代码
	 */
	private String currencyCode = "156";
	
	//机构号
	private String org;
	
	/**
	 * 受理机构 32
	 */
	private String acqId;
	
	//订单号
	private Long orderId;
	
	//账号
	private Long acctNbr;
	
	//账户类型
	private AccountType acctType;
	
	//借据号
	private String dueBillNo;
	
	//authmeo的主键
	private Long logKv;
	
	//合同号
	private String contrNo;
	
	//最终余额方向
	private TransAmtDirection Direction;
	
	//贷款产品号
	private String loanCode;
	
	//申请单号
	private String applyNo;
	
	//统一客户号
	private String uuid;
	
	//信用额度
	private BigDecimal creditLmt;
	
	//是否加入寿险计划包
	private Indicator jionLifeInsuInd;
	
	
	//是否购买灵活还款计划包
	private Indicator prepayPkgInd;
	
	//手机号
	private String mobile;
	
	//合同有效期
	private Date contraExpireDate;
	
	//协议利率
	private BigDecimal agreeRate;
	
	//协议利率有效期
	private Date agreeRateExpireDate;
	
	//证件号
	private String idNo;
	
	//证件类型
	private IdType idType;
	
	private Long custId;
	
	private String refNbr;
	
	//子产品编号
	private String loanFeeDefId;
	
	/**
	 * 交易服务码
	 */
	public String serviceId;
	
	/**
	 * 商户id
	 */
	public String merId;

	/**
	 * 终端设备号
	 */
	public String authTxnTerminal;
	
	/**
	 * 商品/订单总金额
	 */
	private BigDecimal merchandiseAmt;
	
	/**
	 * 销售人员编号
	 */
	private String raId;

	/**
	 * 商品贷款订单号
	 */
	private String merchandiseOrder;
	
	/**
	 * 首付金额
	 */
	private BigDecimal downPaymentAmt;
	
	/**
	 * 趸交费
	 */
	private BigDecimal premiumAmt;
	
	/**
	 * 是否趸交费
	 */
	private Indicator premiumInd;
	
	/**
	 * 贷款金额
	 */
	private BigDecimal loanAmt;
	
	/**
	 * 拿去花 分期服务费
	 */
	private BigDecimal feeAmt;
	
	/**
	 * 贷款来源
	 */
	private String loanSource;
	
	/**
	 * 拿去花 已退、还本金
	 */
	private BigDecimal principal;
	
	/**
	 * 拿去花 已还罚金
	 */
	private BigDecimal penalty;
	
	/**
	 * 拿去花 原路返回金额
	 */
	private BigDecimal returnAmt;
	
	/**
	 * 还款类型
	 */
	private String repayType;
	
	/**
	 * 还款计划明细 列表
	 */
    private List<TNMTrustLoanSchedReqSubPlan> scheduleDetails;
   
    /**
     * 申请更改账单日
     */
    private String cycleDay;
    /**
     * 申请延期期数
     */
    private int applyDelayTerm;
    
    /**
     * 申请更改账单日
     */
    private String orginCycleDay;
	/**
	 * 更新时间
	 */
	private String changeTime;    
    /**
     * int 变量
     */
    private int variable;
    
	public int getApplyDelayTerm() {
		return applyDelayTerm;
	}

	public void setApplyDelayTerm(int applyDelayTerm) {
		this.applyDelayTerm = applyDelayTerm;
	}

	public String getRefNbr() {
		return refNbr;
	}

	public void setRefNbr(String refNbr) {
		this.refNbr = refNbr;
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

	public BigDecimal getCustomerOTB() {
		return customerOTB;
	}

	public void setCustomerOTB(BigDecimal customerOTB) {
		this.customerOTB = customerOTB;
	}

	public BigDecimal getAccountOTB() {
		return accountOTB;
	}

	public void setAccountOTB(BigDecimal accountOTB) {
		this.accountOTB = accountOTB;
	}

	public BigDecimal getCashOTB() {
		return cashOTB;
	}

	public void setCashOTB(BigDecimal cashOTB) {
		this.cashOTB = cashOTB;
	}

	public BigDecimal getDepositeCashOTB() {
		return depositeCashOTB;
	}

	public void setDepositeCashOTB(BigDecimal depositeCashOTB) {
		this.depositeCashOTB = depositeCashOTB;
	}

	public ManualAuthFlag getManualAuthFlag() {
		return manualAuthFlag;
	}

	public void setManualAuthFlag(ManualAuthFlag manualAuthFlag) {
		this.manualAuthFlag = manualAuthFlag;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public String getResponsCode() {
		return responsCode;
	}

	public void setResponsCode(String responsCode) {
		this.responsCode = responsCode;
	}

	public Date getBizDate() {
		return bizDate;
	}

	public void setBizDate(Date bizDate) {
		this.bizDate = bizDate;
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

	public String getAcctVerifyName() {
		return acctVerifyName;
	}

	public void setAcctVerifyName(String acctVerifyName) {
		this.acctVerifyName = acctVerifyName;
	}

	public Date getSysOlTime() {
		return sysOlTime;
	}

	public void setSysOlTime(Date sysOlTime) {
		this.sysOlTime = sysOlTime;
	}

	public BigDecimal getMicroCreditOTB() {
		return microCreditOTB;
	}

	public void setMicroCreditOTB(BigDecimal microCreditOTB) {
		this.microCreditOTB = microCreditOTB;
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

	public String getGuarantyid() {
		return guarantyid;
	}

	public void setGuarantyid(String guarantyid) {
		this.guarantyid = guarantyid;
	}

	public boolean isBookingDate() {
		return isBookingDate;
	}

	public void setBookingDate(boolean isBookingDate) {
		this.isBookingDate = isBookingDate;
	}

	public BigDecimal getOnWayCrAmt() {
		return onWayCrAmt;
	}

	public void setOnWayCrAmt(BigDecimal onWayCrAmt) {
		this.onWayCrAmt = onWayCrAmt;
	}

	public LoanUsage getLoanUsage() {
		return loanUsage;
	}

	public void setLoanUsage(LoanUsage loanUsage) {
		this.loanUsage = loanUsage;
	}

	public String getSubTerminalType() {
		return subTerminalType;
	}

	public void setSubTerminalType(String subTerminalType) {
		this.subTerminalType = subTerminalType;
	}

	public Long getLogKv() {
		return logKv;
	}

	public void setLogKv(Long logKv) {
		this.logKv = logKv;
	}

	public LoanRegStatus getLoanRegStatus() {
		return loanRegStatus;
	}

	public void setLoanRegStatus(LoanRegStatus loanRegStatus) {
		this.loanRegStatus = loanRegStatus;
	}

	public LoanAction getLoanAction() {
		return loanAction;
	}

	public void setLoanAction(LoanAction loanAction) {
		this.loanAction = loanAction;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public AuthTransStatus getAuthTransStatus() {
		return authTransStatus;
	}

	public void setAuthTransStatus(AuthTransStatus authTransStatus) {
		this.authTransStatus = authTransStatus;
	}

	public String getResponsDesc() {
		return responsDesc;
	}

	public void setResponsDesc(String responsDesc) {
		this.responsDesc = responsDesc;
	}

	public String getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}

	public String getServiceSn() {
		return serviceSn;
	}

	public void setServiceSn(String serviceSn) {
		this.serviceSn = serviceSn;
	}

	public String getMti() {
		return mti;
	}

	public void setMti(String mti) {
		this.mti = mti;
	}

	public String getProcCode() {
		return procCode;
	}

	public void setProcCode(String procCode) {
		this.procCode = procCode;
	}

	public InputSource getInputSource() {
		return inputSource;
	}

	public void setInputSource(InputSource inputSource) {
		this.inputSource = inputSource;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getPayRespCode() {
		return payRespCode;
	}

	public void setPayRespCode(String payRespCode) {
		this.payRespCode = payRespCode;
	}

	public String getPayRespMessage() {
		return payRespMessage;
	}

	public void setPayRespMessage(String payRespMessage) {
		this.payRespMessage = payRespMessage;
	}

	public String getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}

	public Indicator getOnlineFlag() {
		return onlineFlag;
	}

	public void setOnlineFlag(Indicator onlineFlag) {
		this.onlineFlag = onlineFlag;
	}

	public Date getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(Date bookingDate) {
		this.bookingDate = bookingDate;
	}

	public String getBookingType() {
		return bookingType;
	}

	public void setBookingType(String bookingType) {
		this.bookingType = bookingType;
	}

	public String getProductCd() {
		return productCd;
	}

	public void setProductCd(String productCd) {
		this.productCd = productCd;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getAcctNbr() {
		return acctNbr;
	}

	public void setAcctNbr(Long acctNbr) {
		this.acctNbr = acctNbr;
	}

	public AccountType getAcctType() {
		return acctType;
	}

	public void setAcctType(AccountType acctType) {
		this.acctType = acctType;
	}

	public String getDueBillNo() {
		return dueBillNo;
	}

	public void setDueBillNo(String dueBillNo) {
		this.dueBillNo = dueBillNo;
	}

	public String getContrNo() {
		return contrNo;
	}

	public void setContrNo(String contrNo) {
		this.contrNo = contrNo;
	}
	public String getPayOthRespCode() {
		return payOthRespCode;
	}

	public void setPayOthRespCode(String payOthRespCode) {
		this.payOthRespCode = payOthRespCode;
	}

	public String getPayOthRespMessage() {
		return payOthRespMessage;
	}

	public void setPayOthRespMessage(String payOthRespMessage) {
		this.payOthRespMessage = payOthRespMessage;
	}

	public TransAmtDirection getDirection() {
		return Direction;
	}

	public void setDirection(TransAmtDirection direction) {
		Direction = direction;
	}

	public String getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}

	public String getApplyNo() {
		return applyNo;
	}

	public void setApplyNo(String applyNo) {
		this.applyNo = applyNo;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public BigDecimal getCreditLmt() {
		return creditLmt;
	}

	public void setCreditLmt(BigDecimal creditLmt) {
		this.creditLmt = creditLmt;
	}

	public Indicator getJionLifeInsuInd() {
		return jionLifeInsuInd;
	}

	public void setJionLifeInsuInd(Indicator jionLifeInsuInd) {
		this.jionLifeInsuInd = jionLifeInsuInd;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Date getContraExpireDate() {
		return contraExpireDate;
	}

	public void setContraExpireDate(Date contraExpireDate) {
		this.contraExpireDate = contraExpireDate;
	}

	public BigDecimal getAgreeRate() {
		return agreeRate;
	}

	public void setAgreeRate(BigDecimal agreeRate) {
		this.agreeRate = agreeRate;
	}

	public Date getAgreeRateExpireDate() {
		return agreeRateExpireDate;
	}

	public void setAgreeRateExpireDate(Date agreeRateExpireDate) {
		this.agreeRateExpireDate = agreeRateExpireDate;
	}

	public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}

	public IdType getIdType() {
		return idType;
	}

	public void setIdType(IdType idType) {
		this.idType = idType;
	}

	public Long getCustId() {
		return custId;
	}

	public void setCustId(Long custId) {
		this.custId = custId;
	}

	public String getLoanFeeDefId() {
		return loanFeeDefId;
	}

	public void setLoanFeeDefId(String loanFeeDefId) {
		this.loanFeeDefId = loanFeeDefId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public BigDecimal getOrigTransAmt() {
		return origTransAmt;
	}

	public void setOrigTransAmt(BigDecimal origTransAmt) {
		this.origTransAmt = origTransAmt;
	}

	public String getOrigServiceSn() {
		return origServiceSn;
	}

	public void setOrigServiceSn(String origServiceSn) {
		this.origServiceSn = origServiceSn;
	}

	public String getOrigAcqId() {
		return origAcqId;
	}

	public void setOrigAcqId(String origAcqId) {
		this.origAcqId = origAcqId;
	}

	public String getOrigMerId() {
		return origMerId;
	}

	public void setOrigMerId(String origMerId) {
		this.origMerId = origMerId;
	}

	public String getOrigAuthTxnTerminal() {
		return origAuthTxnTerminal;
	}

	public void setOrigAuthTxnTerminal(String origAuthTxnTerminal) {
		this.origAuthTxnTerminal = origAuthTxnTerminal;
	}

	public String getOrigServiceId() {
		return origServiceId;
	}

	public void setOrigServiceId(String origServiceId) {
		this.origServiceId = origServiceId;
	}

	public String getOrigRefNbr() {
		return origRefNbr;
	}

	public void setOrigRefNbr(String origRefNbr) {
		this.origRefNbr = origRefNbr;
	}

	public String getMerId() {
		return merId;
	}

	public void setMerId(String merId) {
		this.merId = merId;
	}

	public String getAuthTxnTerminal() {
		return authTxnTerminal;
	}

	public void setAuthTxnTerminal(String authTxnTerminal) {
		this.authTxnTerminal = authTxnTerminal;
	}

	public BigDecimal getMerchandiseAmt() {
		return merchandiseAmt;
	}

	public void setMerchandiseAmt(BigDecimal merchandiseAmt) {
		this.merchandiseAmt = merchandiseAmt;
	}

	public String getRaId() {
		return raId;
	}

	public void setRaId(String raId) {
		this.raId = raId;
	}

	public String getOrigMerOrderId() {
		return origMerOrderId;
	}

	public void setOrigMerOrderId(String origMerOrderId) {
		this.origMerOrderId = origMerOrderId;
	}

	public String getMerchandiseOrder() {
		return merchandiseOrder;
	}

	public void setMerchandiseOrder(String merchandiseOrder) {
		this.merchandiseOrder = merchandiseOrder;
	}

	public BigDecimal getDownPaymentAmt() {
		return downPaymentAmt;
	}

	public void setDownPaymentAmt(BigDecimal downPaymentAmt) {
		this.downPaymentAmt = downPaymentAmt;
	}

	public BigDecimal getPremiumAmt() {
		return premiumAmt;
	}

	public void setPremiumAmt(BigDecimal premiumAmt) {
		this.premiumAmt = premiumAmt;
	}

	public Indicator getPremiumInd() {
		return premiumInd;
	}

	public void setPremiumInd(Indicator premiumInd) {
		this.premiumInd = premiumInd;
	}

	public BigDecimal getLoanAmt() {
		return loanAmt;
	}

	public void setLoanAmt(BigDecimal loanAmt) {
		this.loanAmt = loanAmt;
	}

	public BigDecimal getFeeAmt() {
		return feeAmt;
	}

	public void setFeeAmt(BigDecimal feeAmt) {
		this.feeAmt = feeAmt;
	}

	public String getLoanSource() {
		return loanSource;
	}

	public void setLoanSource(String loanSource) {
		this.loanSource = loanSource;
	}

	public String getLoanNo() {
		return loanNo;
	}

	public void setLoanNo(String loanNo) {
		this.loanNo = loanNo;
	}

	public String getReNo() {
		return reNo;
	}

	public void setReNo(String reNo) {
		this.reNo = reNo;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}

	public BigDecimal getPenalty() {
		return penalty;
	}

	public void setPenalty(BigDecimal penalty) {
		this.penalty = penalty;
	}

	public BigDecimal getReturnAmt() {
		return returnAmt;
	}

	public void setReturnAmt(BigDecimal returnAmt) {
		this.returnAmt = returnAmt;
	}

	public String getRepayType() {
		return repayType;
	}

	public void setRepayType(String repayType) {
		this.repayType = repayType;
	}

	public List<TNMTrustLoanSchedReqSubPlan> getScheduleDetails() {
		return scheduleDetails;
	}

	public void setScheduleDetails(List<TNMTrustLoanSchedReqSubPlan> scheduleDetails) {
		this.scheduleDetails = scheduleDetails;
	}

	public Indicator getPrepayPkgInd() {
		return prepayPkgInd;
	}

	public void setPrepayPkgInd(Indicator prepayPkgInd) {
		this.prepayPkgInd = prepayPkgInd;
	}

	public String getCycleDay() {
		return cycleDay;
	}

	public void setCycleDay(String cycleDay) {
		this.cycleDay = cycleDay;
	}

	public String getOrginCycleDay() {
		return orginCycleDay;
	}

	public void setOrginCycleDay(String orginCycleDay) {
		this.orginCycleDay = orginCycleDay;
	}

	public int getVariable() {
		return variable;
	}

	public void setVariable(int variable) {
		this.variable = variable;
	}

	public String getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(String changeTime) {
		this.changeTime = changeTime;
	}
	
}
