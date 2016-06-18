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
import com.sunline.ccs.service.protocol.S15050Req;
import com.sunline.ccs.service.protocol.S15050Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMCashLimitRate
 * @see 描述： 取现额度比例设定
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMCashLimitRate {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;

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
	 * @see 描述：取现额度比例设定handler
	 * @see 创建日期：2015年6月25日下午5:53:53
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
	public S15050Resp handler(S15050Req req) throws ProcessException {
		LogTools.printLogger(logger, "S15050", "取现额度比例设定", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 若更新
		if (Constants.OPT_ONE.equals(req.getOpt())) {
			CheckUtil.rejectNull(req.getCash_limit_rt(), Constants.ERRB042_CODE, Constants.ERRB042_MES);
			if (req.getCash_limit_rt().compareTo(BigDecimal.ONE) > 0) {
				throw new ProcessException(Constants.ERRB043_CODE, Constants.ERRB043_MES);
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

		S15050Resp resp = new S15050Resp();
		for (CcsAcct CcsAcct : accountList) {
			CcsAcctO tnAccountO = queryFacility.getAcctOByAcctNbr(CcsAcct.getAcctType(), CcsAcct.getAcctNbr());
			// 更新时
			if (Constants.OPT_ONE.equals(req.getOpt())) {
				// 设置取现额度比例
				CcsAcct.setCashLmtRate(req.getCash_limit_rt());
				rCcsAcct.save(CcsAcct);
				tnAccountO.setCashLmtRate(req.getCash_limit_rt());
				rCcsAcctO.save(tnAccountO);

/*				downMsgFacility.sendMessage(
						fetchMsgCdService.fetchMsgCd(CcsAcct.getProductCd(), CPSMessageCategory.CPS015),
						req.getCard_no(),
						ccsCustomer.getName(),
						ccsCustomer.getGender(),
						ccsCustomer.getMobileNo(),
						new Date(),
						new MapBuilder<String, Object>().add("cashLmtRt", req.getCash_limit_rt().multiply(new BigDecimal("100")))
								.add("currencyCd", CcsAcct.getCurrency()).build());
*/
			}
			// 构建响应报文对象
			resp.setCard_no(req.getCard_no());
			resp.setCurr_cd(req.getCurr_cd());
			resp.setCash_limit_rt(tnAccountO.getCashLmtRate());
		}
		LogTools.printLogger(logger, "S15050", "取现额度比例设定", resp, false);
		return resp;
	}
}
