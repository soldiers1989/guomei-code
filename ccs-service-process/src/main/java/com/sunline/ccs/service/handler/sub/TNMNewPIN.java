package com.sunline.ccs.service.handler.sub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14090Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.PasswordType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMNewPIN
 * @see 描述： 密码设定
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMNewPIN {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;

	/**
	 * @see 方法名：handler
	 * @see 描述：密码设定handler
	 * @see 创建日期：2015年6月25日下午6:09:10
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S14090Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14090", "查询密码/交易密码的设置", req, true);
		CheckUtil.checkCardNo(req.getCard_no());
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		if (req.getPasswdtype() == PasswordType.Q) {
			// 此处注掉调用MPS交易，nfs调了之后才到cps
			// mmCardService.MS3210(req.getCard_no(), req.getNewpin());
			// 设置密码之后给卡上的密码错误次数清0
			CcsCardO.setInqPinTries(0);
		} else if (req.getPasswdtype() == PasswordType.P) {
			// mmCardService.MS3211(req.getCard_no(), req.getNewpin());
			// 设置密码之后给卡上的密码错误次数清0
			CcsCardO.setPinTries(0);
		} else {
			throw new ProcessException(Constants.ERRB017_CODE, Constants.ERRB017_MES);
		}
		LogTools.printLogger(logger, "S14090", "查询密码/交易密码的设置", null, false);
	}
}
