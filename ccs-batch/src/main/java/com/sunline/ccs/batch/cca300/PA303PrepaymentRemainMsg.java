package com.sunline.ccs.batch.cca300;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.common.MessageUtils;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.loan.LoanUtil;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.exchange.MSLoanMsgItem;

public class PA303PrepaymentRemainMsg implements ItemProcessor<CcsLoanReg,MSLoanMsgItem>{
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
	
	private static final Logger logger = LoggerFactory.getLogger(PA303PrepaymentRemainMsg.class);
	@Override
	public MSLoanMsgItem process(CcsLoanReg item) throws Exception {
		if(logger.isDebugEnabled())
			logger.debug("提前还款批量提醒短信开始,合同号["+item.getContrNbr()+"]");
		CcsAcct acct = em.find(CcsAcct.class,new CcsAcctKey(item.getAcctNbr(),item.getAcctType()));
		//取参数前设置机构号
		OrganizationContextHolder.setCurrentOrg(acct.getOrg());
		ProductCredit pc = paramFacility.loadParameter(acct.getProductCd(),ProductCredit.class);
		AccountAttribute aa = paramFacility.loadParameter(pc.accountAttributeId, AccountAttribute.class);
		
		//短信提前天数
		if(aa.prepaymentSmsRemainDays==null||DateUtils.getIntervalDays(batchFacility.getBatchDate(), item.getPreAdDate())<aa.prepaymentSmsRemainDays){
			if(logger.isDebugEnabled())
				logger.debug("无效提前还款提醒,提醒提前天数["+aa.prepaymentSmsRemainDays+"],预约扣款日["+item.getPreAdDate()+"]");
			return null;
		}
		if(DateUtils.getIntervalDays(batchFacility.getBatchDate(), item.getPreAdDate())!=aa.prepaymentSmsRemainDays){
			if(logger.isDebugEnabled())
				logger.debug("未到提前还款提醒日,预约扣款日["+item.getPreAdDate()+"],跳过处理");
			return null;
		}
		MSLoanMsgItem msgItem = new MSLoanMsgItem();
		msgItem.acqId = acct.getAcqId();
		msgItem.loanCode = loanUtil.findLoanCode(acct);
		msgItem.phoneNbr = acct.getMobileNo();
		msgItem.sourceBizSystem = Constants.SOURCE_BIZ_SYSTEM;
		msgItem.serialNo = messageUtils.getMsgSerialNo(acct.getAcctNbr(), batchFacility.getBatchDate());
		msgItem.sourceBizType = Constants.BATCH_MESSAGE_PREPAY_TIPS;
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(item.getPreAdDate());
		msgItem.msgParams = new StringBuffer().append(acct.getName())
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(calendar.get(Calendar.YEAR))
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(calendar.get(Calendar.MONTH)+1)
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(calendar.get(Calendar.DAY_OF_MONTH))
				.append(Constants.BATCH_SMS_SEPARATOR)
				.append(item.getPreAdAmt()).toString();
		return msgItem;
	}
}
