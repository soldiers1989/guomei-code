package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.Calendar;
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
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20050Req;
import com.sunline.ccs.service.protocol.S20050Resp;
import com.sunline.ccs.service.protocol.S20050Txn;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcTxnHst
 * @see 描述： 贷款交易历史查询
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcTxnHst {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	@PersistenceContext
	private EntityManager em;
	private QCcsTxnHst qCcsTxnHst = QCcsTxnHst.ccsTxnHst;

	/**
	 * @see 方法名：handler
	 * @see 描述：贷款交易历史查询handler
	 * @see 创建日期：2015年6月26日上午10:57:01
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
	public S20050Resp handler(S20050Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20050", "贷款交易历史查询", req, true);
		S20050Resp resp = new S20050Resp();

		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());

		// 获取卡片信息
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression;

		// 主卡以账户为单位
		if (CcsCard.getBscSuppInd() == BscSuppIndicator.B) {
			booleanExpression = qCcsTxnHst.acctNbr.eq(CcsCard.getAcctNbr());
		} else {// 附卡以本卡为单位
			booleanExpression = qCcsTxnHst.logicCardNbr.eq(CcsCard.getLogicCardNbr());
		}

		if (req.getStart_date() != null) {
			booleanExpression = booleanExpression.and(qCcsTxnHst.postDate.goe(req.getStart_date()));
		}

		if (req.getEnd_date() != null) {
			booleanExpression = booleanExpression.and(qCcsTxnHst.postDate.loe(req.getEnd_date()));
		}

		if (req.getStart_date() != null && req.getEnd_date() != null && DateUtils.truncatedCompareTo(req.getStart_date(), req.getEnd_date(), Calendar.DATE) > 0) {
			throw new ProcessException(Constants.ERRB004_CODE, Constants.ERRB004_MES);
		}

		booleanExpression = booleanExpression.and(qCcsTxnHst.acctType.eq(AccountType.E));

		// 如果交易类型不为空则作为查询条件
		if (req.getSett_txn_type() != null) {
			booleanExpression = booleanExpression.and(qCcsTxnHst.txnCode.in(unifiedParameterFacilityProvide.txnCode(req.getSett_txn_type())));
		}

		List<CcsTxnHst> txnHstList = query.from(qCcsTxnHst).where(booleanExpression).orderBy(qCcsTxnHst.txnDate.desc(), qCcsTxnHst.cardNbr.asc())
				.offset(req.getFirstrow()).limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsTxnHst);
		ArrayList<S20050Txn> txns = new ArrayList<S20050Txn>();

		if (txnHstList != null) {
			for (CcsTxnHst tmTxnHst : txnHstList) {
				S20050Txn txn = new S20050Txn();
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
				txn.setAcq_acceptor_id(tmTxnHst.getAcqAcceptorId());
				txn.setAcq_name_addr(tmTxnHst.getAcqAddress());

				txns.add(txn);
			}
		}
		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsTxnHst).where(booleanExpression).count();

		// 构建响应报文对象
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setCard_no(req.getCard_no());
		resp.setTxns(txns);

		LogTools.printLogger(logger, "S20050", "贷款交易历史查询", resp, false);
		return resp;
	}
}
