package com.sunline.ccs.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.service.api.MicroCreditService;
import com.sunline.ccs.service.handler.sub.TNMMcAcctClose;
import com.sunline.ccs.service.handler.sub.TNMMcDirectDebit;
import com.sunline.ccs.service.handler.sub.TNMMcFloatRate;
import com.sunline.ccs.service.handler.sub.TNMMcLoanActionVoid;
import com.sunline.ccs.service.handler.sub.TNQMcAcctInfo;
import com.sunline.ccs.service.handler.sub.TNQMcLoan;
import com.sunline.ccs.service.handler.sub.TNQMcLoanAction;
import com.sunline.ccs.service.handler.sub.TNQMcLoanFeeDefList;
import com.sunline.ccs.service.handler.sub.TNQMcLoanListByCardNbr;
import com.sunline.ccs.service.handler.sub.TNQMcLoanListByIdNo;
import com.sunline.ccs.service.handler.sub.TNQMcLoanProduct;
import com.sunline.ccs.service.handler.sub.TNQMcLoanReg;
import com.sunline.ccs.service.handler.sub.TNQMcRepayHst;
import com.sunline.ccs.service.handler.sub.TNQMcRepaySchedule;
import com.sunline.ccs.service.handler.sub.TNQMcRepayScheduleHst;
import com.sunline.ccs.service.handler.sub.TNQMcTxnHst;
import com.sunline.ccs.service.handler.sub.TNRMcLoanAdvance;
import com.sunline.ccs.service.handler.sub.TNRMcLoanExtend;
import com.sunline.ccs.service.handler.sub.TNRMcLoanShorten;
import com.sunline.ccs.service.protocol.S20001Req;
import com.sunline.ccs.service.protocol.S20001Resp;
import com.sunline.ccs.service.protocol.S20010Req;
import com.sunline.ccs.service.protocol.S20010Resp;
import com.sunline.ccs.service.protocol.S20011Req;
import com.sunline.ccs.service.protocol.S20011Resp;
import com.sunline.ccs.service.protocol.S20014Req;
import com.sunline.ccs.service.protocol.S20014Resp;
import com.sunline.ccs.service.protocol.S20020Req;
import com.sunline.ccs.service.protocol.S20020Resp;
import com.sunline.ccs.service.protocol.S20021Req;
import com.sunline.ccs.service.protocol.S20021Resp;
import com.sunline.ccs.service.protocol.S20022Req;
import com.sunline.ccs.service.protocol.S20022Resp;
import com.sunline.ccs.service.protocol.S20023Req;
import com.sunline.ccs.service.protocol.S20023Resp;
import com.sunline.ccs.service.protocol.S20024Req;
import com.sunline.ccs.service.protocol.S20024Resp;
import com.sunline.ccs.service.protocol.S20025Req;
import com.sunline.ccs.service.protocol.S20025Resp;
import com.sunline.ccs.service.protocol.S20026Req;
import com.sunline.ccs.service.protocol.S20026Resp;
import com.sunline.ccs.service.protocol.S20027Req;
import com.sunline.ccs.service.protocol.S20030Req;
import com.sunline.ccs.service.protocol.S20030Resp;
import com.sunline.ccs.service.protocol.S20040Req;
import com.sunline.ccs.service.protocol.S20040Resp;
import com.sunline.ccs.service.protocol.S20050Req;
import com.sunline.ccs.service.protocol.S20050Resp;
import com.sunline.ccs.service.protocol.S20060Req;
import com.sunline.ccs.service.protocol.S20060Resp;
import com.sunline.ccs.service.protocol.S20070Req;
import com.sunline.ccs.service.protocol.S20080Req;
import com.sunline.ccs.service.protocol.S20080Resp;
import com.sunline.ccs.service.protocol.S20090Req;
import com.sunline.ccs.service.protocol.S20090Resp;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：MicroCreditServiceImpl
 * @see 描述：小额贷款新增非金融服务实现
 *
 * @see 创建日期： 2015年6月26日上午11:08:00
 * @author yanjingfeng
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service("nfMicroCreditServiceImpl")
public class MicroCreditServiceImpl implements MicroCreditService {

    public Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TNQMcLoanProduct tnqMcLoanProduct;
    @Autowired
    private TNQMcLoanFeeDefList tnqMcLoanFeeDefList;
    @Autowired
    private TNRMcLoanAdvance tnrMcLoanAdvance;
    @Autowired
    private TNQMcAcctInfo tnqMcAcctInfo;
    @Autowired
    private TNQMcLoanListByCardNbr tnqMcLoanListByCardNbr;
    @Autowired
    private TNQMcLoan tnqMcLoan;
    @Autowired
    private TNQMcLoanListByIdNo tnqMcLoanListByIdNo;
    @Autowired
    private TNQMcRepaySchedule tnqMcRepaySchedule;
    @Autowired
    private TNQMcLoanAction tnqMcLoanAction;
    @Autowired
    private TNQMcRepayScheduleHst tnqMcRepayScheduleHst;
    @Autowired
    private TNQMcLoanReg tnqMcLoanReg;
    @Autowired
    private TNMMcLoanActionVoid tnmMcLoanActionVoid;
    @Autowired
    private TNMMcDirectDebit tnmMcDirectDebit;
    @Autowired
    private TNMMcFloatRate tnmMcFloatRate;
    @Autowired
    private TNQMcTxnHst tnqMcTxnHst;
    @Autowired
    private TNQMcRepayHst tnqMcRepayHst;
    @Autowired
    private TNMMcAcctClose tnmMcAcctClose;
    @Autowired
    private TNRMcLoanExtend tnrMcLoanExtend;
    @Autowired
    private TNRMcLoanShorten tnrMcLoanShorten;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20001(com.sunline.ccs
     * .service.protocol.S20001Req)
     */
    @Override
    public S20001Resp S20001(S20001Req req) throws ProcessException {
	return tnqMcLoanProduct.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20010(com.sunline.ccs
     * .service.protocol.S20010Req)
     */
    @Override
    public S20010Resp S20010(S20010Req req) throws ProcessException {
	return tnqMcLoanFeeDefList.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20011(com.sunline.ccs
     * .service.protocol.S20011Req)
     */
    @Override
    public S20011Resp S20011(S20011Req req) throws ProcessException {
	return tnrMcLoanAdvance.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20014(com.sunline.ccs
     * .service.protocol.S20014Req)
     */
    @Override
    public S20014Resp S20014(S20014Req req) throws ProcessException {
	return tnqMcAcctInfo.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20020(com.sunline.ccs
     * .service.protocol.S20020Req)
     */
    @Override
    public S20020Resp S20020(S20020Req req) throws ProcessException {
	return tnqMcLoanListByCardNbr.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20021(com.sunline.ccs
     * .service.protocol.S20021Req)
     */
    @Override
    public S20021Resp S20021(S20021Req req) throws ProcessException {
	return tnqMcLoan.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20022(com.sunline.ccs
     * .service.protocol.S20022Req)
     */
    @Override
    public S20022Resp S20022(S20022Req req) throws ProcessException {
	return tnqMcLoanListByIdNo.handler(req);
    }

    @Override
    public S20023Resp S20023(S20023Req req) throws ProcessException {
	return tnqMcRepaySchedule.handler(req);
    }

    @Override
    public S20024Resp S20024(S20024Req req) throws ProcessException {
	return tnqMcLoanAction.handler(req);
    }

    @Override
    public S20025Resp S20025(S20025Req req) throws ProcessException {
	return tnqMcRepayScheduleHst.handler(req);
    }

    @Override
    public S20026Resp S20026(S20026Req req) throws ProcessException {
	return tnqMcLoanReg.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20027(com.sunline.ccs
     * .service.protocol.S20027Req)
     */
    @Override
    public void S20027(S20027Req req) throws ProcessException {
	tnmMcLoanActionVoid.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20030(com.sunline.ccs
     * .service.protocol.S20030Req)
     */
    @Override
    public S20030Resp S20030(S20030Req req) throws ProcessException {
	return tnmMcDirectDebit.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20040(com.sunline.ccs
     * .service.protocol.S20040Req)
     */
    @Override
    public S20040Resp S20040(S20040Req req) throws ProcessException {
	return tnmMcFloatRate.handler(req);
    }

    @Override
    public S20050Resp S20050(S20050Req req) throws ProcessException {
	return tnqMcTxnHst.handler(req);
    }

    @Override
    public S20060Resp S20060(S20060Req req) throws ProcessException {
	return tnqMcRepayHst.handler(req);
    }

    @Override
    public void S20070(S20070Req req) throws ProcessException {
	tnmMcAcctClose.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20080(com.sunline.ccs
     * .service.protocol.S20080Req)
     */
    @Override
    public S20080Resp S20080(S20080Req req) throws ProcessException {
	return tnrMcLoanExtend.handler(req);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sunline.ccs.service.api.MicroCreditService#S20090(com.sunline.ccs
     * .service.protocol.S20090Req)
     */
    @Override
    public S20090Resp S20090(S20090Req req) throws ProcessException {
	return tnrMcLoanShorten.handler(req);
    }

}
