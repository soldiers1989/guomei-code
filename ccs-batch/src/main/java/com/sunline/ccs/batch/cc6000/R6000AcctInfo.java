package com.sunline.ccs.batch.cc6000;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.UnmatchStatus;

/** 
 * @see 类名：R6000AcctInfo
 * @see 描述：核心入账程序数据读取
 *
 * @see 创建日期：   2015年6月25日 下午2:40:14
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R6000AcctInfo extends KeyBasedStreamReader<CcsAcctKey, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BatchStatusFacility batchFacility;
	
	@PersistenceContext
	protected EntityManager em;


	@Override
	protected List<CcsAcctKey> loadKeys() {
		QCcsAcct q = QCcsAcct.ccsAcct;
		List<CcsAcctKey> keys = new ArrayList<CcsAcctKey>();

		JPAQuery query = new JPAQuery(em);
		for (Tuple objs : query.from(q).where(q.setupDate.loe(batchFacility.getBatchDate())).list(q.acctNbr, q.acctType))
			keys.add(new CcsAcctKey(objs.get(q.acctNbr), objs.get(q.acctType)));
		
		return keys;
	}

	@Override
	protected S6000AcctInfo loadItemByKey(CcsAcctKey key) {
		S6000AcctInfo info = new S6000AcctInfo();
		CcsAcct account = em.find(CcsAcct.class, key);
		info.setAccount(account);

		//依次取子表
		Long acctNbr = account.getAcctNbr();
		AccountType acctType = account.getAcctType();
		
		//信用计划（子账户）
		QCcsPlan qPlan = QCcsPlan.ccsPlan;
		info.setPlans(new JPAQuery(em)
				.from(qPlan)
				.where(qPlan.acctNbr.eq(acctNbr).and(qPlan.acctType.eq(acctType)))
				.list(qPlan));
		
		//分期还款计划
		QCcsLoan qLoan = QCcsLoan.ccsLoan;
		info.setLoans(new JPAQuery(em)
				.from(qLoan)
				.where(qLoan.acctNbr.eq(acctNbr).and(qLoan.acctType.eq(acctType)))
				.list(qLoan));
		
		//贷款分配信息
		QCcsRepaySchedule qSchedule = QCcsRepaySchedule.ccsRepaySchedule;
		info.setSchedules(new JPAQuery(em)
				.from(qSchedule)
				.where(qSchedule.acctNbr.eq(acctNbr).and(qSchedule.acctType.eq(acctType)))
				.list(qSchedule));
				
		//入账交易
		QCcsPostingTmp qTxnPost = QCcsPostingTmp.ccsPostingTmp;
		info.setTxnPosts(new JPAQuery(em)
				.from(qTxnPost)
				.where(qTxnPost.acctNbr.eq(acctNbr).and(qTxnPost.acctType.eq(acctType)))
				.orderBy(qTxnPost.txnTime.asc())
				.list(qTxnPost));
		
		//授权未达账
		QCcsAuthmemoO qUnmatchO = QCcsAuthmemoO.ccsAuthmemoO;
		info.setUnmatchs(new JPAQuery(em)
				.from(qUnmatchO)
				.where(
						qUnmatchO.acctNbr.eq(acctNbr)
						.and(qUnmatchO.acctType.eq(acctType))
						.and(qUnmatchO.authTxnStatus.in(AuthTransStatus.N, AuthTransStatus.O))
						.and(qUnmatchO.logBizDate.loe(batchFacility.getBatchDate()))
						.and(qUnmatchO.finalAction.eq(AuthAction.A))
						.and(qUnmatchO.txnDirection.in(AuthTransDirection.Advice, AuthTransDirection.Confirm, AuthTransDirection.Normal))
					)
				.list(qUnmatchO));
		
		//授权未入账处理中的借记交易-20151017
		QCcsAuthmemoO qUnmatchO1 = QCcsAuthmemoO.ccsAuthmemoO;
		List<CcsAuthmemoO> onwayMemoList = new JPAQuery(em)
		.from(qUnmatchO1)
		.where(
				qUnmatchO1.acctNbr.eq(acctNbr)
				.and(qUnmatchO1.acctType.eq(acctType))
				.and(qUnmatchO1.authTxnStatus.in(AuthTransStatus.P))
				.and(qUnmatchO1.txnType.in(AuthTransType.AgentDebit))
				.and(qUnmatchO1.logBizDate.loe(batchFacility.getBatchDate()))
//				.and(qUnmatchO1.finalAction.eq(AuthAction.A))
				.and(qUnmatchO1.txnDirection.in(AuthTransDirection.Normal))
			)
		.list(qUnmatchO1);
		if(null != onwayMemoList && onwayMemoList.size()>0){
			info.getUnmatchs().addAll(onwayMemoList);
		}

		//当期账单是否有交易
		QCcsTxnUnstatement qTxnUnstmt = QCcsTxnUnstatement.ccsTxnUnstatement;
		info.setTxnUnstmts(new JPAQuery(em).from(qTxnUnstmt)
				.where(qTxnUnstmt.acctNbr.eq(acctNbr)
						.and(qTxnUnstmt.acctType.eq(acctType))
						.and(qTxnUnstmt.stmtDate.eq(account.getNextStmtDate()))).list(qTxnUnstmt));
		//info.setTxnUnstmtCount(u.intValue());
		
		if (logger.isDebugEnabled()) {
			logger.debug("入账前数据收集：Org["+account.getOrg()
					+"],AcctType["+account.getAcctType()
					+"],acctNbr["+account.getAcctNbr()
					+"],BatchDate["+batchFacility.getBatchDate()
					+"],Plans.size["+info.getPlans().size()
					+"],Loans.size["+info.getLoans().size()
					+"],Schedules.size["+info.getLoans().size()
					+"],TxnPosts.size["+info.getTxnPosts().size()
					+"],Unmatchs.size["+info.getUnmatchs().size()
					+"],NoTrans["+info.getTxnUnstmtCount()
					+"]");
		}

		//	复制账户信息和信用计划信息
		copyAccountStatus(info);

		return info;
	}
	
	/**
	 * 备份账户信息、信用计划信息
	 * 输出总账所需要交易流水时使用
	 * @param accountInfo
	 */
	private void copyAccountStatus(S6000AcctInfo accountInfo){
		CcsAcct preAccount = new CcsAcct();
		preAccount.updateFromMap(accountInfo.getAccount().convertToMap());
		
		List<CcsPlan> prePlans = new ArrayList<CcsPlan>();
		for (CcsPlan plan : accountInfo.getPlans()){
			CcsPlan prePlan = new CcsPlan();
			prePlan.updateFromMap(plan.convertToMap());
			prePlans.add(prePlan);
		}

		for (int i = 0; i < accountInfo.getUnmatchs().size(); i++) {
			accountInfo.getUnmatchStatuses().add(UnmatchStatus.U);
		}

		accountInfo.setPrePlans(prePlans);
		accountInfo.setPreAccount(preAccount);
	}

}
