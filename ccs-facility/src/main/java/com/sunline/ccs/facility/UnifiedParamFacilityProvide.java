package com.sunline.ccs.facility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.SysTxnCdMapping;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.ccs.param.def.enums.TxnType;
import com.sunline.pcm.param.def.CurrencyCd;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
* @author fanghj
 * @version 创建时间：2012-9-14 下午6:48:43 参数提供
 */


@Component
public class UnifiedParamFacilityProvide {

	private static final String DESC_NAME = "获取参数：";

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	
	@Autowired
	private MicroCreditRescheduleUtils microCreditRescheduleUtils;

	@Autowired
	private GlobalManagementService globalManageService;

	/**
	 * 获取org层参数
	 * 
	 * @return 
	 * @throws ProcessException
	 */
	public Organization organization() throws ProcessException {
		try {
			Organization org = unifiedParameterService.loadParameter(null, Organization.class);
			LogTools.printObj(log, org, DESC_NAME + "ORG层参数");
			return org;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	
	/**
	 * 获取bmp的org参数
	 * 
	 * @return
	 * @throws ProcessException
	 */
	public com.sunline.pcm.param.def.Organization organizationBmp() throws ProcessException {
		try {
			com.sunline.pcm.param.def.Organization org = unifiedParameterService.loadParameter(null, com.sunline.pcm.param.def.Organization.class);
			LogTools.printObj(log, org, DESC_NAME + "ORG层参数");
			return org;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 得到临额有效期限 TEMP_LIMIT_DURATION integer 以月为单位
	 * 
	 * @return
	 * @throws ProcessException
	 */
	public Integer tempLimitMths() throws ProcessException {
		try {
			Integer tempLimit = organization().tempLimitMaxMths;
			LogTools.printObj(log, tempLimit, DESC_NAME + "临额有效期限");
			return tempLimit;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 获取最大信用额度
	 * 
	 * @throws ProcessException
	 */
	public BigDecimal maxCreditLimit() throws ProcessException {
		try {
			BigDecimal maxCreditLimit = organization().maxCreditLimit;
			LogTools.printObj(log, maxCreditLimit, DESC_NAME + "最大信用额度");
			return maxCreditLimit;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 获取产品信息
	 * 
	 * @param productCd
	 * @return
	 * @throws ProcessException
	 */
	public ProductCredit productCredit(String productCd) throws ProcessException {
		try {
			ProductCredit pc = unifiedParameterService.loadParameter(productCd, ProductCredit.class);
//			LogTools.printObj(log, pc, DESC_NAME + "产品信息");
			return pc;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 获取产品信息
	 * 
	 * @param productCd
	 * @return
	 * @throws ProcessException 
	 */
	public Product product(String productCd) throws ProcessException {
		try {
			Product p = unifiedParameterService.loadParameter(productCd, Product.class);
			LogTools.printObj(log, p, DESC_NAME + "产品信息");
			return p;
		} catch (Exception ex) {
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	
	/**
	 * 获取年费交易码
	 * 
	 * @return SysTxnCdMapping
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public SysTxnCdMapping cardFee() throws ProcessException {
		try {
			SysTxnCdMapping sysTxnCd = unifiedParameterService.loadParameter(String.valueOf(SysTxnCd.S17), SysTxnCdMapping.class);
			LogTools.printObj(log, sysTxnCd, DESC_NAME + "年费交易码");
			return sysTxnCd;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	
	/**
	 * 获取年费减免交易码
	 * 
	 * @return SysTxnCdMapping
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public SysTxnCdMapping derateCardFee() throws ProcessException {
		try {
			SysTxnCdMapping sysTxnCd = unifiedParameterService.loadParameter(String.valueOf(SysTxnCd.S69), SysTxnCdMapping.class);
			LogTools.printObj(log, sysTxnCd, DESC_NAME + "年费减免交易码");
			return sysTxnCd;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 获取基准货币，即本币币种代码
	 * 
	 * @return
	 * @throws ProcessException
	 */
	public String base_curr_cd() throws ProcessException {
		try {
			String baseCurrCd = organization().baseCurrCd;
			LogTools.printObj(log, baseCurrCd, DESC_NAME + "基准货币");
			return baseCurrCd;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 获取 acct_attribute(本币账户属性引用ID)
	 * 
	 * @param productCd
	 * @return Integer
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public AccountAttribute acct_attribute(String productCd) throws ProcessException {
		try {
			AccountAttribute accoutArr = unifiedParameterService.loadParameter(productCredit(productCd).accountAttributeId, AccountAttribute.class);
			LogTools.printObj(log, accoutArr, DESC_NAME + "本币账户属性");
			return accoutArr;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	
	/**
	 * 获取 acct_attribute(外币账户属性引用ID)
	 * 
	 * @param productCd
	 * @return Integer
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public AccountAttribute dual_acct_attribute(String productCd) throws ProcessException {
		try {
			if(productCredit(productCd).dualAccountAttributeId == null){
				return null;
			}else{
				AccountAttribute dualAccoutArr = unifiedParameterService.loadParameter(productCredit(productCd).dualAccountAttributeId, AccountAttribute.class);
				LogTools.printObj(log, dualAccoutArr, DESC_NAME + "外币账户属性");
				return dualAccoutArr;
			}
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 获取缺省取现额度比例
	 * 
	 * @param productCd
	 * @return
	 * @throws ProcessException
	 *             BigDecimal
	 * @exception
	 * @since 1.0.0
	 */
	public BigDecimal cashLimitRate(String productCd) throws ProcessException {
		return acct_attribute(productCd).cashLimitRate;
	}

	/**
	 * 缺省额度内分期比例 loanLimitRate
	 * 
	 * @param productCd
	 * @return
	 * @throws ProcessException
	 *             BigDecimal
	 * @exception
	 * @since 1.0.0
	 */
	public BigDecimal loanLimitRate(String productCd) throws ProcessException {
		return acct_attribute(productCd).loanLimitRate;
	}

	/**
	 * 约定还款提前天数DIRECT_DB_DAYS
	 * 
	 * @throws ProcessException
	 */
	public Integer direct_Db_Days(String productCd) throws ProcessException {
		try {
			Integer direc = acct_attribute(productCd).directDbDays;
			LogTools.printObj(log, direc, DESC_NAME + " 约定还款提前天数");
			return direc;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 获取分期计划参数
	 * 
	 * @param loanCode
	 * @return LoanPlan
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public LoanPlan loanPlan(String loanCode) throws ProcessException {
		try {
			LoanPlan loanPlan = unifiedParameterService.loadParameter(loanCode, LoanPlan.class);
//			LogTools.printObj(log, loanPlan, DESC_NAME + " 分期计划参数");
			return loanPlan;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 获取分期参数
	 * 
	 * @param productCd
	 * @param loanType
	 * @return LoanPlan
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public LoanPlan loanPlan(String productCd, LoanType loanType) throws ProcessException {
		try {
			if(log.isDebugEnabled())
				log.debug("loanPlan获取贷款产品：productCd:[{}],loanType:[{}]",productCd,loanType);
			
			LoanPlan loanPlan = null;
			Map<LoanType, String> loanPlansMap = productCredit(productCd).loanPlansMap;
			String loanCode = loanPlansMap.get(loanType);
					
			loanPlan = loanPlan(loanCode);
						
//			LogTools.printObj(log, loanPlan, DESC_NAME + " 根据产品[" + productCd + "]，分期类型[" + loanType + "]获取分期计划");
			return loanPlan;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 获取分期参数
	 * 
	 * @param productCd
	 * @param loanType
	 * @return LoanPlan
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public LoanPlan loanPlanProduct(String productCd) throws ProcessException {
		try {
			if(log.isDebugEnabled())
				log.debug("loanPlan获取贷款产品：productCd:[{}]",productCd);
			
			LoanPlan loanPlan = null;
			Map<LoanType, String> loanPlansMap = productCredit(productCd).loanPlansMap;
			String loanCode = loanPlansMap.get(productCredit(productCd).defaultLoanType);
					
			loanPlan = loanPlan(loanCode);
						
//			LogTools.printObj(log, loanPlan, DESC_NAME + " 根据产品[" + productCd + "]，分期类型[" + loanType + "]获取分期计划");
			return loanPlan;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	
	
	/**
	 * 根据产品信息，分期期数，获取分期参数
	 * 按期数匹配参数，只取匹配到的第一条
	 * 
	 * @param productCd
	 *            产品编号
	 * @param loanType
	 *            分期类型
	 * @param term
	 *            分期期数
	 * @return
	 * @throws ProcessException
	 * @since 1.0.0
	 */
	public LoanFeeDef loanFeeDef(String productCd, LoanType loanType, Integer term) throws ProcessException {
		try {
			if(log.isDebugEnabled())
				log.debug("loanFeeDef贷款定价：productCd:[{}],loanType:[{}],term:[{}]",productCd,loanType,term);
				
			Map<Integer, LoanFeeDef> loanFeeDefMap = null;
			LoanFeeDef loanFeeDef=null;
			Map<LoanType, String> loanPlansMap = productCredit(productCd).loanPlansMap;
			String loanCode = loanPlansMap.get(loanType);
					
			LoanPlan loanPlan = loanPlan(loanCode);
			loanFeeDefMap = loanPlan.loanFeeDefMap;
			
			//按期数与分期金额匹配参数，只取匹配到的第一条
			for(Integer loanFeeDefKey: loanFeeDefMap.keySet()){
			    LoanFeeDef tmpLoanFeeDef = loanFeeDefMap.get(loanFeeDefKey);
				if(tmpLoanFeeDef.initTerm.compareTo(term )==0){
					loanFeeDef = tmpLoanFeeDef;
					break;
				}
			}
			//没有匹配到分期定价参数
			if (loanFeeDef == null ) {
				throw new ProcessException("分期失败，不存在分期定价参数");
			}
		//			LogTools.printObj(log, loanFeeDef, DESC_NAME + "根据产品[" + productCd + "]，分期期数[" + term + "]，分期类型[" + loanType + "]获取分期参数");
			
			return loanFeeDef;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	
	
	/**
	 * 根据贷款产品信息，分期期数，获取分期定价参数
	 * 
	 * @param loanCode
	 *            贷款产品
	 * @param term
	 *            分期期数
	 * @return
	 * @throws ProcessException
	 * @since 1.0.0
	 */
/*	public LoanFeeDef loanFeeDef(String loanCode, Integer term) throws ProcessException {
		try {
			if(log.isDebugEnabled())
				log.debug("loanFeeDef贷款定价：loanCode:[{}],term:[{}]",loanCode,term);
				
			Map<Integer, LoanFeeDef> loanFeeDef = null;
			LoanPlan loanPlan = loanPlan(loanCode);
			loanFeeDef = loanPlan.loanFeeDefMap;
			
			if (loanFeeDef != null && !loanFeeDef.containsKey(term)) {
				throw new ProcessException("分期失败，不存在符合申请条件的账单分期参数");
			}
			LoanFeeDef lf = loanFeeDef.get(term);
			if(log.isDebugEnabled())
				log.debug("loanFeeDef贷款定价：loanCode:[{}],loanType[{}],term:[{}]",loanCode,loanPlan.loanType,term);			
			return lf;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}*/
	
	/**
	 * 根据产品信息，定价参数键值，获取分期定价参数
	 * 
	 * @param productCd
	 *            产品编号
	 * @param loanType
	 *            分期类型
	 * @param key -- loanfeedefid
	 *            定价参数键值
	 * @return
	 * @throws ProcessException
	 * @since 1.0.0
	 */
	public LoanFeeDef loanFeeDefByKey(String productCd, LoanType loanType, Integer key) throws ProcessException {
		try {
			if(log.isDebugEnabled())
				log.debug("loanFeeDef贷款定价：productCd:[{}],loanType:[{}],key:[{}]",productCd,loanType,key);
				
			Map<Integer, LoanFeeDef> loanFeeDef = null;
			Map<LoanType, String> loanPlansMap = productCredit(productCd).loanPlansMap;
			String loanCode = loanPlansMap.get(loanType);
					
			LoanPlan loanPlan = loanPlan(loanCode);
			loanFeeDef = loanPlan.loanFeeDefMap;
			
			if (loanFeeDef != null && !loanFeeDef.containsKey(key)) {
				throw new ProcessException("分期失败，不存在分期定价参数");
			}
			LoanFeeDef lf = loanFeeDef.get(key);			
			return lf;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 根据产品信息，定价参数键值，获取分期定价参数
	 * 
	 * @param loancode
	 *            贷款产品代码
	 * @param key
	 *            定价参数键值 loanfeedefid
	 * @return
	 * @throws ProcessException
	 * @since 1.0.0
	 */
	public LoanFeeDef loanFeeDefByKey(String loanCode,Integer key) throws ProcessException {
		try {
			if(log.isDebugEnabled())
				log.debug("loanFeeDef贷款定价：loancode:[{}],key:[{}]",loanCode,key);
				
			Map<Integer, LoanFeeDef> loanFeeDef = null;
					
			LoanPlan loanPlan = loanPlan(loanCode);
			loanFeeDef = loanPlan.loanFeeDefMap;
			
			if (loanFeeDef != null && !loanFeeDef.containsKey(key)) {
				throw new ProcessException("分期失败，不存在分期定价参数");
			}
			LoanFeeDef lf = loanFeeDef.get(key);			
			return lf;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	/**
	 * 根据产品信息,分期期数，分期金额，获取分期定价参数
	 * 
	 * @param productCd
	 *            产品编号
	 * @param loanType
	 *            分期类型
	 * @param term
	 *            定价参数键值
	 * @param amt
	 *            定价参数键值
	 * @return
	 * @throws ProcessException
	 * @since 1.0.0
	 */
	public LoanFeeDef loanFeeDef(String productCd, LoanType loanType, Integer term,BigDecimal amt,String loanFeeDefId) throws ProcessException {
		try {
			if(log.isDebugEnabled())
				log.debug("loanFeeDef贷款定价：productCd:[{}],loanType:[{}],term:[{}],amt:[{}]",productCd,loanType,term,amt);
				
			Map<LoanType, String> loanPlansMap = productCredit(productCd).loanPlansMap;
			String loanCode = loanPlansMap.get(loanType);

			return microCreditRescheduleUtils.getLoanFeeDef(loanCode, term, amt, loanFeeDefId);
		
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	/**
	 * 根据产品信息,分期期数，分期金额，获取分期定价参数
	 * 
	 * @param productCd
	 *            产品编号
	 * @param loanType
	 *            分期类型
	 * @param term
	 *            定价参数键值
	 * @param amt
	 *            定价参数键值
	 * @return
	 * @throws ProcessException
	 * @since 1.0.0
	 */
	public LoanFeeDef loanFeeDef(String loanCode, Integer term,BigDecimal amt) throws ProcessException {
		try {
			if(log.isDebugEnabled())
				log.debug("loanFeeDef贷款定价：loancode:[{}],term:[{}],amt:[{}]",loanCode,term,amt);
				
			Map<Integer, LoanFeeDef> loanFeeDefMap = null;
			LoanFeeDef loanFeeDef = null;
			
			LoanPlan loanPlan = loanPlan(loanCode);
			loanFeeDefMap = loanPlan.loanFeeDefMap;
			
			//按期数与分期金额匹配参数，定价参数金额区间应该互不重叠，这里不做检查，只取匹配到的第一条
			for(Integer loanFeeDefKey: loanFeeDefMap.keySet()){
			    LoanFeeDef tmpLoanFeeDef = loanFeeDefMap.get(loanFeeDefKey);
				if(term.compareTo(tmpLoanFeeDef.initTerm)==0 
				&& amt.compareTo(tmpLoanFeeDef.minAmount)>=0 
				&& amt.compareTo(tmpLoanFeeDef.maxAmount)<=0){
					loanFeeDef= tmpLoanFeeDef;
					break;
				}
			}
			//没有匹配到分期定价参数
			if (loanFeeDef == null ) {
				throw new ProcessException("分期失败，不存在分期定价参数");
			}
					
			return loanFeeDef;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	
	/**
	 * 根据贷款产品信息，获取随借随还分期定价参数
	 * 取最后一个期数定价
	 * @param productCd  产品编号
	 * @return
	 * @throws ProcessException
	 * @since 1.0.0
	 */
	public LoanFeeDef loanFeeDefMCAT(String loanCode) throws ProcessException {
		try {
			if(log.isDebugEnabled())
				log.debug("loanFeeDef贷款定价：loancode:[{}]",loanCode);
				
			Map<Integer, LoanFeeDef> loanFeeDefMap = null;
			LoanFeeDef loanFeeDef = null;
			
			LoanPlan loanPlan = loanPlan(loanCode);
			loanFeeDefMap = loanPlan.loanFeeDefMap;
			
			//按期数与分期金额匹配参数，定价参数金额区间应该互不重叠，这里不做检查，只取匹配到的第一条
			for(Integer loanFeeDefKey: loanFeeDefMap.keySet()){
				loanFeeDef  = loanFeeDefMap.get(loanFeeDefKey);
				if(loanFeeDef.loanFeeDefStatus.equals(LoanFeeDefStatus.A)){
					break;
				}
			}
			//没有匹配到分期定价参数
			if (loanFeeDef == null ) {
				throw new ProcessException("分期失败，不存在分期定价参数");
			}
					
			return loanFeeDef;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	/**
	 * 根据贷款产品信息，获取随借随还分期定价参数
	 * 取最后一个期数定价
	 * @param productCd  产品编号
	 * @return
	 * @throws ProcessException
	 * @since 1.0.0
	 */
	public LoanFeeDef loanFeeDefMCAT(String loanCode,BigDecimal amt) throws ProcessException {
		try {
			if(log.isDebugEnabled())
				log.debug("loanFeeDef贷款定价：loancode:[{}]",loanCode);
				
			Map<Integer, LoanFeeDef> loanFeeDefMap = null;
			LoanFeeDef loanFeeDef = null;
			
			LoanPlan loanPlan = loanPlan(loanCode);
			loanFeeDefMap = loanPlan.loanFeeDefMap;
			
			//按期数与分期金额匹配参数，定价参数金额区间应该互不重叠，这里不做检查，只取匹配到的第一条
			for(Integer loanFeeDefKey: loanFeeDefMap.keySet()){
//				loanFeeDef  = loanFeeDefMap.get(loanFeeDefKey);
				LoanFeeDef tmpLoanFeeDef = loanFeeDefMap.get(loanFeeDefKey);
				if(amt.compareTo(tmpLoanFeeDef.minAmount)>=0 
				&& amt.compareTo(tmpLoanFeeDef.maxAmount)<=0){
					loanFeeDef= tmpLoanFeeDef;
					if(loanFeeDef.loanFeeDefStatus.equals(LoanFeeDefStatus.A)){
						break;
					}
				}
			}
			//没有匹配到分期定价参数
			if (loanFeeDef == null ) {
				throw new ProcessException("分期失败，不存在分期定价参数");
			}
					
			return loanFeeDef;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	
	/**
	 * 获取出账单对应的交易码列表
	 * 
	 * @return List<String>
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public List<String> OutStmtTxnCode() throws ProcessException {
		try {
			Map<String, TxnCd> txnCdMap = unifiedParameterService.retrieveParameterObject(TxnCd.class);
			LogTools.printObj(log, txnCdMap, DESC_NAME + "交易码列表");
			ArrayList<String> arrayList = new ArrayList<String>();
			Collection<TxnCd> txnValues = txnCdMap.values();
			for (TxnCd txnCd : txnValues) {
				if (txnCd != null && txnCd.stmtInd != null && txnCd.stmtInd) {
					arrayList.add(txnCd.txnCd);
				}
			}
			LogTools.printObj(log, arrayList, DESC_NAME + "出账单对应的交易码列表");
			return arrayList;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	
	/**
	 * 根据交易码获取交易类型
	 * 
	 * @param txnCode
	 * @return
	 * @throws ProcessException
	 */
	public TxnType txnType(String txnCode) throws ProcessException {
		try {
			TxnCd txnCd = unifiedParameterService.loadParameter(txnCode, TxnCd.class);
			LogTools.printObj(log, txnCd, DESC_NAME + "交易码");
			return txnCd.txnType;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}
	/**
	 * 根据交易类型获取所有的交易码列表
	 * @param txnType
	 * @return
	 * @throws ProcessException
	 */
	public List<String> txnCode(TxnType txnType) throws ProcessException{
		try {
			Map<String,TxnCd> txnCdMap = unifiedParameterService.retrieveParameterObject(TxnCd.class);
			LogTools.printObj(log, txnCdMap, DESC_NAME + "交易码");
			List<String> list = new ArrayList<String>();
			for(TxnCd txnCd : txnCdMap.values()){
				if(txnCd.txnType == txnType){
					list.add(txnCd.txnCd);
					continue;
				}
			}
			if(list.isEmpty()){
				throw new ProcessException("没有对应交易类型");
			}
			return list;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 根据锁定码获取锁定码信息
	 * 
	 * @param blockCode
	 * @return BlockCode
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public BlockCode blockCode(String blockCode) throws ProcessException {
		try {
			BlockCode bc = unifiedParameterService.loadParameter(blockCode, BlockCode.class);
			LogTools.printObj(log, bc, DESC_NAME + "锁定码");
			return bc;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 获取不能分期的blockCode
	 * 
	 * @param blockCode
	 * @return String
	 * @exception
	 * @since 1.0.0
	 */
	public String unAbleLoanBlockCode() {
		Map<String, BlockCode> blockcodes = unifiedParameterService.retrieveParameterObject(BlockCode.class);
		StringBuffer canLoanBlockCode = new StringBuffer();
		Collection<BlockCode> blockCodeValues = blockcodes.values();
		for (BlockCode blockcode : blockCodeValues) {
			if (!blockcode.loanInd) {
				canLoanBlockCode.append(blockcode.blockCode);
			}
		}
		return canLoanBlockCode.toString();
	}

	/**
	 * 翻译锁定码信息 blockCodeInfo
	 * 
	 * @param blockCode
	 * @return List<String[]>
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public ArrayList<String[]> blockCodeInfo(String blockCode) throws ProcessException {
		try {
			ArrayList<String[]> infoList = null;
			if (StringUtils.isNotEmpty(blockCode)) {
				infoList = new ArrayList<String[]>();
				for (char c : blockCode.toCharArray()) {
					infoList.add(new String[] { String.valueOf(c), blockCode(String.valueOf(c)).description });
				}
			}
			LogTools.printObj(log, infoList, DESC_NAME + "翻译锁定码信息");
			return infoList;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	/**
	 * 
	 * 得到系统批量业务日期
	 * 
	 * @return Date
	 * @exception
	 * @since 1.0.0
	 */
	public Date BusinessDate() {
		return globalManageService.getSystemStatus().getBusinessDate();
	}

	/**
	 * 获取币种参数
	 * 
	 * @param currencyCd
	 * @return
	 * @throws ProcessException 
	 */
	public CurrencyCd currencyCd(String currencyCd) throws ProcessException {
		try {
			CurrencyCd currCd = unifiedParameterService.loadParameter(currencyCd, CurrencyCd.class);
			LogTools.printObj(log, currCd, DESC_NAME + "币种代码");
			return currCd;
		} catch (Exception ex) {
			if(log.isErrorEnabled())
				log.error(ex.getMessage(), ex);
			throw new ProcessException(CcServProConstants.MES_SYS_PARAM_ERROR);
		}
	}

	
}
