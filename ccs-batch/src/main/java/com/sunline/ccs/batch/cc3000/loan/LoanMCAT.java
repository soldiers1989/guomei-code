package com.sunline.ccs.batch.cc3000.loan;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ccs.batch.cc3000.S3001LoanHandler;
import com.sunline.ccs.batch.cc3000.U3001LoanPrepare;
import com.sunline.ccs.batch.common.TxnPrepare;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPostingTmp;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;

/**
 * @see 类名：LoanMCAT
 * @see 描述：小额贷款-随借随还
 *
 * @see 创建日期：   2015-6-23下午7:46:37
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class LoanMCAT extends Loan {

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private U3001LoanPrepare loanPrepare;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private TxnPrepare txnPrepare;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	@Override
	public boolean needApprove() {
		return true;
	}
	
	@Override
	public boolean needTimeOut() {
		return false;
	}
	
	@Override
	public boolean needMatchTxn() {
		return false;
	}
	
	@Override
	public boolean isOnline() {
		return false;
	}
	
	@Override
	public void add(CcsLoanReg loanReg) throws Exception {
		// 确定是否已存在loan
		QCcsLoan qTmLoan = QCcsLoan.ccsLoan;
		CcsLoan loan = new JPAQuery(em).from(qTmLoan)
				.where(qTmLoan.logicCardNbr.eq(loanReg.getLogicCardNbr())
						.and(qTmLoan.loanType.eq(LoanType.MCAT))
						.and(qTmLoan.loanCode.eq(loanReg.getLoanCode()))
						.and(qTmLoan.remainTerm.ne(0)))
				.singleResult(qTmLoan);
		// TODO 贷款上增加参数，是否合并贷款，为以后扩展3年随借随还，1年1还情况
		LoanFeeDef loanFeeDef = null;
		CcsCard card = queryFacility.getCardByCardNbr(loanReg.getCardNbr());
		CcsAcct acct = queryFacility.getAcctByAcctNbr(AccountType.E, card.getAcctNbr());
		if(loan == null){
			// 创建分期信息表记录
			CcsLoan newLoan = loanPrepare.generateLoan(loanReg);
			loanFeeDef = rescheduleUtils.getLoanFeeDef(loanReg.getLoanCode(), loanReg.getLoanInitTerm(), loanReg.getLoanInitPrin(),loanReg.getLoanFeeDefId());
			if(loanFeeDef == null){
				throw new IllegalArgumentException("未获取到分期参数loanFeeDef,分期申请顺序号=["+loanReg.getRegisterId()+"]LoanCode=["+loanReg.getLoanCode()+"],Term=["+loanReg.getLoanInitTerm()+"],Prin=["+loanReg.getLoanInitPrin()+"]");
			}
			// 是否使用协议利率
			if(loanReg.getAgreementRateInd().equals(Indicator.Y)){
				loan = setLoanRate(newLoan, loanReg, loanFeeDef,true);
			}else{
				loan = setLoanRate(newLoan, loanReg, loanFeeDef, false);
			}
			// 修正到期日期
			// 马上项目修正随借随还到期日期，取账户有效期，账户有效期=合同有效期，如合同有效期未上送，则根据随借随还贷款产品最长期数计算
			newLoan.setLoanExpireDate(acct.getAcctExpireDate());
			// 修正期数及剩余期数
			// 马上项目修正随借随还期数计算，联机建立loanReg的时候直接取随借随还贷款产品定价的期数
			//int term = DateUtils.getMonthInterval(acct.getNextStmtDate(), acct.getAcctExpireDate()) +2;
			//newLoan.setLoanInitTerm(term);
			newLoan.setRemainTerm(newLoan.getLoanInitTerm());
			
			em.persist(newLoan);
		}else{
			// 更新loan的信息
			loan.setLoanInitPrin(loan.getLoanInitPrin().add(loanReg.getLoanInitPrin()));
			loan.setLoanInitFee(loan.getLoanInitFee().add(loanReg.getLoanInitFee()));
			loan.setUnstmtPrin(loan.getUnstmtPrin().add(loanReg.getLoanInitPrin()));
			loan.setUnstmtFee(loan.getUnstmtFee().add(loanReg.getLoanInitFee()));
			loan.setLoanPrinXfrout(loan.getLoanPrinXfrout().add(loanReg.getLoanInitPrin()));
			loan.setLoanFeeXfrout(loan.getLoanFeeXfrout().add(loanReg.getLoanInitFee()));
			em.persist(loan);
			
			// 把refNbr拉到ttTxnPost
			QCcsPostingTmp qTtTxnPost = QCcsPostingTmp.ccsPostingTmp;
			CcsPostingTmp post = new JPAQuery(em).from(qTtTxnPost)
					.where(qTtTxnPost.logicCardNbr.eq(loanReg.getLogicCardNbr()).and(qTtTxnPost.refNbr.eq(loanReg.getRefNbr())))
					.singleResult(qTtTxnPost);
			if(post != null){
				post.setRefNbr(loan.getRefNbr());
				em.persist(post);
			}else{
				throw new IllegalArgumentException("未获取到分期参数loanFeeDef,分期申请顺序号=["+loanReg.getRegisterId()+"]");
			}
		}
		
	}

	@Override
	public void reschedule(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) throws Exception {
		
	}

	@Override
	public void prepayment(S3001LoanHandler output, CcsLoanReg loanReg,boolean isToday) throws Exception {
		
	}

	@Override
	public void shorten(S3001LoanHandler output, CcsLoanReg loanReg, CcsLoan loan) {
		// TODO Auto-generated method stub
		
	}


}
