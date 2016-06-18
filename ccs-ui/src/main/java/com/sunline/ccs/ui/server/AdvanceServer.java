package com.sunline.ccs.ui.server;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.service.api.MsQueryService;
import com.sunline.ccs.service.api.SunshineInsuranceService;
import com.sunline.ccs.service.entity.S11001BookingReq;
import com.sunline.ccs.service.entity.S11001BookingResp;
import com.sunline.ccs.service.entity.S30003RecommitOrderResp;
import com.sunline.ccs.service.msentity.TNMLBookingReq;
import com.sunline.ccs.service.msentity.TNMLBookingResp;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.OrderStatus;

@Controller
@RequestMapping(value = "/advanceServer")
public class AdvanceServer{
	
	//服务流水号
	private static final String SERVICE_SN_HERADER="WB";
	//收单机构编号
	private static final String ACQ_ID="99999998";
	@Autowired
	private GlobalManagementService globalManagementService;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private OperatorAuthUtil operatorAuthUtil;
	
	@Autowired
	private CPSBusProvide cpsBusProvide;
	private QCcsLoan qtmLoan = QCcsLoan.ccsLoan;
	
	@Resource(name = "msQueryService")
	private MsQueryService msQueryService;
	
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
	SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
	static String dateFormat="yyyyMMdd";
	Random rd=new Random();
	
	/**
	* @Description 试算的server
	* @author 鹏宇
	* @date 2015-12-15 下午7:03:28
	 */
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/calcAdvance", method = {RequestMethod.POST})
	public TNMLBookingResp calcAdvance(@RequestBody String dueBillNo,@RequestBody String caldDate,@RequestBody String inputSource,@RequestBody String contrNbr) throws FlatException{
		TNMLBookingReq req=new TNMLBookingReq();
    	req.setAcqId(ACQ_ID);
    	Date caldate=null;
		try {
			if (caldDate != null && !"null".equals(caldDate) && !"".equals(caldDate)) {
				caldate = DateUtils.parse(new SimpleDateFormat(dateFormat).format(Long.parseLong(caldDate)), dateFormat);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    req.setOpId(null);
	    req.setOrg(OrganizationContextHolder.getCurrentOrg());
	    String requestTime=format.format(globalManagementService.getSystemStatus().getBusinessDate()).substring(0,8)+format.format(new Date()).substring(8,14);
	    req.setRequestTime(requestTime);
	    req.setInputSource(InputSource.BANK);
	    req.setServiceSn(SERVICE_SN_HERADER+format.format(new Date())+(rd.nextInt(899999)+100000));
	    //终端--内管
	    req.setSubTerminalType(AuthTransTerminal.HOST.toString());
	    req.setBizDate(globalManagementService.getSystemStatus().getBusinessDate());
	    req.setCaldate(caldate);
	    req.setDueBillNo(dueBillNo);
	    req.setContrNbr(contrNbr);
	    req.setType("1");//还款：2;试算：1
	    TNMLBookingResp resp=  msQueryService.tnmlBookingResp(req);
	    return resp;
	}
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/commitAdvance", method = {RequestMethod.POST})
	public String commitAdvance(@RequestBody String dueBillNo,@RequestBody String caldDate,@RequestBody String inputSource,@RequestBody String contrNbr) throws FlatException{
		TNMLBookingReq req=new TNMLBookingReq();
    	req.setAcqId(ACQ_ID);
    	Date caldate=null;
		try {
			caldate = DateUtils.parse(new SimpleDateFormat(dateFormat).format(Long.parseLong(caldDate)), dateFormat);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    req.setOpId(OrganizationContextHolder.getUsername());
	    req.setOrg(OrganizationContextHolder.getCurrentOrg());
	    String requestTime=format.format(globalManagementService.getSystemStatus().getBusinessDate()).substring(0,8)+format.format(new Date()).substring(8,14);
	    req.setRequestTime(requestTime);
	    req.setInputSource(InputSource.BANK);
	    req.setServiceSn(SERVICE_SN_HERADER+format.format(new Date())+(rd.nextInt(899999)+100000));
	    //终端--内管
	    req.setSubTerminalType(AuthTransTerminal.HOST.toString());
	    req.setBizDate(globalManagementService.getSystemStatus().getBusinessDate());
	    req.setCaldate(caldate);
	    req.setDueBillNo(dueBillNo);
	    req.setContrNbr(contrNbr);
	    req.setType("2");//还款：2;试算：1
	    TNMLBookingResp resp=  msQueryService.tnmlBookingResp(req);
	    return "0000".equals(resp.getErrorCode())? "操作成功!，试算金额"+resp.getAmount() : resp.getErrorMessage();
	}

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getAdvanceList", method = {RequestMethod.POST})
	public FetchResponse getAdvanceList(@RequestBody FetchRequest request) throws FlatException {
	    	String contrNbr = (String)request.getParameter("contrNbr");
	    	String dueBillNo = (String)request.getParameter("dueBillNo");
	    	String idType=request.getParameter("idType")==null?null:(String)request.getParameter("idType");
	    	String idNo=request.getParameter("idNo")==null?null:(String)request.getParameter("idNo");
		JPAQuery query = new JPAQuery(em).from(qCcsLoan,qCcsAcct).where(qCcsLoan.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		{
			if(contrNbr != null&&!"null".equals(contrNbr)){
				query = query.where(qCcsLoan.contrNbr.eq(contrNbr));
			}
			if(dueBillNo != null&&!"null".equals(dueBillNo)){
				query = query.where(qCcsLoan.dueBillNo.eq(dueBillNo));
			}
			if(StringUtils.isNotEmpty(idType) && StringUtils.isNotBlank(idNo)) { // 根据证件类型和证据号码获取贷款列表信息
				logger.info("证件类型[" + idType + "]，证件号码[" + CodeMarkUtils.markIDCard(idNo) + "]查询贷款列表");
				CcsCustomer customer = cpsBusProvide.getTmCustomerByIdNoAndIdType(idNo,
						IdType.valueOf(idType));
				CheckUtil.rejectNull(customer, "证件类型:[" + idType + "]证件号码[" + idNo + "]查询不到对应的客户信息");

				List<CcsAcct> tmAccountList = cpsBusProvide.getTmAccountToCustId(customer.getCustId());
				CheckUtil.rejectNull(tmAccountList, "客户号[" + customer.getCustId() + "]查询不到对应的账户信息");
				BooleanExpression subExp=qtmLoan.acctNbr.eq(tmAccountList.get(0).getAcctNbr());
				//一个客户号对应多个账户时
				if(tmAccountList.size()>1){
				for(int i=1;i<tmAccountList.size();i++){
					subExp=subExp.or(qtmLoan.acctNbr.eq(tmAccountList.get(i).getAcctNbr()));
				}}
				query=query.where(subExp);
			}
			//随借随还产品不被查出
			query=query.where(qCcsLoan.loanStatus.notIn(LoanStatus.T).and(qCcsLoan.loanStatus.notIn(LoanStatus.F).and(qCcsLoan.loanStatus.notIn(LoanStatus.O))));
			query=query.where(qCcsLoan.loanType.notIn(LoanType.MCAT));
			//账户不为阳光
			query=query.where(qCcsLoan.acctNbr.eq(qCcsAcct.acctNbr).and(qCcsLoan.acctType.eq(qCcsAcct.acctType)).and(qCcsAcct.custSource.notIn(InputSource.SUNS)));
		}
				
		FetchResponse response;
		try {
		    response =
			    new JPAQueryFetchResponseBuilder(request, query)
				    .addFieldMapping(qCcsLoan).build();
		} catch (Exception e) {
		    logger.info(e.getMessage());
		    response = null;
		}
		return response;
	}
}
