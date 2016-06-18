package com.sunline.ccs.service.process;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.QueryRequest;
import com.sunline.ark.support.service.QueryResult;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsCssfeeReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsStatement;
import com.sunline.ccs.infrastructure.server.repos.RCcsStmtReprintReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCssfeeReg;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsStmtReprintReg;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCssfeeReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsStmtReprintReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.Fee;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.PostAvailiableInd;
import com.sunline.ccs.param.def.enums.StatementFlag;
import com.sunline.ccs.service.provide.CallOTBProvide;
import com.sunline.ccs.service.util.BlockCodeUtil;
import com.sunline.ccs.service.util.CPSServProBusUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CcAccountService;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.SmsInd;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;


/** 
 * @see 类名：AccountServiceImpl
 * @see 描述：账户类服务接口
 *
 * @see 创建日期：   2015年6月24日 下午2:44:52
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AccountServiceImpl implements CcAccountService {

	private final float TATE = 100;

	QCcsCard qCcsCard = QCcsCard.ccsCard;
	QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;
	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	QCcsTxnHst qCcsTxnHst = QCcsTxnHst.ccsTxnHst;
	QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
	QCcsCssfeeReg qCcsCssfeeReg = QCcsCssfeeReg.ccsCssfeeReg;
	QCcsStmtReprintReg qCcsStmtReprintReg = QCcsStmtReprintReg.ccsStmtReprintReg;
	@PersistenceContext
	public EntityManager em;

	@Autowired
	public RCcsAcct rCcsAcct;

	@Autowired
	public RCcsAcctO rCcsAcctO;

	@Autowired
	private RCcsAddress rCcsAddress;

	@Autowired
	private RCcsStatement rCcsStatement;

	@Autowired
	private RCcsStmtReprintReg rCcsReprintReg;

	@Autowired
	private RCcsCssfeeReg rCcsCssfeeReg;

	@Autowired
	private CustAcctCardFacility queryFacility;

	@Autowired
	private CallOTBProvide callOTBProvide;

	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;

	@Autowired
	private RCcsCustomer rCcsCustomer;



	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;

	@Autowired
	private BlockCodeUtils blockCodeUtils;

	@Autowired
	private GlobalManagementService globalManagementService;
/*    @Autowired
    private DownMsgFacility downMsgFacility;
*/    @Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
    @Autowired
    private UnifiedParameterFacility unifiedParameterFacility;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	// 按照卡号查询账户信息，其中账户信息=
	// CcsAcct+CcsAcctO+KEY_CASH_LIMIT(取现额度)+KEY_LOAN_LIMIT(分期额度）
	@Override
	@Transactional
	public List<Map<String, Serializable>> NF2101(String cardNbr) throws ProcessException {
		log.info("NF2101:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
		CheckUtil.checkCardNo(cardNbr);
		List<CcsAcct> CcsAcctList = queryFacility.getAcctByCardNbr(cardNbr);
		List<Map<String, Serializable>> CcsAcctListMap = mergeMaps(CcsAcctList);
		LogTools.printObj(log, CcsAcctListMap, "NF2101返回数据:");
		return CcsAcctListMap;
	}

	// 按照证件号码查询账户信息 其中账户信息=
	// CcsAcct+CcsAcctO+KEY_CASH_LIMIT(取现额度)+KEY_LOAN_LIMIT(分期额度）
	@Override
	@Transactional
	public List<Map<String, Serializable>> NF2102(String idType, String idNo) throws ProcessException {
		log.info("NF2102:证件类型[" + idType + "],证件号码[" + CodeMarkUtils.markIDCard(idNo) + "]");
		// // 判断证件类型
		// if (!CheckUtil.isIdNo(IdType.valueOf(idType), idNo)) {
		// throw new ProcessException("非法的证件类型或证件号码");
		// }
		List<Map<String, Serializable>> CcsAcctListMap = mergeMaps(queryFacility.getAcctByIdTypeIdNo(idType, idNo));
		LogTools.printObj(log, CcsAcctListMap, "NF2102返回数据:");
		return CcsAcctListMap;

	}

	// 按照账单月，卡号查询账单统计信息
	@Override
	@Transactional
	public Map<String, Serializable> NF2103(String cardNbr, Date stmtMonth) throws ProcessException {
		log.info("NF2103:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],账单日[" + stmtMonth + "]");
		CheckUtil.checkCardNo(cardNbr);
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(
				qCcsCard.acctNbr.eq(qCcsStatement.acctNbr).and(
						qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr)).and(qCcsStatement.stmtDate.eq(stmtMonth))));
		CcsStatement ccsStatement = query.from(qCcsCardLmMapping, qCcsCard, qCcsStatement).where(booleanExpression).singleResult(qCcsStatement);
		CheckUtil.rejectNull(ccsStatement, "卡号[" + cardNbr + "],账单日[" + stmtMonth + "]找不到对应账单信息");
		Map<String, Serializable> ccsStatementMap = ccsStatement.convertToMap();
		LogTools.printObj(log, ccsStatementMap, "NF2103返回数据:");
		return ccsStatementMap;
	}

	// 按照账单月，卡号查询账单交易信息
	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF2104(QueryRequest queryRequest, String cardNbr, Date stmtMonth) throws ProcessException {
		log.info("NF2104:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],账单日[" + stmtMonth + "]");
		CheckUtil.checkCardNo(cardNbr);
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExcepssion = qCcsCardLmMapping.org
				.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qCcsCardLmMapping.cardNbr.eq(cardNbr))
				.and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(
						qCcsCard.acctNbr.eq(qCcsTxnHst.acctNbr).and(qCcsTxnHst.stmtDate.eq(stmtMonth).and(qCcsTxnHst.txnCode.in(unifiedParameterFacilityProvide.OutStmtTxnCode())))));
		List<CcsTxnHst> qtxnHstList = query.from(qCcsCardLmMapping, qCcsCard, qCcsTxnHst).where(booleanExcepssion).orderBy(qCcsTxnHst.stmtDate.desc(), qCcsTxnHst.logicCardNbr.desc())
				.offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow()).list(qCcsTxnHst);
		JPAQuery query2 = new JPAQuery(em);
		int totalRows = (int) query2.from(qCcsCardLmMapping, qCcsCard, qCcsTxnHst).where(booleanExcepssion).count();
		List<Map<String, Serializable>> list = new ArrayList<Map<String, Serializable>>();
		for (CcsTxnHst tmTxnHst : qtxnHstList) {
			list.add(tmTxnHst.convertToMap());
		}
		QueryResult<Map<String, Serializable>> queryResult = CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRows, list);
		LogTools.printObj(log, queryResult, "NF2104返回数据:");
		return queryResult;
	}

	// 按照卡号查询余额成份信息
	@Override
	@Transactional
	public List<Map<String, Serializable>> NF2106(String cardNbr) throws ProcessException {
		log.info("NF2106:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
		CheckUtil.checkCardNo(cardNbr);
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(
				qCcsCard.acctNbr.eq(qCcsPlan.acctNbr).and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr))));
		Iterator<CcsPlan> tmPlanIter = query.from(qCcsCardLmMapping, qCcsCard, qCcsPlan).where(booleanExpression).list(qCcsPlan).iterator();
		if (!tmPlanIter.hasNext()) {
			throw new ProcessException("卡号[" + cardNbr + "]查询不到对应的账户余额成分信息");
		}
		ArrayList<Map<String, Serializable>> tmPlanList = new ArrayList<Map<String, Serializable>>();
		while (tmPlanIter.hasNext()) {
			CcsPlan tmplan = tmPlanIter.next();
			tmPlanList.add(tmplan.convertToMap());
		}
		LogTools.printObj(log, tmPlanList, "NF2106返回数据:");
		return tmPlanList;

	}

	// 账户临额设置
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transactional
	public Date NF2202(String currencyCd, String cardNbr, Integer tempLmt, Date startDate, Date endDate) throws ProcessException {
		log.info("NF2202:币种[" + currencyCd + "],卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],临时额度[" + tempLmt + "],临时额度开始时间[" + startDate + "]");
		AccountType acctType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(cardNbr, currencyCd);

		CcsCard card = queryFacility.getCardByCardNbr(cardNbr);
		CheckUtil.rejectNull(card, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
		CcsCustomer customer = rCcsCustomer.findOne(card.getCustId());
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		CcsAcctO CcsAcctO = queryFacility.getAcctOByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		if(startDate == null || endDate == null){
			throw new ProcessException("临额设置失败：开始日期或结束日期不能为空");
		}
		if(tempLmt == null){
			throw new ProcessException("临额设置失败：临时额度不能为空");
		}
		if (DateTools.dateCompare(startDate, unifiedParameterFacilityProvide.BusinessDate()) < 0) {
			throw new ProcessException("临额设置失败：额度开始日期不能小于当前日期");
		}
		// 获取参数技术结束时间
		Integer limit = unifiedParameterFacilityProvide.tempLimitMths();
		if(DateUtils.addMonths(startDate, limit).compareTo(endDate) < 0){
			throw new ProcessException("临额设置失败：结束日期大于最大允许的临额有效期限 ");
		}
		// 获取币种参数
		CurrencyCd currCd = unifiedParameterFacilityProvide.currencyCd(currencyCd);
		// 获取最大授信额度
		BigDecimal maxCreditLmt = unifiedParameterFacilityProvide.maxCreditLimit();
		if (new BigDecimal(tempLmt).compareTo(maxCreditLmt) > 1) {
			// 额度调整拒绝短信
//			messageService.sendMessage(MessageCategory.L03, card.getProductCd(), cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(), customer.getEmail(), new Date(),
//					new MapBuilder().add("creditLmt", CcsAcctO.getCreditLmt()).add("currencyCd", currCd.description).build());
			String msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS030);
/*	 		downMsgFacility.sendMessage(msgCd,
					cardNbr,   
					customer.getName(),
					customer.getGender(),
					customer.getMobileNo(),
					new Date(),
				   new MapBuilder().add("creditLmt", CcsAcctO.getCreditLmt()).add("currencyCd", currCd.description).build());
*/			throw new ProcessException("临额设置失败：临时额度不能大于最大信用额度");
		}
		//Date endDate = DateUtils.addMonths(startDate, limit);
		CcsAcct.setTempLmt(new BigDecimal(tempLmt));
		CcsAcct.setTempLmtBegDate(startDate);
		CcsAcct.setTempLmtEndDate(endDate);
		CcsAcctO.setTempLmt(new BigDecimal(tempLmt));
		CcsAcctO.setTempLmtBegDate(startDate);
		CcsAcctO.setTempLmtEndDate(endDate);

		// 临时额度调整成功短信
//		messageService.sendMessage(MessageCategory.L01, card.getProductCd(), cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(), customer.getEmail(), new Date(),
//				new MapBuilder().add("creditLmt", CcsAcctO.getTempLmt()).add("expireDate", CcsAcctO.getTempLmtEndDate()).add("currencyCd", currCd.description).build());
		String msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS028);
/* 		downMsgFacility.sendMessage(msgCd,
		        cardNbr, 
				customer.getName(), 
				customer.getGender(),
				customer.getMobileNo(), 
				new Date(),
                new MapBuilder().add("creditLmt", CcsAcctO.getTempLmt()).add("expireDate", CcsAcctO.getTempLmtEndDate()).add("currencyCd", currCd.description).build());
*/		return endDate;

	}

	// 更新取现比例
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@Transactional
	public void NF2203(String currencyCd, String cardNbr, BigDecimal cashLimitRate) throws ProcessException {
		log.info("NF2203:币种[" + currencyCd + "],卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],取现比例[" + cashLimitRate + "]");
		AccountType acctType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(cardNbr, currencyCd);
		CheckUtil.checkCardNo(cardNbr);
		if (cashLimitRate != null && cashLimitRate.floatValue() > this.TATE) {
			throw new ProcessException("取现比例不能大于100%");
		}
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		CcsAcctO CcsAcctO = queryFacility.getAcctOByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");

		BigDecimal oldCashLmtRt = CcsAcct.getCashLmtRate();

		CcsAcct.setCashLmtRate(cashLimitRate.divide(new BigDecimal("100")));
		CcsAcctO.setCashLmtRate(cashLimitRate.divide(new BigDecimal("100")));

		// 取现额度设置提醒短信
		CcsCard card = queryFacility.getCardByCardNbr(cardNbr);
		CheckUtil.rejectNull(card, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
		if ((oldCashLmtRt == null || cashLimitRate == null) || (oldCashLmtRt.compareTo(cashLimitRate) != 0)) {
			CcsCustomer customer = rCcsCustomer.findOne(card.getCustId());
//			messageService.sendMessage(MessageCategory.M15, card.getProductCd(), cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(), customer.getEmail(), new Date(),
//					new MapBuilder().add("cashLmtRt", cashLimitRate).build());
			String msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS015);
/*	 		downMsgFacility.sendMessage(msgCd,
					cardNbr, customer.getName(),
					customer.getGender(), 
					customer.getMobileNo(),
					new Date(),
			        new MapBuilder().add("cashLmtRt", cashLimitRate).build());
*/		}
	}

	// 更新分期比例
	@Override
	@Transactional
	public void NF2204(String currencyCd, String cardNbr, BigDecimal loanLmtRate) throws ProcessException {
		log.info("NF2204:币种[" + currencyCd + "]," + "卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]分期比例[" + loanLmtRate + "]");
		AccountType acctType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(cardNbr, currencyCd);
		CheckUtil.checkCardNo(cardNbr);
		if (loanLmtRate.floatValue() > this.TATE) {
			throw new ProcessException("分期比例不能大于100%");
		}
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		CcsAcctO CcsAcctO = queryFacility.getAcctOByCardNbr(cardNbr, acctType);

		BigDecimal oldLoanLmtRt = CcsAcct.getLoanLmtRate();

		CcsAcct.setLoanLmtRate(loanLmtRate);
		CcsAcctO.setLoanLmtRate(loanLmtRate);

		// 分期额度设置提醒短信
		// if (oldLoanLmtRt == null && loanLmtRate == null) return;
		if ((oldLoanLmtRt == null || loanLmtRate == null) || (oldLoanLmtRt.compareTo(loanLmtRate) != 0)) {
			CcsCard card = queryFacility.getCardByCardNbr(cardNbr);
			CheckUtil.rejectNull(card, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
			CcsCustomer customer = rCcsCustomer.findOne(CcsAcct.getCustId());
/*			downMsgFacility.sendMessage(
					fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS016), 
					cardNbr, 
					customer.getName(), 
					customer.getGender(), 
					customer.getMobileNo(),
					new Date(),
					new MapBuilder<String, Object>().add("loanLmtRt", loanLmtRate).build());
*/		}
	}

	// 约定还款设置
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transactional
	public void NF2207(String cardNbr, String currencyCd, DdIndicator directDebitIndicator, String ddBankName, String ddBankBranch, String ddAccountNo, String ddAccountName) throws ProcessException {
		log.info("NF2207:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]," + "约定还款指示 [" + directDebitIndicator + "],约定还款银行名称[" + ddBankName + "],约定还款开户行号[" + ddBankBranch + "]," + "约定还款账号[" + ddAccountNo + "], 约定还款账号用户姓名 ["
				+ ddAccountName + "]");
		AccountType acctType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(cardNbr, currencyCd);
		CheckUtil.checkCardNo(cardNbr);
		if (ddAccountNo.length() < 13 || ddAccountNo.length() > 19) {
			throw new ProcessException("无效的约定还款账号");
		}
		CheckUtil.rejectNull(directDebitIndicator, "约定还款指示不能为空");
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		if (!StringUtils.trim(CcsAcct.getName()).equals(StringUtils.trim(ddAccountName))) {
			throw new ProcessException("约定还款设置失败:持卡人姓名与约定还款账号用户姓名不一致");
		}
		// 对于重复设置的问题，DdIndicator =N的时候忽略约定还款银行名称,约定还款开户行号,约定还款账号, 约定还款账号用户姓名
		if (directDebitIndicator == DdIndicator.N) {
			if (CcsAcct.getDdInd() == DdIndicator.N) {
				throw new ProcessException("已经为未设置状态，无需在设");
			}
		} else {
			CheckUtil.rejectNull(ddAccountNo, "约定还款账号不能为空");
			CheckUtil.rejectNull(ddAccountName, "约定还款账号用户姓名不能为空");
			if (CcsAcct.getDdInd() == directDebitIndicator && StringUtils.equals(CcsAcct.getDdBankName(), ddBankName) && StringUtils.equals(CcsAcct.getDdBankBranch(), ddBankBranch)
					&& StringUtils.equals(CcsAcct.getDdBankAcctNbr(), ddAccountNo) && StringUtils.equals(CcsAcct.getDdBankAcctName(), ddAccountName)) {
				throw new ProcessException("输入信息与现有信息相同,无需在设");
			}
		}
		CcsAcct.setDdInd(directDebitIndicator);
		CcsAcct.setDdBankName(ddBankName);
		CcsAcct.setDdBankBranch(ddBankBranch);
		CcsAcct.setDdBankAcctName(ddAccountName);
		CcsAcct.setDdBankAcctNbr(ddAccountNo);

		// 绑定约定还款短信
		CcsCard card = queryFacility.getCardByCardNbr(cardNbr);
		CheckUtil.rejectNull(card, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
		CcsCustomer customer = rCcsCustomer.findOne(card.getCustId());
//		messageService.sendMessage(MessageCategory.O02, card.getProductCd(), cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(), customer.getEmail(), new Date(),
//				new MapBuilder().add("ddInd", CcsAcct.getDdInd()).add("nextStmtDate", CcsAcct.getNextStmtDate()).build());
		String msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS038);
/* 		downMsgFacility.sendMessage(msgCd,
				cardNbr,
				customer.getName(),
				customer.getGender(),
				customer.getMobileNo(), 
				new Date(),
		        new MapBuilder().add("ddInd", CcsAcct.getDdInd()).add("nextStmtDate", CcsAcct.getNextStmtDate()).build());
*/	}

	@Override
	@Transactional
	@Deprecated
	public void NF2208(String cardNbr, String dualBillingIndicator) {

	}

	@Override
	@Transactional
	@Deprecated
	public void NF2301(String cardNbr, Integer stmtDay) {

	}

	// 补寄对账单
	@Override
	@Transactional
	public void NF2302(String cardNbr, String productCd, List<Date> stmtDayList) throws ProcessException {
		log.info("NF2302:卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
		CheckUtil.checkCardNo(cardNbr);
		List<Long> CcsAcctList = queryFacility.getDistinctAcctNbrListByCardNbr(cardNbr);
		// 获取系统业务日期
		Date busDate = unifiedParameterFacilityProvide.BusinessDate();
		Long i = Long.valueOf(1);
        //后来说每次只打一次账单，所以是一对一的关系，不用遍历
		CcsStmtReprintReg	tmReprintReg = rCcsReprintReg.findOne(qCcsStmtReprintReg.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsStmtReprintReg.acctNbr.eq(CcsAcctList.get(0)).and(qCcsStmtReprintReg.stmtDate.eq(stmtDayList.get(0)))));	
		
		if(tmReprintReg != null){
			
			throw new ProcessException("该账单记录已补打对账单");
		}
		CcsStmtReprintReg tmRepringRegTemp = null;
			
	for (Long acctNbr : CcsAcctList) {
			for (Date stmtDay : stmtDayList) {
				CcsStatement ccsStatement = null;
				ccsStatement = rCcsStatement.findOne(qCcsStatement.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsStatement.acctNbr.eq(acctNbr).and(qCcsStatement.stmtDate.eq(stmtDay))));
				if (!ccsStatement.getStmtFlag().equals(StatementFlag.O)) {
					CcsStmtReprintReg ccsStmtReprintReg = new CcsStmtReprintReg();
					ccsStmtReprintReg.setAcctNbr(ccsStatement.getAcctNbr());
					ccsStmtReprintReg.setOrg(ccsStatement.getOrg());
					ccsStmtReprintReg.setCardNbr(cardNbr);
					ccsStmtReprintReg.setStmtDate(ccsStatement.getStmtDate());
					ccsStmtReprintReg.setTxnDate(globalManagementService.getSystemStatus().getBusinessDate());
					ccsStmtReprintReg.setRequestTime(new Date());
					tmRepringRegTemp = rCcsReprintReg.save(ccsStmtReprintReg);
				}
				Fee fee = unifiedParameterFacilityProvide.productCredit(productCd).fee;
				if (!DateUtils.addMonths(stmtDay, fee.waiveMonthReprintStmt).after(busDate)) {
					CcsCssfeeReg tmCssfeeReg = new CcsCssfeeReg();
					tmCssfeeReg.setOrg(OrganizationContextHolder.getCurrentOrg());
					tmCssfeeReg.setCssfeeTxnSeq(i++);
					tmCssfeeReg.setServiceNbr("NF2302");
					tmCssfeeReg.setCardNbr(cardNbr);
					tmCssfeeReg.setTxnDate(busDate);
					tmCssfeeReg.setRequestTime(new Date());
					tmCssfeeReg.setRegId(tmRepringRegTemp.getReprintSeq());
					rCcsCssfeeReg.save(tmCssfeeReg);
				}

			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void NF2401(String cardNbr, String currencyCd) throws ProcessException {
		log.info("NF2401卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNbr) + "],币种:[" + currencyCd + "]");
		AccountType acctType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(cardNbr, currencyCd);
		CheckUtil.checkCardNo(cardNbr);
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		CcsAcctO CcsAcctO = queryFacility.getAcctOByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		// 判断CcsAcct 和CcsAcctO表是否有冻结码，如果有返回账户已冻结，如果没有则设置冻结锁定码
		if (BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
			log.error("卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNbr) + "]+账户类型:[" + acctType + "]账户已经冻结");
			throw new ProcessException("卡号:[" + cardNbr + "],账户已经冻结");
		} else {
			CcsAcct.setBlockCode(BlockCodeUtil.addBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_T));
		}
		if (BlockCodeUtil.hasBlockCode(CcsAcctO.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
			log.error("卡号后四位:[" + cardNbr + "]+账户类型:[" + acctType + "]账户已经冻结");
			throw new ProcessException("卡号:[" + cardNbr + "],账户已经冻结");
		} else {
			CcsAcctO.setBlockCode(BlockCodeUtil.addBlockCode(CcsAcctO.getBlockCode(), CcServProConstants.BLOCKCODE_T));
		}
	}

	@Override
	@Transactional
	public void NF2402(String cardNbr, String currencyCd) throws ProcessException {
		log.info("NF2402卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNbr) + "],币种:[" + currencyCd + "]");
		AccountType acctType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(cardNbr, currencyCd);
		CheckUtil.checkCardNo(cardNbr);
		// 判断CcsAcct和CcsAcctO表中记录是否有冻结码，如果有清除该冻结码，没有就返回账户非冻结状态，无需解冻。
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		CcsAcctO CcsAcctO = queryFacility.getAcctOByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		if (!BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
			log.error("卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNbr) + "]+账户类型:[" + acctType + "]账户非冻结状态，无需解冻。");
			throw new ProcessException("卡号:[" + cardNbr + "],账户非冻结状态，无需解冻。");
		} else {
			CcsAcct.setBlockCode(BlockCodeUtil.removeBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_T));
		}
		if (!BlockCodeUtil.hasBlockCode(CcsAcctO.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
			log.error("卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNbr) + "]+账户类型:[" + acctType + "]账户非冻结状态，无需解冻。");
			throw new ProcessException("卡号:[" + cardNbr + "],账户非冻结状态，无需解冻。");
		} else {
			CcsAcctO.setBlockCode(BlockCodeUtil.removeBlockCode(CcsAcctO.getBlockCode(), CcServProConstants.BLOCKCODE_T));
		}
	}

	@Override
	@Transactional
	@Deprecated
	public void NF2404(String accountNo, String accountType) {

	}

	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF2107(QueryRequest queryRequest, String cardNbr, Date startDate, Date endDate) throws ProcessException {
		log.info("NF2107:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],开始日期:[" + startDate + "],结束日期：[" + endDate + "]");
		CheckUtil.checkCardNo(cardNbr);
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(qCcsCard.acctNbr.eq(qCcsStatement.acctNbr)).and(qCcsCardLmMapping.cardNbr.eq(cardNbr))
				.and(qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()));
		if (startDate != null) {
			booleanExpression = booleanExpression.and(qCcsStatement.stmtDate.goe(startDate));
		}
		if (endDate != null) {
			booleanExpression = booleanExpression.and(qCcsStatement.stmtDate.loe(endDate));
		}
		List<CcsStatement> ccsStatement = query.from(qCcsCardLmMapping, qCcsCard, qCcsStatement).where(booleanExpression).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow()).list(qCcsStatement);
		CheckUtil.rejectNull(ccsStatement, "卡号：[" + cardNbr + "]查询不到账户账单历史信息！！");

		JPAQuery query1 = new JPAQuery(em);
		int totalRows = (int) query1.from(qCcsCardLmMapping, qCcsCard, qCcsStatement).where(booleanExpression).count();

		List<Map<String, Serializable>> list = new ArrayList<Map<String, Serializable>>();
		for (CcsStatement ccsStatementList : ccsStatement) {
			list.add(ccsStatementList.convertToMap());
		}
		return CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRows, list);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transactional
	public void NF2201(String cardNbr, String addrType) throws ProcessException {
		log.info("NF2201:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],地址类型[" + addrType + "]");
		CheckUtil.checkCardNo(cardNbr);
		List<CcsAcct> CcsAcctList = queryFacility.getAcctByCardNbr(cardNbr);

		AddressType oldAddressType = null;
		// 新地址类型
		AddressType newAddressType = AddressType.valueOf(addrType);

		for (CcsAcct CcsAcct : CcsAcctList) {
			// 旧地址类型
			oldAddressType = CcsAcct.getStmtMailAddrInd();

			if (CcsAcct.getStmtMailAddrInd().equals(AddressType.valueOf(addrType))) {
				throw new ProcessException("当前寄送标志与修改的寄送地址标志相同");
			}
			QCcsAddress qCcsAddress = QCcsAddress.ccsAddress;
			CcsAddress address = rCcsAddress.findOne(qCcsAddress.custId.eq(CcsAcct.getCustId()).and(qCcsAddress.addrType.eq(AddressType.valueOf(addrType))));
			CheckUtil.rejectNull(address, "客户号[" + CcsAcct.getCustId() + "]地址类型[" + addrType + "]查询不到对应的地址");
			CcsAcct.setStmtMailAddrInd(AddressType.valueOf(addrType));
			CcsAcct.setStmtCountryCode(address.getCountryCode());
			CcsAcct.setStmtCity(address.getCity());
			CcsAcct.setStmtDistrict(address.getDistrict());
			CcsAcct.setStmtState(address.getState());
			CcsAcct.setStmtPostcode(address.getPostcode());
			CcsAcct.setStmtAddress(address.getAddress());
		}

		CcsCard card = queryFacility.getCardByCardNbr(cardNbr);
		CheckUtil.rejectNull(card, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
		CcsCustomer customer = rCcsCustomer.findOne(card.getCustId());
//		messageService.sendMessage(MessageCategory.M12, card.getProductCd(), cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(), customer.getEmail(), new Date(),
//				new MapBuilder().add("oldAddressType", oldAddressType).add("newAddressType", newAddressType).build());
		String msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS012);
/* 		downMsgFacility.sendMessage(msgCd,
	            cardNbr, 
				customer.getName(), 
				customer.getGender(),
				customer.getMobileNo(),
				new Date(),
			    new MapBuilder().add("oldAddressType", oldAddressType).add("newAddressType", newAddressType).build());
*/	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transactional
	public void NF2205(String currencyCd, String cardNbr, Integer creditLmt) throws ProcessException {
		log.info("NF2201:币种[" + currencyCd + "]," + "卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],永久额度[" + creditLmt + "]");
		AccountType acctType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(cardNbr, currencyCd);
		CheckUtil.checkCardNo(cardNbr);

		CcsCard card = queryFacility.getCardByCardNbr(cardNbr);
		CheckUtil.rejectNull(card, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
		CcsCustomer customer = rCcsCustomer.findOne(card.getCustId());
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		CcsAcctO CcsAcctO = queryFacility.getAcctOByCardNbr(cardNbr, acctType);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		CcsAcct.setCreditLmt(new BigDecimal(creditLmt));
		CcsAcctO.setCreditLmt(new BigDecimal(creditLmt));
		CcsCustomerCrlmt tmCustLmtO = queryFacility.getCustomerCrLmtByCustLmtId(CcsAcct.getCustLmtId());
		if (new BigDecimal(creditLmt).compareTo(tmCustLmtO.getCreditLmt()) > 0) {
			tmCustLmtO.setCreditLmt(new BigDecimal(creditLmt));
		}

		// 永久额度调整成功短信
		CurrencyCd currCd = unifiedParameterFacilityProvide.currencyCd(currencyCd);
//		messageService.sendMessage(MessageCategory.L02, card.getProductCd(), cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(), customer.getEmail(), new Date(),
//				new MapBuilder().add("creditLmt", CcsAcctO.getCreditLmt()).add("currencyCd", currCd.description).build());
		String msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS029);
/* 		downMsgFacility.sendMessage(msgCd,
				cardNbr, 
				customer.getName(), 
				customer.getGender(),
				customer.getMobileNo(), 
				new Date(),
		        new MapBuilder().add("creditLmt", CcsAcctO.getCreditLmt()).add("currencyCd", currCd.description).build());
*/	}

	// 计算账单分期允许的最大金额
	private BigDecimal caculateBillLoanAmt(CcsAcct CcsAcct) {
		BigDecimal billLoan = queryFacility.countPlan_PastPrincipal(CcsAcct.getAcctNbr(), CcsAcct.getAcctType(), PlanType.R);
		if (billLoan.compareTo(CcsAcct.getCreditLmt()) > 0)
			return CcsAcct.getCreditLmt();
		else
			return billLoan;
	}

	/**
	 * 账户信息=CcsAcct+CcsAcctO，组合返回的数据
	 * 
	 * 账户表
	 * 
	 * @param CcsAcctO
	 * @return
	 * @throws ProcessException
	 */
	private Map<String, Serializable> mergeMap(CcsAcct CcsAcct) throws ProcessException {
		CcsAcctO acctO = queryFacility.getAcctOByAcctNbr(CcsAcct.getAcctType(), CcsAcct.getAcctNbr());
		Map<String, Serializable> mapCcsAcct = acctO.convertToMap();
		CheckUtil.rejectNull(acctO, "查询不到对应的账户信息");
		mapCcsAcct.putAll(CcsAcct.convertToMap());
		callOTBProvide.setAccountOTB(acctO, mapCcsAcct);
		// 计算并放置取现额度
		mapCcsAcct.put(CcServProConstants.KEY_CASH_LIMIT, calculateCashLmt(CcsAcct).setScale(0, BigDecimal.ROUND_HALF_UP));
		// 计算并放置分期额度
		mapCcsAcct.put(CcServProConstants.KEY_LOAN_LIMIT, calculateLoanLmt(CcsAcct).setScale(0, BigDecimal.ROUND_HALF_UP));
		// 产品默认取现比例
		mapCcsAcct.put(CcServProConstants.KEY_PROC_CASH_LIMIT_BT, unifiedParameterFacilityProvide.acct_attribute(CcsAcct.getProductCd()).cashLimitRate);
		// 账单分期最大允许分期金额
		mapCcsAcct.put(CcServProConstants.KEY_TOTAL_PASTPRINCIPAL, caculateBillLoanAmt(CcsAcct));
		
		
		//相同字段，以TM_ACCOUNT_O数据为准
		mapCcsAcct.put(CcsAcctO.P_MemoDb, acctO.getMemoDb());
		mapCcsAcct.put(CcsAcctO.P_MemoCash, acctO.getMemoCash());
		mapCcsAcct.put(CcsAcctO.P_MemoCr, acctO.getMemoCr());
		
		return mapCcsAcct;
	}

	/**
	 * 计算取现额度
	 * 
	 * @param CcsAcct
	 * @return BigDecimal
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	private BigDecimal calculateCashLmt(CcsAcct CcsAcct) throws ProcessException {
		if (CcsAcct.getCashLmtRate() == null) {
			return validAmt(CcsAcct).multiply(unifiedParameterFacilityProvide.cashLimitRate(CcsAcct.getProductCd()));
		}
		return validAmt(CcsAcct).multiply(CcsAcct.getCashLmtRate());
	}

	/**
	 * 计算分期额度
	 * 
	 * @param CcsAcct
	 * @return BigDecimal
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	private BigDecimal calculateLoanLmt(CcsAcct CcsAcct) throws ProcessException {
		if (CcsAcct.getLoanLmtRate() == null) {
			return validAmt(CcsAcct).multiply(unifiedParameterFacilityProvide.loanLimitRate(CcsAcct.getProductCd()));
		}
		return validAmt(CcsAcct).multiply(CcsAcct.getLoanLmtRate());
	}

	/**
	 * 获取有效额度
	 * 
	 * @param CcsAcct
	 * @return BigDecimal
	 * @exception
	 * @since 1.0.0
	 */
	private BigDecimal validAmt(CcsAcct CcsAcct) {
		BigDecimal limit = CcsAcct.getCreditLmt();
		if (CcsAcct.getTempLmtBegDate() != null && CcsAcct.getTempLmtEndDate() != null) {
			if (DateTools.dateBetwen_IncludeEQ(CcsAcct.getTempLmtBegDate(), new Date(), CcsAcct.getTempLmtEndDate())) {
				limit = CcsAcct.getTempLmt();
			}
		}
		return limit;
	}

	/**
	 * 账户信息=CcsAcct+CcsAcctO，组合返回列表
	 * 
	 * @param CcsAcctList
	 * @param CcsAcctOList
	 * @return
	 * @throws ProcessException
	 */
	private List<Map<String, Serializable>> mergeMaps(List<CcsAcct> CcsAcctList) throws ProcessException {
		ArrayList<Map<String, Serializable>> CcsAcctMaps = new ArrayList<Map<String, Serializable>>();
		for (CcsAcct CcsAcct : CcsAcctList) {
			CcsAcctMaps.add(mergeMap(CcsAcct));
		}
		return CcsAcctMaps;
	}

	/**
	 * 根据卡号查询该卡号是否可以还款
	 */
	@Override
	public Boolean NF2105(String cardNbr) throws ProcessException {
		log.info("NF2105:" + "卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
		boolean flag = true;
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(cardNbr).get(0);
		BlockCode block = blockCodeUtils.getFirstByPriority(CcsAcct.getBlockCode());
		if (block != null && block.postInd == PostAvailiableInd.R) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 当日补打账单历史
	 * 
	 * @param cardNbr
	 *            卡号
	 * @param serviceNbr
	 *            客服交易编号
	 * @return 
	 * @throws ProcessException
	 * */
	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF2108(QueryRequest queryRequest, String cardNbr) throws ProcessException {
		log.info("NF2108:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
		CheckUtil.checkCardNo(cardNbr);
		BooleanExpression be = qCcsStmtReprintReg.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsStmtReprintReg.cardNbr.eq(cardNbr));
		JPAQuery query = new JPAQuery(em);
		 List<CcsStmtReprintReg> tmReprintRegs = query.from(qCcsStmtReprintReg).where(be).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow()).orderBy(qCcsStmtReprintReg.requestTime.asc())
				.list(qCcsStmtReprintReg);
		JPAQuery query2 = new JPAQuery(em);
		int totalRows = (int) query2.from(qCcsStmtReprintReg).where(be).count();

		List<Map<String, Serializable>> list = new ArrayList<Map<String, Serializable>>();
		for (CcsStmtReprintReg tmRepringReg : tmReprintRegs) {
			list.add(tmRepringReg.convertToMap());
		}

		return CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRows, list);
	}

	/**
	 * 删除客服费用
	 * 
	 * @param cardNbr
	 *            卡号
	 * @param stmtDate
	 *           账单日期
	 * @return 删除记录数
	 * @throws ProcessException
	 * */
	@Override
	@Transactional
	public int NF2109(String cardNbr, Date stmtDate) throws ProcessException {
		log.info("NF2109:卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNbr) + "],cssFeeTxnSeq[" + stmtDate + "]");
		//查询tmrepringreg数据
		CcsStmtReprintReg tmrepringReg = rCcsReprintReg.findOne(qCcsStmtReprintReg.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsStmtReprintReg.cardNbr.eq(cardNbr).and(qCcsStmtReprintReg.stmtDate.eq(stmtDate))));
		
		JPADeleteClause jPADeleteClause = new JPADeleteClause(em, qCcsCssfeeReg);
		long totalRow = jPADeleteClause.where(qCcsCssfeeReg.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCssfeeReg.regId.eq(tmrepringReg.getReprintSeq()))).execute();
		JPADeleteClause jPADeleteClause2 = new JPADeleteClause(em, qCcsStmtReprintReg);
		jPADeleteClause2.where(qCcsStmtReprintReg.reprintSeq.eq(tmrepringReg.getReprintSeq())).execute();
		return (int) totalRow;
	}

	// 账单日调整 (cardNbr 主卡卡号)
	@Override
	@Transactional
	public Date NF2405(String cardNbr, String billingCycle) throws ProcessException {
		log.info("NF2405:卡号后四位:[" + CodeMarkUtils.subCreditCard(cardNbr) + "],账单周期+[" + billingCycle + "]");

		// 获取参数最大账单日修改次数
		Integer maxCycChange = unifiedParameterFacilityProvide.organization().maxCycChange;

		List<CcsAcct> CcsAccts = queryFacility.getAcctByCardNbr(cardNbr);

		CcsAcct tmaccoutttemp = CcsAccts.get(0);

		if (tmaccoutttemp.getYtdCycleChagCnt() != null && tmaccoutttemp.getYtdCycleChagCnt() >= maxCycChange) {
			throw new ProcessException("调整失败：超过本年调整最大次数");
		}
		Date nextStmaDay = null;
		
		Integer cycleint = Integer.valueOf(billingCycle);
		
		if(cycleint <=0 || cycleint > 28){
			throw new ProcessException("无效的账单周期");
		}
		
		// 修改CcsAcct账单日
		for (CcsAcct CcsAcct : CcsAccts) {
			CcsAcct.setCycleDay(String.format("%02d", cycleint));
			if(CcsAcct.getYtdCycleChagCnt() == null){
				CcsAcct.setYtdCycleChagCnt(1);
			}
			CcsAcct.setYtdCycleChagCnt(CcsAcct.getYtdCycleChagCnt() + 1);
			Date busdate = globalManagementService.getSystemStatus().getBusinessDate();
			
			nextStmaDay = DateTools.getSoonDate(busdate, cycleint);
			
			if(DateTools.dateCompare(nextStmaDay, CcsAcct.getGraceDate())<1){
				nextStmaDay = DateUtils.addMonths(nextStmaDay, 1);
			}
			CcsAcct.setNextStmtDate(nextStmaDay);
		}
		// 修改CcsAcctO账单日
		List<CcsAcctO> CcsAcctOs = queryFacility.getAcctOByCardNbr(cardNbr);
		for (CcsAcctO CcsAccto : CcsAcctOs) {
			CcsAccto.setCycleDay(String.format("%02d", cycleint));
		}

		return nextStmaDay;
	}

	@Override
	@Transactional
	public void NF2406(String cardNbr, SmsInd postSmsInd, BigDecimal postUserSmsAmt, SmsInd dualSmsInd, BigDecimal dualUserSmsAmt) throws ProcessException {
		
		CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
		if(CcsCard.getBscSuppInd()==BscSuppIndicator.S){
			throw new ProcessException("设置失败，附卡不能做短信设置");
		}

		ProductCredit product = cardNbrTOProdctAcctFacility.CardNoToProductCr(cardNbr);
		//处理本币
	
		
		if(product.postCurrCd != null){
			CcsAcct postCcsAcct = queryFacility.getAcctByCardNbrCurrency(cardNbr, product.postCurrCd);
			postCcsAcct.setSmsInd(postSmsInd);
			postCcsAcct.setUserSmsAmt(postUserSmsAmt);
			CcsAcctO postCcsAcctO = queryFacility.getAcctOByAcctNbr(postCcsAcct.getAcctType(),postCcsAcct.getAcctNbr());
			postCcsAcctO.setSmsInd(postSmsInd);
			postCcsAcctO.setUserSmsAmt(postUserSmsAmt);
			
			
			 
		}
		//处理外币
		
		if (product.dualCurrCd != null) {
			CcsAcct dualCcsAcct = queryFacility.getAcctByCardNbrCurrency(cardNbr,product.dualCurrCd );
			dualCcsAcct.setSmsInd(dualSmsInd);
			dualCcsAcct.setUserSmsAmt(dualUserSmsAmt);			
			CcsAcctO dualCcsAcctO = queryFacility.getAcctOByAcctNbr(dualCcsAcct.getAcctType(),dualCcsAcct.getAcctNbr());
			dualCcsAcctO.setSmsInd(dualSmsInd);
			dualCcsAcctO.setUserSmsAmt(postUserSmsAmt);
		}

	}

}
