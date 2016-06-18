package com.sunline.ccs.param.ui.client.program;

import java.util.HashMap;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.SavePage;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.GridEditParamEntity;
import com.sunline.ui.grid.client.listener.IBeforeEditListener;
/**
 * 分期活动:分行列表
 * 
 * @author lisy
 * @version [版本号, Jun 26, 2015]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
@Singleton
public class CtrlBranchListLayout extends SavePage {

    @Inject
    private ProgramConstants constants;

    private TextColumnHelper branchSelect;
    private KylinGrid grid;
	private static String CtrlBranch = "ctrlBranch";

	private ListData enumListData;
    private HashMap<String,String> enumMap;

    @Override
    public IsWidget createPage() {
	final VerticalPanel panel = new VerticalPanel();

	panel.setWidth("100%");
	// panel.setHorizontalAlignment(HasAlignment.ALIGN_RIGHT);

	grid = new KylinGrid();
		grid.getSetting().enabledEdit(true);
		grid.getSetting().usePager(false);
		enumMap = new HashMap<String,String>();

		// refrush();
		grid.setHeader(ClientUtils.createAddItem(new IClickEventListener(){
		    @Override
		    public void onClick() {
			grid.getUi().addEditRow();
		    }
		}), ClientUtils.createDeleteItem(new IClickEventListener(){
		    @Override
		    public void onClick() {
			grid.getUi().deleteSelectedRow();
		    }
		}));

		// 记录显示网格
		 grid.setWidth("100%");
		 grid.setHeight("100%");
		// 设置禁止分页
		grid.getSetting().usePager(false);

		branchSelect = new TextColumnHelper("ctrlBranch", constants.ctrlBranch(), 400);
		grid.setColumns(branchSelect.setColumnWidth(400).setColunmEditor(new Editor().type(EditorType.SELECT)).columnRender(enumMap));
		grid.getSetting().onBeforeEdit(new IBeforeEditListener() {

			@Override
			public boolean onBeforeEdit(GridEditParamEntity editParm) {
				MapData columnData = editParm.getColumn();
				if(CtrlBranch.equals(editParm.getColumnName())) {
					MapData editor = columnData.getData("editor").asMapData();
					if(enumListData == null){
						enumListData = new ListData();
					}
					editor.put("data", enumListData);
				}
				return true;
			}
		});
		panel.add(grid);


	return panel;
    }

    @SuppressWarnings("unchecked")
    public void updataView(ListData listData) {
    	RPC.ajax("rpc/ccsSelectOptionServer/getBranchList", new RpcCallback<Data>(){
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
    	
	grid.clearData();

	ListData gridData = new ListData();
	
	for(int i = 0; i < listData.toList().size(); i++)
	{
	    MapData dataMap = new MapData();
	    dataMap.put("ctrlBranch", listData.getString(i));
	    gridData.add(dataMap);
	}
	
	grid.loadData(gridData);
    }

    public MapData sumbitData() {
    	grid.getUi().endEdit();
	ListData target=new ListData();
	ListData ld = grid.getData();
	for (int i=0; i < ld.size(); i++) {
	    if(ld.get(i).asMapData().getString("ctrlBranch")!=null&&
		    !ld.get(i).asMapData().getString("ctrlBranch").equals("")){
		
	    target.addString(ld.get(i).asMapData().getString("ctrlBranch"));
	    }
	}
	MapData md=new MapData();
	md.put("ctrlBranchList",target.asMapData());
	return md;
    }
}