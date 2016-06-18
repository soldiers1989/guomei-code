package com.sunline.ccs.service.handler.sub;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S16040Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.api.MediumService;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.CntType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNVBaseInfo
 * @see 描述： 信息验证
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNVBaseInfo {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private Common common;
	
	@Resource(name = "mediumService")
	private MediumService mediumService;
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	@Autowired
	private RCcsAddress rCcsAddress;

	/**
	 * @see 方法名：handler
	 * @see 描述：信息验证handler
	 * @see 创建日期：2015年6月25日下午6:20:46
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S16040Req req) throws ProcessException {
		LogTools.printLogger(logger, "S16040", "账户信息验证", req, true);
		// CheckUtil.checkCardNo(req.getCard_no());
		CcsCustomer ccsCustomer = null;
		CcsCardO CcsCardO = null;
		if (req.getId_type() != null && StringUtils.isNotBlank(req.getId_no())) {
			ccsCustomer = queryFacility.getCustomerById(req.getId_no(), req.getId_type());
		}
		if (StringUtils.isNotBlank(req.getCard_no())) {
			ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());
			CcsCardO = custAcctCardQueryFacility.getCardOByCardNbr(req.getCard_no());
		}
		CheckUtil.rejectNull(ccsCustomer, Constants.ERRB015_CODE, Constants.ERRB015_MES);
		String expiryDate = null;
		if (req.getExpire_date() != null) {
			expiryDate = new SimpleDateFormat("yyMM").format(req.getExpire_date());
		}
		if (req.getId_type() != null && ccsCustomer.getIdType() != req.getId_type()) {
			throw new ProcessException(Constants.ERRB055_CODE, Constants.ERRB055_MES);
		}
		if (req.getId_no() != null && !StringUtils.equals(ccsCustomer.getIdNo(), req.getId_no())) {
			throw new ProcessException(Constants.ERRB056_CODE, Constants.ERRB056_MES);
		}

		if (req.getMobile_no() != null && !StringUtils.equals(ccsCustomer.getMobileNo(), req.getMobile_no())) {
			throw new ProcessException(Constants.ERRB057_CODE, Constants.ERRB057_MES);
		}

		// if (req.getHome_phone() != null &&
		// !StringUtils.equals(ccsCustomer.getHomePhone(), req.getHome_phone()))
		// 2.4版本改动，在客户表中电话号码中间有“-”，在信息验证时，去掉“-”,同时可省略区号进行验证
		if (req.getHome_phone() != null && !CheckUtil.checkHomeOrCorpPhone(req.getHome_phone(), ccsCustomer.getHomePhone())) {
			throw new ProcessException(Constants.ERRB058_CODE, Constants.ERRB058_MES);
		}
		if (req.getCust_name() != null && !StringUtils.equals(ccsCustomer.getName(), req.getCust_name())) {
			throw new ProcessException(Constants.ERRB059_CODE, Constants.ERRB059_MES);
		}

		if (req.getBirthday() != null && ccsCustomer.getBirthday().compareTo(req.getBirthday()) != 0) {
			throw new ProcessException(Constants.ERRB060_CODE, Constants.ERRB060_MES);
		}

		if (req.getCorp_phone() != null) {
			CcsAddress address = rCcsAddress.findByCustIdAndAddrType(ccsCustomer.getCustId(), AddressType.C).get(0);
			// if (address == null || !StringUtils.equals(address.getPhone(),
			// req.getCorp_phone()))
			// 2.4版本改动，在客户表中电话号码中间有“-”，在信息验证时，去掉“-”,同时可省略区号进行验证
			if (address != null && !CheckUtil.checkHomeOrCorpPhone(req.getCorp_phone(), address.getPhone())) {
				throw new ProcessException(Constants.ERRB061_CODE, Constants.ERRB061_MES);
			}

		}
		if (CcsCardO != null) {
			ProductCredit product = unifiedParaFacilityProvide.productCredit(CcsCardO.getProductCd());
			// 校验查询密码
			if (req.getQ_pin() != null) {
				common.validateInqPwd(req.getCard_no(), req.getQ_pin(), product);
			}
			// 校验交易密码
			if (req.getP_pin() != null) {
				common.validatePPwd(req.getCard_no(), req.getP_pin(), product);
			}
			if (expiryDate != null && !mediumService.isValidExpiryDate(req.getCard_no(), expiryDate)) {
				throw new ProcessException(Constants.ERRB064_CODE, Constants.ERRB064_MES);
			}

			if (req.getCvv2() != null) {
				if (expiryDate == null) {
					throw new ProcessException(Constants.ERRB100_CODE, Constants.ERRB100_MES);
				}
				if (!mediumService.isValidCvv2(req.getCard_no(), expiryDate, req.getCvv2())) {
					mmCardService.setErrCnt(CntType.cvn2, req.getCard_no(), req.getExpire_date());
					common.cvv2IsOver(req.getCard_no(), expiryDate, product);
					throw new ProcessException(Constants.ERRB065_CODE, Constants.ERRB065_MES);
				} else {
					common.cvv2IsNotOverSetZero(req.getCard_no(), expiryDate, product);
				}
			}
		}
		// 获得联系人
		List<CcsLinkman> contacts = queryFacility.getCcsLinkmanByCustId(ccsCustomer.getCustId());
		if (contacts != null) {
			// 直属联系人姓名
			if (req.getName() != null) {
				boolean isNameErr = false;
				for (CcsLinkman contact : contacts) {
					if (StringUtils.equals(contact.getName(), req.getName())) {
						isNameErr = true;
					}
				}
				if (!isNameErr) {
					throw new ProcessException(Constants.ERRB066_CODE, Constants.ERRB066_MES);
				}
			}
			// 直属联系人电话
			if (req.getPhone() != null) {
				boolean isPhoneErr = false;
				for (CcsLinkman contact : contacts) {
					if (StringUtils.equals(contact.getCorpTelephNbr(), req.getPhone()) || StringUtils.equals(contact.getMobileNo(), req.getPhone())) {
						isPhoneErr = true;
					}
				}
				if (!isPhoneErr) {
					throw new ProcessException(Constants.ERRB067_CODE, Constants.ERRB067_MES);
				}
			}

		} else {
			throw new ProcessException(Constants.ERRB200_CODE, Constants.ERRB200_MES);
		}
		LogTools.printLogger(logger, "S16040", "账户信息验证", null, false);
	}

}
