package com.sunline.ccs.ui.server;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.impl.QueryImpl;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.TxnCodeUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;

/**
 * 历史账单查询
 * 
 * @author dch
 */
@Controller
@RequestMapping(value = "/t1201Server")
public class QueryHistoryBillServer {

	@PersistenceContext
	public EntityManager em;
	@Autowired
	private TxnCodeUtils txnCodeUtil;
	private QCcsTxnHst qTmTxnHst = QCcsTxnHst.ccsTxnHst;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsStatement rStmtHst;

	/**
	 * 获取历史账单信息
	 * 
	 * @param request
	 * @return
	 * @throws FlatException
	 */
	@ResponseBody()
	@RequestMapping(value = "/getStmtList", method = { RequestMethod.POST })
	public FetchResponse getStmtList(@RequestBody FetchRequest request)
			throws FlatException {
		return this.NF2107(request);
	}

	/**
	 * 获取某一历史账单交易记录
	 * 
	 * @param request
	 * @return
	 * @throws FlatException
	 */
	@ResponseBody()
	@RequestMapping(value = "/getStmtTxnList", method = { RequestMethod.POST })
	public FetchResponse getStmtTxnList(@RequestBody FetchRequest request)
			throws FlatException {
		FetchResponse response = this.NF2104(request);
		return response;
	}

	/**
	 * 操作数据库获取某一历史账单中的账户交易信息
	 * 
	 * @param request
	 * @param cardNo
	 * @param stmtMonth
	 * @return
	 * */
	@Transactional
	public FetchResponse NF2104(FetchRequest request) throws FlatException {
		log.debug("操作数据库获取某一历史账单中的账户交易信息开始...");
		JPAQuery query = new JPAQuery(em).from(qTmTxnHst).where(
				qTmTxnHst.org.eq(OrganizationContextHolder.getCurrentOrg()));

		try {
			// 账户号
			String acctNbr = (String) request.getParameter(CcsAcct.P_AcctNbr) == null ? null
					: request.getParameter(CcsAcct.P_AcctNbr).toString();
			
			Calendar calendar =Calendar.getInstance();
			String stmtMonthStr=null;
			Date stmtMonth=new Date();
			if(request.getParameter(CcsStatement.P_StmtDate) != null){
				stmtMonthStr =  request.getParameter(CcsStatement.P_StmtDate).toString();
			/** 账单查询可能频繁点击，使用SimpleDateFormat可能引起线程安全问题--lsy20151017
			* stmtMonth:yyyy-DD-mm
			* 使用公历日历
			*/
				if(calendar instanceof GregorianCalendar){
					//Calendar的月份从0计算
					calendar.set(Integer.valueOf(stmtMonthStr.substring(0,4)),Integer.valueOf(stmtMonthStr.substring(5,7))-1,Integer.valueOf(stmtMonthStr.substring(8,10)));
					stmtMonth=calendar.getTime();
				}
			}
 			if (acctNbr != null) {
				query = query.where(qTmTxnHst.acctNbr.eq(Long
						.parseLong(acctNbr)));
			}
			if (stmtMonth != null) {
				query = query.where(qTmTxnHst.stmtDate.eq(stmtMonth));
			}
		} catch (Exception e) {
			log.debug("" + e);

			throw new FlatException("历史账单中的账户交易信息报错：" + e.getMessage());
		}
		return new JPAQueryFetchResponseBuilder(request, query)
				.addFieldMapping(CcsTxnHst.P_TxnSeq, qTmTxnHst.txnSeq)
				.addFieldMapping(CcsTxnHst.P_AcctNbr, qTmTxnHst.acctNbr)
				.addFieldMapping(CcsTxnHst.P_AcctType, qTmTxnHst.acctType)
				.addFieldMapping(CcsTxnHst.P_CardNbr, qTmTxnHst.cardNbr)
				.addFieldMapping(CcsTxnHst.P_LogicCardNbr,
						qTmTxnHst.logicCardNbr)
				.addFieldMapping(CcsTxnHst.P_CardBasicNbr,
						qTmTxnHst.cardBasicNbr)
				.addFieldMapping(CcsTxnHst.P_TxnDate, qTmTxnHst.txnDate)
				.addFieldMapping(CcsTxnHst.P_TxnTime, qTmTxnHst.txnTime)
				.addFieldMapping(CcsTxnHst.P_TxnCode, qTmTxnHst.txnCode)
				.addFieldMapping(CcsTxnHst.P_DbCrInd, qTmTxnHst.dbCrInd)
				.addFieldMapping(CcsTxnHst.P_TxnAmt, qTmTxnHst.txnAmt)
				.addFieldMapping(CcsTxnHst.P_PostAmt, qTmTxnHst.postAmt)
				.addFieldMapping(CcsTxnHst.P_PostDate, qTmTxnHst.postDate)
				.addFieldMapping(CcsTxnHst.P_AuthCode, qTmTxnHst.authCode)
				.addFieldMapping(CcsTxnHst.P_TxnCurrency, qTmTxnHst.txnCurrency)
				.addFieldMapping(CcsTxnHst.P_PostCurrency,
						qTmTxnHst.postCurrency)
				.addFieldMapping(CcsTxnHst.P_OrigTransDate,
						qTmTxnHst.origTransDate)
				.addFieldMapping(CcsTxnHst.P_PlanNbr, qTmTxnHst.planNbr)
				.addFieldMapping(CcsTxnHst.P_RefNbr, qTmTxnHst.refNbr)
				.addFieldMapping(CcsTxnHst.P_TxnShortDesc,
						qTmTxnHst.txnShortDesc)
				.addFieldMapping(CcsTxnHst.P_TxnDesc, qTmTxnHst.txnDesc)
				.addFieldMapping(CcsTxnHst.P_Points, qTmTxnHst.points)
				.addFieldMapping(CcsTxnHst.P_PostingFlag, qTmTxnHst.postingFlag)
				.addFieldMapping(CcsTxnHst.P_RelPmtAmt, qTmTxnHst.relPmtAmt)
				.addFieldMapping(CcsTxnHst.P_OrigPmtAmt, qTmTxnHst.origPmtAmt)
				.addFieldMapping(CcsTxnHst.P_AcqBranchIq, qTmTxnHst.acqBranchIq)
				.addFieldMapping(CcsTxnHst.P_AcqAddress, qTmTxnHst.acqAddress)
				.addFieldMapping(CcsTxnHst.P_AcqTerminalId,
						qTmTxnHst.acqTerminalId)
				.addFieldMapping(CcsTxnHst.P_Mcc, qTmTxnHst.mcc)
				.addFieldMapping(CcsTxnHst.P_OrigTxnCode, qTmTxnHst.origTxnCode)
				.addFieldMapping(CcsTxnHst.P_OrigTxnAmt, qTmTxnHst.origTxnAmt)
				.addFieldMapping(CcsTxnHst.P_OrigSettAmt, qTmTxnHst.origSettAmt)
				.addFieldMapping(CcsTxnHst.P_InterchangeFee,
						qTmTxnHst.interchangeFee)
				.addFieldMapping(CcsTxnHst.P_FeePayout, qTmTxnHst.feePayout)
				.addFieldMapping(CcsTxnHst.P_FeeProfit, qTmTxnHst.feeProfit)
				.addFieldMapping(CcsTxnHst.P_LoanIssueProfit,
						qTmTxnHst.loanIssueProfit)
				.addFieldMapping(CcsTxnHst.P_StmtDate, qTmTxnHst.stmtDate)
				.addFieldMapping(CcsTxnHst.P_VoucherNo, qTmTxnHst.voucherNo)
				.addFieldMapping(CcsTxnHst.P_Term,qTmTxnHst.term)
				.build();

	}

	/**
	 * 操作数据库获取账户历史账单
	 * 
	 * @param request
	 * @param cardNO
	 * @param startDate
	 * @param endDate
	 * @return
	 * */
	@Transactional
	public FetchResponse NF2107(FetchRequest request) throws FlatException {
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		log.debug("操作数据库获取账户历史账单开始...");
		FetchResponse response = new FetchResponse();
		
		String contrNbr = request.getParameter(CcsAcct.P_ContrNbr) == null ? null
				: request.getParameter(CcsAcct.P_ContrNbr).toString();
		// 开始日期
//		if(request.getParameter("bDate") != null&&!"null".equals(request.getParameter("bDate"))){
//		Date startDate = new Date(Long.parseLong(request.getParameter("bDate")+"" ));
//		}
//		// 结束日期
//		if(request.getParameter("eDate") != null&&!"null".equals(request.getParameter("eDate"))){
//		Date endDate = new Date(Long.parseLong(request.getParameter("eDate")+"" ));
//		}
		int startPageIndex=request.getPage();
		int startPageSize=request.getPageSize();
		
		// 根据合同号获取对应账户号
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT stmt.org as org,stmt.acctNbr as acctNbr,stmt.acctType as acctType,stmt.stmtDate as stmtDate,stmt.name as name,stmt.pmtDueDate as pmtDueDate,stmt.currency as currency,stmt.stmtFlag as stmtFlag,stmt.creditLmt as creditLmt,stmt.tempLmt as tempLmt,stmt.tempLmtBegDate as tempLmtBegDate,stmt.tempLmtEndDate as tempLmtEndDate,stmt.lastStmtDate as lastStmtDate,stmt.stmtBegBal as stmtBegBal,stmt.stmtCurrBal as stmtCurrBal,stmt.qualGraceBal as qualGraceBal,stmt.totDueAmt as totDueAmt,stmt.ctdCashAmt as ctdCashAmt,stmt.ctdCashCnt as ctdCashCnt,stmt.ctdRetailAmt as ctdRetailAmt,stmt.ctdRetailCnt as ctdRetailCnt,stmt.ctdRepayAmt as ctdRepayAmt,stmt.ctdRepayCnt as ctdRepayCnt,stmt.ctdDbAdjAmt as ctdDbAdjAmt,stmt.ctdDbAdjCnt as ctdDbAdjCnt,stmt.ctdCrAdjAmt as ctdCrAdjAmt,stmt.ctdCrAdjCnt as ctdCrAdjCnt,stmt.ctdFeeAmt as ctdFeeAmt,stmt.ctdFeeCnt as ctdFeeCnt,stmt.ctdInterestAmt as ctdInterestAmt,stmt.ctdInterestCnt as ctdInterestCnt,stmt.ctdRefundAmt as ctdRefundAmt,stmt.ctdRefundCnt as ctdRefundCnt,stmt.ctdAmtDb as ctdAmtDb,stmt.ctdNbrDb as ctdNbrDb,stmt.ctdAmtCr as ctdAmtCr,stmt.ctdNbrCr as ctdNbrCr,stmt.ageCode as ageCode,stmt.graceDaysFullInd as graceDaysFullInd,stmt.ovrlmtDate as ovrlmtDate,stmt.pointsBegBal as pointsBegBal,stmt.ctdPoints as ctdPoints,stmt.ctdAdjPoints as ctdAdjPoints,stmt.ctdSpendPoints as ctdSpendPoints,stmt.pointsBal as pointsBal,stmt.blockCode as blockCode,stmt.dualBillingFlag as dualBillingFlag,stmt.cashLmtRate as cashLmtRate,stmt.loanLmtRate as loanLmtRate,stmt.currDueAmt as currDueAmt,stmt.pastDueAmt1 as pastDueAmt1,stmt.pastDueAmt2 as pastDueAmt2,stmt.pastDueAmt3 as pastDueAmt3,stmt.pastDueAmt4 as pastDueAmt4,stmt.pastDueAmt5 as pastDueAmt5,stmt.pastDueAmt6 as pastDueAmt6,stmt.pastDueAmt7 as pastDueAmt7,stmt.pastDueAmt8 as pastDueAmt8,stmt.defaultLogicCardNbr as defaultLogicCardNbr,stmt.email as email,stmt.gender as gender,stmt.mobileNo as mobileNo,stmt.stmtAddress as stmtAddress,stmt.stmtCity as stmtCity,stmt.stmtCountryCode as stmtCountryCode,stmt.stmtDistrict as stmtDistrict,stmt.stmtMediaType as stmtMediaType,stmt.stmtState as stmtState,stmt.stmtPostcode as stmtPostcode,stmt.lastStmtBal as lastStmtBal,stmt.ctdStmtBal as ctdStmtBal,stmt.createTime as createTime,stmt.createUser as createUser,stmt.lstUpdTime as lstUpdTime,stmt.lstUpdUser as lstUpdUser FROM CcsStatement stmt");
		sql.append(" WHERE ORG=:org");
		sql.append(" AND acctNbr=:acctNbr");
		StringBuffer countSql = new StringBuffer();
		countSql.append("SELECT count(*) FROM CcsStatement stmt_ WHERE ORG=:org AND acctNbr=:acctNbr");
		if(request.getParameter("bDate")!=null&&!"null".equals(request.getParameter("bDate"))){
			sql.append(" AND stmtDate>=:startDate");
			countSql.append(" AND stmtDate>=:startDate");
		}
		if(request.getParameter("eDate")!=null&&!"null".equals(request.getParameter("eDate"))){
			sql.append(" AND stmtDate<=:endDate");
			countSql.append(" AND stmtDate<=:endDate");
		}
		if(request.getSortname()!=null&&request.getSortorder()!=null){
			String sortName=request.getSortname();
			String sortOrder=request.getSortorder();
			sql.append(" order by stmt."+sortName+" "+sortOrder);
		}
		sql.append(" order by stmt.stmtDate");
		if (contrNbr != null && !"null".equals(contrNbr)) {
			
			Long acctNbr = rCcsAcct.findOne(qCcsAcct.contrNbr.eq(contrNbr))
					.getAcctNbr();
			Query query = em.createQuery(sql.toString());
			Query countQuery = em.createQuery(countSql.toString());
			query.setParameter("org",OrganizationContextHolder.getCurrentOrg());
			query.setParameter("acctNbr",acctNbr);
			countQuery.setParameter("org",OrganizationContextHolder.getCurrentOrg());
			countQuery.setParameter("acctNbr",acctNbr);
			if(request.getParameter("bDate")!=null&&!"null".equals(request.getParameter("bDate"))){
				query.setParameter("startDate", new Date(Long.parseLong(request.getParameter("bDate")+"" )));
				countQuery.setParameter("startDate", new Date(Long.parseLong(request.getParameter("bDate")+"" )));
			}
			if(request.getParameter("eDate")!=null&&!"null".equals(request.getParameter("eDate"))){
				query.setParameter("endDate", new Date(Long.parseLong(request.getParameter("eDate")+"" )));
				countQuery.setParameter("endDate", new Date(Long.parseLong(request.getParameter("eDate")+"" )));
			}
				query.unwrap(QueryImpl.class).setResultTransformer(Transformers.aliasToBean(CcsStatement.class));
			query.setFirstResult((startPageIndex-1)*startPageSize);
			query.setMaxResults(startPageSize);
			
			int count = Integer.valueOf(countQuery.getSingleResult()+"");
			response.setTotal(count);
			response.setRows(query.getResultList());
//			if(query.getResultList()!=null) response.setTotal(query.getResultList().size());
			
		}
		return response;
	}
}
