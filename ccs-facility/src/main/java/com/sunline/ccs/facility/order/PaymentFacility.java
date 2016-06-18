package com.sunline.ccs.facility.order;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.serializable.JsonSerializeUtil;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.service.payEntity.MainReq;
import com.sunline.ccs.service.payEntity.PayCutPaymentQueryReq;
import com.sunline.ccs.service.payEntity.PayCutPaymentReq;
import com.sunline.ccs.service.payEntity.PaySinPaymentQueryReq;
import com.sunline.ccs.service.payEntity.PaySinPaymentReq;

/**
 * @author wangz
 *
 */
@Service
public class PaymentFacility {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());


	/**
	 * 设置 单笔代扣指令
	 * @param order
	 * @param req
	 */
	public String installPayCutPaymentCommand(CcsOrder order) {
		PayCutPaymentReq req1 = new PayCutPaymentReq();
		
		req1.setCardNo(order.getCardNo());//银行卡号或者存折号
		req1.setCardType(order.getCardType());//卡号或存折号标识位 (0表示卡,1表示折)
		req1.setCertId(order.getCertId());//证件号
		req1.setCertType(order.getCertType());//身份证01，军官证02  护照03，户口簿04 回乡证05  其他06(对公账户请选此类型，参数值任意传递)
		req1.setChannelDate(DateUtils.formatDate2String(
				order.getBusinessDate(), DateUtils.YYYYMMDD));//该笔交易发生的日期
		req1.setChannelSerial(order.getOrderId().toString());//交易日期 不满16位补0
		req1.setCuryId(order.getCurrency());//固定为156，表示货币单位为人民币
		req1.setOpenBankId(order.getOpenBankId());//开户行号
		req1.setPriv1(order.getPriv1());//商户保留域
		req1.setPurpose(order.getPurpose());//接口预留字段
		//支付要求金额必须有两位小数点
		String amt = order.getTxnAmt().toString();
		if(amt.contains(".")) {
			String[] amtArry = amt.split("\\.");
			amtArry[1] = StringUtils.rightPad(amtArry[1], 2, '0');
			amt = amtArry[0] + "." + amtArry[1];
		}else {
			amt = amt + ".00";
		}
		req1.setTransAmt(amt);//交易金额
//		req1.setTransAmt(order.getTxnAmt().toString());//交易金额
		req1.setUsrName(order.getUsrName());//持卡人姓名
		
		setPayHeader(order, req1);
		String json = JsonSerializeUtil.jsonSerializerNoNull(req1);
		
		if(logger.isDebugEnabled()){
			logger.debug("支付指令：[{}]",json);
		}
		
		return json;
	}

	/**
	 * 设置 单笔代付指令
	 * @param order
	 * @param req
	 */
	public String installPaySinPaymentCommand(CcsOrder order) {
		PaySinPaymentReq req1 = new PaySinPaymentReq();
		
		req1.setCardNo(order.getCardNo());
		req1.setChannelDate(DateUtils.formatDate2String(
				order.getBusinessDate(), DateUtils.YYYYMMDD));//该笔交易发生的日期
		req1.setChannelSerial(order.getOrderId().toString());//不满16位补0
		req1.setPurpose(order.getPurpose());//接口预留字段
		//支付要求金额必须有两位小数点
		String amt = order.getTxnAmt().toString();
		if(amt.contains(".")) {
			String[] amtArry = amt.split("\\.");
			amtArry[1] = StringUtils.rightPad(amtArry[1], 2, '0');
			amt = amtArry[0] + "." + amtArry[1];
		}else {
			amt = amt + ".00";
		}
		req1.setTransAmt(amt);//交易金额
		req1.setUsrName(order.getUsrName());//持卡人姓名
		req1.setOpenBank(order.getOpenBank());//开户银行名称
		req1.setProv(order.getState());//省
		req1.setCity(order.getCity());//市
		req1.setFlag(order.getFlag());//“00”对私，“01”对公 默认为00
		req1.setSubBank(order.getSubBank());
		
		setPayHeader(order, req1);
		String json = JsonSerializeUtil.jsonSerializerNoNull(req1);
		
		if(logger.isDebugEnabled()){
			logger.debug("支付指令：[{}]",json);
		}
		
		return json;
	}
	
	/**
	 * 设置 单笔代付查询指令
	 * @param order
	 * @param req
	 */
	public String installPaySinPaymentQueryCommand(CcsOrder order) {
		PaySinPaymentQueryReq req1 = new PaySinPaymentQueryReq();
		
		req1.setChannelDate(DateUtils.formatDate2String(
				order.getBusinessDate(), DateUtils.YYYYMMDD));//该笔交易发生的日期
		req1.setChannelSerial(order.getOrderId().toString());//不满16位补0
		
		setPayHeader(order, req1);
		String json = JsonSerializeUtil.jsonSerializerNoNull(req1);
		
		if(logger.isDebugEnabled()){
			logger.debug("支付指令：[{}]",json);
		}
		
		return json;
	}
	/**
	 * 设置 单笔代付查询指令
	 * @param order
	 * @param req
	 */
	public String installPayCutPaymentQueryCommand(CcsOrder order) {
		PayCutPaymentQueryReq req1 = new PayCutPaymentQueryReq();
		
		req1.setChannelDate(DateUtils.formatDate2String(
				order.getBusinessDate(), DateUtils.YYYYMMDD));//该笔交易发生的日期
		req1.setChannelSerial(order.getOrderId().toString());//
		
		setPayHeader(order, req1);
		String json = JsonSerializeUtil.jsonSerializerNoNull(req1);
		
		if(logger.isDebugEnabled()){
			logger.debug("支付指令：[{}]",json);
		}
		
		return json;
	}

	/**
	 * 组装支付报文公共部分
	 * @param order
	 * @param req1
	 */
	private void setPayHeader(CcsOrder order, MainReq req1) {
		req1.setChannelId(order.getPayChannelId());
		req1.setChannelBizCode(order.getPayBizCode());
	}
}
