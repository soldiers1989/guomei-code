package com.sunline.ccs.facility;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ppy.dictionary.enums.AccountType;

/**
 * 对交易要素处理时，使用到的公共方法
* @author fanghj
 *
 */
@Service
public class TxnUtils {
	
	/**
	 * 生成refnbr号
	 * 日期（yyMMddhhmmddSSS）+4位随机数+3位外部流水号末尾
	 * 
	 * @param time
	 * @return
	 */
	public String genRefnbr(Date batchDate, String servicesn) {
		String nowdate = null;
		String tmpdate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
		tmpdate = sdf.format(new Date());
		if(null != batchDate) {
			SimpleDateFormat sdft = new SimpleDateFormat("yyMMdd");
			nowdate = sdft.format(batchDate) + tmpdate.substring(tmpdate.length()-9) ;
		}else {
			nowdate = tmpdate;
		}
		//生成4位随机数
		int randonstr = (int) (8999*Math.random())+1000;
		if(StringUtils.isNotBlank(servicesn)) {
			return nowdate + String.valueOf(randonstr) + servicesn.substring(servicesn.length()-3);
		}else {
			randonstr = (int) (8999999*Math.random())+1000000;
			return nowdate + String.valueOf(randonstr);
		}
	}
	
	/**
	 * 生成refnbr号
	 *	账号+账户类型+批量日期
	 * 
	 * @param acctNo
	 * @return
	 */
	public String genRefnbr(Long acctNo, AccountType acctType, Date batchDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return acctNo + acctType.toString() + sdf.format(batchDate);
	}
	
	
	/**
	 * 获取refNbr= b007(后8位) +b038(后3位) +b037(12位)
	 * 
	 * @param b007
	 * @param b038
	 * @param b037
	 * @return
	 */
	public String getRefnbr(String b007, String b038, String b037) {
		return formatStr(b007, 8) + formatStr(b038, 3) + formatStr(b037, 12);
	}
	

	/**
	 * 泛型字符串并截取x位
	 * 
	 * @param str
	 * @param i
	 * @return
	 */
	private String formatStr(String str, int i) {
		StringBuffer sb = new StringBuffer(i);
		for (int j = 0; j < i; j++) {
			sb.append("0");
		}
		if(StringUtils.isNotBlank(str)){
			return sb.append(str.trim()).substring(sb.length() - i, sb.length());
		}else{
			return sb.toString();
		}
	}
	
	
	
}
