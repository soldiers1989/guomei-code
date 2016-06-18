/**
 * 
 */
package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12010Req;
import com.sunline.ccs.service.protocol.S12010Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.CurrencyCodeTools;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQStmt
 * @see 描述：账单汇总信息查询
 *
 * @see 创建日期： 2015-6-25下午2:33:37
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQStmt {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private CommProvide commonProvide;
	@PersistenceContext
	public EntityManager em;
	QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;

	@Transactional
	public S12010Resp handler(S12010Req req) throws ProcessException {

		LogTools.printLogger(logger, "S12010", "账单汇总信息查询", req, true);
		S12010Resp resp = new S12010Resp();

		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);
		CheckUtil.checkStmtDate(req.getStmt_date(), true);

		// 获取卡片信息
		CcsCard CcsCard;
		CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 获取参数
		AccountAttribute acctAttr = unifiedParameterFacilityProvide.acct_attribute(CcsCard.getProductCd());
		AccountAttribute dualAcctAttr = unifiedParameterFacilityProvide.dual_acct_attribute(CcsCard.getProductCd());
		AccountType accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(req.getCard_no(), req.getCurr_cd());
		Date businessDate = unifiedParameterFacilityProvide.BusinessDate();

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsStatement.acctNbr.eq(CcsCard.getAcctNbr()).and(qCcsStatement.acctType.eq(accountType));

		// 若"000000"则本期(最近一期)，即非本期则增加时间区间条件
		if (!Constants.DEFAULT_STMT_DATE.equals(req.getStmt_date())) {
			Date startDate = DateTools.parseStmtDate(req.getStmt_date());
			Date endDate = DateUtils.addMonths(startDate, 1);
			booleanExpression = booleanExpression.and(qCcsStatement.stmtDate.goe(startDate)).and(qCcsStatement.stmtDate.lt(endDate));
		}

		CcsStatement ccsStatement = query.from(qCcsStatement).where(booleanExpression).orderBy(qCcsStatement.stmtDate.desc()).singleResult(qCcsStatement);
		CheckUtil.rejectNull(ccsStatement, Constants.ERRB068_CODE, Constants.ERRB068_MES);
		// 判断临额是否有效
		BigDecimal creditLmt = ccsStatement.getCreditLmt();
		if (ccsStatement.getTempLmtBegDate() != null && ccsStatement.getTempLmtEndDate() != null
				&& businessDate.compareTo(ccsStatement.getTempLmtBegDate()) >= 0 && businessDate.compareTo(ccsStatement.getTempLmtEndDate()) <= 0) {
			creditLmt = ccsStatement.getTempLmt();
		}

		// 构建响应报文对象
		resp.setCard_no(ccsStatement.getDefaultLogicCardNbr());
		resp.setCurr_cd(ccsStatement.getCurrency());
		resp.setStmt_date(ccsStatement.getStmtDate());
		resp.setBilling_date(ccsStatement.getStmtDate());
		resp.setName(ccsStatement.getName());
		resp.setPmt_due_date(ccsStatement.getPmtDueDate());
		resp.setCredit_limit(ccsStatement.getCreditLmt());
		resp.setCash_limit(creditLmt.multiply(acctAttr.cashLimitRate).setScale(2, BigDecimal.ROUND_HALF_UP));
		resp.setTemp_limit(ccsStatement.getTempLmt());
		resp.setTemp_limit_begin_date(ccsStatement.getTempLmtBegDate());
		resp.setTemp_limit_end_date(ccsStatement.getTempLmtEndDate());
		resp.setLast_stmt_date(ccsStatement.getLastStmtDate());
		resp.setStmt_beg_bal(ccsStatement.getLastStmtBal());
		resp.setStmt_curr_bal((ccsStatement.getQualGraceBal().compareTo(BigDecimal.ZERO) == 0 && ccsStatement.getStmtCurrBal().compareTo(BigDecimal.ZERO) < 0) ? ccsStatement
				.getStmtCurrBal() : ccsStatement.getQualGraceBal());
		resp.setCtd_cash_amt(ccsStatement.getCtdCashAmt());
		// resp.setQual_grace_bal(ccsStatement.getQualGraceBal());
		BigDecimal qualGraceBal = ccsStatement.getQualGraceBal();// 全部应还款额小于0按0修正
		resp.setQual_grace_bal(qualGraceBal.compareTo(new BigDecimal("0")) < 0 ? new BigDecimal("0") : qualGraceBal);
		resp.setTot_due_amt(ccsStatement.getTotDueAmt());
		resp.setCtd_amt_db(ccsStatement.getCtdAmtDb());
		resp.setCtd_nbr_db(ccsStatement.getCtdNbrDb());
		resp.setCtd_amt_cr(ccsStatement.getCtdAmtCr());
		resp.setCtd_nbr_cr(ccsStatement.getCtdNbrCr());
		resp.setAge_cd(ccsStatement.getAgeCode());
		resp.setGrace_days_full_ind(ccsStatement.getGraceDaysFullInd());
		resp.setPoint_begin_bal(ccsStatement.getPointsBegBal().intValue());
		resp.setCtd_earned_points(ccsStatement.getCtdPoints().intValue());
		resp.setCtd_adj_points(ccsStatement.getCtdAdjPoints().intValue());
		resp.setCtd_disb_points(ccsStatement.getCtdSpendPoints().intValue());
		resp.setPoint_bal(ccsStatement.getPointsBal().intValue());
		resp.setDual_curr_ind(CurrencyCodeTools.isExistOtherCurrCd(req.getCurr_cd(), acctAttr, dualAcctAttr));
		resp.setDual_curr_cd(CurrencyCodeTools.getOtherCurrCd(req.getCurr_cd(), acctAttr, dualAcctAttr));
		/**
		 * 2014-8-25 谢晓斌制定方案如下：
		 * 本期账单已还金额、本期账单未还金额参照12000交易中全部应还款额计算方法，全部应还款额-当期所有贷记交易交易之和
		 * 查询最近一期账单返回上述值，往期账单全返回0 FIXME 对于修改账单日的情况以后再优化
		 */

		Date nextStmtDate = DateUtils.addMonths(ccsStatement.getStmtDate(), 1);
		// 判断查询的账单是否为最近一期
		if (businessDate.compareTo(ccsStatement.getStmtDate()) > 0 && businessDate.compareTo(nextStmtDate) < 0) {
			// 此处不需要对acct判空
			CcsAcct acct = custAcctCardQueryFacility.getAcctByAcctNbr(ccsStatement.getAcctType(), ccsStatement.getAcctNbr());
			CcsAcctO accto = custAcctCardQueryFacility.getAcctOByAcctNbr(ccsStatement.getAcctType(), ccsStatement.getAcctNbr());
			List<CcsPlan> plans = commonProvide.getCcsPlanByCcsAcct(acct);
			BigDecimal pmtNotAmt = commonProvide.getRemainGraceBal(plans).subtract(accto.getMemoCr()).setScale(2, BigDecimal.ROUND_HALF_UP);
			if (pmtNotAmt.compareTo(BigDecimal.ZERO) < 0) {
				pmtNotAmt = BigDecimal.ZERO;
			}

			BigDecimal pmtYetAmt = acct.getQualGraceBal().subtract(pmtNotAmt).setScale(2, BigDecimal.ROUND_HALF_UP);
			resp.setCtd_pmt_yet_amt(pmtYetAmt);
			resp.setCtd_pmt_not_amt(pmtNotAmt);
		} else {
			resp.setCtd_pmt_yet_amt(new BigDecimal("0"));
			resp.setCtd_pmt_not_amt(new BigDecimal("0"));
		}
		LogTools.printLogger(logger, "S12010", "账单汇总信息查询", resp, false);
		return resp;

	}

}
