/**
 * 
 */
package com.sunline.ccs.ui.shared;

import java.math.BigDecimal;

/**
 * 公共常量集合
* @author fanghj
 *
 */
public interface PublicConst {
	public static final BigDecimal TPS_TRAN_AMT_MAX = BigDecimal.valueOf(9999999999.99); //tps报文最大的交易金额
	public static final String NAME_CURRENCY_CODE = "currencyCode"; //币种代码
	public static final String KEY_BLOCKCODE_LIST = "KEY_BLOCKCODE_LIST";

}
