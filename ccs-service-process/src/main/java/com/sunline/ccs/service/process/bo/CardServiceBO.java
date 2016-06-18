package com.sunline.ccs.service.process.bo;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctNbr;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardLmMapping;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardUsage;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.server.repos.RCcsEmployee;
import com.sunline.ccs.infrastructure.server.repos.RCcsLinkman;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsPostingTmp;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnReject;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnSeq;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctNbr;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsEmployee;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnReject;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnSeq;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsEmployee;
import com.sunline.ccs.infrastructure.shared.model.QCcsLinkman;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.FirstCardFeeInd;
import com.sunline.ccs.param.def.enums.FirstUsageIndicator;
import com.sunline.ccs.param.def.enums.LimitType;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ccs.service.util.U0101SetupLoanP;
import com.sunline.ccs.service.util.U0101SetupLoanP.LoanRegInfo;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.AppRejectReason;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.CorpStructure;
import com.sunline.ppy.dictionary.enums.DualBillingInd;
import com.sunline.ppy.dictionary.enums.EmpPositionAttrType;
import com.sunline.ppy.dictionary.enums.EmpType;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.enums.ProductType;
import com.sunline.ppy.dictionary.enums.Relationship;
import com.sunline.ppy.dictionary.enums.RenewInd;
import com.sunline.ppy.dictionary.enums.SmsInd;
import com.sunline.ppy.dictionary.enums.TitleOfTechnicalType;
import com.sunline.ppy.dictionary.exchange.ApplyFileItem;
import com.sunline.ppy.dictionary.exchange.VCardApplyResponseItem;
import com.sunline.ppy.dictionary.report.ccs.ApplyResponseRptItem;
//import com.sunline.smsd.service.sdk.ApplyResponseSMItem;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

@Component
public class CardServiceBO {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final BigDecimal DEFAULT_LIMIT = new BigDecimal("9999999999999");
    private static final String DEFAULT_AUTH_CODE = "000000";
    private static final String BLOCK_CODE_C = "C";
    private static final String BLOCK_CODE_P = "P";
    @Autowired
    private UnifiedParameterFacility unifiedParameterFacility;
/*    @Autowired
    private DownMsgFacility downMsgFacility;
*/    @Autowired
    private FetchSmsNbrFacility fetchMsgCdService;
    @Autowired
    private RCcsCardThresholdCtrl rCcsCardLmtOverideO;
    @Autowired
    CustAcctCardFacility queryFacility;
    @Autowired
    private RCcsCardLmMapping rCcsCardLmMapping;
    @Autowired
    private MmCardService mmCardService;
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private UnifiedParamFacilityProvide cpsUnifiedParameterFacilityProvide;
    @Autowired
    private RCcsCustomer rCustomer;
    @Autowired
    private RCcsCustomerCrlmt rCustomerCrLmt;
    @Autowired
    private RCcsAddress rAddress;
    @Autowired
    private RCcsLinkman rLinkman;
    @Autowired
    private RCcsAcct rAcct;
    @Autowired
    private RCcsAcctO rAcctO;
    @Autowired
    private RCcsCard rCcsCard;
    @Autowired
    private RCcsEmployee rCcsEmployee;
    @Autowired
    private UnifiedParameterFacility parameterFacility;
    @Autowired
    private BlockCodeUtils blockCodeUtils;
    @Autowired
    private TxnUtils txnUtils;
    @Autowired
    private U0101SetupLoanP u0001SetupLoanP;
    @Autowired
    private GlobalManagementService globalManagementService;
    @Autowired
    private RCcsCardUsage rCcsCardUsage;
    @Autowired
    private RCcsCardO rCcsCardO;
    @Autowired
    private RCcsAcctNbr rCcsAcctNbr;
    @Autowired
    private RCcsPostingTmp rCcsPostingTmp;
    @Autowired
    private RCcsLoanReg rCcsLoanReg;
    @Autowired
    private RCcsTxnReject rCcsTxnReject;
    @Autowired
    private RCcsTxnSeq rCcsTxnSeq;
    @Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;

    /**
     * @see 方法名：apply
     * @see 描述：卡片申请
     * @author songyanchao
     * 
     * @param app
     * @throws Exception
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public void apply(ApplyFileItem app) throws Exception {
	logger.debug("建客建账建卡");
	logger.debug("appNo ---------------------- " + app.appNo);
	logger.debug("productCd ------------------ " + app.productCd);
	logger.debug("cardNo --------------------- " + app.cardNo);

	try {
	    //S0101Setup info = new S0101Setup();

	    // 取机构号并设置上下文
	    OrganizationContextHolder.setCurrentOrg(app.org);
	    Organization organization = unifiedParameterFacility.loadParameter(null, Organization.class);

	    // 确定卡产品
	    ProductCredit productCredit = unifiedParameterFacility.loadParameter(app.productCd, ProductCredit.class);
	    Product product = unifiedParameterFacility.loadParameter(app.productCd, Product.class);

	    AccountAttribute acctAttr =
		    unifiedParameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
	    logger.debug("accountAttributeId --------- " + productCredit.accountAttributeId);

	    AccountAttribute dualAcctAttr = null;
	    if (productCredit.dualAccountAttributeId != null) {
		dualAcctAttr =
			unifiedParameterFacility.loadParameter(productCredit.dualAccountAttributeId,
							       AccountAttribute.class);
		logger.debug("dualAccountAttributeId ----- " + productCredit.dualAccountAttributeId);
	    }

	    // 确定客户
	    CcsCustomer customer = mergeCustomer(app);

	    // 确定地址
	    if (StringUtils.isNotBlank(app.homeAddress)) {
		this.mergeAddress(app.org, customer.getCustId(), AddressType.H, app.homeAddress, app.homeCity,
				  app.homeCountryCd, app.homeDistrict, app.homeState, app.homePhone, app.homeZip);
	    }

	    if (StringUtils.isNotBlank(app.companyAddress)) {
		this.mergeAddress(app.org, customer.getCustId(), AddressType.C, app.companyAddress, app.companyCity,
				  app.companyCountryCd, app.companyDistrict, app.companyState, app.companyPhone,
				  app.companyZip);
	    }

	    // 确定联系人, 附卡没有联系人
	    if (app.contactRelationship != null) {
		this.mergeLinkman(app.org, customer.getCustId(), app.contactRelationship, app.contactName,
				  app.contactGender, app.contactMobileNo, app.contactBirthday, app.contactCorpName,
				  app.contactIdType, app.contactIdNo, app.contactCorpPhone, app.contactCorpFax,
				  app.contactCorpPost);
	    }
	    if (app.contactORelationship != null) {
		this.mergeLinkman(app.org, customer.getCustId(), app.contactORelationship, app.contactOName, null,
				  app.contactOMobileNo, null, app.contactOCorpName, app.contactOIdType,
				  app.contactOIdNo, app.contactOCorpPhone, null, app.contactOCorpPost);
	    }

	    // 新建或更新工作信息
	    this.mergeEmployee(app.org, customer.getCustId(), app.companyName, app.companyPhone, app.companyFax,
			       app.title, app.industryCategory, app.companyCategory, app.titleOfTechnical,
			       app.revenuePerYear, app.familyAverageRevenue);

	    // 确定主卡客户
	    CcsCustomer basicCustomer;
	    CcsCustomerCrlmt custLimit;
	    Long acctNo;
	    CcsAcct account = null;
	    CcsAcct dualAccount = null;

	    if (app.bscSuppInd == BscSuppIndicator.B) {
		// 如果是主卡申请则取当前客户
		basicCustomer = customer;
		// 取主卡客户的额度信息
		custLimit = this.mergeCustomerCrLmt(app, product, acctAttr, customer, basicCustomer);
		// 取账号, 如果是共享额度账户，则acctNo取现有本币或外币的共享账号
		acctNo = this.mergeAcctNbr(app.org, basicCustomer, acctAttr);
		// 建账
		account = this.mergeAcct(acctNo, product, productCredit, acctAttr, customer, app);
		// 外币账户
		if (dualAcctAttr != null) {
		    dualAccount = this.mergeAcct(acctNo, product, productCredit, dualAcctAttr, customer, app);
		}
	    } else {
		// 确定主卡
		QCcsCard qCard = QCcsCard.ccsCard;
		CcsCard basicCard = rCcsCard.findOne(qCard.logicCardNbr.eq(app.primCardNo));
		if (basicCard == null) {
		    throw new RuntimeException(AppRejectReason.R12.getReasonDesc());
		}

		// 卡产品是否一致
		if (!app.productCd.equals(basicCard.getProductCd())) {
		    throw new RuntimeException(AppRejectReason.R13.getReasonDesc());
		}

		// 附卡则取主卡客户
		basicCustomer = rCustomer.findOne(basicCard.getCustId());
		// 取主卡客户的额度信息
		custLimit = rCustomerCrLmt.findOne(basicCustomer.getCustLmtId());
		// 取账号
		acctNo = basicCard.getAcctNbr();
		account = queryFacility.getAcctByAcctNbr(acctAttr.accountType, acctNo);
		if (dualAcctAttr != null) {
		    dualAccount = queryFacility.getAcctByAcctNbr(dualAcctAttr.accountType, acctNo);
		}
	    }

	    // 建立卡片
	    CcsCard card = this.mergeCard(app, product, productCredit, customer, account);

	    // 建立CardO，基本从card中复制
	    this.generateCardO(app, card, custLimit, account);

	    // 建立介质卡逻辑卡映射
	    this.generateCardLmMapping(app);

	    // 随交易开卡
	    this.mergePostingTmpOrTxnReject(productCredit, app, account);

	    // 报表
//	    info.getApplyResponseRptItemList().add(makeApplyResponseRptItem(app, AppRejectReason.R00, customer,
//									    custLimit, card, account));
//	    if (dualAccount != null) {
//		info.getApplyResponseRptItemList().add(makeApplyResponseRptItem(app, AppRejectReason.R00, customer,
//										custLimit, card, dualAccount));
//	    }

	    // 短信
//	    info.setApplyResponseSMItem(makeApplyResponseSMItem(app, customer, account, dualAccount, card));

	    // 虚拟卡申请回盘文件
//	    if (product.productType == ProductType.M && organization.microLoanSupport == MicroLoanSupport.V) {
//		info.setvCardApplyResponse(makeVCardApplyResponseFile(app));
//	    }

	    // return info;

	} catch (Exception e) {
	    logger.error("建客建帐建卡异常,申请单号{}", app.appNo);
	    throw e;
	}
    }

    /**
     * 
     * @see 方法名：makeVCardApplyResponseFile
     * @see 描述： 创建虚拟卡申请回盘文件
     * @see 创建日期：2015-6-23下午6:33:15
     * @author ChengChun
     * 
     * @param app
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private VCardApplyResponseItem makeVCardApplyResponseFile(ApplyFileItem app) {
	VCardApplyResponseItem vCardAppResp = new VCardApplyResponseItem();

	vCardAppResp.org = app.org;
	vCardAppResp.productCd = app.productCd;
	vCardAppResp.ddBankAcctNo = app.ddBankAcctNo;
	vCardAppResp.cardNo = app.cardNo;
	vCardAppResp.name = app.name;
	vCardAppResp.idType = app.idType;
	vCardAppResp.idNo = app.idNo;

	return vCardAppResp;
    }

    /**
     * @see 方法名：mergeCustomer
     * @see 描述：客户信息维护
     * @see 创建日期：2015年6月18日下午3:32:00
     * @author yuyang
     * 
     * @param app
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private CcsCustomer mergeCustomer(ApplyFileItem app) {
	QCcsCustomer qCustomer = QCcsCustomer.ccsCustomer;
	CcsCustomer customer =
		rCustomer.findOne(qCustomer.idType.eq(app.idType).and(qCustomer.idNo.eq(app.idNo))
			.and(qCustomer.org.eq(app.org)));

	if (customer == null) {
	    logger.debug("新客户");
	    logger.debug("idType --------------------- " + app.idType);
	    logger.debug("cardNo --------------------- " + CodeMarkUtils.markIDCard(app.idNo));

	    customer = new CcsCustomer();

	    customer.setOrg(app.org);
	    customer.setIdNo(app.idNo);
	    customer.setIdType(app.idType);
	    customer.setSetupDate(new Date());
	    customer.setInternalCustomerId(app.bankCustomerId);
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
	} else {
	    logger.debug("老客户");
	    logger.debug("idType --------------------- " + app.idType);
	    logger.debug("cardNo --------------------- " + CodeMarkUtils.markIDCard(app.idNo));
	    logger.debug("custId --------------------- " + customer.getCustId());
	}

	// 更新客户信息，上送更新，否则不更新
	if (app.title != null) customer.setTitle(app.title);
	if (app.name != null) customer.setName(app.name);
	if (app.gender != null) customer.setGender(app.gender);
	if (app.birthday != null) customer.setBirthday(app.birthday);
	if (app.occupation != null) customer.setOccupation(app.occupation);
	if (StringUtils.isNotBlank(app.bankmemberNo)) customer.setInternalStaffId(app.bankmemberNo);
	if (StringUtils.isNotBlank(app.nationality)) customer.setNationality(app.nationality);
	if (app.prOfCountry != null) customer.setPrOfCountry(app.prOfCountry);
	if (StringUtils.isNotBlank(app.residencyCountryCd)) customer.setResidencyCountryCd(app.residencyCountryCd);
	if (app.maritalStatus != null) customer.setMaritalStatus(app.maritalStatus);
	if (app.qualification != null) customer.setEducation(app.qualification);
	if (app.socialStatus != null) customer.setSocialStatus(app.socialStatus);
	if (StringUtils.isNotBlank(app.idIssuerAddress)) customer.setIdIssAddr(app.idIssuerAddress);
	if (StringUtils.isNotBlank(app.homePhone)) customer.setHomePhone(app.homePhone);
	if (app.houseOwnership != null) customer.setHouseOwnership(app.houseOwnership);
	if (app.houseType != null) customer.setHouseType(app.houseType);
	if (app.homeStandFrom != null) customer.setHomeStandFrom(app.homeStandFrom);
	if (app.liquidAsset != null) customer.setLiquidAsset(app.liquidAsset);
	if (StringUtils.isNotBlank(app.mobileNo)) customer.setMobileNo(app.mobileNo);
	if (StringUtils.isNotBlank(app.email)) customer.setEmail(app.email);
	if (app.empStatus != null) customer.setEmpStatus(app.empStatus);
	if (app.nbrOfDependents != null) customer.setNbrOfDependents(app.nbrOfDependents);
	if (StringUtils.isNotBlank(app.languageInd)) customer.setLanguageInd(app.languageInd);
	if (app.socialInsAmt != null) {
	    customer.setSocialInsAmt(app.socialInsAmt);
	} else {
	    if (customer.getSocialInsAmt() == null) {
		customer.setSocialInsAmt(BigDecimal.ZERO);
	    }
	}
	if (StringUtils.isNotBlank(app.driveLicenseId)) customer.setDriveLicenseId(app.driveLicenseId);
	if (app.driveLicRegDate != null) customer.setDriveLicRegDate(app.driveLicRegDate);
	if (StringUtils.isNotBlank(app.obligateQuestion)) customer.setSecureQuestion(app.obligateQuestion);
	if (StringUtils.isNotBlank(app.obligateAnswer)) customer.setSecureAnswer(app.obligateAnswer);
	if (app.empStability != null) customer.setEmpQuitFreq(app.empStability);
	if (StringUtils.isNotBlank(app.companyName)) customer.setCorpName(app.companyName);
	if (StringUtils.isNotBlank(app.embName)) customer.setOncardName(app.embName);

	return em.merge(customer);
    }

    /**
     * @see 方法名：mergeAddress
     * @see 描述：地址信息维护
     * @see 创建日期：2015年6月18日下午4:09:20
     * @author yuyang
     * 
     * @param org
     * @param long1
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
    private CcsAddress mergeAddress(String org, Long long1, AddressType addressType, String address, String city,
	    String countryCode, String district, String state, String phone, String zip) {
	QCcsAddress qTmAddress = QCcsAddress.ccsAddress;
	CcsAddress addr =
		rAddress.findOne(qTmAddress.org.eq(org).and(qTmAddress.custId.eq(long1))
			.and(qTmAddress.addrType.eq(addressType)));

	if (addr == null) {
	    addr = new CcsAddress();
	    addr.setOrg(org);
	    addr.setAddrType(addressType);
	    addr.setCustId(long1);
	}
	addr.setAddress(getDefaultValue(address, addr.getAddress(), null, String.class));
	addr.setCity(getDefaultValue(city, addr.getCity(), null, String.class));
	addr.setCountryCode(getDefaultValue(countryCode, addr.getCountryCode(), null, String.class));
	addr.setDistrict(getDefaultValue(district, addr.getDistrict(), null, String.class));
	addr.setState(getDefaultValue(state, addr.getState(), null, String.class));
	addr.setPhone(getDefaultValue(phone, addr.getPhone(), null, String.class));
	addr.setPostcode(getDefaultValue(zip, addr.getPostcode(), null, String.class));

	return em.merge(addr);
    }

    /**
     * @see 方法名：mergeCustomerCrLmt
     * @see 描述： 获取(创建)客户层信用额度
     * @see 创建日期：2015-6-23下午6:39:53
     * @author ChengChun
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
    private CcsCustomerCrlmt mergeCustomerCrLmt(ApplyFileItem app, Product product, AccountAttribute acctAttr,
	    CcsCustomer customer, CcsCustomer basicCustomer) {
	CcsCustomerCrlmt custLimit;
	BigDecimal custLimitAmt = app.creditLimit.multiply(BigDecimal.ONE.add(acctAttr.ovrlmtRate));

	if (basicCustomer.getCustLmtId() != null) {
	    custLimit = rCustomerCrLmt.findOne(basicCustomer.getCustLmtId());

	    // 更新客户级额度
	    if (custLimitAmt.compareTo(custLimit.getCreditLmt()) > 0) {
		custLimit.setCreditLmt(custLimitAmt);
	    }
	    // 更新专项贷款额度
	    // updateLoanLimit(app.org, app.carLoan, LimitCategory.CarLoan,
	    // basicCustomer.getCustLmtId());
	    // updateLoanLimit(app.org, app.houseLoan, LimitCategory.HouseLoan,
	    // basicCustomer.getCustLmtId());
	    // updateLoanLimit(app.org, app.renovationLoan,
	    // LimitCategory.RenovationLoan, basicCustomer.getCustLmtId());
	    // updateLoanLimit(app.org, app.travelLoan,
	    // LimitCategory.TravelLoan, basicCustomer.getCustLmtId());
	    // updateLoanLimit(app.org, app.weddingLoan,
	    // LimitCategory.WeddingLoan, basicCustomer.getCustLmtId());
	    // updateLoanLimit(app.org, app.durableLoan,
	    // LimitCategory.DurableLoan, basicCustomer.getCustLmtId());
	} else {
	    // 到这步如果还没有custLimitId，表明是新建的主卡客户，需要新建一个CustLimitO
	    custLimit = new CcsCustomerCrlmt();
	    custLimit.setOrg(app.org);
	    custLimit.setLmtCalcMethod(LimitType.H);
	    custLimit.setLmtCategroy(product.productType.getLimitCategory());
	    custLimit.setCreditLmt(custLimitAmt);
	    rCustomerCrLmt.save(custLimit);
	    customer.setCustLmtId(custLimit.getCustLmtId());

	    // 新建专项贷款额度
	    // createLoanLimit(app.org, app.carLoan, LimitCategory.CarLoan);
	    // createLoanLimit(app.org, app.houseLoan, LimitCategory.HouseLoan);
	    // createLoanLimit(app.org, app.renovationLoan,
	    // LimitCategory.RenovationLoan);
	    // createLoanLimit(app.org, app.travelLoan,
	    // LimitCategory.TravelLoan);
	    // createLoanLimit(app.org, app.weddingLoan,
	    // LimitCategory.WeddingLoan);
	    // createLoanLimit(app.org, app.durableLoan,
	    // LimitCategory.DurableLoan);
	}
	return custLimit;
    }

    /**
     * 
     * @see 方法名：mergeCard
     * @see 描述：创建卡片
     * @see 创建日期：2015-6-23下午6:40:39
     * @author ChengChun
     * 
     * @param app
     * @param product
     * @param productCredit
     * @param cust
     * @param acct
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private CcsCard mergeCard(ApplyFileItem app, Product product, ProductCredit productCredit, CcsCustomer cust,
	    CcsAcct acct) {
	CcsCard card = new CcsCard();

	card.setOrg(app.org);
	card.setLogicCardNbr(app.cardNo);
	card.setCustId(cust.getCustId());
	card.setAcctNbr(acct.getAcctNbr());
	card.setProductCd(app.productCd);
	card.setApplNbr(app.appNo);
	card.setBarcode(app.barcode);
	card.setBscSuppInd(app.bscSuppInd);
	card.setCardBasicNbr(app.primCardNo);
	card.setOwningBranch(app.owningBranch);
	card.setApplPromoCode(app.appPromotionCd);
	card.setRecomName(app.recomName);
	card.setRecomCardNo(app.recomCardNo);
	card.setSetupDate(new Date());

	if (product.fabricationInd == Indicator.N) {
	    card.setActiveInd(Indicator.Y);
	    card.setActiveDate(new Date());
	} else {
	    card.setActiveInd(Indicator.N);
	}
	card.setLastestMediumCardNbr(app.cardNo);
	card.setSalesInd(app.salesInd);
	card.setApplSrc(app.appSource);
	card.setRepresentName(app.representName);
	card.setPosPinVerifyInd(app.posPinVerifyInd == null ? Indicator.Y : app.posPinVerifyInd);
	card.setRelationshipToBsc(app.relationshipToBsc);
	// 新卡有效期 = batchDay + PRODUCT.NEW_CARD_VALID_PRD(卡管送入)
	card.setCardExpireDate(app.cardExpireDate);
	// 若开卡即收取年费
	if (productCredit.fee.firstCardFeeInd == FirstCardFeeInd.I) {
	    card.setNextCardFeeDate(new Date());
	}
	card.setCardFeeRate(BigDecimal.ZERO);
	card.setRenewInd(RenewInd.D);
	card.setFirstUsageFlag(FirstUsageIndicator.A);
	card.setWaiveCardfeeInd(Indicator.N);
	card.setCardDeliverMethod(app.cardFetchMethod);
	card.setCardDeliverAddrFlag(app.cardMailerInd);

	logger.info("---------------------新建卡片----------------------");
	rCcsCard.save(card);
	logger.debug("新建卡片, LogicalCardNo后四位=" + CodeMarkUtils.subCreditCard(card.getLogicCardNbr()));

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
	rCcsCardUsage.save(cardSt);
	return card;
    }

    /**
     * 
     * @see 方法名：generateCardO
     * @see 描述：创建卡片联机
     * @see 创建日期：2015-6-23下午6:41:27
     * @author ChengChun
     * 
     * @param app
     * @param card
     * @param custLimit
     * @param acct
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void generateCardO(ApplyFileItem app, CcsCard card, CcsCustomerCrlmt custLimit, CcsAcct acct) {
	CcsCardO cardO = new CcsCardO();

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
	cardO.setCycleRetailLmt(app.cardCycleLimit == null ? DEFAULT_LIMIT : app.cardCycleLimit);
	cardO.setCycleCashLmt(app.cardCycleCashLimit == null ? DEFAULT_LIMIT : app.cardCycleCashLimit);
	cardO.setCycleNetLmt(app.cardCycleNetLimit == null ? DEFAULT_LIMIT : app.cardCycleNetLimit);
	cardO.setTxnLmt(app.cardTxnLimit == null ? DEFAULT_LIMIT : app.cardTxnLimit);
	cardO.setTxnCashLmt(app.cardTxnCashLimit == null ? DEFAULT_LIMIT : app.cardTxnCashLimit);
	cardO.setTxnNetLmt(app.cardTxnNetLimit == null ? DEFAULT_LIMIT : app.cardTxnNetLimit);
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
	rCcsCardO.save(cardO);
    }

    /**
     * @see 方法名：mergeAcctNbr
     * @see 描述：创建(获取)账号
     * @see 创建日期：2015-6-23下午6:45:48
     * @author ChengChun
     * 
     * @param org
     * @param basicCustomer
     * @param acctAttr
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private Long mergeAcctNbr(String org, CcsCustomer basicCustomer, AccountAttribute acctAttr) {
	Long acctNo = null;
	if (acctAttr.accountType.isSharedCredit()) {
	    // 共享额度，查询是否有现成账户
	    QCcsAcct qTmAccount = QCcsAcct.ccsAcct;

	    List<CcsAcct> acctList =
		    new JPAQuery(em).from(qTmAccount)
			    .where(qTmAccount.org.eq(org).and(qTmAccount.custId.eq(basicCustomer.getCustId())))
			    .list(qTmAccount);

	    // 如果有账户，则使用该账号
	    for (CcsAcct acct : acctList) {
		if (blockCodeUtils.isExists(acct.getBlockCode(), BLOCK_CODE_C)
			|| blockCodeUtils.isExists(acct.getBlockCode(), BLOCK_CODE_P)
			|| !acct.getAcctType().isSharedCredit()) {
		    continue;
		} else {
		    acctNo = acct.getAcctNbr();
		    break;
		}
	    }
	}
	if (acctNo == null) {
	    // 不共享或没有账号则生成账号
	    CcsAcctNbr acctNbr = new CcsAcctNbr();
	    acctNbr.setOrg(org);
	    logger.info("--------------------生成账号--------------------");
	    rCcsAcctNbr.save(acctNbr);

	    acctNo = acctNbr.getAcctNbr();
	}

	logger.debug("获取账号, acctNo=" + acctNo);
	return acctNo;
    }

    /**
     * 
     * @see 方法名：mergeAcct
     * @see 描述：创建(更新)账户
     * @see 创建日期：2015-6-23下午6:52:44
     * @author ChengChun
     * 
     * @param acctNo
     * @param product
     * @param productCredit
     * @param acctAttr
     * @param customer
     * @param app
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private CcsAcct mergeAcct(Long acctNo, Product product, ProductCredit productCredit, AccountAttribute acctAttr,
	    CcsCustomer customer, ApplyFileItem app) {
	// 获取账户
	CcsAcct acct = rAcct.findOne(new CcsAcctKey(acctNo, acctAttr.accountType));

	// 获取汇率
	CurrencyCtrl currCtrl =
		parameterFacility.loadParameter(acctAttr.accountType.getCurrencyCode(), CurrencyCtrl.class);
	BigDecimal creditLimit =
		app.creditLimit.divide(currCtrl.conversionRt.multiply(new BigDecimal(100)), 0, BigDecimal.ROUND_DOWN)
			.multiply(new BigDecimal(100));// 向下取整到百

	if (acct == null) {
	    logger.debug("新建账户, acctNo=" + acctNo + ", acctType=" + acctAttr.accountType);

	    acct = new CcsAcct();

	    acct.setOrg(app.org);
	    acct.setAcctNbr(acctNo);
	    acct.setAcctType(acctAttr.accountType);
	    acct.setCustId(customer.getCustId());
	    acct.setCustLmtId(customer.getCustLmtId()); // 附卡客户不会新建账户，所以这里没问题
	    acct.setProductCd(app.productCd);
	    acct.setDefaultLogicCardNbr(app.cardNo);// 无卡交易
	    acct.setCurrency(acctAttr.accountType.getCurrencyCode());
	    acct.setCreditLmt(creditLimit);
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
	    logger.info("new Date()=-------------------------------------------"+new Date()+"----------------------");
	    acct.setSetupDate(new Date());// 跑批日期
	    //acct.setSetupDate(globalManagementService.getSystemStatus().getBusinessDate());
	    acct.setOvrlmtNbrOfCyc(0);
	    acct.setName(app.name);
	    acct.setGender(app.gender);
	    acct.setOwningBranch(app.owningBranch);
	    acct.setMobileNo(app.mobileNo);
	    acct.setCorpName(app.companyName);
	    String billingCycleFormat =
		    StringUtils.isNotBlank(app.billingCycle) ? app.billingCycle : productCredit.dfltCycleDay.toString();
	    int bc = Integer.parseInt(billingCycleFormat.trim());
	    acct.setCycleDay(String.format("%02d", bc));
	    acct.setNextStmtDate(getNextStmtDay(app.productCd, app.billingCycle, getBizDate()));// 计算下个账单日期
	    acct.setStmtFlag(Indicator.Y);
	    acct.setStmtMailAddrInd(app.stmtMailAddrInd);
	    acct.setStmtMediaType(app.stmtMediaType);
	    // 获取账单地址
	    if (app.stmtMailAddrInd != null) {
		QCcsAddress qTmAddress = QCcsAddress.ccsAddress;
		CcsAddress addr =
			rAddress.findOne(qTmAddress.org.eq(app.org).and(qTmAddress.custId.eq(customer.getCustId()))
				.and(qTmAddress.addrType.eq(app.stmtMailAddrInd)));
		acct.setStmtCountryCode(addr.getCountryCode());
		acct.setStmtState(addr.getState());
		acct.setStmtCity(addr.getCity());
		acct.setStmtDistrict(addr.getDistrict());
		acct.setStmtAddress(addr.getAddress());
		acct.setStmtPostcode(addr.getPostcode());
	    }
	    acct.setEmail(app.email);
	    acct.setAgeCode("0");// 账龄默认给"0"
	    acct.setAgeCodeGl("0");
	    acct.setMemoDb(BigDecimal.ZERO);
	    acct.setMemoCash(BigDecimal.ZERO);
	    acct.setMemoCr(BigDecimal.ZERO);
	    acct.setDdInd(app.ddInd);
	    acct.setDdBankName(app.ddBankName);
	    acct.setDdBankBranch(app.ddBankBranch);
	    acct.setDdBankAcctNbr(app.ddBankAcctNo);
	    acct.setDdBankAcctName(app.ddBankAcctName);
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
	    acct.setPmtDueDate(rescheduleUtils.getNextPaymentDay(app.productCd, acct.getNextStmtDate()));
	    acct.setPmtDueDayBal(BigDecimal.ZERO);
	    acct.setGraceDate(rescheduleUtils.getNextGraceDay(app.productCd, acct.getNextStmtDate()));
	    acct.setQualGraceBal(BigDecimal.ZERO);

	    logger.info("---------------------插入账户信息表---------------------");
	    rAcct.save(acct);

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
	    acctO.setFloatRate(app.custRateDiscount);
	    logger.info("---------------------插入账户信息表-授权---------------------");
	    rAcctO.save(acctO);
	} else {
	    logger.debug("更新老账户, acctNo=" + acctNo + ", acctType=" + acctAttr.accountType);

	    // 获取TmAccountO
	    CcsAcctO acctO = rAcctO.findOne(new CcsAcctOKey(acctNo, acctAttr.accountType));

	    // 旧账户的话有可能更新账户级Rate,来自账户下最高级别的
	    Product oldProduct = parameterFacility.loadParameter(acct.getProductCd(), Product.class);
	    // 旧有产品等级比新申请的低
	    if (oldProduct.cardClass.compareTo(product.cardClass) < 0) {
		// 更新TmAccount
		acct.setProductCd(product.productCode);
		acct.setDefaultLogicCardNbr(app.cardNo);// 新申请的介质卡与逻辑卡相同
		// 更新TmAccountO
		acctO.setProductCd(product.productCode);
	    }
	    // 更新账户级额度
	    if (creditLimit.compareTo(acct.getCreditLmt()) > 0) {
		// 更新TmAccount
		acct.setCreditLmt(creditLimit);
		// 更新TmAccountO
		acctO.setCreditLmt(creditLimit);
	    }
	}
	return acct;
    }

    /**
     * 
     * @see 方法名：generateCardLmMapping
     * @see 描述： 创建介质卡号逻辑卡号映射
     * @see 创建日期：2015-6-23下午6:53:57
     * @author ChengChun
     * 
     * @param app
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void generateCardLmMapping(ApplyFileItem app) {
	CcsCardLmMapping map = new CcsCardLmMapping();

	map.setOrg(app.org);
	map.setCardNbr(app.cardNo);
	map.setLogicCardNbr(app.cardNo);

	logger.info("------------------插入卡号映射表-----------------");
	rCcsCardLmMapping.save(map);
    }

    /**
     * 
     * @see 方法名：mergeLinkman
     * @see 描述：创建(更新)联系人信息
     * @see 创建日期：2015-6-23下午6:58:08
     * @author ChengChun
     * 
     * @param org
     * @param long1
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
    private CcsLinkman mergeLinkman(String org, Long long1, Relationship relationship, String name, Gender gender,
	    String mobileNo, Date birthday, String corpName, IdType idType, String idNo, String corpPhone,
	    String corpFax, EmpPositionAttrType corpPost) {

	QCcsLinkman qTmContact = QCcsLinkman.ccsLinkman;
	CcsLinkman contact =
		rLinkman.findOne(qTmContact.org.eq(org).and(qTmContact.custId.eq(long1))
			.and(qTmContact.relationship.eq(relationship)));

	if (contact == null) {
	    contact = new CcsLinkman();
	    contact.setOrg(org);
	    contact.setCustId(long1);
	    contact.setRelationship(relationship);
	}
	contact.setName(getDefaultValue(name, contact.getName(), null, String.class));
	contact.setGender(getDefaultValue(gender, contact.getGender(), null, Gender.class));
	contact.setMobileNo(getDefaultValue(mobileNo, contact.getMobileNo(), null, String.class));
	contact.setBirthday(getDefaultValue(birthday, contact.getBirthday(), null, Date.class));
	contact.setCorpName(getDefaultValue(corpName, contact.getCorpName(), null, String.class));
	contact.setIdType(getDefaultValue(idType, contact.getIdType(), null, IdType.class));
	contact.setIdNo(getDefaultValue(idNo, contact.getIdNo(), null, String.class));
	contact.setCorpTelephNbr(getDefaultValue(corpPhone, contact.getCorpTelephNbr(), null, String.class));
	contact.setCorpFax(getDefaultValue(corpFax, contact.getCorpFax(), null, String.class));
	contact.setCorpPosition(getDefaultValue(corpPost, contact.getCorpPosition(), null, EmpPositionAttrType.class));

	return em.merge(contact);
    }

    /**
     * 
     * @see 方法名：mergeEmployee
     * @see 描述：创建(更新)工作信息
     * @see 创建日期：2015-6-23下午6:59:16
     * @author ChengChun
     * 
     * @param org
     * @param long1
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
    private CcsEmployee mergeEmployee(String org, Long long1, String companyName, String companyPhone,
	    String companyFax, EmpPositionAttrType title, EmpType industryCategory, CorpStructure companyCategory,
	    TitleOfTechnicalType titleOfTechnical, BigDecimal revenuePerYear, BigDecimal familyAverageRevenue) {

	QCcsEmployee qTmEmployee = QCcsEmployee.ccsEmployee;
	CcsEmployee employee = rCcsEmployee.findOne(qTmEmployee.org.eq(org).and(qTmEmployee.custId.eq(long1)));

	if (employee == null) {
	    employee = new CcsEmployee();
	    employee.setOrg(org);
	    employee.setCustId(long1);
	}
	employee.setCorpPosition(getDefaultValue(title, employee.getCorpPosition(), EmpPositionAttrType.Z,
						 EmpPositionAttrType.class));
	employee.setCorpIndustryCategory(getDefaultValue(industryCategory, employee.getCorpIndustryCategory(),
							 EmpType.Z, EmpType.class));
	employee.setCorpStructure(getDefaultValue(companyCategory, employee.getCorpStructure(), CorpStructure.Z,
						  CorpStructure.class));
	employee.setCorpTechTitle(getDefaultValue(titleOfTechnical, employee.getCorpTechTitle(),
						  TitleOfTechnicalType.D, TitleOfTechnicalType.class));
	employee.setIncomePy(getDefaultValue(revenuePerYear, employee.getIncomePy(), BigDecimal.ZERO, BigDecimal.class));
	employee.setFamilyIncomePyp(getDefaultValue(familyAverageRevenue, employee.getFamilyIncomePyp(),
						    BigDecimal.ZERO, BigDecimal.class));

	return em.merge(employee);
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
    private <T> T getDefaultValue(Object inputValue, Object origValue, Object defaultValue, Class<T> clazz) {
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
	return (T)o;
    }

    /**
     * 
     * @see 方法名：makeApplyResponseRptItem
     * @see 描述：送报表接口
     * @see 创建日期：2015-6-23下午7:00:24
     * @author ChengChun
     * 
     * @param app
     * @param reason
     * @param cust
     * @param limit
     * @param card
     * @param acct
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private ApplyResponseRptItem makeApplyResponseRptItem(ApplyFileItem app, AppRejectReason reason, CcsCustomer cust,
	    CcsCustomerCrlmt limit, CcsCard card, CcsAcct acct) {
	ApplyResponseRptItem rpt = new ApplyResponseRptItem();

	rpt.appRejectReason = reason;
	rpt.setupDate = new Date();
	rpt.appNo = app.appNo;
	rpt.cardNo = app.cardNo;
	rpt.bscSuppInd = app.bscSuppInd;
	rpt.pyhCd = app.designCd;

	if (limit != null) {
	    rpt.creditLimitCust = limit.getCreditLmt();
	}
	if (acct != null) {
	    rpt.acctNo = acct.getAcctNbr().toString();
	    rpt.acctType = acct.getAcctType();
	    rpt.creditLimitAcct = acct.getCreditLmt();
	}
	if (card != null) {
	    rpt.org = card.getOrg();
	    rpt.logicalCardNo = card.getLogicCardNbr();
	    rpt.blockCode = card.getBlockCode();
	    rpt.custId = card.getCustId().toString();
	    rpt.productCd = card.getProductCd();
	    rpt.owningBranch = card.getOwningBranch();
	}
	if (cust != null) {
	    rpt.name = cust.getName();
	    rpt.idType = cust.getIdType();
	    rpt.idNo = cust.getIdNo();
	    rpt.mobileNo = cust.getMobileNo();
	}

	return rpt;
    }

    /**
     * @see 方法名：makeApplyResponseSMItem
     * @see 描述：送短信接口
     * @see 创建日期：2015-6-23下午7:01:11
     * @author ChengChun
     * 
     * @param app
     * @param cust
     * @param acct
     * @param dualAcct
     * @param card
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
/*    private ApplyResponseSMItem makeApplyResponseSMItem(ApplyFileItem app, CcsCustomer cust, CcsAcct acct,
	    CcsAcct dualAcct, CcsCard card) {
	ApplyResponseSMItem sm = new ApplyResponseSMItem();

	sm.org = app.org;
	sm.custName = cust.getName();
	sm.gender = cust.getGender();
	sm.cardNo = getCardNoBySendMsgCardType(acct, card);
	sm.mobileNo = cust.getMobileNo();
	sm.msgCd = fetchMsgCdService.fetchMsgCd(card.getProductCd(), CPSMessageCategory.CPS043);
	sm.creditLimit = acct.getCreditLmt();

	if (dualAcct != null) {
	    sm.dualCreditLimit = dualAcct.getCreditLmt();
	} else {
	    sm.dualCreditLimit = BigDecimal.ZERO;
	}

	return sm;
    }*/

    /**
     * 
     * @see 方法名：mergePostingTmpOrTxnReject
     * @see 描述：随交易开卡
     * @see 创建日期：2015-6-23下午7:04:43
     * @author ChengChun
     * 
     * @param productCredit
     * @param app
     * @param acct
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    private void mergePostingTmpOrTxnReject(ProductCredit productCredit, ApplyFileItem app, CcsAcct acct) {
	if (productCredit.inactiveTrade != null && productCredit.inactiveTrade) {
	    // 指定POS分期开卡
	    if (productCredit.setupLoanP != null && productCredit.setupLoanP) {
		CcsPostingTmp txnPost = generateTxnLoanP(productCredit, app, acct);
		LoanRegInfo loanRegInfo = u0001SetupLoanP.generateLoanReg(productCredit, acct, txnPost);

		if (loanRegInfo.getTmLoanReg() != null) {
		    logger.info("------------------------插入当日入账交易表---------------------");
		    rCcsPostingTmp.save(txnPost);
		    logger.info("------------------------插入消费转分期注册表---------------------");
		    rCcsLoanReg.save(loanRegInfo.getTmLoanReg());

		    logger.debug("指定POS开卡交易成功，交易参考号：{}", txnPost.getRefNbr());
		} else {
		    CcsTxnReject txnReject = new CcsTxnReject();
		    txnReject.updateFromMap(txnPost.convertToMap());
		    txnReject.setPostingFlag(loanRegInfo.getResult());

		    logger.info("------------------------插入挂账交易历史表---------------------");
		    rCcsTxnReject.save(txnReject);

		    logger.debug("指定POS开卡交易挂账{}，交易参考号：{}", loanRegInfo.getResult(), txnPost.getRefNbr());
		}
	    }

	    // TODO 其他随交易开卡类型
	}
    }

    /**
     * 
     * @see 方法名：generateTxnLoanP
     * @see 描述：生成指定POS分期交易，交易金额默认信用额度
     * @see 创建日期：2015-6-23下午7:05:38
     * @author ChengChun
     * 
     * @param productCredit
     * @param app
     * @param acct
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public CcsPostingTmp generateTxnLoanP(ProductCredit productCredit, ApplyFileItem app, CcsAcct acct) {

	// 交易主键生成
	CcsTxnSeq txnSeq = new CcsTxnSeq();
	txnSeq.setOrg(acct.getOrg());

	logger.info("------------------插入交易流水号表------------------");
	rCcsTxnSeq.save(txnSeq);

	// 交易要素
	SysTxnCdMapping mapping = parameterFacility.loadParameter(SysTxnCd.S40, SysTxnCdMapping.class);
	TxnCd txnCd = parameterFacility.loadParameter(mapping.txnCd, TxnCd.class);
	String planNbr = productCredit.planNbrList.get(txnCd.planType);
	String refNbr = txnUtils.genRefnbr(acct.getAcctNbr(), acct.getAcctType(), new Date());

	CcsPostingTmp txn = new CcsPostingTmp();

	txn.setOrg(acct.getOrg()); // 机构号
	txn.setTxnSeq(txnSeq.getTxnSeq()); // 交易流水号
	txn.setAcctNbr(acct.getAcctNbr()); // 账户编号
	txn.setAcctType(acct.getAcctType()); // 账户类型
	txn.setCardNbr(app.cardNo); // 介质卡号
	txn.setLogicCardNbr(app.cardNo); // 逻辑卡号
	txn.setCardBasicNbr(app.primCardNo); // 逻辑卡主卡卡号
	txn.setProductCd(app.productCd); // 产品代码
	txn.setTxnDate(new Date()); // 交易日期
	txn.setTxnTime(new Date()); // 交易时间(使用业务日期，但应该是new
	// Date())
	txn.setPostTxnType(PostTxnType.M); // 入账交易类型
	txn.setTxnCode(txnCd.txnCd); // 交易码
	txn.setDbCrInd(txnCd.logicMod.getDbCrInd()); // 借贷标志
	txn.setTxnAmt(app.creditLimit); // 交易金额
	txn.setPostAmt(app.creditLimit); // 入账币种金额
	txn.setPostDate(new Date()); // 入账日期
	txn.setAuthCode(DEFAULT_AUTH_CODE); // 授权码
	txn.setCardBlockCode(null); // 卡片锁定码
	txn.setTxnCurrency(acct.getCurrency()); // 交易币种代码
	txn.setPostCurrency(acct.getCurrency()); // 入账币种代码
	txn.setOrigTransDate(null); // 原始交易日期
	txn.setPlanNbr(planNbr); // 信用计划号
	txn.setRefNbr(refNbr); // 交易参考号
	txn.setTxnDesc(txnCd.description); // 交易描述
	txn.setTxnShortDesc(txnCd.shortDesc);
	txn.setPoints(BigDecimal.ZERO); // 积分数值
	txn.setPostingFlag(PostingFlag.F00); // 入账结果标示码
	txn.setPrePostingFlag(PostingFlag.F00);
	txn.setRelPmtAmt(BigDecimal.ZERO); // 公司卡还款金额
	txn.setOrigPmtAmt(BigDecimal.ZERO); // 还款交易原始金额
	txn.setAcqBranchIq(""); // 受理分行代码
	txn.setAcqTerminalId(""); // 受理机构终端标识码
	txn.setAcqAcceptorId(""); // 受卡方标识码
	txn.setOrigTxnCode(""); // 原交易交易码
	txn.setOrigTxnAmt(BigDecimal.ZERO); // 原交易交易金额
	txn.setOrigSettAmt(BigDecimal.ZERO); // 原交易清算金额
	txn.setInterchangeFee(BigDecimal.ZERO); // 原交易货币转换费
	txn.setFeePayout(BigDecimal.ZERO); // 原交易交易手续费
	txn.setFeeProfit(BigDecimal.ZERO); // 发卡方应得手续费收入
	txn.setLoanIssueProfit(BigDecimal.ZERO); // 分期交易发卡行收益
	txn.setStmtDate(acct.getNextStmtDate()); // 账单日期
	txn.setVoucherNo(""); // 销售单凭证号
	txn.setTerm(null);

	// 指定POS交易信息
	txn.setMcc(productCredit.setupLoanPMerchantMCC); // 商户类别代码
	txn.setAcqAddress(productCredit.setupLoanPCommodityNameAddr); // 受理机构名称地址

	return txn;
    }

    /**
     * 计算下一账单日期
     * 
     * @param account
     * @param processDate
     * @return
     */
    public Date getNextStmtDay(String productCd, String billingCycle, Date processDate) {
	// 账单日
	Date stmtDay = null;

	// 获取参数
	ProductCredit productCredit = unifiedParameterFacility.loadParameter(productCd, ProductCredit.class);
	AccountAttribute acctAttr =
		unifiedParameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);

	switch (acctAttr.cycleBaseInd) {
	case W:
	    // 当周账单日
	    Calendar c = DateUtils.toCalendar(processDate);
	    // dfltCycleDay=7表示周七
	    if (productCredit.dfltCycleDay == 7) {
		c.set(Calendar.DAY_OF_WEEK, 1);
	    } else {
		c.set(Calendar.DAY_OF_WEEK, productCredit.dfltCycleDay + 1);
	    }
	    stmtDay = c.getTime();
	    // 跑批日期>=当周账单日，则周+1
	    if (DateUtils.truncatedCompareTo(processDate, stmtDay, Calendar.DATE) >= 0) {
		stmtDay = DateUtils.addWeeks(stmtDay, 1);
	    }
	    break;
	case M:
	    // 当月账单日, 优先取账户上的，无则取参
	    if (StringUtils.isNotBlank(billingCycle)) {
		Integer cycle = Integer.parseInt(billingCycle.trim());
		// billingCycle大于28则28
		stmtDay = DateUtils.setDays(processDate, cycle > 28 ? 28 : cycle);
	    } else {
		stmtDay = DateUtils.setDays(processDate, productCredit.dfltCycleDay);
	    }

	    // 获取下一个账单日期
	    if (DateUtils.truncatedCompareTo(processDate, stmtDay, Calendar.DATE) >= 0) {
		stmtDay = DateUtils.addMonths(stmtDay, 1);
	    }
	    break;
	default:
	    throw new IllegalArgumentException("账户属性中账单周期类型不正确");
	}
	return stmtDay;
    }

    private Date getBizDate() {
	return globalManagementService.getSystemStatus().getBusinessDate();
    }

    /**
     * @see 方法名：getCardNoBySendMsgCardType
     * @see 描述：获取不同短信发送卡号方式的卡号
     * @see 创建日期：2015-6-24下午4:58:51
     * @author ChengChun
     * 
     * @param acct
     * @param card
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String getCardNoBySendMsgCardType(CcsAcct acct, CcsCard card) {
	ProductCredit productCr = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
	// 参数未设置，自动填充规则
	if (productCr.sendMessageCardType == null) {
	    Product product = parameterFacility.loadParameter(card.getProductCd(), Product.class);
	    if (product.productType == ProductType.M) {
		if (StringUtils.isNotBlank(acct.getDdBankAcctNbr())) {
		    return acct.getDdBankAcctNbr();
		} else {
		    return card.getLastestMediumCardNbr();
		}
	    } else {
		return card.getLastestMediumCardNbr();
	    }
	}
	// 根据参数返回需要的卡号
	switch (productCr.sendMessageCardType) {
	case C:
	    return card.getLastestMediumCardNbr();
	case L:
	    return card.getLogicCardNbr();
	case D:
	    if (StringUtils.isNotBlank(acct.getDdBankAcctNbr())) {
		return acct.getDdBankAcctNbr();
	    } else {
		return card.getLastestMediumCardNbr();
	    }
	default:
	    throw new IllegalArgumentException("短信发送卡号方式不正确：" + productCr.sendMessageCardType);
	}
    }
}
