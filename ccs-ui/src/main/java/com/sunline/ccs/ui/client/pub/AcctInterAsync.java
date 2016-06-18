/**
 * 
 */
package com.sunline.ccs.ui.client.pub;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
* @author fanghj
 *
 */
public interface AcctInterAsync {

	/**
	 * 根据介质卡号查询账户列表信息，包括TmAccount/TmAccountO
	 * @param cardNo
	 * @param callback
	 */
	void getAcctList(String cardNo,
			AsyncCallback<List<Map<String, Serializable>>> callback);

	/**
	 * 根据介质卡号和账户类型查询账户信息，包括TmAccount/TmAccountO
	 * @param acctType
	 * @param cardNo
	 * @param callback
	 */
	void getAcctInfo(AccountType acctType, String cardNo,
			AsyncCallback<Map<String, Serializable>> callback);

}
