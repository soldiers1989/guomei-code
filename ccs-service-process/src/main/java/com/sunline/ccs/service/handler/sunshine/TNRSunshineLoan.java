package com.sunline.ccs.service.handler.sunshine;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.card.LuhnMod10;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctNbr;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardLmMapping;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardUsage;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.server.repos.RCcsEmployee;
import com.sunline.ccs.infrastructure.server.repos.RCcsLinkman;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctNbr;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.CcsCardnbrGrt;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsEmployee;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardnbrGrt;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsEmployee;
import com.sunline.ccs.infrastructure.shared.model.QCcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.loan.McLoanProvideImpl.LoanInstallmentFeeMethodimple;
import com.sunline.ccs.loan.McLoanProvideImpl.LoanSVCFeeMethodimple;
import com.sunline.ccs.loan.McLoanProvideImpl.LoanStampTAXMethodimple;
import com.sunline.ccs.loan.McLoanProvideImpl.LoanlnsuranceMethodimple;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.FirstCardFeeInd;
import com.sunline.ccs.param.def.enums.FirstUsageIndicator;
import com.sunline.ccs.param.def.enums.LimitType;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.entity.S30001LoanReq;
import com.sunline.ccs.service.entity.S30001LoanResp;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CcCardNbrService;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.CardFetchMethod;
import com.sunline.ppy.dictionary.enums.CorpStructure;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.enums.DualBillingInd;
import com.sunline.ppy.dictionary.enums.EmpPositionAttrType;
import com.sunline.ppy.dictionary.enums.EmpType;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.Relationship;
import com.sunline.ppy.dictionary.enums.RenewInd;
import com.sunline.ppy.dictionary.enums.SmsInd;
import com.sunline.ppy.dictionary.enums.StmtMediaType;
import com.sunline.ppy.dictionary.enums.TitleOfTechnicalType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRSunshineLoan
 * @see 描述：马上-阳光保险贷 放款申请
 *
 * @see 创建日期： 2015年8月10日 
 * @author lizz
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */

@Service
public class TNRSunshineLoan {
	private Logger logger = LoggerFactory.getLogger(getClass());

    private static final BigDecimal DEFAULT_LIMIT = new BigDecimal("9999999999999");
    @Autowired
    private UnifiedParameterFacility unifiedParameterFacility;
    @Autowired
    CustAcctCardFacility queryFacility;
    @Autowired
    private RCcsCustomer rCustomer;
    @Autowired
    private RCcsAddress rAddress;
    @Autowired
    private RCcsLinkman rLinkman;
    @Autowired
    private RCcsAcct rAcct;
    @Autowired
    private RCcsAcctO rAcctO;
    @Autowired
    private RCcsCard rCcsCard;
    @Autowired
    private RCcsEmployee rCcsEmployee;
    @Autowired
    private RCcsAcctNbr rCcsAcctNbr;
    @Autowired
    private RCcsCardLmMapping rCcsCardLmMapping;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private SunshineCommService sunshineCommService;
	@Autowired
    private RCcsCardUsage rCcsCardUsage;
	@Autowired
	private RCcsCustomerCrlmt rCustomerCrLmt;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
    private RCcsCardO rCcsCardO;
	@Autowired
	private PaymentFacility paymentFacility;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Autowired
	private CcCardNbrService cardNbrServiceImpl;
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	TxnUtils txnUtils;
	public S30001LoanResp handler(S30001LoanReq req) throws ProcessException {
		LogTools.printLogger(logger, "S30001LoanReq", "放款申请", req, true);
		LogTools.printObj(logger, req, "S30001LoanReq请求参数");
		S30001LoanResp resp = new S30001LoanResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		String payJson = "";
		
		context.setTxnInfo(txnInfo);
		context.setSunshineRequestInfo(req);
		
		try {
			this.initTxnInfo(req, txnInfo);
			
			//重复交易
			sunshineCommService.checkReqAndRepeatTxn(context);
			
			this.checkReq(req);

		    // 确定产品参数
			this.getProdParam(context);
			
			//单独事务处理
			payJson = this.bizProc(context);
			
			//处理结果，单独事务处理
			sunshineCommService.payResultProc(context,payJson);
			
		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			
//			this.exceptionProc(context, pe);
			sunshineCommService.preException(pe, pe, txnInfo);
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();//e不是pe，处理逻辑与没有code的pe一样
			sunshineCommService.preException(pe, pe, txnInfo);
//			this.exceptionProc(context, pe);
		}finally{
			LogTools.printLogger(logger, "S30001LoanResp", "放款申请", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo);
		LogTools.printObj(logger, resp, "S30001LoanResp响应参数");
		return resp;
		
	}

	/**
	 * 检查报文
	 * @param req
	 */
	private void checkReq(S30001LoanReq req) {
		
		//检查开户行号
		sunshineCommService.checkBankCode(req.bankcode);
		
		//期数检查
		if(Integer.valueOf(req.getLoanterm()).intValue()<=0){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{LOANTERM},必须大于0");
		}
		
		if(!StringUtils.equals(req.country,"156")){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{COUNTRY},必须为156");
		}
		
	}

	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(S30001LoanResp resp, S30001LoanReq req, TxnInfo txnInfo) {
		resp.setGuarantyid(req.getGuarantyid());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}

	/**
	 * 单独事务处理业务逻辑，保证订单等基础信息能保存完整
	 * @param context
	 * @param payJson
	 * @return
	 */
	@Transactional
	private String bizProc(TxnContext context) {
		if(logger.isDebugEnabled())
			logger.debug("独立事务，业务处理开始--bizProc");
		
		TxnInfo txnInfo = context.getTxnInfo();
		S30001LoanReq req = (S30001LoanReq) context.getSunshineRequestInfo();
		String payJson = "";
		
		this.validateForApply(context);
		
		// 确定客户
	    this.mergeCustomer(context);
	    
	    // 确定地址
	    if (StringUtils.isNotBlank(req.province)) {
			this.mergeAddress(txnInfo.getOrg(), context.getCustomer().getCustId(), AddressType.H, req.familyadd, req.city,
					  req.country, req.areacode, req.province, req.familytel, req.zipcode);
	    }
	    //用家庭 市,国家,区,省 放入单位地址
	    if (StringUtils.isNotBlank(req.workadd)) {
			this.mergeAddress(txnInfo.getOrg(), context.getCustomer().getCustId(), AddressType.C, req.workadd, req.city,
					req.country, req.areacode, req.province, req.worktel,req.workzip);
	    }

	    // 确定联系人, 附卡没有联系人
	    if (req.linkrelation1 != null) {
	    	this.mergeLinkman(txnInfo.getOrg(), context.getCustomer().getCustId(), 
	    			req.linkrelation1, req.link1, null, req.linkmobile1);
	    }
	    if (req.linkrelation2 != null) {
			this.mergeLinkman(txnInfo.getOrg(), context.getCustomer().getCustId(), 
					req.linkrelation2, req.link2,null, req.linkmobile2);
	    }

	    // 新建或更新工作信息
	    this.mergeEmployee(txnInfo.getOrg(), context.getCustomer().getCustId(), req.workcorp, req.worktel, null,
			       req.posionlevel, req.unitkind, null, req.position,
			       req.monthlywages.multiply(BigDecimal.valueOf(12)), 
			       req.monthlywages.multiply(BigDecimal.valueOf(12)));

	    // 设置客户的额度信息
		this.mergeCustomerCrLmt(context);
	    
	    //确定账户及联机账户
	    this.mergeAcctAndO(context);

	    // 建立卡片
	    this.mergeCard(context);
	    
	    //检查otb
		sunshineCommService.valiLoanOtb(context);

		//进入放款授权
	    this.mergeLoanReg(context);
	    
	    sunshineCommService.saveCcsAuthmemoO(context);
	    
		//调用支付
		sunshineCommService.installOrder(context);
		
		
		//组装支付指令
		payJson = paymentFacility.installPaySinPaymentCommand(context.getOrder());
	
		return payJson;
	}

	/**
	 * 建账   处理账户
	 * @param context
	 * @return
	 */
	private void mergeAcctAndO(TxnContext context) {
		S30001LoanReq req = (S30001LoanReq) context.getSunshineRequestInfo();
		AccountAttribute acctAttr = context.getAccAttr();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcctO accounto = null;
		CcsAcct account = null;
		
		// 取账号, 如果是共享额度账户，则acctNo取现有本币或外币的共享账号
		Long acctNo = this.mergeAcctNbr(context);
		// 建账
		account = rAcct.findOne(new CcsAcctKey(acctNo, acctAttr.accountType));
		
		context.setAccount(account);
		
		if(null != account) {
			// 判断账户上是否有锁定码存在，存在则无法分期
			if(account.getDdBankAcctNbr()==null){
				throw new ProcessException(MsRespCode.E_1011.getCode(), MsRespCode.E_1011.getMessage());
			}
			accounto = rAcctO.findOne(new CcsAcctOKey(acctNo,acctAttr.accountType));
			
			context.setAccounto(accounto);
			sunshineCommService.checkBlockCode(context);
		}else {
			account = this.mergeAcct(acctNo, context);
			accounto = this.mergeAccto(account, req);
		}
		
		txnInfo.setAcctNbr(acctNo);
		txnInfo.setAcctType(acctAttr.accountType);
		context.setAccounto(accounto);
		context.setAccount(account);
	}
	
	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(S30001LoanReq req, TxnInfo txnInfo) {
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentDebit);
		txnInfo.setGuarantyid(req.getGuarantyid());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setMti("0208");
		txnInfo.setProcCode("480001");
		txnInfo.setProductCd(req.productcd);
		txnInfo.setLoanUsage(LoanUsage.L);
		txnInfo.setTransAmt(req.businesssum);
		txnInfo.setOrg(OrganizationContextHolder.getCurrentOrg());
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setAuthTransStatus(AuthTransStatus.N);
		txnInfo.setContrNo(req.contractno);
		txnInfo.setDirection(TransAmtDirection.D);
		txnInfo.setUuid(req.dataid);//统一客户号
		txnInfo.setIdNo(req.certid);//证件号
		txnInfo.setIdType(req.certtype);//证件类型
		txnInfo.setCdTerms(Integer.valueOf(req.getLoanterm()));
		txnInfo.setServiceId(req.getServiceId());
		
		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "txnInfo交易中间信息");
		
	}
	
	/**
	 * 保存贷款注册信息
	 * @param context
	 */
	private void mergeLoanReg(TxnContext context) {
		LogTools.printObj(logger, "", "保存贷款注册信息mergeLoanReg");
		
		S30001LoanReq req = (S30001LoanReq) context.getSunshineRequestInfo();
		CcsCard card = context.getCard();
		CcsAcct acct = context.getAccount();
		LoanPlan loanPlan = context.getLoanPlan();
		LoanFeeDef loanFeeDef = context.getLoanFeeDef();

		String refNo = context.getTxnInfo().getRefNbr();//内部流水号  b037
		CcsLoanReg loanReg = sunshineCommService.genLoanReg(Integer.valueOf(req.getLoanterm()), 
				req.businesssum, refNo, card.getLogicCardNbr(),card.getCardBasicNbr(), acct.getAcctNbr().longValue(),
				acct.getAcctType(), loanPlan.loanCode, unifiedParamFacilityProvide.BusinessDate());

		loanReg.setAgreementRateInd(Indicator.Y);
		loanReg.setInterestRate(null==loanFeeDef.interestRate?BigDecimal.ZERO:loanFeeDef.interestRate);//基础利率
		loanReg.setLoanFeeMethod(loanFeeDef.loanFeeMethod);//分期手续费收取方式
		loanReg.setLifeInsuFeeRate(null==loanFeeDef.lifeInsuFeeRate?BigDecimal.ZERO:loanFeeDef.lifeInsuFeeRate);//寿险计划包费率
		loanReg.setLifeInsuFeeMethod(loanFeeDef.lifeInsuFeeMethod);//寿险计划包费收取方式
		loanReg.setInsuranceRate(null==req.confirmvaluemonthrate?loanFeeDef.insRate:
			req.confirmvaluemonthrate.multiply(new BigDecimal(req.loanterm)));//保险月费率 20151127
		loanReg.setInsAmt(loanFeeDef.insAmt==null?BigDecimal.ZERO:loanFeeDef.insAmt);
		loanReg.setLoanInsFeeMethod(loanFeeDef.insCollMethod);//保险费收取方式
		loanReg.setStampdutyRate(null==loanFeeDef.stampRate?BigDecimal.ZERO:loanFeeDef.stampRate);//印花税率
		loanReg.setStampdutyMethod(loanFeeDef.stampMethod);//印花税率收取方式
		loanReg.setCompoundRate(null==loanFeeDef.compoundIntTableId?BigDecimal.ZERO:loanFeeDef.compoundIntTableId);//复利利率
		loanReg.setPenaltyRate(null==loanFeeDef.penaltyIntTableId?BigDecimal.ZERO:loanFeeDef.penaltyIntTableId);//罚息利率
		loanReg.setFeeRate(null == loanFeeDef.feeRate ? BigDecimal.ZERO: loanFeeDef.feeRate);
		loanReg.setJoinLifeInsuInd(Indicator.N);//是否购买寿险计划包
		loanReg.setSvcfeeMethod(loanFeeDef.loanFeeMethod);
		loanReg.setReplacePenaltyRate(acct.getReplacePenaltyRate());
		
		//总寿险计划包费
		loanReg.setTotLifeInsuAmt(BigDecimal.ZERO);
		
		//总保费=贷款金额*总期数*保费率
		loanReg.setInsuranceAmt(LoanlnsuranceMethodimple.valueOf(loanReg.getLoanInsFeeMethod().toString()).loanlnsurance(loanReg, loanFeeDef,0));
		

		
		//总手续费
		loanReg.setLoanInitFee(LoanInstallmentFeeMethodimple.valueOf(loanReg.getLoanFeeMethod().toString()).loanInstallmentFee(loanReg, loanFeeDef,0));
		//贷款服务费
		if(null != loanReg.getSvcfeeMethod()){
			loanReg.setLoanSvcFee(LoanSVCFeeMethodimple.valueOf(loanReg.getSvcfeeMethod().toString()).loanSVCFee(loanReg,loanFeeDef,0));
		}else{
			loanReg.setLoanSvcFee(BigDecimal.ZERO);
		}
		//总印花税
		loanReg.setStampdutyAmt(LoanStampTAXMethodimple.valueOf(loanReg.getStampdutyMethod().toString()).loanStampTAX(loanReg, loanFeeDef,0));
		
		loanReg.setLoanFeeDefId(loanFeeDef.loanFeeDefId.toString());//分期子产品编号
		
		loanReg.setLoanType(loanPlan.loanType);
		loanReg.setContrNbr(req.getContractno());
		loanReg.setGuarantyId(req.getGuarantyid());
		
		loanReg.setDueBillNo(context.getTxnInfo().getRefNbr());//借据号
		rCcsLoanReg.save(loanReg);
		
		context.getTxnInfo().setDueBillNo(loanReg.getDueBillNo());
		context.setLoanReg(loanReg);
	}
	
	/**
	 * 获取产品参数
	 * @param context
	 */
	private void getProdParam(TxnContext context){
		if(logger.isDebugEnabled())
			logger.debug("获取产品参数--getProdParam");
		S30001LoanReq req = (S30001LoanReq) context.getSunshineRequestInfo();
		
		try{
			ProductCredit productCredit = unifiedParameterFacility.loadParameter(req.productcd, ProductCredit.class);
		    Product product = unifiedParameterFacility.loadParameter(req.productcd, Product.class);
		    AccountAttribute acctAttr = unifiedParameterFacility.loadParameter(
		    		productCredit.accountAttributeId, AccountAttribute.class);
		    LoanPlan loanPlan = unifiedParamFacilityProvide.loanPlan(req.productcd, LoanType.MCEI);
			LoanFeeDef loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(req.productcd, LoanType.MCEI, Integer.valueOf(req.getLoanterm()));
		    
			if(null == loanPlan || null == loanFeeDef){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage());
			}
			
		    context.setProduct(product);
		    context.setProductCredit(productCredit);
		    context.setAccAttr(acctAttr);
		    context.setLoanPlan(loanPlan);
		    context.setLoanFeeDef(loanFeeDef);
		    logger.debug("accountAttributeId --------- " + productCredit.accountAttributeId);
		}catch(Exception e){
			if(logger.isErrorEnabled())
				logger.error("获取产品参数异常,产品号["+req.getProductcd()+"]",e);
			throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage());
		}

	}

	/**
     * @see 方法名：mergeCustomer
     * @see 描述：客户信息维护
     * @see 创建日期：2015年8月10日下午17:17
     * @author lizz
     * 
     * @param req
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void mergeCustomer(TxnContext context) {
    	LogTools.printObj(logger, "", "客户信息维护mergeCustomer");
    	S30001LoanReq req = (S30001LoanReq) context.getSunshineRequestInfo();
    	TxnInfo txnInfo = context.getTxnInfo();
		QCcsCustomer qCustomer = QCcsCustomer.ccsCustomer;
		CcsCustomer customer = rCustomer.findOne(qCustomer.idType.eq(req.certtype)
				.and(qCustomer.idNo.eq(req.certid))
				.and(qCustomer.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " "))));
	
		if (customer == null) {
		    logger.debug("新客户");
		    logger.debug("idType --------------------- " + req.certtype);
		    logger.debug("cardNo --------------------- " + CodeMarkUtils.markIDCard(req.certid));
	
		    customer = new CcsCustomer();
	
		    customer.setOrg(txnInfo.getOrg());
		    customer.setCustSource(txnInfo.getInputSource());
		    customer.setIdNo(req.certid);
		    customer.setIdType(req.certtype);
//		    customer.setCustId(Long.valueOf(req.dataid));
		    customer.setSetupDate(txnInfo.getBizDate());
		    customer.setInternalCustomerId(req.dataid);
		    customer.setUserAmt1(BigDecimal.ZERO);
		    customer.setUserAmt2(BigDecimal.ZERO);
		    customer.setUserAmt3(BigDecimal.ZERO);
		    customer.setUserAmt4(BigDecimal.ZERO);
		    customer.setUserAmt5(BigDecimal.ZERO);
		    customer.setUserAmt6(BigDecimal.ZERO);
		    customer.setUserNumber1(0);
		    customer.setUserNumber2(0);
		    customer.setUserNumber3(0);
		    customer.setUserNumber4(0);
		    customer.setUserNumber5(0);
		    customer.setUserNumber6(0);
		    customer.setOncardName("-");
		} else {
		    logger.debug("老客户");
		    logger.debug("idType --------------------- " + req.certtype);
		    logger.debug("idNo --------------------- " + CodeMarkUtils.markIDCard(req.certid));
		    logger.debug("custId --------------------- " + customer.getCustId());
		}
	
		// 更新客户信息，上送更新，否则不更新
		if (req.posionlevel != null) customer.setTitle(req.posionlevel);//职位
		if (req.customername != null) customer.setName(req.customername);
		if (req.sex != null) customer.setGender(req.sex);
		if (req.birthday != null) customer.setBirthday(req.birthday);
		if (StringUtils.isNotBlank(req.country)) customer.setNationality(req.country);//国籍
		if (req.marriage != null) customer.setMaritalStatus(req.marriage);
		if (req.edudegree != null) customer.setEduexperience(req.edudegree);
		if (req.eduexperience != null) customer.setEducation(req.eduexperience);
		if (req.workbegindate != null) customer.setWorkbegindate(req.workbegindate);
		if (req.incomeflag != null) customer.setIncomeflag(req.incomeflag);
		if (req.monthlywages != null) customer.setMonthlywages(req.monthlywages);
		if (StringUtils.isNotBlank(req.familytel)) customer.setHomePhone(req.familytel);
		if (StringUtils.isNotBlank(req.mobile)) customer.setMobileNo(req.mobile);
			customer.setSocialInsAmt(BigDecimal.ZERO);
		if (StringUtils.isNotBlank(req.workcorp)) customer.setCorpName(req.workcorp);

		rCustomer.save(customer);
		context.setCustomer(customer);
    }
	

    /**
     * @see 方法名：mergeAddress
     * @see 描述：地址信息维护
     * @see 创建日期：2015年8月10日
     * @author lizz
     * 
     * @param org
     * @param bigDecimal
     * @param addressType
     * @param address
     * @param city
     * @param countryCode
     * @param district
     * @param state
     * @param phone
     * @param zip
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private CcsAddress mergeAddress(String org, Long custId, AddressType addressType, String address, String city,
	    String countryCode, String district, String state, String phone, String zip) {
    	LogTools.printObj(logger, addressType, "地址信息维护mergeAddress");
    	
		QCcsAddress qTmAddress = QCcsAddress.ccsAddress;
		CcsAddress addr = rAddress.findOne(qTmAddress.org.eq(org)
				.and(qTmAddress.custId.eq(custId))
				.and(qTmAddress.addrType.eq(addressType)));
	
		if (addr == null) {
		    addr = new CcsAddress();
		    addr.setOrg(org);
		    addr.setAddrType(addressType);
		    addr.setCustId(custId);
		}
		addr.setAddress(getDefaultValue(address, addr.getAddress(), null, String.class));
		addr.setCity(getDefaultValue(city, addr.getCity(), null, String.class));
		addr.setCountryCode(getDefaultValue(countryCode, addr.getCountryCode(), "156", String.class));
		addr.setDistrict(getDefaultValue(district, addr.getDistrict(), null, String.class));
		addr.setState(getDefaultValue(state, addr.getState(), null, String.class));
		addr.setPhone(getDefaultValue(phone, addr.getPhone(), null, String.class));
		addr.setPostcode(getDefaultValue(zip, addr.getPostcode(), null, String.class));
	
		return rAddress.save(addr);
    }

    /**
     * @see 方法名：mergeCustomerCrLmt
     * @see 描述： 获取(创建)客户层信用额度
     * @see 创建日期：2015-8-10
     * @author lizz
     * 
     * @param app
     * @param product
     * @param acctAttr
     * @param customer
     * @param basicCustomer
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void mergeCustomerCrLmt(TxnContext context) {
    	LogTools.printObj(logger, "", "获取(创建)客户层信用额度mergeCustomerCrLmt");
    	
    	Product product = context.getProduct();
    	AccountAttribute acctAttr = context.getAccAttr();
    	CcsCustomer customer = context.getCustomer();
    	TxnInfo txnInfo = context.getTxnInfo();
		CcsCustomerCrlmt custLimit;
		BigDecimal custLimitAmt = txnInfo.getTransAmt().multiply(BigDecimal.ONE.add(acctAttr.ovrlmtRate));
	
		if (customer.getCustLmtId() != null) {
		    custLimit = rCustomerCrLmt.findOne(customer.getCustLmtId());
	
		    // 更新客户级额度
		    if (null != custLimit) {
		    	if(custLimitAmt.compareTo(custLimit.getCreditLmt()) > 0) {
		    		custLimit.setCreditLmt(txnInfo.getTransAmt());
		    	}
		    }else {
			    // 到这步如果还没有custLimitId，表明是新建的主卡客户，需要新建一个CustLimitO
			    custLimit = new CcsCustomerCrlmt();
			    custLimit.setOrg(txnInfo.getOrg());
			    custLimit.setLmtCalcMethod(LimitType.H);
			    custLimit.setLmtCategroy(product.productType.getLimitCategory());
			    custLimit.setCreditLmt(custLimitAmt);
			    rCustomerCrLmt.save(custLimit);
			    customer.setCustLmtId(custLimit.getCustLmtId());
			}
		} else {
		    // 到这步如果还没有custLimitId，表明是新建的主卡客户，需要新建一个CustLimitO
		    custLimit = new CcsCustomerCrlmt();
		    custLimit.setOrg(txnInfo.getOrg());
		    custLimit.setLmtCalcMethod(LimitType.H);
		    custLimit.setLmtCategroy(product.productType.getLimitCategory());
		    custLimit.setCreditLmt(custLimitAmt);
		    rCustomerCrLmt.save(custLimit);
		    customer.setCustLmtId(custLimit.getCustLmtId());
		}
		
		context.setCustLimitO(custLimit);
    }
    
    /**
     * 
     * @see 方法名：mergeLinkman
     * @see 描述：创建(更新)联系人信息
     * @see 创建日期：2015-8-10
     * @author lizz
     * 
     * @param org
     * @param custId
     * @param relationship
     * @param name
     * @param gender
     * @param mobileNo
     * @param birthday
     * @param corpName
     * @param idType
     * @param idNo
     * @param corpPhone
     * @param corpFax
     * @param corpPost
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private CcsLinkman mergeLinkman(String org, Long custId, Relationship relationship, String name, Gender gender,
	    String mobileNo) {
    	LogTools.printObj(logger, relationship, "创建(更新)联系人信息mergeLinkman");
		QCcsLinkman qTmContact = QCcsLinkman.ccsLinkman;
		CcsLinkman contact =
			rLinkman.findOne(qTmContact.org.eq(org).and(qTmContact.custId.eq(custId))
				.and(qTmContact.relationship.eq(relationship)));
	
		if (contact == null) {
		    contact = new CcsLinkman();
		    contact.setOrg(org);
		    contact.setCustId(custId);
		    contact.setRelationship(relationship);
		}
		contact.setName(getDefaultValue(name, contact.getName(), null, String.class));
		contact.setGender(getDefaultValue(gender, contact.getGender(), null, Gender.class));
		contact.setMobileNo(getDefaultValue(mobileNo, contact.getMobileNo(), null, String.class));
		
		return rLinkman.save(contact);
    }

    /**
     * 
     * @see 方法名：mergeEmployee
     * @see 描述：创建(更新)工作信息
     * @see 创建日期：2015-6-23下午6:59:16
     * @author ChengChun
     * 
     * @param org
     * @param custId
     * @param companyName
     * @param companyPhone
     * @param companyFax
     * @param title
     * @param industryCategory
     * @param companyCategory
     * @param titleOfTechnical
     * @param revenuePerYear
     * @param familyAverageRevenue
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private CcsEmployee mergeEmployee(String org, Long custId, String companyName, String companyPhone,
	    String companyFax, EmpPositionAttrType title, EmpType industryCategory, CorpStructure companyCategory,
	    TitleOfTechnicalType titleOfTechnical, BigDecimal revenuePerYear, BigDecimal familyAverageRevenue) {
    	LogTools.printObj(logger, title, "创建(更新)工作信息mergeEmployee");
		QCcsEmployee qTmEmployee = QCcsEmployee.ccsEmployee;
		CcsEmployee employee = rCcsEmployee.findOne(qTmEmployee.org.eq(org).and(qTmEmployee.custId.eq(custId)));
	
		if (employee == null) {
		    employee = new CcsEmployee();
		    employee.setOrg(org);
		    employee.setCustId(custId);
		    employee.setCorpName(companyName);
		    employee.setCorpTelephNbr(companyPhone);
		    employee.setCorpFax(companyFax);
		}
		employee.setCorpPosition(getDefaultValue(title, employee.getCorpPosition(), EmpPositionAttrType.Z,
							 EmpPositionAttrType.class));
		employee.setCorpIndustryCategory(getDefaultValue(industryCategory, employee.getCorpIndustryCategory(),
								 EmpType.Z, EmpType.class));
		employee.setCorpStructure(getDefaultValue(companyCategory, employee.getCorpStructure(), CorpStructure.Z,
							  CorpStructure.class));
		employee.setCorpTechTitle(getDefaultValue(titleOfTechnical, employee.getCorpTechTitle(),
							  TitleOfTechnicalType.D, TitleOfTechnicalType.class));
		employee.setIncomePy(getDefaultValue(revenuePerYear, employee.getIncomePy(), BigDecimal.ZERO, BigDecimal.class));
		employee.setFamilyIncomePyp(getDefaultValue(familyAverageRevenue, employee.getFamilyIncomePyp(),
							    BigDecimal.ZERO, BigDecimal.class));
	
		
		return rCcsEmployee.save(employee);
    }

    /**
     * @see 方法名：mergeAcctNbr
     * @see 描述：创建(获取)账号
     * @see 创建日期：2015-8-10
     * @author lizz
     * 
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private Long mergeAcctNbr(TxnContext context) {
    	LogTools.printObj(logger, "", "创建(获取)账号mergeAcctNbr");
    	AccountAttribute acctAttr = context.getAccAttr();
    	TxnInfo txnInfo = context.getTxnInfo();
    	String org = OrganizationContextHolder.getCurrentOrg();
		Long acctNo = null;
		
		if (acctAttr.accountType.isSharedCredit()) {
			if(logger.isDebugEnabled())
				logger.debug("账户属性参数的账户类型为共享[{}]",acctAttr.accountType);
			throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage());
		}
		//一个合同一个账户
		if(logger.isDebugEnabled())
			logger.debug("账户已存在，合同号为：[{}]",txnInfo.getContrNo());
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		CcsAcct account = rAcct.findOne(qCcsAcct.contrNbr.eq(txnInfo.getContrNo()));
		if(null != account) {
			throw new ProcessException(MsRespCode.E_1013.getCode(),MsRespCode.E_1013.getMessage());
		}else {
			// 不共享或没有账号则生成账号
			CcsAcctNbr acctNbr = new CcsAcctNbr();
			acctNbr.setOrg(org);
			logger.info("--------------------生成账号--------------------");
			rCcsAcctNbr.save(acctNbr);
			
			acctNo = acctNbr.getAcctNbr();
			
			logger.debug("获取账号, acctNo=" + acctNo);
		}
		
		return acctNo;
    }

    /**
     * 
     * @see 方法名：mergeAcct
     * @see 描述：创建(更新)账户
     * @see 创建日期：2015-6-23下午6:52:44
     * @author lizz
     * 
     * @return
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private CcsAcct mergeAcct(Long acctNo, TxnContext context) {
    	LogTools.printObj(logger, "账号："+acctNo, "创建(更新)账户mergeAcct");
    	AccountAttribute acctAttr = context.getAccAttr();
	    CcsCustomer customer = context.getCustomer();
	    CcsAcct acct = context.getAccount();
	    S30001LoanReq req = (S30001LoanReq) context.getSunshineRequestInfo();
	    TxnInfo txnInfo = context.getTxnInfo();
	    LoanFeeDef loanFeeDef = context.getLoanFeeDef();
	
		if (acct == null) {
		    logger.debug("新建账户, acctNo=" + acctNo + ", acctType=" + acctAttr.accountType);
	
		    acct = new CcsAcct();
	
		    acct.setOrg(txnInfo.getOrg());
		    acct.setAcctNbr(acctNo);
		    acct.setAcctType(acctAttr.accountType);
		    acct.setCustId(customer.getCustId());
		    acct.setCustLmtId(customer.getCustLmtId()); // 附卡客户不会新建账户，所以这里没问题
		    acct.setProductCd(req.productcd);
		    acct.setDefaultLogicCardNbr("0");// 无卡交易
		    acct.setCurrency(acctAttr.accountType.getCurrencyCode());
		    acct.setStmtMediaType(StmtMediaType.E); //账单介质类型
		    acct.setCreditLmt(txnInfo.getTransAmt());
		    acct.setTempLmt(BigDecimal.ZERO);
		    acct.setCashLmtRate(null);
		    acct.setOvrlmtRate(null);
		    acct.setLoanLmtRate(null);
		    acct.setCurrBal(BigDecimal.ZERO);
		    acct.setCashBal(BigDecimal.ZERO);
		    acct.setPrincipalBal(BigDecimal.ZERO);
		    acct.setLoanBal(BigDecimal.ZERO);
		    acct.setDisputeAmt(BigDecimal.ZERO);
		    acct.setBegBal(BigDecimal.ZERO);
		    acct.setGraceDaysFullInd(Indicator.N);// 默认值
		    acct.setPointsBegBal(BigDecimal.ZERO);
		    acct.setCtdPoints(BigDecimal.ZERO);
		    acct.setCtdSpendPoints(BigDecimal.ZERO);
		    acct.setCtdAdjPoints(BigDecimal.ZERO);
		    acct.setPointsBal(BigDecimal.ZERO);
		    acct.setSetupDate(txnInfo.getBizDate());
		    acct.setOvrlmtNbrOfCyc(0);
		    acct.setName(req.customername);
		    acct.setGender(req.sex);
		    acct.setOwningBranch("000000001");
		    acct.setMobileNo(req.mobile);
		    acct.setCorpName(req.workcorp);
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(txnInfo.getBizDate());
		    int bc = cal.get(Calendar.DATE);//账单日
		    acct.setCycleDay(String.format("%02d", bc));
		    acct.setNextStmtDate(microCreditRescheduleUtils.getLoanPmtDueDate(txnInfo.getBizDate(), loanFeeDef, 2));// 计算下个账单日期
		    acct.setPmtDueDate(microCreditRescheduleUtils.getNextPaymentDay(acct.getProductCd(),  acct.getNextStmtDate()));// 计算下个还款日期
		    acct.setGraceDate(microCreditRescheduleUtils.getNextGraceDay(acct.getProductCd(),acct.getNextStmtDate()));// 计算下个还款日期
		    acct.setStmtFlag(Indicator.Y);
		    acct.setAgeCode("0");// 账龄默认给"0"
		    acct.setAgeCodeGl("0");
		    acct.setMemoDb(BigDecimal.ZERO);
		    acct.setMemoCash(BigDecimal.ZERO);
		    acct.setMemoCr(BigDecimal.ZERO);
		    acct.setDdInd(DdIndicator.N);
		    acct.setDdBankName(req.bankname);
		    acct.setDdBankBranch(req.bankcode);
		    acct.setDdBankAcctNbr(req.putpaycardid);
		    acct.setDdBankAcctName(req.bankcardowner);
		    acct.setDdBankCity(req.bankcity);
		    acct.setDdBankCityCode(req.bankcitycode);
		    acct.setDdBankProvince(req.bankprovince);
		    acct.setDdBankProvinceCode(req.bankprovincecode);
		    acct.setLastDdAmt(BigDecimal.ZERO);
		    acct.setDualBillingFlag(DualBillingInd.N);
		    acct.setLastPmtAmt(BigDecimal.ZERO);
		    acct.setFirstStmtDate(acct.getNextStmtDate());// 建账时为首个账单日期
		    acct.setFirstRetlAmt(BigDecimal.ZERO);
		    acct.setTotDueAmt(BigDecimal.ZERO);
		    acct.setCurrDueAmt(BigDecimal.ZERO);
		    acct.setPastDueAmt1(BigDecimal.ZERO);
		    acct.setPastDueAmt2(BigDecimal.ZERO);
		    acct.setPastDueAmt3(BigDecimal.ZERO);
		    acct.setPastDueAmt4(BigDecimal.ZERO);
		    acct.setPastDueAmt5(BigDecimal.ZERO);
		    acct.setPastDueAmt6(BigDecimal.ZERO);
		    acct.setPastDueAmt7(BigDecimal.ZERO);
		    acct.setPastDueAmt8(BigDecimal.ZERO);
		    acct.setCtdCashAmt(BigDecimal.ZERO);
		    acct.setCtdCashCnt(0);
		    acct.setCtdRetailAmt(BigDecimal.ZERO);
		    acct.setCtdRetailCnt(0);
		    acct.setCtdRepayAmt(BigDecimal.ZERO);
		    acct.setCtdRepayCnt(0);
		    acct.setCtdDbAdjAmt(BigDecimal.ZERO);
		    acct.setCtdDbAdjCnt(0);
		    acct.setCtdCrAdjAmt(BigDecimal.ZERO);
		    acct.setCtdCrAdjCnt(0);
		    acct.setCtdFeeAmt(BigDecimal.ZERO);
		    acct.setCtdFeeCnt(0);
		    acct.setCtdInterestAmt(BigDecimal.ZERO);
		    acct.setCtdInterestCnt(0);
		    acct.setCtdRefundAmt(BigDecimal.ZERO);
		    acct.setCtdRefundCnt(0);
		    acct.setCtdMaxOvrlmtAmt(BigDecimal.ZERO);
		    acct.setMtdRetailAmt(BigDecimal.ZERO);
		    acct.setMtdRetailCnt(0);
		    acct.setMtdCashAmt(BigDecimal.ZERO);
		    acct.setMtdCashCnt(0);
		    acct.setMtdRefundAmt(BigDecimal.ZERO);
		    acct.setMtdRefundCnt(0);
		    acct.setMtdPaymentAmt(BigDecimal.ZERO);
		    acct.setMtdPaymentCnt(0);
		    acct.setYtdRetailAmt(BigDecimal.ZERO);
		    acct.setYtdRetailCnt(0);
		    acct.setYtdCashAmt(BigDecimal.ZERO);
		    acct.setYtdCashCnt(0);
		    acct.setYtdRefundAmt(BigDecimal.ZERO);
		    acct.setYtdRefundCnt(0);
		    acct.setYtdOvrlmtFeeAmt(BigDecimal.ZERO);
		    acct.setYtdOvrlmtFeeCnt(0);
		    acct.setYtdLateFeeAmt(BigDecimal.ZERO);
		    acct.setYtdLateFeeCnt(0);
		    acct.setYtdRepayAmt(BigDecimal.ZERO);
		    acct.setYtdRepayCnt(0);
		    acct.setLtdRetailAmt(BigDecimal.ZERO);
		    acct.setLtdRetailCnt(0);
		    acct.setLtdCashAmt(BigDecimal.ZERO);
		    acct.setLtdCashCnt(0);
		    acct.setLtdRefundAmt(BigDecimal.ZERO);
		    acct.setLtdRefundCnt(0);
		    acct.setLtdRepayAmt(BigDecimal.ZERO);
		    acct.setLtdRepayCnt(0);
		    acct.setLtdHighestPrin(BigDecimal.ZERO);
		    acct.setLtdHighestCrBal(BigDecimal.ZERO);
		    acct.setLtdHighestBal(BigDecimal.ZERO);
		    acct.setCollectCnt(0);
		    acct.setWaiveOvlfeeInd(Indicator.N);
		    acct.setWaiveCardfeeInd(Indicator.N);
		    acct.setWaiveLatefeeInd(Indicator.N);
		    acct.setWaiveSvcfeeInd(Indicator.N);
		    acct.setUserNumber1(0);
		    acct.setUserNumber2(0);
		    acct.setUserNumber3(0);
		    acct.setUserNumber4(0);
		    acct.setUserNumber5(0);
		    acct.setUserNumber6(0);
		    acct.setUserAmt1(BigDecimal.ZERO);
		    acct.setUserAmt2(BigDecimal.ZERO);
		    acct.setUserAmt3(BigDecimal.ZERO);
		    acct.setUserAmt4(BigDecimal.ZERO);
		    acct.setUserAmt5(BigDecimal.ZERO);
		    acct.setUserAmt6(BigDecimal.ZERO);
		    acct.setSmsInd(SmsInd.Y);
		    acct.setUserSmsAmt(null);// 非0，个性化可定义为0，与null有区别
		    acct.setYtdCycleChagCnt(0);
		    acct.setPmtDueDayBal(BigDecimal.ZERO);
		    acct.setQualGraceBal(BigDecimal.ZERO);
		    //获取协议利率及合同有效期
		    openAcctCommService.getExpireDate(context);
		    acct.setAcctExpireDate(txnInfo.getContraExpireDate());
		    //马上-阳光保险贷 新增
		    acct.setContrNbr(req.getContractno());
		    acct.setGuarantyId(req.getGuarantyid());
		    acct.setCustSource(txnInfo.getInputSource());
		    acct.setAcqId(req.getAcqId());
		    
		    //阳光默认使用协议费率
		    acct.setAgreementRateInd(Indicator.Y);
		    acct.setInsuranceRate(null==req.confirmvaluemonthrate?loanFeeDef.insRate:
				req.confirmvaluemonthrate.multiply(new BigDecimal(req.loanterm)));
		    acct.setAgreementRateExpireDate(txnInfo.getContraExpireDate());
		    
		    //兜底 增加代收罚金费率
		    acct.setReplacePenaltyRate(loanFeeDef.replacePenaltyRate == null?BigDecimal.ZERO:loanFeeDef.replacePenaltyRate);
		    
		    //20151127
		    acct.setLoanFeeDefId(loanFeeDef.loanFeeDefId.toString());
		    acct.setSubTerminalType(req.getSubTerminalType());//销售渠道
		    logger.info("---------------------插入账户信息表---------------------");
		    rAcct.save(acct);
		}
		return acct;
    }

	private CcsAcctO mergeAccto(CcsAcct acct, S30001LoanReq req) {
		LogTools.printObj(logger, acct.getAcctNbr(), "创建(更新)账户mergeAccto");
		// 建立对应的AccountO
		CcsAcctO acctO = new CcsAcctO();

		acctO.setOrg(acct.getOrg());
		acctO.setAcctNbr(acct.getAcctNbr());
		acctO.setAcctType(acct.getAcctType());
		acctO.setCustLmtId(acct.getCustLmtId());
		acctO.setCustId(acct.getCustId());
		acctO.setProductCd(acct.getProductCd());
		acctO.setCreditLmt(acct.getCreditLmt());
		acctO.setTempLmt(acct.getTempLmt());
		acctO.setTempLmtBegDate(acct.getTempLmtBegDate());
		acctO.setTempLmtEndDate(acct.getTempLmtEndDate());
		acctO.setOvrlmtRate(acct.getOvrlmtRate());
		acctO.setCashLmtRate(acct.getCashLmtRate());
		acctO.setLoanLmtRate(acct.getLoanLmtRate());
		acctO.setCurrBal(acct.getCurrBal());
		acctO.setCashBal(acct.getCashBal());
		acctO.setLoanBal(acct.getLoanBal());
		acctO.setDisputeAmt(acct.getDisputeAmt());
		acctO.setBlockCode(acct.getBlockCode());
		acctO.setMemoCash(acct.getMemoCash());
		acctO.setMemoCr(acct.getMemoCr());
		acctO.setMemoDb(acct.getMemoDb());
		acctO.setOwningBranch(acct.getOwningBranch());
		acctO.setCycleDay(acct.getCycleDay());
		acctO.setSmsInd(SmsInd.Y);
		acctO.setUserSmsAmt(null);// 非0，个性化可定义为0，与null有区别
	    acctO.setFloatRate(BigDecimal.ZERO);
		
		//马上-阳光保险贷 新增
		acctO.setContrNbr(req.getContractno());
		acctO.setGuarantyId(req.getGuarantyid());
		acctO.setCustSource(acct.getCustSource());
		
		logger.info("---------------------插入账户信息表-授权---------------------");
		return rAcctO.save(acctO);
	}

	
    /**
     * inputValue > origValue > defaultValue
     * 
     * @param origValue
     * @param inputValue
     * @param defaultValue
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> T getDefaultValue(Object inputValue, Object origValue, Object defaultValue, Class<T> clazz) {
		Object o;
		if (inputValue != null && StringUtils.isNotBlank(inputValue.toString())) {
		    o = inputValue;
		} else {
		    if (origValue == null) {
			o = defaultValue;
		    } else {
			o = origValue;
		    }
		}
		return (T)o;
    }

    /**
     * 
     * @see 方法名：mergeCard
     * @see 描述：创建卡片
     * @see 创建日期：20158月14日下午6:40:39
     * @author lizz
     * 
     * @param req
     * @param product
     * @param productCredit
     * @param cust
     * @param acct
     * @return
     * @throws ParseException 
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void mergeCard(TxnContext context) {
    	LogTools.printObj(logger, "", "创建卡片mergeCard");
    	TxnInfo txnInfo = context.getTxnInfo();
    	Product product = context.getProduct();
    	ProductCredit productCredit = context.getProductCredit();
	    CcsCustomer customer = context.getCustomer();
	    CcsAcct acct = context.getAccount();
	    S30001LoanReq req = (S30001LoanReq) context.getSunshineRequestInfo();
	    QCcsCard qCcsCard = QCcsCard.ccsCard;
		CcsCard card = rCcsCard.findOne(qCcsCard.acctNbr.eq(acct.getAcctNbr()));
		if(null == card) {
			
			String cardno = dealWithCardNbr(txnInfo,product);
			card = new CcsCard();
			
			card.setOrg(txnInfo.getOrg());
			card.setLogicCardNbr(cardno);
			card.setCustId(customer.getCustId());
			card.setAcctNbr(acct.getAcctNbr());
			card.setProductCd(req.productcd);
			card.setApplNbr("0");
			card.setBarcode("");
			card.setBscSuppInd(BscSuppIndicator.B);
			card.setCardBasicNbr(cardno);
			card.setOwningBranch("000000001");
			card.setApplPromoCode("");
			card.setRecomName("");
			card.setRecomCardNo("");
			card.setSetupDate(txnInfo.getBizDate());
			
			if (product.fabricationInd == Indicator.N) {
				card.setActiveInd(Indicator.Y);
				card.setActiveDate(txnInfo.getBizDate());
			} else {
				card.setActiveInd(Indicator.N);
			}
			card.setLastestMediumCardNbr(cardno);
			card.setSalesInd(Indicator.N);
			card.setApplSrc(req.getInputSource().name());
			card.setRepresentName("");
			card.setPosPinVerifyInd(Indicator.Y);
			// 新卡有效期 = batchDay + PRODUCT.NEW_CARD_VALID_PRD(卡管送入)
			DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
			Date expireDate = new Date();
			try {
				expireDate = dateFormat1.parse("9999-01-01");
			} catch (ParseException e) {
				//暂不处理异常，虚拟卡
			}
			card.setCardExpireDate(expireDate);
			// 若开卡即收取年费
			if (productCredit.fee.firstCardFeeInd == FirstCardFeeInd.I) {
				card.setNextCardFeeDate(new Date());
			}
			card.setCardFeeRate(BigDecimal.ZERO);
			card.setRenewInd(RenewInd.D);
			card.setFirstUsageFlag(FirstUsageIndicator.A);
			card.setWaiveCardfeeInd(Indicator.N);
			card.setCardDeliverMethod(CardFetchMethod.A);
			
			logger.info("---------------------新建卡片----------------------");
			rCcsCard.save(card);
			logger.debug("新建卡片, LogicalCardNo后四位=" + CodeMarkUtils.subCreditCard(card.getLogicCardNbr()));
			
			// 建立对应的统计对象
			CcsCardUsage cardSt = new CcsCardUsage();
			
			cardSt.setOrg(card.getOrg());
			cardSt.setLogicCardNbr(card.getLogicCardNbr());
			cardSt.setCtdRetailAmt(BigDecimal.ZERO);
			cardSt.setCtdRetailCnt(0);
			cardSt.setCtdCashAmt(BigDecimal.ZERO);
			cardSt.setCtdCashCnt(0);
			cardSt.setMtdRetailAmt(BigDecimal.ZERO);
			cardSt.setMtdRetailCnt(0);
			cardSt.setMtdCashAmt(BigDecimal.ZERO);
			cardSt.setMtdCashCnt(0);
			cardSt.setYtdRetailAmt(BigDecimal.ZERO);
			cardSt.setYtdRetailCnt(0);
			cardSt.setYtdCashAmt(BigDecimal.ZERO);
			cardSt.setYtdCashCnt(0);
			cardSt.setLtdRetailAmt(BigDecimal.ZERO);
			cardSt.setLtdRetailCnt(0);
			cardSt.setLtdCashAmt(BigDecimal.ZERO);
			cardSt.setLtdCashCnt(0);
			cardSt.setLastCycleRetailAmt(BigDecimal.ZERO);
			cardSt.setLastCycleRetailCnt(0);
			cardSt.setLastCycleCashAmt(BigDecimal.ZERO);
			cardSt.setLastCycleCashCnt(0);
			cardSt.setLastMthRetlAmt(BigDecimal.ZERO);
			cardSt.setLastMthRetlCnt(0);
			cardSt.setLastMthCashAmt(BigDecimal.ZERO);
			cardSt.setLastMthCashCnt(0);
			cardSt.setLastYearRetlAmt(BigDecimal.ZERO);
			cardSt.setLastYearRetlCnt(0);
			cardSt.setLastYearCashAmt(BigDecimal.ZERO);
			cardSt.setLastYearCashCnt(0);
			
			logger.info("----------------------插入逻辑卡统计表-------------------");
			rCcsCardUsage.save(cardSt);
			
			CcsCustomerCrlmt custLimit = context.getCustLimitO();
	    	
			CcsCardO cardO = new CcsCardO();
		
			cardO.setOrg(card.getOrg());
			cardO.setLogicCardNbr(card.getLogicCardNbr());
			cardO.setAcctNbr(card.getAcctNbr());
			cardO.setCustId(card.getCustId());
			cardO.setCustLmtId(custLimit.getCustLmtId());
			cardO.setProductCd(card.getProductCd());
			cardO.setBscSuppInd(card.getBscSuppInd());
			cardO.setCardBasicNbr(card.getCardBasicNbr());
			cardO.setActiveInd(card.getActiveInd());
			cardO.setPosPinVerifyInd(card.getPosPinVerifyInd());
			cardO.setPinTries(0);
			cardO.setBlockCode(card.getBlockCode());
			// 以下6项额度都是持卡人自己设置的，没有参数，默认赋值逻辑如下，持卡人如果有特殊需要，可以通过客服修改
			cardO.setCycleRetailLmt(DEFAULT_LIMIT);
			cardO.setCycleCashLmt(DEFAULT_LIMIT);
			cardO.setCycleNetLmt(DEFAULT_LIMIT);
			cardO.setTxnLmt(DEFAULT_LIMIT);
			cardO.setTxnCashLmt(DEFAULT_LIMIT);
			cardO.setTxnNetLmt(DEFAULT_LIMIT);
			cardO.setDayUsedAtmNbr(0);
			cardO.setDayUsedAtmAmt(BigDecimal.ZERO);
			cardO.setDayUsedRetailNbr(0);
			cardO.setDayUsedRetailAmt(BigDecimal.ZERO);
			cardO.setDayUsedCashNbr(0);
			cardO.setDayUsedCashAmt(BigDecimal.ZERO);
			cardO.setDayUsedXfroutNbr(0);
			cardO.setDayUsedXfroutAmt(BigDecimal.ZERO);
			cardO.setCtdUsedAmt(BigDecimal.ZERO);
			cardO.setCtdCashAmt(BigDecimal.ZERO);
			cardO.setCtdNetAmt(BigDecimal.ZERO);
			cardO.setInqPinTries(0);
			cardO.setDayUsedAtmCupxbAmt(BigDecimal.ZERO);
		
			logger.info("--------------------插入逻辑卡表-授权----------------");
			rCcsCardO.save(cardO);
			
			CcsCardLmMapping map = new CcsCardLmMapping();

			map.setOrg(card.getOrg());
			map.setCardNbr(card.getLogicCardNbr());
			map.setLogicCardNbr(card.getLogicCardNbr());

			logger.info("-------------------- 创建介质卡号逻辑卡号映射----------------");
			rCcsCardLmMapping.save(map);
			
		}
		//账户上设置卡号
		acct.setDefaultLogicCardNbr(card.getLogicCardNbr());
		
		context.setCard(card);
		context.getTxnInfo().setCardNo(card.getLogicCardNbr());
    }
    

    /**
     * 
     * @see 方法名：dealWithCardNbr
     * @see 描述：获得卡号
     * @see 创建日期：2015年8月14日下午7:11:49
     * @author lizz
     * @param product 
     * 
     * @param afi
     * @param bp
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private String dealWithCardNbr(TxnInfo txnInfo, Product product) {
		// 如果上送文件中有卡号 ，则不生成新卡号
		String cardNo = "";
		if(logger.isDebugEnabled())
    		logger.debug("生成卡号开始，产品{},org{}", product.productCode, txnInfo.getOrg());
//			CcsCardnbrGrt config = getNextCardNbr(product, txnInfo.getOrg());
//			cardNo = formatCardNbr(config, product);
		Long currValue = cardNbrServiceImpl.getCardNbr(product.productCode);
		if (currValue.longValue() > getMaxCardNbr(product)) {
			throw new ProcessException("卡号已用完，无法申请新卡号");
		}
		cardNo = formatCardNbr(currValue, product);
			
		return cardNo;
    }

    /**
     * 
     * @see 方法名：getNextCardNbr
     * @see 描述：生成下一个卡号
     * @see 创建日期：2015年8月14日上午11:28:36
     * @author lizz
     * 
     * @param product
     * @param org
     * @return
     * @throws ProcessException
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized CcsCardnbrGrt getNextCardNbr(Product product, String org) throws ProcessException {
		QCcsCardnbrGrt q = QCcsCardnbrGrt.ccsCardnbrGrt;
		JPAUpdateClause jpu = new JPAUpdateClause(em, q);
		jpu.where(q.org.eq(org).and(q.productCd.eq(product.productCode))).set(q.currValue, q.currValue.add(1));
		jpu.execute();
		JPAQuery query = new JPAQuery(em);
		CcsCardnbrGrt result =
			query.from(q).where(q.org.eq(org).and(q.productCd.eq(product.productCode))).singleResult(q);
		if (result != null) {
		    em.refresh(result);
		}
	
		if (result == null) {
		    result = new CcsCardnbrGrt();
		    result.setOrg(org);
		    result.setProductCd(product.productCode);
		    result.setCurrValue(Long.parseLong(product.cardnoRangeFlr));
		    em.persist(result);
		    return result;
		}
		if (result.getCurrValue().longValue() > getMaxCardNbr(product)) {
		    throw new ProcessException("卡号已用完，无法申请新卡号");
		}
		return result;
    }
    
    /**
     * 
     * @see 方法名：getMaxCardNbr
     * @see 描述：获得产品对应最大卡号
     * @see 创建日期：2015年8月14日上午11:29:10
     * @author lizz
     * 
     * @param product
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public long getMaxCardNbr(Product product) {
		String ceil = product.cardnoRangeCeil;
		return Long.parseLong(ceil);
    }
    
    /**
     * 
     * @see 方法名：formatCardNbr
     * @see 描述：生成格式化的卡号
     * @see 创建日期：2015年8月17日
     * @author lizz
     * 
     * @param config
     * @param product
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private String formatCardNbr(CcsCardnbrGrt config, Product product) {
		DecimalFormat df = new DecimalFormat("#" + this.getStringForLength('0', product.cardnoRangeFlr.length()));
		StringBuffer cardNbr = new StringBuffer(product.bin);
		cardNbr.append(df.format(config.getCurrValue()));
	
		char luhnModNum = LuhnMod10.getDigit(cardNbr.toString());
		if(logger.isDebugEnabled())
			logger.debug("卡号={}", "" + cardNbr + luhnModNum);
		return cardNbr.append(luhnModNum).toString();
    }
    
    private String formatCardNbr(Long currValue, Product product) {
        DecimalFormat df = new DecimalFormat("#" + this.getStringForLength('0', product.cardnoRangeFlr.length()));
        StringBuffer cardNbr = new StringBuffer(product.bin);
        cardNbr.append(df.format(currValue));
       
        char luhnModNum = LuhnMod10.getDigit(cardNbr.toString());
        if(logger.isDebugEnabled())
             logger.debug("卡号={}", "" + cardNbr + luhnModNum);
        return cardNbr.append(luhnModNum).toString();
   }



    /**
     * 
     * @see 方法名：getStringForLength
     * @see 描述：获得给定长度字符串
     * @see 创建日期：2015年8月17日
     * @author lizz
     * 
     * @param c
     * @param length
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public StringBuilder getStringForLength(char c, int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
		    sb.append(c);
		}
		return sb;
    }
    
	/**
	 * 产生唯一流水号
	 * @return
	 */
	public String generateFlowNo(){
		DateFormat df = new SimpleDateFormat("yyDS");
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		StringBuffer sb = new StringBuffer(df.format(c.getTime()));
		sb.append(hour*60*60+minute*60+second);
		return sb.substring(1);
	}
	
	/**
	 * 检查放款参数
	 * @param context
	 * @throws ProcessException
	 */
	public void validateForApply(TxnContext context) throws ProcessException{
		LogTools.printObj(logger, "", "检查放款参数validateForApply");
		
		LoanPlan loanPlan = context.getLoanPlan();
		LoanFeeDef loanFeeDef = context.getLoanFeeDef();
		TxnInfo txnInfo = context.getTxnInfo();
		
//		LogTools.printObj(logger, loanPlan, "贷款产品loanPlan");
//		LogTools.printObj(logger, loanFeeDef, "贷款产品定价loanFeeDef");
		
		//判断已有现金分期笔数是否超限
		QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		JPAQuery queryLoanReg = new JPAQuery(em);
		JPAQuery queryLoan = new JPAQuery(em);
		long cnt = 0;
		cnt+=queryLoanReg.from(qCcsLoanReg).where(qCcsLoanReg.guarantyId.eq(txnInfo.getGuarantyid()).
				and(qCcsLoanReg.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " ")))).count();
//				and(qCcsLoanReg.loanType.eq(LoanType.MCEI)).
//				and(qCcsLoanReg.loanAction.in(LoanAction.A))).count();
		
		LogTools.printObj(logger, cnt, "贷款注册CcsLoanReg的条数");
		
		cnt+=queryLoan.from(qCcsLoan).where(qCcsLoan.guarantyId.eq(txnInfo.getGuarantyid()).
				and(qCcsLoan.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " ")))).count();
//				and(qCcsLoan.loanType.eq(LoanType.MCEI))).
//				and(qCcsLoan.loanStatus.notIn(LoanStatus.T,LoanStatus.F))).count();
		
		LogTools.printObj(logger, cnt, "贷款CcsLoan的条数");
		
		if(null == loanPlan.loanMold)
			throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());
		if(loanPlan.loanMold == LoanMold.S){
			if(cnt>0){
				throw new ProcessException(MsRespCode.E_1013.getCode(),MsRespCode.E_1013.getMessage());
			}
		}
		if(txnInfo.getTransAmt().compareTo(loanFeeDef.maxAmount)>0 ){
			throw new ProcessException(MsRespCode.E_1036.getCode(),MsRespCode.E_1036.getMessage());
		}
		if(txnInfo.getTransAmt().compareTo(loanFeeDef.minAmount)<0){
			throw new ProcessException(MsRespCode.E_1037.getCode(),MsRespCode.E_1037.getMessage());
		}
		if(loanPlan.loanValidity.before(txnInfo.getBizDate())){
			throw new ProcessException(MsRespCode.E_1038.getCode(), MsRespCode.E_1038.getMessage());
		}
		if(loanPlan.loanStaus != LoanPlanStatus.A){
			throw new ProcessException(MsRespCode.E_1039.getCode(),MsRespCode.E_1039.getMessage());
		}
		if(loanFeeDef.loanFeeDefStatus != null && LoanFeeDefStatus.A != loanFeeDef.loanFeeDefStatus){
			throw new ProcessException(MsRespCode.E_1068.getCode(),MsRespCode.E_1068.getMessage());
		}
		
		sunshineCommService.checkCustomer(context);
	}
	
	
}
