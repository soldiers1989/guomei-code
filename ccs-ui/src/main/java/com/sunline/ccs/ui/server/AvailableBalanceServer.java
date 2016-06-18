package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
//import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardThresholdCtrl;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.CcsCardUsage;
//import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
//import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
//import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CallOTBProvide;
import com.sunline.ccs.ui.server.commons.DateTools;

import com.sunline.ppy.api.CcServProConstants;

import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Controller
@RequestMapping(value="/availableBalanceServer")
public class AvailableBalanceServer {
    Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private CPSBusProvide cpsBusProvide;
    @Autowired
    private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
    @Autowired
	private CustAcctCardFacility queryFacility;
    @Autowired
	private CallOTBProvide callOTBProvide;
    @Autowired
    private GlobalManagementService globalManagementService;
    
    QCcsAcctO qTmAccountO = QCcsAcctO.ccsAcctO;
    
	@SuppressWarnings("all")
	@ResponseBody()
	@RequestMapping(value="/getAcctCardCustInfo",method={RequestMethod.POST})
	public Map<String, Serializable> getAcctCardCustInfo(@RequestBody String cardNo,@RequestBody String currCd)  {
		log.info("getAcctCardCustInfo:cardNo[{}]后四位,currCd[{}]",CodeMarkUtils.subCreditCard(cardNo),currCd);
		
		Map<String,Serializable> rtnValue = new HashMap<String,Serializable>();
		
		this.putAll(rtnValue, getAcctMap(cardNo, currCd),getCustInfoMap(cardNo),getCardMap(cardNo));
		
	    return rtnValue;
	}

	public Map<String,Serializable> getAcc(String cardNo,String currCd){
		return getAcctMap(cardNo, currCd);
	}

	public Map<String,Serializable> getCustInfo(String cardNo){
		return getCustInfoMap(cardNo);
	}
	
	@ResponseBody()
	@RequestMapping(value="/getCard",method={RequestMethod.POST})
	public Map<String,Serializable> getCard(String cardNo){
		return getCardMap(cardNo);
	}
	
	@SuppressWarnings("unchecked")
  	private Map<String, Serializable> getAcctMap(String cardNo,String currCd) throws ProcessException{
		//账户信息处理
	  CcsAcct tmAccount = cpsBusProvide.getTmAccountTocardNbrAndcurrencyCode(cardNo, currCd);
	  CcsAcctO tmAccountO = cpsBusProvide.getTmAccountOTocardNbr(cardNo, tmAccount.getAcctType());
      Map<String, Serializable>  map = new HashMap<String, Serializable>();
	  
      this.putAll(map, tmAccount.convertToMap(),tmAccountO.convertToMap());
      //可用额度设置
      //暂时屏蔽
     callOTBProvide.setAccountOTB(tmAccountO, map);
      
     map.put(CcServProConstants.KEY_CASH_LIMIT, calculateCashLimit(tmAccount).setScale(0, BigDecimal.ROUND_HALF_UP));
		// 计算并放置分期额度
	  map.put(CcServProConstants.KEY_LOAN_LIMIT, calculateLoanLimit(tmAccount).setScale(0, BigDecimal.ROUND_HALF_UP));
		// 产品默认取现比例
	  map.put(CcServProConstants.KEY_PROC_CASH_LIMIT_BT, unifiedParameterFacilityProvide.acct_attribute(tmAccount.getProductCd()).cashLimitRate);
     map.put(CcServProConstants.KEY_PROC_CASH_LIMIT_BT, new BigDecimal(10.00));
		// 账单分期最大允许分期金额
	 map.put(CcServProConstants.KEY_TOTAL_PASTPRINCIPAL, caculateBillLoanAmt(tmAccount));
     map.put(CcServProConstants.KEY_TOTAL_PASTPRINCIPAL, new BigDecimal(2.10));
	  
	 return map;
  }
   //获取卡片信息
	@SuppressWarnings("unchecked")
	private Map<String, Serializable> getCardMap(String cardNo)
			throws ProcessException {
		CcsCardO tmCardO = cpsBusProvide.getTmCardOTocardNbr(cardNo);
		CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
		CcsCardUsage tmCardStst = cpsBusProvide.getTmCardStstTocardNbr(cardNo);
		// 该对象可能为空
		CcsCardThresholdCtrl tmCardLimitOverrideO = cpsBusProvide.getTmCardLimitOverrideOTocardNbr(cardNo);
		Map<String, Serializable> map = new HashMap<String, Serializable>();

		this.putAll(map, tmCardO.convertToMap(), tmCard.convertToMap(),tmCardStst.convertToMap());

		if (tmCardLimitOverrideO != null) {
			map.putAll(tmCardLimitOverrideO.convertToMap());
		}
		map.put(CcServProConstants.KEY_REAL_OTB,callOTBProvide.realOTB(cardNo,queryFacility.getAcctByCardNbr(cardNo)));

		callOTBProvide.setCardOTB(tmCardO, map);

		return map;
	}
    //获取用户信息
	@SuppressWarnings("unchecked")
	private Map<String, Serializable> getCustInfoMap(String cardNo)
	{
		CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
		//获取客服信息，用主卡，附卡没有客户额度说法
		CcsCustomer tmCustomer = cpsBusProvide.getTmCustomerToCard(tmCard.getCardBasicNbr());
		CcsCustomerCrlmt tmCustLimitO  = new CcsCustomerCrlmt();
		tmCustomer.getCustLmtId();
		if(tmCustomer.getCustLmtId() != null){
		 tmCustLimitO = cpsBusProvide.getTmCustLimitOToCustLimitId(tmCustomer.getCustId());
		}
		Map<String, Serializable> map = new HashMap<String, Serializable>();
		if(tmCustLimitO == null){
			tmCustLimitO = new CcsCustomerCrlmt();
		}
		this.putAll(map, tmCustomer.convertToMap(), tmCustLimitO.convertToMap());
		Date processDate = globalManagementService.getSystemStatus().getBusinessDate();
		List<CcsAcctO> accountoList = cpsBusProvide.getTmAccountOTocardNbr(cardNo);
		
		callOTBProvide.setCustomerOTB(tmCustomer, tmCustLimitO, accountoList, processDate, map);
		return map;
	}

	/**
	 * 计算取现额度
	 * 
	 * @param tmAccount
	 * @return BigDecimal
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	private BigDecimal calculateCashLimit(CcsAcct tmAccount) throws ProcessException {
		if (tmAccount.getCashLmtRate() == null) {
			return validAmt(tmAccount).multiply(unifiedParameterFacilityProvide.cashLimitRate(tmAccount.getProductCd()));
		}
		return validAmt(tmAccount).multiply(tmAccount.getCashLmtRate());
	}
	
	/**
	 * 获取有效额度
	 * 
	 * @param tmAccount
	 * @return BigDecimal
	 * @exception
	 * @since 1.0.0
	 */
	private BigDecimal validAmt(CcsAcct tmAccount) {
		BigDecimal limit = tmAccount.getCreditLmt();
		if (tmAccount.getTempLmtBegDate() != null && tmAccount.getTempLmtEndDate() != null) {
			if (DateTools.dateBetwen_IncludeEQ(tmAccount.getTempLmtBegDate(), new Date(), tmAccount.getTempLmtEndDate())) {
				limit = tmAccount.getTempLmt();
			}
		}
		return limit;
	}
	
	/**
	 * 计算分期额度
	 * 
	 * @param tmAccount
	 * @return BigDecimal
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	private BigDecimal calculateLoanLimit(CcsAcct tmAccount) throws ProcessException {
		if (tmAccount.getLoanLmtRate() == null) {
			return validAmt(tmAccount).multiply(unifiedParameterFacilityProvide.loanLimitRate(tmAccount.getProductCd()));
		}
		return validAmt(tmAccount).multiply(tmAccount.getLoanLmtRate());
	}
	
	// 计算账单分期允许的最大金额
	private BigDecimal caculateBillLoanAmt(CcsAcct tmAccount) {
		BigDecimal billLoan = queryFacility.countPlan_PastPrincipal(tmAccount.getAcctNbr(), tmAccount.getAcctType(), PlanType.R);
		if (billLoan.compareTo(tmAccount.getCreditLmt()) > 0)
			return tmAccount.getCreditLmt();
		else
			return billLoan;
	}
	//集合复制
   private <T extends Map<String , Serializable>> void  putAll(Map<String, Serializable> map,T... args){
	 for(T t : args){
		if(t == null)
			continue;
		 map.putAll(t);
	 }
	}

}
