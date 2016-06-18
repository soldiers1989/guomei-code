package com.sunline.ccs.service.handler.sub;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S16050Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DataTypeUtils;
import com.sunline.ppy.api.MediumService;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.CntType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNVThirdPayOpen
 * @see 描述：第三方快捷支付开通验证
 *
 * @see 创建日期： 2015年6月24日 下午4:57:04
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNVThirdPayOpen {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private GlobalManagementService globalManagementService;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private Common common;

	@Resource(name = "mmCardService")
	private MmCardService mmCardService;
	@Resource(name = "mediumService")
	private MediumService mediumService;

	@Transactional
	public void handler(S16050Req req) throws ProcessException {
		LogTools.printLogger(logger, "S16050", "第三方快捷支付开通验证", null, true);

		CheckUtil.checkCardNo(req.getCard_no());
		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());
		// CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CcsCardO CcsCardO = custAcctCardQueryFacility.getCardOByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(ccsCustomer, Constants.ERRB015_CODE, Constants.ERRB015_MES);
		// 币种写死为156
		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), S16050Req.CURR_CD);

		String expiryDate = null;
		if (req.getExpire_date() != null)
			expiryDate = new SimpleDateFormat("yyMM").format(req.getExpire_date());

		if (req.getId_type() != null && ccsCustomer.getIdType() != req.getId_type())
			throw new ProcessException(Constants.ERRB055_CODE, Constants.ERRB055_MES);

		if (req.getId_no() != null && !StringUtils.equals(ccsCustomer.getIdNo(), req.getId_no()))
			throw new ProcessException(Constants.ERRB056_CODE, Constants.ERRB056_MES);

		if (req.getMobile_no() != null && !StringUtils.equals(ccsCustomer.getMobileNo(), req.getMobile_no()))
			throw new ProcessException(Constants.ERRB057_CODE, Constants.ERRB057_MES);

		if (req.getCust_name() != null && !StringUtils.equals(ccsCustomer.getName(), req.getCust_name()))
			throw new ProcessException(Constants.ERRB059_CODE, Constants.ERRB059_MES);

		ProductCredit product = unifiedParaFacilityProvide.productCredit(CcsCardO.getProductCd());
		// 校验查询密码
		if (req.getQ_pin() != null)
			common.validateInqPwd(req.getCard_no(), req.getQ_pin(), product);
		// 校验交易密码
		if (req.getP_pin() != null)
			common.validatePPwd(req.getCard_no(), req.getP_pin(), product);

		if (expiryDate != null && !mediumService.isValidExpiryDate(req.getCard_no(), expiryDate))
			throw new ProcessException(Constants.ERRB064_CODE, Constants.ERRB064_MES);
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

		// 调用MPS服务获取介质卡片信息
		Map<String, Serializable> map = mmCardService.MS3102(req.getCard_no());
		// 是否激活肯定有值，不用判断非空
		Indicator activateInd = Indicator.valueOf(map.get(S16050Req.P_ActivateInd).toString());
		if (activateInd.equals(Indicator.N)) {
			throw new ProcessException(Constants.ERRB109_CODE, Constants.ERRB109_MES);
		}
		// 有效期
		Date date = DataTypeUtils.getDateValue(map.get(S16050Req.P_ExpiryDate));
		if (DateUtils.truncatedCompareTo(date, globalManagementService.getSystemStatus().getBusinessDate(), Calendar.DATE) < 0) {
			throw new ProcessException(Constants.ERRB110_CODE, Constants.ERRB110_MES);
		}
		// 得到介质卡、逻辑卡、账户的锁定码
		String blockCodeMps = DataTypeUtils.getStringValue(map.get(S16050Req.P_BlockCd));
		String blockCodeAcct = CcsAcct.getBlockCode();
		String blockCodeCard = CcsCardO.getBlockCode();
		// 合并之后的锁定码
		String blockCodeAll = blockCodeUtils.unionBlockCodes(blockCodeMps, blockCodeUtils.unionBlockCodes(blockCodeAcct, blockCodeCard));
		// if(!blockCodeAll.isEmpty() && blockCodeAll != ""){
		// for(Character c : blockCodeAll.toCharArray()){
		// BlockCode b = unifiedParameterFacility.loadParameter(c,
		// BlockCode.class);
		// if(b.agentAction.equals(AuthAction.D) ||
		// b.postInd.equals(PostAvailiableInd.R)){
		//
		// }
		// }
		// }
		if (StringUtils.contains(blockCodeAll, S16050Req.BLOCKCODE_P)) {
			throw new ProcessException(Constants.ERRB113_CODE, Constants.ERRB113_MES);
		}
		if (StringUtils.contains(blockCodeAll, S16050Req.BLOCKCODE_C)) {
			throw new ProcessException(Constants.ERRB111_CODE, Constants.ERRB111_MES);
		}
		if (StringUtils.contains(blockCodeAll, S16050Req.BLOCKCODE_T)) {
			throw new ProcessException(Constants.ERRB112_CODE, Constants.ERRB112_MES);
		}
		if (StringUtils.contains(blockCodeAll, S16050Req.BLOCKCODE_R) || StringUtils.contains(blockCodeAll, S16050Req.BLOCKCODE_S)) {
			throw new ProcessException(Constants.ERRB114_CODE, Constants.ERRB114_MES);
		}
		if (StringUtils.contains(blockCodeAll, S16050Req.BLOCKCODE_L)) {
			throw new ProcessException(Constants.ERRB115_CODE, Constants.ERRB115_MES);
		}

		LogTools.printLogger(logger, "S16050", "第三方快捷支付开通验证", null, false);
	}

}
