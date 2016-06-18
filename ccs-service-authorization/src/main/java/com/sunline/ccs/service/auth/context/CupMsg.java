package com.sunline.ccs.service.auth.context;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.utils.AuthStringUtils;
import com.sunline.ark.support.service.YakMessage;

/**
 * 
 * @see 类名：CupMsg
 * @see 描述：银联报文
 *
 * @see 创建日期：   2015年6月24日下午3:17:06
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class CupMsg extends AuthBaseMsg {

	private YakMessage message;

	/*
	 * [48域用法验证] - [48域AS用法子域验证,tag*2 , len*3 , 0/1是否定长] [57域用法验证] -
	 * [57域AS用法子域验证,tag*2 , len*3 , 0/1是否定长] [55域标签验证]
	 */
	private static final String[] b048AddMethod, b048AddMethodAS, b057Method, b057MethodAS, b055TagPrex, b055TagAll;

	public static final String METHOD_AS = "AS", TAG = "TAG", LEN = "LEN", VAL = "VAL", ORIG_LEN = "ORIG_LEN", TOTAL_LEN = "TOTAL_LEN", RECURR_PAY = "RECURR_PAY", CHB_NAME = "CHB_NAME",
			MOBI_NBR = "MOBI_NBR", DYN_CODE = "DYN_CODE" , METHOD_IP="IP";

	private Logger logger = LoggerFactory.getLogger(getClass());

//	@Autowired
//	private ProcessorCommonService pcs;

	static {
		b048AddMethod = new String[] { "CD2551", "AA5101", "BC0190", "NK5101", "IN2551", "PB004Amt0", "IP0621", "RA0120", "RP0601", "PZ1600" };
		b048AddMethodAS = new String[] { "AO0020", "PM0550", "ON0400", "AA0260", "IN2551", "IP0620", "RA0120", "RP0600", "PB004Amt0", "TA0010", "PZ1370", "IA0080" };

		b057Method = new String[] { "AB0981", "IP0831", "IC0981", "RP0721" };
		b057MethodAS = new String[] { "AB0981", "IP0831", "CI0981", "RP0721", "RA0120", "NA0120" };

		b055TagPrex = new String[] { "1F", "3F", "5F", "7F", "9F", "BF", "DF", "FF" };
		b055TagAll = new String[] { "9F26", "9F27", "9F10", "9F37", "9F36", "95", "9A", "9C", "9F02", "5F2A", "82", "9F1A", "9F03", "9F33", "9F34", "9F35", "9F1E", "84", "9F09", "9F41", "91", "71",
				"72", "DF31", "9F74", "9F63", "8A" };
	}

	public CupMsg() {
		super();
	}

	public CupMsg(YakMessage message) {
		this.message = message;
	}

	/**
	 * 获取指定域
	 */
	@Override
	public String field(int index) {
		if (message.getBodyAttributes().containsKey(index)) {
			// System.out.println("field= " + index + "  value= " +
			// message.getBodyAttributes().get(index));
			return (String) message.getBodyAttributes().get(index); // 如果非String这里报错也合理,因为TPS已经全部转成String
		}

		return null;
	}

	/**
	 * 指定域是否存在
	 */
	@Override
	public boolean exist(int index) {
		return StringUtils.isNotBlank(field(index));
	}
	
	/**
	 * 认证标志
	 */
	@Override
	public String getThe3dsecureType() {
		return getF060_2_8_Ebusiness();
	}
	
	/**
	 * @return 报文的MTI
	 */
	@Override
	public String getMti() {
		return (String) message.getCustomAttributes().get(CustomAttributesKey.MTI);
	}
	
	/**
	 * 商户id
	 */
	@Override
	public String getMerchantId() {
		return field(42);
	}

	/**
	 * 商户地址
	 */
	@Override
	public String getAcqAddress() {
		return field(43);
	}
	
	/**
	 * @return 报文的input-source
	 */
	public InputSource getReqInputSource(){
		return (InputSource) message.getCustomAttributes().get(CustomAttributesKey.INPUT_SOURCE);
	}

	/**
	 * 
	 * @see 方法名：getF003_substrFirstTwo 
	 * @see 描述：获取交易类型
	 * @see 创建日期：2015年6月26日下午6:32:57
	 * @author liruilin
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public String getF003_substrFirstTwo() {
		if (field(3) == null || (field(3) != null && field(3).length() < 2)) {
			return null;
		}
		return StringUtils.substring(field(3), 0, 2);
	}
	


	/**
	 * 
	 * @see 方法名：getF004_Amount 
	 * @see 描述：交易金额
	 * @see 创建日期：2015年6月26日下午6:33:23
	 * @author liruilin
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getF004_Amount() {
		if (StringUtils.isEmpty(field(4))) {
			return BigDecimal.ZERO;
		} else {
			return new BigDecimal(field(4));
		}

	}

	/**
	 * 持卡人扣账金额
	 * @see 方法名：getF006_Amount 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015年6月26日下午6:33:40
	 * @author liruilin
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal getF006_Amount() {
		if (StringUtils.isEmpty(field(6))) {
			return BigDecimal.ZERO;
		} else {
			return BigDecimal.valueOf(Long.valueOf(field(6)));
		}

	}
	
	//和getF022_1_PosPanEntMode内容相同，注释掉
//	public String getF022_substrFirstTwo() {
//		if (field(22) == null || (field(22) != null && field(22).length() < 2)) {
//			return null;
//		}
//		return StringUtils.substring(field(22), 0, 2);
//	}
	
	public String getF022_1_PosPanEntMode() {
		if (field(22) == null || (field(22) != null && field(22).length() < 2)) {
			return null;
		}
		return StringUtils.substring(field(22), 0, 2);
	}

	public String getF022_3_PosPinEntMode() {
		if (field(22) == null || (field(22) != null && field(22).length() < 3)) {
			return null;
		}
		return StringUtils.substring(field(22), 2, 3);
	}
	
	
	
	
	
	/**
	 * 32域判断受理机构是否银联境外
	 * 
	 * @return
	 */
	public boolean isF032_Crossborder() {
		if (StringUtils.isNotBlank(field(32))) {
			String f32_5 = field(32).substring(4, 5);
			String f32_5_8 = field(32).substring(4, 8);
			if (f32_5.equals("0") && !f32_5_8.equals("0000")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 33域判断发送机构是否银联境外
	 * 
	 * @return
	 */
	public boolean isF033_Crossborder() {
		if (StringUtils.isNotBlank(field(33))) {
			String f33_5 = field(33).substring(4, 5);
			String f33_5_8 = field(33).substring(4, 8);
			if (f33_5.equals("0") && !f33_5_8.equals("0000")) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 根据银联入网规范，银联境外交易均通过银联在香港的代理机构转接，所以应判断33域是否为00010344（特殊机构机构号）
	 * @param specialInstributions 特殊机构号
	 * @return true 银联境外交易 false 非银联境外
	 * @since 2.5.0
	 */
	public boolean isF033_Crossborder(List<String> specialInstributions){
		if (StringUtils.isNotBlank(field(33))) {
			if(specialInstributions != null){
				// 33域指定的 发送机构标识码 在指定的特殊机构号列表中
				for (String institution : specialInstributions) {
					if(field(33).equals(institution)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @see 方法名：getF035Track2Cardno 
	 * @see 描述：第二磁道数据 卡号
	 * @see 创建日期：2015年6月26日下午7:03:11
	 * @author liruilin
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public String getF035Track2Cardno() {
		String cardno = "";
		if (!StringUtils.isEmpty(field(35))) {
			String[] f035 = StringUtils.split(field(35), "=|d|D");
			// 因为规则引擎不能处理异常。所以这里对于二磁格式错（不含分隔符）直接对卡号返回null，由规则进行处理
			if (f035.length == 1) {
				return null;
			}
			cardno = f035[0];
		}
		return cardno;
	}

	/**
	 * 
	 * @see 方法名：getF035Track2Expiry 
	 * @see 描述：第二磁道数据 有效期
	 * @see 创建日期：2015年6月26日下午7:03:53
	 * @author liruilin
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public String getF035Track2Expiry() {
		String expiryDate = "";
		if (!StringUtils.isEmpty(field(35))) {
			String[] f035 = StringUtils.split(field(35), "=|d|D");
			if (f035.length > 1 && !StringUtils.isEmpty(f035[1])) {
				expiryDate = StringUtils.substring(f035[1], 0, 4);
			} else {
				// 因为规则引擎不能处理异常。所以这里对于二磁格式错（不含分隔符）直接对卡号返回null，由规则进行处理
				return null;
			}
		}
		return expiryDate;
	}
	
	
	/**
	 * 行内还款交易从48域里获取收款人姓名
	 * @return 返回收款人姓名 或者 null （当未上送48域姓名时）
	 * @since 2.5.0
	 */
	public String getF048_ReceiverName_Bank(){
		if(StringUtils.isNotBlank(field(48))){
			// 根据生产环境日志文件得到YakMessage中48域的值为16进制字符串需要先解码，才能获取到正确的姓名
			try {
				byte[] field48charBytes = Hex.decodeHex(field(48).toCharArray());
				// message-def.xml中指定的编码格式为GBK，这里写死为GBK
				return new String(field48charBytes,"GBK").trim();
			} catch (Exception e) {
				// 在这里出现异常说明上送48域出现问题，当作未上送处理
				return null;
			}
		}
		return null;
	}

	/***
	 * 检查48域中[method]用法是否存在
	 * 
	 * @param methodName
	 * @return
	 */
	public boolean existF48(String methodName) {
		try {
			return getF048_AddPriv().get(methodName) == null ? false : true;
		} catch (Exception e) {
			logger.debug("检查48域中[ " + methodName + " ]用法是否存在时出现异常");
			return false;
		}
	}
	
	/**
	 * 附加交易信息
	 * 
	 * @return {method , val[str/tlvMap(method,val)]}
	 * @throws AuthException
	 */
	public Map<String, Map<String, String>> getF048_AddPriv() throws AuthException {
		return formatAddDataPriv(field(48), 48);
	}
	
	/**
	 * 规则交叉检查48域（tag-length-value），未使用（暂时保留）
	 * 
	 * @return boolean
	 * @throws AuthException
	 */
	public boolean verify48tlv(String data) throws AuthException {
		try {
			Map<String, Map<String, String>> methodMap = formatAddDataPriv(data, 48);
			return (null != methodMap && methodMap.size() != 0);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 查询48域AS用法中的TLV
	 * 
	 * @param tag
	 * @return Map<String, String>() - KEY:TAG,LEN,VAL
	 */
	public Map<String, String> findF48TLV(String f48, String tag) {
		try {
			Map<String, Map<String, String>> f48Map = formatAddDataPriv(f48, 48);
			return f48Map == null ? null : f48Map.get(tag);
		} catch (AuthException e) {
			return null;
		}
	}
	

	/**
	 * 48域校验是否无卡自助开通、返回姓名、返回验证码
	 * 
	 * @param methodName
	 * @param methodVal
	 * @return
	 */
	public String f48AOVerify() {
		try {
			return getF048_AddPriv().get("AO").get(CupMsg.VAL);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 获取48域AS的IA子域值
	 * AIC2.7-银联升级
	 * @param methodName
	 * @param methodVal
	 * @return
	 */
	public String f48IAVerify() {
		try {
			return getF048_AddPriv().get("IA").get(CupMsg.VAL);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * 
	 * @see 方法名：getF049_TransactionCurrencyCode 
	 * @see 描述：交易货币代码
	 * @see 创建日期：2015年6月26日下午7:00:33
	 * @author liruilin
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Integer getF049_TransactionCurrencyCode() {
		if (StringUtils.isNotBlank(field(49))) {
			return Integer.valueOf(field(49));
		}
		return null;
	}
	
	

	/**
	 * 
	 * @see 方法名：getF051_CardholderBillingCurrencyCode 
	 * @see 描述：持卡人账户货币代码
	 * @see 创建日期：2015年6月26日下午7:00:51
	 * @author liruilin
	 *  
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Integer getF051_CardholderBillingCurrencyCode() {
		if (StringUtils.isNotBlank(field(51))) {
			return Integer.valueOf(field(51));
		}
		return null;
	}
	
	/**
	 * 解析B055域
	 * 
	 * @param b055
	 * @return 返回 Map
	 * @throws AuthException
	 */
	public Map<String, Map<String, String>> getF055Map(String f55) throws AuthException {
		try {
			Map<String, Map<String, String>> tlvMap = new HashMap<String, Map<String, String>>();
			while (true) {
				Map<String, String> tlv = formatF055(f55);
				String tag = StringUtils.substring(f55, 0, ArrayUtils.contains(b055TagPrex, f55.substring(0, 2)) ? 4 : 2);
				// 是否在规范tag集合中
				if (ArrayUtils.contains(b055TagAll, tag)) {
					tlvMap.put(tag, tlv);
				}
				// 判断是否到末尾，否则截取下一个TLV
				if (f55.length() == Integer.valueOf(tlv.get(TOTAL_LEN))) {
					break;
				} else {
					f55 = f55.substring(Integer.valueOf(tlv.get(TOTAL_LEN)));
				}
			}
			return tlvMap;
		} catch (Exception e) {
			throw new AuthException(AuthReason.TF02, true);
		}
	}

	/**
	 * 解55域单个TLV
	 * 
	 * @param f55
	 * @return Map{ TAG , LEN , VAL , TOTAL_LEN }
	 */
	public Map<String, String> formatF055(String f55) {
		String tag, val = null;
		Integer len, start, end;
		Map<String, String> tlvMap = new HashMap<String, String>();
		// 是否2字符
		start = ArrayUtils.contains(b055TagPrex, f55.substring(0, 2)) ? 4 : 2;
		end = start + 2;
		// tag值
		tag = StringUtils.substring(f55, 0, start);
		// length长度
		len = Integer.valueOf(f55.substring(start, end), 16);
		// 长度小于70时，无子域
		if (len < 80) {
			val = f55.substring(end, end + len * 2);
			tlvMap.put(TOTAL_LEN, tag.length() + 2 + val.length() + "");
		}
		// 长度大于70时，有子域，需要解
		if (len > 80) {
			// 获取子域位数
			int n = (len - 80) * 2;
			tlvMap.put(ORIG_LEN, len.toString());
			// 获取子域长度
			len = Integer.parseInt(f55.substring(end, end + n), 16);
			// 子域取值
			val = f55.substring(end + n, end + n + len * 2);
			tlvMap.put(TOTAL_LEN, tag.length() + 2 + n + val.length() + "");
		}
		// 填充数据
		tlvMap.put(TAG, tag);
		tlvMap.put(LEN, String.format("%02d", len));
		tlvMap.put(VAL, val);
		return tlvMap;
	}

	/**
	 * 封装55域
	 * 
	 * @param tlvList
	 * @return
	 * @throws AuthException
	 */
	public String f55package(Map<String, Map<String, String>> tlvMap) throws AuthException {
		StringBuffer f55 = new StringBuffer();
		// 遍历tlv集合
		Set<String> tlvTagSet = tlvMap.keySet();
		for (String tlvTag : tlvTagSet) {
			Map<String, String> tlv = tlvMap.get(tlvTag);
			// 添加TAG标签
			f55.append(tlvTag);
			int len = Integer.valueOf(null != tlv.get(ORIG_LEN) ? tlv.get(ORIG_LEN) : tlv.get(LEN));
			// 添加LEN长度
			if (len < 80) {
				// 长度小于80时，无逻辑判断
				f55.append(tlv.get(LEN));
			} else {
				Integer valLen = tlv.get(VAL).length() / 2;
				// 长度转hex;
				String hex = Integer.toHexString(valLen);
				// 长度封装逻辑
				if (valLen <= 127) {
					// 补零
					f55.append(hex.length() < 2 ? "0" + hex : hex);
				}
				if (valLen > 127 && valLen <= 255) {
					f55.append(81);
					f55.append(hex);
				}
				if (valLen > 255 && valLen <= 65535) {
					f55.append(82);
					// 补零
					f55.append(hex.length() < 4 ? "0" + hex : hex);
				}
				if (valLen > 65535) {
					throw new AuthException(AuthReason.TF02, true);
				}
			}
			// 添加VAL数据
			f55.append(tlv.get(VAL));
		}
		return f55.toString();
	}
	
	
	/**
	 * 附加交易信息
	 * 
	 * @return {method , val[str/tlvMap(method,val)]}
	 * @throws AuthException
	 */
	public Map<String, Map<String, String>> getF057_AddPriv() throws AuthException {
		return formatAddDataPriv(field(57), 57);
	}
	

	
	public String getF060_1_MsgReasonCode() {
		if (field(60) == null || (field(60) != null && field(60).length() < 4)) {
			return null;
		}
		return StringUtils.substring(field(60), 0, 4);
	}

	public String getF060_2_1_AcctHoldType() {
		if (field(60) == null || (field(60) != null && field(60).length() < 5)) {
			return null;
		}
		return StringUtils.substring(field(60), 4, 5);
	}

	public String getF060_2_2_TermAbility() {
		if (field(60) == null || (field(60) != null && field(60).length() < 6)) {
			return null;
		}
		return StringUtils.substring(field(60), 5, 6);
	}

	public String getF060_2_3_IcCond() {
		if (field(60) == null || (field(60) != null && field(60).length() < 7)) {
			return null;
		}
		return StringUtils.substring(field(60), 6, 7);
	}

	public String getF060_2_4_R() {
		if (field(60) == null || (field(60) != null && field(60).length() < 8)) {
			return null;
		}
		return StringUtils.substring(field(60), 7, 8);
	}

	public String getF060_2_5_TermType() {
		if (field(60) == null || (field(60) != null && field(60).length() < 10)) {
			return null;
		}
		return StringUtils.substring(field(60), 8, 10);
	}

	public String getF060_2_6_CrosNoPin() {
		if (field(60) == null || (field(60) != null && field(60).length() < 11)) {
			return null;
		}
		return StringUtils.substring(field(60), 10, 11);
	}

	public String getF060_2_7_ICSecu() {
		if (field(60) == null || (field(60) != null && field(60).length() < 12)) {
			return null;
		}
		return StringUtils.substring(field(60), 11, 12);
	}

	public String getF060_2_8_Ebusiness() {
		if (field(60) == null || (field(60) != null && field(60).length() < 14)) {
			return null;
		}
		return StringUtils.substring(field(60), 12, 14);
	}

	public String getF060_2_9_IntMethod() {
		if (field(60) == null || (field(60) != null && field(60).length() < 15)) {
			return null;
		}
		return StringUtils.substring(field(60), 14, 15);
	}

	public String getF060_3_1_SpecFeeType() {
		if (field(60) == null || (field(60) != null && field(60).length() < 17)) {
			return null;
		}
		return StringUtils.substring(field(60), 15, 17);
	}

	public String getF060_3_2_SpecFeeLv() {
		if (field(60) == null || (field(60) != null && field(60).length() < 18)) {
			return null;
		}
		return StringUtils.substring(field(60), 17, 18);
	}

	public String getF060_3_3_R() {
		if (field(60) == null || (field(60) != null && field(60).length() < 21)) {
			return null;
		}
		return StringUtils.substring(field(60), 18, 21);
	}

	public String getF060_3_4_PartAprSupp() {
		if (field(60) == null || (field(60) != null && field(60).length() < 22)) {
			return null;
		}
		return StringUtils.substring(field(60), 21, 22);
	}

	public String getF060_3_5_TxnEntMode() {
		if (field(60) == null || (field(60) != null && field(60).length() < 23)) {
			return null;
		}
		return StringUtils.substring(field(60), 22, 23);
	}

	public String getF060_3_6_TxnMedium() {
		if (field(60) == null || (field(60) != null && field(60).length() < 24)) {
			return null;
		}
		return StringUtils.substring(field(60), 23, 24);
	}

	public String getF060_3_7_ICAppId() {
		if (field(60) == null || (field(60) != null && field(60).length() < 25)) {
			return null;
		}
		return StringUtils.substring(field(60), 24, 25);
	}

	public String getF060_3_8_AcctSeltType() {
		if (field(60) == null || (field(60) != null && field(60).length() < 27)) {
			return null;
		}
		return StringUtils.substring(field(60), 25, 27);
	}

	public String getF060_3_9_AcctClass() {
		if (field(60) == null || (field(60) != null && field(60).length() < 28)) {
			return null;
		}
		return StringUtils.substring(field(60), 27, 28);
	}

	public String getF060_3_10_Product() {
		if (field(60) == null || (field(60) != null && field(60).length() < 28)) {
			return null;
		}
		return StringUtils.substring(field(60), 28);
	}

	public String getF061_1_IdNbr() {
		if (field(61) == null || (field(61) != null && field(61).length() < 22)) {
			return null;
		}
		return StringUtils.substring(field(61), 0, 22);
	}

	public String getF061_2_CvvResult() {
		if (field(61) == null || (field(61) != null && field(61).length() < 23)) {
			return null;
		}
		return StringUtils.substring(field(61), 22, 23);
	}

	public String getF061_3_PvvResult() {
		if (field(61) == null || (field(61) != null && field(61).length() < 24)) {
			return null;
		}
		return StringUtils.substring(field(61), 23, 24);
	}

	public String getF061_4_1_CardOrg() {
		if (field(61) == null || (field(61) != null && field(61).length() < 27)) {
			return null;
		}
		return StringUtils.substring(field(61), 24, 27);
	}

	public String getF061_4_2_Cvv2() {
		if (field(61) == null || (field(61) != null && field(61).length() < 30)) {
			return null;
		}
		return StringUtils.substring(field(61), 27, 30).trim();
	}

	public String getF061_4_3_Cvv2Result() {
		if (field(61) == null || (field(61) != null && field(61).length() < 31)) {
			return null;
		}
		return StringUtils.substring(field(61), 30, 31);
	}

	public String getF061_5_ArqcResult() {
		if (field(61) == null || (field(61) != null && field(61).length() < 32)) {
			return null;
		}
		return StringUtils.substring(field(61), 31, 32);
	}

	public String getF061_6_SecuData() {
		if (field(61) == null || (field(61) != null && field(61).length() < 32)) {
			return null;
		}
		return StringUtils.substring(field(61), 32);
	}

	/**
	 * 密码验证（密码信息存放 52 域和 53 域之中）
	 * 
	 * @return
	 */
	public boolean getF061_6_AM_PinCheckFlag() {
		return getF061_6_AM(5);
	}

	/**
	 * 卡有效期验证（卡有效期信息存放在 14 域之中）
	 * 
	 * @return
	 */
	public boolean getF061_6_AM_ExpireDateCheckFlag() {
		return getF061_6_AM(6);
	}

	/**
	 * 身份证件验证（身份证信息存放在 61.1 域之中）
	 * 
	 * @return
	 */
	public boolean getF061_6_AM_IDCheckFlag() {
		return getF061_6_AM(7);
	}

	/**
	 * 磁道信息验证（磁道信息存放在 35 域、36 域和45 域之中）
	 * 
	 * @return
	 */
	public boolean getF061_6_AM_TrackCheckFlag() {
		return getF061_6_AM(8);
	}

	/**
	 * 委托关系验证（委托关系信息存放在本用法之中）
	 * 
	 * @return
	 */
	public boolean getF061_6_AM_RecurringpayCheckFlag() {
		return getF061_6_AM(9);
	}

	/**
	 * CVN2 验证（CVN2 信息存放在 61.4 域之中）
	 * 
	 * @return
	 */
	public boolean getF061_6_AM_Cvv2CheckFlag() {
		return getF061_6_AM(10);
	}

	/**
	 * PVN 验证（发卡行通过文件向 CUPS 同步 PVN信息）
	 * 
	 * @return
	 */
	public boolean getF061_6_AM_PvnCheckFlag() {
		return getF061_6_AM(11);
	}

	/**
	 * 姓名验证（姓名信息存放在本用法之中）
	 * 
	 * @return
	 */
	public boolean getF061_6_AM_ChbNameCheckFlag() {
		return getF061_6_AM(12);
	}

	/**
	 * 手机号码验证（手机号码信息存放在本用法之中）
	 * 
	 * @return
	 */
	public boolean getF061_6_AM_MobileNbrCheckFlag() {
		return getF061_6_AM(13);
	}

	/**
	 * 动态验证码验证（动态验证码信息存放在本用法之中）
	 * 
	 * @return
	 */
	public boolean getF061_6_AM_DynCodeCheckFlag() {
		return getF061_6_AM(14);
	}

	/**
	 * 截取61.6域各个验证标识
	 * 
	 * @param b
	 * @return
	 */
	public boolean getF061_6_AM(int b) {
		String f = getF061_6_SecuData();
		try {
			return f.substring(0, 5).equals("CUPAM") && f.substring(b, ++b).equals("1") ? true : false;
		} catch (Exception e) {
			return false;
		}
	}
	


	/**
	 * 用于规则中,对姓名的非空判断
	 * 
	 * @return
	 */
	public String getF061_6_AM_Name() {
		try {
			return getF061_6_AM_TlvMap().get(CHB_NAME).get(VAL);
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * 用于规则中,对手机号的非空判断
	 * 
	 * @return
	 */
	public String getF061_6_AM_MOBI_NBR() {
		try {
			return getF061_6_AM_TlvMap().get(MOBI_NBR).get(VAL);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 用于规则中,对动态验证码的非空判断
	 * AIC2.7银联升级
	 * @return
	 */
	public String getF061_6_AM_DYN_CODE () {
		try {
			return getF061_6_AM_TlvMap().get(DYN_CODE).get(VAL);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 截取61.6域AM用法为空或全0
	 * @return
	 */
	public boolean isNullF061_6_AM(){
		String f = getF061_6_SecuData();
		try {
			// 61域=null 或  length<32
			if (f == null) {
				return true;
			}
			// 61.6中没有AM用法
			if (!f.substring(0, 5).equals("CUPAM")) {
				return true;
			}
			// 61.6AM用法全为0
			if (f.substring(5, 21).equals("0000000000000000")) {
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * 委托关系: KEY=RECURR_PAY; 姓名验证: KEY=CHB_NAME; 手机号码: KEY=MOBI_NBR; 动态验证码:
	 * KEY=DYN_CODE;
	 * 
	 * @return 如果存在就增加到MAP中 KEY = TAG, VAL_MAP = KEY{TAG , LEN , VAL}
	 * @throws AuthException
	 */
	public Map<String, Map<String, String>> getF061_6_AM_TlvMap() throws AuthException {
		String f616 = getF061_6_SecuData();
		try {
			// 判断头是否符合要求
			if (!f616.substring(0, 5).equals("CUPAM")) {
				return null;
			}
			// 截掉[处理中心标志-3位],[用法-2位],[位图-16位];
			int splitNbr = 3 + 2 + 16;
			// 获得TLV格式数据;(61.6域取值：getF061_6_SecuData或 字符串参数)
			f616 = f616.substring(splitNbr);
			// 返回的tlvMap
			Map<String, Map<String, String>> tlvMap = new HashMap<String, Map<String, String>>();
			// 存放[委托关系][姓名验证][手机号码][动态验证码]
			List<String> tagList = new ArrayList<String>();
			if (getF061_6_AM_RecurringpayCheckFlag()) {
				tagList.add(RECURR_PAY);
			}
			if (getF061_6_AM_ChbNameCheckFlag()) {
				tagList.add(CHB_NAME);
			}
			if (getF061_6_AM_MobileNbrCheckFlag()) {
				tagList.add(MOBI_NBR);
			}
			if (getF061_6_AM_DynCodeCheckFlag()) {
				tagList.add(DYN_CODE);
			}
			for (String tag : tagList) {
				Map<String, String> tlv = new HashMap<String, String>();
				String len = f616.substring(0, 3);
				String val = f616.substring(3);
				int x = Integer.valueOf(len);
				for (int i = 0 ; i < x; i++) {
					if (String.valueOf(val.charAt(i)).getBytes().length > 1)
						x--;
				}
				val = f616.substring(3, 3 + x);
				tlv.put(TAG, tag);
				tlv.put(LEN, len);
				tlv.put(VAL, val);
				tlvMap.put(tag, tlv);
				f616 = f616.substring(3 + x);
			}
			return tlvMap;
		} catch (Exception e) {
			throw new AuthException(AuthReason.TF02, true);
		}
	}

	/**
	 * 61.6域中NM用法截取持卡人姓名
	 * 
	 * @return
	 */
	public String getF061_6_NM_ChbName1() {
		String f = getF061_6_SecuData();
		try {
			return f.substring(0, 5).equals("CUPNM") ? AuthStringUtils.subStringByByte(f.substring(5), 30).trim() : null;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 61.6域中NM用法截取收款人姓名
	 * 
	 * @return
	 */
	public String getF061_6_NM_ChbName2() {
		String f = getF061_6_SecuData();
		try {
			return f.substring(0, 5).equals("CUPNM") ? f.replace(AuthStringUtils.subStringByByte(f, 35), "").trim() : null;
		} catch (Exception e) {
			return null;
		}
	}
	

	/**
	 * 原始报文类型
	 * 
	 * @return
	 */
	public String getF090_1_OrigMti() {
		if (field(90) == null || (field(90) != null && field(90).length() < 4)) {
			return null;
		}
		return StringUtils.substring(field(90), 0, 4);
	}

	/**
	 * 原始系统跟踪号
	 * 
	 * @return
	 */
	public String getF090_2_OrigSysTraceId() {
		if (field(90) == null || (field(90) != null && field(90).length() < 10)) {
			return null;
		}
		return StringUtils.substring(field(90), 4, 10);
	}

	/**
	 * 原始系统日期时间
	 * 
	 * @return
	 */
	public String getF090_3_OrigSysDateTime() {
		if (field(90) == null || (field(90) != null && field(90).length() < 20)) {
			return null;
		}
		return StringUtils.substring(field(90), 10, 20);
	}

	/**
	 * 原始受理机构标识码
	 * 
	 * @return
	 */
	public String getF090_4_OrigAcqInstId() {
		if (field(90) == null || (field(90) != null && field(90).length() < 31)) {
			return null;
		}
		String origF32 = StringUtils.substring(field(90), 20, 31);
		if(exist(32) && field(32).length() > 0){
			return origF32.substring(11-field(32).length());
		}
		return origF32;
	}

	/**
	 * 原始发送机构标识码
	 * 
	 * @return
	 */
	public String getF090_5_OrigFwdInstId() {
		if (field(90) == null || (field(90) != null && field(90).length() < 31)) {
			return null;
		}
		String origF33 = StringUtils.substring(field(90), 31);
		if(exist(33) && field(33).length() > 0){
			return origF33.substring(11-field(33).length());
		}
		return origF33;
	}

	public String getF121_1_RespRsn() {
		if (field(121) == null || (field(121) != null && field(121).length() < 1)) {
			return null;
		}
		return StringUtils.substring(field(121), 0, 1);
	}

	public String getF121_2_SmsDualExch() {
		if (field(121) == null || (field(121) != null && field(121).length() < 2)) {
			return null;
		}
		return StringUtils.substring(field(121), 1, 2);
	}

	public String getF121_3_CardType() {
		if (field(121) == null || (field(121) != null && field(121).length() < 3)) {
			return null;
		}
		return StringUtils.substring(field(121), 2, 3);
	}

	public String getF121_4_CupR() {
		if (field(121) == null || (field(121) != null && field(121).length() < 43)) {
			return null;
		}
		return StringUtils.substring(field(121), 3, 43);
	}

	public String getF121_5_transFee() {
		if (field(121) == null || (field(121) != null && field(121).length() < 43)) {
			return null;
		}
		return StringUtils.substring(field(121), 43);
	}

	public String getF122_1_MerchantBeniRate() {
		if (field(122) == null || (field(122) != null && field(122).length() < 6)) {
			return null;
		}
		return StringUtils.substring(field(121), 0, 6);
	}

	public String getF122_2_AcqIfo() {
		if (field(122) == null || (field(122) != null && field(122).length() < 6)) {
			return null;
		}
		return StringUtils.substring(field(121), 6);
	}

	

	

	

	/**
	 * Additional Data Private域解析
	 * 
	 * @param data
	 *            域数据
	 * @param b
	 *            域位置
	 * @throws AuthException
	 */
	public Map<String, Map<String, String>> formatAddDataPriv(String data, int b) throws AuthException {
		if (StringUtils.isBlank(data)) {
			return null;
		}
		Map<String, Map<String, String>> methodMap = new HashMap<String, Map<String, String>>();
		String len, method, val;
		try {
			data = new String(Hex.decodeHex(data.toCharArray()), "GBK");
			len = String.format("%03d", data.length() - 2);
			method = data.substring(0, 2);
			val = data.substring(2);

			// 判断是否为AS用法,待修改
			if (method.equals(METHOD_AS)) {
				methodMap = tlvSplit(val, b * 10);
			} else {
				// 域用法CD等
				if (!checkMethod(method, val, b)) {
					throw new AuthException(AuthReason.TF02, true);
				} else {
					Map<String, String> map = new HashMap<String, String>();
					map.put(TAG, method);
					map.put(LEN, len);
					map.put(VAL, val);
					methodMap.put(method, map);
				}
			}
		} catch (Exception e) {
			throw new AuthException(AuthReason.TF02, true);
		}

		return methodMap;
	}

	/**
	 * 解析48/57域tlv
	 * 
	 * @param tlv
	 * @return
	 * @throws AuthException
	 */
	public Map<String, Map<String, String>> tlvSplit(String tlv, int b) throws Exception {
		Map<String, Map<String, String>> tlvMap = new HashMap<String, Map<String, String>>();
		String tag, val;
		Integer len, totalLen;
		// 循环解析TLV
		while (true) {
			Map<String, String> map = new HashMap<String, String>();
			// 截取标签
			tag = tlv.substring(0, 2);
			// 截取长度
			len = Integer.valueOf(tlv.substring(2, 5));
			// 截取数据
			val = AuthStringUtils.subStringByByte(tlv.substring(5), len);
			// 截取总长度
			totalLen = 5 + val.length();
			// 验证子域格式
			if (!checkMethod(tag, val, b)) {
				throw new AuthException(AuthReason.TF02, true);
			} else {
				map.put(TAG, tag);
				map.put(VAL, val);
				map.put(LEN, String.format("%03d", len));
				tlvMap.put(tag, map);
			}
			// 判断长度
			if (totalLen >= tlv.length()) {
				break;
			} else {
				// 截取下一个TLV
				tlv = tlv.substring(totalLen);
			}
		}
		return tlvMap;
	}

	/**
	 * 
	 * 验证用法格式
	 * 
	 * @param len
	 * @param method
	 * @param val
	 * @param b
	 *            域位置
	 * @return
	 * @throws AuthException
	 */
	public Boolean checkMethod(String method, String val, int b) throws Exception {
		if (StringUtils.isEmpty(method) || StringUtils.isEmpty(val)) {
			throw new AuthException(AuthReason.TF02, true);
		}
		// 根据不同域取不同的验证数据
		String[] methodArray;
		switch (b) {
		case 48:
			methodArray = b048AddMethod;
			break;
		case 480:
			methodArray = b048AddMethodAS;
			break;
		case 57:
			methodArray = b057Method;
			break;
		case 570:
			methodArray = b057MethodAS;
			break;
		default:
			throw new AuthException(AuthReason.TF02, true);
		}

		for (int i = 0; i < methodArray.length; i++) {
			// 验证用法名称是否相同
			if (method.equals(methodArray[i].substring(0, 2))) {
				// 定长的长度不一致 || 超出非定长的长度
				if ((methodArray[i].substring(5).equals("0") && val.getBytes().length != Integer.valueOf(methodArray[i].substring(2, 5))) || val.getBytes().length > Integer.valueOf(methodArray[i].substring(2, 5))) {
					throw new AuthException(AuthReason.TF02, true);
				}
				return true;
			}
		}
		logger.warn("没有找到对应[{}]用法的数据", method);
		return false;
	}

	/**
	 * 字符串为null时转换成[""]
	 * 
	 * @param str
	 * @return
	 */
	public String checkDummyStr(String str) {
		return StringUtils.isNotEmpty(str) ? str : "";
	}


	
	/**
	 * 通过100域和32域区分行内外交易(2013-04-05系统优化改造)
	 * @return
	 */
	public InputSource getInputSource() {
		
		String f100 = field( 100 ) == null ? null : field( 100 );
		String f32 = field( 32 ) == null ? null : field( 32 );
		if( StringUtils.isNotBlank( f100 ) && StringUtils.isNotBlank( f32 ) ) {
			f100 = f100.substring( f100.length() - 8, f100.length() - 4 );
			f32 = f32.substring( f32.length() - 8, f32.length() - 4 );
			return f100.equals( f32 ) ? InputSource.BANK : InputSource.CUP ;
		}
		
		return InputSource.BANK;
		
	}
	
	/**
	 * [分期第二步]解析报文48域[分期期数,项目编号,手续费收取方式,商户号]
	 * @return
	 * @throws AuthException
	 */
	public LoanInfo getLoanforMessage() throws AuthException{
		logger.debug("# [分期第二步]解析报文48域[分期期数,项目编号,手续费收取方式,商户号] #");
		Map<String, Map<String, String>> tlvMap = getF048_AddPriv();
		if(tlvMap == null || tlvMap.size() == 0 ){
			return null;
		}
		Map<String, String> ipMap = tlvMap.get(CupMsg.METHOD_IP); 
		
		if( ipMap == null || ipMap.size() == 0 ){
			return null;
		}
		String ipVal = ipMap.get(VAL);
		
		LoanInfo loan = new LoanInfo();
		try {
			loan.setLoanInitTerm(Integer.valueOf(ipVal.substring(0, 2)));
			loan.setProgramId(ipVal.substring(2, 32).trim());
			loan.setMerchantId(exist(42) ? field(42).trim() : null);
			loan.setRefNbr(field(37));
			if(ipVal.substring(33, 34).equals("0")){
				loan.setLoanFeeMethod(LoanFeeMethod.F);
			}else if(ipVal.substring(33, 34).equals("1")){
				loan.setLoanFeeMethod(LoanFeeMethod.E);
			}else{
				logger.debug("无法根据报文获取[分期手续费收取方式],返回[TF02]");
				throw new AuthException(AuthReason.TF02, AuthAction.D);
			}
		} catch (Exception e) {
			throw new AuthException(AuthReason.TF02, AuthAction.D);
		}
		return loan;
	}
	

	public static void main(String[] args) throws AuthException {
//		String f = "CUPNM吕通栓                        111                           ";
		String f = "CUPNM啊啊啊                        打是撒                        ";
		String name2 = f.replace(AuthStringUtils.subStringByByte(f, 35), "");
		
		System.out.println(name2);
		
//		System.out.println(AuthUtils.subStringByByte(f.substring(5), 30).trim());
//		System.out.println(AuthUtils.subStringByByte(f.substring(30), 30).trim());
		
	}
}
