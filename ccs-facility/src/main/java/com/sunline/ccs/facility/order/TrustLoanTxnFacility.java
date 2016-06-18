package com.sunline.ccs.facility.order;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sunline.ccs.infrastructure.server.repos.RCcsTrustLoanTxn;
import com.sunline.ccs.infrastructure.shared.model.CcsTrustLoanTxn;
import com.sunline.ccs.infrastructure.shared.model.QCcsTrustLoanTxn;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 拿去花流水表
 * @author zhengjf
 *
 */
@Service
public class TrustLoanTxnFacility {

private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	RCcsTrustLoanTxn rCcsTrustLoanTxn;
	
	
	/**
	 * 新建流水
	 * @param acctNbr
	 * @param acctType
	 * @param contrNbr
	 * @param bizDate
	 * @param txnType
	 * @param txnAmt
	 * @param serviceSn
	 * @param loanNo
	 * @param serviceId
	 * @param acqId
	 * @return
	 */
	public CcsTrustLoanTxn installTrustLoanTxn(Long acctNbr,AccountType acctType,
			String contrNbr,Date bizDate,AuthTransType txnType, BigDecimal txnAmt,
			String serviceSn,String loanNo,String serviceId,String acqId){
		
		CcsTrustLoanTxn tlx = new CcsTrustLoanTxn();
		
		tlx.setAcctNbr(acctNbr);
		tlx.setAcctType(acctType);
		tlx.setContrNbr(contrNbr);
		tlx.setTlTxnTime(bizDate);
		tlx.setTlTxnType(txnType);
		tlx.setTlTxnAmt(txnAmt);
		tlx.setServicesn(serviceSn);
		tlx.setTlLoanNo(loanNo);
		tlx.setServiceId(serviceId);
		tlx.setAcqId(acqId);
		tlx.setCreateTime(new Date());
		
		
		return tlx;
	}
	
	/**
	 * 累计某交易类型 某状态的订单
	 * @param serviceSn 流水号
	 * @param requestTime 请求时间
	 * @param acqId 收单机构
	 * @return
	 */
	public void valiRepeatLoanTxn(String serviceSn, String acqId){
		try{
			CcsTrustLoanTxn tlx = this.findLoanTxnBySersn(serviceSn, acqId);
			if (null != tlx) {
				throw new ProcessException(MsRespCode.E_1012.getCode(), MsRespCode.E_1012.getMessage());
			}
		}catch(ProcessException pe){
			logger.error(pe.getMessage(), pe);
			throw pe;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_9998.getCode(), MsRespCode.E_9998.getMessage());
		}
	}
	
	/**
	 * 根据流水号及受理机构号查询订单
	 * @param serviceSn
	 * @param acqId
	 * @return
	 */
	public CcsTrustLoanTxn findLoanTxnBySersn(String serviceSn, String acqId){
		QCcsTrustLoanTxn qCcsTrustLoanTxn = QCcsTrustLoanTxn.ccsTrustLoanTxn;
		CcsTrustLoanTxn ccsTrustLoanTxn = rCcsTrustLoanTxn.findOne(qCcsTrustLoanTxn.servicesn.eq(serviceSn));//外部流水 11 37
//				.and(qCcsTrustLoanTxn.acqId.eq(acqId)));//受理机构号 32域
		
		return ccsTrustLoanTxn;
	}
}
