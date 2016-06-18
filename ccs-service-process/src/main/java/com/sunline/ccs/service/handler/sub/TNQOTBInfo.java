/**
 * 
 */
package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;

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
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.otb.CustOTB;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S15020Req;
import com.sunline.ccs.service.protocol.S15020Resp;
import com.sunline.ccs.service.provide.CallOTBProvide;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.CurrencyCodeTools;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQOTBInfo
 * @see 描述：可用额度信息查询
 *
 * @see 创建日期： 2015-6-25下午2:18:43
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQOTBInfo {
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
	private CustOTB customerOTB;
	@Autowired
	private AcctOTB accountOTB;
	@Autowired
	private CallOTBProvide callOTBProvide;
	@PersistenceContext
	public EntityManager em;

	@Transactional
	public S15020Resp handler(S15020Req req) throws ProcessException {

		LogTools.printLogger(logger, "S15020", "可用额度信息查询", req, true);
		S15020Resp resp = new S15020Resp();
		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);

		// 获取卡片信息
		CcsCard card = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(card, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 获取账户信息
		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRS001_CODE, Constants.ERRS001_MES);
		CcsAcctO CcsAcctO = custAcctCardQueryFacility.getAcctOByAcctNbr(CcsAcct.getAcctType(), CcsAcct.getAcctNbr());
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 获取客户信息
		CcsCustomer ccsCustomer = custAcctCardQueryFacility.getCustomerByCardNbr(card.getCardBasicNbr());
		CheckUtil.rejectNull(ccsCustomer, Constants.ERRS001_CODE, Constants.ERRS001_MES);
		CcsCustomerCrlmt custLmtO = custAcctCardQueryFacility.getCustomerCrLmtByCustLmtId(ccsCustomer.getCustLmtId());
		CheckUtil.rejectNull(custLmtO, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 获取参数
		ProductCredit productCredit = unifiedParameterFacilityProvide.productCredit(card.getProductCd());
		AccountAttribute acctAttr = unifiedParameterFacilityProvide.acct_attribute(card.getProductCd());
		AccountAttribute dualAcctAttr = unifiedParameterFacilityProvide.dual_acct_attribute(card.getProductCd());
		Date businessDate = unifiedParameterFacilityProvide.BusinessDate();

		// 计算OTBk
		BigDecimal custOtb = customerOTB.customerOTB(req.getCard_no(), businessDate);
		BigDecimal acctOtb = accountOTB.acctOTB(CcsAcctO, productCredit, businessDate);

		BigDecimal creditLmt = CcsAcctO.getCreditLmt();

		// 临时额度是否有效
		if (CcsAcctO.getTempLmtBegDate() != null && CcsAcctO.getTempLmtEndDate() != null && businessDate.compareTo(CcsAcctO.getTempLmtBegDate()) >= 0
				&& businessDate.compareTo(CcsAcctO.getTempLmtEndDate()) <= 0) {
			creditLmt = CcsAcctO.getTempLmt();
		}
		BigDecimal ovrLmtRate = commonProvide.getOvrlmtRate(CcsAcctO, productCredit, businessDate);
		BigDecimal cashLmtRt = commonProvide.getCashLmtRate(CcsAcctO, productCredit, businessDate);
		BigDecimal loanLmtRt = commonProvide.getLoanLmtRate(CcsAcctO, productCredit, businessDate);

		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setCust_limit(custLmtO.getCreditLmt());
		resp.setCust_otb(custOtb);
		resp.setCust_cash_limit(custLmtO.getCreditLmt());// 临时方案
		resp.setCust_cash_otb(custOtb);// 临时方案
		resp.setAcct_limit(CcsAcctO.getCreditLmt());
		resp.setAcct_otb(acctOtb);
		resp.setAcct_cash_limit(creditLmt.multiply(cashLmtRt).setScale(2, BigDecimal.ROUND_HALF_UP));
		resp.setAcct_cash_otb(accountOTB.acctCashOTB(CcsAcctO, productCredit, businessDate));
		resp.setAcct_loan_limit(creditLmt.multiply(BigDecimal.ONE.add(ovrLmtRate)).multiply(loanLmtRt).setScale(2, BigDecimal.ROUND_HALF_UP));
		// resp.setAcct_loan_otb();//TODO 目前账单分期、消费分期都不需要做分期，暂空

		// 临时额度是否有效
		if (CcsAcctO.getTempLmtBegDate() != null && CcsAcctO.getTempLmtEndDate() != null && businessDate.compareTo(CcsAcctO.getTempLmtBegDate()) >= 0
				&& businessDate.compareTo(CcsAcctO.getTempLmtEndDate()) <= 0) {
			resp.setAcct_temp_limit(CcsAcctO.getTempLmt());
			resp.setAcct_temp_limit_end_date(CcsAcctO.getTempLmtEndDate());
		} else {
			resp.setAcct_temp_limit(BigDecimal.ZERO);
			resp.setAcct_temp_limit_end_date(null);
		}

		// 综合可用额度 = 取小(客户层可用额度, 账户层可用额度)
		resp.setAvailable_otb(callOTBProvide.realOTB(req.getCard_no(), custAcctCardQueryFacility.getAcctByCardNbr(req.getCard_no())));

		// 是否有另外币种及另外币币种
		resp.setDual_curr_ind(CurrencyCodeTools.isExistOtherCurrCd(req.getCurr_cd(), acctAttr, dualAcctAttr));
		resp.setDual_curr_cd(CurrencyCodeTools.getOtherCurrCd(req.getCurr_cd(), acctAttr, dualAcctAttr));

		LogTools.printLogger(logger, "S15020", "可用额度信息查询", resp, false);
		return resp;

	}

}
