package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
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
import com.sunline.ccs.infrastructure.server.repos.RCcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S15030Req;
import com.sunline.ccs.service.protocol.S15030Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.CurrencyCodeTools;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

@Service
public class TNMCardLimit {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;

	@Autowired
	private RCcsCustomer rCcsCustomer;
	@Autowired
	private RCcsCardThresholdCtrl rCcsCardThresholdCtrl;
	@PersistenceContext
	private EntityManager em;

	@Transactional
	public S15030Resp handler(S15030Req req) throws ProcessException {
		LogTools.printLogger(logger, "S15030", "卡片限额设定", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 获得卡片信息
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 获取参数
		AccountAttribute acctAttr;
		AccountAttribute dualAcctAttr;
		acctAttr = unifiedParaFacilityProvide.acct_attribute(CcsCardO.getProductCd());
		dualAcctAttr = unifiedParaFacilityProvide.dual_acct_attribute(CcsCardO.getProductCd());

		// 获取逻辑卡限额覆盖表
		CcsCardThresholdCtrl qCcsCardThresholdCtrl = rCcsCardThresholdCtrl.findOne(CcsCardO.getLogicCardNbr());
		// 业务日期
		Date bussinessDate = unifiedParaFacilityProvide.BusinessDate();

		ProductCredit productCredit = unifiedParaFacilityProvide.productCredit(CcsCardO.getProductCd());
		// 管理类设置卡片信息并返回对应的信息
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ONE)) {

			// 重构一下
			// // 判断限额不能为负数
			// if (req.getTxn_limit() != null &&
			// req.getTxn_limit().compareTo(BigDecimal.ZERO) < 0)
			// throw new ProcessException(Constants.ERRB086_CODE,
			// Constants.ERRB086_MES);
			// if (req.getTxn_cash_limit() != null &&
			// req.getTxn_cash_limit().compareTo(BigDecimal.ZERO) < 0)
			// throw new ProcessException(Constants.ERRB086_CODE,
			// Constants.ERRB086_MES);
			// if (req.getTxn_net_limit() != null &&
			// req.getTxn_net_limit().compareTo(BigDecimal.ZERO) < 0)
			// throw new ProcessException(Constants.ERRB086_CODE,
			// Constants.ERRB086_MES);
			// if (req.getCycle_limit() != null &&
			// req.getCycle_limit().compareTo(BigDecimal.ZERO) < 0)
			// throw new ProcessException(Constants.ERRB086_CODE,
			// Constants.ERRB086_MES);
			// if (req.getCycle_cash_limit() != null &&
			// req.getCycle_cash_limit().compareTo(BigDecimal.ZERO) < 0)
			// throw new ProcessException(Constants.ERRB086_CODE,
			// Constants.ERRB086_MES);
			// if (req.getCycle_net_limit() != null &&
			// req.getCycle_net_limit().compareTo(BigDecimal.ZERO) < 0)
			// throw new ProcessException(Constants.ERRB086_CODE,
			// Constants.ERRB086_MES);
			//
			// if (req.getDay_used_atm_amt() != null &&
			// req.getDay_used_atm_amt().compareTo(BigDecimal.ZERO) < 0)
			// throw new ProcessException(Constants.ERRB086_CODE,
			// Constants.ERRB086_MES);
			// if (req.getDay_used_atm_nbr() != null &&
			// req.getDay_used_atm_nbr().compareTo(0) < 0)
			// throw new ProcessException(Constants.ERRB107_CODE,
			// Constants.ERRB107_MES);
			//
			//
			// if(req.getDay_used_cash_amt() != null &&
			// req.getDay_used_atm_amt().compareTo(BigDecimal.ZERO) < 0)
			// throw new ProcessException(Constants.ERRB086_CODE,
			// Constants.ERRB086_MES);
			// if(req.getDay_used_cash_nbr() != null &&
			// req.getDay_used_cash_nbr().compareTo(0) < 0)
			// throw new ProcessException(Constants.ERRB107_CODE,
			// Constants.ERRB107_MES);
			//
			// if(req.getDay_used_retail_amt() != null &&
			// req.getDay_used_retail_amt().compareTo(BigDecimal.ZERO) < 0)
			// throw new ProcessException(Constants.ERRB086_CODE,
			// Constants.ERRB086_MES);
			// if(req.getDay_used_retail_nbr() != null &&
			// req.getDay_used_retail_nbr().compareTo(0) < 0)
			// throw new ProcessException(Constants.ERRB107_CODE,
			// Constants.ERRB107_MES);
			//
			// if(req.getDay_used_xfrout_amt() != null &&
			// req.getDay_used_xfrout_amt().compareTo(BigDecimal.ZERO) < 0)
			// throw new ProcessException(Constants.ERRB086_CODE,
			// Constants.ERRB086_MES);
			// if(req.getDay_used_xfrout_nbr() != null &&
			// req.getDay_used_xfrout_nbr().compareTo(0) < 0)
			// throw new ProcessException(Constants.ERRB107_CODE,
			// Constants.ERRB107_MES);
			//
			// if (req.getTxn_limit() != null &&
			// req.getTxn_limit().compareTo(BigDecimal.ZERO) >= 0)
			// CcsCardO.setTxnLmt(req.getTxn_limit());
			// if (req.getTxn_cash_limit() != null &&
			// req.getTxn_cash_limit().compareTo(BigDecimal.ZERO) >= 0)
			// CcsCardO.setTxnCashLmt(req.getTxn_cash_limit());
			// if (req.getTxn_net_limit() != null &&
			// req.getTxn_net_limit().compareTo(BigDecimal.ZERO) >= 0)
			// CcsCardO.setTxnNetLmt(req.getTxn_net_limit());
			// if (req.getCycle_limit() != null &&
			// req.getCycle_limit().compareTo(BigDecimal.ZERO) >= 0)
			// CcsCardO.setCycleLmt(req.getCycle_limit());
			// if (req.getCycle_cash_limit() != null &&
			// req.getCycle_cash_limit().compareTo(BigDecimal.ZERO) >= 0)
			// CcsCardO.setCycleCashLmt(req.getCycle_cash_limit());
			// if (req.getCycle_net_limit() != null &&
			// req.getCycle_net_limit().compareTo(BigDecimal.ZERO) >= 0)
			// CcsCardO.setCycleNetLmt(req.getCycle_net_limit());
			// 消费周期限额
			if (CheckUtil.validateData(req.getCycle_limit()))
				CcsCardO.setCycleRetailLmt(req.getCycle_limit());
			// 取现周期限额
			if (CheckUtil.validateData(req.getCycle_cash_limit()))
				CcsCardO.setCycleCashLmt(req.getCycle_cash_limit());
			// 网上消费周期限额
			if (CheckUtil.validateData(req.getCycle_net_limit()))
				CcsCardO.setCycleNetLmt(req.getCycle_net_limit());
			// 消费单笔限额
			if (CheckUtil.validateData(req.getTxn_limit()))
				CcsCardO.setTxnLmt(req.getTxn_limit());
			// 取现单笔限额
			if (CheckUtil.validateData(req.getTxn_cash_limit()))
				CcsCardO.setTxnCashLmt(req.getTxn_cash_limit());
			// 网上消费单笔限额
			if (CheckUtil.validateData(req.getTxn_net_limit()))
				CcsCardO.setTxnNetLmt(req.getTxn_net_limit());
			// 当天ATM取现金额
			if (CheckUtil.validateData(req.getDay_used_atm_amt()))
				CcsCardO.setDayUsedAtmAmt(req.getDay_used_atm_amt());
			// 当天ATM取现交易笔数
			if (CheckUtil.validateData(req.getDay_used_atm_nbr()))
				CcsCardO.setDayUsedAtmNbr(req.getDay_used_atm_nbr());
			// 当天消费金额
			if (CheckUtil.validateData(req.getDay_used_retail_amt()))
				CcsCardO.setDayUsedRetailAmt(req.getDay_used_retail_amt());
			// 当天消费笔数
			if (CheckUtil.validateData(req.getDay_used_retail_nbr()))
				CcsCardO.setDayUsedRetailNbr(req.getDay_used_retail_nbr());
			// 当天取现金额
			if (CheckUtil.validateData(req.getDay_used_cash_amt()))
				CcsCardO.setDayUsedCashAmt(req.getDay_used_cash_amt());
			// 当天取现笔数
			if (CheckUtil.validateData(req.getDay_used_cash_nbr()))
				CcsCardO.setDayUsedCashNbr(req.getDay_used_cash_nbr());
			// 当天转出金额
			if (CheckUtil.validateData(req.getDay_used_xfrout_amt()))
				CcsCardO.setDayUsedXfroutAmt(req.getDay_used_xfrout_amt());
			// 当天转出笔数
			if (CheckUtil.validateData(req.getDay_used_xfrout_nbr()))
				CcsCardO.setDayUsedXfroutNbr(req.getDay_used_xfrout_nbr());
			// 当日ATM银联境外取现金额
			if (CheckUtil.validateData(req.getDay_used_atm_cupxb_amt()))
				CcsCardO.setDayUsedAtmCupxbAmt(req.getDay_used_atm_cupxb_amt());
			// 不存在则新建
			if (qCcsCardThresholdCtrl == null) {
				qCcsCardThresholdCtrl = new CcsCardThresholdCtrl();
				qCcsCardThresholdCtrl.setLogicCardNbr(CcsCardO.getLogicCardNbr());
			}
			// 单日ATM取现笔数和限额
			if (CheckUtil.compareDate(req.getDay_atm_override_start(), bussinessDate, req.getDay_atm_override_end())) {
				if (CheckUtil.validateData(req.getDay_atm_amt_limit()))
					qCcsCardThresholdCtrl.setDayAtmAmtLmt(req.getDay_atm_amt_limit());
				if (CheckUtil.validateData(req.getDay_atm_nbr_limit()))
					qCcsCardThresholdCtrl.setDayAtmNbrLmt(req.getDay_atm_nbr_limit());
				qCcsCardThresholdCtrl.setDayAtmOvriInd(Indicator.Y);

			} else {
				qCcsCardThresholdCtrl.setDayAtmOvriInd(Indicator.N);
				qCcsCardThresholdCtrl.setDayAtmAmtLmt(req.getDay_atm_amt_limit());
				qCcsCardThresholdCtrl.setDayAtmNbrLmt(req.getDay_atm_nbr_limit());
			}
			qCcsCardThresholdCtrl.setDayAtmOvriBegDate(req.getDay_atm_override_start());
			qCcsCardThresholdCtrl.setDayAtmOvriEndDate(req.getDay_atm_override_end());
			// 单日消费限笔和限额
			if (CheckUtil.compareDate(req.getDay_retail_override_start(), bussinessDate, req.getDay_retail_override_end())) {
				if (CheckUtil.validateData(req.getDay_retail_amt_limit()))
					qCcsCardThresholdCtrl.setDayRetailAmtLmt(req.getDay_retail_amt_limit());
				if (CheckUtil.validateData(req.getDay_retail_nbr_limit()))
					qCcsCardThresholdCtrl.setDayRetailNbrLmt(req.getDay_retail_nbr_limit());

				qCcsCardThresholdCtrl.setDayRetlOvriInd(Indicator.Y);
			} else {
				qCcsCardThresholdCtrl.setDayRetailAmtLmt(req.getDay_retail_amt_limit());
				qCcsCardThresholdCtrl.setDayRetailNbrLmt(req.getDay_retail_nbr_limit());
				qCcsCardThresholdCtrl.setDayRetlOvriInd(Indicator.N);
			}
			qCcsCardThresholdCtrl.setDayRetlOvriBegDate(req.getDay_retail_override_start());
			qCcsCardThresholdCtrl.setDayRetlOvriEndDate(req.getDay_retail_override_end());

			// 单日取现限笔和限额
			if (CheckUtil.compareDate(req.getDay_cash_override_end(), bussinessDate, req.getDay_cash_override_end())) {
				if (CheckUtil.validateData(req.getDay_cash_amt_limit()))
					qCcsCardThresholdCtrl.setDayCashAmtLmt(req.getDay_cash_amt_limit());
				if (CheckUtil.validateData(req.getDay_cash_nbr_limit()))
					qCcsCardThresholdCtrl.setDayCashNbrLmt(req.getDay_cash_nbr_limit());
				qCcsCardThresholdCtrl.setDayCashOvriInd(Indicator.Y);
			} else {
				qCcsCardThresholdCtrl.setDayCashAmtLmt(req.getDay_cash_amt_limit());
				qCcsCardThresholdCtrl.setDayCashNbrLmt(req.getDay_cash_nbr_limit());
				qCcsCardThresholdCtrl.setDayCashOvriInd(Indicator.N);
			}
			qCcsCardThresholdCtrl.setDayCashOvriBegDate(req.getDay_cash_override_start());
			qCcsCardThresholdCtrl.setDayCashOvriEndDate(req.getDay_cash_override_end());

			// 单日转出限笔和限额
			if (CheckUtil.compareDate(req.getDay_xfrout_override_start(), bussinessDate, req.getDay_xfrout_override_end())) {
				if (CheckUtil.validateData(req.getDay_xfrout_amt_limit()))
					qCcsCardThresholdCtrl.setDayXfroutAmtLmt(req.getDay_xfrout_amt_limit());
				if (CheckUtil.validateData(req.getDay_xfrout_nbr_limit()))
					qCcsCardThresholdCtrl.setDayXfroutNbrLmt(req.getDay_xfrout_nbr_limit());
				qCcsCardThresholdCtrl.setDayXfroutOvriInd(Indicator.Y);
			} else {
				qCcsCardThresholdCtrl.setDayXfroutAmtLmt(req.getDay_xfrout_amt_limit());
				qCcsCardThresholdCtrl.setDayXfroutNbrLmt(req.getDay_xfrout_nbr_limit());
				qCcsCardThresholdCtrl.setDayXfroutOvriInd(Indicator.N);
			}
			qCcsCardThresholdCtrl.setDayXfroutOvriBegDate(req.getDay_xfrout_override_start());
			qCcsCardThresholdCtrl.setDayXfroutOvriEndDate(req.getDay_xfrout_override_end());

			// 单日银联境外ATM限额和限笔
			if (CheckUtil.validateData(req.getDay_cupxb_atm_amt_limit())) {
				qCcsCardThresholdCtrl.setDayCupxbAtmAmtLmt(BigDecimal.valueOf(req.getDay_cupxb_atm_amt_limit()));

				if (CheckUtil.compareDate(req.getDay_cupxb_atm_override_start(), bussinessDate, req.getDay_cupxb_atm_override_end())) {
					qCcsCardThresholdCtrl.setDayCupxbAtmOvriInd(Indicator.Y);
				} else {
					qCcsCardThresholdCtrl.setDayCupxbAtmOvriInd(Indicator.N);
				}
			} else {
				qCcsCardThresholdCtrl.setDayCupxbAtmOvriInd(Indicator.N);
			}
			qCcsCardThresholdCtrl.setDayCupxbAtmOvriBegDate(req.getDay_cupxb_atm_override_start());
			qCcsCardThresholdCtrl.setDayCupxbAtmOvriEndDate(req.getDay_cupxb_atm_override_end());

			// 如果是新建的则是不受容器管理的实例,保存插入
			if (!em.contains(qCcsCardThresholdCtrl)) {
				em.persist(qCcsCardThresholdCtrl);
			}

			CcsCustomer ccsCustomer = rCcsCustomer.findOne(CcsCardO.getCustId());
			// messageService.sendMessage(MessageCategory.L04,
			// CcsCardO.getProductCd(), req.getCard_no(), ccsCustomer.getName(),
			// ccsCustomer.getGender(), ccsCustomer.getMobileNo(),
			// ccsCustomer.getEmail(),
			// new Date(), new MapBuilder<String,
			// Object>().add("localCycleLmt",
			// CcsCardO.getCycleRetailLmt()).add("dualCycleLmt", null).build());

			String msgCd = fetchMsgCdService.fetchMsgCd(CcsCardO.getProductCd(), CPSMessageCategory.CPS031);
//			downMsgFacility.sendMessage(msgCd, req.getCard_no(), ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(), new MapBuilder<String, Object>().add("localCycleLmt", CcsCardO.getCycleRetailLmt()).add("dualCycleLmt", null).build());
		}

		// 返回信息
		S15030Resp resp = new S15030Resp();
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setCycle_cash_limit(CcsCardO.getCycleCashLmt());
		resp.setCycle_limit(CcsCardO.getCycleRetailLmt());
		resp.setCycle_net_limit(CcsCardO.getCycleNetLmt());
		resp.setTxn_cash_limit(CcsCardO.getTxnCashLmt());
		resp.setTxn_limit(CcsCardO.getTxnLmt());
		resp.setTxn_net_limit(CcsCardO.getTxnNetLmt());
		resp.setDay_used_atm_amt(CcsCardO.getDayUsedAtmAmt());
		resp.setDay_used_atm_nbr(CcsCardO.getDayUsedAtmNbr());
		resp.setDay_used_cash_amt(CcsCardO.getDayUsedCashAmt());
		resp.setDay_used_cash_nbr(CcsCardO.getDayUsedCashNbr());
		resp.setDay_used_retail_amt(CcsCardO.getDayUsedRetailAmt());
		resp.setDay_used_retail_nbr(CcsCardO.getDayUsedRetailNbr());
		resp.setDay_used_xfrout_amt(CcsCardO.getDayUsedXfroutAmt());
		resp.setDay_used_xfrout_nbr(CcsCardO.getDayUsedXfroutNbr());
		resp.setDay_used_atm_cupxb_amt(CcsCardO.getDayUsedAtmCupxbAmt());
		resp.setDual_curr_ind(CurrencyCodeTools.isExistOtherCurrCd(req.getCurr_cd(), acctAttr, dualAcctAttr));
		resp.setDual_curr_cd(CurrencyCodeTools.getOtherCurrCd(req.getCurr_cd(), acctAttr, dualAcctAttr));
		resp.setCtd_cash_amt(CcsCardO.getCtdCashAmt());
		resp.setCtd_used_amt(CcsCardO.getCtdUsedAmt());
		resp.setCtd_net_retl_amt(CcsCardO.getCtdNetAmt());

		if (qCcsCardThresholdCtrl != null) {
			// 单日ATM取现笔数和限额
			if (qCcsCardThresholdCtrl.getDayAtmOvriBegDate() != null || qCcsCardThresholdCtrl.getDayAtmOvriEndDate() != null) {
				if (qCcsCardThresholdCtrl.getDayAtmAmtLmt() == null) {
					throw new ProcessException(Constants.ERRB086_CODE, Constants.ERRB086_MES);
				}
				if (qCcsCardThresholdCtrl.getDayAtmNbrLmt() == null) {
					throw new ProcessException(Constants.ERRB107_CODE, Constants.ERRB107_MES);
				}
				resp.setDay_atm_override_start(qCcsCardThresholdCtrl.getDayAtmOvriBegDate());
				resp.setDay_atm_override_end(qCcsCardThresholdCtrl.getDayAtmOvriEndDate());
			}
			if (qCcsCardThresholdCtrl.getDayAtmAmtLmt() == null) {
				resp.setDay_atm_amt_limit(productCredit.dayAtmLimit);
			} else {
				resp.setDay_atm_amt_limit(qCcsCardThresholdCtrl.getDayAtmAmtLmt());
			}
			if (qCcsCardThresholdCtrl.getDayAtmNbrLmt() == null) {
				resp.setDay_atm_nbr_limit(productCredit.dayAtmNbr);
			} else {
				resp.setDay_atm_nbr_limit(qCcsCardThresholdCtrl.getDayAtmNbrLmt());
			}
		} else {
			resp.setDay_atm_amt_limit(productCredit.dayAtmLimit);
			resp.setDay_atm_nbr_limit(productCredit.dayAtmNbr);
		}
		// 单日消费笔数和限额
		if (qCcsCardThresholdCtrl != null) {
			if (qCcsCardThresholdCtrl.getDayRetlOvriBegDate() != null || qCcsCardThresholdCtrl.getDayRetlOvriEndDate() != null) {
				if (qCcsCardThresholdCtrl.getDayRetailAmtLmt() == null) {
					throw new ProcessException(Constants.ERRB086_CODE, Constants.ERRB086_MES);
				}
				if (qCcsCardThresholdCtrl.getDayRetailNbrLmt() == null) {
					throw new ProcessException(Constants.ERRB107_CODE, Constants.ERRB107_MES);
				}
				resp.setDay_retail_override_start(qCcsCardThresholdCtrl.getDayRetlOvriBegDate());
				resp.setDay_retail_override_end(qCcsCardThresholdCtrl.getDayRetlOvriEndDate());
			}
			if (qCcsCardThresholdCtrl.getDayRetailAmtLmt() == null) {
				resp.setDay_retail_amt_limit(productCredit.dayRetailAmtLimit);
			} else {
				resp.setDay_retail_amt_limit(qCcsCardThresholdCtrl.getDayRetailAmtLmt());
			}
			if (qCcsCardThresholdCtrl.getDayRetailNbrLmt() == null) {
				resp.setDay_retail_nbr_limit(productCredit.dayRetailNbrLimit);
			} else {
				resp.setDay_retail_nbr_limit(qCcsCardThresholdCtrl.getDayRetailNbrLmt());
			}
		} else {
			resp.setDay_retail_amt_limit(productCredit.dayRetailAmtLimit);
			resp.setDay_retail_nbr_limit(productCredit.dayRetailNbrLimit);
		}

		if (qCcsCardThresholdCtrl != null) {
			// 单日取现笔数和限额
			if (qCcsCardThresholdCtrl.getDayCashOvriBegDate() != null || qCcsCardThresholdCtrl.getDayCashOvriEndDate() != null) {
				if (qCcsCardThresholdCtrl.getDayCashAmtLmt() == null) {
					throw new ProcessException(Constants.ERRB086_CODE, Constants.ERRB086_MES);
				}
				if (qCcsCardThresholdCtrl.getDayCashNbrLmt() == null) {
					throw new ProcessException(Constants.ERRB107_CODE, Constants.ERRB107_MES);
				}
				resp.setDay_cash_override_start(qCcsCardThresholdCtrl.getDayCashOvriBegDate());
				resp.setDay_cash_override_end(qCcsCardThresholdCtrl.getDayCashOvriEndDate());
			}
			if (qCcsCardThresholdCtrl.getDayCashAmtLmt() == null) {
				resp.setDay_cash_amt_limit(productCredit.dayCashAmtLimit);
			} else {
				resp.setDay_cash_amt_limit(qCcsCardThresholdCtrl.getDayCashAmtLmt());
			}
			if (qCcsCardThresholdCtrl.getDayCashNbrLmt() == null) {
				resp.setDay_cash_nbr_limit(productCredit.dayCashNbrLimit);
			} else {
				resp.setDay_cash_nbr_limit(qCcsCardThresholdCtrl.getDayCashNbrLmt());
			}
		} else {
			resp.setDay_cash_amt_limit(productCredit.dayCashAmtLimit);
			resp.setDay_cash_nbr_limit(productCredit.dayCashNbrLimit);
		}
		if (qCcsCardThresholdCtrl != null) {
			// 单日转出限笔和金额
			if (qCcsCardThresholdCtrl.getDayXfroutOvriBegDate() != null || qCcsCardThresholdCtrl.getDayXfroutOvriEndDate() != null) {
				if (qCcsCardThresholdCtrl.getDayXfroutAmtLmt() == null) {
					throw new ProcessException(Constants.ERRB086_CODE, Constants.ERRB086_MES);
				}
				if (qCcsCardThresholdCtrl.getDayXfroutNbrLmt() == null) {
					throw new ProcessException(Constants.ERRB107_CODE, Constants.ERRB107_MES);
				}
				resp.setDay_xfrout_override_start(qCcsCardThresholdCtrl.getDayXfroutOvriBegDate());
				resp.setDay_xfrout_override_end(qCcsCardThresholdCtrl.getDayXfroutOvriEndDate());
			}
			if (qCcsCardThresholdCtrl.getDayXfroutAmtLmt() == null) {
				resp.setDay_xfrout_amt_limit(productCredit.dayXfroutAmtLimit);
			} else {
				resp.setDay_xfrout_amt_limit(qCcsCardThresholdCtrl.getDayXfroutAmtLmt());
			}
			if (qCcsCardThresholdCtrl.getDayXfroutNbrLmt() == null) {
				resp.setDay_xfrout_nbr_limit(productCredit.dayXfroutNbrLimit);
			} else {
				resp.setDay_xfrout_nbr_limit(qCcsCardThresholdCtrl.getDayXfroutNbrLmt());
			}
		} else {
			resp.setDay_xfrout_amt_limit(productCredit.dayXfroutAmtLimit);
			resp.setDay_xfrout_nbr_limit(productCredit.dayXfroutNbrLimit);
		}
		if (qCcsCardThresholdCtrl != null) {
			// 单日银联境外ATM取现限额
			if (qCcsCardThresholdCtrl.getDayCupxbAtmOvriBegDate() != null || qCcsCardThresholdCtrl.getDayCupxbAtmOvriEndDate() != null) {
				resp.setDay_cupxb_atm_override_start(qCcsCardThresholdCtrl.getDayCupxbAtmOvriBegDate());
				resp.setDay_cupxb_atm_override_end(qCcsCardThresholdCtrl.getDayCupxbAtmOvriEndDate());
			}
			if (qCcsCardThresholdCtrl.getDayCupxbAtmAmtLmt() == null) {
				resp.setDay_cupxb_atm_amt_limit(productCredit.dayCupxbAtmLimit.intValue());
			} else {
				resp.setDay_cupxb_atm_amt_limit(qCcsCardThresholdCtrl.getDayCupxbAtmAmtLmt().intValue());
			}
		} else {
			resp.setDay_cupxb_atm_amt_limit(productCredit.dayCupxbAtmLimit.intValue());
		}

		LogTools.printLogger(logger, "S15030", "卡片限额设定", resp, false);
		return resp;
	}

}
