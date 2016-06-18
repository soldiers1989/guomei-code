package com.sunline.ccs.service.handler.appapi;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TFNCommodyLoanSetupReq;
import com.sunline.ccs.service.msentity.TFNCommodyLoanSetupResp;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 商品贷开户
 * @author zhengjf
 *
 */
@Service
public class TFNCommodyLoanSetup extends AbstractHandler {

private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	public AppapiCommService appapiCommService;
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
	@PersistenceContext
	private EntityManager em;
	@Autowired
	TxnUtils txnUtils;
	
	@Override
	public MsResponseInfo execute(MsRequestInfo msRequestInfo) {
		LogTools.printLogger(logger, getClass().getSimpleName(), "商品贷开户", msRequestInfo, true);
		TFNCommodyLoanSetupReq req = (TFNCommodyLoanSetupReq) msRequestInfo;
		LogTools.printObj(logger, req, "TFNCommodyLoanSetupReq请求参数");
		TFNCommodyLoanSetupResp resp = new TFNCommodyLoanSetupResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		
		context.setTxnInfo(txnInfo);
		context.setMsRequestInfo(req);
		
		try {
			this.initTxnInfo(req, txnInfo);
			
			this.checkReq(req,txnInfo);
			
			// 确定产品参数
			openAcctCommService.getProdParam(context);

			this.valiBiz(context);
			
			this.bizProc(context);
			//开户事物成功即返回合同号
			if(context.getAccount()!=null){
//				resp.setAcctSetupInd(Indicator.Y);
				resp.setContractNo(context.getAccount().getContrNbr());
			}
			
		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			
			appapiCommService.preException(pe, pe, txnInfo);
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();//e不是pe，处理逻辑与没有code的pe一样
			appapiCommService.preException(pe, pe, txnInfo);
		}finally{
			LogTools.printLogger(logger, "TFNCommodyLoanSetupResp", "商品贷开户", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo);
		LogTools.printObj(logger, resp, "TFNCommodyLoanSetupResp响应参数");
		
		
		return resp;
	}
	
	
	/**
	 * 业务处理，事务控制
	 * @param context
	 */
	@Transactional
	private void bizProc(TxnContext context) {
		TFNCommodyLoanSetupReq req = (TFNCommodyLoanSetupReq) context.getMsRequestInfo();
		TxnInfo txnInfo = context.getTxnInfo();
		
		// 获取有效期
		openAcctCommService.getExpireDate(context);
		// 确定客户
		this.mergeCustomer(context);
		
		  // 确定地址
	    if (StringUtils.isNotBlank(req.province)) {
	    	openAcctCommService.mergeAddress(txnInfo.getOrg(), context.getCustomer().getCustId(), AddressType.H, req.familyaddr, req.city,
					  req.country, req.areacode, req.province, req.familytel, req.zipcode);
	    }
	    //用家庭 市,国家,区,省 放入单位地址
	    if (StringUtils.isNotBlank(req.workAddr)) {
	    	openAcctCommService.mergeAddress(txnInfo.getOrg(), context.getCustomer().getCustId(), AddressType.C, req.workAddr, req.city,
					req.country, req.areacode, req.province, req.workTel,req.workZip);
	    }

	    // 确定联系人, 附卡没有联系人
	    if (req.linkrelation1 != null) {
	    	openAcctCommService.mergeLinkman(txnInfo.getOrg(), context.getCustomer().getCustId(), 
	    			req.linkrelation1, req.link1, null, req.linkmobile1);
	    }
	    if (req.linkrelation2 != null) {
	    	openAcctCommService.mergeLinkman(txnInfo.getOrg(), context.getCustomer().getCustId(), 
					req.linkrelation2, req.link2,null, req.linkmobile2);
	    }

	    // 新建或更新工作信息
	    openAcctCommService.mergeEmployee(txnInfo.getOrg(), context.getCustomer().getCustId(), req.workCorp, req.workTel, null,
			       req.positionLevel, req.unitkind, null, req.position,
			       req.monthlyWages==null?BigDecimal.ZERO: req.monthlyWages.multiply(BigDecimal.valueOf(12)), 
			       req.monthlyWages==null?BigDecimal.ZERO: req.monthlyWages.multiply(BigDecimal.valueOf(12)));

		// 设置客户的额度信息
		openAcctCommService.mergeCustomerCrLmt(context);
		
		//确定账户及联机账户
		this.mergeAcctAndO(context);

		// 建立卡片
		openAcctCommService.mergeCard(context);
		
//		openAcctCommService.mergeLoanReg(context);
	}
	
	/**
	 * 保存客户信息
	 * @param context
	 */
	private void mergeCustomer(TxnContext context) {
		TFNCommodyLoanSetupReq req = (TFNCommodyLoanSetupReq) context.getMsRequestInfo();
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
		TFNCommodyLoanSetupReq req = (TFNCommodyLoanSetupReq) context.getMsRequestInfo();
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
//		acct.setInterestRate(req.agreeRate);//协议利率
		acct.setAgreementRateExpireDate(txnInfo.getAgreeRateExpireDate());//协议利率有效期
		acct.setAcqId(req.getCooperatorID());
		acct.setLoanMold(loanPlan.loanMold);
		//随借随还-约定还款设置F全额还款
		acct.setDdInd(DdIndicator.F);
		
		//二期新增 各种费率
		acct = this.setAcctRateByReq(req, acct, loanFeeDef);
		
		//20151127
		acct.setLoanFeeDefId(loanFeeDef.loanFeeDefId.toString());//子产品编号
		acct.setPurpose(req.purpose);//贷款用途
		acct.setApplyDate(req.applyDate);//申请日期
		acct.setSubTerminalType(req.getSubTerminalType());//销售渠道
		acct.setJoinLifeInsuInd(req.jionLifeInsuInd);//20151204
		acct.setPrepayPkgInd(txnInfo.getPrepayPkgInd());
		
		logger.info("---------------------插入账户信息表---------------------");
		rAcct.save(acct);
		
		logger.info("---------------------插入账户信息表-授权---------------------");
		
		accto.setContrNbr(acct.getContrNbr());
		rAcctO.save(accto);
		
		//设置合同号
		txnInfo.setContrNo(acct.getContrNbr());
	}

	/**
	 * 检查报文
	 * @param req
	 */
	private void checkReq(TFNCommodyLoanSetupReq req,TxnInfo txnInfo) {
		
		//检查开户行号
		appapiCommService.checkBankCode(req.bankcode);
		
		if(null != req.interestRate && req.interestRate.compareTo(BigDecimal.ZERO)<0){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{INTEREST_RATE},不能小于0");
		}
		
		if(null != req.contraExpireDate && req.contraExpireDate.compareTo(txnInfo.getBizDate())<0){
			if(logger.isDebugEnabled())
				logger.debug("合同有效期[{}]小于当前业务日期[{}]",req.contraExpireDate,txnInfo.getBizDate());
			throw new ProcessException(MsRespCode.E_1059.getCode(),MsRespCode.E_1059 .getMessage());
		}
		
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
	private void setResponse(TFNCommodyLoanSetupResp resp, TFNCommodyLoanSetupReq req, TxnInfo txnInfo) {
		
		if(txnInfo.getResponsCode() == null || txnInfo.getResponsCode().isEmpty()){
			txnInfo.setResponsCode(MsRespCode.E_0000.getCode());
			txnInfo.setResponsDesc(MsRespCode.E_0000.getMessage());
		}
		
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setContractNo(txnInfo.getContrNo());
			resp.setStatus("S");//交易成功
		}else{
//			if(txnInfo.getContrNo()!=null){
//				resp.setContractNo(txnInfo.getContrNo());
//			}				
			resp.setStatus("F");//交易失败
		}
	}

	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(TFNCommodyLoanSetupReq req, TxnInfo txnInfo) {
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(txnInfo.getBizDate(), txnInfo.getServiceSn()));
		txnInfo.setLoanCode(req.loanCode);
		txnInfo.setOrg(OrganizationContextHolder.getCurrentOrg());
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setApplyNo(req.applyNo);
		txnInfo.setUuid(req.uuid);
		txnInfo.setTransAmt(BigDecimal.ZERO);
		txnInfo.setCreditLmt(req.loanAmt);
		txnInfo.setContraExpireDate(req.contraExpireDate);//合同有效期
		txnInfo.setAgreeRate(req.interestRate);// 协议利率
		txnInfo.setAgreeRateExpireDate(req.agreeRateExpireDate);//协议利率有效期
		txnInfo.setIdNo(req.idNo);//证件号
		txnInfo.setIdType(req.idType);//证件类型
		txnInfo.setMobile(req.cellPhone);
		txnInfo.setLoanFeeDefId(req.loanFeeDefId);
		txnInfo.setJionLifeInsuInd(req.jionLifeInsuInd);//20151204
		txnInfo.setPrepayPkgInd(req.getPrepayPkgInd()== null ? Indicator.N : req.getPrepayPkgInd());
		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "txnInfo交易中间信息");
		
	}
	
	/**
	* 业务检查
	 * @param context
	*/
	private void valiBiz(TxnContext context){
		TxnInfo txnInfo = context.getTxnInfo();
		Date expireDate = openAcctCommService.getExpireDateByTerm(context);
		
		openAcctCommService.checkCustomer(context);

		if(context.getLoanPlan().loanValidity.before(txnInfo.getBizDate())){
			throw new ProcessException(MsRespCode.E_1038.getCode(), MsRespCode.E_1038.getMessage());
		}
		// Date1.after(Date2),当Date1大于Date2时，返回TRUE，当小于等于时，返回false；
		if(null != txnInfo.getContraExpireDate()&&txnInfo.getContraExpireDate().after(expireDate)){
			throw new ProcessException(MsRespCode.E_1065.getCode(),
					MsRespCode.E_1065.getMessage()+DateUtils.formatDate2String(expireDate, DateUtils.YYYYMMDD));
		}
	}
	
	/**
	 * 使用req的费率数据设置acct
	 * @param loanReg
	 * @param acct
	 * @param loanFeeDef
	 * @param agreeInd
	 * @return
	 */
	public CcsAcct setAcctRateByReq(TFNCommodyLoanSetupReq req,CcsAcct acct,LoanFeeDef loanFeeDef){
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
			acct.setStampdutyRate(req.stampRate);
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

}
