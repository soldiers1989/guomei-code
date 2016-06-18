package com.sunline.ccs.ui.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import com.sunline.ccs.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.serializable.JsonSerializeUtil;
import com.sunline.ark.support.utils.CodeMarkUtils;

import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 贷款信息查询处理server
 * 
 * @author fanghj
 *
 */
@Controller
@RequestMapping(value = "/t3003Server")
public class QueryLoanInfoServer {
	private static final long serialVersionUID = 1L;
	
	@Autowired
	HttpClient httpClient;
	
	@Value("#{env['applyDetailUrl']?:''}")
	private String applyDetailUrl;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//服务流水号
	private static final String SERVICE_SN_HERADER="YG";
	SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
	Random rd=new Random();

	@PersistenceContext
	private EntityManager em;
	@Autowired
	private CPSBusProvide cpsBusProvide;
	@Autowired
	private GlobalManagementService globalManagementService;

	@Autowired
	private RCcsRepaySchedule rTmSchedule;
	@Autowired
	private RCcsAcct rCcsAcct;

	private QCcsLoan qtmLoan = QCcsLoan.ccsLoan;
	private QCcsRepaySchedule qTmSchedule = QCcsRepaySchedule.ccsRepaySchedule;
	private QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;

	private BooleanExpression exp;
	
	/**
	 * 查询贷款的还款计划
	 */
	@ResponseBody()
	@RequestMapping(value = "/getScheduleList", method = { RequestMethod.POST })
	public List<CcsRepaySchedule> getScheduleList(@RequestBody Long loanId) throws ProcessException {
		logger.info("getScheduleList:loanId[" + loanId + "]");
		CheckUtil.rejectNull(loanId, "贷款编号不能为空");
		QCcsRepaySchedule q = QCcsRepaySchedule.ccsRepaySchedule;
		List<CcsRepaySchedule> scheduleList = new JPAQuery(em).from(q).where(qTmSchedule.loanId.eq(loanId).and(
						qTmSchedule.org.eq(OrganizationContextHolder.getCurrentOrg()))).orderBy(q.currTerm.asc()).list(q);

		return scheduleList;
	}

	/**
	 * 根据查询条件获取贷款信息
	 */
	@ResponseBody()
	@RequestMapping(value = "/getLoanList", method = { RequestMethod.POST })
	public List<CcsLoan> getLoanList(@RequestBody String guarantyId, @RequestBody String idType,
			@RequestBody String idNo, @RequestBody String loanReceiptNbr,
			@RequestBody String contrNbr) throws ProcessException {
		List<CcsLoan> loanList = new ArrayList<CcsLoan>();
		JPAQuery query = new JPAQuery(em);
		exp = qtmLoan.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
				qtmLoan.acctType.eq(AccountType.E));

		if (StringUtils.isNotEmpty(guarantyId)) { // 根据卡号获取贷款列表
			logger.info("保单号[" + guarantyId + "]查询贷款列表");
			// //检查卡号
			// CheckUtil.checkCardNo(cardNo);
			//
			// //查找卡片记录
			// CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
			//
			// exp = exp.and(qtmLoan.logicCardNbr.eq(tmCard.getLogicCardNbr()));
			exp = exp.and(qtmLoan.guarantyId.eq(guarantyId));
			loanList = query.from(qtmLoan).where(exp).orderBy(qtmLoan.registerDate.desc())
					.list(qtmLoan);

			if (loanList.size() == 0) {
				throw new ProcessException("保单号[" + guarantyId + "]查询不到对应的贷款信息");
			}
		} else if (StringUtils.isNotEmpty(idType) && StringUtils.isNotBlank(idNo)) { // 根据证件类型和证据号码获取贷款列表信息
			logger.info("证件类型[" + idType + "]，证件号码[" + CodeMarkUtils.markIDCard(idNo) + "]查询贷款列表");
			CcsCustomer customer = cpsBusProvide.getTmCustomerByIdNoAndIdType(idNo,
					IdType.valueOf(idType));
			CheckUtil.rejectNull(customer, "证件类型:[" + idType + "]证件号码[" + idNo + "]查询不到对应的客户信息");

			List<CcsAcct> tmAccountList = cpsBusProvide.getTmAccountToCustId(customer.getCustId());
//			CcsAcct tmAccount = null;
			// 得到小额贷账户
//			for (CcsAcct _tmAccount : tmAccountList) {
//				if (_tmAccount.getAcctType().equals(AccountType.E)) {
//					tmAccount = _tmAccount;
//				} else {
//					continue;
//				}
//			}
			CheckUtil.rejectNull(tmAccountList, "客户号[" + customer.getCustId() + "]查询不到对应的账户信息");
			BooleanExpression subExp=qtmLoan.acctNbr.eq(tmAccountList.get(0).getAcctNbr());
			//一个客户号对应多个账户时
			if(tmAccountList.size()>1){
			for(int i=1;i<tmAccountList.size();i++){
				subExp=subExp.or(qtmLoan.acctNbr.eq(tmAccountList.get(i).getAcctNbr()));
			}
			}
			exp=exp.and(subExp);
			loanList = query.from(qtmLoan).where(exp).orderBy(qtmLoan.registerDate.desc())
					.list(qtmLoan);

			if (loanList.size() == 0) {
				throw new ProcessException("证件类型[" + idType + "]证件号码[" + idNo + "]查询不到对应的贷款信息");
			}
		} else if (StringUtils.isNotEmpty(loanReceiptNbr)) {

			logger.info("借据号[" + loanReceiptNbr + "]查询贷款列表");
			exp = exp.and(qtmLoan.dueBillNo.eq(loanReceiptNbr));

			loanList = query.from(qtmLoan).where(exp).orderBy(qtmLoan.registerDate.desc())
					.list(qtmLoan);
			if (loanList.size() == 0)
				throw new ProcessException("借据号[" + loanReceiptNbr + "]查询不到对应的贷款信息");

		} else if (StringUtils.isNotEmpty(contrNbr)) {

			logger.info("合同号[" + contrNbr + "]查询贷款列表");
			exp = exp.and(qtmLoan.contrNbr.eq(contrNbr));

			loanList = query.from(qtmLoan).where(exp).orderBy(qtmLoan.registerDate.desc())
					.list(qtmLoan);
			if (loanList.size() == 0)
				throw new ProcessException("合同[" + contrNbr + "]查询不到对应的贷款信息");

		}
		return loanList;
	}
	
	@ResponseBody()
	@RequestMapping(value = "/getApplyDetail", method = { RequestMethod.POST })
	public Map getApplyDetail (@RequestBody String acctNbr,@RequestBody String acctType)throws FlatException{
		
		CcsAcct ccsAcct = rCcsAcct.findOne(qCcsAcct.acctNbr.eq(Long.parseLong(acctNbr)).and(qCcsAcct.acctType.eq(AccountType.valueOf(acctType))));
		if(ccsAcct  == null){
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("\"SERVICE\": {");
		sb.append("\"SERVICE_HEADER\": {");
		sb.append("\"VERSION_ID\":\"01\",");
		sb.append("\"SERVICE_ID\":\"applyDetail\",");
		sb.append("\"ORG\":\""+OrganizationContextHolder.getCurrentOrg()+"\",");
		sb.append("\"SERVICESN\":\""+ SERVICE_SN_HERADER+format.format(new Date())+(rd.nextInt(899999)+100000)+"\",");
		sb.append("\"CHANNEL_ID\":\"INTERNAL\",");   //数据平台和核心都调用了申请单详情查询接口，为了区分请求报文是哪方发过来的，报文头的CHANNEL_ID，数据平台填BANK，核心填INTERNAL
		sb.append("\"SUB_TERMINAL_TYPE\":\""+ AuthTransTerminal.HOST.toString()+"\",");
		sb.append("\"OP_ID\":\"opId\",");
		sb.append("\"REQUEST_TIME\":\""+ format.format(globalManagementService.getSystemStatus().getBusinessDate()).substring(0,8)+format.format(new Date()).substring(8,14)+"\",");
		sb.append("\"ACQ_ID\":\"00130000\",");
		sb.append("\"MAC\":\"mac\"");
		sb.append("},");
		sb.append("\"SERVICE_BODY\": {");
		sb.append("\"REQUEST\": {");
		sb.append("\"APP_NO\":\""+ccsAcct.getApplicationNo()+"\"");
		sb.append("}");
		sb.append("}");
		sb.append("}");
		sb.append("}");
		
		String resp = httpClient.send(applyDetailUrl, sb.toString());
		
		Map<String,Object> map = JsonSerializeUtil.jsonReSerializerNoType(resp,Map.class);
		Map<String,Object> body=(Map<String, Object>) map.get("SERVICE");
		Map<String,Object> map2=(Map<String, Object>) body.get("SERVICE_BODY");
		Map<String,Object> map3=(Map<String, Object>) map2.get("RESPONSE");
			
		return map3;
	}

}
