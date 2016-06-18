package com.sunline.ccs.facility.order;

import java.math.BigDecimal;
import java.util.Date;
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
import com.sunline.ccs.infrastructure.server.repos.RCcsOrder;
import com.sunline.ccs.infrastructure.server.repos.RCcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.payEntity.CommResp;
import com.sunline.pcm.param.def.FinancialOrg;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.CommandType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.OrderStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @author wangz
 *
 */
@Service
public class OrderFacility {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RCcsOrder rCcsOrder;
	@Autowired
	private RCcsOrderHst rCcsOrderHst;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 查询订单列表
	 * @param dueBillNo 借据号
	 * @param orderStatus 订单状态
	 * @param transType 交易类型
	 * @return
	 */
	public CcsOrder findById(Long orderId, boolean nullException){
		
		if(nullException && null == orderId){
			throw new ProcessException();
		}else if(!nullException && null == orderId){
			return null;
		}
		
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		CcsOrder ccsOrderList = rCcsOrder.findOne(qCcsOrder.orderId.eq(orderId));//借据号
		
		if(nullException && null == ccsOrderList){
			throw new ProcessException();
		}
		
		return ccsOrderList;
	}
	
	/**
	 * 查询订单列表
	 * @param dueBillNo 借据号
	 * @param orderStatus 订单状态
	 * @param transType 交易类型
	 * @param acctType
	 * @param acctNo 
	 * @return
	 */
	public Iterable<CcsOrder> findByDueBillNo(String dueBillNo, OrderStatus orderStatus,AuthTransType transType, Long acctNo, AccountType acctType){
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		Iterable<CcsOrder> ccsOrderList = rCcsOrder.findAll(qCcsOrder.dueBillNo.eq(dueBillNo)//借据号
				.and(qCcsOrder.orderStatus.eq(orderStatus))//订单状态
				.and(qCcsOrder.txnType.eq(transType))//交易类型
				.and(qCcsOrder.acctNbr.eq(acctNo))//账号
				.and(qCcsOrder.acctType.eq(acctType)));//账户类型
		return ccsOrderList;
	}
	
	/**
	 * 累计某交易类型 某状态的订单
	 * @param dueBillNo 借据号
	 * @param orderStatus 订单状态
	 * @param transType 交易类型
	 * @return
	 */
	public Long countOrder(String dueBillNo, OrderStatus orderStatus,AuthTransType transType){
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		Long orderCount = rCcsOrder.count(qCcsOrder.dueBillNo.eq(dueBillNo).//借据号
				and(qCcsOrder.orderStatus.eq(orderStatus)).//订单状态
				and(qCcsOrder.txnType.eq(transType)));//交易类型
		return orderCount;
	}
	
	/**
	 * 累计某交易类型 某状态的订单
	 * @param dueBillNo 借据号
	 * @param orderStatus 订单状态
	 * @param loanUsage 订单用途
	 * @return
	 */
	public Long countOrderByUsage(String contrNo, String dueBillNo, OrderStatus orderStatus,LoanUsage loanUsage){
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		Long orderCount = rCcsOrder.count(qCcsOrder.dueBillNo.eq(dueBillNo).//借据号
				and(qCcsOrder.contrNbr.eq(contrNo)).//合同号
				and(qCcsOrder.orderStatus.eq(orderStatus)).//订单状态
				and(qCcsOrder.loanUsage.eq(loanUsage)));//订单用途
		return orderCount;
	}
	
	/**
	 * 累计某交易类型 某状态的订单
	 * @param acctNbr账号
	 * @param acctType账户类型
	 * @param orderStatus订单状态
	 * @param transType交易类型
	 * @return
	 */
	public Long countOrderByAcct(Long acctNbr,AccountType acctType, OrderStatus orderStatus,AuthTransType transType){
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		Long orderCount = rCcsOrder.count(qCcsOrder.acctNbr.eq(acctNbr)//账号
				.and(qCcsOrder.acctType.eq(acctType))//账户类型
				.and(qCcsOrder.orderStatus.eq(orderStatus))//订单状态
				.and(qCcsOrder.txnType.eq(transType)));//交易类型
		return orderCount;
	}
	
	/**
	 * 累计某交易类型 某状态的订单
	 * @param serviceSn 流水号
	 * @param requestTime 请求时间
	 * @param acqId 收单机构
	 * @return
	 */
	public void valiRepeatOrder(String serviceSn, String acqId){
		try{
			CcsOrder order = this.findOrderBySersn(serviceSn, acqId);
			if (null != order) {
				throw new ProcessException(MsRespCode.E_1012.getCode(), MsRespCode.E_1012.getMessage());
			}
		}catch(ProcessException pe){
			logger.error(pe.getMessage(), pe);
			throw pe;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_9998.getCode(), MsRespCode.E_9998.getMessage());
		}
	}
	
	/**
	 * 根据流水号及受理机构号查询订单
	 * @param serviceSn
	 * @param acqId
	 * @return
	 */
	public CcsOrder findOrderBySersn(String serviceSn, String acqId){
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		CcsOrder order = rCcsOrder.findOne(qCcsOrder.servicesn.eq(serviceSn)//外部流水 11 37
				.and(qCcsOrder.acqId.eq(acqId)));//受理机构号 32域
		
		return order;
	}
	
	/**
	* 新建并保存订单
	 * @param acct 账户
	 * @param cust 客户
	 * @param loan 借据
	 * @param loanUsage 贷款用途
	 * @param txnAmt 交易金额
	 * @param bizDate 交易日期
	 * @param ChannelId 接入渠道
	 * @param onlineFlag 联机标识
	 * @param acqId 收单机构 32
	 * @param terminal 终端
	 * @param onLine 是否联机
	 * @param reqTime 请求时间
	 * @param serviceSn 外部流水
	 * @param guarantyId 保单号
	 * @param dueBillNo 借据号
	 * @param origOrderId 原订单号
	 * @param contrNbr 合同号
	 * @return
	 */
	public CcsOrder installOrder(CcsAcct acct, CcsCustomer cust,
			LoanUsage loanUsage, BigDecimal txnAmt,Date bizDate,
			InputSource channelId,Indicator onlineFlag,String acqId,
			String terminal,Indicator onLine,String reqTime, 
			String serviceSn,String refNbr,String guarantyId,String dueBillNo,
			Long origOrderId, String contrNbr,String mobile,String serviceId,
			String merId,String authTxnTerminal){
		CcsOrder order = new CcsOrder();
		Date currDate = new Date();
		
		
		setByDefault(order);
		order.setMerId(merId);
		order.setTerminalDevice(authTxnTerminal);
		
		setByAcctInfo(acct, order);
		setByCustInfo(cust, order);
		setByLoanUsage(loanUsage, order);
		
		order.setGuarantyId(guarantyId);
		order.setDueBillNo(dueBillNo);

		order.setComparedInd(Indicator.N);
		order.setSubTerminalType(terminal);
		order.setRequestTime(reqTime);
		order.setServicesn(serviceSn);
		order.setRefNbr(refNbr);
		order.setOnlineFlag(onLine);
		order.setChannelId(channelId); 
		order.setAcqId(acqId);
//		order.setContactChnl(AcqContactChnl.getContactChnl(acqId));
		order.setOrderStatus(OrderStatus.C);//已提交
		order.setTxnAmt(txnAmt == null?BigDecimal.ZERO:txnAmt);
		order.setCardType("0");//0-卡
		order.setLoanUsage(loanUsage);// 用途
		order.setBusinessDate(bizDate);
		order.setSendTime(currDate);
		order.setSetupDate(bizDate);
		order.setOptDatetime(bizDate);
		order.setOrderTime(currDate);
		order.setJpaVersion(0);
		order.setPayChannelId("1");//支付接口渠道标识 主业务：0长亮核算：1
		order.setFlag("00"); // 对私00 对公01
		order.setSuccessAmt(BigDecimal.valueOf(0.0));
		order.setFailureAmt(BigDecimal.valueOf(0.0));
		order.setOriOrderId(origOrderId);
		order.setContrNbr(contrNbr);//合同号
		order.setMobileNumber(mobile);//交易中的手机号
		order.setServiceId(serviceId);
		//设置对账渠道
		ContactChnlFacility.setContactChnl(serviceId, acqId, order);
		//设置指定交易强制对账
		ContactChnlFacility.setTransFlowMatchChnl(serviceId, acqId, order);
		if(LoanUsage.C == loanUsage) {
			order.setFlag("01");
			ProductCredit pc = unifiedParameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
			FinancialOrg finanicalOrg = unifiedParameterFacility.loadParameter(pc.financeOrgNo, FinancialOrg.class);
			order.setUsrName(finanicalOrg.financialOrgName);
			order.setCardNo(finanicalOrg.financialOrgPayNo);
		}
		
		return rCcsOrder.save(order);
	}
	
	public static void main(String[] args) {
		OrderFacility facility = new OrderFacility();
		CcsOrder order = new CcsOrder();
		order.setLoanUsage(LoanUsage.L);
		
		facility.setByLoanUsage(order.getLoanUsage(), order);
		
		ContactChnlFacility.setContactChnl("TFDCommodyLoanWithDraw", "20000005", order );
		ContactChnlFacility.setTransFlowMatchChnl("TFDCommodyLoanWithDraw", "20000005", order );
		System.out.println("对账渠道"+order.getContactChnl()+ " " + order.getMatchInd() );
		
	}


	/**
	 * 根据贷款用途填充订单
	 * @param loanUsage
	 * @param order
	 */
	private void setByLoanUsage(LoanUsage loanUsage, CcsOrder order) {
		switch (loanUsage) {
		case A://放款重提
			order.setCommandType(CommandType.SPA);
			order.setTxnType(AuthTransType.AgentDebit);
			order.setPurpose("放款重提"); // 接口预留
			order.setPayBizCode("1");//支付接口渠道业务 实时或批量代扣：0 实时或批量代付：1
			order.setMatchInd(Indicator.Y);
			break;
		case L://放款申请
			order.setCommandType(CommandType.SPA);
			order.setTxnType(AuthTransType.AgentDebit);
			order.setPurpose("放款申请"); // 接口预留
			order.setPayBizCode("1");//支付接口渠道业务 实时或批量代扣：0 实时或批量代付：1
			order.setMatchInd(Indicator.Y);
			break;
		case S://联机追偿
			order.setCommandType(CommandType.SDB);
			order.setTxnType(AuthTransType.AgentCredit);
			order.setPurpose("联机追偿"); // 接口预留
			order.setPayBizCode("0");//支付接口渠道业务 实时或批量代扣：0 实时或批量代付：1
			order.setMatchInd(Indicator.Y);
			break;
		case N://正常扣款
			order.setCommandType(CommandType.SDB);
			order.setTxnType(AuthTransType.AgentCredit);
			order.setPurpose("正常扣款"); // 接口预留
			order.setPayBizCode("0");//支付接口渠道业务 实时或批量代扣：0 实时或批量代付：1
			order.setMatchInd(Indicator.Y);
			break;
		case O://逾期扣款
			order.setCommandType(CommandType.SDB);
			order.setTxnType(AuthTransType.AgentCredit);
			order.setPurpose("逾期扣款"); // 接口预留
			order.setPayBizCode("0");//支付接口渠道业务 实时或批量代扣：0 实时或批量代付：1
			order.setMatchInd(Indicator.Y);
			break;
		case M://预约结清扣款
			order.setCommandType(CommandType.SDB);
			order.setTxnType(AuthTransType.AgentCredit);
			order.setPurpose("预约结清"); // 接口预留
			order.setPayBizCode("0");//支付接口渠道业务 实时或批量代扣：0 实时或批量代付：1
			order.setMatchInd(Indicator.Y);
			break;
		case E://代付确认
			order.setCommandType(CommandType.QSP);
			order.setTxnType(AuthTransType.Inq);
			order.setPurpose("代付查询"); // 接口预留
			order.setPayBizCode("1");
			order.setMatchInd(Indicator.N);
			break;
		case F://代扣确认
			order.setCommandType(CommandType.QSD);
			order.setTxnType(AuthTransType.Inq);
			order.setPurpose("代扣查询"); // 接口预留
			order.setPayBizCode("0");
			order.setMatchInd(Indicator.N);
			break;
		case B://结算
			order.setCommandType(CommandType.QSP);
			order.setTxnType(AuthTransType.AdviceSettle);
			order.setPurpose("结算"); // 接口预留
			order.setPayBizCode("1");
			order.setMatchInd(Indicator.Y);
			break;
		case G://结算查询
			order.setCommandType(CommandType.QSP);
			order.setTxnType(AuthTransType.Inq);
			order.setPurpose("结算查询"); // 接口预留
			order.setPayBizCode("1");
			order.setMatchInd(Indicator.N);
			break;
		case R://退货
			order.setCommandType(CommandType.SDB);
			order.setTxnType(AuthTransType.AgentCredit);
			order.setPurpose("退货"); // 接口预留
			order.setPayBizCode("0");//支付接口渠道业务 实时或批量代扣：0 实时或批量代付：1
			order.setMatchInd(Indicator.Y);
			break;
		case V://撤销
//			order.setCommandType(CommandType.SDB);
//			order.setTxnType(AuthTransType.AgentCredit);
			order.setPurpose("撤销"); // 接口预留
			order.setPayBizCode("0");//支付接口渠道业务 实时或批量代扣：0 实时或批量代付：1
			order.setMatchInd(Indicator.N);
			break;
		case D://溢缴款转出
//			order.setCommandType(CommandType.SDB);
//			order.setTxnType(AuthTransType.AgentCredit);
			order.setPurpose("溢缴款转出"); // 接口预留
			order.setPayBizCode("0");//支付接口渠道业务 实时或批量代扣：0 实时或批量代付：1
			order.setMatchInd(Indicator.N);
			break;
		case Q://优惠卷抵扣
			order.setCommandType(CommandType.SDB);
			order.setTxnType(AuthTransType.AgentCredit);
			order.setPurpose("优惠卷还款"); // 接口预留
			order.setPayBizCode("0");//支付接口渠道业务 实时或批量代扣：0 实时或批量代付：1
			order.setMatchInd(Indicator.N);
			break;
		default:
			logger.error("不支持贷款用途：[{}]",loanUsage);
			throw new ProcessException(MsRespCode.E_1009.getCode(),MsRespCode.E_1009.getMessage());
		}
	}

	/**
	 * 初始化空信息
	 * @param order
	 */
	private void setByDefault(CcsOrder order) {
		order.setOrderBrief("");
		order.setOrberFailTime(null);
		order.setMerId("");
		order.setMerName("");
		order.setProductType("");
		order.setStatus("");
		order.setCode("");
		order.setMessage("");
		order.setOriOrderId(null);
		order.setPriv1("");//支付接口私有域
	}

	/**
	 * 客户信息补充订单
	 * @param cust
	 * @param order
	 */
	private void setByCustInfo(CcsCustomer cust, CcsOrder order) {
		if(null != cust){
			order.setCertType(idTypeTransfer(cust.getIdType()));
			order.setCertId(cust.getIdNo());
		}
	}

	/**
	 * 账户信息补充订单
	 * @param acct
	 * @param order
	 */
	private void setByAcctInfo(CcsAcct acct, CcsOrder order) {
		if(null != acct){
			order.setOrg(acct.getOrg());
			order.setAcctNbr(acct.getAcctNbr());
			order.setAcctType(acct.getAcctType());
			order.setCurrency(acct.getCurrency());
			order.setOpenBankId(acct.getDdBankBranch());
			order.setOpenBank(acct.getDdBankName());
			order.setCardNo(acct.getDdBankAcctNbr());
			order.setUsrName(acct.getDdBankAcctName());
			order.setSubBank(acct.getOwningBranch());//马上前置接口没有支行
			order.setState(acct.getDdBankProvince()); // 省份?
			order.setCity(acct.getDdBankCity()); // 城市?
		}
	}

	/**
	 * nova - 马上   证件类型转换
	 * @param idType
	 * @return
	 */
	private String idTypeTransfer(IdType idType) {
		switch(idType){
			case I:
			case S:
			case P:
				return idType.getIdTypeVal();
			case R:
				return "04";
			case H:
				return "05";
			default:
				return "06";
		}
	}
	
	/**
	 * 更新订单
	 * @param order
	 * @param mainResp
	 * @param data
	 */
	public void updateOrder(CcsOrder order, CommResp mainResp,String status , OrderStatus orderStatus,Long logKv,Date bizDate,MsRespCode msRespCode) {
		if(null!=order){
			order.setCode(mainResp.getCode());
			order.setMessage(mainResp.getMessage());
			order.setStatus(status);
			order.setOrderStatus(orderStatus);
			order.setLogKv(logKv);
			order.setOptDatetime(bizDate);
			if(null != msRespCode){
				order.setResponseCode(msRespCode.getCode());
				order.setResponseMessage(msRespCode.getMessage());
			}
			
			if(OrderStatus.S == orderStatus){
				order.setSuccessAmt(order.getTxnAmt());
			}else if(OrderStatus.E == orderStatus){
				order.setFailureAmt(order.getTxnAmt());
			}
		}
	}
	
	/**
	 * 查询原订单
	 * @param transAmt 交易金额
	 * @param merId 商户编号
	 * @param contrNbr 合同号  <<必须传入>>
	 * @param serviceSn 外部流水号
	 * @param serviceId 服务编号
	 * @param acqId 受理机构号
	 * @param merOrderId 商户订单号
	 * @param orderStatus 订单状态
	 * @param transType 交易类型
	 * @return
	 */
	public CcsOrder findByOrig(BigDecimal transAmt,String merId,
			String contrNbr,String serviceSn,String serviceId,
			String acqId,OrderStatus orderStatus,AuthTransType transType,
			String merOrderId){
		
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
		BooleanExpression expression = qCcsOrder.contrNbr.eq(contrNbr);
		if(null != transAmt){
			expression = expression.and(qCcsOrder.txnAmt.eq(transAmt));
		}
		if(StringUtils.isNotBlank(merId)){
			expression = expression.and(qCcsOrder.merId.eq(merId));
		}
		if(StringUtils.isNotBlank(serviceSn)){
			expression = expression.and(qCcsOrder.servicesn.eq(serviceSn));
		}
		if(StringUtils.isNotBlank(serviceId)){
			expression = expression.and(qCcsOrder.serviceId.eq(serviceId));
		}
		if(StringUtils.isNotBlank(acqId)){
			expression = expression.and(qCcsOrder.acqId.eq(acqId));
		}
		if(null != orderStatus){
			expression = expression.and(qCcsOrder.orderStatus.eq(orderStatus));
		}
		if(null != transType){
			expression = expression.and(qCcsOrder.txnType.eq(transType));
		}
		if(StringUtils.isNotBlank(merOrderId)){
			expression = expression.and(qCcsOrder.merchandiseOrder.eq(merOrderId));
		}
		
		CcsOrder ccsOrder = rCcsOrder.findOne(expression);
		
		return ccsOrder;
	}
	
	/**
	 * 查询原订单历史
	 * @param transAmt 交易金额
	 * @param merId 商户编号
	 * @param contrNbr 合同号  <<必须传入>>
	 * @param serviceSn 外部流水号
	 * @param serviceId 服务编号
	 * @param acqId 受理机构号
	 * @param merOrderId 商户订单号
	 * @param orderStatus 订单状态
	 * @param transType 交易类型
	 * @return
	 */
	public CcsOrderHst findByOrigHst(BigDecimal transAmt,String merId,
			String contrNbr,String serviceSn,String serviceId,String acqId,
			OrderStatus orderStatus,AuthTransType transType,String merOrderId){
		
		QCcsOrderHst qCcsOrderHst = QCcsOrderHst.ccsOrderHst;
		BooleanExpression expression = qCcsOrderHst.contrNbr.eq(contrNbr);
		if(null != transAmt){
			expression = expression.and(qCcsOrderHst.txnAmt.eq(transAmt));
		}
		if(StringUtils.isNotBlank(merId)){
			expression = expression.and(qCcsOrderHst.merId.eq(merId));
		}
		if(StringUtils.isNotBlank(serviceSn)){
			expression = expression.and(qCcsOrderHst.servicesn.eq(serviceSn));
		}
		if(StringUtils.isNotBlank(serviceId)){
			expression = expression.and(qCcsOrderHst.serviceId.eq(serviceId));
		}
		if(StringUtils.isNotBlank(acqId)){
			expression = expression.and(qCcsOrderHst.acqId.eq(acqId));
		}
		if(null != orderStatus){
			expression = expression.and(qCcsOrderHst.orderStatus.eq(orderStatus));
		}
		if(null != transType){
			expression = expression.and(qCcsOrderHst.txnType.eq(transType));
		}
		if(StringUtils.isNotBlank(merOrderId)){
			expression = expression.and(qCcsOrderHst.merchandiseOrder.eq(merOrderId));
		}
		
		CcsOrderHst ccsOrderHst = rCcsOrderHst.findOne(expression);
		
		return ccsOrderHst;
	}

	/**
	 * 查询订单交易方向
	 * @param 内部交易订单服务码
	 * @return 外部订单流水交易方向
	 */
	public String getServiceIdMapping(String outOrderType){
		//正向交易
		final String[] payArr = {
				"TNRAAcctSetupWithDraw",//等额本息开户放款接口（现金贷）
				"TFNCommodyLoanSetup",//等额本息开户（商品贷）
				"TFDCommodyLoanWithDraw",//等额本息提现（商品贷）
				"TFNCommodyLoanSetupWithDraw"//等额本息开户放款（商品贷）
		};
		//撤销交易
		final String[] canclArr ={
				"TFDWithDrawVoid"//放款撤销接口
		};
		//退货交易
		final String[] refundArr ={
				"TFCRefund"//商品贷退货接口
		};
		class ArrayCompare{
			boolean compare(String str,String[] arr){
				for(int i=0;i<arr.length;i++){
					if(arr[i].equals(str)) return true;
				}
				return false;
			}
		}
		ArrayCompare arrayCompare = new ArrayCompare();
		return arrayCompare.compare(outOrderType, payArr)?"pay":
			arrayCompare.compare(outOrderType, canclArr)?"cancel":
				arrayCompare.compare(outOrderType, refundArr)?"refund":null;
	}
	
	/**
	 * 获取order里面符合条件的数据
	 * @param String contrNbr,String serviceSn,String serviceId
	 * @return
	 */
	public  List<CcsOrder> getCcsOrderList(String contrNbr,String serviceSn,String serviceId) {
		
		QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;

		BooleanExpression booleanExpression = qCcsOrder.contrNbr.eq(contrNbr);
//		//判断传入合同号是否为空
//		if (contrNbr != null) {
//			booleanExpression = booleanExpression.and( qCcsOrder.contrNbr.eq(contrNbr));
//		}
		//判断传入serviceid是否为空
		if (serviceId != null) {
			booleanExpression = booleanExpression.and(qCcsOrder.serviceId.eq(serviceId));
		}
		//判断流水号是否为空
		if (serviceSn != null) {
			booleanExpression = booleanExpression.and(qCcsOrder.servicesn.eq(serviceSn));
		}
		return new JPAQuery(em).from(qCcsOrder).where(booleanExpression).list(qCcsOrder);
	}
	/**
	 * 获取orderHst里面符合条件的数据
	 * @param String contrNbr,String serviceSn,String serviceId
	 * @return
	 */
	public  List<CcsOrderHst> getCcsOrderHstList(String contrNbr,String serviceSn,String serviceId) {
		
		QCcsOrderHst qCcsOrderHst = QCcsOrderHst.ccsOrderHst;
	
		BooleanExpression booleanExpression = qCcsOrderHst.contrNbr.eq(contrNbr);
		//判断传入流水号是否为空
		if (serviceSn != null) {
			booleanExpression = booleanExpression.and(qCcsOrderHst.servicesn.eq(serviceSn));
		}
		//判断传入serviceid是否为空
		if (serviceId != null) {
			booleanExpression = booleanExpression.and(qCcsOrderHst.serviceId.eq(serviceId));
		}
		
		return new JPAQuery(em).from(qCcsOrderHst).where(booleanExpression).list(qCcsOrderHst);
	}
}
