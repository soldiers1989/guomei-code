package com.sunline.ccs.batch.rpt.cca000;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.sunline.ccs.batch.rpt.cca000.items.YGRpyDetailItem;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;


/**
 * 贷款还款明细
 * @author wanghl
 *
 */
public class PA003YGRpyDetail implements ItemProcessor<SA003YGRpyDetailInfo, YGRpyDetailItem>{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Override
	public YGRpyDetailItem process(SA003YGRpyDetailInfo info) throws Exception {
		YGRpyDetailItem item;
		if(info.getIsRecovery()){
			item = getRecoveryItem(info);
		}else{
			item = getAcctRepayItem(info);
		}
		
		return item;
	}

	private YGRpyDetailItem getRecoveryItem(SA003YGRpyDetailInfo info) {
		
		
		YGRpyDetailItem item = null;
		
		if(info.getOrderHst() != null){
			CcsOrderHst orderHst = info.getOrderHst();
			item = new YGRpyDetailItem();
			logger.info("=代扣还款,订单历史Id[{}]=借据号[{}]", orderHst.getOrderId(),orderHst.getDueBillNo());
			item.seqNo = orderHst.getOrderId();
			item.repayDate = orderHst.getOptDatetime();
			item.putOutNo = orderHst.getDueBillNo();
			item.loanTerm = -1;
			item.paymentKind = "3";
			item.paymentAcctNo = orderHst.getCardNo();
			item.balance = orderHst.getSuccessAmt().setScale(2, RoundingMode.HALF_UP);
			item.inte = null;
			item.infine = null;
		}
		if(info.getOrder() != null){
			CcsOrder order = info.getOrder();
			item = new YGRpyDetailItem();
			logger.info("=代扣还款,订单Id[{}]=借据号[{}]", order.getOrderId(),order.getDueBillNo());
			item.seqNo = order.getOrderId();
			item.repayDate = order.getOptDatetime();
			item.putOutNo = order.getDueBillNo();
			item.loanTerm = -1;
			item.paymentKind = "3";
			item.paymentAcctNo = order.getCardNo();
			item.balance = order.getSuccessAmt().setScale(2, RoundingMode.HALF_UP);
			item.inte = null;
			item.infine = null;
		}
		return item;
	}

	/**
	 * @param info
	 * @return
	 */
	private YGRpyDetailItem getAcctRepayItem(SA003YGRpyDetailInfo info) {
		logger.info("=账户还款=借据号["+info.getDueBillNo()+"]");
		YGRpyDetailItem item = new YGRpyDetailItem();
		List<CcsRepayHst> repays = info.getRepays();
		BigDecimal balance = new BigDecimal(0);
		BigDecimal inte = new BigDecimal(0);
		BigDecimal infine = new BigDecimal(0);
		for(CcsRepayHst repayHst : repays){
			logger.info("repayID[" + repayHst.getPaymentId() + "]");
			switch (repayHst.getBnpType()) {
			case ctdPrincipal:
			case pastPrincipal:
				balance = balance.add(repayHst.getRepayAmt());
				break;
			case ctdInterest:
			case pastInterest:
				inte = inte.add(repayHst.getRepayAmt());
				break;
			case ctdMulct:
			case pastMulct:
				infine = infine.add(repayHst.getRepayAmt());
				break;
			default:
				break;
			}
		}
		item.balance = balance.setScale(2, RoundingMode.HALF_UP);
		item.inte = inte.setScale(2, RoundingMode.HALF_UP);
		item.infine = infine.setScale(2, RoundingMode.HALF_UP);
		item.putOutNo = info.getDueBillNo();
		item.repayDate = info.getBatchDate();
		item.loanTerm = info.getCurrTerm();
		item.paymentAcctNo = info.getDdBankAcctNo();
		
		if(info.getIsLoanTerminated()){
			if(info.getTerminalReason().equals(LoanTerminateReason.P)){
				//提前结清
				item.paymentKind = "1";
				item.loanTerm = 0;
			}
		}else{
			Integer compareDate = null;
			if(info.getTermPmtDueDate() == null){
				compareDate = 2;
			}else
				compareDate = info.getBatchDate().compareTo(info.getTermPmtDueDate());
			if(compareDate > 0){//逾期还款
				item.paymentKind = "2";
			}else if (compareDate == 0){
				//正常还款
				item.paymentKind = "0";
					
			}else{//提前还款
				item.paymentKind = "1";
				item.loanTerm = 0;
			}
		}
		
		if(info.getOrderIds().size()>0){
			item.seqNo = info.getOrderIds().get(0);
		}//TODO else 订单找不到...
		return item;
	}
	
}
