package com.sunline.ccs.batch.cc6000.interestaccrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.cc6000.S6000AcctInfo;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.DbCrInd;


/**
 * @see 类名：InterestReview
 * @see 描述： 利息的回溯
 *
 * @see 创建日期：   2015-11-11
 * @author liuqi
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class InterestReview{
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
    private BatchStatusFacility batchFacility;
	@PersistenceContext
	protected EntityManager em;
	
	/**
	 *  利息的回溯
	 * @param item
	 * @param postAmt 还款的金额
	 */
	public BigDecimal accumulateInterReview(S6000AcctInfo item,BigDecimal postAmt,Date txnDate, List<CcsPostingTmp> newTxnPosts)
	{
		CcsAcct acct = item.getAccount();
		logger.debug("利息回溯信息收集:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
				"交易日=["+postAmt+"],上一账单日=["+acct.getLastStmtDate()+"]");
		//不考虑已出的账单，溢缴款，冲销顺序无法匹配不考虑
//		CcsStatement ctdStatement = null;
		Date beginDate = txnDate;
		if(acct.getLastStmtDate() != null){
			//跨账单日
//			ctdStatement = getStatement(acct, txnDate);
			if( acct.getLastStmtDate().compareTo(txnDate) >= 0 ){
				beginDate = acct.getLastStmtDate();
			}
		}
		
		//当期的所有贷记交易
		BigDecimal reviewAmt = BigDecimal.ZERO;
//		if(ctdStatement != null){
//			int days = DateUtils.getIntervalDays(beginDate,batchFacility.getBatchDate());
//			if(ctdStatement.getTotDueAmt().compareTo(postAmt)<0){
//				reviewAmt = ctdStatement.getTotDueAmt().multiply(new BigDecimal(days));
//				postAmt = postAmt.subtract(ctdStatement.getTotDueAmt());
//			}else{
//				reviewAmt = postAmt.multiply(new BigDecimal(days));
//				postAmt = BigDecimal.ZERO;
//			}
//		}
		if(postAmt.compareTo(BigDecimal.ZERO)<=0){
			return reviewAmt; 
		}
		List<CcsTxnHst> txnDList = this.getDTxn(item.getAccount(), beginDate);
		if(txnDList.size()>0){
			// 查找系统内部交易类型对照表
			SysTxnCdMapping sysTxnCdXFR = parameterFacility.loadParameter(SysTxnCd.S52, SysTxnCdMapping.class);
			// 根据交易码，查找交易码对象
			TxnCd txnCodeXFR = parameterFacility.loadParameter(sysTxnCdXFR.txnCd, TxnCd.class);
			
			// 查找系统内部交易类型对照表
			SysTxnCdMapping sysTxnMCAT = parameterFacility.loadParameter(SysTxnCd.S80, SysTxnCdMapping.class);
			// 根据交易码，查找交易码对象
			TxnCd txnCodeMCAT = parameterFacility.loadParameter(sysTxnMCAT.txnCd, TxnCd.class);
			
			// 查找系统内部交易类型对照表
			SysTxnCdMapping sysTxnMCAI = parameterFacility.loadParameter(SysTxnCd.S81, SysTxnCdMapping.class);
			// 根据交易码，查找交易码对象
			TxnCd txnCodeMCAI = parameterFacility.loadParameter(sysTxnMCAI.txnCd, TxnCd.class);
						
			for(CcsTxnHst txnHst:txnDList){
				logger.debug("利息回溯信息收集:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
						"交易金额:["+txnHst.getPostAmt()+"],交易码:["+txnHst.getTxnCode()+"],reviewAmt:["+reviewAmt+"]");
				if(postAmt.compareTo(BigDecimal.ZERO)>0){
					if(txnHst.getTxnCode().equals(txnCodeXFR.txnCd) || 
							txnHst.getTxnCode().equals(txnCodeMCAT.txnCd) || 
							txnHst.getTxnCode().equals(txnCodeMCAI.txnCd) ){
						//本金
						int days = DateUtils.getIntervalDays(txnHst.getPostDate(),batchFacility.getBatchDate());
						if(postAmt.compareTo(txnHst.getPostAmt()) > 0){
							reviewAmt = txnHst.getPostAmt().multiply(new BigDecimal(days));
							postAmt =  postAmt.subtract(txnHst.getPostAmt());
						}else{
							reviewAmt = postAmt.multiply(new BigDecimal(days));
							postAmt = BigDecimal.ZERO;
						}
						CcsPlan plan = getTxnPlan(item, txnHst);
						
						if(plan != null){
							logger.debug("本金基数维护前:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
									"planid:["+plan.getPlanId()+"],plan本金累计基数:["+plan.getAccruPrinSum()+"],reviewAmt:["+reviewAmt+"],postAmt:["+postAmt+"]");
							
							if((plan.getAccruPrinSum().subtract(reviewAmt)).compareTo(BigDecimal.ZERO)>0){
								plan.setAccruPrinSum(plan.getAccruPrinSum().subtract(reviewAmt));
							}else{
								plan.setAccruPrinSum(BigDecimal.ZERO);
							}
							logger.debug("本金基数维护后:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
									"planid:["+plan.getPlanId()+"],plan本金累计基数:["+plan.getAccruPrinSum()+"],reviewAmt:["+reviewAmt+"],postAmt:["+postAmt+"]");
							
						}
						
					}else{
						//非本金不累计
						if(postAmt.compareTo(txnHst.getPostAmt()) > 0){
							postAmt =  postAmt.subtract(txnHst.getPostAmt());
						}else{
							postAmt = BigDecimal.ZERO;
						}
					}
				}else{
					break;
				}
			}
			
			logger.debug("利息回溯信息收集:账户编号：["+acct.getAcctNbr()+"],账户类型:["+acct.getAcctType()+"]," +
					"回溯本金累计金额:["+reviewAmt+"]");
		}
		


		return reviewAmt;
		
	}
	
	private List<CcsTxnHst> getDTxn(CcsAcct acct,Date beginDate){
		//获取入账日在账单日之后交易日txnDate之前,所有借记入账交易
		QCcsTxnHst qccsTxnHst = QCcsTxnHst.ccsTxnHst;
		List<CcsTxnHst> txnHstList = new ArrayList<CcsTxnHst>();
		txnHstList = new JPAQuery(em).from(qccsTxnHst)
			.where(qccsTxnHst.postDate.goe(beginDate)
					.and(qccsTxnHst.dbCrInd.eq(DbCrInd.D))
					.and(qccsTxnHst.acctNbr.eq(acct.getAcctNbr()).and(qccsTxnHst.acctType.eq(acct.getAcctType())))
					).orderBy(qccsTxnHst.txnDate.asc())
			.list(qccsTxnHst);
		return txnHstList;
	}
	
//	private CcsStatement getStatement(CcsAcct acct,Date stmtDate){
//		//获取交易日txnDate前的账单日
//		QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;
//		CcsStatement statement = new JPAQuery(em).from(qCcsStatement)
//			.where(qCcsStatement.acctNbr.eq(acct.getAcctNbr())
//					.and(qCcsStatement.acctType.eq(acct.getAcctType()))
//					.and(qCcsStatement.stmtDate.eq(stmtDate)))
//					.singleResult(qCcsStatement);
//		return statement;
//	}
	
	private CcsPlan getTxnPlan(S6000AcctInfo item,CcsTxnHst txnHst){
		CcsPlan plan = null;
		if(txnHst.getRefNbr() == null ){
			return plan;
		}
		int term = txnHst.getTerm()==null?0:txnHst.getTerm();
		for(CcsPlan p:item.getPlans()){
			
			if(p.getTerm() !=null && p.getRefNbr().equals(txnHst.getRefNbr()) && p.getTerm().equals(term) && p.getPlanNbr().equals(txnHst.getPlanNbr())){
				plan = p;
				break;
			}
		}
		return plan;
	}
	
}


