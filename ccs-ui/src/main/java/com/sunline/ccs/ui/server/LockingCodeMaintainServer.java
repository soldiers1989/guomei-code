package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.enums.BlockLevel;
import com.sunline.ccs.service.api.OperateService;
import com.sunline.ccs.service.protocol.S14051Req;
import com.sunline.ccs.service.protocol.S14120Req;
import com.sunline.ccs.ui.server.commons.BlockCodeUtil;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.AccountType;

/**
 * 锁定码维护
 * 
 * @author linxc 2015年6月22日
 *
 */
@Controller
@RequestMapping(value = "/lockingCodeMaintainServer")
public class LockingCodeMaintainServer {

	Logger log = LoggerFactory.getLogger(Logger.class);

	@Autowired
	private UnifiedParameterFacility facility;

	@Autowired
	private CPSBusProvide cpsBusProvide;

	@Autowired
	private OpeLogUtil opeLogUtil;

	/*此服务调用MMS服务，弃用-20150914lsy
	@Resource(name = "mpsCardService")
	private MmCardService mmCardService;
	*/
	@Resource(name = "operateService")
	private OperateService operateService;

	QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;

	QCcsCard qCcsCard = QCcsCard.ccsCard;
	
	@Autowired
	private RCcsAcct rCccsAcct;
	@Autowired
	private RCcsAcctO rCcsAcctO;
	
	QCcsCardLmMapping qCcsCardMediaMap = QCcsCardLmMapping.ccsCardLmMapping;

	private static final String P_BlockCd = "blockCd";

	private static final String R_BLOCKCODE = "R";// R、S挂失的锁定码

	private static final String S_BLOCKCODE = "S";

	private static final String C_BLOCKCODE = "C";// 锁定码C，会单独处理

	private static final String L_BLOCKCODE = "L";// 锁定码L

	private static final String OPT_0 = "0";// 操作码0

	private static final String OPT_1 = "1";// 操作码1

	private String mediaCardBl = "";// 介质卡上的锁定码

	private String bothCode = "";// 返回前台的共用锁定码

	private String acctCode = "";// 返回前台的账户锁定码

	private String cardCode = "";// 返回前台卡片层锁定码

	private String contextCode = "";// 上下文blockcode

	@ResponseBody()
	@RequestMapping(value = "/getBlockCode", method = { RequestMethod.POST })
	public Map<String, BlockCode> getBlockCode(@RequestBody BlockLevel bl)
			throws FlatException {
		log.info("getBlockCode:获取模板中所有锁定码信息");
		Map<String, BlockCode> map = this.getAllBC(BlockCode.class, bl);
		return map;
	}

	// 获取数据库所有锁定码模板参数
	private Map<String, BlockCode> getAllBC(Class<BlockCode> clazz, BlockLevel bl) {
		Map<String, BlockCode> map = (Map<String, BlockCode>) facility.retrieveParameterObject(clazz);
		// 对所有的锁定码排序
		List<String> list = new ArrayList<String>(map.keySet());
		// 锁定码0--9为账龄，不允许修改，需要在后台过滤掉
		List<String> listNew = new ArrayList<String>();
		for (String s : list) {
			char c = s.charAt(0);
			// “a”的ASCII码值为65
			if (c >= 65) {
				listNew.add(String.valueOf(c));
			}
		}
		java.util.Collections.sort(listNew);
		// 采用LinkedHashMap，是怎么放进去的怎么拿出来有序
		Map<String, BlockCode> seqMap = new LinkedHashMap<String, BlockCode>();
		// 根据要显示的锁定码级别返回
		for (String key : listNew) {
			if (map.get(key).blockLevel.equals(bl)) {
				seqMap.put(key, map.get(key));
			} else {
				continue;
			}
		}
		return seqMap;
	}

	@ResponseBody()
	@RequestMapping(value = "/loadBlockCode", method = { RequestMethod.POST })
	public String loadBlockCode(@RequestBody String cardNo,
			@RequestBody AccountType acctType, @RequestBody BlockLevel bl)
			throws FlatException {
		log.info("loadBlockCode:账户类型[" + acctType + "],卡号["
				+ CodeMarkUtils.subCreditCard(cardNo) + "], + 锁定码层级" + bl);
		// 账户锁定码
		String acctBlockCode = acctBlInfo(acctType, cardNo);
		// 合并之后的卡片锁定码
		String cardBlockCode = cardBlinfo(cardNo);
		// 合并账户和卡片锁定码
		String resultBlockCode = BlockCodeUtil.unionBlockCodes(acctBlockCode,cardBlockCode);
		// 根据锁定码层级返回对应的锁定码
		StringBuffer sb = new StringBuffer();
		if (resultBlockCode != null) {
			for (Character c : resultBlockCode.toCharArray()) {
				// 如果为账龄锁定码，跳出进行下一个循环
				if (c.toString().charAt(0) < 65) {
					continue;
				}
				BlockCode b = facility.loadParameter(c.toString(),
						BlockCode.class);
				if (b.blockLevel.equals(bl)) {
					sb.append(b.blockCode);
				}
			}
		}
		// 把返回的各个层级锁定码保存，以备后用
		switch (bl) {
		case All:
			bothCode = sb.toString();
			break;
		case ACCT:
			acctCode = sb.toString();
			break;
		case CARD:
			cardCode = sb.toString();
			break;
		}
		log.info("锁定码层级：" + bl + ",返回卡片或者账户对应的锁定码为：" + sb.toString());
		return sb.toString();
	}

	/**
	 * 获取账户信息
	 * 
	 * @param acctType
	 * @param cardNo
	 * @return
	 * @throws FlatException
	 */
	public String acctBlInfo(AccountType acctType, String cardNo)
			throws FlatException {
		log.info("acctBlInfo:账户类型[" + acctType + "],卡号["
				+ CodeMarkUtils.subCreditCard(cardNo) + "]");
		CcsAcct tmAccount = cpsBusProvide.getTmAccountTocardNbr(cardNo,
				acctType);
		CcsAcctO tmAccountO = cpsBusProvide.getTmAccountOTocardNbr(cardNo,
				acctType);
		Map<String, Serializable> acctinfo = mergeMap(tmAccount, tmAccountO);
		String acctBlockCode = (String) acctinfo.get(CcsAcct.P_BlockCode);
		log.info("账户锁定码为：" + acctBlockCode);
		return acctBlockCode;
	}

	/**
	 * 账户信息=CcsAcct+CcsAcctO，组合返回的数据
	 * 
	 * 账户表
	 * 
	 * @param CcsAcctO
	 * @return
	 */
	private Map<String, Serializable> mergeMap(CcsAcct tmAccount,
			CcsAcctO tmAccountO) {
		Map<String, Serializable> mapCcsAcct = tmAccount.convertToMap();
		mapCcsAcct.putAll(tmAccountO.convertToMap());
		cpsBusProvide.setOTB(mapCcsAcct);
		return mapCcsAcct;
	}

	/**
	 * 得到卡片信息，合并介质、逻辑卡锁定码，返回
	 * 
	 * @param cardNo
	 * @return
	 */
	public String cardBlinfo(String cardNo) {
		log.info("cardBlinfo+卡号:" + CodeMarkUtils.subCreditCard(cardNo));
		// TMCARD和TMCARDO上的锁定码一致，这里值查询TMCARD上的锁定码
		CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
		String logicCardBl = tmCard.getBlockCode();
		// 得到介质卡锁定码
		//Map<String, Serializable> map = mmCardService.MS3102(cardNo);
		//mediaCardBl = map.get(P_BlockCd) == null ? "" : map.get(P_BlockCd).toString();
		// 合并介质卡逻辑卡锁定码
		String cardBlockCode = BlockCodeUtil.unionBlockCodes(logicCardBl,mediaCardBl);
		log.info("合并介质卡逻辑卡锁定码后为：" + cardBlockCode);
		return cardBlockCode;
	}

	/**
	 * 根据锁定码字符串和锁定码级别更新锁定码
	 * 
	 * @param cardNo
	 * @param accountType
	 * @param bothBlockCode
	 * @param acctBlockCode
	 * @param cardBlockCode
	 * @throws FlatException
	 */
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/updateBlockCode", method = { RequestMethod.POST })
	public void updateBlockCode(@RequestBody String cardNo,
			@RequestBody String accountTypeValue,
			//@RequestBody String bothBlockCode,
			@RequestBody String acctBlockCode)
			//, @RequestBody String cardBlockCode)
			throws FlatException {
//		String bothBlockCode="";
//		String cardBlockCode="";
		log.info("updateBlockCode+卡号:[" + CodeMarkUtils.subCreditCard(cardNo)
				+ "],账户类型:[" + accountTypeValue + "],账户层锁定码:[" + acctBlockCode + "]");
		// 合并返回给前台的锁定码，保存上下文
		AccountType accountType=AccountType.valueOf(accountTypeValue);
		contextCode = BlockCodeUtil.unionBlockCodes(bothCode,
				BlockCodeUtil.unionBlockCodes(cardCode, acctCode));
		log.info("updateBlockCode中contextCode+[" + contextCode + "]");
		// 合并页面传过来的锁定码
//		String updateUnionBlockCode = BlockCodeUtil.unionBlockCodes(
//				bothBlockCode,
//				BlockCodeUtil.unionBlockCodes(acctBlockCode, cardBlockCode));
		String updateUnionBlockCode=acctBlockCode;
		String updateUnionTotal = updateUnionBlockCode;
		log.info("updateBlockCode中updateUnionBlockCode+["
				+ updateUnionBlockCode + "]");
		boolean isUpdate = BlockCodeUtil.isContainEachother(contextCode,
				updateUnionBlockCode);
		log.info("updateBlockCode中isUpdate+[" + isUpdate + "]");
		// 如果不相互包含，说定搜索时和更新时锁定码不一致，则需要更新
		if (!isUpdate) {
			// 当查询时有挂失锁定码，更新时没有，需要调用mps的解挂服务
			/*不使用-20150914
			if (BlockCodeUtil.hasBlockCode(cardCode, R_BLOCKCODE)
					&& !BlockCodeUtil.hasBlockCode(cardBlockCode, R_BLOCKCODE)) {
				mmCardService.MS3205(cardNo);
			}
			// 当查询时有挂失锁定码，更新时没有，需要调用mps的解挂服务
			if (BlockCodeUtil.hasBlockCode(cardCode, S_BLOCKCODE)
					&& !BlockCodeUtil.hasBlockCode(cardBlockCode, S_BLOCKCODE)) {
				mmCardService.MS3205(cardNo);
			}
			// 当查询时没有有挂失的锁定码，更新时出现，需要调用mps的挂失服务，调完后删除挂失锁定码R或者S
			if (!BlockCodeUtil.hasBlockCode(cardCode, R_BLOCKCODE)
					&& BlockCodeUtil.hasBlockCode(cardBlockCode, R_BLOCKCODE)) {
				mmCardService.MS3204(cardNo, R_BLOCKCODE);
			}
			// 当查询时没有有挂失的锁定码，更新时出现，需要调用mps的挂失服务，调完后删除挂失锁定码R或者S
			if (!BlockCodeUtil.hasBlockCode(cardCode, S_BLOCKCODE)
					&& BlockCodeUtil.hasBlockCode(cardBlockCode, S_BLOCKCODE)) {
				mmCardService.MS3204(cardNo, S_BLOCKCODE);
			}
			*/
//			// 调用非金融的销卡撤销或者销卡服务
//			if (BlockCodeUtil.hasBlockCode(bothCode, C_BLOCKCODE)
//					&& !BlockCodeUtil.hasBlockCode(bothBlockCode, C_BLOCKCODE)) {
//				this.S14120(OPT_1, cardNo);
//			}
//			if (!BlockCodeUtil.hasBlockCode(bothCode, C_BLOCKCODE)
//					&& BlockCodeUtil.hasBlockCode(bothBlockCode, C_BLOCKCODE)) {
//				this.S14120(OPT_0, cardNo);
//			}
//			// 调用非金融解除临时挂失的锁定码服务
//			if (BlockCodeUtil.hasBlockCode(bothCode, L_BLOCKCODE)
//					&& !BlockCodeUtil.hasBlockCode(bothBlockCode, L_BLOCKCODE)) {
//				this.S14051(OPT_1, cardNo);
//			}
//			if (!BlockCodeUtil.hasBlockCode(bothCode, L_BLOCKCODE)
//					&& BlockCodeUtil.hasBlockCode(bothBlockCode, L_BLOCKCODE)) {
//				this.S14051(OPT_0, cardNo);
//			}
			// 去掉上在介质卡上的R、S、C锁定码
			updateUnionBlockCode = BlockCodeUtil.replace(updateUnionBlockCode,
					R_BLOCKCODE);
			updateUnionBlockCode = BlockCodeUtil.replace(updateUnionBlockCode,
					S_BLOCKCODE);
			updateUnionBlockCode = BlockCodeUtil.replace(updateUnionBlockCode,
					C_BLOCKCODE);
			// 去掉上在介质卡上的L锁定码
			updateUnionBlockCode = BlockCodeUtil.replace(updateUnionBlockCode,
					L_BLOCKCODE);
			bothCode = BlockCodeUtil.replace(bothCode, C_BLOCKCODE);
//			StringBuffer cardSb = new StringBuffer("");
			StringBuffer acctSb = new StringBuffer("");
			for (Character s : updateUnionBlockCode.toCharArray()) {
				String c = s.toString();
				BlockLevel bl = facility.loadParameter(c, BlockCode.class).blockLevel;
				switch (bl) {
//				case All:
//					cardSb.append(c);
//					acctSb.append(c);
//					break;
//				case CARD:
//					cardSb.append(c);
//					break;
				case ACCT:
					acctSb.append(c);
					break;
				}
			}
			CcsAcct tmAccount = cpsBusProvide.getTmAccountTocardNbr(cardNo,
					accountType);
			CcsAcctO tmAccountO = cpsBusProvide.getTmAccountOTocardNbr(cardNo,
					accountType);
//			CcsCard tmCard = cpsBusProvide.getTmCardTocardNbr(cardNo);
//			CcsCardO tmCardO = cpsBusProvide.getTmCardOTocardNbr(cardNo);
			tmAccount.setBlockCode(/*BlockCodeUtil.unionBlockCodes(BlockCodeUtil
					.replace(tmAccount.getBlockCode(),
							BlockCodeUtil.unionBlockCodes(acctCode, bothCode)),*/
					acctSb.toString());//);
			tmAccountO.setBlockCode(acctSb.toString());
//			tmCard.setBlockCode(BlockCodeUtil.unionBlockCodes(
//					BlockCodeUtil.replace(tmCard.getBlockCode(),
//							BlockCodeUtil.unionBlockCodes(bothCode, cardCode)),
//					cardSb.toString()));
//			tmCardO.setBlockCode(BlockCodeUtil.unionBlockCodes(
//					BlockCodeUtil.replace(tmCardO.getBlockCode(),
//							BlockCodeUtil.unionBlockCodes(bothCode, cardCode)),
//					cardSb.toString()));
			rCccsAcct.save(tmAccount);
			rCcsAcctO.save(tmAccountO);
			// 操作完之后记录操作日志
			opeLogUtil.cardholderServiceLog("3404", null, cardNo, "锁定码维护",
					"锁定码由[" + contextCode + "]修改为[" + updateUnionTotal + "]");
		}
//		cardCode = cardBlockCode;
		acctCode = acctBlockCode;
//		bothCode = bothBlockCode;
	}

	/**
	 * 对锁定码C的单独处理
	 * 
	 * @param opt
	 *            操作码 0销卡，1销卡撤销
	 * @param cardNo
	 *            卡号
	 */
	private void S14120(String opt, String cardNo) {
		S14120Req req = new S14120Req();
		req.setCard_no(cardNo);
		req.setOpt(opt);
		operateService.S14120(req);
	}

	/**
	 * 对锁定码L的单独处理
	 * 
	 * @param opt
	 *            0：挂失，1：解挂
	 * @param cardNo
	 *            卡号
	 */
	private void S14051(String opt, String cardNo) {
		S14051Req req = new S14051Req();
		req.setCard_no(cardNo);
		req.setOpt(opt);
		operateService.S14051(req);
	}
}
