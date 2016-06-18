package com.sunline.ccs.ui.server.commons;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.otb.CardOTB;
import com.sunline.ccs.otb.CustOTB;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
* @author fanghj
 * @version 创建时间：2012-7-30 下午6:48:43 调用OTB计算
 */

@Component
public class CallOTBProvide {

	@Autowired
	private CustOTB customerOTB;
	@Autowired
	private AcctOTB accountOTB;
	@Autowired
	private CardOTB cardOTB;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private GlobalManagementService globalManSer;
	
	
	// 综合可用额度  需要获取客户层，账户层，卡片层的最小值；
	public BigDecimal realOTB(String cardNo,List<CcsAcct> tmAccountList) throws ProcessException {
		ArrayList<BigDecimal> temp = new ArrayList<BigDecimal>();		
		Date businessDate = unifiedParameterFacilityProvide.BusinessDate();
		temp.add(customerOTB.customerOTB(cardNo,businessDate));
		for(CcsAcct tmAccount:tmAccountList){
			temp.add(accountOTB.acctOTB(cardNo,tmAccount.getAcctType(),businessDate));
		}
		temp.add(cardOTB.cardDayRetailOTB(cardNo));
		temp.add(cardOTB.cardCtdOTB(cardNo));
		Collections.sort(temp);
		
		return temp.get(0);
	}
	
	

	/**
	 * 放置客户层OTB数据
	 * 
	 * @param maps
	 * @throws ProcessException
	 */
	public void setCustomerOTB(CcsCustomer customer, CcsCustomerCrlmt custLimitO, List<CcsAcctO> accounto, Date processDate, Map<String, Serializable> maps) throws ProcessException {
		BigDecimal otb = customerOTB.customerOTB(custLimitO, unifiedParameterFacilityProvide.productCredit(accounto.get(0).getProductCd()), accounto, processDate);
		//客户层可用额度和可用取现额度取同一值，没有客户层取现额度概念
		maps.put("CUST_OTB", otb);
		maps.put("CUST_CASH_OTB", otb);
	}

	/**
	 * 放置账户层OTB数据
	 * 
	 * @param maps
	 * @throws ProcessException
	 */
	public void setAccountOTB(CcsAcctO tmAccountO, Map<String, Serializable> maps) throws ProcessException {
		BigDecimal cashOTB = accountOTB.acctCashOTB(tmAccountO, unifiedParameterFacilityProvide.productCredit(tmAccountO.getProductCd()), globalManSer.getSystemStatus().getBusinessDate());
		BigDecimal OTB = accountOTB.acctOTB(tmAccountO, unifiedParameterFacilityProvide.productCredit(tmAccountO.getProductCd()),  globalManSer.getSystemStatus().getBusinessDate());
		maps.put(CcServProConstants.KEY_CASH_OTB, cashOTB);
		maps.put(CcServProConstants.KEY_OTB, OTB);
		// 账户溢缴款取现可用额度调用接口
		ProductCredit pc = new ProductCredit();
		maps.put(CcServProConstants.KEY_DRPOSITE_CASHOTB, accountOTB.acctDepositeCashOTB(tmAccountO, unifiedParameterFacilityProvide.productCredit(tmAccountO.getProductCd()), unifiedParameterFacilityProvide.BusinessDate()));
//		maps.put(CcServProConstants.KEY_DRPOSITE_CASHOTB, pc);
	}

	/**
	 * 放置卡片层OTB数据
	 * 
	 * @param maps
	 * @throws ProcessException
	 */
	public void setCardOTB(CcsCardO tmCardO, Map<String, Serializable> maps) throws ProcessException {
		maps.put(CcServProConstants.KEY_CARD_CTD_OTB, cardOTB.cardDayCashOTB(tmCardO));
		maps.put(CcServProConstants.KEY_CARD_CTD_CASH_OTB, cardOTB.cardCtdCashOTB(tmCardO));
		BigDecimal cashOTB = cardOTB.cardCtdCashOTB(tmCardO);
		
//		BigDecimal OTB = cardOTB.cardCtdOTB(tmCardO);
		// BigDecimal cashOTB = BigDecimal.ZERO;
		// BigDecimal OTB = BigDecimal.ZERO;
		maps.put(CcServProConstants.KEY_CARD_CTD_OTB, cashOTB);
		maps.put(CcServProConstants.KEY_CARD_CTD_CASH_OTB, cardOTB.cardCtdOTB(tmCardO));
		maps.put(CcServProConstants.KEY_CARD_CTD_NET_OTB, cardOTB.cardCtdNetOTB(tmCardO));
		maps.put(CcServProConstants.KEY_CARD_DAY_ATM_OTB, cardOTB.cardDayAtmOTB(tmCardO));
		maps.put(CcServProConstants.KEY_DAY_ATM_OPEN_NBR, cardOTB.cardDayAtmOpenNBR(tmCardO));
		
	}

}
