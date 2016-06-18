package com.sunline.ccs.service.handler.sub;

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
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14080Req;
import com.sunline.ccs.service.protocol.S14080Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMVerifyPIN
 * @see 描述： 消费凭密设定
 *
 * @see 创建日期： 2015年06月25日下午 02:25:39
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMVerifyPIN {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private Common common;
	
	@Resource(name = "mmCardService")
    public MmCardService mmCardService;

	/**
	 * @see 方法名：handler
	 * @see 描述：消费凭密设定handler
	 * @see 创建日期：2015年6月25日下午5:58:19
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
	public S14080Resp handler(S14080Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14080", "消费凭密设定", req, true);
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}
		CheckUtil.checkCardNo(req.getCard_no());
		S14080Resp resp = new S14080Resp();
		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// 获取[是否存在交易密码]
		Indicator pPinExistInd = mmCardService.MS3502(req.getCard_no()) ? Indicator.Y : Indicator.N;
		// 查询类直接返回卡片信息
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ZERO)) {
			resp.setCard_no(req.getCard_no());
			resp.setPos_pin_verify_ind(CcsCardO.getPosPinVerifyInd());
			resp.setP_pin_exist_ind(pPinExistInd);
			return resp;
		}

		// 设定[消费凭密]
		String CcsCardBlockCode = CcsCard.getBlockCode();
		String CcsCardOBlockCode = CcsCardO.getBlockCode();
		if (!isSetPosPinVerifyInd(CcsCardBlockCode)) {
			throw new ProcessException(Constants.ERRB006_CODE, Constants.ERRB006_MES);
		}
		if (!isSetPosPinVerifyInd(CcsCardOBlockCode)) {
			throw new ProcessException(Constants.ERRB006_CODE, Constants.ERRB006_MES);
		}
		if (CcsCard.getPosPinVerifyInd().equals(req.getPos_pin_verify_ind())) {
			throw new ProcessException(Constants.ERRB007_CODE, Constants.ERRB007_MES);
		}
		if (CcsCardO.getPosPinVerifyInd().equals(req.getPos_pin_verify_ind())) {
			throw new ProcessException(Constants.ERRB007_CODE, Constants.ERRB007_MES);
		}
		if (req.getPos_pin_verify_ind() != null) {
			CcsCard.setPosPinVerifyInd(req.getPos_pin_verify_ind());
		}
		if (req.getPos_pin_verify_ind() != null) {
			CcsCardO.setPosPinVerifyInd(req.getPos_pin_verify_ind());
		}

		// 凭密标识变更短信
		// CcsCustomer ccsCustomer = rCcsCustomer.findOne(CcsCard.getCustId());
		// messageService.sendMessage(MessageCategory.M07,
		// CcsCard.getProductCd(), req.getCard_no(), ccsCustomer.getName(),
		// ccsCustomer.getGender(), ccsCustomer.getMobileNo(),
		// ccsCustomer.getEmail(),
		// new Date(), new MapBuilder().build());
		common.sendMsg(CcsCard.getProductCd(), CcsCard.getCustId(), req.getCard_no(), CPSMessageCategory.CPS007);

		resp.setCard_no(req.getCard_no());
		resp.setPos_pin_verify_ind(req.getPos_pin_verify_ind());
		resp.setP_pin_exist_ind(pPinExistInd);
		LogTools.printLogger(logger, "S14080", "消费凭密设定", resp, false);
		return resp;
	}

	/**
	 * @see 方法名：isSetPosPinVerifyInd
	 * @see 描述：判断是否可以设置凭密标志
	 * @see 创建日期：2015年6月25日下午6:12:43
	 * @author yanjingfeng
	 * 
	 * @param blockCodeStr
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public boolean isSetPosPinVerifyInd(String blockCodeStr) throws ProcessException {
		boolean flag = true;
		if (blockCodeStr != null && blockCodeStr.length() > 0) {
			char[] cc = blockCodeStr.toCharArray();
			for (char c : cc) {
				BlockCode blockCode = unifiedParaFacilityProvide.blockCode(String.valueOf(c));
				if (blockCode.nonMotoRetailAction != AuthAction.A) {
					flag = false;
				}
			}
		}
		return flag;
	}

}
