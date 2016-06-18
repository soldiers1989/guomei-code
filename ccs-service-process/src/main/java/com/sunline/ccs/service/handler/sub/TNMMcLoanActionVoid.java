package com.sunline.ccs.service.handler.sub;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20027Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMMcLoanActionVoid
 * @see 描述： 当日贷款变更撤销
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMMcLoanActionVoid {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	@Autowired
	private RCcsLoanReg rCcsLoanReg;

	@PersistenceContext
	private EntityManager em;
	private QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;

	/**
	 * @see 方法名：handler
	 * @see 描述：当日贷款变更撤销handler
	 * @see 创建日期：2015年6月26日上午10:52:30
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S20027Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20027", "当日贷款变更撤销", req, true);
		// 检查上送请求报文字段
		CheckUtil.checkLoanReceiptNbr(req.getLoan_receipt_nbr());
		CheckUtil.checkCardNo(req.getCard_no());
		if (req.getRegister_id() == null) {
			throw new ProcessException(Constants.ERRC037_CODE, Constants.ERRC037_MES);
		}
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// 根据分期顺序号查询当天注册没有审核的变更
		CcsLoanReg loanReg = rCcsLoanReg.findOne(qCcsLoanReg.dueBillNo.eq(req.getLoan_receipt_nbr()).and(qCcsLoanReg.registerId.eq(Long.valueOf(req.getRegister_id())))
				.and(qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLoanReg.logicCardNbr.eq(CcsCard.getLogicCardNbr()))));
		CheckUtil.rejectNull(loanReg, Constants.ERRC017_CODE, Constants.ERRC017_MES);
		switch (loanReg.getLoanAction()) {
		case P:
			// 当贷款变更行为为提前还款时
			loanReg.setLoanRegStatus(LoanRegStatus.V);
			// TODO 短信
			break;
		case R:
		case S:
			// 当贷款变更行为为展期和缩期时,并且内管还没有审核
			if (loanReg.getLoanRegStatus().equals(LoanRegStatus.N)) {
				loanReg.setLoanRegStatus(LoanRegStatus.V);
				// TODO 短信
			} else {
				throw new ProcessException(Constants.ERRC038_CODE, Constants.ERRC038_MES);
			}
			break;
		default:
			throw new ProcessException(Constants.ERRC038_CODE, Constants.ERRC038_MES);
		}
		LogTools.printLogger(logger, "S20027", "当日贷款变更撤销", null, false);
	}
}
