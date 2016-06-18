package com.sunline.ccs.ui.client.commons;

import com.google.gwt.user.client.Window;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.dialog.client.Dialog;

/**
 * 加载影像件信息
 * 
 * @author songyanchao
 *
 */
public class PictureWindow {

	public void showPicture(final String contrNbr, final String resize) {
		RPC.ajax("rpc/applyPictureServer/showPictureUrl", new RpcCallback<Data>() {

			@Override
			public void onSuccess(Data result) {
				final String url = result + "";
				RPC.ajax("rpc/applyPictureServer/getApplicationNo", new RpcCallback<Data>() {

					@Override
					public void onSuccess(Data result) {
						String path = "&bizNo=" + result + "&resize=" + resize;
						Window.open(url+path, "影像查看",
								"height=600, width=800,resizable=yes,fullscreen=1");
					}
				}, contrNbr);
			}
		});
	}

}
