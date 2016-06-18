package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.ccs.ui.server.commons.DateTools;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 
 * @see 类名：CardInfoServer
 * @see 描述：卡号详情
 *
 * @see 创建日期：   Jun 23, 201510:23:07 AM
 * @author yeyu
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@SuppressWarnings("all")
@Controller
@RequestMapping(value = "/CardInfoServer")
public class CardInfoServer {
	@Autowired
	private CPSBusProvide cpsBusProvide;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	/*不使用MMS服务-20150914lsy*/
	//@Resource(name="mpsCardService")
	//private MmCardService mmCardService;

	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 
	 * @see 方法名：getCardList 
	 * @see 描述：获取卡片信息列表
	 * @see 创建日期：Jun 23, 201510:28:06 AM
	 * @author yeyu
	 *  
	 * @param cardNo
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 * 测试用
	 */
	@ResponseBody()
	@RequestMapping(value="/queryCardInfo",method={RequestMethod.POST})
	public FetchResponse getCardList(@RequestBody FetchRequest request) {
		String cardNbr = (String)request.getParameter(CcsCardLmMapping.P_CardNbr);
		log.info("卡片信息查询开始，卡号后四位["+CodeMarkUtils.subCreditCard(cardNbr)+"]");
		//存放卡片信息
		List<Map<String, Serializable>> cardList = new ArrayList<Map<String, Serializable>>();
		List<CcsCard> cards = cpsBusProvide.getSUPPTmCardTocardNbr(cardNbr);
		if(cards.size() != 0){
			for (CcsCard tmCard : cards) {
				cardList.add(tmCard.convertToMap());
			}
		}else{
			CcsCard card = cpsBusProvide.getTmCardTocardNbr(cardNbr);
			cardList.add(card.convertToMap());
		}
		FetchResponse response = new FetchResponse();
		response.setRows(cardList);
		return response;
	}
	
	/**
	 * 
	 * @see 方法名：getCardInfo 
	 * @see 描述：根据卡号获取卡片明细
	 * @see 创建日期：Jun 23, 201510:32:34 AM
	 * @author yeyu
	 *  
	 * @param cardNo
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@ResponseBody()
	@RequestMapping(value="/getCardInfo",method={RequestMethod.POST})
	public Map<String, Serializable> getCardInfo(@RequestBody String cardNo) throws ProcessException {
		
		//检查卡号
		CheckUtil.checkCardNo(cardNo);
		log.info("根据卡号获取卡片明细，卡号后四位["+CodeMarkUtils.subCreditCard(cardNo)+"]");
//		
//		// 调用MPS服务获取介质卡片信息
		//Map<String, Serializable> mediaMap = mmCardService.MS3102(cardNo);
		//存放卡片明细信息
		Map<String, Serializable> map = new HashMap<String,Serializable>();
		//获取卡信息
		CcsCard card = cpsBusProvide.getTmCardTocardNbr(cardNo);
		
		//获取卡片TmCardO数据
		CcsCardO cardO = cpsBusProvide.getTmCardOTocardNbr(cardNo);
		
		CcsCustomer tmCustomer = cpsBusProvide.getTmCustomerToCard(cardNo);
		
		map.put(CcsCard.P_LastestMediumCardNbr, card.getLastestMediumCardNbr());//最新介质卡号
		Product p = unifiedParameterService.loadParameter(card.getProductCd(), Product.class);
		map.put(CcsCard.P_ProductCd, p.description);//卡产品名称
		map.put(CcsCard.P_ProductCd, "测试产品名称");
		map.put(CcsCard.P_BscSuppInd,card.getBscSuppInd());//主附卡指示
		map.put(CcsCustomer.P_Name, tmCustomer.getName());
		map.put(CcsCustomer.P_IdType, tmCustomer.getIdType());
		map.put(CcsCustomer.P_IdNo, tmCustomer.getIdNo());
		map.put(CcsCard.P_LogicCardNbr, card.getLogicCardNbr());//主卡卡号
		map.put(CcsCard.P_OwningBranch, card.getOwningBranch());//发卡网点
		map.put(CcsCard.P_SetupDate, card.getSetupDate());//创建日期
		//map.put(CcsCard.P_ActiveInd, Indicator.valueOf(mediaMap.get("activeInd").toString()));//是否已激活
		//map.put(CcsCard.P_ActiveDate, DateTools.getDateValue(mediaMap.get("activateDate")));//激活日期
		//map.put(CcsCard.P_CloseDate, DateTools.getDateValue(mediaMap.get("cancelDate")));//销卡销户日期
		map.put(CcsCard.P_PosPinVerifyInd, card.getPosPinVerifyInd());//是否消费凭密
		map.put(CcsCard.P_RelationshipToBsc, card.getRelationshipToBsc());//与主卡持卡人关系
		map.put(CcsCard.P_CardExpireDate, DateTools.convertToYearAndMonth(card.getCardExpireDate()));//卡片有效日期
		map.put(CcsCard.P_NextCardFeeDate, card.getNextCardFeeDate());//下个年费收取日期
		map.put(CcsCard.P_RenewInd, card.getRenewInd());//续卡标识
		map.put(CcsCard.P_RenewRejectCd, card.getRenewRejectCd());//续卡拒绝原因码
		//String cardno = mediaMap.get(CcsCardLmMapping.P_CardNbr).toString();
		//String firstCardno = mediaMap.get("firstCardNbr").toString();
		//Date lastExpiryDate = mediaMap.get("lastExpiryDate") == null?null:(Date) mediaMap.get("lastExpiryDate");
		
		//String blockCode = mediaMap.get(CcsCard.P_BlockCode) == null?null:(String) mediaMap.get(CcsCard.P_BlockCode);
		
		String blockCode1 = card.getBlockCode() == null?null:card.getBlockCode();

		// 卡号=首卡卡号&&旧卡有效期为空，则新发卡
		/*
		if (cardno.equals(firstCardno) && lastExpiryDate == null) {
			map.put("newCardIssueInd",Indicator.Y);
		} else {
			map.put("newCardIssueInd",Indicator.N);
		}
		*/
		// 将逻辑卡片和介质卡片的封锁码合并
		//map.put(CcsCard.P_BlockCode, blockCodeUtils.unionBlockCodes(blockCode, blockCode1));//锁定码
		
		//map.put("qQinExistInd",mmCardService.MS3501(cardNo) == true ? Indicator.Y : Indicator.N);//是否存在查询密码
		//map.put("pQinExistInd", mmCardService.MS3502(cardNo) == true ? Indicator.Y : Indicator.N);//是否存在交易密码
		map.put("qQinExistInd",Indicator.Y);//是否存在查询密码
		map.put("pQinExistInd",Indicator.Y);//是否存在交易密码
	    
		map.put(CcsCardO.P_PinTries, cardO.getPinTries());//交易密码错误次数
		map.put(CcsCardO.P_InqPinTries, cardO.getInqPinTries());//查询密码错误次数
		map.put(CcsCardO.P_LastPinTriesTime, cardO.getLastPinTriesTime());//上次密码错时间
		map.put(CcsCardO.P_LastInqPinTriesTime, cardO.getLastInqPinTriesTime());//上次查询密码错误时间
		return map;
	}

}
