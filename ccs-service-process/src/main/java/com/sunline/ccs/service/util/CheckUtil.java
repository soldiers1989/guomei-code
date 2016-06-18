package com.sunline.ccs.service.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.exception.ProcessException;


/** 
 * @see 类名：CheckUtil
 * @see 描述：校验工具
 *
 * @see 创建日期：   2015年6月24日 下午2:50:09
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class CheckUtil {

	private static Logger log = LoggerFactory.getLogger(CheckUtil.class);
	private static int CARD_LENTH_16=16;
	private static int CARD_LENTH_19=19;
	
	/**
	 * 验证礼品编号不能为空和只能为数字
	 * @param giftNbr
	 * @throws ProcessException
	 */
	public static void checkGiftNbr(String giftNbr) throws ProcessException {
		if (StringUtils.isEmpty(giftNbr)) {
			throw new ProcessException(Constants.ERRB070_CODE,"礼品编号不能为空");
		}
		if (!StringUtils.isNumeric(giftNbr)) {
			throw new ProcessException(Constants.ERRB071_CODE,"礼品编号只能为数字类型");
		}
	}
	

	/**
	 * 判断对象是否为空
	 * 
	 * @param o
	 * @throws ProcessException
	 */
	public static void rejectNull(Object o) throws ProcessException {
		CheckUtil.rejectNull(o, null);
	}

	/**
	 * 判断对象是否为空，并给出错误信息
	 * 
	 * @param o
	 * @param errMsg
	 * @throws ProcessException
	 */
	public static void rejectNull(Object o, String errMsg) throws ProcessException {
		if (o == null) {
			if (errMsg == null) {
				throw new ProcessException("不允许为空");
			} else {
				throw new ProcessException(errMsg);
			}
		}
	}
	
	public static void rejectNull(Object o, String errorcode,String errMsg) throws ProcessException {
		if (o == null) {
			if (errMsg == null) {
				throw new ProcessException(errorcode,"不允许为空");
			} else {
				throw new ProcessException(errorcode,errMsg);
			}
		}
	}
	
	/**
	 * 判断对用不为空
	 * 
	 * @param o
	 * @param errMsg
	 * @throws ProcessException
	 */
	public static void rejectNotNull(Object o, String errorcode,String errMsg) throws ProcessException {
		if (o != null) {
			if (errMsg == null) {
				throw new ProcessException(errorcode,o + "不为空");
			} else {
				throw new ProcessException(errorcode,errMsg);
			}
		}
	}

	/**
	 * 判断对用不为空
	 * 
	 * @param o
	 * @param errMsg
	 * @throws ProcessException
	 */
	public static void rejectNotNull(Object o, String errMsg) throws ProcessException {
		if (o != null) {
			if (errMsg == null) {
				throw new ProcessException(o + "不为空");
			} else {
				throw new ProcessException(errMsg);
			}
		}
	}

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		return obj == null || obj.equals("");
	}

	/**
	 * 检查输入的卡号是否合法
	 * 
	 * @param CardNo
	 * @throws ProcessException
	 *             抛出卡号不能为空，卡号只能为数字类型
	 */
	public static void checkCardNo(String cardNbr) throws ProcessException {
		if (StringUtils.isEmpty(cardNbr)) {
			throw new ProcessException(Constants.ERRB002_CODE,"卡号不能为空");
		}
		if (!StringUtils.isNumeric(cardNbr)) {
			throw new ProcessException(Constants.ERRB002_CODE,"卡号只能为数字类型");
		}
		if (cardNbr.trim().length()!=CARD_LENTH_16 && cardNbr.trim().length()!=CARD_LENTH_19) {
			throw new ProcessException(Constants.ERRB108_CODE,Constants.ERRB108_MES);
		}
	}
	
	/**
	 * 校验借据号是否合法，长度小于20位s
	 * @param loan_receipt_nbr
	 */
	public static void checkLoanReceiptNbr(String dueBillNo) {
		if (StringUtils.isEmpty(dueBillNo) || dueBillNo.length() > 30) {
			throw new ProcessException(Constants.ERRL049_CODE, Constants.ERRL049_MES);
		}
	}
	
	/**
	 * 检查贷款产品编号是否合法，不能为空，只能是数字类型
	 * @param loanCode
	 * @throws ProcessException
	 */
	public static void checkLoanCode(String loanCode) throws ProcessException{
		if (StringUtils.isEmpty(loanCode)) {
			throw new ProcessException(Constants.ERRC010_CODE,Constants.ERRC010_MES);
		}
		if (!StringUtils.isNumeric(loanCode)) {
			throw new ProcessException(Constants.ERRC010_CODE,Constants.ERRC010_MES);
		}
	}
	
	/**
	 * 检查上送币种是否有效
	 * 
	 * @param currCd
	 * @param isDual
	 * @throws ProcessException
	 */
	public static void checkCurrCd(String currCd, boolean dualSupport) throws ProcessException {
		if(StringUtils.isBlank(currCd)) {
			throw new ProcessException(Constants.ERRB003_CODE,"币种不能为空");
		}
		//支持"000"为双币种
		if(dualSupport){
			if(!currCd.equals(Constants.DEFAULT_CURR_CD)
					&& !currCd.equals(AccountType.C.getCurrencyCode())
					&& !currCd.equals(AccountType.D.getCurrencyCode())){
				throw new ProcessException(Constants.ERRB003_CODE,Constants.ERRB003_MES);
			}
		//不支持"000"为双币种
		}else{
			if(!currCd.equals(AccountType.C.getCurrencyCode())
					&& !currCd.equals(AccountType.D.getCurrencyCode())){
				throw new ProcessException(Constants.ERRB003_CODE,Constants.ERRB003_MES);
			}
		}
	}

	/**
	 * 检查输入的客户号是否合法
	 * 
	 * @param long1
	 * @throws ProcessException
	 */
	public static void checkCustomer(Long long1) throws ProcessException {
		if (long1 == null) {
			throw new ProcessException("客户号不能为空");
		}
	}

	/**
	 * 检查输入的账户号是否合法
	 * 
	 * @param accountid
	 * @throws ProcessException
	 */
	public static void checkAccount(Integer acctNbr) throws ProcessException {
		if (acctNbr == null) {
			throw new ProcessException("账号不能为空");
		}
	}
	

	/**
	 * 检查输入的客户信息是否合法 checkCustomer
	 * 
	 * @param ccsCustomer
	 * @throws ProcessException
	 * @exception
	 * @since 1.0.0
	 */
	public static void checkInputCustomer(CcsCustomer ccsCustomer) throws ProcessException {
		// 判断客户号是否为空
		checkCustomer(ccsCustomer.getCustId());
		
		// 判断手机号码
		if (StringUtils.isBlank(ccsCustomer.getMobileNo())) {
			log.error("手机号不能为空[" + CodeMarkUtils.markMobile(ccsCustomer.getMobileNo()) + "]");
			throw new ProcessException("手机号不能为空");
		}

		if (!isPhone(ccsCustomer.getMobileNo())) {
			log.error("非法的手机号码[" + CodeMarkUtils.markMobile(ccsCustomer.getMobileNo()) + "]");
			throw new ProcessException("非法的手机号码");
		}

		if (StringUtils.isNotEmpty(ccsCustomer.getHomePhone())) {
			if (!(CheckUtil.isPhoneNr(ccsCustomer.getHomePhone()) || CheckUtil.isPhoneNrWithoutCode(ccsCustomer.getHomePhone()) || CheckUtil.isPhoneNrWithoutLine(ccsCustomer.getHomePhone()))) {
				log.error("非法的电话号码[" + CodeMarkUtils.markMobile(ccsCustomer.getHomePhone()) + "]");
				throw new ProcessException("电话号码输入错误，只能输入0XX-XXXXXXXX-XXX;XXXXXXXX;0XXXXXXXXXX");
			}
		}
	}

	/**
	 * 验证国内电话号码 格式：010-67676767，区号长度3-4位，必须以"0"开头，号码是7-8位
	 * */
	public static boolean isPhoneNr(String phoneNr) {
		String regex = "^[0]\\d{2,3}\\-\\d{7,8}|^[0]\\d{2,3}\\-\\d{7,8}-\\d{1,4}";
		return startCheck(regex, phoneNr);
	}

	/**
	 * 验证国内电话号码 格式：6767676, 号码位数必须是7-8位,头一位不能是"0"
	 * */
	public static boolean isPhoneNrWithoutCode(String phoneNr) {
		String reg = "^[1-9]\\d{6,7}";
		return startCheck(reg, phoneNr);
	}

	/**
	 * 验证国内电话号码 格式：0106767676，共11位或者12位，必须是0开头
	 * */
	public static boolean isPhoneNrWithoutLine(String phoneNr) {
		String reg = "^[0]\\d{10,11}";
		return startCheck(reg, phoneNr);
	}

	/**
	 * 验证国内身份证号码：15或18位，由数字组成，不能以0开头
	 * */
	public static boolean checkIdCard(String idNr) {
		String reg = "^[1-9](\\d{13}|\\d{16})\\w";

		return startCheck(reg, idNr);
	}

	/**
	 * 手机号码验证,11位，不知道详细的手机号码段，只是验证开头必须是1和位数
	 * */
	public static boolean isPhone(String cellPhoneNr) {
		String reg = "^[1][\\d]{10}";
		return startCheck(reg, cellPhoneNr);
	}

	/**
	 * 检验空白符
	 * */
	public static boolean checkWhiteLine(String line) {
		String regex = "(\\s|\\t|\\r)+";
		return startCheck(regex, line);
	}

	/**
	 * 检查邮政编码(中国),6位
	 * */
	public static boolean checkPostcode(String postCode) {
		String regex = "^[0-9]\\d{5}";
		return startCheck(regex, postCode);
	}

	/**
	 * 检查证件 checkID(这里用一句话描述这个方法的作用)
	 * 
	 * @param idType
	 *            证件类型
	 * @param idNo
	 *            证件号码
	 * @return boolean true 合法 false非法
	 * @exception
	 * @since 1.0.0
	 */
	public static boolean isIdNo(IdType idType, String idNo) {
		boolean flag = true;
		if (idType == null)
			return false;
		if (StringUtils.isBlank(idNo))
			return false;
		// 判断身份证是否合法
		if (idType == IdType.I) {
			return IdentificationCodeUtil.isIdentityCode(idNo);
		}
		return flag;
	}

	/**
	 * 正则表达式的检验 startCheck(这里用一句话描述这个方法的作用)
	 * 
	 * @param reg
	 * @param string
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	public static boolean startCheck(String reg, String string) {
		boolean tem = false;
		if (string == null) {
			return tem;
		}
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(string);
		tem = matcher.matches();
		return tem;
	}

	/**
	 * 字符串比较
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean equals(String s1, String s2) {
		return StringUtils.isNotBlank(s1) && s1.trim().equals(s2) ? true : false;
	}

	public static void main(String[] args) {
		try {
			checkMail(null);
			checkMail("xwh@163.com");
			
			System.out.println(CheckUtil.checkHomeOrCorpPhone("2021121", "021-2021121"));
			System.out.println(CheckUtil.checkHomeOrCorpPhone("021-2021121", "021-2021121"));
			System.out.println(CheckUtil.checkHomeOrCorpPhone("13412121212", "13412121212"));
			System.out.println(CheckUtil.checkHomeOrCorpPhone("051420211211", "0514-20211211"));
			System.out.println(CheckUtil.checkHomeOrCorpPhone("021-2021121", "2021121"));
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 生日与身份证交叉校验
	 * 
	 * @param birthday
	 * @param idNo
	 * @return
	 */
	public static boolean isBirthday(Date birthday, String idNo) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String birthdayStr;
		if (idNo.length() == 15) {
			birthdayStr = "19" + idNo.substring(6, 12);
		} else {
			birthdayStr = idNo.substring(6, 14);
		}
		return birthdayStr.equals(sdf.format(birthday)) ? true : false;
	}
	
	/**
	 * 验证账单年月
	 * @param stmtDate
	 * @throws ProcessException
	 */
	public static void checkStmtDate(String stmtDate, boolean currentSupport) throws ProcessException {
		if (StringUtils.isBlank(stmtDate)) {
			throw new ProcessException(Constants.ERRB078_CODE,Constants.ERRB078_MES);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		sdf.setLenient(false);
		
		try {
			sdf.parse(stmtDate);
		} catch (Exception e) {
			if(currentSupport && !Constants.DEFAULT_STMT_DATE.equals(stmtDate)){
				throw new ProcessException(Constants.ERRB079_CODE,Constants.ERRB079_MES);
			}
		}
	}
	
	/**
	 * 校验Mail地址是否合法
	 * 
	 * @param email
	 */
	public static void checkMail(String mail) throws ProcessException{
		Pattern p = Pattern.compile("(\\w|\\.|-)+@\\w+(\\.{1}+[a-z]{2,3}){1,2}$");
		if(StringUtils.isNotBlank(mail) && !p.matcher(mail).matches()){
			throw new ProcessException(Constants.ERRB019_CODE,Constants.ERRB019_MES);
		}
	}
	/**
	 * 额度和次数不能小于0
	 * @param number
	 * @throws ProcessException
	 */
	public static boolean  validateData(Number number)throws ProcessException{
		if(number == null)
			return false;
		//额度不能小于0
		if (number instanceof BigDecimal) {
			if (((BigDecimal) number).compareTo(BigDecimal.ZERO) < 0)
				throw new ProcessException(Constants.ERRB086_CODE,Constants.ERRB086_MES);
			else
				return true;
		} else if (number instanceof Integer) {
		//次数不能小于0	
			if (((Integer) number).compareTo(0) < 0) {
				throw new ProcessException(Constants.ERRB107_CODE,Constants.ERRB107_MES);
			}else
				return true;
		}
		//其他数据类型,TODO
	
		return false;
		
	}
	
	/**
	 * 
	 * 比较的符号
	 * 
	 */
	public enum DateCompareType{
		/**
		 * 大于
		 */
		Greater,
		/**
		 * 大于或者等于
		 */
		GreaterOrEquals,
		/**
		 * 等于
		 */
		Equals,
		
		/**
		 * 小于或者等于
		 */
		LessOrEquals,
		
		/**
		 * 小于
		 */
		Less;
	}
	
	/**
	 * <pre>
	 * 用于三个日期之间的比较
	 * 如果beginDate <= betweenDate <= endDate 则返回<code>true</code>
	 * 否则返回<code>false</code>
	 * </pre>
	 * @param beginDate
	 * @param betweenDate
	 * @param endDate
	 * @return
	 */
	public static boolean compareDate(Date beginDate ,Date betweenDate ,Date endDate){
        if(compareDate(beginDate, betweenDate,DateCompareType.LessOrEquals) && compareDate(endDate, betweenDate,DateCompareType.GreaterOrEquals))	
        	return true;
		return false;
	}
	/**
	 * 用可以指定比较类型的于两个日期之间的比较
	 * <pre>
	 *    如果有一个日期是<code>null</code>则返回<code>false</code>
	 *    可以指定所有的日期比较类型{@link DateCompareType}}
	 * </pre>
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public static boolean compareDate(Date beginDate,Date endDate,DateCompareType type){
		if (beginDate == null || endDate == null)
			return false;
		switch (type) {
		case Greater:
			if (beginDate.compareTo(endDate) > 0)
				return true;
			else
				return false;
		case GreaterOrEquals:
			if (beginDate.compareTo(endDate) >= 0)
				return true;
			else
				return false;
		case Equals:
			if (beginDate.equals(endDate))
				return true;
			else
				return false;
		case LessOrEquals:
			if (beginDate.compareTo(endDate) <= 0)
				return true;
			else
				return false;
		case Less:
			if (beginDate.compareTo(endDate) < 0)
				return true;
			else
				return false;
		default:
			// 应该不会出现
			throw new IllegalArgumentException("无效的日期比较类型");

		}
		
	}
	
	
	/**
		 * 在2.4版本中，在客户信息表中家庭号码字段区号和号码有“-”，在信息验证时如下判断
		 * <pre>
		 * (1234567, 0100-1234567) true 
		 * (12345678, 010-12345678) true 
		 * (010-12345678, 010-12345678)true
		 * (01012345678, 010-12345678)true
		 * </pre>
		 * @param reqPhone
		 * @param custPhone
		 * @return 
		 */
		public static boolean checkHomeOrCorpPhone(String reqPhone, String custPhone){
			if(custPhone==null)return false;
			String cust = StringUtils.replace(custPhone, "-", "");
			
			if(reqPhone.indexOf("-") != -1){
				return StringUtils.equals(reqPhone, custPhone);
			}else {
				int index = custPhone.indexOf("-");
				switch (reqPhone.length()) {
				case 7:
				case 8:
					String custLast = custPhone.substring(index+1);
					return StringUtils.equals(reqPhone, custLast);
				case 11:
				case 12:
					return StringUtils.equals(reqPhone, cust);
				default:
					return StringUtils.equals(reqPhone, custPhone);
				}
			}
		}
		
		/**
		 * 字段补位
		 * 
		 * @param str
		 * @param strLength
		 * @return
		 */
		public static String addCharForNum(String str, int strLength, String charStr) {
			int strLen = str.length();
			if (strLen < strLength) {
				while (strLen < strLength) {
					StringBuffer sb = new StringBuffer();
					sb.append(str).append(charStr);// 右补空格
					str = sb.toString();
					strLen = str.length();
				}
			}
			return str;
		}
}
