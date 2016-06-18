package com.sunline.ccs.param.ui.client.contrlServMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UNfContrlServMapping;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.core.client.Flat;
import com.sunline.kylin.web.core.client.mvp.Token;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.common.Field;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.function.IFunction;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

@Singleton
public class NfContrlSrvMappingAddPage extends SavePage
{
    private static final String MEMO = "memo";

    @Inject
    private UNfContrlServMapping uNfContrlSrvMapping;

    @Inject
    private ContrSvrMappingConstants constants;

    private TextColumnHelper orgID;

    private KylinForm orgIDForm;

    private KylinForm nfSerDefForm;

    private KylinForm blockCodeForm;

    private KylinForm contrlFieldForm;

    private String memo = "";

    private HashMap<String, String> blocksMap;

    private HashMap<String, String> nfControlFieldMap;

    private boolean isCreated = false;

    @Override
    public IsWidget createPage()
    {
	final VerticalPanel panel = new VerticalPanel();

	// 机构id
	orgIDForm = new KylinForm();

	orgIDForm.setWidth("100%");

	orgID = new TextColumnHelper("orgID", constants.orgID(), 200);

	orgIDForm.setField(orgID.readonly(true));

	// 服务定义
	nfSerDefForm = new KylinForm();

	nfSerDefForm.setWidth("100%");

	TextColumnHelper servCode = uNfContrlSrvMapping.ServCode();
	servCode.required(true);

	nfSerDefForm.setField(servCode.setGroup(constants.nfServiceDef()).setGroupicon("skins/icons/communication.gif")
		.asSelectItem());

	// 锁定码
	blockCodeForm = new KylinForm();
	blockCodeForm.setWidth("100%");
	blockCodeForm.getSetting().labelWidth(80);
	blockCodeForm.setCol(4);

	// 其他状态
	contrlFieldForm = new KylinForm();
	contrlFieldForm.setWidth("100%");
	contrlFieldForm.getSetting().labelWidth(80);
	contrlFieldForm.setCol(4);

	// 获取锁定码数据
	RPC.ajax("rpc/nfContrlSvrMappingServer/getBlockCode ", new RpcCallback<Data>()
	{
	    @SuppressWarnings("unchecked")
	    @Override
	    public void onSuccess(Data result)
	    {

		List<BooleanColumnHelper> blockCodeList = new ArrayList<BooleanColumnHelper>();

		blocksMap = (HashMap<String, String>)result.asMapData().toMap();

		if (blocksMap != null)
		{

		    Set<Entry<String, String>> set = blocksMap.entrySet();
		    Iterator<Entry<String, String>> it = set.iterator();

		    while (it.hasNext())
		    {

			Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			BooleanColumnHelper blockCode =
				new BooleanColumnHelper(key, value).asCheckBoxItem().setGroup(constants.bloceCode())
					.setGroupicon("skins/icons/communication.gif");
			blockCodeList.add(blockCode);
		    }

		    blockCodeForm.getSetting().labelWidth(200);
		    blockCodeForm.setField(blockCodeList.toArray(new BooleanColumnHelper[blockCodeList.size()]));

		}
		panel.add(blockCodeForm);

		// 获取其他状态数据
		RPC.ajax("rpc/nfContrlSvrMappingServer/getNfControlField ", new RpcCallback<Data>()
		{
		    @Override
		    public void onSuccess(Data result)
		    {

			List<BooleanColumnHelper> nfControlFieldList = new ArrayList<BooleanColumnHelper>();

			nfControlFieldMap = (HashMap<String, String>)result.asMapData().toMap();

			if (nfControlFieldMap != null)
			{

			    Set<Entry<String, String>> set = nfControlFieldMap.entrySet();
			    Iterator<Entry<String, String>> it = set.iterator();

			    while (it.hasNext())
			    {

				Entry<String, String> entry = it.next();
				String key = entry.getKey();
				String value = entry.getValue();
				BooleanColumnHelper nfControlField =
					new BooleanColumnHelper(key, value).asCheckBoxItem()
						.setGroup(constants.nfControlField())
						.setGroupicon("skins/icons/communication.gif");
				nfControlFieldList.add(nfControlField);

			    }

			    contrlFieldForm.getSetting().labelWidth(200);
			    contrlFieldForm.setField(nfControlFieldList
				    .toArray(new BooleanColumnHelper[nfControlFieldList.size()]));

			}

			isCreated = true;
			panel.add(contrlFieldForm);
		    }
		});
	    }
	});

	KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		if (!nfSerDefForm.valid()) return;

		MapData md = new MapData();

		MapData choiceMap = new MapData();

		md.put(uNfContrlSrvMapping.ServCode().getName(),
		       nfSerDefForm.getFieldValue(uNfContrlSrvMapping.ServCode().getName()));

		for (Field field : blockCodeForm.getFields())
		{
		    choiceMap.put("block-" + field.getName(), blockCodeForm.getFieldBoolValue(field.getName()));

		    if (blockCodeForm.getFieldBoolValue(field.getName()))
		    {
			memo += blocksMap.get(field.getName()) + " ";
		    }
		}

		for (Field field : contrlFieldForm.getFields())
		{
		    choiceMap.put(field.getName(), contrlFieldForm.getFieldBoolValue(field.getName()));

		    if (contrlFieldForm.getFieldBoolValue(field.getName()))
		    {
			memo += nfControlFieldMap.get(field.getName()) + " ";
		    }
		}

		md.put("fieldCodeMap", choiceMap);

		md.put(MEMO, memo);

		// 保存数据
		RPC.ajax("rpc/nfContrlSvrMappingServer/addNfContrlSvrMapping", new RpcCallback<Data>()
		{
		    @Override
		    public void onSuccess(Data result)
		    {
			notice(false);
			Dialog.tipNotice(constants.success());
			Token token = Flat.get().getCurrentToken();
			token.directPage(NfContrlSrvMappingPage.class);
			Flat.get().goTo(token);
		    }
		}, md);
	    }
	});
	KylinButton cBtn = ClientUtils.creCancelButton(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		Token token = Flat.get().getCurrentToken();
		token.directPage(NfContrlSrvMappingPage.class);
		Flat.get().goTo(token);
	    }
	});
	addButton(submitBtn);
	addButton(cBtn);
	panel.add(orgIDForm);
	panel.add(nfSerDefForm);
	return panel;
    }

    @Override
    public void refresh()
    {
	memo ="";
	
	orgIDForm.getUi().clear();
	nfSerDefForm.getUi().clear();

	if (isCreated)
	{
	    blockCodeForm.getUi().clear();
	    contrlFieldForm.getUi().clear();
	}

	notice(true);

	orgIDForm.setFieldValue(orgID.getName(), Flat.get().getContext().getCurrentUser().getRootOrgCode());

	// 获取服务编码下拉框
	RPC.ajax("rpc/nfContrlSvrMappingServer/getServCode", new RpcCallback<Data>()
	{
	    @Override
	    public void onSuccess(Data result)
	    {
		// Dialog.tipNotice("获取数据成功！");
		SelectItem<String> si2 = new SelectItem<String>();
		si2.setValue(result.asListData());
		nfSerDefForm.setFieldSelectData(uNfContrlSrvMapping.ServCode().getName(), si2);
	    }
	});

    }
}

