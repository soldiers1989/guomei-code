package com.sunline.ccs.batch.cc3100;


import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.cc2100.S2101TxnAdjImp;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnWaiveLogHst;
import com.sunline.ppy.dictionary.enums.InputSource;

/**
 * 
 * @see 类名：P2102TxnWaiveImp
 * @see 描述：自动豁免成功交易重新待入账预处理
 *
 * @see 创建日期：   2015-11-13
 * @author liuqi
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P3100TxnWaiveImp implements ItemProcessor<CcsTxnWaiveLog, Object> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	CustAcctCardFacility custAcctCardQueryFacility;
	
	@Autowired
	private BatchStatusFacility batchFacility;

	@Autowired
	private TxnPrepare txnPrepare;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public S2101TxnAdjImp process(CcsTxnWaiveLog txn) throws Exception {
		
		OrganizationContextHolder.setCurrentOrg(txn.getOrg());
		
		try {
			//日志表记录全量出报表
			//待入账的调账交易:VoidInd=N && (b039=00||b039=11)
			CcsTxnWaiveLogHst txnHst = new CcsTxnWaiveLogHst();
			switch(txn.getAdjState()){
			case A:
				CcsPostingTmp post = new CcsPostingTmp();
				post.updateFromMap(txn.convertToMap());
				post.setPostCurrency(txn.getCurrency());
				post.setFeeProfit(BigDecimal.ZERO);
				post.setFeePayout(BigDecimal.ZERO);
				post.setLoanIssueProfit(BigDecimal.ZERO);
				post.setPostAmt(txn.getTxnAmt());
				post.setTxnCurrency(txn.getCurrency());
				post.setPostCurrency(txn.getCurrency());
				post.setOrigTxnAmt(txn.getTxnAmt());
				post.setOrigSettAmt(txn.getTxnAmt());
				post.setOrigTxnCode(txn.getTxnCode());
				post.setPlanNbr(txn.getPlanNbr());
				
				//交易日期时间优先取post.TxnDate,若null则取b007, b007也null则new Date()
				if(txn.getTxnDate() != null){
					post.setTxnDate(txn.getTxnDate());
				}else{
					post.setTxnDate(batchFacility.getBatchDate());
				}
				if(txn.getOpTime() != null){
					post.setTxnTime(txn.getOpTime());
				}else{
					post.setTxnTime(batchFacility.getBatchDate());
				}
				
				//交易预处理
				txnPrepare.txnPrepare(post, InputSource.BANK);
				txnHst.updateFromMap((txn.convertToMap()));
				em.persist(txnHst);
				em.remove(txn);
				break;
			case R:
				txnHst.updateFromMap((txn.convertToMap()));
				em.persist(txnHst);
				em.remove(txn);
				break;
			case W:
				//待处理
				break;
			default : throw new IllegalArgumentException("账务调整批量作业异常, 操作序列号:["+txn.getOpSeq()+"]");
			}
			return null;
			
		} catch (Exception e) {
			logger.error("账务调整批量作业异常, 操作序列号:["+txn.getOpSeq()+"]");
			throw e;
		}
	}
}
