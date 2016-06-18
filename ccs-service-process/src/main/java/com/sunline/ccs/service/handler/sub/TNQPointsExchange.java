package com.sunline.ccs.service.handler.sub;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsPointsReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsPointsUseLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsGiftGoods;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsUseLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsGiftGoods;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsUseLog;
import com.sunline.ccs.param.def.enums.SendInd;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S17020Req;
import com.sunline.ccs.service.protocol.S17020Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.ppy.dictionary.enums.GiftStatus;
import com.sunline.ppy.dictionary.enums.PointAdjustIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQPointsExchange
 * @see 描述：积分兑换
 *
 * @see 创建日期： 2015-6-25上午11:55:11
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQPointsExchange {
	@PersistenceContext
	public EntityManager em;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@Autowired
	private Common pointComm;
	@Autowired
	private RCcsPointsReg rCcsPointsReg;
	@Autowired
	private RCcsAddress rCcsAddress;
	@Autowired
	private RCcsPointsUseLog rCcsPointsUseLog;

	private Logger logger = LoggerFactory.getLogger(getClass());
	QCcsPointsUseLog qCcsPointsUseLog = QCcsPointsUseLog.ccsPointsUseLog;
	QCcsGiftGoods qCcsGiftGoods = QCcsGiftGoods.ccsGiftGoods;
	QCcsAddress qCcsAddress = QCcsAddress.ccsAddress;

	@Transactional
	public S17020Resp handler(S17020Req req) throws ProcessException {

		LogTools.printLogger(logger, "S17020", "积分兑换", req, true);
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), false);
		// 检查礼品编号是否为空
		if (StringUtils.isEmpty(req.getGift_nbr())) {
			throw new ProcessException(Constants.ERRB070_CODE, Constants.ERRB070_MES);
		}
		// 礼品数量不能为空，否则抛“需确定兑换数量”
		if (req.getItem_cnt() == null)
			throw new ProcessException(Constants.ERRB072_CODE, Constants.ERRB072_MES);

		// 客户自定义的寄送地址,其地址不能为空
		if (req.getSend_ind() == SendInd.C) {
			// 2014-6-12，去掉收件人座机号码不能为空的判断
			if (StringUtils.isEmpty(req.getReceive_address()) || StringUtils.isEmpty(req.getReceive_mobile()) || StringUtils.isEmpty(req.getReceive_name()))
				throw new ProcessException(Constants.ERRB087_CODE, Constants.ERRB087_MES);
			// 移动电话校验
			if (req.getReceive_mobile() != null) {
				if (!CheckUtil.isPhone(req.getReceive_mobile()))
					throw new ProcessException(Constants.ERRB028_CODE, Constants.ERRB028_MES);
			}

		}

		JPAQuery query = new JPAQuery(em);
		// 查询礼品所对应的积分
		CcsGiftGoods giftExch = query.from(qCcsGiftGoods).where(qCcsGiftGoods.giftNbr.eq(req.getGift_nbr())).singleResult(qCcsGiftGoods);

		// 查询不到礼品
		if (giftExch == null) {
			throw new ProcessException(Constants.ERRB082_CODE, Constants.ERRB082_MES);
		}

		// 判断礼品是否可以兑换
		if (giftExch.getGiftStatus() == GiftStatus.N)
			throw new ProcessException(Constants.ERRB073_CODE, Constants.ERRB073_MES);

		// 判断当前业务日期是否在“礼品兑换开始日期”和“礼品兑换结束日期”内
		if (!DateTools.dateBetwen(giftExch.getRedeemBegDate(), unifiedParameterFacilityProvide.BusinessDate(), giftExch.getRedeemEndDate()))
			throw new ProcessException(Constants.ERRB074_CODE, Constants.ERRB074_MES);

		// 剩余礼品不足
		if (req.getItem_cnt() > giftExch.getGiftAvailibleCnt())
			throw new ProcessException(Constants.ERRB075_CODE, Constants.ERRB075_MES);

		// 若兑换礼品数量大于单笔最大可兑换数量，则返回“超过单笔最大可兑换数量
		if (req.getItem_cnt() > giftExch.getMaxCntPerTime())
			throw new ProcessException(Constants.ERRB076_CODE, Constants.ERRB076_MES);

		CcsAcct acct = null;
		acct = queryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		if (acct == null)
			throw new ProcessException(Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// 积分余额如果小于“单件礼品兑换积分”*兑换礼品数量，则返回“积分余额不足
		if (acct.getPointsBal().subtract(pointComm.countReducePoint(acct).add(pointComm.countAddPoint(acct))).intValue() < giftExch.getGiftBonus().intValue()
				* req.getItem_cnt()) {
			throw new ProcessException(Constants.ERRB071_CODE, Constants.ERRB071_MES);
		}
		// 登记积分兑换信息表
		CcsPointsReg tmPointReg = pointComm.subtractPoint(req.getCard_no(), acct, giftExch.getGiftBonus() * req.getItem_cnt(), PointAdjustIndicator.D, null);
		rCcsPointsReg.save(tmPointReg);
		// 修改积分礼品表
		// 礼品余量
		giftExch.setGiftAvailibleCnt(giftExch.getGiftAvailibleCnt() - req.getItem_cnt());
		// 已兑换礼品数量
		giftExch.setGiftUsedCnt(giftExch.getGiftUsedCnt() + req.getItem_cnt());

		// 组装返回报文
		S17020Resp resp = new S17020Resp();
		resp.setCard_no(req.getCard_no());
		resp.setCurr_cd(req.getCurr_cd());
		resp.setGift_nbr(req.getGift_nbr());
		resp.setItem_cnt(req.getItem_cnt());
		resp.setExch_type(req.getExch_type());
		resp.setSend_ind(req.getSend_ind());
		resp.setAddr_type(req.getAddr_type());
		// 如果是自定义的，返回上送报文的地址
		if (req.getSend_ind() == SendInd.C) {
			resp.setReceive_name(req.getReceive_name());
			resp.setReceive_mobile(req.getReceive_mobile());
			resp.setReceive_phone(req.getReceive_phone());
			resp.setReceive_address(req.getReceive_address());
		} else if (req.getSend_ind() == SendInd.U) {
			// 如果使用客户类型的话，返回地址类型对应的地址
			CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());
			CcsAddress address = rCcsAddress.findOne(qCcsAddress.custId.eq(ccsCustomer.getCustId()).and(qCcsAddress.addrType.eq(req.getAddr_type())));
			// 上送的地址类型无对应的地址
			if (address == null)
				throw new ProcessException(Constants.ERRB077_CODE, Constants.ERRB077_MES);
			resp.setReceive_name(ccsCustomer.getName());
			resp.setReceive_mobile(ccsCustomer.getMobileNo());
			resp.setReceive_phone(address.getPhone());
			StringBuffer sb = new StringBuffer();
			sb.append(address.getState());
			sb.append(address.getDistrict());
			sb.append(address.getCity());
			sb.append(address.getAddress());
			resp.setReceive_address(sb.toString());
		}
		// 兑换积分=单件礼品*兑换的总数
		resp.setExch_bonus(giftExch.getGiftBonus() * req.getItem_cnt());
		// 当期兑换积分
		resp.setCurr_exch_point(acct.getCtdSpendPoints().add(pointComm.countReducePoint(acct)).intValue());
		// 积分余额
		resp.setPoint_bal(acct.getPointsBal().subtract(pointComm.countReducePoint(acct)).intValue());
		resp.setItem_price(giftExch.getGiftPrice());

		// 记录兑换明细表
		CcsPointsUseLog pointUseLog = new CcsPointsUseLog();
		pointUseLog.setTxnCardNo(resp.getCard_no());
		pointUseLog.setGiftNbr(req.getGift_nbr());
		pointUseLog.setGiftName(giftExch.getGiftName());
		pointUseLog.setRedeemApplDate(unifiedParameterFacilityProvide.BusinessDate());
		pointUseLog.setGiftUsedCnt(resp.getItem_cnt());
		pointUseLog.setRedeemMethod(resp.getExch_type());
		pointUseLog.setPointsNeeded(resp.getExch_bonus());
		pointUseLog.setGiftPrice(resp.getItem_price());
		pointUseLog.setSendInd(resp.getSend_ind());
		pointUseLog.setAddrType(resp.getAddr_type());
		pointUseLog.setReceiveAddress(resp.getReceive_address());
		pointUseLog.setReceiveName(resp.getReceive_name());
		pointUseLog.setReceiveMobile(resp.getReceive_mobile());
		pointUseLog.setReceivePhone(resp.getReceive_phone());
		rCcsPointsUseLog.save(pointUseLog);

		LogTools.printLogger(logger, "S17020", "积分兑换", resp, false);
		return resp;

	}
}
