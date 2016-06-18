package com.sunline.ccs.service.handler.sub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CashLendingFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.loan.AbstractLoanProvide;
import com.sunline.ccs.loan.CashLoanProvideImpl;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.handler.CashLoanService;
import com.sunline.ccs.service.protocol.S13084Req;
import com.sunline.ccs.service.protocol.S13084Resp;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRLoanC
 * @see 描述：现金分期申请
 *
 * @see 创建日期： 2015年6月25日 上午12:15:18
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRLoanC {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private CashLoanService cashLoanService;

	@Transactional
	public S13084Resp handler(S13084Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13084", "现金分期申请", req, true);
		cashLoanService.validateInput(req);
		CcsCard card = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CcsAcct acct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		LoanPlan loanPlan = unifiedParamFacilityProvide.loanPlan(card.getProductCd(), LoanType.C);
		LoanFeeDef loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(card.getProductCd(), LoanType.C, req.getLoan_init_term());
		cashLoanService.validateSysInfo(req, card, acct, loanPlan, loanFeeDef);
		if (!cashLoanService.isCalcOnly(req)) {
			cashLoanService.validateForApply(req, card, acct, loanPlan, loanFeeDef);
		}

		AbstractLoanProvide loanProvide = new CashLoanProvideImpl(loanFeeDef, LoanType.C);
		String refNo = cashLoanService.generateFlowNo();
		CcsLoanReg loanReg = loanProvide.genLoanReg(req.loan_init_term, req.cash_amt, refNo, card.getLogicCardNbr(), req.card_no, req.loan_fee_method,
				acct.getAcctNbr(), acct.getAcctType(), loanPlan.loanCode, unifiedParamFacilityProvide.BusinessDate());

		S13084Resp resp = new S13084Resp();
		String returnResponse = cashLoanService.loan(req, card, acct, loanReg);
		boolean isLendingTrue = CashLendingFacility.RPC_SUCCESS.equals(returnResponse);
		if (!isLendingTrue) {
			throw new ProcessException(Constants.ERRL052_CODE, Constants.ERRL052_MES + returnResponse);
		}
		resp = cashLoanService.makeResponse(acct, loanReg);
		LogTools.printLogger(logger, "S13084", "现金分期申请", resp, false);
		return resp;
	}

}
