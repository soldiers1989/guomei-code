package com.sunline.ccs.param.def.enums;

public enum SettleTxnDirection {
	
	/**
	 * 支付给合作方费用
	 */
	ToCoop("+"),
	/**
	 * 收取合作方费用
	 */
	FromCoop("-");
	
	private String settleFileSign;
	
	private SettleTxnDirection(String settleFileSign) {
		this.settleFileSign = settleFileSign;
	}

	public String getSettleFileSign() {
		return settleFileSign;
	}
	
}
