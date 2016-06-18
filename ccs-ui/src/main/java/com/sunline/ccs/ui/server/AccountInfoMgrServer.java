/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CallOTBProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.ccs.ui.server.commons.DateTools;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Controller
@RequestMapping(value="/accountInfoMgrServer")
public class AccountInfoMgrServer {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private CPSBusProvide cpsBusProvide;
	@Autowired
    private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private CallOTBProvide callOTBProvide;
	@Autowired
	private OpeLogUtil opeLogUtil;
	@Autowired
	private RCcsAcct rCcsAcct;
	
	private QCcsAcct qTmAccount = QCcsAcct.ccsAcct;
	private QCcsAcctO qTmAccountO = QCcsAcctO.ccsAcctO;
	private QCcsCustomer qTmCustomer = QCcsCustomer.ccsCustomer;
	private BooleanExpression exp;
	
	@SuppressWarnings("all")
	@ResponseBody()
	@RequestMapping(value="/getAcctList",method={RequestMethod.POST})
	public FetchResponse getAcctList(@RequestBody FetchRequest request){
		String cardNo = (String)request.getParameter(CcsCardLmMapping.P_CardNbr);
		String currencyCode = (String)request.getParameter(CcsAcct.P_Currency);
		String idType = (String)request.getParameter(CcsCustomer.P_IdType);
		String idNo = (String)request.getParameter(CcsCustomer.P_IdNo);
		String telphone = (String)request.getParameter(CcsCustomer.P_MobileNo);
		List<Tuple> acctList = new ArrayList<Tuple>();
		JPAQuery query = new JPAQuery(em);
		exp = qTmAccount.acctNbr.eq(qTmAccountO.acctNbr)
				.and(qTmAccount.acctType.eq(qTmAccountO.acctType)).
				and(qTmAccount.custId.eq(qTmCustomer.custId))
				.and(qTmAccount.org.eq(OrganizationContextHolder.getCurrentOrg()));
		if(StringUtils.isNotEmpty(cardNo) && StringUtils.isNotEmpty(currencyCode)){ //根据卡号获取账户列表
			//检查卡号
			CheckUtil.checkCardNo(cardNo);
			//查找卡片记录
			CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
			
			exp = exp.and(qTmAccount.acctNbr.eq(tmCard.getAcctNbr()).and(qTmAccount.currency.eq(currencyCode)));
			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer).where(exp)
					.list(qTmAccount, qTmAccountO, qTmCustomer);
			
			if (acctList.size() == 0){
				throw new ProcessException("卡号[" + cardNo +"]查询不到对应的账户信息");
			}
		}else if(StringUtils.isNotEmpty(idType) && StringUtils.isNotBlank(idNo) && StringUtils.isNotEmpty(currencyCode)){ //根据证件类型和证据号码获取账户列表信息
			//检查证件类型和证据号码
			CheckUtil.checkId(idType, idNo);
			exp = exp.and(qTmCustomer.idType.eq((IdType.valueOf(idType))).and(qTmCustomer.idNo.eq(idNo)).and(qTmAccount.currency.eq(currencyCode)));
					
			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer).where(exp)
					.list(qTmAccount, qTmAccountO, qTmCustomer);
			
			if (acctList.size() == 0){
				throw new ProcessException("证件类型["+ idType +"],证件号码["+idNo+"]查询不到对应的账户信息");
			}
		}else if(StringUtils.isNotEmpty(telphone) && StringUtils.isNotEmpty(currencyCode)){ //根据手机号码获取账户列表信息
			
			exp = exp.and(qTmCustomer.mobileNo.eq(telphone)).and(qTmAccount.currency.eq(currencyCode));
			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer).where(exp)
					.list(qTmAccount, qTmAccountO, qTmCustomer);
			
			if (acctList.size() == 0){
				throw new ProcessException("手机号码["+ telphone +"]查询不到对应的账户信息");
			}
		}
		
		
		List<Map<String, Serializable>> acctMapList = new ArrayList<Map<String, Serializable>>();
		for(Tuple objs : acctList) {
			CcsAcct tmAccount = objs.get(qTmAccount);
			CcsAcctO tmAccountO = objs.get(qTmAccountO);
			CcsCustomer customer = objs.get(qTmCustomer);
			Map<String, Serializable> acctMap = tmAccountO.convertToMap();
			acctMap.putAll(tmAccount.convertToMap());
			//查找卡片记录
			CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(tmAccount.getDefaultLogicCardNbr());
			//相同字段，以TM_ACCOUNT_O数据为准
			acctMap.put(tmAccountO.P_MemoDb, tmAccountO.getMemoDb());
			acctMap.put(tmAccountO.P_MemoCash, tmAccountO.getMemoCash());
			acctMap.put(tmAccountO.P_MemoCr, tmAccountO.getMemoCr());
			acctMap.put(customer.P_IdType, customer.getIdType());
			acctMap.put(customer.P_IdNo, customer.getIdNo());
			acctMap.put(CcServProConstants.KEY_CASH_LIMIT, calculateCashLimit(tmAccount).setScale(0, BigDecimal.ROUND_HALF_UP));
			acctMap.put(CcServProConstants.KEY_REAL_OTB,callOTBProvide.realOTB(tmCard.getLogicCardNbr(),queryFacility.getAcctByCardNbr(tmCard.getLogicCardNbr())));
			Product pro = unifiedParameterFacilityProvide.product(tmAccount.getProductCd());
			acctMap.put("productName", pro.description);
			acctMapList.add(acctMap);
		}
		FetchResponse response = new FetchResponse();
		response.setRows(acctMapList);
		return response;
	}
	
	private BigDecimal calculateCashLimit(CcsAcct tmAccount) throws ProcessException {
		if (tmAccount.getCashLmtRate() == null) {
			return validAmt(tmAccount).multiply(unifiedParameterFacilityProvide.cashLimitRate(tmAccount.getProductCd()));
		}
		return validAmt(tmAccount).multiply(tmAccount.getCashLmtRate());
	}
	
	private BigDecimal validAmt(CcsAcct tmAccount) {
		BigDecimal limit = tmAccount.getCreditLmt();
		if (tmAccount.getTempLmtBegDate() != null && tmAccount.getTempLmtEndDate() != null) {
			if (DateTools.dateBetwen_IncludeEQ(tmAccount.getTempLmtBegDate(), new Date(), tmAccount.getTempLmtEndDate())) {
				limit = tmAccount.getTempLmt();
			}
		}
		return limit;
	}
	
	@SuppressWarnings("all")
	@Transactional
	@ResponseBody()
	@RequestMapping(value="/updateAccountInfo",method={RequestMethod.POST})
	public void updateAccountInfo(@RequestBody Map accountInfoMap) {
		CcsAcct tmAccountInp = new CcsAcct();
		Date d1 = new Date();
		Date d2 = new Date();
		Date d3 = new Date();
		Date d4 = new Date();
		Date d5 = new Date();
		Date d6 = new Date();
		Date d7 = new Date();
		Date d8 = new Date();
		Date d9 = new Date();
		Date d10 = new Date();
		Date d11 = new Date();
		Date d12 = new Date();
		Date d13 = new Date();
		if(accountInfoMap.get("firstStmtDate")!=null){
		long date1 = new BigDecimal(String.valueOf(accountInfoMap.get("firstStmtDate"))).longValue();
		d1.setTime(date1);
		accountInfoMap.put("firstStmtDate", d1);
		}
		if(accountInfoMap.get("graceDate")!=null){
		long date2 = new BigDecimal(String.valueOf(accountInfoMap.get("graceDate"))).longValue();
		d2.setTime(date2);
		accountInfoMap.put("graceDate", d2);
		}
		if(accountInfoMap.get("closedDate")!=null){
		long date3 = new BigDecimal(String.valueOf(accountInfoMap.get("closedDate"))).longValue();
		d3.setTime(date3);
		accountInfoMap.put("closedDate", d3);
		}
		if(accountInfoMap.get("chargeOffDate")!=null){
		long date4 = new BigDecimal(String.valueOf(accountInfoMap.get("chargeOffDate"))).longValue();
		d4.setTime(date4);
		accountInfoMap.put("chargeOffDate", d4);
		}
		if(accountInfoMap.get("tempLmtEndDate")!=null){
		long date5 = new BigDecimal(String.valueOf(accountInfoMap.get("tempLmtEndDate"))).longValue();
		d5.setTime(date5);
		accountInfoMap.put("tempLmtEndDate", d5);
		}
		if(accountInfoMap.get("ddDate")!=null){
		long date6 = new BigDecimal(String.valueOf(accountInfoMap.get("ddDate"))).longValue();
		d6.setTime(date6);
		accountInfoMap.put("ddDate", d6);
		}
		if(accountInfoMap.get("lastStmtDate")!=null){
		long date7 = new BigDecimal(String.valueOf(accountInfoMap.get("lastStmtDate"))).longValue();
		d7.setTime(date7);
		accountInfoMap.put("lastStmtDate", d7);
		}
		if(accountInfoMap.get("nextStmtDate")!=null){
		long date8 = new BigDecimal(String.valueOf(accountInfoMap.get("nextStmtDate"))).longValue();
		d8.setTime(date8);
		accountInfoMap.put("nextStmtDate", d8);
		}
		if(accountInfoMap.get("pmtDueDate")!=null){
		long date9 = new BigDecimal(String.valueOf(accountInfoMap.get("pmtDueDate"))).longValue();
		d1.setTime(date9);
		accountInfoMap.put("pmtDueDate", d9);
		}
		if(accountInfoMap.get("firstRetlDate")!=null){
		long date10 = new BigDecimal(String.valueOf(accountInfoMap.get("firstRetlDate"))).longValue();
		d10.setTime(date10);
		accountInfoMap.put("firstRetlDate", d10);
		}
		if(accountInfoMap.get("setupDate")!=null){
		long date11 = new BigDecimal(String.valueOf(accountInfoMap.get("setupDate"))).longValue();
		d11.setTime(date11);
		accountInfoMap.put("setupDate", d11);
		}
		if(accountInfoMap.get("tempLmtBegDate")!=null){
		long date12 = new BigDecimal(String.valueOf(accountInfoMap.get("tempLmtBegDate"))).longValue();
		d12.setTime(date12);
		accountInfoMap.put("tempLmtBegDate", d12);
		}
		if(accountInfoMap.get("closeDate")!=null){
		long date13 = new BigDecimal(String.valueOf(accountInfoMap.get("closeDate"))).longValue();
		d13.setTime(date13);
		accountInfoMap.put("closeDate", d13);
		}
		
		tmAccountInp.updateFromMap(accountInfoMap);
		
		logger.info("updateAccountInfo:acctNo[" + tmAccountInp.getAcctNbr() + "], acctType[" + tmAccountInp.getAcctType() + "]");
		
		CheckUtil.rejectNull(tmAccountInp.getAcctNbr(), "账号不允许为空");
		CheckUtil.rejectNull(tmAccountInp.getAcctType(), "账户类型不允许为空");
		
		//先查找账户信息，不存在则报错
		CcsAcct tmAccount = cpsBusProvide.getTmAccountTOacctNbr(tmAccountInp.getAcctType().name(), tmAccountInp.getAcctNbr());
		tmAccount.updateFromMap(accountInfoMap);
		
		rCcsAcct.saveAndFlush(tmAccount);
		
		//记录操作日志
        opeLogUtil.cardholderServiceLog("3002", tmAccount.getCustId(), tmAccount.getDefaultLogicCardNbr(), tmAccount.getAcctNbr()+","+tmAccount.getAcctType(), "账户信息修改");
	}
	
}
//	private static final long serialVersionUID = 1L;
//	private Logger logger = LoggerFactory.getLogger(this.getClass());
//	
//	@PersistenceContext
//	private EntityManager em;
//	@Autowired
//	private CPSBusProvide cpsBusProvide;
//	@Autowired
//    private UnifiedParameterFacilityProvide unifiedParameterFacilityProvide;
//	@Autowired
//	private CustAcctCardQueryFacility queryFacility;
//	@Autowired
//	private CallOTBProvide callOTBProvide;
//	@Autowired
//	private OpeLogUtil opeLogUtil;
//	
//	private QTmAccount qTmAccount = QTmAccount.tmAccount;
//	private QTmAccountO qTmAccountO = QTmAccountO.tmAccountO;
//	private QTmCustomer qTmCustomer = QTmCustomer.tmCustomer;
//	private BooleanExpression exp;
//	
//	/**
//	 * 根据查询条件获取账户列表信息
//	 */
//	@Override
//	public List<Map<String, Serializable>> getAcctList(String cardNo,
//			String idType, String idNo, String telphone, String currencyCode)
//					throws ProcessException{
//		
//		List<Tuple> acctList = new ArrayList<Tuple>();
//		JPAQuery query = new JPAQuery(em);
//		exp = qTmAccount.acctNo.eq(qTmAccountO.acctNo)
//				.and(qTmAccount.acctType.eq(qTmAccountO.acctType)).
//				and(qTmAccount.custId.eq(qTmCustomer.custId))
//				.and(qTmAccount.org.eq(OrganizationContextHolder.getCurrentOrg()));
//		if(StringUtils.isNotEmpty(cardNo) && StringUtils.isNotEmpty(currencyCode)){ //根据卡号获取账户列表
//			//检查卡号
//			CheckUtil.checkCardNo(cardNo);
//
//			//查找卡片记录
//			TmCard tmCard = cpsBusProvide.getTmCardToCardNo(cardNo);
//			
//			exp = exp.and(qTmAccount.acctNo.eq(tmCard.getAcctNo()).and(qTmAccount.currCd.eq(currencyCode)));
//			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer).where(exp)
//					.list(qTmAccount, qTmAccountO, qTmCustomer);
//			
//			if (acctList.size() == 0){
//				throw new ProcessException("卡号[" + cardNo +"]查询不到对应的账户信息");
//			}
//		}else if(StringUtils.isNotEmpty(idType) && StringUtils.isNotBlank(idNo) && StringUtils.isNotEmpty(currencyCode)){ //根据证件类型和证据号码获取账户列表信息
//			//检查证件类型和证据号码
//			CheckUtil.checkId(idType, idNo);
//			exp = exp.and(qTmCustomer.idType.eq((IdType.valueOf(idType)))
//					.and(qTmCustomer.idNo.eq(idNo)).and(qTmAccount.currCd.eq(currencyCode)));
//			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer).where(exp)
//					.list(qTmAccount, qTmAccountO, qTmCustomer);
//			
//			if (acctList.size() == 0){
//				throw new ProcessException("证件类型["+ idType +"],证件号码["+idNo+"]查询不到对应的账户信息");
//			}
//		}else if(StringUtils.isNotEmpty(telphone) && StringUtils.isNotEmpty(currencyCode)){ //根据手机号码获取账户列表信息
//			
//			exp = exp.and(qTmCustomer.mobileNo.eq(telphone)).and(qTmAccount.currCd.eq(currencyCode));
//			acctList = query.from(qTmAccount, qTmAccountO, qTmCustomer).where(exp)
//					.list(qTmAccount, qTmAccountO, qTmCustomer);
//			
//			if (acctList.size() == 0){
//				throw new ProcessException("手机号码["+ telphone +"]查询不到对应的账户信息");
//			}
//		}
//		
//		
//		List<Map<String, Serializable>> acctMapList = new ArrayList<Map<String, Serializable>>();
//		for(Tuple objs : acctList) {
//			TmAccount tmAccount = objs.get(qTmAccount);
//			TmAccountO tmAccountO = objs.get(qTmAccountO);
//			TmCustomer customer = objs.get(qTmCustomer);
//			
//			Map<String, Serializable> acctMap = tmAccountO.convertToMap();
//			acctMap.putAll(tmAccount.convertToMap());
//			//查找卡片记录
//			TmCard tmCard = cpsBusProvide.getTmCardToCardNo(tmAccount.getDefaultLogicalCardNo());
//			
//			//相同字段，以TM_ACCOUNT_O数据为准
//			acctMap.put(tmAccountO.P_UnmatchDb, tmAccountO.getUnmatchDb());
//			acctMap.put(tmAccountO.P_UnmatchCash, tmAccountO.getUnmatchCash());
//			acctMap.put(tmAccountO.P_UnmatchCr, tmAccountO.getUnmatchCr());
//			
//			acctMap.put(customer.P_IdType, customer.getIdType());
//			acctMap.put(customer.P_IdNo, customer.getIdNo());
//			
//			acctMap.put(CPSServProConstants.KEY_CASH_LIMIT, calculateCashLimit(tmAccount).setScale(0, BigDecimal.ROUND_HALF_UP));
//			acctMap.put(CPSServProConstants.KEY_REAL_OTB,callOTBProvide.realOTB(tmCard.getLogicalCardNo(),queryFacility.getTmAccountByCardNo(tmCard.getLogicalCardNo())));
//			Product pro = unifiedParameterFacilityProvide.product(tmAccount.getProductCd());
//			
//			acctMap.put("productName", pro.description);
//			
//			acctMapList.add(acctMap);
//		}
//		return acctMapList;
//	}
//
//	

//
//	/**
//	 * 计算取现额度
//	 * 
//	 * @param tmAccount
//	 * @return BigDecimal
//	 * @throws ProcessException
//	 * @exception
//	 * @since 1.0.0
//	 */
//	private BigDecimal calculateCashLimit(TmAccount tmAccount) throws ProcessException {
//		if (tmAccount.getCashLimitRt() == null) {
//			return validAmt(tmAccount).multiply(unifiedParameterFacilityProvide.cashLimitRate(tmAccount.getProductCd()));
//		}
//		return validAmt(tmAccount).multiply(tmAccount.getCashLimitRt());
//	}
//	
//	/**
//	 * 获取有效额度
//	 * 
//	 * @param tmAccount
//	 * @return BigDecimal
//	 * @exception
//	 * @since 1.0.0
//	 */
//	private BigDecimal validAmt(TmAccount tmAccount) {
//		BigDecimal limit = tmAccount.getCreditLimit();
//		if (tmAccount.getTempLimitBeginDate() != null && tmAccount.getTempLimitEndDate() != null) {
//			if (DateTools.dateBetwen_IncludeEQ(tmAccount.getTempLimitBeginDate(), new Date(), tmAccount.getTempLimitEndDate())) {
//				limit = tmAccount.getTempLimit();
//			}
//		}
//		return limit;
//	}

	

