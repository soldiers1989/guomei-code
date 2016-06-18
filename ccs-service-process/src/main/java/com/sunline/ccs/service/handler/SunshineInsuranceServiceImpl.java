package com.sunline.ccs.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.service.api.SunshineInsuranceService;
import com.sunline.ccs.service.entity.S10001AlterBankInfoReq;
import com.sunline.ccs.service.entity.S10001AlterBankInfoResp;
import com.sunline.ccs.service.entity.S11001BookingReq;
import com.sunline.ccs.service.entity.S11001BookingResp;
import com.sunline.ccs.service.entity.S30001LoanReq;
import com.sunline.ccs.service.entity.S30001LoanResp;
import com.sunline.ccs.service.entity.S30002RecommitReq;
import com.sunline.ccs.service.entity.S30002RecommitResp;
import com.sunline.ccs.service.entity.S30003RecommitOrderReq;
import com.sunline.ccs.service.entity.S30003RecommitOrderResp;
import com.sunline.ccs.service.entity.S30004RecommitSettleReq;
import com.sunline.ccs.service.entity.S30004RecommitSettleResp;
import com.sunline.ccs.service.entity.S30005PaymentCheckReq;
import com.sunline.ccs.service.entity.S30005PaymentCheckResp;
import com.sunline.ccs.service.entity.S31001PaymentReq;
import com.sunline.ccs.service.entity.S31001PaymentResp;
import com.sunline.ccs.service.entity.S32001SubrogationReq;
import com.sunline.ccs.service.entity.S32001SubrogationResp;
import com.sunline.ccs.service.entity.S39001PaySinPaymentQueryReq;
import com.sunline.ccs.service.entity.S39001PaySinPaymentQueryResp;
import com.sunline.ccs.service.entity.S39002PayCutPaymentQueryReq;
import com.sunline.ccs.service.entity.S39002PayCutPaymentQueryResp;
import com.sunline.ccs.service.entity.S39003SettleQueryReq;
import com.sunline.ccs.service.entity.S39003SettleQueryResp;
import com.sunline.ccs.service.entity.SunshineRequestInfo;
import com.sunline.ccs.service.handler.sunshine.TNRBankInfo;
import com.sunline.ccs.service.handler.sunshine.TNRBooking;
import com.sunline.ccs.service.handler.sunshine.TNRPayCutPaymentQuery;
import com.sunline.ccs.service.handler.sunshine.TNRPaySinPaymentQuery;
import com.sunline.ccs.service.handler.sunshine.TNRPayment;
import com.sunline.ccs.service.handler.sunshine.TNRPaymentCheck;
import com.sunline.ccs.service.handler.sunshine.TNRRecommit;
import com.sunline.ccs.service.handler.sunshine.TNRRecommitOrder;
import com.sunline.ccs.service.handler.sunshine.TNRRecommitSettle;
import com.sunline.ccs.service.handler.sunshine.TNRSettleQuery;
import com.sunline.ccs.service.handler.sunshine.TNRSubrogation;
import com.sunline.ccs.service.handler.sunshine.TNRSunshineLoan;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：SunshineInsuranceServiceImpl
 * @see 描述：马上-阳光保险贷新增非金融服务实现
 *
 * @see 创建日期： 2015年8月10日上午11:08:00
 * @author lizz
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class SunshineInsuranceServiceImpl implements SunshineInsuranceService {

    public Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private GlobalManagementService globalManageService;

    @Autowired
	private TNRSunshineLoan tnrSunshineLoan;
    
    @Autowired
    private TNRPayment payment;
    
    @Autowired
    private TNRBankInfo bankInfo;
    
    @Autowired
    private TNRBooking booking;
    
    @Autowired
    private TNRRecommit recommit;
    
    @Autowired
    private TNRRecommitOrder recommitOrder;
    
    @Autowired
    private TNRRecommitSettle recommitSettle;

    @Autowired
    private TNRSubrogation subrogation;
    
    @Autowired
    private TNRPaySinPaymentQuery paySinPaymentQuery;
    
    @Autowired
    private TNRPayCutPaymentQuery payCutPaymentQuery;

    @Autowired
    private TNRSettleQuery settleQuery;

    @Autowired
    private TNRPaymentCheck paymentCheck;
    
	@Override
	public S30001LoanResp loan(S30001LoanReq req) throws ProcessException {
		setEnv(req);
		return tnrSunshineLoan.handler(req);
	}

	@Override
	public S30002RecommitResp recommit(S30002RecommitReq req)
			throws ProcessException {
		setEnv(req);
		return recommit.handler(req);
	}

	@Override
	public S30003RecommitOrderResp recommitOrder(S30003RecommitOrderReq req)
			throws ProcessException {
		setEnv(req);
		return recommitOrder.handler(req);
	}
	
	@Override
	public S30004RecommitSettleResp recommitSettle(S30004RecommitSettleReq req)
			throws ProcessException {
		setEnv(req);
		return recommitSettle.handler(req);
	}
	
	@Override
	public S11001BookingResp booking(S11001BookingReq req)
			throws ProcessException {
		setEnv(req);
		return booking.handler(req);
	}

	@Override
	public S32001SubrogationResp subrogation(S32001SubrogationReq req)
			throws ProcessException {
		setEnv(req);
		return subrogation.handler(req);
	}

	@Override
	public S10001AlterBankInfoResp alterBankInfo(S10001AlterBankInfoReq req)
			throws ProcessException {
		setEnv(req);
		return bankInfo.handler(req);
	}

	@Override
	public S31001PaymentResp payment(S31001PaymentReq req) throws ProcessException {
		setEnv(req);
		return payment.handler(req);
	}
	@Override
	public S39001PaySinPaymentQueryResp paySinPaymentQuery(
			S39001PaySinPaymentQueryReq req) throws ProcessException {
		setEnv(req);
		return paySinPaymentQuery.handler(req);
	}

	@Override
	public S39002PayCutPaymentQueryResp payCutPaymentQuery(
			S39002PayCutPaymentQueryReq req) throws ProcessException {
		setEnv(req);
		return payCutPaymentQuery.handler(req);
	}
	
	@Override
	public S39003SettleQueryResp settleQuery(
			S39003SettleQueryReq req) throws ProcessException {
		setEnv(req);
		return settleQuery.handler(req);
	}
	/**
	 * 设置机构上下文
	 * @param req
	 */
	private void setEnv(SunshineRequestInfo req){
		// 获取机构ID
		String org = req.getOrg();
		// 获取操作员
		String opID = req.getOpId();

		// 放置机构上下文
		OrganizationContextHolder.setCurrentOrg(org);
		OrganizationContextHolder.setUsername(opID);
		
		if(InputSource.SUNS != req.getInputSource()){
			//TODO 渠道不是阳光的,需要拦截吗？
		}
	}

	@Override
	public S30005PaymentCheckResp paymentCheck(S30005PaymentCheckReq req)
			throws ProcessException {
		setEnv(req);
		return paymentCheck.hanlder(req);
	}


	

}
