package com.sunline.ccs.param.ui.server;

import java.math.BigDecimal;
import java.util.List;

import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.RateDef;

public class ServerUtil {

	/**
	 * 校验费率的最大金额下一级必须必上一级大
	 * @param rateCeils
	 * @throws ProcessException 
	 */
	public static void validateRateCeil(List<RateDef> chargeRates) throws ProcessException{
		
		if(chargeRates != null && !chargeRates.isEmpty()){
			
			
			for(int i = 0; i <= chargeRates.size() - 2; i++){
				
				BigDecimal a = chargeRates.get(i).rateBase  ;
				BigDecimal b = chargeRates.get(i + 1).rateBase;
				if(a.compareTo(b) >= 0){
					throw new ProcessException("后一级利率最大金额必须大于前一级");
				}
			}
		}
	}
}
