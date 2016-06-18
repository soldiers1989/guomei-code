package com.sunline.ccs.batch.cc8400;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsXsyncAcctTmp;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.param.def.enums.TransAmtDirection;
import com.sunline.kylin.web.ark.client.utils.DataTypeUtils;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：P8401AcctUpdList
 * @see 描述：日切、处理UpdatingList
 *
 * @see 创建日期：   2015-6-24下午2:30:11
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P8401AcctUpdList implements ItemProcessor<CcsXsyncAcctTmp, List<U8401AcctO>> {

	public static final QCcsAuthmemoO UMO = QCcsAuthmemoO.ccsAuthmemoO;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private BatchStatusFacility batchFacility;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<U8401AcctO> process(CcsXsyncAcctTmp item) throws Exception {
		List<U8401AcctO> acctList = new ArrayList<U8401AcctO>();
		// 批量期间匹配类交易数据
		Integer otherDayTxnCount = 0;
		// 根据updatingList的数据查询Accto
		CcsAcctO acct = findAccountOByAcctNo(item);
		if (null == acct) {
			log.warn("没有找到账户信息。账号 : [" + item.getAcctNbr() + "] , 类型 : [" + item.getAcctType() + "]");
			throw new ProcessException("没有找到账户信息。账号 : [" + item.getAcctNbr() + "] , 类型 : [" + item.getAcctType() + "]");
		}
		// 处理之前的数据
		U8401AcctO beforeAcct = generateFromMap(item.convertToMap(), Indicator.N, null);
		// 添加文件数据
		acctList.add(beforeAcct);
		// AFU 流程1根据AccountUpdList覆盖accountO
		setAccountO(acct, item);
		// AFU处理流程2 计算更新OTB相关值
		execBatchTxn(acct, item, otherDayTxnCount);
		// 更新数据库
		em.merge(acct);
		// 处理之后的数据
		U8401AcctO b = generateFromMap(acct.convertToMap(), Indicator.Y, otherDayTxnCount);
		acctList.add(b);
		
		return acctList;
	}
        /**
         * @see 方法名：setAccountO 
         * @see 描述：TODO 方法描述
         * @see 创建日期：2015-6-24下午2:33:30
         * @author ChengChun
         *  
         * @param ao
         * @param au
         * 
         * @see 修改记录： 
         * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
         */
	public void setAccountO(CcsAcctO ao, CcsXsyncAcctTmp au) {
		ao.setCurrBal(au.getCurrBal());
		ao.setCashBal(au.getCashBal());
		ao.setLoanBal(au.getLoanBal());
		ao.setDisputeAmt(au.getDisputeAmt());
		ao.setBlockCode(au.getBlockCode());
		ao.setLtdLoanAmt(au.getLtdLoanAmt());
		//最新同步日期-20151022
		ao.setLastSyncDate(batchFacility.getSystemStatus().getBusinessDate());
	}

	/**
	 * @see 方法名：findAccountOByAcctNo 
	 * @see 描述：根据账户号查询一个账户
	 * @see 创建日期：2015-6-24下午2:33:06
	 * @author ChengChun
	 *  
	 * @param au
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsAcctO findAccountOByAcctNo(CcsXsyncAcctTmp au) {
		CcsAcctOKey acctKey = new CcsAcctOKey();
		acctKey.setAcctNbr(au.getAcctNbr());
		acctKey.setAcctType(au.getAcctType());
		return em.find(CcsAcctO.class, acctKey);
	}

	/**
	 * @see 方法名：execBatchTxn 
	 * @see 描述：判断是否有批量期间的交易(List)
	 * @see 创建日期：2015-6-24下午2:32:53
	 * @author ChengChun
	 *  
	 * @param acct
	 * @param au
	 * @param otherDayTxnCount
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public CcsAcctO execBatchTxn(CcsAcctO acct, CcsXsyncAcctTmp au, Integer otherDayTxnCount) {
		// 判断批量期间是否有联机交易
		if (null != acct.getLastUpdateBizDate() && acct.getLastUpdateBizDate().compareTo(batchFacility.getBatchDate()) > 0) {
			// 判断批量期间有匹配类交易
			if (null != acct.getLastMatchedOvernightDate() && acct.getLastMatchedOvernightDate().compareTo(batchFacility.getBatchDate()) > 0) {
				// 查询 EOD - NOW的所有成功的隔日匹配交易（list）
				List<CcsAuthmemoO> txnList = findOtherDayTxn(acct.getAcctNbr());
				// 获隔日匹配类交易的数量
				otherDayTxnCount = txnList == null ? 0 :txnList.size();
				for (CcsAuthmemoO currLog : txnList) {
					// 根据m的原交易KV 查询 ORIG-LOG
					CcsAuthmemoO origLog = em.find(CcsAuthmemoO.class, currLog.getOrigLogKv());
					if (null != origLog) {
						// 此处状态只检查N，是因为此处只考虑预授权完成或第一次冲正，其他隔日匹配认为不存在
						if (origLog.getAuthTxnStatus() == AuthTransStatus.N) {
							log.warn("原交易状态异常。账号 : [" + origLog.getAcctNbr() + "] , 类型 : [" + origLog.getAcctType() + "] , 交易状态 : [" + origLog.getAuthTxnStatus() + "]");
							continue;
						}
						// 预授权
						if (origLog.getTxnType() == AuthTransType.PreAuth) {
							// 更新batch-db =[batch-db] - [orig-chb-txn-amt] + [comp-chb-txn-amt]
							au.setMemoDb(au.getMemoDb().subtract(currLog.getOrigChbTxnAmt()).add(currLog.getChbTxnAmt()));
						} else if (origLog.getFinalUpdDirection().equals("D")) {
							// 取现
							if (origLog.getTxnType() == AuthTransType.Cash || origLog.getTxnType() == AuthTransType.TransferDeditDepos) {
								// 更新batch-cash=[batch-cash] -orig-chb-txn-amt]
								au.setMemoCash(au.getMemoCash().subtract(currLog.getOrigChbTxnAmt()));
							}
							// 更新batch-db = [batch-db] - [orig-chb-txn-amt]
							au.setMemoDb(au.getMemoDb().subtract(currLog.getOrigChbTxnAmt()));
						} else if (origLog.getFinalUpdDirection().equals("C")) {
							// 更新batch-cr = [batch-cr] - [orig-chb-txn-amt]
							au.setMemoCr(au.getMemoCr().subtract(currLog.getOrigChbTxnAmt()));
						}
						
						// db<0 则修正为0
						if(au.getMemoDb().compareTo(BigDecimal.ZERO) == -1){
							au.setMemoDb(BigDecimal.ZERO);
						}
						// Cash<0 则修正为0
						if(au.getMemoCash().compareTo(BigDecimal.ZERO) == -1){
							au.setMemoCash(BigDecimal.ZERO);
						}
						
					} else {
						log.warn("原交易为空。原交易键值 : [" + currLog.getOrigLogKv() + "]");
					}
				}
			}
			// TODO 累计交易数量（二期）
			// 有交易 SUM DB和SUM CR
			//任何时间的在途交易,额度被冻结,需要显示在memoDb上
			acct.setMemoDb(au.getMemoDb().add(sumDbCr(acct.getAcctNbr(), TransAmtDirection.D))
					.add(sumOnWayDbCr(acct.getAcctNbr(), TransAmtDirection.D)));
			acct.setMemoCr(au.getMemoCr().add(sumDbCr(acct.getAcctNbr(), TransAmtDirection.C)));
			acct.setMemoCash(au.getMemoCash().add(sumCash(acct.getAcctNbr())));
		} else {
			// TODO 累计交易数量（二期）
			log.debug("acctNo=["+au.getAcctNbr()+"],acctType=["+au.getAcctType()+"]批量期间没有联机交易");
			acct.setMemoDb(au.getMemoDb());
			acct.setMemoCr(au.getMemoCr());
			acct.setMemoCash(au.getMemoCash());
		}
		return acct;
	}

	/**
	 * @see 方法名：findOtherDayTxn 
	 * @see 描述：查询[EOD-NOW]的所有成功的隔日匹配交易，目前只累计隔日预授权完成和隔日冲正
	 * @see 创建日期：2015-6-24下午2:32:14
	 * @author ChengChun
	 *  
	 * @param acctNo
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public List<CcsAuthmemoO> findOtherDayTxn(Long acctNo) {
		return new JPAQuery(em)
				.from(UMO)
					.where(UMO.acctNbr.eq(acctNo)
							.and(UMO.origBizDate.isNotNull()).and(UMO.origBizDate.loe(batchFacility.getBatchDate()))
							.and(UMO.logBizDate.gt(batchFacility.getBatchDate()))
							.and(UMO.finalAction.eq(AuthAction.A))
							.and(UMO.authTxnStatus.eq(AuthTransStatus.N))
							.and(UMO.txnDirection.eq(AuthTransDirection.Reversal).or(UMO.txnType.in(AuthTransType.PAComp, AuthTransType.AdviceSettle)))
						).list(UMO);
	}

	/**
	 * @see 方法名：sumDbCr 
	 * @see 描述：SUM DB | CR 日切后所有通过的状态正常的非隔日正向交易
	 * @see 创建日期：2015-6-24下午2:32:01
	 * @author ChengChun
	 *  
	 * @param acctNo
	 * @param direction
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal sumDbCr(Long acctNo, TransAmtDirection direction) {
		BigDecimal bd = new JPAQuery(em)
				.from(UMO)
					.where(UMO.acctNbr.eq(acctNo)
							.and(UMO.txnDirection.in(AuthTransDirection.Normal, AuthTransDirection.Advice, AuthTransDirection.Confirm))
							.and(UMO.txnType.notIn(AuthTransType.ContractBuildUp,AuthTransType.ContractTermination,AuthTransType.AcctVerfication))
							.and(UMO.origBizDate.eq(UMO.logBizDate).or(UMO.origBizDate.isNull()))
							.and(UMO.logBizDate.gt(batchFacility.getBatchDate()))
							.and(UMO.finalAction.eq(AuthAction.A))
							.and(UMO.authTxnStatus.in(AuthTransStatus.N, AuthTransStatus.O))
							.and(UMO.finalUpdDirection.eq(direction.toString()))
							).singleResult(UMO.chbTxnAmt.sum());
		return null == bd ? BigDecimal.ZERO : bd;
	}
	
	/**
	 * @see 方法名：sumOnWayDbCr 
	 * @see 描述：SUM DB | CR 日切后所有的状态在途的正向交易
	 * @see 创建日期：2015-6-24下午2:32:01
	 * @author ChengChun
	 *  
	 * @param acctNo
	 * @param direction
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal sumOnWayDbCr(Long acctNo, TransAmtDirection direction) {
		BigDecimal bd = new JPAQuery(em)
				.from(UMO)
					.where(UMO.acctNbr.eq(acctNo)
							.and(UMO.txnDirection.in(AuthTransDirection.Normal))
							.and(UMO.txnType.in(AuthTransType.AgentCredit,AuthTransType.AgentDebit))
							.and(UMO.origBizDate.eq(UMO.logBizDate).or(UMO.origBizDate.isNull()))
							.and(UMO.logBizDate.gt(batchFacility.getBatchDate()))
//							.and(UMO.finalAction.eq(AuthAction.A))
							.and(UMO.authTxnStatus.in(AuthTransStatus.P))
							.and(UMO.finalUpdDirection.eq(direction.toString()))
							).singleResult(UMO.chbTxnAmt.sum());
		return null == bd ? BigDecimal.ZERO : bd;
	}

	/**
	 * @see 方法名：sumCash 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015-6-24下午2:31:49
	 * @author ChengChun
	 *  
	 * @param acctNo
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public BigDecimal sumCash(Long acctNo) {
		BigDecimal bd = new JPAQuery(em)
				.from(UMO)
					.where(UMO.acctNbr.eq(acctNo)
							.and(UMO.txnDirection.in(AuthTransDirection.Normal, AuthTransDirection.Advice))
							.and(UMO.logBizDate.gt(batchFacility.getBatchDate()))
							.and(UMO.finalAction.eq(AuthAction.A))
							.and(UMO.authTxnStatus.eq(AuthTransStatus.N))
							.and(UMO.txnType.in(AuthTransType.Cash,AuthTransType.TransferDeditDepos))
							).singleResult(UMO.chbTxnAmt.sum());
		return null == bd ? BigDecimal.ZERO : bd;
	}
	
	/**
	 * @see 方法名：generateFromMap 
	 * @see 描述：转换模型数据
	 * @see 创建日期：2015-6-24下午2:31:36
	 * @author ChengChun
	 *  
	 * @param map
	 * @param batchFlag
	 * @param otherDayTxnCount
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public U8401AcctO generateFromMap(Map<String, Serializable> map, Indicator batchFlag, Integer otherDayTxnCount) {
		U8401AcctO accto = new U8401AcctO();
		if (map.containsKey("acctNbr"))
			accto.acctNo = DataTypeUtils.getLongValue(map.get("acctNbr"));
		if (map.containsKey("acctType"))
			accto.acctType = DataTypeUtils.getEnumValue(map.get("acctType"), AccountType.class);
		if (map.containsKey("org"))
			accto.org = DataTypeUtils.getStringValue(map.get("org"));
		if (map.containsKey("currBal"))
			accto.currBal = DataTypeUtils.getBigDecimalValue(map.get("currBal"));
		if (map.containsKey("cashBal"))
			accto.cashBal = DataTypeUtils.getBigDecimalValue(map.get("cashBal"));
		if (map.containsKey("loanBal"))
			accto.loanBal = DataTypeUtils.getBigDecimalValue(map.get("loanBal"));
		if (map.containsKey("disputeAmt"))
			accto.disputeAmt = DataTypeUtils.getBigDecimalValue(map.get("disputeAmt"));
		if (map.containsKey("memoDb"))
			accto.unmatchDb = DataTypeUtils.getBigDecimalValue(map.get("memoDb"));
		if (map.containsKey("memoCr"))
			accto.unmatchCr = DataTypeUtils.getBigDecimalValue(map.get("memoCr"));
		if (map.containsKey("memoCash"))
			accto.unmatchCash = DataTypeUtils.getBigDecimalValue(map.get("memoCash"));
		if (map.containsKey("blockCode"))
			accto.blockCode = DataTypeUtils.getStringValue(map.get("blockCode"));
		if (map.containsKey("ltdLoanAmt"))
			accto.ltdLoanAmt = DataTypeUtils.getStringValue(map.get("ltdLoanAmt"));
		if (batchFlag != null)
			accto.batchFlag = batchFlag;
		if (otherDayTxnCount != null)
			accto.otherDayTxnCount = otherDayTxnCount;
		
		return accto;
	}
	
	
	
}
