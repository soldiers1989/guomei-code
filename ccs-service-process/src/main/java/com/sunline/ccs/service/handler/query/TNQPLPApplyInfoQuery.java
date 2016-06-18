package com.sunline.ccs.service.handler.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TNQPLPApplyInfoReq;
import com.sunline.ccs.service.msentity.TNQPLPApplyInfoResp;
import com.sunline.ccs.service.util.DateFormatUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 产品信息查询(审批系统使用)
 * @author ymk
 *
 */
@Service
public class TNQPLPApplyInfoQuery {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	AppapiCommService appapiCommService;
	@Autowired
	MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Autowired
	McLoanProvideImpl mcLoanProvideImpl;
//	@Autowired
//	private QueryCommService queryCommService;
    @Autowired
    GlobalManagementService globalManagementService;
    @Autowired
    OpenAcctCommService openAcctCommService;
    

	@Value("#{env.respDateFormat}")
	private String respDateFormat;
	public TNQPLPApplyInfoResp handler(TNQPLPApplyInfoReq req) throws ProcessException {
		LogTools.printLogger(log, "TNQPLPApplyInfoReq", "产品信息查询(审批系统使用)", req, true);
		LogTools.printObj(log, req, "请求参数TNQPLPApplyInfoReq");
		//请求报文
		String loanCode = req.getLoanCode();
		String loanTerm = req.getLoanTerm();
		BigDecimal loanAmt = req.getLoanAmt();
		Integer loanFeeDefId = req.getLoanFeeDefId();
		
		//初始化返回报文
		TNQPLPApplyInfoResp resp = new TNQPLPApplyInfoResp();
		TxnInfo txnInfo = new TxnInfo();
		try{
			
			resp.setLoanCode(loanCode);
			resp.setLoanAmt(loanAmt);
			
			//根据贷款产品代码loanCode和期数获取金额范围
			LoanPlan loanPlan;
			try{
				loanPlan = unifiedParamFacilityProvide.loanPlan(loanCode);
				
				if(loanPlan == null||loanPlan.loanValidity.before(req.getBizDate())
						|| loanPlan.loanStaus != LoanPlanStatus.A){
					throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());				
				}
			}catch(Exception e){
				throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());				
			}
			
			LoanFeeDef loanFeeDef;
			BigDecimal minAmt = new BigDecimal(0);
			BigDecimal maxAmt = new BigDecimal(0);
			//如果报文传入子产品代码，则直接返回产品，如果没有输入子产品代码，则判断是否为随借随还，
			//如果是随借随还，并期数金额为空，则返回默认产品，否则根据期数金额确定产品
			try{
				if(loanFeeDefId != null){
					loanFeeDef = unifiedParamFacilityProvide.loanFeeDefByKey(loanPlan.loanCode,Integer.valueOf(req.getLoanFeeDefId()));
					minAmt = loanFeeDef.minAmount;
					maxAmt = loanFeeDef.maxAmount;
					if (null==req.getLoanAmt()) {
						loanAmt = BigDecimal.ZERO;
					}else {
						loanAmt = req.getLoanAmt();
					}
					
					
				}else {
					//随借随换如果期数金额为空 取默认期数
					if(loanPlan.loanType == LoanType.MCAT){
						if(loanAmt == null && loanTerm == null){   //金额期数都为空取默认参数
							loanFeeDef = unifiedParamFacilityProvide.loanFeeDefMCAT(loanCode);
						}else if(loanAmt != null && loanTerm == null){  //金额不为空期数为空
							loanFeeDef = unifiedParamFacilityProvide.loanFeeDefMCAT(loanCode,loanAmt);
						}else if (loanAmt != null && loanTerm != null) {  //金额期数都不为空
							loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(loanCode,Integer.parseInt(loanTerm),loanAmt);
						}else {
							throw new ProcessException(MsRespCode.E_1008.getCode(), MsRespCode.E_1008.getMessage());
						}
						minAmt = loanFeeDef.minAmount;
						maxAmt = loanFeeDef.maxAmount;
							
					}else{
						if(loanTerm == null || loanAmt == null){
							throw new ProcessException(MsRespCode.E_1008.getCode(), MsRespCode.E_1008.getMessage());
						}
						//根据金额期数查找loanfeeDef
						loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(loanCode,Integer.parseInt(loanTerm),loanAmt);
						minAmt = loanFeeDef.minAmount;
						maxAmt = loanFeeDef.maxAmount;
					}	
				}
				//loanFeeDef是否为空或者为非使用中
				if(loanFeeDef== null || LoanFeeDefStatus.A != loanFeeDef.loanFeeDefStatus){
					throw new ProcessException(MsRespCode.E_1068.getCode(),MsRespCode.E_1068.getMessage());
				}
				//对loanTerm赋值方便计算月费率
				loanTerm = String.valueOf(loanFeeDef.initTerm);
			}catch(Exception e){
				if(log.isErrorEnabled())
					log.error(e.getMessage(), e);
				if(e instanceof ProcessException){
					ProcessException pe = (ProcessException)e;
					if(pe.getErrorCode()== MsRespCode.E_1008.getCode()){
						throw pe;
					}
				}
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage());				
			}
			
			resp.setLoanTerm(loanFeeDef.initTerm);
			//设置最小允许贷款金额和最大允许贷款金额
			resp.setMinAmt(minAmt);
			resp.setMaxAmt(maxAmt);
			//获取贷款类型
			resp.setLoanType(loanPlan.loanType);
			
			//设置月利率和年利率
			resp.setAnnualIntRate(loanFeeDef.interestRate);
			resp.setMonthlyIntRate(loanFeeDef.interestRate.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP));
			//代收服务费
			if(loanFeeDef.replaceFeeMethod.equals(LoanFeeMethod.F)){
				resp.setMonthlyAgentFeeAmt(loanFeeDef.replaceFeeAmt == null ? BigDecimal.ZERO : loanFeeDef.replaceFeeAmt );
				resp.setMonthlyAgentFeeRate(loanFeeDef.replaceFeeRate == null ?BigDecimal.ZERO : loanFeeDef.replaceFeeRate );
			}else if(loanFeeDef.replaceFeeMethod.equals(LoanFeeMethod.E)){
				resp.setMonthlyAgentFeeAmt(loanFeeDef.replaceFeeAmt == null ?  BigDecimal.ZERO : loanFeeDef.replaceFeeAmt.divide(BigDecimal.valueOf(Long.parseLong(loanTerm)), 8, RoundingMode.HALF_UP) );
				resp.setMonthlyAgentFeeRate(loanFeeDef.replaceFeeRate == null ?  BigDecimal.ZERO : loanFeeDef.replaceFeeRate.divide(BigDecimal.valueOf(Long.parseLong(loanTerm)), 8, RoundingMode.HALF_UP) );
			}
			//设置每月服务费和每月寿险计划包费
			if(loanFeeDef.loanFeeMethod.equals(LoanFeeMethod.F)){
				resp.setMonthlySvcFee(loanFeeDef.feeAmount == null ? BigDecimal.ZERO : loanFeeDef.feeAmount );
				resp.setMonthlySvcFeeRate(loanFeeDef.feeRate == null ?BigDecimal.ZERO : loanFeeDef.feeRate );
			}else if(loanFeeDef.loanFeeMethod.equals(LoanFeeMethod.E)){
				resp.setMonthlySvcFee(loanFeeDef.feeAmount == null ?  BigDecimal.ZERO : loanFeeDef.feeAmount.divide(BigDecimal.valueOf(Long.parseLong(loanTerm)), 6, RoundingMode.HALF_UP) );
				resp.setMonthlySvcFeeRate(loanFeeDef.feeRate == null ?  BigDecimal.ZERO : loanFeeDef.feeRate.divide(BigDecimal.valueOf(Long.parseLong(loanTerm)), 6, RoundingMode.HALF_UP) );
			}
			if(loanFeeDef.lifeInsuFeeMethod.equals(LoanFeeMethod.F)){
				resp.setMonthlyLifeInsuFee(loanFeeDef.lifeInsuFeeAmt == null ? BigDecimal.ZERO: loanFeeDef.lifeInsuFeeAmt );
				resp.setMonthlyLifeInsuRate(loanFeeDef.lifeInsuFeeRate == null ? BigDecimal.ZERO: loanFeeDef.lifeInsuFeeRate );
			}else if(loanFeeDef.lifeInsuFeeMethod.equals(LoanFeeMethod.E)){
				resp.setMonthlyLifeInsuFee(loanFeeDef.lifeInsuFeeAmt == null ? BigDecimal.ZERO: loanFeeDef.lifeInsuFeeAmt.divide(BigDecimal.valueOf(Long.parseLong(loanTerm)), 6, RoundingMode.HALF_UP) );
				resp.setMonthlyLifeInsuRate(loanFeeDef.lifeInsuFeeRate == null ? BigDecimal.ZERO: loanFeeDef.lifeInsuFeeRate.divide(BigDecimal.valueOf(Long.parseLong(loanTerm)), 6, RoundingMode.HALF_UP) );
			}
			//20151127
			//分期手续费
			if(loanFeeDef.installmentFeeMethod.equals(LoanFeeMethod.F)){
				resp.setMonthlyInstlmtFeeAmt(loanFeeDef.installmentFeeAmt == null ? BigDecimal.ZERO : loanFeeDef.installmentFeeAmt );
				resp.setMonthlyInstlmtFeeRate(loanFeeDef.installmentFeeRate == null ?BigDecimal.ZERO : loanFeeDef.installmentFeeRate );
			}else if(loanFeeDef.installmentFeeMethod.equals(LoanFeeMethod.E)){
				resp.setMonthlyInstlmtFeeAmt(loanFeeDef.installmentFeeAmt == null ?  BigDecimal.ZERO : loanFeeDef.installmentFeeAmt.divide(BigDecimal.valueOf(Long.parseLong(loanTerm)), 6, RoundingMode.HALF_UP) );
				resp.setMonthlyInstlmtFeeRate(loanFeeDef.installmentFeeRate == null ?  BigDecimal.ZERO : loanFeeDef.installmentFeeRate.divide(BigDecimal.valueOf(Long.parseLong(loanTerm)), 6, RoundingMode.HALF_UP) );
			}
			//保险费
			if(loanFeeDef.installmentFeeMethod.equals(LoanFeeMethod.F)){
				resp.setMonthlyInsFeeAmt(loanFeeDef.insAmt == null ? BigDecimal.ZERO : loanFeeDef.insAmt );
				resp.setMonthlyInsRate(loanFeeDef.insRate == null ?BigDecimal.ZERO : loanFeeDef.insRate );
			}else if(loanFeeDef.installmentFeeMethod.equals(LoanFeeMethod.E)){
				resp.setMonthlyInsFeeAmt(loanFeeDef.insAmt == null ?  BigDecimal.ZERO : loanFeeDef.insAmt.divide(BigDecimal.valueOf(Long.parseLong(loanTerm)), 6, RoundingMode.HALF_UP) );
				resp.setMonthlyInsRate(loanFeeDef.insRate == null ?  BigDecimal.ZERO : loanFeeDef.insRate.divide(BigDecimal.valueOf(Long.parseLong(loanTerm)), 6, RoundingMode.HALF_UP) );
			}
			
			//每月应还总额
			if(loanPlan.loanType.equals(LoanType.MCAT)){
				resp.setLoanTermAmt(BigDecimal.ZERO);
			}else{
				resp.setLoanTermAmt(getMonthTermAmt(loanAmt, Integer.valueOf(loanTerm), loanPlan, loanFeeDef,req.getBizDate()));
			}
			//产品有效期
			resp.setLoanValidity(DateFormatUtil.format(loanPlan.loanValidity, respDateFormat));
			
			//20151127
			resp.setLoanFeeDefId(loanFeeDef.loanFeeDefId.toString());//子产品编码
			resp.setLoanFeeDefStatus(loanFeeDef.loanFeeDefStatus);//子产品状态
			resp.setSvcFeeCalMtd(loanFeeDef.loanFeeCalcMethod);//贷款服务费计算方式
			resp.setSvcFeeChargeMtd(loanFeeDef.loanFeeMethod);//贷款服务费收取方式
			resp.setLifeinsuFeeCalMtd(loanFeeDef.lifeInsuFeeCalMethod);//寿险计划包计算方式
			resp.setLifeinsuFeeChargeMtd(loanFeeDef.lifeInsuFeeMethod);//寿险计划包收取方式
			resp.setInstlmtFeeCalMtd(loanFeeDef.installmentFeeCalMethod);//分期手续费计算方式
			resp.setInstlmtFeeChargeMtd(loanFeeDef.installmentFeeMethod);//分期手续费收取方式
			resp.setInsCalcMethod(loanFeeDef.insCalcMethod);//保险费计算方式
			resp.setInsCollMethod(loanFeeDef.insCollMethod);//保险费收取方式
			resp.setAgentFeeChargeMtd(loanFeeDef.replaceFeeMethod);//代收服务费收取方式
			resp.setAgentFeeCalMtd(loanFeeDef.replaceFeeCalMethod);//代收服务费计算方式
			
			
		}
		catch(ProcessException pe){
			if(log.isErrorEnabled())
				log.error(pe.getMessage(),pe);
			appapiCommService.preException(pe, pe, txnInfo);
		}
		catch(Exception e){
			if(log.isErrorEnabled())
				log.error(e.getMessage(),e);
			appapiCommService.preException(e, null, txnInfo);				
		}finally{
			LogTools.printLogger(log, "STNQAAcctsbyCustUUIDResp", "合同列表查询", resp, false);
		}
		setResponse(resp, txnInfo);
		return resp;
		
	}
		
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(MsResponseInfo resp, TxnInfo txnInfo) {
		
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
	
	private BigDecimal getMonthTermAmt(BigDecimal loanAmt,Integer loanTerm,LoanPlan loanPlan,LoanFeeDef loanFeeDef,Date bizDate)
	throws Exception{
		CcsAcct acct = acctTestCal(loanPlan.productCode, loanFeeDef,bizDate);
		
		CcsLoanReg loanReg = openAcctCommService.genLoanReg(acct,loanPlan,loanFeeDef,
				loanTerm,null,loanAmt,
				null,null,bizDate,
				Indicator.N,null,null);
//		
//		CcsLoanReg loanReg = queryCommService.loanRegPrecalculate(acct,loanPlan, loanFeeDef, 
//				loanAmt, loanTerm, 
//				null, Indicator.N);
		BigDecimal tmpTotAmt = BigDecimal.ZERO;
//		try{
			List<CcsRepaySchedule> ccsRepayScheduleList =mcLoanProvideImpl.getLSchedule(loanReg, loanFeeDef, bizDate, acct);			
			CcsRepaySchedule schedule = ccsRepayScheduleList.get(0);
//			tmpTotAmt = tmpTotAmt.add(schedule.getLoanTermPrin()==null?BigDecimal.ZERO:schedule.getLoanTermPrin()).
//					add(schedule.getLoanTermFee()==null?BigDecimal.ZERO:schedule.getLoanTermFee()).
//					add(schedule.getLoanTermInt()==null?BigDecimal.ZERO:schedule.getLoanTermInt()).
//					add(schedule.getLoanStampdutyAmt()==null?BigDecimal.ZERO:schedule.getLoanStampdutyAmt()).
//					add(schedule.getLoanLifeInsuAmt()==null?BigDecimal.ZERO:schedule.getLoanLifeInsuAmt());
			//修复 TNTLLoanScheduleCalc接口与TNQPLPApplyInfo接口查出的每月总还款额不同 的问题，增加了贷款服务费，代收服务费，保费的金额  by lizz 20160220
			tmpTotAmt = tmpTotAmt.add(schedule.getLoanTermPrin()==null?BigDecimal.ZERO:schedule.getLoanTermPrin()).
					add(schedule.getLoanTermFee()==null?BigDecimal.ZERO:schedule.getLoanTermFee()).
					add(schedule.getLoanTermInt()==null?BigDecimal.ZERO:schedule.getLoanTermInt()).
					add(schedule.getLoanStampdutyAmt()==null?BigDecimal.ZERO:schedule.getLoanStampdutyAmt()).
					add(schedule.getLoanLifeInsuAmt()==null?BigDecimal.ZERO:schedule.getLoanLifeInsuAmt()).
					add(schedule.getLoanSvcFee()==null?BigDecimal.ZERO:schedule.getLoanSvcFee()).
					add(schedule.getLoanReplaceSvcFee()==null?BigDecimal.ZERO:schedule.getLoanReplaceSvcFee()).
					add(schedule.getLoanInsuranceAmt()==null?BigDecimal.ZERO:schedule.getLoanInsuranceAmt());
//		}
//		catch(Exception e){
//			throw e;
//		}
		return tmpTotAmt;
	}
	
	/*
	 * 账户试算，只赋值试算schedule时需要的部分，product_cd he next_stmt_date
	 */
	private CcsAcct acctTestCal(String productCd,LoanFeeDef loanFeeDef,Date bizDate){
		CcsAcct acct = new CcsAcct();
		acct.setAcctNbr(Long.valueOf(0));
		acct.setAcctType(AccountType.E);

		acct.setProductCd(productCd);
		
		acct.setNextStmtDate(microCreditRescheduleUtils.getLoanPmtDueDate(
				 bizDate, loanFeeDef, 2));// 计算下个账单日期
		Calendar cal = Calendar.getInstance();
		cal.setTime(bizDate);
		int bc = cal.get(Calendar.DATE);// 账单日
		
		acct.setCycleDay(String.format("%02d", bc));
		
		acct.setAgreementRateInd(Indicator.N);

		return acct;
	}
}

