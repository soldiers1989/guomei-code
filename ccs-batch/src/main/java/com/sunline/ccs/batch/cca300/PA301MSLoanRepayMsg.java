package com.sunline.ccs.batch.cca300;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.common.BatchUtils;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.batch.common.MessageUtils;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.loan.LoanUtil;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.SmsInd;
import com.sunline.ppy.dictionary.exchange.MSLoanMsgItem;

public class PA301MSLoanRepayMsg implements ItemProcessor<CcsLoan, MSLoanMsgItem> {
	
	private static final Logger logger = LoggerFactory.getLogger(PA301MSLoanRepayMsg.class);
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private MessageUtils messageUtils;
	
	@Autowired
	private BatchUtils batchUtils;
	
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private LoanUtil loanUtil;
	
	QCcsRepaySchedule s = QCcsRepaySchedule.ccsRepaySchedule;
	
	@Override
	public MSLoanMsgItem process(CcsLoan loan) throws Exception {
		
		OrganizationContextHolder.setCurrentOrg(loan.getOrg());
		
		// 马上贷
		LoanPlan loanPlan = parameterFacility.loadParameter(loan.getLoanCode(), LoanPlan.class);
		if(Ownership.O != loanPlan.ownership)
			return null;
		
		CcsAcct acct = em.find(CcsAcct.class, new CcsAcctKey(loan.getAcctNbr(), loan.getAcctType()));
		CcsAcctO accto = em.find(CcsAcctO.class, new CcsAcctOKey(loan.getAcctNbr(), loan.getAcctType()));

		if(SmsInd.Y != acct.getSmsInd())
			return null;
		
		ProductCredit productCredit = parameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
		AccountAttribute acctAttr =	parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		//支持多次提醒短信 by lizz 20160321 
		String pmtDueLtrPrd = acctAttr.pmtDueLtrPrd;
		//是否发送
		Boolean isSend = false;
		Date noticeDate = null;
		if(!StringUtils.isEmpty(pmtDueLtrPrd) && acct.getPmtDueDate()!=null) {
			for(String days :pmtDueLtrPrd.split(Constants.REMINDER_SMS_ADVANCE_DAYS_SEPARATOR)) {
				// 下一个到期还款日
				Date nextPmtDueDate = rescheduleUtils.getNextPaymentDay(acct.getProductCd(), acct.getNextStmtDate());
				noticeDate = DateUtils.addDays(batchStatusFacility.getBatchDate(), Integer.valueOf(days));
				// 约扣日前days天通知--如果当前批量日期在账户到期还款日之前，用到期还款日与提前天数推算是否发短信，
				//如果批量日期在到期还款日之后（到期还款日到下一个账单日才会更新），用下一个到期还款日推算 --by lizz 20160325
				if(DateUtils.truncatedCompareTo(noticeDate, batchStatusFacility.getBatchDate().before(acct.getPmtDueDate())?acct.getPmtDueDate():nextPmtDueDate, Calendar.DATE) == 0) {
					isSend = true;//符合发送提前天数
					break;
				}
			}
		}else {
			return null;
		}
		
		if(isSend) {
//		Date noticeDate = DateUtils.addDays(batchStatusFacility.getBatchDate(), acctAttr.pmtDueLtrPrd);
		
			CcsRepaySchedule schedule = new JPAQuery(em).from(s)
					.where(s.loanId.eq(loan.getLoanId())
							.and(s.loanPmtDueDate.eq(noticeDate)))
					.singleResult(s);
			if(null == schedule)
				return null;
			
			if(logger.isDebugEnabled()){
				logger.debug("马上贷还款短信提醒,scheduleId:["+schedule.getScheduleId()+"]");
			}
			
			MSLoanMsgItem item = new MSLoanMsgItem();
			
			String custName = acct.getName();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(schedule.getLoanPmtDueDate());
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			BigDecimal repayAmt = schedule.getLoanTermPrin()
					.add(schedule.getLoanTermInt()).add(schedule.getLoanTermFee())
					.add(schedule.getLoanStampdutyAmt()).add(schedule.getLoanInsuranceAmt())
					.add(schedule.getLoanLifeInsuAmt()).add(schedule.getLoanSvcFee())
					.add(schedule.getLoanReplaceSvcFee());
			// 再加上因容忍金额导致可能存在的欠款
			repayAmt = repayAmt.add(mcLoanProvideImpl.genLoanBal(loan));	
			// 再加上溢缴款 
			repayAmt = repayAmt.subtract(mcLoanProvideImpl.genLoanDeposit(loan));	
			//获取账户上的最小还款额   --需要减去未入账贷记金额 by lizz 20160317
			repayAmt = repayAmt.subtract(accto.getMemoCr());
			if(repayAmt.compareTo(BigDecimal.ZERO) > 0) {
				String params = custName + Constants.BATCH_SMS_SEPARATOR
						+ year + Constants.BATCH_SMS_SEPARATOR
						+ String.format("%02d", month) + Constants.BATCH_SMS_SEPARATOR
						+ String.format("%02d", day) + Constants.BATCH_SMS_SEPARATOR
						+ repayAmt.setScale(2, RoundingMode.UP);
				
				item.serialNo = messageUtils.getMsgSerialNo(acct.getAcctNbr(), batchStatusFacility.getBatchDate());
				item.phoneNbr = acct.getMobileNo();
				item.msgParams = params;
				//批量短信文件接口新增四个字段
				item.sourceBizSystem = Constants.SOURCE_BIZ_SYSTEM;				//业务系统
				item.acqId = acct.getAcqId();									//来源结构编号
				//如果合同是建户未进行提款的状态，ccsloan里没有值，这时候要先进行判断
				String loanCode = loanUtil.findLoanCode(acct);
				item.loanCode = loanCode;							//贷款产品代码
				item.sourceBizType = Constants.ACCOUNTING_REPAYMENT_REMINDER;	//业务类型
						
				return item;
			}else {
				return null;
			}
		}
		return null;
	}
}
