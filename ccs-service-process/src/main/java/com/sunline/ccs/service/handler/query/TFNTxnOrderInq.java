package com.sunline.ccs.service.handler.query;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msdentity.STFNTxnOrderInqReq;
import com.sunline.ccs.service.msdentity.STFNTxnOrderInqResp;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 商品贷--交易结果查询接口
 * @author zhengjf
 *
 */
@Service
public class TFNTxnOrderInq {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	AppapiCommService appapiCommService;
	
	QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
	QCcsOrderHst qCcsOrderHst = QCcsOrderHst.ccsOrderHst;
	
	public STFNTxnOrderInqResp handler(STFNTxnOrderInqReq req) {
		
		LogTools.printLogger(log, "STFNTxnOrderInqReq", "交易结果查询接口", req, true);
		LogTools.printObj(log, req, "请求参数STNQOrderInfoReq");
		
		STFNTxnOrderInqResp resp = new STFNTxnOrderInqResp();
		TxnInfo txnInfo = new TxnInfo();
		try{
			
			List<CcsOrder> ccsOrderList=getCcsOrderList(req);
			List<CcsOrderHst> ccsOrderHstList=getCcsOrderHstList(req);

			//判断order和orderHst表是否有符合的数据
			if (ccsOrderList.size()+ccsOrderHstList.size()==0) {
				throw new ProcessException(MsRespCode.E_1007.getCode(),MsRespCode.E_1007.getMessage());
			}
			if (ccsOrderList.size()+ccsOrderHstList.size()>1) {
				throw new ProcessException(MsRespCode.E_1072.getCode(),"根据送入条件找到多条交易记录");
			}
			
			if(ccsOrderHstList.size()!=0){
				CcsOrderHst ccsOrderHst=ccsOrderHstList.get(0);
				//报文检查
				resp.setContrNbr(ccsOrderHst.getContrNbr());
				resp.setBueBillNo(ccsOrderHst.getDueBillNo());
				resp.setTxnStatus(ccsOrderHst.getOrderStatus());
				resp.setTxnCode(ccsOrderHst.getResponseCode());
				resp.setFailureMassage(ccsOrderHst.getResponseMessage());
			}else {
				CcsOrder ccsOrder=ccsOrderList.get(0);
				resp.setContrNbr(ccsOrder.getContrNbr());
				resp.setBueBillNo(ccsOrder.getDueBillNo());
				resp.setTxnStatus(ccsOrder.getOrderStatus());
				resp.setTxnCode(ccsOrder.getResponseCode());
				resp.setFailureMassage(ccsOrder.getResponseMessage());
			}
			
		}
		catch(ProcessException pe){
			if(log.isErrorEnabled())
					log.error(pe.getMessage(),pe);
			appapiCommService.preException(pe, pe, txnInfo);
		}
		catch(Exception e){
			if(log.isErrorEnabled())
				log.error(e.getMessage(),e);
			appapiCommService.preException(e, null, txnInfo);				
		}finally{
			LogTools.printLogger(log, "TNTLLoanScheduleCalc", "还款计划试算", resp, false);
		}
		
		setResponse(resp, txnInfo);
		
		return resp;
	}
	/**
	 * 获取order里面符合条件的数据
	 * @param req
	 * @return
	 */
	public  List<CcsOrder> getCcsOrderList(STFNTxnOrderInqReq req) {
		BooleanExpression booleanExpression =qCcsOrder.servicesn.eq(req.origServiceSn)
				.and(qCcsOrder.acqId.eq(req.origAcqId));
		//判断传入合同号是否为空
		if (req.getContrNbr()!=null) {
			booleanExpression = booleanExpression.and( qCcsOrder.contrNbr.eq(req.contrNbr));
		}
		//判断传入serviceid是否为空
		if (req.getOrigServiceId()!=null) {
			booleanExpression = booleanExpression.and(qCcsOrder.serviceId.eq(req.origServiceId));
		}
		
		return new JPAQuery(em).from(qCcsOrder).where(booleanExpression).list(qCcsOrder);
	}
	/**
	 * 获取orderHst里面符合条件的数据
	 * @param req
	 * @return
	 */
	public  List<CcsOrderHst> getCcsOrderHstList(STFNTxnOrderInqReq req) {
		BooleanExpression booleanExpression = qCcsOrderHst.servicesn.eq(req.origServiceSn)
				.and(qCcsOrderHst.acqId.eq(req.origAcqId));
		//判断传入合同号是否为空
		if (req.getContrNbr()!=null) {
			booleanExpression = booleanExpression.and(qCcsOrderHst.contrNbr.eq(req.contrNbr));
		}
		//判断传入serviceid是否为空
		if (req.getOrigServiceId() != null) {
			booleanExpression = booleanExpression.and(qCcsOrderHst.serviceId.eq(req.origServiceId));
		}
		
		return new JPAQuery(em).from(qCcsOrderHst).where(booleanExpression).list(qCcsOrderHst);
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
