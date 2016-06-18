package com.sunline.ccs.service.handler.sub;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14180Req;
import com.sunline.ccs.service.util.BlockCodeUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMCardFrozen
 * @see 描述： 卡片冻结/解冻
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMCardFrozen {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private Common common;

	/**
	 * @see 方法名：handler
	 * @see 描述：卡片冻结/解冻handler
	 * @see 创建日期：2015年6月25日下午5:48:47
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S14180Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14180", "冻结/解冻", req, true);

		CheckUtil.checkCardNo(req.getCard_no());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}
		// 冻结
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ZERO)) {
			S14180_OPT_ZERO(req.getCard_no());
		}
		// 解冻
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ONE)) {
			S14180_OPT_ONE(req.getCard_no());
		}
		LogTools.printLogger(logger, "S14180", "冻结/解冻", null, false);
	}

	/**
	 * @see 方法名：S14180_OPT_ZERO
	 * @see 描述：卡片冻结
	 * @see 创建日期：2015年6月25日下午5:48:59
	 * @author yanjingfeng
	 * 
	 * @param cardNbr
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void S14180_OPT_ZERO(String cardNbr) throws ProcessException {
		CheckUtil.checkCardNo(cardNbr);
		CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		if (BlockCodeUtil.hasBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
			throw new ProcessException(Constants.ERRB012_CODE, Constants.ERRB012_MES);
		}
		if (BlockCodeUtil.hasBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
			throw new ProcessException(Constants.ERRB012_CODE, Constants.ERRB012_MES);
		}
		CcsCard.setBlockCode(BlockCodeUtil.addBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_T));
		CcsCardO.setBlockCode(BlockCodeUtil.addBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_T));

		// 卡片冻结提醒短信
		common.sendMsg(CcsCard.getProductCd(), CcsCard.getCustId(), cardNbr, CPSMessageCategory.CPS054);
	}

	/**
	 * @see 方法名：S14180_OPT_ONE
	 * @see 描述：卡片解冻
	 * @see 创建日期：2015年6月25日下午5:49:13
	 * @author yanjingfeng
	 * 
	 * @param cardNbr
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void S14180_OPT_ONE(String cardNbr) throws ProcessException {
		CheckUtil.checkCardNo(cardNbr);
		CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		if (!BlockCodeUtil.hasBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
			throw new ProcessException(Constants.ERRB013_CODE, Constants.ERRB013_MES);
		}
		if (!BlockCodeUtil.hasBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
			throw new ProcessException(Constants.ERRB013_CODE, Constants.ERRB013_MES);
		}
		CcsCard.setBlockCode(BlockCodeUtil.removeBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_T));
		CcsCardO.setBlockCode(BlockCodeUtil.removeBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_T));

		// 卡片冻结解除提醒短信
		common.sendMsg(CcsCard.getProductCd(), CcsCard.getCustId(), cardNbr, CPSMessageCategory.CPS009);
	}

}
