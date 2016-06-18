package com.sunline.ccs.batch.cc3000;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.cc3000.loan.Loan;
import com.sunline.ccs.batch.cc3000.loan.LoanFactory;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsSettleClaim;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.loan.TrialResp;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;

/**
 * 分期交易处理（消费转分期、账单转分期、商户分期、展期、提前还款）
 * 
 * 生成分期信息表记录；生成金融交易流水；生成分期注册信息历史 ；生成分期报表;
 * 
* @author fanghj
 */
public class P3001LoanHandler implements ItemProcessor<CcsAcct, S3001LoanHandler> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String ERR_MORE_THAN_UNMATCHDBRTNPRD = "超过额度冻结天数";// 异常：超过未匹配借记授权额度冻结天数
	private static final String ERR_NO_APPROVE = "不需要审批";
	
	@Autowired
	private Card2ProdctAcctFacility parameterFacility;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private LoanFactory loanFactory;
	@Autowired
	private U3001LoanPrepare loanPrepare;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	
	
	@Override
	public S3001LoanHandler process(CcsAcct acct) throws Exception {
		OrganizationContextHolder.setCurrentOrg(acct.getOrg());
		
		S3001LoanHandler output = new S3001LoanHandler();
		
		QCcsLoanReg q = QCcsLoanReg.ccsLoanReg;
		List<CcsLoanReg> loanRegs = new JPAQuery(em).from(q).where(q.acctNbr.eq(acct.getAcctNbr())
					.and(q.acctType.eq(acct.getAcctType()))
					.and(q.registerDate.loe(batchFacility.getBatchDate()))).list(q);
		for(CcsLoanReg loanReg:loanRegs){
			
			try {
				Loan l = loanFactory.defineLoan(loanReg);
				
				// 注册待审批
				if(loanReg.getLoanRegStatus() == LoanRegStatus.N){
					if(l.needApprove()){
						if(isTimeOut(loanReg)){
							loanReg.setRemark(ERR_MORE_THAN_UNMATCHDBRTNPRD); // 超期删除
							loanReg.setLoanRegStatus(LoanRegStatus.T);
						}else{
							continue;// 未超期不处理
						}
					}else{
						loanReg.setRemark(ERR_NO_APPROVE);
						loanReg.setLoanRegStatus(LoanRegStatus.T);
					}
				}else if(loanReg.getLoanRegStatus() == LoanRegStatus.C){
					//处理中,不处理
					continue;
				}else if(loanReg.getLoanRegStatus() == LoanRegStatus.F ){
					if(loanReg.getLoanType() != LoanType.MCAT){
						// 放款失败,不处理
						continue;
					}
					//不处理
					
				} 
				// 通过
				else if(loanReg.getLoanRegStatus() == LoanRegStatus.A || loanReg.getLoanRegStatus() == LoanRegStatus.P){
					CcsLoan loan;
					switch (loanReg.getLoanAction()){
					case A: 
						// 需要匹配清算:商户分期
						if(l.needMatchTxn()){
							// 是否超期
							if(isTimeOut(loanReg)){
								loanReg.setRemark(ERR_MORE_THAN_UNMATCHDBRTNPRD);
								loanReg.setLoanRegStatus(LoanRegStatus.T);
								// TODO 超期出异常报表
							}else{
								CcsPostingTmp txnPost = loanPrepare.matchPostingTmp(loanReg);
								if(txnPost != null){
									loanReg.setMatched(Indicator.Y);
								}else{
									continue;// 未匹配到清算不处理
								}
							}
						}
						
						if(l.isOnline()){
							// 需要校验是否超期:现金分期
							if(l.needTimeOut()){
								if(isTimeOut(loanReg)){
									loanReg.setRemark(ERR_MORE_THAN_UNMATCHDBRTNPRD);
									loanReg.setLoanRegStatus(LoanRegStatus.T);
								}else{
									continue; // 未超期不处理
								}
							}
						}
						
						if(loanReg.getLoanRegStatus()!=LoanRegStatus.T){
							if(loanReg.getLoanRegStatus()==LoanRegStatus.A){
								loanReg.setLoanRegStatus(LoanRegStatus.B);
							}
							
							l.setBefInitTerm(0);
							l.setBefInitPrin(BigDecimal.ZERO);
							l.add(loanReg);
						}
						break;
						
					case R: 
						loan = loanPrepare.matchCcsLoan(loanReg);
						if(loan!=null){
							l.setBefInitTerm(loan.getLoanInitTerm());
							l.setBefInitPrin(loan.getLoanInitPrin());
							l.reschedule(output, loanReg, loan);
						}
						break;
						
					case S: 
						loan = loanPrepare.matchCcsLoan(loanReg);
						if(loan!=null){
							l.setBefInitTerm(loan.getLoanInitTerm());
							l.setBefInitPrin(loan.getLoanInitPrin());
							l.shorten(output, loanReg, loan);
						}
						break;
						
					case P: 
						//当日不结清，账单日结清
						l.prepayment(output, loanReg,false);
						break;
					case T: 
						// 退货交易， 当日结清
						l.prepayment(output, loanReg,true);
						break;
					case O: 
						//预约提前还款
						//// 预约还款日=当前批量日期，或上一批量日期<预约还款日<当前批量日期，否则退出
						//扣款标示为Y
						//当日结清
						if (batchFacility.getBatchDate().compareTo(loanReg.getPreAdDate())>=0 && Indicator.Y == loanReg.getDdRspFlag() 
								&& batchFacility.getBatchDate().compareTo(loanReg.getValidDate())>=0){
							l.prepayment(output, loanReg,true);
						}else if(Indicator.N == loanReg.getDdRspFlag()){
							//如果扣款失败，考虑未匹配金额
							loan = loanPrepare.matchCcsLoan(loanReg);
							QCcsPlan qPlan = QCcsPlan.ccsPlan;
							List<CcsPlan> plans = new JPAQuery(em).from(qPlan).where(qPlan.acctNbr.eq(loan.getAcctNbr()).and(qPlan.acctType.eq(loan.getAcctType()))).list(qPlan);
							BigDecimal trAmt = mcLoanProvideImpl.mCLoanTodaySettlement(loan,loanReg.getRegisterDate(), batchFacility.getSystemStatus().getBusinessDate(), LoanUsage.M, new TrialResp(), plans,loanReg).getTotalAMT();
							// 试算金额能结清
							if(trAmt.compareTo(BigDecimal.ZERO)<=0){
								l.prepayment(output, loanReg, true);
							}else if(batchFacility.getBatchDate().compareTo(loanReg.getPreAdDate())>=0
								&& (null == loanReg.getValidDate()|| batchFacility.getBatchDate().compareTo(loanReg.getValidDate())>=0)){
							//如果有效期失效，不做操作，清理
							break;
							}
						}else {
							//如果未到约定扣款日，或者过了扣款日，没有明确处理结果，不处理
							continue;
						}
						break;
					case C: 
						//理赔结清
						//现不需要判断扣款标示为Y，理赔80天就终止
						//当日结清 转移理赔
						loan = loanPrepare.matchCcsLoan(loanReg);
						if(loan!=null && loan.getTerminalReasonCd()!=null && loan.getTerminalReasonCd().equals(LoanTerminateReason.C)){
							//loan已经终止，不做操作
						}else{
							if(logger.isDebugEnabled()){
								logger.debug("贷款开始理赔，贷款id:["+loan.getLoanId()+"]");
							}
							
							this.createCcsSettleClaim(loanReg,loan);
							//不管理赔扣款结果是否成功，都执行贷款终止操作
							l.prepayment(output, loanReg,true);
						}
						break;
					case D:
					case U:
						//延迟还款、账单日变更在日间完成，直接清理
						break;
					default : throw new IllegalArgumentException("分期行动类型不存在，分期行动类型" +loanReg.getLoanAction()+"，申请号{}" +loanReg.getRegisterId());
					}
				}
				// 实时放款成功、自动消费转分期、贷款放款
				else if(loanReg.getLoanRegStatus() == LoanRegStatus.S
						 || loanReg.getLoanRegStatus() == LoanRegStatus.R){
					l.setBefInitTerm(0);
					l.setBefInitPrin(BigDecimal.ZERO);
					l.add(loanReg);
				}
				// 拒绝、撤销
				else if(loanReg.getLoanRegStatus() == LoanRegStatus.D
						|| loanReg.getLoanRegStatus() == LoanRegStatus.V){
					// do nothing
					//转移到历史表
				}
				
				// 生成报表
				output.getLoanSuccessRpt().add(loanPrepare.makeLoanSuccessItem(loanReg));
				
				// loanReg存历史
				em.persist(loanPrepare.generateLoanRegHst(loanReg, l.getBefInitTerm(), l.getBefInitPrin()));
				em.remove(loanReg);
				
			} catch (Exception e) {
				logger.error("分期注册信息处理异常, 申请号{}", loanReg.getRegisterId());
				throw e;
			}
		}
		return output;
	}


	/**
	 * 是否超期
	 * 
	 * @param loanReg
	 * @return
	 */
	private boolean isTimeOut(CcsLoanReg loanReg) {
		ProductCredit productCr = parameterFacility.CardNoToProductCr(loanReg.getCardNbr());
		return DateUtils.truncatedCompareTo(batchFacility.getBatchDate(), DateUtils.addDays(loanReg.getRegisterDate(), productCr.unmatchDbRtnPrd), Calendar.DATE) >=0;
	}
	

	/**
	 * 转移理赔代偿表，理赔代偿的金额进行重算，
	 * @param item
	 * @param loan
	 * @throws Exception 
	 */
	private void createCcsSettleClaim(CcsLoanReg loanReg,CcsLoan loan) throws Exception{
		CcsAcct acct = em.find(CcsAcct.class, new CcsAcctKey(loanReg.getAcctNbr(), loanReg.getAcctType()));
		
		TrialResp trialResp = new TrialResp();
		Date batchDate = batchFacility.getBatchDate();
		QCcsPlan qPlan = QCcsPlan.ccsPlan;
		List<CcsPlan> plans =new JPAQuery(em)
			.from(qPlan)
			.where(qPlan.acctNbr.eq(loanReg.getAcctNbr()).and(qPlan.acctType.eq(loanReg.getAcctType())))
			.list(qPlan);
		trialResp = mcLoanProvideImpl.mCLoanTodaySettlement(loan,null, batchDate, LoanUsage.C, trialResp, plans,null);
		
		CcsSettleClaim ccsSettleClaim = new CcsSettleClaim();
		ccsSettleClaim.setOrg(loan.getOrg());
		ccsSettleClaim.setAcctNbr(loan.getAcctNbr());
		ccsSettleClaim.setCardNbr(loan.getCardNbr());
		ccsSettleClaim.setLoanId(loan.getLoanId());
		ccsSettleClaim.setContrNbr(loan.getContrNbr());
		ccsSettleClaim.setDueBillNo(loan.getDueBillNo());
		ccsSettleClaim.setGuarantyId(loan.getGuarantyId());
		ccsSettleClaim.setRegisterDate(loan.getRegisterDate());
		ccsSettleClaim.setLoanType(loan.getLoanType());
		ccsSettleClaim.setLoanStatus(loan.getLoanStatus());
		ccsSettleClaim.setLoanInitTerm(loan.getLoanInitTerm());
		ccsSettleClaim.setRemainTerm(loan.getRemainTerm());
		ccsSettleClaim.setLoanInitPrin(loan.getLoanInitPrin());
		ccsSettleClaim.setLoanInitFee(loan.getLoanInitFee());
		ccsSettleClaim.setPaidPrincipal(loan.getPaidPrincipal());
		ccsSettleClaim.setPaidInterest(loan.getPaidInterest());
		ccsSettleClaim.setPaidFee(loan.getPaidFee());
		ccsSettleClaim.setCompoundRate(loan.getCompoundRate());
		ccsSettleClaim.setInterestRate(loan.getInterestRate());
		ccsSettleClaim.setPenaltyRate(loan.getPenaltyRate());
		ccsSettleClaim.setOverdueDate(loan.getOverdueDate());
		ccsSettleClaim.setDdBankAcctName(acct.getDdBankAcctName());
		ccsSettleClaim.setDdBankAcctNbr(acct.getDdBankAcctNbr());
		ccsSettleClaim.setDdBankBranch(acct.getDdBankBranch());
		ccsSettleClaim.setDdBankName(acct.getDdBankName());
		ccsSettleClaim.setCurrBal(acct.getCurrBal());
		ccsSettleClaim.setQualGraceBal(acct.getQualGraceBal());
		ccsSettleClaim.setSettleDate(batchDate);
		ccsSettleClaim.setSettleAmt(trialResp.getTotalAMT());
		ccsSettleClaim.setSettlePrincipal(trialResp.getCtdPricinpalAMT().add(trialResp.getPastPricinpalAMT()));
		ccsSettleClaim.setCompensatoryAmt(BigDecimal.ZERO);
		ccsSettleClaim.setLastCompensatoryDate(null);
		ccsSettleClaim.setInsuranceRate(loan.getInsuranceRate());
		ccsSettleClaim.setSettleInterest(trialResp.getCtdInterestAMT().add(trialResp.getPastInterestAMT()));
		ccsSettleClaim.setSettleInsuranceAmt(trialResp.getCtdInsuranceAMT().add(trialResp.getPastInsuranceAMT()));
		ccsSettleClaim.setSettleLifeInsuAmt(trialResp.getCtdLifeInsuFeeAMT().add(trialResp.getPastLifeInsuFeeAMT()));
		ccsSettleClaim.setSettleStampdutyAmt(trialResp.getCtdStampdutyAMT().add(trialResp.getPastStampdutyAMT()));
		ccsSettleClaim.setSettleMulct(trialResp.getCtdMulctAMT().add(trialResp.getPastMulctAMT()));
		ccsSettleClaim.setSettleFlag(loanReg.getDdRspFlag());
		if(loanReg.getDdRspFlag() == Indicator.Y){
			ccsSettleClaim.setSettleSucDate(batchDate);
		}
		ccsSettleClaim.setCompensatoryFlag(null);
		ccsSettleClaim.setActiveDate(loan.getActiveDate());
		// 数据持久化
		em.persist(ccsSettleClaim);

	}

	public static void main(String args[]){
		System.out.println(new Date().compareTo(null));
	}
}
