package com.sunline.ccs.service.auth.context;

/**
 * 
 * @see 类名：AuthBaseMsg
 * @see 描述：不分渠道的授权报文访问基类
 *
 * @see 创建日期：   2015年6月24日下午3:16:45
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public abstract class AuthBaseMsg {

	public abstract String field(int index);
	
	public abstract boolean exist(int index);
	
	/**
	 * 不同渠道的卡的认证标志不一样，故需各个品牌在自己的类实现这个方法
	 * @return
	 */
	public abstract String getThe3dsecureType();
	
	/**
	 * 
	 * @return
	 */
	public abstract String getMti();
	
	/**
	 * 商户ID b042MerId
	 * @return
	 */
	public abstract String getMerchantId();
	
	/**
	 * 受卡点（商户）b043
	 * @return
	 */
	public abstract String getAcqAddress();
}
