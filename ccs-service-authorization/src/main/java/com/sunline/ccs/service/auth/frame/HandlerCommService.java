package com.sunline.ccs.service.auth.frame;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.param.def.AuthProduct;
import com.sunline.ccs.param.def.AuthReasonMapping;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransTerminal;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.TxnInfo;

/**
 * 
 * @see 类名：HandlerCommService
 * @see 描述：授权交易业务处理公用服务，可以考虑服务
 *
 * @see 创建日期：   2015年6月24日下午3:25:34
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class HandlerCommService {
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	
	
	
//	public BigDecimal getCustomerOTB(CcsAcctO account,CcsCustomerCrlmt custLimitO){
//		TxnInfo txnInfo = new TxnInfo();
//	 AuthProduct product = new AuthProduct();
//	 Map<AuthTransType, Map<AuthTransTerminal, Boolean>> transMap = product.transTypeTerminalEnabled;
//		for(Entry<AuthTransType, Map<AuthTransTerminal, Boolean>>  enty :transMap.entrySet()){
//			AuthTransType transType = enty.getKey();
//			if(transType.equals(txnInfo.getTransType())){
//				
//				Map<AuthTransTerminal, Boolean> transTermalMap  = enty.getValue();
//				for(Entry<AuthTransTerminal, Boolean>  transTerminal  : transTermalMap.entrySet()){
//					AuthTransTerminal terminal = transTerminal.getKey();
//					
//					if(terminal.equals(txnInfo.getTransTerminal())){
//						if(!transTerminal.getValue()){
//							//put reason and action 
//						}
//					}
//					
//				}
//			}
//		}
//		
//		return null;
//	}
	
	/**
	 * 根据AuthReason回写authAction 和returnCode，为返回域准备
	 * @param context
	 */
//	public void addCompletedInfo(AuthContext context){ 
//		//获取authReason对应的ActionMapping
////		AuthReasonMapping mapping = unifiedParameterFacility.retrieveParameterObject("CUP" + context.getAuthReason(), AuthReasonMapping.class);
//		//获取authReason在授权产品参数中对应的Action
////		AuthAction action = context.getAuthProduct().reasonActions.get(context.getAuthReason());
//
////		context.setAuthAction(action);
////		//根据Action来确定该去哪个返回码
////		if(action.equals(AuthAction.Decline))
////			context.setResponsCode(mapping.declineUnionpay);
////		else if(action.equals(AuthAction.Pickup))
////			context.setResponsCode(mapping.pickupUnionpay);
////		else if(action.equals(AuthAction.Call))
////			context.setResponsCode(mapping.callUnionpay);
////		//对于approve的action，如果returnCode不是00，则取该approve下的returnCode值，实际上不用判断，直接取来覆盖就好
////		else if(action.equals(AuthAction.A)) 
////			context.setResponsCode(mapping.approveUnionpay);
//		 
//	}
	
	/**
	 * 根据授权产品参数取相应Action
	 * @param context
	 * @return
	 */
//	public AuthAction getAuthPrdocutAction( AuthContext context ){
//		
//		AuthAction action = context.getAuthProduct().reasonActions.get("");
//		
//		return action;
//	}
	
	/**
	 * 对于直接写死的Action直接从AuthReasonMapping中获取即可
	 * @param context
	 * @return
	 */
	public AuthAction getAuthDefaultAction( AuthReason authReason ){
		
		AuthReasonMapping mapping = unifiedParameterFacility.retrieveParameterObject("CUP" + "|" + authReason, AuthReasonMapping.class);

		 return mapping.defaultAction;
	}
	
	

//	/**
//	 * 获取blockCode对象，方便获取该对象相关的reason和action
//	 * @param context
//	 * @return
//	 */
//	public BlockCode getBlockCodeAction( String blockCode ){
//		
//		BlockCode blCode = unifiedParameterFacility.retrieveParameterObject(blockCode, BlockCode.class);
//
//		 return blCode;
//	}
	
	
	

}
