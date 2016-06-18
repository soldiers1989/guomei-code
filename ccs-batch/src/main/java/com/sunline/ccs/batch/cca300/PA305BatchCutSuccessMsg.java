package com.sunline.ccs.batch.cca300;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.common.MessageUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.loan.LoanUtil;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.service.api.Constants;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.exchange.MSLoanMsgItem;
/**
 * 批扣成功通知客户短信-处理类
 *  1.正常扣款和提前结清扣款成功，没有扣款拆分按正常发送一笔扣款成功短信。
 *  2.逾期后扣款成功处理：逾期用户扣款，如果是拆分多笔批量扣款，需程序处理为最终扣款成功叠加总额，发送一条短信。
 * @author lizz
 *
 */
public class PA305BatchCutSuccessMsg implements ItemProcessor<BatchCutSmsInfo,MSLoanMsgItem>{
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
	
	private static final Logger logger = LoggerFactory.getLogger(PA305BatchCutSuccessMsg.class);
	public MSLoanMsgItem process(BatchCutSmsInfo item) throws Exception {
		
		Date batDate = batchFacility.getBatchDate();
		CcsOrder order = item.getOrder();
		CcsOrderHst orderHst = item.getOrderHst();
		BigDecimal txnAmt = BigDecimal.ZERO;
		BigDecimal successAmt = BigDecimal.ZERO;
		Date optDate = null;
		Date orderBusinessTime = null;
		if(logger.isDebugEnabled()) {
			if(order != null) {
				logger.debug("批扣成功通知客户短信开始,合同号["+order.getContrNbr()+"],批量日期[" + batDate +"]" );
			}else if(orderHst != null) {
				logger.debug("批扣成功通知客户短信开始,合同号["+orderHst.getContrNbr()+"],批量日期[" + batDate +"]" );
			}else {
				return null;
			}
		}
		if(order != null) {
			txnAmt = order.getTxnAmt();
			successAmt = order.getSuccessAmt();
			optDate = order.getOptDatetime();
			orderBusinessTime = order.getBusinessDate();
		}else if(orderHst != null) {
			txnAmt = orderHst.getTxnAmt();
			successAmt = orderHst.getSuccessAmt();
			optDate = orderHst.getOptDatetime();
			orderBusinessTime = orderHst.getBusinessDate();
		}else {
			return null;
		}
		//
		if(successAmt.compareTo(BigDecimal.ZERO) <=0 ) return null;
		
		CcsAcct acct = item.getAcct();
		CcsLoan loan = item.getLoan();
		MSLoanMsgItem msgItem = new MSLoanMsgItem();
		msgItem.acqId = acct.getAcqId();
		msgItem.loanCode = loanUtil.findLoanCode(acct);
		msgItem.phoneNbr = acct.getMobileNo();
		msgItem.sourceBizSystem = Constants.SOURCE_BIZ_SYSTEM;
		msgItem.serialNo = messageUtils.getMsgSerialNo(acct.getAcctNbr(), batchFacility.getBatchDate());
		msgItem.sourceBizType = Constants.BATCH_CUT_SUCCESS_REMINDER;
		Calendar calendar = GregorianCalendar.getInstance();
		//正常扣款和提前结清扣款的取订单业务时间，逾期扣款的订单去逾期起始日期
		calendar.setTime(loan.getOverdueDate() == null ? orderBusinessTime : loan.getOverdueDate());
		Calendar cutCalendar = GregorianCalendar.getInstance();
		//取回盘时间
		cutCalendar.setTime(optDate);
		msgItem.msgParams = new StringBuffer().append(acct.getName())
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(calendar.get(Calendar.YEAR))
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(calendar.get(Calendar.MONTH)+1)
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(calendar.get(Calendar.DAY_OF_MONTH))
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(txnAmt)
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(acct.getDdBankAcctNbr().substring(acct.getDdBankAcctNbr().length()-4))
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(cutCalendar.get(Calendar.YEAR))
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(cutCalendar.get(Calendar.MONTH)+1)
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(cutCalendar.get(Calendar.DAY_OF_MONTH))
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(successAmt).toString();
		return msgItem;
	}
}
