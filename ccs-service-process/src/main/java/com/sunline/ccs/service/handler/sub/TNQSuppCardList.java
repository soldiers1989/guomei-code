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
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14000Card;
import com.sunline.ccs.service.protocol.S14001Card;
import com.sunline.ccs.service.protocol.S14001Req;
import com.sunline.ccs.service.protocol.S14001Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DataTypeUtils;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQSuppCardList
 * @see 描述：按主卡卡号查询附卡列表(含该主卡)
 *
 * @see 创建日期： 2015-6-25下午4:51:40
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQSuppCardList {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private RCcsCustomer rCcsCustomer;
	@PersistenceContext
	public EntityManager em;
	QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
	QCcsCard qCcsCard = QCcsCard.ccsCard;

	@Transactional
	public S14001Resp handler(S14001Req req) throws ProcessException {

		LogTools.printLogger(logger, "S14001", "按主卡卡号查询附卡列表", req, true);
		// 构建响应报文对象
		S14001Resp resp = new S14001Resp();
		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		// 获取卡片信息
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 若附卡直接返回
		if (BscSuppIndicator.S.equals(CcsCard.getBscSuppInd())) {
			throw new ProcessException(Constants.ERRB069_CODE, Constants.ERRB069_MES);
		}

		ArrayList<S14001Card> cards = new ArrayList<S14001Card>();

		// // 该主卡也放入返回列表中
		// S14001Card respBase = new S14001Card();
		// respBase.setSupp_card_no(req.getCard_no());
		// respBase.setProduct_name(unifiedParameterFacilityProvide.product(CcsCard.getProductCd()).description);
		// respBase.setCardholder_name(rCcsCustomer.findOne(qCcsCustomer.custId.eq(CcsCard.getCustId())).getName());
		// respBase.setBsc_supp_ind(CcsCard.getBscSuppInd());
		// respBase.setCard_expire_date(CcsCard.getCardExpireDate());
		// respBase.setActive_ind(CcsCard.getActiveInd());
		// respBase.setActive_date(CcsCard.getActiveDate());
		// respBase.setBlock_code(CcsCard.getBlockCode());
		//
		// respSupp.setQ_pin_exist_ind(DataTypeUtils.getEnumValue(map.get(S14000Card.Q_PIN_EXIST_IND),Indicator.class));
		// respSupp.setP_pin_exist_ind(DataTypeUtils.getEnumValue(map.get(S14000Card.P_PIN_EXIST_IND),Indicator.class));
		// respSupp.setNew_card_issue_ind(DataTypeUtils.getEnumValue(
		// map.get(S14000Card.NEW_CARD_ISSUE_IND),Indicator.class));
		//
		// cards.add(respBase);

		// 查询附卡列表
		JPAQuery query = new JPAQuery(em);
		// 此处还是应该把主卡给去掉，因为单用逻辑主卡查询主卡也在内
		BooleanExpression booleanExpression = qCcsCard.logicCardNbr.eq(CcsCard.getLogicCardNbr()).and(qCcsCard.bscSuppInd.eq(BscSuppIndicator.S));

		List<CcsCard> CcsCardList = query.from(qCcsCard).where(booleanExpression).orderBy(qCcsCard.logicCardNbr.asc()).offset(req.getFirstrow())
				.limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsCard);

		// List<CcsCard> CcsCardList =
		// custAcctCardQueryFacility.getCcsCardListByCardNo(req.getCard_no());

		for (CcsCard CcsCardTemp : CcsCardList) {
			// 通过逻辑卡查询对应介质卡列表
			List<CcsCardLmMapping> CcsCardLmMappingList = custAcctCardQueryFacility.getCardLmMappingByLogicCardNbr(CcsCardTemp.getLogicCardNbr());
			for (CcsCardLmMapping CcsCardLmMapping : CcsCardLmMappingList) {
				// 调用MPS服务获取介质卡片信息
				Map<String, Serializable> map = mmCardService.MS3102(CcsCardLmMapping.getCardNbr());

				S14001Card respSupp = new S14001Card();
				respSupp.setSupp_card_no(DataTypeUtils.getStringValue(map.get(S14001Card.P_Cardno)));
				respSupp.setCardholder_name(rCcsCustomer.findOne(qCcsCustomer.custId.eq(CcsCardTemp.getCustId())).getName());
				respSupp.setProduct_name(unifiedParameterFacilityProvide.product(DataTypeUtils.getStringValue(map.get(S14001Card.P_ProductCd))).description);
				respSupp.setCard_expire_date(DataTypeUtils.getDateValue(map.get(S14001Card.P_ExpiryDate)));
				respSupp.setBsc_supp_ind(CcsCardTemp.getBscSuppInd());
				respSupp.setActivate_ind(DataTypeUtils.getEnumValue(map.get(S14001Card.P_ActiveInd), Indicator.class));
				respSupp.setActivate_date(DataTypeUtils.getDateValue(map.get(S14001Card.P_ActivateDate)));
				respSupp.setQ_pin_exist_ind(DataTypeUtils.getEnumValue(map.get(S14000Card.Q_PIN_EXIST_IND), Indicator.class));
				respSupp.setP_pin_exist_ind(DataTypeUtils.getEnumValue(map.get(S14000Card.P_PIN_EXIST_IND), Indicator.class));
				respSupp.setNew_card_issue_ind(DataTypeUtils.getEnumValue(map.get(S14000Card.NEW_CARD_ISSUE_IND), Indicator.class));
				// 将逻辑卡片和介质卡片的封锁码合并
				respSupp.setBlock_code(blockCodeUtils.unionBlockCodes(DataTypeUtils.getStringValue(map.get(S14001Card.P_BlockCd)), CcsCardTemp.getBlockCode()));

				respSupp.setQ_pin_exist_ind(mmCardService.MS3501(DataTypeUtils.getStringValue(map.get(S14001Card.P_Cardno))) == true ? Indicator.Y
						: Indicator.N);
				respSupp.setP_pin_exist_ind(mmCardService.MS3502(DataTypeUtils.getStringValue(map.get(S14001Card.P_Cardno))) == true ? Indicator.Y
						: Indicator.N);

				String cardno = DataTypeUtils.getStringValue(map.get(S14001Card.P_Cardno));
				String firstCardno = DataTypeUtils.getStringValue(map.get(S14001Card.P_FirstCardno));
				Date lastExpiryDate = DataTypeUtils.getDateValue(map.get(S14001Card.P_LastExpiryDate));
				// 卡号=首卡卡号&&旧卡有效期为空，则新发卡
				if (cardno.equals(firstCardno) && lastExpiryDate == null) {
					respSupp.setNew_card_issue_ind(Indicator.Y);
				} else {
					respSupp.setNew_card_issue_ind(Indicator.N);
				}
				cards.add(respSupp);
			}
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsCard).where(booleanExpression).count();

		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setCard_no(req.getCard_no());
		resp.setCards(cards);

		LogTools.printLogger(logger, "S14001", "按主卡卡号查询附卡列表", resp, false);
		return resp;

	}

}
