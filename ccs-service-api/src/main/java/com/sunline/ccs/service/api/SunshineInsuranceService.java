package com.sunline.ccs.service.api;

import com.sunline.ark.support.meta.RPCVersion;
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
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 阳光保险贷服务类
* @author lizz
 *
 */
@RPCVersion(value="1.0.0")
public interface SunshineInsuranceService {
	
	/**
	 * 放款申请
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S30001LoanResp loan(S30001LoanReq req) throws ProcessException;
	
	/**
	 * 放款重提
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S30002RecommitResp recommit(S30002RecommitReq req) throws ProcessException;
	
	/**
	 * 订单重提
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S30003RecommitOrderResp recommitOrder(S30003RecommitOrderReq req) throws ProcessException;

	/**
	 * 付款审批
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S30005PaymentCheckResp paymentCheck(S30005PaymentCheckReq req) throws ProcessException;
	
	/**
	 * 结算重提
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S30004RecommitSettleResp recommitSettle(S30004RecommitSettleReq req) throws ProcessException;

	/**
	 * 预约提前还款 试算/申请
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S11001BookingResp booking(S11001BookingReq req) throws ProcessException;

	/**
	 * 代位追偿
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S32001SubrogationResp subrogation(S32001SubrogationReq req) throws ProcessException;
	
	/**
	 * 客户银行卡信息修改
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S10001AlterBankInfoResp alterBankInfo(S10001AlterBankInfoReq req) throws ProcessException;
	
	/**
	 * 实时代扣
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S31001PaymentResp payment(S31001PaymentReq req) throws ProcessException;
	
	/**
	 * 代付查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S39001PaySinPaymentQueryResp paySinPaymentQuery(S39001PaySinPaymentQueryReq req) throws ProcessException;

	/**
	 * 代扣查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S39002PayCutPaymentQueryResp payCutPaymentQuery(S39002PayCutPaymentQueryReq req) throws ProcessException;
	
	/**
	 * 结算查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S39003SettleQueryResp settleQuery(S39003SettleQueryReq req) throws ProcessException;


}
