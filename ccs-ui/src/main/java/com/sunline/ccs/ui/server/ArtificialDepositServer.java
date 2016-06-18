package com.sunline.ccs.ui.server;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javassist.expr.NewArray;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.antlr.grammar.v3.ANTLRv3Parser.finallyClause_return;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.service.api.MsQueryService;
import com.sunline.ccs.service.msdentity.STFDRemainderTransferReq;
import com.sunline.ccs.service.msdentity.STFDRemainderTransferResp;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ui.dialog.client.Dialog;

@Controller
@RequestMapping(value = "/artificialDepositServer")
public class ArtificialDepositServer {
	
	@PersistenceContext
	private EntityManager em;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private GlobalManagementService globalManagementService;
	
	private static final String serviceId = "TFDRemainderTransfer";
	//服务流水号
	private static final String SERVICE_SN_HERADER="WEB";
	
	@Resource(name = "msQueryService")
	private MsQueryService msQueryService;
	
	Random rd=new Random();
	SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
	
	@ResponseBody
	@RequestMapping(value = "/getPlanList", method = {RequestMethod.POST})
	public FetchResponse getPlanList(@RequestBody FetchRequest req) throws FlatException{
		QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		JPAQuery query = new JPAQuery(em);
		CcsAcct ccsAcct = null;
		query = query.from(qCcsAcct).where(qCcsAcct.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		String contrNbr = (String)req.getParameter(CcsOrder.P_ContrNbr);
		if (contrNbr != null && !"null".equals(contrNbr) && !"".equals(contrNbr)) {
			ccsAcct =  query.where(qCcsAcct.contrNbr.eq(contrNbr)).singleResult(qCcsAcct);
		}
		
		Long acctNo = ccsAcct.getAcctNbr();
		JPAQuery query2 = new JPAQuery(em);
		query2 = query2.from(qCcsPlan).where(qCcsPlan.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		
		if (acctNo != null && !"null".equals(acctNo) && !"".equals(acctNo)) {
			query2 = query2.where(qCcsPlan.acctNbr.eq(acctNo));
		}
		
		return new JPAQueryFetchResponseBuilder(req, query2).addFieldMapping(qCcsPlan).build();
		
	}
	
	@ResponseBody
	@RequestMapping(value = "/getOrderList", method = {RequestMethod.POST})
	public FetchResponse getOrderList(@RequestBody FetchRequest req)throws FlatException{
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		JPAQuery query = new JPAQuery(em);
		query = query.from(qCcsOrder).where(qCcsOrder.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		
		String contrNbr = (String)req.getParameter(CcsOrder.P_ContrNbr);
		query = query.where(qCcsOrder.contrNbr.eq(contrNbr).and(qCcsOrder.loanUsage.eq(LoanUsage.D)));
		query = query.orderBy(qCcsOrder.orderTime.desc());
		return new JPAQueryFetchResponseBuilder(req,query).addFieldMapping(qCcsOrder).build();
	}
	
	//试算
	@ResponseBody
	@RequestMapping(value = "/trialDeposiet", method = {RequestMethod.POST})
	public STFDRemainderTransferResp trialDeposiet(@RequestBody String contrNbr)throws FlatException{
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		
		if (contrNbr == null || "".equals(contrNbr) || "null".equals(contrNbr)) {
			throw new FlatException("合同号不能为空！");
		}
		CcsOrder ccsOrder = null;
		JPAQuery query = new JPAQuery(em);
		query = query.from(qCcsOrder).where(qCcsOrder.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		
		ccsOrder = query.where(qCcsOrder.contrNbr.eq(contrNbr)).singleResult(qCcsOrder);
		if (ccsOrder != null) {
			OrderStatus status = ccsOrder.getOrderStatus();
			if (status.equals(OrderStatus.W)) {
				throw new FlatException("存在处理中溢缴款订单！"); 
			}
		}
		
		// 获取acqid
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		JPAQuery query1 = new JPAQuery(em);
		CcsAcct ccsAcct = null;
		query1 = query1.from(qCcsAcct).where(
				qCcsAcct.org.trim().eq(
						OrganizationContextHolder.getCurrentOrg()));
		if (contrNbr != null && !"null".equals(contrNbr)
				&& !"".equals(contrNbr)) {
			ccsAcct = query1.where(qCcsAcct.contrNbr.eq(contrNbr)).singleResult(
					qCcsAcct);
		}
		String acqId = ccsAcct.getAcqId();
				
		STFDRemainderTransferReq req = new STFDRemainderTransferReq();
		//报文头
		req.setOrg(OrganizationContextHolder.getCurrentOrg());
		req.setOpId(null);
		req.setInputSource(InputSource.BANK);
		req.setBizDate(globalManagementService.getSystemStatus().getBusinessDate());
		String requestTime = format.format(globalManagementService.getSystemStatus().getBusinessDate()).substring(0, 8)+format.format(new Date()).substring(8, 14);
		req.setRequestTime(requestTime);
		req.setServiceSn(SERVICE_SN_HERADER + format.format(new Date())+(rd.nextInt(89999)+10000));
		req.setServiceId(serviceId);
		req.setSubTerminalType(AuthTransTerminal.HOST.toString());
		req.setAcqId(acqId);
		//报文体
		req.setContrNo(contrNbr);
		req.setType("1"); //1是试算，2是转出
		STFDRemainderTransferResp resp = msQueryService.tfdRemainderTransfer(req);
		return resp;
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/rollOutDeposiet", method = {RequestMethod.POST})
	public String rollOutDeposiet(@RequestBody String contrNbr, @RequestBody BigDecimal transferLmt)throws FlatException{
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		if (contrNbr == null || "".equals(contrNbr) || "null".equals(contrNbr)) {
			throw new FlatException("合同号不能为空！");
		}
		CcsOrder ccsOrder = null;
		JPAQuery query = new JPAQuery(em);
		query = query.from(qCcsOrder).where(qCcsOrder.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		ccsOrder = query.where(qCcsOrder.contrNbr.eq(contrNbr)).singleResult(qCcsOrder);
		if (ccsOrder != null) {
			OrderStatus status = ccsOrder.getOrderStatus();
			if (status.equals(OrderStatus.W)) {
				throw new FlatException("存在处理中溢缴款订单！"); 
			}
		}
		if (transferLmt == null) {
			throw new FlatException("转出金额不能为空！");
		}
		if (transferLmt.compareTo(BigDecimal.ZERO)<0 || transferLmt.equals(BigDecimal.ZERO)) {
			throw new FlatException("只能输入大于零的数！"); 
		}
		
		if (transferLmt.scale()>2) {
			throw new FlatException("不能输入超过两位小数！"); 
		}
		
		//获取acqid
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		JPAQuery query1 = new JPAQuery(em);
		CcsAcct ccsAcct = null;
		query1 = query1.from(qCcsAcct).where(qCcsAcct.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		if (contrNbr != null && !"null".equals(contrNbr) && !"".equals(contrNbr)) {
			ccsAcct =  query1.where(qCcsAcct.contrNbr.eq(contrNbr)).singleResult(qCcsAcct);
		}
		String acqId = ccsAcct.getAcqId();
		
		STFDRemainderTransferReq req = new STFDRemainderTransferReq();
		//报文头
		req.setOrg(OrganizationContextHolder.getCurrentOrg());
		req.setOpId(null);
		req.setInputSource(InputSource.BANK);
		req.setBizDate(globalManagementService.getSystemStatus().getBusinessDate());
		String requestTime = format.format(globalManagementService.getSystemStatus().getBusinessDate()).substring(0, 8)+format.format(new Date()).substring(8, 14);
		req.setRequestTime(requestTime);
		req.setServiceSn(SERVICE_SN_HERADER + format.format(new Date())+(rd.nextInt(89999)+10000));
		req.setServiceId(serviceId);
		req.setSubTerminalType(AuthTransTerminal.HOST.toString());
		req.setAcqId(acqId);
		
		//报文体
		req.setContrNo(contrNbr);
		req.setTransferLmt(transferLmt);
		req.setType("2"); 
		
		STFDRemainderTransferResp resp = msQueryService.tfdRemainderTransfer(req);
		return "0000".equals(resp.getErrorCode())?"操作成功!":resp.getErrorMessage();
	}

}
