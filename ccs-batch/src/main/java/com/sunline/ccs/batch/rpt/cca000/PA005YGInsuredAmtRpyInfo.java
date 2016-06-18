package com.sunline.ccs.batch.rpt.cca000;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.sunline.ccs.batch.rpt.cca000.items.YGInsuredAmtRpyInfoItem;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;

/**
 * 保费还款信息
 * @author wanghl
 *
 */
public class PA005YGInsuredAmtRpyInfo implements ItemProcessor<SA005YGInsuredAmtRpy, YGInsuredAmtRpyInfoItem>{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Override
	public YGInsuredAmtRpyInfoItem process(SA005YGInsuredAmtRpy info)
			throws Exception {
		CcsPlan plan = info.getPlan();
		logger.debug("账户[{}][{}]借据号[{}]信用计划ID[{}]交易参考号[{}]",
				plan.getAcctNbr(),plan.getAcctType(),info.getDueBillNo(),plan.getPlanId(),plan.getRefNbr());
		
		YGInsuredAmtRpyInfoItem item = new YGInsuredAmtRpyInfoItem(); 
		BigDecimal actualPayCorp = new BigDecimal(0);
		
		for(CcsRepayHst repayHst : info.getRepays()){
			logger.debug("还款分配planid[{}]paymentId[{}]",
					repayHst.getPlanId(),repayHst.getPaymentId());
			
			actualPayCorp = actualPayCorp.add(repayHst.getRepayAmt());
		}
		if(info.getOrderIds().size() > 0){
			item.seqNo = info.getOrderIds().get(0);
		}
		item.putOutNo = info.getDueBillNo();
		item.loanTerm = info.getCurrTerm();
		item.payDate = info.getTermPmtDueDate();
		item.actualPayDate = info.getBatchDate();
		item.actualPayCorp = actualPayCorp.setScale(2, RoundingMode.HALF_UP);
		item.payCorp = plan.getCtdInsurance().add(plan.getPastInsurance()).setScale(2, RoundingMode.HALF_UP);
		item.inputDate = info.getBatchDate();
		
		if(info.getIsLoanTerminated()){
			if(info.getTerminalReason().equals(LoanTerminateReason.P)){
				//提前结清
				item.payMentKind = "1";
				item.loanTerm = 0;
			}
		}else if(info.getTermPmtDueDate() != null){
			int compareDate = info.getBatchDate().compareTo(info.getTermPmtDueDate());
			if(compareDate > 0){//逾期还款
				item.payMentKind = "2";
			}else if (compareDate == 0){
				//提前结清
				if(info.getIsLoanTerminated() && info.getTerminalReason().equals(LoanTerminateReason.P)){
					item.payMentKind = "1";
					item.loanTerm = 0;
				}else{//正常还款
					item.payMentKind = "0";
				}
			}else{//提前还款
				item.payMentKind = "1";
				item.loanTerm = 0;
			}
		}
		
		return item;
	}

}
