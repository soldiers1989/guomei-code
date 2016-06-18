package com.sunline.ccs.batch.cc6000;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.cc6000.common.TransactionGenerator;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.enums.GraceIntType;
import com.sunline.ccs.param.def.enums.SysTxnCd;
import com.sunline.acm.service.sdk.BatchStatusFacility;


/** 
 * @see 类名：P6037InterestStatement
 * @see 描述：结息处理
 *
 * @see 创建日期：   2015年6月25日 下午2:21:10
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P6037InterestStatement implements ItemProcessor<S6000AcctInfo, S6000AcctInfo> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private TransactionGenerator generatorTransaction;
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
    private BatchStatusFacility batchFacility;

	@Override
	public S6000AcctInfo process(S6000AcctInfo item) {
		if (logger.isDebugEnabled()) {
			logger.debug("账单日利息处理：Org["+item.getAccount().getOrg()
					+"],AcctNo["+item.getAccount().getAcctNbr()
					+"],AcctType["+item.getAccount().getAcctType()
					+"],BatchDate["+batchFacility.getBatchDate()
					+"]");
		}
		Date batchDate = batchFacility.getBatchDate();

		// 判断批量日期是否是下一账单日，如果是下一账单日，执行账单日处理
		if (batchFacility.shouldProcess(item.getAccount().getNextStmtDate())){
			logger.info("批量日期符合下一账单日  当前批量日期:[" + batchDate.toString() 
					+ "]  下一账单日:[" + item.getAccount().getNextStmtDate().toString() + "]");
			statementInterest(item);
		}
		
		logger.info("账单日利息处理Process finished! AccountNo:[" + item.getAccount().getAcctNbr() 
				+ "] AccountType:[" + item.getAccount().getAcctType() + "] ");
		
		return item;
	}
	
	/**
	 * 账单日利息处理
	 * @param item 账户信息
	 */
	private void statementInterest(S6000AcctInfo item){
		//获取参数
		Organization org = parameterFacility.loadParameter(null, Organization.class);
		
		//	锁定码指示是否对未入帐利息进行减免
		Boolean intWaiveInd = blockCodeUtils.getMergedIntWaiveInd(item.getAccount().getBlockCode());
		
		//	待入账利息
		BigDecimal postInterestAmt = BigDecimal.ZERO;
		
		//是否全额还款
		boolean isGraceDaysFull = getGraceDaysFull(item.getAccount());
		//是否超限
		boolean isOverlimitDefer = getOverlimitDefer(item.getAccount());
		
		//参数是否合法校验
		checkParameter(org);
		
		for(CcsPlan plan : item.getPlans()){
			
			//非延时利息入账，将plan非延时利息字段清零
			if(!plan.getPlanType().isXfrOut()){
				postInterestAmt = postInterest(item, org, intWaiveInd, postInterestAmt, plan, plan.getNodefbnpIntAcru().setScale(2, RoundingMode.HALF_UP));
				plan.setNodefbnpIntAcru(BigDecimal.ZERO);
			}
			//罚息入账
			if(isGraceDaysFull){//已全额还款
				plan.setPenaltyAcru(BigDecimal.ZERO);
			}else{
				postPenaltyInterest(item,plan,intWaiveInd);
			}
			
			//复利入账
			if(isGraceDaysFull){//已全额还款
				plan.setCompoundAcru(BigDecimal.ZERO);
			}else{
				postCompoundInterest(item,plan,intWaiveInd);
			}
			
			//代收罚息入账
			if(isGraceDaysFull){
				plan.setReplacePenaltyAcru(BigDecimal.ZERO);
			}else{
				postReplacePenaltyAcru(item,plan,intWaiveInd);
			}
			
			//延时利息入账
			if(isGraceDaysFull){//已全额还款
				if(isOverlimitDefer){//超限
					if(org.overlimitDeferInd == GraceIntType.C){//超限是否继续享受免息
						plan.setBegDefbnpIntAcru(plan.getCtdDefbnpIntAcru());
						plan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
					}else{
						postInterestAmt = postInterest(item, org, intWaiveInd, postInterestAmt, plan, plan.getCtdDefbnpIntAcru().setScale(2, RoundingMode.HALF_UP));
						plan.setBegDefbnpIntAcru(BigDecimal.ZERO);
						plan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
					}
				}else{//不超限
					plan.setBegDefbnpIntAcru(plan.getCtdDefbnpIntAcru());
					plan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
				}
			}else{//未全额还款
				postInterestAmt = postInterest(item, org, intWaiveInd, postInterestAmt, plan, plan.getBegDefbnpIntAcru().setScale(2, RoundingMode.HALF_UP));
				plan.setBegDefbnpIntAcru(BigDecimal.ZERO);
				if(isOverlimitDefer){//超限
					if(org.overlimitDeferInd == GraceIntType.C && org.nofullpayDeferInd==GraceIntType.C){
						plan.setBegDefbnpIntAcru(plan.getCtdDefbnpIntAcru());
						plan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
					}else{
						postInterestAmt = postInterest(item, org, intWaiveInd, postInterestAmt, plan, plan.getCtdDefbnpIntAcru().setScale(2, RoundingMode.HALF_UP));
						plan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
					}
				}else{//未超限
					if(org.nofullpayDeferInd == GraceIntType.C){
						plan.setBegDefbnpIntAcru(plan.getCtdDefbnpIntAcru());
						plan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
					}else{
						postInterestAmt = postInterest(item, org, intWaiveInd, postInterestAmt, plan, plan.getCtdDefbnpIntAcru().setScale(2, RoundingMode.HALF_UP));
						plan.setCtdDefbnpIntAcru(BigDecimal.ZERO);
					}
				}
			}
		}
		
		
		//	FIXME 增加内部交易码和外部交易码参数数据
		//	判断应入账利息是否大于0,大于0时进行利息入账
		if (postInterestAmt.compareTo(BigDecimal.ZERO) > 0){
			//	FIXME 入账接口增加planNbr的参数
			generatorTransaction.generateInterestTransaction(item, null, postInterestAmt.setScale(2, RoundingMode.HALF_UP), batchFacility.getBatchDate());
			
		}
	}

	
	/**
	 * 复利入账
	 * @param item
	 * @param plan
	 */
	private void postCompoundInterest(S6000AcctInfo item, CcsPlan plan,Boolean intWaiveInd) {
		if(!intWaiveInd && plan.getCompoundAcru().compareTo(BigDecimal.ZERO) > 0){
			generatorTransaction.generateInterestTransaction(item, plan, plan.getCompoundAcru().setScale(2, RoundingMode.HALF_UP), batchFacility.getBatchDate(),SysTxnCd.S44);
			plan.setCompoundAcru(BigDecimal.ZERO);
		}
	}

	/**
	 * 罚息入账
	 * @param item
	 * @param plan
	 */
	private void postPenaltyInterest(S6000AcctInfo item, CcsPlan plan,Boolean intWaiveInd) {
		if(!intWaiveInd && plan.getPenaltyAcru().compareTo(BigDecimal.ZERO) > 0){
			generatorTransaction.generateInterestTransaction(item, plan, plan.getPenaltyAcru().setScale(2, RoundingMode.HALF_UP), batchFacility.getBatchDate(),SysTxnCd.S43);
			plan.setPenaltyAcru(BigDecimal.ZERO);
		}
		
	}
	
	/**
	 * 代收罚息入账
	 */
	private void postReplacePenaltyAcru(S6000AcctInfo item,CcsPlan plan,Boolean intWaiveInd){
		if(!intWaiveInd&&plan.getReplacePenaltyAcru().compareTo(BigDecimal.ZERO)>0){
			generatorTransaction.generateInterestTransaction(item, plan, plan.getReplacePenaltyAcru().setScale(2,RoundingMode.HALF_UP),batchFacility.getBatchDate(),SysTxnCd.D13);
			plan.setReplacePenaltyAcru(BigDecimal.ZERO);
		}
	}
	
	/**
	 * 参数合法性校验
	 * 
	 * @param org
	 */
	private void checkParameter(Organization org) {
		if(org.nofullpayDeferInd != GraceIntType.C && org.nofullpayDeferInd != GraceIntType.N ){
			throw new IllegalArgumentException("非法的未全额还款免息标识：" +org.nofullpayDeferInd.toString());
		}
		if(org.overlimitDeferInd != GraceIntType.C && org.overlimitDeferInd != GraceIntType.N ){
			throw new IllegalArgumentException("非法的超限免息标识：" +org.overlimitDeferInd.toString());
		}
	}

	/**
	 * 判断是否全额还款
	 * 
	 * @param tmAccount
	 * @return
	 */
	private boolean getGraceDaysFull(CcsAcct tmAccount) {
		
		if(tmAccount.getGraceDaysFullInd() == Indicator.Y){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * 判断是否超限
	 * 
	 * @param tmAccount
	 * @return
	 */
	private boolean getOverlimitDefer(CcsAcct tmAccount) {
		if(tmAccount.getOvrlmtDate() != null){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * 往期延迟利息处理，判断blockcode指示是否免息，如果不免息，则进行利息入账处理
	 * 
	 * @param item
	 * @param org
	 * @param intWaiveInd
	 * @param postInterestAmt
	 * @param plan
	 * @param defbnpIntAcru
	 * @return
	 */
	private BigDecimal postInterest(S6000AcctInfo item, Organization org, Boolean intWaiveInd, BigDecimal postInterestAmt, CcsPlan plan, BigDecimal defbnpIntAcru) {
		//判断blockcode指示是否免息，如果不免息，则进行利息入账处理
		if (!intWaiveInd && defbnpIntAcru.compareTo(BigDecimal.ZERO) > 0) {
			//	判断利息是按信用计划入账，还是合并入账
			if (org.intPostOnPlan) {
				//按信用计划入账
				generatorTransaction.generateInterestTransaction(item, plan, defbnpIntAcru, batchFacility.getBatchDate());
			} else {
				//合并入账，未全额还款往期累积延时利息增加至待入账金额
				postInterestAmt = postInterestAmt.add(defbnpIntAcru);
			}
		}
		return postInterestAmt;
	}

}
