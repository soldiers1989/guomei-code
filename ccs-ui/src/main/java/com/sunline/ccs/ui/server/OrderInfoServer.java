package com.sunline.ccs.ui.server;

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
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;

@Controller
@RequestMapping(value = "/orderInfoServer")
public class OrderInfoServer {
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getOrderList", method = {RequestMethod.POST})
	public FetchResponse getOrderList(@RequestBody FetchRequest req) throws FlatException {
		JPAQuery query=new JPAQuery(em);
		query=query.from(qCcsOrder).where(qCcsOrder.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		String orderId = (String)req.getParameter(CcsOrder.P_OrderId);
    	String guarantyId = (String)req.getParameter(CcsOrder.P_GuarantyId);
    	String contrNbr = (String)req.getParameter(CcsOrder.P_ContrNbr);
    	if(guarantyId!=null&&!"null".equals(guarantyId)){
    		query=query.where(qCcsOrder.guarantyId.eq(guarantyId));
    	}
    	if(orderId!=null&&!"null".equals(orderId)){
    		query=query.where(qCcsOrder.orderId.eq(Long.parseLong(orderId)));
    	}
    	if(contrNbr!=null&&!"null".equals(contrNbr)){
    		query=query.where(qCcsOrder.contrNbr.eq(contrNbr));
    	}
    	
    	FetchResponse response =  new JPAQueryFetchResponseBuilder(req, query)
    	.addFieldMapping(qCcsOrder)
		.build();
		return response;
		
	}
}
