package com.sunline.ccs.service.handler.query;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.SMSService.MsgFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.msdentity.SMSentitySendReq;
import com.sunline.ccs.service.msdentity.SMTNMAAdjustQuotaReq;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;
/**
 * 调额通知
 * @author zhengjf
 *
 */
@Service
public class TNMAAdjustQuota {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@PersistenceContext
	private EntityManager em;
	@Autowired
	MsgFacility msgFacility;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	
	QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	public void handler(SMTNMAAdjustQuotaReq req) {
		LogTools.printLogger(logger, "SMTNMAAdjustQuotaReq", "调额通知", req, true);
		LogTools.printObj(logger, req, "请求参数SMTNMAAdjustQuotaReq");
		SMSentitySendReq resp=new SMSentitySendReq();
		//创建一个map集合
		Map<Integer, Object> params=new HashMap<Integer, Object>();
		CcsAcct ccsacct=getCcsAcct(req.getContractNo());
		params=getParams(params,req);
		
		//把params组装到resp里面
		resp=msgFacility.getSMSInfo(resp,params);
		
		//装入手机号
		resp.setMobileNumber(ccsacct.getMobileNo());
		//查出loancode
		String loanCode = null;
		//如果合同是建户未进行提款的状态，ccsloan里没有值，这时候要先进行判断
		CcsLoan ccsLoan =  new JPAQuery(em).from(qCcsLoan).where(qCcsLoan.contrNbr.eq(ccsacct.getContrNbr())).singleResult(qCcsLoan);
		if(ccsLoan != null){
			loanCode = ccsLoan.getLoanCode();
		}else {
			ProductCredit product = unifiedParameterService.retrieveParameterObject(ccsacct.getProductCd(), ProductCredit.class);
			if(null != product.loanPlansMap && product.loanPlansMap.size() > 1) {
				loanCode = product.loanPlansMap.get(product.defaultLoanType);
				if(loanCode == null) {
					throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+":未设置产品默认分期类型！");
				}
			}else {
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+":请检查产品参数【分期产品】是否正确设置！");
			}
		}
		
		//装入业务类型
		resp.setSourceBizType(resp.getSourceBizSystem()+"-"+ccsacct.getAcqId()
				+"-"+loanCode+"-"+"manual_adjustment");
		
		//往短信平台发送通知
		try{
			msgFacility.sendSingleSms(req.getOperator(),resp);
		}catch(Exception e){
			//只打印日志，再往前抛异常，保证交易正常结束
			logger.error("通知平台短信发送异常",e);
		}
	}
	/**
	 * 取ACCT表单条数据
	 * @param req
	 * @return
	 */
	private CcsAcct getCcsAcct(String req) {
		
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsAcct.contrNbr.eq(req);
		return query.from(qCcsAcct).where(booleanExpression).singleResult(qCcsAcct);
	}
	//组装map集合
	public Map<Integer, Object> getParams(Map<Integer, Object> params,SMTNMAAdjustQuotaReq req) {
		String time=new SimpleDateFormat("yyyyMMdd").format(req.getDate());
		String time1=time.substring(0, 4);
		String time2=time.substring(4, 6);
		String time3=time.substring(6, 8);
		
		
		params.put(0, "");
		params.put(1, req.getName());
		params.put(2, req.getCreditLmt());
		params.put(3, time1);
		params.put(4, time2);
		params.put(5, time3);
		return params;
	}
}
