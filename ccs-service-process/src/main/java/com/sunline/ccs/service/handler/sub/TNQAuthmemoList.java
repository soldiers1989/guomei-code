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
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13010Req;
import com.sunline.ccs.service.protocol.S13010Resp;
import com.sunline.ccs.service.protocol.S13010Unmatch;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQAuthmemoList
 * @see 描述：授权未入账交易明细查询
 *
 * @see 创建日期： 2015-6-25下午4:33:34
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQAuthmemoList {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
	@PersistenceContext
	public EntityManager em;
	QCcsAuthmemoO qCcsAuthmemoO = QCcsAuthmemoO.ccsAuthmemoO;

	@Transactional
	public S13010Resp handler(S13010Req req) throws ProcessException {

		LogTools.printLogger(logger, "S13010", "授权未入账交易明细查询", req, true);
		// 构建响应报文对象
		S13010Resp resp = new S13010Resp();
		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);

		// 获取卡片信息
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression;
		// 主卡以账户为单位
		if (CcsCard.getBscSuppInd() == BscSuppIndicator.B) {
			booleanExpression = qCcsAuthmemoO.acctNbr.eq(CcsCard.getAcctNbr());
		}
		// 附卡以本卡为单位
		else {
			booleanExpression = qCcsAuthmemoO.logicCardNbr.eq(CcsCard.getLogicCardNbr());
		}

		if (!Constants.DEFAULT_CURR_CD.equals(req.getCurr_cd())) {
			AccountType accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(req.getCard_no(), req.getCurr_cd());
			booleanExpression = booleanExpression.and(qCcsAuthmemoO.acctType.eq(accountType));
		}
		if (req.getStart_date() != null) {
			booleanExpression = booleanExpression.and(qCcsAuthmemoO.logOlTime.goe(req.getStart_date()));
		}
		if (req.getEnd_date() != null) {
			booleanExpression = booleanExpression.and(qCcsAuthmemoO.logOlTime.before(DateUtils.addDays(req.getEnd_date(), 1)));
		}
		if (req.getStart_date() != null && req.getEnd_date() != null && DateUtils.truncatedCompareTo(req.getStart_date(), req.getEnd_date(), Calendar.DATE) > 0) {
			throw new ProcessException(Constants.ERRB004_CODE, Constants.ERRB004_MES);
		}

		List<CcsAuthmemoO> tmUnmatchOList = query.from(qCcsAuthmemoO).where(booleanExpression)
				.orderBy(qCcsAuthmemoO.logOlTime.desc(), qCcsAuthmemoO.b002CardNbr.asc()).offset(req.getFirstrow())
				.limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsAuthmemoO);

		ArrayList<S13010Unmatch> unmatchs = new ArrayList<S13010Unmatch>();
		for (CcsAuthmemoO tmUnmatchO : tmUnmatchOList) {
			S13010Unmatch unmatch = new S13010Unmatch();
			unmatch.setTxn_card_no(tmUnmatchO.getB002CardNbr());
			unmatch.setAcq_ref_no(tmUnmatchO.getAcqRefNbr());
			unmatch.setTxn_amt(tmUnmatchO.getTxnAmt());
			unmatch.setTxn_curr_cd(tmUnmatchO.getTxnCurrency());
			unmatch.setAuth_code(tmUnmatchO.getAuthCode());
			unmatch.setAcq_name_addr(tmUnmatchO.getAcqAddress());
			unmatch.setChb_txn_amt(tmUnmatchO.getChbTxnAmt());
			unmatch.setChannel(tmUnmatchO.getInputSource());

			// if(tmUnmatchO.getMcc().equals("6011")){
			// unmatch.setTerminal_type(AuthTransTerminal.ATM);
			// }else if(tmUnmatchO.getMcc().equals("6010")){
			// unmatch.setTerminal_type(AuthTransTerminal.OTC);
			// }else{
			// unmatch.setTerminal_type(AuthTransTerminal.T00);
			// }
//			unmatch.setTerminal_type(tmUnmatchO.getAuthTxnTerminal());
			unmatch.setMcc(tmUnmatchO.getMcc());
			unmatch.setTxn_direction(tmUnmatchO.getTxnDirection());
			unmatch.setTxn_status(tmUnmatchO.getAuthTxnStatus());
			unmatch.setTxn_type(tmUnmatchO.getTxnType());
			unmatch.setLog_ol_time(tmUnmatchO.getLogOlTime());
			unmatch.setLog_biz_date(tmUnmatchO.getLogBizDate());
			unmatch.setMti(tmUnmatchO.getMti());
			unmatch.setManual_auth_flag(tmUnmatchO.getManualAuthFlag());
			unmatch.setFinal_action(tmUnmatchO.getFinalAction());
			unmatch.setB039(tmUnmatchO.getB039RtnCode());
			unmatch.setPay_pwd_err_num(tmUnmatchO.getPayPwdErrNum());
			unmatchs.add(unmatch);
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsAuthmemoO).where(booleanExpression).count();

		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setUnmatchs(unmatchs);

		LogTools.printLogger(logger, "S13010", "授权未入账交易明细查询", resp, false);
		return resp;

	}

}
