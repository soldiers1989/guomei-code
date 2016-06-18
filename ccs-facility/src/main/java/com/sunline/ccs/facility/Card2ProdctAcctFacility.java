package com.sunline.ccs.facility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ark.support.utils.CodeMarkUtils;

/**
 * 通过卡号获取卡号对应的产品信息，账户属性
 *   
 * CardNoTOAccountAttribute  
 * 
		  
 * zhanght  
  * 2012-12-12 下午4:02:36  
 * 
		  
 * @version 1.0.0  
 *
 */
@Service
public class Card2ProdctAcctFacility {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	private static final String MES_SYS_PARAM_ERROR ="参数获取错误";
	
	/**
	 * 根据卡号，获取产品参数
	 * 
	 * @param cardNo
	 * @return
	 * @throws ProcessException
	 */
	public ProductCredit CardNoToProductCr(String cardNo) throws ProcessException {
		// 获取卡BIN
		String lCardNoBin = cardNo.substring(0, 6);
		// 去掉卡BIN和最后一位校验位，获取卡号值
		BigDecimal lCardNoValue = new BigDecimal(cardNo.substring(6, cardNo.length() - 1));
		List<Product> productList = new ArrayList<Product>();
		Map<String, Product> productMap = unifiedParameterService.retrieveParameterObject(Product.class);
		for (String productCode : productMap.keySet()) {
			Product productObj = productMap.get(productCode);

			if (!lCardNoBin.equals(productObj.bin))
				continue;

			// 卡号段下限
			BigDecimal cardNoRangeFlr = new BigDecimal(productObj.cardnoRangeFlr);
			// 卡号段上限
			BigDecimal cardNoRangeCeil = new BigDecimal(productObj.cardnoRangeCeil);

			if (log.isDebugEnabled()) {
				log.debug("productCode[" + productCode + "],bin[" + productObj.bin + "],cardNoRangeFlr[" + cardNoRangeFlr + "],cardNoRangeCeil[" + cardNoRangeCeil + "]");
			}
			if (lCardNoValue.compareTo(cardNoRangeFlr) >= 0 && lCardNoValue.compareTo(cardNoRangeCeil) <= 0) {
				if (log.isDebugEnabled()) {
					log.debug("productCode[" + productCode + "]");
				}
				productList.add(productObj);
			}
		}
		if (productList.size() < 1) {
			log.error("根据逻辑卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "]，未找到对应产品代码！");
			throw new ProcessException(MES_SYS_PARAM_ERROR);
		}
		if (productList.size() > 1) {
			log.error("根据逻辑卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "]，找到多个产品代码！");
			throw new ProcessException(MES_SYS_PARAM_ERROR);
		}
		return unifiedParameterService.loadParameter(productList.get(0).productCode, ProductCredit.class);
	}
	
	/**
	 * 根据卡号获取账户属性
	 * CardNoTOAccountAttribute
	 * @param cardNo
	 * @return   
	 *AccountAttribute  
	 * @throws ProcessException 
	 * @exception   
	 * @since  1.0.0
	 */
	public AccountAttribute CardNoTOAccountAttribute(String cardNo,String currencyCd) throws ProcessException{
		ProductCredit productCredit = CardNoToProductCr(cardNo);
		Integer accountAttributeId = null ;
		if(productCredit.postCurrCd != null && productCredit.postCurrCd.equals(currencyCd)){
			accountAttributeId = productCredit.accountAttributeId;
		}
		if(productCredit.dualCurrCd != null && productCredit.dualCurrCd.equals(currencyCd)){
			accountAttributeId = productCredit.dualAccountAttributeId;
		}
		if(accountAttributeId == null){
			log.error("卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "],币种["+currencyCd+"]所对应的产品["+productCredit.productCd+"所查询的AccountArrributeID为空");
			throw new ProcessException(MES_SYS_PARAM_ERROR);
		}
		AccountAttribute accountArrribute = unifiedParameterService.loadParameter(accountAttributeId, AccountAttribute.class);
		if(accountArrribute == null){
			log.error("卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "],币种["+currencyCd+"]所对应的产品["+productCredit.productCd+"所查询的AccountArrribute为空");
			throw new ProcessException(MES_SYS_PARAM_ERROR);
		}
		return accountArrribute;
	}
	
	/**
	 * 
	 * 卡号，币种获取账户类型
	 * @param cardNo
	 * @param currencyCd
	 * @return
	 * @throws ProcessException   
	 *AccountType  
	 * @exception   
	 * @since  1.0.0
	 */
	public AccountType CardNoCurrCdToAccoutType(String cardNo,String currencyCd) throws ProcessException{
		return CardNoTOAccountAttribute(cardNo, currencyCd).accountType;
	}
	
	/**
	 * 获取 acct_attribute(本币账户属性引用ID)
	 * 
	 * @param productCd
	 * @return Integer
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public AccountAttribute acct_attribute(ProductCredit productCd) throws ProcessException {
		try {
			AccountAttribute accoutArr = unifiedParameterService.loadParameter(productCd.accountAttributeId, AccountAttribute.class);
			return accoutArr;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ProcessException(MES_SYS_PARAM_ERROR);
		}
	}
	
	/**
	 * 获取 acct_attribute(外币账户属性引用ID)
	 * 
	 * @param productCd
	 * @return Integer
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public AccountAttribute dualacct_attribute(ProductCredit productCd) throws ProcessException {
		try {
			AccountAttribute accoutArr = unifiedParameterService.loadParameter(productCd.dualAccountAttributeId, AccountAttribute.class);
			return accoutArr;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ProcessException(MES_SYS_PARAM_ERROR);
		}
	}

}
