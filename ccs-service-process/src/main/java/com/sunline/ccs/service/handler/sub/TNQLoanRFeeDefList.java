package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13000LoanFeeDef;
import com.sunline.ccs.service.protocol.S13000Req;
import com.sunline.ccs.service.protocol.S13000Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQLoanRFeeDefList
 * @see 描述：消费分期参数信息查询
 *
 * @see 创建日期： 2015年6月24日 下午5:47:09
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQLoanRFeeDefList {
	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;

	@Transactional
	public S13000Resp handler(S13000Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13000", "消费分期参数信息查询", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		S13000Resp resp = new S13000Resp();

		CcsCard card = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(card, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		LoanPlan loanPlan = unifiedParameterFacilityProvide.loanPlan(card.getProductCd(), LoanType.R);
		CheckUtil.rejectNull(loanPlan, Constants.ERRL046_CODE, Constants.ERRL046_MES);

		resp.setCard_no(req.getCard_no());
		resp.setLoan_type(LoanType.R);
		resp.setDescription(loanPlan.description);
		resp.setPrepayment_ind(loanPlan.prepaymentInd);
		resp.setPrepayment_fee_ind(loanPlan.prepaymentFeeInd);

		ArrayList<S13000LoanFeeDef> loanFeeDefs = new ArrayList<S13000LoanFeeDef>();
		Map<Integer, LoanFeeDef> loanFeedefMap = loanPlan.loanFeeDefMap;
		// 返回所有期数的分期计价方式
		for (Integer key : loanFeedefMap.keySet()) {
			S13000LoanFeeDef loanFeeDef = new S13000LoanFeeDef();
			LoanFeeDef lfd = loanPlan.loanFeeDefMap.get(key);
			loanFeeDef.setLoan_init_term(key);
			loanFeeDef.setMin_amount(lfd.minAmount);
			loanFeeDef.setMax_amount(lfd.maxAmount);
			loanFeeDef.setLoan_fee_method(lfd.loanFeeMethod);
			loanFeeDef.setLoan_fee_calc_method(lfd.loanFeeCalcMethod);
			loanFeeDef.setFee_amount(lfd.feeAmount == null ? BigDecimal.ZERO : lfd.feeAmount);
			loanFeeDef.setFee_rate(lfd.feeRate == null ? BigDecimal.ZERO : lfd.feeRate);
			loanFeeDef.setReschedule_ind(lfd.rescheduleInd ? Indicator.Y : Indicator.N);
			loanFeeDef.setReschedule_fee_method(lfd.loanFeeMethod);
			loanFeeDef.setReschedule_calc_method(lfd.loanFeeCalcMethod);
			loanFeeDef.setReschedule_fee_amount(lfd.rescheduleFeeAmount == null ? BigDecimal.ZERO : lfd.rescheduleFeeAmount);
			loanFeeDef.setReschedule_fee_rate(lfd.rescheduleFeeRate == null ? BigDecimal.ZERO : lfd.rescheduleFeeRate);
			loanFeeDef.setPrepayment_fee_method(lfd.prepaymentFeeMethod);
//			loanFeeDef.setPrepayment_fee_amount(lfd.prepaymentFeeAmount == null ? BigDecimal.ZERO : lfd.prepaymentFeeAmount);
//			loanFeeDef.setPrepayment_fee_amount_rate(lfd.prepaymentFeeAmountRate == null ? BigDecimal.ZERO : lfd.prepaymentFeeAmountRate);
			loanFeeDefs.add(loanFeeDef);
		}

		// 分期列表中按照分期期数排序
		Comparator<S13000LoanFeeDef> comparator = new Comparator<S13000LoanFeeDef>() {
			@Override
			public int compare(S13000LoanFeeDef o1, S13000LoanFeeDef o2) {
				return o1.getLoan_init_term() - o2.getLoan_init_term();
			}
		};
		Collections.sort(loanFeeDefs, comparator);

		resp.setLoanfeedefs(loanFeeDefs);

		LogTools.printLogger(logger, "S13000", "消费分期信息查询", resp, false);
		return resp;
	}

}