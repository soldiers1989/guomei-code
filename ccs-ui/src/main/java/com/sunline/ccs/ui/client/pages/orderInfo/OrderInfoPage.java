package com.sunline.ccs.ui.client.pages.orderInfo;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsOrder;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;

@Singleton
public class OrderInfoPage extends Page{
	private KylinForm form;
	private KylinGrid grid;
	private KylinForm detailForm;
	@Inject
	private UCcsOrder uCcsOrder;
	@Override
	public IsWidget createPage() {
		VerticalPanel panel=new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight("100%");
		
		form=new KylinForm();
		{
			form.setWidth("98%");
			form.setField(uCcsOrder.ContrNbr(),uCcsOrder.GuarantyId(),uCcsOrder.OrderId());
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
					uCcsOrder.OrderId(),
					uCcsOrder.ContrNbr(),
                    uCcsOrder.GuarantyId(),
                    uCcsOrder.DueBillNo(),
                    uCcsOrder.OrderStatus().columnRender(),
                    uCcsOrder.OrderTime(),
                    uCcsOrder.TxnType().columnRender(),
                    uCcsOrder.TxnAmt(),
                    uCcsOrder.LoanUsage().columnRender()
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
			detailForm.setField(uCcsOrder.AcctNbr().readonly(true),
                    uCcsOrder.AcctType().readonly(true),
                    uCcsOrder.ContrNbr().readonly(true),
                    uCcsOrder.BusinessDate().readonly(true),
                    uCcsOrder.CardNo().readonly(true),
                    uCcsOrder.CardType().readonly(true),
                    uCcsOrder.CertId().readonly(true),
                    uCcsOrder.CertType().readonly(true),
                    uCcsOrder.RefNbr().readonly(true),
                    uCcsOrder.ChannelId().readonly(true),
                    uCcsOrder.Code().readonly(true),
                    uCcsOrder.City().readonly(true),
                    uCcsOrder.CommandType().readonly(true),
                    uCcsOrder.Currency().readonly(true),
                    uCcsOrder.DueBillNo().readonly(true),
                    uCcsOrder.FailureAmt().readonly(true),
                    uCcsOrder.Flag().readonly(true),
                    uCcsOrder.GuarantyId().readonly(true),
                    uCcsOrder.LoanUsage().readonly(true),
                    uCcsOrder.LogKv().readonly(true),
                    uCcsOrder.MerId().readonly(true),
                    uCcsOrder.MerName().readonly(true),
                    uCcsOrder.Message().readonly(true),
                    uCcsOrder.OnlineFlag().readonly(true),
                    uCcsOrder.OpenBank().readonly(true),
                    uCcsOrder.OpenBankId().readonly(true),
                    uCcsOrder.OptDatetime().readonly(true),
                    uCcsOrder.OrberFailTime().readonly(true),
                    uCcsOrder.OrderBrief().readonly(true),
                    uCcsOrder.OrderId().readonly(true),
                    uCcsOrder.OrderStatus().readonly(true),
                    uCcsOrder.OrderTime().readonly(true),
                    uCcsOrder.OriOrderId().readonly(true),
                    uCcsOrder.PayBizCode().readonly(true),
                    uCcsOrder.PayChannelId().readonly(true),
                    uCcsOrder.ProductType().readonly(true),
                    uCcsOrder.Purpose().readonly(true),
                    uCcsOrder.SendTime().readonly(true),
                    uCcsOrder.SetupDate().readonly(true),
                    uCcsOrder.State().readonly(true),
                    uCcsOrder.Status().readonly(true),
                    uCcsOrder.SubBank().readonly(true),
                    uCcsOrder.SuccessAmt().readonly(true),
                    uCcsOrder.TxnAmt().readonly(true),
                    uCcsOrder.TxnType().readonly(true),
                    uCcsOrder.UsrName().readonly(true),
                    uCcsOrder.LoanAmt().readonly(true),//添加放款金额、趸交费金额
                    uCcsOrder.PremiumAmt().readonly(true),
                    uCcsOrder.PremiumInd().readonly(true),
                    uCcsOrder.Term().readonly(true),
                    uCcsOrder.CouponId().readonly(true),
                    uCcsOrder.OffsetType().readonly(true)
			);
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
		grid.loadDataFromUrl("rpc/orderInfoServer/getOrderList");
	}
	
}
