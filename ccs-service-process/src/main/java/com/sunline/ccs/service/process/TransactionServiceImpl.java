package com.sunline.ccs.service.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.sunline.ark.support.service.QueryRequest;
import com.sunline.ark.support.service.QueryResult;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoInqLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoInqLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.service.util.CPSServProBusUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.ccs.service.util.ExpressionTools;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.api.CcTransactionService;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;


/** 
 * @see 类名：TransactionServiceImpl
 * @see 描述：交易类服务接口
 *
 * @see 创建日期：   2015年6月24日 下午2:48:37
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TransactionServiceImpl implements CcTransactionService {

	QCcsAuthmemoO qCcsAuthmemoO = QCcsAuthmemoO.ccsAuthmemoO;
	QCcsAuthmemoInqLog qCcsAuthmemoInqLog = QCcsAuthmemoInqLog.ccsAuthmemoInqLog;
	QCcsAuthmemoHst qCcsAuthmemoHst = QCcsAuthmemoHst.ccsAuthmemoHst;
	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
			;
	QCcsCard qCcsCard = QCcsCard.ccsCard;
	QCcsTxnUnstatement qCcsTxnUnstatement = QCcsTxnUnstatement.ccsTxnUnstatement;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	QCcsTxnHst qCcsTxnHst = QCcsTxnHst.ccsTxnHst;

	@PersistenceContext
	private EntityManager em;

	
	@Autowired
	private RCcsTxnAdjLog rCcsTxnAdjLog;

	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
	

	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF4103(QueryRequest queryRequest, String cardAccountIndicator, String cardNbr, Date startDate, Date endDate) throws ProcessException {
		log.info("NF4103:卡号[" + CodeMarkUtils.subCreditCard(cardNbr) + "],交易卡帐指示[" + cardAccountIndicator + "]," + "开始日期[" + startDate + "],结束日期[" + endDate + "]");
		CheckUtil.checkCardNo(cardNbr);
		CheckUtil.rejectNull(cardAccountIndicator, "交易卡帐指示不能为空");
		List<CcsAuthmemoHst> tmAuthHstList = null;
		int totalRow;
		if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_A)) {
			tmAuthHstList = NF4103_A(queryRequest, cardNbr, startDate, endDate);
			totalRow = countNF4103_A(cardNbr, startDate, endDate);
		} else if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_C)) {
			tmAuthHstList = NF4103_C(queryRequest, cardNbr, startDate, endDate);
			totalRow = countNF4103_C(cardNbr, startDate, endDate);
		} else {
			throw new ProcessException("无效的交易卡帐指示");
		}
		List<Map<String, Serializable>> tmAuthHstMaps = new ArrayList<Map<String, Serializable>>();
		for (CcsAuthmemoHst tmAuthHst : tmAuthHstList) {
			tmAuthHstMaps.add(tmAuthHst.convertToMap());
		}
		return CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRow, tmAuthHstMaps);
	}

	// 未出账单查询
	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF4104(QueryRequest queryRequest, String cardNbr, String cardAccountIndicator, String currencyCd) throws ProcessException {
		log.info("NF4104:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],交易卡帐指示[" + cardAccountIndicator + "]");
		CheckUtil.checkCardNo(cardNbr);
		CheckUtil.rejectNull(cardAccountIndicator, "交易卡帐指示不能为空");
		AccountType accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(cardNbr, currencyCd);
		List<CcsTxnUnstatement> tmTxnUnstmtList = null;
		int totalRow;
		if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_A)) {
			tmTxnUnstmtList = NF4104_A(queryRequest, cardNbr, accountType);
			totalRow = countNF4104_A(cardNbr, accountType);
		} else if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_C)) {
			tmTxnUnstmtList = NF4104_C(queryRequest, cardNbr, accountType);
			totalRow = countNF4104_C(cardNbr, accountType);
		} else {
			throw new ProcessException("无效的交易卡帐指示");
		}
		List<Map<String, Serializable>> tmTxnUnstmtMaps = new ArrayList<Map<String, Serializable>>();
		for (CcsTxnUnstatement tmTxnUnstmt : tmTxnUnstmtList) {
			tmTxnUnstmtMaps.add(tmTxnUnstmt.convertToMap());
		}
		return CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRow, tmTxnUnstmtMaps);
	}

	/**
	 * 根据卡号获取该卡片未出账单交易列表
	 * 
	 * @param queryRequest
	 * @param cardNbr
	 * @param accountType
	 *            TODO
	 * @return
	 * @throws ProcessException
	 */
	private List<CcsTxnUnstatement> NF4104_C(QueryRequest queryRequest, String cardNbr, AccountType accountType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		return query.from(qCcsTxnUnstatement, qCcsCardLmMapping).orderBy(qCcsTxnUnstatement.acctNbr.asc()).where(genExpressionNF4104_C(cardNbr, accountType)).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow()).list(qCcsTxnUnstatement);
	}

	/**
	 * 根据卡号获取该卡片未出账单交易总数
	 * 
	 * @param cardNbr
	 * @param accountType
	 *            TODO
	 * @return
	 * @throws ProcessException
	 */
	private int countNF4104_C(String cardNbr, AccountType accountType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		int totalRow = (int) query.from(qCcsTxnUnstatement, qCcsCardLmMapping).orderBy(qCcsTxnUnstatement.acctNbr.asc()).where(genExpressionNF4104_C(cardNbr, accountType)).count();
		return totalRow;
	}

	/**
	 * 生成NF4104_C查询表达式
	 * 
	 * @param cardNbr
	 * @param accountType
	 *            TODO
	 * @return
	 * @throws ProcessException
	 */
	private BooleanExpression genExpressionNF4104_C(String cardNbr, AccountType accountType) throws ProcessException {
		return qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsTxnUnstatement.logicCardNbr))).and(qCcsTxnUnstatement.acctType.eq(accountType))
				.and(qCcsTxnUnstatement.txnCode.in(unifiedParameterFacilityProvide.OutStmtTxnCode()));
	}

	/**
	 * 根据卡号获取该卡片对应账户下所以得未出账单交易列表
	 * 
	 * @param queryRequest
	 * @param cardNbr
	 * @param accountType
	 *            TODO
	 * @return
	 * @throws ProcessException
	 */
	private List<CcsTxnUnstatement> NF4104_A(QueryRequest queryRequest, String cardNbr, AccountType accountType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		return query.from(qCcsTxnUnstatement, qCcsCardLmMapping, qCcsCard).orderBy(qCcsTxnUnstatement.acctNbr.asc()).where(genExpressionNF4104_A(cardNbr, accountType)).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow()).list(qCcsTxnUnstatement);
	}

	/**
	 * 根据卡号获取该卡片对应账户下所以得未出账单交易总数
	 * 
	 * @param cardNbr
	 * @param accountType
	 *            TODO
	 * @param queryRequest
	 * 
	 * @return
	 * @throws ProcessException
	 */
	private int countNF4104_A(String cardNbr, AccountType accountType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		return (int) query.from(qCcsTxnUnstatement, qCcsCardLmMapping, qCcsCard).orderBy(qCcsTxnUnstatement.acctNbr.asc()).where(genExpressionNF4104_A(cardNbr, accountType)).count();
	}

	/**
	 * 生成查询表达式
	 * 
	 * @param cardNbr
	 * @param accountType
	 *            TODO
	 * @return
	 * @throws ProcessException
	 */
	private BooleanExpression genExpressionNF4104_A(String cardNbr, AccountType accountType) throws ProcessException {
		return qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr))).and(qCcsCard.acctNbr.eq(qCcsTxnUnstatement.acctNbr))
				.and(qCcsTxnUnstatement.acctType.eq(accountType)).and(qCcsTxnUnstatement.txnCode.in(unifiedParameterFacilityProvide.OutStmtTxnCode()));
	}

	/**
	 * 根据卡号获取所卡片下所有未授权交易总数
	 * 
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	private int countNF4101_C(String cardNbr, Date startDate, Date endDate) {
		JPAQuery query = new JPAQuery(em);
		int totalRow = (int) query.from(qCcsAuthmemoO, qCcsCardLmMapping).orderBy(qCcsAuthmemoO.acctNbr.asc()).where(genExpressionNF4101_C(cardNbr, startDate, endDate)).count();
		return totalRow;
	}

	/**
	 * 根据卡号获取所卡片下所有未授权交易列表
	 * 
	 * @param queryRequest
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private List<CcsAuthmemoO> NF4101_C(QueryRequest queryRequest, String cardNbr, Date startDate, Date endDate) {
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoO> tmUnmatchList = query.from(qCcsAuthmemoO, qCcsCardLmMapping).orderBy(qCcsAuthmemoO.acctNbr.asc()).where(genExpressionNF4101_C(cardNbr, startDate, endDate)).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow())
				.list(qCcsAuthmemoO);
		return tmUnmatchList;
	}

	/**
	 * 生成nf4101_C查询表达式
	 * 
	 * @param queryRequest
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private BooleanExpression genExpressionNF4101_C(String cardNbr, Date startDate, Date endDate) {
		BooleanExpression ex = qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsAuthmemoO.logicCardNbr)));
		if (startDate != null) {
			ex = ex.and(ExpressionTools.getDateGOEExpression(qCcsAuthmemoO.logOlTime, DateTools.startDateStamp(startDate)));
		}
		if (endDate != null) {
			ex = ex.and(ExpressionTools.getDateLOEExpression(qCcsAuthmemoO.logOlTime, DateTools.endDateStamp(endDate)));
		}
		return ex;
	}

	/**
	 * 根据卡号获取对应账户下所有未授权交易总数
	 * 
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	private int countNF4101_A(String cardNbr, Date startDate, Date endDate) {
		JPAQuery query = new JPAQuery(em);
		int totalRows = (int) query.from(qCcsAuthmemoO, qCcsCard, qCcsCardLmMapping).orderBy(qCcsAuthmemoO.acctNbr.asc()).where(genExpressionNF4101_A(cardNbr, startDate, endDate)).count();
		return totalRows;
	}

	/**
	 * 根据卡号获取对应账户下所有未授权交易列表
	 * 
	 * @param queryRequest
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private List<CcsAuthmemoO> NF4101_A(QueryRequest queryRequest, String cardNbr, Date startDate, Date endDate) {
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoO> tmUnmatchO = query.from(qCcsAuthmemoO, qCcsCard, qCcsCardLmMapping).orderBy(qCcsAuthmemoO.acctNbr.asc()).where(genExpressionNF4101_A(cardNbr, startDate, endDate)).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow())
				.list(qCcsAuthmemoO);
		return tmUnmatchO;
	}

	/**
	 * 生成nf4101_A查询表达式
	 * 
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	private BooleanExpression genExpressionNF4101_A(String cardNbr, Date startDate, Date endDate) {
		BooleanExpression ex = qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr))).and(qCcsCard.acctNbr.eq(qCcsAuthmemoO.acctNbr));
		if (startDate != null) {
			ex = ex.and(ExpressionTools.getDateGOEExpression(qCcsAuthmemoO.logOlTime, DateTools.startDateStamp(startDate)));
		}
		if (endDate != null) {
			ex = ex.and(ExpressionTools.getDateLOEExpression(qCcsAuthmemoO.logOlTime, DateTools.endDateStamp(endDate)));
		}
		return ex;
	}

	/**
	 * 根据卡号获取所在卡片下的所有授权交易的总数
	 * 
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	private int countNF4103_C(String cardNbr, Date startDate, Date endDate) {
		JPAQuery query = new JPAQuery(em);
		int totalRow = (int) query.from(qCcsAuthmemoHst, qCcsCardLmMapping).orderBy(qCcsAuthmemoHst.acctNbr.asc()).where(genExpression_NF4103_C(cardNbr, startDate, endDate)).count();
		return totalRow;
	}

	/**
	 * 根据卡号获取所在卡片下的所有授权交易列表
	 * 
	 * @param queryRequest
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private List<CcsAuthmemoHst> NF4103_C(QueryRequest queryRequest, String cardNbr, Date startDate, Date endDate) {
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoHst> tmAuthHstList = query.from(qCcsAuthmemoHst, qCcsCardLmMapping).orderBy(qCcsAuthmemoHst.acctNbr.asc()).where(genExpression_NF4103_C(cardNbr, startDate, endDate)).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow())
				.list(qCcsAuthmemoHst);
		return tmAuthHstList;
	}

	/**
	 * 生成NF4103_C的查询表达式
	 * 
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	private BooleanExpression genExpression_NF4103_C(String cardNbr, Date startDate, Date endDate) {
		BooleanExpression ex = qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsAuthmemoHst.logicCardNbr)));
		if (startDate != null) {
			ex = ex.and(ExpressionTools.getDateGOEExpression(qCcsAuthmemoHst.logOlTime, DateTools.startDateStamp(startDate)));
		}
		if (endDate != null) {
			ex = ex.and(ExpressionTools.getDateLOEExpression(qCcsAuthmemoHst.logOlTime, DateTools.endDateStamp(endDate)));
		}
		return ex;
	}

	/**
	 * 根据卡号获取所在账户下的所有授权交易总数
	 * 
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	private int countNF4103_A(String cardNbr, Date startDate, Date endDate) {
		JPAQuery query = new JPAQuery(em);
		int totalRow = (int) query.from(qCcsAuthmemoHst, qCcsCard, qCcsCardLmMapping).orderBy(qCcsAuthmemoHst.acctNbr.asc()).where(genExpressionNF4103_A(cardNbr, startDate, endDate)).count();
		return totalRow;
	}

	/**
	 * 根据卡号获取所在账户下的所有授权交易总数
	 * 
	 * @param queryRequest
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private List<CcsAuthmemoHst> NF4103_A(QueryRequest queryRequest, String cardNbr, Date startDate, Date endDate) {
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoHst> tmAuthHstList = query.from(qCcsAuthmemoHst, qCcsCard, qCcsCardLmMapping).orderBy(qCcsAuthmemoHst.acctNbr.asc()).where(genExpressionNF4103_A(cardNbr, startDate, endDate)).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow())
				.list(qCcsAuthmemoHst);
		return tmAuthHstList;
	}

	/**
	 * 生成NF4103_A查询表达式
	 * 
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private BooleanExpression genExpressionNF4103_A(String cardNbr, Date startDate, Date endDate) {
		BooleanExpression ex = qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr))).and(qCcsCard.acctNbr.eq(qCcsAuthmemoHst.acctNbr));
		if (startDate != null) {
			ex = ex.and(ExpressionTools.getDateGOEExpression(qCcsAuthmemoHst.logOlTime, DateTools.startDateStamp(startDate)));
		}
		if (endDate != null) {
			ex = ex.and(ExpressionTools.getDateLOEExpression(qCcsAuthmemoHst.logOlTime, DateTools.endDateStamp(endDate)));
		}
		return ex;
	}

	// 查询授权未入账交易流水列表
	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF4101(QueryRequest queryRequest, String cardAccountIndicator, String cardNbr, Date startDate, Date endDate) throws ProcessException {
		log.info("NF4101:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],开始日期[" + startDate + "],结束日期[" + endDate + "]");
		CheckUtil.checkCardNo(cardNbr);
		CheckUtil.rejectNull(cardAccountIndicator, "交易卡帐指示不能为空");
		List<CcsAuthmemoO> tmUnmatchOList;
		int totalRow = 0;
		if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_A)) {
			tmUnmatchOList = NF4101_A(queryRequest, cardNbr, startDate, endDate);
			totalRow = countNF4101_A(cardNbr, startDate, endDate);
		} else if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_C)) {
			tmUnmatchOList = NF4101_C(queryRequest, cardNbr, startDate, endDate);
			totalRow = countNF4101_C(cardNbr, startDate, endDate);
		} else {
			throw new ProcessException("无效的交易卡帐指示");
		}
		List<Map<String, Serializable>> tmUnMatchMaps = new ArrayList<Map<String, Serializable>>();
		for (CcsAuthmemoO tmUnmatchO : tmUnmatchOList) {
			tmUnMatchMaps.add(tmUnmatchO.convertToMap());
		}
		return CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRow, tmUnMatchMaps);
	}

	/**
	 * 根据卡号获取所卡片下所有未授权交易总数
	 * 
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	private int countNF4102_C(String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		int totalRow = (int) query.from(qCcsAuthmemoInqLog, qCcsCardLmMapping).orderBy(qCcsAuthmemoInqLog.acctNbr.asc()).where(genExpressionNF4102_C(cardNbr)).count();
		return totalRow;
	}

	/**
	 * 根据卡号获取所卡片下所有未授权交易列表
	 * 
	 * @param queryRequest
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private List<CcsAuthmemoInqLog> NF4102_C(QueryRequest queryRequest, String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoInqLog> tmAuthInqLog = query.from(qCcsAuthmemoInqLog, qCcsCardLmMapping).orderBy(qCcsAuthmemoInqLog.acctNbr.asc()).where(genExpressionNF4102_C(cardNbr)).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow()).list(qCcsAuthmemoInqLog);
		return tmAuthInqLog;
	}

	/**
	 * 生成NF4102_C查询表达式
	 * 
	 * @param queryRequest
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private BooleanExpression genExpressionNF4102_C(String cardNbr) {
		return qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsAuthmemoInqLog.logicCardNbr)));
	}

	/**
	 * 根据卡号获取对应账户下所有未授权交易总数
	 * 
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	private int countNF4102_A(String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		int totalRows = (int) query.from(qCcsAuthmemoInqLog, qCcsCard, qCcsCardLmMapping).orderBy(qCcsAuthmemoInqLog.acctNbr.asc()).where(genExpressionNF4102_A(cardNbr)).count();
		return totalRows;
	}

	/**
	 * 根据卡号获取对应账户下所有未授权交易列表
	 * 
	 * @param queryRequest
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private List<CcsAuthmemoInqLog> NF4102_A(QueryRequest queryRequest, String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoInqLog> tmAuthInqLog = query.from(qCcsAuthmemoInqLog, qCcsCard, qCcsCardLmMapping).orderBy(qCcsAuthmemoInqLog.acctNbr.asc()).where(genExpressionNF4102_A(cardNbr)).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow())
				.list(qCcsAuthmemoInqLog);
		return tmAuthInqLog;
	}

	/**
	 * 生成NF4102_A查询表达式
	 * 
	 * @param cardNbr
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	private BooleanExpression genExpressionNF4102_A(String cardNbr) {
		return qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr))).and(qCcsCard.acctNbr.eq(qCcsAuthmemoInqLog.acctNbr));
	}

	// 查询类授权交易流水查询
	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF4102(QueryRequest queryRequest, String cardAccountIndicator, String cardNbr) throws ProcessException {
		log.info("NF4102:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
		CheckUtil.checkCardNo(cardNbr);
		CheckUtil.rejectNull(cardAccountIndicator, "交易卡帐指示不能为空");
		List<CcsAuthmemoInqLog> tmAuthInqLogList;
		int totalRow = 0;
		if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_A)) {
			tmAuthInqLogList = NF4102_A(queryRequest, cardNbr);
			totalRow = countNF4102_A(cardNbr);
		} else if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_C)) {
			tmAuthInqLogList = NF4102_C(queryRequest, cardNbr);
			totalRow = countNF4102_C(cardNbr);
		} else {
			throw new ProcessException("无效的交易卡帐指示");
		}
		List<Map<String, Serializable>> tmAuthInqLogMaps = new ArrayList<Map<String, Serializable>>();
		for (CcsAuthmemoInqLog tmAuthInqLog : tmAuthInqLogList) {
			tmAuthInqLogMaps.add(tmAuthInqLog.convertToMap());
		}
		return CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRow, tmAuthInqLogMaps);
	}

	// 年费交易清单列表查询
	@Override
	public List<Map<String, Serializable>> NF4105(String cardNbr, String cardAccountIndicator) throws ProcessException {
		log.info("NF4105:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],交易卡帐指示[" + cardAccountIndicator + "]");
		List<CcsTxnHst> txnHstList = new ArrayList<CcsTxnHst>();
		if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_A)) {
			txnHstList = getCcsTxnHstList_A(cardNbr);
		} else if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_C)) {
			txnHstList = getCcsTxnHstList_C(cardNbr);
		} else {
			throw new ProcessException("无效的交易卡帐指示");
		}
		ArrayList<Map<String, Serializable>> list = new ArrayList<Map<String, Serializable>>();
		for (CcsTxnHst tmTxnHst : txnHstList) {
			list.add(tmTxnHst.convertToMap());
		}
		return list;
	}

	// 按账户查询

	List<CcsTxnHst> getCcsTxnHstList_A(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression ex = qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr))).and(qCcsCard.acctNbr.eq(qCcsTxnHst.acctNbr))
				.and(qCcsTxnHst.txnCode.eq(unifiedParameterFacilityProvide.cardFee().txnCd));

		return query.from(qCcsTxnHst, qCcsCardLmMapping, qCcsCard).orderBy(qCcsTxnHst.acctNbr.asc()).where(ex).list(qCcsTxnHst);
	}

	// 按卡片查询

	List<CcsTxnHst> getCcsTxnHstList_C(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression exp = qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsTxnHst.logicCardNbr)))
				.and(qCcsTxnHst.txnCode.eq(unifiedParameterFacilityProvide.cardFee().txnCd));
		return query.from(qCcsTxnHst, qCcsCardLmMapping).orderBy(qCcsTxnHst.acctNbr.asc()).where(exp).list(qCcsTxnHst);
	}

	@Override
	public void NF4106(Map<String, Serializable> tmTxnReject) throws ProcessException {
		CcsTxnAdjLog temp = new CcsTxnAdjLog();
		temp.updateFromMap(tmTxnReject);
		log.info("NF4106:["+CodeMarkUtils.subCreditCard(temp.getCardNbr())+"]");
		rCcsTxnAdjLog.save(temp);
	}

}
