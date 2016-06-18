package com.sunline.ccs.service.provide;

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
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.exception.ProcessException;


/** 
 * @see 类名：CallOTBProvide
 * @see 描述：调用OTB计算
 *
 * @see 创建日期：   2015年6月24日 下午2:49:10
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
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
	public BigDecimal realOTB(String cardNbr,List<CcsAcct> CcsAcctList) throws ProcessException {
		ArrayList<BigDecimal> temp = new ArrayList<BigDecimal>();		
		Date businessDate = unifiedParameterFacilityProvide.BusinessDate();
		temp.add(customerOTB.customerOTB(cardNbr,businessDate));
		for(CcsAcct CcsAcct:CcsAcctList){
			temp.add(accountOTB.acctOTB(cardNbr,CcsAcct.getAcctType(),businessDate));
		}
		temp.add(cardOTB.cardDayRetailOTB(cardNbr));
		temp.add(cardOTB.cardCtdOTB(cardNbr));
		Collections.sort(temp);
		return temp.get(0);
	}
	
	

	/**
	 * 放置客户层OTB数据
	 * 
	 * @param maps
	 * @throws ProcessException
	 */
	public void setCustomerOTB(CcsCustomer customer, CcsCustomerCrlmt custLmtO, List<CcsAcctO> accounto, Date processDate, Map<String, Serializable> maps) throws ProcessException {
		BigDecimal otb = customerOTB.customerOTB(custLmtO, unifiedParameterFacilityProvide.productCredit(accounto.get(0).getProductCd()), accounto, processDate);
		maps.put(CcServProConstants.KEY_CASH_OTB, otb);
		maps.put(CcServProConstants.KEY_OTB, otb);
	}

	/**
	 * 放置账户层OTB数据
	 * 
	 * @param maps
	 * @throws ProcessException
	 */
	public void setAccountOTB(CcsAcctO CcsAcctO, Map<String, Serializable> maps) throws ProcessException {
		BigDecimal cashOTB = accountOTB.acctCashOTB(CcsAcctO, unifiedParameterFacilityProvide.productCredit(CcsAcctO.getProductCd()), globalManSer.getSystemStatus().getBusinessDate());
		BigDecimal OTB = accountOTB.acctOTB(CcsAcctO, unifiedParameterFacilityProvide.productCredit(CcsAcctO.getProductCd()),  globalManSer.getSystemStatus().getBusinessDate());
		maps.put(CcServProConstants.KEY_CASH_OTB, cashOTB);
		maps.put(CcServProConstants.KEY_OTB, OTB);
		// 账户溢缴款取现可用额度调用接口
		maps.put(CcServProConstants.KEY_DRPOSITE_CASHOTB, accountOTB.acctDepositeCashOTB(CcsAcctO, unifiedParameterFacilityProvide.productCredit(CcsAcctO.getProductCd()), unifiedParameterFacilityProvide.BusinessDate()));
	}

	/**
	 * 放置卡片层OTB数据
	 * 
	 * @param maps
	 * @throws ProcessException
	 */
	public void setCardOTB(CcsCardO CcsCardO, Map<String, Serializable> maps) throws ProcessException {
		maps.put(CcServProConstants.KEY_CARD_CTD_OTB, cardOTB.cardDayCashOTB(CcsCardO));
		maps.put(CcServProConstants.KEY_CARD_CTD_CASH_OTB, cardOTB.cardCtdCashOTB(CcsCardO));
		BigDecimal cashOTB = cardOTB.cardCtdCashOTB(CcsCardO);
//		BigDecimal OTB = cardOTB.cardCtdOTB(CcsCardO);
		// BigDecimal cashOTB = BigDecimal.ZERO;
		// BigDecimal OTB = BigDecimal.ZERO;
		maps.put(CcServProConstants.KEY_CARD_CTD_OTB, cashOTB);
		maps.put(CcServProConstants.KEY_CARD_CTD_CASH_OTB, cardOTB.cardCtdOTB(CcsCardO));
		maps.put(CcServProConstants.KEY_CARD_CTD_NET_OTB, cardOTB.cardCtdNetOTB(CcsCardO));
		maps.put(CcServProConstants.KEY_CARD_DAY_ATM_OTB, cardOTB.cardDayAtmOTB(CcsCardO));
		maps.put(CcServProConstants.KEY_DAY_ATM_OPEN_NBR, cardOTB.cardDayAtmOpenNBR(CcsCardO));
		
	}

}
