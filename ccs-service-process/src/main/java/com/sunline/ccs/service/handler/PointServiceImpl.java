package com.sunline.ccs.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.service.api.PointService;
import com.sunline.ccs.service.handler.sub.TNQGiftExchange;
import com.sunline.ccs.service.handler.sub.TNQGiftInfo;
import com.sunline.ccs.service.handler.sub.TNQPointsBal;
import com.sunline.ccs.service.handler.sub.TNQPointsExchange;
import com.sunline.ccs.service.handler.sub.TNQPointsExchangeBank;
import com.sunline.ccs.service.handler.sub.TNQPointsTxnList;
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
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：PointServiceImpl
 * @see 描述：非金融交易后台积分类交易实现
 *
 * @see 创建日期：   2015-6-25下午1:30:39
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service("nfPointServiceImpl")
public class PointServiceImpl implements PointService {
	@Autowired
	private TNQPointsBal tnqPointsBal;
	@Autowired
	private TNQPointsTxnList tnqPointsTxnList;
	@Autowired
	private TNQGiftExchange tnqGiftExchange;
	@Autowired
	private TNQPointsExchange tnqPointsExchange;
	@Autowired
	private TNQGiftInfo tnqGiftInfo;
	@Autowired
	private TNQPointsExchangeBank tnqPointsExchangeBank;

	@Override
	public S17010Resp S17010(S17010Req req) throws ProcessException {
	    return tnqPointsBal.handler(req);
	}

	@Override
	public S17011Resp S17011(S17011Req req) throws ProcessException {
	    return tnqPointsTxnList.handler(req);
	}

	@Override
	public S17012Resp S17012(S17012Req req) throws ProcessException {
	    return tnqGiftExchange.handler(req);
	    
	}
	@Override
	public S17020Resp S17020(S17020Req req) throws ProcessException {
	    return tnqPointsExchange.handler(req);
	}
	
	@Override
	public S17030Resp S17030(S17030Req req) throws ProcessException {
	    return tnqGiftInfo.handler(req);
	    
	}

	@Override
	public S17021Resp S17021(S17021Req req) throws ProcessException {
	   return tnqPointsExchangeBank.handler(req);
	    
	}
}
