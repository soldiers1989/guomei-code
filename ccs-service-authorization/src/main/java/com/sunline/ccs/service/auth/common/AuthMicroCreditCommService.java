package com.sunline.ccs.service.auth.common;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.loan.AuthLoanProvideImpl;
import com.sunline.ccs.otb.CustOTB;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.FloatRate;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.LoanInfo;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.LimitCategory;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * 
 * @see 类名：AuthMicroCreditCommService
 * @see 描述：小额贷公共业务处理
 *
 * @see 创建日期：   2015年6月24日下午3:14:13
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthMicroCreditCommService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TxnUtils txnUtils;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private AuthCommService authCommonService;
	@Autowired
	private CustOTB customerOTB;
	@Autowired
	private CustAcctCardFacility custAcctCardFacility;

	/**
	 * 规则：获取账户锁定码
	 * 
	 * @param accountO
	 * @return
	 */
	public Integer getAcctCd(CcsAcctO accountO) {
		if (StringUtils.isBlank(accountO.getBlockCode())) {
			return 0;
		}
		char[] blockCodeArray = accountO.getBlockCode().toCharArray();
		for (char blockCode : blockCodeArray) {
			if (blockCode >= '0' && blockCode <= '9') {
				return Integer.parseInt(blockCode+"");
			}
		}
		return 0;
	}

	/**
	 * 规则：获取账户最近6个月总共的逾期期数
	 * @param accounto
	 * @return
	 */
	public Integer getSixMonCdCount(CcsAcctO accounto) {
		String ageCodeHis = custAcctCardFacility.getAcctByAcctNbr(accounto.getAcctType(), accounto.getAcctNbr()).getAgeHst();
		if (StringUtils.isBlank(ageCodeHis)) {
			return 0;
		}
		char[] ageCodeArray = ageCodeHis.substring(0, ageCodeHis.length() < 6 ? ageCodeHis.length() : 6).toCharArray();
		Integer cdCount = 0;
		for (char age : ageCodeArray) {
			if (age > '0' && age <= '9') {
				cdCount += Integer.parseInt(age+"");
			}
		}
		return cdCount;
	}
	
	
	/**
	 * 规则：获取小额贷款最后一期日期,用于比较贷款授信有效期
	 * @param loanType 
	 * @param billingCycle 
	 * @throws AuthException 
	 * @throws ParseException 
	 */
	public boolean isGtLastTermDate(String expiryDate, Date bizDate, Integer cd, String billingCycle, LoanType loanType) throws AuthException{
		if(StringUtils.isBlank(expiryDate)){
			authCommonService.throwAuthException(AuthReason.V034, "生成小额贷最后一期超期检查项时获取[卡片有效期]为空");
		}
		try {
			// 业务日期
			Calendar bizCal = Calendar.getInstance();
			bizCal.setTime(bizDate);
			// 卡片有效期（格式：yyMM）
			Calendar expireCal = Calendar.getInstance();
			/** 由于有效期格式为yyMM，格式化后日期“年”缺少前两位。现使用业务日期年的前两位代替 **/
			String expiryYear = String.valueOf(bizCal.get(Calendar.YEAR)).substring(0,2);
			expireCal.setTime(new SimpleDateFormat("yyyyMM").parse(expiryYear + expiryDate));
			// 账单日
			Integer billingCycleDay = Integer.valueOf(billingCycle);  

			/** 判断贷款类型-组装贷款最后一期的日期 **/
			if (loanType == LoanType.MCAT) {
				// 随借随还-获取当月的最后一天
				expireCal.set(Calendar.DAY_OF_MONTH, expireCal.getActualMaximum(Calendar.DAY_OF_MONTH));
			} else if (loanType == LoanType.MCEI || loanType == LoanType.MCEP) {
				// 等额本息、等额本金-获取[当月+期数]后的账单日
				expireCal.set(Calendar.DAY_OF_MONTH, billingCycleDay);
				// 判断下一个账单日（贷款最后一期的账单日截止）
				if (bizCal.get(Calendar.DAY_OF_MONTH) > billingCycleDay) {
					bizCal.add(Calendar.MONTH, cd);
				} else {
					bizCal.add(Calendar.MONTH, cd - 1);
				}
				bizCal.set(Calendar.DAY_OF_MONTH, billingCycleDay);
			} else {
				authCommonService.throwAuthException(AuthReason.S009, "小额贷出现非法贷款类型：" + loanType);
			}
			return bizCal.compareTo(expireCal) > 0 ? true : false;
		} catch (ParseException e) {
			authCommonService.throwAuthException(logger, e, AuthReason.TF02, "生成小额贷最后一期超期检查项时出现异常");
		}
		return false;
	}
	
	/**
	 * [小额贷第一步]获取小额贷信息
	 * @param message
	 * @return 
	 * @throws AuthException
	 */
	public void microCreditProcessor(AuthContext context) throws AuthException{
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcctO account = context.getAccount();
		CupMsg message = context.getMessage();
		if (context.getProduct().productType.getLimitCategory() == LimitCategory.MicroCreditLimit && txnInfo.getTransType() == AuthTransType.TransferDeditDepos && txnInfo.getTransDirection() == AuthTransDirection.Normal) {

			logger.debug("# [小额贷第一步]获取小额贷信息 #");
			// 获取小额贷信息
			LoanInfo mcInfo = getMicroCreditforMessage(message, account);
			if (mcInfo == null) {
				authCommonService.throwAuthException(AuthReason.TF02, "报文上送的小额贷数据获取失败");
			}
			// 获取小额贷项目 , 获取发卡分行列表 , 获取卡产品列表
			LoanPlan loanPlan = parameterFacility.loadParameter(mcInfo.getLoanCode(), LoanPlan.class);
			mcInfo.setLoanType(loanPlan.loanType);
			mcInfo.setLoanPlan(loanPlan);
			/** 获取小额贷计价方式 */
			mcInfo.setLoanFeeDef(getLoanFeeDef(mcInfo.getLoanInitTerm(), loanPlan));
			/** 获取商户信息 */
			//mcInfo.setLoanMerchant(parameterFacility.loadParameter(mcInfo.getMerchantId(), LoanMerchant.class));
			/** 当前逾期期数 */
			context.getTxnInfo().setCdTerms(getAcctCd(account));
			/** 6个月内逾期期数合计 */
			context.getTxnInfo().setSixMonCdCount(getSixMonCdCount(account));
			/** 获取小额贷款最后一期日期,用于比较贷款授信有效期 */
			context.getTxnInfo().setGtLastTermDate(isGtLastTermDate(txnInfo.getExpiryDate(), txnInfo.getBizDate(), mcInfo.getLoanInitTerm(), account.getCycleDay(), loanPlan.loanType));
			/** 增加小额贷OTB */
			context.getTxnInfo().setMicroCreditOTB(customerOTB.customerMicroCreditOTB(account, txnInfo.getBizDate()));

			logger.debug("# MicroCreditOTB == " + txnInfo.getMicroCreditOTB());
			// 返回小额贷信息，程序后面执行规则校验
			context.setLoanInfo(mcInfo);
			/**
			 * 获得分期注册表信息、暂存在context中
			 */
			buildLoanReg(context);
		}
	}
	
	/**
	 * [小额贷第二步]解析报文48域[分期期数,项目编号,手续费收取方式,商户号]
	 * @return
	 * @throws AuthException
	 */
	public LoanInfo getMicroCreditforMessage(CupMsg message , CcsAcctO accountO) throws AuthException{
		logger.debug("# [小额贷第二步]解析报文48域[分期期数,项目编号,手续费收取方式,商户号] #");
		Map<String, Map<String, String>> tlvMap = message.getF048_AddPriv();
		if (tlvMap == null || tlvMap.size() == 0)
			return null;
		Map<String, String> ipMap = tlvMap.get(CupMsg.METHOD_IP);
		if (ipMap == null || ipMap.size() == 0)
			return null;
		String ipVal = ipMap.get(CupMsg.VAL);
		LoanInfo loan = new LoanInfo();
		try {
			loan.setMerchantId(message.exist(42) ? message.field(42).trim() : null);
			loan.setRefNbr(txnUtils.getRefnbr(message.field(7), message.field(38), message.field(37)));
			loan.setLoanInitTerm(Integer.valueOf(ipVal.substring(0, 2)));
			// 根据loanCode获取LoanPlan
			loan.setLoanCode(ipVal.substring(2, 32).trim());
			/** 暂时写死为一次性收取 */
			loan.setLoanFeeMethod(LoanFeeMethod.F);
			/** 获取浮动比例标识 */
			loan.setFloatRate(getFlatRate(ipVal, accountO.getFloatRate()));
		} catch (Exception e) {
			throw new AuthException(AuthReason.TF02, AuthAction.D);
		}
		return loan;
	}
	
	/**
	 * 获得小额贷注册表信息-暂存在context中
	 * @param context
	 * @throws AuthException 
	 */
	public void buildLoanReg(AuthContext context) throws AuthException{
		logger.debug("# [小额贷第三步]生成小额贷注册表信息 #");
		LoanInfo loanInfo = context.getLoanInfo();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsCardO card = context.getCard();
		CcsAcctO accountO = context.getAccount();
		
		try {
			//增加小额贷
			AuthLoanProvideImpl authLoan = new AuthLoanProvideImpl(loanInfo.getLoanType(), loanInfo.getLoanFeeDef(), loanInfo.getLoanFeeMethod());
			CcsLoanReg ccsLoanReg = authLoan.genMcAuthLoanReg(accountO.getAcctNbr(), accountO.getAcctType(), 
					card.getLogicCardNbr(), txnInfo.getBizDate(), txnInfo.getSysOlTime(), txnInfo.getChbTransAmt(),
					loanInfo.getLoanType(), loanInfo.getLoanCode(), loanInfo.getLoanInitTerm(),
					loanInfo.getLoanFeeDef(), loanInfo.getFloatRate(), loanInfo.getRefNbr());
			// 将小额贷注册表放入loanInfo
			context.getLoanInfo().setCcsLoanReg(ccsLoanReg);
		} catch (Exception e) {
			//TODO 获取小额贷注册表信息时出现异常、reason没有确定
			authCommonService.throwAuthException(logger, e, AuthReason.S009, "生成小额贷注册表信息时出现异常");
		}
	}
	
	/**
	 * 获取小额贷期数<br>
	 * <b>判断期数是否在计价方式区间内</b>
	 * @param loanInitTerm
	 * @param loanPlan
	 * @return
	 * @throws AuthException
	 */
	public LoanFeeDef getLoanFeeDef(Integer loanInitTerm, LoanPlan loanPlan) throws AuthException{

		if (loanPlan.loanType == LoanType.MCEI || loanPlan.loanType == LoanType.MCEP) {
			if ((loanPlan.minCycle != null && loanInitTerm < loanPlan.minCycle) || (loanPlan.maxCycle != null && loanInitTerm > loanPlan.maxCycle)) {
				authCommonService.throwAuthException(AuthReason.TL51, "报文上送的小额贷期数不在项目活动小额贷期数列表中");
			}
		}
		
		int key = -1;
		for (int i : loanPlan.loanFeeDefMap.keySet()) {
			// 期数相等直接返回
			if (i == loanInitTerm) {
				key = i;
				break;
			} else {
				// 判断期数，获取当前期数的下一个期数
				if (i > loanInitTerm) {
					if (key == -1) {
						key = i;
					} else {
						if (i < key) {
							key = i;
						}
					}
				}
			}
		}
		if (key == -1) {
			authCommonService.throwAuthException(AuthReason.TL51, "报文上送的小额贷期数不在项目活动小额贷期数列表中");
		}
		return loanPlan.loanFeeDefMap.get(key);
	}
	
	/**
	 * 获取loanReg的浮动比例
	 * D=下浮
	 * U=上浮
	 * N=不浮动
	 * 空格=取账户浮动比例
	 * 
	 * @param ipVal
	 * @param acctFloatRate
	 * @return
	 * @throws AuthException
	 */
	public BigDecimal getFlatRate(String ipVal , BigDecimal acctFloatRate) throws AuthException{
		// 获取浮动比例标识
		String frInd = ipVal.substring(32, 33).trim();
		if (StringUtils.isBlank(frInd)) {
			return acctFloatRate;
		}
		try {
			FloatRate floatRateInd = FloatRate.valueOf(frInd.toUpperCase());
			BigDecimal floatRate = new BigDecimal(ipVal.substring(33, 38).trim()).movePointLeft(4);
			// 上浮下浮判断并赋值
			if (floatRateInd == FloatRate.D) {
				return floatRate.negate();
			} else if (floatRateInd == FloatRate.U) {
				return floatRate;
			} else if (floatRateInd == FloatRate.N) {
				return BigDecimal.ZERO;
			}
		} catch (Exception e) {
			authCommonService.throwAuthException(logger, e, AuthReason.TF02, "报文上送的小额贷浮动比例异常");
		}
		return acctFloatRate;
	}

}
