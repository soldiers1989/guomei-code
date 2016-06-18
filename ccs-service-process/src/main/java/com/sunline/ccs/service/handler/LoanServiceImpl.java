package com.sunline.ccs.service.handler;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.service.api.LoanService;
import com.sunline.ccs.service.handler.sub.TNQBeLoanBBal;
import com.sunline.ccs.service.handler.sub.TNQBeLoanRTxnList;
import com.sunline.ccs.service.handler.sub.TNQLoanBFeeDefList;
import com.sunline.ccs.service.handler.sub.TNQLoanCFeeDefList;
import com.sunline.ccs.service.handler.sub.TNQLoanList;
import com.sunline.ccs.service.handler.sub.TNQLoanRFeeDefList;
import com.sunline.ccs.service.handler.sub.TNQLoanRegHstList;
import com.sunline.ccs.service.handler.sub.TNQLoanRegList;
import com.sunline.ccs.service.handler.sub.TNRLoanAdvance;
import com.sunline.ccs.service.handler.sub.TNRLoanB;
import com.sunline.ccs.service.handler.sub.TNRLoanC;
import com.sunline.ccs.service.handler.sub.TNRLoanExtend;
import com.sunline.ccs.service.handler.sub.TNRLoanR;
import com.sunline.ccs.service.handler.sub.TNRLoanRegCancel;
import com.sunline.ccs.service.protocol.S12012Req;
import com.sunline.ccs.service.protocol.S12012Resp;
import com.sunline.ccs.service.protocol.S13000Req;
import com.sunline.ccs.service.protocol.S13000Resp;
import com.sunline.ccs.service.protocol.S13001Req;
import com.sunline.ccs.service.protocol.S13001Resp;
import com.sunline.ccs.service.protocol.S13002Req;
import com.sunline.ccs.service.protocol.S13002Resp;
import com.sunline.ccs.service.protocol.S13080Req;
import com.sunline.ccs.service.protocol.S13080Resp;
import com.sunline.ccs.service.protocol.S13081Req;
import com.sunline.ccs.service.protocol.S13081Resp;
import com.sunline.ccs.service.protocol.S13082Req;
import com.sunline.ccs.service.protocol.S13082Resp;
import com.sunline.ccs.service.protocol.S13083Req;
import com.sunline.ccs.service.protocol.S13083Resp;
import com.sunline.ccs.service.protocol.S13084Req;
import com.sunline.ccs.service.protocol.S13084Resp;
import com.sunline.ccs.service.protocol.S13085Req;
import com.sunline.ccs.service.protocol.S13085Resp;
import com.sunline.ccs.service.protocol.S13090Req;
import com.sunline.ccs.service.protocol.S13110Req;
import com.sunline.ccs.service.protocol.S13110Resp;
import com.sunline.ccs.service.protocol.S13120Req;
import com.sunline.ccs.service.protocol.S13120Resp;
import com.sunline.ccs.service.protocol.S13130Req;
import com.sunline.ccs.service.protocol.S13130Resp;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;


/** 
 * @see 类名：LoanServiceImpl
 * @see 描述：非金融后台服务分期类交易接口
 *
 * @see 创建日期：   2015年6月24日 下午3:09:06
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service("nfLoanServiceImpl")
public class LoanServiceImpl implements LoanService {
	Logger logger = LoggerFactory.getLogger(getClass());
	
/*    @Autowired
    private DownMsgFacility downMsgFacility;
*/    @Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
    
    @Autowired
    private RCcsCustomer rCcsCustomer;
	@PersistenceContext
	private EntityManager em;
	
	QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;
	
	@Autowired
	private TNRLoanR tnrLoanR;
	@Autowired
	private TNRLoanB tnrLoanB;
	@Autowired
	private TNRLoanC tnrLoanC;
	@Autowired
	private TNQLoanRFeeDefList tnqLoanRFeeDefList;
	@Autowired
	private TNQLoanBFeeDefList tnqLoanBFeeDefList;
	@Autowired
	private TNQLoanCFeeDefList tnqLoanCFeeDefList;
	@Autowired
	private TNQLoanRegList tnqLoanRegList;
	@Autowired
	private TNQLoanRegHstList tnqLoanRegHstList;
	@Autowired
	private TNRLoanRegCancel tnrLoanRegCancel;
	@Autowired
	private TNQLoanList tnqLoanList;
	@Autowired
	private TNQBeLoanRTxnList tnqBeLoanRTxnList;
	@Autowired
	private TNQBeLoanBBal tnqBeLoanBBal;
	@Autowired
	private TNRLoanExtend tnrLoanExtend;
	@Autowired
	private TNRLoanAdvance tnrLoanAdvance;
	
	
	@Override
	public S12012Resp S12012(S12012Req req) throws ProcessException {
		return tnrLoanB.handler(req);
	}
	
	@Override
	public S13000Resp S13000(S13000Req req) throws ProcessException {
		return tnqLoanRFeeDefList.handler(req);
	}
	
	@Override
	public S13001Resp S13001(S13001Req req) throws ProcessException {
		return tnqLoanBFeeDefList.handler(req);
	}
	
	@Override
	public S13002Resp S13002(S13002Req req) throws ProcessException {
		return tnqLoanCFeeDefList.handler(req);
	}
	
	@Override
	public S13080Resp S13080(S13080Req req) throws ProcessException {
		return tnqLoanList.handler(req);
	}
	
	@Override
	public S13081Resp S13081(S13081Req req) throws ProcessException {
		return tnqBeLoanRTxnList.handler(req);
	}
	
	@Override
	public S13082Resp S13082(S13082Req req) throws ProcessException {
		return tnrLoanR.handler(req);
	}
	
	@Override
	public S13083Resp S13083(S13083Req req) throws ProcessException {
		return tnqLoanRegList.handler(req);
	}
	
	@Override
	public S13084Resp S13084(S13084Req req) throws ProcessException {
		return tnrLoanC.handler(req);
	}
	
	@Override
	public S13085Resp S13085(S13085Req req) throws ProcessException {
		return tnqLoanRegHstList.handler(req);
	}
	
	@Override
	public void S13090(S13090Req req) throws ProcessException {
		tnrLoanRegCancel.handler(req);
	}

	@Override
	public S13110Resp S13110(S13110Req req) throws ProcessException {
		return tnqBeLoanBBal.handler(req);
	}
	
	@Override
	public S13120Resp S13120(S13120Req req) throws ProcessException {
		return tnrLoanAdvance.handler(req);
	}

	@Override
	public S13130Resp S13130(S13130Req req) throws ProcessException {
		return tnrLoanExtend.handler(req);
	}
	
}

