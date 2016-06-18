package com.sunline.ccs.service.handler.appapi;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.order.PaymentFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TNRAAcctSetupWithDrawReq;
import com.sunline.ccs.service.msentity.TNRAAcctSetupWithDrawResp;
import com.sunline.ccs.service.noticetemplate.SMSLoanDeductions;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 非循环现金贷开户放款
 * @author wangz
 *
 */
@Service
public class TNRAAcctSetupWithDrawHandler extends AbstractHandler{
	private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    CustAcctCardFacility queryFacility;
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	private PaymentFacility paymentFacility;
	@Autowired
	public RCcsAcct rAcct;
	@Autowired
	public RCcsAcctO rAcctO;
	@Autowired
	public RCcsCustomer rCustomer;
	@Autowired
	public UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	public UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private SMSLoanDeductions smsLoanDeductions;
	@Autowired
	TxnUtils txnUtils;
	
	public MsResponseInfo execute(MsRequestInfo msRequestInfo) throws ProcessException {
		LogTools.printLogger(logger, "TNRAAcctSetupWithDrawReq", "非循环现金贷开户放款", msRequestInfo, true);
		TNRAAcctSetupWithDrawReq req = (TNRAAcctSetupWithDrawReq) msRequestInfo;
		LogTools.printObj(logger, req, "TNRAAcctSetupWithDrawReq请求参数");
		TNRAAcctSetupWithDrawResp resp = new TNRAAcctSetupWithDrawResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		
		context.setTxnInfo(txnInfo);
		context.setMsRequestInfo(req);
		try {
			this.initTxnInfo(req, txnInfo);
			
			//重复交易
			appapiCommService.checkRepeatTxn(context);
			
			this.checkReq(req,txnInfo);
			
			// 确定产品参数
			openAcctCommService.getProdParam(context);
			
			this.validateForApply(context);

			//单独事务处理
			this.bizProc(context);
			//开户事物成功即返回合同号
			if(context.getAccount()!=null){
				resp.setAcctSetupInd(Indicator.Y);
				resp.setContractNo(context.getAccount().getContrNbr());
			}
			
			//处理结果，单独事务处理
			this.payResultProc(context);
			
			//向通知平台发送通知		
			smsLoanDeductions.sendSMS(context);	
			
		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			
			appapiCommService.preException(pe, pe, txnInfo);
//			appapiCommService.saveErrorOrder(context);
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();//e不是pe，处理逻辑与没有code的pe一样
			appapiCommService.preException(pe, pe, txnInfo);
//			appapiCommService.saveErrorOrder(context);
		}finally{
			LogTools.printLogger(logger, "TNRAAcctSetupWithDrawResp", "放款申请", resp, false);
		}
		
		this.setResponse(resp,req,context);
		LogTools.printObj(logger, resp, "TNRAAcctSetupWithDrawResp响应参数");
		return resp;
		
	}
	
	/**
	 * 处理支付结果
	 * 
	 * @param context
	 * @param payJson
	 * @param resp
	 */
	@Transactional
	public void payResultProc(TxnContext context) {
		CcsOrder order = context.getOrder();
		TxnInfo txnInfo = context.getTxnInfo();
		ProductCredit productCredit = context.getProductCredit();
		try{
			//组装支付指令
			String payJson = paymentFacility.installPaySinPaymentCommand(order);
			
			if(logger.isDebugEnabled())
				logger.debug("独立事务，处理支付结果--payResultProc,支付报文：[{}]",payJson);
			appapiCommService.loadAcctOByAcct(context, true);
			appapiCommService.loadAcctByContrNo(context, true);

			appapiCommService.loadAuthmemoLogKv(context, context.getTxnInfo().getLogKv(),false);
			appapiCommService.loadLoanRegByContrNo(context,null, LoanRegStatus.C, null, true);
			
			if(logger.isDebugEnabled())
				logger.debug("自动放款阈值:[{}],贷款金额:[{}]",productCredit.autoDCAmtLimit,txnInfo.getTransAmt());
			if(productCredit.autoDCAmtLimit.compareTo(txnInfo.getTransAmt())>=0){
				appapiCommService.loanProc(context,payJson);
			}else{
				throw new ProcessException(MsPayfrontError.E_90004.getCode(), MsPayfrontError.E_90004.getDesc());
			}
			
		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			ProcessException newPe = appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.exceptionProc(context, newPe);
			
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			
			ProcessException pe = new ProcessException();
			ProcessException newPe = appapiCommService.preException(e, pe, txnInfo);;//e不是pe，处理逻辑与没有code的pe一样
			appapiCommService.exceptionProc(context, newPe);
		}
		//保存交易数据
		appapiCommService.mergeProc(context);
	}

	/**
	 * 检查报文
	 * @param req
	 */
	private void checkReq(TNRAAcctSetupWithDrawReq req,TxnInfo txnInfo) {
		

		if(null != req.interestRate && req.interestRate.compareTo(BigDecimal.ZERO)<0){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{INTEREST_RATE},不能小于等于0");
		}
		
		if(null != req.contraExpireDate && req.contraExpireDate.compareTo(txnInfo.getBizDate())<0){
			if(logger.isDebugEnabled())
				logger.debug("合同有效期[{}]小于当前业务日期[{}]",req.contraExpireDate,txnInfo.getBizDate());
			throw new ProcessException(MsRespCode.E_1059.getCode(),MsRespCode.E_1059 .getMessage());
		}
		
		//检查开户行号
		appapiCommService.checkBankCode(req.bankcode);
		
		if(!StringUtils.equals(req.country,"156")){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{COUNTRY},必须为156");
		}
		
		if(req.loanAmt.compareTo(BigDecimal.ZERO)<=0){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{CREDIT_LIMIT},必须大于0");
		}
		
	}

	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TNRAAcctSetupWithDrawResp resp, TNRAAcctSetupWithDrawReq req, TxnContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		if(txnInfo.getResponsCode() == null || txnInfo.getResponsCode().isEmpty()){
			txnInfo.setResponsCode(MsRespCode.E_0000.getCode());
			txnInfo.setResponsDesc(MsRespCode.E_0000.getMessage());
		}
		
		if(resp.getAcctSetupInd()==null || resp.getAcctSetupInd()!=Indicator.Y){
			resp.setAcctSetupInd(Indicator.N);			
		}

		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setContractNo(txnInfo.getContrNo());
			resp.setDueBillNo(txnInfo.getDueBillNo());
			resp.setStatus("S");//交易成功
			if(context.getAccount()!=null){
				resp.setContractNo(context.getAccount().getContrNbr());
			}
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
	private void bizProc(TxnContext context) {
		if (logger.isDebugEnabled())
			logger.debug("独立事务，业务处理开始--bizProc");

		TxnInfo txnInfo = context.getTxnInfo();
		TNRAAcctSetupWithDrawReq req = (TNRAAcctSetupWithDrawReq) context.getMsRequestInfo();

		// 获取有效期
		openAcctCommService.getExpireDate(context);
		
		// 确定客户
		this.mergeCustomer(context);

		// 确定地址
		if (StringUtils.isNotBlank(req.province)) {
			openAcctCommService.mergeAddress(txnInfo.getOrg(), context
					.getCustomer().getCustId(), AddressType.H, req.familyaddr,
					req.city, req.country, req.areacode, req.province,
					req.cellPhone, req.zipcode);
		}
		// 用家庭 市,国家,区,省 放入单位地址
		if (StringUtils.isNotBlank(req.workAddr)) {
			openAcctCommService.mergeAddress(txnInfo.getOrg(), context
					.getCustomer().getCustId(), AddressType.C, req.workAddr,
					req.city, req.country, req.areacode, req.province,
					req.workTel, req.workZip);
		}

		// 确定联系人, 附卡没有联系人
		if (req.linkrelation1 != null) {
			openAcctCommService.mergeLinkman(txnInfo.getOrg(), context
					.getCustomer().getCustId(), req.linkrelation1, req.link1,
					null, req.linkmobile1);
		}
		if (req.linkrelation2 != null) {
			openAcctCommService.mergeLinkman(txnInfo.getOrg(), context
					.getCustomer().getCustId(), req.linkrelation2, req.link2,
					null, req.linkmobile2);
		}

		// 新建或更新工作信息
		openAcctCommService.mergeEmployee(txnInfo.getOrg(), context
				.getCustomer().getCustId(), req.workCorp, req.workTel, null,
				req.positionLevel, req.unitkind, null, req.position,
				req.monthlyWages==null?BigDecimal.ZERO : req.monthlyWages.multiply(BigDecimal.valueOf(12)),
				req.monthlyWages==null?BigDecimal.ZERO : req.monthlyWages.multiply(BigDecimal.valueOf(12)));

		// 设置客户的额度信息
		openAcctCommService.mergeCustomerCrLmt(context);

		// 确定账户及联机账户
		this.mergeAcctAndO(context);
		
		// 建立卡片
		openAcctCommService.mergeCard(context);

		// 检查otb
		appapiCommService.valiLoanOtb(context);

		// 进入放款授权
		openAcctCommService.mergeLoanReg(context);
		
		appapiCommService.saveCcsAuthmemoO(context);

		// 调用支付
		appapiCommService.installOrder(context);
	}

	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(TNRAAcctSetupWithDrawReq req, TxnInfo txnInfo) {
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setTransDirection(AuthTransDirection.Normal);
		txnInfo.setTransType(AuthTransType.AgentDebit);
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setMti("0208");
		txnInfo.setProcCode("480001");
		txnInfo.setLoanCode(req.loanCode);
		txnInfo.setLoanUsage(LoanUsage.L);
		txnInfo.setCreditLmt(req.loanAmt);;
		txnInfo.setTransAmt(req.loanAmt);
		txnInfo.setOrg(OrganizationContextHolder.getCurrentOrg());
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setAuthTransStatus(AuthTransStatus.N);
		txnInfo.setDirection(TransAmtDirection.D);
		txnInfo.setUuid(req.uuid);
		txnInfo.setApplyNo(req.applyNo);
		txnInfo.setCdTerms(req.getLoanTerm());
		txnInfo.setAgreeRate(req.interestRate);// 协议利率
		txnInfo.setAgreeRateExpireDate(req.agreeRateExpireDate);//协议利率有效期
		txnInfo.setContraExpireDate(req.contraExpireDate);
		txnInfo.setIdNo(req.idNo);
		txnInfo.setIdType(req.idType);
		txnInfo.setMobile(req.cellPhone);
		txnInfo.setJionLifeInsuInd(req.jionLifeInsuInd);//20151204
//		txnInfo.setRefNbr(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setServiceId(req.getServiceId());
		txnInfo.setLoanFeeDefId(req.loanFeeDefId);
		txnInfo.setServiceId(req.getServiceId());
		//
		txnInfo.setPrepayPkgInd(req.prepayPkgInd == null ? Indicator.N : req.prepayPkgInd);//20160513
		
		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "txnInfo交易中间信息");
		
	}
	
	/**
	 * 保存客户信息
	 * @param context
	 */
	private void mergeCustomer(TxnContext context) {
		TNRAAcctSetupWithDrawReq req = (TNRAAcctSetupWithDrawReq) context.getMsRequestInfo();
		CcsCustomer customer = openAcctCommService.initCustomer(context);
		
		// 更新客户信息，上送更新，否则不更新
		customer.setIdNo(req.idNo);
		customer.setIdType(req.idType);
		if (req.positionLevel != null)
			customer.setTitle(req.positionLevel);// 职位
		if (req.customername != null)
			customer.setName(req.customername);
		if (req.gender != null)
			customer.setGender(req.gender);
		if (req.birthday != null)
			customer.setBirthday(req.birthday);
		if (StringUtils.isNotBlank(req.country))
			customer.setNationality(req.country);// 国籍
		if (req.maritalStatus != null)
			customer.setMaritalStatus(req.maritalStatus);
		if (req.eduDegree != null)
			customer.setEduexperience(req.eduDegree);
		if (req.eduExperience != null)
			customer.setEducation(req.eduExperience);
		if (req.workbegindate != null)
			customer.setWorkbegindate(req.workbegindate);
		if (req.incomeFlag != null)
			customer.setIncomeflag(req.incomeFlag);
		if (req.monthlyWages != null)
			customer.setMonthlywages(req.monthlyWages);
		if (StringUtils.isNotBlank(req.familytel))
			customer.setHomePhone(req.familytel);
		if (StringUtils.isNotBlank(req.cellPhone))
			customer.setMobileNo(req.cellPhone);
		if (StringUtils.isNotBlank(req.workCorp))
			customer.setCorpName(req.workCorp);

		customer = rCustomer.save(customer);
		context.setCustomer(customer);
	}
	
	/**
	 * 保存acct和accto
	 * @param context
	 */
	private void mergeAcctAndO(TxnContext context) {
		
		//获取账户及联机账户
		openAcctCommService.initAcctAndO(context);
		
		CcsAcct acct = context.getAccount();
		CcsAcctO accto = context.getAccounto();
		TNRAAcctSetupWithDrawReq req = (TNRAAcctSetupWithDrawReq) context.getMsRequestInfo();
		Product product = context.getProduct();
		TxnInfo txnInfo = context.getTxnInfo();
		LoanPlan loanPlan = context.getLoanPlan();
		LoanFeeDef loanFeeDef = context.getLoanFeeDef();
		
		acct.setProductCd(product.productCode);
		acct.setName(req.customername);
		acct.setGender(req.gender);
		acct.setMobileNo(req.cellPhone);
		acct.setCorpName(req.workCorp);
		acct.setDdBankName(req.bankname);
		acct.setDdBankBranch(req.bankcode);
		acct.setDdBankAcctNbr(req.putpaycardid);
		acct.setDdBankAcctName(req.bankcardowner);
		acct.setDdBankCity(req.bankcity);
		acct.setDdBankCityCode(req.bankcitycode);
		acct.setDdBankProvince(req.bankprovince);
		acct.setDdBankProvinceCode(req.bankprovincecode);
		acct.setAcctExpireDate(txnInfo.getContraExpireDate());//合同有效期
		acct.setApplicationNo(req.applyNo);//申请单号
		acct.setInterestRate(req.interestRate);//协议利率
		acct.setAgreementRateExpireDate(txnInfo.getAgreeRateExpireDate());//协议利率有效期
		acct.setAcqId(req.cooperatorID);
		acct.setLoanMold(loanPlan.loanMold);
		acct.setPrepayPkgInd(txnInfo.getPrepayPkgInd());
		
		//二期新增 各种费率   modify 20151127 月费率*期数   不能修正为0
		acct = this.setAcctRateByReq(req, acct, loanFeeDef);
		
		//20151127
		acct.setLoanFeeDefId(loanFeeDef.loanFeeDefId.toString());//子产品编号
		acct.setPurpose(req.purpose);//贷款用途
		acct.setApplyDate(req.applyDate);//申请日期
		acct.setSubTerminalType(req.getSubTerminalType());//销售渠道
		acct.setJoinLifeInsuInd(req.jionLifeInsuInd);//20151204
		
		logger.info("---------------------插入账户信息表---------------------");
		rAcct.save(acct);
		
		logger.info("---------------------插入账户信息表-授权---------------------");
		
		accto.setContrNbr(acct.getContrNbr());
		rAcctO.save(accto);
		
		//设置合同号
		txnInfo.setContrNo(acct.getContrNbr());
	}
	
	/**
	 * 使用req的费率数据设置acct
	 * @param loanReg
	 * @param acct
	 * @param loanFeeDef
	 * @param agreeInd
	 * @return
	 */
	public CcsAcct setAcctRateByReq(TNRAAcctSetupWithDrawReq req,CcsAcct acct,LoanFeeDef loanFeeDef){
		//使用协议费率，这些费率都取acct中的   20151127不修正为0，直接赋值
		Boolean isAgree = Indicator.Y.equals(req.agreeRateInd);
		acct.setAgreementRateInd(req.agreeRateInd==null?Indicator.Y:req.agreeRateInd);//默认为使用协议费率

		//贷款服务费率
		if(logger.isDebugEnabled())
			logger.debug("贷款服务费计算方式:[{}]",loanFeeDef.loanFeeCalcMethod);
		if (req.feeRate != null && isAgree) {
			acct.setFeeRate(req.feeRate);
		} else {
			if(null == loanFeeDef.feeRate && CalcMethod.R == loanFeeDef.loanFeeCalcMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",贷款服务费收取比例为空");
			}
			acct.setFeeRate(loanFeeDef.feeRate);
		}
		//贷款服务金额
		if (req.feeAmount != null && isAgree) {
			acct.setFeeAmt(req.feeAmount);
		} else {
			if(null == loanFeeDef.feeAmount && CalcMethod.A == loanFeeDef.loanFeeCalcMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",贷款服务费固定金额为空");
			}
			acct.setFeeAmt(loanFeeDef.feeAmount);
		}
		//寿险费率
		if(logger.isDebugEnabled())
			logger.debug("寿险计划包计算方式:[{}]",loanFeeDef.lifeInsuFeeCalMethod);
		if (req.lifeInsuFeeRate != null && isAgree) {
			acct.setLifeInsuFeeRate(req.lifeInsuFeeRate);
		} else {
			if(null == loanFeeDef.lifeInsuFeeRate && PrepaymentFeeMethod.R == loanFeeDef.lifeInsuFeeCalMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",寿险计划包费率为空");
			}
			acct.setLifeInsuFeeRate(loanFeeDef.lifeInsuFeeRate);
		}
		//寿险固定金额
		if (req.lifeInsuFeeAmt != null && isAgree) {
			acct.setLifeInsuFeeAmt(req.lifeInsuFeeAmt);
		} else {
			if(null == loanFeeDef.lifeInsuFeeAmt && PrepaymentFeeMethod.A == loanFeeDef.lifeInsuFeeCalMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",寿险计划包固定金额为空");
			}
			acct.setLifeInsuFeeAmt(loanFeeDef.lifeInsuFeeAmt);
		}
		//保费月费率
		if(logger.isDebugEnabled())
			logger.debug("保险费计算方式:[{}]",loanFeeDef.insCalcMethod);
		if (req.insRate != null && isAgree) {
			acct.setInsuranceRate(req.insRate);
		} else {
			if(null == loanFeeDef.insRate && PrepaymentFeeMethod.R == loanFeeDef.insCalcMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",保险费率为空");
			}
			acct.setInsuranceRate(loanFeeDef.insRate);
		}
		//保费月固定金额
		if (req.insAmt != null && isAgree) {
			acct.setInsAmt(req.insAmt);
		} else {
			if(null == loanFeeDef.insAmt && PrepaymentFeeMethod.A == loanFeeDef.insCalcMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",保险费固定金额为空");
			}
			acct.setInsAmt(loanFeeDef.insAmt);
		}
		//分期手续费率
		if(logger.isDebugEnabled())
			logger.debug("分期手续费计算方式:[{}]",loanFeeDef.installmentFeeCalMethod);
		if (req.installmentFeeRate != null && isAgree) {
			acct.setInstallmentFeeRate(req.installmentFeeRate);
		} else {
			if(null == loanFeeDef.installmentFeeRate && PrepaymentFeeMethod.R == loanFeeDef.installmentFeeCalMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",分期手续费收取比例为空");
			}
			acct.setInstallmentFeeRate(loanFeeDef.installmentFeeRate);
		}
		//分期手续费固定金额
		if (req.installmentFeeAmt != null && isAgree) {
			acct.setInstallmentFeeAmt(req.installmentFeeAmt);
		} else {
			if(null == loanFeeDef.installmentFeeAmt && PrepaymentFeeMethod.A == loanFeeDef.installmentFeeCalMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",分期手续费固定金额为空");
			}
			acct.setInstallmentFeeAmt(loanFeeDef.installmentFeeAmt);
		}
		//灵活还款包费率
		if(logger.isDebugEnabled())
			logger.debug("灵活还款计划包费计算方式:[{}]",loanFeeDef.prepayPkgFeeCalMethod);
		if (req.prepaymentFeeRate != null && isAgree) {
			acct.setPrepayPkgFeeRate(req.prepaymentFeeRate);
		} else {
			if(null == loanFeeDef.prepayPkgFeeAmountRate && PrepaymentFeeMethod.R == loanFeeDef.prepayPkgFeeCalMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",灵活还款计划包费比例为空");
			}
			acct.setPrepayPkgFeeRate(loanFeeDef.prepayPkgFeeAmountRate);
		}
		//灵活还款包固定金额
		if (req.prepaymentFeeAmt != null && isAgree) {
			acct.setPrepayPkgFeeAmt(req.prepaymentFeeAmt);
		} else {
			if(null == loanFeeDef.prepayPkgFeeAmount && PrepaymentFeeMethod.A == loanFeeDef.prepayPkgFeeCalMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",灵活还款计划包费金额为空");
			}
			acct.setPrepayPkgFeeAmt(loanFeeDef.prepayPkgFeeAmount);
		}
		//罚息利率
		if (req.penaltyRate != null && isAgree) {
			acct.setPenaltyRate(req.penaltyRate);
		} else {
			acct.setPenaltyRate(loanFeeDef.penaltyIntTableId);
		}
		//复利利率
		if (req.compoundRate != null && isAgree) {
			acct.setCompoundRate(req.compoundRate);
		} else {
			acct.setCompoundRate(loanFeeDef.compoundIntTableId);
		}		
		//基础利率
		if (req.interestRate != null && isAgree) {
			acct.setInterestRate(req.interestRate);
		} else {
			acct.setInterestRate(loanFeeDef.interestRate);
		}	
		//印花税费率
		if(logger.isDebugEnabled())
			logger.debug("印花税计算方式:[{}]",loanFeeDef.stampCalcMethod);
		if (req.stampRate != null && isAgree) {
			acct.setStampdutyRate(req.stampRate.multiply(BigDecimal.valueOf(loanFeeDef.initTerm.longValue())));
		} else {
			if(null == loanFeeDef.stampRate && PrepaymentFeeMethod.R == loanFeeDef.stampCalcMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",印花税率为空");
			}
			acct.setStampdutyRate(loanFeeDef.stampRate);
		}	
		//印花税费率
		if (req.stampAmt != null && isAgree) {
			acct.setStampAmt(req.stampAmt);
		} else {
			if(null == loanFeeDef.stampAMT && PrepaymentFeeMethod.A == loanFeeDef.stampCalcMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",印花税固定金额为空");
			}
			acct.setStampAmt(loanFeeDef.stampAMT);
		}	
		//代收服务费固定金额
		if(logger.isDebugEnabled())
			logger.debug("贷款服务费计算方式:[{}]",loanFeeDef.replaceFeeCalMethod);
		if (req.agentFeeAmount != null && isAgree) {
			acct.setReplaceSvcFeeAmt(req.agentFeeAmount);
		} else {
			if(null == loanFeeDef.replaceFeeAmt && CalcMethod.A == loanFeeDef.replaceFeeCalMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",代收服务费收取比例为空");
			}
			acct.setReplaceSvcFeeAmt(loanFeeDef.replaceFeeAmt);
		}
		//代收服务费率
		if (req.getAgentFeeRate() != null && isAgree) {
			acct.setReplaceSvcFeeRate(req.agentFeeRate);
		} else {
			if(null == loanFeeDef.replaceFeeRate && CalcMethod.R== loanFeeDef.replaceFeeCalMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",代收服务费固定金额为空");
			}
			acct.setReplaceSvcFeeRate(loanFeeDef.replaceFeeRate);
		}
		
		//兜底 增加代收罚金费率
	    acct.setReplacePenaltyRate(loanFeeDef.replacePenaltyRate == null?BigDecimal.ZERO:loanFeeDef.replacePenaltyRate);
	    
		return acct;
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
		TNRAAcctSetupWithDrawReq req = (TNRAAcctSetupWithDrawReq) context.getMsRequestInfo();
		
		openAcctCommService.checkCustomer(context);
		
		
		if(null == loanPlan.loanMold)
			throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());
		
		if(context.getLoanPlan().loanType != LoanType.MCEI 
		&& context.getLoanPlan().loanType != LoanType.MCEP){
			throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+"，不是等额本息/等额本金贷款产品");
		}
		
		if(context.getLoanPlan().loanMold != LoanMold.M 
		&& context.getLoanPlan().loanMold != LoanMold.S){
			throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+"，不是非循环贷款产品");
		}
		
		//20151127 由于传入贷款子产品编码，说明已经约定好最大最小放款金额，不需要检查，
		//否则在贷款审批途中，子产品的金额区间修改后，导致放款金额和子产品金额区间不一致而被拒绝放款
//		if(txnInfo.getTransAmt().compareTo(loanFeeDef.maxAmount)>0 ){
//			throw new ProcessException(MsRespCode.E_1036.getCode(),MsRespCode.E_1036.getMessage());
//		}
//		if(txnInfo.getTransAmt().compareTo(loanFeeDef.minAmount)<0){
//			throw new ProcessException(MsRespCode.E_1037.getCode(),MsRespCode.E_1037.getMessage());
//		}
		//20151127  传入子产品编号，但是子产品期数和传入期数不一致，异常
		if(null != req.loanTerm &&loanFeeDef.initTerm.compareTo(req.loanTerm)!=0){
			throw new ProcessException(MsRespCode.E_1043.getCode(), MsRespCode.E_1043.getMessage()+",字段[LOAN_TERM]与贷款子产品期数不一致");
		}
		if(loanPlan.loanValidity.before(txnInfo.getBizDate())){
			throw new ProcessException(MsRespCode.E_1038.getCode(), MsRespCode.E_1038.getMessage());
		}
		if(loanPlan.loanStaus != LoanPlanStatus.A){
			throw new ProcessException(MsRespCode.E_1039.getCode(),MsRespCode.E_1039.getMessage());
		}
//		if(loanFeeDef.loanFeeDefStatus != null && LoanFeeDefStatus.A != loanFeeDef.loanFeeDefStatus){
//			throw new ProcessException(MsRespCode.E_1068.getCode(),MsRespCode.E_1068.getMessage());
//		}
	}
	
}
