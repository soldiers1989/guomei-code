package com.sunline.ccs.batch.rpt.cca000.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * YGConfirmClaimInfo
 * @author wanghl
 *
 */
public class YGConfirmClaimInfoItem {

	/**
	 * 借据编号
 	 */
	@CChar( value = 32, order = 100 )
	public String putOutNo;

	/**
	 * 应理陪金额
 	 */
	@CChar( value = 15, pointSupported=true, order = 200 )
	public BigDecimal lpamt;

	/**
	 * 实际理赔金额
 	 */
	@CChar( value = 15, pointSupported=true, order = 300 )
	public BigDecimal actlpamt;

	/**
	 * 借据余额
 	 */
	@CChar( value = 15, pointSupported=true, order = 400 )
	public BigDecimal balance;

	/**
	 * 处理日期
 	 */
	@CChar( value = 8, datePattern="yyyyMMdd", order = 500 )
	public Date occurDate;
}
