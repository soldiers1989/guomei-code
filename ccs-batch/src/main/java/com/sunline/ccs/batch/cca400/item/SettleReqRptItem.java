package com.sunline.ccs.batch.cca400.item;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;
import com.sunline.ccs.param.def.enums.SettleFeeType;

public class SettleReqRptItem {
	
	/** 合作方编号	VARCHAR(32)         */
	@CChar(value = 32, order = 100)
	public String cooperationId;
	
	/** 客户姓名	NUMBER(15,2)        */
	@CChar(value = 32, order = 200)
	public String customerName;
	
	/** 手机号 */
	@CChar(value = 18, order = 300)
	public String mobileNo;
	
	/** 身份证号 */
	@CChar(value = 32, order = 400)
	public String idNo;
	
	/** 证件类型	 */
//	public IdType idType;
	
	/** 申请单号 */
	@CChar(value = 32, order = 500)
	public String applyNo;
	
	/** 贷款合同号	VARCHAR(32)         */
	@CChar(value = 32, order = 600)
	public String contraNbr;
	
	/** 费用类型	CHAR(1)             */
	@CChar(value = 1, order = 700)
	public SettleFeeType settleFeeType;
	
	/** 费用金额	VARCHAR(32)         */
	@CChar(value = 15, pointSupported=true, order = 800)
	public BigDecimal amt;
	
	/** 入账日期	Date(8)         */
	@CChar(value = 8, datePattern = "yyyyMMdd", order = 900)
	public Date postDate;

	/** 贷款产品编码	VARCHAR(32)     */
	@CChar(value = 32, order = 1000)
	public String productCd;
	
	/** 期数 */
	@CChar(value = 3, order = 1200)
	public Integer term;
	
	/** 理赔日期 */
//	public Date claimDate;
	
	/** 合同终止日期 */
	@CChar(value = 12, datePattern = "yyyyMMdd", order = 1400)
	public Date terminalDate;
	
	/** 交易方向	CHAR(1)             */
	@CChar(value = 1, order = 1500)
	public String txnDirection;
	
}
