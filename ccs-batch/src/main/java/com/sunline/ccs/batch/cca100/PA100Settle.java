package com.sunline.ccs.batch.cca100;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.front.FrontBatchUtil;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleLoanHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 金融机构结算
 * @author zhangqiang
 *
 */
public class PA100Settle implements ItemProcessor<FinancialOrg, FinancialOrg> {
	
	private static final Logger logger = LoggerFactory.getLogger(PA100Settle.class);
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private FrontBatchUtil frontBatchUtil;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TxnUtils txnUtils;
	
//	@Autowired
//	private PaymentFacility paymentFacility;
//	
//	@Resource(name = "bankService")
//	private BankServiceForAps bankServiceForApsImpl;
//	
//	@Autowired
//	private OrderFacility orderFacility;
//	
//	@Autowired
//	private ServJsonUtil servJsonUtil;
//	
	
	@Override
	public FinancialOrg process(FinancialOrg financialOrg) throws Exception {
		
		if(logger.isDebugEnabled())
			logger.debug("开始结算financialOrgNO:[{}]", financialOrg.financialOrgNO);
		
		Calendar c = Calendar.getInstance();
		c.setTime(batchStatusFacility.getBatchDate());
		// 不参与结算 || 非结算日 直接返回不结算
		if(Indicator.N == financialOrg.isSettle 
				|| c.get(Calendar.DAY_OF_MONTH) != financialOrg.monthSettleDay){
			return financialOrg;
		}
		// 清算起始日期为上月清算起始日
		Date settleStartDate = DateUtils.setDays(DateUtils.addMonths(batchStatusFacility.getBatchDate(), -1), financialOrg.settleStartDay);
		settleStartDate = DateUtils.truncate(settleStartDate, Calendar.DATE);
		// 清算截止日期为本月清算截止日
		Date settleEndDate = DateUtils.setDays(batchStatusFacility.getBatchDate(), financialOrg.settleEndDay);
		settleEndDate = DateUtils.truncate(settleEndDate, Calendar.DATE);
		
		QCcsRepayHst qrepayHst = QCcsRepayHst.ccsRepayHst;
		// 结算保费:金融机构 && 余额成分 && 起止日期
		BigDecimal repayInsuranceFee = new JPAQuery(em).from(qrepayHst).where(qrepayHst.acqId.eq(financialOrg.acqAcceptorId)
				.and(qrepayHst.bnpType.in(BucketObject.ctdIns, BucketObject.pastIns))
				.and(qrepayHst.batchDate.goe(settleStartDate))
				.and(qrepayHst.batchDate.lt(settleEndDate)))
				.singleResult(qrepayHst.repayAmt.sum());
		repayInsuranceFee = repayInsuranceFee==null ? BigDecimal.ZERO : repayInsuranceFee;
		
		SysTxnCdMapping sysTxnCdMapping = parameterFacility.loadParameter(SysTxnCd.S75, SysTxnCdMapping.class);
		TxnCd txnCd = parameterFacility.loadParameter(sysTxnCdMapping.txnCd, TxnCd.class);
		QCcsTxnHst qTxnHst = QCcsTxnHst.ccsTxnHst;
		BigDecimal DInsuranceFee = new JPAQuery(em).from(qTxnHst).where(qTxnHst.acqAcceptorId.eq(financialOrg.acqAcceptorId)
				.and(qTxnHst.txnCode.eq(txnCd.txnCd))
				.and(qTxnHst.postDate.goe(settleStartDate))
				.and(qTxnHst.postDate.lt(settleEndDate)))
				.singleResult(qTxnHst.postAmt.sum());
		DInsuranceFee = DInsuranceFee==null ? BigDecimal.ZERO : DInsuranceFee;
		repayInsuranceFee = repayInsuranceFee.subtract(DInsuranceFee);
		if(repayInsuranceFee.compareTo(BigDecimal.ZERO)<=0){
			repayInsuranceFee = BigDecimal.ZERO;
		}
		
		// 结算提前结清手续费:(金融机构 && 余额成分 && 起止日期)*分成比例
		BigDecimal repayTxnFee = new JPAQuery(em).from(qrepayHst).where(qrepayHst.acqId.eq(financialOrg.acqAcceptorId)
				.and(qrepayHst.bnpType.in(BucketObject.ctdTxnFee, BucketObject.pastTxnFee))
				.and(qrepayHst.batchDate.goe(settleStartDate))
				.and(qrepayHst.batchDate.lt(settleEndDate)))
				.singleResult(qrepayHst.repayAmt.sum());
		repayTxnFee = repayTxnFee==null ? BigDecimal.ZERO : repayTxnFee;
		repayTxnFee = repayTxnFee.multiply(financialOrg.adFeeScale).setScale(2, RoundingMode.HALF_UP);
		QCcsOrderHst qorderHst = QCcsOrderHst.ccsOrderHst;
		// 联机结算追偿费: 金融机构 && 用途 && 起止日期 && 订单已完成 &&原订单号为空
		BigDecimal repaySubrogationAmt1 = new JPAQuery(em).from(qorderHst).where(qorderHst.acqId.eq(financialOrg.acqAcceptorId)
				.and(qorderHst.loanUsage.eq(LoanUsage.S))
				.and(qorderHst.optDatetime.goe(settleStartDate))
				.and(qorderHst.optDatetime.lt(settleEndDate))
				.and(qorderHst.orderStatus.eq(OrderStatus.S))
				.and(qorderHst.oriOrderId.isNull()))
				.singleResult(qorderHst.txnAmt.sum());
		repaySubrogationAmt1 = repaySubrogationAmt1==null ? BigDecimal.ZERO : repaySubrogationAmt1;
		// 批量拆分结算追偿费: 金融机构 && 用途 && 起止日期 && 订单拆分已完成 
		BigDecimal repaySubrogationAmt2 = new JPAQuery(em).from(qorderHst).where(qorderHst.acqId.eq(financialOrg.acqAcceptorId)
				.and(qorderHst.loanUsage.eq(LoanUsage.S))
				.and(qorderHst.optDatetime.goe(settleStartDate))
				.and(qorderHst.optDatetime.lt(settleEndDate))
				.and(qorderHst.orderStatus.eq(OrderStatus.D)))
				.singleResult(qorderHst.successAmt.sum());
		repaySubrogationAmt2 = repaySubrogationAmt2==null ? BigDecimal.ZERO : repaySubrogationAmt2;
		
		// 总订单交易金额
		BigDecimal txnAmt = repayInsuranceFee.add(repayTxnFee).add(repaySubrogationAmt1).add(repaySubrogationAmt2);
		if(txnAmt.compareTo(BigDecimal.ZERO) == 0)
			return financialOrg;
		// 保存订单
		CcsOrder order = frontBatchUtil.initOrder(null, null, null, LoanUsage.B, txnAmt, financialOrg);
		order.setOrg(OrganizationContextHolder.getCurrentOrg());
		order.setPayChannelId("1");
		order.setPayBizCode("1");
		order.setPurpose("结算");
		String dateStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		order.setRequestTime(dateStr);
//		order.setServicesn("BT"+dateStr+String.format("%06d", new Random().nextInt(999999)));
		order.setServicesn(txnUtils.genRefnbr(batchStatusFacility.getBatchDate(), null));
		order.setRefNbr(txnUtils.genRefnbr(batchStatusFacility.getBatchDate(), null));
		order.setSubTerminalType(AuthTransTerminal.T00.toString());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		order.setMemo("结算:" + formatter.format(settleStartDate) + "-" + formatter.format(settleEndDate) + ",保费:" + repayInsuranceFee.toString() + ",手续费:" + repayTxnFee.toString() + ",追偿费:" + repaySubrogationAmt1.add(repaySubrogationAmt2).toString());
		order.setOrderStatus(OrderStatus.P);
		// 保存结算历史信息
		mergeSettleLoanHst(order, financialOrg, repayInsuranceFee, repayTxnFee, repaySubrogationAmt1.add(repaySubrogationAmt2));
//		try {
//			// 组装支付指令
//			String json = paymentFacility.installPaySinPaymentCommand(order);
//			// 发送支付指令
//			String retJson = bankServiceForApsImpl.sendMsPayFront(json, order.getLoanUsage());
//			
//			if(logger.isDebugEnabled())
//				logger.debug("retJson:[{}]",retJson);
//			// 处理结果
//			payResultProc(order, retJson);
//			// 结算成功更新历史表
//			settleLoanHst.setIsSettle(Indicator.Y);
//			settleLoanHst.setSettleSuccessDate(batchStatusFacility.getBatchDate());
//		} catch (Exception e) {
//			// 只要发生异常即按处理中处理, 等待页面查询重提
//			logger.error("非成功结算交易,修正为处理中:"+e.getMessage(), e);
//			order.setOrderStatus(OrderStatus.W);
//		}
		
		return financialOrg;
	}


	private CcsSettleLoanHst mergeSettleLoanHst(CcsOrder order, FinancialOrg financialOrg, BigDecimal repayInsuranceFee, BigDecimal repayTxnFee, BigDecimal repaySubrogationAmt) {
		
		CcsSettleLoanHst hst = new CcsSettleLoanHst();
		hst.setOrderId(order.getOrderId());
		hst.setSettleAmt(repayInsuranceFee.add(repayTxnFee).add(repaySubrogationAmt)); // 结算
		hst.setInsuranceAmt(repayInsuranceFee); // 保费
		hst.setPenaltyAmt(repayTxnFee); // 违约金
		hst.setCompensatoryAmt(repaySubrogationAmt); // 追偿
		hst.setBeginDate(DateUtils.setDays(DateUtils.addMonths(batchStatusFacility.getBatchDate(), -1), financialOrg.settleStartDay));
		hst.setEndDate(DateUtils.setDays(batchStatusFacility.getBatchDate(), financialOrg.settleEndDay));
		hst.setSettleDate(batchStatusFacility.getBatchDate());
		hst.setFinancialOrgNo(financialOrg.financialOrgNO);
		hst.setIsSettle(Indicator.N);
		em.persist(hst);
		
		return hst;
	}


	/**
	 * 处理支付结果
	 * @param order
	 * @param retJson
	 */
//	private void payResultProc(CcsOrder order, String retJson) {
//		// 将返回json转为实体
//		CommResp mainResp = JsonSerializeUtil.jsonReSerializerNoType(retJson, CommResp.class);
//		MsPayfrontError msPayfrontError = null;
//		// 获取报文头code响应码  
//		if(logger.isDebugEnabled())
//			logger.debug("报文头响应码:[{}],报文头响应描述:[{}]",mainResp.getCode(),mainResp.getMessage());
//		msPayfrontError = getErrorEnum(mainResp.getCode(), MsPayfrontError.class);
//		// 单笔代付报文体
//		PayCutPaymentResp data = new PayCutPaymentResp();
//		
//		
//		if(msPayfrontError == MsPayfrontError.S_0){//放款成功
//			servJsonUtil.setBeanProperty(mainResp, data);
//			if(logger.isDebugEnabled())
//				logger.debug("报文体响应码:[{}],报文体响应描述:[{}]",data.getErrorCode(),data.getErrorMessage());
//			//获取报文体errorCode响应码
//			if(msPayfrontError == MsPayfrontError.S_0){
//				msPayfrontError = getErrorEnum(data.getErrorCode(), MsPayfrontError.class);
//				orderFacility.updateOrder(order, mainResp, data.getStatus(),OrderStatus.S, null, batchStatusFacility.getSystemStatus().getBusinessDate());
//			}else {
//				logger.error(mainResp.getCode(), mainResp.getMessage());
//				orderFacility.updateOrder(order, mainResp, null,OrderStatus.W, null, batchStatusFacility.getSystemStatus().getBusinessDate());
//				throw new ProcessException(msPayfrontError.getCode(),msPayfrontError.getDesc());
//			}
//		}else {
//			logger.error(mainResp.getCode(), mainResp.getMessage());
//			orderFacility.updateOrder(order, mainResp, null,OrderStatus.W, null, batchStatusFacility.getSystemStatus().getBusinessDate());
//			throw new ProcessException(msPayfrontError.getCode(),msPayfrontError.getDesc());
//		}
//		
//		
//	}
//	
//	/**
//	 * 获取异常枚举
//	 * @param value
//	 * @param clazz
//	 * @return
//	 */
//	public <T extends Enum<T>> T getErrorEnum(String value, Class<T> clazz) {
//		try{
//			String tip = "S_";
//			if(!StringUtils.equals("0", value)){
//				tip = "E_";
//			}
//			
//			return Enum.valueOf(clazz, tip + value);
//		}catch(Exception e){
//			if (logger.isErrorEnabled())
//				logger.error("无效响应码[{"+value+"}]", e);
//
//			throw new ProcessException(MsPayfrontError.E_90000.getCode(),MsPayfrontError.E_90000.getDesc());
//		}
//	}

}
