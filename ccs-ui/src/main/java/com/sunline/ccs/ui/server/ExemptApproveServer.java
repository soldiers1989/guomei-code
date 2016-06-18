package com.sunline.ccs.ui.server;

import java.util.Date;

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
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnWaiveLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnWaiveLogHst;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;

/**
* @Description 豁免审批的Server
* @author 鹏宇
* @date 2015-11-14 上午10:23:58
 */
@Controller
@RequestMapping(value = "/ExemptApproveServer")
public class ExemptApproveServer {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private GlobalManagementService globalManagementService;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private RCcsTxnWaiveLog rCcsTxnWaiveLog;
	
	QCcsTxnWaiveLog qCcsTxnWaiveLog = QCcsTxnWaiveLog.ccsTxnWaiveLog;
	
	QCcsTxnWaiveLogHst qCcsTxnWaiveLogHst = QCcsTxnWaiveLogHst.ccsTxnWaiveLogHst;
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getWaiveLogList" , method = {RequestMethod.POST})
	public FetchResponse getWaiveLogList(@RequestBody(required = false) FetchRequest request)throws FlatException{
		String org = OrganizationContextHolder.getCurrentOrg();
		String contrNbr = (String) request.getParameter("contrNbr");
		logger.info("合同号["+contrNbr+"]查询豁免列表");
		
		JPAQuery query = new JPAQuery(em).from(qCcsTxnWaiveLog).where(qCcsTxnWaiveLog.contrNbr.eq(contrNbr).and(qCcsTxnWaiveLog.org.eq(org)));
		
		FetchResponse response = new JPAQueryFetchResponseBuilder(request, query).addFieldMapping(qCcsTxnWaiveLog).build();
		
		return response;
	}
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getWaiveLogHstList" , method = {RequestMethod.POST})
	public FetchResponse getWaiveLogHstList(@RequestBody(required = false)FetchRequest request)throws FlatException{
		String org = OrganizationContextHolder.getCurrentOrg();
		String contrNbr = (String) request.getParameter("contrNbr");
		logger.info("合同号["+contrNbr+"]查询历史豁免记录");
		
		JPAQuery query = new JPAQuery(em).from(qCcsTxnWaiveLogHst).where(qCcsTxnWaiveLogHst.contrNbr.eq(contrNbr).and(qCcsTxnWaiveLogHst.org.eq(org)));
		
		FetchResponse response = new JPAQueryFetchResponseBuilder(request, query).addFieldMapping(qCcsTxnWaiveLogHst).build();
		
		return response;
	}
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/allowWaive" , method={RequestMethod.POST})
	public String allowWaive(@RequestBody String contrNbr, @RequestBody String acctNbr , @RequestBody String refNbr , @RequestBody String txnCd , @RequestBody String opSeq)throws FlatException{
		String org = OrganizationContextHolder.getCurrentOrg();
		JPAQuery query = new JPAQuery(em);
		CcsTxnWaiveLog ccsTxnWaiveLog = query.from(qCcsTxnWaiveLog).where(qCcsTxnWaiveLog.contrNbr.eq(contrNbr)
				.and(qCcsTxnWaiveLog.refNbr.eq(refNbr)).and(qCcsTxnWaiveLog.org.eq(org))
				.and(qCcsTxnWaiveLog.acctNbr.eq(Long.parseLong(acctNbr)))
				.and(qCcsTxnWaiveLog.txnCode.eq(txnCd))
				.and(qCcsTxnWaiveLog.opSeq.eq(Long.parseLong(opSeq)))
				).list(qCcsTxnWaiveLog).get(0);
		
		if(ccsTxnWaiveLog.getAdjState().equals(AdjState.A)){
			return "已允许，请勿再次操作";
		}
		
		if(ccsTxnWaiveLog.getAdjState().equals(AdjState.R)){
			return "已拒绝，请勿再次操作";
		}
		
		ccsTxnWaiveLog.setCheckOpId(OrganizationContextHolder.getUsername());        //复核员
		ccsTxnWaiveLog.setCheckOpTime(globalManagementService.getSystemStatus().getBusinessDate());
		ccsTxnWaiveLog.setAdjState(AdjState.A);
		ccsTxnWaiveLog.setTxnDate(globalManagementService.getSystemStatus().getBusinessDate());
		ccsTxnWaiveLog.setLogBizDate(globalManagementService.getSystemStatus().getBusinessDate());     //联机业务日期
		rCcsTxnWaiveLog.save(ccsTxnWaiveLog);            //保存
		
		return "操作成功";
		
	}
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/refuseWaive" , method={RequestMethod.POST})
	public String refuseWaive(@RequestBody String contrNbr, @RequestBody String acctNbr , @RequestBody String refNbr,@RequestBody String txnCd , @RequestBody String opSeq)throws FlatException{
		String org = OrganizationContextHolder.getCurrentOrg();
		JPAQuery query = new JPAQuery(em);
		CcsTxnWaiveLog ccsTxnWaiveLog = query.from(qCcsTxnWaiveLog).where(qCcsTxnWaiveLog.contrNbr.eq(contrNbr)
				.and(qCcsTxnWaiveLog.refNbr.eq(refNbr)).and(qCcsTxnWaiveLog.org.eq(org))
				.and(qCcsTxnWaiveLog.acctNbr.eq(Long.parseLong(acctNbr)))
				.and(qCcsTxnWaiveLog.txnCode.eq(txnCd))
				.and(qCcsTxnWaiveLog.opSeq.eq(Long.parseLong(opSeq)))
				).list(qCcsTxnWaiveLog).get(0);
		
		if(ccsTxnWaiveLog.getAdjState().equals(AdjState.A)){
			return "已允许，请勿再次操作";
		}
		
		if(ccsTxnWaiveLog.getAdjState().equals(AdjState.R)){
			return "已拒绝，请勿再次操作";
		}
		
		ccsTxnWaiveLog.setCheckOpId(OrganizationContextHolder.getUsername());        //复核员
		ccsTxnWaiveLog.setCheckOpTime(globalManagementService.getSystemStatus().getBusinessDate());
		ccsTxnWaiveLog.setAdjState(AdjState.R);
		ccsTxnWaiveLog.setRejReason("内管手工拒绝");
		ccsTxnWaiveLog.setTxnDate(globalManagementService.getSystemStatus().getBusinessDate());
		ccsTxnWaiveLog.setLogBizDate(globalManagementService.getSystemStatus().getBusinessDate());     //联机业务日期
		rCcsTxnWaiveLog.save(ccsTxnWaiveLog);            //保存
		
		return "操作成功";
		
	}

}
