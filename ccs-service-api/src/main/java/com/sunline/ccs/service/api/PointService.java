package com.sunline.ccs.service.api;

import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.service.protocol.S17010Req;
import com.sunline.ccs.service.protocol.S17010Resp;
import com.sunline.ccs.service.protocol.S17011Req;
import com.sunline.ccs.service.protocol.S17011Resp;
import com.sunline.ccs.service.protocol.S17012Req;
import com.sunline.ccs.service.protocol.S17012Resp;
import com.sunline.ccs.service.protocol.S17020Req;
import com.sunline.ccs.service.protocol.S17020Resp;
import com.sunline.ccs.service.protocol.S17021Req;
import com.sunline.ccs.service.protocol.S17021Resp;
import com.sunline.ccs.service.protocol.S17030Req;
import com.sunline.ccs.service.protocol.S17030Resp;

/**
 * 非金融后台服务积分类交易接口
 * 
* @author fanghj
 * @date 2013-4-19 下午4:36:56
 * @version 1.0
 */
public interface PointService {
	/**
	 * 积分查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 * @throws Exception
	 */
	S17010Resp S17010(S17010Req req) throws ProcessException;
	
	/**
	 * 积分历史明细查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 * @throws Exception
	 */
	S17011Resp S17011(S17011Req req) throws ProcessException;
	
	/**
	 * 积分兑换明细查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 * @throws Exception
	 */
	S17012Resp S17012(S17012Req req) throws ProcessException;
	
	/**
	 * 积分兑换
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 * @throws Exception
	 */
	S17020Resp S17020(S17020Req req) throws ProcessException;
	
	/**
	 * 礼品信息查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 * @throws Exception
	 */
	S17030Resp S17030(S17030Req req) throws ProcessException;
	
	/**
	 * 积分兑换(礼品信息在行内时，用此接口)
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	S17021Resp S17021(S17021Req req) throws ProcessException;
}
