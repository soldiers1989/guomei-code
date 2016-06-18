package com.sunline.ccs.service.handler.query;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msentity.TNMCCustMobileReq;
import com.sunline.ccs.service.msentity.TNMCCustMobileResp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 手机号修改
 * @author wanghl
 *
 */
@Service
public class TNMCCustMobile {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private AppapiCommService appapiCommService;

//	@PersistenceContext
//	private EntityManager em;
	@Autowired
	private RCcsCustomer rCustomer;
	@Autowired
	private RCcsAcct rCcsAcct;
	TxnInfo txnInfo = new TxnInfo();

	private QCcsCustomer qCust = QCcsCustomer.ccsCustomer;

	public TNMCCustMobileResp handler(TNMCCustMobileReq req) throws ProcessException{
		LogTools.printLogger(log, "TNMCCustMobile", "修改手机号码", req, true);
		LogTools.printObj(log, req, "请求参数TNMCCustMobileReq");
		TxnInfo txnInfo = new TxnInfo();

		TNMCCustMobileResp resp = new TNMCCustMobileResp();
		try{
			txnInfo.setResponsCode(MsRespCode.E_0000.getCode());
			txnInfo.setResponsDesc(MsRespCode.E_0000.getMessage());
			//检查手机号格式
			CodeMarkUtils.markMobile(req.getMobile());
			if (!CheckUtil.isPhone(req.getMobile())) {
				log.error("非法的手机号码[" + CodeMarkUtils.markMobile(req.getMobile()) + "]");
//					throw new ProcessException("非法的手机号码");//TODO 错误码
				throw new ProcessException(MsRespCode.E_1058.getCode(),MsRespCode.E_1058.getMessage());
			}
			//更新客户表手机号
			updateCustomer(req);
			
			LogTools.printObj(log, resp, "返回信息TNMCCustMobileResp");
			LogTools.printLogger(log, "TNMCCustMobile", "修改手机号码", req, false);
		}
		catch (ProcessException pe){
			if(log.isErrorEnabled())
				log.error(pe.getMessage(), pe);
			appapiCommService.preException(pe, pe, txnInfo);
		}catch (Exception e) {
			if(log.isErrorEnabled())
				log.error(e.getMessage(), e);
			appapiCommService.preException(e, null, txnInfo);
		}finally{
			LogTools.printLogger(log, "TNMAAcctDDcardResp", "修改银行卡信息", resp, false);
		}
		this.setResponse(resp, req,txnInfo);
		return resp;
	}

	/**
	 * 更新客户表&账户表手机号
	 * @param req
	 */
	private void updateCustomer(TNMCCustMobileReq req) {
		//客户表
		//根据uuit查出客户
		CcsCustomer ccsCustomer = rCustomer.findOne(qCust.internalCustomerId.eq(req.getUuid()));
		if(ccsCustomer == null){
			throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
		}
		
		log.info("更新客户[{}]手机号码", ccsCustomer.getCustId());
		ccsCustomer.setMobileNo(req.getMobile());
		rCustomer.save(ccsCustomer);
		
		//账户表
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		//根据客户表custid查出所有账户，并更新所有账户手机号  todo：需与马上确认，是否符合业务需求
		Iterator<CcsAcct> custIt = rCcsAcct.findAll(qCcsAcct.custId.eq(ccsCustomer.getCustId())).iterator();
		CcsAcct acct = null;
		while(custIt.hasNext()){
			acct = custIt.next();//
			acct.setMobileNo(req.getMobile());
			log.info("更新客户账户[{}]手机号码", acct.getAcctNbr());
			rCcsAcct.save(acct);
		}
		
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TNMCCustMobileResp resp,TNMCCustMobileReq req,  TxnInfo txnInfo) {
		resp.setUuid(req.getUuid());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}

}
