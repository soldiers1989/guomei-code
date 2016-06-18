package com.sunline.ccs.service.handler.query;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.param.def.EarlyRepayDef;
import com.sunline.ccs.param.def.LatePaymentCharge;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.RateDef;
import com.sunline.ccs.param.def.ReplaceEarlyRepayDef;
import com.sunline.ccs.param.def.TxnFee;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msdentity.STNQPLSPbyAmtTermReq;
import com.sunline.ccs.service.msdentity.STNQPLSPbyAmtTermResp;
import com.sunline.ccs.service.msdentity.STNQPLSPbyAmtTermRespCooperMulctRule;
import com.sunline.ccs.service.msdentity.STNQPLSPbyAmtTermRespCooperSubPrepayRule;
import com.sunline.ccs.service.msdentity.STNQPLSPbyAmtTermRespSubMulctRule;
import com.sunline.ccs.service.msdentity.STNQPLSPbyAmtTermRespSubPrepayRule;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.util.DateFormatUtil;
import com.sunline.pcm.param.def.GroupCtrl;
import com.sunline.pcm.param.def.Mulct;
import com.sunline.pcm.param.def.MulctDef;
import com.sunline.pcm.param.def.Terminal;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/*
 * 根据金额、期数获取产品信息接口
 */
@Service
public class TNQPLSPbyAmtTerm {
	private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    AppapiCommService appapiCommService;

	@Autowired
	private UnifiedParameterFacility unifiedParameterService;

	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired 
	private UnifiedParameterFacility unifiedParameterFacility;
	@Value("#{env.respDateFormat}")
	private String respDateFormat;

	
	public STNQPLSPbyAmtTermResp handler(STNQPLSPbyAmtTermReq req) throws ProcessException {
		LogTools.printLogger(log, "STNQPLSPbyAmtTermReq", "根据金额、期数获取产品信息接口", req, true);
		LogTools.printObj(log, req, "请求参数STNQPLSPbyAmtTermReq");
		STNQPLSPbyAmtTermResp resp = new STNQPLSPbyAmtTermResp();
		TxnInfo txnInfo = new TxnInfo();
		try{
			ProductCredit productCredit;
			LoanPlan loanPlan;
			LoanFeeDef loanFeeDef;
			Mulct mulct;
			GroupCtrl groupCtrl;
			
			try{
				loanPlan = unifiedParamFacilityProvide.loanPlan(req.getLoanCode());
				productCredit = unifiedParameterFacility.loadParameter(loanPlan.productCode,ProductCredit.class );
				if(req.getLoanFeeDefId()==null){
					loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(req.getLoanCode(), req.getLoanTerm(), req.getLoanAmt());}
				else{
					loanFeeDef=unifiedParamFacilityProvide.loanFeeDefByKey(loanPlan.loanCode,Integer.valueOf(req.getLoanFeeDefId()));
				}
			
				mulct = unifiedParameterService.loadParameter(loanFeeDef.mulctTableId, Mulct.class);
				
				
				groupCtrl = unifiedParameterService.loadParameter(loanPlan.groupId,GroupCtrl.class);
				
				if(loanPlan == null|| loanFeeDef == null
						|| loanPlan.loanValidity.before(req.getBizDate())
						|| loanPlan.loanStaus != LoanPlanStatus.A
						|| LoanFeeDefStatus.A != loanFeeDef.loanFeeDefStatus){
					throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());				
				}
			}
			catch(Exception e){
				throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());				
			}
			
			List<MulctDef> mulctdefList = mulct.mulctDefs;
			
			List<ReplaceEarlyRepayDef> replaceEarlyRepayDefList = loanFeeDef.replaceEarlyRepayDef;
			List<EarlyRepayDef> earlyRepayDefList = loanFeeDef.earlyRepayDefs;
			
			setRespInfoByLoanPlan(resp, loanPlan);
			setRespInfoByLoanFeeDef(resp, req,productCredit,loanPlan,loanFeeDef);
			setRespInfoEarlyRepayDefList(resp, earlyRepayDefList);
			setRespReplaceEarlyRepayDefList(resp, replaceEarlyRepayDefList,loanFeeDef);
			setRespInfoMulctRuleList(resp, mulct, mulctdefList);
			setRespCooperMulctRuleList(resp, loanFeeDef);
			setRespInfoGroupCtrlList(resp,groupCtrl);//设置终端列表
			//提现手续费率
			setExtractFeeInfo(resp, productCredit, req.getLoanAmt());
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
			LogTools.printLogger(log, "TNQPLSPbyAmtTerm", "根据金额、期数获取产品信息接口", resp, false);
		}
		
		setResponse(resp, txnInfo);

		return resp;
	}

	private void setRespInfoGroupCtrlList(STNQPLSPbyAmtTermResp resp, GroupCtrl groupCtrl) {
		List<String> subTerminalList = new ArrayList<String>();
		for(Terminal terminal:groupCtrl.subTermialList){
			subTerminalList.add(terminal.terminalId);
		}
		resp.setSubTerminalList(subTerminalList);
	}

	public void setRespInfoByLoanPlan(STNQPLSPbyAmtTermResp resp,LoanPlan loanPlan){
		
		resp.setLoanCode(loanPlan.loanCode); //贷款产品代码
		resp.setDesc(loanPlan.description); //贷款产品描述
		resp.setLoanType(loanPlan.loanType); //贷款类型
		resp.setTerminateAgeCd(loanPlan.terminateAgeCd); //中止账龄
		resp.setLoanValidity(DateFormatUtil.format(loanPlan.loanValidity, respDateFormat)); //产品有效期
		resp.setLoanStatus(loanPlan.loanStaus ); //贷款产品状态
		resp.setLoanMold(loanPlan.loanMold); //放款类型
		resp.setMinCycle(loanPlan.minCycle); //贷款最短周期
		resp.setMaxCycle(loanPlan.maxCycle); //贷款最长周期
	}
	public void setRespInfoByLoanFeeDef(STNQPLSPbyAmtTermResp resp,STNQPLSPbyAmtTermReq req,
		ProductCredit productCredit,	LoanPlan loanPlan, LoanFeeDef loanFeeDef){
		MathContext mc = new MathContext(8,RoundingMode.HALF_UP); //精度 add 20151127

		resp.setLoanFeeDefId(loanFeeDef.loanFeeDefId.toString());//贷款子产品编号 add 20151127
		resp.setHesitationDays(loanFeeDef.hesitationDays); //犹豫期
		resp.setLoanTerm(loanFeeDef.initTerm); //贷款期数
		resp.setInterestRate(loanFeeDef.interestRate); //基准利率
		resp.setPenaltyIntRate(loanFeeDef.penaltyIntTableId); //罚息利率
		resp.setCompoundIntRate(loanFeeDef.compoundIntTableId); //复利利率
		resp.setPaymentUnit(loanFeeDef.paymentIntervalUnit); //还款间隔单位
		resp.setPaymentPeriod(loanFeeDef.paymentIntervalPeriod); //还款间隔周期
		resp.setSvcFeeChargeMtd(loanFeeDef.loanFeeMethod ); //贷款服务费收取方式
		resp.setSvcFeeCalMtd(loanFeeDef.loanFeeCalcMethod); //贷款服务费计算方式
		resp.setSvcFeeAmt(loanFeeDef.feeAmount==null?null:loanFeeDef.feeAmount); //贷款服务费金额
		resp.setSvcFeeCalRate(loanFeeDef.feeRate==null?null:loanFeeDef.feeRate.divide(//modify 20151127
				BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //贷款服务费费率
		resp.setInstlmtFeeChargeMtd(loanFeeDef.installmentFeeMethod==null?null:loanFeeDef.installmentFeeMethod); //分期手续费收取方式
		resp.setInstlmtFeeCalMtd(loanFeeDef.installmentFeeCalMethod); //分期手续费计算方式
		resp.setInstlmtFeeAmt(loanFeeDef.installmentFeeAmt==null?null:loanFeeDef.installmentFeeAmt); //分期手续费金额
		resp.setInstlmtFeeRate(loanFeeDef.installmentFeeRate==null?null:loanFeeDef.installmentFeeRate.divide(//modify 20151127
				BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //分期手续费费率
		
		resp.setAnnualFeeChargeMtd(productCredit.fee.firstCardFeeInd);    //首次年费收取方式 
		resp.setAnnualFeeAmt(productCredit.fee.primCardFee==null?null:productCredit.fee.primCardFee); //年费金额
		
		resp.setRescheduleInd(loanFeeDef.rescheduleInd); //是否允许展期标志
		resp.setPrepayCalMtd(loanFeeDef.prepaymentFeeMethod); //提前还款手续费计算方式
		resp.setEarlySettleInd(loanFeeDef.appointEarlySettleEnable); //是否支持预约提前结清
		resp.setEarlySettleAppDays(loanFeeDef.appointEarlySettleDate); //预约提前结清提前天数
		resp.setShortSchedInd(loanFeeDef.shortedRescInd); //是否允许缩期标志
		
		//从产品productcredit参数中取
		LatePaymentCharge latePaymentCharge = productCredit.latePaymentCharge;
		if(latePaymentCharge.assessInd==true){
			resp.setDuePenaltyInd(Indicator.Y); //违约金收取标志
		}
		else{
			resp.setDuePenaltyInd(Indicator.N);
		}
		resp.setDuePenaltyMinAmt(latePaymentCharge.minCharge); //违约金单笔最小金额
		resp.setDuePenaltyMaxAmt(latePaymentCharge.maxCharge); //违约金单笔最大金额
		resp.setDuePenaltyYearMaxAmt(latePaymentCharge.yearMaxCharge); //违约金年累计最大金额
		resp.setDuePenaltyYearMaxCnt(latePaymentCharge.yearMaxCnt); //违约金年累计最大次数
		resp.setDuePenaltyBase(latePaymentCharge.calcBaseInd); //违约金计算基准金额
		resp.setDuePenaltyChargeMtd(latePaymentCharge.tierInd); //违约金计算方式
	
		for(RateDef ratedef :latePaymentCharge.chargeRates){
			BigDecimal currAmtRange = BigDecimal.ZERO;
			
			if((req.loanAmt==null?BigDecimal.ZERO:req.loanAmt).compareTo(ratedef.rateCeil)  <= 0 && currAmtRange.compareTo(ratedef.rateCeil) <= 0 ){
				currAmtRange=ratedef.rateCeil;
				resp.setDuePenaltyAddiAmt(ratedef.rateBase); //违约金固定附加金额
				resp.setDuePenaltyRate(ratedef.rate); //违约金对应手续费费率
			}
		}
		//从产品productcredit参数中取合作方代收违约金
		LatePaymentCharge replaceLatePaymentCharge = productCredit.replaceLatePaymentCharge;
		if(null != replaceLatePaymentCharge){
			if(replaceLatePaymentCharge.assessInd==true){
				resp.setCooperDuePenaltyInd(Indicator.Y); //合作方代收违约金收取标志
			}
			else{
				resp.setCooperDuePenaltyInd(Indicator.N);
			}
			resp.setCooperDuePenaltyMinAmt(replaceLatePaymentCharge.minCharge); //合作方代收违约金单笔最小金额
			resp.setCooperDuePenaltyMaxAmt(replaceLatePaymentCharge.maxCharge); //合作方代收违约金单笔最大金额
			resp.setCooperDuePenaltyYearMaxAmt(replaceLatePaymentCharge.yearMaxCharge); //合作方代收违约金年累计最大金额
			resp.setCooperDuePenaltyYearMaxCnt(replaceLatePaymentCharge.yearMaxCnt); //合作方代收违约金年累计最大次数
			resp.setCooperDuePenaltyBase(replaceLatePaymentCharge.calcBaseInd); //合作方代收违约金计算基准金额
			resp.setCooperDuePenaltyChargeMtd(replaceLatePaymentCharge.tierInd); //合作方代收违约金计算方式
			for(RateDef ratedef :replaceLatePaymentCharge.chargeRates){
				BigDecimal currAmtRange = BigDecimal.ZERO;
				
				if((req.loanAmt==null?BigDecimal.ZERO:req.loanAmt).compareTo(ratedef.rateCeil)  <= 0 && currAmtRange.compareTo(ratedef.rateCeil) <= 0 ){
					currAmtRange=ratedef.rateCeil;
					resp.setCooperDuePenaltyAddiAmt(ratedef.rateBase); //合作方代收违约金固定附加金额
					resp.setCooperDuePenaltyRate(ratedef.rate); //合作方代收违约金对应手续费费率
				}
				
			}
		}
		resp.setPremiumReturn(loanFeeDef.premiumReturnInd);
		resp.setLifeinsuFeeChargeMtd(loanFeeDef.lifeInsuFeeMethod); //寿险包收取方式
		resp.setLifeinsuFeeCalMtd(loanFeeDef.lifeInsuFeeCalMethod); //寿险包计算方式
		resp.setLifeinsuFeeRate(loanFeeDef.lifeInsuFeeRate==null?null:loanFeeDef.lifeInsuFeeRate.divide(//modify 20151127
				BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //寿险包费率
		resp.setLifeinsuFeeAmt(loanFeeDef.lifeInsuFeeAmt); //寿险包固定金额
		
		//20160525 libo 新增灵活还款相关字段
		resp.setPrepayPkgChargeMtd(loanFeeDef.prepayPkgFeeMethod);
		resp.setPrepayPkgCalMtd(loanFeeDef.prepayPkgFeeCalMethod);
		resp.setPrepayPkgRate(loanFeeDef.prepayPkgFeeAmountRate==null?null:loanFeeDef.prepayPkgFeeAmountRate.divide(//modify 20151127
				BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //寿险包费率);
		resp.setPrepayPkgAmt(loanFeeDef.prepayPkgFeeAmount);
		
//代收服务费
		resp.setAgentFeeCalMtd(loanFeeDef.replaceFeeCalMethod);//待收服务费计算方式
		resp.setAgentFeeChargeMtd(loanFeeDef.replaceFeeMethod);//代收服务费收取方式
		resp.setMonthlyAgentFeeRate(loanFeeDef.replaceFeeRate==null?null:loanFeeDef.replaceFeeRate.divide(//modify 20151127
				BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //代收服务费收取比例
		resp.setMonthlyAgentFeeAmt(loanFeeDef.replaceFeeAmt);//待收服务费固定金额
//
		resp.setStampChargeMtd(loanFeeDef.stampMethod); //印花税收取方式
		resp.setStampCalMtd(loanFeeDef.stampCalcMethod); //印花税计算方式
		resp.setStampRate(loanFeeDef.stampRate==null?null:loanFeeDef.stampRate.divide(//modify 20151127
				BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //印花税率
		resp.setStampAmt(loanFeeDef.stampAMT==null?null:loanFeeDef.stampAMT); //印花税固定金额
		resp.setStampOffsetIntInd(loanFeeDef.isOffsetRate); //印花税是否冲减
		resp.setStampCustChargeInd(loanFeeDef.stampCustomInd); //印花税是否入客户帐
		resp.setWithDrawLowlimit(productCredit.withdrawLowlimit);//提现最小金额
		resp.setRepayLowlimit(productCredit.repayLowlimit);//还款最小金额
		resp.setContractTemplateId(loanPlan.contractTemplateId);//合同模板编号
		resp.setCooperPenaltyIntRate(loanFeeDef.penaltyIntTableId);
	}
	/*
	 * 罚金相关 罚金列表
	 */
	public void setRespInfoMulctRuleList(STNQPLSPbyAmtTermResp resp,Mulct mulct,List<MulctDef> mulctdefList){
		
		if(mulct == null)
			return ;
		
		resp.setMulctMethod(mulct.mulctMethod); //罚金收取方式
		resp.setMulctBaseYear(mulct.baseYear); //罚金计息基准年
		
		for(MulctDef mulctDef: mulctdefList){
			STNQPLSPbyAmtTermRespSubMulctRule subMulctDef = new STNQPLSPbyAmtTermRespSubMulctRule();
			subMulctDef.setDayPastDue(mulctDef.mulctOverDays); //逾期天数
			subMulctDef.setColPastDue(mulctDef.cpdOverDays); //进入催收天数
			subMulctDef.setMulctAmt(mulctDef.mulctOverAmt); //罚金金额
			subMulctDef.setMulctRate(mulctDef.mulctOverRate); //罚金费率
			resp.getMulctRulesList().add(subMulctDef);
		}
	}
	/*
	 * 合作方代收罚息列表
	 * Mr.L
	 */
	public void setRespCooperMulctRuleList(STNQPLSPbyAmtTermResp resp,LoanFeeDef loanFeeDef){
		Mulct mulcts = null;
		if (null == loanFeeDef.replaceMulctTableId) {
			return;
		}
		mulcts = unifiedParameterService.loadParameter(loanFeeDef.replaceMulctTableId, Mulct.class);
		List<MulctDef> mulctdefList = mulcts.mulctDefs;
		for(MulctDef mulctDef: mulctdefList){
			STNQPLSPbyAmtTermRespCooperMulctRule repayMulctDef = new STNQPLSPbyAmtTermRespCooperMulctRule();
			repayMulctDef.setCooperDayPastDue(mulctDef.mulctOverDays); //合作方逾期天数
			repayMulctDef.setCooperColPastDue(mulctDef.cpdOverDays); //合作方进入催收天数
			repayMulctDef.setCooperMulctAmt(mulctDef.mulctOverAmt); //合作方罚金金额
			repayMulctDef.setCooperMulctRate(mulctDef.mulctOverRate); //合作方罚金费率
			resp.getCooperMulctRulesList().add(repayMulctDef);
		}
	}
	/*
	 * 提前还款规则列表
	 */
	public void setRespInfoEarlyRepayDefList(STNQPLSPbyAmtTermResp resp,List<EarlyRepayDef> earlyRepayDefList){
		for(EarlyRepayDef earlyRepayDef:earlyRepayDefList ){
			STNQPLSPbyAmtTermRespSubPrepayRule subPrepayRule = new STNQPLSPbyAmtTermRespSubPrepayRule();
			
			subPrepayRule.setPrepayCurrTerm(earlyRepayDef.adCurPeriod); //提前还款当前期数
			subPrepayRule.setPrepayFeeAmt(earlyRepayDef.adFeeAmt); //提前还款手续费金额
			subPrepayRule.setPrepayFeeRate(earlyRepayDef.adFeeScale); //提前还款手续费费率
		
			resp.getPrepayRulesList().add(subPrepayRule);
		}
	}
	/*
	 * 合作方提前还款规则列表
	 * Mr.L
	 */
	public void setRespReplaceEarlyRepayDefList(STNQPLSPbyAmtTermResp resp,List<ReplaceEarlyRepayDef> replaceEarlyRepayDefList,LoanFeeDef loanFeeDef){
		if( null == replaceEarlyRepayDefList)
			return ;
		for(ReplaceEarlyRepayDef earlyRepayDef:replaceEarlyRepayDefList ){
			STNQPLSPbyAmtTermRespCooperSubPrepayRule subPrepayRule = new STNQPLSPbyAmtTermRespCooperSubPrepayRule();
			subPrepayRule.setCooperPrepayCurrTerm(earlyRepayDef.replaceAdCurPeriod); //提前还款当前期数
			subPrepayRule.setCooperPrepayFeeAmt(earlyRepayDef.replaceAdFeeAmt); //提前还款手续费金额
			subPrepayRule.setCooperPrepayFeeRate(earlyRepayDef.replaceAdFeeScale); //提前还款手续费费率
			resp.getCooperPrepayRulesList().add(subPrepayRule);
		}
	}
	
	private void setExtractFeeInfo(STNQPLSPbyAmtTermResp resp,ProductCredit productCredit,BigDecimal amt){
		resp.setExtractFeeRate(BigDecimal.ZERO); //提现手续费费率
		resp.setExtractFeeAddiAmt(BigDecimal.ZERO); //提现手续费固定附加

		Map<String, List<TxnFee>>  txnFeeList = productCredit.txnFeeList;
		for(String txncd:txnFeeList.keySet()){
			for(TxnFee txnFee: txnFeeList.get(txncd)){
				//暂时写死G217交易码
				if (txnFee.feeTxnCd.endsWith("G217")){
					//取得对应金额区间 
					BigDecimal tmpRateCeil = BigDecimal.ZERO;
					for(RateDef ratedef: txnFee.chargeRates){
						if ((amt==null?BigDecimal.ZERO:amt).compareTo(ratedef.rateCeil) < 0 
						 && (amt==null?BigDecimal.ZERO:amt).compareTo(tmpRateCeil) >= 0){
							tmpRateCeil = ratedef.rateCeil;
							resp.setExtractFeeRate(ratedef.rate); //提现手续费费率
							resp.setExtractFeeAddiAmt(ratedef.rateBase); //提现手续费固定附加
							//年费收取方式返回没有该字段与年费
						}
					}
				}
			}
		}
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

