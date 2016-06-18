package com.sunline.ccs.service.handler.sunshine;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.entity.S10001AlterBankInfoReq;
import com.sunline.ccs.service.entity.S10001AlterBankInfoResp;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 修改银行卡信息
 * @author wangz
 *
 */
@Service
public class TNRBankInfo {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SunshineCommService sunshineCommService;
	@Autowired
	private RCcsAcct rCcsAcct;
	
	public S10001AlterBankInfoResp handler(S10001AlterBankInfoReq req) throws ProcessException {
		LogTools.printLogger(log, "S10001AlterBankInfoReq", "修改银行卡信息", req, true);
		LogTools.printObj(log, req, "请求参数S10001AlterBankInfoReq");
		if(log.isDebugEnabled())
			log.debug("开始修改保单号:[{}]对应的银行卡信息",req.getGuarantyid());
		
		S10001AlterBankInfoResp resp = new S10001AlterBankInfoResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		context.setTxnInfo(txnInfo);
		context.setSunshineRequestInfo(req);
		try{
			this.initTxnInfo(context,req);
			//检查开户行号
			sunshineCommService.checkBankCode(req.getBankcode());
			
			sunshineCommService.loadLoan(context, false);
			sunshineCommService.loadLoanReg(context,null, null,null, false);
			sunshineCommService.loadAcctO(context, true);
			CcsAcctO accounto = context.getAccounto();
			// 账户锁定码包含"P"销户
			if (StringUtils.isNotEmpty(accounto.getBlockCode()) && accounto.getBlockCode().toUpperCase().indexOf("P") > -1) {
				throw new ProcessException(MsRespCode.E_1014.getCode(), MsRespCode.E_1014.getMessage());
			}
			
			//查询账户信息表
			sunshineCommService.loadAcct(context, true);
			updateAcct(context,req);
		}catch (ProcessException pe){
			if(log.isErrorEnabled())
				log.error(pe.getMessage(), pe);
			
			this.sunshineCommService.preException(pe, pe, txnInfo);
			
		}catch (Exception e) {
			if(log.isErrorEnabled())
				log.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();
			this.sunshineCommService.preException(pe, pe, txnInfo);
		}finally{
			LogTools.printLogger(log, "S10001AlterBankInfoResp", "修改银行卡信息", resp, true);
		}
		
		this.setResponse(resp,req,txnInfo);
		LogTools.printObj(log, resp, "请求参数S10001AlterBankInfoResp");
		return resp;
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(S10001AlterBankInfoResp resp, S10001AlterBankInfoReq req, TxnInfo txnInfo) {
		resp.setGuarantyid(req.getGuarantyid());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}

	/**
	 * 更新账户
	 * @param context
	 * @param req
	 */
	@Transactional
	private void updateAcct(TxnContext context,S10001AlterBankInfoReq req) {
		if(log.isDebugEnabled())
			log.debug("生成授权码--updateAcct");
		CcsAcct ccsAcct = context.getAccount();
		//新的卡号
		ccsAcct.setDdBankAcctNbr(req.getNewputpaycardid());
		//开户人姓名
		ccsAcct.setDdBankAcctName(req.getBankowner());
		//开户行名称
		ccsAcct.setDdBankName(req.getBankname());
		//开户行code
		ccsAcct.setDdBankBranch(req.getBankcode());
		//开户行所属省
		ccsAcct.setDdBankProvince(req.getBankprovince());
		//开户行所属省code
		ccsAcct.setDdBankProvinceCode(req.getBankprovincecode());
		//开户行所属市
		ccsAcct.setDdBankCity(req.getBankcity());
		//开户行所属市code
		ccsAcct.setDdBankCityCode(req.getBankcitycode());

		rCcsAcct.save(ccsAcct);
		
	}
	
	/**
	 * 初始化交易中介信息
	 * @param context
	 * @param req
	 */
	public void initTxnInfo(TxnContext context,S10001AlterBankInfoReq req){		
		TxnInfo txnInfo = context.getTxnInfo();
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setGuarantyid(req.getGuarantyid());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
		txnInfo.setServiceId(req.getServiceId());
		
		if(log.isDebugEnabled())
			log.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(log, txnInfo, "交易中间信息txnInfo");
	}
}
