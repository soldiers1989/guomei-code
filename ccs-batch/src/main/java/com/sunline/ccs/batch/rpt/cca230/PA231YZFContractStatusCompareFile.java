/**
 * 
 */
package com.sunline.ccs.batch.rpt.cca230;


import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.rpt.cca230.items.YZFContractStatusCompareFileItem;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.param.def.consts.CooperationCode;

/**  
 * @描述		: 生成合同对账文件processor
 *  
 * @作者		: JiaoJian 
 * @创建时间	: 2015年11月26日  下午3:39:54   
 */
public class PA231YZFContractStatusCompareFile implements ItemProcessor<ContractCompareCcsLoanKey, YZFContractStatusCompareFileItem>{

	private static final Logger logger = LoggerFactory.getLogger(PA231YZFContractStatusCompareFile.class);
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Override
	public YZFContractStatusCompareFileItem process(ContractCompareCcsLoanKey loan) throws Exception {
		JPAQuery query = new JPAQuery(em);
		//查询账户
		QCcsAcct acct = QCcsAcct.ccsAcct;
		List<Tuple> acctTupleRows = query.from(acct).where(acct.acctNbr.eq(loan.getAcctNbr()).and(acct.acqId.eq(CooperationCode.YZF_ACQ_ID)))
										 .list(acct.acqId, acct.custId);
		if(null == acctTupleRows || acctTupleRows.size() == 0) {
			logger.error("生成合同状态对账文件，账户编号acctNbr：{}, 合作方编码：acqId:{} ，没有查询到账户信息",
					loan.getAcctNbr(), CooperationCode.YZF_ACQ_ID);
			return null;
		}
		Tuple acctTupleRow = acctTupleRows.get(0);
		
		//查询客户
		QCcsCustomer customer = QCcsCustomer.ccsCustomer;
		List<String> customerRows = query.from(customer).where(customer.custId.eq(acctTupleRow.get(acct.custId)))
		                    						    .list(customer.internalCustomerId);
		
		if(null == customerRows || customerRows.size() == 0) {
			logger.error("生成合同状态对账文件，客户号custId:{}没有查询到相应的customer", acctTupleRow.get(acct.custId));
			return null;
		}
		
		String custUniqueId = customerRows.get(0);
		
		//组合返回对象
		YZFContractStatusCompareFileItem fileItem = new YZFContractStatusCompareFileItem();
		fileItem.setAcqId(acctTupleRow.get(acct.acqId));
		fileItem.setActiveDate(loan.getActiveDate());
		fileItem.setContractLimit(loan.getLoanInitPrin());
		fileItem.setContractNo(loan.getContrNbr());
		
		Date batchDate = batchStatusFacility.getBatchDate();
		if(loan.getActiveDate().compareTo(batchDate) == 0 && loan.getPaidOutDate() == null){
			fileItem.setContractStatus("D");
		}else if(loan.getPaidOutDate() != null && batchDate.compareTo(loan.getPaidOutDate())==0) {
			fileItem.setContractStatus("A");
		}else {
			return null;
		}
		fileItem.setCustUniqueId(custUniqueId);
		fileItem.setFailReason("");
		
		return fileItem;
	}


}
