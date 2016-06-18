package com.sunline.ccs.service.api;

import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.service.protocol.S11020Req;
import com.sunline.ccs.service.protocol.S11020Resp;
import com.sunline.ccs.service.protocol.S12000Req;
import com.sunline.ccs.service.protocol.S12000Resp;
import com.sunline.ccs.service.protocol.S12010Req;
import com.sunline.ccs.service.protocol.S12010Resp;
import com.sunline.ccs.service.protocol.S12011Req;
import com.sunline.ccs.service.protocol.S12011Resp;
import com.sunline.ccs.service.protocol.S12050Req;
import com.sunline.ccs.service.protocol.S12050Resp;
import com.sunline.ccs.service.protocol.S13010Req;
import com.sunline.ccs.service.protocol.S13010Resp;
import com.sunline.ccs.service.protocol.S13020Req;
import com.sunline.ccs.service.protocol.S13020Resp;
import com.sunline.ccs.service.protocol.S13060Req;
import com.sunline.ccs.service.protocol.S13060Resp;
import com.sunline.ccs.service.protocol.S13070Req;
import com.sunline.ccs.service.protocol.S13070Resp;
import com.sunline.ccs.service.protocol.S14000Req;
import com.sunline.ccs.service.protocol.S14000Resp;
import com.sunline.ccs.service.protocol.S14001Req;
import com.sunline.ccs.service.protocol.S14001Resp;
import com.sunline.ccs.service.protocol.S14020Req;
import com.sunline.ccs.service.protocol.S14020Resp;
import com.sunline.ccs.service.protocol.S14021Req;
import com.sunline.ccs.service.protocol.S14021Resp;
import com.sunline.ccs.service.protocol.S14022Req;
import com.sunline.ccs.service.protocol.S14022Resp;
import com.sunline.ccs.service.protocol.S15013Req;
import com.sunline.ccs.service.protocol.S15013Resp;
import com.sunline.ccs.service.protocol.S15020Req;
import com.sunline.ccs.service.protocol.S15020Resp;

public interface QueryService {

	/**
	 * 4.1.2.	账户信息查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S12000Resp S12000(S12000Req req) throws ProcessException;

	/**
	 * 4.1.3.	可用额度信息查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S15020Resp S15020(S15020Req req) throws ProcessException;
	
	/**
	 * 4.1.4.	账单列表查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S12050Resp S12050(S12050Req req) throws ProcessException;

	/**
	 * 4.1.5.	账单汇总信息查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S12010Resp S12010(S12010Req req) throws ProcessException;


	/**
	 * 4.1.6.	账单交易明细查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S12011Resp S12011(S12011Req req) throws ProcessException;

	/**
	 * 4.1.7.	授权未入账交易明细查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S13010Resp S13010(S13010Req req) throws ProcessException;
	
	/**
	 * 授权交易历史查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S13020Resp S13020(S13020Req req)throws ProcessException;

	/**
	 * 4.1.8.	未出账单交易明细查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S13060Resp S13060(S13060Req req) throws ProcessException;

	/**
	 * 入账历史交易明细查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S13070Resp S13070(S13070Req req) throws ProcessException;
	
	/**
	 * 4.1.9.	按证件号码查询卡片列表
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S14000Resp S14000(S14000Req req) throws ProcessException;

	/**
	 * 4.1.10.	按主卡卡号查询附卡列表(含该主卡)
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S14001Resp S14001(S14001Req req) throws ProcessException;

	/**
	 * 4.1.11.	卡片信息查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S14020Resp S14020(S14020Req req) throws ProcessException;
	
	/**
	 * 固定费用查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S14021Resp S14021(S14021Req req) throws ProcessException;
	/**
	 * 查询卡片信息(通过手机号和卡号后四位)
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S11020Resp s11020(S11020Req req) throws ProcessException;
	
	/**
	 * 利息试算
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S14022Resp S14022(S14022Req req) throws ProcessException;
	
	/**
	 * 查询卡片信息(通过手机号和卡号后四位)
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S15013Resp S15013(S15013Req req) throws ProcessException;
	

}
