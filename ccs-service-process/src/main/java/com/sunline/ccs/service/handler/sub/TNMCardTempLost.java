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
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14051Req;
import com.sunline.ccs.service.util.BlockCodeUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DataTypeUtils;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMCardTempLost
 * @see 描述：卡片临时挂失/解挂
 *
 * @see 创建日期： 2015年6月24日 下午3:31:51
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMCardTempLost {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private Common common;
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	@Transactional
	public void handler(S14051Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14051", "卡片临时挂失/解挂", req, true);
		CheckUtil.checkCardNo(req.getCard_no());

		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		CcsCardO CcsCardO = custAcctCardQueryFacility.getCardOByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 临时卡片挂失
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ZERO)) {
			// 因为封装的方法中有是否集合为空的校验，这里就不做校验
			List<CcsAcct> accounts = custAcctCardQueryFacility.getAcctByCardNbr(req.getCard_no());
			for (CcsAcct CcsAcct : accounts) {
				if (BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_C))
					throw new ProcessException(Constants.ERRB096_CODE, Constants.ERRB096_MES);
			}
			if (BlockCodeUtil.hasBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
				throw new ProcessException(Constants.ERRB096_CODE, Constants.ERRB096_MES);
			}
			if (BlockCodeUtil.hasBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
				throw new ProcessException(Constants.ERRB096_CODE, Constants.ERRB096_MES);
			}
			mmCardService.MS3212(req.getCard_no());
			// 杂项费用
			// TODO 以账户为准,这里暂时写死,只看本币账户
			CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), Constants.CURR_CD_156);
			if (req.getFee_ind() == Indicator.Y) {
				// 当账户中不免除服务费时才收取客服费
				if (CcsAcct.getWaiveSvcfeeInd() == Indicator.N) {
					common.cssfeeReg(req.getCard_no(), "S14051");
				}
			}
		}
		// 卡片解挂
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ONE)) {

			// 已做了挂失换卡的卡片当天或者隔天都不能再做解挂
			List<Map<String, Serializable>> cardRepals = mmCardService.MS3104(req.getCard_no());
			if (cardRepals != null && !cardRepals.isEmpty()) {
				for (Map<String, Serializable> map : cardRepals) {
					String cardNbr = DataTypeUtils.getStringValue(map.get("cardNbr"));
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
							mmCardService.MS3213(req.getCard_no());
							break;
						} else {
							throw new ProcessException(Constants.ERRB106_CODE, Constants.ERRB106_MES);
						}
					}
				}

			} else {
				// 未做过换卡的直接解挂
				mmCardService.MS3213(req.getCard_no());
			}
		}
		LogTools.printLogger(logger, "S14051", "卡片挂失/解挂", null, false);
	}

}
