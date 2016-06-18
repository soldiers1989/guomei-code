package com.sunline.ccs.service.handler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.service.api.MsLoanService;
import com.sunline.ccs.service.handler.appapi.AbstractHandler;
import com.sunline.ccs.service.handler.appapi.TFCCLSWithholding;
import com.sunline.ccs.service.handler.appapi.TFCCouponRepay;
import com.sunline.ccs.service.handler.appapi.TFCRefundHandler;
import com.sunline.ccs.service.handler.appapi.TFCRepayHandler;
import com.sunline.ccs.service.handler.appapi.TFCTerminalRepayHandler;
import com.sunline.ccs.service.handler.appapi.TFCWithholding;
import com.sunline.ccs.service.handler.appapi.TFDCommodyLoanWithDraw;
import com.sunline.ccs.service.handler.appapi.TFDWithDrawHandler;
import com.sunline.ccs.service.handler.appapi.TFDWithDrawVoidHandler;
import com.sunline.ccs.service.handler.appapi.TFNAAcctSetupCycleHandler;
import com.sunline.ccs.service.handler.appapi.TFNCommodyLoanSetup;
import com.sunline.ccs.service.handler.appapi.TFNCommodyLoanSetupWithDraw;
import com.sunline.ccs.service.handler.appapi.TFRLargeCaseLoan;
import com.sunline.ccs.service.handler.appapi.TNRAAcctSetupWithDrawHandler;
import com.sunline.ccs.service.handler.qunarcooper.TFNTrustLoanSetup;
import com.sunline.ccs.service.handler.sunshine.TNRRecommitOrderExtemal;
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
 * 
 * 马上贷服务实现
 * @author wangz
 *
 */
@Service
public class MsLoanServiceImpl implements MsLoanService {

    public Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
	private Map<String, AbstractHandler> bizHandlers;
    
	@Override
	public TFCRepayResp tfcRepay(TFCRepayReq tfcRepayReq) {
		
		return (TFCRepayResp) bizHandlers.get(TFCRepayHandler.class.getSimpleName()).execute(tfcRepayReq);
	}
	
	@Override
	public TFDWithDrawResp tfdWithDraw(TFDWithDrawReq tfdWithDrawReq) {
		
		return (TFDWithDrawResp) bizHandlers.get(TFDWithDrawHandler.class.getSimpleName()).execute(tfdWithDrawReq);
	}
	
	@Override
	public TFNAAcctSetupCycleResp tfnaAcctSetupCycle(TFNAAcctSetupCycleReq tfnaAcctSetupCycleReq) {
		
		return (TFNAAcctSetupCycleResp) bizHandlers.get(TFNAAcctSetupCycleHandler.class.getSimpleName()).execute(tfnaAcctSetupCycleReq);
	}
	
	@Override
	public TNRAAcctSetupWithDrawResp tnraAcctSetupWithDraw(TNRAAcctSetupWithDrawReq tnraAcctSetupWithDrawReq) {
		
		return (TNRAAcctSetupWithDrawResp) bizHandlers.get(TNRAAcctSetupWithDrawHandler.class.getSimpleName()).execute(tnraAcctSetupWithDrawReq);
	}

	@Override
	public TFCCLSWithholdingResp tfccLSWithholding(TFCCLSWithholdingReq tfccLSWithholdingReq) {
		
		return (TFCCLSWithholdingResp)bizHandlers.get(TFCCLSWithholding.class.getSimpleName()).execute(tfccLSWithholdingReq);
	}

	@Override
	public TFNCommodyLoanSetupResp tfnCommodyLoanSetup(TFNCommodyLoanSetupReq tnfCommodyLoanSetupReq) {

		return (TFNCommodyLoanSetupResp)bizHandlers.get(TFNCommodyLoanSetup.class.getSimpleName()).execute(tnfCommodyLoanSetupReq);
	}


	@Override
	public TFCTerminalRepayResp tfcTerminalRepayHandler(TFCTerminalRepayReq req) {
		
		return (TFCTerminalRepayResp)bizHandlers.get(TFCTerminalRepayHandler.class.getSimpleName()).execute(req);
	}

	@Override
	public TFCRefundResp tfcRefund(TFCRefundReq tfcRefundReq) {
		return (TFCRefundResp) bizHandlers.get(TFCRefundHandler.class.getSimpleName()).execute(tfcRefundReq);
	}

	@Override
	public TFDWithDrawVoidResp tfdWithDrawVoid(TFDWithDrawVoidReq tfdWithDrawVoidReq) {
		return (TFDWithDrawVoidResp) bizHandlers.get(TFDWithDrawVoidHandler.class.getSimpleName()).execute(tfdWithDrawVoidReq);
	}

	@Override
	public TFDCommodyLoanWithDrawResp tfdCommodyLoanWithDraw(TFDCommodyLoanWithDrawReq tfdCommodyLoanWithDrawReq) {

		return (TFDCommodyLoanWithDrawResp)bizHandlers.get(TFDCommodyLoanWithDraw.class.getSimpleName()).execute(tfdCommodyLoanWithDrawReq);
	}

	@Override
	public TFNCommodyLoanSetupWithDrawResp tfnCommodyLoanSetupWithDraw(TFNCommodyLoanSetupWithDrawReq tfnCommodyLoanSetupWithDrawReq) {
		// TODO Auto-generated method stub
		return (TFNCommodyLoanSetupWithDrawResp)bizHandlers.get(TFNCommodyLoanSetupWithDraw.class.getSimpleName()).execute(tfnCommodyLoanSetupWithDrawReq);
	}

	@Override
	public TFRLargeCaseLoanResp tfrLargeCaseLoan(TFRLargeCaseLoanReq tfrLargeCaseLoanReq) {
		// TODO Auto-generated method stubTFRLargeCaseLoan
		return (TFRLargeCaseLoanResp)bizHandlers.get(TFRLargeCaseLoan.class.getSimpleName()).execute(tfrLargeCaseLoanReq);
	}

	@Override
	public TNRRecommitOrderExtemalResp tnrRecommitOrderExtemal(TNRRecommitOrderExtemalReq tnrRecommitOrderExtemalReq) {
		// TODO Auto-generated method stub
		return (TNRRecommitOrderExtemalResp)bizHandlers.get(TNRRecommitOrderExtemal.class.getSimpleName()).execute(tnrRecommitOrderExtemalReq);
	}

	@Override
	public TFNTrustLoanSetupResp tfnTrustLoanSetup(TFNTrustLoanSetupReq tfnTrustLoanSetupReq) {
		// TODO Auto-generated method stub
		return (TFNTrustLoanSetupResp)bizHandlers.get(TFNTrustLoanSetup.class.getSimpleName()).execute(tfnTrustLoanSetupReq);
	}

	@Override
	public TFCWithholdingResp tfcWithholding(TFCWithholdingReq tfcWithholdingReq) {
		
		return (TFCWithholdingResp)bizHandlers.get(TFCWithholding.class.getSimpleName()).execute(tfcWithholdingReq);
}

	@Override
	public TFCCouponRepayResp tfcCouponRepay(TFCCouponRepayReq req) {
																								
		return(TFCCouponRepayResp)bizHandlers.get(TFCCouponRepay.class.getSimpleName()).execute(req);
	}
}
