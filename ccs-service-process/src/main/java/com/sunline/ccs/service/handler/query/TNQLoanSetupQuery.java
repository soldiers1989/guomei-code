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
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TNQLoanSetupQueryReq;
import com.sunline.ccs.service.msentity.TNQLoanSetupQueryResp;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 开户查询接口
 * @author zhengjf
 *
 */
@Service
public class TNQLoanSetupQuery {

	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsCustomer rCcsCustomer;
	@Autowired
	private RCcsLoan rCcsLoan;
	
	
	public TNQLoanSetupQueryResp handler(TNQLoanSetupQueryReq req) {
		
		LogTools.printLogger(log, "TNQLoanSetupQueryReq", "开户查询接口", req, true);
		LogTools.printObj(log, req, "请求参数TNQLoanSetupQueryReq");
		
		TNQLoanSetupQueryResp resp = new TNQLoanSetupQueryResp();
		TxnInfo txnInfo = new TxnInfo();
		try{
			//初始化
			this.infoTxnInfo(txnInfo,req);
			
			//查找账户表信息
			boolean label = this.getCcsAcct(txnInfo,true);
			
			this.getDueBillNo(txnInfo , label);
			
			this.bizProcess(resp,txnInfo);
			
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
			LogTools.printLogger(log, "TNQLoanSetupQueryResp", "开户查询接口", resp, false);
		}
		
		setResponse(resp, txnInfo);
		
		return resp;
	}
	/**
	 * 对合同号跟借据号赋值
	 * @param resp
	 * @param txnInfo
	 */
	private void bizProcess(TNQLoanSetupQueryResp resp, TxnInfo txnInfo) {
		
		resp.setContractNo(txnInfo.getContrNo());
		resp.setDueBillNo(txnInfo.getDueBillNo());
		
	}

	/**
	 * 初始化
	 * @param txnInfo
	 * @param req
	 */
	private void infoTxnInfo(TxnInfo txnInfo, TNQLoanSetupQueryReq req) {
		
		txnInfo.setUuid(req.getUuid());
		txnInfo.setApplyNo(req.getApplyNo());
		
	}

	/**
	 * 取交易历史
	 * @param txnInfo
	 * @param label
	 */
	private void getDueBillNo(TxnInfo txnInfo, boolean label) {
		if (label) {
			QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
			QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
			CcsLoanReg ccsLoanReg = null;
			
			
			CcsLoan loan = rCcsLoan.findOne(qCcsLoan.contrNbr.eq(txnInfo.getContrNo()));
			
			if (loan == null) {
				List<CcsLoanReg> loanRegList = new JPAQuery(em).from(qCcsLoanReg).where(
						qCcsLoanReg.contrNbr.eq(txnInfo.getContrNo())).list(qCcsLoanReg);
				
				if (loanRegList != null && loanRegList.size() > 0) {
					ccsLoanReg = loanRegList.get(0);
				}
				
				//取注册时间最早的loanreg
				for (CcsLoanReg loanReg : loanRegList) {
					if (ccsLoanReg.getRequestTime().after(loanReg.getRequestTime())) {
						ccsLoanReg = loanReg;
					}
				}
				
			}
			//对借据号赋值
			if (loan != null || ccsLoanReg != null) {
				txnInfo.setDueBillNo(
						loan == null ? ccsLoanReg.getDueBillNo() : loan.getDueBillNo());
			}	
		}
		
	}
	/**
	 * 取账户表
	 * @param txnInfo
	 * @return
	 */
	private boolean getCcsAcct(TxnInfo txnInfo , boolean label) {
		
		QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		
		CcsCustomer cust = null;
		//查找账户表条件
		BooleanExpression booleanExpression = qCcsAcct.applicationNo.eq(txnInfo.getApplyNo());
		//查找客户表
		BooleanExpression expression = qCcsCustomer.internalCustomerId.eq(txnInfo.getUuid());
		
		//判断传入uuid是否为空
		if (txnInfo.getUuid() != null) {
			cust = rCcsCustomer.findOne(expression);
		}
		
		//判断客户表是否为空
		if (cust != null && label) {
			booleanExpression = booleanExpression.and(qCcsAcct.custId.eq(cust.getCustId()));
		}else{
			return false;
		}
			
		CcsAcct acct = rCcsAcct.findOne(booleanExpression);
		
		//如果账户表不为空，则返回true
		if (acct != null) {
			txnInfo.setContrNo(acct.getContrNbr());
			return true;
		}else{
			return false;
		}
			
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
