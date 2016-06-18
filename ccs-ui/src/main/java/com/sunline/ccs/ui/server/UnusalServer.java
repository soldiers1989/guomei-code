package com.sunline.ccs.ui.server;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.service.api.SunshineInsuranceService;
import com.sunline.ccs.service.entity.S30003RecommitOrderReq;
import com.sunline.ccs.service.entity.S30003RecommitOrderResp;
import com.sunline.ccs.service.entity.S30004RecommitSettleReq;
import com.sunline.ccs.service.entity.S30004RecommitSettleResp;
import com.sunline.ccs.service.entity.S39001PaySinPaymentQueryReq;
import com.sunline.ccs.service.entity.S39001PaySinPaymentQueryResp;
import com.sunline.ccs.service.entity.S39002PayCutPaymentQueryReq;
import com.sunline.ccs.service.entity.S39002PayCutPaymentQueryResp;
import com.sunline.ccs.service.entity.S39003SettleQueryReq;
import com.sunline.ccs.service.entity.S39003SettleQueryResp;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

/**
 * 代收代付管理
 * 
/** 
 * @see 类名：UnusalServer
 * @see 描述：代收代付管理
 *
 * @see 创建日期：   2015年8月26日上午11:45:54
 * @author songyanchao
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Controller
@RequestMapping(value = "/unusalServer")
public class UnusalServer{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	//服务流水号
	private static final String SERVICE_SN_HERADER="WB";
	//收单机构编号
//	private static final String ACQ_ID="99999998";
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private GlobalManagementService globalManagementService;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	
	@Resource(name="authorizationService")
	private AuthorizationService authorizationService;
	
	@Resource(name = "ccsSunshineInsuranceService")
	private SunshineInsuranceService sunshineInsuranceService;
	
	@Autowired
	private RCcsOrder rCcsOrder;
	 
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	
	@Autowired
	private OperatorAuthUtil operatorAuthUtil;
	QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
	
	Random rd=new Random();
	SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getUnusalList", method = {RequestMethod.POST})
	public FetchResponse getUnusalList(@RequestBody FetchRequest request) throws FlatException {
	    	//
	    	String guarantyId = (String)request.getParameter("guarantyId");
	    	String orderStatus = (String)request.getParameter("orderStatus");
	    	String beginDate = (String)request.getParameter("beginDate");
	    	String endDate = (String)request.getParameter("endDate");
	    	String loanUsage = (String)request.getParameter("loanUsage");
	    	String orderId = (String)request.getParameter("orderId");
	    	String contrNbr = (String)request.getParameter("contrNbr");
	    	//新增查询条件：证件类型、证件号、手机号、交易类型
	    	String certType = (String)request.getParameter("certType");
	    	String certId = (String)request.getParameter("certId");
	    	String mobileNumber = (String)request.getParameter("mobileNumber");
	    	String txnType = (String)request.getParameter("txnType");
	    	
	    	
		JPAQuery query = new JPAQuery(em).from(qCcsOrder).where(qCcsOrder.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		{
			if(guarantyId != null&&!"null".equals(guarantyId)){
				query = query.where(qCcsOrder.guarantyId.eq(guarantyId));
			}
			if(orderStatus != null&&!"null".equals(orderStatus)){
			    	OrderStatus status = OrderStatus.valueOf(orderStatus);
				query = query.where(qCcsOrder.orderStatus.eq(status));
			}
			if(contrNbr != null&&!"null".equals(contrNbr)){
//				QCcsLoan qCcsLoan=QCcsLoan.ccsLoan;
//				CcsLoan ccsLoan=rCcsLoan.findOne(qCcsLoan.contrNbr.eq(contrNbr));
				query = query.where(qCcsOrder.contrNbr.eq(contrNbr));
			}
			if(beginDate!=null&&!"null".equals(beginDate)){
				query=query.where(qCcsOrder.businessDate.goe(new Date(Long.parseLong(beginDate))));
			}
			if(endDate!=null&&!"null".equals(endDate)){
				query=query.where(qCcsOrder.businessDate.loe(new Date(Long.parseLong(endDate))));
			}
			if("B".equals(loanUsage)){
				query=query.where(qCcsOrder.loanUsage.eq(LoanUsage.B));
				query = query.where(qCcsOrder.orderStatus.eq(OrderStatus.W).or(qCcsOrder.orderStatus.eq(OrderStatus.E)).or(qCcsOrder.orderStatus.eq(OrderStatus.P)));
			}else{
				query = query.where(qCcsOrder.orderStatus.eq(OrderStatus.W).or(qCcsOrder.orderStatus.eq(OrderStatus.E)).or(qCcsOrder.orderStatus.eq(OrderStatus.C)));
				if(loanUsage!=null&&!"null".equals(loanUsage)){
				query=query.where(qCcsOrder.loanUsage.eq(LoanUsage.valueOf(loanUsage)));
				}
				query = query.where(qCcsOrder.onlineFlag.eq(Indicator.Y).and(qCcsOrder.loanUsage.notIn(LoanUsage.B)));
			}
			if(orderId!=null&&!"null".equals(orderId)){
				query=query.where(qCcsOrder.orderId.eq(Long.parseLong(orderId)));
			}
			
			if(certType!=null&&!"null".equals(certType)){
				query=query.where(qCcsOrder.certType.eq(certType));
			}
			if(certId!=null&&!"null".equals(certId)){
				query=query.where(qCcsOrder.certId.eq(certId));
			}
			if(mobileNumber!=null&&!"null".equals(mobileNumber)){
				query=query.where(qCcsOrder.mobileNumber.eq(mobileNumber));
			}
			if(txnType!=null&&!"null".equals(txnType)){
				query=query.where(qCcsOrder.txnType.eq(AuthTransType.valueOf(txnType)));
			}
		}
				
		FetchResponse response =  new JPAQueryFetchResponseBuilder(request, query)
		.addFieldMapping(qCcsOrder)
		.build();
		return response;
	}

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/reLoan", method = {RequestMethod.POST})
	public String reLoan(@RequestBody String orderId,@RequestBody String guarantyId,@RequestBody String dueBillNo,@RequestBody String inputSource,@RequestBody String acqId) throws FlatException {
	    logger.info("订单号:[" + orderId + "],保单号:[" + guarantyId + "]");
	    S30003RecommitOrderReq  req = new S30003RecommitOrderReq();
	    //公共报文部分
	    req.setAcqId(acqId);
	    req.setOpId(null);
	    req.setOrg(OrganizationContextHolder.getCurrentOrg());
	    //请求时间用业务时间--测试时可能造成业务日期与当前日期相差太大的问题
//	    req.setRequestTime(format.format(new Date()));
	    String requestTime=format.format(globalManagementService.getSystemStatus().getBusinessDate()).substring(0,8)+format.format(new Date()).substring(8,14);
	    req.setRequestTime(requestTime);
	    req.setInputSource(InputSource.valueOf(inputSource));
	    req.setServiceSn(SERVICE_SN_HERADER+format.format(new Date())+(rd.nextInt(899999)+100000));
	    req.setSubTerminalType(AuthTransTerminal.HOST.toString());
	    Long orderIdNum = Long.parseLong(orderId);
	    req.setOrderId(orderIdNum);
	    req.setGuarantyid(guarantyId);
	    req.setDuebillno(dueBillNo);
	    req.setBizDate(globalManagementService.getSystemStatus().getBusinessDate());
	    S30003RecommitOrderResp resp =  sunshineInsuranceService.recommitOrder(req);
//	    if(resp.getErrorCode().equals("0000")){//调用服务成功
//		String resultCode = resp.getStatus();
		//放款成功，原订单更新为放款重提
//		if(resultCode.equals("S")){
//		    updateOrderStatus(orderIdNum,OrderStatus.R);
//		}
//		//处理中
//		else if(resultCode.equals("P")){
//		    updateOrderStatus(orderIdNum,OrderStatus.W);
//		}
//		//放款失败
//		else if(resultCode.equals("N")){
//		    updateOrderStatus(orderIdNum,OrderStatus.E);
//		}}
		
	    //放款成功
	    return "0000".equals(resp.getErrorCode())?"放款成功!":resp.getErrorMessage();
	}
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/reSettle", method = {RequestMethod.POST})
	public String reSettle(@RequestBody String orderId,@RequestBody String inputSource,@RequestBody String acqId) throws FlatException {
	    logger.info("订单号:[" + orderId + "]");
	    S30004RecommitSettleReq  req = new S30004RecommitSettleReq();
	    //公共报文部分
	    req.setAcqId(acqId);
	    req.setOpId(null);
	    req.setOrg(OrganizationContextHolder.getCurrentOrg());
	    String requestTime=format.format(globalManagementService.getSystemStatus().getBusinessDate()).substring(0,8)+format.format(new Date()).substring(8,14);
	    req.setRequestTime(requestTime);
	    req.setInputSource(InputSource.valueOf(inputSource));
	    req.setServiceSn(SERVICE_SN_HERADER+format.format(new Date())+(rd.nextInt(899999)+100000));
	    req.setSubTerminalType(AuthTransTerminal.HOST.toString());
	    req.setBizDate(globalManagementService.getSystemStatus().getBusinessDate());
	    Long orderIdNum = Long.parseLong(orderId);
	    req.setOrderId(orderIdNum);
	    
	    S30004RecommitSettleResp resp =  sunshineInsuranceService.recommitSettle(req);
//	    if("0000".equals(resp.getErrorCode())){
//	    	if("S".equals(resp.getStatus()))
//	    		updateOrderStatus(orderIdNum,OrderStatus.R);
//	    	if("P".equals(resp.getStatus()))
//	    		updateOrderStatus(orderIdNum,OrderStatus.W);
//	    	if("N".equals(resp.getStatus()))
//	    		updateOrderStatus(orderIdNum,OrderStatus.E);
//	    }
	    return "0000".equals(resp.getErrorCode())?"结算成功 !":resp.getErrorMessage();
	}
	
	/** 
	 * @see 方法名：updateOrderStatus 
	 * @see 描述：更新订单状态-不再使用-lsy20150930
	 * @see 创建日期：Sep 4, 20155:18:26 PM
	 * @author Liming.Feng
	 *  
	 * @param orderIdNum
	 * @param s
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
//	private void updateOrderStatus(Long orderIdNum, OrderStatus s) {
//	    CcsOrder order = rCcsOrder.findOne(orderIdNum);
//	    if(order != null){
//		order.setOrderStatus(s);
//	    }
//	    rCcsOrder.save(order);
//	}

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/queryPayment", method = {RequestMethod.POST})
	public String queryPayment(@RequestBody String orderId, @RequestBody String loanUsage,@RequestBody String inputSource,@RequestBody String acqId) throws FlatException {
	    logger.info("订单号:[" + orderId + "]");
	    Long orderIdNum = Long.parseLong(orderId);
	    //代付的两种状态：放款申请/放款重提/溢缴款转出
	    if(loanUsage.equals(LoanUsage.L.name())||loanUsage.equals(LoanUsage.A.name())||loanUsage.equals(LoanUsage.D.name())){
		S39001PaySinPaymentQueryReq  req = new S39001PaySinPaymentQueryReq();
	    req.setAcqId(acqId);
	    req.setOpId(null);
	    req.setOrg(OrganizationContextHolder.getCurrentOrg());
	    String requestTime=format.format(globalManagementService.getSystemStatus().getBusinessDate()).substring(0,8)+format.format(new Date()).substring(8,14);
	    req.setRequestTime(requestTime);
	    req.setInputSource(InputSource.valueOf(inputSource));
	    req.setServiceSn(SERVICE_SN_HERADER+format.format(new Date())+(rd.nextInt(899999)+100000));
	    req.setSubTerminalType(AuthTransTerminal.HOST.toString());
	    req.setBizDate(globalManagementService.getSystemStatus().getBusinessDate());
		req.setOrderid(orderIdNum);
		
		S39001PaySinPaymentQueryResp resp =  sunshineInsuranceService.paySinPaymentQuery(req);
		return resp.getErrorMessage();
	    }
	    //代扣的五种情形：正常扣款/逾期扣款/追偿代扣/预约提前结清扣款/理赔
	    else if(loanUsage.equals(LoanUsage.N.name())||loanUsage.equals(LoanUsage.O.name())||loanUsage.equals(LoanUsage.S.name())
	    		||loanUsage.equals(LoanUsage.M.name())||loanUsage.equals(LoanUsage.C.name())){
		S39002PayCutPaymentQueryReq  req = new S39002PayCutPaymentQueryReq();
	    req.setAcqId(acqId);
	    req.setOpId(null);
	    req.setOrg(OrganizationContextHolder.getCurrentOrg());
	    String requestTime=format.format(globalManagementService.getSystemStatus().getBusinessDate()).substring(0,8)+format.format(new Date()).substring(8,14);
	    req.setRequestTime(requestTime);
	    req.setInputSource(InputSource.valueOf(inputSource));
	    req.setServiceSn(SERVICE_SN_HERADER+format.format(new Date())+(rd.nextInt(899999)+100000));
	    req.setSubTerminalType(AuthTransTerminal.HOST.toString());
	    req.setBizDate(globalManagementService.getSystemStatus().getBusinessDate());
		req.setOrderid(orderIdNum);
		S39002PayCutPaymentQueryResp resp =  sunshineInsuranceService.payCutPaymentQuery(req);
		return resp.getErrorMessage();
	    }
	    //结算
	    else if(loanUsage.equals(LoanUsage.B.name())){
	    S39003SettleQueryReq  req = new S39003SettleQueryReq();
	    req.setAcqId(acqId);
	    req.setOpId(null);
	    req.setOrg(OrganizationContextHolder.getCurrentOrg());
	    String requestTime=format.format(globalManagementService.getSystemStatus().getBusinessDate()).substring(0,8)+format.format(new Date()).substring(8,14);
	    req.setRequestTime(requestTime);
	    req.setInputSource(InputSource.valueOf(inputSource));
	    req.setServiceSn(SERVICE_SN_HERADER+format.format(new Date())+(rd.nextInt(899999)+100000));
	    req.setSubTerminalType(AuthTransTerminal.HOST.toString());
	    req.setBizDate(globalManagementService.getSystemStatus().getBusinessDate());
		req.setOrderid(orderIdNum);
		S39003SettleQueryResp resp =  sunshineInsuranceService.settleQuery(req);
		return resp.getErrorMessage();
	    }
	    else return null;
	}
	
	@ResponseBody()
	@RequestMapping(value = "/getLoanType", method = {RequestMethod.POST})
	public LoanType getLoanType(@RequestBody String contrNbr){
		try{
			return rCcsLoanReg.findOne(qCcsLoanReg.contrNbr.eq(contrNbr)).getLoanType();
		}catch(Exception e){
			return null;
//			throw new FlatException("未找到贷款信息,合同号["+contrNbr+"]");
		}
	}
}
