package com.sunline.ccs.ui.server;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.LoanLendWay;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.api.BankClientService;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * 
 * @see 类名：FianaceLendingServer
 * @see 描述：现金分期放款
 *
 * @see 创建日期： Jun 23, 201512:58:02 PM
 * @author yeyu
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@SuppressWarnings("all")
@Controller
@RequestMapping(value = "/fianaceLendingServer")
public class FianaceLendingServer {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private RCcsLoanReg rTmLoanReg;

	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	// @Autowired
	// private CashLoanLending cashLoanLending;

	@Resource(name = "authCodeMap")
	private Map<String, String> authCodeMap;

	@Resource(name = "authorizationService")
	private AuthorizationService authorizationService;

	@Resource(name = "bankClientService")
	private BankClientService bankClientService;

/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/
	@Autowired
	private OpeLogUtil opeLogUtil;

	QCcsLoanReg qTmLoanReg = QCcsLoanReg.ccsLoanReg;

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getNeedLendList", method = { RequestMethod.POST })
	// public FetchResponse getNeedLendList(Date beginDate, Date endDate) {
	public FetchResponse getNeedLendList(@RequestBody FetchRequest request) throws FlatException{
		String begDate = (request.getParameter("beginDate") != null) ? String
				.valueOf(request.getParameter("beginDate")) : null;
		String edDate = (request.getParameter("endDate") != null) ? String
				.valueOf(request.getParameter("endDate")) : null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date beginDate = null;
		Date endDate = null;
		if (begDate != null) {
			beginDate = new Date();
			beginDate.setTime(Long.parseLong(begDate));
		}
		if (edDate != null) {
			endDate = new Date();
			endDate.setTime(Long.parseLong(edDate));
		}

		// 根据cps机构层参数判断是否允许试试放款操作
//		com.sunline.ccs.param.def.Organization cpsOrg = unifiedParameterFacilityProvide
//				.organization();
//		if (!LoanLendWay.O.equals(cpsOrg.cashLoanSendMode)) {
//			throw new FlatException("该机构不允许实时放款");
//		}

		JPAQuery query = new JPAQuery(em).from(qTmLoanReg);
		{
			if (beginDate != null) {
				beginDate = DateUtils.truncate(beginDate, Calendar.DATE);
				logger.debug("现金分期放款查询的起始时间：" + beginDate);
				query = query.where(qTmLoanReg.requestTime.goe(beginDate));
			}
			if (endDate != null) {
				endDate = DateUtils.truncate(DateUtils.addDays(endDate, 1),
						Calendar.DATE);
				logger.debug("现金分期放款查询的结束时间：" + endDate);
				query = query.where(qTmLoanReg.requestTime.lt(endDate));
			}

			query = query.where(
					qTmLoanReg.loanAction
							.eq(LoanAction.A)
							.and(qTmLoanReg.loanRegStatus.eq(LoanRegStatus.A))
							.and(qTmLoanReg.loanType.eq(LoanType.C))
							.and(qTmLoanReg.org.eq(OrganizationContextHolder
									.getCurrentOrg()))).orderBy(
					new OrderSpecifier<Long>(Order.DESC,
							qTmLoanReg.registerId));
		}
		FetchResponse response = new JPAQueryFetchResponseBuilder(request,
				query)
				.addFieldMapping(CcsLoanReg.P_Org, qTmLoanReg.org)
				.addFieldMapping(CcsLoanReg.P_AcctNbr, qTmLoanReg.acctNbr)
				.addFieldMapping(CcsLoanReg.P_AcctType, qTmLoanReg.acctType)
				.addFieldMapping(CcsLoanReg.P_RegisterId, qTmLoanReg.registerId)
				.addFieldMapping(CcsLoanReg.P_CardNbr, qTmLoanReg.cardNbr)
				.addFieldMapping(CcsLoanReg.P_LogicCardNbr,
						qTmLoanReg.logicCardNbr)
				.addFieldMapping(CcsLoanReg.P_RefNbr, qTmLoanReg.refNbr)
				.addFieldMapping(CcsLoanReg.P_OrigAuthCode,
						qTmLoanReg.origAuthCode)
				.addFieldMapping(CcsLoanReg.P_OrigTransDate,
						qTmLoanReg.origTransDate)
				.addFieldMapping(CcsLoanReg.P_OrigTxnAmt, qTmLoanReg.origTxnAmt)
				.addFieldMapping(CcsLoanReg.P_B007TxnTime,
						qTmLoanReg.b007TxnTime)
				.addFieldMapping(CcsLoanReg.P_B011Trace, qTmLoanReg.b011Trace)
				.addFieldMapping(CcsLoanReg.P_B032AcqInst,
						qTmLoanReg.b032AcqInst)
				.addFieldMapping(CcsLoanReg.P_B033FwdIns, qTmLoanReg.b033FwdIns)
				.addFieldMapping(CcsLoanReg.P_LoanCode, qTmLoanReg.loanCode)
				.addFieldMapping(CcsLoanReg.P_LoanFeeMethod,
						qTmLoanReg.loanFeeMethod)
				.addFieldMapping(CcsLoanReg.P_LoanFinalTermFee,
						qTmLoanReg.loanFinalTermFee)
				.addFieldMapping(CcsLoanReg.P_LoanFinalTermPrin,
						qTmLoanReg.loanFinalTermPrin)
				.addFieldMapping(CcsLoanReg.P_LoanFixedFee,
						qTmLoanReg.loanFixedFee)
				.addFieldMapping(CcsLoanReg.P_LoanFixedPmtPrin,
						qTmLoanReg.loanFixedPmtPrin)
				.addFieldMapping(CcsLoanReg.P_LoanFirstTermFee,
						qTmLoanReg.loanFirstTermFee)
				.addFieldMapping(CcsLoanReg.P_LoanFirstTermPrin,
						qTmLoanReg.loanFirstTermPrin)
				.addFieldMapping(CcsLoanReg.P_LoanInitFee,
						qTmLoanReg.loanInitFee)
				.addFieldMapping(CcsLoanReg.P_LoanInitPrin,
						qTmLoanReg.loanInitPrin)
				.addFieldMapping(CcsLoanReg.P_LoanInitTerm,
						qTmLoanReg.loanInitTerm)
				.addFieldMapping(CcsLoanReg.P_LoanType, qTmLoanReg.loanType)
				.addFieldMapping(CcsLoanReg.P_RequestTime,
						qTmLoanReg.requestTime)
				.addFieldMapping(CcsLoanReg.P_RegisterDate,
						qTmLoanReg.registerDate)
				.addFieldMapping(CcsLoanReg.P_Remark, qTmLoanReg.remark)
				.build();
		return response;
	}

	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/setLending", method = { RequestMethod.POST })
	public Map<String, String> setLending(@RequestBody Map values) throws FlatException{

		// 根据cps机构层参数判断是否允许试试放款操作
		com.sunline.ccs.param.def.Organization cpsOrg = unifiedParameterFacilityProvide
				.organization();
		if (!LoanLendWay.O.equals(cpsOrg.cashLoanSendMode)) {
			throw new FlatException("该机构不允许实时放款");
		}

		Long registerId = Long.parseLong(String.valueOf(values.get(CcsLoanReg.P_RegisterId)));
		String remark = values.get(CcsLoanReg.P_Remark)==null?null:values.get(CcsLoanReg.P_Remark).toString();

		CcsLoanReg tmLoanReg = rTmLoanReg.findOne(qTmLoanReg.registerId.eq(registerId));
		if (tmLoanReg == null) {
			throw new FlatException("找不到分期注册信息");
		}

		CcsCardO tmCardO = custAcctCardQueryFacility
				.getCardOByLogicCardNbr(tmLoanReg.getLogicCardNbr());
		if (tmCardO == null) {
			throw new FlatException("找不到该分期对应卡");
		}

		CcsAcct tmAccount = custAcctCardQueryFacility.getAcctByAcctNbr(
				tmLoanReg.getAcctType(), tmLoanReg.getAcctNbr());
		if (tmAccount == null) {
			throw new FlatException("找不到该分期对应账户");
		}

		// 没有绑定约定扣款则不予以通过
		if (null == tmAccount.getDdInd()
				|| DdIndicator.N.equals((tmAccount.getDdInd()))) {
			throw new FlatException("无效的约定扣款账户");
		}

		// 调用行内放款
		// 行内放款的方法在facility里没有
		/*
		 * CashLendingMsg returnMsg = cashLoanLending.lending(bankClientService,
		 * authorizationService, tmLoanReg, tmAccount, tmCardO.getProductCd(),
		 * remark);
		 * 
		 * downMsgFacility.sendMessage(cashLoanLending.getMsgCd(tmCardO.getProductCd
		 * (), tmLoanReg, returnMsg.isSuccess()), tmLoanReg.getCardNo(),
		 * tmAccount.getName(), tmAccount.getGender(), tmAccount.getMobileNo(),
		 * new Date(), cashLoanLending.getSendMessage(tmLoanReg,
		 * returnMsg.isSuccess()));
		 * 
		 * String lendingReturn = CashLoanLending.RPC_SUCCESS; if
		 * (StringUtils.isNotBlank(returnMsg.getB039()) && null !=
		 * authCodeMap.get(returnMsg.getB039())) { lendingReturn = new
		 * StringBuffer
		 * (authCodeMap.get(returnMsg.getB039())).append(returnMsg.getErrorMsg
		 * ()) .append("。【错误码：").append(returnMsg.getB039()).append("】")
		 * .toString(); tmLoanReg.setRemark(MsgUtil.substring(lendingReturn,
		 * 40)); } //放款失败，异常在这以字符串形式给出，同时记录操作员日志 if (returnMsg.isSuccess()) {
		 * opeLogUtil.cardholderServiceLog("3310", null,
		 * tmCardO.getLogicalCardNo(), tmAccount.getName(), "现金分期实时放款:成功，金额[" +
		 * tmLoanReg.getLoanInitPrin() + "]"); return lendingReturn; } else {
		 * opeLogUtil.cardholderServiceLog("3310", null,
		 * tmCardO.getLogicalCardNo(), tmAccount.getName(), "现金分期实时放款:失败，金额[" +
		 * tmLoanReg.getLoanInitPrin() + "]，失败原因[" + lendingReturn + "]");
		 * return lendingReturn; }
		 */
		String lendingReturn = "SUCCESS";
		Map<String, String> rtn = new HashMap<String, String>();
		rtn.put("lendingReturn", "SUCCESS");
		return rtn;
	}

}
