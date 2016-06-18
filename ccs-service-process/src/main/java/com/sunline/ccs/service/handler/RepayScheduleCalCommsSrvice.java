package com.sunline.ccs.service.handler;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.ccs.service.msdentity.STNTLLoanScheduleCalcReq;
import com.sunline.ccs.service.msdentity.STNTLLoanScheduleCalcResp;
import com.sunline.ccs.service.msdentity.STNTLLoanScheduleCalcRespSubSchedule;
import com.sunline.ccs.service.util.DateFormatUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 还款试算公函
 * @author zhengjf
 *
 */
@Service
public class RepayScheduleCalCommsSrvice {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private OpenAcctCommService openAcctCommService;
	@Autowired
	McLoanProvideImpl mcLoanProvideImpl;
	private BigDecimal loanIntSum;
	@Value("#{env.respDateFormat}")
	private String respDateFormat;
	@Autowired
	AppapiCommService appapiCommService;
	
	public STNTLLoanScheduleCalcResp Trial(ProductCredit productCredit,AccountAttribute acctAttr,
			LoanPlan loanPlan,LoanFeeDef loanFeeDef,STNTLLoanScheduleCalcReq req) throws Exception {
		
		STNTLLoanScheduleCalcResp resp = new STNTLLoanScheduleCalcResp();
		
		loanIntSum = BigDecimal.ZERO;
		CcsAcct acct = acctTestCal(productCredit,acctAttr,loanPlan, loanFeeDef,req);
		
		CcsLoanReg loanReg = openAcctCommService.genLoanReg(acct,loanPlan,loanFeeDef,
				req.getLoanTerm(),null,req.getLoanAmt(),
				null,null,req.getBizDate(),
				req.getLifeInsuranceInd(),null,null);
		
		resp.setLoanTerm(loanReg.getLoanInitTerm());
		resp.setLoanInitPrin(loanReg.getLoanInitPrin());
		
		resp.setLoanFeeSum(loanReg.getLoanInitFee().add(loanReg.getTotLifeInsuAmt()).
				add(loanReg.getLoanSvcFee()).add(loanReg.getInsuranceAmt()).
				add(loanReg.getTotReplaceSvcFee()).add(loanReg.getTotPrepayPkgAmt()));
		
		List<CcsRepaySchedule> ccsRepayScheduleList =mcLoanProvideImpl.getLSchedule(loanReg, loanFeeDef, req.getBizDate(), acct);
		
		setSubScheduleList(resp,ccsRepayScheduleList);
		
		resp.setLoanIntSum(loanIntSum);
		
		return resp;
	}
	
	/*
	 * 账户试算，只赋值试算schedule时需要的部分，product_cd he next_stmt_date
	 */
	private CcsAcct acctTestCal(ProductCredit productCredit,AccountAttribute acctAttr,LoanPlan loanPlan,LoanFeeDef loanFeeDef,STNTLLoanScheduleCalcReq req){
		CcsAcct acct = new CcsAcct();
		acct.setAcctNbr(Long.valueOf(0));
		acct.setAcctType(AccountType.E);
		acct.setProductCd(productCredit.productCd);

		acct = openAcctCommService.setNextStmtDate(acct, req.getBizDate(), loanFeeDef,acctAttr,loanPlan);	
		
		//二期新增 各种费率
		acct = this.setAcctRateByReq(req, acct, loanFeeDef);
		
		//兜底二期新增灵活还款服务包
		acct.setPrepayPkgInd(req.prepayPkgInd==null ? Indicator.N : req.prepayPkgInd);//默认不使用协议费率
		
		return acct;
	}
	
	/**
	 * 使用req的费率数据设置acct
	 * @param loanReg
	 * @param acct
	 * @param loanFeeDef
	 * @param agreeInd
	 * @return
	 */
	public CcsAcct setAcctRateByReq(STNTLLoanScheduleCalcReq req,CcsAcct acct,LoanFeeDef loanFeeDef){
		//使用协议费率，这些费率都取acct中的   20151127不修正为0，直接赋值
		Boolean isAgree = Indicator.Y.equals(req.agreeRateInd);
		acct.setAgreementRateInd(req.agreeRateInd==null?Indicator.Y:req.agreeRateInd);//默认为使用协议费率
		
		//贷款服务费率
		if(log.isDebugEnabled()){
			log.debug("贷款服务费计算方式:[{}]",loanFeeDef.loanFeeCalcMethod);
		}
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
		if(log.isDebugEnabled())
			log.debug("寿险计划包计算方式:[{}]",loanFeeDef.lifeInsuFeeCalMethod);
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
		if(log.isDebugEnabled())
			log.debug("保险费计算方式:[{}]",loanFeeDef.insCalcMethod);
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
		if(log.isDebugEnabled())
			log.debug("分期手续费计算方式:[{}]",loanFeeDef.installmentFeeCalMethod);
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
		//灵活还款服务包费率
		if(log.isDebugEnabled())
			log.debug("提前还款计划包费计算方式:[{}]",loanFeeDef.prepayPkgFeeCalMethod);
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
		//代收服务费率
		if (req.getAgentFeeRate() != null && isAgree) {
			acct.setReplaceSvcFeeRate(req.agentFeeRate);
		} else {
			if(null == loanFeeDef.replaceFeeRate && CalcMethod.R== loanFeeDef.replaceFeeCalMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",代收服务费固定金额为空");
			}
			acct.setReplaceSvcFeeRate(loanFeeDef.replaceFeeRate);
		}
		//代收服务费固定金额
		if (req.agentFeeAmount != null && isAgree) {
			acct.setReplaceSvcFeeAmt(req.agentFeeAmount);
		} else {
			if(null == loanFeeDef.replaceFeeAmt && CalcMethod.A == loanFeeDef.replaceFeeCalMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",代收服务费收取比例为空");
			}
			acct.setReplaceSvcFeeAmt(loanFeeDef.replaceFeeAmt);
		}
		//印花税费率
		if(log.isDebugEnabled())
			log.debug("印花税计算方式:[{}]",loanFeeDef.stampCalcMethod);
		if (req.stampRate != null && isAgree) {
			acct.setStampdutyRate(req.stampRate.multiply(BigDecimal.valueOf(loanFeeDef.initTerm.longValue())));
		} else {
			if(null == loanFeeDef.stampRate && PrepaymentFeeMethod.R == loanFeeDef.stampCalcMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",印花税率为空");
			}
			acct.setStampdutyRate(loanFeeDef.stampRate);
		}	
		//印花税费固定金额
		if (req.stampAmt != null && isAgree) {
			acct.setStampAmt(req.stampAmt);
		} else {
			if(null == loanFeeDef.stampAMT && PrepaymentFeeMethod.A == loanFeeDef.stampCalcMethod){
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",印花税固定金额为空");
			}
			acct.setStampAmt(loanFeeDef.stampAMT);
		}	
		
		return acct;
	
}
	
	/*
	 * 赋值查询返回信息中的还款计划信息
	 */
	public void setSubScheduleList(STNTLLoanScheduleCalcResp resp,List<CcsRepaySchedule> scheduleList){
		for(CcsRepaySchedule schedule : scheduleList){

			STNTLLoanScheduleCalcRespSubSchedule subSchedule = new STNTLLoanScheduleCalcRespSubSchedule();
			subSchedule.setLoanCurrTerm(schedule.getCurrTerm()); //当前期数

			BigDecimal tmpTotAmt = BigDecimal.ZERO;
			tmpTotAmt = tmpTotAmt.add(schedule.getLoanTermPrin()==null?BigDecimal.ZERO:schedule.getLoanTermPrin()).
					add(schedule.getLoanTermFee() == null ? BigDecimal.ZERO : schedule.getLoanTermFee()).
					add(schedule.getLoanTermInt() == null ? BigDecimal.ZERO : schedule.getLoanTermInt()).
					add(schedule.getLoanStampdutyAmt() == null ? BigDecimal.ZERO : schedule.getLoanStampdutyAmt()).
					add(schedule.getLoanLifeInsuAmt() == null ? BigDecimal.ZERO : schedule.getLoanLifeInsuAmt()).
					add(schedule.getLoanSvcFee() == null ? BigDecimal.ZERO : schedule.getLoanSvcFee()).
					add(schedule.getLoanReplaceSvcFee() == null ? BigDecimal.ZERO : schedule.getLoanReplaceSvcFee()).
					add(schedule.getLoanInsuranceAmt() == null ? BigDecimal.ZERO : schedule.getLoanInsuranceAmt()).
					add(schedule.getLoanPrepayPkgAmt() == null ? BigDecimal.ZERO : schedule.getLoanPrepayPkgAmt());
			subSchedule.setLoanTermTotAmt(tmpTotAmt);
			
			subSchedule.setLoanTermPrin(schedule.getLoanTermPrin()); //应还本金
			//应还服务费
			subSchedule.setLoanTermSvcFee(schedule.getLoanTermFee()==null?BigDecimal.ZERO:schedule.getLoanTermFee());
			//应还手续费
			subSchedule.setLoanTermInstallFee(schedule.getLoanSvcFee()==null?BigDecimal.ZERO:schedule.getLoanSvcFee());
			//应还提前还款计划包费
			subSchedule.setLoanPrepayPkgFee(schedule.getLoanPrepayPkgAmt());
			//应还保费
			subSchedule.setLoanInsuranceFee(schedule.getLoanInsuranceAmt());
			//应还代收服务费
			subSchedule.setLoanAgentFee(schedule.getLoanReplaceSvcFee());
			subSchedule.setLoanTermInt(schedule.getLoanTermInt()); //应还利息
			loanIntSum = loanIntSum.add(schedule.getLoanTermInt());
			subSchedule.setLoanStampDutyAmt(schedule.getLoanStampdutyAmt()); //应还印花税
			subSchedule.setLoanLifeInsuFee(schedule.getLoanLifeInsuAmt()); //应还寿险费
			subSchedule.setLoanPmtDueDate(DateFormatUtil.format(schedule.getLoanPmtDueDate(), respDateFormat)); //到期还款日期		
			
			resp.getSubScheduleList().add(subSchedule);
		}
	}	
}
