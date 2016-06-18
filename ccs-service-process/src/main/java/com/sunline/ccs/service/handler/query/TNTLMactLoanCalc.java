package com.sunline.ccs.service.handler.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

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
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.OpenAcctCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msdentity.STNTLMactLoanCalcReq;
import com.sunline.ccs.service.msdentity.STNTLMactLoanCalcResp;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.util.DateFormatUtil;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;


/*
 * 随借随还信息试算
 */
@Service
public class TNTLMactLoanCalc {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private QueryCommService queryCommService;
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	McLoanProvideImpl  loanProvideImpl;
	@Autowired
	McLoanProvideImpl mcLoanProvideImpl;
	@Autowired
	MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Value("#{env.respDateFormat}")
	private String respDateFormat;
	@Autowired
	private GlobalManagementService globalManageService;
	@Autowired
	AppapiCommService appapiCommService;
	
    @Autowired
    GlobalManagementService globalManagementService;
//    Date bizDate;

	
	public STNTLMactLoanCalcResp handler(STNTLMactLoanCalcReq req) throws ProcessException {
		LogTools.printLogger(log, "STNTLMactLoanCalcReq", "随借随还信息试算", req, true);
		LogTools.printObj(log, req, "请求参数STNTLMactLoanCalcReq");
		
		STNTLMactLoanCalcResp resp = new STNTLMactLoanCalcResp();
		TxnInfo txnInfo = new TxnInfo();
		try{
			ProductCredit productCredit;
			LoanPlan   loanPlan;
			LoanFeeDef loanFeeDef;
			AccountAttribute acctAttr;

			try{
				loanPlan = unifiedParamFacilityProvide.loanPlan(req.getLoanCode());
				productCredit = unifiedParameterFacility.loadParameter(loanPlan.productCode, ProductCredit.class);
				loanFeeDef = unifiedParamFacilityProvide.loanFeeDefMCAT(req.getLoanCode(),req.getLoanAmt());
				acctAttr = unifiedParameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
				//货款产品有效期是否小于业务日期
				if(loanPlan==null || loanPlan.loanValidity.before(req.getBizDate())){
					throw new ProcessException(MsRespCode.E_1038.getCode(), MsRespCode.E_1038.getMessage());
				}
				//贷款产品活动状态是否为非使用中
				if(loanPlan.loanStaus != LoanPlanStatus.A){
					throw new ProcessException(MsRespCode.E_1039.getCode(),MsRespCode.E_1039.getMessage());
				}
			}
			catch(Exception e){
				throw new ProcessException(MsRespCode.E_1057.getCode(),MsRespCode.E_1057.getMessage());				
			}
			
			if(!loanPlan.loanType.equals(LoanType.MCAT)){
				throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+",只支持随借随还");
			}
						
			
			CcsAcct acct = acctTestCal(loanPlan.productCode, loanFeeDef,req.getBizDate());
			acct = openAcctCommService.setNextStmtDate(acct, req.getBizDate(), loanFeeDef, acctAttr, loanPlan);
			resp.setContraBeginDate(DateFormatUtil.format( req.getBizDate(), respDateFormat));
			Date expireDate = openAcctCommService.getExpireDateByTerm(loanFeeDef, req.getBizDate(), loanFeeDef.initTerm);
			resp.setAcctExpireDate(DateFormatUtil.format(expireDate,respDateFormat));
			resp.setBillCycleDay(acct.getCycleDay());
			Date pmtDueDate = microCreditRescheduleUtils.getNextPaymentDay(
					loanPlan.productCode, acct.getNextStmtDate());
			//最后还款日后两位
			Calendar cal = Calendar.getInstance();
			cal.setTime(pmtDueDate);
			int bc = cal.get(Calendar.DATE);
			resp.setPmtDueDay(String.format("%02d", bc));
			
			resp.setDailyPenaltyRate(loanFeeDef.penaltyIntTableId.divide(BigDecimal.valueOf(Long.parseLong("360")), 6, RoundingMode.HALF_UP) );
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
			LogTools.printLogger(log, "TNTLLoanScheduleCalc", "还款计划试算", resp, false);
		}
		
		setResponse(resp, txnInfo);
		
		return resp;
	}

	/*
	
	/*
	 * 账户试算，只赋值试算schedule时需要的部分，product_cd he next_stmt_date
	 */
	private CcsAcct acctTestCal(String productCd,LoanFeeDef loanFeeDef,Date bizDate){
		CcsAcct acct = new CcsAcct();
		acct.setProductCd(productCd);
		
		acct.setNextStmtDate(microCreditRescheduleUtils.getLoanPmtDueDate(
				 bizDate, loanFeeDef, 2));// 计算下个账单日期
		Calendar cal = Calendar.getInstance();
		cal.setTime(bizDate);
		int bc = cal.get(Calendar.DATE);// 账单日
		
		acct.setCycleDay(String.format("%02d", bc));
		return acct;
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
	
}

