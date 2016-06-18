package com.sunline.ccs.service.process.test.util; 

import java.math.BigDecimal;
import java.util.Date;

/** 
* @author fanghj
* @version 创建时间：2012-7-26 下午5:49:36 
* 类说明 
*/ 

public class CPSConstantsTest {

	public static final String ID_NO ="612325198112121211";
	public static final String ID_TYPE ="I";
	public static final String ORG = "000000000001";
	public static final String CUSTID = "000000000000001";
	public static final String CARD_NO ="4521123123333331111";
	
	public static final String ACCTTYPE="1";
	public static final String BLOCKCODE_T="T";
	public static final String BLOCKCODE_P = "P";
	
	//测试NF2202方法时用到的常量
	public static final int TEMPLIMIT = 44;
	public static final Date STARTDATE = new Date();
	public static final Date ENDDATE = new Date();
		
	//测试NF2203方法时用的的常量 
	public static final BigDecimal CASHLIMITRATE = new BigDecimal("0.5");//取现比例
	
	//测试NF2204方法时用的常量
	public static final BigDecimal LOANLIMITRATE = new BigDecimal("0.22");//分期比例
	
	//测试NF2205方法时用到的常量
	public static final int CREDITLIMIT = 1000;//永久额度
	
	public static final String NF2207_DDBANKNAME = "HHH";//约定还款银行名称
	public static final String NF2207_DDBANKBRANCH = "15651212";//约定还款开户行号
	public static final String NF2207_DDACCOUNTNO = "595956595";//约定还款账号
	public static final String NF2207_DDACCOUNTNAME = "NSAHIS";//约定还款账号用户姓名
	

	//测试CardServiceImpl类中NF3205方法是用常量
	public static final String ADDRETYPE ="H";//寄送地址类型
	
	
	//测试NF3404时，待销卡销户
	public static final String BLOCKCODE_C = "C";//销卡标志码C
	
	//测试TransactionServiceImpl类时用的常量
	public static final String NF4101_TRANSTYPE_A = "A";//交易类型A
	public static final String NF4101_TRANSTYPE_C = "C";//交易类型C
	
	public static final String NF4102_TRANSTYPE_A = "A";//交易类型A
	public static final String NF4102_TRANSTYPE_C = "C";//交易类型C
	
	public static final String NF4103_TRANSTYPE_A = "A";//交易类型A
	public static final String NF4103_TRANSTYPE_C = "C";//交易类型C
	
	public static final String NF4104_TRANSTYPE_A = "A";//交易类型A
	public static final String NF4104_TRANSTYPE_C = "C";//交易类型C
	
	//测试LoanServiceImpl类时用到的常量
	public static final String NF6101_ACCOUNTINDICATOR_A = "A";//功能标识A表示账号
	public static final String NF6101_CARDINDICATOR_C = "C";//功能标识C表示卡号
	public static final Integer NF6301_TERM = 5;//交易转分期期数
	public static final String NF6303_LOANSTATUS_M = "M";//分期终止状态码
	
	//测试PointServiceImpl类时用到的常量
	public static final String NF5201_ADJUSTTYPE_A = "A";//积分调整方向--增加
	public static final String NF5201_ADJUSTTYPE_C = "C";//积分调整方向--减少
			
}
 
