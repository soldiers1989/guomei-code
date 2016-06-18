package com.sunline.ccs.facility.order;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sunline.ccs.infrastructure.server.repos.RCcsMerchandiseOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsMerchandiseOrder;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 商品贷信息管理表
 * @author zhengjf
 */
@Service
public class MerchandiseFacility {

//private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RCcsMerchandiseOrder rCcsMerchandiseOrder;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	/**
	 * 商品贷信息管理表赋值
	 * @param serviceSn
	 * @param refNbr
	 * @param contrNbr
	 * @param applicationNo
	 * @param merId
	 * @param terminalId
	 * @param raId
	 * @param promotionId
	 * @param merchandiseAmt
	 * @param merchandiseCnt
	 * @param consignee
	 * @param consigneePhone
	 * @param consigneeAddress
	 * @param collectionDownpayment
	 * @param downPayment
	 * @param subsidyRatio
	 * @param paymentType
	 * @return
	 */
	public CcsMerchandiseOrder installMerchandiseOrder(String serviceSn,String refNbr,
			String contrNbr,String applicationNo,String merId,String terminalId,
			String raId,String promotionId,BigDecimal merchandiseAmt,BigDecimal merchandiseCnt,
			String consignee,String consigneePhone,String consigneeAddress,Indicator collectionDownpayment,
			BigDecimal downPayment, BigDecimal subsidyRatio,String paymentType){
		CcsMerchandiseOrder merchandiseOrder = new CcsMerchandiseOrder();
//		Date currDate = new Date();
		
		merchandiseOrder.setServicesn(serviceSn);
		merchandiseOrder.setRefNbr(refNbr);
		merchandiseOrder.setContrNbr(contrNbr);
		merchandiseOrder.setApplicationNo(applicationNo);
		merchandiseOrder.setMerId(merId);
		merchandiseOrder.setTerminalId(terminalId);
		merchandiseOrder.setRaId(raId);
		merchandiseOrder.setPromotionId(promotionId);
		merchandiseOrder.setMerchandiseAmt(merchandiseAmt);
		merchandiseOrder.setMerchandiseCnt(merchandiseCnt);
		merchandiseOrder.setConsignee(consignee);
		merchandiseOrder.setConsigneePhone(consigneePhone);
		merchandiseOrder.setConsigneeAddress(consigneeAddress);
		merchandiseOrder.setCollectionDownpayment(collectionDownpayment);
		merchandiseOrder.setDownPayment(downPayment);
		merchandiseOrder.setSubsidyRatio(subsidyRatio);
		merchandiseOrder.setPaymentType(paymentType);
		
		return rCcsMerchandiseOrder.save(merchandiseOrder);
	}
	
}
