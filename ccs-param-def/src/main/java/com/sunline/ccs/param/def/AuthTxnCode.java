package com.sunline.ccs.param.def;

import java.io.Serializable;

import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ark.support.meta.PropertyInfo;

/**
 * 交易码
 */
public class AuthTxnCode implements Serializable{

	private static final long serialVersionUID = 6828401848956690268L;

	/**
     * 交易代码
     */
    @PropertyInfo( name = "交易代码", length = 6 )
    public String txnCode;
   
    /**
     * 交易代码描述
     */
    @PropertyInfo( name = "描述", length = 40 )
    public String description;

	/**
     * 模板代码
     */
    @PropertyInfo( name = "模板代码", length = 5 )
    public String templateCode;

    /**
     * 组装key值
     * @param txnCode
     * @param inputSource
     * @return
     */
    public static String assemblingKey( String txnCode, InputSource inputSource ) {
    	return txnCode + "|" + inputSource;
    }
    
}
