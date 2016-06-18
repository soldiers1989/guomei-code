package com.sunline.ccs.ui.server;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanRegHst;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanAction;

@Controller
@RequestMapping(value = "/prepaymentInfoServer")
public class PrepaymentInfoServer {
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	QCcsLoanReg qLoanReg = QCcsLoanReg.ccsLoanReg;
	QCcsLoanRegHst qLoanRegHst = QCcsLoanRegHst.ccsLoanRegHst;
	
	@ResponseBody()
	@RequestMapping(value = "/getPrepaymentList", method = {RequestMethod.POST})
	public FetchResponse getPrepaymentList(@RequestBody FetchRequest req) throws FlatException {
		try{
			String contrNbr = req.getParameter(CcsLoanReg.P_ContrNbr)==null?null:req.getParameter(CcsLoanReg.P_ContrNbr).toString();
			if(contrNbr==null) return null;
			JPAQuery query=new JPAQuery(em);
			query = query.from(qLoanReg).where(qLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qLoanReg.contrNbr.eq(contrNbr))
					.and(qLoanReg.loanAction.eq(LoanAction.O)));
			return new JPAQueryFetchResponseBuilder(req,query).addFieldMapping(qLoanReg).build();
		}catch(Exception e){
			//查询不到不抛出异常
			logger.debug("未查询到提前还款信息");
			return null;
		}
	}
	
	@ResponseBody()
	@RequestMapping(value = "/getPrepaymentHstList", method = {RequestMethod.POST})
	public FetchResponse getPrepaymentHstList(@RequestBody FetchRequest req) throws FlatException {
		try{
			String contrNbr = req.getParameter(CcsLoanReg.P_ContrNbr)==null?null:req.getParameter(CcsLoanReg.P_ContrNbr).toString();
			if(contrNbr==null) return null;
			JPAQuery query=new JPAQuery(em);
			query = query.from(qLoanRegHst).where(qLoanRegHst.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qLoanRegHst.contrNbr.eq(contrNbr))
					.and(qLoanRegHst.loanAction.eq(LoanAction.O)));
			return new JPAQueryFetchResponseBuilder(req,query).addFieldMapping(qLoanRegHst).build();
		}catch(Exception e){
			//查询不到不抛出异常
			logger.debug("未查询到提前还款历史信息");
			return null;
		}
	}
}
