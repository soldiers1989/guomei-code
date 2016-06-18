package com.sunline.ccs.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.payEntity.CommResp;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 核心交易处理
 * @author zhengjf
 *
 */
@Service
public class RepayCommService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private OrderFacility orderFacility;
	
	public void bisProcess(TxnContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		try{
			CommResp mainResp = new CommResp();
			mainResp.setCode(null);
			mainResp.setMessage(null);
			txnInfo.setPayRespCode(mainResp.getCode());
			txnInfo.setPayRespMessage(mainResp.getMessage());
			txnInfo.setResponsCode(MsPayfrontError.S_0.getRespCode());
			txnInfo.setResponsDesc(MsPayfrontError.S_0.getDesc());
			
			appapiCommService.saveCcsAuthmemoO(context);
			context.getOrder().setTxnType(txnInfo.getTransType());
			orderFacility.updateOrder(context.getOrder(), mainResp, "SUCCESS",OrderStatus.S,txnInfo.getLogKv(),txnInfo.getBizDate(),MsRespCode.E_0000);
			appapiCommService.updateAcctO(context);
			appapiCommService.updateAcctoMemoAmt(context.getAccounto(), txnInfo,true);
			appapiCommService.updateAuthmemo(context, AuthTransStatus.N);//交易流水
			
			appapiCommService.savePosting(txnInfo);//入账记录
			
			appapiCommService.mergeProc(context);
			
		}catch (ProcessException pe){
			if(logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);
			
			ProcessException newPe = appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.exceptionProc(context, newPe);
			appapiCommService.saveErrorOrder(context);
		}catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			
			ProcessException pe = new ProcessException();
			ProcessException newPe = appapiCommService.preException(e, pe, txnInfo);//e不是pe，处理逻辑与没有code的pe一样
			appapiCommService.exceptionProc(context, newPe);
			appapiCommService.saveErrorOrder(context);
		}
		//保存交易数据
		appapiCommService.mergeProc(context);
	
	}
}
