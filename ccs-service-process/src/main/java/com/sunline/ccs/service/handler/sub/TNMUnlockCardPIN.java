package com.sunline.ccs.service.handler.sub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14100Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.enums.PasswordType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMUnlockCardPIN
 * @see 描述： 卡片交易/查询密码锁定解除
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMUnlockCardPIN {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private Common common;

	@Autowired
	private RCcsCustomer rCcsCustomer;

	/**
	 * @see 方法名：handler
	 * @see 描述：卡片交易/查询密码锁定解除handler
	 * @see 创建日期：2015年6月25日下午6:11:45
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S14100Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14100", "卡片交易/查询密码锁定解除", req, true);

		CheckUtil.checkCardNo(req.getCard_no());
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// CcsCustomer ccsCustomer = rCcsCustomer.findOne(CcsCardO.getCustId());
		// 重置交易密码
		if (req.getPasswdtype() == PasswordType.P) {
			CcsCardO.setPinTries(CcServProConstants.PINTRIES_DEFAULT);
			// 发短信
			common.sendMsg(CcsCardO.getProductCd(), CcsCardO.getCustId(), req.getCard_no(), CPSMessageCategory.CPS008);
		}
		// 重置查询密码
		if (req.getPasswdtype() == PasswordType.Q) {
			CcsCardO.setInqPinTries(CcServProConstants.PINTRIES_DEFAULT);
			// 发短信
			common.sendMsg(CcsCardO.getProductCd(), CcsCardO.getCustId(), req.getCard_no(), CPSMessageCategory.CPS018);
		}

		// 查询密码锁定重置短信

		// messageService.sendMessage(MessageCategory.M18,
		// CcsCardO.getProductCd(), req.getCard_no(), ccsCustomer.getName(),
		// ccsCustomer.getGender(), ccsCustomer.getMobileNo(),
		// ccsCustomer.getEmail(),
		// new Date(), new MapBuilder().build());

		LogTools.printLogger(logger, "S14100", "卡片交易/查询密码锁定解除", null, false);
	}
}
