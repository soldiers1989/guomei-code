package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12000Req;
import com.sunline.ccs.service.protocol.S12000Resp;
import com.sunline.ccs.service.provide.CallOTBProvide;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.CurrencyCodeTools;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQAcctInfo
 * @see 描述：账户信息查询
 *
 * @see 创建日期： 2015-6-25下午2:14:28
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQAcctInfo {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	// @Resource(name="apsQueryService")
	// private APSQueryService apsQueryService;
	@Autowired
	private CommProvide commonProvide;
	@Autowired
	private AcctOTB accountOTB;
	@Autowired
	private CallOTBProvide callOTBProvide;
	@PersistenceContext
	public EntityManager em;

	QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
	QCcsCard qCcsCard = QCcsCard.ccsCard;
	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;
	QCcsTxnHst qCcsTxnHst = QCcsTxnHst.ccsTxnHst;
	QCcsTxnUnstatement qCcsTxnUnstatement = QCcsTxnUnstatement.ccsTxnUnstatement;
	QCcsAuthmemoO qCcsAuthmemoO = QCcsAuthmemoO.ccsAuthmemoO;
	QCcsAcctCrlmtAdjLog qCcsAcctCrlmtAdjLog = QCcsAcctCrlmtAdjLog.ccsAcctCrlmtAdjLog;
	QCcsAuthmemoHst qCcsAuthmemoHst = QCcsAuthmemoHst.ccsAuthmemoHst;

	@Transactional
	public S12000Resp handler(S12000Req req) throws ProcessException {

		LogTools.printLogger(logger, "S12000", "账户信息查询", req, true);
		S12000Resp resp = new S12000Resp();
		// 校验上送报文域
		logger.debug("卡号是：=========="+req.getCard_no());
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);

		// 获取卡片信息
		CcsCard CcsCard;
		CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		logger.debug("校验卡片信息是否存在");
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 获取参数
		Product product = unifiedParameterFacilityProvide.product(CcsCard.getProductCd());
		ProductCredit productCredit = unifiedParameterFacilityProvide.productCredit(CcsCard.getProductCd());
		Date businessDate = unifiedParameterFacilityProvide.BusinessDate();
		AccountAttribute acctAttr = unifiedParameterFacilityProvide.acct_attribute(CcsCard.getProductCd());
		AccountAttribute dualAcctAttr = unifiedParameterFacilityProvide.dual_acct_attribute(CcsCard.getProductCd());

		// 获取账户信息
		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsAcctO CcsAcctO = custAcctCardQueryFacility.getAcctOByAcctNbr(CcsAcct.getAcctType(), CcsAcct.getAcctNbr());
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 获取信用计划
		List<CcsPlan> plans = commonProvide.getCcsPlanByCcsAcct(CcsAcct);

		// 构建响应报文对象
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setProduct_name(product.description);
		resp.setName(CcsAcct.getName());
		resp.setCredit_limit(CcsAcct.getCreditLmt());
		resp.setTemp_limit(CcsAcct.getTempLmt());
		resp.setTemp_limit_begin_date(CcsAcct.getTempLmtBegDate());
		resp.setTemp_limit_end_date(CcsAcct.getTempLmtEndDate());
		resp.setCash_limit_rt(CcsAcct.getCashLmtRate());
		resp.setOvrlmt_rate(CcsAcct.getOvrlmtRate());
		resp.setLoan_limit_rt(CcsAcct.getLoanLmtRate());
		resp.setCurr_bal(CcsAcct.getCurrBal());// 相同字段，以TM_ACCOUNT数据为准
		resp.setCash_bal(CcsAcct.getCashBal());// 相同字段，以TM_ACCOUNT数据为准
		resp.setPrincipal_bal(CcsAcct.getPrincipalBal());
		resp.setLoan_bal(CcsAcct.getLoanBal());// 相同字段，以TM_ACCOUNT数据为准
		resp.setDispute_amt(CcsAcct.getDisputeAmt());// 相同字段，以TM_ACCOUNT数据为准
		resp.setBegin_bal(CcsAcct.getBegBal());
		resp.setPmt_due_day_bal(CcsAcct.getPmtDueDayBal());
		BigDecimal qualGraceBal = commonProvide.getRemainGraceBal(plans).subtract(CcsAcctO.getMemoCr()).setScale(2, BigDecimal.ROUND_HALF_UP);
		if (qualGraceBal.compareTo(new BigDecimal("0")) < 0) {// 全部应还款额小于0按0修正
			qualGraceBal = new BigDecimal("0");
		}
		resp.setQual_grace_bal(qualGraceBal);//
		resp.setGrace_days_full_ind(CcsAcct.getGraceDaysFullInd());
		resp.setSetup_date(CcsAcct.getSetupDate());
		resp.setOwning_branch(CcsAcct.getOwningBranch());
		resp.setBilling_cycle(CcsAcct.getCycleDay());
		resp.setStmt_flag(CcsAcct.getStmtFlag());
		resp.setStmt_mail_addr_ind(CcsAcct.getStmtMailAddrInd());
		resp.setStmt_media_type(CcsAcct.getStmtMediaType());
		resp.setBlock_code(CcsAcct.getBlockCode());// 相同字段，以TM_ACCOUNT数据为准
		resp.setAge_cd(CcsAcct.getAgeCode());
		resp.setUnmatch_db(CcsAcctO.getMemoDb());// 相同字段，以TM_ACCOUNT_O数据为准
		resp.setUnmatch_cash(CcsAcctO.getMemoCash());// 相同字段，以TM_ACCOUNT_O数据为准
		resp.setUnmatch_cr(CcsAcctO.getMemoCr());// 相同字段，以TM_ACCOUNT_O数据为准
		resp.setDual_billing_flag(CcsAcct.getDualBillingFlag());
		resp.setLast_pmt_amt(CcsAcct.getLastPmtAmt());
		resp.setLast_pmt_date(CcsAcct.getLastPmtDate());
		resp.setLast_stmt_date(CcsAcct.getLastStmtDate());
		resp.setNext_stmt_date(CcsAcct.getNextStmtDate());
		resp.setPmt_due_date(CcsAcct.getPmtDueDate());
		resp.setDd_date(CcsAcct.getDdDate());
		resp.setGrace_date(CcsAcct.getGraceDate());
		resp.setClosed_date(CcsAcct.getClosedDate());
		resp.setFirst_stmt_date(CcsAcct.getFirstStmtDate());
		resp.setCancel_date(CcsAcct.getCloseDate());
		resp.setCharge_off_date(CcsAcct.getChargeOffDate());
		resp.setFirst_purchase_date(CcsAcct.getFirstRetlDate());
		resp.setFirst_purchase_amt(CcsAcct.getFirstRetlAmt());
		resp.setTot_due_amt(CcsAcct.getTotDueAmt());
		resp.setAcct_cash_otb(accountOTB.acctCashOTB(CcsAcctO, productCredit, businessDate));
		BigDecimal available_otb = callOTBProvide.realOTB(req.getCard_no(), custAcctCardQueryFacility.getAcctByCardNbr(req.getCard_no())).setScale(2,
				BigDecimal.ROUND_HALF_UP);
		resp.setAvailable_otb(available_otb.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : available_otb);// 当综合可用额度为负时，取0
		resp.setDual_curr_ind(CurrencyCodeTools.isExistOtherCurrCd(req.getCurr_cd(), acctAttr, dualAcctAttr));
		resp.setDual_curr_cd(CurrencyCodeTools.getOtherCurrCd(req.getCurr_cd(), acctAttr, dualAcctAttr));
		resp.setCurr_remain_tot_bal(commonProvide.getRemainGraceBal(plans));
		LogTools.printLogger(logger, "S12000", "账户信息查询", resp, false);
		return resp;

	}

}
