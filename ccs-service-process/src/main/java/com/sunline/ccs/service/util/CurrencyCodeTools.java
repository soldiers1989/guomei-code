package com.sunline.ccs.service.util;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.param.def.AccountAttribute;

/** 
 * @see 类名：CurrencyCodeTools
 * @see 描述：币种工具类
 *
 * @see 创建日期：   2015年6月24日 下午2:55:32
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class CurrencyCodeTools {

	/**
	 * 根据当前币种查询是否存在另外币种
	 * 
	 * @param currCd
	 * @param acctAttr
	 * @param dualAcctAttr
	 * @return
	 */
	public static Indicator isExistOtherCurrCd(String currCd, AccountAttribute acctAttr, AccountAttribute dualAcctAttr) {
		return dualAcctAttr == null ? Indicator.N :Indicator.Y;
	}
	
	
	/**
	 * 根据当前币种查询另外币种
	 * 
	 * @param currCd
	 * @param acctAttr
	 * @param dualAcctAttr
	 * @return
	 */
	public static String getOtherCurrCd(String currCd, AccountAttribute acctAttr, AccountAttribute dualAcctAttr) {
		if(dualAcctAttr != null){
			return currCd.equals(acctAttr.accountType.getCurrencyCode()) ? dualAcctAttr.accountType.getCurrencyCode() : currCd;
		}else{
			return null;
		}
	}
}
