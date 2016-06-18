/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsPointsAdjLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsOpPrivilege;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsAdjLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsReg;
import com.sunline.ccs.param.def.ProductCredit;
// import com.sunline.ccs.ui.client.txn.t3204.T3204Inter;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.PointAdjustIndicator;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 
 * @see 类名：RewardPointsAdjustmentServer
 * @see 描述：积分调整
 *
 * @see 创建日期： Jun 26, 20152:59:37 PM
 * @author yeyu
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@SuppressWarnings("all")
@Controller
@RequestMapping(value = "/rPtsAdjustmentServer")
public class RewardPointsAdjustmentServer {

	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CPSBusProvide cpsBusProvide;

	@Autowired
	private RCcsPointsReg rCcsPointReg;

	@Autowired
	private Card2ProdctAcctFacility cardNo2ProdctAcctFacility;

	@Autowired
	private OpeLogUtil opeLogUtil;
	@Autowired
	private RCcsPointsAdjLog rCcsPointAdjLog;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private GlobalManagementService globalManagementService;

	@Autowired
	private OperatorAuthUtil operatorAuthUtil;

	/**
	 * 
	 * @see 方法名：addPoint
	 * @see 积分增加
	 * @see 创建日期：Jun 26, 20152:59:17 PM
	 * @author Liming.Feng
	 * 
	 * @param cardNo
	 * @param tmAccount
	 * @param point
	 * @param memo
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private CcsPointsReg addPoint(String cardNo, CcsAcct tmAccount,BigDecimal point, String memo) {
		CcsPointsReg tmPointReg = genTmPointReg(tmAccount, cardNo, point, memo,PointAdjustIndicator.I);
		return tmPointReg;
	}

	/**
	 * 
	 * @see 方法名：subtractPoint
	 * @see 描述：积分减少
	 * @see 创建日期：Jun 26, 20152:59:04 PM
	 * @author Liming.Feng
	 * 
	 * @param cardNo
	 * @param tmAccount
	 * @param point
	 * @param memo
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private CcsPointsReg subtractPoint(String cardNo, CcsAcct tmAccount,BigDecimal point, String memo) throws FlatException {
		CcsPointsReg tmPointReg = genTmPointReg(tmAccount, cardNo, point, memo,PointAdjustIndicator.A);
		rCcsPointReg.save(tmPointReg);
		return tmPointReg;
	}

	/**
	 * 
	 * @see 方法名：genTmPointReg
	 * @see 描述：生成积分接口文件
	 * @see 创建日期：Jun 26, 20152:58:52 PM
	 * @author Liming.Feng
	 * 
	 * @param tmAccount
	 * @param cardNo
	 * @param point
	 * @param memo
	 * @param adjustType
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private CcsPointsReg genTmPointReg(CcsAcct tmAccount, String cardNo,BigDecimal point, String memo, PointAdjustIndicator adjustType) {
		CcsPointsReg tmPointReg = new CcsPointsReg();
		tmPointReg.setAcctNbr(tmAccount.getAcctNbr());
		tmPointReg.setAcctType(tmAccount.getAcctType());
		tmPointReg.setPoints(point);
		tmPointReg.setAdjInd(adjustType);
		tmPointReg.setCardNbr(cardNo);
		tmPointReg.setOrg(tmAccount.getOrg());
		tmPointReg.setPostTxnType(PostTxnType.P);
		tmPointReg.setTxnDate(globalManagementService.getSystemStatus()
				.getBusinessDate());
		tmPointReg.setRequestTime(new Date());
		tmPointReg.setMemo(memo);
		return tmPointReg;
	}

	/**
	 * 
	 * @see 方法名：addPoint
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 26, 20152:58:45 PM
	 * @author Liming.Feng
	 * 
	 * @param cardNo
	 * @param point
	 * @param memo
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void addPoint(String cardNo, BigDecimal point, String memo) throws FlatException {
		logger.info("addPoint:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo)
				+ "],积分值[" + point + "]" + ",原因：[" + memo + "]");

		/*---- 1.校验输入项目是否合法 ---*/
		CheckUtil.checkCardNo(cardNo);
		CheckUtil.rejectNull(point, "积分值不能为空");

		// 检查操作员可调整的最大积分值
		CcsOpPrivilege tmOperAuth = operatorAuthUtil.getCurrentOperatorAuth();
		if (null == tmOperAuth || tmOperAuth.getMaxPointsAdj() == null
				|| tmOperAuth.getMaxPointsAdj().compareTo(point) < 0) {
			throw new FlatException("调整的积分值已超出操作权限范围！");
		}

		ProductCredit productCredit = cardNo2ProdctAcctFacility
				.CardNoToProductCr(cardNo);
		// 积分调整过程
		CcsAcct tmAccount = cpsBusProvide
				.getTmAccountTocardNbr(
						cardNo,
						cardNo2ProdctAcctFacility.acct_attribute(productCredit).accountType);
		CcsPointsReg tmPointReg = addPoint(cardNo, tmAccount, point, memo);
		rCcsPointReg.save(tmPointReg);
		/* 添加积分调整日志 */
		Date now = new Date();
		CcsPointsAdjLog tmPointAdjLog = new CcsPointsAdjLog();
		tmPointAdjLog.setAcctNbr(tmAccount.getAcctNbr());
		tmPointAdjLog.setAcctType(tmAccount.getAcctType());
		// 调整标志
		tmPointAdjLog.setAdjInd(PointAdjustIndicator.I);
		tmPointAdjLog.setCardNbr(cardNo);
		tmPointAdjLog.setOpId(OrganizationContextHolder.getUsername());
		tmPointAdjLog.setOpSeq(null);
		tmPointAdjLog.setOpTime(now);
		tmPointAdjLog.setOrg(OrganizationContextHolder.getCurrentOrg());
		tmPointAdjLog.setPoints(point);
		tmPointAdjLog.setMemo(memo);
		addTmPointAdjLog(tmPointAdjLog);
		// 记录操作日志
		opeLogUtil.cardholderServiceLog("3204", null, cardNo,AccountType.A.toString(), "积分调整,积分方向[调增],积分值[" + point + "]");

	}
	
	@ResponseBody()
	@RequestMapping(value = "/adjPts", method = { RequestMethod.POST })
	public void adjRewardPts(@RequestBody String cardNo,@RequestBody String strPoint, @RequestBody String memo
			,@RequestBody String pointAdjustIndicator,@RequestBody String acctType) throws FlatException{
		if(PointAdjustIndicator.valueOf(pointAdjustIndicator) == PointAdjustIndicator.A){
			//减少积分
			this.subtractPoint(cardNo, strPoint, memo, pointAdjustIndicator, acctType);
		}
		if(PointAdjustIndicator.valueOf(pointAdjustIndicator) == PointAdjustIndicator.I){
			//增加积分
			this.addPoint(cardNo, new BigDecimal(strPoint), memo);
		}
		if(PointAdjustIndicator.valueOf(pointAdjustIndicator) == PointAdjustIndicator.D){
			//兑换积分
			this.subtractPoint(cardNo, strPoint, memo, pointAdjustIndicator, acctType);
		}
	}

	/**
	 * 
	 * @see 方法名：subtractPoint
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 26, 20152:58:39 PM
	 * @author Liming.Feng
	 * 
	 * @param cardNo
	 * @param point
	 * @param memo
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void subtractPoint(String cardNo,String strPoint, String memo
			,String pointAdjustIndicator,String acctType)
			throws FlatException {
		BigDecimal point = new BigDecimal(strPoint);
		logger.info("subtractPoint:卡号[" + CodeMarkUtils.subCreditCard(cardNo)
				+ "],积分值[" + point + "]" + ",原因：[" + memo + "]");

		/*---- 1.校验输入项目是否合法 ---*/
		CheckUtil.checkCardNo(cardNo);
		CheckUtil.rejectNull(point, "积分值不能为空");

		// 检查操作员可调整的最大积分值
		CcsOpPrivilege tmOperAuth = operatorAuthUtil.getCurrentOperatorAuth();
		if (null == tmOperAuth || tmOperAuth.getMaxPointsAdj() == null
				|| tmOperAuth.getMaxPointsAdj().compareTo(point) < 0) {
			throw new FlatException("调整的积分值已超出操作权限范围！");
		}

		CcsAcct tmAccount;

		boolean flag = true;// 单币种，双币种账户标志； true 为双币种 false 单币种账户
		// 获取产品信息；
		ProductCredit productCredit = cardNo2ProdctAcctFacility.CardNoToProductCr(cardNo);
		// 判断是否为单币种账户；
		if (productCredit != null) {
			// 如果为外币账户参数id为空，说明该账户为单币种账户
			if (productCredit.dualAccountAttributeId == null) {
				flag = false;
			}
		}
		if (flag) {
			// 双币种积分调整过程，先调整外币积分，在调整外币积分，在调整本币积分
			CcsAcct dualTmAccount = cpsBusProvide
					.getTmAccountTocardNbr(cardNo, cardNo2ProdctAcctFacility
							.dualacct_attribute(productCredit).accountType);

			tmAccount = dualTmAccount;

			// 先统计外币账户TmPointReg中的积分值
			QCcsPointsReg qCcsPointReg = QCcsPointsReg.ccsPointsReg;
			JPAQuery query = new JPAQuery(em);
			List<CcsPointsReg> tmPointRegList = query
					.from(qCcsPointReg)
					.where(qCcsPointReg.acctNbr.eq(dualTmAccount.getAcctNbr())
							.and(qCcsPointReg.acctType.eq(dualTmAccount
									.getAcctType()))).list(qCcsPointReg);
			BigDecimal ttPoints = BigDecimal.ZERO;
			for (CcsPointsReg tmPointReg : tmPointRegList) {
				switch (tmPointReg.getAdjInd()) {
				case I: // 积分增加
					ttPoints = ttPoints.add(tmPointReg.getPoints());
					break;
				case A: // 积分减少
				case D: // 积分兑换
					ttPoints = ttPoints.subtract(tmPointReg.getPoints());
					break;
				}
			}

			// 统计当前外币账户的积分余额
			BigDecimal dualPoint = dualTmAccount.getPointsBal().add(ttPoints);

			// 外币币种积分够扣减
			if (dualPoint.compareTo(point) >= 0) {
				subtractPoint(cardNo, dualTmAccount, point, memo);
			} else {
				// 外币账户积分不够减，先扣减外币币种积分，在减少本币币种积分
				CcsAcct currTmAccount = cpsBusProvide
						.getTmAccountTocardNbr(
								cardNo,
								cardNo2ProdctAcctFacility
										.acct_attribute(productCredit).accountType);

				tmAccount = currTmAccount;

				BigDecimal currPoint = point;

				// 如果外币账户积分余额<=0则仅扣减本币账户积分
				if (dualPoint.compareTo(BigDecimal.ZERO) > 0) {
					currPoint = point.subtract(dualPoint);// 计算本币账户需要减少的积分
					subtractPoint(cardNo, dualTmAccount, dualPoint, memo);// 减少外币账户积分
				}

				subtractPoint(cardNo, currTmAccount, currPoint, memo);// 减少本币账户积分
			}
		} else {
			// 单币种积分扣减
			CcsAcct currTmAccount = cpsBusProvide.getTmAccountTocardNbr(cardNo, cardNo2ProdctAcctFacility.acct_attribute(productCredit).accountType);
			tmAccount = currTmAccount;
			subtractPoint(cardNo, currTmAccount, point, memo);
		}
		// 添加积分调整日志
		Date now = new Date();
		CcsPointsAdjLog tmPointAdjLog = new CcsPointsAdjLog();
		tmPointAdjLog.setAcctNbr(tmAccount.getAcctNbr());
		tmPointAdjLog.setAcctNbr(new Long(2));
		tmPointAdjLog.setAcctType(AccountType.valueOf(acctType));
		// 调整标志
		tmPointAdjLog.setAdjInd(PointAdjustIndicator.valueOf(pointAdjustIndicator));
		tmPointAdjLog.setCardNbr(cardNo);
		tmPointAdjLog.setOpId(OrganizationContextHolder.getUsername());
		tmPointAdjLog.setOpSeq(null);
		tmPointAdjLog.setOpTime(now);
		tmPointAdjLog.setOrg(OrganizationContextHolder.getCurrentOrg());
		tmPointAdjLog.setPoints(point);
		tmPointAdjLog.setMemo(memo);
		addTmPointAdjLog(tmPointAdjLog);

		// 记录操作日志
		opeLogUtil.cardholderServiceLog("3204", null, cardNo,AccountType.valueOf(acctType).toString(), "积分调整,积分方向[调减],积分值[" + point + "]");
	}

	/**
	 * 
	 * @see 方法名：getCurrentPointAdjLogList
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 26, 20152:58:31 PM
	 * @author Liming.Feng
	 * 
	 * @param request
	 * @param beginDate
	 * @param endDate
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@ResponseBody()
	@RequestMapping(value = "/getCurrentPointAdjLogList", method = { RequestMethod.POST })
	public FetchResponse getCurrentPointAdjLogList(
			@RequestBody FetchRequest request) {
		String strBegDate = (request.getParameter("begDate") != null) ? String
				.valueOf(request.getParameter("begDate")) : null;
		String strEndDate = (request.getParameter("endDate") != null) ? String
				.valueOf(request.getParameter("endDate")) : null;
		Date beginDate = null;
		Date endDate = null;
		if (strBegDate != null && StringUtils.isNotBlank(strBegDate)) {
			beginDate = new Date(Long.parseLong(strBegDate));
		}
		if (strBegDate != null && StringUtils.isNotBlank(strEndDate)) {
			endDate = new Date(Long.parseLong(strEndDate));
		}
		// 获取积分调整历史记录
		QCcsPointsAdjLog qCcsPointsAdjLog = QCcsPointsAdjLog.ccsPointsAdjLog;
		JPAQuery query = new JPAQuery(em).from(qCcsPointsAdjLog);
		{
			// 条件限于当前操作员
			query = query.where(
					qCcsPointsAdjLog.org.eq(OrganizationContextHolder
							.getCurrentOrg())).where(
					qCcsPointsAdjLog.opId.eq(OrganizationContextHolder
							.getUsername()));

			if (null != beginDate) {
				beginDate = DateUtils.truncate(beginDate, Calendar.DATE);
				query = query.where(qCcsPointsAdjLog.opTime.goe(beginDate));
			}
			if (null != endDate) {
				endDate = DateUtils.truncate(DateUtils.addDays(endDate, 1),
						Calendar.DATE);
				query = query.where(qCcsPointsAdjLog.opTime.lt(endDate));
			}

			query = query.orderBy(new OrderSpecifier<Long>(Order.DESC,
					qCcsPointsAdjLog.opSeq));
		}

		return new JPAQueryFetchResponseBuilder(request, query)
				.addFieldMapping(CcsPointsAdjLog.P_Org, qCcsPointsAdjLog.org)
				.addFieldMapping(CcsPointsAdjLog.P_OpSeq,
						qCcsPointsAdjLog.opSeq)
				.addFieldMapping(CcsPointsAdjLog.P_OpTime,
						qCcsPointsAdjLog.opTime)
				.addFieldMapping(CcsPointsAdjLog.P_OpId, qCcsPointsAdjLog.opId)
				.addFieldMapping(CcsPointsAdjLog.P_AcctNbr,
						qCcsPointsAdjLog.acctNbr)
				.addFieldMapping(CcsPointsAdjLog.P_AcctType,
						qCcsPointsAdjLog.acctType)
				.addFieldMapping(CcsPointsAdjLog.P_CardNbr,
						qCcsPointsAdjLog.cardNbr)
				.addFieldMapping(CcsPointsAdjLog.P_AdjInd,
						qCcsPointsAdjLog.adjInd)
				.addFieldMapping(CcsPointsAdjLog.P_Points,
						qCcsPointsAdjLog.points)
				.addFieldMapping(CcsPointsAdjLog.P_Memo, qCcsPointsAdjLog.memo)
				.build();
	}

	/**
	 * 
	 * @see 方法名：getTmAcountPointBal
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 26, 20152:58:24 PM
	 * @author Liming.Feng
	 * 
	 * @param cardNo
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@ResponseBody()
	@RequestMapping(value = "/getTmAcountPointBal", method = { RequestMethod.POST })
	public Map getTmAcountPointBal(@RequestBody String cardNbr) throws FlatException{
		/*---- 1.校验输入项目是否合法 ---*/
		CheckUtil.checkCardNo(cardNbr);

		CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNbr);
		CheckUtil.rejectNull(tmCard, "卡号[" + cardNbr + "]查询不到对应的卡片信息");

		List<CcsAcct> acctList = cpsBusProvide.getTmAccountListByacctNbr(tmCard
				.getAcctNbr());

		if (acctList.size() == 0) {
			throw new FlatException("卡号[" + cardNbr + "]查询不到对应的账户信息");
		}
		BigDecimal points = BigDecimal.ZERO;
		for (CcsAcct acct : acctList) {
			if (acct.getPointsBal() != null) {
				points = points.add(acct.getPointsBal());
			}
		}

		CcsAcct tmAccount = acctList.get(0);
		tmAccount.setPointsBal(points);
		return tmAccount.convertToMap();
	}

	/**
	 * 
	 * @see 方法名：addTmPointAdjLog
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：Jun 26, 20152:58:20 PM
	 * @author Liming.Feng
	 * 
	 * @param tmPointAdjLog
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	private void addTmPointAdjLog(CcsPointsAdjLog tmPointAdjLog) {
		rCcsPointAdjLog.save(tmPointAdjLog);
	}

}
