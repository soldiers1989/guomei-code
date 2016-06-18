package com.sunline.ccs.ui.client.pages.advance;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinDialog;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.event.client.IClickEventListener;

public class AdvanceCalcDialog extends KylinDialog{
	
	@Inject
	private KylinForm showForm;
	private VerticalPanel  panel;

	private TextColumnHelper amount;
	private TextColumnHelper dueBillNo;
	private TextColumnHelper interest;
	private TextColumnHelper poundage;
	private TextColumnHelper premium;
	private TextColumnHelper principal;
	private TextColumnHelper lifeInsu;
	private TextColumnHelper stamp;
	private TextColumnHelper replaceSvc;
	
	private MapData mapData;
	
	public MapData getMapData() {
		return mapData;
	}

	public void setMapData(MapData mapData) {
		this.mapData = mapData;
	}

	@Override
	protected Widget createContent() {
		setTitle("还款试算结果");
		setWidth(400);
		setHeight(400);
		
		amount = new TextColumnHelper("amount", "总金额", 70);
		dueBillNo = new TextColumnHelper("dueBillNo", "借据号", 70);
		interest = new TextColumnHelper("interest", "利息", 70);
		poundage = new TextColumnHelper("poundage", "手续费", 70);
		premium = new TextColumnHelper("premium", "保费", 70);
		principal = new TextColumnHelper("principal", "本金", 70);
		lifeInsu = new TextColumnHelper("lifeInsu", "寿险计划包费", 70);
		stamp = new TextColumnHelper("stamp", "印花税", 70);
		replaceSvc = new TextColumnHelper("replaceSvc","代收服务费", 70);
		TextColumnHelper loanTermFee = new TextColumnHelper("loanTermFee","贷款服务费",70);
		TextColumnHelper loanTermSvc = new TextColumnHelper("loanTermSvc","分期手续费",70);
		TextColumnHelper premiumAmt =new TextColumnHelper("premiumAmt", "趸交费",	70);
		TextColumnHelper memoAmt =new TextColumnHelper("memoAmt", "未匹配金额",	70);
		TextColumnHelper replacePrepayFee =new TextColumnHelper("replacePrepayFee", "代收提前还款手续费",	70);
		TextColumnHelper replaceMulct =new TextColumnHelper("replaceMulct", "代收罚金",	70);
		TextColumnHelper replacePenalty =new TextColumnHelper("replacePenalty", "代收罚息",	70);
		TextColumnHelper deposit =new TextColumnHelper("deposit", "溢缴款",	70);
		TextColumnHelper replaceLpc =new TextColumnHelper("replaceLpc", "代收滞纳金", 70);
		
		panel = new VerticalPanel();
		panel.setWidth("100%");
		
		showForm = new KylinForm();
		showForm.setField(
				amount.readonly(true),
				dueBillNo.readonly(true),
				interest.readonly(true),
				poundage.readonly(true),
				premium.readonly(true),
				principal.readonly(true),
				lifeInsu.readonly(true),
				stamp.readonly(true),
				replaceSvc.readonly(true),
				loanTermFee.readonly(true),
				loanTermSvc.readonly(true),
				premiumAmt.readonly(true),
				memoAmt.readonly(true),
				replacePrepayFee.readonly(true),
				replaceMulct.readonly(true),
				replacePenalty.readonly(true),
				deposit.readonly(true),
				replaceLpc.readonly(true)
				);
		
		KylinButton cancelButton = new KylinButton("关闭", null);
		cancelButton.addClickEventListener(new IClickEventListener() {

			@Override
			public void onClick() {
				close();
			}
		});
		
		panel.add(showForm);
		panel.add(cancelButton);
		
		return panel;
	}

	@Override
	protected void updateView() {
		if(mapData != null){
			if(mapData.getString("amount") != null && !"null".equals(mapData.getString("amount"))){
				showForm.setFieldValue("amount", mapData.getString("amount"));
			}
			if(mapData.getString("dueBillNo") != null && !"null".equals(mapData.getString("dueBillNo"))){
				showForm.setFieldValue("dueBillNo", mapData.getString("dueBillNo"));
			}
			if(mapData.getString("interest") != null && !"null".equals(mapData.getString("interest"))){
				showForm.setFieldValue("interest", mapData.getString("interest"));
			}
			if(mapData.getString("poundage") != null && !"null".equals(mapData.getString("poundage"))){
				showForm.setFieldValue("poundage", mapData.getString("poundage"));
			}
			if(mapData.getString("premium") != null && !"null".equals(mapData.getString("premium"))){
				showForm.setFieldValue("premium", mapData.getString("premium"));
			}
			if(mapData.getString("principal") != null && !"null".equals(mapData.getString("principal"))){
				showForm.setFieldValue("principal", mapData.getString("principal"));
			}
			if(mapData.getString("lifeInsu") != null && !"null".equals(mapData.getString("lifeInsu"))){
				showForm.setFieldValue("lifeInsu", mapData.getString("lifeInsu"));
			}
			if(mapData.getString("stamp") != null && !"null".equals(mapData.getString("stamp"))){
				showForm.setFieldValue("stamp", mapData.getString("stamp"));
			}
			if(mapData.getString("replaceSvc") != null && !"null".equals(mapData.getString("replaceSvc"))){
				showForm.setFieldValue("replaceSvc", mapData.getString("replaceSvc"));
			}
				showForm.setFieldValue("loanTermSvc", mapData.getString("loanTermSvc"));
				showForm.setFieldValue("loanTermFee", mapData.getString("loanTermFee"));
			if(mapData.getString("premiumAmt") != null && !"null".equals(mapData.getString("premiumAmt"))){
				showForm.setFieldValue("premiumAmt", mapData.getString("premiumAmt"));
			}
			if(mapData.getString("memoAmt") != null && !"null".equals(mapData.getString("memoAmt"))){
				showForm.setFieldValue("memoAmt", mapData.getString("memoAmt"));
			}
			if(mapData.getString("replacePrepayFee") != null && !"null".equals(mapData.getString("replacePrepayFee"))){
				showForm.setFieldValue("replacePrepayFee", mapData.getString("replacePrepayFee"));
			}
			if(mapData.getString("replaceMulct") != null && !"null".equals(mapData.getString("replaceMulct"))){
				showForm.setFieldValue("replaceMulct", mapData.getString("replaceMulct"));
			}
			if(mapData.getString("replacePenalty") != null && !"null".equals(mapData.getString("replacePenalty"))){
				showForm.setFieldValue("replacePenalty", mapData.getString("replacePenalty"));
			}
			if(mapData.getString("deposit") != null && !"null".equals(mapData.getString("deposit"))){
				showForm.setFieldValue("deposit", mapData.getString("deposit"));
			}
			if(mapData.getString("replaceLpc") != null && !"null".equals(mapData.getString("replaceLpc"))){
				showForm.setFieldValue("replaceLpc", mapData.getString("replaceLpc"));
			}
		}
	}

}
