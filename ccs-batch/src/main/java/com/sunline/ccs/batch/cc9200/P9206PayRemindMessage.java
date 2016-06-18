package com.sunline.ccs.batch.cc9200;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.batch.common.BatchUtils;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsStatementKey;
import com.sunline.ccs.otb.CommProvide;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.acm.service.sdk.BatchStatusFacility;
//import com.sunline.smsd.service.sdk.PayRemindMsgItem;
/**
 * @see 类名：P9206PayRemindMessage
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-24下午5:31:02
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public  class P9206PayRemindMessage implements ItemProcessor<S9201MasterData,S9201MasterData>{
    
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BatchUtils batchutils;
	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
	@Autowired
	private RCcsStatement rTmStmtHst;
	@Autowired
	private CommProvide commonProvide;

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public S9201MasterData process(S9201MasterData item) throws Exception {
		
		CcsAcct acct = item.getAccount();
		ProductCredit product = parameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
		AccountAttribute accountAttribute = parameterFacility.loadParameter(product.accountAttributeId, AccountAttribute.class);
		Date pmtDueInAdvRemDate = DateUtils.addDays(acct.getPmtDueDate(), -Integer.valueOf(accountAttribute.pmtDueLtrPrd));//获取短信提前提醒日期
		logger.debug("账号{}{}, 还款日前提醒日期{}", acct.getAcctNbr(),acct.getAcctType(), pmtDueInAdvRemDate);
		
		BigDecimal remainGraceBal = commonProvide.getRemainGraceBal(item.getListPlan());
		
		if(DateUtils.truncatedCompareTo(batchFacility.getBatchDate(), pmtDueInAdvRemDate, Calendar.DATE)==0 && remainGraceBal.compareTo(BigDecimal.ZERO)!=0){
			CcsStatementKey key = new CcsStatementKey();
			key.setAcctNbr(acct.getAcctNbr());
			key.setAcctType(acct.getAcctType());
			key.setStmtDate(acct.getLastStmtDate());
			CcsStatement stmtHst = rTmStmtHst.findOne(key);
			
//			PayRemindMsgItem msgItem = new PayRemindMsgItem();
			
			CcsCard card = queryFacility.getCardByLogicCardNbr(acct.getDefaultLogicCardNbr());	
/*		    msgItem.cardNo = batchutils.getCardNoBySendMsgCardType(acct, card);
		    msgItem.msgCd = fetchMsgCdService.fetchMsgCd(acct.getProductCd(), CPSMessageCategory.CPS061);
			msgItem.currencyCd = acct.getCurrency();
			msgItem.custName = acct.getName();
			msgItem.due = stmtHst.getTotDueAmt();//在账单历史里取
			msgItem.gender = acct.getGender();
			msgItem.graceBalance = acct.getQualGraceBal(); //无欠款取CURR_BAL
			msgItem.mobileNo = acct.getMobileNo();
			msgItem.org = acct.getOrg();
			msgItem.paymentDate = acct.getPmtDueDate();
			msgItem.stmtDate = acct.getLastStmtDate();
			msgItem.currHavePay = acct.getQualGraceBal().subtract(remainGraceBal);
			msgItem.needToPay = remainGraceBal;//剩余应还款额
			msgItem.minNeedToPay = acct.getTotDueAmt();//剩余最小应还款额
		    item.setPayRemindMsg(msgItem);
*/		}
		return item;
	}
}
