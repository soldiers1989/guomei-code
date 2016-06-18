package com.sunline.ccs.ui.server;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 合同信息查询
 * 
/** 
 * @see 类名：SettleInfoServer
 * @see 描述：合同信息查询
 *
 * @see 创建日期：   2015年8月27日上午11:05:29
 * @author songyanchao
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Controller
@RequestMapping(value = "/contractInfoServer")
public class ContractInfoServer{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private OperatorAuthUtil operatorAuthUtil;
	
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
	QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getContractInfoListByIdNo", method = {RequestMethod.POST})
	public List<CcsLoan> getContractInfoListByIdNo(@RequestBody String idNo,@RequestBody String mobileNo) throws FlatException {
		List<CcsLoan> finalList = new ArrayList<CcsLoan>();
		List<CcsCustomer> customerList = new ArrayList<CcsCustomer>();
		
		JPAQuery q1 = new JPAQuery(em).from(qCcsCustomer);
		if(idNo != null){
			q1 = q1.where(qCcsCustomer.idNo.eq(idNo));
		}
		if(mobileNo != null){
			q1 = q1.where(qCcsCustomer.mobileNo.eq(mobileNo));
		}
		customerList = q1.list(qCcsCustomer);
		
		if(! (customerList != null && customerList.size() > 0)){
			throw new FlatException("找不到该客户的信息");
		}
		
		for(CcsCustomer customer : customerList){
			JPAQuery q = new JPAQuery(em);
			List<CcsAcct> acctList = q.from(qCcsAcct).where(qCcsAcct.custId.eq(customer.getCustId())).list(qCcsAcct);
			if(acctList != null && acctList.size() > 0){
				for(CcsAcct acct : acctList){
					JPAQuery q2 = new JPAQuery(em);
					List<CcsLoan> loanList = q2.from(qCcsLoan).where(qCcsLoan.acctNbr.eq(acct.getAcctNbr()).and(qCcsLoan.acctType.eq(acct.getAcctType()))).list(qCcsLoan);
					if(loanList != null && loanList.size() > 0){
						for(CcsLoan loan : loanList){
							finalList.add(loan);
						}
					}
				}
			}
		}
		return finalList;
	}
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getContractInfoList", method = {RequestMethod.POST})
	public List<CcsLoan> getContractInfoList(@RequestBody String contrNbr,@RequestBody String guarantyId) throws FlatException {
		List<CcsLoan> list = new ArrayList<CcsLoan>();
		
	JPAQuery query = new JPAQuery(em).from(qCcsLoan).where(qCcsLoan.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
	{
		if(contrNbr != null){
			query = query.where(qCcsLoan.contrNbr.eq(contrNbr));
		}
		if(guarantyId != null){
			query = query.where(qCcsLoan.guarantyId.eq(guarantyId));
		}
	}
	
	list = query.list(qCcsLoan);
	
	return list;
	}
}
