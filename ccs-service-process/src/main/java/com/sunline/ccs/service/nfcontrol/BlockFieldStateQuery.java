package com.sunline.ccs.service.nfcontrol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.service.protocol.S14000Card;
import com.sunline.ccs.service.protocol.ServiceControlResult;
import com.sunline.ark.support.utils.CodeMarkUtils;

/** 
 * @see 类名：BlockFieldStateQuery
 * @see 描述：查询是否存在控制的锁定码
 *
 * @see 创建日期：   2015年6月24日 下午2:39:42
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class BlockFieldStateQuery {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	@Autowired
	private UnifiedParameterFacility unifiedParameterService;

	@Autowired
	private BlockCodeUtils blockCodeUtils;

	/*
	 * 查询是否存在控制的锁定码
	 */
	public ArrayList<ServiceControlResult> process(String cardNbr, String block) throws ProcessException {
		logger.debug("查询是否存在控制的锁定码处理卡{},锁定码{}", CodeMarkUtils.subCreditCard(cardNbr), block);
		ArrayList<ServiceControlResult> serviceCtrlResulstList = new ArrayList<ServiceControlResult>();
		String cardBlock = "";
		String accountBlock = "";
		String mediaBlock = "";
		String allBlock = "";
		if (StringUtils.isNotBlank(block)) {

			logger.debug("查询是否存在控制的锁定码，卡号{},锁定码 {}", CodeMarkUtils.subCreditCard(cardNbr), block);

			// 获取介质卡片锁定码
			Map<String, Serializable> map = mmCardService.MS3102(cardNbr);
			if (map != null) {
				mediaBlock = (String) map.get(S14000Card.P_BlockCd);
			}

			// 获取卡片锁定码
			CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(cardNbr);
			if (CcsCard != null) {
				cardBlock = CcsCard.getBlockCode();
			}

			// 获取所用账户的锁定码
			List<CcsAcct> CcsAcctList = custAcctCardQueryFacility.getAcctByCardNbr(cardNbr);
			if (CcsAcctList != null) {
				for (CcsAcct CcsAcct : CcsAcctList) {
					accountBlock = blockCodeUtils.unionBlockCodes(accountBlock, CcsAcct.getBlockCode());
				}
			}

			// 两两合并，这里实现不够清爽，后期优化
			allBlock = blockCodeUtils.unionBlockCodes(mediaBlock, cardBlock);
			allBlock = blockCodeUtils.unionBlockCodes(allBlock, accountBlock);
//			StringBuffer sb = new StringBuffer();
			for (char c : block.toCharArray()) {
				String s = String.valueOf(c);
				if (StringUtils.contains(allBlock, s)) {
					BlockCode blockCode = unifiedParameterService.retrieveParameterObject(s, BlockCode.class);
					if (blockCode != null) {
						if (s.charAt(0) < 65) {
							s = numberBlockCodeMapping(s);
						}
						ServiceControlResult blockResult = new ServiceControlResult();
						blockResult.setPass(false);
						blockResult.setReturnCode(CtlFieldReturnCode.valueOf(s).getReturnCode());
						blockResult.setReturnMessage(blockCode.blockCode + "-" + blockCode.description);
						serviceCtrlResulstList.add(blockResult);
					}
				}
			}
		}
		logger.debug("查询是否存在控制的锁定码 卡、账，介质卡锁定码{},配置的锁定码{},处理结果{}", allBlock, block, serviceCtrlResulstList);
		return serviceCtrlResulstList;
	}

	private String numberBlockCodeMapping(String s){
		String numberBlockCodeString = "";
		if(s.equals("0")){
			numberBlockCodeString = "ZREO";
		}else if(s.endsWith("1")){
			numberBlockCodeString = "ONE";
		}else if(s.endsWith("2")){
			numberBlockCodeString = "TWO";
		}else if(s.endsWith("3")){
			numberBlockCodeString = "THREE";
		}else if(s.endsWith("4")){
			numberBlockCodeString = "FOUR";
		}else if(s.endsWith("5")){
			numberBlockCodeString = "FIVE";
		}else if(s.endsWith("6")){
			numberBlockCodeString = "SIX";
		}else if(s.endsWith("7")){
			numberBlockCodeString = "SEVEN";
		}else if(s.endsWith("8")){
			numberBlockCodeString = "EIGHT";
		}else if(s.endsWith("9")){
			numberBlockCodeString = "NINE";
		}
		return numberBlockCodeString;
	}
}
