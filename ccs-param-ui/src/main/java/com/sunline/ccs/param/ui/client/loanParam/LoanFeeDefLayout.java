package com.sunline.ccs.param.ui.client.loanParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.sunline.ark.support.def.DomainClientSupport;
import com.sunline.ccs.infrastructure.client.domain.CalcMethodDomainClient;
import com.sunline.ccs.infrastructure.client.ui.ULoanFeeDef;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.kylin.web.ark.client.helper.DecimalColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.IColumnRenderFunctionListener;
import com.sunline.ui.grid.client.listener.IDblClickRowEventListener;
public class LoanFeeDefLayout {
	
	@Inject
	private LoanParamConstants loanParamConstants;
	

	@Inject
	private CalcMethodDomainClient calcMethodDomainClient;
	
	@Inject
	private KylinGrid loanFeeInfoListGrid;
	
	@Inject
	private LoanFeeInfoDialog loanFeeInfoDialog;
	
	private Map<String, MapData> loanFeeDefMap;
	@Inject
	private ULoanFeeDef uLoanFeeDef;
	public IsWidget createCanvas() {
		VerticalPanel loanFeeDefLanyout = new VerticalPanel();
		{
			loanFeeDefLanyout.setWidth("98%");
			loanFeeInfoListGrid = new KylinGrid();
			{
				loanFeeInfoListGrid.getSetting().usePager(false);
				DecimalColumnHelper feeAmountItem=uLoanFeeDef.FeeAmount().columnRender(new IColumnRenderFunctionListener(){

					@Override
					public String render(MapData rowdata, int rowindex, String value,
							EventObjectHandler column) {
						if(rowdata.getString(uLoanFeeDef.FeeAmount().getName())==null||"null".equals(rowdata.getString(uLoanFeeDef.FeeAmount().getName()))) return "";
						else return rowdata.getString(uLoanFeeDef.FeeAmount().getName());
					}
					
				});
				DecimalColumnHelper feeRateItem=uLoanFeeDef.FeeRate().columnRender(new IColumnRenderFunctionListener(){

					@Override
					public String render(MapData rowdata, int rowindex, String value,
							EventObjectHandler column) {
						if(rowdata.getString(uLoanFeeDef.FeeRate().getName())==null||"null".equals(rowdata.getString(uLoanFeeDef.FeeRate().getName()))) return "";
						else return rowdata.getString(uLoanFeeDef.FeeRate().getName());
					}
					
				});
				loanFeeInfoListGrid.setColumns(uLoanFeeDef.LoanFeeDefId().setColumnWidth("14%"),
						uLoanFeeDef.InitTerm().setColumnWidth("14%"),
						uLoanFeeDef.MinAmount().setColumnWidth("14%"),
						uLoanFeeDef.MaxAmount().setColumnWidth("14%"),
						uLoanFeeDef.LoanFeeCalcMethod().columnRender().setColumnWidth("14%"),
						feeAmountItem.setColumnWidth("14%"),
						feeRateItem.setColumnWidth("14%")
						);
				loanFeeInfoListGrid.addDblClickListener(new IDblClickRowEventListener() {
					@Override
					public void onDblClickRow(MapData data, String rowid,
							EventObjectHandler row) {
						String key = data.getString("loanFeeDefId");
						loanFeeInfoDialog.setAdd(false);
						loanFeeInfoDialog.setGrid(loanFeeInfoListGrid);
						loanFeeInfoDialog.setLoanFeeDefMap(loanFeeDefMap);
						loanFeeInfoDialog.setLoanNum(key);
						loanFeeInfoDialog.show();
					}
				});
			}
			loanFeeInfoListGrid.addHeader(ClientUtils
					.createRefreshItem(new IClickEventListener() {
						@Override
						public void onClick() {
							//添加listGrid的本地排序
							ListData beforeSort=loanFeeInfoDialog.StringToData(loanFeeInfoDialog.getGridData(loanFeeDefMap)).asListData();
							loanFeeInfoListGrid.loadData(listSort(beforeSort));
						}
					}));
//			loanFeeInfoListGrid.getSetting().sortName(uLoanFeeDef.InitTerm().getName());
//			loanFeeInfoListGrid.getSetting().sortOrder("desc");
			// 增加按钮
			loanFeeInfoListGrid.setHeader(ClientUtils.createAddItem(new IClickEventListener() {
				@Override
				public void onClick() {
					loanFeeInfoDialog.setAdd(true);
					loanFeeInfoDialog.setGrid(loanFeeInfoListGrid);
					loanFeeInfoDialog.setLoanFeeDefMap(loanFeeDefMap);
					loanFeeInfoDialog.show();
				}
			}));
			// 删除按钮
			loanFeeInfoListGrid.setHeader(ClientUtils.createDeleteItem(new IClickEventListener() {
				@Override
				public void onClick() {
					if(loanFeeInfoListGrid.getGrid().getSelectedRows().size() < 1){
						Dialog.alert(loanParamConstants.pleaseChoose());
					}else{
						ListData listData = loanFeeInfoListGrid.getGrid().getSelectedRows();
						
						for(int i = 0; i < listData.size(); i++) {
							String s = loanFeeDefMap.get(listData.get(i).asMapData().getString(uLoanFeeDef.LoanFeeDefId().getName())).asMapData().getString(uLoanFeeDef.LoanFeeDefStatus().getName());
							if(s != null && ! s.equals("I")){
								Dialog.alert("删除的子产品中包含非新建状态的子产品，请重新操作");
								return ;
							}
						}
						
						for(int i = 0; i < listData.size(); i++) {
							loanFeeDefMap.remove(listData.get(i).asMapData().getString("loanFeeDefId"));
						}
						ListData beforeSort=loanFeeInfoDialog.StringToData(loanFeeInfoDialog.getGridData(loanFeeDefMap)).asListData();
						loanFeeInfoListGrid.loadData(listSort(beforeSort));
					}
				}
			}));
			
			loanFeeDefLanyout.add(loanFeeInfoListGrid);
		}
		
		return loanFeeDefLanyout;
	}

	public void updateView(Map<String, MapData> loanFeeDefMap) {
		this.loanFeeDefMap = loanFeeDefMap;
		ListData beforeSort=loanFeeInfoDialog.StringToData(loanFeeInfoDialog.getGridData(loanFeeDefMap)).asListData();
		loanFeeInfoListGrid.loadData(listSort(beforeSort));
	}
	

	public void clearValues() {
		loanFeeInfoListGrid.loadData(loanFeeInfoDialog.StringToData("[]"));
	}

	public void updateModel(MapData loanData) {

		loanFeeInfoDialog.updateModel(loanData);
	}
	
	  public final EnumColumnHelper<CalcMethod> LoanFeeCalcMethod() {
	        return new EnumColumnHelper<CalcMethod>("loanFeeCalcMethod",loanParamConstants.loanFeeCalcMethod(), CalcMethod.class){
	            public DomainClientSupport<String> getDomain(){
	                return calcMethodDomainClient;
	            }
	        };
	    }
	  
	  public ListData listSort(ListData gridData){
		  List<Integer> initTermList=new ArrayList<Integer>();
		  //一维--期数排序
		  
		  //1--期数排序
		  ListData gridDataCopy=gridData;
		  ListData sortedTermData=new ListData();
		  while(gridDataCopy.size()!=0){
			  int minTerm=gridDataCopy.get(0).asMapData().getInteger(uLoanFeeDef.InitTerm().getName());
			  int minIndex=0;
			  for(int j=0;j<gridDataCopy.size();j++){
				  if(gridDataCopy.get(j).asMapData().getInteger(uLoanFeeDef.InitTerm().getName())<minTerm){
					  minTerm=gridDataCopy.get(j).asMapData().getInteger(uLoanFeeDef.InitTerm().getName());
					  minIndex=j;
				  }
			  }
			  sortedTermData.add(gridDataCopy.get(minIndex).asMapData());
			  gridDataCopy.remove(minIndex);
		  }
		  
		  //已经按照期数排序完成
		  gridData=sortedTermData;
		  
		  //2--筛选期数(可以优化)
		  for(int i=0;i<gridData.size();i++){
			  if(!initTermList.contains(gridData.get(i).asMapData().getInteger(uLoanFeeDef.InitTerm().getName())))
			  initTermList.add(gridData.get(i).asMapData().getInteger(uLoanFeeDef.InitTerm().getName()));
		  }
		  //二维--最小金额排序
		  ListData beforeSort=new ListData();
		  ListData target=new ListData();
		  int i,j;
		  j=0;
		  for(i=0;i<initTermList.size();i++){
			  while(j<gridData.size()&&gridData.get(j).asMapData().getInteger(uLoanFeeDef.InitTerm().getName()).compareTo(initTermList.get(i))==0){
				  beforeSort.add(gridData.get(j).asMapData());
				  j++;
			  }
			  if(beforeSort.size()==0) continue;
			  //对相同期数进行最小金额的排序
			  ListData sortedMinAmountList=sortBigDecimal(beforeSort);
			  beforeSort=new ListData();
			  for(int k=0;k<sortedMinAmountList.size();k++){
				  target.add(sortedMinAmountList.get(k));
			  }
		  }
		  return target;
	  }
	  

	private ListData sortBigDecimal(ListData minAmountList) {
		//如果长度为1,则不排序
		if(minAmountList.size()==1) return minAmountList;
		ListData target=new ListData();
			while(minAmountList.size()!=0){
				BigDecimal minBigDecimal=minAmountList.get(0).asMapData().getBigDecimal(uLoanFeeDef.MinAmount().getName());
				int minIndex=0;
				//当长度为1时，跳过排序，直接取剩下的一个
				if(minAmountList.size()==1){
					target.add(minAmountList.get(0).asMapData());
					break;
				}
			for(int j=0;j<minAmountList.size();j++){
				//通过冒泡的方式，取出剩余最小金额中的最小值
				if(minAmountList.get(j).asMapData().getBigDecimal(uLoanFeeDef.MinAmount().getName()).compareTo(minBigDecimal)<=0){
					minBigDecimal=minAmountList.get(j).asMapData().getBigDecimal(uLoanFeeDef.MinAmount().getName());
					minIndex=j;
				}
			}
			target.add(minAmountList.get(minIndex).asMapData());
			minAmountList.remove(minIndex);
			}
		return target;
	}
}
