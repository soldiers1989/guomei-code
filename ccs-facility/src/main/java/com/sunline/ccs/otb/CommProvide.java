package com.sunline.ccs.otb;


import static com.sunline.ccs.facility.CaclUtils.checkPositive;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.BnpPeriod;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Component
public class CommProvide {
	
	private static final String BNP_TYPE_ERROR = "余额成份类型不正确";
	
	@PersistenceContext
	private EntityManager em;
	
	
	/**
	 * 获取参数工具类
	 */
	@Autowired
	private UnifiedParameterFacility unifiedParameter;
	
	/**
	 * 自定义额度信息表-授权
	 */
	@Autowired
	private RCcsCustomerCrlmt rCcsCustomerCrlmt;
	
	/**
	 * 根据卡号获取CcsCardO数据
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardO findCardOByCardNbr(String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		QCcsCardO qCcsCardO = QCcsCardO.ccsCardO;
		CcsCardO ccsCardO = query
				.from(qCcsCardLmMapping, qCcsCardO)
				.where(qCcsCardLmMapping.logicCardNbr.eq(qCcsCardO.logicCardNbr).and(
						qCcsCardLmMapping.org.eq(qCcsCardO.org).and(
								qCcsCardLmMapping.cardNbr.eq(cardNbr).and(
										qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.singleResult(qCcsCardO);

		return ccsCardO;
	}
	
	/**
	 * 根据卡号，账户类型获取账户CcsAcctO 信息
	 * @param cardNbr
	 * @param acctType
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcctO findAcctOByCardNbr(String cardNbr, AccountType acctType) {
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		QCcsCardO qCcsCardO = QCcsCardO.ccsCardO;
		QCcsAcctO qCcsAcctO = QCcsAcctO.ccsAcctO;
		JPAQuery query = new JPAQuery(em);
		CcsAcctO ccsAccountO = query
				.from(qCcsCardLmMapping, qCcsCardO, qCcsAcctO)
				.where(qCcsCardLmMapping.logicCardNbr.eq(qCcsCardO.logicCardNbr).and(
						qCcsCardLmMapping.org
								.eq(qCcsCardO.org)
								.and(qCcsCardLmMapping.cardNbr.eq(cardNbr))
								.and(qCcsCardO.acctNbr.eq(qCcsAcctO.acctNbr).and(
										qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
												qCcsAcctO.acctType.eq(acctType)))))).singleResult(qCcsAcctO);

		return ccsAccountO;
	}
	
	/**
	 * 根据卡号查询客户CcsCustomer信息
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCustomer findCustomerByCardNbr(String cardNbr) {
		QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
		QCcsCardO qCcsCardO = QCcsCardO.ccsCardO ;
		QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsCardLmMapping.org
				.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qCcsCardLmMapping.cardNbr.eq(cardNbr))
				.and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCardO.logicCardNbr))
				.and(qCcsCardO.custId.eq(qCcsCustomer.custId));
		CcsCustomer ccsCustomer = query
				.from(qCcsCardLmMapping, qCcsCardO, qCcsCustomer)
				.where(booleanExpression).singleResult(qCcsCustomer);

		return ccsCustomer;
	}
	
	/**
	 * 根据custLmtId查询客户额度信息
	 * @param custLmtId
	 * @return
	 */
	public CcsCustomerCrlmt findCustomerCrlmtByCustLmtId(Long custLmtId){
		QCcsCustomerCrlmt qCcsCustomerCrlmt = QCcsCustomerCrlmt.ccsCustomerCrlmt;
		CcsCustomerCrlmt ccsCustLimitO = rCcsCustomerCrlmt.findOne(qCcsCustomerCrlmt.custLmtId.eq(custLmtId));
		return ccsCustLimitO;
	}
	
	/**
	 * 根据客户号获取账户列表信息
	 * @param custId
	 * @return
	 */
	public List<CcsAcctO> findAcctOByCustId(Long custId){
		QCcsAcctO qCcsAcctO = QCcsAcctO.ccsAcctO;
		JPAQuery query = new JPAQuery(em);
		BooleanExpression bp = qCcsAcctO.org.eq(
				OrganizationContextHolder.getCurrentOrg()).and(
						qCcsAcctO.custId.eq(custId));
		return query.from(qCcsAcctO).where(bp).list(qCcsAcctO);
	}
	
	/**
	 * 获取账户层当前信用额度，考虑临时额度
	 * @param account 账户信息
	 * @return 当前信用额度
	 */
	public BigDecimal getCurrCreditLmt(Date processDate, CcsAcctO accountO) {
		// 当前信用额度
		BigDecimal currLimit = accountO.getCreditLmt();
		
		// 判断临时额度是否在有效期内
		if(accountO.getTempLmtBegDate()!= null  && accountO.getTempLmtEndDate() != null){
			if (processDate.compareTo(accountO.getTempLmtBegDate()) >= 0
					&& processDate.compareTo(accountO.getTempLmtEndDate()) <= 0) {
				// 临时额度有效，当前信用额度 = 临时信用额度
				currLimit = accountO.getTempLmt(); 
			}
		}
		
		return currLimit;
	}
	
	/**
	 * 获得授权超限比例
	 * @param accountO
	 * @param productCr
	 * @param processDate
	 * @return
	 */
	public BigDecimal getOvrlmtRate(CcsAcctO accountO ,ProductCredit productCr ,Date processDate) {
		BigDecimal ovrLmtRate = accountO.getOvrlmtRate();
		if (ovrLmtRate == null) {
			AccountAttribute accountAttribute = this.retrieveAccountAttribute(productCr, accountO.getAcctType());
			ovrLmtRate = accountAttribute.ovrlmtRate;
		}
		
		return ovrLmtRate;
	}
	
	/**
	 * 获取授权超限比例
	 * 
	 * @param acct
	 * @param productCr
	 * @return
	 */
	public BigDecimal getOvrlmtRate(CcsAcct acct, ProductCredit productCr) {
		BigDecimal ovrLmtRate = acct.getOvrlmtRate();
		if (ovrLmtRate == null) {
			AccountAttribute accountAttribute = this.retrieveAccountAttribute(productCr, acct.getAcctType());
			ovrLmtRate = accountAttribute.ovrlmtRate;
		}
		return ovrLmtRate;
	}
	
	/**
	 * 获取授权超限比例
	 * 
	 * @param acct
	 * @return
	 */
	public BigDecimal getOvrlmtRate(CcsAcct acct) {
		ProductCredit productCr = this.retrieveProductCredit(acct.getProductCd());
		return this.getOvrlmtRate(acct, productCr);
	}
	
	/**
	 * 获得取现额度比例
	 * @param accountO
	 * @param productCr
	 * @param processDate
	 * @return
	 */
	public BigDecimal getCashLmtRate(CcsAcctO accountO ,ProductCredit productCr ,Date processDate) {
		BigDecimal cashLimitRt = accountO.getCashLmtRate();
		if (cashLimitRt == null) {
			AccountAttribute accountAttribute = this.retrieveAccountAttribute(productCr, accountO.getAcctType());
			cashLimitRt = accountAttribute.cashLimitRate;
		}
		
		return cashLimitRt;
	}
	
	/**
	 * 获取取现额度比例
	 * 
	 * @param acct
	 * @param productCr
	 * @return
	 */
	public BigDecimal getCashLmtRate(CcsAcct acct, ProductCredit productCr) {
		BigDecimal cashLimitRt = acct.getCashLmtRate();
		if (cashLimitRt == null) {
			AccountAttribute accountAttribute = this.retrieveAccountAttribute(productCr, acct.getAcctType());
			cashLimitRt = accountAttribute.cashLimitRate;
		}
		return cashLimitRt;
	}
	
	/**
	 * 获取取现额度比例
	 * 
	 * @param acct
	 * @return
	 */
	public BigDecimal getCashLmtRate(CcsAcct acct) {
		ProductCredit productCr = this.retrieveProductCredit(acct.getProductCd());
		return this.getCashLmtRate(acct, productCr);
	}
	
	/**
	 * 获得分期额度比例
	 * @param accountO
	 * @param productCr
	 * @param processDate
	 * @return
	 */
	public BigDecimal getLoanLmtRate(CcsAcctO accountO ,ProductCredit productCr ,Date processDate) {
		BigDecimal loanLimitRt = accountO.getLoanLmtRate();
		if (loanLimitRt == null) {
			AccountAttribute accountAttribute = this.retrieveAccountAttribute(productCr, accountO.getAcctType());
			loanLimitRt = accountAttribute.loanLimitRate;
		}
		
		return loanLimitRt;
	}
	
	/**
	 * 获取分期额度比例
	 * 
	 * @param acct
	 * @param productCr
	 * @return
	 */
	public BigDecimal getLoanLmtRate(CcsAcct acct, ProductCredit productCr) {
		BigDecimal loanLimitRt = acct.getLoanLmtRate();
		if (loanLimitRt == null) {
			AccountAttribute accountAttribute = this.retrieveAccountAttribute(productCr, acct.getAcctType());
			loanLimitRt = accountAttribute.loanLimitRate;
		}
		return loanLimitRt;
	}
	
	/**
	 * 获取分期额度比例
	 * 
	 * @param acct
	 * @return
	 */
	public BigDecimal getLoanLmtRate(CcsAcct acct) {
		ProductCredit productCr = this.retrieveProductCredit(acct.getProductCd());
		return this.getLoanLmtRate(acct, productCr);
	}
	
	/**
	 * 获取产品参数
	 * @param productCd 产品代码
	 * @return 产品参数
	 */
	public ProductCredit retrieveProductCredit(String productCd) {
		return unifiedParameter.loadParameter(productCd, ProductCredit.class);
	}
	
	/**
	 * 获取账户类型参数对象
	 * @param product 产品参数对象
	 * @param accountType 账户类型
	 * @return 账户类型参数对象
	 */
	public AccountAttribute retrieveAccountAttribute(ProductCredit product, AccountType accountType)
	{
		AccountAttribute accountAttribute = unifiedParameter.loadParameter(String.valueOf(product.accountAttributeId), AccountAttribute.class);
		if (accountAttribute.accountType != accountType){
			accountAttribute = unifiedParameter.loadParameter(String.valueOf(product.dualAccountAttributeId), AccountAttribute.class);
			if (accountAttribute.accountType != accountType){
				throw new IllegalArgumentException("account type does not match product definition");
			}
		}
		return accountAttribute;
	}
	/**
	 * 查询organization信息
	 * @return
	 */
	public Organization getOrganiztion(){
		return unifiedParameter.loadParameter(null, Organization.class);
	}
	
	/**
	 * 查询Product信息
	 * @param productCode
	 * @return
	 */
	public Product getProduct(String productCode){
		return unifiedParameter.loadParameter(productCode, Product.class);
	}
	
	/**
	 * 查询账户PlanList
	 * @param acct
	 * @return
	 */
	public List<CcsPlan> getCcsPlanByCcsAcct(CcsAcct acct){
		QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
		JPAQuery query = new JPAQuery(em);
		return query.from(qCcsPlan).where(qCcsPlan.acctNbr.eq(acct.getAcctNbr())
				.and(qCcsPlan.acctType.eq(acct.getAcctType()))).list(qCcsPlan);
	}
	
	/**
	 * 获取信用计划中不同余额成份类型的实际金额
	 * @param plan 还款计划
	 * @param bucketType 余额成份类型
	 * @param isCtd 是否当期余额 ture-当期余额 false-往期余额
	 * @return 金额
	 */
	public BigDecimal getBucketAmount(CcsPlan plan, BucketType bucketType, BnpPeriod bnpPeriod)
	{
		switch (bucketType){
		case Pricinpal:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdPrincipal() : plan.getPastPrincipal(); 
		case Interest:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdInterest() : plan.getPastInterest();
		case CardFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdCardFee() : plan.getPastCardFee();
		case InsuranceFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdInsurance() : plan.getPastInsurance();
		case Mulct:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdMulctAmt() : plan.getPastMulctAmt();
		case StampDuty:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdStampdutyAmt() : plan.getPastStampdutyAmt();
		case LifeInsuFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdLifeInsuAmt() : plan.getPastLifeInsuAmt();
		case LatePaymentCharge:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdLateFee() : plan.getPastLateFee();
		case NSFCharge:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdNsfundFee() : plan.getPastNsfundFee();
		case OverLimitFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdOvrlmtFee() : plan.getPastOvrlmtFee();
		case SVCFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdSvcFee() : plan.getPastSvcFee();
		case TXNFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdTxnFee() : plan.getPastTxnFee();
		case UserFee1:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee1() : plan.getPastUserFee1();
		case UserFee2:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee2() : plan.getPastUserFee2();
		case UserFee3:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee3() : plan.getPastUserFee3();
		case UserFee4:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee4() : plan.getPastUserFee4();
		case UserFee5:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee5() : plan.getPastUserFee5();
		case UserFee6:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdUserFee6() : plan.getPastUserFee6();
		case Compound:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdCompound() : plan.getPastCompound();
		case Penalty:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdPenalty() : plan.getPastPenalty();
		case ReplaceSvcFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdReplaceSvcFee() : plan.getPastReplaceSvcFee();
		case ReplacePenalty:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdReplacePenalty() : plan.getPastReplacePenalty();
		case ReplaceMulct:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdReplaceMulct() : plan.getPastReplaceMulct();
		case ReplaceLatePaymentCharge:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdReplaceLateFee() : plan.getPastReplaceLateFee();
		case ReplaceTxnFee:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdReplaceTxnFee() : plan.getPastReplaceTxnFee();
		case PrepayPkg:
			return bnpPeriod == BnpPeriod.CTD ? plan.getCtdPrepayPkgFee() : plan.getPastPrepayPkgFee();
		default: throw new IllegalArgumentException(BNP_TYPE_ERROR);
		}
	}
	
	/**
	 * 获取当期剩余应还款额
	 * 
	 * @param plans
	 * @return
	 */
	public BigDecimal getRemainGraceBal(List<CcsPlan> plans) {
		//当期剩余应还款额
		BigDecimal remainGraceBal = BigDecimal.ZERO;
		
		for(CcsPlan plan : plans) {
			//获取信用计划参数
			PlanTemplate planTemplate = unifiedParameter.retrieveParameterObject(plan.getPlanNbr(), PlanTemplate.class);
			
			//循环所有余额成分
			//判断余额成分对应的是否计入全额应还款金额参数为true的余额成分计入全额还款金额
			for (BucketType bucketType : BucketType.values()) {
				if(planTemplate.intParameterBuckets.get(bucketType) == null) continue;
				Boolean b = planTemplate.intParameterBuckets.get(bucketType).graceQualify;
				if (b==null?false:b) {
					//当期剩余应还款额只需累加往期金额即可，当期发生金额无需在当期偿还
					remainGraceBal = remainGraceBal.add(this.getBucketAmount(plan, bucketType, BnpPeriod.PAST));
				}
			}
		}
		return checkPositive(remainGraceBal);
	}
}
