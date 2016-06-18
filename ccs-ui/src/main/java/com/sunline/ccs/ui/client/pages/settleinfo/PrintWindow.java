package com.sunline.ccs.ui.client.pages.settleinfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinDialog;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.print.client.Print;

@Singleton
public class PrintWindow extends KylinDialog{
	private Print print;
	private Data data;
	@Override
	protected Widget createContent() {
		HTML html=new HTML(data.toString());
		print=new Print(html);
		addConfirmButton(new IClickEventListener(){

			@Override
			public void onClick() {
				print.show();
			}
			
		});
		addCancelButton(new IClickEventListener(){

			@Override
			public void onClick() {
				hide();
			}
			
		});
		return print;
	}

	public Data getHtml() {
		return data;
	}

	public void setHtml(Data data) {
		this.data = data;
	}

	@Override
	protected void updateView() {
		//
	}
	
}
