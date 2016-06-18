package com.sunline.ccs.service.handler.sub;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14092Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.CntType;
import com.sunline.ppy.dictionary.enums.UnlockPwdType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMUnlockPIN
 * @see 描述： 密码解锁
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMUnlockPIN {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	/**
	 * @see 方法名：handler
	 * @see 描述：密码解锁handler
	 * @see 创建日期：2015年6月25日下午6:12:05
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S14092Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14092", "客服/内管解锁服务", req, true);
		CheckUtil.checkCardNo(req.getCard_no());
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// 如果上送卡片有效期不为null，则验证卡片有效期
		if (req.getExpire_date() != null) {
			DateFormat df = new SimpleDateFormat("yyMM");
			if (!df.format(CcsCard.getCardExpireDate()).equals(req.getExpire_date())) {
				throw new ProcessException(Constants.ERRB064_CODE, Constants.ERRB064_MES);
			}
		}
		if (req.getError_type() == UnlockPwdType.Q) {
			// 设置密码之后给卡上的密码错误次数清0
			CcsCardO.setInqPinTries(0);
		} else if (req.getError_type() == UnlockPwdType.P) {
			// 设置密码之后给卡上的密码错误次数清0
			CcsCardO.setPinTries(0);
		} else if (req.getError_type() == UnlockPwdType.CVV2) {
			// 设置密码之后给卡上的密码错误次数清0
			mmCardService.MS3401(req.getCard_no(), req.getExpire_date(), CntType.cvn2);
		} else if (req.getError_type() == UnlockPwdType.CVV) {
			// 设置密码之后给卡上的密码错误次数清0
			mmCardService.MS3401(req.getCard_no(), req.getExpire_date(), CntType.cvn);
		} else if (req.getError_type() == UnlockPwdType.ICVV) {
			// 设置密码之后给卡上的密码错误次数清0
			mmCardService.MS3401(req.getCard_no(), req.getExpire_date(), CntType.icvn);
		} else {
			throw new ProcessException(Constants.ERRB017_CODE, Constants.ERRB017_MES);
		}
		LogTools.printLogger(logger, "S14092", "客服/内管解锁服务", null, false);
	}

}
