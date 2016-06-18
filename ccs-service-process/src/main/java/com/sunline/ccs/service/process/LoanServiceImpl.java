package com.sunline.ccs.service.process;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.hibernate.HibernateSubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.QueryRequest;
import com.sunline.ark.support.service.QueryResult;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardLmMapping;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsStatement;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsStatementKey;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.loan.StmtLoanProvideImpl;
import com.sunline.ccs.loan.TxnLoanProvideImpl;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.service.util.CPSServProBusUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CcLoanService;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;


/** 
 * @see 类名：LoanServiceImpl
 * @see 描述：分期服务类接口
 *
 * @see 创建日期：   2015年6月24日 下午2:47:48
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class LoanServiceImpl implements CcLoanService {

	private static final String NF6101_CARDINDICATOR_C = "C";
	private static final String NF6101_ACCOUNTINDICATOR_A = "A";

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RCcsTxnUnstatement rCcsTxnUnstatement;
	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private RCcsStatement rccsStatement;
	@Autowired
	private RCcsCustomer rCcsCustomer;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private UnifiedParamFacilityProvide unifieldParServer;
	@Autowired
	private RCcsCardLmMapping rCcsCardLmMapping;
//	@Autowired
//    private MessageService messageService;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
/*	@Autowired
	private DownMsgFacility downMsgFacility; 
*/	
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
    
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;

	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	QCcsTxnUnstatement qCcsTxnUnstatement = QCcsTxnUnstatement.ccsTxnUnstatement;
	QCcsCard qCcsCard = QCcsCard.ccsCard;
	
	
	//可转分期交易查询
	@Transactional
	public QueryResult<Map<String, Serializable>> NF6106(QueryRequest queryRequest, String cardNbr, String cardAccountIndicator, String currencyCd) throws ProcessException {
		log.info("NF6106:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],交易卡帐指示[" + cardAccountIndicator + "]");
		CheckUtil.checkCardNo(cardNbr);
		CheckUtil.rejectNull(cardAccountIndicator, "交易卡帐指示不能为空");
		AccountType accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(cardNbr, currencyCd);
		List<CcsTxnUnstatement> tmTxnUnstmtList = null;
		int totalRow;
		if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_A)) {
			tmTxnUnstmtList = NF6106_A(queryRequest, cardNbr, accountType);
			totalRow = countNF6106_A(cardNbr, accountType);
		} else if (cardAccountIndicator.equals(CcServProConstants.TRANSTYPE_C)) {
			tmTxnUnstmtList = NF6106_C(queryRequest, cardNbr, accountType);
			totalRow = countNF6106_C(cardNbr, accountType);
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
	private List<CcsTxnUnstatement> NF6106_C(QueryRequest queryRequest, String cardNbr, AccountType accountType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		return query.from(qCcsTxnUnstatement, qCcsCardLmMapping).orderBy(qCcsTxnUnstatement.acctNbr.asc()).where(genExpressionNF6106_C(cardNbr, accountType)).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow()).list(qCcsTxnUnstatement);
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
	private int countNF6106_C(String cardNbr, AccountType accountType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		int totalRow = (int) query.from(qCcsTxnUnstatement, qCcsCardLmMapping).orderBy(qCcsTxnUnstatement.acctNbr.asc()).where(genExpressionNF6106_C(cardNbr, accountType)).count();
		return totalRow;
	}

	/**
	 * 生成NF6106_C查询表达式
	 * 
	 * @param cardNbr
	 * @param accountType
	 *            TODO
	 * @return
	 * @throws ProcessException
	 */
	private BooleanExpression genExpressionNF6106_C(String cardNbr, AccountType accountType) throws ProcessException {
		CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
		LoanPlan loanPlan = unifieldParServer.loanPlan(CcsCard.getProductCd(), LoanType.R);
		return qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsTxnUnstatement.logicCardNbr))).and(qCcsTxnUnstatement.acctType.eq(accountType))
				.and(qCcsTxnUnstatement.txnCode.in(loanPlan.txnCdList));
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
	private List<CcsTxnUnstatement> NF6106_A(QueryRequest queryRequest, String cardNbr, AccountType accountType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		return query.from(qCcsTxnUnstatement, qCcsCardLmMapping, qCcsCard).orderBy(qCcsTxnUnstatement.acctNbr.asc()).where(genExpressionNF6106_A(cardNbr, accountType)).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow()).list(qCcsTxnUnstatement);
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
	private int countNF6106_A(String cardNbr, AccountType accountType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		return (int) query.from(qCcsTxnUnstatement, qCcsCardLmMapping, qCcsCard).orderBy(qCcsTxnUnstatement.acctNbr.asc()).where(genExpressionNF6106_A(cardNbr, accountType)).count();
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
	private BooleanExpression genExpressionNF6106_A(String cardNbr, AccountType accountType) throws ProcessException {
		CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
		LoanPlan loanPlan = unifieldParServer.loanPlan(CcsCard.getProductCd(), LoanType.R);
		return qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr))).and(qCcsCard.acctNbr.eq(qCcsTxnUnstatement.acctNbr))
				.and(qCcsTxnUnstatement.acctType.eq(accountType)).and(qCcsTxnUnstatement.txnCode.in(loanPlan.txnCdList));
	}

	
	

	// 分期交易查询
	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF6101(QueryRequest queryRequest, String cardNbr, String cardAccountIndicator) throws ProcessException {
		log.info("NF6101:卡号[" + CodeMarkUtils.subCreditCard(cardNbr) + "],卡帐指示[" + cardAccountIndicator + "]");
		CheckUtil.checkCardNo(cardNbr);
		CheckUtil.rejectNull(cardAccountIndicator, "卡帐指示不能为空");
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		// 按账户号查询
		List<CcsLoan> loanList;
		int totalRows = 0;
		if (cardAccountIndicator.equals(NF6101_ACCOUNTINDICATOR_A)) {
			QCcsCard qCcsCard = QCcsCard.ccsCard;
			JPAQuery query = new JPAQuery(em);
			BooleanExpression booleanExpression = qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(
					qCcsCard.acctNbr.eq(qCcsLoan.acctNbr).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()))));
			loanList = query.from(qCcsCardLmMapping, qCcsCard, qCcsLoan).where(booleanExpression).list(qCcsLoan);
			JPAQuery query1 = new JPAQuery(em);
			totalRows = (int) query1.from(qCcsCardLmMapping, qCcsCard, qCcsLoan).where(booleanExpression).count();
			// 按介质卡查询
		} else if (cardAccountIndicator.equals(NF6101_CARDINDICATOR_C)) {
			JPAQuery query = new JPAQuery(em);
			BooleanExpression booleanExpression = qCcsCardLmMapping.org.eq(qCcsLoan.org).and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsLoan.logicCardNbr))));
			loanList = query.from(qCcsCardLmMapping, qCcsLoan).where(booleanExpression).list(qCcsLoan);
			JPAQuery query1 = new JPAQuery(em);
			totalRows = (int) query1.from(qCcsCardLmMapping, qCcsLoan).where(booleanExpression).count();
		} else {
			log.error("无效的卡帐指示[" + cardAccountIndicator + "]");
			throw new ProcessException("无效的卡帐指示");
		}
		List<Map<String, Serializable>> al = new ArrayList<Map<String, Serializable>>();
		for (CcsLoan loan : loanList) {
			al.add(loan.convertToMap());
		}
		return CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRows, al);
	}

	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF6102(QueryRequest queryRequest, String cardNbr, String cardAccountIndicator) throws ProcessException {
		log.info("NF6102:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]，卡帐指示[" + cardAccountIndicator + "]");
		QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		int totalRows = 0;
		List<CcsLoanReg> loanRegList = null;
		// 按账户号查询
		if (cardAccountIndicator.equals(NF6101_ACCOUNTINDICATOR_A)) {
			QCcsCard qCcsCard = QCcsCard.ccsCard;
			JPAQuery query = new JPAQuery(em);
			BooleanExpression booleanExpression = qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(
					qCcsCard.acctNbr.eq(qCcsLoanReg.acctNbr).and(qCcsCardLmMapping.cardNbr.eq(cardNbr).and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLoanReg.registerDate.eq(unifieldParServer.BusinessDate())))));
			loanRegList = query.from(qCcsCardLmMapping, qCcsLoanReg, qCcsCard).where(booleanExpression).list(qCcsLoanReg);
			JPAQuery query1 = new JPAQuery(em);
			totalRows = (int) query1.from(qCcsCardLmMapping, qCcsLoanReg, qCcsCard).where(booleanExpression).count();
		} else if (cardAccountIndicator.equals(NF6101_CARDINDICATOR_C)) {// 按卡查询
			JPAQuery query = new JPAQuery(em);
			BooleanExpression booleanExpression = qCcsCardLmMapping.org.eq(qCcsLoanReg.org)
					.and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr)).and(qCcsLoanReg.registerDate.eq(unifieldParServer.BusinessDate()))
							.and(qCcsCardLmMapping.logicCardNbr.eq(qCcsLoanReg.logicCardNbr)));
			loanRegList = query.from(qCcsCardLmMapping, qCcsLoanReg).where(booleanExpression).list(qCcsLoanReg);

			JPAQuery query1 = new JPAQuery(em);
			totalRows = (int) query1.from(qCcsCardLmMapping, qCcsLoanReg).where(booleanExpression).count();
		} else {
			log.error("无效卡帐指示[" + cardAccountIndicator + "]");
			throw new ProcessException("无效的卡帐指示");
		}
		List<Map<String, Serializable>> loanListMap = new ArrayList<Map<String, Serializable>>();
		if (!loanRegList.isEmpty()) {
			for (CcsLoanReg loanReg : loanRegList) {
				loanListMap.add(loanReg.convertToMap());
			}
		}
		return CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRows, loanListMap);
	}

	// 交易转分期
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transactional
	public Map<String, Serializable> NF6301(Integer term, Map<String, Serializable> transaction, boolean isPreview, LoanFeeMethod loanFeeMethod) throws ProcessException {
		log.info("NF6301:分期数[" + term + "]");
		LogTools.printObj(log, transaction, "上送的转分期交易数据");
		CcsTxnUnstatement tmTxnUnStmtTemp = new CcsTxnUnstatement();
		tmTxnUnStmtTemp.updateFromMap(transaction);
		
		//查询原交易信息
		CcsTxnUnstatement tmTxnUnstmt = rCcsTxnUnstatement.findOne(tmTxnUnStmtTemp.getTxnSeq());		
		CheckUtil.rejectNull(tmTxnUnstmt, "分期申请失败：找不到原消费交易");
		
		//查询卡片信息
		CcsCard CcsCard = queryFacility.getCardByCardNbr(tmTxnUnstmt.getCardNbr());
		CheckUtil.rejectNull(CcsCard, "卡号:[" + CcsCard + "]查询不到对应的卡片信息");
		
		//判断转分期交易对应的卡片是否过有效期
		if(unifieldParServer.BusinessDate().compareTo(CcsCard.getCardExpireDate())>0){
			throw new ProcessException("分期申请失败：卡片已经失效");
		}
		
		// 获取分期计划参数
		LoanPlan loanPlan = unifieldParServer.loanPlan(tmTxnUnstmt.getProductCd(), LoanType.R);

		// 获取分期参数
		LoanFeeDef loanFeeDef = unifieldParServer.loanFeeDef(tmTxnUnstmt.getProductCd(), LoanType.R, term);

		// 判断交易码是否可以做分期
		if (!loanPlan.txnCdList.contains(tmTxnUnstmt.getTxnCode())) {
			throw new ProcessException("分期申请失败：非可转分期交易类型");
		}
		// 判断当天是否已经做分期申请
		QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
		CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.cardNbr.eq(tmTxnUnstmt.getCardNbr())
				.and(qCcsLoanReg.origTransDate.eq(tmTxnUnstmt.getTxnDate()).and(
						qCcsLoanReg.origTxnAmt.eq(tmTxnUnstmt.getTxnAmt()).and(qCcsLoanReg.origAuthCode.eq(tmTxnUnstmt.getAuthCode())).and(qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg())))));
		CheckUtil.rejectNotNull(loanReg, "分期申请失败：该笔交易已经转分期申请，不可重复申请");

		// 判断该交易是否已经做分期
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		CcsLoan loan = rCcsLoan.findOne(qCcsLoan.cardNbr.eq(tmTxnUnstmt.getCardNbr()).and(
				qCcsLoan.origTransDate.eq(tmTxnUnstmt.getTxnDate()).and(qCcsLoan.origTxnAmt.eq(tmTxnUnstmt.getTxnAmt()).and(qCcsLoan.origAuthCode.eq(tmTxnUnstmt.getAuthCode())).and(qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg())))));
		CheckUtil.rejectNotNull(loan, "分期申请失败：该笔交易已经转分期，不可再做申请");

		 CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(tmTxnUnstmt.getCardNbr(), tmTxnUnstmt.getAcctType());
		 CheckUtil.rejectNull(CcsAcct, "卡号[" + tmTxnUnstmt.getCardNbr() + "]查询不到对应的账户信息");

		// 判断账户上是否有锁定码存在，存在则无法分期
		if (!blockCodeUtils.getMergedLoanInd(CcsAcct.getBlockCode())) {
			throw new ProcessException("分期申请失败：存在不能分期的锁定码 ");
		}
		
		// 判断卡片上是否有锁定码存在，存在则无法分期
		if (!blockCodeUtils.getMergedLoanInd(CcsCard.getBlockCode())) {
			throw new ProcessException("分期申请失败：存在不能分期的锁定码 ");
		}

		// 根据卡号找到主卡对应的消费计划
		JPAQuery query = new JPAQuery(em);
		QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
		QCcsCard qCcsCard = QCcsCard.ccsCard;
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		BooleanExpression ex = qCcsCard.logicCardNbr.eq(qCcsCardLmMapping.logicCardNbr).and(qCcsCardLmMapping.cardNbr.eq(tmTxnUnstmt.getCardNbr()));
		List<BigDecimal> currBalanceList = query.from(qCcsPlan).where(qCcsPlan.logicCardNbr.eq(new HibernateSubQuery().from(qCcsCard, qCcsCardLmMapping).where(ex).unique(qCcsCard.cardBasicNbr)).and(qCcsPlan.planType.eq(PlanType.R)))
				.list(qCcsPlan.currBal);

		if (currBalanceList.isEmpty()) {
			throw new ProcessException("分期申请失败：查询不到对应的消费计划");
		}
		BigDecimal loanInitPrin = new BigDecimal(0);

		for (BigDecimal bigDecimail : currBalanceList) {
			loanInitPrin = loanInitPrin.add(bigDecimail);
		}

		// 加总金额大于交易金额则使用交易金额进行分期，否则使用加总金额进行分期
		if (loanInitPrin.compareTo(tmTxnUnstmt.getTxnAmt()) > 0) {
			loanInitPrin = tmTxnUnstmt.getTxnAmt();
		}

		// 判断申请金额是否小于分期最小允许金额LOAN_MIN_AMT
		if (loanInitPrin.compareTo(loanFeeDef.minAmount) < 0) {
			throw new ProcessException("分期申请失败：分期金额小于最小可分期金额");
		}

		if (loanFeeDef.distributeMethod == null) {
			log.error("本金分配方式的参数为空");
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
		TxnLoanProvideImpl txnLoanProvideImpl = new TxnLoanProvideImpl(LoanType.R, loanFeeDef, tmTxnUnstmt);
		CcsLoanReg loanRegtemp = txnLoanProvideImpl.genLoanReg(term, loanInitPrin, tmTxnUnstmt.getRefNbr(), tmTxnUnstmt.getLogicCardNbr(), tmTxnUnstmt.getCardNbr(), loanFeeMethod, tmTxnUnstmt.getAcctNbr(), tmTxnUnstmt.getAcctType(),
				loanPlan.loanCode, unifieldParServer.BusinessDate());
		// 不是预览，保存分期数据
		if (!isPreview) {
			rCcsLoanReg.save(loanRegtemp);
			
			// 分期付款申请成功
			CcsCustomer customer = rCcsCustomer.findOne(CcsCard.getCustId());
		    String msgCd = fetchMsgCdService.fetchMsgCd(CcsCard.getProductCd(), CPSMessageCategory.CPS032);
//			messageService.sendMessage(MessageCategory.P01, CcsCard.getProductCd(), tmTxnUnstmt.getCardNbr(), customer.getName(), customer.getGender(), 
//		    		customer.getMobileNo(), customer.getEmail(), new Date(), new MapBuilder()
//	    			.add("loanType", loanRegtemp.getLoanType())
//	    			.add("amt", loanRegtemp.getLoanInitPrin())
//	    			.add("term", loanRegtemp.getLoanInitTerm())
//	    			.add("loanFee", loanRegtemp.getLoanInitFee())
//	    			.add("nextPayment", loanRegtemp.getLoanFirstTermPrin().add(loanRegtemp.getLoanFirstTermFee()))
//	    			.add("loanFixedFee", loanRegtemp.getLoanFixedFee())
//		    		.build());
/*		   downMsgFacility.sendMessage(msgCd, tmTxnUnstmt.getCardNbr(), customer.getName(), customer.getGender(), 
		    		customer.getMobileNo(), new Date(), new MapBuilder()
	    			.add("loanType", loanRegtemp.getLoanType())
	    			.add("amt", loanRegtemp.getLoanInitPrin())
	    			.add("term", loanRegtemp.getLoanInitTerm())
	    			.add("loanFee", loanRegtemp.getLoanInitFee())
	    			.add("nextPayment", loanRegtemp.getLoanFirstTermPrin().add(loanRegtemp.getLoanFirstTermFee()))
	    			.add("loanFixedFee", loanRegtemp.getLoanFixedFee())
		    		.build());
*/		}
		return loanRegtemp.convertToMap();
	}

	@Override
	@Transactional
	public void NF6302(Map<String, Serializable> loanReg) throws ProcessException {
		CcsLoanReg loanRegTemp = new CcsLoanReg();
		loanRegTemp.updateFromMap(loanReg);		
		log.info("NF6302:分期编号[" + loanRegTemp.getRegisterId() + "]");
		CheckUtil.rejectNull(loanRegTemp.getRegisterId(), "分期编号不能为空");
		Long registerId = loanRegTemp.getRegisterId();
		QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
		CcsLoanReg NF6302LoanReg = rCcsLoanReg.findOne(qCcsLoanReg.registerId.eq(registerId));
		CheckUtil.rejectNull(NF6302LoanReg, "无分期交易可做撤销");
		rCcsLoanReg.delete(NF6302LoanReg);
		
		// 分期付款取消
		CcsAcctKey accountKey = new CcsAcctKey();
		accountKey.setAcctNbr(NF6302LoanReg.getAcctNbr());
		accountKey.setAcctType(NF6302LoanReg.getAcctType());
		CcsAcct account = rCcsAcct.findOne(accountKey);
		CcsCustomer customer = rCcsCustomer.findOne(account.getCustId());
		String msgCd = fetchMsgCdService.fetchMsgCd(account.getProductCd(), CPSMessageCategory.CPS034);
//		messageService.sendMessage(MessageCategory.P03, account.getProductCd(), loanReg.getCardNbr(), customer.getName(), customer.getGender(), 
//	    		customer.getMobileNo(), customer.getEmail(), new Date(), new MapBuilder<String, Object>()
//    			.add("loanType", loanRegTemp.getLoanType())
//    			.add("amt", loanRegTemp.getLoanInitPrin())
//    			.add("term", loanRegTemp.getLoanInitTerm())
//	    		.build());
/*		downMsgFacility.sendMessage(msgCd,  NF6302LoanReg.getCardNbr(), customer.getName(), customer.getGender(), 
	    		customer.getMobileNo(),  new Date(), new MapBuilder<String, Object>()
    			.add("loanType", loanRegTemp.getLoanType())
    			.add("amt", loanRegTemp.getLoanInitPrin())
    			.add("term", loanRegTemp.getLoanInitTerm())
	    		.build());
*/	}

	@Override
	@Transactional
	public void NF6303(Map<String, Serializable> loan) throws ProcessException {
		CcsLoan loantemp = new CcsLoan();
		loantemp.updateFromMap(loan);
		log.info("NF6303:分期编号[" + String.valueOf(loantemp.getLoanId()) + "]");
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		CcsLoan loanSearch = rCcsLoan.findOne(qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLoan.loanId.eq(loantemp.getLoanId())));
		CheckUtil.rejectNull(loanSearch, "分期交易不存在，无法终止");
		if (loanSearch.getLoanStatus().equals(LoanStatus.T)) {
			throw new ProcessException("分期终止失败，该分期已为终止状态");
		}
		loanSearch.setLoanStatus(LoanStatus.T);
		loanSearch.setTerminalDate(unifieldParServer.BusinessDate());
		loanSearch.setLastLoanStatus(loantemp.getLoanStatus());
		loanSearch.setTerminalReasonCd(LoanTerminateReason.V);
		
		// 分期付款中止
		CcsAcctKey accountKey = new CcsAcctKey();
		accountKey.setAcctNbr(loanSearch.getAcctNbr());
		accountKey.setAcctType(loanSearch.getAcctType());
		CcsAcct account = rCcsAcct.findOne(accountKey);
		CcsCustomer customer = rCcsCustomer.findOne(account.getCustId());
		String msgCd = fetchMsgCdService.fetchMsgCd(account.getProductCd(), CPSMessageCategory.CPS035);
//		messageService.sendMessage(MessageCategory.P04, account.getProductCd(), loantemp.getCardNbr(), customer.getName(), customer.getGender(), 
//	    		customer.getMobileNo(), customer.getEmail(), new Date(), new MapBuilder<String, Object>()
//    			.add("loanType", loanSearch.getLoanType())
//    			.add("amt", loanSearch.getLoanInitPrin())
//    			.add("term", loanSearch.getLoanInitTerm())
//	    		.build());
/*		downMsgFacility.sendMessage(msgCd,  loantemp.getCardNbr(), customer.getName(), customer.getGender(), 
	    		customer.getMobileNo(),  new Date(), new MapBuilder<String, Object>()
    			.add("loanType", loanSearch.getLoanType())
    			.add("amt", loanSearch.getLoanInitPrin())
    			.add("term", loanSearch.getLoanInitTerm())
	    		.build());
*/	}

	@Override
	@Transactional
	public void NF6304(Map<String, Serializable> loan) throws ProcessException {
		CcsLoan loantemp = new CcsLoan();
		loantemp.updateFromMap(loan);
		log.info("NF6304:分期编号[" + String.valueOf(loantemp.getLoanId()) + "]");
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		CcsLoan loanSearch = rCcsLoan.findOne(qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLoan.loanId.eq(loantemp.getLoanId())));
		CheckUtil.rejectNull(loanSearch, "分期交易不存在，无法终止撤销");
		if (!loanSearch.getLoanStatus().equals(LoanStatus.T)) {
			throw new ProcessException("分期不是终止状态，无法进行撤销");
		}
		if (!loanSearch.getTerminalReasonCd().equals(LoanTerminateReason.V)) {
			throw new ProcessException("分期不是持卡人发起的中止，无法进行撤销");
		}
		if (!DateFormatUtils.ISO_DATE_FORMAT.format(loanSearch.getTerminalDate()).equals(DateFormatUtils.ISO_DATE_FORMAT.format(unifieldParServer.BusinessDate()))) {
			throw new ProcessException("隔日的分期终止，无法进行撤销");
		}
		loanSearch.setLoanStatus(loanSearch.getLastLoanStatus());
		loanSearch.setLastLoanStatus(null);
		loanSearch.setTerminalReasonCd(null);
		loanSearch.setTerminalDate(null);
	}

	// 账单分期
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transactional
	public Map<String, Serializable> NF6305(Integer term, Map<String, Serializable> ccsStatementMap, LoanFeeMethod loanFeeMethod, boolean isPreview, BigDecimal initPrin) throws ProcessException {
		log.info("NF6305:分期数[" + term + "]，分期金额[" + initPrin + "]");
		LogTools.printObj(log, ccsStatementMap, "上送的转分期账单数据");
		CcsStatement ccsStatementTemp = new CcsStatement();
		ccsStatementTemp.updateFromMap(ccsStatementMap);
		CcsStatementKey ccsStatementKey = new CcsStatementKey();
		ccsStatementKey.setAcctNbr(ccsStatementTemp.getAcctNbr());
		ccsStatementKey.setAcctType(ccsStatementTemp.getAcctType());
		ccsStatementKey.setStmtDate(ccsStatementTemp.getStmtDate());
		CcsStatement ccsStatement = rccsStatement.findOne(ccsStatementKey);

		CheckUtil.rejectNull(ccsStatement, "分期申请失败：找不到可分期的账单");

		// 获取账户信息
		CcsAcct CcsAcct = queryFacility.getAcctByAcctNbr(ccsStatementTemp.getAcctType(), ccsStatement.getAcctNbr());
		CheckUtil.rejectNull(CcsAcct, "查询不到对应的账户信息");
		
		// ref_nbr = 账号+账户类型+上次账单日期
		String refNbr = CcsAcct.getAcctNbr() + CcsAcct.getAcctType().toString() + CcsAcct.getLastStmtDate();
		// 使用ref_nbr判断是否已经做过分期
		CcsLoan loan = rCcsLoan.findOne(qCcsLoan.refNbr.eq(refNbr));
		CheckUtil.rejectNotNull(loan, "分期申请失败：该笔账单已做过账单分期");
		CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.refNbr.eq(refNbr));
		CheckUtil.rejectNotNull(loanReg, "分期申请失败：该笔账单已做过账单分期");
		
		// 判读是否有锁定码N存在，存在则无法分期
		if (!blockCodeUtils.getMergedLoanInd(CcsAcct.getBlockCode())) {
			throw new ProcessException("分期申请失败：存在不能分期的锁定码 ");
		}

		// 日期区间
		// 判断申请当日业务日期是否处于“最近一次账单日期LAST_STMT_DATE次日”至“到期还款日前N天（N为DIRECT_DB_DAYS参数值）且保证是最后一期账单
		if (!DateTools.dateBetwen(DateUtils.addDays(CcsAcct.getLastStmtDate(), 1), unifieldParServer.BusinessDate(), DateUtils.addDays(CcsAcct.getPmtDueDate(), -unifieldParServer.direct_Db_Days(CcsAcct.getProductCd())))
				|| CcsAcct.getLastStmtDate().compareTo(ccsStatement.getStmtDate()) != 0) {
			throw new ProcessException("分期申请失败：申请日期不在规定日期之内");
		}
		
		
		// 获取账单分期参数
		LoanFeeDef loanFeeDef = unifieldParServer.loanFeeDef(CcsAcct.getProductCd(), LoanType.B, term);
		if (loanFeeDef.distributeMethod == null) {
				throw new ProcessException("分期申请失败：本金分配方式的参数为空");
		}
		
	
		// 判断申请金额是否大于账户的“该账户下所有消费信用计划当前PAST_PRIN之和
		if (initPrin.compareTo(queryFacility.countPlan_PastPrincipal(CcsAcct.getAcctNbr(), CcsAcct.getAcctType(), PlanType.R).multiply(loanFeeDef.maxAmountRate)) > 0) {
			throw new ProcessException("分期申请失败：申请金额大于允许的分期比例");
		}
		
		//判断申请金额是否大于参数中设置的最大允许分期金额
		if(initPrin.compareTo(loanFeeDef.maxAmount)>0){
			throw new ProcessException("分期申请失败：申请金额大于最大允许分期金额");
		}
		
		// 判断申请金额是否小于分期最小允许金额LOAN_MIN_AMT
		if (initPrin.compareTo(loanFeeDef.minAmount) < 0) {
			throw new ProcessException("分期申请失败：申请金额小于最小可分期金额");
		}
		
		// 判断申请金额是否大于账户的永久额度
		if (initPrin.compareTo(CcsAcct.getCreditLmt()) > 0) {
					throw new ProcessException("分期申请失败：申请金额大于信用额度");
		}				

		// 获取分期计划参数
		LoanPlan loanPlan = unifieldParServer.loanPlan(CcsAcct.getProductCd(), LoanType.B);
		
		//获取卡号
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		CcsCardLmMapping CcsCardLmMapping = rCcsCardLmMapping.findOne(qCcsCardLmMapping.logicCardNbr.eq(CcsAcct.getDefaultLogicCardNbr()));

		StmtLoanProvideImpl stmtProvide = new StmtLoanProvideImpl(LoanType.B, loanFeeDef);
		loanReg = stmtProvide.genLoanReg(term, initPrin, refNbr, CcsAcct.getDefaultLogicCardNbr(), CcsCardLmMapping.getCardNbr(), loanFeeMethod, CcsAcct.getAcctNbr(), CcsAcct.getAcctType(), loanPlan.loanCode,
				unifieldParServer.BusinessDate());
		// 不是预览，保存分期数据
		if (!isPreview) {
			rCcsLoanReg.save(loanReg);
			
			// 分期付款申请成功
			CcsCustomer customer = rCcsCustomer.findOne(CcsAcct.getCustId());
			String msgCd = fetchMsgCdService.fetchMsgCd(CcsAcct.getProductCd(), CPSMessageCategory.CPS032);
//			messageService.sendMessage(MessageCategory.P01, CcsAcct.getProductCd(), CcsCardLmMapping.getCardNbr(), customer.getName(), customer.getGender(), 
//		    		customer.getMobileNo(), customer.getEmail(), new Date(), new MapBuilder()
//	    			.add("loanType", loanReg.getLoanType())
//	    			.add("amt", loanReg.getLoanInitPrin())
//	    			.add("term", loanReg.getLoanInitTerm())
//	    			.add("loanFee", loanReg.getLoanInitFee())
//	    			.add("nextPayment", loanReg.getLoanFirstTermPrin().add(loanReg.getLoanFirstTermFee()))
//	    			.add("loanFixedFee", loanReg.getLoanFixedFee())
//		    		.build());
//		}
/*		downMsgFacility.sendMessage(msgCd,  CcsCardLmMapping.getCardNbr(), customer.getName(), customer.getGender(), 
	    		customer.getMobileNo(),  new Date(), new MapBuilder()
    			.add("loanType", loanReg.getLoanType())
    			.add("amt", loanReg.getLoanInitPrin())
    			.add("term", loanReg.getLoanInitTerm())
    			.add("loanFee", loanReg.getLoanInitFee())
    			.add("nextPayment", loanReg.getLoanFirstTermPrin().add(loanReg.getLoanFirstTermFee()))
    			.add("loanFixedFee", loanReg.getLoanFixedFee())
	    		.build());
*/	}
		return loanReg.convertToMap();
	}

}
