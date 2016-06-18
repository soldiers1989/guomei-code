package com.sunline.ccs.batch.front;

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
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.LineItem;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsOutsideDdTxn;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOutsideDdTxn;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;

/**
 * PTP扣款
 * 
 * @author Lisy
 *
 */
@Component
public class P6000PTPayment implements ItemProcessor<LineItem<SPtpBatchCutFile>, SFrontInfo> {
	private Logger logger = LoggerFactory.getLogger(P6000PTPayment.class);

	@Autowired
	private RCcsCustomer rCcsCust;
	@Autowired
	private RCcsLoan rCcsLoan;

	@Autowired
	private FrontBatchUtil frontBatchUtil;
	@Autowired
	private RCcsOutsideDdTxn rCcsOutsideDdTxn;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	@PersistenceContext
	private EntityManager em;
	private static final String USER_BATCH = "batch";

	@Override
	public SFrontInfo process(LineItem<SPtpBatchCutFile> lineItem) throws Exception {
		logger.debug("PTP批量扣款开始...");
		
		try {
			SPtpBatchCutFile item = lineItem.getLineObject();

			CcsOutsideDdTxn ccsOutsideDdTxn = new CcsOutsideDdTxn();
			// 公共部分
			ccsOutsideDdTxn.setCreateTime(new Date());
			ccsOutsideDdTxn.setCreateUser(USER_BATCH);
			ccsOutsideDdTxn.setLstUpdTime(new Date());
			ccsOutsideDdTxn.setLstUpdUser(USER_BATCH);
			ccsOutsideDdTxn.setSourceChnl("PTP扣款");
			ccsOutsideDdTxn.setBusinessDate(batchStatusFacility.getSystemStatus().getBusinessDate());
			SFrontInfo info = new SFrontInfo();
			if (StringUtils.isEmpty((item.contractCode)) || item.customerCode == null
					|| item.loanInfoCode == null || item.paybackAmount == null) {
				logger.debug("输入文件格式错误");
				ccsOutsideDdTxn.setErrInd(Indicator.Y);
				ccsOutsideDdTxn.setErrReason("输入文件格式错误");
				ccsOutsideDdTxn.setContrNbr(item.contractCode);
				ccsOutsideDdTxn.setInternalCustomerId(item.customerCode);
//				ccsOutsideDdTxn.setLoanId(item.loanInfoCode);
				ccsOutsideDdTxn.setDueBillNo(item.loanInfoCode);
				ccsOutsideDdTxn.setTxnAmt(item.paybackAmount);
				rCcsOutsideDdTxn.save(ccsOutsideDdTxn);
				return null;
			}

			// 四舍五入保留扣款金额两位小数
			item.paybackAmount = item.paybackAmount.setScale(2, BigDecimal.ROUND_HALF_UP);

			ccsOutsideDdTxn.setContrNbr(item.contractCode);
			ccsOutsideDdTxn.setInternalCustomerId(item.customerCode);
			ccsOutsideDdTxn.setDueBillNo(item.loanInfoCode);
			ccsOutsideDdTxn.setTxnAmt(item.paybackAmount);

			JPAQuery query = new JPAQuery(em);
			QCcsAcct qAcct = QCcsAcct.ccsAcct;
			CcsAcct acct  = query.from(qAcct)
					.where(qAcct.contrNbr.eq(item.contractCode))
					.singleResult(qAcct);
			
			QCcsLoan qLoan = QCcsLoan.ccsLoan;
			CcsLoan loan  = query.from(qLoan)
					.where(qLoan.dueBillNo.eq(item.loanInfoCode))
					.singleResult(qLoan);
			
			info.setAcct(acct);
			info.setLoan(loan);

			ccsOutsideDdTxn.setErrInd(Indicator.Y);
			if(loan == null || acct == null){
				logger.debug("找不到loan或acct,contractCode["+item.contractCode+"]");
				ccsOutsideDdTxn.setErrReason("查询不到对应loan或acct信息");
				rCcsOutsideDdTxn.save(ccsOutsideDdTxn);
				return null;
			}
			
			CcsCustomer cust = rCcsCust.findOne(acct.getCustId());
			info.setCust(cust);

			// 客户-账户不匹配
			if (!item.customerCode.equals(cust.getInternalCustomerId())) {
				logger.debug("客账不匹配!");
				ccsOutsideDdTxn.setErrReason("客户-账户不匹配");
				rCcsOutsideDdTxn.save(ccsOutsideDdTxn);
				return null;
			}
			// 账户-贷款不匹配
			if (!loan.getContrNbr().equals(acct.getContrNbr())) {
				logger.debug("客户-贷款不匹配!");
				ccsOutsideDdTxn.setErrReason("客户-贷款不匹配");
				rCcsOutsideDdTxn.save(ccsOutsideDdTxn);
				return null;
			}
			if (item.paybackAmount.compareTo(BigDecimal.ZERO) <= 0) {
				logger.debug("扣款金额不大于0");
				ccsOutsideDdTxn.setErrReason("扣款金额不大于0");
				rCcsOutsideDdTxn.save(ccsOutsideDdTxn);
				return null;
			}
			//获取账单日
//			Calendar calendar = GregorianCalendar.getInstance();
//			calendar.setTime(batchStatusFacility.getSystemStatus().getBusinessDate());
//			int cycleDate = Integer.parseInt(acct.getCycleDay());
			//对于随借随还，应取到期还款日
			//对于等额本息，应取账单日。目前等额本息的到期还款日即账单日
			if (frontBatchUtil.getWOrderCount(loan.getAcctNbr(), loan.getAcctType()) > 0
					//存在在途订单且非账单日扣款
					&&batchStatusFacility.getSystemStatus().getBusinessDate().compareTo(acct.getPmtDueDate())!=0) {
				logger.debug("存在处理中扣款");
				ccsOutsideDdTxn.setErrReason("存在在途扣款");
				rCcsOutsideDdTxn.save(ccsOutsideDdTxn);
				return null;
			}
			ccsOutsideDdTxn.setErrInd(Indicator.N);
			ccsOutsideDdTxn.setAcctNbr(acct.getAcctNbr());
			ccsOutsideDdTxn.setAcctType(acct.getAcctType());
//			ccsOutsideDdTxn.setDueBillNo(loan.getDueBillNo());
			ccsOutsideDdTxn.setLoanId(loan.getLoanId());

			rCcsOutsideDdTxn.save(ccsOutsideDdTxn);
			frontBatchUtil.initOrder(info.getAcct(), info.getCust(), info.getLoan(), LoanUsage.P,item.paybackAmount, null);
			return info;
		} catch (Exception e) {
			logger.debug("PTP扣款异常");
			throw e;
		}
		
	}
}
