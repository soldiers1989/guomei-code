package com.sunline.ccs.batch.cc1400;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.report.ccs.MsxfOrderExpiredRpt;
import com.sunline.ppy.dictionary.report.ccs.MsxfOrderHstDaygenRpt;

/**
 * 对账完成后清理转移Order表
 * @author wanghl
 *
 */
public class P1404MsxfOrderClean implements ItemProcessor<CcsOrder, S1404RptInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@PersistenceContext
    private EntityManager em;
    @Autowired
    private BatchStatusFacility batchFacility;
    private static final int checkExpired = 3;
    private static final int failExpired = 100;
    public static final int processExpired = 100;
	
	@Override
	public S1404RptInfo process(CcsOrder item) throws Exception {
		
		S1404RptInfo rpt = new S1404RptInfo();
		
		CcsOrderHst hst = new CcsOrderHst();
		//已失效，已完成，超时，拆分已完成，已重提态的订单转移到hst表
		//如果需要对账，并且对账的比较结果是Y，不管成功时候失败都根据条件转移到历史表
		//如果不需要对账，根据条件转移到历史表
		MsxfOrderHstDaygenRpt hstRpt = null;
		MsxfOrderExpiredRpt expiredRpt = null;
		//对账缓冲天数
		Date expired = DateUtils.addDays(item.getBusinessDate(), checkExpired) ;
		logger.debug("订单清理：订单号:["+item.getOrderId()+"],对账标示=["+item.getMatchInd()+"],比较结果标示=["+item.getComparedInd()+"]");
		
		//对账功能未开放 对账结果标志置空
		if(item.getComparedInd()== null){
			item.setComparedInd(Indicator.N);
		}
		//需要对账，对账比较结果为N,没有对账文件
		//撤销交易参与对账
		if(item.getMatchInd().equals(Indicator.Y) && item.getComparedInd().equals(Indicator.N)){
			logger.debug("订单转移历史表：订单号:["+item.getOrderId()+"],对账判断结果判断不符合条件");
			//达到对账有效期天数
			if(batchFacility.getBatchDate().compareTo(expired)>=0){
				if(isMoveHst(item)){
					logger.debug("订单转移历史表：订单号:["+item.getOrderId()+"],对账判断结果超过失效天数,订单表状态判断可以转移到历史表");
					hst.updateFromMap(item.convertToMap());
					hstRpt = getMsxfOrderHstRpt(item);
					em.persist(hst);
					em.remove(item);
				}else{
					logger.debug("订单转移历史表：订单号:["+item.getOrderId()+"],对账判断结果超过失效天数,订单表状态判断不符合转移到历史表条件");
				}
				expiredRpt = getMsxfOrderExpiredRpt(item);
			}else{
				logger.debug("订单转移历史表：订单号:["+item.getOrderId()+"],对账判断结果未超过失效天数,不处理");
			}
		}else{
			//这里的是不需要对账或者需要对账，对账标示为Ｙ已对账
			if(isMoveHst(item)){
				logger.debug("订单转移历史表：订单号:["+item.getOrderId()+"],对账判断结果可以转移到历史表,订单表状态判断可以转移到历史表");
				hst.updateFromMap(item.convertToMap());
				hstRpt = getMsxfOrderHstRpt(item);
				em.persist(hst);
				em.remove(item);
			}else{
				logger.debug("订单处理结束：订单号:["+item.getOrderId()+"],对账判断结果可以转移到历史表,订单表状态判断不符合转移到历史表条件");
			}
		}
		rpt.setMsxfOrderExpiredRpt(expiredRpt);
		rpt.setMsxfOrderHstDaygenRpt(hstRpt);
		return rpt;
	}

	/*
	 * 根据订单状态判断该订单流水是否需要转移到历史表中
	 */
	public boolean isMoveHst(CcsOrder order){
		if(logger.isDebugEnabled())
			logger.debug("订单订单判断：订单号:["+order.getOrderId()+"],订单状态=["+order.getOrderStatus()+"],订单用途=["+order.getLoanUsage()+"]");
		//失败交易转移缓冲天数
		Date failStatusExpired = DateUtils.addDays(order.getBusinessDate(), failExpired) ;
		//处理中交易转移缓冲天数
		Date processStatusExpired = DateUtils.addDays(order.getBusinessDate(), processExpired) ;	
		
		if(logger.isDebugEnabled())
			logger.debug("当前批量日期[{}],失败交易到期日期[{}],处理中交易到期日期[{}]",batchFacility.getBatchDate(),failStatusExpired,processStatusExpired);

		//查询交易不管状态直接移到历史表中
		if( order.getLoanUsage()==LoanUsage.E 
		 || order.getLoanUsage()==LoanUsage.F
		 || order.getLoanUsage()==LoanUsage.G ){
			return true;
		}
		//优惠券交易根据日期进行处理
		if( order.getLoanUsage() == LoanUsage.Q){
			return isNeedCleanCoupon(order);
		}
		switch(order.getOrderStatus()){
		case B://原交易状态为已撤销
		case S://已完成
		case V://已失效
		case T://超时
		case D://拆分已完成
		case R://已重提
			return true;
		case C://已提交
		case P://待提交
		case W://处理中
		case G://拆分处理中
		case Q://审批中
			//超过限制天数则转移
			if(batchFacility.getBatchDate().compareTo(processStatusExpired)>=0){
				return true;
			}
			break;
		case E://失败 
			//放款 结算交易失败缓冲一段时间(等待重提)后才移入历史表
			if(order.getLoanUsage().equals(LoanUsage.L) 
			|| order.getLoanUsage().equals(LoanUsage.A) 
			|| order.getLoanUsage().equals(LoanUsage.B)
			|| order.getLoanUsage().equals(LoanUsage.D)){
				if(batchFacility.getBatchDate().compareTo(failStatusExpired)>=0){
					return true;					
				}
			}
			else{
				return true;
			}
			
			break;
		}
		return false;
	}
	
	/**
	 * 判断优惠券订单流水是否应转移
	 * @author Lisy
	 * @param order
	 * @return boolean
	 */
	private Boolean isNeedCleanCoupon(CcsOrder order){
		QCcsLoan q = QCcsLoan.ccsLoan;
		// 异常数据直接转移历史表
		if(order.getAcctNbr()==null||order.getAcctType()==null){
			return false;
		}
		CcsLoan loan = new JPAQuery(em).from(q).where(q.acctNbr.eq(order.getAcctNbr())
				.and(q.acctType.eq(order.getAcctType()))).singleResult(q);
		// 随借随还账户次日处理处理
		if(LoanType.MCAT==loan.getLoanType()){
			if(batchFacility.getBatchDate().compareTo(order.getBusinessDate())>0){
				return true;
			}
		}else{
			//等额本息账单日后处理
//			CcsAcct acct = em.find(CcsAcct.class,new CcsAcctKey(order.getAcctNbr(),order.getAcctType()));
			//优惠券订单在账单日前发起，且账单日当天入账，账单日后失效；
//			if(order.getBusinessDate().compareTo(batchFacility.getBatchDate())<0&&
//					batchFacility.getBatchDate().compareTo(acct.getNextStmtDate())!=0){
//				return true;
//			}
			//此处无法判断
			return false;
		}
		return false;
	}
	
	public MsxfOrderExpiredRpt getMsxfOrderExpiredRpt(CcsOrder order){
		MsxfOrderExpiredRpt rpt = new MsxfOrderExpiredRpt();
		 rpt.channelSerial = order.getOrderId().toString();
		 rpt.channelDate = order.getBusinessDate();
		 rpt.bankId =  order.getOpenBankId();
		 rpt.cardType = order.getCardType();
		 rpt.cardNo = order.getCardNo();
		 rpt.usrName = order.getUsrName();
		 rpt.idType = order.getCertType();
		 rpt.idNo = order.getCertId();
		 rpt.openBank = order.getOpenBank();
		 rpt.prov = order.getState();
		 rpt.city = order.getCity();
		 rpt.txnAmt = order.getTxnAmt();
		 rpt.purpose = order.getPurpose();
		 rpt.subBank = order.getSubBank();
		 rpt.flag = order.getFlag();
		 rpt.status = order.getState();
		 rpt.msDdReturnCode = order.getCode();
		 rpt.returnMessage = order.getMessage();
		 rpt.privateField = order.getPriv1();
		return rpt;
	}

	public MsxfOrderHstDaygenRpt getMsxfOrderHstRpt(CcsOrder order){
		MsxfOrderHstDaygenRpt rpt = new MsxfOrderHstDaygenRpt();
		 rpt.channelSerial = order.getOrderId().toString();
		 rpt.channelDate = order.getBusinessDate();
		 rpt.bankId =  order.getOpenBankId();
		 rpt.cardType = order.getCardType();
		 rpt.cardNo = order.getCardNo();
		 rpt.loanUsage = order.getLoanUsage();
		 rpt.orderStatus = order.getOrderStatus();
		 rpt.usrName = order.getUsrName();
		 rpt.idType = order.getCertType();
		 rpt.idNo = order.getCertId();
		 rpt.openBank = order.getOpenBank();
		 rpt.prov = order.getState();
		 rpt.city = order.getCity();
		 rpt.txnAmt = order.getTxnAmt();
		 rpt.purpose = order.getPurpose();
		 rpt.subBank = order.getSubBank();
		 rpt.flag = order.getFlag();
		 rpt.status = order.getState();
		 rpt.msDdReturnCode = order.getCode();
		 rpt.returnMessage = order.getMessage();
		 rpt.privateField = order.getPriv1();
		return rpt;
	}

//	@Override
//	public MsxfOrderExpiredRpt process(CcsOrder item) throws Exception {
//		CcsOrderHst hst = new CcsOrderHst();
//		//已失效，已完成，超时，拆分已完成，已重提态的订单转移到hst表
//		//如果需要对账，并且对账的比较结果是Y，不管成功时候失败都根据条件转移到历史表
//		//如果不需要对账，根据条件转移到历史表
//		MsxfOrderExpiredRpt rpt = null;
//		//处理中的订单，如果超过3天出报表
//		Date expired = DateUtils.addDays(item.getBusinessDate(), 3) ;
//		if(item.getComparedInd()== null){
//			item.setComparedInd(Indicator.N);
//		}
//		if(item.getMatchInd().equals(Indicator.Y) && item.getComparedInd().equals(Indicator.N) ){
//			if(batchFacility.getBatchDate().compareTo(expired)>=0){
//				logger.debug("订单超期处理：订单号:["+item.getOrderId()+"]");
//				rpt = getMsxfOrderExpiredRpt(item);
//				if(isNeedHst(item)){
//					logger.debug("订单转移历史表：订单号:["+item.getOrderId()+"]");
//					hst.updateFromMap(item.convertToMap());
//					em.persist(hst);
//					em.remove(item);
//				}
//			}
//				
//		}else {
//			if(isNeedHst(item)){
//				logger.debug("订单转移历史表：订单号:["+item.getOrderId()+"]");
//				hst.updateFromMap(item.convertToMap());
//				em.persist(hst);
//				em.remove(item);
//			}
//		}
//		
//		return rpt;
//	}
//	public boolean isNeedHst(CcsOrder item){
//	if(item.getOrderStatus().equals(OrderStatus.V) ||item.getOrderStatus().equals(OrderStatus.S) || 
//			item.getOrderStatus().equals(OrderStatus.T) || item.getOrderStatus().equals(OrderStatus.D) || item.getOrderStatus().equals(OrderStatus.R)){
//	return true;
//	}else if(item.getOrderStatus().equals(OrderStatus.E)) {
//		if(!item.getLoanUsage().equals(LoanUsage.L) && !item.getLoanUsage().equals(LoanUsage.A) && !item.getLoanUsage().equals(LoanUsage.B)){
//			//	失败的订单，以用途区分
//			//用途为"M|预约提前结清扣款","C|理赔(claims)","N|正常扣款N","O|逾期扣款(overdue)",S|追偿代扣"	"T|实时扣款"，转移
//			//不转移：L放款申请,A|放款重提","R|联机追偿","B 结算"
//			return true;
//		}
//	}
//	return false;
//}
}
