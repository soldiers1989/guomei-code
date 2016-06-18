package com.sunline.ccs.service.handler.sub;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnHst;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12110Req;
import com.sunline.ccs.service.protocol.S12110Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMBillingCycle
 * @see 描述： 账单日修改
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMBillingCycle {

	private Logger logger = LoggerFactory.getLogger(getClass());

/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;

	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsAcctO rCcsAcctO;
	@Autowired
	private RCcsTxnHst rCcsTxnHst;
	@Autowired
	private RCcsTxnUnstatement rCcsTxnUnstatement;

	@PersistenceContext
	private EntityManager em;
	private QCcsTxnHst qCcsTxnHst = QCcsTxnHst.ccsTxnHst;
	private QCcsTxnUnstatement qCcsTxnUnstatement = QCcsTxnUnstatement.ccsTxnUnstatement;

	/**
	 * @see 方法名：handler
	 * @see 描述：账单日修改handler
	 * @see 创建日期：2015年6月25日下午5:44:12
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
	public S12110Resp handler(S12110Req req) throws ProcessException {
		LogTools.printLogger(logger, "S12110", "账单日修改", req, true);
		// 校验卡号、账单周期
		CheckUtil.checkCardNo(req.getCard_no());
		if (req.getBilling_cycle() == null || req.getBilling_cycle() < 1 || req.getBilling_cycle() > 28) {
			throw new ProcessException(Constants.ERRB048_CODE, Constants.ERRB048_MES);
		}

		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		if (CcsCard.getBscSuppInd() == BscSuppIndicator.S) {
			throw new ProcessException(Constants.ERRB069_CODE, Constants.ERRB069_MES);
		}

		// 获取账户列表
		List<CcsAcct> CcsAcctList;
		CcsAcctList = queryFacility.getAcctByCardNbr(req.getCard_no());
		if (CcsAcctList.size() == 0) {
			throw new ProcessException(Constants.ERRS001_CODE, Constants.ERRS001_MES);
		}

		// 获取客户信息
		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());

		S12110Resp resp = new S12110Resp();
		for (CcsAcct CcsAcct : CcsAcctList) {

			// 上送账单日与原账单日相同 = 拒绝
			if (req.getBilling_cycle() == Integer.parseInt(CcsAcct.getCycleDay().trim())) {
				throw new ProcessException(Constants.ERRB047_CODE, Constants.ERRB047_MES);
			}

			// 已修改账单日次数 > 参数最大账单日修改次数 = 拒绝
			Integer maxCycChange;
			maxCycChange = unifiedParaFacilityProvide.organization().maxCycChange;

			if (CcsAcct.getYtdCycleChagCnt() >= maxCycChange) {
				throw new ProcessException(Constants.ERRB046_CODE, Constants.ERRB046_MES);
			}

			CcsAcctO CcsAcctO = queryFacility.getAcctOByAcctNbr(CcsAcct.getAcctType(), CcsAcct.getAcctNbr());
			// 原账单周期
			String billingCycle = CcsAcct.getCycleDay();
			// 更新
			CcsAcct.setCycleDay(String.format("%02d", req.getBilling_cycle()));
			CcsAcct.setYtdCycleChagCnt(CcsAcct.getYtdCycleChagCnt() + 1);
			// 计算下一个账单日
			Date businessDate = unifiedParaFacilityProvide.BusinessDate();
			Date origNextStmtDate = CcsAcct.getNextStmtDate();
			Date nextStmtDate = DateUtils.setDays(businessDate, req.getBilling_cycle());
			// 与当前日期修正
			if (DateUtils.truncatedCompareTo(businessDate, nextStmtDate, Calendar.DATE) > 0) {
				nextStmtDate = DateUtils.addMonths(nextStmtDate, 1);
			}
			// 与宽限日期修正
			if (CcsAcct.getGraceDate() != null) {
				if (DateUtils.truncatedCompareTo(CcsAcct.getGraceDate(), nextStmtDate, Calendar.DATE) > 0) {
					nextStmtDate = DateUtils.addMonths(nextStmtDate, 1);
				}
			}
			CcsAcct.setNextStmtDate(nextStmtDate);
			rCcsAcct.save(CcsAcct);

			// 联机表更新
			CcsAcctO.setCycleDay(String.format("%02d", req.getBilling_cycle()));
			rCcsAcctO.save(CcsAcctO);

			// 交易流水表更新
			List<CcsTxnHst> txnList = new JPAQuery(em).from(qCcsTxnHst).where(qCcsTxnHst.org.eq(CcsAcct.getOrg()).and(qCcsTxnHst.acctNbr.eq(CcsAcct.getAcctNbr())).and(qCcsTxnHst.acctType.eq(CcsAcct.getAcctType())).and(qCcsTxnHst.stmtDate.eq(origNextStmtDate))).list(qCcsTxnHst);
			for (CcsTxnHst tmTxnHst : txnList) {
				tmTxnHst.setStmtDate(nextStmtDate);
				rCcsTxnHst.save(tmTxnHst);
			}

			// 未出账单交易表更新
			List<CcsTxnUnstatement> txnUnstmtList = new JPAQuery(em).from(qCcsTxnUnstatement)
					.where(qCcsTxnUnstatement.org.eq(CcsAcct.getOrg()).and(qCcsTxnUnstatement.acctNbr.eq(CcsAcct.getAcctNbr())).and(qCcsTxnUnstatement.acctType.eq(CcsAcct.getAcctType())).and(qCcsTxnUnstatement.stmtDate.eq(origNextStmtDate))).list(qCcsTxnUnstatement);
			for (CcsTxnUnstatement tmTxnUnstmt : txnUnstmtList) {
				tmTxnUnstmt.setStmtDate(nextStmtDate);
				rCcsTxnUnstatement.save(tmTxnUnstmt);
			}

			// 账单日修改提醒短信
			// product =
			// unifiedParameterFacility.loadParameter(CcsAcct.getProductCd(),ProductCredit.class);

/*			downMsgFacility.sendMessage(fetchMsgCdService.fetchMsgCd(CcsAcct.getProductCd(), CPSMessageCategory.CPS022), req.getCard_no(), ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(), new MapBuilder<String, Object>().add("oldBillingCycle", billingCycle)
					.add("newBillingCycle", req.getBilling_cycle()).build());
*/
			// 构建响应报文对象
			resp.setCard_no(req.getCard_no());
			resp.setBilling_cycle(Integer.parseInt(CcsAcct.getCycleDay()));
			resp.setLast_stmt_date(CcsAcct.getLastStmtDate());
			resp.setNext_stmt_date(CcsAcct.getNextStmtDate());
			resp.setPmt_due_date(CcsAcct.getPmtDueDate());
			resp.setDd_date(CcsAcct.getDdDate());
			resp.setUsed_cnt(CcsAcct.getYtdCycleChagCnt());
			resp.setAval_cnt(maxCycChange - CcsAcct.getYtdCycleChagCnt());
		}
		LogTools.printLogger(logger, "S12110", "账单日修改", resp, false);
		return resp;
	}
}
