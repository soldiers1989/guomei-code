package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
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
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S15010Req;
import com.sunline.ccs.service.protocol.S15010Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.CurrencyCodeTools;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMTempCrLimit
 * @see 描述： 临时额度设定
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMTempCrLimit {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private AcctOTB accountOTB;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsAcctO rCcsAcctO;

	/**
	 * @see 方法名：handler
	 * @see 描述： 临时额度设定handler
	 * @see 创建日期：2015年6月25日下午6:10:52
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
	public S15010Resp handler(S15010Req req) throws ProcessException {
		LogTools.printLogger(logger, "S15010", "临时额度设定", req, true);
		Organization organization = unifiedParaFacilityProvide.organization();
		// 构建响应报文对象
		S15010Resp resp = new S15010Resp();
		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)
				&& !StringUtils.equals(req.getOpt(), Constants.OPT_TWO)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 获取业务日期
		Date businessDate = unifiedParaFacilityProvide.BusinessDate();
		// 获取卡片信息
		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 附卡不能调额
		if (CcsCard.getBscSuppInd() == BscSuppIndicator.S) {
			throw new ProcessException(Constants.ERRB069_CODE, Constants.ERRB069_MES);
		}
		// 获取客户信息
		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());
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

		// 获取账户可用额度
		BigDecimal accountOtb = accountOTB.acctOTB(CcsAcctO, unifiedParaFacilityProvide.productCredit(CcsAcct.getProductCd()), businessDate);
		// 获取机构层最大授信额度
		BigDecimal organizationLmt = organization.maxCreditLimit;
		// 临时额度可调整最大值
		BigDecimal maxCreditLmt = CcsAcct.getCreditLmt().multiply(organization.tlMaxRt.add(BigDecimal.ONE));

		// 临时额度可调整最小值
		BigDecimal minCreditLmt = BigDecimal.ZERO;

		if (CcsAcct.getTempLmtBegDate() != null && CcsAcct.getTempLmtEndDate() != null
				&& DateTools.dateBetwen_IncludeEQ(CcsAcct.getTempLmtBegDate(), businessDate, CcsAcct.getTempLmtEndDate())) {
			minCreditLmt = CcsAcct.getTempLmt().subtract(accountOtb);
		} else {
			minCreditLmt = CcsAcct.getCreditLmt().subtract(accountOtb);
		}

		// 当最小临时额度为负数时，做零修正
		if (minCreditLmt.compareTo(BigDecimal.ZERO) < 0) {
			minCreditLmt = BigDecimal.ZERO;
		}

		// 若更新
		if (Constants.OPT_ONE.equals(req.getOpt())) {
			// 无开始日期，则默认当前日期
			if (req.getTemp_limit_begin_date() == null) {
				req.setTemp_limit_begin_date(businessDate);
				// 无开始日期，有结束日期，按异常处理
				if (req.getTemp_limit_end_date() != null) {
					throw new ProcessException(Constants.ERRB051_CODE, Constants.ERRB051_MES);
				}
			}
			// 无结束日期，则默认结束日期=开始日期+1个月
			if (req.getTemp_limit_end_date() == null) {
				req.setTemp_limit_end_date(DateUtils.addMonths(req.getTemp_limit_begin_date(), 1));
			}
			// 开始日期不能小于当前日期
			if (DateUtils.truncatedCompareTo(req.getTemp_limit_begin_date(), businessDate, Calendar.DATE) < 0) {
				throw new ProcessException(Constants.ERRB038_CODE, Constants.ERRB038_MES);
			}
			// 开始日期不能大于结束日期
			if (DateUtils.truncatedCompareTo(req.getTemp_limit_begin_date(), req.getTemp_limit_end_date(), Calendar.DATE) > 0) {
				throw new ProcessException(Constants.ERRB039_CODE, Constants.ERRB039_MES);
			}
			// 临额有效期不大于临额最大有效月数
			Integer tempLmtMaxMths = unifiedParaFacilityProvide.tempLimitMths();
			Date tempLmtMaxEndDateDate = DateUtils.addMonths(req.getTemp_limit_begin_date(), tempLmtMaxMths);
			if (DateUtils.truncatedCompareTo(tempLmtMaxEndDateDate, req.getTemp_limit_end_date(), Calendar.DATE) < 0) {
				throw new ProcessException(Constants.ERRB040_CODE, Constants.ERRB040_MES);
			}

			// 临额不能为空
			CheckUtil.rejectNull(req.getTemp_limit(), Constants.ERRB037_CODE, Constants.ERRB037_MES);

			// 临时额度不能小于0，不能大于信用额度*（1+临额最大调整比例），不能大于机构最大授信额度
			if (BigDecimal.valueOf(0).compareTo(req.getTemp_limit()) > 0 || req.getTemp_limit().compareTo(maxCreditLmt) > 0
					|| req.getTemp_limit().compareTo(minCreditLmt) < 0 || req.getTemp_limit().compareTo(organizationLmt) > 0) {
				throw new ProcessException(Constants.ERRB041_CODE, Constants.ERRB041_MES);
			}

			// 更新
			// 当临时额度大于客户级额度，更新客户级额度为临时额度*(1+账户层默认超限比例)，当临时额度失效时，客户级额度不变
			AccountAttribute accountAttribute = unifiedParaFacilityProvide.acct_attribute(CcsCard.getProductCd());
			BigDecimal vorlmRote = accountAttribute.ovrlmtRate;
			BigDecimal creditLmt = req.getTemp_limit().add(req.getTemp_limit().multiply(vorlmRote)).setScale(0, BigDecimal.ROUND_HALF_UP);
			tmCustLmtO.setCreditLmt(creditLmt.compareTo(tmCustLmtO.getCreditLmt()) > 0 ? creditLmt : tmCustLmtO.getCreditLmt());

			CcsAcct.setTempLmt(req.getTemp_limit());
			CcsAcct.setTempLmtBegDate(req.getTemp_limit_begin_date());
			CcsAcct.setTempLmtEndDate(req.getTemp_limit_end_date());
			rCcsAcct.save(CcsAcct);

			CcsAcctO.setTempLmt(req.getTemp_limit());
			CcsAcctO.setTempLmtBegDate(req.getTemp_limit_begin_date());
			CcsAcctO.setTempLmtEndDate(req.getTemp_limit_end_date());
			rCcsAcctO.save(CcsAcctO);

			// 临时额度调整成功短信
/*			downMsgFacility.sendMessage(
					fetchMsgCdService.fetchMsgCd(CcsAcct.getProductCd(), CPSMessageCategory.CPS028),
					req.getCard_no(),
					ccsCustomer.getName(),
					ccsCustomer.getGender(),
					ccsCustomer.getMobileNo(),
					new Date(),
					new MapBuilder<String, Object>().add("creditLmt", CcsAcctO.getTempLmt()).add("expireDate", CcsAcctO.getTempLmtEndDate())
							.add("currencyCd", CcsAcctO.getAcctType().getCurrencyCode()).build());
*/		}
		// 如果为取消临额，把临时额度的开始、结束日期设置为null
		if (Constants.OPT_TWO.equals(req.getOpt())) {
			// 临额已经失效，则拒绝
			if (CcsAcct.getTempLmtEndDate() == null) {
				throw new ProcessException(Constants.ERRB129_CODE, Constants.ERRB129_MES);
			}
			if (DateUtils.truncatedCompareTo(businessDate, CcsAcct.getTempLmtEndDate(), Calendar.DATE) > 0) {
				throw new ProcessException(Constants.ERRB128_CODE, Constants.ERRB128_MES);
			}
			CcsAcct.setTempLmtBegDate(null);
			CcsAcct.setTempLmtEndDate(null);
			CcsAcctO.setTempLmtBegDate(null);
			CcsAcctO.setTempLmtEndDate(null);

			// 临时额度取消成功短信
/*			downMsgFacility.sendMessage(fetchMsgCdService.fetchMsgCd(CcsAcct.getProductCd(), CPSMessageCategory.CPS062), req.getCard_no(),
					ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(),
					new MapBuilder<String, Object>().add("creditLmt", CcsAcctO.getCreditLmt()).add("currencyCd", CcsAcctO.getAcctType().getCurrencyCode())
							.build());
*/		}

		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setTemp_limit(CcsAcctO.getTempLmt());
		resp.setTemp_limit_begin_date(CcsAcctO.getTempLmtBegDate());
		resp.setTemp_limit_end_date(CcsAcctO.getTempLmtEndDate());
		resp.setDual_curr_ind(CurrencyCodeTools.isExistOtherCurrCd(CcsAcct.getAcctType().getCurrencyCode(), acctAttr, dualAcctAttr));
		resp.setDual_curr_cd(CurrencyCodeTools.getOtherCurrCd(CcsAcct.getAcctType().getCurrencyCode(), acctAttr, dualAcctAttr));

		resp.setMax_temp_limit(maxCreditLmt);
		resp.setMin_temp_limit(minCreditLmt);
		resp.setTemp_limit_max_mths(organization.tempLimitMaxMths);
		LogTools.printLogger(logger, "S15010", "临时额度设定", resp, false);
		return resp;
	}
}
