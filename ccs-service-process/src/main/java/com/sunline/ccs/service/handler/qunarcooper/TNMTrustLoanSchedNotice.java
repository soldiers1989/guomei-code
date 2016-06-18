package com.sunline.ccs.service.handler.qunarcooper;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.mvel2.util.ThisLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsTrustLoanSchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsTrustLoanSchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsTrustLoanSchedule;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TNMTrustLoanSchedNoticeReq;
import com.sunline.ccs.service.msentity.TNMTrustLoanSchedNoticeResp;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 去哪还款计划通知接口
 * @author Mr.L
 *
 */
@Service
public class TNMTrustLoanSchedNotice  {

private Logger logger = LoggerFactory.getLogger(ThisLiteral.class);
	
	@Autowired
	public AppapiCommService appapiCommService;
	@Autowired
	private RCcsTrustLoanSchedule rLoanSchedule;
	@PersistenceContext
	private EntityManager em;

	
	public TNMTrustLoanSchedNoticeResp handler(TNMTrustLoanSchedNoticeReq req) {
		LogTools.printLogger(logger, getClass().getSimpleName(), "去哪还款计划通知接口",req , true);
		LogTools.printObj(logger, req, "TNMTrustLoanSchedNoticeReq请求参数");
		TNMTrustLoanSchedNoticeResp resp = new TNMTrustLoanSchedNoticeResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		context.setTxnInfo(txnInfo);
		context.setMsRequestInfo(req);
		
		try {
			this.initTxnInfo(req, txnInfo);
			
			//重复交易
//			this.findOrderBySersn(req.getServiceSn());
			
			//取账户
			appapiCommService.loadAcctByContrNo(context, true);
			
			//存入数据库
			appapiCommService.installTrustLoanSchedule(context);
			
			resp.setContractNo(req.contractNo);
			
		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			
			appapiCommService.preException(pe, pe, txnInfo);
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();//e不是pe，处理逻辑与没有code的pe一样
			appapiCommService.preException(pe, pe, txnInfo);
		}finally{
			LogTools.printLogger(logger, "TNMTrustLoanSchedNoticeResp", "去哪还款计划明细", resp, false);
		}
		setResponse(resp, txnInfo);
		LogTools.printObj(logger, resp, "TNMTrustLoanSchedNoticeResp响应参数");
		
		
		return resp;
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(MsResponseInfo resp, TxnInfo txnInfo) {
		
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}

	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(TNMTrustLoanSchedNoticeReq req, TxnInfo txnInfo) {
		//报文头
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setOrg(OrganizationContextHolder.getCurrentOrg());
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setServiceId(req.getServiceId());
		
		
		//常量
		txnInfo.setTransType(AuthTransType.Auth);
		txnInfo.setOrderStatus(OrderStatus.S);
		
		
		//请求体
		txnInfo.setContrNo(req.contractNo);
		txnInfo.setLoanNo(req.loanNo);
		txnInfo.setTransAmt(req.amount);
		txnInfo.setCdTerms(req.term);
		txnInfo.setScheduleDetails(req.scheduleDetails);
		txnInfo.setChangeTime(req.changeTime);
		if(logger.isDebugEnabled())
			logger.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "txnInfo交易中间信息");
		
	}
	public void findOrderBySersn(String serviceSn){
			JPAQuery query = new JPAQuery(em);
			QCcsTrustLoanSchedule qCcsTrustLoanSchedule = QCcsTrustLoanSchedule.ccsTrustLoanSchedule;
			CcsTrustLoanSchedule ccsTrustLoanSchedule = query.from(qCcsTrustLoanSchedule)
					.where(qCcsTrustLoanSchedule.servicesn.eq(serviceSn))
					.singleResult(qCcsTrustLoanSchedule);
			if(null != ccsTrustLoanSchedule){
				throw new ProcessException(MsRespCode.E_1012.getCode(), MsRespCode.E_1012.getMessage());
			}
		
	}
}
	

	
	