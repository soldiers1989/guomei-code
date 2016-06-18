/**
 * 
 */
package com.sunline.ccs.ui.shared;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.kylin.web.ark.client.utils.DataTypeUtils;

/**
 * 加入了OTB属性的TmCustLimitO类
* @author fanghj
 *
 */
public class CssTmCustLimitO extends CcsCustomerCrlmt {

	private static final long serialVersionUID = 1L;
	
	private BigDecimal oTB;
	private BigDecimal cashOTB;
	
	@Override
	public void fillDefaultValues() {
		super.fillDefaultValues();
		if (oTB == null) {
			oTB = BigDecimal.ZERO;
		}
        if (cashOTB == null) {
        	cashOTB = BigDecimal.ZERO;
        }
	}
	
	@Override
	public void updateFromMap(Map<String, Serializable> map) {
		super.updateFromMap(map);
		if (map.containsKey("OTB")) {
			this.setOTB(DataTypeUtils.getBigDecimalValue(map.get("OTB")));
		}
		if (map.containsKey("CASH_OTB")) {
			this.setOTB(DataTypeUtils.getBigDecimalValue(map.get("CASH_OTB")));
		}
	}
	
	@Override
	public Map<String, Serializable> convertToMap() {
		Map<String, Serializable> map = super.convertToMap();
		map.put("OTB", oTB);
        map.put("CASH_OTB", cashOTB);
		return map;
	}
	
	public BigDecimal getOTB() {
		return oTB;
	}
	public void setOTB(BigDecimal otb) {
		this.oTB = otb;
	}
	public BigDecimal getCashOTB() {
		return cashOTB;
	}
	public void setCashOTB(BigDecimal cashOTB) {
		this.cashOTB = cashOTB;
	}
	
}
