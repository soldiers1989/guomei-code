package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20010LoanFee;
import com.sunline.ccs.service.protocol.S20010Req;
import com.sunline.ccs.service.protocol.S20010Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcLoanFeeDefList
 * @see 描述： 贷款产品定价信息查询
 *
 * @see 创建日期： 2015年06月26日上午 09:50:50
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcLoanFeeDefList {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;

	/**
	 * @see 方法名：handler
	 * @see 描述：贷款产品定价信息查询handler
	 * @see 创建日期：2015年6月26日上午10:16:13
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public S20010Resp handler(S20010Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20010", "贷款产品定价信息查询", req, true);
		// 检查上送报文字段
		CheckUtil.checkLoanCode(req.getLoan_code());
		S20010Resp resp = new S20010Resp();
		// 根据产品编号查询贷款产品
		LoanPlan loanPlan = unifiedParameterFacilityProvide.loanPlan(req.getLoan_code());
		CheckUtil.rejectNull(loanPlan, Constants.ERRC011_CODE, Constants.ERRC011_MES);

		// 得到贷款产品定价信息
		Map<Integer, LoanFeeDef> loanfeedefmMap = loanPlan.loanFeeDefMap;

		ArrayList<S20010LoanFee> loanfeelist = new ArrayList<S20010LoanFee>();
		for (Integer key : loanfeedefmMap.keySet()) {
			S20010LoanFee loanFee = new S20010LoanFee();
			LoanFeeDef loanFeeDef = loanfeedefmMap.get(key);

			loanFee.setLoan_term(key);
			loanFee.setLoan_fee_method(loanFeeDef.loanFeeMethod);
			loanFee.setLoan_fee_calc_method(loanFeeDef.loanFeeCalcMethod);
			loanFee.setFee_amount(loanFeeDef.feeAmount);
			loanFee.setFee_rate(loanFeeDef.feeRate);
			loanFee.setReschedule_ind(loanFeeDef.rescheduleInd);
			loanFee.setReschedule_fee_method(loanFeeDef.rescheduleFeeMethod);
			loanFee.setReschedule_calc_method(loanFeeDef.rescheduleCalcMethod);
			loanFee.setReschedule_fee_amount(loanFeeDef.rescheduleFeeAmount);
			loanFee.setReschedule_fee_rate(loanFeeDef.rescheduleFeeRate);
			loanFee.setPrepayment_fee_method(loanFeeDef.prepaymentFeeMethod);
//			loanFee.setPrepayment_fee_amount(loanFeeDef.prepaymentFeeAmount);
//			loanFee.setPrepayment_fee_amount_rate(loanFeeDef.prepaymentFeeAmountRate);
			loanFee.setSystolicphase_ind(loanFeeDef.shortedRescInd);
			loanFee.setSystolicphase_fee_method(loanFeeDef.shortedRescCalcMethod);
			loanFee.setSystolicphase_fee_amount(loanFeeDef.shortedRescFeeAmount);
			loanFee.setSystolicphase_fee_amount_rate(loanFeeDef.shortedRescFeeAmountRate);
			loanFee.setInterest_rate(loanFeeDef.interestRate);
			loanFee.setCompoundinttableid(loanFeeDef.compoundIntTableId);
			loanFee.setPenaltyinttableid(loanFeeDef.penaltyIntTableId);
			loanFee.setInterestacrumethod(loanFeeDef.interestAcruMethod);
			loanFee.setInterestadjmethod(loanFeeDef.interestAdjMethod);
			loanFee.setLatefeecharge(loanFeeDef.lateFeeCharge);
			loanFee.setMinagecd(loanFeeDef.minAgeCd);
			loanFee.setMincharge(loanFeeDef.minCharge);
			loanFee.setMaxcharge(loanFeeDef.maxCharge);
			loanFee.setYearmaxcharge(loanFeeDef.yearMaxCharge);
			loanFee.setYearmaxcnt(loanFeeDef.yearMaxCnt);
			loanFee.setCalcbaseind(loanFeeDef.calcBaseInd);
			loanFee.setTierind(loanFeeDef.tierInd);

			loanfeelist.add(loanFee);
		}
		// 对loanfeelist按照期数从小到大排序
		Collections.sort(loanfeelist, new Comparator<S20010LoanFee>() {

			@Override
			public int compare(S20010LoanFee arg0, S20010LoanFee arg1) {
				if (arg0.getLoan_term().compareTo(arg1.getLoan_term()) > 0) {
					return 1;
				} else {
					return -1;
				}
			}

		});

		resp.setLoan_code(req.getLoan_code());
		resp.setLoanFees(loanfeelist);
		LogTools.printLogger(logger, "S20010", "贷款产品定价信息查询", resp, false);
		return resp;
	}
}
