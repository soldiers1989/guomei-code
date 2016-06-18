package com.sunline.ccs.service.handler.query;


import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msdentity.STNQPrepayPkgQueryReq;
import com.sunline.ccs.service.msdentity.STNQPrepayPkgQueryResp;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 灵活还款计划包查询
 * @author Mr.L
 *
 */
@Service
public class TNQPrepayPkgQuery {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
	private EntityManager em;
	@Autowired
    AppapiCommService appapiCommService;
	@Autowired
	UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	UnifiedParamFacilityProvide  unifiedParamFacilityProvide;
	
	QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
	
	public STNQPrepayPkgQueryResp handler(STNQPrepayPkgQueryReq req){
 		LogTools.printLogger(log, "STNQPrepayPkgQueryReq", "灵活还款计划包查询", req, true);
		LogTools.printObj(log, req, "请求参数STNQPrepayPkgQueryReq");
		STNQPrepayPkgQueryResp resp = new STNQPrepayPkgQueryResp();
		TxnInfo txnInfo = new TxnInfo();
		try {
			
			//判断账户中灵活还款包使用情况
			if (Indicator.Y.equals(getCcsAcct(req).getPrepayPkgInd())) {
				
				this.getPrepayPkgParameters(resp, getCcsAcct(req));
				
			}else {
				resp.setPrepayPkgInd(getCcsAcct(req).getPrepayPkgInd());
			}
			
			log.debug("灵活还款计划包查询返回报文:" + resp);
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
			LogTools.printLogger(log, "STNQPrepayPkgQueryResp", "灵活还款计划包查询", resp, false);
		}
		setResponse(resp,  txnInfo);
		
		return resp;
	}


	/**
	 * 查询合同号对应的灵活还款包状态
	 * @param req
	 * @return
	 */
	public CcsAcct  getCcsAcct(STNQPrepayPkgQueryReq req) {
		
		 CcsAcct ccsAcct = new JPAQuery(em).from(qCcsAcct).where(qCcsAcct.contrNbr.eq(req.getContractNo())).singleResult(qCcsAcct);

		if ( null == ccsAcct) {
			throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
		}

		return ccsAcct;
	}
	
	
	
	/**
	 * 获取灵活还款包相关参数
	 * @param req
	 * @return
	 */
	public void getPrepayPkgParameters(STNQPrepayPkgQueryResp resp ,CcsAcct ccsAcct){
		
		ProductCredit productCredit = unifiedParameterFacility.loadParameter(ccsAcct.getProductCd(), ProductCredit.class);
		String loanCode = productCredit.loanPlansMap.get(productCredit.defaultLoanType);
		LoanFeeDef loanFeeDef = unifiedParamFacilityProvide.loanFeeDefByKey(loanCode,Integer.valueOf(ccsAcct.getLoanFeeDefId()));
		// 灵活还款计划包费率
		resp.setPrepayPkgFeeAmountRate(loanFeeDef.prepayPkgFeeAmountRate == null ? null:loanFeeDef.prepayPkgFeeAmountRate);
		// 灵活还款计划包固定金额
		resp.setPrepayPkgFeeAmount(loanFeeDef.prepayPkgFeeAmount == null ? BigDecimal.ZERO :loanFeeDef.prepayPkgFeeAmount);
		resp.setDelayAccuMaxTerm(loanFeeDef.delayAccuMaxTerm == null ? 0 :loanFeeDef.delayAccuMaxTerm);
		resp.setDelayApplyAdvDays(loanFeeDef.delayApplyAdvDays == null ? 0 :loanFeeDef.delayApplyAdvDays);
		resp.setDelayApplyAgainTerm(loanFeeDef.delayApplyAgainTerm == null ? 0 :loanFeeDef.delayApplyAgainTerm);
		resp.setDelayApplyMax(loanFeeDef.delayApplyMax == null ? 0 :loanFeeDef.delayApplyMax);
		resp.setDelayFristApplyTerm(loanFeeDef.delayFristApplyTerm == null ? 0 :loanFeeDef.delayFristApplyTerm);
		resp.setDelayMaxTerm(loanFeeDef.delayMaxTerm == null ? 0 :loanFeeDef.delayMaxTerm);
		resp.setDisPrepaymentApplyTerm(loanFeeDef.disPrepaymentApplyTerm == null ? 0 :loanFeeDef.disPrepaymentApplyTerm);
		resp.setPayDateAccuMax(loanFeeDef.payDateAccuMax == null ? 0 :loanFeeDef.payDateAccuMax);
		resp.setPayDateApplyAgainTerm(loanFeeDef.payDateApplyAgainTerm == null ? 0 :loanFeeDef.payDateApplyAgainTerm);
		resp.setPayDateExpireAdvDays(loanFeeDef.payDateExpireAdvDays == null ? 0 :loanFeeDef.payDateExpireAdvDays);
		resp.setPayDateFirstApplyTerm(loanFeeDef.payDateFirstApplyTerm == null ? 0 :loanFeeDef.payDateFirstApplyTerm);
		resp.setWavieGraceIntInd(loanFeeDef.wavieGraceIntInd == null ? Indicator.N :loanFeeDef.wavieGraceIntInd);
		resp.setPrepayPkgInd(ccsAcct.getPrepayPkgInd());
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
