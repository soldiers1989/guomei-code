/**
 * 
 */
package com.sunline.ccs.service.handler.sub;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14020Req;
import com.sunline.ccs.service.protocol.S14020Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DataTypeUtils;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.CntType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQCardInfo
 * @see 描述：卡片信息查询
 *
 * @see 创建日期： 2015-6-25下午5:02:04
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQCardInfo {
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

	@Transactional
	public S14020Resp handler(S14020Req req) throws ProcessException {

		LogTools.printLogger(logger, "S14020", "卡片信息查询", req, true);
		S14020Resp resp = new S14020Resp();

		// 校验卡号是否合法
		CheckUtil.checkCardNo(req.getCard_no());

		// 获取卡片信息
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsCardO CcsCardO = custAcctCardQueryFacility.getCardOByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 调用MPS服务获取介质卡片信息
		Map<String, Serializable> map = mmCardService.MS3102(req.getCard_no());

		// 构建响应报文对象
		resp.setCard_no(req.getCard_no());
		resp.setCardholder_name(rCcsCustomer.findOne(qCcsCustomer.custId.eq(CcsCard.getCustId())).getName());
		resp.setProduct_name(unifiedParameterFacilityProvide.product(CcsCard.getProductCd()).description);
		resp.setBsc_supp_ind(CcsCard.getBscSuppInd());
		// 主附取主卡卡号方式不同
		if (CcsCard.getBscSuppInd() == BscSuppIndicator.B) {
			resp.setBsc_card_no(CcsCard.getLastestMediumCardNbr());
		} else {
			// 附卡则获取主卡的卡号
			CcsCard card = custAcctCardQueryFacility.getCardByLogicCardNbr(CcsCard.getCardBasicNbr());
			resp.setBsc_card_no(card.getLastestMediumCardNbr());
		}

		resp.setOwning_branch(CcsCard.getOwningBranch());
		resp.setSetup_date(CcsCard.getSetupDate());

		resp.setActivate_ind(Indicator.valueOf(map.get(S14020Resp.P_ActivateInd).toString()));
		resp.setActivate_date(DataTypeUtils.getDateValue(map.get(S14020Resp.P_ActivateDate)));
		resp.setCancel_date(CcsCard.getCloseDate());
		resp.setPos_pin_verify_ind(CcsCard.getPosPinVerifyInd());
		resp.setRelationship(CcsCard.getRelationshipToBsc());
		resp.setCard_expire_date(CcsCard.getCardExpireDate());
		resp.setNext_card_fee_date(CcsCard.getNextCardFeeDate());
		resp.setRenew_ind(CcsCard.getRenewInd());
		resp.setRenew_reject_cd(CcsCard.getRenewRejectCd());
		resp.setQ_pin_exist_ind(mmCardService.MS3501(req.getCard_no()) == true ? Indicator.Y : Indicator.N);
		resp.setP_pin_exist_ind(mmCardService.MS3502(req.getCard_no()) == true ? Indicator.Y : Indicator.N);
		resp.setPin_tries(CcsCardO.getPinTries());
		resp.setInq_pin_tries(CcsCardO.getInqPinTries());
		resp.setLast_pin_tries_time(CcsCardO.getLastPinTriesTime());
		resp.setLast_inq_pin_tries_time(CcsCardO.getLastInqPinTriesTime());
		ProductCredit product = unifiedParameterFacilityProvide.productCredit(CcsCard.getProductCd());
		if (CcsCardO.getInqPinTries() >= product.maxInqPinTry) {
			resp.setInq_pin_limiterr_ind(Indicator.Y);
		} else {
			resp.setInq_pin_limiterr_ind(Indicator.N);
		}
		if (CcsCardO.getPinTries() >= product.pinTry) {
			resp.setPin_limiterr_ind(Indicator.Y);
		} else {
			resp.setPin_limiterr_ind(Indicator.N);
		}

		String cardno = DataTypeUtils.getStringValue(map.get(S14020Resp.P_Cardno));
		String firstCardno = DataTypeUtils.getStringValue(map.get(S14020Resp.P_FirstCardno));
		Date lastExpiryDate = DataTypeUtils.getDateValue(map.get(S14020Resp.P_LastExpiryDate));
		String blockCode = DataTypeUtils.getStringValue(map.get(S14020Resp.P_BlockCd));

		// 卡号=首卡卡号&&旧卡有效期为空，则新发卡
		if (cardno.equals(firstCardno) && lastExpiryDate == null) {
			resp.setNew_card_issue_ind(Indicator.Y);
		} else {
			resp.setNew_card_issue_ind(Indicator.N);
		}

		// 将逻辑卡片和介质卡片的封锁码合并
		resp.setBlock_code(blockCodeUtils.unionBlockCodes(blockCode, CcsCard.getBlockCode()));
		SimpleDateFormat sdf = new SimpleDateFormat("yyMM");
		Map<CntType, Integer> cntMap = mmCardService.MS3106(req.getCard_no(), sdf.format(CcsCard.getCardExpireDate()));
		resp.setCvv2_tries(cntMap.get(CntType.cvn2) == null ? 0 : cntMap.get(CntType.cvn2));
		if (cntMap.get(CntType.cvn2) == null) {
			resp.setCvv2_limiterr_ind(Indicator.N);
		} else {
			if (cntMap.get(CntType.cvn2) >= product.cvv2Try) {
				resp.setCvv2_limiterr_ind(Indicator.Y);
			} else {
				resp.setCvv2_limiterr_ind(Indicator.N);
			}
		}
		resp.setCvv_tries(cntMap.get(CntType.cvn) == null ? 0 : cntMap.get(CntType.cvn));
		if (cntMap.get(CntType.cvn) == null) {
			resp.setCvv_limiterr_ind(Indicator.N);
		} else {
			if (cntMap.get(CntType.cvn) >= product.cvvTry) {
				resp.setCvv_limiterr_ind(Indicator.Y);
			} else {
				resp.setCvv_limiterr_ind(Indicator.N);
			}
		}
		resp.setIcvv_tries(cntMap.get(CntType.icvn) == null ? 0 : cntMap.get(CntType.icvn));
		if (cntMap.get(CntType.icvn) == null) {
			resp.setIcvv_limiterr_ind(Indicator.N);
		} else {
			if (cntMap.get(CntType.icvn) >= product.icvvTry) {
				resp.setIcvv_limiterr_ind(Indicator.Y);
			} else {
				resp.setIcvv_limiterr_ind(Indicator.N);
			}
		}
		LogTools.printLogger(logger, "S14020", "卡片信息查询", resp, false);
		return resp;

	}

}
