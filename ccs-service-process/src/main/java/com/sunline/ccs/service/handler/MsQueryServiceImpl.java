package com.sunline.ccs.service.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.service.api.MsQueryService;
import com.sunline.ccs.service.handler.appapi.AbstractHandler;
import com.sunline.ccs.service.handler.query.TFDRemainderTransfer;
import com.sunline.ccs.service.handler.query.TFNTxnOrderInq;
import com.sunline.ccs.service.handler.query.TNMAAcctDDcard;
import com.sunline.ccs.service.handler.query.TNMAAdjustQuota;
import com.sunline.ccs.service.handler.query.TNMApplyDelay;
import com.sunline.ccs.service.handler.query.TNMCCustMobile;
import com.sunline.ccs.service.handler.query.TNMLBookingHandler;
import com.sunline.ccs.service.handler.query.TNMUpdateBillDate;
import com.sunline.ccs.service.handler.query.TNQAAcctsbyCustUUID;
import com.sunline.ccs.service.handler.query.TNQAAcctsbyCustUUID2;
import com.sunline.ccs.service.handler.query.TNQLLoanPMTScheByContrID;
import com.sunline.ccs.service.handler.query.TNQLLoanPMTScheByContrID2;
import com.sunline.ccs.service.handler.query.TNQLoanSetupQuery;
import com.sunline.ccs.service.handler.query.TNQOrderInfo;
import com.sunline.ccs.service.handler.query.TNQPLPAllLoanFeeDef;
import com.sunline.ccs.service.handler.query.TNQPLPAllLoanFeeDefDetail;
import com.sunline.ccs.service.handler.query.TNQPLPAllLoanPlan;
import com.sunline.ccs.service.handler.query.TNQPLPAmtRange;
import com.sunline.ccs.service.handler.query.TNQPLPApplyInfoQuery;
import com.sunline.ccs.service.handler.query.TNQPLSPbyAmtTerm;
import com.sunline.ccs.service.handler.query.TNQPLoanPlanAppleDataEMaterialQuery;
import com.sunline.ccs.service.handler.query.TNQPartnerLoanControl;
import com.sunline.ccs.service.handler.query.TNQPrepayPkgQuery;
import com.sunline.ccs.service.handler.query.TNQTxnByContract;
import com.sunline.ccs.service.handler.query.TNTAdvanceRepay;
import com.sunline.ccs.service.handler.query.TNTLLoanScheduleCalc;
import com.sunline.ccs.service.handler.query.TNTLLoanUsableCalc;
import com.sunline.ccs.service.handler.query.TNTLMCATWithdrawCalc;
import com.sunline.ccs.service.handler.query.TNTLMactLoanCalc;
import com.sunline.ccs.service.handler.qunarcooper.TFCTrustLoanRefundNotice;
import com.sunline.ccs.service.handler.qunarcooper.TFCTrustLoanRepayNotice;
import com.sunline.ccs.service.handler.qunarcooper.TFDTrustLoanWithDrawNotice;
import com.sunline.ccs.service.handler.qunarcooper.TNMTrustLoanSchedNotice;
import com.sunline.ccs.service.msdentity.SMTNMAAdjustQuotaReq;
import com.sunline.ccs.service.msdentity.STFDRemainderTransferReq;
import com.sunline.ccs.service.msdentity.STFDRemainderTransferResp;
import com.sunline.ccs.service.msdentity.STFNTxnOrderInqReq;
import com.sunline.ccs.service.msdentity.STFNTxnOrderInqResp;
import com.sunline.ccs.service.msdentity.STNQAAcctsbyCustUUIDReq;
import com.sunline.ccs.service.msdentity.STNQAAcctsbyCustUUIDResp;
import com.sunline.ccs.service.msdentity.STNQLLoanPMTScheByContrIDReq;
import com.sunline.ccs.service.msdentity.STNQLLoanPMTScheByContrIDResp;
import com.sunline.ccs.service.msdentity.STNQOrderInfoReq;
import com.sunline.ccs.service.msdentity.STNQOrderInfoResp;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefDetailReq;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefDetailResp;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefReq;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanFeeDefResp;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanPlanReq;
import com.sunline.ccs.service.msdentity.STNQPLPAllLoanPlanResp;
import com.sunline.ccs.service.msdentity.STNQPLPAmtRangeReq;
import com.sunline.ccs.service.msdentity.STNQPLPAmtRangeResp;
import com.sunline.ccs.service.msdentity.STNQPLSPbyAmtTermReq;
import com.sunline.ccs.service.msdentity.STNQPLSPbyAmtTermResp;
import com.sunline.ccs.service.msdentity.STNQPartnerLoanControlReq;
import com.sunline.ccs.service.msdentity.STNQPartnerLoanControlResp;
import com.sunline.ccs.service.msdentity.STNQPrepayPkgQueryReq;
import com.sunline.ccs.service.msdentity.STNQPrepayPkgQueryResp;
import com.sunline.ccs.service.msdentity.STNTLLoanScheduleCalcReq;
import com.sunline.ccs.service.msdentity.STNTLLoanScheduleCalcResp;
import com.sunline.ccs.service.msdentity.STNTLLoanUsableCalcResp;
import com.sunline.ccs.service.msdentity.STNTLMactLoanCalcReq;
import com.sunline.ccs.service.msdentity.STNTLMactLoanCalcResp;
import com.sunline.ccs.service.msentity.TFCTrustLoanRefundNoticeReq;
import com.sunline.ccs.service.msentity.TFCTrustLoanRefundNoticeResp;
import com.sunline.ccs.service.msentity.TFCTrustLoanRepayNoticeReq;
import com.sunline.ccs.service.msentity.TFCTrustLoanRepayNoticeResp;
import com.sunline.ccs.service.msentity.TFDTrustLoanWithDrawNoticeReq;
import com.sunline.ccs.service.msentity.TFDTrustLoanWithDrawNoticeResp;
import com.sunline.ccs.service.msentity.TNMAAcctDDcardReq;
import com.sunline.ccs.service.msentity.TNMAAcctDDcardResp;
import com.sunline.ccs.service.msentity.TNMApplyDelayReq;
import com.sunline.ccs.service.msentity.TNMApplyDelayResp;
import com.sunline.ccs.service.msentity.TNMCCustMobileReq;
import com.sunline.ccs.service.msentity.TNMCCustMobileResp;
import com.sunline.ccs.service.msentity.TNMLBookingReq;
import com.sunline.ccs.service.msentity.TNMLBookingResp;
import com.sunline.ccs.service.msentity.TNMTrustLoanSchedNoticeReq;
import com.sunline.ccs.service.msentity.TNMTrustLoanSchedNoticeResp;
import com.sunline.ccs.service.msentity.TNMUpdateBillDateReq;
import com.sunline.ccs.service.msentity.TNMUpdateBillDateResp;
import com.sunline.ccs.service.msentity.TNQLoanSetupQueryReq;
import com.sunline.ccs.service.msentity.TNQLoanSetupQueryResp;
import com.sunline.ccs.service.msentity.TNQPLPApplyInfoReq;
import com.sunline.ccs.service.msentity.TNQPLPApplyInfoResp;
import com.sunline.ccs.service.msentity.TNQPLoanPlanAPPLeDataReq;
import com.sunline.ccs.service.msentity.TNQPLoanPlanAPPLeDataResp;
import com.sunline.ccs.service.msentity.TNQTxnByContractReq;
import com.sunline.ccs.service.msentity.TNQTxnByContractResp;
import com.sunline.ccs.service.msentity.TNTAdvanceRepayReq;
import com.sunline.ccs.service.msentity.TNTAdvanceRepayResp;
import com.sunline.ccs.service.msentity.TNTLMCATWithdrawCalcReq;
import com.sunline.ccs.service.msentity.TNTLMCATWithdrawCalcResp;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：MsQueryServiceImpl
 * @see 描述：消费金融查询类服务
 *
 * @see 创建日期：   2015-6-25下午2:32:33
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */

@Service
public class MsQueryServiceImpl implements MsQueryService {
	@Autowired
	private TNQAAcctsbyCustUUID tnqaAcctsbyCustUUID;
	@Autowired
	private TNQAAcctsbyCustUUID2 tnqaAcctsbyCustUUID2;
	@Autowired
	private TNQLLoanPMTScheByContrID tnqlLoanPMTScheByContrID;
	@Autowired
	private TNQLLoanPMTScheByContrID2 tnqlLoanPMTScheByContrID2;
	@Autowired
	private TNQPLPAmtRange tNQPLPAmtRange;
	@Autowired
	private TNQPLPApplyInfoQuery tNQPLPApplyInfoQuery;
	@Autowired
	private TNQPLSPbyAmtTerm tNQPLSPbyAmtTerm;
	@Autowired
	private TNTLLoanScheduleCalc tNTLLoanScheduleCalc;
	@Autowired
	public TNTLMactLoanCalc tNTLMactLoanCalc;

	@Autowired
	private TNMAAcctDDcard tNMAAcctDDcard;
	@Autowired
	private TNMCCustMobile tNMCCustMobile;
	@Autowired
	TNQPLoanPlanAppleDataEMaterialQuery tNQPLoanPlanAppleDataEMaterialQuery;
	@Autowired
	private Map<String, AbstractHandler> bizHandlers;
	@Autowired
	private TNQOrderInfo tNQOrderInfo;
	@Autowired
	private TNMAAdjustQuota tnmAAdjustQuota;
	@Autowired
	private TNQPLPAllLoanPlan tnqPLPAllLoanPlan;
	@Autowired
	private TNQPLPAllLoanFeeDef tnqPLPAllLoanFeeDef;
	@Autowired
	private TFNTxnOrderInq tfnTxnOrderInq;
	@Autowired
	private TNQTxnByContract tnqTxnByContract;
	@Autowired
	private TNTLMCATWithdrawCalc tntlMCATWithdrawCalc;
	@Autowired
	private TNTLLoanUsableCalc tntLLoanUsableCalc;
	@Autowired
	private TNQPLPAllLoanFeeDefDetail tnqPLPAllLoanFeeDefDetail;
	@Autowired
	private TNTAdvanceRepay tntAdvanceRepay;
	@Autowired
	private TFDTrustLoanWithDrawNotice tfdTrustLoanWithDrawNotice;
	
//	@Autowired
//	private TNQToselect tnqToselect;
	@Autowired
	private TNQPartnerLoanControl tnqPartnerLoanControl;

	@Autowired
	private TFDRemainderTransfer tfdRemainderTransfer;
	@Autowired
	private TFCTrustLoanRefundNotice tfcTrustLoanRefundNotice;
	@Autowired
	private TNMTrustLoanSchedNotice tnmTrustLoanSchedNotice;
	@Autowired
	private TFCTrustLoanRepayNotice tfcTrustLoanRepayNoticeHandler;
	@Autowired
	private TNQLoanSetupQuery tnqLoanSetupQuery;
	@Autowired
	private TNQPrepayPkgQuery tnqPrepayPkgQuery;
	@Autowired
	private TNMUpdateBillDate tnmUpdateBillDate;
	@Autowired
	private TNMApplyDelay tnmApplyDelay;
	
	
	@Override
	public STNQAAcctsbyCustUUIDResp acctsQueryByCustUUID(STNQAAcctsbyCustUUIDReq req) throws ProcessException{
		return tnqaAcctsbyCustUUID.handler(req);
	}
	
	@Override
	public STNQAAcctsbyCustUUIDResp acctsQueryByCustUUID2(STNQAAcctsbyCustUUIDReq req) throws ProcessException{
		return tnqaAcctsbyCustUUID2.handler(req);
	}
	
	@Override
	public STNQLLoanPMTScheByContrIDResp loanPmtScheQueryByContrId(STNQLLoanPMTScheByContrIDReq req) throws ProcessException{
		return tnqlLoanPMTScheByContrID.handler(req);
	}
	
	@Override
	public STNQLLoanPMTScheByContrIDResp loanPmtScheQueryByContrId2(STNQLLoanPMTScheByContrIDReq req) throws ProcessException{
		return tnqlLoanPMTScheByContrID2.handler(req);
	}
	
	@Override
	public TNQPLPApplyInfoResp loanProdQueryForApply(TNQPLPApplyInfoReq req)
			throws ProcessException {
		return tNQPLPApplyInfoQuery.handler(req);
	}

	@Override
	public STNQPLSPbyAmtTermResp loanProdDetailQueryByAmtAndTerm(
			STNQPLSPbyAmtTermReq req) throws ProcessException {
		return tNQPLSPbyAmtTerm.handler(req);
	}


	@Override
	public STNQPLPAmtRangeResp loanAmtRangeQueryByTermAndAmt(
			STNQPLPAmtRangeReq req) throws ProcessException {
		return tNQPLPAmtRange.handler(req);
	}

	@Override
	public STNTLLoanScheduleCalcResp loanScheduleCalc(
			STNTLLoanScheduleCalcReq req) throws ProcessException {
		return tNTLLoanScheduleCalc.handler(req);
	}
	
	@Override
	public STNTLMactLoanCalcResp mactLoanCalc(
			STNTLMactLoanCalcReq req) throws ProcessException {
		return tNTLMactLoanCalc.handler(req);
	}
	
	@Override
	public TNMAAcctDDcardResp acctDDcardModify(TNMAAcctDDcardReq req)
			throws ProcessException {
		return tNMAAcctDDcard.handler(req);
	}

	@Override
	public TNMCCustMobileResp custMobileModify(TNMCCustMobileReq req)
			throws ProcessException {
		return tNMCCustMobile.handler(req);
	}

	@Override
	public TNQPLoanPlanAPPLeDataResp loanPlanAPPLeDataEMaterialQuery(TNQPLoanPlanAPPLeDataReq req) 
			throws ProcessException {
		return tNQPLoanPlanAppleDataEMaterialQuery.handler(req);
	}

	@Override
	public TNMLBookingResp tnmlBookingResp(TNMLBookingReq req) {
		
		return (TNMLBookingResp) bizHandlers.get(TNMLBookingHandler.class.getSimpleName()).execute(req);
	}

	@Override
	public STNQOrderInfoResp tnqOrderInfo(STNQOrderInfoReq req)
			throws ProcessException {
		
		return tNQOrderInfo.handler(req);
	}

	@Override
	public void tnmAAdjustQuota(SMTNMAAdjustQuotaReq req) throws ProcessException {
		
		tnmAAdjustQuota.handler(req);
	}

	@Override
	public STNQPLPAllLoanPlanResp tnqPLPAllLoanPlan(STNQPLPAllLoanPlanReq req)
			throws ProcessException {
		
		return tnqPLPAllLoanPlan.handler(req);
	}
	
	@Override
	public STNQPLPAllLoanFeeDefResp tnqPLPAllLoanFeeDef(STNQPLPAllLoanFeeDefReq req) throws ProcessException {
		
		return tnqPLPAllLoanFeeDef.handler(req);
	}

	@Override
	public STFNTxnOrderInqResp tfnTxnOrderInq(STFNTxnOrderInqReq req) throws ProcessException {

		return tfnTxnOrderInq.handler(req);
	}

	@Override
	public TNQTxnByContractResp tnqTxnByContract( TNQTxnByContractReq req) throws ProcessException {
		
		return tnqTxnByContract.handler(req);
	}

	@Override
	public TNTLMCATWithdrawCalcResp tntlMCATWithdrawCalc( TNTLMCATWithdrawCalcReq req) throws ProcessException {
		
		return tntlMCATWithdrawCalc.handler(req);
	}

	@Override
	public STNTLLoanUsableCalcResp tntLLoanUsableCalc(STNTLLoanScheduleCalcReq req) throws ProcessException {

		return tntLLoanUsableCalc.handler(req);
	}

	@Override
	public STNQPLPAllLoanFeeDefDetailResp tnqPLPAllLoanFeeDefDetail(STNQPLPAllLoanFeeDefDetailReq req) throws ProcessException {
		
		return tnqPLPAllLoanFeeDefDetail.handler(req);
	}

	@Override
	public TNTAdvanceRepayResp tntAdvanceRepay(TNTAdvanceRepayReq req) throws ProcessException {
		
		return tntAdvanceRepay.handler(req);
	}


//	@Override
//	public STNQToselectResp tnqToselect(STNQToselectReq req) throws ProcessException {
//
//		return tnqToselect.handler(req);
//	}


	@Override
	public STFDRemainderTransferResp tfdRemainderTransfer(
			STFDRemainderTransferReq req) throws ProcessException {

		return tfdRemainderTransfer.handle(req);
	}

	@Override
	public STNQPartnerLoanControlResp tnqPartnerLoanControl(STNQPartnerLoanControlReq req) throws ProcessException {

		return tnqPartnerLoanControl.handler(req);
	}

	@Override
	public TFDTrustLoanWithDrawNoticeResp tfdTrustLoanWithDrawNotice(TFDTrustLoanWithDrawNoticeReq req) throws ProcessException {
		
		return tfdTrustLoanWithDrawNotice.handler(req);
	}

	@Override
	public TFCTrustLoanRefundNoticeResp tfcTrustLoanRefundNotice(TFCTrustLoanRefundNoticeReq req) throws ProcessException {
		
		return tfcTrustLoanRefundNotice.handler(req);
	}

	@Override
	public TFCTrustLoanRepayNoticeResp tfcTrustLoanRepayNotice(TFCTrustLoanRepayNoticeReq req) throws ProcessException {
		return tfcTrustLoanRepayNoticeHandler.handler(req);
	}

//	@Override
//	public STNQToselectResp tnqToselect(STNQToselectReq req) throws ProcessException {
	
//		return null;
//	}

	@Override
	public TNMTrustLoanSchedNoticeResp tnmTrustLoanSchedNotice(TNMTrustLoanSchedNoticeReq req) throws ProcessException {

		return tnmTrustLoanSchedNotice.handler(req);
	}

	@Override
	public TNQLoanSetupQueryResp tnqLoanSetupQuery(TNQLoanSetupQueryReq req) throws ProcessException {
		
		return tnqLoanSetupQuery.handler(req);
	}

	@Override
	public STNQPrepayPkgQueryResp tnqPrepayPkgQuery(STNQPrepayPkgQueryReq req)
			throws ProcessException {
		// TODO Auto-generated method stub
		return tnqPrepayPkgQuery.handler(req);
	}

	@Override
	public TNMUpdateBillDateResp tnmUpdateBillDate(TNMUpdateBillDateReq req) throws ProcessException {
		
		return tnmUpdateBillDate.handler(req);
	}

	@Override
	public TNMApplyDelayResp tnmApplyDelay(TNMApplyDelayReq req)
			throws ProcessException {
		return tnmApplyDelay.handler(req);
	}

}
