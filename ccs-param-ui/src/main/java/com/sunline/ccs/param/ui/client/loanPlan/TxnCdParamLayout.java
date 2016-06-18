package com.sunline.ccs.param.ui.client.loanPlan;

import java.util.HashMap;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.GridEditParamEntity;
import com.sunline.ui.grid.client.listener.IBeforeEditListener;

public class TxnCdParamLayout {

	@Inject
	private LoanPlanConstants constants;

	private TextColumnHelper txnSelect;

	private KylinGrid paramGrid;

	private ListData txnCdList;

	private static String TxnCd = "txnCd";

	private ListData enumListData;
	
	private HashMap<String, String> enumMap;

	public IsWidget createPage() {
		final VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		paramGrid = new KylinGrid();
		paramGrid.setWidth("100%");
		paramGrid.setHeight("100%");
		paramGrid.getSetting().enabledEdit(true);
		paramGrid.getSetting().usePager(false);
		paramGrid.getSetting().checkbox(false);
		txnSelect = new TextColumnHelper(TxnCd, constants.txnCd(), 300);
		enumMap = new HashMap<String, String>();
		paramGrid.setColumns(txnSelect.setColumnWidth(300).setColunmEditor(new Editor().type(EditorType.SELECT)).columnRender(enumMap));
		// 编辑前事件，根据类型初始化值域下拉框的值
		paramGrid.getSetting().onBeforeEdit(new IBeforeEditListener() {

			@Override
			public boolean onBeforeEdit(GridEditParamEntity editParm) {
				MapData columnData = editParm.getColumn();
				if(TxnCd.equals(editParm.getColumnName())) {
					MapData editor = columnData.getData("editor").asMapData();
					if(enumListData == null){
						enumListData = new ListData();
					}
					editor.put("data", enumListData);
				}
				return true;
			}
		});
		paramGrid.setHeader(ClientUtils.createAddItem(new IClickEventListener() {

			@Override
			public void onClick() {
				paramGrid.getUi().addEditRow();
			}
		}), ClientUtils.createDeleteItem(new IClickEventListener() {

			@Override
			public void onClick() {
				paramGrid.getUi().deleteSelectedRow();
			}
		}));
		
		panel.add(paramGrid);
		return panel;
	}

	public void updateView() {
		RPC.ajax("rpc/ccsSelectOptionServer/getTxnCd", new RpcCallback<Data>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(Data result) {
				MapData map = new MapData();
				ListData data = result.asListData();
				if(enumListData == null){
					enumListData = new ListData();
				}
				for(int i=0; i<data.size(); i++){
					map.put(data.get(i).asMapData().getString("id"), data.get(i).asMapData().getString("text"));
					enumListData.add(data.get(i));
				}
				enumMap.putAll((HashMap<String, String>) map.toMap());
			}
		});
		if(txnCdList != null && txnCdList.size() > 0) {
			ListData showList = new ListData();
			for(int i = 0; i < txnCdList.size(); i++) {
				MapData map = new MapData();
				map.put(TxnCd, txnCdList.getString(i));
				showList.add(map);
			}
			paramGrid.loadData(showList);
		}
	}

	public void setTxnCdList(ListData txnCdList) {
		this.txnCdList = txnCdList;
	}

	public Data getSubmitData() {
		paramGrid.getUi().endEdit();
		ListData inputData = paramGrid.getData().asListData();
		ListData submitData = new ListData();
		if(inputData != null && inputData.size() > 0) {
			for(int i = 0; i < inputData.size(); i++) {
				submitData.addString(inputData.get(i).asMapData().getString(TxnCd));
			}
		}
		return submitData;
	}

	public ListData getTxnCdList() {
		return txnCdList;
	}

	public void clearValues() {
		paramGrid.loadData(StringToData("[]"));
	}

	public Data StringToData(String gridDataString) {
		Data data = new Data();
		data.setJsData(DataUtil.convertDataType(gridDataString));
		return data;
	}
}
