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
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.loan.LoanUtil;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.SmsInd;
import com.sunline.ppy.dictionary.exchange.MSLoanMsgItem;

public class PA302WhiteListRepayMsg implements ItemProcessor<CcsLoan, MSLoanMsgItem> {
	
	private static final Logger logger = LoggerFactory.getLogger(PA302WhiteListRepayMsg.class);
	
	@Autowired
	private MessageUtils messageUtils;

	@Autowired
	private BatchUtils batchUtils;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	@Autowired
	private LoanUtil loanUtil;

	@Autowired
	private McLoanProvideImpl mcLoanProvideImpl;
	
	@Override
	public MSLoanMsgItem process(CcsLoan loan) throws Exception {
		
		OrganizationContextHolder.setCurrentOrg(loan.getOrg());
		// 随翼花
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
		if(!StringUtils.isEmpty(pmtDueLtrPrd) && acct.getPmtDueDate()!=null ) {
			Date noticeDate = null;
			for(String days :pmtDueLtrPrd.split(Constants.REMINDER_SMS_ADVANCE_DAYS_SEPARATOR)) {
//				Date nextDdDate = batchUtils.getNextDdDay(acct.getProductCd(), acct.getNextStmtDate());
				Date nextPmtDueDate = rescheduleUtils.getNextPaymentDay(acct.getProductCd(), acct.getNextStmtDate());
				noticeDate = DateUtils.addDays(batchStatusFacility.getBatchDate(), Integer.valueOf(days));
				// 到期还款日前days天通知
				if(DateUtils.truncatedCompareTo(noticeDate, batchStatusFacility.getBatchDate().before(acct.getPmtDueDate())?acct.getPmtDueDate():nextPmtDueDate, Calendar.DATE) == 0) {
					isSend = true;//符合发送提前天数
					break;
				}
			}
		}else {
			return null;
		}
		
		if(isSend) {
			if(logger.isDebugEnabled()){
				logger.debug("白名单还款短信提醒,org:[" + acct.getOrg()
						+ "],acctNbr[" + acct.getAcctNbr()
						+ "],acctType[" + acct.getAcctType()
						+ "]");
			}
			
			MSLoanMsgItem item = new MSLoanMsgItem();
			
			String custName = acct.getName();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(acct.getDdDate() == null? acct.getPmtDueDate() : acct.getDdDate());
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			QCcsPlan q = QCcsPlan.ccsPlan;
			CcsPlan plan = new JPAQuery(em).from(q)
					.where(q.org.eq(acct.getOrg())
							.and(q.acctNbr.eq(acct.getAcctNbr()))
							.and(q.acctType.eq(acct.getAcctType()))
							.and(q.planType.eq(PlanType.L)))
							.orderBy(q.term.desc())
							.singleResult(q);
			if(plan == null)
				return null;
			//取上期账单的应还金额，前提：提醒日在账单日与到期还款日之间  by lizz 20160118
			//获取账户上的最小还款额   --需要减去未入账贷记金额 by lizz 20160317
			BigDecimal repayAmt = acct.getTotDueAmt().subtract(accto.getMemoCr());
			// 再加上溢缴款 
			repayAmt = repayAmt.subtract(mcLoanProvideImpl.genLoanDeposit(loan));
			
			if(repayAmt.compareTo(BigDecimal.ZERO) > 0) {
				/**
				 * 尊敬的{客户姓名}，温馨提醒您的账单还款日期为{4}年{2}月{2}日，到期还款金额为{ }元，请确保您的代扣账户余额充足。
				 */
				String params = custName + Constants.BATCH_SMS_SEPARATOR
						+ year + Constants.BATCH_SMS_SEPARATOR
						+ String.format("%02d", month) + Constants.BATCH_SMS_SEPARATOR
						+ String.format("%02d", day) + Constants.BATCH_SMS_SEPARATOR
						+ repayAmt.setScale(2, RoundingMode.UP);
				
				item.serialNo = messageUtils.getMsgSerialNo(acct.getAcctNbr(), batchStatusFacility.getBatchDate());
				item.phoneNbr = acct.getMobileNo();
				item.msgParams = params;
				//批量短信文件接口新增四个字段
				item.sourceBizSystem = Constants.SOURCE_BIZ_SYSTEM;					//业务系统
				item.acqId = acct.getAcqId();										//来源机构编号
				//如果合同是建户未进行提款的状态，ccsloan里没有值，这时候要先进行判断
				String loanCode = loanUtil.findLoanCode(acct);
				item.loanCode = loanCode;								//贷款产品代码
				item.sourceBizType = Constants.BILL_ACCOUNTING_REPAYMENT_REMINDER;	//业务类型
				
				return item;
			}else {
				return null;
			}
		}
		return null;
		
	}

}
