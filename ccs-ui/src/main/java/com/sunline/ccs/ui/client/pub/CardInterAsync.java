/**
 * 
 */
package com.sunline.ccs.ui.client.pub;

import java.io.Serializable;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
* @author fanghj
 *
 */
public interface CardInterAsync {

	/**
	 * 
	 * @see com.sunline.ccs.ui.client.pub.CardInter#getCardInfo(java.lang.String)
	 */
	void getCardInfo(String cardNo,
			AsyncCallback<Map<String, Serializable>> callback);

}
