package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;
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
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S15051Req;
import com.sunline.ccs.service.protocol.S15051Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMLoanLimitRate
 * @see 描述： 分期额度比例设定
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMLoanLimitRate {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsAcctO rCcsAcctO;

	@PersistenceContext
	private EntityManager em;
	private QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	private QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
	private QCcsCard qCcsCard = QCcsCard.ccsCard;

	/**
	 * @see 方法名：handler
	 * @see 描述：分期额度比例设定handler
	 * @see 创建日期：2015年6月25日下午6:06:45
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
	public S15051Resp handler(S15051Req req) throws ProcessException {
		LogTools.printLogger(logger, "S15051", "分期额度比例设定", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 若更新
		if (Constants.OPT_ONE.equals(req.getOpt())) {
			CheckUtil.rejectNull(req.getLoan_limit_rt(), Constants.ERRB044_CODE, Constants.ERRB044_MES);
			if (req.getLoan_limit_rt().compareTo(BigDecimal.ONE) > 0) {
				throw new ProcessException(Constants.ERRB045_CODE, Constants.ERRB045_MES);
			}
		}

		// 获取账户列表
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsCard.logicCardNbr.eq(qCcsCardLmMapping.logicCardNbr).and(qCcsCard.acctNbr.eq(qCcsAcct.acctNbr))
				.and(qCcsCardLmMapping.cardNbr.eq(req.getCard_no()));
		if (!Constants.DEFAULT_CURR_CD.equals(req.getCurr_cd())) {
			AccountType accountType;
			accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(req.getCard_no(), req.getCurr_cd());

			booleanExpression = booleanExpression.and(qCcsAcct.acctType.eq(accountType));
		}

		List<CcsAcct> accountList = query.from(qCcsCardLmMapping, qCcsCard, qCcsAcct).where(booleanExpression).list(qCcsAcct);
		if (accountList.size() == 0) {
			throw new ProcessException(Constants.ERRS001_CODE, Constants.ERRS001_MES);
		}

		// 获取客户信息
		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());

		S15051Resp resp = new S15051Resp();
		for (CcsAcct CcsAcct : accountList) {
			CcsAcctO CcsAcctO = queryFacility.getAcctOByAcctNbr(CcsAcct.getAcctType(), CcsAcct.getAcctNbr());
			// 更新时
			if (Constants.OPT_ONE.equals(req.getOpt())) {
				// 设置分期额度比例
				CcsAcct.setLoanLmtRate(req.getLoan_limit_rt());
				rCcsAcct.save(CcsAcct);
				CcsAcctO.setLoanLmtRate(req.getLoan_limit_rt());
				rCcsAcctO.save(CcsAcctO);
				// 将短信格式处理，输出型如40.00、40.10、40.11格式数据,如果输入40.111这种格式数据会抛异常
				BigDecimal loanLmtRt = req.getLoan_limit_rt().multiply(BigDecimal.valueOf(100)).setScale(2);
/*				downMsgFacility.sendMessage(fetchMsgCdService.fetchMsgCd(CcsAcct.getProductCd(), CPSMessageCategory.CPS016), req.getCard_no(),
						ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(),
						new MapBuilder<String, Object>().add("loanLmtRt", loanLmtRt).add("currencyCd", CcsAcct.getCurrency()).build());
*/			}
			// 构建响应报文对象
			resp.setCard_no(req.getCard_no());
			resp.setCurr_cd(req.getCurr_cd());
			resp.setLoan_limit_rt(CcsAcctO.getLoanLmtRate());
		}
		LogTools.printLogger(logger, "S15051", "分期额度比例设定", resp, false);
		return resp;
	}
}
