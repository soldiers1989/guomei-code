package com.sunline.ccs.batch.cc6000;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsXsyncAcctTmp;


/** 
 * @see 类名：P6063GenerateAcctUpdateList
 * @see 描述：生成账户同步数据
 * 			  如果账户的下述各字段有任何一个和程序最初暂存的数值不同，则执行
 * 			  CURR_BAL、CASH_BAL、LOAN_BAL、DISPUTE_AMT、BLOCK_CODE、UNMATCH_DB、UNMATCH_CASH、UNMATCH_CR
 *
 * @see 创建日期：   2015年6月25日 下午2:37:32
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class P6063GenerateAcctUpdateList implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	@Autowired
    private BatchStatusFacility batchFacility;//最新同步日期-20151022
	
	@PersistenceContext
	private EntityManager em;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("生成账户同步数据：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"]");
		}
		if (item.getAccount().getCurrBal().compareTo(item.getPreAccount().getCurrBal()) != 0
				|| item.getAccount().getCashBal().compareTo(item.getPreAccount().getCashBal()) != 0
				|| item.getAccount().getLoanBal().compareTo(item.getPreAccount().getLoanBal()) != 0
				|| item.getAccount().getDisputeAmt().compareTo(item.getPreAccount().getDisputeAmt()) != 0
				|| item.getAccount().getMemoDb().compareTo(item.getPreAccount().getMemoDb()) != 0
				|| item.getAccount().getMemoCr().compareTo(item.getPreAccount().getMemoCr()) != 0
				|| item.getAccount().getMemoCash().compareTo(item.getPreAccount().getMemoCash()) != 0
				|| !blockCodeUtils.isEquals(item.getAccount().getBlockCode(), item.getPreAccount().getBlockCode())
				|| item.getUnmatchDeletes().size()!=0) {
			
			// 增加记录到账户更新临时表处理(TT_ACCOUNT_UPD_LIST)
			CcsXsyncAcctTmp acctUpdList = new CcsXsyncAcctTmp();
			acctUpdList.setAcctNbr(item.getAccount().getAcctNbr());
			acctUpdList.setAcctType(item.getAccount().getAcctType());
			acctUpdList.setBlockCode(item.getAccount().getBlockCode());
			acctUpdList.setCashBal(item.getAccount().getCashBal());
			acctUpdList.setCurrBal(item.getAccount().getCurrBal());
			acctUpdList.setDisputeAmt(item.getAccount().getDisputeAmt());
			acctUpdList.setLoanBal(item.getAccount().getLoanBal());
			acctUpdList.setOrg(item.getAccount().getOrg());
			acctUpdList.setMemoCash(item.getAccount().getMemoCash());
			acctUpdList.setMemoCr(item.getAccount().getMemoCr());
			acctUpdList.setMemoDb(item.getAccount().getMemoDb());
			acctUpdList.setLtdLoanAmt(item.getAccount().getLtdLoanAmt());
			
			//最新同步日期-20151022
			item.getAccount().setLastSyncDate(batchFacility.getSystemStatus().getBusinessDate());
			
			// 数据持久化
			em.persist(acctUpdList);
		}
		
		return item;
	}

}
