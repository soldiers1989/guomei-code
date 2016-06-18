/**
 * 
 */
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
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctCrlmtAdjLog;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S15013Item;
import com.sunline.ccs.service.protocol.S15013Req;
import com.sunline.ccs.service.protocol.S15013Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.exception.ProcessException;

/** 
 * @see 类名：TNQCrLimitProgress
 * @see 描述：永久额度调整申请进度查询(通过卡号+币种查询)
 *
 * @see 创建日期：   2015-6-25下午5:14:56
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQCrLimitProgress {
    private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@PersistenceContext
	public EntityManager em;
	QCcsAcctCrlmtAdjLog qCcsAcctCrlmtAdjLog = QCcsAcctCrlmtAdjLog.ccsAcctCrlmtAdjLog;
	
	
	@Transactional
	public S15013Resp handler(S15013Req req) throws ProcessException {

		LogTools.printLogger(logger, "15013", "永久额度调整申请进度查询", req, true);
		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);

		// 获取账户信息
		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsAcctO CcsAcctO = custAcctCardQueryFacility.getAcctOByAcctNbr(CcsAcct.getAcctType(), CcsAcct.getAcctNbr());
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		BooleanExpression booleanExpression = qCcsAcctCrlmtAdjLog.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsAcctCrlmtAdjLog.cardNbr.eq(req.getCard_no()))
				.and(qCcsAcctCrlmtAdjLog.acctNbr.eq(CcsAcctO.getAcctNbr()));

		// 查询额度调整日志表中是否有等待调额的记录，如果有则返回有等待复核调额不能继续申请
		JPAQuery query = new JPAQuery(em);
		List<CcsAcctCrlmtAdjLog> limitAdjLogList = query.from(qCcsAcctCrlmtAdjLog).where(booleanExpression).orderBy(qCcsAcctCrlmtAdjLog.opTime.desc()).offset(req.getFirstrow())
				.limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsAcctCrlmtAdjLog);
		// tm_limit_adj_log
		S15013Resp resp = new S15013Resp();

		ArrayList<S15013Item> items = new ArrayList<S15013Item>();
		for (CcsAcctCrlmtAdjLog tmlog : limitAdjLogList) {
			S15013Item item = new S15013Item();
			item.setCredit_limit_new(tmlog.getCreditLmtNew());
			item.setCredit_limit_old(tmlog.getCreditLmtOrig());
			item.setOper_time(tmlog.getOpTime());
			item.setOpera_id(tmlog.getOpId());
			item.setRtf_state(tmlog.getAdjState().toString());
			items.add(item);
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsAcctCrlmtAdjLog).where(booleanExpression).count();

		// 构建响应报文对象
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setItems(items);
		LogTools.printLogger(logger, "15013", "永久额度调整申请进度查询", resp, false);
		return resp;
	
	}
    

}
