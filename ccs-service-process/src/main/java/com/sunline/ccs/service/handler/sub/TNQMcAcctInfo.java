package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20014Req;
import com.sunline.ccs.service.protocol.S20014Resp;
import com.sunline.ccs.service.provide.CallOTBProvide;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcAcctInfo
 * @see 描述： 贷款账户信息查询
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcAcctInfo {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private AcctOTB accountOTB;
	@Autowired
	private CallOTBProvide callOTBProvide;

	/**
	 * @see 方法名：handler
	 * @see 描述：贷款账户信息查询handler
	 * @see 创建日期：2015年6月26日上午10:28:18
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public S20014Resp handler(S20014Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20014", "账户信息查询", req, true);
		S20014Resp resp = new S20014Resp();
		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());

		// 获取卡片信息
		CcsCard card;
		card = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(card, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 获取参数
		Product product = unifiedParameterFacilityProvide.product(card.getProductCd());
		ProductCredit productCredit = unifiedParameterFacilityProvide.productCredit(card.getProductCd());
		Date businessDate = unifiedParameterFacilityProvide.BusinessDate();

		// 获取账户信息
		CcsAcct acct = custAcctCardQueryFacility.getAcctByCardNbr(req.getCard_no(), AccountType.E);
		CheckUtil.rejectNull(acct, Constants.ERRC012_CODE, Constants.ERRC012_MES);
		CcsAcctO CcsAcctO = custAcctCardQueryFacility.getAcctOByAcctNbr(acct.getAcctType(), acct.getAcctNbr());
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRC026_CODE, Constants.ERRC026_MES);

		// 构建响应报文对象
		resp.setCard_no(req.getCard_no());
		resp.setProduct_name(product.description);
		resp.setName(acct.getName());
		resp.setCredit_limit(acct.getCreditLmt());
		resp.setTemp_limit(acct.getTempLmt());
		resp.setTemp_limit_begin_date(acct.getTempLmtBegDate());
		resp.setTemp_limit_end_date(acct.getTempLmtEndDate());
		resp.setCash_limit_rt(acct.getCashLmtRate());
		resp.setOvrlmt_rate(acct.getOvrlmtRate());
		resp.setLoan_limit_rt(acct.getLoanLmtRate());
		resp.setCurr_bal(acct.getCurrBal());
		resp.setCash_bal(acct.getCashBal());
		resp.setPrincipal_bal(acct.getPrincipalBal());
		resp.setLoan_bal(acct.getLoanBal());
		resp.setDispute_amt(acct.getDisputeAmt());
		resp.setBegin_bal(acct.getBegBal());
		resp.setPmt_due_day_bal(acct.getPmtDueDayBal());
		BigDecimal qualGraceBal = acct.getQualGraceBal()
				.subtract(acct.getCtdRepayAmt().add(acct.getCtdCrAdjAmt()).add(acct.getCtdRefundAmt()).add(CcsAcctO.getMemoCr()))
				.setScale(2, BigDecimal.ROUND_HALF_UP);
		resp.setQual_grace_bal(qualGraceBal);
		resp.setGrace_days_full_ind(acct.getGraceDaysFullInd());
		resp.setAcct_cash_otb(accountOTB.acctCashOTB(CcsAcctO, productCredit, businessDate));
		BigDecimal available_otb = callOTBProvide.realOTB(req.getCard_no(), custAcctCardQueryFacility.getAcctByCardNbr(req.getCard_no())).setScale(2,
				BigDecimal.ROUND_HALF_UP);
		// 当综合可用额度为负时，取0
		resp.setAvailable_otb(available_otb.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : available_otb);
		resp.setCurr_remain_tot_bal(acct.getQualGraceBal().subtract(acct.getCtdRepayAmt()).subtract(acct.getCtdCrAdjAmt()));
		resp.setSetup_date(acct.getSetupDate());
		resp.setOwning_branch(acct.getOwningBranch());
		resp.setBilling_cycle(acct.getCycleDay());
		resp.setStmt_flag(acct.getStmtFlag());
		resp.setStmt_mail_addr_ind(acct.getStmtMailAddrInd());
		resp.setStmt_media_type(acct.getStmtMediaType());
		resp.setBlock_code(acct.getBlockCode());// 相同字段，以TM_ACCOUNT数据为准
		resp.setAge_cd(acct.getAgeCode());
		resp.setUnmatch_db(CcsAcctO.getMemoDb());// 相同字段，以TM_ACCOUNT_O数据为准
		resp.setUnmatch_cash(CcsAcctO.getMemoCash());// 相同字段，以TM_ACCOUNT_O数据为准
		resp.setUnmatch_cr(CcsAcctO.getMemoCr());// 相同字段，以TM_ACCOUNT_O数据为准
		resp.setDual_billing_flag(acct.getDualBillingFlag());
		resp.setLast_pmt_amt(acct.getLastPmtAmt());
		resp.setLast_pmt_date(acct.getLastPmtDate());
		resp.setLast_stmt_date(acct.getLastStmtDate());
		resp.setNext_stmt_date(acct.getNextStmtDate());
		resp.setPmt_due_date(acct.getPmtDueDate());
		resp.setDd_date(acct.getDdDate());
		resp.setGrace_date(acct.getGraceDate());
		resp.setClosed_date(acct.getClosedDate());
		resp.setFirst_stmt_date(acct.getFirstStmtDate());
		resp.setCancel_date(acct.getCloseDate());
		resp.setCharge_off_date(acct.getChargeOffDate());
		resp.setFirst_purchase_date(acct.getFirstRetlDate());
		resp.setFirst_purchase_amt(acct.getFirstRetlAmt());
		resp.setTot_due_amt(acct.getTotDueAmt());
		LogTools.printLogger(logger, "S20014", "贷款账户信息查询", resp, false);
		return resp;
	}
}
