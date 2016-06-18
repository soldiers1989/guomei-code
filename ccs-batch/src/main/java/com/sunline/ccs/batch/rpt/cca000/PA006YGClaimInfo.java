package com.sunline.ccs.batch.rpt.cca000;

import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.rpt.cca000.items.YGClaimInfoItem;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.loan.TrialResp;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanUsage;

/**
 * 预理赔文件
 * @author wanghl
 *
 */
public class PA006YGClaimInfo implements ItemProcessor<CcsLoan, YGClaimInfoItem> {
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@PersistenceContext
    private EntityManager em;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
	private QCcsPlan qPlan = QCcsPlan.ccsPlan;
	@Autowired
	private McLoanProvideImpl loanProvide;

	@Override
	public YGClaimInfoItem process(CcsLoan info) throws Exception {
		OrganizationContextHolder.setCurrentOrg(info.getOrg());
		
		TrialResp trialResp = new TrialResp();
		Date batchDate = batchStatusFacility.getBatchDate();
		
		Calendar overDueDateCld = Calendar.getInstance();
		overDueDateCld.setTime(info.getOverdueDate());
		
		Integer overDueDays = DateUtils.getIntervalDays(info.getOverdueDate(), batchDate);
		// 获取卡产品信息
		CcsCard card = queryFacility.getCardByCardNbr(info.getCardNbr());
		ProductCredit productCredit = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
		
		if(!(productCredit.preClaimStartDays <= overDueDays && overDueDays <= productCredit.preClaimEndDays)){
			return null;
		}
//		if(!(70 <= overDueDays && overDueDays <= 79)){
//			return null;
//		}
		List<CcsPlan> plans = new JPAQuery(em).from(qPlan).where(qPlan.acctNbr.eq(info.getAcctNbr())).list(qPlan);
		loanProvide.mCLoanTodaySettlement(info,null, batchDate , LoanUsage.C, trialResp, plans,null);
		
		YGClaimInfoItem item = new YGClaimInfoItem();
		item.putOutNo = info.getDueBillNo();
		item.overDueDays = overDueDays;
		item.overDueBalance = trialResp.getPastPricinpalAMT().setScale(2, RoundingMode.HALF_UP);
		item.overDueInte = trialResp.getPastInterestAMT().setScale(2, RoundingMode.HALF_UP);
		item.overDueInfine = trialResp.getPastMulctAMT().add(trialResp.getCtdMulctAMT()).setScale(2, RoundingMode.HALF_UP);
		item.nint = trialResp.getCtdInterestAMT();
		item.balance = info.getUnstmtPrin().add(trialResp.getPastPricinpalAMT()).setScale(2, RoundingMode.HALF_UP);
		item.lpamt = trialResp.getTotalAMT().setScale(2, RoundingMode.HALF_UP);
		item.lptxt = ""; //
		
		overDueDateCld.add(Calendar.DAY_OF_MONTH, productCredit.claimsDays);// 
		item.yjlprq = overDueDateCld.getTime();
		
		return item;
	}

}
