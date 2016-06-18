package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
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
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanRegHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanRegHst;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13085LoanReg;
import com.sunline.ccs.service.protocol.S13085Req;
import com.sunline.ccs.service.protocol.S13085Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.EnumUtils;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQLoanRegHstList
 * @see 描述：分期申请状态查询
 *
 * @see 创建日期： 2015年6月25日 上午12:18:48
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQLoanRegHstList {
	private Logger logger = LoggerFactory.getLogger(getClass());

	// 用于翻页，表明S13085响应最后一条记录字段来自于TM_LOAN_REG表
	public static String S13085_R_TYPE = "R";
	// 用于翻页，表明S13085响应最后一条记录字段来自TM_LOAN_REG_HST表
	public static String S13085_H_TYPE = "H";

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@PersistenceContext
	private EntityManager em;

	QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
	QCcsLoanRegHst qCcsLoanRegHst = QCcsLoanRegHst.ccsLoanRegHst;

	@Transactional
	public S13085Resp handler(S13085Req req) throws ProcessException {
		LogTools.printLogger(logger, "S13085", "分期申请状态查询", req, true);

		// 检查请求报文各类参数
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.rejectNull(req.getPagesize(), Constants.ERRS003_CODE, Constants.ERRS003_MES);

		// last_row_key为0则last_row_type必须为空，否则type为P或者H
		if ((StringUtils.isNotBlank(req.getLast_row_type()) && req.getLast_row_key() == 0)
				|| (req.getLast_row_key() != 0 && !S13085_R_TYPE.equals(req.getLast_row_type()) && !S13085_H_TYPE.equals(req.getLast_row_type()))) {
			throw new ProcessException(Constants.ERRS003_CODE, Constants.ERRS003_MES);
		}

		if (!EnumUtils.in(req.getLoan_type(), LoanType.values())) {
			throw new ProcessException(Constants.ERRS003_CODE, Constants.ERRS003_MES);
		}

		CcsCardO cardO = custAcctCardQueryFacility.getCardOByCardNbr(req.getCard_no());
		if (null == cardO) {
			throw new ProcessException(Constants.ERRB001_CODE, Constants.ERRB001_MES);
		}

		List<CcsLoanReg> loanRegList = new ArrayList<CcsLoanReg>();
		List<CcsLoanRegHst> loanRegHstList = new ArrayList<CcsLoanRegHst>();

		if (S13085_H_TYPE.equals(req.getLast_row_type())) {
			loanRegHstList = getCcsLoanRegHstListByS13085(req, cardO.getAcctNbr(), req.getLast_row_key(), req.getPagesize() + 1);
		} else {
			// 查询出pagesize条记录
			loanRegList = getCcsLoanRegListByS13085(req, cardO.getAcctNbr());

			// 如果TM_LOAN_REG中数据不足，则从TM_LOAN_REG_HST中补
			if (loanRegList.size() <= req.getPagesize()) {
				loanRegHstList = getCcsLoanRegHstListByS13085(req, cardO.getAcctNbr(), 0l, req.getPagesize() + 1 - loanRegList.size());
			}
		}

		// 组装响应报文
		S13085Resp resp = new S13085Resp();

		resp.setCard_no(req.getCard_no());
		resp.setLoan_type(req.getLoan_type());
		resp.setPagesize(req.getPagesize());
		resp.setStart_date(req.getStart_date());
		resp.setEnd_date(req.getEnd_date());
		resp.setLast_row_key(req.getLast_row_key());
		resp.setLast_row_type(req.getLast_row_type());

		List<S13085LoanReg> loanRegRespList = new ArrayList<S13085LoanReg>();
		if (loanRegList.size() > 0) {
			for (CcsLoanReg loanReg : loanRegList) {
				loanRegRespList.add(constructorToCcsLoanReg(loanReg));
				resp.setLast_row_key(loanReg.getRegisterId());
				resp.setLast_row_type(S13085_R_TYPE);
				if (loanRegRespList.size() == req.getPagesize()) {
					break;
				}
			}
		}

		if (loanRegList.size() < req.getPagesize() && loanRegHstList.size() > 0) {
			for (CcsLoanRegHst loanRegHst : loanRegHstList) {
				loanRegRespList.add(constructorToCcsLoanRegHst(loanRegHst));
				resp.setLast_row_key(loanRegHst.getRegisterId());
				resp.setLast_row_type(S13085_H_TYPE);
				if (loanRegRespList.size() == req.getPagesize()) {
					break;
				}
			}
		}

		// 由于上面查询的都为查询pagesize+1条数据，所以如果两个list和大于pagesize则一定有下一页
		if (loanRegList.size() + loanRegHstList.size() > req.getPagesize()) {
			resp.setNextpage(Indicator.Y.name());
		} else {
			resp.setNextpage(Indicator.N.name());
		}

		if (StringUtils.isBlank(resp.getLast_row_type())) {
			resp.setLast_row_type(S13085_R_TYPE);
		}

		resp.setLoanRegList(loanRegRespList);
		LogTools.printLogger(logger, "S13085", "分期申请状态查询", resp, false);
		return resp;
	}

	/**
	 * 根据S13085的请求报文查询TM_LOAN_REG
	 * 
	 * @param req
	 * @return
	 */
	private List<CcsLoanReg> getCcsLoanRegListByS13085(S13085Req req, Long long1) {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLoanReg.acctNbr.eq(long1));

		// 如果分期类型为空则全量查询，否则按条件查询
		if (null != req.getLoan_type()) {
			booleanExpression = booleanExpression.and(qCcsLoanReg.loanType.eq(LoanType.valueOf(req.getLoan_type())));
		}

		// 如果不是首次查询则要加入分页条件
		if (0 != req.getLast_row_key()) {
			booleanExpression = booleanExpression.and(qCcsLoanReg.registerId.lt(req.getLast_row_key()));
		}

		// 如果请求中存在起止时间则加上时间条件
		if (null != req.getStart_date()) {
			booleanExpression = booleanExpression.and(qCcsLoanReg.requestTime.goe(req.getStart_date()));
		}
		if (null != req.getEnd_date()) {
			booleanExpression = booleanExpression.and(qCcsLoanReg.requestTime.lt(req.getEnd_date()));
		}

		return query.from(qCcsLoanReg).where(booleanExpression).orderBy(qCcsLoanReg.registerId.desc()).limit(req.getPagesize() + 1).list(qCcsLoanReg);
	}

	/**
	 * 根据S13085的请求报文查询TM_LOAN_REG_HST
	 * 
	 * @param req
	 * @return
	 */
	private List<CcsLoanRegHst> getCcsLoanRegHstListByS13085(S13085Req req, Long long1, long last_row_key, long pagesize) {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsLoanRegHst.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLoanRegHst.acctNbr.eq(long1));

		// 如果分期类型为空则全量查询，否则按条件查询
		if (null != req.getLoan_type()) {
			booleanExpression = booleanExpression.and(qCcsLoanRegHst.loanType.eq(LoanType.valueOf(req.getLoan_type())));
		}

		// 如果不是首次查询则要加入分页条件
		if (0 != last_row_key) {
			booleanExpression = booleanExpression.and(qCcsLoanRegHst.registerId.lt(req.getLast_row_key()));
		}

		// 如果请求中存在起止时间则加上时间条件
		if (null != req.getStart_date()) {
			booleanExpression = booleanExpression.and(qCcsLoanRegHst.requestTime.goe(req.getStart_date()));
		}
		if (null != req.getEnd_date()) {
			booleanExpression = booleanExpression.and(qCcsLoanRegHst.requestTime.lt(req.getEnd_date()));
		}

		return query.from(qCcsLoanRegHst).where(booleanExpression).orderBy(qCcsLoanRegHst.registerId.desc()).limit(pagesize).list(qCcsLoanRegHst);
	}

	/**
	 * 根据TM_LOAN_REG为S13085LoanFeeDef填充响应报文
	 * 
	 * @param loanReq
	 * @return
	 */
	private S13085LoanReg constructorToCcsLoanReg(CcsLoanReg loanReg) {
		S13085LoanReg s13085LoanReg = new S13085LoanReg();
		s13085LoanReg.setCurr_cd(loanReg.getAcctType().getCurrencyCode());
		s13085LoanReg.setRegister_id(loanReg.getRegisterId());
		s13085LoanReg.setRef_nbr(loanReg.getRefNbr());
		s13085LoanReg.setLoan_type(null == loanReg.getLoanType() ? "" : loanReg.getLoanType().name());
		s13085LoanReg.setLoan_reg_status(null == loanReg.getLoanRegStatus() ? "" : loanReg.getLoanRegStatus().name());
		s13085LoanReg.setLoan_init_term(loanReg.getLoanInitTerm());
		s13085LoanReg.setLoan_fee_method(null == loanReg.getLoanFeeMethod() ? "" : loanReg.getLoanFeeMethod().name());
		s13085LoanReg.setLoan_init_prin(loanReg.getLoanInitPrin());
		s13085LoanReg.setLoan_fixed_pmt_prin(loanReg.getLoanFixedPmtPrin());
		s13085LoanReg.setLoan_first_term_prin(loanReg.getLoanFirstTermPrin());
		s13085LoanReg.setLoan_final_term_prin(loanReg.getLoanFinalTermPrin());
		s13085LoanReg.setLoan_init_fee1(loanReg.getLoanInitFee());
		s13085LoanReg.setLoan_fixed_fee1(loanReg.getLoanFixedFee());
		s13085LoanReg.setLoan_first_term_fee1(loanReg.getLoanFirstTermFee());
		s13085LoanReg.setLoan_final_term_fee1(loanReg.getLoanFinalTermFee());
		s13085LoanReg.setRemark(loanReg.getRemark());
		s13085LoanReg.setOrig_txn_amt(loanReg.getOrigTxnAmt());
		s13085LoanReg.setOrig_trans_date(loanReg.getOrigTransDate());
		s13085LoanReg.setOrig_auth_code(loanReg.getOrigAuthCode());
		s13085LoanReg.setLoan_code(loanReg.getLoanCode());
		s13085LoanReg.setLoan_action(loanReg.getLoanAction().name());
		return s13085LoanReg;
	}

	/**
	 * 根据TM_LOAN_REG_HST为S13085LoanFeeDef填充响应报文
	 * 
	 * @param loanReq
	 * @return
	 */
	private S13085LoanReg constructorToCcsLoanRegHst(CcsLoanRegHst loanRegHst) {
		S13085LoanReg s13085LoanRegHst = new S13085LoanReg();
		s13085LoanRegHst.setCurr_cd(loanRegHst.getAcctType().getCurrencyCode());
		s13085LoanRegHst.setRegister_id(loanRegHst.getRegisterId());
		s13085LoanRegHst.setRef_nbr(loanRegHst.getRefNbr());
		s13085LoanRegHst.setLoan_type(null == loanRegHst.getLoanType() ? "" : loanRegHst.getLoanType().name());
		s13085LoanRegHst.setLoan_reg_status(null == loanRegHst.getLoanRegStatus() ? "" : loanRegHst.getLoanRegStatus().name());
		s13085LoanRegHst.setLoan_init_term(loanRegHst.getLoanInitTerm());
		s13085LoanRegHst.setLoan_fee_method(null == loanRegHst.getLoanFeeMethod() ? "" : loanRegHst.getLoanFeeMethod().name());
		s13085LoanRegHst.setLoan_init_prin(loanRegHst.getLoanInitPrin());
		s13085LoanRegHst.setLoan_fixed_pmt_prin(loanRegHst.getLoanFixedPmtPrin());
		s13085LoanRegHst.setLoan_first_term_prin(loanRegHst.getLoanFirstTermPrin());
		s13085LoanRegHst.setLoan_final_term_prin(loanRegHst.getLoanFinalTermPrin());
		s13085LoanRegHst.setLoan_init_fee1(loanRegHst.getLoanInitFee());
		s13085LoanRegHst.setLoan_fixed_fee1(loanRegHst.getLoanFixedFee());
		s13085LoanRegHst.setLoan_first_term_fee1(loanRegHst.getLoanFirstTermFee());
		s13085LoanRegHst.setLoan_final_term_fee1(loanRegHst.getLoanFinalTermFee());
		s13085LoanRegHst.setRemark(loanRegHst.getRemark());
		s13085LoanRegHst.setOrig_txn_amt(loanRegHst.getOrigTxnAmt());
		s13085LoanRegHst.setOrig_trans_date(loanRegHst.getOrigTransDate());
		s13085LoanRegHst.setOrig_auth_code(loanRegHst.getOrigAuthCode());
		s13085LoanRegHst.setLoan_code(loanRegHst.getLoanCode());
		s13085LoanRegHst.setLoan_action(loanRegHst.getLoanAction().name());
		return s13085LoanRegHst;
	}

}
