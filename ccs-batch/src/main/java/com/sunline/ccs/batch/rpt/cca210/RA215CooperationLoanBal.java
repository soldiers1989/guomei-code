package com.sunline.ccs.batch.rpt.cca210;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.cca210.item.CooperationLoanBalItem;
import com.sunline.ccs.batch.rpt.common.RptBatchUtil;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;


/**
 * 合作方贷款余额
 * @author lin
 *
 */
public class RA215CooperationLoanBal extends KeyBasedStreamReader<Long, List<CooperationLoanBalItem>>{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private RptBatchUtil batchUtil;
	@Autowired
	private UnifiedParameterFacility param;
	@PersistenceContext
	private EntityManager em;
	
	private QCcsAcct qacct = QCcsAcct.ccsAcct;
	

	@Override
	public List<Long> loadKeys() {
		
		return Arrays.asList(1L);
	}

	@Override
	public List<CooperationLoanBalItem> loadItemByKey(Long key) {
		logger.info("机构贷款余额---开始---");
		batchUtil.setCurrOrgNoToContext();
		List<CooperationLoanBalItem> list = new ArrayList<CooperationLoanBalItem>();
		
		Map<String, FinancialOrg> orgs = param.retrieveParameterObject(FinancialOrg.class);
		
		for(FinancialOrg o : orgs.values()){
			CooperationLoanBalItem i = new CooperationLoanBalItem();
			i.cooperationId = o.acqAcceptorId;
			BigDecimal sum = new JPAQuery(em).from(qacct).where(qacct.acqId.eq(o.acqAcceptorId)).singleResult(qacct.currBal.sum());
			
			i.bal = sum == null ? BigDecimal.ZERO : sum;
			
			list.add(i);
		}
		return list;
		
	}

}
