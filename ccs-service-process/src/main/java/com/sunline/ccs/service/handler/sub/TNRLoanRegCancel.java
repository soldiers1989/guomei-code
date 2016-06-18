package com.sunline.ccs.service.handler.sub;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.LoanTerminateReason;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.handler.CashLoanService;
import com.sunline.ccs.service.protocol.S13090Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNRLoanRegCancel
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期： 2015年6月24日 下午11:45:21
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRLoanRegCancel {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private CashLoanService cashLoanService;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;

	@Autowired
	private RCcsCustomer rCcsCustomer;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;

	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;

	@Transactional
	public void handler(S13090Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13090", "分期撤销", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		CcsCard card = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(card, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CheckUtil.rejectNull(req.getRegister_id(), Constants.ERRL020_CODE, Constants.ERRL020_MES);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)
				&& !StringUtils.equals(req.getOpt(), Constants.OPT_TWO)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 当日分期分期撤销
		if (StringUtils.equals(req.getOpt(), Constants.OPT_ZERO)) {
			CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.registerId.eq(req.getRegister_id()).and(qCcsLoanReg.logicCardNbr.eq(card.getLogicCardNbr())));
			CheckUtil.rejectNull(loanReg, Constants.ERRL021_CODE, Constants.ERRL021_MES);
			CcsAcctKey accountKey = new CcsAcctKey();
			accountKey.setAcctNbr(loanReg.getAcctNbr());
			accountKey.setAcctType(loanReg.getAcctType());
			CcsAcct acct = rCcsAcct.findOne(accountKey);
			CcsCustomer customer = rCcsCustomer.findOne(acct.getCustId());
			ProductCredit productCredit = unifiedParamFacilityProvide.productCredit(acct.getProductCd());

			switch (loanReg.getLoanType()) {
			case C:
				cashLoanService.reverse(loanReg, productCredit);
				rCcsLoanReg.delete(loanReg);
				break;
			case R:
			case B:
				rCcsLoanReg.delete(loanReg);
				break;
			default:
				throw new ProcessException(Constants.ERRL051_CODE, Constants.ERRL051_MES);
			}

			// messageService.sendMessage(MessageCategory.P03,
			// CcsAcct.getProductCd(), loanReg.getCardNbr(),
			// ccsCustomer.getName(), ccsCustomer.getGender(),
			// ccsCustomer.getMobileNo(),
			// ccsCustomer.getEmail(), new Date(),
			// new MapBuilder<String, Object>().add("loanType",
			// loanReg.getLoanType()).add("amt",
			// loanReg.getLoanInitPrin()).add("term",
			// loanReg.getLoanInitTerm()).build());
			String msgCd = fetchMsgCdService.fetchMsgCd(acct.getProductCd(), CPSMessageCategory.CPS034);
/*			downMsgFacility.sendMessage(
					msgCd,
					loanReg.getCardNbr(),
					customer.getName(),
					customer.getGender(),
					customer.getMobileNo(),
					new Date(),
					new MapBuilder<String, Object>().add("loanType", loanReg.getLoanType()).add("amt", loanReg.getLoanInitPrin())
							.add("term", loanReg.getLoanInitTerm()).build());
*/
		}

		// 分期提前还款撤销
		if (StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {

			// 根据registerId查询原分期对应申请成功的提前还款,修改该提前还款LoanRegStatus为撤销
			CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.registerId.eq(req.getRegister_id()).and(qCcsLoanReg.loanAction.eq(LoanAction.P))
					.and(qCcsLoanReg.loanRegStatus.eq(LoanRegStatus.A)));
			CheckUtil.rejectNull(loanReg, Constants.ERRL025_CODE, Constants.ERRL025_MES);

			// 根据卡号和交易参考号查询分期主表信息
			CcsLoan loan = rCcsLoan.findOne(qCcsLoan.cardNbr.eq(req.getCard_no()).and(qCcsLoan.refNbr.eq(loanReg.getRefNbr())));
			CheckUtil.rejectNull(loan, Constants.ERRL021_CODE, Constants.ERRL021_MES);
			// 验证该分期状态是否为终止状态
			if (!loan.getLoanStatus().equals(LoanStatus.T) && loan.getTerminalDate() == null) {
				throw new ProcessException(Constants.ERRL023_CODE, Constants.ERRL023_MES);
			}
			// 验证终止原因是否为提前还款
			if (!loan.getTerminalReasonCd().equals(LoanTerminateReason.P)) {
				throw new ProcessException(Constants.ERRL024_CODE, Constants.ERRL024_MES);
			}
			// 验证是否为当日提前还款终止
			if (DateUtils.truncatedCompareTo(loan.getTerminalDate(), unifiedParamFacilityProvide.BusinessDate(), Calendar.DATE) != 0) {
				throw new ProcessException(Constants.ERRL025_CODE, Constants.ERRL025_MES);
			}
			// 回滚分期状态
			loan.setLoanStatus(loan.getLastLoanStatus());
			loan.setLastLoanStatus(LoanStatus.T);
			loan.setTerminalDate(null);
			loan.setTerminalReasonCd(null);

			loanReg.setLoanRegStatus(LoanRegStatus.V);

		}

		// 分期展期撤销
		if (StringUtils.equals(req.getOpt(), Constants.OPT_TWO)) {

			// 根据registerId查询原分期对应申请成功的展期,修改该展期LoanRegStatus为撤销
			CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.registerId.eq(req.getRegister_id()).and(qCcsLoanReg.loanAction.eq(LoanAction.R))
					.and(qCcsLoanReg.loanRegStatus.eq(LoanRegStatus.A)));
			CheckUtil.rejectNull(loanReg, Constants.ERRL029_CODE, Constants.ERRL029_MES);
			loanReg.setLoanRegStatus(LoanRegStatus.V);

			CcsAcctKey accountKey = new CcsAcctKey();
			accountKey.setAcctNbr(loanReg.getAcctNbr());
			accountKey.setAcctType(loanReg.getAcctType());
			CcsAcct acct = rCcsAcct.findOne(accountKey);
			CcsCustomer customer = rCcsCustomer.findOne(acct.getCustId());

			String msgCd = fetchMsgCdService.fetchMsgCd(acct.getProductCd(), CPSMessageCategory.CPS036);
/*			downMsgFacility.sendMessage(
					msgCd,
					loanReg.getCardNbr(),
					customer.getName(),
					customer.getGender(),
					customer.getMobileNo(),
					new Date(),
					new MapBuilder<String, Object>().add("loanType", loanReg.getLoanType()).add("amt", loanReg.getLoanInitPrin())
							.add("term", loanReg.getLoanInitTerm()).add("term", loanReg.getLoanInitTerm()).add("loanFee", loanReg.getLoanInitFee())
							.add("nextPayment", loanReg.getLoanFixedPmtPrin()).add("loanFixedFee", loanReg.getLoanFixedFee()).build());
*/
		}

		LogTools.printLogger(logger, "S13090", "分期撤销", null, false);
	}

}
