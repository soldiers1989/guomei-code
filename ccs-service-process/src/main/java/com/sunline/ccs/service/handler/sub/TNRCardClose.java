package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardCloseReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardCloseReg;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14120Req;
import com.sunline.ccs.service.util.BlockCodeUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.RequestType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRCardClose
 * @see 描述： 销卡/销卡撤销
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRCardClose {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private Common common;

	@Autowired
	private RCcsCardCloseReg rCcsCardCloseReg;

	/**
	 * @see 方法名：handler
	 * @see 描述：销卡/销卡撤销handler
	 * @see 创建日期：2015年6月25日下午6:13:47
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional(rollbackFor = Exception.class)
	public void handler(S14120Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14120", "销卡/销卡撤销", req, true);

		CheckUtil.checkCardNo(req.getCard_no());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}
		// 销卡
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ZERO)) {
			S14120_OPT_ZERO(req.getCard_no());
		}
		// 销卡撤销
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ONE)) {
			S14120_OPT_ONE(req.getCard_no());
		}
		LogTools.printLogger(logger, "S14120", "销卡/销卡撤销", null, false);
	}

	/**
	 * @see 方法名：S14120_OPT_ZERO
	 * @see 描述：销卡销户
	 * @see 创建日期：2015年6月25日下午6:14:31
	 * @author yanjingfeng
	 * 
	 * @param cardNbr
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void S14120_OPT_ZERO(String cardNbr) throws ProcessException {
		CheckUtil.checkCardNo(cardNbr);
		ArrayList<String> list = new ArrayList<String>();
		CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// 检查锁定码是否包含C，如果是C，抛异常 "已为销卡状态，无需再设"
		try {
			BlockCodeUtil.isCanAddBlockCode_C(CcsCard.getBlockCode());
			BlockCodeUtil.isCanAddBlockCode_C(CcsCardO.getBlockCode());
		} catch (ProcessException e) {
			logger.error(e.getMessage());
			throw new ProcessException(Constants.ERRB053_CODE, Constants.ERRB053_MES);
		}

		CcsCard.setBlockCode(BlockCodeUtil.addBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C));
		CcsCardO.setBlockCode(BlockCodeUtil.addBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C));
		CcsCard.setCloseDate(unifiedParaFacilityProvide.BusinessDate());
		list.add(cardNbr);
		// 保存tt_calcel_reg数据
		saveTtCancelReg_C(cardNbr, RequestType.A, CcsCard);
		// 判断是否主卡,如果是主卡给所有的附卡的都加上对应的销卡锁定码
		if (CcsCard.getBscSuppInd().equals(BscSuppIndicator.B)) {
			list = addCSUPPCcsCard(cardNbr, list);
		}
		// 判定账户下所有卡片是否都已为销卡状态
		if (hasCAllCard(CcsCard.getAcctNbr())) {
			// 执行到这里，意味着所有的卡片都已经为销卡状态，需要执行销户动作
			addCAccount(CcsCard, cardNbr);
		}

		// 调用mps的销卡服务；
		// mmCardService.MS3202(list);

	}

	/**
	 * @see 方法名：S14120_OPT_ONE
	 * @see 描述：销卡撤销
	 * @see 创建日期：2015年6月25日下午6:14:44
	 * @author yanjingfeng
	 * 
	 * @param cardNbr
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void S14120_OPT_ONE(String cardNbr) throws ProcessException {
		CheckUtil.checkCardNo(cardNbr);

		CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 判断是否能销卡撤销
		try {
			BlockCodeUtil.isCanCancel(CcsCard.getBlockCode());
			BlockCodeUtil.isCanCancel(CcsCardO.getBlockCode());
		} catch (ProcessException e1) {
			logger.error(e1.getMessage());
			throw new ProcessException(Constants.ERRB052_CODE, Constants.ERRB052_MES);
		}

		ArrayList<String> list = new ArrayList<String>();

		// 主卡
		if (CcsCard.getBscSuppInd().equals(BscSuppIndicator.B)) {
			S14120_OPT_ONE_B(cardNbr, CcsCard, CcsCardO, list);

		} else if (CcsCard.getBscSuppInd().equals(BscSuppIndicator.S)) {
			// 附卡
			S14120_OPT_ONE_S(CcsCard, CcsCardO, list);
		}
		// 调用mps的销卡撤销
		// mmCardService.MS3203(list);

	}

	/**
	 * @see 方法名：S14120_OPT_ONE_B
	 * @see 描述：主卡销卡撤销
	 * @see 创建日期：2015年6月25日下午6:14:55
	 * @author yanjingfeng
	 * 
	 * @param cardNbr
	 * @param ccsCard
	 * @param ccsCardO
	 * @param list
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void S14120_OPT_ONE_B(String cardNbr, CcsCard ccsCard, CcsCardO ccsCardO, ArrayList<String> list) throws ProcessException {

		// 如果为主卡
		if (!BlockCodeUtil.hasBlockCode(ccsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
			throw new ProcessException(Constants.ERRB009_CODE, Constants.ERRB009_MES);
		}
		if (!BlockCodeUtil.hasBlockCode(ccsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
			throw new ProcessException(Constants.ERRB009_CODE, Constants.ERRB009_MES);
		}
		ccsCard.setBlockCode(BlockCodeUtil.removeBlockCode(ccsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C));
		ccsCardO.setBlockCode(BlockCodeUtil.removeBlockCode(ccsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C));
		saveTtCancelReg_C(cardNbr, RequestType.B, ccsCard);
		list.add(cardNbr);

		List<CcsAcct> CcsAcctList = queryFacility.getAcctByAcctNbr(ccsCard.getAcctNbr());
		List<CcsAcctO> CcsAcctOList = queryFacility.getAcctOByAcctNbr(ccsCardO.getAcctNbr());
		for (CcsAcct CcsAcct : CcsAcctList) {
			if (BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_P)) {
				throw new ProcessException(Constants.ERRB010_CODE, Constants.ERRB010_MES);
			}
			if (BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
				CcsAcct.setBlockCode(BlockCodeUtil.removeBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_C));
				saveTtCancelReg_A(RequestType.D, CcsAcct, cardNbr);
			}
		}
		for (CcsAcctO CcsAcctO : CcsAcctOList) {
			if (BlockCodeUtil.hasBlockCode(CcsAcctO.getBlockCode(), CcServProConstants.BLOCKCODE_P)) {
				throw new ProcessException(Constants.ERRB010_CODE, Constants.ERRB010_MES);
			}
			if (BlockCodeUtil.hasBlockCode(CcsAcctO.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
				CcsAcctO.setBlockCode(BlockCodeUtil.removeBlockCode(CcsAcctO.getBlockCode(), CcServProConstants.BLOCKCODE_C));
			}
		}

	}

	/**
	 * @see 方法名：S14120_OPT_ONE_S
	 * @see 描述：副卡销卡撤销
	 * @see 创建日期：2015年6月25日下午6:15:21
	 * @author yanjingfeng
	 * 
	 * @param ccsCard
	 * @param ccsCardO
	 * @param list
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void S14120_OPT_ONE_S(CcsCard ccsCard, CcsCardO ccsCardO, ArrayList<String> list) throws ProcessException {
		// 如果为附卡
		// 根据逻辑卡主卡卡号获取主卡信息
		CcsCard bscCcsCard = queryFacility.getCardByLogicCardNbr(ccsCard.getCardBasicNbr());
		CheckUtil.rejectNull(bscCcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		CcsCardO bscCcsCardO = queryFacility.getCardOByLogicCardNbr(ccsCard.getCardBasicNbr());
		CheckUtil.rejectNull(bscCcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// *主卡销卡状态，则附卡不许做销卡撤销*
		if (BlockCodeUtil.hasBlockCode(bscCcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
			throw new ProcessException(Constants.ERRB011_CODE, Constants.ERRB011_MES);
		}
		if (BlockCodeUtil.hasBlockCode(bscCcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
			throw new ProcessException(Constants.ERRB011_CODE, Constants.ERRB011_MES);
		}
		ccsCard.setBlockCode(BlockCodeUtil.removeBlockCode(ccsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C));
		ccsCardO.setBlockCode(BlockCodeUtil.removeBlockCode(ccsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C));
		saveTtCancelReg_C(ccsCard.getLogicCardNbr(), RequestType.B, ccsCard);
		list.add(ccsCard.getLastestMediumCardNbr());
	}

	/**
	 * @see 方法名：addCSUPPCcsCard
	 * @see 描述：给所有附卡增加锁定码
	 * @see 创建日期：2015年6月25日下午6:15:47
	 * @author yanjingfeng
	 * 
	 * @param cardNbr
	 * @param list
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private ArrayList<String> addCSUPPCcsCard(String cardNbr, ArrayList<String> list) throws ProcessException {
		// 查询所有附卡
		List<CcsCard> CcsCardList = queryFacility.getSuppCardByCardNbr(cardNbr);
		for (CcsCard suppCcsCard : CcsCardList) {
			if (!BlockCodeUtil.hasBlockCode(suppCcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
				suppCcsCard.setBlockCode(BlockCodeUtil.addBlockCode(suppCcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C));
				saveTtCancelReg_C(suppCcsCard.getLogicCardNbr(), RequestType.A, suppCcsCard);
				list.add(suppCcsCard.getLogicCardNbr());
			}
		}
		List<CcsCardO> CcsCardOList = queryFacility.getSuppCardOByCardNbr(cardNbr);
		for (CcsCardO suppCcsCardO : CcsCardOList) {
			if (!BlockCodeUtil.hasBlockCode(suppCcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
				suppCcsCardO.setBlockCode(BlockCodeUtil.addBlockCode(suppCcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C));
			}
		}

		return list;
	}

	/**
	 * @see 方法名：saveTtCancelReg_C
	 * @see 描述：保存销卡请求
	 * @see 创建日期：2015年6月25日下午6:15:58
	 * @author yanjingfeng
	 * 
	 * @param cardNbr
	 * @param rt
	 * @param ccsCard
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void saveTtCancelReg_C(String cardNbr, RequestType rt, CcsCard ccsCard) {
		CcsCardCloseReg cardCloseReg = new CcsCardCloseReg();
		cardCloseReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		cardCloseReg.setAcctNbr(ccsCard.getAcctNbr());
		cardCloseReg.setCardNbr(cardNbr);
		cardCloseReg.setRequestType(rt);
		cardCloseReg.setLogicCardNbr(ccsCard.getLogicCardNbr());
		cardCloseReg.setLogBizDate(unifiedParaFacilityProvide.BusinessDate());
		cardCloseReg.setRequestTime(new Date());
		rCcsCardCloseReg.save(cardCloseReg);
	}

	/**
	 * @see 方法名：addCAccount
	 * @see 描述：销户，给所用的账户增加锁定码C，并记录销卡的记录
	 * @see 创建日期：2015年6月25日下午6:16:17
	 * @author yanjingfeng
	 * 
	 * @param ccsCard
	 * @param cardNbr
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void addCAccount(CcsCard ccsCard, String cardNbr) throws ProcessException {
		List<CcsAcct> CcsAcctList = queryFacility.getAcctByAcctNbr(ccsCard.getAcctNbr());
		for (CcsAcct CcsAcct : CcsAcctList) {
			if (isCloseAccount(CcsAcct)) {
				CcsAcct.setBlockCode(BlockCodeUtil.addBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_C));
				CcsAcct.setCloseDate(unifiedParaFacilityProvide.BusinessDate());
				saveTtCancelReg_A(RequestType.C, CcsAcct, cardNbr);
			} else {
				throw new ProcessException(Constants.ERRB008_CODE, Constants.ERRB008_MES);
			}
		}
		List<CcsAcctO> CcsAcctOList = queryFacility.getAcctOByAcctNbr(ccsCard.getAcctNbr());
		for (CcsAcctO CcsAcctO : CcsAcctOList) {
			if (isCloseAccount(CcsAcctO)) {
				CcsAcctO.setBlockCode(BlockCodeUtil.addBlockCode(CcsAcctO.getBlockCode(), CcServProConstants.BLOCKCODE_C));
			} else {
				throw new ProcessException(Constants.ERRB008_CODE, Constants.ERRB008_MES);
			}
		}

		// 销户成功的短信提醒
		common.sendMsg(ccsCard.getProductCd(), ccsCard.getCustId(), cardNbr, CPSMessageCategory.CPS046);
	}

	/**
	 * @see 方法名：isCloseAccount
	 * @see 描述：账户无销户锁定码<br>
	 *      且<br>
	 *      账户的当前CURR_BAL为0<br>
	 *      取现余额CASH_BAL为0<br>
	 *      额度内分期余额LOAN_BAL为0<br>
	 *      争议金额DISPUTE_AMT为0<br>
	 *      未匹配取现金额UNMATCH_CASH为0<br>
	 *      未匹配贷记金额UNMATCH_CR为0<br>
	 *      未匹配借记金额UNMATCH_DB为0<br>
	 * @see 创建日期：2015年6月25日下午6:16:36
	 * @author yanjingfeng
	 * 
	 * @param ccsAcctO
	 * @return true:可以对账户进行销户，false 不能对账户销户
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private boolean isCloseAccount(CcsAcctO ccsAcctO) {
		return ccsAcctO.getCurrBal().doubleValue() <= 0 && ccsAcctO.getCashBal().doubleValue() <= 0 && ccsAcctO.getLoanBal().doubleValue() <= 0
				&& ccsAcctO.getDisputeAmt().doubleValue() == 0 && ccsAcctO.getMemoCash().doubleValue() == 0 && ccsAcctO.getMemoCr().doubleValue() == 0
				&& ccsAcctO.getMemoDb().doubleValue() == 0;
	}

	/**
	 * @see 方法名：saveTtCancelReg_A
	 * @see 描述：保存销户请求
	 * @see 创建日期：2015年6月25日下午6:17:25
	 * @author yanjingfeng
	 * 
	 * @param rt
	 * @param ccsAcct
	 * @param cardNbr
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private void saveTtCancelReg_A(RequestType rt, CcsAcct ccsAcct, String cardNbr) {
		CcsCardCloseReg tmCancelReg = new CcsCardCloseReg();
		tmCancelReg.setOrg(OrganizationContextHolder.getCurrentOrg());
		tmCancelReg.setAcctNbr(ccsAcct.getAcctNbr());
		tmCancelReg.setAcctType(ccsAcct.getAcctType());
		tmCancelReg.setCardNbr(cardNbr);
		tmCancelReg.setRequestType(rt);
		tmCancelReg.setLogicCardNbr(ccsAcct.getDefaultLogicCardNbr());
		tmCancelReg.setLogBizDate(unifiedParaFacilityProvide.BusinessDate());
		tmCancelReg.setRequestTime(new Date());
		rCcsCardCloseReg.save(tmCancelReg);
	}

	/**
	 * @see 方法名：hasCAllCard
	 * @see 描述： 判定账户下所有卡片是否都已为销卡状态,判断条件，只要账号下所有的卡片中有一个锁定码中不包含"C"就返回false
	 * @see 创建日期：2015年6月25日下午6:17:38
	 * @author yanjingfeng
	 * 
	 * @param long1
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private boolean hasCAllCard(Long long1) {
		List<CcsCard> CcsCardList = queryFacility.getCardListByAcctNbr(long1);
		// 销户标志
		boolean isCancel = true;
		for (CcsCard tc : CcsCardList) {
			if (BlockCodeUtil.hasNoBlockCode(tc.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
				isCancel = false;
				break;
			}
		}
		return isCancel;
	}

	/**
	 * @see 方法名：isCloseAccount
	 * @see 描述：账户无销户锁定码且当前CURR_BAL<0、取现余额CASH_BAL<0、
	 *      额度内分期余额LOAN_BAL<0、争议金额DISPUTE_AMT为0
	 * @see 创建日期：2015年6月25日下午6:17:51
	 * @author yanjingfeng
	 * 
	 * @param ccsAcct
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private boolean isCloseAccount(CcsAcct ccsAcct) {
		return ccsAcct.getCurrBal().doubleValue() <= 0 && ccsAcct.getCashBal().doubleValue() <= 0 && ccsAcct.getLoanBal().doubleValue() <= 0
				&& ccsAcct.getDisputeAmt().doubleValue() == 0;
	}

}
