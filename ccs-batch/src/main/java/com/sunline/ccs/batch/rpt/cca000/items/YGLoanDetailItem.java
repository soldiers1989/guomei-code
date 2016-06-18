package com.sunline.ccs.batch.rpt.cca000.items;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;

/**
 * YGLoanDetail
 * @author wanghl
 *
 */
public class YGLoanDetailItem {

	/**
	 * 保单号
 	 */
	@CChar( value = 20, order = 100 )
	public String guarantyID;

	/**
	 * 客户名称
 	 */
	@CChar( value = 60, order = 200 )
	public String customeName;

	/**
	 * 证件类型
 	 */
	@CChar( value = 3, order = 300 )
	public String certType;

	/**
	 * 证件号码
 	 */
	@CChar( value = 20, order = 400 )
	public String certID;

	/**
	 * 合同编号
 	 */
	@CChar( value = 32, order = 500 )
	public String contractNo;

	/**
	 * 借据编号
 	 */
	@CChar( value = 32, order = 600 )
	public String putOutNo;

	/**
	 * 扣款帐号
 	 */
	@CChar( value = 32, order = 700 )
	public String dpsacct;

	/**
	 * 放款时间
 	 */
	@CChar( value = 20,datePattern="yyyyMMddHH:mm:ss", order = 800 )
	public Date putOutDate;

	/**
	 * 放款金额
 	 */
	@CChar( value = 15,pointSupported=true, order = 900 )
	public BigDecimal businessSum;

	/**
	 * 期数
 	 */
	@CChar( value = 12, order = 1000 )
	public Integer loanTerm;

	/**
	 * 贷款利率
 	 */
	@CChar( value = 15,pointSupported=true, order = 1100 )
	public BigDecimal businessRate;

	/**
	 * 罚息利率方式
 	 */
	@CChar( value = 3, order = 1200 )
	public String fineRateType;

	/**
	 * 罚息浮动比例
 	 */
	@CChar( value = 15, order = 1300 )
	public String fineRateFloat;

	/**
	 * 贷款到期日
 	 */
	@CChar( value = 8,datePattern="yyyyMMdd", order = 1400 )
	public Date maturityDate;

	/**
	 * 结清类型
 	 */
	@CChar( value = 3, order = 1500 )
	public String finishType;

	/**
	 * 结清日期
 	 */
	@CChar( value = 8,datePattern="yyyyMMdd", order = 1600 )
	public Date finishDate;
	
	/**
	 * 放款状态
	 */
	@CChar( value = 2, order = 1700)
	public String status;
	/**
	 * 失败原因
	 */
	@CChar( value = 2, order = 1800)
	public String reanson;
}
