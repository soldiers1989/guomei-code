package com.sunline.ccs.service.handler.qunarcooper;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsTrustLoanTxn;
import com.sunline.ccs.infrastructure.shared.model.QCcsTrustLoanTxn;
import com.sunline.ccs.service.context.TxnContext;

import org.apache.commons.lang.StringUtils;

import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msentity.TFCTrustLoanRepayNoticeReq;
import com.sunline.ccs.service.msentity.TFCTrustLoanRepayNoticeResp;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 拿去花还款结果通知
 * @author qinshanbin
 *
 */
@Service
public class TFCTrustLoanRepayNotice{
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AppapiCommService appapiCommService;
	@PersistenceContext
	private EntityManager em;
	
	public TFCTrustLoanRepayNoticeResp handler(TFCTrustLoanRepayNoticeReq req){
		LogTools.printLogger(logger, "TFCTrustLoanRepayNoticeReq", "还款结果通知", req, true);
		LogTools.printObj(logger, req, "请求参数TFCTrustLoanRepayNoticeReq");
		TFCTrustLoanRepayNoticeResp resp = new TFCTrustLoanRepayNoticeResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		context.setTxnInfo(txnInfo);
		List<CcsTrustLoanTxn> ccsTrustLoanTxns;

		try {
			this.initTxnInfo(req, txnInfo);
			
			ccsTrustLoanTxns = this.getCcsTrustLoanTxn(txnInfo);
			//重复交易
			if (ccsTrustLoanTxns.size()>0) {
				this.beyondCompare(ccsTrustLoanTxns, txnInfo);
			}else {
				//取账户
				appapiCommService.loadAcctByContrNo(context, true);
				
				//存入数据库
				appapiCommService.installTrustLoanTxn(context);
			}
			resp.setContractNo(req.contrNo);
			
		} catch (ProcessException pe) {
			if (logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
				pe.printStackTrace();
			appapiCommService.preException(pe, pe, txnInfo);
//			appapiCommService.saveErrorOrder(context);

		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			ProcessException pe = new ProcessException();
			appapiCommService.preException(pe, pe, txnInfo);
//			appapiCommService.saveErrorOrder(context);

		} finally {
			LogTools.printLogger(logger, "TFCTrustLoanRepayNoticeResp", "还款结果通知", resp, false);
		}

//		this.setResponse(resp, req, txnInfo);
//		LogTools.printObj(logger, resp, "响应参数TFCTrustLoanRepayNoticeResp");
		setResponse(resp, txnInfo);
		return resp;
	}

	/**
	 * 组装响应报文
	 * 
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TFCTrustLoanRepayNoticeResp resp, TxnInfo txnInfo) {
		resp.setContractNo(txnInfo.getContrNo());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if (StringUtils.equals(MsRespCode.E_0000.getCode(),
				txnInfo.getResponsCode())) {
			resp.setStatus("S");// 交易成功
		} else {
			resp.setStatus("F");// 交易失败
		}
	}

	/**
	 * 初始化中间交易信息
	 * 
	 * @param context
	 */
	private void initTxnInfo(TFCTrustLoanRepayNoticeReq req, TxnInfo txnInfo) {
//		TxnInfo txnInfo = context.getTxnInfo();
		
		/*
		 * 报文头
		 */
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setOrg(OrganizationContextHolder.getCurrentOrg());
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setServiceId(req.getServiceId());
		
		/*
		 * 常量
		 */
		txnInfo.setTransType(AuthTransType.AgentCredit);
		txnInfo.setOrderStatus(OrderStatus.S);
		/*
		 * 请求体
		 */
		txnInfo.setContrNo(req.getContrNo());//合同号
		txnInfo.setLoanNo(req.getLoanNo());//贷款流水号
		txnInfo.setReNo(req.getReNo());//还款流水号
		txnInfo.setTransAmt(req.getTransAmt());//还款金额
		txnInfo.setCdTerms(req.getCdTerms());//还款期数
		txnInfo.setPrincipal(req.getPrincipal());//已还本金
		txnInfo.setFeeAmt(req.getFeeAmt());//已还分期服务费
		txnInfo.setPenalty(req.getPenalty());//已还罚金
		txnInfo.setRepayType(req.getRepayType());//还款类型

		if (logger.isDebugEnabled())
			logger.debug("业务日期：[{}]", txnInfo.getBizDate());
		LogTools.printObj(logger, txnInfo, "交易中间信息txnInfo");
	}
	private List<CcsTrustLoanTxn> getCcsTrustLoanTxn(TxnInfo txnInfo ) {
		JPAQuery query = new JPAQuery(em);
		QCcsTrustLoanTxn qCcsTrustLoanTxn = QCcsTrustLoanTxn.ccsTrustLoanTxn;
		List<CcsTrustLoanTxn> ccsTrustLoanTxn = query.from(qCcsTrustLoanTxn)
				.where(qCcsTrustLoanTxn.tlTxnType.eq(AuthTransType.AgentCredit)
						.and(qCcsTrustLoanTxn.tlReverseNo.eq(txnInfo.getReNo())))
				.list(qCcsTrustLoanTxn);
		return ccsTrustLoanTxn;
	}
	private void beyondCompare(List<CcsTrustLoanTxn> ccsTrustLoanTxn,TxnInfo txnInfo){
			
		for(CcsTrustLoanTxn ccsTrustLoanTx : ccsTrustLoanTxn){
			
			if(ccsTrustLoanTx.getTlLoanNo().equals(txnInfo.getLoanNo())
					 && ccsTrustLoanTx.getContrNbr().equals(txnInfo.getContrNo())
					 && ccsTrustLoanTx.getRepayType().equals( txnInfo.getRepayType())
//					 || !ccsTrustLoanTx.getTlTxnAmt().equals(txnInfo.getTransAmt())
					 && ccsTrustLoanTx.getTlTxnAmt().compareTo(txnInfo.getTransAmt()) ==0
					 && ccsTrustLoanTx.getPaidFeeAmount().compareTo( txnInfo.getFeeAmt()) ==0
					 && ccsTrustLoanTx.getPenalty().compareTo( txnInfo.getPenalty() )==0
					 && ccsTrustLoanTx.getPaidTerm().equals(txnInfo.getCdTerms()) 
					 && ccsTrustLoanTx.getPrincipal().compareTo(txnInfo.getPrincipal())==0
					){
				return;
			}
		}
		throw new ProcessException(MsRespCode.E_1012.getCode(), MsRespCode.E_1012.getMessage());
	}
}
