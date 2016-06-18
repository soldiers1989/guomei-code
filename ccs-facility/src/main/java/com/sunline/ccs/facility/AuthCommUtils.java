package com.sunline.ccs.facility;

import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.YakMessage;
import com.sunline.ppy.api.CustomAttributesKey;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;

/**
 * 
* @author fanghj
 *
 */
public class AuthCommUtils {
	
	private static final String TPS_VAL_INST_ACQUIR_ID = "99999999";
	private static final String TPS_VAL_INST_FORWARD_ID = "99999999";
	private static final String TPS_VAL_MERCHANT_ID = "999999999999999";
	public static final String TPS_VAL_MTI_ADJUST = "0200";
	public static final String TPS_VAL_MTI_ADJUST_VOID = "0420";
	public static final String TPS_CUST_KEY_OPERATOR = "cps.operatorID";
	public static final String TPS_CUST_KEY_TERMID = "cps.termid";
	public static final String TPS_CUST_KEY_TXNREFNO = "cps.cpstxnrefnbr";
	
	private static final String TPS_CUST_VAL_TERMID = "cps-service-impl";
	/**
	 * yakMessage生成所需参数,不需要对每个域都赋值，需要哪个赋哪个
* @author fanghj
	 *
	 */
	public static class MsgParameter {
		private String cardNo;
		private String b003;
		private String txnAmt;
		private String mcc;
		private String currCd;
		private Date busiDate;
		private String b025;
		private String txnCode;
		private String b038;
		private String b090;
		private String b037;
		
		public String getCardNo() {
			return cardNo;
		}

		public void setCardNo(String cardNo) {
			this.cardNo = cardNo;
		}

		public String getB003() {
			return b003;
		}

		public void setB003(String b003) {
			this.b003 = b003;
		}

		public String getTxnAmt() {
			return txnAmt;
		}

		public void setTxnAmt(String txnAmt) {
			this.txnAmt = txnAmt;
		}

		public String getMcc() {
			return mcc;
		}

		public void setMcc(String mcc) {
			this.mcc = mcc;
		}

		public String getCurrCd() {
			return currCd;
		}

		public void setCurrCd(String currCd) {
			this.currCd = currCd;
		}

		public Date getBusiDate() {
			return busiDate;
		}

		public void setBusiDate(Date busiDate) {
			this.busiDate = busiDate;
		}

		public String getB025() {
			return b025;
		}

		public void setB025(String b025) {
			this.b025 = b025;
		}

		public String getTxnCode() {
			return txnCode;
		}

		public void setTxnCode(String txnCode) {
			this.txnCode = txnCode;
		}

		public String getB038() {
			return b038;
		}

		public void setB038(String b038) {
			this.b038 = b038;
		}

		public String getB090() {
			return b090;
		}

		public void setB090(String b090) {
			this.b090 = b090;
		}

		public String getB037() {
			return b037;
		}

		public void setB037(String b037) {
			this.b037 = b037;
		}
	}
	
	/**
	 * 设置组装yakmessage的公共字段，这些域都是写死的
	 * @param msg YakMessage实例
	 * @param busiDate 业务日期
	 */
	private static void setCommonField(YakMessage msg,Date busiDate){
		Map<Integer, String> bodyAttributes = msg.getBodyAttributes();
		Map<String, Serializable> customAttributes = msg.getCustomAttributes();
		
		bodyAttributes.put(7, makeTimeTag()); //交易时间(客服的自然时间):b007 mmddHHMMss
		bodyAttributes.put(11, makeRandomData(6)); //系统流水号(客服人工授权流水号):b011 6位随机数
		bodyAttributes.put(32, TPS_VAL_INST_ACQUIR_ID); //收单机构id:b032 固定值
		bodyAttributes.put(33, TPS_VAL_INST_FORWARD_ID); //转发机构id:b033 固定值
		bodyAttributes.put(40, "898"); //终端类型码: AIC内管系统	
		bodyAttributes.put(42, TPS_VAL_MERCHANT_ID); //商户id :B042 固定值全9
		bodyAttributes.put(37, makeRandomData(12));//12位随机数
		
		/*---- yakmessage定义字段 ------*/
		
		customAttributes.put(CustomAttributesKey.INPUT_SOURCE, InputSource.BANK);
		customAttributes.put(CustomAttributesKey.MANUAL_AUTH_FLAG, ManualAuthFlag.A);
		customAttributes.put(TPS_CUST_KEY_OPERATOR, OrganizationContextHolder.getUsername()); //操作员ID
		customAttributes.put(TPS_CUST_KEY_TERMID, TPS_CUST_VAL_TERMID); //操作终端ID
		customAttributes.put(TPS_CUST_KEY_TXNREFNO, bodyAttributes.get(37)); //ref_nbr  =37域
		customAttributes.put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME, busiDate);
	}
	
	/**
	 * 生成现金分期当日撤销的请求报文
	 * @param msgParam
	 * @return
	 */
	public static YakMessage makeCashLoanReverseRequestMsg(MsgParameter msgParam){
		YakMessage msg = new YakMessage();
		Map<Integer, String> bodyAttributes = msg.getBodyAttributes();
		setCommonField(msg, msgParam.busiDate);
		bodyAttributes.put(2, msgParam.cardNo); //介质卡号:B002
		//交易类型: 消费(借调消费类分期类plan)/ 取现(借调取现类plan) /还款 (贷调 ):B003前两位=00 /01/ 21
		bodyAttributes.put(3, msgParam.b003);
		bodyAttributes.put(4, msgParam.txnAmt); //交易金额:b004，交易金额不带小数点，最右边两位视为小数位
		
		bodyAttributes.put(18, msgParam.mcc); //商户类型:B018 固定值全9
		bodyAttributes.put(25, msgParam.b025); //服务点条件码 00-正常提交 64=分期
		bodyAttributes.put(49, msgParam.currCd); //交易货币代码:b049
		bodyAttributes.put(38, msgParam.b038);//原授权码
		bodyAttributes.put(90, msgParam.b090);//7+11+32+33+MTI
	
		msg.getCustomAttributes().put(CustomAttributesKey.MTI, TPS_VAL_MTI_ADJUST_VOID);
		return msg;
	}
	
	/**
	 * 生成现金融交易的请求报文
	 * @param msgParam
	 * @return
	 */
	public static YakMessage makeCashLoanRequestMsg(MsgParameter msgParam){
		YakMessage msg = new YakMessage();
		setCommonField(msg, msgParam.getBusiDate());
		Map<Integer, String> bodyAttributes = msg.getBodyAttributes();
		
		bodyAttributes.put(2, msgParam.getCardNo()); //介质卡号:B002
		//交易类型: 消费(借调消费类分期类plan)/ 取现(借调取现类plan) /还款 (贷调 ):B003前两位=00 /01/ 21
		bodyAttributes.put(3, msgParam.getB003());
		bodyAttributes.put(4, msgParam.getTxnAmt()); //交易金额:b004，交易金额不带小数点，最右边两位视为小数位
		
		bodyAttributes.put(18, msgParam.getMcc()); //商户类型:B018：5999
		bodyAttributes.put(25, msgParam.getB025()); //服务点条件码 00-正常提交 64=分期
		bodyAttributes.put(49, msgParam.getCurrCd()); //交易货币代码:b049
		
		bodyAttributes.put(37, msgParam.getB037());//覆盖自动生成的随机数
		msg.getCustomAttributes().put(CustomAttributesKey.MTI, TPS_VAL_MTI_ADJUST);
		return msg;
	}
	
	/**
	 * 生成日期字符串，格式为MMddHHmmss
	 * @return
	 */
	private static String makeTimeTag(){
		SimpleDateFormat df = new SimpleDateFormat("MMddHHmmss");
		return df.format(new Date());
	}
	/**
	 * 生成给定长度的数字字符串
	 * @param length
	 * @return
	 */
	public static String makeRandomData(int length){
		StringBuffer sb = new StringBuffer("#");
		int sed = 1000000000;
		for(int i = 0;i<length;i++){
			sb.append("0");
		}
		DecimalFormat df = new DecimalFormat(sb.toString());
		Random r = new Random();
		return df.format(r.nextInt(sed)).substring(0,length);
	}
	public static void main(String[] args) {
		makeRandomData(12);
		int line= 0;
		try {
			for(int i = 0;i<100;i++){
				line=i;
				System.out.println(makeRandomData(12));
			}
		} catch (Exception e) {
			System.out.println(line+"------------");
			e.printStackTrace();
		}
	}
	/**
	 * 获取字符串的前N个字节，长度不够则左补指定字符
	 * @param value  字符串值
	 * @param length 获取字节长度
	 * @return
	 */
	public static byte[] leftPad(String value, int length, char filledChar) {
		if(length < 1) {
			return null;
		}
		
		byte[] retBytes = new byte[length];
		
		if(null == value) {
			value = "";
		}
		
		byte[] oriBytes = value.getBytes();
		int index = 0;
		
		int filledLength = length - oriBytes.length;
		if(filledLength > 0) {
			for( ; index < filledLength; index++) {
				retBytes[index] = (byte)filledChar;
			}
		} else {
			filledLength = 0;
		}
		
		for( ; index < length; index++) {
			retBytes[index] = oriBytes[index - filledLength];
		}
		
		return retBytes;
	}
	
	/**
	 * 获取字符串的前N个字节，长度不够则左补零
	 * @param value  字符串值
	 * @param length 获取字节长度
	 * @return
	 */
	public static byte[] leftPad(String value, int length) {
		return leftPad(value, length, '0');
	}
	
	/**
	 * 根据mti b011+b007+b032+b033生成90域
	 * @param mti
	 * @param b011
	 * @param b007
	 * @param b032
	 * @param b033
	 * @return
	 */
	public static String makeFeild90(String mti,String b011,String b007,String b032,String b033) {
		byte[] mtiBytes = MsgUtils.leftPad(mti, 4);
		byte[] b011Bytes = MsgUtils.leftPad(b011, 6);
		byte[] b007Bytes = MsgUtils.leftPad(b007, 10);
		byte[] b032Bytes = MsgUtils.leftPad(b032, 11);
		byte[] b033Bytes = MsgUtils.leftPad(b033, 11);
		ByteBuffer buffer = ByteBuffer.allocate(42);
		buffer.put(mtiBytes);
		buffer.put(b011Bytes);
		buffer.put(b007Bytes);
		buffer.put(b032Bytes);
		buffer.put(b033Bytes);
		buffer.flip();
		return new String(buffer.array());
	}
	
	/**
	 * <p>生成授权要用的金额值*100取整</p>
	 * @param 金额
	 * @return 金额*100
	 */
	public static String getAuthAmt(BigDecimal amount){
		DecimalFormat df = new DecimalFormat("#0");
		return df.format(amount.doubleValue()*100);
	}
	
	/**
	 * 
	 * @see 方法名：checkDateValid 
	 * @see 描述：检查时间是否有效
	 * @see 创建日期：2015年6月25日下午5:43:29
	 * @author liruilin
	 *  
	 * @param indicator 是否有效标志
	 * @param bizDate 业务日期
	 * @param begDate 有效起始时间
	 * @param endDate 有效结束时间
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public static boolean checkDateValid(Indicator indicator, Date bizDate, Date begDate, Date endDate){
		return indicator == Indicator.Y && begDate.compareTo(bizDate) < 1 && endDate.compareTo(bizDate) > -1;
	}
}
