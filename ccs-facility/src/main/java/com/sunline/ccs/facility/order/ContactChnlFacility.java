package com.sunline.ccs.facility.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.param.def.enums.AcqIdContactChnl;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 获取对账信息
 * @author wanghl
 *
 */
public class ContactChnlFacility {
	private static Logger logger = LoggerFactory.getLogger(ContactChnlFacility.class);

	/**
	 * 根据serviceId acqId 设置订单的对账方和是否对账
	 * @param serviceId
	 * @param acqId
	 * @param order
	 */
	public static void setContactChnl(String serviceId, String acqId, CcsOrder order) {
		if(acqId == null) throw new ProcessException("方法参数：机构编码acqId不能为空");
//		if(serviceId == null) throw new ProcessException("方法参数：交易编码serviceId不能为空");
		if(order == null) throw new ProcessException("方法参数：订单order不能为空");
		//获取支付类型
		PayType payType = judgePayType(serviceId, order.getLoanUsage());
		logger.info("交易:"+payType.desc);
		
		//设置支付类型对应的对账渠道，以及判断是否强制不对账
		if(payType == PayType.notCheck) {
			order.setMatchInd(Indicator.N);
		}else{
			
			//获取交易方AcqId对账信息
			AcqIdContactChnl ac = null;
			try {
				ac = AcqIdContactChnl.valueOf("E" + acqId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(ac == null) {
				logger.warn("未知的交易方AcqId[{}],设置默认对账信息", acqId);
				setDefaultContactChnl(order, acqId, payType);
				return;
			}else{
				//对应支付类型的对账方
				String contactChnl = getContactChnl(ac, payType);
				if(contactChnl == null){
					order.setMatchInd(Indicator.N);
				}else{
					order.setContactChnl(contactChnl);
					Indicator isForceNotCheckOrder = getNotCheckOrderInd(ac, payType);
					if(Indicator.Y == isForceNotCheckOrder) 
						order.setMatchInd(Indicator.N);
				}
			}
		}
		
	}
	
	public static void setTransFlowMatchChnl(String serviceId, String acqId, CcsOrder order){
		if(AcqIdContactChnl.E20000005.getAcqId().equals(acqId)){
			
			String[] servIds = new String[]{
					//放款撤销接口
					"TFDWithDrawVoid",
					//商品贷退货接口
					"TFCRefund" 
			};
			for(int i=0; i<servIds.length; i++){
				if(servIds[i].equals(serviceId)){
					order.setContactChnl(AcqIdContactChnl.E20000005.getAcqId());
					order.setMatchInd(Indicator.Y);
				}
			}
		}
	}

	/**
	 * 未知来源渠道的的默认配置
	 * @param order
	 * @param acqId
	 * @param payType
	 */
	private static void setDefaultContactChnl(CcsOrder order, String acqId, PayType payType) {
		switch (payType) {
		case directCut: 
		case directLoan: 
			order.setContactChnl(AcqIdContactChnl.E10000000.getAcqId());
			order.setMatchInd(Indicator.Y);
			break;
		case externalCut: 
		case externalLoan: 
			order.setContactChnl(acqId);
			order.setMatchInd(Indicator.N);
			break;
		case notCheck: 
			order.setMatchInd(Indicator.N);
			break;
		default: 
			throw new ProcessException("支付类型"+payType.name()+"无指定操作");
		}
	}

	/**
	 * 获取对应支付类型是否不对账标志
	 * @param ac
	 * @param payType
	 * @return
	 */
	private static Indicator getNotCheckOrderInd(AcqIdContactChnl ac,
			PayType payType) {
		switch (payType) {
		case directCut: return ac.getDirectCutNoCheckInd();
		case directLoan: return ac.getDirectLoanNoCheckInd();
		case externalCut: return ac.getExternalCutNoCheckInd();
		case externalLoan: return ac.getExternalLoanNoCheckInd();
		case notCheck: return Indicator.Y;
		default: 
			throw new ProcessException("支付类型"+payType.name()+"无指定操作");
		}
	}

	/**
	 * 获取对应支付类型的对账方
	 * @param AcqIdContactChnl ac
	 * @param PayType payType
	 * @return
	 */
	private static String getContactChnl(AcqIdContactChnl ac, PayType payType) {
		switch (payType) {
		case directCut: return ac.getDirectCutCoAcqId();
		case directLoan: return ac.getDirectLoanCoAcqId();
		case externalCut: return ac.getExternalCutCoAcqId();
		case externalLoan: return ac.getExternalLoanCoAcqId();
		case notCheck: return null;
		default: 
			throw new ProcessException("支付类型"+payType.name()+"无指定操作");
		}
		
	}

	/**
	 * 根据ServiceId判断交易的支付类型
	 * 未知的交易，若订单用途不在已知的非对账订单用途中，抛异常
	 * @param serviceId
	 * @param loanUsage
	 * @return
	 */
	private static PayType judgePayType(String serviceId, LoanUsage loanUsage) {
		if("TFDWithDraw".equals(serviceId)){
			return PayType.directLoan;
		}else if("TFDRemainderTransfer".equals(serviceId)){
			return PayType.directLoan;
		}else if("TFCRepay".equals(serviceId)){
			return PayType.directCut;
		}else if("TNRAAcctSetupWithDraw".equals(serviceId)){
			return PayType.directLoan;
		}else if("TFDCommodyLoanWithDraw".equals(serviceId)){
			return PayType.externalLoan;
		}else if("TFNCommodyLoanSetupWithDraw".equals(serviceId)){
			return PayType.externalLoan;
		}else if("TFCTerminalRepay".equals(serviceId)){
			return PayType.externalCut;
		}else if("TFCRefund".equals(serviceId)){
			return PayType.externalCut;
		}else if("30001".equals(serviceId)){
			return PayType.directLoan;
		}else if("30002".equals(serviceId)){
			return PayType.directLoan;
		}else if("31001".equals(serviceId)){
			return PayType.directCut;
		}else if("TFCCLSWithholding".equals(serviceId)){
			return PayType.directCut;
		}else if("TFCWithholding".equals(serviceId)){
			return PayType.directCut;
		}else if("32001".equals(serviceId)){
			return PayType.directCut;
		}else if("TFRLargeCaseLoan".equals(serviceId)){
			return PayType.directLoan;	
		}else{
			switch (loanUsage) {
			case V:
			case G:
			case F:
			case E:
			case Q:
				return PayType.notCheck;
			default:
				throw new ProcessException(MsRespCode.E_1077.getCode(), MsRespCode.E_1077.getMessage().replace("N", serviceId));
			}
		}
	}

	private enum PayType {

		/**
		 * 外部放款
		 */
		externalLoan("外部放款"),
		/**
		 * 直接放款
		 */
		directLoan("直接放款"),
		/**
		 * 外部扣款
		 */
		externalCut("外部扣款"),
		/**
		 * 直接扣款
		 */
		directCut("直接扣款"),
		/**
		 * 不对账
		 */
		notCheck("不对账");
		
		private String desc;

		private PayType(String desc) {
			this.desc = desc;
		}
		
	}
	

}
