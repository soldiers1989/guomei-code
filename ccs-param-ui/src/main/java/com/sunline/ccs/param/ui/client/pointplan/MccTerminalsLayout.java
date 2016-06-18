package com.sunline.ccs.param.ui.client.pointplan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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
import com.sunline.ui.layout.client.Layout;

//注意，这里不是singleton
@SuppressWarnings("serial")
public class MccTerminalsLayout extends Layout {

	@Inject
	private PointPlanConstants constants;

	private KylinGrid grid;

	private final static String MCC = "mcc";

	private final static String TERMINALS = "terminals";

	private boolean created = false;

	private boolean refesh = false;

	private Data updateData;

	protected IsWidget createCanvas() {
		final VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight("100%");

		grid = new KylinGrid();
		grid.getSetting().enabledEdit(true);
		grid.getSetting().usePager(false);
		grid.setWidth(352);

		grid.setHeader(ClientUtils.createAddItem(new IClickEventListener() {
			@Override
			public void onClick() {
				grid.getUi().addEditRow();
			}
		}), ClientUtils.createDeleteItem(new IClickEventListener() {
			@Override
			public void onClick() {
				grid.getUi().deleteSelectedRow();
			}
		}));
		// 标题
		final TextColumnHelper mccText = new TextColumnHelper(MCC,
				constants.mcc(), 120);
		final TextColumnHelper terminalText = new TextColumnHelper(TERMINALS,
				constants.terminal(), 120);
		RPC.ajax("rpc/pcmSelectOptionServer/getMcc", new RpcCallback<Data>() {
			@Override
			public void onSuccess(Data result) {
				grid.setColumns(mccText.setColumnWidth(200).setColunmEditor(
						new Editor().type(EditorType.SELECT).data(
								result.toString())));
				grid.setColumns(terminalText.setColumnWidth(120)
						.setColunmEditor(new Editor().type(EditorType.TEXT)));

				panel.add(grid);
				created = true;
				if (refesh) {
					updateLayout(updateData);
				}
			}
		});
		return panel;
	}

	public MapData getValue() {
		grid.getUi().endEdit();
		ListData gridData = grid.getUi().getData();

		Set<String> mcc = new HashSet<String>();

		MapData md = new MapData();

		String key;
		String value;
		for (int i = 0; i < gridData.size(); i++) {
			key = (String) gridData.get(i).asMapData().toMap().get(MCC);
			value = (String) gridData.get(i).asMapData().toMap().get(TERMINALS);
			if (!mcc.contains(key)) {
				mcc.add(key);

				ListData ld = new ListData();
				ld.addString(value);

				md.put(key, ld);
			} else {
				md.getData(key).asListData().addString(value);
			}
		}

		return md;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void updateLayout(Data data) {
		if (created) {
			Data temp = new Data();
			MapData md = data.asMapData();
			List<String> list = new ArrayList<String>();
			for (Entry entry : md.toMap().entrySet()) {
				list = (ArrayList<String>) entry.getValue();
				for (int i = 0; i < list.size(); i++) {
					temp.asMapData()
							.put(MCC, (String) entry.getKey());
					temp.asMapData()
					.put(TERMINALS, list.get(i));
				}
			}
			ListData result = new ListData();
			result.add(temp);
			grid.loadData(result);
		} else {
			refesh = true;
			updateData = data;
		}
	}

	public void clearValues() {
		Data data = new Data();
		data.setJsData(DataUtil.convertDataType("[]"));
		grid.loadData(data);
	}

}
