package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCssfeeReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13081Req;
import com.sunline.ccs.service.protocol.S13081Resp;
import com.sunline.ccs.service.protocol.S13081Txn;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQBeLoanRTxnList
 * @see 描述：可转分期交易明细查询
 *
 * @see 创建日期： 2015年6月24日 下午6:55:31
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQBeLoanRTxnList {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
	@PersistenceContext
	private EntityManager em;

	QCcsCard qCcsCard = QCcsCard.ccsCard;
	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
	QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;
	QCcsTxnUnstatement qCcsTxnUnstatement = QCcsTxnUnstatement.ccsTxnUnstatement;
	QCcsCssfeeReg qCcsCssfeeReg = QCcsCssfeeReg.ccsCssfeeReg;
	QCcsLoanRegHst qCcsLoanRegHst = QCcsLoanRegHst.ccsLoanRegHst;

	@Transactional
	public S13081Resp handler(S13081Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13081", "可转分期交易明细查询", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);

		// 获取卡片信息
		CcsCard card = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(card, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByAcctNbrCurrency(req.getCurr_cd(), card.getAcctNbr());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 主查询
		JPAQuery query = new JPAQuery(em);
		LoanPlan loanPlan;
		loanPlan = unifiedParamFacilityProvide.loanPlan(card.getProductCd(), LoanType.R);

		// 获取账户层参数
		// AccountAttribute accountAttribute =
		// unifiedParameterFacilityProvide.acct_attribute(CcsAcct.getProductCd());
		// Date businessDate = unifiedParameterFacilityProvide.BusinessDate();

		// 根据卡号找到主卡对应的消费计划
		JPAQuery queryPlan = new JPAQuery(em);
		List<BigDecimal> currBalList = queryPlan.from(qCcsPlan).where(qCcsPlan.logicCardNbr.eq(card.getCardBasicNbr()).and(qCcsPlan.planType.eq(PlanType.R)))
				.list(qCcsPlan.currBal);
		if (currBalList.isEmpty()) {
			throw new ProcessException(Constants.ERRL009_CODE, Constants.ERRL009_MES);
		}

		BooleanExpression booleanExpression = qCcsTxnUnstatement.acctNbr.eq(card.getAcctNbr()).and(qCcsTxnUnstatement.txnCode.in(loanPlan.txnCdList));
		// 若"000"则全币种，即非全币种则增加币种对应的账户类型条件
		if (!Constants.DEFAULT_CURR_CD.equals(req.getCurr_cd())) {
			AccountType accountType;
			accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(req.getCard_no(), req.getCurr_cd());

			booleanExpression = booleanExpression.and(qCcsTxnUnstatement.acctType.eq(accountType));
		}

		List<CcsTxnUnstatement> txnUnstmtList = query.from(qCcsTxnUnstatement).where(booleanExpression)
				.orderBy(qCcsTxnUnstatement.txnDate.desc(), qCcsTxnUnstatement.cardNbr.asc()).offset(req.getFirstrow())
				.limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsTxnUnstatement);

		ArrayList<S13081Txn> txns = new ArrayList<S13081Txn>();

		for (CcsTxnUnstatement txnUnstatement : txnUnstmtList) {

			// 临额不参与分期
			// if(!loanPlan.useTemplimit){
			// //允许消费分期的最大金额，分期参数的期数不同，最大金额所有期数对应的最大金额
			// BigDecimal txnLoanMaxAmt = BigDecimal.ZERO;
			// BigDecimal txnLoanAmt = BigDecimal.ZERO;
			// Map<Integer, LoanFeeDef> loanFeedefMap = loanPlan.loanFeeDefMap;
			// for(Integer key : loanFeedefMap.keySet()){
			// // 获取分期参数
			// LoanFeeDef loanFeeDef =
			// unifiedParameterFacilityProvide.loanFeeDef(CcsCard.getProductCd(),
			// LoanType.R, key);
			//
			// txnLoanAmt =
			// tempLmtNotConductLoanProvider.TxnLoanMaxAmt(tmTxnUnstmt, CcsAcct,
			// CcsCard, currBalList, accountAttribute, loanFeeDef,
			// businessDate);
			// txnLoanMaxAmt = txnLoanAmt.compareTo(txnLoanMaxAmt) > 0 ?
			// txnLoanAmt : txnLoanMaxAmt;
			// }
			// }

			SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
			S13081Txn txn = new S13081Txn();

			JPAQuery regQuery = new JPAQuery(em);
			JPAQuery loanQuery = new JPAQuery(em);

			// 根据卡号/交易号和机构号查询分期注册表是否有数据
			CcsLoanReg loanReg = regQuery
					.from(qCcsLoanReg)
					.where(qCcsLoanReg.cardNbr.eq(txnUnstatement.getCardNbr()).and(qCcsLoanReg.refNbr.eq(txnUnstatement.getRefNbr()))
							.and(qCcsLoanReg.org.eq(txnUnstatement.getOrg()))).singleResult(qCcsLoanReg);
			CcsLoan loan = loanQuery
					.from(qCcsLoan)
					.where(qCcsLoan.cardNbr.eq(txnUnstatement.getCardNbr()).and(qCcsLoan.refNbr.eq(txnUnstatement.getRefNbr()))
							.and(qCcsLoan.org.eq(txnUnstatement.getOrg()))).singleResult(qCcsLoan);
			if (loanReg != null) { // 分期注册表有数据，is_loan的值“Y”
				txn.setIs_loan(Indicator.Y);
			} else {
				if (loan != null) { // 分期表有数据，is_loan的值“Y”
					txn.setIs_loan(Indicator.Y);
				} else {
					txn.setIs_loan(Indicator.N); // 两个对象都为空，is_loan的值“N”
				}
			}

			txn.setTxn_card_no(txnUnstatement.getCardNbr());
			txn.setTxn_date(txnUnstatement.getTxnDate());
			txn.setTxn_time(sdf.format(txnUnstatement.getTxnTime()));
			txn.setTxn_code(txnUnstatement.getTxnCode());
			txn.setTxn_amt(txnUnstatement.getTxnAmt());
			txn.setPost_amt(txnUnstatement.getPostAmt());
			txn.setPost_curr_cd(txnUnstatement.getPostCurrency());
			txn.setPost_date(txnUnstatement.getPostDate());
			txn.setAuth_code(txnUnstatement.getAuthCode());
			txn.setTxn_curr_cd(txnUnstatement.getTxnCurrency());
			txn.setRef_nbr(txnUnstatement.getRefNbr());
			txn.setTxn_short_desc(txnUnstatement.getTxnShortDesc());
			txn.setPoint(txnUnstatement.getPoints().intValue());
			txn.setAcq_acceptor_id(txnUnstatement.getAcqAcceptorId());
			txn.setAcq_name_addr(txnUnstatement.getAcqAddress());

			txns.add(txn);
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsTxnUnstatement).where(booleanExpression).count();

		// 构建响应报文对象
		S13081Resp resp = new S13081Resp();
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setTxns(txns);

		LogTools.printLogger(logger, "S13081", "可转分期交易明细查询", resp, false);
		return resp;
	}

}
