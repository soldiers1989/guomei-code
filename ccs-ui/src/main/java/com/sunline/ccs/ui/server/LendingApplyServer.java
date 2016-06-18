/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanRegHst;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/** 
 * @see 类名：LendingApplyServer
 * @see 描述：分期申请状态查询
 *
 * @see 创建日期：   2015年6月30日下午9:06:44
 * @author yanjingfeng
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
@RequestMapping("lendingApplyServer")
public class LendingApplyServer {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
	QCcsLoanRegHst qTmLoanRegHst = QCcsLoanRegHst.ccsLoanRegHst;
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value="/getLoanRegList",method={RequestMethod.POST})
	public FetchResponse getLoanRegList(@RequestBody FetchRequest request) throws ProcessException {
		
	        String cardNbr = (String)request.getParameter(CcsLoanReg.P_CardNbr);
	        String isOnThatDay = (String)request.getParameter("isOnThatDay"); 
	        String loanType = (String)request.getParameter(CcsLoanReg.P_LoanType);
	        Date beginDate  = null;
	        Date endDate = null;
		if (request.getParameter("beginDate") != null) {
		    beginDate = new Date(Long.parseLong((String)request.getParameter("beginDate")));
		}
		if (request.getParameter("endDate") != null) {
		    endDate = new Date(Long.parseLong((String)request.getParameter("endDate")));
		}

	        /**
		 * <p>首次进入页面会有一次加载数据的动作</p>
		 * <p>此业务场景中cardNbr是必输项</p>
		 * <p>page端直接控制会导致因为没有CallBack page一直等待的bug，所以在server返回一个空的Response</p>
		 */
		if (StringUtils.isBlank(cardNbr)) {
			return new FetchResponse();
		}
		
		CcsCardO tmCardO = custAcctCardQueryFacility.getCardOByCardNbr(cardNbr);
		if (null == tmCardO) {
			throw new ProcessException("无效的卡号");
		}
		
		//主查询
		JPAQuery query = new JPAQuery(em);
		
		if (Indicator.Y.toString().equals(isOnThatDay)) {
			query = query.from(qCcsLoanReg).where(getTmLoanRegCondByReq(tmCardO.getAcctNbr(), loanType, beginDate, endDate)).orderBy(qCcsLoanReg.registerId.desc());
			return new JPAQueryFetchResponseBuilder(request, query)
					.addFieldMapping(CcsLoanReg.P_Org, qCcsLoanReg.org)
					.addFieldMapping(CcsLoanReg.P_AcctNbr, qCcsLoanReg.acctNbr)
					.addFieldMapping(CcsLoanReg.P_AcctType, qCcsLoanReg.acctType)
					.addFieldMapping(CcsLoanReg.P_RegisterId, qCcsLoanReg.registerId)
					.addFieldMapping(CcsLoanReg.P_CardNbr, qCcsLoanReg.cardNbr)
					.addFieldMapping(CcsLoanReg.P_LogicCardNbr, qCcsLoanReg.logicCardNbr)
					.addFieldMapping(CcsLoanReg.P_RefNbr, qCcsLoanReg.refNbr)
					.addFieldMapping(CcsLoanReg.P_OrigAuthCode, qCcsLoanReg.origAuthCode)
					.addFieldMapping(CcsLoanReg.P_OrigTransDate, qCcsLoanReg.origTransDate)
					.addFieldMapping(CcsLoanReg.P_OrigTxnAmt, qCcsLoanReg.origTxnAmt)
					.addFieldMapping(CcsLoanReg.P_B007TxnTime, qCcsLoanReg.b007TxnTime)
					.addFieldMapping(CcsLoanReg.P_B011Trace, qCcsLoanReg.b011Trace)
					.addFieldMapping(CcsLoanReg.P_B032AcqInst, qCcsLoanReg.b032AcqInst)
					.addFieldMapping(CcsLoanReg.P_B033FwdIns, qCcsLoanReg.b033FwdIns)
					.addFieldMapping(CcsLoanReg.P_LoanCode, qCcsLoanReg.loanCode)
					.addFieldMapping(CcsLoanReg.P_LoanFeeMethod, qCcsLoanReg.loanFeeMethod)
					.addFieldMapping(CcsLoanReg.P_LoanFinalTermFee, qCcsLoanReg.loanFinalTermFee)
					.addFieldMapping(CcsLoanReg.P_LoanFinalTermPrin, qCcsLoanReg.loanFinalTermPrin)
					.addFieldMapping(CcsLoanReg.P_LoanFixedFee, qCcsLoanReg.loanFixedFee)
					.addFieldMapping(CcsLoanReg.P_LoanFixedPmtPrin, qCcsLoanReg.loanFixedPmtPrin)
					.addFieldMapping(CcsLoanReg.P_LoanFirstTermFee, qCcsLoanReg.loanFirstTermFee)
					.addFieldMapping(CcsLoanReg.P_LoanFirstTermPrin, qCcsLoanReg.loanFirstTermPrin)
					.addFieldMapping(CcsLoanReg.P_LoanInitFee, qCcsLoanReg.loanInitFee)
					.addFieldMapping(CcsLoanReg.P_LoanInitPrin, qCcsLoanReg.loanInitPrin)
					.addFieldMapping(CcsLoanReg.P_LoanInitTerm, qCcsLoanReg.loanInitTerm)
					.addFieldMapping(CcsLoanReg.P_LoanType, qCcsLoanReg.loanType)
					.addFieldMapping(CcsLoanReg.P_RequestTime, qCcsLoanReg.requestTime)
					.addFieldMapping(CcsLoanReg.P_RegisterDate, qCcsLoanReg.registerDate)
					.addFieldMapping(CcsLoanReg.P_LoanRegStatus, qCcsLoanReg.loanRegStatus)
					.addFieldMapping(CcsLoanReg.P_TotLifeInsuAmt,qCcsLoanReg.totLifeInsuAmt)
					.addFieldMapping(CcsLoanReg.P_TotPrepayPkgAmt,qCcsLoanReg.totPrepayPkgAmt)
					.addFieldMapping(CcsLoanReg.P_JoinLifeInsuInd,qCcsLoanReg.joinLifeInsuInd)
					.addFieldMapping(CcsLoanReg.P_JoinPrepayPkgInd,qCcsLoanReg.joinPrepayPkgInd)
					.addFieldMapping(CcsLoanReg.P_PrepayPkgFeeMethod,qCcsLoanReg.prepayPkgFeeMethod)
					.addFieldMapping(CcsLoanReg.P_PrepayPkgFeeRate,qCcsLoanReg.prepayPkgFeeRate)
					.addFieldMapping(CcsLoanReg.P_LifeInsuFeeMethod,qCcsLoanReg.lifeInsuFeeMethod)
					.addFieldMapping(CcsLoanReg.P_LifeInsuFeeRate,qCcsLoanReg.lifeInsuFeeRate)
					.build();
		} else {
			if(beginDate != null){
				beginDate = DateUtils.truncate(beginDate, Calendar.DATE);
				logger.debug("分期申请状态查询的起始时间："+beginDate);
			}
			if(endDate != null){
				endDate = DateUtils.truncate(DateUtils.addDays(endDate, 1), Calendar.DATE);
				logger.debug("分期申请状态查询的结束时间："+endDate);
			}
			
			query = query.from(qTmLoanRegHst).where(getTmLoanRegHstCondByReq(tmCardO.getAcctNbr(), loanType, beginDate, endDate)).orderBy(qTmLoanRegHst.registerId.desc());
			
			//重新设置起始位置
			return new JPAQueryFetchResponseBuilder(request, query)
					.addFieldMapping(CcsLoanReg.P_Org, qTmLoanRegHst.org)
					.addFieldMapping(CcsLoanReg.P_AcctNbr, qTmLoanRegHst.acctNbr)
					.addFieldMapping(CcsLoanReg.P_AcctType, qTmLoanRegHst.acctType)
					.addFieldMapping(CcsLoanReg.P_RegisterId, qTmLoanRegHst.registerId)
					.addFieldMapping(CcsLoanReg.P_CardNbr, qTmLoanRegHst.cardNbr)
					.addFieldMapping(CcsLoanReg.P_LogicCardNbr, qTmLoanRegHst.logicCardNbr)
					.addFieldMapping(CcsLoanReg.P_RefNbr, qTmLoanRegHst.refNbr)
					.addFieldMapping(CcsLoanReg.P_OrigAuthCode, qTmLoanRegHst.origAuthCode)
					.addFieldMapping(CcsLoanReg.P_OrigTransDate, qTmLoanRegHst.origTransDate)
					.addFieldMapping(CcsLoanReg.P_OrigTxnAmt, qTmLoanRegHst.origTxnAmt)
					.addFieldMapping(CcsLoanReg.P_B007TxnTime, qTmLoanRegHst.b007TxnTime)
					.addFieldMapping(CcsLoanReg.P_B011Trace, qTmLoanRegHst.b011Trace)
					.addFieldMapping(CcsLoanReg.P_B032AcqInst, qTmLoanRegHst.b032AcqInst)
					.addFieldMapping(CcsLoanReg.P_B033FwdIns, qTmLoanRegHst.b033FwdIns)
					.addFieldMapping(CcsLoanReg.P_LoanCode, qTmLoanRegHst.loanCode)
					.addFieldMapping(CcsLoanReg.P_LoanFeeMethod, qTmLoanRegHst.loanFeeMethod)
					.addFieldMapping(CcsLoanReg.P_LoanFinalTermFee, qTmLoanRegHst.loanFinalTermFee)
					.addFieldMapping(CcsLoanReg.P_LoanFinalTermPrin, qTmLoanRegHst.loanFinalTermPrin)
					.addFieldMapping(CcsLoanReg.P_LoanFixedFee, qTmLoanRegHst.loanFixedFee)
					.addFieldMapping(CcsLoanReg.P_LoanFixedPmtPrin, qTmLoanRegHst.loanFixedPmtPrin)
					.addFieldMapping(CcsLoanReg.P_LoanFirstTermFee, qTmLoanRegHst.loanFirstTermFee)
					.addFieldMapping(CcsLoanReg.P_LoanFirstTermPrin, qTmLoanRegHst.loanFirstTermPrin)
					.addFieldMapping(CcsLoanReg.P_LoanInitFee, qTmLoanRegHst.loanInitFee)
					.addFieldMapping(CcsLoanReg.P_LoanInitPrin, qTmLoanRegHst.loanInitPrin)
					.addFieldMapping(CcsLoanReg.P_LoanInitTerm, qTmLoanRegHst.loanInitTerm)
					.addFieldMapping(CcsLoanReg.P_LoanType, qTmLoanRegHst.loanType)
					.addFieldMapping(CcsLoanReg.P_RequestTime, qTmLoanRegHst.requestTime)
					.addFieldMapping(CcsLoanReg.P_RegisterDate, qTmLoanRegHst.registerDate)
					.addFieldMapping(CcsLoanReg.P_LoanRegStatus, qTmLoanRegHst.loanRegStatus)
					.build();
		}
	}
	
	/**
	 * 根据传入的条件统一生成TM_LOAN_REG条件语句
	 * @param cardNbr
	 * @param loanType
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	private BooleanExpression getTmLoanRegCondByReq(Long acctNbr, String loanType, Date beginDate, Date endDate) {
		
		BooleanExpression booleanExpression = qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg())
												.and(qCcsLoanReg.acctNbr.eq(acctNbr));
		
		//如果分期类型为空则全量查询，否则按条件查询
		if (null != loanType) {
			booleanExpression = booleanExpression.and(qCcsLoanReg.loanType.eq(LoanType.valueOf(loanType)));
		}
				
		return booleanExpression;
	}
	
	/**
	 * 根据传入的条件统一生成TM_LOAN_REG条件语句
	 * @param cardNbr
	 * @param loanType
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	private BooleanExpression getTmLoanRegHstCondByReq(Long acctNbr, String loanType, Date beginDate, Date endDate) {
		
		BooleanExpression booleanExpression = qTmLoanRegHst.org.eq(OrganizationContextHolder.getCurrentOrg())
												.and(qTmLoanRegHst.acctNbr.eq(acctNbr));
		
		//如果分期类型为空则全量查询，否则按条件查询
		if (null != loanType) {
			booleanExpression = booleanExpression.and(qTmLoanRegHst.loanType.eq(LoanType.valueOf(loanType)));
		}
				
		//如果请求中存在起止时间则加上时间条件
		if(null != beginDate){
			booleanExpression = booleanExpression.and(qTmLoanRegHst.requestTime.goe(beginDate));
		}
		if(null != endDate){
			booleanExpression = booleanExpression.and(qTmLoanRegHst.requestTime.lt(endDate));
		}
		
		return booleanExpression;
	}
}
