package com.sunline.ccs.service.handler.sub;

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
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12011Req;
import com.sunline.ccs.service.protocol.S12011Resp;
import com.sunline.ccs.service.protocol.S12011Txn;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQStmtTxnList
 * @see 描述：账单交易明细查询
 *
 * @see 创建日期： 2015-6-25下午4:29:59
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQStmtTxnList {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@PersistenceContext
	public EntityManager em;
	QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;
	QCcsTxnHst qCcsTxnHst = QCcsTxnHst.ccsTxnHst;

	@Transactional
	public S12011Resp handler(S12011Req req) throws ProcessException {

		LogTools.printLogger(logger, "S12011", "账单交易明细查询", req, true);
		// 构建响应报文对象
		S12011Resp resp = new S12011Resp();

		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);
		CheckUtil.checkStmtDate(req.getStmt_date(), true);

		// 获取卡片信息
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 查询账单日
		JPAQuery queryStmtDate = new JPAQuery(em);
		BooleanExpression booleanExpressionStmtDate = qCcsStatement.acctNbr.eq(CcsCard.getAcctNbr());

		// 若"000000"则本期(最近一期)，即非本期则增加时间区间条件
		if (!Constants.DEFAULT_STMT_DATE.equals(req.getStmt_date())) {
			Date startDate = DateTools.parseStmtDate(req.getStmt_date());
			Date endDate = DateUtils.addMonths(startDate, 1);
			booleanExpressionStmtDate = booleanExpressionStmtDate.and(qCcsStatement.stmtDate.goe(startDate)).and(qCcsStatement.stmtDate.lt(endDate));
		}

		// 获取预查询的账单日
		CcsStatement ccsStatement = queryStmtDate.from(qCcsStatement).where(booleanExpressionStmtDate).orderBy(qCcsStatement.stmtDate.desc())
				.singleResult(qCcsStatement);
		CheckUtil.rejectNull(ccsStatement, Constants.ERRB068_CODE, Constants.ERRB068_MES);

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsTxnHst.acctNbr.eq(CcsCard.getAcctNbr()).and(qCcsTxnHst.stmtDate.eq(ccsStatement.getStmtDate()))
				.and(qCcsTxnHst.txnCode.in(unifiedParameterFacilityProvide.OutStmtTxnCode()));
		// 如果交易类型不为空则作为查询条件
		if (req.getSett_txn_type() != null)
			booleanExpression = booleanExpression.and(qCcsTxnHst.txnCode.in(unifiedParameterFacilityProvide.txnCode(req.getSett_txn_type())));

		// 若"000"则全币种，即非全币种则增加币种对应的账户类型条件
		if (!Constants.DEFAULT_CURR_CD.equals(req.getCurr_cd())) {
			AccountType accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(req.getCard_no(), req.getCurr_cd());
			booleanExpression = booleanExpression.and(qCcsTxnHst.acctType.eq(accountType));
		}

		List<CcsTxnHst> txnHstList = query.from(qCcsTxnHst).where(booleanExpression).orderBy(qCcsTxnHst.txnDate.asc(), qCcsTxnHst.cardNbr.desc())
				.offset(req.getFirstrow()).limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsTxnHst);

		ArrayList<S12011Txn> txns = new ArrayList<S12011Txn>();
		for (CcsTxnHst tmTxnHst : txnHstList) {
			S12011Txn txn = new S12011Txn();
			txn.setTxn_card_no(tmTxnHst.getCardNbr());
			txn.setTxn_date(tmTxnHst.getTxnDate());
			txn.setTxn_time(tmTxnHst.getTxnTime());
			txn.setTxn_code(tmTxnHst.getTxnCode());
			txn.setSett_txn_type(unifiedParameterFacilityProvide.txnType(tmTxnHst.getTxnCode()));
			txn.setTxn_amt(tmTxnHst.getTxnAmt());
			txn.setPost_amt(tmTxnHst.getPostAmt());
			txn.setPost_curr_cd(tmTxnHst.getPostCurrency());
			txn.setPost_date(tmTxnHst.getPostDate());
			txn.setAuth_code(tmTxnHst.getAuthCode());
			txn.setTxn_curr_cd(tmTxnHst.getTxnCurrency());
			txn.setRef_nbr(tmTxnHst.getRefNbr());
			txn.setTxn_short_desc(tmTxnHst.getTxnShortDesc());
			txn.setPoint(tmTxnHst.getPoints().intValue());
			txn.setAcq_acceptor_id(tmTxnHst.getAcqAcceptorId());
			txn.setAcq_name_addr(tmTxnHst.getAcqAddress());

			txns.add(txn);
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsTxnHst).where(booleanExpression).count();

		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setCard_no(req.getCard_no());
		resp.setStmt_date(req.getStmt_date());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setTxns(txns);

		LogTools.printLogger(logger, "S12011", "账单交易明细查询", resp, false);
		return resp;

	}

}
