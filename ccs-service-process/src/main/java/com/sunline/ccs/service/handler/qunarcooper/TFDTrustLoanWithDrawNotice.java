package com.sunline.ccs.service.handler.qunarcooper;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
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
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TFDTrustLoanWithDrawNoticeReq;
import com.sunline.ccs.service.msentity.TFDTrustLoanWithDrawNoticeResp;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;


/**
 * 去哪儿贷款消费结果通知（半托管）
 * @author zhengjf
 *
 */
@Service
public class TFDTrustLoanWithDrawNotice{

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AppapiCommService appapiCommService;
	@PersistenceContext
	private EntityManager em;
	
	
	public TFDTrustLoanWithDrawNoticeResp handler(TFDTrustLoanWithDrawNoticeReq req) {
		LogTools.printLogger(log, "TFDTrustLoanWithDrawNoticeReq", "交易结果查询接口", req, true);
		LogTools.printObj(log, req, "请求参数TFDTrustLoanWithDrawNoticeReq");
		
		TFDTrustLoanWithDrawNoticeResp resp = new TFDTrustLoanWithDrawNoticeResp();
		
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		List<CcsTrustLoanTxn> ccsTrustLoanTxns;
		context.setTxnInfo(txnInfo);
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
			resp.setContractNo(req.contractNo);
			
		}catch(ProcessException pe){
				if(log.isErrorEnabled())
						log.error(pe.getMessage(),pe);
				appapiCommService.preException(pe, pe, txnInfo);
		}catch(Exception e){
				if(log.isErrorEnabled())
					log.error(e.getMessage(),e);
				appapiCommService.preException(e, null, txnInfo);				
		}finally{
				LogTools.printLogger(log, "TFDTrustLoanWithDrawNoticeResp", "去哪儿贷款消费结果通知（半托管）", resp, false);
		}

		setResponse(resp, txnInfo);
			
		return resp;
		
	}
	
	/**
	 * 初始化交易中间信息
	 * @param req
	 * @param txnInfo
	 */
	private void initTxnInfo(TFDTrustLoanWithDrawNoticeReq req, TxnInfo txnInfo) {
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
		txnInfo.setFeeAmt(req.feeAmount);
		txnInfo.setLoanSource(req.loanSource);
		txnInfo.setMerId(req.merId);
		txnInfo.setAuthTxnTerminal(req.authTxnTerminal);
		txnInfo.setMerchandiseOrder(req.merchandiseOrder);
		txnInfo.setMerchandiseAmt(req.merchandiseAmt);
		txnInfo.setDownPaymentAmt(req.downPaymentAmt);
		txnInfo.setRaId(req.raId);
		txnInfo.setMobile(req.mobile);
		
		if(log.isDebugEnabled())
			log.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(log, txnInfo, "txnInfo交易中间信息");
		
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
	private List<CcsTrustLoanTxn> getCcsTrustLoanTxn(TxnInfo txnInfo ) {
		JPAQuery query = new JPAQuery(em);
		QCcsTrustLoanTxn qCcsTrustLoanTxn = QCcsTrustLoanTxn.ccsTrustLoanTxn;
		List<CcsTrustLoanTxn> ccsTrustLoanTxn = query.from(qCcsTrustLoanTxn)
				.where(qCcsTrustLoanTxn.tlTxnType.eq(AuthTransType.Auth)
						.and(qCcsTrustLoanTxn.tlLoanNo.eq(txnInfo.getLoanNo())))
				.list(qCcsTrustLoanTxn);
		return ccsTrustLoanTxn;
	}
	private void beyondCompare(List<CcsTrustLoanTxn> ccsTrustLoanTxn,TxnInfo txnInfo){
			
		for(CcsTrustLoanTxn ccsTrustLoanTx : ccsTrustLoanTxn){
			if( ccsTrustLoanTx.getContrNbr().equals(txnInfo.getContrNo()) 
					&& ccsTrustLoanTx.getLoanSource().equals(txnInfo.getLoanSource())
					&& ccsTrustLoanTx.getTerminalId().equals(txnInfo.getAuthTxnTerminal())
					&& ccsTrustLoanTx.getMerId().equals( txnInfo.getMerId())
					&& ccsTrustLoanTx.getMerchandiseOrder() .equals( txnInfo.getMerchandiseOrder())
					&& ccsTrustLoanTx.getRaId() .equals(txnInfo.getRaId())
					&& ccsTrustLoanTx.getMobile() .equals( txnInfo.getMobile())
					 && ccsTrustLoanTx.getTlTxnAmt().compareTo(txnInfo.getTransAmt()) ==0  
					 && ccsTrustLoanTx.getFeeAmount() .compareTo(txnInfo.getFeeAmt()) ==0
					 && ccsTrustLoanTx.getPaidTerm().equals(txnInfo.getCdTerms())
					 && ccsTrustLoanTx.getMerchandiseAmt() .compareTo(txnInfo.getMerchandiseAmt()) ==0
					 && ccsTrustLoanTx.getDownPaymentAmt().compareTo(txnInfo.getDownPaymentAmt())==0
					){
				return;
			}
		}
		throw new ProcessException(MsRespCode.E_1012.getCode(), MsRespCode.E_1012.getMessage());
	}
}
