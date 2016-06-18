/**
 * 
 */
package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S11020Req;
import com.sunline.ccs.service.protocol.S11020Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQCardNbr
 * @see 描述：根据手机号码和卡号后四位查询卡号
 *
 * @see 创建日期： 2015-6-25下午5:11:42
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQCardNbr {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	@Autowired
	private RCcsCustomer rCcsCustomer;
	@PersistenceContext
	public EntityManager em;
	QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;

	@Transactional
	public S11020Resp handler(S11020Req req) throws ProcessException {

		LogTools.printLogger(logger, "11020", "卡片信息查询", req, true);
		// 检查手机号
		if (StringUtils.isBlank(req.getMobileno()))
			throw new ProcessException(Constants.ERRB028_CODE, Constants.ERRB028_MES);
		// 检查卡号后四位
		if (StringUtils.isBlank(req.getCard_no_lastfour()))
			throw new ProcessException(Constants.ERRS002_CODE, Constants.ERRB002_MES);
		Iterable<CcsCustomer> iterable = rCcsCustomer.findAll(qCcsCustomer.mobileNo.eq(req.getMobileno()));
		// 检查用户信息
		if (CheckUtil.isEmpty(iterable))
			throw new ProcessException(Constants.ERRB015_CODE, Constants.ERRB015_MES);
		List<CcsAcct> CcsAccts = new ArrayList<CcsAcct>();
		for (CcsCustomer customer : iterable) {
			CcsAccts.addAll(custAcctCardQueryFacility.getAcctByCustId(customer.getCustId()));
		}
		// 检查账户信息
		if (CcsAccts.size() == 0)
			throw new ProcessException(Constants.ERRB001_CODE, Constants.ERRB001_MES);
		List<CcsCard> cards = new ArrayList<CcsCard>();
		for (CcsAcct CcsAcct : CcsAccts) {
			cards.addAll(custAcctCardQueryFacility.getCardListByAcctNbr(CcsAcct.getAcctNbr()));

		}
		if (cards.size() == 0)
			throw new ProcessException(Constants.ERRB001_CODE, Constants.ERRB001_MES);
		List<CcsCardLmMapping> cardMediaList = new ArrayList<CcsCardLmMapping>();
		for (CcsCard card : cards) {
			cardMediaList.addAll(custAcctCardQueryFacility.getCardLmMappingByLogicCardNbr(card.getLogicCardNbr()));
		}
		if (cardMediaList.size() == 0)
			throw new ProcessException(Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// 卡号
		String cardNbr = "";
		String logicCardNbr = "";
		for (CcsCardLmMapping cardMedia : cardMediaList) {
			// 后四位比较
			if (req.getCard_no_lastfour().equals(cardMedia.getCardNbr().substring(cardMedia.getCardNbr().length() - 4))) {
				cardNbr = cardMedia.getCardNbr();
				logicCardNbr = cardMedia.getLogicCardNbr();
				break;
			}

		}
		S11020Resp resp = new S11020Resp();
		for (CcsCard card : cards) {
			if (logicCardNbr.equals(card.getLogicCardNbr())) {
				resp.setActivate_date(card.getActiveDate());
				resp.setActivate_ind(card.getActiveInd());
				resp.setCard_no(cardNbr);
				resp.setCard_no_lastfour(req.getCard_no_lastfour());
				resp.setActivate_date(card.getActiveDate());
				Product product = unifiedParameterService.loadParameter(card.getProductCd(), Product.class);
				resp.setProduct_name(product.description);
				CcsCustomer customer = custAcctCardQueryFacility.getCustomerByCardNbr(cardNbr);
				resp.setId_no(customer.getIdNo());
				resp.setId_type(customer.getIdType());
				resp.setCard_expire_date(card.getCardExpireDate());
				resp.setBsc_supp_ind(card.getBscSuppInd());
				break;
			}

		}
		LogTools.printLogger(logger, "11020", "卡片信息查询", resp, false);
		return resp;

	}

}
