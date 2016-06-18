package com.sunline.ccs.ui.client.pages.artificialdeposit;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinDialog;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.event.client.IClickEventListener;

public class ArtificialDepositDialog extends KylinDialog{
	
	@Inject
	private KylinForm showForm;
	private VerticalPanel panel;
	
	private TextColumnHelper transferAmt;
	
	private MapData mapData;
	
	public MapData getMapData() {
		return mapData;
	}

	public void setMapData(MapData mapData) {
		this.mapData = mapData;
	}
	

	@Override
	protected Widget createContent() {
		setTitle("溢缴款试算结果");
		setWidth(400);
		setHeight(200);
		transferAmt = new TextColumnHelper("transferAmt", "最大可转出溢缴款", 70);
		
		panel = new VerticalPanel();
		panel.setWidth("100%");
		
		showForm = new KylinForm();
		showForm.setField(transferAmt.readonly(true));
		
		KylinButton kButton = new KylinButton("关闭", null);
		kButton.addClickEventListener(new IClickEventListener() {

			@Override
			public void onClick() {
				close();
			}
		});
		
		panel.add(showForm);
		panel.add(kButton);
		
		return panel;
	}

	@Override
	protected void updateView() {
		if(mapData != null){
			if(mapData.getString("transferAmt") != null && !"null".equals(mapData.getString("transferAmt"))){
				showForm.setFieldValue("transferAmt", mapData.getString("transferAmt"));
			}
		}
		
	}

}
