package com.sunline.ccs.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.service.api.OperateService;
import com.sunline.ccs.service.handler.sub.TNMAddress;
import com.sunline.ccs.service.handler.sub.TNMBillingCycle;
import com.sunline.ccs.service.handler.sub.TNMCardActive;
import com.sunline.ccs.service.handler.sub.TNMCardDeliver;
import com.sunline.ccs.service.handler.sub.TNMCardFrozen;
import com.sunline.ccs.service.handler.sub.TNMCardLimit;
import com.sunline.ccs.service.handler.sub.TNMCardLost;
import com.sunline.ccs.service.handler.sub.TNMCardTempLost;
import com.sunline.ccs.service.handler.sub.TNMCashLimitRate;
import com.sunline.ccs.service.handler.sub.TNMCrLimit;
import com.sunline.ccs.service.handler.sub.TNMCrLimitWithReview;
import com.sunline.ccs.service.handler.sub.TNMCustInfo;
import com.sunline.ccs.service.handler.sub.TNMDirectDebit;
import com.sunline.ccs.service.handler.sub.TNMLinkMan;
import com.sunline.ccs.service.handler.sub.TNMLoanLimitRate;
import com.sunline.ccs.service.handler.sub.TNMMessageInd;
import com.sunline.ccs.service.handler.sub.TNMModifyPIN;
import com.sunline.ccs.service.handler.sub.TNMNewPIN;
import com.sunline.ccs.service.handler.sub.TNMSecureQA;
import com.sunline.ccs.service.handler.sub.TNMStmtAddress;
import com.sunline.ccs.service.handler.sub.TNMStmtMedia;
import com.sunline.ccs.service.handler.sub.TNMTempCrLimit;
import com.sunline.ccs.service.handler.sub.TNMUnlockCardPIN;
import com.sunline.ccs.service.handler.sub.TNMUnlockPIN;
import com.sunline.ccs.service.handler.sub.TNMVerifyPIN;
import com.sunline.ccs.service.handler.sub.TNQCycleDayList;
import com.sunline.ccs.service.handler.sub.TNRCardChange;
import com.sunline.ccs.service.handler.sub.TNRCardClose;
import com.sunline.ccs.service.handler.sub.TNRCardFeeReturn;
import com.sunline.ccs.service.handler.sub.TNRCardFeeWaive;
import com.sunline.ccs.service.handler.sub.TNRStmtReprint;
import com.sunline.ccs.service.handler.sub.TNVBaseInfo;
import com.sunline.ccs.service.handler.sub.TNVThirdPayOpen;
import com.sunline.ccs.service.nfcontrol.ServiceControl;
import com.sunline.ccs.service.protocol.S11010Req;
import com.sunline.ccs.service.protocol.S11010Resp;
import com.sunline.ccs.service.protocol.S11030Req;
import com.sunline.ccs.service.protocol.S11030Resp;
import com.sunline.ccs.service.protocol.S11040Req;
import com.sunline.ccs.service.protocol.S11040Resp;
import com.sunline.ccs.service.protocol.S11050Req;
import com.sunline.ccs.service.protocol.S11050Resp;
import com.sunline.ccs.service.protocol.S12020Req;
import com.sunline.ccs.service.protocol.S12020Resp;
import com.sunline.ccs.service.protocol.S12021Req;
import com.sunline.ccs.service.protocol.S12021Resp;
import com.sunline.ccs.service.protocol.S12030Req;
import com.sunline.ccs.service.protocol.S12040Req;
import com.sunline.ccs.service.protocol.S12040Resp;
import com.sunline.ccs.service.protocol.S12041Req;
import com.sunline.ccs.service.protocol.S12041Resp;
import com.sunline.ccs.service.protocol.S12100Req;
import com.sunline.ccs.service.protocol.S12100Resp;
import com.sunline.ccs.service.protocol.S12101Req;
import com.sunline.ccs.service.protocol.S12101Resp;
import com.sunline.ccs.service.protocol.S12110Req;
import com.sunline.ccs.service.protocol.S12110Resp;
import com.sunline.ccs.service.protocol.S12111Req;
import com.sunline.ccs.service.protocol.S12111Resp;
import com.sunline.ccs.service.protocol.S14040Req;
import com.sunline.ccs.service.protocol.S14040Resp;
import com.sunline.ccs.service.protocol.S14050Req;
import com.sunline.ccs.service.protocol.S14051Req;
import com.sunline.ccs.service.protocol.S14070Req;
import com.sunline.ccs.service.protocol.S14080Req;
import com.sunline.ccs.service.protocol.S14080Resp;
import com.sunline.ccs.service.protocol.S14090Req;
import com.sunline.ccs.service.protocol.S14091Req;
import com.sunline.ccs.service.protocol.S14092Req;
import com.sunline.ccs.service.protocol.S14100Req;
import com.sunline.ccs.service.protocol.S14120Req;
import com.sunline.ccs.service.protocol.S14170Req;
import com.sunline.ccs.service.protocol.S14170Resp;
import com.sunline.ccs.service.protocol.S14180Req;
import com.sunline.ccs.service.protocol.S15010Req;
import com.sunline.ccs.service.protocol.S15010Resp;
import com.sunline.ccs.service.protocol.S15011Req;
import com.sunline.ccs.service.protocol.S15011Resp;
import com.sunline.ccs.service.protocol.S15012Req;
import com.sunline.ccs.service.protocol.S15030Req;
import com.sunline.ccs.service.protocol.S15030Resp;
import com.sunline.ccs.service.protocol.S15050Req;
import com.sunline.ccs.service.protocol.S15050Resp;
import com.sunline.ccs.service.protocol.S15051Req;
import com.sunline.ccs.service.protocol.S15051Resp;
import com.sunline.ccs.service.protocol.S16040Req;
import com.sunline.ccs.service.protocol.S16050Req;
import com.sunline.ccs.service.protocol.ServiceControlResult;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：OperateServiceImpl
 * @see 描述：非金融后台服务管理类交易接口
 *
 * @see 创建日期： 2015年6月24日 下午6:51:22
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service("nfOperateServiceImpl")
public class OperateServiceImpl implements OperateService {
    public Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public GlobalManagementService globalManagementService;
    @Autowired
    public CustAcctCardFacility custAcctCardQueryFacility;
    @Autowired
    private ServiceControl serviceControl;
    
    @Autowired
    private TNMCardTempLost tnmCardTempLost;
    @Autowired
    private TNVThirdPayOpen tnvThirdPayOpen;
    @Autowired
    private TNMCardLimit tnmCardLimit;
    @Autowired
    private TNMSecureQA tnmSecureQA;
    @Autowired
    private TNMStmtMedia tnmStmtMedia;
    @Autowired
    private TNRCardFeeReturn tnrCardFeeReturn;
    @Autowired
    private TNMDirectDebit tnmDirectDebit;
    @Autowired
    private TNMBillingCycle tnmBillingCycle;
    @Autowired
    private TNMCardActive tnmCardActive;
    @Autowired
    private TNRCardChange tnrCardChange;
    @Autowired
    private TNMModifyPIN tnmModifyPIN;
    @Autowired
    private TNMUnlockCardPIN tnmUnlockCardPIN;
    @Autowired
    private TNMCardDeliver tnmCardDeliver;
    @Autowired
    private TNMTempCrLimit tnmTempCrLimit;
    @Autowired
    private TNMCrLimitWithReview tnmCrLimitWithReview;
    @Autowired
    private TNMLoanLimitRate tnmLoanLimitRate;
    @Autowired
    private TNMVerifyPIN tnmVerifyPIN;
    @Autowired
    private TNMStmtAddress tnmStmtAddress;
    @Autowired
    private TNRStmtReprint tnrStmtReprint;
    @Autowired
    private TNRCardFeeWaive tnrCardFeeWaive;
    @Autowired
    private TNMMessageInd tnmMessageInd;
    @Autowired
    private TNQCycleDayList tnqCycleDayList;
    @Autowired
    private TNMCardLost tnmCardLost;
    @Autowired
    private TNMNewPIN tnmNewPIN;
    @Autowired
    private TNMUnlockPIN tnmUnlockPIN;
    @Autowired
    private TNRCardClose tnrCardClose;
    @Autowired
    private TNMCardFrozen tnmCardFrozen;
    @Autowired
    private TNMCrLimit tnmCrLimit;
    @Autowired
    private TNMCashLimitRate tnmCashLimitRate;
    @Autowired
    private TNVBaseInfo tnvBaseInfo;
    @Autowired
    private TNMLinkMan tnmLinkMan;
    @Autowired
    private TNMCustInfo tnmCustInfo;
    @Autowired
    private TNMAddress tnmAddress;

    
    @Override
	public ServiceControlResult serviceControl(String servCode, String cardNbr) throws ProcessException {
		LogTools.printLogger(logger, "serviceControl..", "服务控制调用", servCode + ":" + cardNbr, true);
		ServiceControlResult result = serviceControl.processControl(servCode, cardNbr);
		LogTools.printLogger(logger, "serviceControl..", "服务控制调用", result, false);
		return result;
	}
    
	// 客户基本资料查询/维护
	@Override
	public S11010Resp S11010(S11010Req req) throws ProcessException {
		return tnmCustInfo.handler(req);
	}

	// 客户地址信息查询/维护
	@Override
	public S11030Resp S11030(S11030Req req) throws ProcessException {
		return tnmAddress.handler(req);
	}

	// 客户联系人信息查询/维护
	@Override
	public S11040Resp S11040(S11040Req req) throws ProcessException {
		return tnmLinkMan.handler(req);
	}

	// 预留问题答案维护
	@Override
	public S11050Resp S11050(S11050Req req) throws ProcessException {
		return tnmSecureQA.handler(req);
	}

	// 账单寄送地址设定
	@Override
	public S12020Resp S12020(S12020Req req) throws ProcessException {
		return tnmStmtAddress.handler(req);
	}

	// 账单介质设定
	@Override
	public S12021Resp S12021(S12021Req req) throws ProcessException {
		return tnmStmtMedia.handler(req);
	}

	// 补打账单
	@Override
	public void S12030(S12030Req req) throws ProcessException {
		tnrStmtReprint.handler(req);
	}

	// 未收年费减免
	@Override
	public S12040Resp S12040(S12040Req req) throws ProcessException {
		return tnrCardFeeWaive.handler(req);
	}

	// 已收年费减免
	@Override
	public S12041Resp S12041(S12041Req req) throws ProcessException {
		return tnrCardFeeReturn.handler(req);
	}

	// 约定还款签约/取消
	@Override
	public S12100Resp S12100(S12100Req req) throws ProcessException {
		return tnmDirectDebit.handler(req);
	}

	// 发送短信设置
	@Override
	public S12101Resp S12101(S12101Req req) throws ProcessException {
		return tnmMessageInd.handler(req);
	}

	// 账单日修改
	@Override
	public S12110Resp S12110(S12110Req req) throws ProcessException {
		return tnmBillingCycle.handler(req);
	}

	// 账单日列表查询
	@Override
	public S12111Resp S12111(S12111Req req) throws ProcessException {
		return tnqCycleDayList.handler(req);
	}

	// 卡片激活
	@Override
	public S14040Resp S14040(S14040Req req) throws ProcessException {
		return tnmCardActive.handler(req);
	}

	// 卡片挂失/解挂
	@Override
	public void S14050(S14050Req req) throws ProcessException {
		tnmCardLost.handler(req);
	}

	// 卡片临时挂失/解挂
	@Override
	public void S14051(S14051Req req) throws ProcessException {
		tnmCardTempLost.handler(req);
	}

	// 挂失换卡/损坏换卡
	@Override
	public void S14070(S14070Req req) throws ProcessException {
		tnrCardChange.handler(req);
	}

	// 消费凭密设定
	@Override
	public S14080Resp S14080(S14080Req req) throws ProcessException {
		return tnmVerifyPIN.handler(req);
	}

	// 查询密码/交易密码的设置
	@Override
	public void S14090(S14090Req req) throws ProcessException {
		tnmNewPIN.handler(req);
	}

	// 查询密码/交易密码的修改
	@Override
	public void S14091(S14091Req req) throws ProcessException {
		tnmModifyPIN.handler(req);
	}

	// 客服/内管解锁服务
	@Override
	public void S14092(S14092Req req) throws ProcessException {
		tnmUnlockPIN.handler(req);
	}

	// 卡片交易/查询密码锁定解除
	@Override
	public void S14100(S14100Req req) throws ProcessException {
		tnmUnlockCardPIN.handler(req);
	}

	// 销卡/销卡撤销
	@Override
	public void S14120(S14120Req req) throws ProcessException {
		tnrCardClose.handler(req);
	}

	// 卡片寄送地址设定
	@Override
	public S14170Resp S14170(S14170Req req) throws ProcessException {
		return tnmCardDeliver.handler(req);
	}

	// 冻结/解冻
	@Override
	public void S14180(S14180Req req) throws ProcessException {
		tnmCardFrozen.handler(req);
	}

	// 临时额度设定
	@Override
	public S15010Resp S15010(S15010Req req) throws ProcessException {
		return tnmTempCrLimit.handler(req);
	}

	// 永久额度调整（直接调整，仅限内管使用）
	@Override
	public S15011Resp S15011(S15011Req req) throws ProcessException {
		return tnmCrLimit.handler(req);
	}

	// 永久额度调整（带复核）
	@Override
	public void S15012(S15012Req req) throws ProcessException {
		tnmCrLimitWithReview.handler(req);
	}

	// 卡片限额设定
	@Override
	public S15030Resp S15030(S15030Req req) throws ProcessException {
		return tnmCardLimit.handler(req);
	}

	// 取现额度比例设定
	@Override
	public S15050Resp S15050(S15050Req req) throws ProcessException {
		return tnmCashLimitRate.handler(req);
	}

	// 分期额度比例设定
	@Override
	public S15051Resp S15051(S15051Req req) throws ProcessException {
		return tnmLoanLimitRate.handler(req);
	}

	// 账户信息验证
	@Override
	public void S16040(S16040Req req) throws ProcessException {
		tnvBaseInfo.handler(req);
	}

	// 第三方快捷支付开通验证
	@Override
	public void S16050(S16050Req req) throws ProcessException {
		tnvThirdPayOpen.handler(req);
	}


}
