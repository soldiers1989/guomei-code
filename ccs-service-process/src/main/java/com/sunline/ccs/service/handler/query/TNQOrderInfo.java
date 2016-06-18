package com.sunline.ccs.service.handler.query;



import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.param.def.enums.PaymentPurpose;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.msdentity.STNQOrderInfoReq;
import com.sunline.ccs.service.msdentity.STNQOrderInfoResp;
import com.sunline.ccs.service.msdentity.STNQOrderInfoRespInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.util.DateFormatUtil;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 订单查询
 * @author zhengjf
 *
 */
@Service
public class TNQOrderInfo {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	@PersistenceContext
	private EntityManager em;
	@Autowired
    AppapiCommService appapiCommService;
	
	QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
	QCcsOrderHst qCcsOrderHst = QCcsOrderHst.ccsOrderHst;
	
	
	public STNQOrderInfoResp handler(STNQOrderInfoReq req){
 		LogTools.printLogger(log, "STNQOrderInfoReq", "订单查询", req, true);
		LogTools.printObj(log, req, "请求参数STNQOrderInfoReq");
	
		TxnInfo txnInfo = new TxnInfo();
		
		//报文检查
		if (req.getPagesize()<1 || req.getPageposition()<1 ) {
			throw new ProcessException(MsRespCode.E_1043.getCode(),"页数或者每页条数最少为1");
		}
		if(req.getPagesize()>50){ 
			throw new ProcessException(MsRespCode.E_1043.getCode(),"请求单页记录条数最大为50条");
		}
		
		STNQOrderInfoResp resp=new STNQOrderInfoResp();
		//定义集合
		List<CcsOrder> order = new ArrayList<CcsOrder>();
		List<CcsOrderHst> orderHst = new ArrayList<CcsOrderHst>();
		List<STNQOrderInfoRespInfo> txnlist=new ArrayList<STNQOrderInfoRespInfo>();
		try{
			
			//获取页数及单页个数
			int size=req.getPagesize();
			int position=req.getPageposition();
			
			//获取公共条件
			BooleanExpression OrderHstCondition= getOrderHstCondition(req);
			BooleanExpression OrderCondition=getOrderCondition(req);
			
			//获取order跟orderHst表中符合的总条数
			long ordernum = new JPAQuery(em).from(qCcsOrder).where(OrderCondition).count();
			long orderHstnum = new JPAQuery(em).from(qCcsOrderHst).where(OrderHstCondition).count();
			
			//如果页数乘以页面个数大于等于order表符合的数据，则从order表里面取
			if(size*(position-1)>=ordernum){
				orderHst = getOrderHst(OrderHstCondition,size*(position-1)-ordernum,req.getPagesize());
			} else {
				//如果页数+1乘以页面个数小于等于order表符合的数据，则从orderHst表里面取
				order = getOreder(OrderCondition, size*(position-1),req.getPagesize());
				if (order.size() < req.getPagesize()) {
					orderHst = getOrderHst(OrderHstCondition,0,req.getPagesize()-order.size());
				}
			}	
			//对于txnlist赋值 如果order不为null
			if (order.size() > 0) {
				for (CcsOrder odr : order){
					STNQOrderInfoRespInfo ofri=new STNQOrderInfoRespInfo();
					ofri.setAmt(odr.getTxnAmt());
					ofri.setStatus(odr.getOrderStatus().toString());
					ofri.setMessage(odr.getMessage());
					ofri.setTime(DateFormatUtil.format(odr.getOrderTime(), "yyyyMMddHHmmss"));
					ofri.setBankName(odr.getOpenBank());
					ofri.setBankCardNbr(odr.getCardNo());
					ofri.setPurpose(paymentMethod(odr.getLoanUsage(),odr.getOnlineFlag(),odr.getSubTerminalType()));
					txnlist.add(ofri);
				}
			}
			//如果orderHst不为空而且order并没有取满
			if (order.size() < req.getPagesize() && orderHst.size() > 0) {
				for (CcsOrderHst odr : orderHst) {
					STNQOrderInfoRespInfo ofri=new STNQOrderInfoRespInfo();
					ofri.setAmt(odr.getTxnAmt());
					ofri.setStatus(odr.getOrderStatus().toString());
					ofri.setMessage(odr.getMessage());
					ofri.setTime(DateFormatUtil.format(odr.getOrderTime(), "yyyyMMddHHmmss"));
					ofri.setBankName(odr.getOpenBank());
					ofri.setBankCardNbr(odr.getCardNo());
					ofri.setPurpose(paymentMethod(odr.getLoanUsage(),odr.getOnlineFlag(),odr.getSubTerminalType()));
					txnlist.add(ofri);
				}
			}
			resp.setTxncount(ordernum+orderHstnum);
			resp.setContractNo(req.getContractNo());
			resp.setPagesize(req.getPagesize());
			resp.setTxnlist(txnlist);
			log.debug("订单查询返回报文:" + resp);
		}catch(ProcessException pe){
			if(log.isErrorEnabled())
				log.error(pe.getMessage(),pe);
			appapiCommService.preException(pe, pe, txnInfo);
		}
		catch(Exception e){
			if(log.isErrorEnabled())
				log.error(e.getMessage(),e);
			appapiCommService.preException(e, null, txnInfo);				
		}finally{
			LogTools.printLogger(log, "STNQAAcctsbyCustUUIDResp", "合同列表查询", resp, false);
		}
		setResponse(resp,  txnInfo);
		
		return resp;
	}
	/**
	 * 根据查询条件获取出Order表中数据
	 * @param req
	 * @return
	 */
	private List<CcsOrder> getOreder(BooleanExpression booleanExpression,long key,long number){
	
		JPAQuery query = new JPAQuery(em);

		//返回过滤后的集合
		return query.from(qCcsOrder).where(booleanExpression).orderBy(qCcsOrder.orderTime.desc()).offset(key).limit(number).list(qCcsOrder);
		
	}
	/**
	 * 根据查询条件获取出OrderHst表中数据
	 * @param req number
	 * @return
	 */
	private List<CcsOrderHst> getOrderHst(BooleanExpression booleanExpression,long key,long number) {
	
		JPAQuery query = new JPAQuery(em);

		//返回过滤后的集合
		return query.from(qCcsOrderHst).where(booleanExpression).orderBy(qCcsOrderHst.orderTime.desc()).offset(key).limit(number).list(qCcsOrderHst);		
	}
	/**
	 * 支付方式赋值关系
	 *	如果ONLINE_FLAG 为 否（批量交易）
	 *	如果LOANUSAGE 是 逾期扣款  则 返回 “W|拆分代扣（CPD代扣）”
	 *	其他情况返回“Z|正常代扣”
	 *	如果ONLINE_FLAG 为 是（联机交易）
	 *	如果 终端类型SubTerminalType 是 CLS-催收，则返回“Y|催收实时扣款”
	 *	其他情况返回“主动还款”
	 *  获取支付方式
	 * 
	 * @param LoanUsage Indicator String
	 * @return
	 */
	private String paymentMethod(LoanUsage loanUsage,Indicator onlineFlag,String subTerminalType){
		
		if(LoanUsage.L.equals(loanUsage)){
			return PaymentPurpose.L.toString();
		}
		if(LoanUsage.A.equals(loanUsage)){
			return PaymentPurpose.A.toString();
		}
		//判断是否为联机
		if(Indicator.N.equals(onlineFlag)){
			if(LoanUsage.O.equals(loanUsage)){
				return PaymentPurpose.W.toString();
			}else{
				return PaymentPurpose.Z.toString();
			}
		}else{
			if("CLS".equals(subTerminalType)){
				return PaymentPurpose.Y.toString();
			}else{
				return PaymentPurpose.X.toString();
			}
		}
	}
	/**
	 * 获取符合order的总条件
	 * @param req
	 * @return
	 */
	public BooleanExpression getOrderCondition(STNQOrderInfoReq req) {
		BooleanExpression booleanExpression = qCcsOrder.contrNbr.eq(req.getContractNo()).and(qCcsOrder.orderStatus.notIn(OrderStatus.G,OrderStatus.D))
				.and(qCcsOrder.loanUsage.in(LoanUsage.M,LoanUsage.N,LoanUsage.O,LoanUsage.L,LoanUsage.A));
	
		// 如果请求中存在起止时间则加上时间条件
		if(null!=req.getStarttime()){
			booleanExpression = booleanExpression.and(qCcsOrder.orderTime.goe(req.getStarttime()));
		}
		
		if(null!=req.getEndtime()){
			booleanExpression = booleanExpression.and(qCcsOrder.orderTime.loe(req.getEndtime()));
		}
		return booleanExpression;
	}
	
	
	
	/**
	 * 获取符合orderHst的总条件
	 * @param req
	 * @return
	 */
	private BooleanExpression getOrderHstCondition(STNQOrderInfoReq req){
		BooleanExpression booleanExpression = qCcsOrderHst.contrNbr.eq(req.getContractNo()).and(qCcsOrderHst.orderStatus.notIn(OrderStatus.G,OrderStatus.D))
				.and(qCcsOrderHst.loanUsage.in(LoanUsage.M,LoanUsage.N,LoanUsage.O,LoanUsage.L,LoanUsage.A));
	
		// 如果请求中存在起止时间则加上时间条件
		if(null!=req.getStarttime()){
			booleanExpression = booleanExpression.and(qCcsOrderHst.orderTime.goe(req.getStarttime()));
		}
		
		if(null!=req.getEndtime()){
			booleanExpression = booleanExpression.and(qCcsOrderHst.orderTime.loe(req.getEndtime()));
		}
		return booleanExpression;
	
	}
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(MsResponseInfo resp, TxnInfo txnInfo) {
		
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
}
