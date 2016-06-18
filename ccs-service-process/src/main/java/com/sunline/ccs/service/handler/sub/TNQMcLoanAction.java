package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanRegHst;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20024LoanRegHst;
import com.sunline.ccs.service.protocol.S20024Req;
import com.sunline.ccs.service.protocol.S20024Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQMcLoanAction
 * @see 描述： 根据借据号查询贷款变更历史
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQMcLoanAction {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustAcctCardFacility custAcctCardQueryFacility;

    @PersistenceContext
    private EntityManager em;
    private QCcsLoanRegHst qCcsLoanRegHst = QCcsLoanRegHst.ccsLoanRegHst;

    /**
     * @see 方法名：handler
     * @see 描述：根据借据号查询贷款变更历史handler
     * @see 创建日期：2015年6月26日上午10:43:47
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
    public S20024Resp handler(S20024Req req) throws ProcessException {
	LogTools.printLogger(logger, "S20024", "根据借据号查询贷款变更历史", req, true);
	S20024Resp resp = new S20024Resp();

	// 检查上送请求报文字段
	CheckUtil.checkLoanReceiptNbr(req.getLoan_receipt_nbr());
	CheckUtil.checkCardNo(req.getCard_no());
	CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
	CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
	BooleanExpression booleanExpression =
		qCcsLoanRegHst.org.eq(OrganizationContextHolder.getCurrentOrg())
			.and(qCcsLoanRegHst.dueBillNo.eq(req.getLoan_receipt_nbr()))
			.and(qCcsLoanRegHst.logicCardNbr.eq(CcsCard.getLogicCardNbr()));

	JPAQuery queryList = new JPAQuery(em);

	List<CcsLoanRegHst> loanRegHstList =
		queryList.from(qCcsLoanRegHst).where(booleanExpression).list(qCcsLoanRegHst);
	if (loanRegHstList.isEmpty()) {
	    throw new ProcessException(Constants.ERRC017_CODE, Constants.ERRC017_MES);
	}

	ArrayList<S20024LoanRegHst> loanRegHsts = new ArrayList<S20024LoanRegHst>();

	for (CcsLoanRegHst loanRegHst : loanRegHstList) {
	    S20024LoanRegHst s20024LoanRegHst = new S20024LoanRegHst();
	    s20024LoanRegHst.setRegister_hst_id(loanRegHst.getRegisterId());
	    s20024LoanRegHst.setRegister_date(loanRegHst.getRegisterDate());
	    s20024LoanRegHst.setRequest_time(loanRegHst.getRequestTime());
	    s20024LoanRegHst.setLoan_type(loanRegHst.getLoanType());
	    s20024LoanRegHst.setLoan_reg_status(loanRegHst.getLoanRegStatus());
	    s20024LoanRegHst.setLoan_init_term(loanRegHst.getLoanInitTerm());
	    s20024LoanRegHst.setLoan_init_prin(loanRegHst.getLoanInitPrin());
	    s20024LoanRegHst.setLoan_init_fee1(loanRegHst.getLoanInitFee());
	    s20024LoanRegHst.setLoan_fee_method(loanRegHst.getLoanFeeMethod());
	    s20024LoanRegHst.setLoan_code(loanRegHst.getLoanCode());
	    s20024LoanRegHst.setLoan_action(loanRegHst.getLoanAction());
	    s20024LoanRegHst.setInterest_rate(loanRegHst.getInterestRate());
	    s20024LoanRegHst.setPenalty_rate(loanRegHst.getPenaltyRate());
	    s20024LoanRegHst.setCompound_rate(loanRegHst.getCompoundRate());
	    s20024LoanRegHst.setFloat_rate(loanRegHst.getFloatRate());
	    s20024LoanRegHst.setAdv_pmt_amt(loanRegHst.getAdvPmtAmt());
	    s20024LoanRegHst.setValid_date(loanRegHst.getValidDate());
	    s20024LoanRegHst.setBef_resch_init_term(loanRegHst.getBefExtendInitTerm());
	    s20024LoanRegHst.setBef_resch_init_prin(loanRegHst.getBefExtendInitPrin());
	    s20024LoanRegHst.setReschedule_term(loanRegHst.getBefExtendInitTerm());
	    s20024LoanRegHst.setBef_shorted_init_term(loanRegHst.getBefShortenInitTerm());
	    s20024LoanRegHst.setBef_shorted_init_prin(loanRegHst.getBefShortenInitPrin());
	    s20024LoanRegHst.setShorted_resc_type(loanRegHst.getShortedType());
	    s20024LoanRegHst.setShorted_term(loanRegHst.getShortedTerm());
	    s20024LoanRegHst.setShorted_pmt_due(loanRegHst.getShortedPmtDue());
	    s20024LoanRegHst.setRemark(loanRegHst.getRemark());

	    loanRegHsts.add(s20024LoanRegHst);
	}
	resp.setCard_no(req.getCard_no());
	resp.setLoanRegHsts(loanRegHsts);
	resp.setLoan_receipt_nbr(req.getLoan_receipt_nbr());
	LogTools.printLogger(logger, "20024", "根据借据号查询贷款变更历史", resp, false);
	return resp;
    }
}
