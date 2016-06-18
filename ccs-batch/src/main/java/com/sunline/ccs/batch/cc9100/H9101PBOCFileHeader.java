package com.sunline.ccs.batch.cc9100;

import java.util.Date;

import com.sunline.ark.support.cstruct.CChar;
/**
 * @see 类名：H9101PBOCFileHeader
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-24下午2:34:08
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class H9101PBOCFileHeader {
	
	/**
	 * 数据格式版本号
	 */
	@CChar(value=3, autoTrim=true, order=10)
	public String h8115;
	
	/**
	 * 金融机构代码
	 */
	@CChar(value=14, autoTrim=true, order=20)
	public String h6101;
	
	/**
	 * 报文生成时间
	 */
	@CChar(value=14, datePattern="yyyyMMddHHmmss", order=30)
	public Date h2402;
	
	/**
	 * 上传报文版本号
	 */
	@CChar(value=3, autoTrim=true, order=40)
	public String h8117;
	
	/**
	 * 重报提示
	 */
	@CChar(value=1, autoTrim=true, order=50)
	public String h8121;
	
	/**
	 * 报文类别
	 */
	@CChar(value=1, zeroPadding=true, order=60)
	public String h8119;
	
	/**
	 * 账户记录总数
	 */
	@CChar(value=10, zeroPadding=true, order=70)
	public int h8111;
	
	/**
	 * 最早结算/应还款日期
	 */
	@CChar(value=8, datePattern="yyyyMMdd", order=80)
	public Date h2412;
	
	/**
	 * 最晚结算/应还款日期
	 */
	@CChar(value=8, datePattern="yyyyMMdd", order=90)
	public Date h2414;
	
	/**
	 * 联系人
	 */
	@CChar(value=30, autoTrim=true, order=100)
	public String h5212;
	
	/**
	 * 联系电话
	 */
	@CChar(value=25, autoTrim=true, order=120)
	public String h3145;
	
	/**
	 * 预留字段
	 */
	@CChar(value=30, autoTrim=true, order=130)
	public String h8107;
	
	
}
