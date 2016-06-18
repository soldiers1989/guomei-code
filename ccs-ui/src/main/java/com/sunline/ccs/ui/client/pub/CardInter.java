/**
 * 
 */
package com.sunline.ccs.ui.client.pub;

import java.io.Serializable;
import java.util.Map;

import com.sunline.ppy.dictionary.exception.ProcessException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 提供卡片服务接口
* @author fanghj
 *
 */
@RemoteServiceRelativePath("rpc/cardServer")
public interface CardInter extends RemoteService {
	/**
	 * 根据介质卡号获取TmCard/TmCardO/TmCardStst
	 * @param cardNo
	 * @return
	 * @throws ProcessException
	 */
	public Map<String, Serializable> getCardInfo(String cardNo) throws ProcessException;
	
}
