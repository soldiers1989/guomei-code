package com.sunline.ccs.service.handler.sub;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.FirstCardFeeInd;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14040Req;
import com.sunline.ccs.service.protocol.S14040Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.api.MediumService;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.CntType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMCardActive
 * @see 描述： 卡片激活
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMCardActive {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
	@Autowired
	private Common common;
	
	@Resource(name = "mediumService")
	private MediumService mediumService;
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	@Autowired
	private RCcsCustomer rCcsCustomer;

	/**
	 * @see 方法名：handler
	 * @see 描述：卡片激活handler
	 * @see 创建日期：2015年6月25日下午5:45:47
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Transactional
	public S14040Resp handler(S14040Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14040", "卡片激活", req, true);
		DateFormat df = new SimpleDateFormat("yyMM");
		S14040Resp resp = new S14040Resp();
		CheckUtil.checkCardNo(req.getCard_no());
		if (req.getExpire_date() == null) {
			throw new ProcessException(Constants.ERRB100_CODE, Constants.ERRB100_MES);
		}
		if (req.getCvv2() == null) {
			throw new ProcessException(Constants.ERRB104_CODE, Constants.ERRB104_MES);
		}
		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(req.getCard_no());
		// 验证卡片有效期是否正确
		if (!df.format(CcsCard.getCardExpireDate()).equals(req.getExpire_date())) {
			throw new ProcessException(Constants.ERRB064_CODE, Constants.ERRB064_MES);
		}
		if (req.getExpire_date() != null && !mediumService.isValidExpiryDate(req.getCard_no(), req.getExpire_date())) {
			throw new ProcessException(Constants.ERRB064_CODE, Constants.ERRB064_MES);
		}
		if (req.getCvv2() != null && !mediumService.isValidCvv2(req.getCard_no(), req.getExpire_date(), req.getCvv2())) {
			try {
				mmCardService.setErrCnt(CntType.cvn2, req.getCard_no(), df.parse(req.getExpire_date()));
				if (CcsCardO != null) {
					ProductCredit product = unifiedParaFacilityProvide.productCredit(CcsCardO.getProductCd());
					common.cvv2IsOver(req.getCard_no(), req.getExpire_date(), product);
				}
			} catch (ParseException e) {
				// 理论上不会出现异常
			}
			throw new ProcessException(Constants.ERRB065_CODE, Constants.ERRB065_MES);
		} else if (req.getCvv2() != null && mediumService.isValidCvv2(req.getCard_no(), req.getExpire_date(), req.getCvv2())) {
			if (CcsCardO != null) {
				ProductCredit product = unifiedParaFacilityProvide.productCredit(CcsCardO.getProductCd());
				common.cvv2IsNotOverSetZero(req.getCard_no(), req.getExpire_date(), product);
			}
		}
		// 在激活之前，调用MPS的卡片查询，看是否已经激活
		Map<String, Serializable> map = mmCardService.MS3102(req.getCard_no());
		Indicator activateInd = Indicator.valueOf(map.get(S14040Resp.P_ActivateInd).toString());
		if (activateInd.equals(Indicator.Y)) {
			throw new ProcessException(Constants.ERRB080_CODE, Constants.ERRB080_MES);
		}
		// 调用mps激活服务
		mmCardService.MS3201(req.getCard_no(), false);

		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		if (CcsCard.getActiveInd() == Indicator.N && CcsCardO.getActiveInd() == Indicator.N) {
			CcsCard.setActiveInd(Indicator.Y);
			// 激活日期
			CcsCard.setActiveDate(unifiedParaFacilityProvide.BusinessDate());
			// 杂项费中的首次年费收取方式，如果是“激活收年费”才需要更新下次年费日期；否则不需要更新
			if (unifiedParaFacilityProvide.productCredit(CcsCard.getProductCd()).fee.firstCardFeeInd == FirstCardFeeInd.A) {
				CcsCard.setNextCardFeeDate(unifiedParaFacilityProvide.BusinessDate());
			}
			CcsCardO.setActiveInd(Indicator.Y);

			// messageService.sendMessage(MessageCategory.M17,
			// CcsCard.getProductCd(), req.getCard_no(), ccsCustomer.getName(),
			// ccsCustomer.getGender(), ccsCustomer.getMobileNo(),
			// ccsCustomer.getEmail(),
			// new Date(), new MapBuilder().add("posPinVerifyInd",
			// CcsCard.getPosPinVerifyInd()).build());
		}
		// else {
		// throw new ProcessException(Constants.ERRB080_CODE,
		// Constants.ERRB080_MES);
		// }

		// 激活成功短信
		CcsCustomer ccsCustomer = rCcsCustomer.findOne(CcsCard.getCustId());
		String msgCd = fetchMsgCdService.fetchMsgCd(CcsCard.getProductCd(), CPSMessageCategory.CPS017);
/*		downMsgFacility.sendMessage(msgCd, req.getCard_no(), ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(),
				new MapBuilder().add("posPinVerifyInd", CcsCard.getPosPinVerifyInd()).build());
*/
		resp.setCard_no(req.getCard_no());
		resp.setNew_card_issue_ind(mmCardService.MS3503(req.getCard_no()) ? Indicator.Y : Indicator.N);
		resp.setP_pin_exist_ind(mmCardService.MS3502(req.getCard_no()) ? Indicator.Y : Indicator.N);
		resp.setQ_pin_exist_ind(mmCardService.MS3501(req.getCard_no()) ? Indicator.Y : Indicator.N);

		resp.setPos_pin_verify_ind(CcsCard.getPosPinVerifyInd());
		LogTools.printLogger(logger, "S14040", "卡片激活", resp, false);
		return resp;
	}
}
