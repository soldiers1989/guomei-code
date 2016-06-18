package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20001Loan;
import com.sunline.ccs.service.protocol.S20001Req;
import com.sunline.ccs.service.protocol.S20001Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcLoanProduct
 * @see 描述： 贷款产品信息查询
 *
 * @see 创建日期： 2015年06月26日上午 09:50:50
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcLoanProduct {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	/**
	 * @see 方法名：handler
	 * @see 描述：贷款产品信息查询handler
	 * @see 创建日期：2015年6月26日上午10:10:07
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
	public S20001Resp handler(S20001Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20001", "贷款产品信息查询", req, true);
		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		// 校验卡号是否贷款卡
		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbr(req.getCard_no(), AccountType.E);
		CheckUtil.rejectNull(CcsAcct, Constants.ERRC012_CODE, Constants.ERRC012_MES);

		S20001Resp resp = new S20001Resp();
		// 查询到所有的loanplan
		Map<String, LoanPlan> loanplanMap = unifiedParameterService.retrieveParameterObject(LoanPlan.class);

		ArrayList<S20001Loan> loanlist = new ArrayList<S20001Loan>();

		// 当请求条件为null时
		if (req.getLoan_status() == null && req.getLoan_type() == null) {
			for (String loancode : loanplanMap.keySet()) {
				S20001Loan loan = new S20001Loan();
				LoanPlan loanPlan = loanplanMap.get(loancode);
				// 分期类型不是小额贷的去掉
				if (loanPlan.loanType.equals(LoanType.B) || loanPlan.loanType.equals(LoanType.C) || loanPlan.loanType.equals(LoanType.M)
						|| loanPlan.loanType.equals(LoanType.P) || loanPlan.loanType.equals(LoanType.R)) {
					continue;
				}
				loan.setDescription(loanPlan.description);
				loan.setLoan_code(loanPlan.loanCode);
				loan.setLoan_status(loanPlan.loanStaus);
				loan.setLoan_type(loanPlan.loanType);
				loan.setLoan_validity(loanPlan.loanValidity);
				loan.setPrepaymentind(loanPlan.prepaymentInd);
				loan.setMin_cyle(loanPlan.minCycle);
				loan.setMax_cyle(loanPlan.maxCycle);

				loanlist.add(loan);
			}
		}
		// 当产品状态不为null,贷款产品类型为null时，
		if (req.getLoan_status() != null && req.getLoan_type() == null) {
			for (String loancode : loanplanMap.keySet()) {
				S20001Loan loan = new S20001Loan();
				LoanPlan loanPlan = loanplanMap.get(loancode);
				// 分期类型不是小额贷的去掉
				if (loanPlan.loanType.equals(LoanType.B) || loanPlan.loanType.equals(LoanType.C) || loanPlan.loanType.equals(LoanType.M)
						|| loanPlan.loanType.equals(LoanType.P) || loanPlan.loanType.equals(LoanType.R)) {
					continue;
				}
				if (loanPlan.loanStaus.equals(req.getLoan_status())) {
					loan.setDescription(loanPlan.description);
					loan.setLoan_code(loanPlan.loanCode);
					loan.setLoan_status(loanPlan.loanStaus);
					loan.setLoan_type(loanPlan.loanType);
					loan.setLoan_validity(loanPlan.loanValidity);
					loan.setPrepaymentind(loanPlan.prepaymentInd);
					loan.setMin_cyle(loanPlan.minCycle);
					loan.setMax_cyle(loanPlan.maxCycle);

					loanlist.add(loan);
				}
			}
		}
		// 当产品状态为null,贷款产品类型不为null时，
		if (req.getLoan_status() == null && req.getLoan_type() != null) {
			for (String loancode : loanplanMap.keySet()) {
				S20001Loan loan = new S20001Loan();
				LoanPlan loanPlan = loanplanMap.get(loancode);
				// 分期类型不是小额贷的去掉
				if (loanPlan.loanType.equals(LoanType.B) || loanPlan.loanType.equals(LoanType.C) || loanPlan.loanType.equals(LoanType.M)
						|| loanPlan.loanType.equals(LoanType.P) || loanPlan.loanType.equals(LoanType.R)) {
					continue;
				}
				if (loanPlan.loanType.equals(req.getLoan_type())) {
					loan.setDescription(loanPlan.description);
					loan.setLoan_code(loanPlan.loanCode);
					loan.setLoan_status(loanPlan.loanStaus);
					loan.setLoan_type(loanPlan.loanType);
					loan.setLoan_validity(loanPlan.loanValidity);
					loan.setPrepaymentind(loanPlan.prepaymentInd);
					loan.setMin_cyle(loanPlan.minCycle);
					loan.setMax_cyle(loanPlan.maxCycle);

					loanlist.add(loan);
				}
			}
		}
		// 当产品状态不为null,贷款产品类型不为null时，
		if (req.getLoan_status() != null && req.getLoan_type() != null) {
			for (String loancode : loanplanMap.keySet()) {
				S20001Loan loan = new S20001Loan();
				LoanPlan loanPlan = loanplanMap.get(loancode);
				// 分期类型不是小额贷的去掉
				if (loanPlan.loanType.equals(LoanType.B) || loanPlan.loanType.equals(LoanType.C) || loanPlan.loanType.equals(LoanType.M)
						|| loanPlan.loanType.equals(LoanType.P) || loanPlan.loanType.equals(LoanType.R)) {
					continue;
				}
				if (loanPlan.loanStaus.equals(req.getLoan_status()) && loanPlan.loanType.equals(req.getLoan_type())) {
					loan.setDescription(loanPlan.description);
					loan.setLoan_code(loanPlan.loanCode);
					loan.setLoan_status(loanPlan.loanStaus);
					loan.setLoan_type(loanPlan.loanType);
					loan.setLoan_validity(loanPlan.loanValidity);
					loan.setPrepaymentind(loanPlan.prepaymentInd);
					loan.setMin_cyle(loanPlan.minCycle);
					loan.setMax_cyle(loanPlan.maxCycle);

					loanlist.add(loan);
				}
			}
		}
		resp.setCard_no(req.getCard_no());
		resp.setLoans(loanlist);
		LogTools.printLogger(logger, "S20001", "贷款产品信息查询", resp, false);
		return resp;
	}
}
