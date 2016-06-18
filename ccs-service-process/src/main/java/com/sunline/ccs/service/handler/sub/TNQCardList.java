/**
 * 
 */
package com.sunline.ccs.service.handler.sub;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14000Card;
import com.sunline.ccs.service.protocol.S14000Req;
import com.sunline.ccs.service.protocol.S14000Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DataTypeUtils;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQCardList
 * @see 描述：按证件号码查询卡片列表
 *
 * @see 创建日期： 2015-6-25下午4:48:19
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQCardList {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;
	// @Resource(name="apsQueryService")
	// private APSQueryService apsQueryService;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private RCcsCustomer rCcsCustomer;
	@PersistenceContext
	public EntityManager em;
	QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
	QCcsCard qCcsCard = QCcsCard.ccsCard;
	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;

	@Transactional
	public S14000Resp handler(S14000Req req) throws ProcessException {

		LogTools.printLogger(logger, "S14000", "按证件号码查询卡片列表", req, true);
		// 构建响应报文对象
		S14000Resp resp = new S14000Resp();

		// 校验上送报文域
		if (!CheckUtil.isIdNo(req.getId_type(), req.getId_no())) {
			throw new ProcessException(Constants.ERRB021_CODE, Constants.ERRB021_MES);
		}
		// 判断客户信息是否存在
		CcsCustomer ccsCustomer = custAcctCardQueryFacility.getCustomerById(req.getId_no(), req.getId_type());
		if (ccsCustomer == null)
			throw new ProcessException(Constants.ERRB015_CODE, Constants.ERRB015_MES);

		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsCustomer.custId.eq(qCcsCard.custId).and(qCcsCustomer.idNo.eq(req.getId_no()))
				.and(qCcsCustomer.idType.eq(req.getId_type()));

		List<CcsCard> CcsCardList = query.from(qCcsCustomer, qCcsCard).where(booleanExpression)
				.orderBy(qCcsCard.logicCardNbr.desc(), qCcsCard.cardExpireDate.desc()).offset(req.getFirstrow())
				.limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsCard);

		// if (CcsCardList == null || CcsCardList.size() == 0) {
		// throw new
		// ProcessException(Constants.ERRS001_CODE,Constants.ERRS001_MES);
		// }
		// List<CcsCard> CcsCardList =
		// custAcctCardQueryFacility.getCcsCardByIdNo_idType(req.getId_no(),
		// req.getId_type());

		ArrayList<S14000Card> cards = new ArrayList<S14000Card>();
		for (CcsCard CcsCard : CcsCardList) {
			// 通过逻辑卡查询对应介质卡列表
			List<CcsCardLmMapping> CcsCardLmMappingList = custAcctCardQueryFacility.getCardLmMappingByLogicCardNbr(CcsCard.getLogicCardNbr());
			for (CcsCardLmMapping CcsCardLmMapping : CcsCardLmMappingList) {
				// 调用MPS服务获取介质卡片信息
				Map<String, Serializable> map = mmCardService.MS3102(CcsCardLmMapping.getCardNbr());

				S14000Card card = new S14000Card();
				card.setCard_no(DataTypeUtils.getStringValue(map.get(S14000Card.P_Cardno)));
				card.setCardholder_name(rCcsCustomer.findOne(qCcsCustomer.custId.eq(CcsCard.getCustId())).getName());
				card.setProduct_name(unifiedParameterFacilityProvide.product(DataTypeUtils.getStringValue(map.get(S14000Card.P_ProductCd))).description);
				card.setCard_expire_date(DataTypeUtils.getDateValue(map.get(S14000Card.P_ExpiryDate)));
				card.setBsc_supp_ind(CcsCard.getBscSuppInd());
				card.setActivate_ind(DataTypeUtils.getEnumValue(map.get(S14000Card.P_ActiveInd), Indicator.class));
				card.setActivate_date(DataTypeUtils.getDateValue(map.get(S14000Card.P_ActivateDate)));
				card.setQ_pin_exist_ind(DataTypeUtils.getEnumValue(map.get(S14000Card.Q_PIN_EXIST_IND), Indicator.class));
				card.setP_pin_exist_ind(DataTypeUtils.getEnumValue(map.get(S14000Card.P_PIN_EXIST_IND), Indicator.class));
				card.setNew_card_issue_ind(DataTypeUtils.getEnumValue(map.get(S14000Card.NEW_CARD_ISSUE_IND), Indicator.class));
				// 将逻辑卡片和介质卡片的封锁码合并
				card.setBlock_code(blockCodeUtils.unionBlockCodes(DataTypeUtils.getStringValue(map.get(S14000Card.P_BlockCd)), CcsCard.getBlockCode()));
				card.setQ_pin_exist_ind(mmCardService.MS3501(DataTypeUtils.getStringValue(map.get(S14000Card.P_Cardno))) == true ? Indicator.Y : Indicator.N);
				card.setP_pin_exist_ind(mmCardService.MS3502(DataTypeUtils.getStringValue(map.get(S14000Card.P_Cardno))) == true ? Indicator.Y : Indicator.N);

				String cardno = DataTypeUtils.getStringValue(map.get(S14000Card.P_Cardno));
				String firstCardno = DataTypeUtils.getStringValue(map.get(S14000Card.P_FirstCardno));
				Date lastExpiryDate = DataTypeUtils.getDateValue(map.get(S14000Card.P_LastExpiryDate));
				// 卡号=首卡卡号&&旧卡有效期为空，则新发卡
				if (cardno.equals(firstCardno) && lastExpiryDate == null) {
					card.setNew_card_issue_ind(Indicator.Y);
				} else {
					card.setNew_card_issue_ind(Indicator.N);
				}

				cards.add(card);
			}

		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsCardLmMapping).where(qCcsCardLmMapping.logicCardNbr.in(getQueryConditionsByLogicalCardNo(booleanExpression)))
				.count();

		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setId_type(req.getId_type());
		resp.setId_no(req.getId_no());
		resp.setCards(cards);

		LogTools.printLogger(logger, "S14000", "按证件号码查询卡片列表", resp, false);
		return resp;

	}

	/**
	 * @see 方法名：getQueryConditionsByLogicalCardNo
	 * @see 描述：获取根据传入查询条件查询出的所有逻辑卡号对应的卡号总数
	 * @see 创建日期：2015-6-25下午4:50:10
	 * @author ChengChun
	 * 
	 * @param booleanExpression
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public List<String> getQueryConditionsByLogicalCardNo(BooleanExpression booleanExpression) {
		JPAQuery query = new JPAQuery(em);
		return query.from(qCcsCustomer, qCcsCard).where(booleanExpression).list(qCcsCard.logicCardNbr);
	}

}
