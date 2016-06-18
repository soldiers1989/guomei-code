package com.sunline.ccs.batch.cc1400;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MatchErrorReason;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exchange.MsxfMerchantTranFlow;
import com.sunline.ppy.dictionary.report.ccs.MsxfMerchantMatchErrRpt;
import com.sunline.ppy.dictionary.report.ccs.MsxfMerchantMatchSuccRpt;

public class P1406MerchantOrder implements ItemProcessor<LineItem<MsxfMerchantTranFlow>, S1406RptInfo > {
	@PersistenceContext
	private EntityManager em;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private OrderFacility orderFacility;
	@Override
	public S1406RptInfo process(LineItem<MsxfMerchantTranFlow> items) throws Exception {
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		QCcsOrderHst qCcsOrderHst = QCcsOrderHst.ccsOrderHst;
		MsxfMerchantTranFlow item = items.getLineObject();
		S1406RptInfo info = new S1406RptInfo();

		if(log.isDebugEnabled()){
			log.debug("商户流水对账开始...");
		}
		
		//获取币种金额精度--支持币种列表[人民币CNY]
		//直接取人民币精度--2位
		//现金支付金额小数点左移
		BigDecimal txnAmt = new BigDecimal(item.cashFee).movePointLeft(2);
		//商户渠道
		//要素:流水号
		CcsOrder order = null;
		//默认正向交易
		if(item.orderStatus.equals("pay")){
			order = new JPAQuery(em).from(qCcsOrder).where(qCcsOrder.merchandiseOrder.eq(item.outTraceNo)
					//渠道：商户平台
					.and(qCcsOrder.contactChnl.eq(Constants.MERCHANT_ACQ_ID))
					//需要对账
					.and(qCcsOrder.matchInd.eq(Indicator.Y))
					//未对账的
					.and(qCcsOrder.comparedInd.isNull().or(qCcsOrder.comparedInd.eq(Indicator.N))))
					.singleResult(qCcsOrder);
		}else{
			//反向交易寻找原交易
			Long oriOrderId = null;
			CcsOrder oriOrder = new JPAQuery(em).from(qCcsOrder).where(qCcsOrder.merchandiseOrder.eq(item.outTraceNo))
					.singleResult(qCcsOrder);
			if(oriOrder!=null){
				oriOrderId = oriOrder.getOrderId();
			}else{
				CcsOrderHst oriOrderHst = new JPAQuery(em).from(qCcsOrderHst).where(qCcsOrderHst.merchandiseOrder.eq(item.outTraceNo))
							.singleResult(qCcsOrderHst);
				if(oriOrderHst!=null){
					oriOrderId = oriOrderHst.getOrderId();
				}
			}
			if(oriOrderId!=null){
				order = new JPAQuery(em).from(qCcsOrder).where(qCcsOrder.oriOrderId.eq(oriOrderId)
					.and(qCcsOrder.loanUsage.in(LoanUsage.V,LoanUsage.R))
					//需要对账
					.and(qCcsOrder.matchInd.eq(Indicator.Y))
					//渠道:商户平台
					.and(qCcsOrder.contactChnl.eq(Constants.MERCHANT_ACQ_ID))
					//未对账的
					.and(qCcsOrder.comparedInd.isNull().or(qCcsOrder.comparedInd.eq(Indicator.N))))
					.singleResult(qCcsOrder);
			}
		}
		if(order==null){
			log.debug("匹配不到，订单号["+item.outTraceNo+"]");
			info.setMsxfMerchantMatchErrRpt(setErrRpt(item,null,MatchErrorReason.R01));
		}else{
			log.debug("匹配到订单,orderId["+order.getOrderId()+"],交易方向["+item.orderStatus+"]");
			OrganizationContextHolder.setCurrentOrg(order.getOrg());
			//订单交易方向--正向/撤销/退货
			String orderTxnType = orderFacility.getServiceIdMapping(order.getServiceId());
			//对账文件成功--金额正确
			if(order.getTxnAmt().compareTo(txnAmt)==0
					//--系统结果成功/原交易被撤销
					&&EnumUtils.in(order.getOrderStatus(), OrderStatus.S,OrderStatus.B)
					//--交易方向匹配成功
					&&item.orderStatus.equals(orderTxnType)){
				log.debug("对账成功");
				order.setErrInd(Indicator.N);
				info.setMsxfMerchantMatchSuccRpt(setSuccRpt(order,item));
				order.setComparedInd(Indicator.Y);
			//要素不匹配
			}else{
				order.setErrInd(Indicator.Y);
				order.setComparedInd(Indicator.Y);
				if(EnumUtils.in(order.getOrderStatus(),OrderStatus.E,OrderStatus.W)){
					log.debug("对账结果系统订单状态异常");
				}
				if(!item.orderStatus.equals(orderTxnType)){
					log.debug("对账结果状态不匹配,内部交易方向不匹配");
				}
				if(order.getTxnAmt().compareTo(txnAmt)!=0){
					log.debug("对账结果不匹配,金额不匹配");
				}
				info.setMsxfMerchantMatchErrRpt(setErrRpt(item,order.getMerchandiseOrder(),MatchErrorReason.R03));
				if(!"pay".equals(orderTxnType)){
					log.debug("反向交易,寻找原交易...");
					CcsOrder oriOrder = null;
					CcsOrderHst oriOrderHst = null;
					oriOrder = new JPAQuery(em).from(qCcsOrder).where(qCcsOrder.orderId.eq(order.getOriOrderId())).singleResult(qCcsOrder);
					if(oriOrder==null){
						oriOrderHst = new JPAQuery(em).from(qCcsOrderHst).where(qCcsOrderHst.orderId.eq(order.getOriOrderId())).singleResult(qCcsOrderHst);
					}
					//根据原交易->原交易历史->当前交易的优先级取商户订单号
					String merchantDiseOrder = oriOrder==null?(oriOrderHst==null?order.getMerchandiseOrder():oriOrderHst.getMerchandiseOrder()):oriOrder.getMerchandiseOrder();
					info.setMsxfMerchantMatchErrRpt(setErrRpt(item,merchantDiseOrder,MatchErrorReason.R03));
				}
			}
			em.merge(order);
		}
		return info;
	}

	//设置差错报表--若存在订单则使用订单信息，否则使用文件信息
	private MsxfMerchantMatchErrRpt setErrRpt(MsxfMerchantTranFlow item, String merchantDiseOrder,MatchErrorReason reason) {
		MsxfMerchantMatchErrRpt errRpt = new MsxfMerchantMatchErrRpt();
		errRpt.outTraceNo = merchantDiseOrder==null?item.outTraceNo:merchantDiseOrder;
		errRpt.cashFee = item.cashFee;
		errRpt.deviceInfo = item.deviceInfo;
		errRpt.feeType = item.feeType;
		errRpt.mchId = item.mchId;
		errRpt.orderStatus = item.orderStatus;
		errRpt.outRefundNo = item.outRefundNo;
		errRpt.startTime = item.startTime;
		errRpt.totalFee = item.totalFee;
		errRpt.matchErrorReason = reason;
		return errRpt;
	}
	
	//设置成功报表
	private MsxfMerchantMatchSuccRpt setSuccRpt(CcsOrder order,MsxfMerchantTranFlow item){
		MsxfMerchantMatchSuccRpt rpt = new MsxfMerchantMatchSuccRpt();
		rpt.cashFee = new Integer(order.getTxnAmt().toString().replace(".",""));
		rpt.deviceInfo = item.deviceInfo;
		rpt.feeType = item.feeType;
		rpt.mchId = item.mchId;
		rpt.orderStatus = orderFacility.getServiceIdMapping(order.getServiceId());
		rpt.outRefundNo = item.outRefundNo;
		rpt.outTraceNo = order.getMerchandiseOrder()==null?item.outTraceNo:order.getMerchandiseOrder();
		rpt.startTime = order.getOrderTime();
		rpt.totalFee = item.totalFee;
		return rpt;
	}
}
