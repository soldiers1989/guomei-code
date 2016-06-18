package com.sunline.ccs.ui.client.pages.cardinfo;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsCard;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardLmMapping;
import com.sunline.ccs.infrastructure.client.ui.UCcsCardO;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.ColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;

/**
 * 
 * @see 类名：QueryCardInfo
 * @see 描述：卡片信息查询
 *
 * @see 创建日期：   Jun 22, 20155:42:07 PM
 * @author yeyu
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Singleton
public class QueryCardInfo extends Page {
	
	@Inject 
	private UCcsCard uCcsCard;
	
	@Inject
	private UCcsCustomer uCcsCustomer;
	
	@Inject
	private UCcsCardLmMapping cardMapping;

	/**
	 * 表单布局面板
	 */
	private StackPanel formPanel;
	/**
	 * 表格布局面板
	 */
	private StackPanel gridPanel;
	/**
	 * 选项卡布局面板
	 */
	private StackPanel tabPanel;
	/**
	 * 主窗体
	 */
	private VerticalPanel mainWindow;
	/**
	 * 查询表单
	 */
	private KylinForm queryForm;
	/**
	 * 卡片信息表格
	 */
	private KylinGrid cardInfoGrid;
	/**
	 * 选项卡
	 */
	private Tab detailsTab;
	/**
	 * 选项卡配置参数类
	 */
	private TabSetting setting;
	/**
	 * 
	 */
	private KylinForm detailsForm;
	/**
	 * 
	 */
	private StackPanel stackPanel;
	/**
	 * 
	 */
	@Inject
	private UCcsCardO uCcsCardO;
	
	@Override
	public void refresh() {
		queryForm.getUi().clear();
		cardInfoGrid.clearData();
		detailsForm.getUi().clear();
	}

	@Override
	public IsWidget createPage() {
		this.buildMainWindow();
		this.buildForm(mainWindow);
		this.buildCardInfoGrid(mainWindow);
		this.buildTab(mainWindow);
		return mainWindow;
	}
	
	private void buildTab(VerticalPanel mainWindow){
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.add(this.getTab());
		mainWindow.add(panel);
	}
	
	private Tab getTab(){
		setting = new TabSetting();
		setting.dblClickToClose(false).dragToMove(false);
		detailsTab = new Tab(setting);
		TabItemSetting item = new TabItemSetting(null,"卡片详细信息");
		ScrollPanel detialsPanel = new ScrollPanel();
		detailsForm = this.getForm();
		detailsForm.getSetting().labelWidth(130);
		detailsForm.setField(new ColumnHelper[]{
				uCcsCustomer.Name().readonly(true),
				uCcsCustomer.IdType().readonly(true),
				uCcsCustomer.IdNo().readonly(true),
				uCcsCard.ProductCd().readonly(true),
				uCcsCard.FirstUsageFlag().readonly(true),
				uCcsCard.CardExpireDate().readonly(true),
				uCcsCard.BscSuppInd().readonly(true),
				uCcsCard.LogicCardNbr().readonly(true),
				uCcsCard.OwningBranch().readonly(true), //发卡网点
				uCcsCard.RelationshipToBsc().readonly(true),
				uCcsCard.SetupDate().readonly(true),
				uCcsCard.CloseDate().readonly(true),
				uCcsCard.WaiveCardfeeInd().readonly(true),
				//newCardIssueIndItem,//卡状态
				uCcsCard.ActiveInd().readonly(true),
				uCcsCard.ActiveDate().readonly(true),
				uCcsCard.PosPinVerifyInd().readonly(true),
				//qQinExistInd,
				//pPinExistInd ,
				uCcsCard.NextCardFeeDate().readonly(true),
				uCcsCard.RenewInd().readonly(true), //卡片续卡信息
				uCcsCard.RenewRejectCd().readonly(true),
				uCcsCardO.PinTries().readonly(true), //卡片密码信息
				uCcsCardO.InqPinTries().readonly(true),
				uCcsCardO.LastPinTriesTime().readonly(true),
				uCcsCardO.LastInqPinTriesTime().readonly(true),
				uCcsCard.BlockCode().readonly(true)
		});
		detialsPanel.setHeight("350px");
		detialsPanel.setWidth("100%");
		detialsPanel.add(detailsForm);
		detailsTab.addItem(item, detialsPanel);
		return detailsTab;
	}
	
	/**
	 * 
	 * @see 方法名：buildCardInfoGrid 
	 * @see 描述：创建卡片查询结果列表
	 * @see 创建日期：Jun 22, 20156:31:38 PM
	 * @author yeyu
	 *  
	 * @param mainWindow
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void buildCardInfoGrid(VerticalPanel mainWindow){
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight("280px");
		this.getGrid(); 
		panel.add(cardInfoGrid);
		mainWindow.add(panel);
	}
	
	/**
	 * 
	 * @see 方法名：getGrid 
	 * @see 描述：获取KylinGrid
	 * @see 创建日期：Jun 22, 20156:30:20 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private KylinGrid getGrid(){
		cardInfoGrid = new KylinGrid();
		cardInfoGrid.setWidth("100%");
		cardInfoGrid.setHeight("280px");
		cardInfoGrid.setColumns(new ColumnHelper[]{
				uCcsCard.LastestMediumCardNbr(),
				uCcsCard.BscSuppInd(),
				uCcsCard.ProductCd(),
				uCcsCard.OwningBranch(),
				uCcsCard.BlockCode(),
				uCcsCard.SetupDate()
		});
		cardInfoGrid.getSetting().delayLoad(true);
		cardInfoGrid.loadDataFromUrl("rpc/CardInfoServer/queryCardInfo");
		cardInfoGrid.getSetting().onSelectRow(new ISelectRowEventListener() {

		    @Override
		    public void selectRow(MapData rowdata, String rowid, EventObjectHandler rowobj) {
		    	detailsForm.setFormData(rowdata);
		    }
		    
		});
		//TODO un select row 无效
		cardInfoGrid.getSetting().onUnSelectRow(new ISelectRowEventListener() {

		    @Override
		    public void selectRow(MapData rowdata, String rowid, EventObjectHandler rowobj) {
		    	detailsForm.getUi().clear();
		    }
		});
		return cardInfoGrid;
	}
	
	/**
	 * 
	 * @see 方法名：buildMainWindow 
	 * @see 描述：创建主面板
	 * @see 创建日期：Jun 22, 20155:47:12 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private VerticalPanel buildMainWindow(){
		mainWindow = new VerticalPanel();
		mainWindow.setWidth("98%");
		mainWindow.setHeight("100%");
		return mainWindow;
	}
	
	/**
	 * 
	 * @see 方法名：buildForm 
	 * @see 描述：创建查询表单
	 * @see 创建日期：Jun 22, 20156:24:57 PM
	 * @author yeyu
	 *  
	 * @param mainWindow
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void buildForm(VerticalPanel mainWindow){
		KylinButton btnSubmit = ClientUtils.createSearchButton(new IClickEventListener(){
			@Override
			public void onClick() {
				if(queryForm.valid()) {
					cardInfoGrid.loadData(queryForm);
				} else {
					
				}
			}
		});
		queryForm = this.getForm();
		queryForm.setField(new ColumnHelper[]{
				cardMapping.CardNbr().required(true)
		});
		HorizontalPanel hPanel = CommonUiUtils.lineLayoutForm(queryForm, btnSubmit, null, null);
		mainWindow.add(hPanel);
	}
	
	/**
	 * 
	 * @see 方法名：getForm 
	 * @see 描述：获取表单
	 * @see 创建日期：Jun 22, 20155:50:41 PM
	 * @author yeyu
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private KylinForm getForm(){
	    KylinForm newForm = new KylinForm();
	    newForm.setWidth("100%");
		return newForm;
	}
}
