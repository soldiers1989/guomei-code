package com.sunline.ccs.ui.client.loader;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.kylin.web.flat.client.data.ClientContext;

/**
 * 
* @author fanghj
 *
 */
public interface AppLoaderInterAsync {

	/**
	 * 
	 * @param callback
	 */
	void getClientContext(AsyncCallback<ClientContext> callback);

}
