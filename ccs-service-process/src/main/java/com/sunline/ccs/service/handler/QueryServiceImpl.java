package com.sunline.ccs.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.service.api.QueryService;
import com.sunline.ccs.service.handler.sub.TNQAcctInfo;
import com.sunline.ccs.service.handler.sub.TNQAuthmemoHstList;
import com.sunline.ccs.service.handler.sub.TNQAuthmemoList;
import com.sunline.ccs.service.handler.sub.TNQCardInfo;
import com.sunline.ccs.service.handler.sub.TNQCardList;
import com.sunline.ccs.service.handler.sub.TNQCardNbr;
import com.sunline.ccs.service.handler.sub.TNQCrLimitProgress;
import com.sunline.ccs.service.handler.sub.TNQOTBInfo;
import com.sunline.ccs.service.handler.sub.TNQProductFee;
import com.sunline.ccs.service.handler.sub.TNQStatementList;
import com.sunline.ccs.service.handler.sub.TNQStmt;
import com.sunline.ccs.service.handler.sub.TNQStmtTxnList;
import com.sunline.ccs.service.handler.sub.TNQSuppCardList;
import com.sunline.ccs.service.handler.sub.TNQTxnHstList;
import com.sunline.ccs.service.handler.sub.TNQUnstmtTxnList;
import com.sunline.ccs.service.handler.sub.TNTInterest;
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
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：QueryServiceImpl
 * @see 描述：非金融后台服务查询类交易接口
 *
 * @see 创建日期：   2015-6-25下午2:32:33
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */

@Service("nfQueryServiceImpl")
public class QueryServiceImpl implements QueryService {
	@Autowired
	private TNQAcctInfo tnqAcctInfo;
	@Autowired
	private TNQOTBInfo tnqOTBInfo;
	@Autowired
	private TNTInterest tntInterest;
	@Autowired
	private TNQStatementList tnqStatementList;
	@Autowired
	private TNQStmt tnqStmt;
	@Autowired
	private TNQStmtTxnList tnqStmtTxnList;
	@Autowired
	private TNQAuthmemoList tnqAuthmemoList;
	@Autowired
	private TNQAuthmemoHstList tnqAuthmemoHstList;
	@Autowired
	private TNQUnstmtTxnList tnqUnstmtTxnList;
	@Autowired
	private TNQTxnHstList tnqTxnHstList;
	@Autowired
	private TNQCardList tnqCardList;
	@Autowired
	private TNQSuppCardList tnqSuppCardList;
	@Autowired
	private TNQCardInfo tnqCardInfo;
	@Autowired
	private TNQProductFee tnqProductFee;
	@Autowired
	private TNQCardNbr tnqCardNbr;
	@Autowired
	private TNQCrLimitProgress tnqCrLimitProgress;

	@Override
	public S12000Resp S12000(S12000Req req) throws ProcessException {
	    return tnqAcctInfo.handler(req);
	}

	@Override
	public S15020Resp S15020(S15020Req req) throws ProcessException {
       return tnqOTBInfo.handler(req);
	}

	public S14022Resp S14022(S14022Req req) throws ProcessException {
		return tntInterest.handler(req);
	}

	@Override
	public S12050Resp S12050(S12050Req req) throws ProcessException {
        return tnqStatementList.handler(req);
	}
	
	@Override
	public S12010Resp S12010(S12010Req req) throws ProcessException {
	    return tnqStmt.handler(req);
	}

	@Override
	public S12011Resp S12011(S12011Req req) throws ProcessException {
	    return tnqStmtTxnList.handler(req);
	}

	@Override
	public S13010Resp S13010(S13010Req req) throws ProcessException {
	    return tnqAuthmemoList.handler(req);
	}

	@Override
	public S13020Resp S13020(S13020Req req) throws ProcessException {
	    return tnqAuthmemoHstList.handler(req);
	}

	@Override
	public S13060Resp S13060(S13060Req req) throws ProcessException {
	    return tnqUnstmtTxnList.handler(req);
	}

	@Override
	public S13070Resp S13070(S13070Req req) throws ProcessException {
	    return tnqTxnHstList.handler(req);
	}

	@Override
	public S14000Resp S14000(S14000Req req) throws ProcessException {
	    return tnqCardList.handler(req);
	}

	@Override
	public S14001Resp S14001(S14001Req req) throws ProcessException {
	    return tnqSuppCardList.handler(req);
	}

	@Override
	public S14020Resp S14020(S14020Req req) throws ProcessException {
	    return tnqCardInfo.handler(req);
	}

	@Override
	public S14021Resp S14021(S14021Req req) throws ProcessException {
	    return tnqProductFee.handler(req);
	}

	@Override
	public S11020Resp s11020(S11020Req req) throws ProcessException {
	    return tnqCardNbr.handler(req);
	}

	@Override
	public S15013Resp S15013(S15013Req req) throws ProcessException {
	    return tnqCrLimitProgress.handler(req);
	}
	

}
