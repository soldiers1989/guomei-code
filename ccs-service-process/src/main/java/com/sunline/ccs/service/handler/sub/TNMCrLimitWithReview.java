package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
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
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctCrlmtAdjLog;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S15012Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMCrLimitWithReview
 * @see 描述： 永久额度调整（带复核）
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMCrLimitWithReview {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;

	@Autowired
	private RCcsAcctCrlmtAdjLog rCcsAcctCrlmtAdjLog;

	@PersistenceContext
	private EntityManager em;
	private QCcsAcctCrlmtAdjLog qCcsAcctCrlmtAdjLog = QCcsAcctCrlmtAdjLog.ccsAcctCrlmtAdjLog;

	/**
	 * @see 方法名：handler
	 * @see 描述：永久额度调整（带复核）handler
	 * @see 创建日期：2015年6月25日下午6:00:59
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S15012Req req) throws ProcessException {

		LogTools.printLogger(logger, "S15012", "永久额度调整（带复核）", req, true);

		// 检查上送各字段的合法性
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		if (BscSuppIndicator.S.equals(CcsCard.getBscSuppInd())) {
			throw new ProcessException(Constants.ERRB069_CODE, Constants.ERRB069_MES);
		}
		// 当上送的调整额度为空或者为负值时报金额不能为空或负值
		if (req.getCredit_limit() == null || req.getCredit_limit().compareTo(BigDecimal.valueOf(0)) < 0) {
			throw new ProcessException(Constants.ERRB092_CODE, Constants.ERRB092_MES);
		}

		// 根据卡号和币种获取账户信息
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 查询额度调整日志表中是否有等待调额的记录，如果有则返回有等待复核调额不能继续申请
		JPAQuery query = new JPAQuery(em);
		CcsAcctCrlmtAdjLog limitAdjLog = query
				.from(qCcsAcctCrlmtAdjLog)
				.where(qCcsAcctCrlmtAdjLog.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsAcctCrlmtAdjLog.acctNbr.eq(CcsAcct.getAcctNbr()))
						.and(qCcsAcctCrlmtAdjLog.acctType.eq(CcsAcct.getAcctType())).and(qCcsAcctCrlmtAdjLog.adjState.eq(AdjState.W)))
				.singleResult(qCcsAcctCrlmtAdjLog);
		if (limitAdjLog != null) {
			throw new ProcessException(Constants.ERRB094_CODE, Constants.ERRB094_MES);
		}

		// 查询额度调整日志表中最近一条额度调整成功的记录
		JPAQuery lastSuccessAdjLogListQuery = new JPAQuery(em);
		List<CcsAcctCrlmtAdjLog> lastSuccessAdjLogList = lastSuccessAdjLogListQuery
				.from(qCcsAcctCrlmtAdjLog)
				.where(qCcsAcctCrlmtAdjLog.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsAcctCrlmtAdjLog.acctNbr.eq(CcsAcct.getAcctNbr()))
						.and(qCcsAcctCrlmtAdjLog.acctType.eq(CcsAcct.getAcctType())).and(qCcsAcctCrlmtAdjLog.adjState.eq(AdjState.A)))
				.orderBy(qCcsAcctCrlmtAdjLog.opSeq.desc()).list(qCcsAcctCrlmtAdjLog);

		// 最近一次调额日期或者卡激活日期
		Date lastDate = null;
		if (0 != lastSuccessAdjLogList.size()) {
			CcsAcctCrlmtAdjLog lastSuccessAdjLog = lastSuccessAdjLogList.get(0);
			lastDate = lastSuccessAdjLog.getProcDate();
		} else {
			lastDate = CcsAcct.getSetupDate();
		}

		AccountAttribute accountAttr = unifiedParaFacilityProvide.acct_attribute(CcsCard.getProductCd());
		CheckUtil.rejectNull(accountAttr, Constants.ERRB016_CODE, Constants.ERRB016_MES);
		if (null != accountAttr.creditLimitAdjustInterval && accountAttr.creditLimitAdjustInterval > 0) {
			// 判断距离今天的时间间隔是否大于参数设置的额度调整时间间隔
			if (DateUtils.addDays(lastDate, accountAttr.creditLimitAdjustInterval).compareTo(unifiedParaFacilityProvide.BusinessDate()) > 0) {
				throw new ProcessException(Constants.ERRB131_CODE, Constants.ERRB131_MES);
			}
		}

		// 创建额度调整日志表,把记录保存到此表中，把调整状态置为等待调整，待管理人员登录内管复核，再看是否同意
		CcsAcctCrlmtAdjLog tmLmtAdjLog = new CcsAcctCrlmtAdjLog();
		tmLmtAdjLog.setOrg(OrganizationContextHolder.getCurrentOrg());
		tmLmtAdjLog.setOpTime(new Date());
		tmLmtAdjLog.setOpId(req.getOperaId());
		tmLmtAdjLog.setAcctNbr(CcsAcct.getAcctNbr());
		tmLmtAdjLog.setAcctType(CcsAcct.getAcctType());
		tmLmtAdjLog.setCardNbr(req.getCard_no());
		tmLmtAdjLog.setCreditLmtOrig(CcsAcct.getCreditLmt());
		tmLmtAdjLog.setCreditLmtNew(req.getCredit_limit());
		tmLmtAdjLog.setAdjState(AdjState.W);
		tmLmtAdjLog.setProcDate(unifiedParaFacilityProvide.BusinessDate());
		rCcsAcctCrlmtAdjLog.save(tmLmtAdjLog);
		LogTools.printLogger(logger, "15012", "永久额度调整（带复核)", null, false);
	}
}
