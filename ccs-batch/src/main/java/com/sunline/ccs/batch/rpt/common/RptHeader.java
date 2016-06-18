package com.sunline.ccs.batch.rpt.common;

/**
 * 阳光贷报表头
 * @author wanghl
 *
 */
public class RptHeader {
	
	public final static String YG_Loan_Rpt_TH = "贷款合同编号|客户姓名|身份号码|放款日期|付款状态|合同金额|合同期数|利率|保险费率";
	public final static String YG_Loan_Repay_Rpt_TH = "贷款合同编号|客户姓名|身份号码|放款日期|合同金额|还款金额|实还本金|实还利息|实还罚息|实还保费|实还提前还款手续费|还款日期";
	public final static String YG_Loan_Settle_Pay_Rpt_TH = "贷款合同编号|客户姓名|身份号码|放款日期|合同金额|付款时间|支付保险费用|支付提前还款手续费";
	public final static String YG_Loan_SettleConfirm_Rpt_TH = "贷款合同编号|客户姓名|身份号码|放款日期|合同金额|理赔本金|理赔利息|理赔罚金|理赔日期";
	public final static String YG_Loan_Recovery_Pay_Rpt_TH = "贷款合同编号|客户姓名|身份号码|放款日期|合同金额|代付追偿金额|追偿日期";
	public final static String YG_Loan_Recovery_Rpt_TH = "贷款合同编号|客户姓名|身份号码|放款日期|合同金额|追偿金额|追偿日期";
	public final static String YG_Loan_Balance_Rpt_TH = "贷款合同编号|客户姓名|身份号码|查询日期|放款日期|合同金额|合同期数|贷款余额|逾期天数|应预提利息|应预提保费|应预提罚息";

}
