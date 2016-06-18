/**
 * 
 */
package com.sunline.ccs.ui.client.pub;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 提供账户服务接口
* @author fanghj
 *
 */
@RemoteServiceRelativePath("rpc/acctServer")
public interface AcctInter extends RemoteService {

	/**
	 * 根据介质卡号查询账户列表信息，包括TmAccount/TmAccountO
	 * @param cardNo
	 * @return
	 * @throws ProcessException
	 */
	public List<Map<String, Serializable>> getAcctList(String cardNo) throws ProcessException;
	
	/**
	 * 根据介质卡号和账户类型查询账户信息，包括TmAccount/TmAccountO
	 * @param cardNo
	 * @param acctType
	 * @return
	 * @throws ProcessException
	 */
	public Map<String, Serializable> getAcctInfo(AccountType acctType, String cardNo) throws ProcessException;
}
