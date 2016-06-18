package com.sunline.ccs.service.api;

import com.sunline.ark.support.meta.RPCVersion;
import com.sunline.ccs.service.msdentity.TNRRecommitOrderExtemalReq;
import com.sunline.ccs.service.msdentity.TNRRecommitOrderExtemalResp;
import com.sunline.ccs.service.msentity.TFCCLSWithholdingReq;
import com.sunline.ccs.service.msentity.TFCCLSWithholdingResp;
import com.sunline.ccs.service.msentity.TFCCouponRepayReq;
import com.sunline.ccs.service.msentity.TFCCouponRepayResp;
import com.sunline.ccs.service.msentity.TFCRefundReq;
import com.sunline.ccs.service.msentity.TFCRefundResp;
import com.sunline.ccs.service.msentity.TFCRepayReq;
import com.sunline.ccs.service.msentity.TFCRepayResp;
import com.sunline.ccs.service.msentity.TFCTerminalRepayReq;
import com.sunline.ccs.service.msentity.TFCTerminalRepayResp;
import com.sunline.ccs.service.msentity.TFCWithholdingReq;
import com.sunline.ccs.service.msentity.TFCWithholdingResp;
import com.sunline.ccs.service.msentity.TFDCommodyLoanWithDrawReq;
import com.sunline.ccs.service.msentity.TFDCommodyLoanWithDrawResp;
import com.sunline.ccs.service.msentity.TFDWithDrawReq;
import com.sunline.ccs.service.msentity.TFDWithDrawResp;
import com.sunline.ccs.service.msentity.TFDWithDrawVoidReq;
import com.sunline.ccs.service.msentity.TFDWithDrawVoidResp;
import com.sunline.ccs.service.msentity.TFNAAcctSetupCycleReq;
import com.sunline.ccs.service.msentity.TFNAAcctSetupCycleResp;
import com.sunline.ccs.service.msentity.TFNCommodyLoanSetupReq;
import com.sunline.ccs.service.msentity.TFNCommodyLoanSetupResp;
import com.sunline.ccs.service.msentity.TFNCommodyLoanSetupWithDrawReq;
import com.sunline.ccs.service.msentity.TFNCommodyLoanSetupWithDrawResp;
import com.sunline.ccs.service.msentity.TFNTrustLoanSetupReq;
import com.sunline.ccs.service.msentity.TFNTrustLoanSetupResp;
import com.sunline.ccs.service.msentity.TFRLargeCaseLoanReq;
import com.sunline.ccs.service.msentity.TFRLargeCaseLoanResp;
import com.sunline.ccs.service.msentity.TNRAAcctSetupWithDrawReq;
import com.sunline.ccs.service.msentity.TNRAAcctSetupWithDrawResp;


/**
 * 马上贷款服务接口
* @author lizz
 *
 */
@RPCVersion(value="1.0.0")
public interface MsLoanService {
	
	/**
	 * 主动还款接口（随借随还）
	 * @param tfcRepayReq
	 * @return
	 */
	public TFCRepayResp tfcRepay(TFCRepayReq tfcRepayReq);
	
	/**
	 * 循环现金贷提款响应接口(随借随换)
	 * @param tfdWithDrawReq
	 * @return
	 */
	public TFDWithDrawResp tfdWithDraw(TFDWithDrawReq tfdWithDrawReq);
	
	/**
	 * 循环现金贷开户响应接口
	 * @param tfnaAcctSetupCycleReq
	 * @return
	 */
	public TFNAAcctSetupCycleResp tfnaAcctSetupCycle(TFNAAcctSetupCycleReq tfnaAcctSetupCycleReq);
	
	/**
	 * 非循环现金贷开户放款响应接口
	 * @param tnraAcctSetupWithDrawReq
	 * @return
	 */
	public TNRAAcctSetupWithDrawResp tnraAcctSetupWithDraw(TNRAAcctSetupWithDrawReq tnraAcctSetupWithDrawReq);

	/**
	 * 催收系统代扣接口
	 * @param tfccLSWithholdingReq
	 * @return
	 */
	public TFCCLSWithholdingResp tfccLSWithholding(TFCCLSWithholdingReq tfccLSWithholdingReq);
	
	/**
	 * 商品贷开户接口
	 * @param TFNCommodyLoanSetupReq
	 * @return
	 */
	public TFNCommodyLoanSetupResp tfnCommodyLoanSetup(TFNCommodyLoanSetupReq tnfCommodyLoanSetupReq);
	/**
	 * 商品贷提现接口
	 * @param TFDCommodyLoanWithDrawReq
	 * @return
	 */
	public TFDCommodyLoanWithDrawResp tfdCommodyLoanWithDraw(TFDCommodyLoanWithDrawReq tfdCommodyLoanWithDrawReq);
	/**
	 * 商品贷开户放款
	 * @param tfccLSWithholdingReq
	 * @return
	 */
	public TFNCommodyLoanSetupWithDrawResp tfnCommodyLoanSetupWithDraw(TFNCommodyLoanSetupWithDrawReq tfdCommodyLoanWithDrawReq);

	
	
	/**
	 * 退货接口
	 * @param tfcRefundReq
	 * @return
	 */
	public TFCRefundResp tfcRefund(TFCRefundReq tfcRefundReq);
	
	/**
	 * 放款撤销接口
	 * @param tfccLSWithholdingReq
	 * @return
	 */
	public TFDWithDrawVoidResp tfdWithDrawVoid(TFDWithDrawVoidReq tfdWithDrawVoidReq);
	
	/**
	 * 不调用支付的随借随还主动还款
	 */
	public TFCTerminalRepayResp tfcTerminalRepayHandler(TFCTerminalRepayReq req);
	
	/**
	 * 大额现金贷开户放款
	 */
	public TFRLargeCaseLoanResp tfrLargeCaseLoan(TFRLargeCaseLoanReq req);
	/**
	 * 放款重提接口
	 */
	public TNRRecommitOrderExtemalResp tnrRecommitOrderExtemal(TNRRecommitOrderExtemalReq req);
	/**
	 * 去哪开户接口
	 */
	public TFNTrustLoanSetupResp tfnTrustLoanSetup(TFNTrustLoanSetupReq req);
	
	/**
	 * 不发送催收通知接口
	 */
	public TFCWithholdingResp tfcWithholding(TFCWithholdingReq req);
	
	/**
	 * 不调用支付的随借随还主动还款
	 */
	public TFCCouponRepayResp tfcCouponRepay(TFCCouponRepayReq req);
	
}
