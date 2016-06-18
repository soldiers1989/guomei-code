package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S15011Req;
import com.sunline.ccs.service.protocol.S15011Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.CurrencyCodeTools;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMCrLimit
 * @see 描述： 永久额度调整（直接调整，仅限于内管使用）
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMCrLimit {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsAcctCrlmtAdjLog rCcsAcctCrlmtAdjLog;
	@Autowired
	private RCcsAcctO rCcsAcctO;

	/**
	 * @see 方法名：handler
	 * @see 描述：永久额度调整（直接调整，仅限于内管使用）handler
	 * @see 创建日期：2015年6月25日下午5:54:58
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
	public S15011Resp handler(S15011Req req) throws ProcessException {

		LogTools.printLogger(logger, "15011", "永久额度调整", req, true);
		// 构建响应报文
		S15011Resp resp = new S15011Resp();

		// 检查上送各字段的合法性
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 获取卡片信息
		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		if (CcsCard.getBscSuppInd() == BscSuppIndicator.S) {
			throw new ProcessException(Constants.ERRB069_CODE, Constants.ERRB069_MES);

		}
		// 获取客户信息
		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(CcsCard.getCardBasicNbr());
		CheckUtil.rejectNull(ccsCustomer, Constants.ERRS001_CODE, Constants.ERRS001_MES);
		// 获取客户额度信息
		CcsCustomerCrlmt tmCustLmtO = queryFacility.getCustomerCrLmtByCustLmtId(ccsCustomer.getCustLmtId());
		CheckUtil.rejectNull(tmCustLmtO, Constants.ERRS001_CODE, Constants.ERRS001_MES);
		// 获取参数
		AccountAttribute acctAttr = unifiedParaFacilityProvide.acct_attribute(CcsCard.getProductCd());
		AccountAttribute dualAcctAttr = unifiedParaFacilityProvide.dual_acct_attribute(CcsCard.getProductCd());
		// 获取账户列表
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRS001_CODE, Constants.ERRS001_MES);
		CcsAcctO CcsAcctO = queryFacility.getAcctOByAcctNbr(CcsAcct.getAcctType(), CcsAcct.getAcctNbr());
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 保存调整前信用额度
		BigDecimal prev_credit_limit = CcsAcctO.getCreditLmt();

		// 若更新
		if (Constants.OPT_ONE.equals(req.getOpt())) {
			// 当上送的调整额度为空或者为负值时报金额不能为空或负值
			if (req.getCredit_limit() == null || req.getCredit_limit().compareTo(BigDecimal.ZERO) < 0) {
				throw new ProcessException(Constants.ERRB092_CODE, Constants.ERRB092_MES);
			}

			CcsAcctCrlmtAdjLog tmLmtAdjLog = new CcsAcctCrlmtAdjLog();
			tmLmtAdjLog.setAcctNbr(CcsAcctO.getAcctNbr());
			tmLmtAdjLog.setAcctType(CcsAcctO.getAcctType());
			tmLmtAdjLog.setCardNbr(req.getCard_no());
			tmLmtAdjLog.setAdjState(AdjState.A);
			tmLmtAdjLog.setCreditLmtNew(req.getCredit_limit());
			tmLmtAdjLog.setCreditLmtOrig(prev_credit_limit);
			tmLmtAdjLog.setOpId("");
			tmLmtAdjLog.setOpTime(new Date());
			tmLmtAdjLog.setProcDate(unifiedParaFacilityProvide.BusinessDate());
			tmLmtAdjLog.setOrg(OrganizationContextHolder.getCurrentOrg());
			rCcsAcctCrlmtAdjLog.save(tmLmtAdjLog);

			CcsAcct.setCreditLmt(req.getCredit_limit());
			rCcsAcct.save(CcsAcct);
			CcsAcctO.setCreditLmt(req.getCredit_limit());
			rCcsAcctO.save(CcsAcctO);

			// 当额度大于客户级额度，更新客户级额度为账户额度
			tmCustLmtO.setCreditLmt(req.getCredit_limit().compareTo(tmCustLmtO.getCreditLmt()) > 0 ? req.getCredit_limit() : tmCustLmtO.getCreditLmt());
/*			downMsgFacility.sendMessage(fetchMsgCdService.fetchMsgCd(CcsAcct.getProductCd(), CPSMessageCategory.CPS029), req.getCard_no(),
					ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(),
					new MapBuilder<String, Object>().add("creditLmt", CcsAcctO.getCreditLmt()).build());
*/		}
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setPrev_credit_limit(prev_credit_limit);
		resp.setCredit_limit(CcsAcctO.getCreditLmt());
		resp.setDual_curr_ind(CurrencyCodeTools.isExistOtherCurrCd(CcsAcct.getAcctType().getCurrencyCode(), acctAttr, dualAcctAttr));
		resp.setDual_curr_cd(CurrencyCodeTools.getOtherCurrCd(CcsAcct.getAcctType().getCurrencyCode(), acctAttr, dualAcctAttr));

		LogTools.printLogger(logger, "15011", "永久额度调整", resp, false);

		return resp;
	}
}
