/**
 * 
 */
package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12050Req;
import com.sunline.ccs.service.protocol.S12050Resp;
import com.sunline.ccs.service.protocol.S12050Stmt;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.CurrencyCodeTools;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQStatementList
 * @see 描述：账单列表查询
 *
 * @see 创建日期： 2015-6-25下午2:29:23
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQStatementList {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@PersistenceContext
	public EntityManager em;
	QCcsCard qCcsCard = QCcsCard.ccsCard;
	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;

	@Transactional
	public S12050Resp handler(S12050Req req) throws ProcessException {

		LogTools.printLogger(logger, "S12050", "账单列表查询", req, true);

		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);
		// 获取卡片信息
		CcsCard CcsCard;
		CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 起始日期不能大于截止日期
		if (req.getStmt_start_date() != null && req.getStmt_end_date() != null
				&& Integer.parseInt(req.getStmt_start_date()) > Integer.parseInt(req.getStmt_end_date())) {
			throw new ProcessException(Constants.ERRB004_CODE, Constants.ERRB004_MES);
		}

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(qCcsStatement.acctNbr.eq(qCcsCard.acctNbr))
				.and(qCcsCardLmMapping.cardNbr.eq(req.getCard_no()));

		if (req.getStmt_start_date() != null) {
			booleanExpression = booleanExpression.and(qCcsStatement.stmtDate.goe(DateTools.parseStmtDate(req.getStmt_start_date())));
		}
		if (req.getStmt_end_date() != null) {
			booleanExpression = booleanExpression.and(qCcsStatement.stmtDate.lt(DateUtils.addMonths(DateTools.parseStmtDate(req.getStmt_end_date()), 1)));
		}
		if (!Constants.DEFAULT_CURR_CD.equals(req.getCurr_cd())) {
			AccountType accountType;
			accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(req.getCard_no(), req.getCurr_cd());
			booleanExpression = booleanExpression.and(qCcsStatement.acctType.eq(accountType));
		}
		// 获取参数
		AccountAttribute acctAttr = unifiedParameterFacilityProvide.acct_attribute(CcsCard.getProductCd());
		AccountAttribute dualAcctAttr = unifiedParameterFacilityProvide.dual_acct_attribute(CcsCard.getProductCd());

		List<CcsStatement> ccsStatementList = query.from(qCcsCardLmMapping, qCcsCard, qCcsStatement).where(booleanExpression)
				.orderBy(qCcsStatement.stmtDate.desc()).offset(req.getFirstrow()).limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow()))
				.list(qCcsStatement);

		ArrayList<S12050Stmt> stmts = new ArrayList<S12050Stmt>();
		Date businessDate = unifiedParameterFacilityProvide.BusinessDate();
		for (CcsStatement ccsStatement : ccsStatementList) {
			// 判断临额是否有效
			BigDecimal creditLmt = ccsStatement.getCreditLmt();
			if (ccsStatement.getTempLmtBegDate() != null && ccsStatement.getTempLmtEndDate() != null
					&& businessDate.compareTo(ccsStatement.getTempLmtBegDate()) >= 0 && businessDate.compareTo(ccsStatement.getTempLmtEndDate()) <= 0) {
				creditLmt = ccsStatement.getTempLmt();
			}
			S12050Stmt stmt = new S12050Stmt();
			stmt.setCard_no(ccsStatement.getDefaultLogicCardNbr());
			stmt.setCurr_cd(ccsStatement.getCurrency());
			stmt.setStmt_date(ccsStatement.getStmtDate());
			stmt.setBilling_date(ccsStatement.getStmtDate());
			stmt.setName(ccsStatement.getName());
			stmt.setPmt_due_date(ccsStatement.getPmtDueDate());
			stmt.setCredit_limit(ccsStatement.getCreditLmt());
			stmt.setCash_limit(creditLmt.multiply(acctAttr.cashLimitRate).setScale(2, BigDecimal.ROUND_HALF_UP));
			stmt.setTemp_limit(ccsStatement.getTempLmt());
			stmt.setTemp_limit_begin_date(ccsStatement.getTempLmtBegDate());
			stmt.setTemp_limit_end_date(ccsStatement.getTempLmtEndDate());
			stmt.setLast_stmt_date(ccsStatement.getLastStmtDate());
			stmt.setStmt_beg_bal(ccsStatement.getLastStmtBal());
			stmt.setStmt_curr_bal((ccsStatement.getQualGraceBal().compareTo(BigDecimal.ZERO) == 0 && ccsStatement.getStmtCurrBal().compareTo(BigDecimal.ZERO) < 0) ? ccsStatement
					.getStmtCurrBal() : ccsStatement.getQualGraceBal());
			stmt.setCtd_cash_amt(ccsStatement.getCtdCashAmt());
			// stmt.setQual_grace_bal(ccsStatement.getQualGraceBal());
			BigDecimal qualGraceBal = ccsStatement.getQualGraceBal();// 全部应还款额小于0按0修正
			stmt.setQual_grace_bal(qualGraceBal.compareTo(new BigDecimal("0")) < 0 ? new BigDecimal("0") : qualGraceBal);
			stmt.setTot_due_amt(ccsStatement.getTotDueAmt());
			stmt.setCtd_amt_db(ccsStatement.getCtdAmtDb());
			stmt.setCtd_nbr_db(ccsStatement.getCtdNbrDb());
			stmt.setCtd_amt_cr(ccsStatement.getCtdAmtCr());
			stmt.setCtd_nbr_cr(ccsStatement.getCtdNbrCr());
			stmt.setAge_cd(ccsStatement.getAgeCode());
			stmt.setGrace_days_full_ind(ccsStatement.getGraceDaysFullInd());
			stmt.setPoint_begin_bal(ccsStatement.getPointsBegBal().intValue());
			stmt.setCtd_earned_points(ccsStatement.getCtdPoints().intValue());
			stmt.setCtd_adj_points(ccsStatement.getCtdAdjPoints().intValue());
			stmt.setCtd_disb_points(ccsStatement.getCtdSpendPoints().intValue());
			stmt.setPoint_bal(ccsStatement.getPointsBal().intValue());
			stmt.setDual_curr_ind(CurrencyCodeTools.isExistOtherCurrCd(req.getCurr_cd(), acctAttr, dualAcctAttr));
			stmt.setDual_curr_cd(CurrencyCodeTools.getOtherCurrCd(req.getCurr_cd(), acctAttr, dualAcctAttr));
			stmts.add(stmt);
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsCardLmMapping, qCcsCard, qCcsStatement).where(booleanExpression).count();

		// 构建响应报文对象
		S12050Resp resp = new S12050Resp();
		resp.setTotal_rows(totalRows);
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setStmts(stmts);

		LogTools.printLogger(logger, "S12050", "账单列表查询", resp, false);
		return resp;

	}

}
