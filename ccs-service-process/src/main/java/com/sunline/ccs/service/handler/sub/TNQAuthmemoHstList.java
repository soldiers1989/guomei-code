/**
 * 
 */
package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.Calendar;
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
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoHst;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13020Req;
import com.sunline.ccs.service.protocol.S13020Resp;
import com.sunline.ccs.service.protocol.S13020Unmatch;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQAuthmemoHstList
 * @see 描述：授权交易历史查询
 *
 * @see 创建日期： 2015-6-25下午4:39:35
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQAuthmemoHstList {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
	@PersistenceContext
	public EntityManager em;

	QCcsAuthmemoHst qCcsAuthmemoHst = QCcsAuthmemoHst.ccsAuthmemoHst;

	@Transactional
	public S13020Resp handler(S13020Req req) throws ProcessException {

		LogTools.printLogger(logger, "S13020", "授权交易历史查询", req, true);
		// 构建响应报文对象
		S13020Resp resp = new S13020Resp();
		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);

		// 获取卡片信息
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsAuthmemoHst.org.eq(OrganizationContextHolder.getCurrentOrg());
		// 主卡以账户为单位
		if (CcsCard.getBscSuppInd() == BscSuppIndicator.B) {
			booleanExpression = qCcsAuthmemoHst.acctNbr.eq(CcsCard.getAcctNbr());
		}
		// 附卡以本卡为单位
		else {
			booleanExpression = qCcsAuthmemoHst.logicCardNbr.eq(CcsCard.getLogicCardNbr());
		}
		if (!Constants.DEFAULT_CURR_CD.equals(req.getCurr_cd())) {
			AccountType accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(req.getCard_no(), req.getCurr_cd());
			booleanExpression = booleanExpression.and(qCcsAuthmemoHst.acctType.eq(accountType));
		}

		if (req.getStart_date() != null && req.getEnd_date() != null && DateUtils.truncatedCompareTo(req.getStart_date(), req.getEnd_date(), Calendar.DATE) > 0) {
			throw new ProcessException(Constants.ERRB004_CODE, Constants.ERRB004_MES);
		}
		if (req.getStart_date() != null) {
			booleanExpression = booleanExpression.and(qCcsAuthmemoHst.logOlTime.goe(req.getStart_date()));
		}
		if (req.getEnd_date() != null) {
			booleanExpression = booleanExpression.and(qCcsAuthmemoHst.logOlTime.before(DateUtils.addDays(req.getEnd_date(), 1)));
		}
		List<CcsAuthmemoHst> tmAuthHstsList = query.from(qCcsAuthmemoHst).where(booleanExpression)
				.orderBy(qCcsAuthmemoHst.logOlTime.desc(), qCcsAuthmemoHst.b002CardNbr.asc()).offset(req.getFirstrow())
				.limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsAuthmemoHst);

		ArrayList<S13020Unmatch> unmatchs = new ArrayList<S13020Unmatch>();
		for (CcsAuthmemoHst tmAuthHst : tmAuthHstsList) {
			S13020Unmatch unmatch = new S13020Unmatch();
			unmatch.setTxn_card_no(tmAuthHst.getB002CardNbr());
			unmatch.setAcq_ref_no(tmAuthHst.getAcqRefNbr());
			unmatch.setTxn_amt(tmAuthHst.getTxnAmt());
			unmatch.setTxn_curr_cd(tmAuthHst.getTxnCurrency());
			unmatch.setAuth_code(tmAuthHst.getAuthCode());
			unmatch.setAcq_name_addr(tmAuthHst.getAcqAddress());
			unmatch.setChb_txn_amt(tmAuthHst.getChbTxnAmt());
			unmatch.setChannel(tmAuthHst.getInputSource());

			// if(tmAuthHst.getMcc().equals("6011")){
			// unmatch.setTerminal_type(AuthTransTerminal.ATM);
			// }else if(tmAuthHst.getMcc().equals("6010")){
			// unmatch.setTerminal_type(AuthTransTerminal.OTC);
			// }else{
			// unmatch.setTerminal_type(AuthTransTerminal.T00);
			// }
//			unmatch.setTerminal_type(tmAuthHst.getAuthTxnTerminal());
			unmatch.setMcc(tmAuthHst.getMcc());
			unmatch.setTxn_direction(tmAuthHst.getTxnDirection());
			unmatch.setTxn_status(tmAuthHst.getAuthTxnStatus());
			unmatch.setTxn_type(tmAuthHst.getTxnType());
			unmatch.setLog_ol_time(tmAuthHst.getLogOlTime());
			unmatch.setLog_biz_date(tmAuthHst.getLogBizDate());
			unmatch.setMti(tmAuthHst.getMti());
			unmatch.setManual_auth_flag(tmAuthHst.getManualAuthFlag());
			unmatch.setFinal_action(tmAuthHst.getFinalAction());
			unmatch.setB039(tmAuthHst.getB039RtnCode());
			unmatch.setPay_pwd_err_num(tmAuthHst.getPayPwdErrNum());

			unmatchs.add(unmatch);
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsAuthmemoHst).where(booleanExpression).count();
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setUnmatchs(unmatchs);

		LogTools.printLogger(logger, "S13020", "授权交易历史查询", resp, false);
		return resp;

	}

}
