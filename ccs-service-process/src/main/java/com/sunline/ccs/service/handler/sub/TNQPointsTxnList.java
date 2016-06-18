package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S17011Req;
import com.sunline.ccs.service.protocol.S17011Resp;
import com.sunline.ccs.service.protocol.S17011TxnPoint;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQPointsTxnList
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期： 2015-6-25上午11:31:20
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQPointsTxnList {
	@PersistenceContext
	public EntityManager em;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	private Logger logger = LoggerFactory.getLogger(getClass());

	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	QCcsTxnHst qCcsTxnHst = QCcsTxnHst.ccsTxnHst;
	QCcsCard qCcsCard = QCcsCard.ccsCard;

	@Transactional
	public S17011Resp handler(S17011Req req) throws ProcessException {

		// 校验卡号是否合法
		CheckUtil.checkCardNo(req.getCard_no());

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(qCcsTxnHst.acctNbr.eq(qCcsCard.acctNbr))
				.and(qCcsCardLmMapping.cardNbr.eq(req.getCard_no()))
				.and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsTxnHst.points.gt(0)));

		if (req.getStart_date() != null) {
			booleanExpression = booleanExpression.and(qCcsTxnHst.txnDate.goe(req.getStart_date()));
		}
		if (req.getEnd_date() != null) {
			booleanExpression = booleanExpression.and(qCcsTxnHst.txnDate.loe(req.getEnd_date()));
		}
		if (req.getStart_date() != null && req.getEnd_date() != null && DateUtils.truncatedCompareTo(req.getStart_date(), req.getEnd_date(), Calendar.DATE) > 0) {
			throw new ProcessException(Constants.ERRB004_CODE, Constants.ERRB004_MES);
		}

		List<CcsTxnHst> txnHstList = query.from(qCcsCardLmMapping, qCcsCard, qCcsTxnHst).where(booleanExpression).orderBy(qCcsTxnHst.txnTime.desc())
				.offset(req.getFirstrow()).limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsTxnHst);

		ArrayList<S17011TxnPoint> txns = new ArrayList<S17011TxnPoint>();
		for (CcsTxnHst txnHst : txnHstList) {
			S17011TxnPoint txn = new S17011TxnPoint();

			txn.setCurr_cd(txnHst.getAcctType().getCurrencyCode());
			txn.setTxn_date(txnHst.getTxnDate());
			txn.setTxn_time(txnHst.getTxnTime());
			txn.setTxn_code(txnHst.getTxnCode());
			txn.setSett_txn_type(unifiedParameterFacilityProvide.txnType(txnHst.getTxnCode()));
			txn.setTxn_amt(txnHst.getTxnAmt());
			txn.setTxn_curr_cd(txnHst.getTxnCurrency());
			txn.setRef_nbr(txnHst.getRefNbr());
			txn.setTxn_short_desc(txnHst.getTxnShortDesc());
			txn.setPoint(txnHst.getPoints());
			txn.setAcq_acceptor_id(txnHst.getAcqAcceptorId());
			txn.setAcq_name_addr(txnHst.getAcqAddress());

			txns.add(txn);
		}

		JPAQuery queryCount = new JPAQuery(em);
		int totalRows = (int) queryCount.from(qCcsCardLmMapping, qCcsCard, qCcsTxnHst).where(booleanExpression).count();

		// 构建响应报文
		S17011Resp resp = new S17011Resp();
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setCard_no(req.getCard_no());
		resp.setTxn_points(txns);

		LogTools.printLogger(logger, "S17011", "积分明细查询", resp, false);
		return resp;

	}

}
