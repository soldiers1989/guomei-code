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
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefDetailReq;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefDetailResp;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefSubLoanFeeDefList;
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
@Service
public class TNQPLPAllLoanFeeDefDetail {
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

	public STNQPLPAllLoanFeeDefDetailResp handler(STNQPLPAllLoanFeeDefDetailReq req) throws ProcessException{
		LogTools.printLogger(log, "STNQPLPAllLoanFeeDefDetailReq", "贷款子产品详情列表查询接口", req, true);
		LogTools.printObj(log, req, "请求参数STNQPLPAllLoanFeeDefDetailReq");
		STNQPLPAllLoanFeeDefDetailResp resp = new STNQPLPAllLoanFeeDefDetailResp();
		List<STNQPLPAllLoanFeeDefSubLoanFeeDefList> loanFeeDefList = new ArrayList<STNQPLPAllLoanFeeDefSubLoanFeeDefList>();
		ProductCredit productCredit;
		TxnInfo txnInfo = new TxnInfo();
		try{
			LoanPlan loanPlan;
			GroupCtrl groupCtrl;
			try {
				loanPlan = unifiedParamFacilityProvide.loanPlan(req.getLoanCode());
				productCredit = unifiedParameterFacility.loadParameter(loanPlan.productCode,ProductCredit.class );
				
				if(loanPlan == null|| loanPlan.loanValidity.before(req.getBizDate())
						|| loanPlan.loanStaus != LoanPlanStatus.A ){
					throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());				
				}
				
			} catch (Exception e) {
				throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());
			}
				groupCtrl = unifiedParameterService.loadParameter(loanPlan.groupId,GroupCtrl.class);		
				loanFeeDefList=setloanfeedef(loanFeeDefList,loanPlan, txnInfo, groupCtrl, productCredit);
				resp.setLoanFeeDefList(loanFeeDefList);
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
			LogTools.printLogger(log, "STNQPLPAllLoanFeeDefDetail", "贷款子产品详情列表查询接口", resp, false);
		}
		setResponse(resp, txnInfo);
		return resp;
	}
	
	/**
	 * loanfeedef层
	 * @param loanFeeDefMap
	 * @param loanPlan
	 */
	public List<STNQPLPAllLoanFeeDefSubLoanFeeDefList> setloanfeedef(List<STNQPLPAllLoanFeeDefSubLoanFeeDefList> loanFeeDefList,LoanPlan loanPlan,TxnInfo txnInfo,GroupCtrl groupCtrl,ProductCredit productCredit){
		Map<Integer, LoanFeeDef> loanFeeDefMap;
		loanFeeDefMap = loanPlan.loanFeeDefMap;
		for (Integer term1 : loanFeeDefMap.keySet()) {
			MathContext mc = new MathContext(8,RoundingMode.HALF_UP); //精度 add 20151127
			STNQPLPAllLoanFeeDefSubLoanFeeDefList subLoanFeeDef = new STNQPLPAllLoanFeeDefSubLoanFeeDefList();
			LoanFeeDef loanFeeDef=loanFeeDefMap.get(term1);
			List<EarlyRepayDef> earlyRepayDefLists = loanFeeDef.earlyRepayDefs;
			List<ReplaceEarlyRepayDef> replaceEarlyRepayDefList = loanFeeDef.replaceEarlyRepayDef;
			if(loanFeeDef == null || LoanFeeDefStatus.A != loanFeeDef.loanFeeDefStatus){
				continue;
			}
			Mulct mulct = unifiedParameterService.loadParameter(loanFeeDef.mulctTableId, Mulct.class);
			subLoanFeeDef.setLoanCode(loanPlan.loanCode); //贷款产品代码
			subLoanFeeDef.setLoanFeeDefId(loanFeeDef.loanFeeDefId.toString());
			subLoanFeeDef.setDesc(loanPlan.description); //贷款产品描述
			subLoanFeeDef.setLoanType(loanPlan.loanType); //贷款类型
			subLoanFeeDef.setTerminateAgeCd(loanPlan.terminateAgeCd); //中止账龄
			subLoanFeeDef.setLoanValidity(DateFormatUtil.format(loanPlan.loanValidity, respDateFormat)); //产品有效期
			subLoanFeeDef.setLoanStatus(loanPlan.loanStaus ); //贷款产品状态
			subLoanFeeDef.setLoanMold(loanPlan.loanMold); //放款类型
			subLoanFeeDef.setMinCycle(loanPlan.minCycle); //贷款最短周期
			subLoanFeeDef.setMaxCycle(loanPlan.maxCycle); //贷款最长周期
			subLoanFeeDef.setHesitationDays(loanFeeDef.hesitationDays); //犹豫期
			subLoanFeeDef.setLoanTerm(loanFeeDef.initTerm); //贷款期数
			subLoanFeeDef.setInterestRate(loanFeeDef.interestRate); //基准利率
			subLoanFeeDef.setPenaltyIntRate(loanFeeDef.penaltyIntTableId); //罚息利率
			subLoanFeeDef.setCompoundIntRate(loanFeeDef.compoundIntTableId); //复利利率
			subLoanFeeDef.setPaymentUnit(loanFeeDef.paymentIntervalUnit); //还款间隔单位
			subLoanFeeDef.setPaymentPeriod(loanFeeDef.paymentIntervalPeriod); //还款间隔周期
			subLoanFeeDef.setMulctMethod(mulct.mulctMethod); //罚金收取方式
			subLoanFeeDef.setMulctBaseYear(mulct.baseYear); //罚金计息基准年
			subLoanFeeDef.setSvcFeeChargeMtd(loanFeeDef.loanFeeMethod ); //贷款服务费收取方式
			subLoanFeeDef.setSvcFeeCalMtd(loanFeeDef.loanFeeCalcMethod); //贷款服务费计算方式
			subLoanFeeDef.setSvcFeeAmt(loanFeeDef.feeAmount==null?null:loanFeeDef.feeAmount); //贷款服务费金额
			subLoanFeeDef.setSvcFeeCalRate(loanFeeDef.feeRate==null?null:loanFeeDef.feeRate.divide(//modify 20151127
					BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //贷款服务费费率
			subLoanFeeDef.setInstlmtFeeChargeMtd(loanFeeDef.installmentFeeMethod==null?null:loanFeeDef.installmentFeeMethod); //分期手续费收取方式
			subLoanFeeDef.setInstlmtFeeCalMtd(loanFeeDef.installmentFeeCalMethod); //分期手续费计算方式
			subLoanFeeDef.setInstlmtFeeAmt(loanFeeDef.installmentFeeAmt==null?null:loanFeeDef.installmentFeeAmt); //分期手续费金额
			subLoanFeeDef.setInstlmtFeeRate(loanFeeDef.installmentFeeRate==null?null:loanFeeDef.installmentFeeRate.divide(//modify 20151127
					BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //分期手续费费率
			subLoanFeeDef.setAnnualFeeChargeMtd(productCredit.fee.firstCardFeeInd);    //首次年费收取方式 
			subLoanFeeDef.setAnnualFeeAmt(productCredit.fee.primCardFee==null?null:productCredit.fee.primCardFee); //年费金额
			subLoanFeeDef.setRescheduleInd(loanFeeDef.rescheduleInd); //是否允许展期标志
			subLoanFeeDef.setPrepayCalMtd(loanFeeDef.prepaymentFeeMethod); //提前还款手续费计算方式
			subLoanFeeDef.setEarlySettleInd(loanFeeDef.appointEarlySettleEnable); //是否支持预约提前结清
			subLoanFeeDef.setEarlySettleAppDays(loanFeeDef.appointEarlySettleDate); //预约提前结清提前天数
			subLoanFeeDef.setShortSchedInd(loanFeeDef.shortedRescInd); //是否允许缩期标志
			//从产品productcredit参数中取
			LatePaymentCharge latePaymentCharge = productCredit.latePaymentCharge;
			if(latePaymentCharge.assessInd==true){
				subLoanFeeDef.setDuePenaltyInd(Indicator.Y); //违约金收取标志
			}
			else{
				subLoanFeeDef.setDuePenaltyInd(Indicator.N);
			}
			subLoanFeeDef.setDuePenaltyMinAmt(latePaymentCharge.minCharge); //违约金单笔最小金额
			subLoanFeeDef.setDuePenaltyMaxAmt(latePaymentCharge.maxCharge); //违约金单笔最大金额
			subLoanFeeDef.setDuePenaltyYearMaxAmt(latePaymentCharge.yearMaxCharge); //违约金年累计最大金额
			subLoanFeeDef.setDuePenaltyYearMaxCnt(latePaymentCharge.yearMaxCnt); //违约金年累计最大次数
			subLoanFeeDef.setDuePenaltyBase(latePaymentCharge.calcBaseInd); //违约金计算基准金额
			subLoanFeeDef.setDuePenaltyChargeMtd(latePaymentCharge.tierInd); //违约金计算方式
		
//			subLoanFeeDef.setDuePenaltyAddiAmt(BigDecimal.ZERO); //违约金固定附加金额
//			subLoanFeeDef.setDuePenaltyRate(BigDecimal.ZERO); //违约金对应手续费费率
			for(RateDef ratedef :latePaymentCharge.chargeRates){
				BigDecimal currAmtRange = BigDecimal.ZERO;
				
				if((BigDecimal.ZERO).compareTo(ratedef.rateCeil)  <= 0 && currAmtRange.compareTo(ratedef.rateCeil) <= 0 ){
					currAmtRange=ratedef.rateCeil;
					subLoanFeeDef.setDuePenaltyAddiAmt(ratedef.rateBase); //违约金固定附加金额
					subLoanFeeDef.setDuePenaltyRate(ratedef.rate); //违约金对应手续费费率
				}
			}
			subLoanFeeDef.setMaxAmounts(loanFeeDef.maxAmount);
			subLoanFeeDef.setMinAmounts(loanFeeDef.minAmount);
			LatePaymentCharge replaceLatePaymentCharge = productCredit.replaceLatePaymentCharge;
			if (null != replaceLatePaymentCharge) {
				if(replaceLatePaymentCharge.assessInd==true){
					subLoanFeeDef.setCooperDuePenaltyInd(Indicator.Y); //合作方代收违约金收取标志
				}
				else{
					subLoanFeeDef.setCooperDuePenaltyInd(Indicator.N);
				}
				subLoanFeeDef.setCooperDuePenaltyMinAmt(replaceLatePaymentCharge.minCharge); //合作方代收违约金单笔最小金额
				subLoanFeeDef.setCooperDuePenaltyMaxAmt(replaceLatePaymentCharge.maxCharge); //合作方代收违约金单笔最大金额
				subLoanFeeDef.setCooperDuePenaltyYearMaxAmt(replaceLatePaymentCharge.yearMaxCharge); //合作方代收违约金年累计最大金额
				subLoanFeeDef.setCooperDuePenaltyYearMaxCnt(replaceLatePaymentCharge.yearMaxCnt); //合作方代收违约金年累计最大次数
				subLoanFeeDef.setCooperDuePenaltyBase(replaceLatePaymentCharge.calcBaseInd); //合作方代收违约金计算基准金额
				subLoanFeeDef.setCooperDuePenaltyChargeMtd(replaceLatePaymentCharge.tierInd); //合作方代收违约金计算方式
				for(RateDef ratedef :replaceLatePaymentCharge.chargeRates){
					BigDecimal currAmtRange = BigDecimal.ZERO;
					
					if((BigDecimal.ZERO).compareTo(ratedef.rateCeil)  <= 0 && currAmtRange.compareTo(ratedef.rateCeil) <= 0 ){
						currAmtRange=ratedef.rateCeil;
						subLoanFeeDef.setCooperDuePenaltyAddiAmt(ratedef.rateBase); //合作方代收违约金固定附加金额
						subLoanFeeDef.setCooperDuePenaltyRate(ratedef.rate); //合作方代收违约金对应手续费费率
					}
				}
			}
			subLoanFeeDef.setPremiumReturn(loanFeeDef.premiumReturnInd);
			subLoanFeeDef.setLifeinsuFeeChargeMtd(loanFeeDef.lifeInsuFeeMethod); //寿险包收取方式
			subLoanFeeDef.setLifeinsuFeeCalMtd(loanFeeDef.lifeInsuFeeCalMethod); //寿险包计算方式
			subLoanFeeDef.setLifeinsuFeeRate(loanFeeDef.lifeInsuFeeRate==null?null:loanFeeDef.lifeInsuFeeRate.divide(//modify 20151127
					BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //寿险包费率
			subLoanFeeDef.setLifeinsuFeeAmt(loanFeeDef.lifeInsuFeeAmt); //寿险包固定金额
			//20160525  libo 
			//20160525 libo 新增灵活还款相关字段
			subLoanFeeDef.setPrepayPkgChargeMtd(loanFeeDef.prepayPkgFeeMethod);
			subLoanFeeDef.setPrepayPkgCalMtd(loanFeeDef.prepayPkgFeeCalMethod);
			subLoanFeeDef.setPrepayPkgRate(loanFeeDef.prepayPkgFeeAmountRate==null?null:loanFeeDef.prepayPkgFeeAmountRate.divide(//modify 20151127
					BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); 
			subLoanFeeDef.setPrepayPkgAmt(loanFeeDef.prepayPkgFeeAmount);
	//代收服务费		
			subLoanFeeDef.setAgentFeeCalMtd(loanFeeDef.replaceFeeCalMethod);//待收服务费计算方式
			subLoanFeeDef.setAgentFeeChargeMtd(loanFeeDef.replaceFeeMethod);//代收服务费收取方式
			subLoanFeeDef.setMonthlyAgentFeeRate(loanFeeDef.replaceFeeRate==null?null:loanFeeDef.replaceFeeRate.divide(//modify 20151127
					BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //代收服务费收取比例
			subLoanFeeDef.setMonthlyAgentFeeAmt(loanFeeDef.replaceFeeAmt);//待收服务费固定金额
			subLoanFeeDef.setStampChargeMtd(loanFeeDef.stampMethod); //印花税收取方式
			subLoanFeeDef.setStampCalMtd(loanFeeDef.stampCalcMethod); //印花税计算方式
			subLoanFeeDef.setStampRate(loanFeeDef.stampRate==null?null:loanFeeDef.stampRate.divide(//modify 20151127
					BigDecimal.valueOf(loanFeeDef.initTerm), mc).setScale(8, RoundingMode.HALF_UP)); //印花税率
			subLoanFeeDef.setStampAmt(loanFeeDef.stampAMT==null?null:loanFeeDef.stampAMT); //印花税固定金额
			subLoanFeeDef.setStampOffsetIntInd(loanFeeDef.isOffsetRate); //印花税是否冲减
			subLoanFeeDef.setStampCustChargeInd(loanFeeDef.stampCustomInd); //印花税是否入客户帐
			subLoanFeeDef.setWithDrawLowlimit(productCredit.withdrawLowlimit);//提现最小金额
			subLoanFeeDef.setRepayLowlimit(productCredit.repayLowlimit);//还款最小金额
			subLoanFeeDef.setCooperPenaltyIntRate(loanFeeDef.penaltyIntTableId);
			setRespInfoGroupCtrlList(subLoanFeeDef, groupCtrl);
			setInfoMulctRuleList(subLoanFeeDef, mulct);
			setRespCooperMulctRuleList(subLoanFeeDef, loanFeeDef);
			//提前还款手续费计算规则列表
			setRespInfoEarlyRepayDefList(subLoanFeeDef, earlyRepayDefLists);
			setRespReplaceEarlyRepayDefList(subLoanFeeDef, replaceEarlyRepayDefList, loanFeeDef);
			setExtractFeeInfo(subLoanFeeDef, productCredit, null);
			loanFeeDefList.add(subLoanFeeDef);
		}	
		return loanFeeDefList;
	}
	/*
	 * 罚金相关 罚金列表
	 */
	public void setInfoMulctRuleList(STNQPLPAllLoanFeeDefSubLoanFeeDefList subLoanFeeDef,Mulct mulct){
		List<MulctDef> mulctdefList = mulct.mulctDefs;
		for(MulctDef mulctDef: mulctdefList){
			STNQPLSPbyAmtTermRespSubMulctRule subMulctDef = new STNQPLSPbyAmtTermRespSubMulctRule();
			subMulctDef.setDayPastDue(mulctDef.mulctOverDays); //逾期天数
			subMulctDef.setColPastDue(mulctDef.cpdOverDays); //进入催收天数
			subMulctDef.setMulctAmt(mulctDef.mulctOverAmt); //罚金金额
			subMulctDef.setMulctRate(mulctDef.mulctOverRate); //罚金费率
			subLoanFeeDef.getMulctRulesList().add(subMulctDef);
		}
	}
	/*
	 * 合作方代收罚金列表
	 * Mr.L
	 */
	public void setRespCooperMulctRuleList(STNQPLPAllLoanFeeDefSubLoanFeeDefList subLoanFeeDef,LoanFeeDef loanFeeDef){
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
			subLoanFeeDef.getCooperMulctRulesList().add(repayMulctDef);
		}
	}
	/*
	 * 提前还款规则列表
	 */
	public void setRespInfoEarlyRepayDefList(STNQPLPAllLoanFeeDefSubLoanFeeDefList subLoanFeeDef,List<EarlyRepayDef> earlyRepayDefList){
		for(EarlyRepayDef earlyRepayDef:earlyRepayDefList ){
			STNQPLSPbyAmtTermRespSubPrepayRule subPrepayRule = new STNQPLSPbyAmtTermRespSubPrepayRule();
			
			subPrepayRule.setPrepayCurrTerm(earlyRepayDef.adCurPeriod); //提前还款当前期数
			subPrepayRule.setPrepayFeeAmt(earlyRepayDef.adFeeAmt); //提前还款手续费金额
			subPrepayRule.setPrepayFeeRate(earlyRepayDef.adFeeScale); //提前还款手续费费率
			subLoanFeeDef.getPrepayRulesList().add(subPrepayRule);
		}
	}
	/*
	 * 合作方提前还款规则列表
	 * Mr.L
	 */
	public void setRespReplaceEarlyRepayDefList(STNQPLPAllLoanFeeDefSubLoanFeeDefList subLoanFeeDef,List<ReplaceEarlyRepayDef> replaceEarlyRepayDefList,LoanFeeDef loanFeeDef){
		if( null == replaceEarlyRepayDefList)
			return ;
		for(ReplaceEarlyRepayDef earlyRepayDef:replaceEarlyRepayDefList ){
			STNQPLSPbyAmtTermRespCooperSubPrepayRule subPrepayRule = new STNQPLSPbyAmtTermRespCooperSubPrepayRule();
			subPrepayRule.setCooperPrepayCurrTerm(earlyRepayDef.replaceAdCurPeriod); //提前还款当前期数
			subPrepayRule.setCooperPrepayFeeAmt(earlyRepayDef.replaceAdFeeAmt); //提前还款手续费金额
			subPrepayRule.setCooperPrepayFeeRate(earlyRepayDef.replaceAdFeeScale); //提前还款手续费费率
			subLoanFeeDef.getCooperPrepayRulesList().add(subPrepayRule);
		}
	}
	
	
	/*
	 * 终端类型
	 */
	private void setRespInfoGroupCtrlList(STNQPLPAllLoanFeeDefSubLoanFeeDefList subLoanFeeDef, GroupCtrl groupCtrl) {
		List<String> subTerminalList = new ArrayList<String>();
		for(Terminal terminal:groupCtrl.subTermialList){
			subTerminalList.add(terminal.terminalId);
		}
		subLoanFeeDef.setSubTerminalList(subTerminalList);
	}
	
	private void setExtractFeeInfo(STNQPLPAllLoanFeeDefSubLoanFeeDefList resp,ProductCredit productCredit,BigDecimal amt){
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

