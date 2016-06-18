package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12101Req;
import com.sunline.ccs.service.protocol.S12101Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.CurrencyCodeTools;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.SmsInd;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMMessageInd
 * @see 描述： 发送短信设置
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMMessageInd {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;

	@Autowired
	private RCcsAcctO rCcsAcctO;
	@Autowired
	private RCcsAcct rCcsAcct;

	/**
	 * @see 方法名：handler
	 * @see 描述：发送短信设置handle
	 * @see 创建日期：2015年6月25日下午6:07:32
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
	public S12101Resp handler(S12101Req req) throws ProcessException {

		LogTools.printLogger(logger, "S12101", "发送短信设置", req, true);

		// 检查上送各字段的合法性
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 创建返回对象
		S12101Resp resp = new S12101Resp();

		// 获取卡片信息
		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// 判断是否为附卡，若为附卡则不能做短信设置
		if (CcsCard.getBscSuppInd() == BscSuppIndicator.S) {
			throw new ProcessException(Constants.ERRB069_CODE, Constants.ERRB069_MES);
		}

		// 获取账户列表
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRS001_CODE, Constants.ERRS001_MES);
		CcsAcctO CcsAcctO = queryFacility.getAcctOByAcctNbr(CcsAcct.getAcctType(), CcsAcct.getAcctNbr());
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 根据卡号获取卡产品贷记属性参数
		ProductCredit product = cardNbrTOProdctAcctFacility.CardNoToProductCr(req.getCard_no());
		// 获取参数
		AccountAttribute acctAttr = unifiedParaFacilityProvide.acct_attribute(CcsCard.getProductCd());
		AccountAttribute dualAcctAttr = unifiedParaFacilityProvide.dual_acct_attribute(CcsCard.getProductCd());

		// 若更新
		CcsAcct postCcsAcct = null;
		if (product.postCurrCd != null) {
			postCcsAcct = queryFacility.getAcctByCardNbrCurrency(req.getCard_no(), product.postCurrCd);
		}
		CcsAcct dualCcsAcct = null;
		if (product.dualCurrCd != null) {
			dualCcsAcct = queryFacility.getAcctByCardNbrCurrency(req.getCard_no(), product.dualCurrCd);
		}

		if (Constants.OPT_ONE.equals(req.getOpt())) {
			// 验证短信发送标志
			if (!StringUtils.equals(SmsInd.C.name(), req.getSms_ind().name()) && !StringUtils.equals(SmsInd.Y.name(), req.getSms_ind().name())
					&& !StringUtils.equals(SmsInd.N.name(), req.getSms_ind().name())) {
				throw new ProcessException(Constants.ERRB093_CODE, Constants.ERRB093_MES);
			}
			// 处理本币
			if (product.postCurrCd != null) {
				postCcsAcct.setSmsInd(req.getSms_ind());
				CcsAcctO postCcsAcctO = queryFacility.getAcctOByAcctNbr(postCcsAcct.getAcctType(), postCcsAcct.getAcctNbr());
				postCcsAcctO.setSmsInd(req.getSms_ind());
				// 如果为更新个性化短信阀值的时候，校验阀值大于0
				if (req.getSms_ind() == SmsInd.C) {
					if (req.getUser_sms_amt() != null && req.getUser_sms_amt().compareTo(new BigDecimal(0)) >= 0) {
						postCcsAcct.setUserSmsAmt(req.getUser_sms_amt());
						postCcsAcctO.setUserSmsAmt(req.getUser_sms_amt());
						rCcsAcct.save(postCcsAcct);
						rCcsAcctO.save(postCcsAcctO);
					} else {
						throw new ProcessException(Constants.ERRB092_CODE, Constants.ERRB092_MES);
					}
				}
			}
			// 处理外币
			if (product.dualCurrCd != null) {
				dualCcsAcct.setSmsInd(req.getSms_ind());
				CcsAcctO dualCcsAcctO = queryFacility.getAcctOByAcctNbr(dualCcsAcct.getAcctType(), dualCcsAcct.getAcctNbr());
				dualCcsAcctO.setSmsInd(req.getSms_ind());
				// 如果为更新个性化短信阀值的时候，校验阀值大于0
				if (req.getSms_ind() == SmsInd.C) {
					if (req.getUser_sms_amt() != null && req.getUser_sms_amt().compareTo(new BigDecimal(0)) >= 0) {
						dualCcsAcct.setUserSmsAmt(req.getUser_sms_amt());
						dualCcsAcctO.setUserSmsAmt(req.getUser_sms_amt());
						rCcsAcct.save(dualCcsAcct);
						rCcsAcctO.save(dualCcsAcctO);
					} else {
						throw new ProcessException(Constants.ERRB092_CODE, Constants.ERRB092_MES);
					}
				}
			}
		}

		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setSmsind(postCcsAcct == null ? dualCcsAcct.getSmsInd() : postCcsAcct.getSmsInd());
		resp.setUser_sms_amt(postCcsAcct == null ? dualCcsAcct.getUserSmsAmt() : postCcsAcct.getUserSmsAmt());
		resp.setDual_curr_ind(CurrencyCodeTools.isExistOtherCurrCd(CcsAcct.getAcctType().getCurrencyCode(), acctAttr, dualAcctAttr));
		resp.setDual_curr_cd(CurrencyCodeTools.getOtherCurrCd(CcsAcct.getAcctType().getCurrencyCode(), acctAttr, dualAcctAttr));

		LogTools.printLogger(logger, "S12101", "发送短信设置", resp, false);

		return resp;
	}
}
