package com.sunline.ccs.service.handler.sub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14091Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.PasswordType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMModifyPIN
 * @see 描述： 密码修改
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMModifyPIN {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private Common common;

	/**
	 * @see 方法名：handler
	 * @see 描述：密码修改handler
	 * @see 创建日期：2015年6月25日下午6:08:48
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S14091Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14091", "查询密码/交易密码的修改", req, true);
		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		ProductCredit product = unifiedParaFacilityProvide.productCredit(CcsCard.getProductCd());
		CheckUtil.checkCardNo(req.getCard_no());
		// 此处注掉调用MPS交易，nfs调了之后才到cps
		if (req.getPasswdtype() == PasswordType.Q) {
			common.validateInqPwd(req.getCard_no(), req.getPin(), product);
			// mmCardService.MS3209(req.getCard_no(), req.getPin(),
			// req.getNewpin());
		} else if (req.getPasswdtype() == PasswordType.P) {
			common.validatePPwd(req.getCard_no(), req.getPin(), product);
			// mmCardService.MS3208(req.getCard_no(), req.getPin(),
			// req.getNewpin());
		} else {
			throw new ProcessException(Constants.ERRB017_CODE, Constants.ERRB017_MES);
		}
		LogTools.printLogger(logger, "S14091", "查询密码/交易密码的修改", null, false);
	}
}
