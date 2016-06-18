package com.sunline.ccs.service.handler.sub;

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
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsGiftGoods;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsUseLog;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S17021Req;
import com.sunline.ccs.service.protocol.S17021Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.PointAdjustIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQPointsExchangeBank
 * @see 描述：积分兑换(礼品信息在行内时，用此接口)
 *
 * @see 创建日期： 2015-6-25下午12:29:19
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQPointsExchangeBank {
	@PersistenceContext
	public EntityManager em;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private RCcsPointsReg rCcsPointsReg;
	@Autowired
	private Common pointComm;
	private Logger logger = LoggerFactory.getLogger(getClass());
	QCcsPointsUseLog qCcsPointsUseLog = QCcsPointsUseLog.ccsPointsUseLog;
	QCcsGiftGoods qCcsGiftGoods = QCcsGiftGoods.ccsGiftGoods;
	QCcsAddress qCcsAddress = QCcsAddress.ccsAddress;
	QCcsPointsReg qCcsPointsReg = QCcsPointsReg.ccsPointsReg;

	@Transactional
	public S17021Resp handler(S17021Req req) throws ProcessException {

		LogTools.printLogger(logger, "S17021", "积分兑换(礼品信息在行内时，用此接口)", req, true);
		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);
		// 校验操作码
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}
		CheckUtil.rejectNull(req.getRef_nbr(), Constants.ERRB123_CODE, Constants.ERRB123_MES);
		if (req.getRef_nbr().length() > 23) {
			throw new ProcessException(Constants.ERRB126_CODE, Constants.ERRB126_MES);
		}

		CcsAcct CcsAcct = queryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		S17021Resp resp = new S17021Resp();
		// 查询时
		if (req.getOpt().equals(Constants.OPT_ZERO)) {
			// 通过卡号和交易参考号查询积分兑换注册信息
			JPAQuery query = new JPAQuery(em);
			CcsPointsReg tmPointReg = query
					.from(qCcsPointsReg)
					.where(qCcsPointsReg.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsPointsReg.cardNbr.eq(req.getCard_no()))
							.and(qCcsPointsReg.refNbr.eq(req.getRef_nbr()))).singleResult(qCcsPointsReg);
			CheckUtil.rejectNull(tmPointReg, Constants.ERRB124_CODE, Constants.ERRB124_MES);
			resp.setExch_bonus(tmPointReg.getPoints().intValue());
		} else {
			// 兑换时
			CheckUtil.rejectNull(req.getExch_bonus(), Constants.ERRB127_CODE, Constants.ERRB127_MES);
			if (req.getExch_bonus() <= 0) {
				throw new ProcessException(Constants.ERRB127_CODE, Constants.ERRB127_MES);
			}
			List<CcsPointsReg> pointsRegList = (List<CcsPointsReg>) rCcsPointsReg.findAll(qCcsPointsReg.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
					qCcsPointsReg.refNbr.eq(req.getRef_nbr())));
			// 根据参考号查询不为空，抛出异常
			if (!pointsRegList.isEmpty()) {
				throw new ProcessException(Constants.ERRB125_CODE, Constants.ERRB125_MES);
			}
			CcsPointsReg pointsReg = pointComm.subtractPoint(req.getCard_no(), CcsAcct, req.getExch_bonus(), PointAdjustIndicator.D, req.getRef_nbr());
			rCcsPointsReg.save(pointsReg);
			resp.setExch_bonus(req.getExch_bonus());
		}
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setRef_nbr(req.getRef_nbr());
		// 当期兑换积分
		resp.setCurr_exch_point(CcsAcct.getCtdSpendPoints().add(pointComm.countReducePoint(CcsAcct)).intValue());
		// 积分余额
		resp.setPoint_bal(CcsAcct.getPointsBal().subtract(pointComm.countReducePoint(CcsAcct)).intValue());

		LogTools.printLogger(logger, "S17021", "积分兑换(礼品信息在行内时，用此接口)", resp, false);
		return resp;
	}
}
