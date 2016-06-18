package com.sunline.ccs.service.handler.query;



import java.math.BigDecimal;
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
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msdentity.STNQPartnerLoanControlReq;
import com.sunline.ccs.service.msdentity.STNQPartnerLoanControlResp;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 担保方贷款总控余额查询
 * @author Mr.L
 *
 */
@Service
public class TNQPartnerLoanControl {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private AppapiCommService appapiCommService;
    @Autowired
    GlobalManagementService globalManagementService;
	
	QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
	QCcsOrderHst qCcsOrderHst = QCcsOrderHst.ccsOrderHst;
	
	
	public STNQPartnerLoanControlResp handler(STNQPartnerLoanControlReq req){
 		LogTools.printLogger(log, "STNQPartnerLoanControlReq", "担保方贷款总控查询", req, true);
		LogTools.printObj(log, req, "请求参数STNQPartnerLoanControlReq");
		Date bizDate = globalManagementService.getSystemStatus().getBusinessDate();
	
		TxnInfo txnInfo = new TxnInfo();
		STNQPartnerLoanControlResp resp=new STNQPartnerLoanControlResp();
		
		try{
			//总贷款余额
			BigDecimal total = getTotalBal(req);
			//当日放款金额
			BigDecimal dbAmt = getSumTxnAmt(req, bizDate);
			//当日还款金额
			BigDecimal crAmt = getSumTxnAmtPay(req, bizDate);
			//担保方贷款总余额
			BigDecimal partnerAmt = BigDecimal.ZERO;
			if(null != req.getAsqId()){
				 partnerAmt = total.subtract(crAmt).add(dbAmt);
			} 
			resp.setTotalBal(partnerAmt);
			resp.setAcqId(req.getAsqId());
			log.debug("担保方贷款总控查询返回报文:" + resp);
		}catch(ProcessException pe){
			if(log.isErrorEnabled())
				log.error(pe.getMessage(),pe);
			appapiCommService.preException(pe, pe, txnInfo);
		}
		catch(Exception e){
			if(log.isErrorEnabled())
				log.error(e.getMessage(),e);
			appapiCommService.preException(e, null, txnInfo);				
		}finally{
			LogTools.printLogger(log, "STNQPartnerLoanControlResp", "担保方贷款总控查询", resp, false);
		}
		setResponse(resp,  txnInfo);
		
		return resp;
	}
	/**
	 * 根据查询条件获取出Order表中数据
	 * 当日放款金额
	 * @param req
	 * @return
	 */
	private BigDecimal getSumTxnAmt(STNQPartnerLoanControlReq req,Date bizDate){
		BigDecimal txnAmt = BigDecimal.ZERO;
		BigDecimal sumTxnAmt = BigDecimal.ZERO;
		JPAQuery query = new JPAQuery(em);
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		List<CcsOrder> orders = query.from(qCcsOrder)
				.where(qCcsOrder.acqId.eq(req.getAsqId())
				.and(qCcsOrder.orderStatus.eq(OrderStatus.S)
				.and(qCcsOrder.txnType.eq(AuthTransType.AgentDebit)))
				.and(qCcsOrder.optDatetime.eq(bizDate)))
				.list(qCcsOrder);
		for(CcsOrder ccsOrder : orders){
			txnAmt = ccsOrder.getTxnAmt();
			sumTxnAmt = sumTxnAmt.add(txnAmt);
		}
		return sumTxnAmt;
		
	}
	/**
	 * 根据查询条件获取出Order表中数据
	 * 当日还款金额
	 * @param req 
	 * @return
	 */
	private BigDecimal getSumTxnAmtPay(STNQPartnerLoanControlReq req,Date bizDate) {
	
		BigDecimal txnAmt = BigDecimal.ZERO;
		BigDecimal sumTxnAmtPay = BigDecimal.ZERO;
		JPAQuery query = new JPAQuery(em);
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		List<CcsOrder> orders = query.from(qCcsOrder)
				.where(qCcsOrder.acqId.eq(req.getAsqId())
				.and(qCcsOrder.orderStatus.eq(OrderStatus.S)
				.and(qCcsOrder.txnType.eq(AuthTransType.AgentCredit)))
				.and(qCcsOrder.optDatetime.eq(bizDate)))
				.list(qCcsOrder);
		for(CcsOrder ccsOrder : orders){
			txnAmt = ccsOrder.getTxnAmt();
			sumTxnAmtPay = sumTxnAmtPay.add(txnAmt);
		}
		return sumTxnAmtPay;
	}
	
	/**
	 * 获取符合账户表的总条件
	 * @param req
	 * @return
	 */
	public BigDecimal getTotalBal(STNQPartnerLoanControlReq req) {
		
		BigDecimal txnAmt = BigDecimal.ZERO;
		BigDecimal totalBal = BigDecimal.ZERO;
		JPAQuery query = new JPAQuery(em);
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		List<CcsAcct> accts = query.from(qCcsAcct)
				.where(qCcsAcct.acqId.eq(req.getAsqId()))
				.list(qCcsAcct);
		for(CcsAcct ccsAcct : accts){
			txnAmt = ccsAcct.getCurrBal();
			totalBal = totalBal.add(txnAmt);
		}
		return totalBal;
		
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
}
