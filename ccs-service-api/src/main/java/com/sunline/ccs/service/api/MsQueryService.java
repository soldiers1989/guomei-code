package com.sunline.ccs.service.api;

import com.sunline.ark.support.meta.RPCVersion;
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
 * 马上贷查询服务类
* @author lizz
 *
 */
@RPCVersion(value="1.0.0")
public interface MsQueryService {
	
	/**
	 * 根据金额、期数获取产品详情
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNQPLSPbyAmtTermResp loanProdDetailQueryByAmtAndTerm(STNQPLSPbyAmtTermReq req) throws ProcessException;

	/**
	 * 计算产品还款详情
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNTLLoanScheduleCalcResp loanScheduleCalc(STNTLLoanScheduleCalcReq req) throws ProcessException;

	/**
	 * 计算随借随还试算信息
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNTLMactLoanCalcResp mactLoanCalc(STNTLMactLoanCalcReq req) throws ProcessException;

	/**
	 * 获取产品期数、金额范围
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNQPLPAmtRangeResp loanAmtRangeQueryByTermAndAmt(STNQPLPAmtRangeReq req) throws ProcessException;
	/**
	 * 合同列表查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNQAAcctsbyCustUUIDResp acctsQueryByCustUUID(STNQAAcctsbyCustUUIDReq req) throws ProcessException;

	public STNQAAcctsbyCustUUIDResp acctsQueryByCustUUID2(STNQAAcctsbyCustUUIDReq req) throws ProcessException;

	/**
	 * 合同详情查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNQLLoanPMTScheByContrIDResp loanPmtScheQueryByContrId(STNQLLoanPMTScheByContrIDReq req) throws ProcessException;
	
	public STNQLLoanPMTScheByContrIDResp loanPmtScheQueryByContrId2(STNQLLoanPMTScheByContrIDReq req) throws ProcessException;

	/**
	 * 产品信息查询(审批系统使用)
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNQPLPApplyInfoResp loanProdQueryForApply(TNQPLPApplyInfoReq req) throws ProcessException;

	/**
	 * 银行卡修改接口(全部有效合同进行更改)
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNMAAcctDDcardResp acctDDcardModify(TNMAAcctDDcardReq req) throws ProcessException;

	/**
	 * 客户手机号修改接口
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNMCCustMobileResp custMobileModify(TNMCCustMobileReq req) throws ProcessException;

	/**
	 * 查询产品需上传电子资料清单
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNQPLoanPlanAPPLeDataResp loanPlanAPPLeDataEMaterialQuery(TNQPLoanPlanAPPLeDataReq req) throws ProcessException;

	/**
	 * 提前结清申请
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNMLBookingResp tnmlBookingResp(TNMLBookingReq req) throws ProcessException;
	
	/**
	 * 订单流水查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNQOrderInfoResp tnqOrderInfo(STNQOrderInfoReq req) throws ProcessException;
	
	/**
	 * 调额短信发送
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public void tnmAAdjustQuota(SMTNMAAdjustQuotaReq req) throws ProcessException;
	
	/**
	 * 查询所有贷款产品
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNQPLPAllLoanPlanResp tnqPLPAllLoanPlan(STNQPLPAllLoanPlanReq req) throws ProcessException;

	/**
	 * 金融产品匹配
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNQPLPAllLoanFeeDefResp tnqPLPAllLoanFeeDef(STNQPLPAllLoanFeeDefReq req) throws ProcessException;

	/**
	 * 交易结果查询接口
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STFNTxnOrderInqResp tfnTxnOrderInq(STFNTxnOrderInqReq req) throws ProcessException;
	
	/**
	 * 交易查询（包含入账和未入账）
	 */
	public TNQTxnByContractResp tnqTxnByContract(TNQTxnByContractReq req) throws ProcessException;
	
	/**
	 * 提现试算
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNTLMCATWithdrawCalcResp tntlMCATWithdrawCalc(TNTLMCATWithdrawCalcReq req) throws ProcessException;

	/**
	 * 可用子产品试算
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNTLLoanUsableCalcResp tntLLoanUsableCalc(STNTLLoanScheduleCalcReq req) throws ProcessException;
	/**
	 * 贷款子产品详情列表查询接口
	 * 
	 */
	public STNQPLPAllLoanFeeDefDetailResp tnqPLPAllLoanFeeDefDetail(STNQPLPAllLoanFeeDefDetailReq req) throws ProcessException;
	
	/**
	 * 兜底提前结清申请
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNTAdvanceRepayResp tntAdvanceRepay(TNTAdvanceRepayReq req) throws ProcessException;
	
	/**
	 * 兜底溢缴款转出接口
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STFDRemainderTransferResp tfdRemainderTransfer(STFDRemainderTransferReq req) throws ProcessException;
//	/**
//	 * 审批查询接口
//	 * @param req
//	 * @return
//	 * @throws ProcessException
//	 */
//	public STNQToselectResp tnqToselect(STNQToselectReq req) throws ProcessException;
	/**
	 * 担保方贷款总控接口
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNQPartnerLoanControlResp tnqPartnerLoanControl(STNQPartnerLoanControlReq req) throws ProcessException;
	
	/**
	 * 去哪儿贷款消费结果通知（半托管）
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TFDTrustLoanWithDrawNoticeResp tfdTrustLoanWithDrawNotice(TFDTrustLoanWithDrawNoticeReq req) throws ProcessException;
	
	/**
	 * 去哪儿贷款退款结果通知（半托管）
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TFCTrustLoanRefundNoticeResp tfcTrustLoanRefundNotice(TFCTrustLoanRefundNoticeReq req) throws ProcessException;

	/**
	 * 去哪儿还款计划结果通知（半托管）
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNMTrustLoanSchedNoticeResp tnmTrustLoanSchedNotice(TNMTrustLoanSchedNoticeReq req) throws ProcessException;

	/**
	 * 去哪儿还款结果通知接口（半托管）
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TFCTrustLoanRepayNoticeResp tfcTrustLoanRepayNotice(TFCTrustLoanRepayNoticeReq req) throws ProcessException;

	/**
	 * 开户查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNQLoanSetupQueryResp tnqLoanSetupQuery(TNQLoanSetupQueryReq req) throws ProcessException;
	
	/**
	 * 灵活还款计划包查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public STNQPrepayPkgQueryResp tnqPrepayPkgQuery(STNQPrepayPkgQueryReq req) throws ProcessException;
	
	/**
	 * 变更换款日
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNMUpdateBillDateResp tnmUpdateBillDate(TNMUpdateBillDateReq req) throws ProcessException;
	
	/**
	 * 延期还款
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public TNMApplyDelayResp tnmApplyDelay(TNMApplyDelayReq req) throws ProcessException;
	
}
