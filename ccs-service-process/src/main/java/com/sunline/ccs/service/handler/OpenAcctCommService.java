package com.sunline.ccs.service.handler;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.card.LuhnMod10;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctNbr;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardLmMapping;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardUsage;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.server.repos.RCcsEmployee;
import com.sunline.ccs.infrastructure.server.repos.RCcsLinkman;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctNbr;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.CcsCardnbrGrt;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsEmployee;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardnbrGrt;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsEmployee;
import com.sunline.ccs.infrastructure.shared.model.QCcsLinkman;
import com.sunline.ccs.loan.McLoanProvideImpl.LoanInstallmentFeeMethodimple;
import com.sunline.ccs.loan.McLoanProvideImpl.LoanLifeInsuFeeMethodimple;
import com.sunline.ccs.loan.McLoanProvideImpl.LoanReplaceSvcFeeMethodimple;
import com.sunline.ccs.loan.McLoanProvideImpl.LoanSVCFeeMethodimple;
import com.sunline.ccs.loan.McLoanProvideImpl.LoanStampTAXMethodimple;
import com.sunline.ccs.loan.McLoanProvideImpl.LoanlnsuranceMethodimple;
import com.sunline.ccs.loan.McLoanProvideImpl.PrepayPkgMethodimple;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.FirstCardFeeInd;
import com.sunline.ccs.param.def.enums.FirstUsageIndicator;
import com.sunline.ccs.param.def.enums.LimitType;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CcCardNbrService;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.CardFetchMethod;
import com.sunline.ppy.dictionary.enums.CorpStructure;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.enums.DualBillingInd;
import com.sunline.ppy.dictionary.enums.EmpPositionAttrType;
import com.sunline.ppy.dictionary.enums.EmpType;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.enums.Relationship;
import com.sunline.ppy.dictionary.enums.RenewInd;
import com.sunline.ppy.dictionary.enums.SmsInd;
import com.sunline.ppy.dictionary.enums.StmtMediaType;
import com.sunline.ppy.dictionary.enums.TitleOfTechnicalType;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Service
public class OpenAcctCommService {
	public Logger logger = LoggerFactory.getLogger(getClass());
	public static final BigDecimal DEFAULT_LIMIT = new BigDecimal("9999999999999");

	@Autowired
	CustAcctCardFacility queryFacility;
	@Autowired
	public RCcsCustomer rCustomer;
	@Autowired
	public RCcsAddress rAddress;
	@Autowired
	public RCcsLinkman rLinkman;
	@Autowired
	public RCcsAcct rAcct;
	@Autowired
	public RCcsAcctO rAcctO;
	@Autowired
	public RCcsCard rCcsCard;
	@Autowired
	public RCcsEmployee rCcsEmployee;
	@Autowired
	public RCcsAcctNbr rCcsAcctNbr;
	@Autowired
	public RCcsCardLmMapping rCcsCardLmMapping;
	@Autowired
	public AppapiCommService appapiCommService;
	@Autowired
	public RCcsCardUsage rCcsCardUsage;
	@Autowired
	public RCcsCustomerCrlmt rCustomerCrLmt;
	@Autowired
	public RCcsCardO rCcsCardO;
	@Autowired
	public RCcsLoanReg rCcsLoanReg;
	@PersistenceContext
	public EntityManager em;
	@Autowired
	public MicroCreditRescheduleUtils microCreditRescheduleUtils;
	@Autowired
	public UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	public UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private CcCardNbrService cardNbrServiceImpl;
	/**
	 * 建账 处理账户
	 * 
	 * @param context
	 * @return
	 */
	public void initAcctAndO(TxnContext context) {
		AccountAttribute acctAttr = context.getAccAttr();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcctO accounto = null;
		CcsAcct account = null;

		// 取账号, 如果是共享额度账户，则无效贷款产品
		Long acctNo = this.mergeAcctNbr(context);
		// 建账
		account = this.initAcct(acctNo, context);
		accounto = this.initAccto(account);

		txnInfo.setAcctNbr(acctNo);
		txnInfo.setAcctType(acctAttr.accountType);
		context.setAccounto(accounto);
		context.setAccount(account);
	}

	/**
	 * @see 方法名：mergeCustomer
	 * @see 描述：客户信息维护
	 * @see 创建日期：2015年8月10日下午17:17
	 * @author lizz
	 * 
	 * @param req
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsCustomer initCustomer(TxnContext context) {
		LogTools.printObj(logger, "", "客户信息维护mergeCustomer");
		TxnInfo txnInfo = context.getTxnInfo();
		if (logger.isDebugEnabled())
			logger.debug("行内统一客户号internalCustomerId --------------------- " + txnInfo.getUuid());
		QCcsCustomer qCustomer = QCcsCustomer.ccsCustomer;
//		CcsCustomer customer = rCustomer.findOne(qCustomer.internalCustomerId .eq(txnInfo.getUuid()).and(
//						qCustomer.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " "))));
		CcsCustomer customer = rCustomer.findOne(qCustomer.idType.eq(txnInfo.getIdType())
				.and(qCustomer.idNo.eq(txnInfo.getIdNo()))
				.and(qCustomer.org.eq(CheckUtil.addCharForNum(txnInfo.getOrg(), 12, " "))));

		if (customer == null) {

			customer = new CcsCustomer();

			customer.setOrg(txnInfo.getOrg());
			customer.setCustSource(txnInfo.getInputSource());
			customer.setSetupDate(txnInfo.getBizDate());
			customer.setInternalCustomerId(txnInfo.getUuid());
			customer.setUserAmt1(BigDecimal.ZERO);
			customer.setUserAmt2(BigDecimal.ZERO);
			customer.setUserAmt3(BigDecimal.ZERO);
			customer.setUserAmt4(BigDecimal.ZERO);
			customer.setUserAmt5(BigDecimal.ZERO);
			customer.setUserAmt6(BigDecimal.ZERO);
			customer.setUserNumber1(0);
			customer.setUserNumber2(0);
			customer.setUserNumber3(0);
			customer.setUserNumber4(0);
			customer.setUserNumber5(0);
			customer.setUserNumber6(0);
			customer.setOncardName("-");
			customer.setSocialInsAmt(BigDecimal.ZERO);
		} else {
			if (logger.isDebugEnabled())
				logger.debug("老客户 idType:[{}],certid:[{}],custId[{}],internalCustomerId[{}]",
						customer.getIdType(), customer.getIdNo(),
						customer.getCustId(), customer.getInternalCustomerId());
			if(!StringUtils.equals(customer.getInternalCustomerId(),txnInfo.getUuid())){
				throw new ProcessException(MsRespCode.E_1061.getCode(), MsRespCode.E_1061.getMessage());
			}
		}
		return customer;
	}

	/**
	 * @see 方法名：mergeAddress
	 * @see 描述：地址信息维护
	 * @see 创建日期：2015年8月10日
	 * @author lizz
	 * 
	 * @param org
	 * @param bigDecimal
	 * @param addressType
	 * @param address
	 * @param city
	 * @param countryCode
	 * @param district
	 * @param state
	 * @param phone
	 * @param zip
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsAddress mergeAddress(String org, Long custId,
			AddressType addressType, String address, String city,
			String countryCode, String district, String state, String phone,
			String zip) {
		LogTools.printObj(logger, addressType, "地址信息维护mergeAddress");

		QCcsAddress qTmAddress = QCcsAddress.ccsAddress;
		CcsAddress addr = rAddress.findOne(qTmAddress.org.eq(org)
				.and(qTmAddress.custId.eq(custId))
				.and(qTmAddress.addrType.eq(addressType)));

		if (addr == null) {
			addr = new CcsAddress();
			addr.setOrg(org);
			addr.setAddrType(addressType);
			addr.setCustId(custId);
		}
		addr.setAddress(getDefaultValue(address, addr.getAddress(), null,
				String.class));
		addr.setCity(getDefaultValue(city, addr.getCity(), null, String.class));
		addr.setCountryCode(getDefaultValue(countryCode, addr.getCountryCode(),
				"156", String.class));
		addr.setDistrict(getDefaultValue(district, addr.getDistrict(), null,
				String.class));
		addr.setState(getDefaultValue(state, addr.getState(), null,
				String.class));
		addr.setPhone(getDefaultValue(phone, addr.getPhone(), null,
				String.class));
		addr.setPostcode(getDefaultValue(zip, addr.getPostcode(), null,
				String.class));

		return rAddress.save(addr);
	}

	/**
	 * @see 方法名：mergeCustomerCrLmt
	 * @see 描述： 获取(创建)客户层信用额度
	 * @see 创建日期：2015-8-10
	 * @author lizz
	 * 
	 * @param app
	 * @param product
	 * @param acctAttr
	 * @param customer
	 * @param basicCustomer
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void mergeCustomerCrLmt(TxnContext context) {
		LogTools.printObj(logger, "", "获取(创建)客户层信用额度mergeCustomerCrLmt");

		Product product = context.getProduct();
		AccountAttribute acctAttr = context.getAccAttr();
		CcsCustomer customer = context.getCustomer();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsCustomerCrlmt custLimit;
		BigDecimal custLimitAmt = txnInfo.getCreditLmt().multiply(
				BigDecimal.ONE.add(acctAttr.ovrlmtRate));

		if (customer.getCustLmtId() != null) {
			custLimit = rCustomerCrLmt.findOne(customer.getCustLmtId());

			// 更新客户级额度
			if (null != custLimit) {
				if (custLimitAmt.compareTo(custLimit.getCreditLmt()) > 0) {
					custLimit.setCreditLmt(txnInfo.getCreditLmt());
				}
			} else {
				// 到这步如果还没有custLimitId，表明是新建的主卡客户，需要新建一个CustLimitO
				custLimit = new CcsCustomerCrlmt();
				custLimit.setOrg(txnInfo.getOrg());
				custLimit.setLmtCalcMethod(LimitType.H);
				custLimit.setLmtCategroy(product.productType.getLimitCategory());
				custLimit.setCreditLmt(custLimitAmt);
				custLimit = rCustomerCrLmt.save(custLimit);
				customer.setCustLmtId(custLimit.getCustLmtId());
			}
		} else {
			// 到这步如果还没有custLimitId，表明是新建的主卡客户，需要新建一个CustLimitO
			custLimit = new CcsCustomerCrlmt();
			custLimit.setOrg(txnInfo.getOrg());
			custLimit.setLmtCalcMethod(LimitType.H);
			custLimit.setLmtCategroy(product.productType.getLimitCategory());
			custLimit.setCreditLmt(custLimitAmt);
			custLimit = rCustomerCrLmt.save(custLimit);
			customer.setCustLmtId(custLimit.getCustLmtId());
		}

		context.setCustLimitO(custLimit);
	}

	/**
	 * 
	 * @see 方法名：mergeLinkman
	 * @see 描述：创建(更新)联系人信息
	 * @see 创建日期：2015-8-10
	 * @author lizz
	 * 
	 * @param org
	 * @param custId
	 * @param relationship
	 * @param name
	 * @param gender
	 * @param mobileNo
	 * @param birthday
	 * @param corpName
	 * @param idType
	 * @param idNo
	 * @param corpPhone
	 * @param corpFax
	 * @param corpPost
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsLinkman mergeLinkman(String org, Long custId,
			Relationship relationship, String name, Gender gender,
			String mobileNo) {
		LogTools.printObj(logger, relationship, "创建(更新)联系人信息mergeLinkman");
		QCcsLinkman qTmContact = QCcsLinkman.ccsLinkman;
		CcsLinkman contact = rLinkman.findOne(qTmContact.org.eq(org)
				.and(qTmContact.custId.eq(custId))
				.and(qTmContact.relationship.eq(relationship)));

		if (contact == null) {
			contact = new CcsLinkman();
			contact.setOrg(org);
			contact.setCustId(custId);
			contact.setRelationship(relationship);
		}
		contact.setName(getDefaultValue(name, contact.getName(), null,
				String.class));
		contact.setGender(getDefaultValue(gender, contact.getGender(), null,
				Gender.class));
		contact.setMobileNo(getDefaultValue(mobileNo, contact.getMobileNo(),
				null, String.class));

		return rLinkman.save(contact);
	}

	/**
	 * 
	 * @see 方法名：mergeEmployee
	 * @see 描述：创建(更新)工作信息
	 * @see 创建日期：2015-6-23下午6:59:16
	 * @author ChengChun
	 * 
	 * @param org
	 * @param custId
	 * @param companyName
	 * @param companyPhone
	 * @param companyFax
	 * @param title
	 * @param industryCategory
	 * @param companyCategory
	 * @param titleOfTechnical
	 * @param revenuePerYear
	 * @param familyAverageRevenue
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsEmployee mergeEmployee(String org, Long custId,
			String companyName, String companyPhone, String companyFax,
			EmpPositionAttrType title, EmpType industryCategory,
			CorpStructure companyCategory,
			TitleOfTechnicalType titleOfTechnical, BigDecimal revenuePerYear,
			BigDecimal familyAverageRevenue) {
		LogTools.printObj(logger, title, "创建(更新)工作信息mergeEmployee");
		QCcsEmployee qTmEmployee = QCcsEmployee.ccsEmployee;
		CcsEmployee employee = rCcsEmployee.findOne(qTmEmployee.org.eq(org)
				.and(qTmEmployee.custId.eq(custId)));

		if (employee == null) {
			employee = new CcsEmployee();
			employee.setOrg(org);
			employee.setCustId(custId);
			employee.setCorpName(companyName);
		    employee.setCorpTelephNbr(companyPhone);
		    employee.setCorpFax(companyFax);
		}
		employee.setCorpPosition(getDefaultValue(title,
				employee.getCorpPosition(), EmpPositionAttrType.Z,
				EmpPositionAttrType.class));
		employee.setCorpIndustryCategory(getDefaultValue(industryCategory,
				employee.getCorpIndustryCategory(), EmpType.Z, EmpType.class));
		employee.setCorpStructure(getDefaultValue(companyCategory,
				employee.getCorpStructure(), CorpStructure.Z,
				CorpStructure.class));
		employee.setCorpTechTitle(getDefaultValue(titleOfTechnical,
				employee.getCorpTechTitle(), TitleOfTechnicalType.D,
				TitleOfTechnicalType.class));
		employee.setIncomePy(getDefaultValue(revenuePerYear,
				employee.getIncomePy(), BigDecimal.ZERO, BigDecimal.class));
		employee.setFamilyIncomePyp(getDefaultValue(familyAverageRevenue,
				employee.getFamilyIncomePyp(), BigDecimal.ZERO,
				BigDecimal.class));

		return rCcsEmployee.save(employee);
	}

	/**
	 * @see 方法名：mergeAcctNbr
	 * @see 描述：创建(获取)账号
	 * @see 创建日期：2015-8-10
	 * @author lizz
	 * 
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Long mergeAcctNbr(TxnContext context) {
		LogTools.printObj(logger, "", "创建(获取)账号mergeAcctNbr");
		AccountAttribute acctAttr = context.getAccAttr();
		TxnInfo txnInfo = context.getTxnInfo();
		String org = OrganizationContextHolder.getCurrentOrg();
		Long acctNo = null;

		if (acctAttr.accountType.isSharedCredit()) {
			if (logger.isDebugEnabled())
				logger.debug("账户属性参数的账户类型为共享[{}]", acctAttr.accountType);
			throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",非独立账户类型");
		}
		// 一个申请单一个账户
		if (logger.isDebugEnabled())
			logger.debug("申请号为：[{}]", txnInfo.getApplyNo());
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		CcsAcct account = rAcct.findOne(qCcsAcct.applicationNo.eq(txnInfo.getApplyNo()));//acct需要添加申请单号
		if (null != account) {
			if (logger.isDebugEnabled())
				logger.debug("账户已存在，账户类型[{}],账户属性参数的账户类型[{}]",account.getAcctType(), acctAttr.accountType);
			throw new ProcessException(MsRespCode.E_1054.getCode(), MsRespCode.E_1054.getMessage());
		}

		if (acctNo == null) {
			// 不共享或没有账号则生成账号
			CcsAcctNbr acctNbr = new CcsAcctNbr();
			acctNbr.setOrg(org);
			logger.info("--------------------生成账号--------------------");
			acctNbr = rCcsAcctNbr.save(acctNbr);

			acctNo = acctNbr.getAcctNbr();
		}

		logger.debug("获取账号, acctNo=" + acctNo);
		return acctNo;
	}

	/**
	 * 
	 * @see 方法名：mergeAcct
	 * @see 描述：初始化账户
	 * @see 创建日期：2015-6-23下午6:52:44
	 * @author lizz
	 * 
	 * @return
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsAcct initAcct(Long acctNo, TxnContext context) {
		LogTools.printObj(logger, "账号：" + acctNo, "创建(更新)账户mergeAcct");
		AccountAttribute acctAttr = context.getAccAttr();
		CcsCustomer customer = context.getCustomer();
		CcsAcct acct = context.getAccount();
		TxnInfo txnInfo = context.getTxnInfo();
		LoanFeeDef loanFeeDef = context.getLoanFeeDef();
		LoanPlan loanPlan = context.getLoanPlan();

		if (acct == null) {
			if(logger.isDebugEnabled())
				logger.debug("新建账户, acctNo=" + acctNo + ", acctType=" + acctAttr.accountType);

			acct = new CcsAcct();

			acct.setOrg(txnInfo.getOrg());
			acct.setAcctNbr(acctNo);
			acct.setAcctType(acctAttr.accountType);
			acct.setCustId(customer.getCustId());
			acct.setCustLmtId(customer.getCustLmtId()); // 附卡客户不会新建账户，所以这里没问题
			acct.setDefaultLogicCardNbr("0");// 无卡交易
			acct.setCurrency(acctAttr.accountType.getCurrencyCode());
			acct.setStmtMediaType(StmtMediaType.E); // 账单介质类型
			acct.setCreditLmt(txnInfo.getCreditLmt());
			acct.setTempLmt(BigDecimal.ZERO);
			acct.setCashLmtRate(null);
			acct.setOvrlmtRate(null);
			acct.setLoanLmtRate(null);
			acct.setCurrBal(BigDecimal.ZERO);
			acct.setCashBal(BigDecimal.ZERO);
			acct.setPrincipalBal(BigDecimal.ZERO);
			acct.setLoanBal(BigDecimal.ZERO);
			acct.setDisputeAmt(BigDecimal.ZERO);
			acct.setBegBal(BigDecimal.ZERO);
			acct.setGraceDaysFullInd(Indicator.N);// 默认值
			acct.setPointsBegBal(BigDecimal.ZERO);
			acct.setCtdPoints(BigDecimal.ZERO);
			acct.setCtdSpendPoints(BigDecimal.ZERO);
			acct.setCtdAdjPoints(BigDecimal.ZERO);
			acct.setPointsBal(BigDecimal.ZERO);
			acct.setSetupDate(txnInfo.getBizDate());
			acct.setOvrlmtNbrOfCyc(0);
			acct.setProductCd(txnInfo.getProductCd());
			acct = this.setNextStmtDate(acct, txnInfo, loanFeeDef,acctAttr,loanPlan);
			acct.setPmtDueDate(microCreditRescheduleUtils.getNextPaymentDay(
					acct.getProductCd(), acct.getNextStmtDate()));// 计算下个还款日期
			acct.setGraceDate(microCreditRescheduleUtils.getNextGraceDay(
					acct.getProductCd(), acct.getNextStmtDate()));// 计算下个还款日期
			acct.setStmtFlag(Indicator.Y);
			acct.setAgeCode("0");// 账龄默认给"0"
			acct.setAgeCodeGl("0");
			acct.setMemoDb(BigDecimal.ZERO);
			acct.setMemoCash(BigDecimal.ZERO);
			acct.setMemoCr(BigDecimal.ZERO);
			acct.setDdInd(DdIndicator.N);
			acct.setLastDdAmt(BigDecimal.ZERO);
			acct.setDualBillingFlag(DualBillingInd.N);
			acct.setLastPmtAmt(BigDecimal.ZERO);
			acct.setFirstStmtDate(acct.getNextStmtDate());// 建账时为首个账单日期
			acct.setFirstRetlAmt(BigDecimal.ZERO);
			acct.setTotDueAmt(BigDecimal.ZERO);
			acct.setCurrDueAmt(BigDecimal.ZERO);
			acct.setPastDueAmt1(BigDecimal.ZERO);
			acct.setPastDueAmt2(BigDecimal.ZERO);
			acct.setPastDueAmt3(BigDecimal.ZERO);
			acct.setPastDueAmt4(BigDecimal.ZERO);
			acct.setPastDueAmt5(BigDecimal.ZERO);
			acct.setPastDueAmt6(BigDecimal.ZERO);
			acct.setPastDueAmt7(BigDecimal.ZERO);
			acct.setPastDueAmt8(BigDecimal.ZERO);
			acct.setCtdCashAmt(BigDecimal.ZERO);
			acct.setCtdCashCnt(0);
			acct.setCtdRetailAmt(BigDecimal.ZERO);
			acct.setCtdRetailCnt(0);
			acct.setCtdRepayAmt(BigDecimal.ZERO);
			acct.setCtdRepayCnt(0);
			acct.setCtdDbAdjAmt(BigDecimal.ZERO);
			acct.setCtdDbAdjCnt(0);
			acct.setCtdCrAdjAmt(BigDecimal.ZERO);
			acct.setCtdCrAdjCnt(0);
			acct.setCtdFeeAmt(BigDecimal.ZERO);
			acct.setCtdFeeCnt(0);
			acct.setCtdInterestAmt(BigDecimal.ZERO);
			acct.setCtdInterestCnt(0);
			acct.setCtdRefundAmt(BigDecimal.ZERO);
			acct.setCtdRefundCnt(0);
			acct.setCtdMaxOvrlmtAmt(BigDecimal.ZERO);
			acct.setMtdRetailAmt(BigDecimal.ZERO);
			acct.setMtdRetailCnt(0);
			acct.setMtdCashAmt(BigDecimal.ZERO);
			acct.setMtdCashCnt(0);
			acct.setMtdRefundAmt(BigDecimal.ZERO);
			acct.setMtdRefundCnt(0);
			acct.setMtdPaymentAmt(BigDecimal.ZERO);
			acct.setMtdPaymentCnt(0);
			acct.setYtdRetailAmt(BigDecimal.ZERO);
			acct.setYtdRetailCnt(0);
			acct.setYtdCashAmt(BigDecimal.ZERO);
			acct.setYtdCashCnt(0);
			acct.setYtdRefundAmt(BigDecimal.ZERO);
			acct.setYtdRefundCnt(0);
			acct.setYtdOvrlmtFeeAmt(BigDecimal.ZERO);
			acct.setYtdOvrlmtFeeCnt(0);
			acct.setYtdLateFeeAmt(BigDecimal.ZERO);
			acct.setYtdLateFeeCnt(0);
			acct.setYtdRepayAmt(BigDecimal.ZERO);
			acct.setYtdRepayCnt(0);
			acct.setLtdRetailAmt(BigDecimal.ZERO);
			acct.setLtdRetailCnt(0);
			acct.setLtdCashAmt(BigDecimal.ZERO);
			acct.setLtdCashCnt(0);
			acct.setLtdRefundAmt(BigDecimal.ZERO);
			acct.setLtdRefundCnt(0);
			acct.setLtdRepayAmt(BigDecimal.ZERO);
			acct.setLtdRepayCnt(0);
			acct.setLtdHighestPrin(BigDecimal.ZERO);
			acct.setLtdHighestCrBal(BigDecimal.ZERO);
			acct.setLtdHighestBal(BigDecimal.ZERO);
			acct.setLtdLoanAmt(BigDecimal.ZERO);
			acct.setCollectCnt(0);
			acct.setWaiveOvlfeeInd(Indicator.N);
			acct.setWaiveCardfeeInd(Indicator.N);
			acct.setWaiveLatefeeInd(Indicator.N);
			acct.setWaiveSvcfeeInd(Indicator.N);
			acct.setUserNumber1(0);
			acct.setUserNumber2(0);
			acct.setUserNumber3(0);
			acct.setUserNumber4(0);
			acct.setUserNumber5(0);
			acct.setUserNumber6(0);
			acct.setUserAmt1(BigDecimal.ZERO);
			acct.setUserAmt2(BigDecimal.ZERO);
			acct.setUserAmt3(BigDecimal.ZERO);
			acct.setUserAmt4(BigDecimal.ZERO);
			acct.setUserAmt5(BigDecimal.ZERO);
			acct.setUserAmt6(BigDecimal.ZERO);
			acct.setSmsInd(SmsInd.Y);
			acct.setUserSmsAmt(null);// 非0，个性化可定义为0，与null有区别
			acct.setYtdCycleChagCnt(0);
			acct.setPmtDueDayBal(BigDecimal.ZERO);
			acct.setOwningBranch("000000001");
			acct.setQualGraceBal(BigDecimal.ZERO);
			acct.setLastSyncDate(txnInfo.getBizDate());//账户同步日期

			acct.setCustSource(txnInfo.getInputSource());
			
			//合同号
			acct.setContrNbr(this.genContrNbr(acctNo, txnInfo.getBizDate(), txnInfo.getLoanCode()));

		}
		context.setAccount(acct);
		return acct;
	}

	/**
	 * 设置账单日，下一账单日期
	 * 是否跳过月末
	 * @param acct
	 * @param txnInfo
	 * @param loanFeeDef
	 * @param acctAttr 
	 * @param loanPlan 
	 */
	public CcsAcct setNextStmtDate(CcsAcct acct, TxnInfo txnInfo,
			LoanFeeDef loanFeeDef, AccountAttribute acctAttr, LoanPlan loanPlan) {
		
		return setNextStmtDate(acct, txnInfo.getBizDate(), loanFeeDef, acctAttr, loanPlan);
	}
	
	/**
	 * 设置账单日，下一账单日期
	 * 是否跳过月末
	 * @param acct
	 * @param txnInfo
	 * @param loanFeeDef
	 * @param acctAttr 
	 * @param loanPlan 
	 */
	public CcsAcct setNextStmtDate(CcsAcct acct,Date bizDate,
			LoanFeeDef loanFeeDef, AccountAttribute acctAttr, LoanPlan loanPlan) {
		//跳过月末
		Date fixDate = bizDate;
		
		//随借随还不处理账单日
		if(!loanPlan.loanType.equals(LoanType.MCAT)){
			fixDate = microCreditRescheduleUtils.skipEndOfMonth(acctAttr, bizDate);
		}
		
		Calendar fixCal = Calendar.getInstance();
		fixCal.setTime(fixDate);
		int cycleDay = fixCal.get(Calendar.DATE);// 账单日
		
		acct.setCycleDay(String.format("%02d", cycleDay));
		acct.setNextStmtDate(microCreditRescheduleUtils.getLoanPmtDueDate(
				fixDate, loanFeeDef, 2));// 计算下个账单日期
		
		return acct;
	}
	
	/**
	 * 初始化联机账户
	 * @param acct
	 * @return
	 */
	public CcsAcctO initAccto(CcsAcct acct) {
		LogTools.printObj(logger, acct.getAcctNbr(), "创建(更新)账户mergeAccto");
		// 建立对应的AccountO
		CcsAcctO acctO = new CcsAcctO();

		acctO.setOrg(acct.getOrg());
		acctO.setAcctNbr(acct.getAcctNbr());
		acctO.setAcctType(acct.getAcctType());
		acctO.setCustLmtId(acct.getCustLmtId());
		acctO.setCustId(acct.getCustId());
		acctO.setProductCd(acct.getProductCd());
		acctO.setCreditLmt(acct.getCreditLmt());
		acctO.setTempLmt(acct.getTempLmt());
		acctO.setTempLmtBegDate(acct.getTempLmtBegDate());
		acctO.setTempLmtEndDate(acct.getTempLmtEndDate());
		acctO.setOvrlmtRate(acct.getOvrlmtRate());
		acctO.setCashLmtRate(acct.getCashLmtRate());
		acctO.setLoanLmtRate(acct.getLoanLmtRate());
		acctO.setCurrBal(acct.getCurrBal());
		acctO.setCashBal(acct.getCashBal());
		acctO.setLoanBal(acct.getLoanBal());
		acctO.setDisputeAmt(acct.getDisputeAmt());
		acctO.setBlockCode(acct.getBlockCode());
		acctO.setMemoCash(acct.getMemoCash());
		acctO.setMemoCr(acct.getMemoCr());
		acctO.setMemoDb(acct.getMemoDb());
		acctO.setOwningBranch(acct.getOwningBranch());
		acctO.setCycleDay(acct.getCycleDay());
		acctO.setSmsInd(SmsInd.Y);
		acctO.setUserSmsAmt(null);// 非0，个性化可定义为0，与null有区别
		acctO.setFloatRate(BigDecimal.ZERO);
		acctO.setLastSyncDate(acct.getLastSyncDate());//账户同步日期

		acctO.setContrNbr(acct.getContrNbr());
		acctO.setCustSource(acct.getCustSource());

		return acctO;
	}

	/**
	 * inputValue > origValue > defaultValue
	 * 
	 * @param origValue
	 * @param inputValue
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDefaultValue(Object inputValue, Object origValue,
			Object defaultValue, Class<T> clazz) {
		Object o;
		if (inputValue != null && StringUtils.isNotBlank(inputValue.toString())) {
			o = inputValue;
		} else {
			if (origValue == null) {
				o = defaultValue;
			} else {
				o = origValue;
			}
		}
		return (T) o;
	}

	/**
	 * 
	 * @see 方法名：mergeCard
	 * @see 描述：创建卡片
	 * @see 创建日期：20158月14日下午6:40:39
	 * @author lizz
	 * 
	 * @param req
	 * @param product
	 * @param productCredit
	 * @param cust
	 * @param acct
	 * @return
	 * @throws ParseException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void mergeCard(TxnContext context) {
		LogTools.printObj(logger, "", "创建卡片mergeCard");
		TxnInfo txnInfo = context.getTxnInfo();
		Product product = context.getProduct();
		ProductCredit productCredit = context.getProductCredit();
		CcsCustomer customer = context.getCustomer();
		CcsAcct acct = context.getAccount();
		QCcsCard qCcsCard = QCcsCard.ccsCard;
		CcsCard card = rCcsCard.findOne(qCcsCard.acctNbr.eq(acct.getAcctNbr()));
//		QCcsCardO qCcsCardO = QCcsCardO.ccsCardO;
		CcsCardO cardO = null ;

		if (null == card) {

			String cardno = dealWithCardNbr(txnInfo, product);
			card = new CcsCard();

			card.setOrg(txnInfo.getOrg());
			card.setLogicCardNbr(cardno);
			card.setCustId(customer.getCustId());
			card.setAcctNbr(acct.getAcctNbr());
			card.setProductCd(acct.getProductCd());
			card.setApplNbr("0");
			card.setBarcode("");
			card.setBscSuppInd(BscSuppIndicator.B);
			card.setCardBasicNbr(cardno);
			card.setOwningBranch("000000001");
			card.setApplPromoCode("");
			card.setRecomName("");
			card.setRecomCardNo("");
			card.setSetupDate(txnInfo.getBizDate());

			if (product.fabricationInd == Indicator.N) {
				card.setActiveInd(Indicator.Y);
				card.setActiveDate(txnInfo.getBizDate());
			} else {
				card.setActiveInd(Indicator.N);
			}
			card.setLastestMediumCardNbr(cardno);
			card.setSalesInd(Indicator.N);
			card.setApplSrc(customer.getCustSource().toString());
			card.setRepresentName("");
			card.setPosPinVerifyInd(Indicator.Y);
			// 新卡有效期 = batchDay + PRODUCT.NEW_CARD_VALID_PRD(卡管送入)
			DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
			Date expireDate = new Date();
			try {
				expireDate = dateFormat1.parse("9999-01-01");
			} catch (ParseException e) {
				// 暂不处理异常，虚拟卡
			}
			card.setCardExpireDate(expireDate);
			// 若开卡即收取年费
			if (productCredit.fee.firstCardFeeInd == FirstCardFeeInd.I) {
				card.setNextCardFeeDate(new Date());
			}
			card.setCardFeeRate(BigDecimal.ZERO);
			card.setRenewInd(RenewInd.D);
			card.setFirstUsageFlag(FirstUsageIndicator.A);
			card.setWaiveCardfeeInd(Indicator.N);
			card.setCardDeliverMethod(CardFetchMethod.A);

			logger.info("---------------------新建卡片----------------------");
			card = rCcsCard.save(card);
			logger.debug("新建卡片, LogicalCardNo后四位="
					+ CodeMarkUtils.subCreditCard(card.getLogicCardNbr()));

			// 建立对应的统计对象
			CcsCardUsage cardSt = new CcsCardUsage();

			cardSt.setOrg(card.getOrg());
			cardSt.setLogicCardNbr(card.getLogicCardNbr());
			cardSt.setCtdRetailAmt(BigDecimal.ZERO);
			cardSt.setCtdRetailCnt(0);
			cardSt.setCtdCashAmt(BigDecimal.ZERO);
			cardSt.setCtdCashCnt(0);
			cardSt.setMtdRetailAmt(BigDecimal.ZERO);
			cardSt.setMtdRetailCnt(0);
			cardSt.setMtdCashAmt(BigDecimal.ZERO);
			cardSt.setMtdCashCnt(0);
			cardSt.setYtdRetailAmt(BigDecimal.ZERO);
			cardSt.setYtdRetailCnt(0);
			cardSt.setYtdCashAmt(BigDecimal.ZERO);
			cardSt.setYtdCashCnt(0);
			cardSt.setLtdRetailAmt(BigDecimal.ZERO);
			cardSt.setLtdRetailCnt(0);
			cardSt.setLtdCashAmt(BigDecimal.ZERO);
			cardSt.setLtdCashCnt(0);
			cardSt.setLastCycleRetailAmt(BigDecimal.ZERO);
			cardSt.setLastCycleRetailCnt(0);
			cardSt.setLastCycleCashAmt(BigDecimal.ZERO);
			cardSt.setLastCycleCashCnt(0);
			cardSt.setLastMthRetlAmt(BigDecimal.ZERO);
			cardSt.setLastMthRetlCnt(0);
			cardSt.setLastMthCashAmt(BigDecimal.ZERO);
			cardSt.setLastMthCashCnt(0);
			cardSt.setLastYearRetlAmt(BigDecimal.ZERO);
			cardSt.setLastYearRetlCnt(0);
			cardSt.setLastYearCashAmt(BigDecimal.ZERO);
			cardSt.setLastYearCashCnt(0);

			logger.info("----------------------插入逻辑卡统计表-------------------");
			cardSt = rCcsCardUsage.save(cardSt);

			CcsCustomerCrlmt custLimit = context.getCustLimitO();

			cardO = new CcsCardO();
			

			cardO.setOrg(card.getOrg());
			cardO.setLogicCardNbr(card.getLogicCardNbr());
			cardO.setAcctNbr(card.getAcctNbr());
			cardO.setCustId(card.getCustId());
			cardO.setCustLmtId(custLimit.getCustLmtId());
			cardO.setProductCd(card.getProductCd());
			cardO.setBscSuppInd(card.getBscSuppInd());
			cardO.setCardBasicNbr(card.getCardBasicNbr());
			cardO.setActiveInd(card.getActiveInd());
			cardO.setPosPinVerifyInd(card.getPosPinVerifyInd());
			cardO.setPinTries(0);
			cardO.setBlockCode(card.getBlockCode());
			// 以下6项额度都是持卡人自己设置的，没有参数，默认赋值逻辑如下，持卡人如果有特殊需要，可以通过客服修改
			cardO.setCycleRetailLmt(DEFAULT_LIMIT);
			cardO.setCycleCashLmt(DEFAULT_LIMIT);
			cardO.setCycleNetLmt(DEFAULT_LIMIT);
			cardO.setTxnLmt(DEFAULT_LIMIT);
			cardO.setTxnCashLmt(DEFAULT_LIMIT);
			cardO.setTxnNetLmt(DEFAULT_LIMIT);
			cardO.setDayUsedAtmNbr(0);
			cardO.setDayUsedAtmAmt(BigDecimal.ZERO);
			cardO.setDayUsedRetailNbr(0);
			cardO.setDayUsedRetailAmt(BigDecimal.ZERO);
			cardO.setDayUsedCashNbr(0);
			cardO.setDayUsedCashAmt(BigDecimal.ZERO);
			cardO.setDayUsedXfroutNbr(0);
			cardO.setDayUsedXfroutAmt(BigDecimal.ZERO);
			cardO.setCtdUsedAmt(BigDecimal.ZERO);
			cardO.setCtdCashAmt(BigDecimal.ZERO);
			cardO.setCtdNetAmt(BigDecimal.ZERO);
			cardO.setInqPinTries(0);
			cardO.setDayUsedAtmCupxbAmt(BigDecimal.ZERO);

			logger.info("--------------------插入逻辑卡表-授权----------------");
			cardO = rCcsCardO.save(cardO);

			CcsCardLmMapping map = new CcsCardLmMapping();

			map.setOrg(card.getOrg());
			map.setCardNbr(card.getLogicCardNbr());
			map.setLogicCardNbr(card.getLogicCardNbr());

			logger.info("-------------------- 创建介质卡号逻辑卡号映射----------------");
			map = rCcsCardLmMapping.save(map);

		}else {
			QCcsCardO qCcsCardO = QCcsCardO.ccsCardO;
			cardO = rCcsCardO.findOne(qCcsCardO.acctNbr.eq(acct.getAcctNbr()));
		}
		// 账户上设置卡号
		acct.setDefaultLogicCardNbr(card.getLogicCardNbr());

		context.setCard(card);
		context.setCardo(cardO);
		context.getTxnInfo().setCardNo(card.getLogicCardNbr());
	}

	/**
	 * 
	 * @see 方法名：dealWithCardNbr
	 * @see 描述：获得卡号
	 * @see 创建日期：2015年8月14日下午7:11:49
	 * @author lizz
	 * @param product
	 * 
	 * @param afi
	 * @param bp
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public String dealWithCardNbr(TxnInfo txnInfo, Product product) {
		// 如果上送文件中有卡号 ，则不生成新卡号
		String cardNo = "";
		if (logger.isDebugEnabled())
			logger.debug("生成卡号开始，产品{},org{}", product.productCode,
					txnInfo.getOrg());
//		CcsCardnbrGrt config = getNextCardNbr(product, txnInfo.getOrg());
//		cardNo = formatCardNbr(config, product);

//		BigDecimal currValue = (BigDecimal)em.createNativeQuery("select CCS_CARDNBR_GRT_SEQ.nextval from dual ").getSingleResult();
//		cardNo = formatCardNbr(currValue, product);
		Long currValue = cardNbrServiceImpl.getCardNbr(product.productCode);
		if (currValue.longValue() > getMaxCardNbr(product)) {
			throw new ProcessException("卡号已用完，无法申请新卡号");
		}
		cardNo = formatCardNbr(currValue, product);

		return cardNo;
	}

	/**
	 * 
	 * @see 方法名：getNextCardNbr
	 * @see 描述：生成下一个卡号
	 * @see 创建日期：2015年8月14日上午11:28:36
	 * @author lizz
	 * 
	 * @param product
	 * @param org
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional(rollbackFor = Exception.class)
	public synchronized CcsCardnbrGrt getNextCardNbr(Product product, String org)
			throws ProcessException {
		QCcsCardnbrGrt q = QCcsCardnbrGrt.ccsCardnbrGrt;
		JPAUpdateClause jpu = new JPAUpdateClause(em, q);
		jpu.where(q.org.eq(org).and(q.productCd.eq(product.productCode))).set(
				q.currValue, q.currValue.add(1));
		jpu.execute();
		JPAQuery query = new JPAQuery(em);
		CcsCardnbrGrt result = query.from(q)
				.where(q.org.eq(org).and(q.productCd.eq(product.productCode)))
				.singleResult(q);
		if (result != null) {
			em.refresh(result);
		}

		if (result == null) {
			result = new CcsCardnbrGrt();
			result.setOrg(org);
			result.setProductCd(product.productCode);
			result.setCurrValue(Long.parseLong(product.cardnoRangeFlr));
			em.persist(result);
			return result;
		}
		if (result.getCurrValue().longValue() > getMaxCardNbr(product)) {
			throw new ProcessException("卡号已用完，无法申请新卡号");
		}
		return result;
	}

	/**
	 * 
	 * @see 方法名：getMaxCardNbr
	 * @see 描述：获得产品对应最大卡号
	 * @see 创建日期：2015年8月14日上午11:29:10
	 * @author lizz
	 * 
	 * @param product
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public long getMaxCardNbr(Product product) {
		String ceil = product.cardnoRangeCeil;
		return Long.parseLong(ceil);
	}

	/**
	 * 
	 * @see 方法名：formatCardNbr
	 * @see 描述：生成格式化的卡号
	 * @see 创建日期：2015年8月17日
	 * @author lizz
	 * 
	 * @param config
	 * @param product
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public String formatCardNbr(CcsCardnbrGrt config, Product product) {
		DecimalFormat df = new DecimalFormat("#"
				+ this.getStringForLength('0', product.cardnoRangeFlr.length()));
		StringBuffer cardNbr = new StringBuffer(product.bin);
		cardNbr.append(df.format(config.getCurrValue()));

		char luhnModNum = LuhnMod10.getDigit(cardNbr.toString());
		if (logger.isDebugEnabled())
			logger.debug("卡号={}", "" + cardNbr + luhnModNum);
		return cardNbr.append(luhnModNum).toString();
	}
	
    private String formatCardNbr(Long currValue, Product product) {
        DecimalFormat df = new DecimalFormat("#" + this.getStringForLength('0', product.cardnoRangeFlr.length()));
        StringBuffer cardNbr = new StringBuffer(product.bin);
        cardNbr.append(df.format(currValue));
       
        char luhnModNum = LuhnMod10.getDigit(cardNbr.toString());
        if(logger.isDebugEnabled())
             logger.debug("卡号={}", "" + cardNbr + luhnModNum);
        return cardNbr.append(luhnModNum).toString();
   }


	/**
	 * 
	 * @see 方法名：getStringForLength
	 * @see 描述：获得给定长度字符串
	 * @see 创建日期：2015年8月17日
	 * @author lizz
	 * 
	 * @param c
	 * @param length
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public StringBuilder getStringForLength(char c, int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(c);
		}
		return sb;
	}

	/**
	 * 产生唯一流水号
	 * 
	 * @return
	 */
	public String generateFlowNo() {
		DateFormat df = new SimpleDateFormat("yyDS");
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		StringBuffer sb = new StringBuffer(df.format(c.getTime()));
		sb.append(hour * 60 * 60 + minute * 60 + second);
		return sb.substring(1);
	}

	/**
	 * 保存贷款注册信息
	 * @param context
	 */
	public void mergeLoanReg(TxnContext context) {   
		LogTools.printObj(logger, "", "保存贷款注册信息mergeLoanReg");
		
		CcsCardO card = context.getCardo();
		CcsAcct acct = context.getAccount();
		LoanPlan loanPlan = context.getLoanPlan();
		LoanFeeDef loanFeeDef = context.getLoanFeeDef();
		TxnInfo txnInfo = context.getTxnInfo();

		String refNo = txnInfo.getRefNbr();//内部流水号  b037

		CcsLoanReg loanReg = genLoanReg(acct,loanPlan,loanFeeDef,
					Integer.valueOf(txnInfo.getCdTerms()),refNo,txnInfo.getTransAmt(),
					card.getCardBasicNbr(),card.getCardBasicNbr(),txnInfo.getBizDate(),
					txnInfo.getJionLifeInsuInd(),txnInfo.getDueBillNo(),txnInfo.getServiceSn());
//		loanReg.setLoanRegStatus(LoanRegStatus.A);//
		loanReg.setPremiumAmt(txnInfo.getPremiumAmt());
		loanReg.setPremiumInd(txnInfo.getPremiumInd());

		loanReg = rCcsLoanReg.save(loanReg);
		
		context.getTxnInfo().setDueBillNo(loanReg.getDueBillNo());
		context.setLoanReg(loanReg);
	}
	/**
	 * 生成贷款注册信息
	 * 提供开户 提现 还款试算统一公函
	 * @param context
	 */
	public CcsLoanReg genLoanReg(CcsAcct acct,LoanPlan loanPlan,LoanFeeDef loanFeeDef,
				Integer terms ,String refNo,BigDecimal transAmt,
				String logicCardNbr,String cardBasicNbr,Date bizDate,
				Indicator jionLifeInsuInd,String dueBillNo,String serviceSn){
		CcsLoanReg loanReg = appapiCommService.genLoanReg(terms, 
				transAmt, refNo, logicCardNbr,cardBasicNbr, acct.getAcctNbr().longValue(),
				acct.getAcctType(), loanPlan.loanCode, bizDate);

		loanReg.setInterestRate(null==loanFeeDef.interestRate?BigDecimal.ZERO:loanFeeDef.interestRate);//基础利率
		//账户协议利率，且账户协议利率有效期大于等于当前业务日期，则使用协议利率
		if(null !=acct.getInterestRate() && acct.getInterestRate().compareTo(BigDecimal.ZERO)>=0
//				&& acct.getAgreementRateExpireDate().compareTo(txnInfo.getBizDate())>=0
				&& Indicator.Y.equals(acct.getAgreementRateInd())){
			loanReg.setInterestRate(acct.getInterestRate());
		}else if(null==loanFeeDef.interestRate){
			throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",基准利率为空");
		}
		if(logger.isDebugEnabled())
			logger.debug("协议利率为[{}],loanReg基础利率为[{}]",acct.getInterestRate(),loanReg.getInterestRate());
		
		loanReg.setLoanFeeDefId(loanFeeDef.loanFeeDefId.toString());//分期子产品编号
		loanReg.setLoanFeeMethod(loanFeeDef.installmentFeeMethod);//分期手续费收取方式
		loanReg.setLifeInsuFeeMethod(loanFeeDef.lifeInsuFeeMethod);//寿险计划包费收取方式
		loanReg.setStampdutyMethod(loanFeeDef.stampMethod);//印花税率收取方式
		loanReg.setSvcfeeMethod(loanFeeDef.loanFeeMethod);
		loanReg.setReplaceSvcFeeMethod(loanFeeDef.replaceFeeMethod);//代收服务费收取方式
		loanReg.setPrepayPkgFeeMethod(loanFeeDef.prepayPkgFeeMethod);//灵活还款服务包收取方式
		//如果产品未配置保费收取方式 按照默认
		if (loanFeeDef.insCollMethod!=null) {
			loanReg.setLoanInsFeeMethod(loanFeeDef.insCollMethod);
		}
		//寿险计划包
		loanReg.setJoinLifeInsuInd(null==jionLifeInsuInd?Indicator.N:jionLifeInsuInd);
		
		//设置协议费率
		loanReg = this.setLoanRegRateByAcct(loanReg, acct, loanFeeDef);
		
		//分期总寿险计划包费--随借随还寿险计划包费为每日累计，联机建立loan_reg时，TOT_LIFE_INSU_AMT 不应赋值 by lizz 20151218 mantis0000877
		if(loanPlan.loanType.equals(LoanType.MCAT)) {
			//随借随还
			loanReg.setTotLifeInsuAmt(BigDecimal.ZERO);
		}else {
			if(loanReg.getJoinLifeInsuInd() == Indicator.Y
					&& null != loanReg.getLifeInsuFeeMethod()){
				loanReg.setTotLifeInsuAmt(LoanLifeInsuFeeMethodimple.valueOf(loanReg.getLifeInsuFeeMethod().toString()).loanLifeInsuFee(loanReg, loanFeeDef,0));
			}else{
				loanReg.setTotLifeInsuAmt(BigDecimal.ZERO);
			}
		}
		//分期总灵活还款包费计算
		if (loanPlan.loanType.equals(LoanType.MCAT)) {
			//随借随还
			loanReg.setTotPrepayPkgAmt(BigDecimal.ZERO);
		}else {
			if(loanReg.getPrepayPkgInd() == Indicator.Y
					&& null != loanReg.getPrepayPkgInd()){
				loanReg.setTotPrepayPkgAmt(PrepayPkgMethodimple.valueOf(loanReg.getPrepayPkgFeeMethod().toString()).loanPrepayPkg(loanReg, loanFeeDef,0));
			}else{
				loanReg.setTotPrepayPkgAmt(BigDecimal.ZERO);
			}
		
		}
		//贷款服务费
		if(loanPlan.loanType.equals(LoanType.MCAT)) {
			//随借随还
			loanReg.setLoanSvcFee(BigDecimal.ZERO);
		}else {
			if(null != loanReg.getSvcfeeMethod()){
				loanReg.setLoanSvcFee(LoanSVCFeeMethodimple.valueOf(loanReg.getSvcfeeMethod().toString()).loanSVCFee(loanReg,loanFeeDef,0));
			}else{
				loanReg.setLoanSvcFee(BigDecimal.ZERO);
			}
		}
		//贷款（分期）手续费
		if(loanPlan.loanType.equals(LoanType.MCAT)) {
			//随借随还
			loanReg.setLoanInitFee(BigDecimal.ZERO);
		}else {
			if(null != loanReg.getLoanFeeMethod()){
				loanReg.setLoanInitFee(LoanInstallmentFeeMethodimple.valueOf(loanReg.getLoanFeeMethod().toString()).loanInstallmentFee(loanReg, loanFeeDef,0));
			}else{
				loanReg.setLoanInitFee(BigDecimal.ZERO);
			}
		}
		//总印花税
		if(loanPlan.loanType.equals(LoanType.MCAT)) {
			//随借随还
			loanReg.setStampdutyAmt(BigDecimal.ZERO);
		}else {
			if(null != loanReg.getStampdutyMethod()){
				loanReg.setStampdutyAmt(LoanStampTAXMethodimple.valueOf(loanReg.getStampdutyMethod().toString()).loanStampTAX(loanReg, loanFeeDef,0));
			}else{
				loanReg.setStampdutyAmt(BigDecimal.ZERO);
			}
		}
		//贷款服务费
		if(loanPlan.loanType.equals(LoanType.MCAT)){
			loanReg.setLoanSvcFee(BigDecimal.ZERO);
		}else{
			if(null != loanReg.getSvcfeeMethod()){
				loanReg.setLoanSvcFee(LoanSVCFeeMethodimple.valueOf(loanReg.getSvcfeeMethod().toString()).loanSVCFee(loanReg,loanFeeDef,0));
			}else{
				loanReg.setLoanSvcFee(BigDecimal.ZERO);
			}
		}
		//代收服务费
		loanReg.setTotReplaceSvcFee(BigDecimal.ZERO);
		if(loanPlan.loanType.equals(LoanType.MCAT)){
			loanReg.setTotReplaceSvcFee(BigDecimal.ZERO);
		}else{
			if(null != loanReg.getReplaceSvcFeeMethod()){
				loanReg.setTotReplaceSvcFee(LoanReplaceSvcFeeMethodimple.valueOf(loanReg.getReplaceSvcFeeMethod().toString()).loanReplaceSvcFee(loanReg,loanFeeDef,0));
			}else{
				loanReg.setTotReplaceSvcFee(BigDecimal.ZERO);
			}
		}
		
		//保费
		if(loanPlan.loanType.equals(LoanType.MCAT)) {
			//随借随还
			loanReg.setInsuranceAmt(BigDecimal.ZERO);
		}else {
			if(null != loanReg.getInsuranceAmt()){
				loanReg.setInsuranceAmt(LoanlnsuranceMethodimple.valueOf(loanReg.getLoanInsFeeMethod().toString()).loanlnsurance(loanReg,loanFeeDef,0));
			}else{
				loanReg.setInsuranceAmt(BigDecimal.ZERO);
			}
		}
		
		loanReg.setLoanType(loanPlan.loanType);
		loanReg.setContrNbr(acct.getContrNbr());
		
		//有借据号,则直接使用借据号
		if(StringUtils.isBlank(dueBillNo)){
			loanReg.setDueBillNo(refNo);
		}else{
			loanReg.setDueBillNo(dueBillNo);
		}
		return loanReg;
	}
	/**
	 * 使用账户的费率数据设置loanReg
	 * @param loanReg
	 * @param acct
	 * @param loanFeeDef
	 * @param agreeInd
	 * @return
	 */
	public CcsLoanReg setLoanRegRateByAcct(CcsLoanReg loanReg,CcsAcct acct,LoanFeeDef loanFeeDef){
		//使用协议费率，这些费率都取loanreg中的   20151127不修正为0，直接赋值
		loanReg.setAgreementRateInd(acct.getAgreementRateInd()==null?Indicator.N:acct.getAgreementRateInd());
		//贷款服务费率
		loanReg.setFeeRate(acct.getFeeRate());
		//贷款服务金额
		loanReg.setFeeAmt(acct.getFeeAmt());
		//寿险费率
		loanReg.setLifeInsuFeeRate(acct.getLifeInsuFeeRate());
		//寿险固定金额
		loanReg.setLifeInsuFeeAmt(acct.getLifeInsuFeeAmt());
		//保费月费率
		loanReg.setInsuranceRate(acct.getInsuranceRate());
		//保费月固定金额
		loanReg.setInsAmt(acct.getInsAmt());
		//分期手续费率
		loanReg.setInstallmentFeeRate(acct.getInstallmentFeeRate());
		//分期手续费固定金额
		loanReg.setInstallmentFeeAmt(acct.getInstallmentFeeAmt());
		//提前还款包费率
		loanReg.setPrepayPkgFeeRate(acct.getPrepayPkgFeeRate());
		//提前还款包固定金额
		loanReg.setPrepayPkgFeeAmt(acct.getPrepayPkgFeeAmt());
		//罚息利率
		loanReg.setPenaltyRate(acct.getPenaltyRate());
		//复利利率
		loanReg.setCompoundRate(acct.getCompoundRate());
		//印花税费率
		loanReg.setStampdutyRate(acct.getStampdutyRate());
		//印花税费率
		loanReg.setStampAmt(acct.getStampAmt());
		//代收服务费固定金额
		loanReg.setReplaceSvcFeeAmt(acct.getReplaceSvcFeeAmt());
		//代收服务费费率
		loanReg.setReplaceSvcFeeRate(acct.getReplaceSvcFeeRate());
		//代收罚息利率
		loanReg.setReplacePenaltyRate(acct.getReplacePenaltyRate());
		//购买灵活还款服务标示
		loanReg.setPrepayPkgInd(acct.getPrepayPkgInd());
		
		return loanReg;
	}
	
	/**
	 * 获取产品参数
	 * 
	 * @param context
	 */
	public void getProdParam(TxnContext context) {
		if (logger.isDebugEnabled())
			logger.debug("获取产品参数--getProdParam");
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcct ccsAcct = context.getAccount();
		try {
			if(StringUtils.isEmpty(txnInfo.getLoanCode())){
				// 没有贷款产品代码，根据账户产品默认贷款产品号获取贷款产品
				if(logger.isDebugEnabled())
					logger.debug("没有贷款产品代码，根据账户产品[{}]默认贷款产品号获取贷款产品",ccsAcct.getProductCd());
				ProductCredit productCredit = unifiedParameterFacility.loadParameter(ccsAcct.getProductCd(), ProductCredit.class);
				txnInfo.setLoanCode(productCredit.loanPlansMap.get(productCredit.defaultLoanType));
			}
			if(logger.isDebugEnabled())
				logger.debug("贷款产品代码[{}]",txnInfo.getLoanCode());
			
			// 根据贷款产品代码，反向获取账户产品
			LoanPlan loanPlan;
			ProductCredit productCredit;
			Product product;
			AccountAttribute acctAttr;
			try{
				loanPlan = unifiedParamFacilityProvide.loanPlan(txnInfo.getLoanCode());
				productCredit = unifiedParameterFacility.loadParameter(loanPlan.productCode, ProductCredit.class);
				product = unifiedParameterFacility.loadParameter(loanPlan.productCode, Product.class);
				acctAttr = unifiedParameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
			}catch(Exception e){
				throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+",找不到贷款产品");
			}
			LoanFeeDef loanFeeDef = null;
			
			//贷款产品号与账户产品上的默认分期类型对应的贷款产品号不一致，异常
			if(!StringUtils.equals(loanPlan.loanCode, productCredit.loanPlansMap.get(productCredit.defaultLoanType))){
				throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage()+",账户产品未配置默认贷款产品"+loanPlan.loanCode);
			}
			
			try {
				if(StringUtils.isNotBlank(txnInfo.getLoanFeeDefId())){
					loanFeeDef = unifiedParamFacilityProvide.loanFeeDefByKey(
							product.productCode, loanPlan.loanType, Integer.valueOf(txnInfo.getLoanFeeDefId()));
				}else{
					if (loanPlan.loanType == LoanType.MCAT) {
						loanFeeDef = unifiedParamFacilityProvide.loanFeeDefMCAT(txnInfo.getLoanCode());
					} else {
						loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(
								txnInfo.getLoanCode(), Integer.valueOf(txnInfo.getCdTerms()), txnInfo.getTransAmt());
					}
				}
				if (null == loanFeeDef) {
					throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage() + ",找不到贷款产品定价");
				}
				// 随借随还 期数去产品定价上的期数
				txnInfo.setCdTerms(loanFeeDef.initTerm);
			} catch (Exception e) {
				throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage() + ",找不到贷款产品定价");
			}

			txnInfo.setProductCd(product.productCode);
			context.setProduct(product);
			context.setProductCredit(productCredit);
			context.setAccAttr(acctAttr);
			context.setLoanPlan(loanPlan);
			context.setLoanFeeDef(loanFeeDef);
			if(logger.isDebugEnabled())
				logger.debug("accountAttributeId --------- " + productCredit.accountAttributeId);
		}catch(ProcessException pe){
			if (logger.isErrorEnabled())
				logger.error("获取产品参数异常,贷款产品号[" + txnInfo.getLoanCode() + "]", pe);
			throw pe;			
		}catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error("获取产品参数异常,贷款产品号[" + txnInfo.getLoanCode() + "]", e);
			throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage());
		}

	}
	
	/**
	 * 生成合同号
	 * 合同编码规则（共计 18位）： 
	 * 产品群代码（4位） + 时间（年月日 6位）+ 流水号（8位）  
	 * 其中 时间（年-后2位 ，月-2位，日-2位）
	 * MS+业务日期+贷款产品+账号
	 * @param acctNbr 账号
	 * @param bizDate 业务日期
	 * @param loanCode 贷款产品
	 * @return
	 */
	public String genContrNbr(Long acctNbr, Date bizDate, String loanCode){
		String seq = acctNbr.toString();
		StringBuffer strBuf = new StringBuffer(loanCode);
		strBuf.append(DateUtils.formatDate2String(bizDate, DateUtils.YYMMDD));
		if(seq.length()<=8){
			strBuf.append(String.format("%08d", acctNbr));
		}else{
			strBuf.append(seq.substring(seq.length()-8,seq.length()));
		}
		
		if(logger.isDebugEnabled())
			logger.debug("生产合同号 --------- " + strBuf.toString());
		return strBuf.toString();
	}
	
//	public static void main(String[] args) {
//		Long acctNbr = new Long("329");
//		String seq = acctNbr.toString();
//		StringBuffer strBuf = new StringBuffer("1101");
//		strBuf.append(DateUtils.formatDate2String(new Date(), DateUtils.YYMMDD));
//		if(seq.length()<=8){
//			strBuf.append(String.format("%08d", acctNbr));
//		}else{
//			strBuf.append(seq.substring(seq.length()-8,seq.length()));
//		}
//		
//		System.out.println(strBuf);
//	}
	
	/**
	 * 获取合同有效期和协议利率有效期
	 * @param context
	 */
	public void getExpireDate(TxnContext context){
		TxnInfo txnInfo = context.getTxnInfo();
		
		//没有传入合同有效期,用产品还款间隔及期数计算
		if(null == txnInfo.getContraExpireDate()){
	        
			Date expireDate = this.getExpireDateByTerm(context);
	        txnInfo.setContraExpireDate(expireDate);
		}
		
		if(logger.isDebugEnabled())
			logger.debug("协议利率有效期[{}]，协议利率[{}]",txnInfo.getAgreeRateExpireDate(),txnInfo.getAgreeRate());
		if(null != txnInfo.getAgreeRate() 
				&& txnInfo.getAgreeRate().compareTo(BigDecimal.ZERO)>0
				&& null == txnInfo.getAgreeRateExpireDate()){
			txnInfo.setAgreeRateExpireDate(txnInfo.getContraExpireDate());
			if(logger.isDebugEnabled())
				logger.debug("协议利率存在，但协议利率有效期为空，修正为合同有效期[{]]",txnInfo.getContraExpireDate());
		}
	}

	/**
	 * 根据期数获取合同有效期
	 * @param context
	 * @return
	 */
	public Date getExpireDateByTerm(TxnContext context) {
		TxnInfo txnInfo = context.getTxnInfo();
		return getExpireDateByTerm(context.getLoanFeeDef(),txnInfo.getBizDate(),txnInfo.getCdTerms());
	}
	
	/**
	 * 根据期数获取合同有效期
	 * @param context
	 * @return
	 */
	public Date getExpireDateByTerm(LoanFeeDef loanFeeDef,Date bizDate,Integer terms) {
		Integer intervalMonth = null;
		try{
			intervalMonth = microCreditRescheduleUtils.getIntervalMonth(loanFeeDef);
		}catch(Exception e){
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage()+",不支持的还款间隔单位");
		}
		Date expireDate = DateUtils.addMonths(bizDate, intervalMonth*terms);
		
		if(logger.isDebugEnabled())
			logger.debug("业务日期[{}]，合同有效期{}]",bizDate,expireDate);
		return expireDate;
	}

	
	/**
	 * 老客户申请，uuid和证件号/证件类型不匹配
	 * @param context
	 */
	public void checkCustomer(TxnContext context){
		TxnInfo txnInfo = context.getTxnInfo();
		
		CcsCustomer customer1 = appapiCommService.loadCustomerByIdNo(context, false);
		CcsCustomer customer2 = appapiCommService.loadCustomerByUuid(context, false);
		
		if(null != customer1){
			if (logger.isDebugEnabled())
				logger.debug("老客户byid-- idType:[{}],certid:[{}],custId[{}],internalCustomerId[{}]",
						customer1.getIdType(), customer1.getIdNo(),
						customer1.getCustId(), customer1.getInternalCustomerId());
			if(!StringUtils.equals(customer1.getInternalCustomerId(),txnInfo.getUuid())){
				throw new ProcessException(MsRespCode.E_1061.getCode(), MsRespCode.E_1061.getMessage());
			}
		}
		
		if(null != customer2){
			if (logger.isDebugEnabled())
				logger.debug("老客户byuuid idType:[{}],certid:[{}],custId[{}],internalCustomerId[{}]",
						customer2.getIdType(), customer2.getIdNo(),
						customer2.getCustId(), customer2.getInternalCustomerId());
			if(!StringUtils.equals(customer2.getIdNo(),txnInfo.getIdNo())
					|| customer2.getIdType() != txnInfo.getIdType()){
				throw new ProcessException(MsRespCode.E_1061.getCode(), MsRespCode.E_1061.getMessage());
			}
		}
		
		//设置客户号
		if(null != customer1 && null != customer2){
			txnInfo.setCustId(customer1.getCustId());
		}
		
	}
	
}
