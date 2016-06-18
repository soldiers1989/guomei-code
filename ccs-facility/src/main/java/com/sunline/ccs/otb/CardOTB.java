package com.sunline.ccs.otb;

import static com.sunline.ccs.facility.CaclUtils.*;
import static com.sunline.ccs.facility.AuthCommUtils.checkDateValid;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.acm.service.api.GlobalManagementService;

/**
 * 卡片级可用限额计算
 * 
* @author fanghj
 * 
 *         卡片级各可用限额的计算都是使用TM_CARD_O表中的字段
 */
@Component
public class CardOTB {

	/**
	 * 公共组件
	 */
	@Autowired
	private CommProvide commonProvide;

	@Autowired
	private GlobalManagementService globalManageService;
	
	/**
	 * 逻辑卡限额覆盖表
	 */
	@Autowired
	private RCcsCardThresholdCtrl rCcsCardThresholdCtrl;

	/**
	 * 查询ccsCardO信息
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardO findCardOByCardNbr(String cardNbr) throws ProcessException {
		CcsCardO cardO = commonProvide.findCardOByCardNbr(cardNbr);
		if (cardO == null)
			throw new ProcessException("卡号[" + cardNbr + "]查询不到对应的卡片信息");
		return cardO;
	}

	/**
	 * 卡片周期可用限额 CARD CTD OTB = 周期限额CYCLE_LIMT – 周期已用金额 CTD_USED_AMT
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardCtdOTB(String cardNbr) throws ProcessException {
		return this.cardCtdOTB(findCardOByCardNbr(cardNbr));
	}

	/**
	 * 卡片周期可用限额 CARD CTD OTB = 周期限额CYCLE_LIMT – 周期已用金额 CTD_USED_AMT
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardCtdOTB(CcsCardO ccsCardO) throws ProcessException {
		BigDecimal cardCtdOtbAmt = BigDecimal.ZERO;
		cardCtdOtbAmt = ccsCardO.getCycleRetailLmt().subtract(ccsCardO.getCtdUsedAmt());
//		return cardCtdOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : cardCtdOtbAmt;
		return checkPositive(cardCtdOtbAmt);
	}

	/**
	 * 卡片周期取现可用限额 CARD CTD CASH OTB = 周期取现限额CYCLE_CASH_LIMT
	 * –周期已用取现金额CTD_USED_CASH_AMT
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardCtdCashOTB(String cardNbr) throws ProcessException {
		return this.cardCtdCashOTB(findCardOByCardNbr(cardNbr));
	}

	/**
	 * 卡片周期取现可用限额 CARD CTD CASH OTB = 周期取现限额CYCLE_CASH_LIMT
	 * –周期已用取现金额CTD_USED_CASH_AMT
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardCtdCashOTB(CcsCardO cardO) throws ProcessException {
		BigDecimal cardCtdCashOtbAmt = BigDecimal.ZERO;
		cardCtdCashOtbAmt = cardO.getCycleCashLmt().subtract(cardO.getCtdCashAmt());
//		return cardCtdCashOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : cardCtdCashOtbAmt;
		return checkPositive(cardCtdCashOtbAmt);
	}

	/**
	 * 卡片周期网银可用限额 CARD CTD NET OTB = 周期网银限额CYCLE_NET_LIMT –
	 * 周期网银交易金额CTD_NET_RETL_AMT
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardCtdNetOTB(String cardNbr) throws ProcessException {
		return this.cardCtdNetOTB(findCardOByCardNbr(cardNbr));
	}

	/**
	 * 卡片周期网银可用限额 CARD CTD NET OTB = 周期网银限额CYCLE_NET_LIMT –
	 * 周期网银交易金额CTD_NET_RETL_AMT
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardCtdNetOTB(CcsCardO cardO) throws ProcessException {
		BigDecimal cardCtdNetOtbAmt = BigDecimal.ZERO;
		cardCtdNetOtbAmt = cardO.getCycleNetLmt().subtract(cardO.getCtdNetAmt());
//		return cardCtdNetOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : cardCtdNetOtbAmt;
		return checkPositive(cardCtdNetOtbAmt);
	}

	/**
	 * 卡片日消费可用限额 CARD DAY RETATL OTB = 日消费限额DAY_RETAIL_AMT_LIMIT –
	 * 日已用消费金额DAY_USED_RETAIL_AMT
	 * 
	 * @param 介质卡号 cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardDayRetailOTB(String cardNbr) throws ProcessException {
		return this.cardDayRetailOTB(findCardOByCardNbr(cardNbr));
	}

	/**
	 * 卡片日消费可用限额 CARD DAY RETATL OTB = 日消费限额DAY_RETAIL_AMT_LIMIT –
	 * 日已用消费金额DAY_USED_RETAIL_AMT
	 * 
	 * @param 介质卡号 cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardDayRetailOTB(CcsCardO cardO) throws ProcessException {
		Date bizDate = globalManageService.getSystemStatus().getBusinessDate();
		CcsCardThresholdCtrl cardLmtOvri = rCcsCardThresholdCtrl.findOne(cardO.getLogicCardNbr());
		
		BigDecimal dayRetailAmtLimit = BigDecimal.ZERO;
		if (cardLmtOvri != null 
				&& checkDateValid(cardLmtOvri.getDayRetlOvriInd(), bizDate, 
						cardLmtOvri.getDayRetlOvriBegDate(), cardLmtOvri.getDayRetlOvriEndDate())) {
			dayRetailAmtLimit = cardLmtOvri.getDayRetailAmtLmt();
		} else {
			ProductCredit productCr = commonProvide.retrieveProductCredit(cardO.getProductCd());
			dayRetailAmtLimit = productCr.dayRetailAmtLimit;
			if (dayRetailAmtLimit == null)
				throw new ProcessException("查询不到对应的逻辑卡[" + cardO.getLogicCardNbr() + "]限额");
		}
		BigDecimal cardDayRetailOtb = dayRetailAmtLimit.subtract(cardO.getDayUsedRetailAmt());
//		return cardDayRetailOtb.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : cardDayRetailOtb;
		return checkPositive(cardDayRetailOtb);
	}

	/**
	 * 卡片日取现可用限额 CARD DAY CASH OTB = 日取现限额DAY_CASH_AMT_LIMIT –
	 * 日已用取现金额DAY_USED_CASH_AMT
	 * 
	 * @param 介质卡号 cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardDayCashOTB(String cardNbr) throws ProcessException {
		return this.cardDayCashOTB(findCardOByCardNbr(cardNbr));
	}

	/**
	 * 卡片日取现可用限额 CARD DAY CASH OTB = 日取现限额DAY_CASH_AMT_LIMIT –
	 * 日已用取现金额DAY_USED_CASH_AMT
	 * 
	 * @param 介质卡号 cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardDayCashOTB(CcsCardO cardO) throws ProcessException {
		Date bizDate = globalManageService.getSystemStatus().getBusinessDate();
		CcsCardThresholdCtrl cardLmtOvri = rCcsCardThresholdCtrl
				.findOne(cardO.getLogicCardNbr());
		BigDecimal dayCashAmtLimit = BigDecimal.ZERO;
		if (cardLmtOvri != null 
				&& checkDateValid(cardLmtOvri.getDayCashOvriInd(), bizDate, 
						cardLmtOvri.getDayCashOvriBegDate(), cardLmtOvri.getDayCashOvriEndDate())) {
			dayCashAmtLimit = cardLmtOvri.getDayCashAmtLmt();
		} else {
			ProductCredit productCr = commonProvide.retrieveProductCredit(cardO.getProductCd());
			dayCashAmtLimit = productCr.dayCashAmtLimit;
			if (dayCashAmtLimit == null)
				throw new ProcessException("查询不到对应的逻辑卡[" + cardO.getLogicCardNbr() + "]限额");
		}
		BigDecimal cardDayCashOtb = dayCashAmtLimit.subtract(cardO.getDayUsedCashAmt());
//		return cardDayCashOtb.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : cardDayCashOtb;
		return checkPositive(cardDayCashOtb);
	}

	/**
	 * 卡片日ATM可用限额 CARD DAY ATM OTB = 日ATM取现限额DAY_ATM_LIMT –
	 * 日ATM已用取现金额DAY_USED_ATM_AMT
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardDayAtmOTB(String cardNbr) throws ProcessException {
		return this.cardDayAtmOTB(findCardOByCardNbr(cardNbr));
	}

	/**
	 * 卡片日ATM可用限额 CARD DAY ATM OTB = 日ATM取现限额DAY_ATM_LIMT –
	 * 日ATM已用取现金额DAY_USED_ATM_AMT
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardDayAtmOTB(CcsCardO cardO) throws ProcessException {
		Date bizDate = globalManageService.getSystemStatus().getBusinessDate();
		CcsCardThresholdCtrl cardLmtOvri = rCcsCardThresholdCtrl
				.findOne(cardO.getLogicCardNbr());
		BigDecimal dayAtmLimit = BigDecimal.ZERO;
		if (cardLmtOvri != null 
				&& checkDateValid(cardLmtOvri.getDayAtmOvriInd(), bizDate, 
						cardLmtOvri.getDayAtmOvriBegDate(), cardLmtOvri.getDayAtmOvriEndDate())) {
			dayAtmLimit = cardLmtOvri.getDayAtmAmtLmt();
		} else {
			ProductCredit productCr = commonProvide.retrieveProductCredit(cardO.getProductCd());
			dayAtmLimit = productCr.dayAtmLimit;
			if (dayAtmLimit == null)
				throw new ProcessException("查询不到对应的逻辑卡[" + cardO.getLogicCardNbr() + "]限额");
		}
		BigDecimal cardDayAtmOtbAmt = dayAtmLimit.subtract(cardO.getDayUsedAtmAmt());
//		return cardDayAtmOtbAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : cardDayAtmOtbAmt;
		return checkPositive(cardDayAtmOtbAmt);
	}

	/**
	 * 卡片日银联境外ATM取现限额 CARD DAY CUPXB ATM OTB = 单日银联境外ATM取现限额DAY_CUPXB_ATM_AMT_LIMIT –
	 * 当日ATM银联境外取现金额DAY_USED_ATM_CUPXB_AMT
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardDayAtmCupxbOTB(String cardNbr) throws ProcessException {
		return this.cardDayAtmCupxbOTB(findCardOByCardNbr(cardNbr));
	}

	/**
	 * 卡片日银联境外ATM取现限额 CARD DAY CUPXB ATM OTB = 单日银联境外ATM取现限额DAY_CUPXB_ATM_AMT_LIMIT –
	 * 当日ATM银联境外取现金额DAY_USED_ATM_CUPXB_AMT
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public BigDecimal cardDayAtmCupxbOTB(CcsCardO cardO) throws ProcessException {
		Date bizDate = globalManageService.getSystemStatus().getBusinessDate();
		CcsCardThresholdCtrl cardLmtOvri = rCcsCardThresholdCtrl
				.findOne(cardO.getLogicCardNbr());
		BigDecimal dayCupxbAtmLimit = BigDecimal.ZERO;
		if (cardLmtOvri != null 
				&& checkDateValid(cardLmtOvri.getDayCupxbAtmOvriInd(), bizDate, 
						cardLmtOvri.getDayCupxbAtmOvriBegDate(), cardLmtOvri.getDayCupxbAtmOvriEndDate())) {
			dayCupxbAtmLimit = cardLmtOvri.getDayCupxbAtmAmtLmt();
		} else {
			ProductCredit productCr = commonProvide.retrieveProductCredit(cardO.getProductCd());
			dayCupxbAtmLimit = productCr.dayCupxbAtmLimit;
			if (dayCupxbAtmLimit == null)
				throw new ProcessException("查询不到对应的逻辑卡[" + cardO.getLogicCardNbr() + "]限额");
		}
		BigDecimal cardDayAtmCupxbOtb = dayCupxbAtmLimit.subtract(cardO.getDayUsedAtmCupxbAmt());
//		return cardDayAtmCupxbOtb.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : cardDayAtmCupxbOtb;
		return checkPositive(cardDayAtmCupxbOtb);
	}

	/**
	 * 卡片日ATM可用次数 CARD DAY ATM OPEN NBR = 日ATM取现限次DAY_ATM_NBR–日ATM已用取现次数
	 * DAY_USED_ATM_NBR
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public Integer cardDayAtmOpenNBR(String cardNbr) throws ProcessException {
		return this.cardDayAtmOpenNBR(findCardOByCardNbr(cardNbr));
	}

	/**
	 * 卡片日ATM可用次数 CARD DAY ATM OPEN NBR = 日ATM取现限次DAY_ATM_NBR–日ATM已用取现次数
	 * DAY_USED_ATM_NBR
	 * 
	 * @param 介质卡号
	 *            cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public Integer cardDayAtmOpenNBR(CcsCardO cardO) throws ProcessException {
		Date bizDate = globalManageService.getSystemStatus().getBusinessDate();
		CcsCardThresholdCtrl cardLmtOvri = rCcsCardThresholdCtrl
				.findOne(cardO.getLogicCardNbr());
		Integer dayAtmNbr = 0;
		if (cardLmtOvri != null 
				&& checkDateValid(cardLmtOvri.getDayAtmOvriInd(), bizDate, 
						cardLmtOvri.getDayAtmOvriBegDate(), cardLmtOvri.getDayAtmOvriEndDate())) {
			dayAtmNbr = cardLmtOvri.getDayAtmNbrLmt();
		} else {
			ProductCredit productCr = commonProvide.retrieveProductCredit(cardO.getProductCd());
			dayAtmNbr = productCr.dayAtmNbr;
			if (dayAtmNbr == null)
				throw new ProcessException("查询不到对应的逻辑卡[" + cardO.getLogicCardNbr() + "]限额");
		}
		Integer cardDayAtmOpenNbr = dayAtmNbr - cardO.getDayUsedAtmNbr();
		return checkPositive(cardDayAtmOpenNbr);
	}
}
