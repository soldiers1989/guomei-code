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
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13060Req;
import com.sunline.ccs.service.protocol.S13060Resp;
import com.sunline.ccs.service.protocol.S13060Txn;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQUnstmtTxnList
 * @see 描述：未出账单交易明细查询
 *
 * @see 创建日期： 2015-6-25下午4:42:40
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQUnstmtTxnList {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@PersistenceContext
	public EntityManager em;
	QCcsTxnUnstatement qCcsTxnUnstatement = QCcsTxnUnstatement.ccsTxnUnstatement;

	@Transactional
	public S13060Resp handler(S13060Req req) throws ProcessException {

		LogTools.printLogger(logger, "S13060", "未出账单交易明细查询", req, true);
		S13060Resp resp = new S13060Resp();
		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);

		// 获取卡片信息
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression;
		// 主卡以账户为单位
		if (CcsCard.getBscSuppInd() == BscSuppIndicator.B) {
			booleanExpression = qCcsTxnUnstatement.acctNbr.eq(CcsCard.getAcctNbr());
		}
		// 附卡以本卡为单位
		else {
			booleanExpression = qCcsTxnUnstatement.logicCardNbr.eq(CcsCard.getLogicCardNbr());
		}

		if (!Constants.DEFAULT_CURR_CD.equals(req.getCurr_cd())) {
			AccountType accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(req.getCard_no(), req.getCurr_cd());
			booleanExpression = booleanExpression.and(qCcsTxnUnstatement.acctType.eq(accountType));
		}
		if (req.getStart_date() != null) {
			booleanExpression = booleanExpression.and(qCcsTxnUnstatement.postDate.goe(req.getStart_date()));
		}
		if (req.getEnd_date() != null) {
			booleanExpression = booleanExpression.and(qCcsTxnUnstatement.postDate.loe(req.getEnd_date()));
		}
		if (req.getStart_date() != null && req.getEnd_date() != null && DateUtils.truncatedCompareTo(req.getStart_date(), req.getEnd_date(), Calendar.DATE) > 0) {
			throw new ProcessException(Constants.ERRB004_CODE, Constants.ERRB004_MES);
		}
		// 如果交易类型不为空则作为查询条件
		if (req.getSett_txn_type() != null)
			booleanExpression = booleanExpression.and(qCcsTxnUnstatement.txnCode.in(unifiedParameterFacilityProvide.txnCode(req.getSett_txn_type())));

		List<CcsTxnUnstatement> tmTxnUnstmtList = query.from(qCcsTxnUnstatement).where(booleanExpression)
				.orderBy(qCcsTxnUnstatement.txnDate.desc(), qCcsTxnUnstatement.cardNbr.asc()).offset(req.getFirstrow())
				.limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsTxnUnstatement);
		ArrayList<S13060Txn> txns = new ArrayList<S13060Txn>();

		if (tmTxnUnstmtList != null) {
			for (CcsTxnUnstatement tmTxnUnstmt : tmTxnUnstmtList) {
				S13060Txn txn = new S13060Txn();
				txn.setTxn_card_no(tmTxnUnstmt.getCardNbr());
				txn.setTxn_date(tmTxnUnstmt.getTxnDate());
				txn.setTxn_time(tmTxnUnstmt.getTxnTime());
				txn.setTxn_code(tmTxnUnstmt.getTxnCode());
				txn.setSett_txn_type(unifiedParameterFacilityProvide.txnType(tmTxnUnstmt.getTxnCode()));
				txn.setTxn_amt(tmTxnUnstmt.getTxnAmt());
				txn.setPost_amt(tmTxnUnstmt.getPostAmt());
				txn.setPost_curr_cd(tmTxnUnstmt.getPostCurrency());
				txn.setPost_date(tmTxnUnstmt.getPostDate());
				txn.setAuth_code(tmTxnUnstmt.getAuthCode());
				txn.setTxn_curr_cd(tmTxnUnstmt.getTxnCurrency());
				txn.setRef_nbr(tmTxnUnstmt.getRefNbr());
				txn.setTxn_short_desc(tmTxnUnstmt.getTxnShortDesc());
				txn.setAcq_acceptor_id(tmTxnUnstmt.getAcqAcceptorId());
				txn.setAcq_name_addr(tmTxnUnstmt.getAcqAddress());

				txns.add(txn);
			}
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsTxnUnstatement).where(booleanExpression).count();

		// 构建响应报文对象
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setTxns(txns);

		LogTools.printLogger(logger, "S13060", "未出账单交易明细查询", resp, false);
		return resp;

	}

}
