package com.sunline.ccs.ui.server;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.service.api.SunshineInsuranceService;
import com.sunline.ccs.service.entity.S30005PaymentCheckReq;
import com.sunline.ccs.service.entity.S30005PaymentCheckResp;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.OrderStatus;

//接口未确定
@Controller
@RequestMapping(value = "/paymentExamServer")
public class PaymentExamServer {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	// 服务流水号
	private static final String SERVICE_SN_HERADER = "WB";
	// 收单机构编号
//	private static final String ACQ_ID = "99999998";
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private GlobalManagementService globalManagementService;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;

	@Resource(name = "authorizationService")
	private AuthorizationService authorizationService;

	@Resource(name = "ccsSunshineInsuranceService")
	private SunshineInsuranceService sunshineInsuranceService;

	@Autowired
	private RCcsOrder rCcsOrder;
	@Autowired
	private OperatorAuthUtil operatorAuthUtil;
	QCcsOrder qCcsOrder = QCcsOrder.ccsOrder;

	Random rd = new Random();
	SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	static String dateFormat="yyyyMMdd";
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getPaymentExamList", method = { RequestMethod.POST })
	public FetchResponse getPaymentExamList(@RequestBody FetchRequest request) throws FlatException {
		//
		String guarantyId = (String) request.getParameter("guarantyId");
		String beginDate = (String) request.getParameter("beginDate");
		String endDate = (String) request.getParameter("endDate");
		String contrNbr = (String) request.getParameter("contrNbr");
		String orderId = (String) request.getParameter("orderId");
		String dueBillNo = (String) request.getParameter("dueBillNo");
		JPAQuery query = new JPAQuery(em).from(qCcsOrder).where(
				qCcsOrder.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		
		{
			if (guarantyId != null && !"null".equals(guarantyId)) {
				query = query.where(qCcsOrder.guarantyId.eq(guarantyId));
			}
			if (dueBillNo != null && !"null".equals(dueBillNo)) {
				query = query.where(qCcsOrder.dueBillNo.eq(dueBillNo));
			}
			if (contrNbr != null && !"null".equals(contrNbr)) {
				query = query.where(qCcsOrder.contrNbr.eq(contrNbr));
			}
			if (beginDate != null && !"null".equals(beginDate)) {
				query = query
						.where(qCcsOrder.businessDate.goe(new Date(Long.parseLong(beginDate))));
			}
			if (endDate != null && !"null".equals(endDate)) {
				query = query.where(qCcsOrder.businessDate.loe(new Date(Long.parseLong(endDate))));
			}
			if (orderId != null && !"null".equals(orderId)) {
				query = query.where(qCcsOrder.orderId.eq(Long.parseLong(orderId)));
			}
			query = query.where(qCcsOrder.orderStatus.eq(OrderStatus.Q));
		}
		FetchResponse response = new JPAQueryFetchResponseBuilder(request, query)
				.addFieldMapping(qCcsOrder)
				.build();
		return response;
	}

	@ResponseBody()
	@RequestMapping(value = "/allowExam", method = { RequestMethod.POST })
	public String allowExam(@RequestBody String orderId,@RequestBody String dueBillNo,@RequestBody String remarks,@RequestBody String inputSource,@RequestBody String contrNbr,@RequestBody String acqId)
			throws FlatException{
		logger.info("订单号:[" + orderId + "],借据号:[" + dueBillNo + "]");
	    S30005PaymentCheckReq  req = new S30005PaymentCheckReq();
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
	    req.setDueBillNo(dueBillNo);
	    req.setRemark(remarks);
	    req.setContrNbr(contrNbr);
	    S30005PaymentCheckResp resp =  sunshineInsuranceService.paymentCheck(req);
	    //交易成功
	    return "0000".equals(resp.getErrorCode())?"审批成功!":resp.getErrorMessage();
	}

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/rejectExam", method = { RequestMethod.POST })
	public String reSettle(@RequestBody String orderId,@RequestBody String guarantyId,@RequestBody String remarks) throws FlatException {
//		String acctNbr=(String) requset.getParameter("acctNbr");
//		String acctType=(String) requset.getParameter("acctType");
//		logger.info("账户号:[" + acctNbr + "],"+"账户类型:[" + acctType + "]");
//		try
//		{
//			QCcsAcct qCcsAcct=QCcsAcct.ccsAcct;
//			CcsAcct ccsAcct=rCcsAcct.findOne(qCcsAcct.acctNbr.eq(Long.parseLong(acctNbr)).and(qCcsAcct.acctType.eq(AccountType.valueOf(acctType))));
//			ccsAcct.setBlockCode("F");//冻结账户
//			rCcsAcct.saveAndFlush(ccsAcct);
//			return "交易成功";
//		}catch(Exception e){
//			logger.debug("审批拒绝-添加账户锁定码异常");
//			logger.debug(""+e);
//			throw new FlatException("操作失败，请重试！");
//		}
		//关闭功能-lsy20151008
		return null;
	}

	private Map<String,String> deleteNullParam(FetchRequest req){
		Map<String,String> criteriaMap=new HashMap<String,String>();
		for(String key:req.getCriteriaMap().keySet()){
			if(req.getCriteriaMap().get(key)==null){
				criteriaMap.put(key,null);
			}else{
				criteriaMap.put(key,req.getCriteriaMap().get(key).toString());
			}
		}
		return criteriaMap;
	}

}
