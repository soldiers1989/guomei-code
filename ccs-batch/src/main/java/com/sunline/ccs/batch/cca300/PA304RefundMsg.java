package com.sunline.ccs.batch.cca300;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.common.MessageUtils;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.loan.LoanUtil;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.service.api.Constants;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.exchange.MSLoanMsgItem;
/**
 * 退货欠款提醒短信-处理类
 * @author lizz
 *
 */
public class PA304RefundMsg implements ItemProcessor<CcsLoanRegHst,MSLoanMsgItem>{
	@Autowired
	private UnifiedParameterFacility paramFacility;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private LoanUtil loanUtil;
	@Autowired
	private MessageUtils messageUtils;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	QCcsRepaySchedule qRepaySchedule = QCcsRepaySchedule.ccsRepaySchedule;
	
	private static final Logger logger = LoggerFactory.getLogger(PA304RefundMsg.class);
	public MSLoanMsgItem process(CcsLoanRegHst item) throws Exception {
		
		Date batDate = batchFacility.getBatchDate();
		if(logger.isDebugEnabled())
			logger.debug("退货欠款批量提醒短信开始,合同号["+item.getContrNbr()+"],批量日期[" + batDate +"]" );
		CcsAcct acct = em.find(CcsAcct.class,new CcsAcctKey(item.getAcctNbr(),item.getAcctType()));
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(acct.getAcctNbr(), acct.getAcctType()));

		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		CcsLoan loan = rCcsLoan.findOne(qCcsLoan.contrNbr.eq(item.getContrNbr()));

		BigDecimal dueAmt = BigDecimal.ZERO;
		Date repayDate = null;
		// 判断有没有大于loan当前term的schedule
		List<CcsRepaySchedule> scheduleList = new JPAQuery(em).from(qRepaySchedule)
				.where(qRepaySchedule.loanId.eq(loan.getLoanId())
				.and(qRepaySchedule.currTerm.gt(loan.getCurrTerm())
				)).list(qRepaySchedule);
		// 计算未到期schedule所有欠款
		BigDecimal scheduleAmt = BigDecimal.ZERO;
		if(scheduleList.size() > 0) {
			for(CcsRepaySchedule ccsRepaySchedule : scheduleList) {
				if(ccsRepaySchedule.getLoanPmtDueDate().compareTo(batDate) > 0){
					repayDate = ccsRepaySchedule.getLoanPmtDueDate();
				}
				scheduleAmt = scheduleAmt.add(ccsRepaySchedule.getLoanTermPrin()==null?BigDecimal.ZERO:ccsRepaySchedule.getLoanTermPrin()
						.add(ccsRepaySchedule.getLoanTermInt()==null?BigDecimal.ZERO:ccsRepaySchedule.getLoanTermInt())
						.add(ccsRepaySchedule.getLoanTermFee()==null?BigDecimal.ZERO:ccsRepaySchedule.getLoanTermFee())
						.add(ccsRepaySchedule.getLoanInsuranceAmt()==null?BigDecimal.ZERO:ccsRepaySchedule.getLoanInsuranceAmt())
						.add(ccsRepaySchedule.getLoanStampdutyAmt()==null?BigDecimal.ZERO:ccsRepaySchedule.getLoanStampdutyAmt())
						.add(ccsRepaySchedule.getLoanLifeInsuAmt()==null?BigDecimal.ZERO:ccsRepaySchedule.getLoanLifeInsuAmt())
						.add(ccsRepaySchedule.getLoanPrepayPkgAmt()==null?BigDecimal.ZERO:ccsRepaySchedule.getLoanPrepayPkgAmt())
						.add(ccsRepaySchedule.getLoanSvcFee()==null?BigDecimal.ZERO:ccsRepaySchedule.getLoanSvcFee())
						.add(ccsRepaySchedule.getLoanReplaceSvcFee()==null?BigDecimal.ZERO:ccsRepaySchedule.getLoanReplaceSvcFee()));
			}
		}
		// 获取转入plan所有往期欠款
		BigDecimal currPlanAmt= mcLoanProvideImpl.genLoanBal(loan);
		// 加上转入计划的非延迟利息-->增加对宽限日判断，宽限日后逾期金额加上转入计划的罚息累计和复利累计
		QCcsPlan qPlan = QCcsPlan.ccsPlan;
		List<CcsPlan> planList = new JPAQuery(em).from(qPlan).where(qPlan.acctNbr.eq(loan.getAcctNbr()).and(qPlan.acctType.eq(loan.getAcctType()))).list(qPlan);
		for (CcsPlan plan : planList) {
			if (PlanType.Q.equals(plan.getPlanType())) {
				currPlanAmt = currPlanAmt.add(plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP));
				if(DateUtils.truncatedCompareTo(batchStatusFacility.getSystemStatus().getBusinessDate(), acct.getGraceDate(), Calendar.DATE) > 0){
					currPlanAmt = currPlanAmt.add(plan.getPenaltyAcru().setScale(2, RoundingMode.HALF_UP)).add(plan.getCompoundAcru().setScale(2, RoundingMode.HALF_UP));
				}
			}
		}
		// 获取溢缴款
		BigDecimal depositAmt = mcLoanProvideImpl.genLoanDeposit(loan);
		
		//计算贷款最终总欠款（扣除未入账金额）
		dueAmt = dueAmt.add(scheduleAmt).add(currPlanAmt).subtract(depositAmt).subtract(accto.getMemoCr()).add(accto.getMemoDb());
		
		if(dueAmt.compareTo(BigDecimal.ZERO) > 0) {

			// 计算最终还款日期
			// 如果没有schedule，且转入plan上的余额为0，说明有累计罚息复利，还款日期仍置为账户下一个账单日
			if(repayDate == null ){
				if(currPlanAmt.compareTo(BigDecimal.ZERO) <= 0){
					repayDate = acct.getNextStmtDate();
				}else{ //如果没有schedule，且转入plan上的余额大于0，日期置为下一天
					repayDate = DateUtils.addDays(batDate, 1);
				}
			}

			MSLoanMsgItem msgItem = new MSLoanMsgItem();
			msgItem.acqId = acct.getAcqId();
			msgItem.loanCode = loanUtil.findLoanCode(acct);
			msgItem.phoneNbr = acct.getMobileNo();
			msgItem.sourceBizSystem = Constants.SOURCE_BIZ_SYSTEM;
			msgItem.serialNo = messageUtils.getMsgSerialNo(acct.getAcctNbr(), batchFacility.getBatchDate());
			msgItem.sourceBizType = Constants.BATCH_RETURN_REMINDER;
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(repayDate);
			msgItem.msgParams = new StringBuffer().append(acct.getName())
					.append(Constants.BATCH_SMS_SEPARATOR)
					.append(acct.getApplicationNo())
					.append(Constants.BATCH_SMS_SEPARATOR)
					.append(calendar.get(Calendar.YEAR))
					.append(Constants.BATCH_SMS_SEPARATOR)
					.append(calendar.get(Calendar.MONTH)+1)
					.append(Constants.BATCH_SMS_SEPARATOR)
					.append(calendar.get(Calendar.DAY_OF_MONTH))
					.append(Constants.BATCH_SMS_SEPARATOR)
					.append(dueAmt).toString();
			return msgItem;
		} else {
			return null;
		}
	}
}
