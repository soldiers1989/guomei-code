package com.sunline.ccs.service.handler.sub;

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
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12100Req;
import com.sunline.ccs.service.protocol.S12100Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMDirectDebit
 * @see 描述： 约定还款签约/取消
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMDirectDebit {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	@Autowired
	private Common common;

	@Autowired
	private RCcsAcct rCcsAcct;

	@PersistenceContext
	private EntityManager em;
	private QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	private QCcsCard qCcsCard = QCcsCard.ccsCard;
	private QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;

	/**
	 * @see 方法名：handler
	 * @see 描述：约定还款签约/取消handler
	 * @see 创建日期：2015年6月25日下午6:03:53
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
	public S12100Resp handler(S12100Req req) throws ProcessException {
		LogTools.printLogger(logger, "S12100", "约定还款签约/取消", req, true);

		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)
				&& !StringUtils.equals(req.getOpt(), Constants.OPT_TWO)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		if (Constants.OPT_ONE.equals(req.getOpt())) {
			if (StringUtils.isNotBlank(req.getDd_bank_acct_no()) && (req.getDd_bank_acct_no().length() < 13 || req.getDd_bank_acct_no().length() > 19)) {
				throw new ProcessException(Constants.ERRB035_CODE, Constants.ERRB035_MES);
			}
		}

		// 获取账户列表
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsCard.logicCardNbr.eq(qCcsCardLmMapping.logicCardNbr).and(qCcsCard.acctNbr.eq(qCcsAcct.acctNbr))
				.and(qCcsCardLmMapping.cardNbr.eq(req.getCard_no()));

		// 若"000"则全币种，即非全币种则增加币种对应的账户类型条件
		if (!Constants.DEFAULT_CURR_CD.equals(req.getCurr_cd())) {
			AccountType accountType;
			accountType = cardNbrTOProdctAcctFacility.CardNoCurrCdToAccoutType(req.getCard_no(), req.getCurr_cd());
			booleanExpression = booleanExpression.and(qCcsAcct.acctType.eq(accountType));

		}

		List<CcsAcct> CcsAcctList = query.from(qCcsCardLmMapping, qCcsCard, qCcsAcct).where(booleanExpression).list(qCcsAcct);
		if (CcsAcctList.size() == 0) {
			throw new ProcessException(Constants.ERRS001_CODE, Constants.ERRS001_MES);
		}

		// 获取客户信息
		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());

		S12100Resp resp = new S12100Resp();
		boolean isSendMessage = false;
		for (CcsAcct CcsAcct : CcsAcctList) {
			// 查询
			if (Constants.OPT_ZERO.equals(req.getOpt())) {
				resp.setCard_no(req.getCard_no());
				resp.setCurr_cd(req.getCurr_cd());
				resp.setDd_bank_acct_name(CcsAcct.getDdBankAcctName());
				resp.setDd_bank_acct_no(CcsAcct.getDdBankAcctNbr());
				resp.setDd_bank_branch(CcsAcct.getDdBankBranch());
				resp.setDd_bank_name(CcsAcct.getDdBankName());
				resp.setDd_ind(CcsAcct.getDdInd());
				break;
			}
			// 若更新
			if (Constants.OPT_ONE.equals(req.getOpt())) {
				if (req.getOpt() != null && req.getDd_ind().equals(DdIndicator.N)) {
					throw new ProcessException(Constants.ERRB116_CODE, Constants.ERRB116_MES);
				}
				if (req.getDd_ind() != null) {
					CcsAcct.setDdInd(req.getDd_ind());
				}
				if (StringUtils.isNotBlank(req.getDd_bank_name())) {
					CcsAcct.setDdBankName(req.getDd_bank_name());
				}
				if (StringUtils.isNotBlank(req.getDd_bank_branch())) {
					CcsAcct.setDdBankBranch(req.getDd_bank_branch());
				}
				if (StringUtils.isNotBlank(req.getDd_bank_acct_no())) {
					CcsAcct.setDdBankAcctNbr(req.getDd_bank_acct_no());
				}
				if (StringUtils.isNotBlank(req.getDd_bank_acct_name())) {
					if (StringUtils.trim(CcsAcct.getName()).equals(StringUtils.trim(req.getDd_bank_acct_name()))) {
						CcsAcct.setDdBankAcctName(req.getDd_bank_acct_name());
					} else {
						throw new ProcessException(Constants.ERRB036_CODE, Constants.ERRB036_MES);
					}
				}
				rCcsAcct.save(CcsAcct);

				if (!isSendMessage) {
					// 绑定约定还款
					// product = unifiedParameterFacility.loadParameter(
					// CcsAcct.getProductCd(),ProductCredit.class);
					// messageService.sendMessage(MessageCategory.O02,
					// CcsAcct.getProductCd(), req.getCard_no(),
					// ccsCustomer.getName(), ccsCustomer.getGender(),
					// ccsCustomer.getMobileNo(),
					// ccsCustomer.getEmail(), new Date(), new
					// MapBuilder<String,
					// Object>().add("ddInd",
					// CcsAcct.getDdInd()).add("nextStmtDate",
					// CcsAcct.getNextStmtDate()).build());
/*					downMsgFacility.sendMessage(fetchMsgCdService.fetchMsgCd(CcsAcct.getProductCd(), CPSMessageCategory.CPS038), req.getCard_no(),
							ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(),
							new MapBuilder<String, Object>().add("ddInd", CcsAcct.getDdInd()).add("nextStmtDate", CcsAcct.getNextStmtDate()).build());
*/					// 构建响应报文对象
					resp.setCard_no(req.getCard_no());
					resp.setCurr_cd(req.getCurr_cd());
					resp.setDd_ind(CcsAcct.getDdInd());
					resp.setDd_bank_name(CcsAcct.getDdBankName());
					resp.setDd_bank_branch(CcsAcct.getDdBankBranch());
					resp.setDd_bank_acct_no(CcsAcct.getDdBankAcctNbr());
					resp.setDd_bank_acct_name(CcsAcct.getDdBankAcctName());

					isSendMessage = true;
				}
			}

			// 约定还款取消
			if (Constants.OPT_TWO.equals(req.getOpt())) {
				if (CcsAcct.getDdInd() == DdIndicator.N) {
					throw new ProcessException(Constants.ERRB050_CODE, Constants.ERRB050_MES);
				}
				CcsAcct.setDdInd(DdIndicator.N);
				rCcsAcct.save(CcsAcct);

				// 发短信
				common.sendMsg(CcsAcct.getProductCd(), CcsAcct.getCustId(), req.getCard_no(), CPSMessageCategory.CPS057);

				// 构建响应报文对象
				resp.setCard_no(req.getCard_no());
				resp.setCurr_cd(req.getCurr_cd());
				resp.setDd_ind(DdIndicator.N);
			}
		}
		LogTools.printLogger(logger, "S12100", "约定还款签约/取消", resp, false);
		return resp;
	}
}
