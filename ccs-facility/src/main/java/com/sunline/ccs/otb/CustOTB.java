package com.sunline.ccs.otb;

import static com.sunline.ccs.facility.CaclUtils.checkPositive;
import static com.sunline.ccs.facility.CaclUtils.setScale2HalfUp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.param.def.CurrencyCtrl;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ark.support.utils.CodeMarkUtils;

/**
 * 客户级可用额度计算
 * 
 * @author fanghj
 * 
 *         客户级可用额度包括账户OTB和账户的取现OTB， 需要使用TM_ACCT_O表和PRODUCT_CREDIT中的字段进行计算
 */
@Service
public class CustOTB {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 公共组件
	 */
	@Autowired
	private CommProvide commonProvide;

	/**
	 * 获取参数工具类
	 */
	@Autowired
	private UnifiedParameterFacility unifiedParameter;

	@Autowired
	private CustAcctCardFacility queryFacility;

	/**
	 * 账户级可用额度计算
	 */
	@Autowired
	private AcctOTB acctOTB;

	/**
	 * 客户普通授信可用额度 CUSTOMER OTB = 客户普通授信总额度CREDIT_LIMIT – （人民币账户余额之和 + 人民币账户未达借记授权金额之和） - （外币账户余额之和 +
	 * 外币账户未达借记授权金额之和）*外币汇率
	 */
	public BigDecimal customerOTB(String cardNbr, Date processDate) {
		if (logger.isDebugEnabled()) {
			logger.debug("客户普通授信可用额度：cardNbr后四位[{}],processDate[{}]", CodeMarkUtils.subCreditCard(cardNbr), processDate);
		}

		List<CcsAcctO> accountOs;
		try {
			accountOs = queryFacility.getAcctOByCardNbr(cardNbr);
			CcsAcctO accountO = accountOs.get(0);

			ProductCredit productCr = commonProvide.retrieveProductCredit(accountO.getProductCd());
			accountOs = queryFacility.getAcctOListByCustLmtId(accountO.getCustLmtId());
			CcsCustomerCrlmt custLimitO = commonProvide.findCustomerCrlmtByCustLmtId(accountO.getCustLmtId());

			return this.customerOTB(custLimitO, productCr, accountOs, processDate);

		} catch (ProcessException e) {
			logger.error("获取客户信息失败");
		}
		return null;
	}

	/**
	 * 客户普通授信可用额度 CUSTOMER OTB = 客户普通授信总额度CREDIT_LIMIT – （人民币账户余额之和 + 人民币账户未达借记授权金额之和） - （外币账户余额之和 +
	 * 外币账户未达借记授权金额之和）*外币汇率
	 */
	public BigDecimal customerOTB(CcsAcct ccsAcct, Date processDate) {
		if (logger.isDebugEnabled()) {
			logger.debug("客户普通授信可用额度：AcctNo[{}],processDate[{}]", ccsAcct.getAcctNbr(), processDate);
		}

		List<CcsAcctO> accountOs;
		try {
			accountOs = queryFacility.getAcctOByAcctNbr(ccsAcct.getAcctNbr());
			// List<CcsAcctO> accountOs = commonProvide.getCcsAcctOToCustId(customer.getCustId());
			CcsAcctO accountO = accountOs.get(0);
			ProductCredit productCr = commonProvide.retrieveProductCredit(accountO.getProductCd());
			CcsCustomerCrlmt custLimitO = commonProvide.findCustomerCrlmtByCustLmtId(accountO.getCustLmtId());

			return this.customerOTB(custLimitO, productCr, accountOs, processDate);

		} catch (ProcessException e) {
			logger.error("获取客户信息失败");
		}
		return null;
	}

	/**
	 * 客户额度查询 - 不和零修正 （改造出的调用接口）
	 * ？？代码上看和零修正了
	 * 
	 * @param custLimitO
	 * @param productCr
	 * @param accountOs
	 * @param processDate
	 * @return
	 */
	public BigDecimal customerOTB(CcsCustomerCrlmt custLimitO, ProductCredit productCr, Iterable<CcsAcctO> accountOs,
			Date processDate) {
		BigDecimal customerOtbAmt = BigDecimal.ZERO;
		BigDecimal localAcctTotalAmt = BigDecimal.ZERO;
		BigDecimal dualAcctTotalAmt = BigDecimal.ZERO;

		for (CcsAcctO accountO : accountOs) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"客户普通授信可用额度：Org[{}],AcctNo[{}],AcctType[{}],CustLimitId[{}],productCd[{}],processDate[{}]",
						accountO.getOrg(), accountO.getAcctNbr(), accountO.getAcctType(), custLimitO.getCustLmtId(),
						accountO.getProductCd(), processDate);
			}
			if (AccountType.A == accountO.getAcctType() || AccountType.C == accountO.getAcctType()) {
				localAcctTotalAmt = localAcctTotalAmt.add(acctOTB.acctUsedAmt(accountO, productCr, processDate));
			} else {
				CurrencyCtrl currCtrl =
						unifiedParameter.retrieveParameterObject(accountO.getAcctType().getCurrencyCode(),
								CurrencyCtrl.class);
				dualAcctTotalAmt =
						dualAcctTotalAmt.add(acctOTB.acctUsedAmt(accountO, productCr, processDate)).multiply(
								currCtrl.conversionRt);
			}
		}

		customerOtbAmt = custLimitO.getCreditLmt().subtract(localAcctTotalAmt).subtract(dualAcctTotalAmt);

//		customerOtbAmt = customerOtbAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
//		return customerOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : customerOtbAmt;
		return setScale2HalfUp(checkPositive(customerOtbAmt));
	}

	/**
	 * 客户查询可用额度- 和零修正
	 * 
	 * @param custLimitO
	 * @param productCr
	 * @param accountOs
	 * @param processDate
	 * @return
	 */
	public BigDecimal customerInqOTB(CcsCustomerCrlmt custLimitO, ProductCredit productCr,
			Iterable<CcsAcctO> accountOs, Date processDate) {
		BigDecimal customerOtbAmt = this.customerOTB(custLimitO, productCr, accountOs, processDate);
//		return customerOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : customerOtbAmt;
		return checkPositive(customerOtbAmt);
	}

	/**
	 * 小额贷客户额度查询 - 不和零修正 （改造出的调用接口）
	 * ??代码看实际和零修正了
	 * @param cardNbr
	 * @param processDate
	 * @return
	 */
	public BigDecimal customerMicroCreditOTB(CcsAcctO account, Date processDate) {
		// 获取所有账户
		List<CcsAcctO> accountOs = queryFacility.getAcctOByAcctNbr(account.getAcctNbr());
		ProductCredit productCr = commonProvide.retrieveProductCredit(account.getProductCd());
		CcsCustomerCrlmt custLimitO = commonProvide.findCustomerCrlmtByCustLmtId(account.getCustLmtId());

		BigDecimal customerOtbAmt = BigDecimal.ZERO;
		BigDecimal localAcctTotalAmt = BigDecimal.ZERO;
		BigDecimal dualAcctTotalAmt = BigDecimal.ZERO;

		for (CcsAcctO accountO : accountOs) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"客户普通授信可用额度：Org[{}],AcctNo[{}],AcctType[{}],CustLimitId[{}],productCd[{}],processDate[{}]",
						accountO.getOrg(), accountO.getAcctNbr(), accountO.getAcctType(), custLimitO.getCustLmtId(),
						accountO.getProductCd(), processDate);
			}
			// 获取贷款类型的账户
			if (AccountType.E == accountO.getAcctType()) {
				localAcctTotalAmt = localAcctTotalAmt.add(acctOTB.acctUsedAmt(accountO, productCr, processDate));
			} else {
				CurrencyCtrl currCtrl =
						unifiedParameter.retrieveParameterObject(accountO.getAcctType().getCurrencyCode(),
								CurrencyCtrl.class);
				dualAcctTotalAmt =
						dualAcctTotalAmt.add(acctOTB.acctUsedAmt(accountO, productCr, processDate)).multiply(
								currCtrl.conversionRt);
			}
		}

		customerOtbAmt = custLimitO.getCreditLmt().subtract(localAcctTotalAmt).subtract(dualAcctTotalAmt);

//		customerOtbAmt = customerOtbAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
//		return customerOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : customerOtbAmt;
		return setScale2HalfUp(checkPositive(customerOtbAmt));
	}

	/**
	 * 小额贷客户查询可用额度 - 和零修正
	 * 
	 * @param cardNbr
	 * @param processDate
	 * @return
	 */
	public BigDecimal customerMicroCreditInqOTB(CcsAcctO account, Date processDate) {
		BigDecimal customerOtbAmt = this.customerMicroCreditOTB(account, processDate);
//		return customerOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : customerOtbAmt;
		return checkPositive(customerOtbAmt);
	}
}
