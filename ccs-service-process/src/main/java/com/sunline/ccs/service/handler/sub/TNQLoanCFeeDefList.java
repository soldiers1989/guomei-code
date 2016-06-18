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
import com.sunline.ccs.service.protocol.S13002LoanFeeDef;
import com.sunline.ccs.service.protocol.S13002Req;
import com.sunline.ccs.service.protocol.S13002Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQLoanCFeeDefList
 * @see 描述：现金分期参数查询
 *
 * @see 创建日期： 2015年6月24日 下午3:07:31
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQLoanCFeeDefList {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;

	@Transactional
	public S13002Resp handler(S13002Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13002", "现金分期参数查询", req, true);

		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		// 通过卡号查询卡表
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// 根据卡产品和分期类型查询分期产品参数
		LoanPlan loanPlan = unifiedParameterFacilityProvide.loanPlan(CcsCard.getProductCd(), LoanType.C);
		CheckUtil.rejectNull(loanPlan, Constants.ERRL046_CODE, Constants.ERRL046_MES);

		// 得到分期计价参数,此处不判空
		Map<Integer, LoanFeeDef> loanFeeMap = loanPlan.loanFeeDefMap;

		S13002Resp resp = new S13002Resp();
		ArrayList<S13002LoanFeeDef> lfdList = new ArrayList<S13002LoanFeeDef>();
		for (Integer key : loanFeeMap.keySet()) {
			S13002LoanFeeDef loanFeeDef = new S13002LoanFeeDef();
			LoanFeeDef lfd = loanFeeMap.get(key);

			// TODO 对于参数中没有配置的项是返空还是返回0，再商议，此处与账分期和消费分期参数查询保持一致
			loanFeeDef.setLoan_init_term(key);
			loanFeeDef.setMin_amount(lfd.minAmount);
			loanFeeDef.setMax_amount(lfd.maxAmount);
			loanFeeDef.setMax_amount_rate(lfd.maxAmountRate);
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

			lfdList.add(loanFeeDef);
		}
		// 对list根据期数从小到大排序
		Collections.sort(lfdList, new Comparator<S13002LoanFeeDef>() {

			@Override
			public int compare(S13002LoanFeeDef o1, S13002LoanFeeDef o2) {
				if (o1.getLoan_init_term().compareTo(o2.getLoan_init_term()) > 0) {
					return 1;
				} else {
					return -1;
				}
			}

		});
		resp.setCard_no(req.getCard_no());
		resp.setLoan_type(LoanType.C);
		resp.setDescription(loanPlan.description);
		resp.setPrepayment_ind(loanPlan.prepaymentInd);
		resp.setPrepayment_fee_ind(loanPlan.prepaymentFeeInd);
		resp.setLoanfeedefs(lfdList);

		LogTools.printLogger(logger, "S13002", "现金分期参数查询", resp, false);
		return resp;
	}

}
