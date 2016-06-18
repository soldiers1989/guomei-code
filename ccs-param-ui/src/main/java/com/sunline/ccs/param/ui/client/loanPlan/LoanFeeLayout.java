package com.sunline.ccs.param.ui.client.loanPlan;

import java.util.Map;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.sunline.ccs.infrastructure.client.ui.ULoanFeeDef;
import com.sunline.kylin.web.ark.client.helper.IntegerColumnHelper;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;

public class LoanFeeLayout {

	@Inject
	private ULoanFeeDef uLoanFeeDef;

	private KylinGrid loanGrid;

	@Inject
	private LoandFeeInfoDialog loandFeeInfoDialog;

	private Map<String, MapData> loanFeeInfoMap;

	@Inject
	private LoanPlanConstants loanPlanConstants;

	public IsWidget createPage() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		loanGrid = new KylinGrid();
		{
			// 记录显示网格
			loanGrid.setWidth("100%");
			loanGrid.setHeight("100%");
			// 刷新按钮
			loanGrid.setHeader(ClientUtils.createRefreshItem(new IClickEventListener() {

				@Override
				public void onClick() {
//					refresh();
				}
			}));
			// 编辑按钮
			loanGrid.addDblClickListener(new IDblClickRowEventListener() {

				@Override
				public void onDblClickRow(MapData data, String rowid, EventObjectHandler row) {
					String key = data.getString("loanNum");
					loandFeeInfoDialog.setAdd(false);
					loandFeeInfoDialog.setLayoutGrid(loanGrid);
					loandFeeInfoDialog.setLoanFeeInfoMap(loanFeeInfoMap);
					loandFeeInfoDialog.setLoanNum(key);
					loandFeeInfoDialog.show();
				}
			});
			// 增加按钮
			loanGrid.setHeader(ClientUtils.createAddItem(new IClickEventListener() {
				@Override
				public void onClick() {
					loandFeeInfoDialog.setLoanFeeInfoMap(loanFeeInfoMap);
					loandFeeInfoDialog.setLayoutGrid(loanGrid);
					loandFeeInfoDialog.setAdd(true);
					loandFeeInfoDialog.show();
				}
			}));
			// 删除按钮
			loanGrid.setHeader(ClientUtils.createDeleteItem(new IClickEventListener() {
				@Override
				public void onClick() {
					if(loanGrid.getGrid().getSelectedRows().size() < 1){
						Dialog.alert(loanPlanConstants.pleaseChoose());
					}else{
						ListData listData = loanGrid.getGrid().getSelectedRows();
						for(int i = 0; i < listData.size(); i++) {
							loanFeeInfoMap.remove(listData.get(i).asMapData().getString("loanNum"));
						}
						loanGrid.loadData(loandFeeInfoDialog.StringToData(loandFeeInfoDialog.getGridData(loanFeeInfoMap)));
					}
				}
			}));
			// 显示列表名
			loanGrid.setColumns(new IntegerColumnHelper("loanNum", loanPlanConstants.loanNum(), 4, 0, 999).setColumnWidth(120),
					uLoanFeeDef.DistributeMethod().asSelectItem(SelectType.KEY_LABLE).columnRender().setColumnWidth(150), 
					uLoanFeeDef.LoanFeeCalcMethod().asSelectItem(SelectType.KEY_LABLE).columnRender().setColumnWidth(150),
					uLoanFeeDef.FeeAmount().setColumnWidth(150),
					uLoanFeeDef.FeeRate().setColumnWidth(150), 
					uLoanFeeDef.LoanFeeCalcMethod().asSelectItem(SelectType.KEY_LABLE).columnRender().setColumnWidth(150));
		}
		
		panel.add(loanGrid);
		return panel;
	}

	public void updateView() {   
	    loanGrid.loadData(loandFeeInfoDialog.StringToData(loandFeeInfoDialog.getGridData(loanFeeInfoMap)));
	}

	public void clearValues(){
		loanGrid.loadData(loandFeeInfoDialog.StringToData("[]"));
	}

	public Map<String, MapData> getLoanFeeInfoMap() {
		return loanFeeInfoMap;
	}

	public void setLoanFeeInfoMap(Map<String, MapData> loanFeeInfoMap) {
		this.loanFeeInfoMap = loanFeeInfoMap;
	}

	/*
	 * @Inject private ULoanFeeDef uLoanFeeDef;
	 * 
	 * @Inject private LoanPlanConstants loanPlanConstants;
	 * 
	 * @Inject private ClientUtils clientUtils;
	 * 
	 * @Inject private LoandFeeInfoDialog addDialog;
	 * 
	 * @Inject private LoandFeeInfoDialog editDialog;
	 * 
	 * @Inject private ListGrid listGrid;
	 * 
	 * private ListGridField loanNumField;
	 * 
	 * private Map<Integer, LoanFeeDef> data;
	 * 
	 * private boolean changed;
	 * 
	 * private boolean maxAmountRateRequired = false;
	 * 
	 * @Override public String getLayoutTitle() {
	 * 
	 * return loanPlanConstants.loanParam(); }
	 * 
	 * @Override public void createCanvas() {
	 * 
	 * GridHeader header = clientUtils.createGridHeader(); { ToolStripButton
	 * addButton = clientUtils.createAddToolButton(); {
	 * addButton.addClickHandler(new ClickHandler() {
	 * 
	 * @Override public void onClick(ClickEvent event) { addLoanFee(); } }); }
	 * header.addToolStripButton(addButton); } addMember(header);
	 * 
	 * listGrid = new ListGrid(); { DataSource ds = new LocalDataSource() {
	 * 
	 * @Override protected List<? extends Record> buildRecords() {
	 * //把data中的数据组织成Record列表 ArrayList<YakDataSourceRecord> records = new
	 * ArrayList<YakDataSourceRecord>(); int i = 0; if(data != null){ for
	 * (Entry<Integer, LoanFeeDef> entry : data.entrySet()) { Integer loanNum =
	 * entry.getKey(); LoanFeeDef loanFee = entry.getValue();
	 * 
	 * YakDataSourceRecord rec = new YakDataSourceRecord();
	 * rec.setAttributes(LoanFeeDefMapHelper.convertToMap(loanFee));
	 * rec.setAttribute(loanNumField.getName(), loanNum); records.add(rec);
	 * 
	 * LoanFeeInfo lfi = new LoanFeeInfo(); lfi.setLoanNum(loanNum);
	 * lfi.setLoanFeeDef(loanFee); rec.setTag(lfi); rec.setAttribute("pk", i++);
	 * } } return records; } };
	 * 
	 * DataSourceIntegerField pk = new DataSourceIntegerField("pk");
	 * pk.setPrimaryKey(true); ds.setFields( pk, new
	 * IntegerColumnHelper("loanNum",loanPlanConstants.loanNum(), 4 , 0,
	 * 999).createField(), uLoanFeeDef.DistributeMethod().createField(),
	 * uLoanFeeDef.LoanFeeCalcMethod().createField(),
	 * uLoanFeeDef.FeeAmount().createField(),
	 * uLoanFeeDef.FeeRate().createField(),
	 * uLoanFeeDef.LoanFeeCalcMethod().createField() );
	 * 
	 * listGrid.setDataSource(ds); listGrid.setAutoFetchData(true);
	 * listGrid.setWidth100(); listGrid.setHeight100();
	 * listGrid.setSelectionType(SelectionStyle.SIMPLE);
	 * listGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
	 * listGrid.setFields( loanNumField = new
	 * IntegerColumnHelper("loanNum",loanPlanConstants.loanNum(), 4 , 0,
	 * 999).width(150).createLGField(),
	 * uLoanFeeDef.DistributeMethod().width(200).createLGField(),
	 * uLoanFeeDef.LoanFeeCalcMethod().width(150).createLGField(),
	 * uLoanFeeDef.FeeAmount().width(150).createLGField(),
	 * uLoanFeeDef.FeeRate().width(150).createLGField(),
	 * uLoanFeeDef.LoanFeeMethod().width("*").createLGField() );
	 * listGrid.addRecordDoubleClickHandler(new RecordDoubleClickHandler() {
	 * 
	 * @Override public void onRecordDoubleClick(RecordDoubleClickEvent event) {
	 * YakDataSourceRecord rec = (YakDataSourceRecord) event.getRecord();
	 * editTxnFee((LoanFeeInfo) rec.getTag()); } }); } addMember(listGrid);
	 * 
	 * header.createRefreshButton(listGrid);
	 * 
	 * ToolStripButton delBtn = clientUtils.createDeleteToolButton(); {
	 * delBtn.addClickHandler(new ClickHandler() {
	 * 
	 * @Override public void onClick(ClickEvent event) { if
	 * (listGrid.getSelectedRecord() != null) { deleteLoanFee(); } } }); }
	 * header.addToolStripButton(delBtn);
	 * 
	 * addDialog.setAdd(true); editDialog.setAdd(false); }
	 * 
	 * @Override public void updateView(LoanPlan model) {
	 * 
	 * clearValues();
	 * 
	 * data = model.loanFeeDefMap; if (data == null) data = new HashMap<Integer,
	 * LoanFeeDef>();
	 * 
	 * listGrid.invalidateCache(); changed = false;
	 * 
	 * }
	 * 
	 * @Override public boolean updateModel(LoanPlan model) {
	 * model.loanFeeDefMap = data; return changed; }
	 * 
	 * @Override public void clearValues() {
	 * 
	 * data = new HashMap<Integer, LoanFeeDef>(); listGrid.invalidateCache(); }
	 * 
	 * @Override public boolean isChanged() {
	 * 
	 * return changed; }
	 * 
	 * private void addLoanFee() {
	 * addDialog.setMaxAmountRateRequired(maxAmountRateRequired);
	 * addDialog.showDialog(new LoanFeeInfo(), new DialogCallback<LoanFeeInfo>()
	 * {
	 * 
	 * @Override public void onOK(LoanFeeInfo lfi) { if(data == null) data = new
	 * HashMap<Integer, LoanFeeDef>();
	 * 
	 * data.put(lfi.getLoanNum(), lfi.getLoanFeeDef());
	 * listGrid.invalidateCache(); changed = true;
	 * setMaxAmountRateRequired(false); addDialog.hide(); } }); }
	 * 
	 * private void editTxnFee(LoanFeeInfo lfi) { editDialog.showDialog(lfi, new
	 * DialogCallback<LoanFeeInfo>() {
	 * 
	 * @Override public void onOK(LoanFeeInfo data) { //已经直接改了
	 * listGrid.invalidateCache(); changed = true; editDialog.hide(); } }); }
	 * 
	 * private void deleteLoanFee() {
	 * 
	 * ListGridRecord[] records = listGrid.getSelectedRecords();
	 * if(records.length > 0){
	 * 
	 * for(ListGridRecord record : records){
	 * 
	 * YakDataSourceRecord rec = (YakDataSourceRecord)record; LoanFeeInfo lfi =
	 * (LoanFeeInfo)rec.getTag(); data.remove(lfi.getLoanNum()); }
	 * listGrid.invalidateCache(); changed = true; } }
	 *//**
	 * 分期类型为账单分期，则“最大允许分期比例”必须填写
	 * 
	 * @param maxAmountRateRequired
	 */
	/*
	 * public void setMaxAmountRateRequired(boolean maxAmountRateRequired) {
	 * this.maxAmountRateRequired = maxAmountRateRequired; }
	 */
}
