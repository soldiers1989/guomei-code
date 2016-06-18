package com.sunline.ccs.service.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.QueryRequest;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardCloseReg;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardLmMapping;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardCloseReg;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.param.def.enums.FirstCardFeeInd;
import com.sunline.ccs.service.process.bo.CardServiceBO;
import com.sunline.ccs.service.provide.CallOTBProvide;
import com.sunline.ccs.service.util.BlockCodeUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CcCardService;
import com.sunline.ppy.api.CcServProConstants;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.RequestType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ppy.dictionary.exchange.ApplyFileItem;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：CardServiceImpl
 * @see 描述：卡片类服务接口
 *
 * @see 创建日期： 2015年6月24日 下午2:47:23
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class CardServiceImpl implements CcCardService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    CustAcctCardFacility queryFacility;
    @Autowired
    RCcsCardO rCcsCardO;
    @Autowired
    RCcsCardCloseReg rCcsCardCloseReg;
    @Autowired
    private RCcsCustomer rCcsCustomer;
    @Autowired
    private CallOTBProvide callOTBProvide;
    @Autowired
    private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
    @Autowired
    private UnifiedParameterFacility unifiedParameterFacility;
/*    @Autowired
    private DownMsgFacility downMsgFacility;
*/    @Autowired
    private FetchSmsNbrFacility fetchMsgCdService;
    @Autowired
    private RCcsCardThresholdCtrl rCcsCardLmtOverideO;
    @Autowired
    private RCcsCardLmMapping rCcsCardLmMapping;
    @Autowired
    private MmCardService mmCardService;
    QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
    QCcsCard qCcsCard = QCcsCard.ccsCard;
    QCcsCardO qCcsCardO = QCcsCardO.ccsCardO;
    QCcsCardUsage qCcsCardUsage = QCcsCardUsage.ccsCardUsage;
    QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private UnifiedParamFacilityProvide cpsUnifiedParameterFacilityProvide;
    @Autowired
    private CardServiceBO cardServiceBo;

    // 根据证件号码，证件类型查询卡号列表
    @Override
    @Transactional
    public List<Map<String, Serializable>> NF3101(String idType, String idNo) throws ProcessException {
	log.info("NF3101:证件类型[" + idType + "],证件号码[" + CodeMarkUtils.markIDCard(idNo) + "]");
	if (!CheckUtil.isIdNo(IdType.valueOf(idType), idNo)) {
	    throw new ProcessException("非法的证件类型或证件号码");
	}
	List<CcsCard> CcsCardList = queryFacility.getCardById(idNo, IdType.valueOf(idType));
	if (CcsCardList.isEmpty()) {
	    log.error("证件类型[" + idType + "],证件号码[" + CodeMarkUtils.markIDCard(idNo) + "]查询不到对应的卡片信息");
	    throw new ProcessException("证件类型[" + idType + "],证件号码[" + idNo + "]查询不到对应的卡片信息");
	}
	ArrayList<String> logicCardNoList = new ArrayList<String>();
	for (CcsCard CcsCard : CcsCardList) {
	    logicCardNoList.add(CcsCard.getLogicCardNbr());
	}
	JPAQuery query = new JPAQuery(em);
	Iterator<CcsCardLmMapping> CcsCardLmMappingIter =
		query.from(qCcsCardLmMapping).where(qCcsCardLmMapping.logicCardNbr.in(logicCardNoList))
			.list(qCcsCardLmMapping).iterator();
	if (!CcsCardLmMappingIter.hasNext()) {
	    log.error("根据逻辑卡号查询不到对应的介质卡");
	    throw new ProcessException("根据逻辑卡号查询不到对应的介质卡");
	}
	ArrayList<Map<String, Serializable>> listCcsCardLmMappings = new ArrayList<Map<String, Serializable>>();
	while (CcsCardLmMappingIter.hasNext()) {
	    CcsCardLmMapping CcsCardLmMapping = CcsCardLmMappingIter.next();
	    listCcsCardLmMappings.add(CcsCardLmMapping.convertToMap());
	}
	return listCcsCardLmMappings;
    }

    @Override
    @Transactional
    @Deprecated
    public List<String> NF3102(QueryRequest queryRequest, String accountNo) {

	return null;
    }

    @Override
    @Transactional
    public List<Map<String, Serializable>> NF3103(IdType idType, String idNo) throws ProcessException {
	log.info("NF3103:证件类型[" + idType + "],证件号码[" + CodeMarkUtils.markIDCard(idNo) + "]");
	// 判断证件类型
	if (!CheckUtil.isIdNo(idType, idNo)) {
	    throw new ProcessException("非法的证件类型或证件号码");
	}
	List<CcsCard> CcsCardList = queryFacility.getCardById(idNo, idType);
	if (CcsCardList.isEmpty()) {
	    throw new ProcessException("证件类型[" + idType + "],证件号码[" + idNo + "]查询不到对应的卡片");
	}
	List<Map<String, Serializable>> CcsCardSList = new ArrayList<Map<String, Serializable>>();
	for (CcsCard CcsCard : CcsCardList) {
	    CcsCardUsage CcsCardUsage = queryFacility.getCardUsageBylogicCardNbr(CcsCard.getLogicCardNbr());
	    CheckUtil.rejectNull(CcsCardUsage, "查询不到对应的卡片信息");
	    CcsCardThresholdCtrl CcsCardLmtOver = rCcsCardLmtOverideO.findOne(CcsCard.getLogicCardNbr());
	    CcsCardSList.add(merageCcsCard(CcsCard.getLogicCardNbr(), CcsCard,
					   queryFacility.getCardOByLogicCardNbr(CcsCard.getLogicCardNbr()),
					   CcsCardUsage, CcsCardLmtOver));
	}
	return CcsCardSList;
    }

    // * CcsCard，CcsCardO，CcsCardUsage,qCcsCardThresholdCtrl数据
    public Map<String, Serializable> queryCardAll(String cardNbr) throws ProcessException {
	CheckUtil.checkCardNo(cardNbr);
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardUsage CcsCardUsage = queryFacility.getCardUsageByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardUsage, "卡号:[" + cardNbr + "]查询不到对应的数据");
	CcsCardThresholdCtrl CcsCardLmtOver = rCcsCardLmtOverideO.findOne(CcsCard.getLogicCardNbr());
	return merageCcsCard(cardNbr, CcsCard, CcsCardO, CcsCardUsage, CcsCardLmtOver);
    }

    @Override
    @Transactional
    public Map<String, Serializable> NF3104(String cardNbr) throws ProcessException {
	log.info("NF3101:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	return queryCardAll(cardNbr);
    }

    @Override
    @Transactional
    @Deprecated
    public Map<String, Serializable> NF3105(String logicCardNbr) {
	return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Transactional
    public void NF3203(String cardNbr, Map<String, Serializable> transLmt) throws ProcessException {
	log.info("NF3101:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	LogTools.printObj(log, transLmt, "NF3203:上送的交易信息");
	CheckUtil.checkCardNo(cardNbr);
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO.updateFromMap(transLmt);
	CcsCardThresholdCtrl CcsCardlimitO = new CcsCardThresholdCtrl();
	CcsCardlimitO.updateFromMap(transLmt);

	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	QCcsCardThresholdCtrl qCcsCardThresholdCtrl = QCcsCardThresholdCtrl.ccsCardThresholdCtrl;
	//
	String logicCardNbr = rCcsCardLmMapping.findOne(qCcsCardLmMapping.cardNbr.eq(cardNbr)).getLogicCardNbr();
	CcsCardThresholdCtrl CcsCardLmtOverideO =
		rCcsCardLmtOverideO.findOne(qCcsCardThresholdCtrl.logicCardNbr.eq(logicCardNbr));
	if (CcsCardLmtOverideO != null) {
	    CcsCardLmtOverideO.updateFromMap(transLmt);
	    CcsCardLmtOverideO.setLogicCardNbr(logicCardNbr);
	} else {
	    if (CcsCardlimitO.getDayAtmOvriInd() == Indicator.Y) {
		if (CcsCardlimitO.getDayAtmOvriBegDate() == null || CcsCardlimitO.getDayAtmOvriEndDate() == null
			|| CcsCardlimitO.getDayAtmAmtLmt() == null || CcsCardlimitO.getDayAtmNbrLmt() == null) throw new ProcessException(
			"ATM对应域不能为空");
	    }
	    if (CcsCardlimitO.getDayRetlOvriInd() == Indicator.Y) {
		if (CcsCardlimitO.getDayRetlOvriBegDate() == null || CcsCardlimitO.getDayRetlOvriEndDate() == null
			|| CcsCardlimitO.getDayRetailAmtLmt() == null || CcsCardlimitO.getDayRetailNbrLmt() == null) throw new ProcessException(
			"消费限制对应域不能为空");
	    }
	    if (CcsCardlimitO.getDayCashOvriInd() == Indicator.Y) {
		if (CcsCardlimitO.getDayCashOvriBegDate() == null || CcsCardlimitO.getDayCashOvriEndDate() == null
			|| CcsCardlimitO.getDayCashAmtLmt() == null || CcsCardlimitO.getDayCashNbrLmt() == null) throw new ProcessException(
			"取现限制对应域不能为空");
	    }
	    if (CcsCardlimitO.getDayXfroutOvriInd() == Indicator.Y) {
		if (CcsCardlimitO.getDayXfroutOvriBegDate() == null || CcsCardlimitO.getDayXfroutOvriEndDate() == null
			|| CcsCardlimitO.getDayXfroutAmtLmt() == null || CcsCardlimitO.getDayXfroutNbrLmt() == null) throw new ProcessException(
			"转出限制对应域不能为空");
	    }
	    if (CcsCardlimitO.getDayCupxbAtmOvriInd() == Indicator.Y) {
		if (CcsCardlimitO.getDayCupxbAtmOvriBegDate() == null
			|| CcsCardlimitO.getDayCupxbAtmOvriEndDate() == null
			|| CcsCardlimitO.getDayCupxbAtmAmtLmt() == null) throw new ProcessException("转出限制对应域不能为空");
	    }
	    CcsCardlimitO.setLogicCardNbr(logicCardNbr);
	    rCcsCardLmtOverideO.save(CcsCardlimitO);
	}

	// 卡片调整成功
	CcsCustomer customer = rCcsCustomer.findOne(CcsCardO.getCustId());
	// messageService.sendMessage(MessageCategory.L04,
	// CcsCardO.getProductCd(), cardNbr, customer.getName(),
	// customer.getGender(), customer.getMobileNo(), customer.getEmail(),
	// new Date(),
	// new MapBuilder().add("localCycleLmt",
	// CcsCardO.getCycleRetailLmt()).add("dualCycleLmt", null).build());
	String msgCd = fetchMsgCdService.fetchMsgCd(CcsCardO.getProductCd(), CPSMessageCategory.CPS031);
/*	downMsgFacility.sendMessage(msgCd, cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(),
				    new Date(), new MapBuilder().add("localCycleLmt", CcsCardO.getCycleRetailLmt())
					    .add("dualCycleLmt", null).build());
*/    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Transactional
    public void NF3204(String cardNbr, Indicator pinVerifyIndicator) throws ProcessException {
	log.info("NF3204:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],凭密标志[" + pinVerifyIndicator + "]");
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	String CcsCardBlockCode = CcsCard.getBlockCode();
	String CcsCardOBlockCode = CcsCardO.getBlockCode();
	if (!isSetPosPinVerifyInd(CcsCardBlockCode)) {
	    throw new ProcessException("设置失败：凭密设置授权未通过");
	}
	if (!isSetPosPinVerifyInd(CcsCardOBlockCode)) {
	    throw new ProcessException("设置失败：凭密设置授权未通过");
	}
	if (CcsCard.getPosPinVerifyInd().equals(pinVerifyIndicator)) {
	    throw new ProcessException("设置失败：当前状态与设置值一致，无需再设");
	}
	if (CcsCardO.getPosPinVerifyInd().equals(pinVerifyIndicator)) {
	    throw new ProcessException("设置失败：当前状态与设置值一致，无需再设");
	}
	CcsCard.setPosPinVerifyInd(pinVerifyIndicator);
	CcsCardO.setPosPinVerifyInd(pinVerifyIndicator);

	// 凭密标识变更短信
	CcsCustomer customer = rCcsCustomer.findOne(CcsCard.getCustId());
	// messageService.sendMessage(MessageCategory.M07,
	// CcsCard.getProductCd(), cardNbr, customer.getName(),
	// customer.getGender(), customer.getMobileNo(), customer.getEmail(),
	// new Date(), new MapBuilder().build());
	String msgCd = fetchMsgCdService.fetchMsgCd(CcsCard.getProductCd(), CPSMessageCategory.CPS007);
/*	downMsgFacility.sendMessage(msgCd, cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(),
				    new Date(), new MapBuilder().build());
 */   }

    /**
     * 判断是否可以设置凭密标志
     * 
     * @param blockCodeStr
     * @return boolean true 可以设置 false 不可以设置
     * @throws ProcessException
     * @exception
     * @since 1.0.0
     */
    private boolean isSetPosPinVerifyInd(String blockCodeStr) throws ProcessException {
	boolean flag = true;
	if (blockCodeStr != null && blockCodeStr.length() > 0) {
	    char[] cc = blockCodeStr.toCharArray();
	    for (char c : cc) {
		BlockCode blockCode = unifiedParaFacilityProvide.blockCode(String.valueOf(c));
		if (blockCode.nonMotoRetailAction != AuthAction.A) {
		    flag = false;
		}
	    }
	}
	return flag;
    }

    @Override
    @Transactional
    @Deprecated
    public void NF3401(String cardNbr) {

    }

    @Override
    @Transactional
    @Deprecated
    public void NF3402(String cardNbr) {

    }

    // 卡片激活
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Transactional
    public void NF3403(String cardNbr) throws ProcessException {
	log.info("NF3403:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	CheckUtil.checkCardNo(cardNbr);
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	if (CcsCard.getActiveInd().equals(Indicator.N) && CcsCardO.getActiveInd().equals(Indicator.N)) {
	    CcsCard.setActiveInd(Indicator.Y);
	    // 激活日期
	    CcsCard.setActiveDate(unifiedParaFacilityProvide.BusinessDate());
	    // 杂项费中的首次年费收取方式，如果是“激活收年费”才需要更新下次年费日期；否则不需要更新
	    if (unifiedParaFacilityProvide.productCredit(CcsCard.getProductCd()).fee.firstCardFeeInd
		    .equals(FirstCardFeeInd.A)) {
		CcsCard.setNextCardFeeDate(unifiedParaFacilityProvide.BusinessDate());
	    }
	    CcsCardO.setActiveInd(Indicator.Y);

	    // 激活成功短信
	    CcsCustomer customer = rCcsCustomer.findOne(CcsCard.getCustId());
	    String msgCd = fetchMsgCdService.fetchMsgCd(CcsCard.getProductCd(), CPSMessageCategory.CPS017);
/*	    downMsgFacility.sendMessage(msgCd, cardNbr, customer.getName(), customer.getGender(),
					customer.getMobileNo(), new Date(),
					new MapBuilder().add("posPinVerifyInd", CcsCard.getPosPinVerifyInd()).build());
*/	}
    }

    // 销卡销户
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void NF3404(String cardNbr) throws ProcessException {
	log.info("NF3404:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	CheckUtil.checkCardNo(cardNbr);
	ArrayList<String> al = new ArrayList<String>();
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	// 检查锁定吗是否包含C，如果是C，抛异常 "已为销卡状态，无需再设"
	BlockCodeUtil.isCanAddBlockCode_C(CcsCard.getBlockCode());
	BlockCodeUtil.isCanAddBlockCode_C(CcsCardO.getBlockCode());

	CcsCard.setBlockCode(BlockCodeUtil.addBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C));
	CcsCardO.setBlockCode(BlockCodeUtil.addBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C));
	CcsCard.setCloseDate(unifiedParaFacilityProvide.BusinessDate());
	al.add(cardNbr);
	// 保存tt_calcel_reg数据
	saveTtCancelReg_C(cardNbr, RequestType.A, CcsCard);
	// 判断是否主卡,如果是主卡给所有的附卡的都加上对应的销卡锁定码
	if (CcsCard.getBscSuppInd().equals(BscSuppIndicator.B)) {
	    al = addCSUPPCcsCard(cardNbr, al);
	}
	// 判定账户下所有卡片是否都已为销卡状态
	if (hasCAllCard(CcsCard.getAcctNbr())) {
	    // 执行到这里，意味着所有的卡片都已经为销卡状态，需要执行销户动作
	    addCAccount(CcsCard.getAcctNbr(), cardNbr);
	}

	// 调用mps的销卡服务；
	mmCardService.MS3202(al);
    }

    // 销卡撤销
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void NF3406(String cardNbr) throws ProcessException {
	log.info("NF3406:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	CheckUtil.checkCardNo(cardNbr);
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");

	// 判断是否能销卡撤销
	BlockCodeUtil.isCanCancel(CcsCard.getBlockCode());
	BlockCodeUtil.isCanCancel(CcsCardO.getBlockCode());

	ArrayList<String> al = new ArrayList<String>();

	// 主卡
	if (CcsCard.getBscSuppInd().equals(BscSuppIndicator.B)) {
	    NF3406_B(cardNbr, CcsCard, CcsCardO, al);

	} else if (CcsCard.getBscSuppInd().equals(BscSuppIndicator.S)) {
	    // 附卡
	    NF3406_S(CcsCard, CcsCardO, al);
	}
	// 调用mps的销卡撤销
	mmCardService.MS3203(al);

    }

    /**
     * 主卡销卡撤销
     * 
     * @param CcsCard
     * @param CcsCardO
     * @throws ProcessException
     */
    private void NF3406_B(String cardNbr, CcsCard CcsCard, CcsCardO CcsCardO, ArrayList<String> list)
	    throws ProcessException {

	// 如果为主卡
	if (!BlockCodeUtil.hasBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
	    throw new ProcessException("主卡为非销卡状态，无需撤销操作");
	}
	if (!BlockCodeUtil.hasBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
	    throw new ProcessException("主卡为非销卡状态，无需撤销操作");
	}
	CcsCard.setBlockCode(BlockCodeUtil.removeBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C));
	CcsCardO.setBlockCode(BlockCodeUtil.removeBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C));
	saveTtCancelReg_C(cardNbr, RequestType.B, CcsCard);
	list.add(cardNbr);

	List<CcsAcct> CcsAcctList = queryFacility.getAcctByAcctNbr(CcsCard.getAcctNbr());
	List<CcsAcctO> CcsAcctOList = queryFacility.getAcctOByAcctNbr(CcsCardO.getAcctNbr());
	for (CcsAcct CcsAcct : CcsAcctList) {
	    if (BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_P)) {
		throw new ProcessException("账户已经关闭，无法对卡片做销卡操作");
	    }
	    if (BlockCodeUtil.hasBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
		CcsAcct.setBlockCode(BlockCodeUtil.removeBlockCode(CcsAcct.getBlockCode(),
								   CcServProConstants.BLOCKCODE_C));
		saveTtCancelReg_A(RequestType.D, CcsAcct, cardNbr);
	    }
	}
	for (CcsAcctO CcsAcctO : CcsAcctOList) {
	    if (BlockCodeUtil.hasBlockCode(CcsAcctO.getBlockCode(), CcServProConstants.BLOCKCODE_P)) {
		throw new ProcessException("账户已经关闭，无法对卡片做销卡操作");
	    }
	    if (BlockCodeUtil.hasBlockCode(CcsAcctO.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
		CcsAcctO.setBlockCode(BlockCodeUtil.removeBlockCode(CcsAcctO.getBlockCode(),
								    CcServProConstants.BLOCKCODE_C));
	    }
	}

    }

    /**
     * 副卡销卡撤销
     * 
     * @param CcsCard
     * @param CcsCardO
     * @throws ProcessException
     */

    private void NF3406_S(CcsCard CcsCard, CcsCardO CcsCardO, ArrayList<String> list) throws ProcessException {
	// 如果为附卡
	// 根据逻辑卡主卡卡号获取主卡信息
	CcsCard bscCcsCard = queryFacility.getCardByLogicCardNbr(CcsCard.getLogicCardNbr());
	CheckUtil.rejectNull(bscCcsCard, "查询不到卡片信息");
	CcsCardO bscCcsCardO = queryFacility.getCardOByLogicCardNbr(CcsCard.getLogicCardNbr());
	// *主卡销卡状态，则附卡不许做销卡撤销*
	if (BlockCodeUtil.hasBlockCode(bscCcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
	    throw new ProcessException("主卡为销卡状态，不可为附卡进行销卡撤销操作");
	}
	if (BlockCodeUtil.hasBlockCode(bscCcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
	    throw new ProcessException("主卡为销卡状态，不可为附卡进行销卡撤销操作");
	}
	bscCcsCard
		.setBlockCode(BlockCodeUtil.removeBlockCode(bscCcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C));
	bscCcsCardO.setBlockCode(BlockCodeUtil.removeBlockCode(bscCcsCardO.getBlockCode(),
							       CcServProConstants.BLOCKCODE_C));
	saveTtCancelReg_C(CcsCard.getLogicCardNbr(), RequestType.B, CcsCard);
	list.add(CcsCard.getLastestMediumCardNbr());
    }

    /**
     * 判定账户下所有卡片是否都已为销卡状态,判断条件，只要账号下所有的卡片中有一个锁定码中不包含"C"就返回false
     * 
     * @param long1
     * @return
     * @throws ProcessException
     */
    private boolean hasCAllCard(Long long1) throws ProcessException {
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
     * 销户，给所用的账户增加锁定码C，并记录销卡的记录
     * 
     * @param long1
     * @param cardNbr
     *            TODO
     * @throws ProcessException
     */
    private void addCAccount(Long long1, String cardNbr) throws ProcessException {
	List<CcsAcct> CcsAcctList = queryFacility.getAcctByAcctNbr(long1);
	for (CcsAcct CcsAcct : CcsAcctList) {
	    if (isCloseAccount(CcsAcct)) {
		CcsAcct.setBlockCode(BlockCodeUtil.addBlockCode(CcsAcct.getBlockCode(), CcServProConstants.BLOCKCODE_C));
		CcsAcct.setCloseDate(unifiedParaFacilityProvide.BusinessDate());
		saveTtCancelReg_A(RequestType.C, CcsAcct, cardNbr);
	    } else {
		throw new ProcessException("销卡失败：卡片存在欠款");
	    }
	}
	List<CcsAcctO> CcsAcctOList = queryFacility.getAcctOByAcctNbr(long1);
	for (CcsAcctO CcsAcctO : CcsAcctOList) {
	    if (isCloseAccount(CcsAcctO)) {
		CcsAcctO.setBlockCode(BlockCodeUtil.addBlockCode(CcsAcctO.getBlockCode(),
								 CcServProConstants.BLOCKCODE_C));
	    } else {
		throw new ProcessException("销卡失败：卡片存在欠款");
	    }
	}
    }

    /**
     * 给所有附卡增加锁定码
     * 
     * @param cardNbr
     * @throws ProcessException
     */
    private ArrayList<String> addCSUPPCcsCard(String cardNbr, ArrayList<String> list) throws ProcessException {
	// 查询所有附卡
	List<CcsCard> CcsCardList = queryFacility.getSuppCardByCardNbr(cardNbr);
	for (CcsCard suppCcsCard : CcsCardList) {
	    if (!BlockCodeUtil.hasBlockCode(suppCcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
		suppCcsCard.setBlockCode(BlockCodeUtil.addBlockCode(suppCcsCard.getBlockCode(),
								    CcServProConstants.BLOCKCODE_C));
		saveTtCancelReg_C(suppCcsCard.getLogicCardNbr(), RequestType.A, suppCcsCard);
		list.add(suppCcsCard.getLogicCardNbr());
	    }
	}
	List<CcsCardO> CcsCardOList = queryFacility.getSuppCardOByCardNbr(cardNbr);
	for (CcsCardO suppCcsCardO : CcsCardOList) {
	    if (!BlockCodeUtil.hasBlockCode(suppCcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_C)) {
		suppCcsCardO.setBlockCode(BlockCodeUtil.addBlockCode(suppCcsCardO.getBlockCode(),
								     CcServProConstants.BLOCKCODE_C));
	    }
	}

	return list;
    }

    /**
     * 保存销户请求
     * 
     * @param rt
     * @param CcsAcct
     *            TODO
     * @param cardNbr
     *            TODO
     * @param cardNbr
     */
    private void saveTtCancelReg_A(RequestType rt, CcsAcct CcsAcct, String cardNbr) {
	CcsCardCloseReg tmCancelReg = new CcsCardCloseReg();
	tmCancelReg.setOrg(OrganizationContextHolder.getCurrentOrg());
	tmCancelReg.setAcctNbr(CcsAcct.getAcctNbr());
	tmCancelReg.setAcctType(CcsAcct.getAcctType());
	tmCancelReg.setCardNbr(cardNbr);
	tmCancelReg.setRequestType(rt);
	tmCancelReg.setLogicCardNbr(CcsAcct.getDefaultLogicCardNbr());
	tmCancelReg.setLogBizDate(cpsUnifiedParameterFacilityProvide.BusinessDate());
	tmCancelReg.setRequestTime(new Date());
	rCcsCardCloseReg.save(tmCancelReg);
    }

    /**
     * 保存销卡请求
     * 
     * @param cardNbr
     * @param rt
     * @param CcsCard
     *            TODO
     */
    private void saveTtCancelReg_C(String cardNbr, RequestType rt, CcsCard CcsCard) {
	CcsCardCloseReg tmCancelReg = new CcsCardCloseReg();
	tmCancelReg.setOrg(OrganizationContextHolder.getCurrentOrg());
	tmCancelReg.setAcctNbr(CcsCard.getAcctNbr());
	tmCancelReg.setCardNbr(cardNbr);
	tmCancelReg.setRequestType(rt);
	tmCancelReg.setLogicCardNbr(CcsCard.getLogicCardNbr());
	tmCancelReg.setLogBizDate(cpsUnifiedParameterFacilityProvide.BusinessDate());
	tmCancelReg.setRequestTime(new Date());
	rCcsCardCloseReg.save(tmCancelReg);
    }

    /**
     * 账户无销户锁定码且当前CURR_BAL<0、取现余额CASH_BAL<0、 额度内分期余额LOAN_BAL<0、争议金额DISPUTE_AMT为0
     * 
     * @param CcsAcct
     * @return true:可以对账户进行销户，false 不能对账户销户
     */
    private boolean isCloseAccount(CcsAcct CcsAcct) {
	return CcsAcct.getCurrBal().doubleValue() <= 0 && CcsAcct.getCashBal().doubleValue() <= 0
		&& CcsAcct.getLoanBal().doubleValue() <= 0 && CcsAcct.getDisputeAmt().doubleValue() == 0;
    }

    /**
     * 账户无销户锁定码且账户的当前CURR_BAL为0、取现余额CASH_BAL为0、额度内分期余额LOAN_BAL、
     * 争议金额DISPUTE_AMT为0
     * 、未匹配取现金额UNMATCH_CASH为0、未匹配贷记金额UNMATCH_CR为0、未匹配借记金额UNMATCH_DB为0
     * 
     * @param CcsAcctO
     * @return true:可以对账户进行销户，false 不能对账户销户
     */
    private boolean isCloseAccount(CcsAcctO CcsAcctO) {
	return CcsAcctO.getCurrBal().doubleValue() <= 0 && CcsAcctO.getCashBal().doubleValue() <= 0
		&& CcsAcctO.getLoanBal().doubleValue() <= 0 && CcsAcctO.getDisputeAmt().doubleValue() == 0
		&& CcsAcctO.getMemoCash().doubleValue() == 0 && CcsAcctO.getMemoCr().doubleValue() == 0
		&& CcsAcctO.getMemoDb().doubleValue() == 0;
    }

    @Override
    @Transactional
    @Deprecated
    public void NF3202(String cardNbr, List<Map<String, Serializable>> cycleLmtList) {

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Transactional
    public void NF3205(String cardNbr, AddressType addrType) throws ProcessException {
	log.info("NF3205:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],寄送地址[" + addrType + "]");
	CheckUtil.checkCardNo(cardNbr);
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	if (CcsCard.getCardDeliverAddrFlag() != null && CcsCard.getCardDeliverAddrFlag().equals(addrType)) {
	    throw new ProcessException("修改的寄送地址与地址相同，无需再设");
	}

	// 原卡片寄送地址类型
	AddressType oldAddressType = CcsCard.getCardDeliverAddrFlag();

	CcsCard.setCardDeliverAddrFlag(addrType);

	// 卡片寄送地址变更提醒
	CcsCustomer customer = rCcsCustomer.findOne(CcsCard.getCustId());
	// messageService.sendMessage(MessageCategory.M11,
	// CcsCard.getProductCd(), cardNbr, customer.getName(),
	// customer.getGender(), customer.getMobileNo(), customer.getEmail(),
	// new Date(),
	// new MapBuilder().add("oldAddressType",
	// oldAddressType).add("newAddressType", addrType).build());
	String msgCd = fetchMsgCdService.fetchMsgCd(CcsCard.getProductCd(), CPSMessageCategory.CPS011);
/*	downMsgFacility.sendMessage(msgCd,
				    cardNbr,
				    customer.getName(),
				    customer.getGender(),
				    customer.getMobileNo(),
				    new Date(),
				    new MapBuilder().add("oldAddressType", oldAddressType)
					    .add("newAddressType", addrType).build());
*/    }

    @Override
    @Transactional
    public void NF3207(String cardNbr) throws ProcessException {
	log.info("NF3207:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	CheckUtil.checkCardNo(cardNbr);
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CheckUtil.rejectNull(CcsCard, "查询不到卡片信息");
	if (CcsCard.getNextCardFeeDate() != null
		&& DateTools.dateCompare(DateUtils.addYears(cpsUnifiedParameterFacilityProvide.BusinessDate(), 1),
					 CcsCard.getNextCardFeeDate()) < 0) {
	    throw new ProcessException("不能重复减免年费");
	}
	Date newNextCardFeeDate = DateUtils.addYears(CcsCard.getNextCardFeeDate(), 1);
	CcsCard.setNextCardFeeDate(newNextCardFeeDate);
    }

    @Override
    @Transactional
    public void NF3407(String cardNbr) throws ProcessException {
	log.info("NF3407:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	CheckUtil.checkCardNo(cardNbr);
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	if (BlockCodeUtil.hasBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
	    throw new ProcessException("卡片已经冻结,无需在设");
	}
	if (BlockCodeUtil.hasBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
	    throw new ProcessException("卡片已经冻结,无需在设");
	}
	CcsCard.setBlockCode(BlockCodeUtil.addBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_T));
	CcsCardO.setBlockCode(BlockCodeUtil.addBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_T));
    }

    @Override
    @Transactional
    public void NF3408(String cardNbr) throws ProcessException {
	log.info("NF3408:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	CheckUtil.checkCardNo(cardNbr);
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	if (!BlockCodeUtil.hasBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
	    throw new ProcessException("卡片非冻结状态，无需解冻");
	}
	if (!BlockCodeUtil.hasBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_T)) {
	    throw new ProcessException("卡片非冻结状态，无需解冻");
	}
	CcsCard.setBlockCode(BlockCodeUtil.removeBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_T));
	CcsCardO.setBlockCode(BlockCodeUtil.removeBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_T));

	// 卡片冻结解除提醒短信
	CcsCustomer customer = rCcsCustomer.findOne(CcsCard.getCustId());
	// messageService.sendMessage(MessageCategory.M09,
	// CcsCard.getProductCd(), cardNbr, customer.getName(),
	// customer.getGender(), customer.getMobileNo(), customer.getEmail(),
	// new Date(), new HashMap<String, Object>());
	String msgCd = fetchMsgCdService.fetchMsgCd(CcsCard.getProductCd(), CPSMessageCategory.CPS009);
/*	downMsgFacility.sendMessage(msgCd, cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(),
				    new Date(), new HashMap<String, Object>());
*/    }

    /**
     * 合并CcsCard，CcsCardO，CcsCardUsage,qCcsCardThresholdCtrl数据
     * 
     * @param CcsCard
     * @param CcsCardO
     * @param cardUsage
     * @return
     * @throws ProcessException
     */
    private Map<String, Serializable> merageCcsCard(String cardNbr, CcsCard CcsCard, CcsCardO CcsCardO,
	    CcsCardUsage cardUsage, CcsCardThresholdCtrl CcsCardLmtOver) throws ProcessException {
	Map<String, Serializable> CcsCardmap = CcsCard.convertToMap();
	CcsCardmap.putAll(cardUsage.convertToMap());
	CcsCardmap.putAll(CcsCardO.convertToMap());
	// 默认情况下
	// qCcsCardThresholdCtrl数据为空，为了在返回的mpa中有qCcsCardThresholdCtrl的字段，所以这里new了一个对象
	if (CcsCardLmtOver == null) {
	    CcsCardLmtOver = new CcsCardThresholdCtrl();
	    CcsCardLmtOver.setLogicCardNbr(CcsCardO.getLogicCardNbr());
	}
	CcsCardmap.putAll(CcsCardLmtOver.convertToMap());
	callOTBProvide.setCardOTB(CcsCardO, CcsCardmap);
	// 翻译前台需要锁定码信息
	CcsCardmap.put(CcServProConstants.KEY_BLOCK_CODE_INFO_LIST,
		       cpsUnifiedParameterFacilityProvide.blockCodeInfo(CcsCard.getBlockCode()));
	// 综合可用额度
	// 使用卡片层的额度，需要获取客户层，账户层，卡片层的最小值；
	System.out.println("cardNbr==" + cardNbr);
	CcsCardmap.put(CcServProConstants.KEY_REAL_OTB,
		       callOTBProvide.realOTB(cardNbr, queryFacility.getAcctByCardNbr(cardNbr)));
	return CcsCardmap;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Transactional
    public void NF3409(String cardNbr) throws ProcessException {
	log.info("NF3409:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO.setPinTries(CcServProConstants.PINTRIES_DEFAULT);

	CcsCustomer customer = rCcsCustomer.findOne(CcsCardO.getCustId());
	// messageService.sendMessage(MessageCategory.M08,
	// CcsCardO.getProductCd(), cardNbr, customer.getName(),
	// customer.getGender(), customer.getMobileNo(), customer.getEmail(),
	// new Date(), new MapBuilder().build());
	String msgCd = fetchMsgCdService.fetchMsgCd(CcsCardO.getProductCd(), CPSMessageCategory.CPS004);
/*	downMsgFacility.sendMessage(msgCd, cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(),
				    new Date(), new MapBuilder().build());
*/    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Transactional
    public void NF3410(String cardNbr) throws ProcessException {
	log.info("NF3410:卡号[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO.setInqPinTries(CcServProConstants.PINTRIES_DEFAULT);

	// 查询密码锁定重置短信
	CcsCustomer customer = rCcsCustomer.findOne(CcsCardO.getCustId());
	// messageService.sendMessage(MessageCategory.M18,
	// CcsCardO.getProductCd(), cardNbr, customer.getName(),
	// customer.getGender(), customer.getMobileNo(), customer.getEmail(),
	// new Date(), new MapBuilder().build());
	String msgCd = fetchMsgCdService.fetchMsgCd(CcsCardO.getProductCd(), CPSMessageCategory.CPS018);
/*	downMsgFacility.sendMessage(msgCd, cardNbr, customer.getName(), customer.getGender(), customer.getMobileNo(),
				    new Date(), new MapBuilder().build());
*/    }

    // 到期不换卡；
    @Transactional
    public void NF3411(String cardNbr) throws ProcessException {
	log.info("NF3411:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	CheckUtil.checkCardNo(cardNbr);
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");

	// 检查锁定吗是否包含Q，如果是Q，抛异常 "已为到期不换卡，无需再设"
	BlockCodeUtil.isCanAddBlockCode_Q(CcsCard.getBlockCode());
	BlockCodeUtil.isCanAddBlockCode_Q(CcsCardO.getBlockCode());

	CcsCard.setBlockCode(BlockCodeUtil.addBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_Q));
	CcsCardO.setBlockCode(BlockCodeUtil.addBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_Q));
    }

    // 到期不换卡撤销；
    @Transactional
    public void NF3412(String cardNbr) throws ProcessException {
	log.info("NF3412:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
	CheckUtil.checkCardNo(cardNbr);
	CcsCard CcsCard = queryFacility.getCardByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
	CcsCardO CcsCardO = queryFacility.getCardOByCardNbr(cardNbr);
	CheckUtil.rejectNull(CcsCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");

	// 判断是否能销卡撤销
	BlockCodeUtil.isCanCancel_Q(CcsCard.getBlockCode());
	BlockCodeUtil.isCanCancel_Q(CcsCardO.getBlockCode());

	CcsCard.setBlockCode(BlockCodeUtil.removeBlockCode(CcsCard.getBlockCode(), CcServProConstants.BLOCKCODE_Q));
	CcsCardO.setBlockCode(BlockCodeUtil.removeBlockCode(CcsCardO.getBlockCode(), CcServProConstants.BLOCKCODE_Q));
    }

    // 联机卡片申请
    @Override
    @Transactional
    public void NF3304(ApplyFileItem afi) throws Exception {
	cardServiceBo.apply(afi);
    }

}
