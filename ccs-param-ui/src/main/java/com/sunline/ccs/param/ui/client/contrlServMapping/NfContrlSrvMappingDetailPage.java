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
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;

/*
 * import com.sunline.pcm.ui.common.client.ClientContext; import
 * com.sunline.pcm.ui.common.client.ClientUtils; import
 * com.sunline.pcm.ui.common.client.DispatcherPage; import
 * com.sunline.pcm.ui.cp.client.panel.ControlPanelPlace; import
 * com.sunline.ccs.param.def.BlockCode; import
 * com.sunline.ccs.param.def.NfContrlServMapping; import
 * com.sunline.ccs.param.def.NfControlField; import
 * com.sunline.ccs.param.def.NfServiceDef; import
 * com.sunline.ark.gwt.client.mvp.ParamMapToken; import
 * com.sunline.ark.gwt.client.ui.TextColumnHelper; import
 * com.sunline.ark.gwt.client.ui.YakDynamicForm; import
 * com.sunline.ark.gwt.client.util.ClientFactory; import
 * com.sunline.ark.gwt.client.util.RPCExecutor; import
 * com.sunline.ark.gwt.client.util.RPCTemplate; import
 * com.google.gwt.resources.client.ImageResource; import
 * com.google.gwt.user.client.rpc.AsyncCallback; import
 * com.google.inject.Inject; import com.smartgwt.client.types.Alignment; import
 * com.smartgwt.client.widgets.IButton; import
 * com.smartgwt.client.widgets.events.ClickEvent; import
 * com.smartgwt.client.widgets.events.ClickHandler; import
 * com.smartgwt.client.widgets.form.fields.CheckboxItem; import
 * com.smartgwt.client.widgets.form.fields.FormItem; import
 * com.smartgwt.client.widgets.layout.HLayout;
 */
/**
 * 非金融服务控制约束页面配置
 * 
 * @author guxiaoyu
 * @param <T>
 *
 */
@Singleton
public class NfContrlSrvMappingDetailPage extends SavePage
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

    private String servCode;

    private String memo;

    private VerticalPanel panel;

    @Override
    public IsWidget createPage()
    {
	panel = new VerticalPanel();

	// 机构id
	orgIDForm = new KylinForm();

	orgIDForm.setWidth("100%");

	orgID = new TextColumnHelper("orgID", constants.orgID(), 200);

	orgIDForm.setField(orgID.readonly(true));

	// 服务定义

	nfSerDefForm = new KylinForm();

	nfSerDefForm.setWidth("100%");

	nfSerDefForm.setField(uNfContrlSrvMapping.ServCode().required(true).setGroup(constants.nfServiceDef())
		.setGroupicon("skins/icons/communication.gif").readonly(true));

	// 锁定码

	blockCodeForm = new KylinForm();
	blockCodeForm.setWidth("100%");
	blockCodeForm.setCol(4);
	blockCodeForm.getSetting().labelWidth(200);

	// 其他状态
	contrlFieldForm = new KylinForm();
	contrlFieldForm.setWidth("100%");
	contrlFieldForm.setCol(4);
	contrlFieldForm.getSetting().labelWidth(200);

	KylinButton submitBtn = ClientUtils.creConfirmButton(new IClickEventListener()
	{
	    @Override
	    public void onClick()
	    {
		if (!nfSerDefForm.valid()) return;

		MapData md = new MapData();

		MapData choiceMap = new MapData();

		md.put(uNfContrlSrvMapping.ServCode().getName(), servCode);

		for (Field field : blockCodeForm.getFields())
		{
		    choiceMap.put("block-" + field.getName(), blockCodeForm.getFieldBoolValue(field.getName()));
		}

		for (Field field : contrlFieldForm.getFields())
		{
		    choiceMap.put(field.getName(), contrlFieldForm.getFieldBoolValue(field.getName()));
		}

		md.put("fieldCodeMap", choiceMap);

		md.put(MEMO, memo);

		RPC.ajax("rpc/nfContrlSvrMappingServer/updateNfContrlSvrMapping ", new RpcCallback<Data>()
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
	
         
	// 获取锁定码数据
	RPC.ajax("rpc/nfContrlSvrMappingServer/getBlockCode", new RpcCallback<Data>()
	{
	    @SuppressWarnings("unchecked")
	    @Override
	    public void onSuccess(Data result)
	    {
		List<BooleanColumnHelper> blockCodeList = new ArrayList<BooleanColumnHelper>();

		HashMap<String, String> blocksMap = (HashMap<String, String>)result.asMapData().toMap();

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

			HashMap<String, String> nfControlFieldMap = (HashMap<String, String>)result.asMapData().toMap();

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

			    contrlFieldForm.setField(nfControlFieldList
				    .toArray(new BooleanColumnHelper[nfControlFieldList.size()]));

			}

			panel.add(contrlFieldForm);

			//填充数据
			notice(true);
			orgIDForm.setFieldValue(orgID.getName(), Flat.get().getContext().getCurrentUser()
				.getRootOrgCode());

			Token token = Flat.get().getCurrentToken();
			servCode = token.getParam(uNfContrlSrvMapping.ServCode().getName());
			memo = token.getParam(MEMO);

			RPC.ajax("rpc/nfContrlSvrMappingServer/getNfContrlSvrMapping", new RpcCallback<Data>()
			{
			    @Override
			    public void onSuccess(Data result)
			    {
				if (result.asMapData().getData("fieldCodeMap") != null)
				{
				    Map<String, Object> rsltMap =
					    (Map<String, Object>)result.asMapData().getData("fieldCodeMap").asMapData()
						    .toMap();

				    //锁定码
				    for (Field field : blockCodeForm.getFields())
				    {
					if (rsltMap.containsKey("block-" + field.getName()))
					{
					    blockCodeForm.setFieldValue(field.getName(),
									"false".equals(String.valueOf(rsltMap
										.get("block-" + field.getName()))) ? Boolean.FALSE : Boolean.TRUE);
					}
				    }

				    //其他状态控制
				    contrlFieldForm.setFormData(result.asMapData().getData("fieldCodeMap"));
				}
			    }
			}, servCode);

			RPC.ajax("rpc/nfContrlSvrMappingServer/getNfContrlSvrMappingDec", new RpcCallback<Data>()
			{
			    @Override
			    public void onSuccess(Data arg0)
			    {
				nfSerDefForm.setFieldValue(uNfContrlSrvMapping.ServCode().getName(), arg0.toString());
			    }
			}, servCode);

			
			orgIDForm.getUi().clear();
 		        nfSerDefForm.getUi().clear();
			blockCodeForm.getUi().clear();
			contrlFieldForm.getUi().clear();
			//获取机构ID
			orgIDForm.setFieldValue(orgID.getName(), Flat.get().getContext().getCurrentUser().getRootOrgCode());

		    }
		});
	    }
	});
    }
}
