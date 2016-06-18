package com.sunline.ccs.loan;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 分期抽象类，用于分期的处理
 * 
 * AbstarctLoanProvide
 * 
 * @version 1.0.0
 * 
 */
public abstract class AbstractLoanProvide {

	// 分期类型
	public LoanType loanType;

	// 分期参数
	public LoanFeeDef loanFeeDef;

	/**
	 * 生成分期接口文件
	 * @param term 分期期数
	 * @param initPrin 分期总金额
	 * @param refNbr  
	 * @param logicCardNbr 逻辑号
	 * @param cardNbr  卡号
	 * @param loanFeeMethod 手续费收取方式，如果为空，使用默认的数据
	 * @param acctNbr TODO
	 * @param acctType TODO
	 * @return
	 * @throws ProcessException   
	 *TmLoanReg  
	 * @exception   
	 * @since  1.0.0
	 */
	public CcsLoanReg genLoanReg(Integer term, BigDecimal initPrin, String refNbr, String logicCardNbr, 
			String cardNbr, LoanFeeMethod loanFeeMethod, Long acctNbr, AccountType acctType, String loanCode, Date busDate) throws ProcessException {
		CcsLoanReg loanReg = new CcsLoanReg();
		loanReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		loanReg.setAcctNbr(acctNbr);
		loanReg.setAcctType(acctType);
		loanReg.setRegisterDate(busDate);
		loanReg.setRequestTime(new Date());
		loanReg.setLogicCardNbr(logicCardNbr);
		loanReg.setCardNbr(cardNbr);
		loanReg.setRefNbr(refNbr);
		loanReg.setLoanType(loanType);
		loanReg.setLoanRegStatus(LoanRegStatus.A);
		loanReg.setLoanInitTerm(term);// 分期期数
		loanReg.setLoanInitPrin(initPrin);// 分期总本金
		loanReg.setLoanCode(loanCode);//分期计划代码
		loanReg.setLoanAction(LoanAction.A);
		loanReg.setMatched(Indicator.N);
		loanReg.setInterestRate(loanFeeDef.feeRate);
		// 计算本金
		DistributeMethodImple.valueOf(loanFeeDef.distributeMethod.toString()).calcustPrin(loanReg);
		// 计算手续费
		{
			if (loanFeeMethod == null) {
				LoanFeeMethodimple.valueOf(loanFeeDef.loanFeeMethod.toString()).calcuteFee(
						loanReg, loanFeeDef);
				loanReg.setLoanFeeMethod(loanFeeDef.loanFeeMethod);// 手续费收取方式
			} else {
				LoanFeeMethodimple.valueOf(loanFeeMethod.toString())
						.calcuteFee(loanReg, loanFeeDef);
				loanReg.setLoanFeeMethod(loanFeeMethod);// 手续费收取方式
			}
		}
		calcuteOrig(loanReg);// 填写交易信息
		return loanReg;
	}
	
	// 填写交易信息
	abstract void calcuteOrig(CcsLoanReg loanReg);

	// 本金分配方式
	private enum DistributeMethodImple {
		/**
		 * 按月平分
		 */
		F {
			void calcustPrin(CcsLoanReg loanReg) throws ProcessException {
				BigDecimal loanInitPrin = loanReg.getLoanInitPrin();
				Integer term = loanReg.getLoanInitTerm();
				// 计算每期还款金额=总的本金/期数
				BigDecimal fixedPmtPrin = loanInitPrin
						.divideToIntegralValue(new BigDecimal(term));
				// 首期=每期还款金额
				loanReg.setLoanFirstTermPrin(fixedPmtPrin);
				// 每期还款金额
				loanReg.setLoanFixedPmtPrin(fixedPmtPrin);
				// 末期 = 总本金-（每期金额*期数-1）
				loanReg.setLoanFinalTermPrin(loanInitPrin
						.subtract(fixedPmtPrin
								.multiply(new BigDecimal(term - 1))).setScale(2, BigDecimal.ROUND_HALF_UP));
			}
		},
		/**
		 * 分配表
		 */
		S {
			void calcustPrin(CcsLoanReg loanReg) throws ProcessException {
				throw new ProcessException("一期不支持分摊表分期");
			}
		};
		abstract void calcustPrin(CcsLoanReg loanReg) throws ProcessException;
	}

	// 分期手续费收取方式
	private enum LoanFeeMethodimple {
		/**
		 * 一次性收取
		 */
		F {
			@Override
			void calcuteFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {
				// 每期的手续费
				BigDecimal loanFixedFee = CalcMethodimple.valueOf(
						loanFeeDef.loanFeeCalcMethod.toString()).calcuteFee(
						loanReg.getLoanInitPrin(), loanFeeDef);
				//总的手续费
				BigDecimal loanInitFee = loanFixedFee
						.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(2, BigDecimal.ROUND_HALF_UP);
				// 分期总手续费 = 每期的手续费*分期期数
				loanReg.setLoanInitFee(loanInitFee);// 分期总手续费
				loanReg.setLoanFirstTermFee(loanInitFee);// 分期首期手续费

				loanReg.setLoanFixedFee(BigDecimal.ZERO);// 分期每期总手续费
				loanReg.setLoanFinalTermFee(BigDecimal.ZERO);// 分期末期手续费

			}
		},
		/**
		 * 分期收取
		 */
		E {
			@Override
			void calcuteFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef) {

				// 分期期数
				Integer term = loanReg.getLoanInitTerm();

				// 每期的手续费
				BigDecimal loanFixedFee = CalcMethodimple.valueOf(
						loanFeeDef.loanFeeCalcMethod.toString()).calcuteFee(
						loanReg.getLoanInitPrin(), loanFeeDef);
				// 总的手续费
				BigDecimal loanInitFee = loanFixedFee
						.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(2, BigDecimal.ROUND_HALF_UP);

				// 分期总手续费 = 每期的手续费*分期期数
				loanReg.setLoanInitFee(loanInitFee);// 分期总手续费

				loanReg.setLoanFixedFee(loanFixedFee);// 分期每期总手续费
				loanReg.setLoanFirstTermFee(loanFixedFee);// 分期首期手续费
				loanReg.setLoanFinalTermFee(loanInitFee
						.subtract(loanFixedFee.multiply(new BigDecimal(
								term - 1))).setScale(2, BigDecimal.ROUND_HALF_UP));// 分期末期手续费
			}
		};
		abstract void calcuteFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef);
	}
	

	// 手续费计算方式 CalcMethodimple
	public enum CalcMethodimple {
		// 按照百分比计算
		R {
			@Override
			BigDecimal calcuteFee(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef) {
				return loanInitPrin.multiply(loanFeeDef.feeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
			}
		},
		// 固定金额
		A {
			@Override
			BigDecimal calcuteFee(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef) {
				return loanFeeDef.feeAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
			}
		};

		// 计算分期手续费
		abstract BigDecimal calcuteFee(BigDecimal loanInitPrin,
				LoanFeeDef loanFeeDef);
	}
	
	
	/**
	 * 展期手续费计算
	 * @param reTerm 展期总期数
	 * @param loan 
	 * @param acct
	 * @param loanFeeMethod 原分期手续费收取方式，不能不为null
	 * @param loanCode
	 * @param busDate
	 * @return
	 * @throws ProcessException
	 */
	public CcsLoanReg genRescheduleLoanReg(Integer reTerm, CcsLoan loan, CcsAcct acct, 
			LoanFeeMethod loanFeeMethod, String loanCode,Date busDate) throws ProcessException {
		CcsLoanReg loanReg = new CcsLoanReg();
		loanReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		loanReg.setAcctNbr(acct.getAcctNbr());
		loanReg.setAcctType(acct.getAcctType());
		loanReg.setRegisterDate(busDate);
		loanReg.setRequestTime(busDate);
		loanReg.setLogicCardNbr(acct.getDefaultLogicCardNbr());
		loanReg.setCardNbr(loan.getCardNbr());
		loanReg.setRefNbr(loan.getRefNbr());
		loanReg.setLoanType(loanType);
		loanReg.setLoanRegStatus(LoanRegStatus.A);
		loanReg.setLoanInitTerm(reTerm);// 分期期数
		loanReg.setLoanInitPrin(loan.getLoanInitPrin());// 分期总本金
		loanReg.setLoanCode(loanCode);//分期计划代码
		loanReg.setLoanAction(LoanAction.R);
		loanReg.setMatched(Indicator.N);
		
		// 计算本金
		RescheduleDistributeMethodImple.valueOf(loanFeeDef.distributeMethod.toString())
				.rescheduleCalcustPrin(loanReg, loan);
		{
			//计算手续费
			RescheduleLoanFeeMethodimple.valueOf(loanFeeMethod.toString()).rescheduleCalcuteFee(loanReg, loanFeeDef, loan);
			loanReg.setLoanFeeMethod(loanFeeMethod);// 手续费收取方式
		}
		calcuteOrig(loanReg);
		
		return loanReg;
	}
	
	/**
	 * 展期计算本金
* @author fanghj
	 * @date 2013-7-17  下午2:13:13
	 * @version 1.0
	 */
	public enum RescheduleDistributeMethodImple{
		/**
		 * 按月平分
		 */
		F {
			void rescheduleCalcustPrin(CcsLoanReg loanReg, CcsLoan loan) throws ProcessException {
				//用未出账单本金和（展期期数 - 已执行期数）来计算展期后每期应还金额
				BigDecimal loanInitPrin = loan.getUnstmtPrin();
				Integer term = loanReg.getLoanInitTerm() - loan.getCurrTerm();
				// 计算每期还款金额 = 未出账单的本金/期数
				BigDecimal fixedPmtPrin = loanInitPrin
						.divideToIntegralValue(new BigDecimal(term));
				// 首期 = 每期还款金额
				loanReg.setLoanFirstTermPrin(fixedPmtPrin);
				// 每期还款金额
				loanReg.setLoanFixedPmtPrin(fixedPmtPrin);
				// 末期 = 总本金-（每期金额*期数-1）
				loanReg.setLoanFinalTermPrin(loanInitPrin
						.subtract(fixedPmtPrin
								.multiply(new BigDecimal(term - 1))));
			}	
			
		},
		/**
		 * 分配表
		 */
		S {
			void rescheduleCalcustPrin(CcsLoanReg loanReg, CcsLoan loan) throws ProcessException {
				throw new ProcessException("一期不支持分摊表分期");
			}
		};
		abstract void rescheduleCalcustPrin(CcsLoanReg loanReg, CcsLoan loan) throws ProcessException;
	}
	
	/**
	 * 展期手续费收取方式
* @author fanghj
	 * @date 2013-7-17  下午2:15:56
	 * @version 1.0
	 */
	private enum RescheduleLoanFeeMethodimple {
		/**
		 * 一次性收取
		 */
		F {
			@Override
			void rescheduleCalcuteFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef, CcsLoan loan) {
				// 每期的手续费
				BigDecimal loanFixedFee = RescheduleCalcMethodimple.valueOf(
						loanFeeDef.loanFeeCalcMethod.toString()).rescheduleCalcuteFee(
								loanReg.getLoanInitPrin(), loanFeeDef);
				//总的手续费
				BigDecimal loanInitFee = loanFixedFee
						.multiply(new BigDecimal(loanReg.getLoanInitTerm())).setScale(2, BigDecimal.ROUND_HALF_UP);
				// 分期总手续费 = 每期的手续费*分期期数
				loanReg.setLoanInitFee(loanInitFee);// 分期总手续费
				loanReg.setLoanFirstTermFee(loanInitFee);// 分期首期手续费

				loanReg.setLoanFixedFee(BigDecimal.ZERO);// 分期每期手续费
				loanReg.setLoanFinalTermFee(BigDecimal.ZERO);// 分期末期手续费

			}
		},
		/**
		 * 分期收取
		 */
		E {
			@Override
			void rescheduleCalcuteFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef, CcsLoan loan) {

				// 展期后每期的手续费
				BigDecimal loanFixedFee = RescheduleCalcMethodimple.valueOf(loanFeeDef.loanFeeCalcMethod.toString())
						.rescheduleCalcuteFee(loanReg.getLoanInitPrin(), loanFeeDef);
				
				// 展期后总手续费 = 展期后每期的手续费*展期后剩余期数
				BigDecimal loanInitFee = loanFixedFee.multiply(new BigDecimal(loanReg.getLoanInitTerm()-loan.getCurrTerm()))
						.setScale(2, BigDecimal.ROUND_HALF_UP);
				
				loanReg.setLoanInitFee(loanInitFee);// 分期总手续费				
				loanReg.setLoanFixedFee(loanFixedFee);// 分期每期总手续费
				loanReg.setLoanFirstTermFee(loanFixedFee);// 分期首期手续费
				loanReg.setLoanFinalTermFee(loanInitFee.subtract(loanFixedFee.multiply(new BigDecimal(loanReg.getLoanInitTerm() - loan.getCurrTerm() - 1)))
						.setScale(2, BigDecimal.ROUND_HALF_UP));// 分期末期手续费
			}
		};
		abstract void rescheduleCalcuteFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef, CcsLoan loan);
	}
	

	/**
	 * 展期手续费计算方式
* @author fanghj
	 * @date 2013-7-17  下午2:17:21
	 * @version 1.0
	 */
	public enum RescheduleCalcMethodimple {
		// 按照百分比计算
		R {
			@Override
			BigDecimal rescheduleCalcuteFee(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef) {
				return loanInitPrin.multiply(loanFeeDef.feeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
			}
		},
		// 固定金额
		A {
			@Override
			BigDecimal rescheduleCalcuteFee(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef) {
				return loanFeeDef.feeAmount;
			}
		};

		// 计算分期手续费
		abstract BigDecimal rescheduleCalcuteFee(BigDecimal loanInitPrin,
				LoanFeeDef loanFeeDef);
	}


	/**
	 * 小额贷展期每期还款金额、手续费计算  【已经废弃不用】
	 * @param reTerm
	 * @param loan
	 * @param acct
	 * @param loanFeeMethod
	 * @param loanCode
	 * @param busDate
	 * @return
	 * @throws ProcessException
	 */
	public CcsLoanReg genMcExtendLoanReg(Integer reTerm, CcsLoan loan, 
			CcsAcct acct, LoanFeeMethod loanFeeMethod, String loanCode,Date busDate) throws ProcessException{
		CcsLoanReg loanReg = new CcsLoanReg();
		loanReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		loanReg.setAcctNbr(acct.getAcctNbr());
		loanReg.setAcctType(acct.getAcctType());
		loanReg.setRegisterDate(busDate);
		loanReg.setRequestTime(busDate);
		loanReg.setLogicCardNbr(acct.getDefaultLogicCardNbr());
		loanReg.setCardNbr(loan.getCardNbr());
		loanReg.setRefNbr(loan.getRefNbr());
		loanReg.setLoanType(loanType);
		loanReg.setLoanRegStatus(LoanRegStatus.N);
		loanReg.setLoanInitTerm(loan.getLoanInitTerm());// 分期期数
		loanReg.setLoanInitPrin(loan.getLoanInitPrin());// 分期总本金
		loanReg.setLoanCode(loanCode);//分期计划代码
		loanReg.setLoanAction(LoanAction.R);
		loanReg.setExtendTerm(reTerm);
		
		//计算本金
		MicroCriditRescheduleDistributeMethodImple.valueOf(loanFeeDef.distributeMethod.name())
			.microCriditRescheduleCalcustPrin(loanReg, loan);
		{
			MicroCriditRescheduleLoanFeeMethodimple.valueOf(loanFeeMethod.name())
			.microCriditRescheduleCalcuteFee(loanReg, loanFeeDef, loan.getUnstmtPrin());
			loanReg.setLoanFeeMethod(loanFeeMethod);
		}
		calcuteOrig(loanReg);
		return loanReg;
	}
	
	/**
	 * 小额贷展期本金分配方式
* @author fanghj
	 * @time 2014-4-3 下午3:54:43
	 */
	public enum MicroCriditRescheduleDistributeMethodImple{
		/**
		 * 按月平分
		 */
		F {
			void microCriditRescheduleCalcustPrin(CcsLoanReg loanReg, CcsLoan loan) throws ProcessException {
				//用未出账单本金和（展期期数 - 已执行期数）来计算展期后每期应还金额
				BigDecimal loanInitPrin = loan.getUnstmtPrin();
				Integer term = loanReg.getLoanInitTerm() - loan.getCurrTerm();
				// 计算每期还款金额 = 未出账单的本金/期数
				BigDecimal fixedPmtPrin = loanInitPrin
						.divideToIntegralValue(new BigDecimal(term));
				// 首期 = 每期还款金额
				loanReg.setLoanFirstTermPrin(fixedPmtPrin);
				// 每期还款金额
				loanReg.setLoanFixedPmtPrin(fixedPmtPrin);
				// 末期 = 总本金-（每期金额*期数-1）
				loanReg.setLoanFinalTermPrin(loanInitPrin
						.subtract(fixedPmtPrin
								.multiply(new BigDecimal(term - 1))));
			}	
			
		},
		/**
		 * 分配表
		 */
		S {
			void microCriditRescheduleCalcustPrin(CcsLoanReg loanReg, CcsLoan loan) throws ProcessException {
				throw new ProcessException("一期不支持分摊表分期");
			}
		};
		abstract void microCriditRescheduleCalcustPrin(CcsLoanReg loanReg, CcsLoan loan) throws ProcessException;
	}
	
	/**
	 * 小额贷展期手续费收取方式
* @author fanghj
	 * @time 2014-4-3 下午3:48:53
	 */
	private enum MicroCriditRescheduleLoanFeeMethodimple {
		/**
		 * 一次性收取
		 */
		F {
			@Override
			void microCriditRescheduleCalcuteFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef ,BigDecimal prin) {
				// 每期的手续费
				BigDecimal loanFixedFee = MicroCriditRescheduleCalcMethodimple.valueOf(
						loanFeeDef.loanFeeCalcMethod.toString()).microCriditRescheduleCalcuteFee(
						prin, loanFeeDef);
				//总的手续费
				BigDecimal loanInitFee = loanFixedFee
						.multiply(prin).setScale(2, BigDecimal.ROUND_HALF_UP);
				// 分期总手续费 = 每期的手续费*分期期数
				loanReg.setLoanInitFee(loanInitFee);// 分期总手续费
				loanReg.setLoanFirstTermFee(loanInitFee);// 分期首期手续费

				loanReg.setLoanFixedFee(BigDecimal.ZERO);// 分期每期手续费
				loanReg.setLoanFinalTermFee(BigDecimal.ZERO);// 分期末期手续费

			}
		},
		/**
		 * 分期收取
		 */
		E {
			@Override
			void microCriditRescheduleCalcuteFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef, BigDecimal prin) {

				// 分期期数
				Integer term = loanReg.getLoanInitTerm();

				// 每期的手续费
				BigDecimal loanFixedFee = MicroCriditRescheduleCalcMethodimple.valueOf(
						loanFeeDef.loanFeeCalcMethod.toString()).microCriditRescheduleCalcuteFee(
								prin, loanFeeDef);
				// 总的手续费
				BigDecimal loanInitFee = loanFixedFee
						.multiply(prin).setScale(2, BigDecimal.ROUND_HALF_UP);

				// 分期总手续费 = 每期的手续费*分期期数
				loanReg.setLoanInitFee(loanInitFee);// 分期总手续费

				loanReg.setLoanFixedFee(loanFixedFee);// 分期每期总手续费
				loanReg.setLoanFirstTermFee(loanFixedFee);// 分期首期手续费
				loanReg.setLoanFinalTermFee(loanInitFee
						.subtract(loanFixedFee.multiply(new BigDecimal(
								term - 1))).setScale(2, BigDecimal.ROUND_HALF_UP));// 分期末期手续费
			}
		};
		abstract void microCriditRescheduleCalcuteFee(CcsLoanReg loanReg, LoanFeeDef loanFeeDef, BigDecimal prin);
	}
	
	/**
	 * 小额贷展期手续费计算方式
* @author fanghj
	 * @time 2014-4-3 下午3:01:06
	 */
	public enum MicroCriditRescheduleCalcMethodimple {
		// 按照百分比计算,剩余总本金
		R {
			@Override
			BigDecimal microCriditRescheduleCalcuteFee(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef) {
				return loanInitPrin.multiply(loanFeeDef.feeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
			}
		},
		// 固定金额
		A {
			@Override
			BigDecimal microCriditRescheduleCalcuteFee(BigDecimal loanInitPrin, LoanFeeDef loanFeeDef) {
				return loanFeeDef.feeAmount;
			}
		};

		// 计算分期手续费
		abstract BigDecimal microCriditRescheduleCalcuteFee(BigDecimal loanInitPrin,
				LoanFeeDef loanFeeDef);
	}
	
	/**
	 * 联机授权生成小额贷的注册信息表
	 * 
	 * @param acctNbr
	 * @param acctType
	 * @param cardNbr
	 * @param bizDate
	 * @param sysOlTime
	 * @param chbTransAmt
	 * @param loanType
	 * @param loanCode
	 * @param loanInitTerm
	 * @param loanFeeDef
	 * @param floatRate
	 * @param refNbr
	 * @return
	 */
	public CcsLoanReg genMcAuthLoanReg(Long acctNbr,AccountType acctType, String cardNbr, Date bizDate, Date sysOlTime, BigDecimal chbTransAmt, LoanType loanType, String loanCode, Integer loanInitTerm,
			LoanFeeDef loanFeeDef, BigDecimal floatRate, String refNbr) {
		CcsLoanReg loanReg = new CcsLoanReg();
		loanReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		loanReg.setAcctNbr(acctNbr);
		loanReg.setAcctType(acctType);
		loanReg.setRegisterDate(bizDate);
		loanReg.setRequestTime(sysOlTime);
		loanReg.setLogicCardNbr(cardNbr);
		loanReg.setCardNbr(cardNbr);
		loanReg.setRefNbr(refNbr);
		loanReg.setLoanType(loanType);
		loanReg.setLoanRegStatus(LoanRegStatus.A);
		
		loanReg.setLoanInitTerm(loanInitTerm);
		loanReg.setLoanInitPrin(chbTransAmt);
		loanReg.setLoanInitFee(BigDecimal.ZERO);
		
		loanReg.setLoanFinalTermFee(BigDecimal.ZERO);
		loanReg.setLoanFinalTermPrin(BigDecimal.ZERO);
		
		loanReg.setLoanFirstTermPrin(BigDecimal.ZERO);
		loanReg.setLoanFirstTermFee(BigDecimal.ZERO);
		
		loanReg.setLoanFixedPmtPrin(BigDecimal.ZERO);
		loanReg.setLoanFixedFee(BigDecimal.ZERO);
		
		loanReg.setOrigTxnAmt(chbTransAmt);
		
		loanReg.setLoanFeeMethod(LoanFeeMethod.F);
		loanReg.setLoanCode(loanCode);
		loanReg.setLoanAction(LoanAction.A);
		loanReg.setRemark(null);
		// 新增小额贷字段
		loanReg.setMatched(Indicator.N);
		loanReg.setInterestRate(loanFeeDef.interestRate);
		loanReg.setPenaltyRate(loanFeeDef.penaltyIntTableId);
		loanReg.setCompoundRate(loanFeeDef.compoundIntTableId);
		loanReg.setFloatRate(floatRate);
		loanReg.setValidDate(bizDate);
		// 借据号生成 业务日期[8位]+refNbr[12位]
//		StringBuffer dueBillNo = new StringBuffer();
//		DateFormat df = new SimpleDateFormat("yyyyMMdd");
//		dueBillNo.append(df.format(bizDate));
		
		//dueBillNo.append(StringUtils.leftPad(refNbr == null ? "" : refNbr, 12, "0"));
		//loanReg.setDueBillNo(dueBillNo.toString());
		loanReg.setDueBillNo(refNbr);
		
		// 每期的手续费
		// BigDecimal loanFixedFee = CalcMethodimple.valueOf(loanFeeDef.loanFeeCalcMethod.toString()).calcuteFee(chbTransAmt, loanFeeDef);
		// 总的手续费
		// TODO 贷款无手续费
		// BigDecimal loanInitFee = loanFixedFee.multiply(new BigDecimal(loanInitTerm)).setScale(2, BigDecimal.ROUND_HALF_UP);
		
		return loanReg;
	}
}
