package com.sunline.ccs.batch.common;


import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.ProductType;

/**
 * @see 类名：BatchUtils
 * @see 描述：批量中用到的公共方法
 *
 * @see 创建日期：   2015-6-24下午4:52:58
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class BatchUtils {
	
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;
	
	/**
	 * @see 方法名：fixYear 
	 * @see 描述：b007补年
	 * @see 创建日期：2015-6-24下午4:57:01
	 * @author ChengChun
	 *  
	 * @param b007
	 * @param batchDate
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Date fixYear(String b007, Date batchDate) {
		Calendar batchDateTime = Calendar.getInstance();
		//对于交易日期大于当前营业日期2天及以内的交易，视为正常交易，交易年份按当日顺推处理
		batchDateTime.setTime(DateUtils.addDays(batchDate, 2));
		Calendar txnDateTime = Calendar.getInstance();
		txnDateTime.set(batchDateTime.get(Calendar.YEAR), 
						Integer.parseInt(b007.substring(0, 2)) -1, 
						Integer.parseInt(b007.substring(2, 4)), 
						Integer.parseInt(b007.substring(4, 6)), 
						Integer.parseInt(b007.substring(6, 8)), 
						Integer.parseInt(b007.substring(8, 10))); 
		if(txnDateTime.get(Calendar.DAY_OF_YEAR) > batchDateTime.get(Calendar.DAY_OF_YEAR)){
			txnDateTime.add(Calendar.YEAR, -1);
		}
		return txnDateTime.getTime();
	}
	
	
	/**
	 * @see 方法名：getNextStmtDay 
	 * @see 描述：计算下一账单日期
	 * @see 创建日期：2015-6-24下午4:57:22
	 * @author ChengChun
	 *  
	 * @param productCd
	 * @param billingCycle
	 * @param processDate
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Date getNextStmtDay(String productCd, String billingCycle, Date processDate) {
		// 账单日
		Date stmtDay = null;
		
		// 获取参数
		ProductCredit productCredit = parameterFacility.loadParameter(productCd, ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);

		switch (acctAttr.cycleBaseInd) {
		case W:
			// 当周账单日
			Calendar c = DateUtils.toCalendar(processDate);
			// dfltCycleDay=7表示周七
			if (productCredit.dfltCycleDay == 7) {
				c.set(Calendar.DAY_OF_WEEK, 1);
			} else {
				c.set(Calendar.DAY_OF_WEEK, productCredit.dfltCycleDay + 1);
			}
			stmtDay = c.getTime();
			// 跑批日期>=当周账单日，则周+1
			if (DateUtils.truncatedCompareTo(processDate, stmtDay, Calendar.DATE) >= 0) {
				stmtDay = DateUtils.addWeeks(stmtDay, 1);
			}
			break;
		case M:
			// 当月账单日, 优先取账户上的，无则取参
			if(StringUtils.isNotBlank(billingCycle)){
				Integer cycle = Integer.parseInt(billingCycle.trim());
				//billingCycle大于28则28
				stmtDay = DateUtils.setDays(processDate, cycle >28 ? 28 : cycle);
			}else{
				stmtDay = DateUtils.setDays(processDate, productCredit.dfltCycleDay);
			}
			
			//获取下一个账单日期
			if(DateUtils.truncatedCompareTo(processDate, stmtDay, Calendar.DATE) >= 0) {
				stmtDay = DateUtils.addMonths(stmtDay, 1);
			}
			break;
		default:
			throw new IllegalArgumentException("账户属性中账单周期类型不正确");
		}
		return stmtDay;
	}
	
	/**
	 * @see 方法名：getNextDdDay 
	 * @see 描述：计算下一约定还款日期
	 * @see 创建日期：2015-6-24下午4:57:52
	 * @author ChengChun
	 *  
	 * @param productCd
	 * @param nextStmtDate
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public Date getNextDdDay(String productCd, Date nextStmtDate){
		// 还款日
		Date ddDay = null;
		
		// 获取参数
		ProductCredit productCredit = parameterFacility.loadParameter(productCd, ProductCredit.class);
		AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
		
		ddDay = DateUtils.addDays(rescheduleUtils.getNextPaymentDay(productCd, nextStmtDate), - acctAttr.directDbDays);		
		return ddDay;
	}
	
	/**
	 * @see 方法名：getCardNoBySendMsgCardType 
	 * @see 描述：获取不同短信发送卡号方式的卡号
	 * @see 创建日期：2015-6-24下午4:58:51
	 * @author ChengChun
	 *  
	 * @param acct
	 * @param card
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public String getCardNoBySendMsgCardType(CcsAcct acct, CcsCard card) {
		ProductCredit productCr = parameterFacility.loadParameter(card.getProductCd(), ProductCredit.class);
		// 参数未设置，自动填充规则
		if(productCr.sendMessageCardType == null){
			Product product = parameterFacility.loadParameter(card.getProductCd(), Product.class);
			if(product.productType==ProductType.M){
				if(StringUtils.isNotBlank(acct.getDdBankAcctNbr())){
					return acct.getDdBankAcctNbr();
				}else{
					return card.getLastestMediumCardNbr();
				}
			}else{
				return card.getLastestMediumCardNbr();
			}
		}
		// 根据参数返回需要的卡号
		switch (productCr.sendMessageCardType){
		case C:
			return card.getLastestMediumCardNbr();
		case L:
			return card.getLogicCardNbr();
		case D:
			if(StringUtils.isNotBlank(acct.getDdBankAcctNbr())){
				return acct.getDdBankAcctNbr();
			}else{
				return card.getLastestMediumCardNbr();
			}
		default: throw new IllegalArgumentException("短信发送卡号方式不正确："+productCr.sendMessageCardType);
		}
	}

}
