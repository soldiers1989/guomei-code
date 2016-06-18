package com.sunline.ccs.batch.cc1500;

import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.drools.core.util.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.common.MessageUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLogHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.loan.LoanUtil;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.ccs.service.api.Constants;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.exchange.MSLoanMsgItem;
import com.sunline.ppy.dictionary.report.ccs.AcctCrlmtAdjRptItem;

/**
 * @see 类名：P8502AcctCrlmtAdjRpt
 * @see 描述：调整额度记录报表
 *
 * @see 创建日期：   2015-11-09
 * @author mengxiang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P1502AcctCrlmtAdjRpt implements ItemProcessor<CcsAcctCrlmtAdjLog, S1502AcctCrlmtAdjRpt> {
	
	@PersistenceContext
    private EntityManager em;
	
	@Autowired
	private RCcsAcct rAcct;
	
	@Autowired
	private RCcsCustomer rCustomer;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	@Autowired
	private MessageUtils messageUtils;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	@Autowired
	private LoanUtil loanUtil;
	
	@Override
	public S1502AcctCrlmtAdjRpt process(CcsAcctCrlmtAdjLog adjLog) throws Exception {
		
		S1502AcctCrlmtAdjRpt out = new S1502AcctCrlmtAdjRpt();
		
		// 等待审核状态的调额不出报表
		if (AdjState.W.equals(adjLog.getAdjState())) {
			return null;
		}
		// 批量调额短信通知接口
		MSLoanMsgItem msgItem = null;
		StringBuilder smsParam = new StringBuilder();
		
		AcctCrlmtAdjRptItem item = new AcctCrlmtAdjRptItem();
		item.org = adjLog.getOrg();
		item.acctNbr = adjLog.getAcctNbr()==null?null:adjLog.getAcctNbr().toString();
		item.acctType = adjLog.getAcctType()==null?null:adjLog.getAcctType().toString();
		item.searchDate = batchFacility.getBatchDate();
		item.procDate = adjLog.getProcDate();
		item.creditLmtOrig = adjLog.getCreditLmtOrig();
		item.creditLmtNew = adjLog.getCreditLmtNew();
		item.adjState = adjLog.getAdjState()==null?null:adjLog.getAdjState().toString();
		item.reanson = adjLog.getRejReason();
		item.opId = adjLog.getOpId();
		item.expireDate = adjLog.getExpireDate();
		
		if (adjLog.getCreditLmtNew() != null && adjLog.getCreditLmtOrig() != null) {
			item.adjLmt = adjLog.getCreditLmtNew().subtract(adjLog.getCreditLmtOrig());
		}
		
		// 账户号和账户类型不为空，查询账户信息
		if (!StringUtils.isEmpty(adjLog.getAcctNbr()+"")&& adjLog.getAcctType() != null)
		{
			CcsAcct acct = rAcct.findOne(new CcsAcctKey(adjLog.getAcctNbr(),adjLog.getAcctType()));
			
			// 账户不为空
			if (acct != null) {
				if(adjLog.getCreateUser() != null && Constants.OP_USER_BATCH.equals(adjLog.getCreateUser())) {
					msgItem = new MSLoanMsgItem();
					//短信接口设置手机号
					msgItem.phoneNbr = acct.getMobileNo();
					msgItem.serialNo = messageUtils.getMsgSerialNo(acct.getAcctNbr(), batchFacility.getBatchDate());
					/**
					 * 短信模板如下：
					 * 尊敬的{客户姓名}，您的额度已进行了调整。目前您的总额度为￥{xx}，
					 * 您可在{}年{}月{}日前使用此额度。如您有任何疑问，请前往我们的网站 www.msxf.com 祝您生活愉快!
					 */
					String time=new SimpleDateFormat("yyyyMMdd").format(acct.getAcctExpireDate()); //账户有效期
					String year=time.substring(0, 4);
					String month=time.substring(4, 6);
					String day=time.substring(6, 8);
					
					smsParam.append(acct.getName())					//客户姓名
					.append(Constants.BATCH_SMS_SEPARATOR) 		//分隔符
					.append(adjLog.getCreditLmtNew()==null?acct.getCreditLmt():adjLog.getCreditLmtNew())			//新额度,只调有效期，adjlog的新额度字段为空
					.append(Constants.BATCH_SMS_SEPARATOR) 		//分隔符
					.append(year)								//账户有效期-年
					.append(Constants.BATCH_SMS_SEPARATOR) 		//分隔符
					.append(month)								//账户有效期-月
					.append(Constants.BATCH_SMS_SEPARATOR) 		//分隔符
					.append(day);								//账户有效期-日
					msgItem.msgParams  = smsParam.toString(); 		//短信模板参数
					
					//批量短信文件接口新增四个字段
					msgItem.sourceBizSystem = Constants.SOURCE_BIZ_SYSTEM;	//业务系统
					msgItem.acqId = acct.getAcqId();						//来源结构编号
					//如果合同是建户未进行提款的状态，ccsloan里没有值，这时候要先进行判断
					String loanCode = loanUtil.findLoanCode(acct);
					msgItem.loanCode = loanCode;				//产品代码
					msgItem.sourceBizType = Constants.BATCH_ADJUSTMENT;			//业务类型
				}
				
				item.contrNbr = acct.getContrNbr();
				item.name = acct.getName();
				item.contrEstablishDate = acct.getSetupDate();
				
				// 查询客户信息
				CcsCustomer customer = rCustomer.findOne(acct.getCustId());
				if (customer != null) {
					item.idNo = customer.getIdNo();
				}
			}
		}
		
		// 调额成功，记录成功报表;失败，记录失败报表
		if (AdjState.A.equals(adjLog.getAdjState())) {
			out.setAcctCrlmtAdjSuccessRpt(item);
			out.setAcctCrlmtAdjMsg(msgItem);
		} else if (AdjState.R.equals(adjLog.getAdjState())) {
			out.setAcctCrlmtAdjFailRpt(item);
		}
		// 迁移到历史表
		CcsAcctCrlmtAdjLogHst adjLogHst = new CcsAcctCrlmtAdjLogHst();
		adjLogHst.updateFromMap(adjLog.convertToMap());
		em.persist(adjLogHst);
		em.remove(adjLog);
		
		return out;
	}
}
