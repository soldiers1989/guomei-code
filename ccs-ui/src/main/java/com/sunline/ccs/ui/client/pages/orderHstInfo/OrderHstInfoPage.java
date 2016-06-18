package com.sunline.ccs.ui.client.pages.orderHstInfo;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsOrderHst;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;

@Singleton
public class OrderHstInfoPage extends Page{
	private KylinForm form;
	private KylinGrid grid;
	private KylinForm detailForm;
	@Inject
	private UCcsOrderHst uCcsOrderHst;
	@Override
	public IsWidget createPage() {
		VerticalPanel panel=new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight("100%");
		
		form=new KylinForm();
		{
			form.setWidth("98%");
			form.setField(uCcsOrderHst.ContrNbr(),uCcsOrderHst.GuarantyId(),uCcsOrderHst.OrderId());
			form.setCol(4);
			form.getSetting().labelWidth(70);
			form.addButton(ClientUtils.createSearchButton(new IClickEventListener(){

				@Override
				public void onClick() {
					grid.loadData(form);
				}
				
			}));
		}
		
		grid=new KylinGrid();
		{
			grid.setWidth("98%");
			grid.setHeight("350px");
			grid.setColumns(
					uCcsOrderHst.ContrNbr(),
					uCcsOrderHst.OrderId(),
                    uCcsOrderHst.GuarantyId(),
                    uCcsOrderHst.DueBillNo(),
                    uCcsOrderHst.OrderStatus().columnRender(),
                    uCcsOrderHst.OrderTime(),
                    uCcsOrderHst.TxnType().columnRender(),
                    uCcsOrderHst.TxnAmt(),
                    uCcsOrderHst.LoanUsage().columnRender()
                    );
			grid.getSetting().onSelectRow(new ISelectRowEventListener(){

				@Override
				public void selectRow(MapData rowdata, String rowid,EventObjectHandler rowobj) {
					detailForm.setFormData(rowdata);
				}
				
			});
		}
		
		ScrollPanel layout=new ScrollPanel();
		layout.setWidth("98%");
		layout.setHeight("400px");
		detailForm=new KylinForm();
		{
			detailForm.setWidth("98%");
			detailForm.setHeight("400px");
			detailForm.setField(uCcsOrderHst.AcctNbr().readonly(true),
                    uCcsOrderHst.AcctType().readonly(true),
                    uCcsOrderHst.ContrNbr().readonly(true),
                    uCcsOrderHst.BusinessDate().readonly(true),
                    uCcsOrderHst.CardNo().readonly(true),
                    uCcsOrderHst.CardType().readonly(true),
                    uCcsOrderHst.CertId().readonly(true),
                    uCcsOrderHst.RefNbr().readonly(true),
                    uCcsOrderHst.CertType().readonly(true),
                    uCcsOrderHst.ChannelId().readonly(true),
                    uCcsOrderHst.Code().readonly(true),
                    uCcsOrderHst.City().readonly(true),
                    uCcsOrderHst.CommandType().readonly(true),
                    uCcsOrderHst.Currency().readonly(true),
                    uCcsOrderHst.DueBillNo().readonly(true),
                    uCcsOrderHst.FailureAmt().readonly(true),
                    uCcsOrderHst.Flag().readonly(true),
                    uCcsOrderHst.GuarantyId().readonly(true),
                    uCcsOrderHst.LoanUsage().readonly(true),
                    uCcsOrderHst.LogKv().readonly(true),
                    uCcsOrderHst.MerId().readonly(true),
                    uCcsOrderHst.MerName().readonly(true),
                    uCcsOrderHst.Message().readonly(true),
                    uCcsOrderHst.OnlineFlag().readonly(true),
                    uCcsOrderHst.OpenBank().readonly(true),
                    uCcsOrderHst.OpenBankId().readonly(true),
                    uCcsOrderHst.OptDatetime().readonly(true),
                    uCcsOrderHst.OrberFailTime().readonly(true),
                    uCcsOrderHst.OrderBrief().readonly(true),
                    uCcsOrderHst.OrderId().readonly(true),
                    uCcsOrderHst.OrderStatus().readonly(true),
                    uCcsOrderHst.OrderTime().readonly(true),
                    uCcsOrderHst.OriOrderId().readonly(true),
                    uCcsOrderHst.PayBizCode().readonly(true),
                    uCcsOrderHst.PayChannelId().readonly(true),
                    uCcsOrderHst.ProductType().readonly(true),
                    uCcsOrderHst.Purpose().readonly(true),
                    uCcsOrderHst.SendTime().readonly(true),
                    uCcsOrderHst.SetupDate().readonly(true),
                    uCcsOrderHst.State().readonly(true),
                    uCcsOrderHst.Status().readonly(true),
                    uCcsOrderHst.SubBank().readonly(true),
                    uCcsOrderHst.SuccessAmt().readonly(true),
                    uCcsOrderHst.TxnAmt().readonly(true),
                    uCcsOrderHst.TxnType().readonly(true),
                    uCcsOrderHst.UsrName().readonly(true),
                    uCcsOrderHst.LoanAmt().readonly(true), //添加放款金额、趸交费金额
                    uCcsOrderHst.PremiumAmt().readonly(true),
                    uCcsOrderHst.PremiumInd().readonly(true),
                    uCcsOrderHst.Term().readonly(true),
                    uCcsOrderHst.CouponId().readonly(true),
                    uCcsOrderHst.OffsetType().readonly(true)
			);
			detailForm.setCol(3);
			detailForm.getSetting().labelWidth(160);
			
		}
		panel.add(form);
		panel.add(grid);
		layout.add(detailForm);
		panel.add(layout);
		return panel;
	}
	
	public void refresh(){
		form.getUi().clear();
		grid.clearData();
		detailForm.getUi().clear();
		grid.loadDataFromUrl("rpc/orderHstInfoServer/getOrderList");
	}
	
}
