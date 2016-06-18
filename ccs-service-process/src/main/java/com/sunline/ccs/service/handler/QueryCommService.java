package com.sunline.ccs.service.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayScheduleHst;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayScheduleHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.service.msdentity.STNQPLPAmtRangeRespSubTermAmtRange;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Service
public class QueryCommService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;

	@Autowired
	public AppapiCommService appapiCommService;
	@Autowired
	public OpenAcctCommService openAcctCommService;
	@Autowired
    private RCcsAcct rCcsAcct;
	@Autowired
    private RCcsAcctO rCcsAcctO;
	@Autowired
    private RCcsCustomer rCcsCustomer;

    @Autowired
    private RCcsLoan rCcsLoan;
    @Autowired
    private RCcsLoanReg rCcsLoanReg;
    @Autowired
    GlobalManagementService globalManagementService;
    
    /*
	 * 获取客户下所有账户（贷款合同）
	 * @param custid 客户号
	 */
	public List<CcsAcct> getAcctlistByCustId(Long custid){
		List<CcsAcct> acctlist ;
		
		try{
			QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
//			Iterable<CcsAcct> accts= rCcsAcct.findAll(qCcsAcct.custId.eq(custid));
			acctlist = new JPAQuery(em).from(qCcsAcct).where(qCcsAcct.custId.eq(custid)).list(qCcsAcct);

			if ( null == acctlist) {
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}
		return acctlist;
	}
	
	/*
	 * 获取账户（合同）下所有放款借据loan 若账户还未放款返回空
	 * @param acctNbr 账号，acctType 账户类型
	 */
	public List<CcsLoan> getLoanlistByAcctInfo(Long acctNbr,AccountType acctType){
		List<CcsLoan> loanlist = new ArrayList<CcsLoan>();
		try{
			QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
			
			Iterable<CcsLoan> loans = rCcsLoan.findAll(qCcsLoan.acctNbr.eq(acctNbr)
					.and( qCcsLoan.acctType.eq(acctType)));
			if(loans!=null){
				for(CcsLoan ccsLoan: loans){
					loanlist.add(ccsLoan);
				}
			}			
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		
		return loanlist;
	}
	/*
	 * 获取账户（合同）下所有放款借据loanreg 若账户还未放款返回空
	 * @param acctNbr 账号，acctType 账户类型
	 */
	public List<CcsLoanReg> getLoanReglistByAcctInfo(Long acctNbr,AccountType acctType){
		List<CcsLoanReg> loanReglist = new ArrayList<CcsLoanReg>();
		try{
			QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
			
			Iterable<CcsLoanReg> loanRegs = rCcsLoanReg.findAll(qCcsLoanReg.acctNbr.eq(acctNbr)
					.and( qCcsLoanReg.acctType.eq(acctType)));
			if(loanRegs!=null){
				for(CcsLoanReg ccsLoanReg: loanRegs){
					loanReglist.add(ccsLoanReg);
				}
			}			
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		
		return loanReglist;
	}
	public List<CcsPlan> getPlanlistByLoan(Long acctNbr ,String refNbr){
		List<CcsPlan> planlist = new ArrayList<CcsPlan>();
		try{
			QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
			planlist = new JPAQuery(em).from(qCcsPlan).where(qCcsPlan.refNbr.eq(refNbr)
					.and(qCcsPlan.acctNbr.eq(acctNbr)))
					.orderBy(qCcsPlan.term.asc()).list(qCcsPlan);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_9998.getCode(), MsRespCode.E_9998.getMessage());
		}
		return planlist;
	}
	
	public List<CcsRepaySchedule> getSchedulelistAfterCurrtermByLoan(Long loanId,Integer term){
		List<CcsRepaySchedule> schedulelist = new ArrayList<CcsRepaySchedule>();
		try{
			QCcsRepaySchedule  qCcsRepaySchedule = QCcsRepaySchedule.ccsRepaySchedule;
			schedulelist = new JPAQuery(em).from(qCcsRepaySchedule)
					.where(qCcsRepaySchedule.loanId.eq(loanId).and(qCcsRepaySchedule.currTerm.gt(term)))
					.list(qCcsRepaySchedule);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}
		return schedulelist;
	}
	
	public List<CcsRepaySchedule> getSchedulelistByLoan(Long loanId){
		List<CcsRepaySchedule> schedulelist = new ArrayList<CcsRepaySchedule>();
		try{
			QCcsRepaySchedule  qCcsRepaySchedule = QCcsRepaySchedule.ccsRepaySchedule;
			schedulelist = new JPAQuery(em).from(qCcsRepaySchedule)
					.where(qCcsRepaySchedule.loanId.eq(loanId))
					.list(qCcsRepaySchedule);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}
		return schedulelist;
	}
	
	public List<CcsRepayScheduleHst> getScheduleHstListByLoan(Long loanId){
		List<CcsRepayScheduleHst> scheduleHstList = new ArrayList<CcsRepayScheduleHst>();
		try{
			QCcsRepayScheduleHst  qCcsRepayScheduleHst = QCcsRepayScheduleHst.ccsRepayScheduleHst;
			scheduleHstList = new JPAQuery(em).from(qCcsRepayScheduleHst)
					.where(qCcsRepayScheduleHst.loanId.eq(loanId))
					.list(qCcsRepayScheduleHst);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
		}
		return scheduleHstList;
	}
	
	/*
	 * 把scheduleHst总欠款计算出来
	 * key为scheduleHst期数
	 */
	public Map<Integer,BigDecimal> tmpTotAmteMapHst(List<CcsRepayScheduleHst> scheduleHstList){
		Map<Integer,BigDecimal> tmpTotAmteMapHst = new HashMap<Integer, BigDecimal>();
		CcsRepayScheduleHst scheduleHst=null;
		CcsRepayScheduleHst scheduleHst2=null;
		Boolean identification=false;
		
		for (int i = 0; i < scheduleHstList.size()-1; i++) {
			scheduleHst=scheduleHstList.get(0);
			
			for (int j = 1; j < scheduleHstList.size(); j++) {
				scheduleHst2=scheduleHstList.get(j);
				//如过scheduleHstList里面有两个相同的期数的数据
				if (scheduleHst2.getCurrTerm()==scheduleHst.getCurrTerm()) {
					
					identification=true;
					//两个相同期数的数据，删除旧数据
					if (scheduleHst2.getCreateTime().after(scheduleHst.getCreateTime())) {
						scheduleHstList.remove(i);
					}else {
						scheduleHstList.remove(j);
					}
					break;
				}
			}
			if (identification==true) {
				break;
			}
		}
		
		
		
		for(CcsRepayScheduleHst scheduleHst3 : scheduleHstList){
			BigDecimal tmpTotAmt= BigDecimal.ZERO;
			 //当期总欠款
			tmpTotAmt = tmpTotAmt.add(scheduleHst3.getLoanTermPrin()==null?BigDecimal.ZERO:scheduleHst3.getLoanTermPrin()).
						add(scheduleHst3.getLoanTermFee()==null?BigDecimal.ZERO:scheduleHst3.getLoanTermFee()).
						add(scheduleHst3.getLoanTermInt()==null?BigDecimal.ZERO:scheduleHst3.getLoanTermInt()).
						add(scheduleHst3.getLoanStampdutyAmt()==null?BigDecimal.ZERO:scheduleHst3.getLoanStampdutyAmt()).
						add(scheduleHst3.getLoanLifeInsuAmt()==null?BigDecimal.ZERO:scheduleHst3.getLoanLifeInsuAmt()).
						add(scheduleHst3.getLoanSvcFee()==null?BigDecimal.ZERO:scheduleHst3.getLoanSvcFee()).
						add(scheduleHst3.getLoanReplaceSvcFee()==null?BigDecimal.ZERO:scheduleHst3.getLoanReplaceSvcFee()).
						add(scheduleHst3.getLoanInsuranceAmt()==null?BigDecimal.ZERO:scheduleHst3.getLoanInsuranceAmt());
			tmpTotAmteMapHst.put(scheduleHst3.getCurrTerm(), tmpTotAmt);
		 }
		 return tmpTotAmteMapHst;
	}
	
	/*
	 * 放款试算
	 * @param 输入: loanplan,loanfeedef,
	 * @param       loanAmt ,loanTerm
	 * 				agreementRate ,lifeInsuranceInd
	 */
//	public CcsLoanReg loanRegPrecalculate(CcsAcct acct,LoanPlan loanPlan,LoanFeeDef loanFeeDef,
//			BigDecimal loanAmt,Integer loanTerm,BigDecimal agreementRate,Indicator lifeInsuranceInd){
//		
////		String refNo = null;//外部流水号  b037
//		CcsLoanReg loanReg = appapiCommService.genLoanReg(loanTerm, 
//				loanAmt, null, null,null, null,
//				null, loanPlan.loanCode, null);
//		
//		if(logger.isDebugEnabled())
//			logger.debug("协议利率为[{}],loanReg基础利率为[{}]",agreementRate,loanReg.getInterestRate());
//		if(null !=agreementRate && agreementRate.compareTo(BigDecimal.ZERO)>0){
//			loanReg.setInterestRate(agreementRate);
//		}
//		else{
//			loanReg.setInterestRate(null==loanFeeDef.interestRate?BigDecimal.ZERO:loanFeeDef.interestRate);//基础利率			
//		}
//		loanReg.setLoanFeeDefId(loanFeeDef.loanFeeDefId.toString());//分期子产品编号
//		loanReg.setLoanFeeMethod(loanFeeDef.loanFeeMethod);//分期手续费收取方式
//		loanReg.setLifeInsuFeeMethod(loanFeeDef.lifeInsuFeeMethod);//寿险计划包费收取方式
//		loanReg.setStampdutyMethod(loanFeeDef.stampMethod);//印花税率收取方式
//		loanReg.setJoinLifeInsuInd(lifeInsuranceInd);
//		
//		loanReg = openAcctCommService.setLoanRegRateByAcct(loanReg, acct, loanFeeDef);
//
//		loanReg.setLoanInsFeeMethod(loanFeeDef.insCollMethod);//保险费收取方式		
//		
//		//寿险计划包
//		if(lifeInsuranceInd == Indicator.Y){
//			loanReg.setTotLifeInsuAmt(LoanLifeInsuFeeMethodimple.valueOf(loanReg.getLifeInsuFeeMethod().toString()).loanLifeInsuFee(loanReg, loanFeeDef,0));
//		}else{
//			loanReg.setTotLifeInsuAmt(BigDecimal.ZERO);
//		}
//		//总保费=贷款金额*总期数*保费率
//		if(null != loanReg.getLoanInsFeeMethod() ){
//			loanReg.setInsuranceAmt(LoanlnsuranceMethodimple.valueOf(loanReg.getLoanInsFeeMethod().toString()).loanlnsurance(loanReg, loanFeeDef,0));			
//		}
//		else{
//			loanReg.setInsuranceAmt(BigDecimal.ZERO);
//		}
//		//总手续费
//		if(null != loanReg.getLoanFeeMethod()){
//			loanReg.setLoanInitFee(LoanFeeMethodimple.valueOf(loanReg.getLoanFeeMethod().toString()).loanFee(loanReg, loanFeeDef,0));
//		}else{
//			loanReg.setLoanInitFee(BigDecimal.ZERO);
//		}
//		//总印花税
//		if(null != loanReg.getStampdutyMethod()){
//			loanReg.setStampdutyAmt(LoanStampTAXMethodimple.valueOf(loanReg.getStampdutyMethod().toString()).loanStampTAX(loanReg, loanFeeDef,0));
//		}else{
//			loanReg.setStampdutyAmt(BigDecimal.ZERO);
//		}
//
//		loanReg.setLoanType(loanPlan.loanType);
//		loanReg.setContrNbr(null);
//		
//		loanReg.setDueBillNo(null);//借据号
//		loanReg.setLoanRegStatus(LoanRegStatus.A);
//		
//		return loanReg;
//	}
	
	/**
	 * 根据订单表加载核心账户信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public CcsAcct loadAcct( Long acctNbr,AccountType acctType,boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("根据订单表加载核心账户信息--loadAcctByOrder");

		try {

			QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
			CcsAcct acct = rCcsAcct.findOne(qCcsAcct.acctNbr.eq(acctNbr).and(
					qCcsAcct.acctType.eq(acctType)));

			if (nullException && null == acct) {
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}
			return acct;
				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
		}
	}
	/**
	 * 加载联机账户信息
	 * 
	 * @param nullException
	 */
	public CcsAcctO loadAcctO( Long acctNbr,AccountType acctType,boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("根据订单加载联机账户信息--loadAcctOByOrder");

		try {
			
			QCcsAcctO qCcsAcctO = QCcsAcctO.ccsAcctO;
			CcsAcctO acctO = rCcsAcctO.findOne(qCcsAcctO.acctNbr.eq(acctNbr)
					.and(qCcsAcctO.acctType.eq(acctType)));

			if (nullException && null == acctO) {
				throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			}
			return acctO;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
		}
	}
	
	/**
	 * 加载客户信息
	 * 
	 * @param context
	 * @param nullException
	 */
	public CcsCustomer loadCustomerByCustId(Long custId, boolean nullException) {
		if(logger.isDebugEnabled())
			logger.debug("加载客户信息--loadCustomer");
		try {
			
			QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
			CcsCustomer customer = rCcsCustomer.findOne(qCcsCustomer.custId
					.eq(custId));
			if (nullException && null == customer) {
				throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
			}
			return customer;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
		}
	}
	
	/*
	 * 根据账户信息获取账单列表
	 */
	public List<CcsStatement> loadStmtListByAcct(CcsAcct acct){
		if(logger.isDebugEnabled())
			logger.debug("加载账单信息--loadStatement");
		try {
			
			QCcsStatement qCcsStatement = QCcsStatement.ccsStatement;
			
			List<CcsStatement> stmt = new JPAQuery(em).from(qCcsStatement)
					.where(qCcsStatement.acctNbr.eq(acct.getAcctNbr())
						.and(qCcsStatement.acctType.eq(acct.getAcctType())))
					.list(qCcsStatement);

//			if (nullException && null == stmt) {
//				throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
//			}
			return stmt;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1023.getCode(), MsRespCode.E_1023.getMessage());
		}
	}

	
	
	
	/**
	 * 获取产品下等额本息的分期定价参数中各期数的分期金额区间
	 * @param req
	 */	
	public List<STNQPLPAmtRangeRespSubTermAmtRange> getLoanFeeDefAmtRangeSetByLoanCd(String loanCode){
		
//	    LoanPlan loanPlan = unifiedParamFacilityProvide.loanPlan(loanCode);
		//获取 贷款参数
		LoanPlan loanPlan;
		try{
			loanPlan = unifiedParamFacilityProvide.loanPlan(loanCode);
			//如果产品为空，或者产品状态为非使用中或者当前业务日期大于产品有效期则抛出无效贷款产品
			if(loanPlan == null
					|| loanPlan.loanStaus != LoanPlanStatus.A
					|| loanPlan.loanValidity.before(globalManagementService.getSystemStatus().getBusinessDate())){
				throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());				
			}
		}
		catch(Exception e){
			throw new ProcessException(MsRespCode.E_1052.getCode(),MsRespCode.E_1052.getMessage());				
		}

	    //key - term ,value - termAmtRange
	    Map<Integer,STNQPLPAmtRangeRespSubTermAmtRange> termAmtRangeMap = new HashMap<Integer,STNQPLPAmtRangeRespSubTermAmtRange>();
	    
	    //获取所有期数，合并分期金额区间
	    for(Integer key:loanPlan.loanFeeDefMap.keySet()){
	    	LoanFeeDef loanFeeDef = loanPlan.loanFeeDefMap.get(key);
	    	//如果子产品不等于空且状态不为使用中则跳过
	    	if (null == loanFeeDef || LoanFeeDefStatus.A != loanFeeDef.loanFeeDefStatus) {
	    		continue;
			}
	    	//第一次取得该期数
	    	if(termAmtRangeMap.get(loanFeeDef.initTerm) == null){
	    		STNQPLPAmtRangeRespSubTermAmtRange tmpRange = new STNQPLPAmtRangeRespSubTermAmtRange(loanFeeDef.initTerm,loanFeeDef.minAmount,loanFeeDef.maxAmount);
	    		termAmtRangeMap.put(loanFeeDef.initTerm, tmpRange);
	    	}
	    	else{//已取得过该期数，合并最大最小值区间
	    		if(termAmtRangeMap.get(loanFeeDef.initTerm).maxAmount.compareTo(loanFeeDef.maxAmount)<0){
	    			termAmtRangeMap.get(loanFeeDef.initTerm).maxAmount=(loanFeeDef.maxAmount);
	    		}
	    		
	    		if(termAmtRangeMap.get(loanFeeDef.initTerm).minAmount.compareTo(loanFeeDef.minAmount)>0){
	    			termAmtRangeMap.get(loanFeeDef.initTerm).minAmount=(loanFeeDef.minAmount);
	    		}
	    	}
	    }
	    List<STNQPLPAmtRangeRespSubTermAmtRange> termAmtRangeList = new ArrayList<STNQPLPAmtRangeRespSubTermAmtRange>();
	    //map 转成list
	    for(Integer key: termAmtRangeMap.keySet()){
	    	termAmtRangeList.add(termAmtRangeMap.get(key));
	    }
		return termAmtRangeList;
	}
	
	public void batchProcessingCheck(CcsAcct acct,CcsAcctO accto){
		if (logger.isDebugEnabled())
			logger.debug("核心账户最新同步日期--[{}],联机账户最新同步日期[{}]",acct.getLastSyncDate(),accto.getLastSyncDate());
		
		if(null==acct.getLastSyncDate()&&null!=accto.getLastSyncDate()){
			throw new ProcessException(MsRespCode.E_1066.getCode(), MsRespCode.E_1066.getMessage());
		}
		
		if(null!=acct.getLastSyncDate()&&null==accto.getLastSyncDate()){
			throw new ProcessException(MsRespCode.E_1066.getCode(), MsRespCode.E_1066.getMessage());
		}
		
		if(null!=acct.getLastSyncDate()&&null!=accto.getLastSyncDate()
				&&acct.getLastSyncDate().compareTo(accto.getLastSyncDate())!=0){
			throw new ProcessException(MsRespCode.E_1066.getCode(), MsRespCode.E_1066.getMessage());
		}
	}
}
