package com.sunline.ccs.service.handler.sub;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14050Req;
import com.sunline.ccs.service.util.BlockCodeUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DataTypeUtils;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMCardLost
 * @see 描述： 卡片挂失/解挂
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMCardLost {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private Common common;
	
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	/**
	 * @see 方法名：handler
	 * @see 描述：卡片挂失/解挂handler
	 * @see 创建日期：2015年6月25日下午5:51:25
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S14050Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14050", "卡片挂失/解挂", req, true);
		CheckUtil.checkCardNo(req.getCard_no());

		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		CcsCardO CcsCardO = custAcctCardQueryFacility.getCardOByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 卡片挂失
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ZERO)) {
			if (!"R".equals(req.getLost_reason()) && !"S".equals(req.getLost_reason())) {
				throw new ProcessException(Constants.ERRB088_CODE, Constants.ERRB088_MES);
			}
			// 因为封装的方法中有是否集合为空的校验，这里就不做校验
			List<CcsAcct> accounts = custAcctCardQueryFacility.getAcctByCardNbr(req.getCard_no());
			for (CcsAcct CcsAcct : accounts) {
				if (BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
					throw new ProcessException(Constants.ERRB096_CODE, Constants.ERRB096_MES);
				}
			}
			if (BlockCodeUtil.hasBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
				throw new ProcessException(Constants.ERRB096_CODE, Constants.ERRB096_MES);
			}
			if (BlockCodeUtil.hasBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
				throw new ProcessException(Constants.ERRB096_CODE, Constants.ERRB096_MES);
			}
			// 卡片上增加挂失锁定码 挂失不能逻辑卡上锁定码
			// CcsCard.setBlockCode(BlockCodeUtil.addBlockCode(CcsCard.getBlockCode(),
			// req.getLost_reason().toUpperCase()));
			// CcsCardO.setBlockCode(BlockCodeUtil.addBlockCode(CcsCardO.getBlockCode(),
			// req.getLost_reason().toUpperCase()));

			mmCardService.MS3204(req.getCard_no(), req.getLost_reason().toUpperCase());
			// 发短信
			common.sendMsg(CcsCard.getProductCd(), CcsCard.getCustId(), req.getCard_no(), CPSMessageCategory.CPS055);
			// 杂项费用
			// TODO 以账户为准,这里暂时写死,只看本币账户
			CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), Constants.CURR_CD_156);
			if (req.getFee_ind() == Indicator.Y) {
				// 当账户中不免除服务费时才收取客服费
				if (CcsAcct.getWaiveSvcfeeInd() == Indicator.N) {
					common.cssfeeReg(req.getCard_no(), "S14050");
				}
			}
		}
		// 卡片解挂
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ONE)) {

			// 卡片上删除挂失锁定码
			// CcsCard.setBlockCode(BlockCodeUtil.removeBlockCode(CcsCard.getBlockCode(),
			// req.getLost_reason().toUpperCase()));
			// CcsCardO.setBlockCode(BlockCodeUtil.removeBlockCode(CcsCardO.getBlockCode(),
			// req.getLost_reason().toUpperCase()));

			// 已做了挂失换卡的卡片当天或者隔天都不能再做解挂
			List<Map<String, Serializable>> cardRepals = mmCardService.MS3104(req.getCard_no());
			if (cardRepals != null && !cardRepals.isEmpty()) {
				for (Map<String, Serializable> map : cardRepals) {
					String cardNbr = DataTypeUtils.getStringValue(map.get("cardno"));
					String newCardNo = DataTypeUtils.getStringValue(map.get("newCardno"));
					// 由于刚做挂失换卡以后跑批之前卡号还没变还可以
					if (cardNbr.equals(req.getCard_no()) || newCardNo.equals(req.getCard_no())) {
						// 换卡时间
						Date bizDate = DataTypeUtils.getDateValue(map.get("bizDate"));
						// 置成卡片挂失换卡第三天的零点时间比如2013-10-30 00:00:00
						Date limitDate = DateUtils.truncate(DateUtils.addDays(bizDate, 2), Calendar.DATE);
						// 现在业务时间
						Date nowDate = unifiedParaFacilityProvide.BusinessDate();
						;
						if (!nowDate.before(limitDate)) {
							mmCardService.MS3205(req.getCard_no());
							// 发短信
							common.sendMsg(CcsCard.getProductCd(), CcsCard.getCustId(), req.getCard_no(), CPSMessageCategory.CPS056);
							break;
						} else {
							throw new ProcessException(Constants.ERRB106_CODE, Constants.ERRB106_MES);
						}

					}

				}
			} else {
				// 未做过换卡的直接解挂
				mmCardService.MS3205(req.getCard_no());
				// 发短信
				common.sendMsg(CcsCard.getProductCd(), CcsCard.getCustId(), req.getCard_no(), CPSMessageCategory.CPS056);
			}
		}
		LogTools.printLogger(logger, "S14050", "卡片挂失/解挂", null, false);
	}
}
