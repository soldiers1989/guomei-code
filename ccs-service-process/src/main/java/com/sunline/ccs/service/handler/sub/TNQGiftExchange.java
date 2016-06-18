package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsUseLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsUseLog;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S17012Exch;
import com.sunline.ccs.service.protocol.S17012Req;
import com.sunline.ccs.service.protocol.S17012Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQGiftExchange
 * @see 描述：积分兑换模式查询
 *
 * @see 创建日期： 2015-6-25上午11:47:16
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQGiftExchange {
	@PersistenceContext
	public EntityManager em;
	private Logger logger = LoggerFactory.getLogger(getClass());
	QCcsPointsUseLog qCcsPointsUseLog = QCcsPointsUseLog.ccsPointsUseLog;

	@Transactional
	public S17012Resp handler(S17012Req req) throws ProcessException {

		LogTools.printLogger(logger, "S17012", "积分兑换", req, true);
		int totalRows = 0;
		CheckUtil.checkCardNo(req.getCard_no());
		BooleanExpression exp = qCcsPointsUseLog.txnCardNo.eq(req.getCard_no());
		if (req.getStart_date() != null) {
			exp = exp.and(qCcsPointsUseLog.redeemApplDate.goe(req.getStart_date()));
		}
		if (req.getEnd_date() != null) {
			exp = exp.and(qCcsPointsUseLog.redeemApplDate.loe(req.getEnd_date()));
		}
		if (req.getStart_date() != null && req.getEnd_date() != null && DateUtils.truncatedCompareTo(req.getStart_date(), req.getEnd_date(), Calendar.DATE) > 0) {
			throw new ProcessException(Constants.ERRB004_CODE, Constants.ERRB004_MES);
		}

		JPAQuery querycount = new JPAQuery(em);// 查询总页数
		JPAQuery query = new JPAQuery(em);// 查询
		totalRows = (int) querycount.from(qCcsPointsUseLog).where(exp).count();
		List<CcsPointsUseLog> tmExchHsts = query.from(qCcsPointsUseLog).where(exp)
				.orderBy(qCcsPointsUseLog.redeemApplDate.desc(), qCcsPointsUseLog.txnCardNo.asc()).offset(req.getFirstrow())
				.limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsPointsUseLog);

		ArrayList<S17012Exch> s17012Exchs = new ArrayList<S17012Exch>();
		for (CcsPointsUseLog useLog : tmExchHsts) {
			S17012Exch exch = new S17012Exch();
			exch.setCard_no(useLog.getTxnCardNo());
			exch.setItem_id(useLog.getGiftNbr().toString());
			exch.setItem_name(useLog.getGiftName());
			exch.setExch_apply_date(useLog.getRedeemApplDate());
			exch.setItem_cnt(useLog.getGiftUsedCnt());
			exch.setExch_bonus(useLog.getPointsNeeded());
			exch.setItem_price(useLog.getGiftPrice());
			exch.setExch_type(useLog.getRedeemMethod());
			exch.setSend_ind(useLog.getSendInd());
			exch.setAddr_type(useLog.getAddrType());
			exch.setReceive_name(useLog.getReceiveName());
			exch.setReceive_mobile(useLog.getReceiveMobile());
			exch.setReceive_phone(useLog.getReceivePhone());
			exch.setReceive_address(useLog.getReceiveAddress());
			s17012Exchs.add(exch);
		}
		S17012Resp resp = new S17012Resp();
		resp.setCard_no(req.getCard_no());
		resp.setStart_date(req.getStart_date());
		resp.setEnd_date(req.getEnd_date());
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setExchs(s17012Exchs);
		LogTools.printLogger(logger, "S17012", "礼品信息查询", resp, false);
		return resp;
	}

}
