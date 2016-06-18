package com.sunline.ccs.batch.cc5000;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardCloseReg;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.param.def.Organization;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AcctCloseReason;
import com.sunline.ppy.dictionary.enums.RequestType;
import com.sunline.ppy.dictionary.report.ccs.AcctCloseRptItem;
import com.sunline.ppy.dictionary.report.ccs.CancelRptItem;

/**
 * @see 类名：P5001AcctClose
 * @see 描述：销卡销户及关闭账户批处理
 *
 * @see 创建日期：   2015-6-23下午7:54:15
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P5001AcctClose implements ItemProcessor<CcsCardCloseReg, S5001AcctClose> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String P_CODE = "P";
	private static final String C_CODE = "C";

	@PersistenceContext
	private EntityManager em;
	
	/**
	 * 卡档
	 */
	@Autowired
	private RCcsCard rTmCard;
	
	/**
	 * 账户档
	 */
	@Autowired
	private RCcsAcct rTmAccount;
	
	/**
	 * 联机账户档
	 */
	@Autowired
	private RCcsAcctO rTmAccountO;
	
	/**
	 * 账户档
	 */
	@Autowired
	private RCcsCustomer rTmCustomer;
	
	/**
	 * 锁定码处理业务组件
	 */
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	/**
	 * 跑批日期
	 */
	@Autowired
	private BatchStatusFacility batchFacility;
	
	/**
	 * 获取参数
	 */
	@Autowired
	private UnifiedParameterFacility parameterFacility;

	@Override
	public S5001AcctClose process(CcsCardCloseReg cancel) throws Exception 
	{
		try
		{
			S5001AcctClose output = new S5001AcctClose();
			CcsCustomer cust = new CcsCustomer();
			
			//取机构号并设置上下文
			OrganizationContextHolder.setCurrentOrg(cancel.getOrg());
			
			//销卡销户
			if(DateUtils.truncatedCompareTo(batchFacility.getBatchDate(), cancel.getLogBizDate(), Calendar.DATE) == 0)
			{
				switch (cancel.getRequestType())
				{
					//销卡及撤销
					case A:
					case B:
						//销卡撤销卡通过卡片获取客户信息
						CcsCard card = rTmCard.findOne(cancel.getLogicCardNbr());
						cust = rTmCustomer.findOne(card.getCustId());
						
						//生成销卡销户报表
						output.setCancelRptItem(makeCancelResponseItem(cancel, cust));
						
						//移除销卡撤销卡记录
						em.remove(cancel);
						break;
					
					//销户
					case C:
						//销户撤销户通过账户获取客户信息
						CcsAcctKey keyC = new CcsAcctKey();
						keyC.setAcctNbr(cancel.getAcctNbr());
						keyC.setAcctType(cancel.getAcctType());
						CcsAcct acctC = rTmAccount.findOne(keyC);
						cust = rTmCustomer.findOne(acctC.getCustId());
						
						//生成销卡销户报表
						output.setCancelRptItem(makeCancelResponseItem(cancel, cust));
						
						//如果锁定码中不存在标志'C'表示销户已被撤销，删除销户记录
						if (!blockCodeUtils.isExists(acctC.getBlockCode(), C_CODE))
							em.remove(cancel);
						
						//保留销户记录,待关闭账户
						break;
						
					//撤销户
					case D:
						//销户撤销户通过账户获取客户信息
						CcsAcctKey keyD = new CcsAcctKey();
						keyD.setAcctNbr(cancel.getAcctNbr());
						keyD.setAcctType(cancel.getAcctType());
						CcsAcct acctD = rTmAccount.findOne(keyD);
						cust = rTmCustomer.findOne(acctD.getCustId());
						
						//生成销卡销户报表
						output.setCancelRptItem(makeCancelResponseItem(cancel, cust));
						
						//移除撤销户记录
						em.remove(cancel);
						break;
					
					default: throw new IllegalArgumentException("不存在的销卡销户请求类型" + cancel.getRequestType().toString());
				}
			}
			
			//关闭账户, 封锁码C + 预销户满N天(包含N天)
			Organization organization = parameterFacility.loadParameter(null, Organization.class);
			if(cancel.getRequestType() == RequestType.C
					&& DateUtils.truncatedCompareTo(batchFacility.getBatchDate(), DateUtils.addDays(cancel.getLogBizDate(), organization.daysBeforeClose), Calendar.DATE) >= 0)
			{
//FXQ           BigDecimal total = BigDecimal.ZERO;
				BigDecimal currBalTotal = BigDecimal.ZERO;
				BigDecimal unmatchDbTotal = BigDecimal.ZERO;
				BigDecimal unmatchCrTotal = BigDecimal.ZERO;
			
				List<U5001AcctGroup> accList = new ArrayList<U5001AcctGroup>();
				
				QCcsAcct qTmAccount = QCcsAcct.ccsAcct;
				//计算本币账户及外币账户的3项余额(当前余额+未匹配借记金额+未匹配贷记金额)
				for(CcsAcct acct : rTmAccount.findAll(qTmAccount.acctNbr.eq(cancel.getAcctNbr()))){
					U5001AcctGroup acc = new U5001AcctGroup();
					//账户
					acc.setAcct(acct);
					//账户联机
					CcsAcctOKey keyO = new CcsAcctOKey();
					keyO.setAcctNbr(acct.getAcctNbr());
					keyO.setAcctType(acct.getAcctType());
					CcsAcctO acctO = rTmAccountO.findOne(keyO);
					acc.setAcctO(acctO);
					//本外币账户
					accList.add(acc);
					
					//3项余额
//FXQ				total = total.add(acct.getCurrBal()).add(acctO.getMemoDb()).add(acctO.getMemoCr());
					currBalTotal = currBalTotal.add(acct.getCurrBal());
					unmatchDbTotal = unmatchDbTotal.add(acctO.getMemoDb());
					unmatchCrTotal = unmatchCrTotal.add(acctO.getMemoCr());
					
					
					//是有效记录; 账户档销户日期为空则已销户撤销, 账户档销户日期与记录业务日期不等则非最后一次销户操作 
					if(acct.getCloseDate() != null && DateUtils.truncatedCompareTo(cancel.getLogBizDate(), acct.getCloseDate(), Calendar.DATE) != 0){
						em.remove(cancel);
						return output;
					}
				}
				
				//若本币账户3项余额 + 外币账户3项余额 = 0，则关闭账户
//FXQ			if(total.compareTo(BigDecimal.ZERO) == 0)
				if(currBalTotal.compareTo(BigDecimal.ZERO) == 0&&unmatchDbTotal.compareTo(BigDecimal.ZERO) == 0&&unmatchCrTotal.compareTo(BigDecimal.ZERO) == 0)
				{
					for(U5001AcctGroup acc : accList)
					{
						//更新账户
						acc.getAcct().setBlockCode(blockCodeUtils.removeBlockCode(acc.getAcct().getBlockCode(), C_CODE));
						acc.getAcct().setBlockCode(blockCodeUtils.addBlockCode(acc.getAcct().getBlockCode(), P_CODE));
						acc.getAcct().setPointsBal(BigDecimal.ZERO);
						acc.getAcct().setClosedDate(batchFacility.getBatchDate());
						//保存
						em.merge(acc.getAcct());
						
						//更新联机账户
						acc.getAcctO().setBlockCode(blockCodeUtils.removeBlockCode(acc.getAcctO().getBlockCode(), C_CODE));
						acc.getAcctO().setBlockCode(blockCodeUtils.addBlockCode(acc.getAcctO().getBlockCode(), P_CODE));
						//保存
						em.merge(acc.getAcctO());
						
						//生成关闭账户报表
						output.setAcctCloseRptItem(makeAcctCloseRptItem(acc.getAcct(), acc.getAcctO(), AcctCloseReason.R00));
					}
					//移除当前成功关闭账户记录
					em.remove(cancel);
				}
				//若不为0，关闭账户失败
				else
				{
					for(U5001AcctGroup acc : accList)
					{
						//返回原因码
						AcctCloseReason reason = checkAcctCloseReason(acc.getAcct(), acc.getAcctO());
						//生成关闭账户报表
						reason = AcctCloseReason.R00.equals(reason)?AcctCloseReason.R99:reason;
						output.setAcctCloseRptItem(makeAcctCloseRptItem(acc.getAcct(), acc.getAcctO(), reason));
					}
				}
			}
			return output;
			
		} catch (Exception e) {
			logger.error("销卡销户及关闭账户异常,账号{}", cancel.getAcctNbr());
			logger.error("销卡销户及关闭账户异常{}", e);
			throw e;
		}
		
	}


	/**
	 * @see 方法名：makeCancelResponseItem 
	 * @see 描述：生成销卡销户报表
	 * @see 创建日期：2015-6-23下午7:55:00
	 * @author ChengChun
	 *  
	 * @param cancel
	 * @param cust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private CancelRptItem makeCancelResponseItem(CcsCardCloseReg cancel, CcsCustomer cust) {
		
		CancelRptItem cancelRptItem = new CancelRptItem();
		
		cancelRptItem.org = cancel.getOrg();
		cancelRptItem.acctNo = cancel.getAcctNbr();
		cancelRptItem.acctType = cancel.getAcctType();
		cancelRptItem.logicCardNo = cancel.getLogicCardNbr();
		cancelRptItem.requestType = cancel.getRequestType();
		cancelRptItem.appDate = cancel.getLogBizDate();
		cancelRptItem.name = cust.getName();
		cancelRptItem.mobile = cust.getMobileNo();
		
		return cancelRptItem;
	}


	/**
	 * @see 方法名：checkAcctCloseReason 
	 * @see 描述：关闭账户条件校验
	 * @see 创建日期：2015-6-23下午7:55:16
	 * @author ChengChun
	 *  
	 * @param acct
	 * @param acctO
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private AcctCloseReason checkAcctCloseReason(CcsAcct acct, CcsAcctO acctO) {
		
		//当前余额不为0
		if(acct.getCurrBal().compareTo(BigDecimal.ZERO) != 0)
		{
			return AcctCloseReason.R01;
		}
		//未匹配借记金额不为0
		if(acctO.getMemoDb().compareTo(BigDecimal.ZERO) != 0)
		{
			return AcctCloseReason.R02;
		}
		//未匹配贷记金额不为0
		if(acctO.getMemoCr().compareTo(BigDecimal.ZERO) != 0)
		{
			return AcctCloseReason.R03;
		}
		
		return AcctCloseReason.R00;
	}


	/**
	 * @see 方法名：makeAcctCloseRptItem 
	 * @see 描述：生成关闭账户报表
	 * @see 创建日期：2015-6-23下午7:56:05
	 * @author ChengChun
	 *  
	 * @param acct
	 * @param acctO
	 * @param reason
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private AcctCloseRptItem makeAcctCloseRptItem(CcsAcct acct, CcsAcctO acctO, AcctCloseReason reason) 
	{
		AcctCloseRptItem acctCloseRptItem = new AcctCloseRptItem();
		
		acctCloseRptItem.org = acct.getOrg();
		acctCloseRptItem.acctNo = acct.getAcctNbr();
		acctCloseRptItem.acctType = acct.getAcctType();
		acctCloseRptItem.cancelDate = acct.getCloseDate();
		acctCloseRptItem.closedDate = acct.getClosedDate();
		acctCloseRptItem.blockCode = acct.getBlockCode();
		acctCloseRptItem.reason = reason;
		acctCloseRptItem.currBal = acct.getCurrBal();
		acctCloseRptItem.unmatchDb = acctO.getMemoDb();
		acctCloseRptItem.unmatchCr = acctO.getMemoCr();
		acctCloseRptItem.defaultLogicalCardNo = acct.getDefaultLogicCardNbr();
		acctCloseRptItem.currencyCode = acct.getAcctType().getCurrencyCode();
		
		return acctCloseRptItem;
	}

}
