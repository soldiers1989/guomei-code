package com.sunline.ccs.service.handler.query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.facility.contract.AcctOTBCal;
import com.sunline.ccs.facility.contract.ConstractStatusUtil;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayScheduleHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.QCcsRepayScheduleHst;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msdentity.STNQAAcctsbyCustUUIDRESPSubContract;
import com.sunline.ccs.service.msdentity.STNQAAcctsbyCustUUIDRESPSubLoan;
import com.sunline.ccs.service.msdentity.STNQAAcctsbyCustUUIDReq;
import com.sunline.ccs.service.msdentity.STNQAAcctsbyCustUUIDResp;
import com.sunline.ccs.service.util.DateFormatUtil;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 合同列表查询
 * @author zqx
 *
 */
@Service
public class TNQAAcctsbyCustUUID {

	@PersistenceContext
	protected EntityManager em;
	@Value("#{env.respDateFormat}")
	private String respDateFormat;
    @Autowired
    private RCcsCustomer rCustomer;
    @Autowired
    UnifiedParamFacilityProvide unifiedParamFacilityProvide;
    @Autowired
    UnifiedParameterFacility unifiedParameterFacility;
    @Autowired
    private QueryCommService queryCommService;
    @Autowired
    ConstractStatusUtil constractStatusUtil;
    @Autowired
    AcctOTBCal acctOTBCal;
    @Autowired
    GlobalManagementService globalManagementService;
    @Autowired
    AppapiCommService appapiCommService;
	@Autowired
	private MicroCreditRescheduleUtils rescheduleUtils;

    private Logger log = LoggerFactory.getLogger(this.getClass());

//    Date bizDate;
	public STNQAAcctsbyCustUUIDResp handler(STNQAAcctsbyCustUUIDReq req) throws ProcessException {
		LogTools.printLogger(log, "TNQAAcctsbyCustUUID", "合同列表查询", req, true);
		LogTools.printObj(log ,req, "请求参数TNQAAcctsbyCustUUID");

		TxnInfo txnInfo = new TxnInfo();

		STNQAAcctsbyCustUUIDResp resp= new STNQAAcctsbyCustUUIDResp();
		try{
			//传入字段检查
			if(!StringUtils.equals(req.getQueryRangeInd(),"E")
			&& !StringUtils.equals(req.getQueryRangeInd(),"A")){
				throw new ProcessException(MsRespCode.E_1008.getCode(), MsRespCode.E_1008.getMessage()+",查询范围错误");
			}
			resp = getQueryInfo(req);
		}
		catch(ProcessException pe){
			if(log.isErrorEnabled())
					log.error(pe.getMessage(),pe);
			appapiCommService.preException(pe, pe, txnInfo);
		}
		catch(Exception e){
			if(log.isErrorEnabled())
				log.error(e.getMessage(),e);
			appapiCommService.preException(e, null, txnInfo);				
		}finally{
			LogTools.printLogger(log, "STNQAAcctsbyCustUUIDResp", "合同列表查询", resp, false);
		}

		setResponse(resp,  txnInfo);
		return resp;
	}

	private STNQAAcctsbyCustUUIDResp getQueryInfo(STNQAAcctsbyCustUUIDReq req ){
		String internalCustomerId = req.getInternalCustomerId();
		
		CcsCustomer customer = null;
		List<CcsAcct> acctlist= new ArrayList<CcsAcct>();
		STNQAAcctsbyCustUUIDResp respInfo = new STNQAAcctsbyCustUUIDResp();
		
		QCcsCustomer qCustomer = QCcsCustomer.ccsCustomer;
		
		//根据统一客户id 得到客户表
		customer = new JPAQuery(em).from(qCustomer).where(qCustomer.internalCustomerId.eq(internalCustomerId)).singleResult(qCustomer);
		if ( null == customer) {
			throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
		}

		respInfo.setInternalCustomerId(internalCustomerId);
		//根据客户信息获取账户列表（合同列表）
		List<STNQAAcctsbyCustUUIDRESPSubContract> subContractlist = new ArrayList<STNQAAcctsbyCustUUIDRESPSubContract>();		
		acctlist = queryCommService.getAcctlistByCustId(customer.getCustId());
		for(CcsAcct acct: acctlist){
			CcsAcctO accto = queryCommService.loadAcctO(acct.getAcctNbr(), acct.getAcctType(), true);
			//批量时间检查
			queryCommService.batchProcessingCheck(acct, accto);
			
			Map<String,Object> returnmap = setSubContract(acct,accto,req);
			STNQAAcctsbyCustUUIDRESPSubContract subContract = (STNQAAcctsbyCustUUIDRESPSubContract) returnmap.get("subContract");
			//合同是否有效
			Indicator isContractActive = (Indicator) returnmap.get("isContractActive");
			
			// 若选择返回为有效合同 则合同有效才返回
			if(StringUtils.equals( req.getQueryRangeInd(),"E")!=true
			|| isContractActive != Indicator.N ){
				subContractlist.add(subContract);
			}
		}
		respInfo.setContracts(subContractlist);
			
		return respInfo;
	}
	
	/*
	 *  设置合同具体数据
	 *  @param CcsAcct,CcsAcctO
	 *  @return Map<String,Object> 
	 *  	key("subContract") -- STNQAAcctsbyCustUUIDRESPSubContract
	 *  	key("haveActiveLoan") -- Indicator   //合同是否还有活跃的贷款
	 */
	public Map<String,Object>  setSubContract(CcsAcct acct,CcsAcctO accto,STNQAAcctsbyCustUUIDReq req){
		STNQAAcctsbyCustUUIDRESPSubContract contract = new STNQAAcctsbyCustUUIDRESPSubContract();
		Indicator haveActiveLoan = Indicator.N;
		List<CcsLoan> loanlist = queryCommService.getLoanlistByAcctInfo(acct.getAcctNbr(), acct.getAcctType());
		List<CcsLoanReg> loanReglist = queryCommService.getLoanReglistByAcctInfo(acct.getAcctNbr(), acct.getAcctType());
		ProductCredit productCredit = unifiedParameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
		String loanCode = productCredit.loanPlansMap.get(productCredit.defaultLoanType);
		LoanPlan loanPlan = unifiedParameterFacility.loadParameter(loanCode,LoanPlan.class);

		
		contract.setContrNbr(acct.getContrNbr()); //合同号
		//获取默认贷款产品
		contract.setDefaultLoanCode(loanCode);
		contract.setDefaultLoanMold(loanPlan.loanMold);
		contract.setContrLmt(acct.getCreditLmt()); //合同额度
		contract.setContraStatus(constractStatusUtil.getConstractStatus(acct)); //合同状态 
		contract.setContraExpireDate(DateFormatUtil.format(acct.getAcctExpireDate(), respDateFormat));//合同到期日期
		contract.setContraBal(acct.getCurrBal());//欠款总额
		contract.setContraBalPrin(acct.getPrincipalBal());//欠款本金
 		BigDecimal qualGraceBal = acct.getTotDueAmt();
		BigDecimal currDueAmt = acct.getCurrDueAmt()==null?BigDecimal.ZERO:acct.getCurrDueAmt();
		BigDecimal pastDueAmt = BigDecimal.ZERO
				.add(acct.getTotDueAmt()==null?BigDecimal.ZERO:acct.getTotDueAmt())
				.subtract(acct.getCurrDueAmt()==null?BigDecimal.ZERO:acct.getCurrDueAmt());
		//帮前段把 未入账还款金额计算进去
		qualGraceBal = qualGraceBal.subtract(accto.getMemoCr());
		qualGraceBal = qualGraceBal.compareTo(BigDecimal.ZERO)<=0?BigDecimal.ZERO:qualGraceBal;

		if(pastDueAmt.compareTo(accto.getMemoCr()) > 0){
			pastDueAmt = pastDueAmt.subtract(accto.getMemoCr());
		}
		else{
			BigDecimal remainCr = accto.getMemoCr().subtract(pastDueAmt);
			pastDueAmt = BigDecimal.ZERO;//先计算剩余还款额，再修正往期最小还款额
			if(currDueAmt.compareTo(remainCr)>0){
				currDueAmt = currDueAmt.subtract(remainCr);				
			}
			else{
				currDueAmt = BigDecimal.ZERO;
			}
		}
		
		contract.setQualGraceBal(qualGraceBal); //全部应还款额
		contract.setCurrDueAmt(currDueAmt); //当期应还款额
		contract.setPastDueAmt(pastDueAmt); //往期应还款额
		contract.setContraMemoDb(accto.getMemoDb()==null?BigDecimal.ZERO:accto.getMemoDb());
		contract.setContraMemoCr(accto.getMemoCr()==null?BigDecimal.ZERO:accto.getMemoCr());
		//计算可用额度
		CcsAcctO acctO = queryCommService.loadAcctO(acct.getAcctNbr(), acct.getAcctType(), true);
		BigDecimal acctOTB = acctOTBCal.getAcctOTB(null, acct, acctO, req.getBizDate() );
		contract.setContraRemain(acctOTB.compareTo(BigDecimal.ZERO)<=0?BigDecimal.ZERO:acctOTB); //可用额度
		
		contract.setContraSetupDate(DateFormatUtil.format(acct.getSetupDate(), respDateFormat)); //开户日期
		Date nextPmtDueDate = rescheduleUtils.getNextPaymentDay(acct.getProductCd(), acct.getNextStmtDate());
		contract.setNextPmtDueDate(DateFormatUtil.format(nextPmtDueDate, respDateFormat)); //下一到期还款日
		contract.setPmtDueDate(DateFormatUtil.format(acct.getPmtDueDate(), respDateFormat));
		contract.setBizDate(DateFormatUtil.format(req.getBizDate(),respDateFormat));
		contract.setLastStmtDate(DateFormatUtil.format(acct.getLastStmtDate(),respDateFormat));
		contract.setNextStmtDate(DateFormatUtil.format(acct.getNextStmtDate(),respDateFormat));
		contract.setCreatTime(DateFormatUtil.format(acct.getCreateTime(), "yyyyMMddHHmmssSSSS") );
		contract.setContrLstUpdTime(DateFormatUtil.format(acct.getLstUpdTime(), "yyyyMMddHHmmssSSSS") );//20151210
		contract.setDdBankAcctNbr(acct.getDdBankAcctNbr()); //约定还款账号
		contract.setDdBankAcctName(acct.getDdBankAcctName()); //约定还款账户姓名
		contract.setDdBankBranch(acct.getDdBankBranch()); //约定还款开户行号
		contract.setDdBankName(acct.getDdBankName()); //约定还款银行名称
		contract.setDdBankProvince(acct.getDdBankProvince()); //约定还款开户行省
		contract.setDdBankProvCode(acct.getDdBankProvinceCode()); //约定还款开户行省code
		contract.setDdBankCity(acct.getDdBankCity()); //约定还款开户行市
		contract.setDdBankCityCode(acct.getDdBankCityCode()); //约定还款开户行市code
		contract.setApplicationNo(acct.getApplicationNo());
		contract.setContraBizSituation(constractStatusUtil.getContraBizSituation(acct, acctO, loanlist, loanReglist));				
		List<STNQAAcctsbyCustUUIDRESPSubLoan>  contractSubloanList = new ArrayList<STNQAAcctsbyCustUUIDRESPSubLoan>();
		
		// 获取 loan_reg 与 loan 合并记录到loanlist
		for(CcsLoan loan : loanlist){
			if( !loan.getLoanStatus().equals(LoanStatus.T)&& !loan.getLoanStatus().equals(LoanStatus.F) ){
				haveActiveLoan= Indicator.Y;
			}
			
			//对于等额本息贷款退货情况 计算未入账息费
			if(loan.getLoanType()== LoanType.MCEI && loan.getLoanStatus()== LoanStatus.T){
				List<CcsRepaySchedule> scheduleList = queryCommService.getSchedulelistByLoan(loan.getLoanId());
				BigDecimal intAcru = BigDecimal.ZERO;
				BigDecimal svcFee = BigDecimal.ZERO; //服务费
				BigDecimal insuranceFee = BigDecimal.ZERO; //保费
				BigDecimal lifeInsuFee = BigDecimal.ZERO;  //未入账首先费
				BigDecimal prepayFee = BigDecimal.ZERO; //提前还款计划报费
				BigDecimal replaceSvcFee = BigDecimal.ZERO; //代收服务费
				BigDecimal installFee = BigDecimal.ZERO; //手续费
				for(CcsRepaySchedule schedule:scheduleList){
					if(loan.getCurrTerm().compareTo(schedule.getCurrTerm()) < 0){
						intAcru=intAcru.add(schedule.getLoanTermInt()==null?BigDecimal.ZERO:schedule.getLoanTermInt());
						svcFee=svcFee.add(schedule.getLoanTermFee()==null?BigDecimal.ZERO:schedule.getLoanTermFee());
						insuranceFee=insuranceFee.add(schedule.getLoanInsuranceAmt()==null?BigDecimal.ZERO:schedule.getLoanInsuranceAmt());
						lifeInsuFee=lifeInsuFee.add(schedule.getLoanLifeInsuAmt()==null?BigDecimal.ZERO:schedule.getLoanLifeInsuAmt());
						prepayFee=prepayFee.add(schedule.getLoanPrepayPkgAmt()==null?BigDecimal.ZERO:schedule.getLoanPrepayPkgAmt());
						replaceSvcFee=replaceSvcFee.add(schedule.getLoanReplaceSvcFee()==null?BigDecimal.ZERO:schedule.getLoanReplaceSvcFee());
					    installFee=installFee.add(schedule.getLoanSvcFee()==null?BigDecimal.ZERO:schedule.getLoanSvcFee());
					}
				}
				contract.setContraBal(contract.getContraBal()
						.add(intAcru)
						.add(lifeInsuFee)
						.add(svcFee)
						.add(installFee)
						.add(insuranceFee)
						.add(prepayFee)
						.add(replaceSvcFee));
			}
			
			//马上贷 白名单一期 都是只有一个loan 所以先这么写后续 单合同对应多借据需修改
//			contract.setContraBizSituation(constractStatusUtil.getContraBizSituation(acct, acctO, loan, null));
			STNQAAcctsbyCustUUIDRESPSubLoan contractsSubLoan = setSubLoan( acct, accto,contract, loan);
			contractSubloanList.add(contractsSubLoan);
		}
		//先检查loanreg，再检查loan--20151110
		for(CcsLoanReg loanReg : loanReglist){
			//跑批时loan 和 loanreg可能会重复 因此要去重
			Indicator isRepeatLoanReg = Indicator.N;
			for(CcsLoan loan: loanlist){
				if(StringUtils.equals(loan.getLogicCardNbr(),loanReg.getLogicCardNbr())
						&& loan.getRegisterDate().equals(loanReg.getRegisterDate())
						&& StringUtils.equals(loan.getRefNbr() ,loanReg.getRefNbr())
						&& loan.getLoanInitPrin().compareTo(loanReg.getLoanInitPrin()) == 0){
					isRepeatLoanReg = Indicator.Y;
				}
			}

			if( !loanReg.getLoanAction().equals(LoanAction.O)
					&& !loanReg.getLoanAction().equals(LoanAction.U)
					&& !loanReg.getLoanAction().equals(LoanAction.D)
					&& !loanReg.getLoanAction().equals(LoanAction.T)
					&& isRepeatLoanReg == Indicator.N
					&& ( loanReg.getLoanRegStatus().equals(LoanRegStatus.N) 
							||loanReg.getLoanRegStatus().equals(LoanRegStatus.A) 
							||loanReg.getLoanRegStatus().equals(LoanRegStatus.S)
							||loanReg.getLoanRegStatus().equals(LoanRegStatus.C)) ){
				haveActiveLoan= Indicator.Y;
				STNQAAcctsbyCustUUIDRESPSubLoan contractsSubLoan = setSubLoan(acct, loanReg, req);
				contractSubloanList.add(contractsSubLoan);
			}
		}

		
		//如果还没有loan loanreg生成 则默认账户仍为活跃状态
		if(loanReglist.size() == 0 && loanlist.size() == 0){
			haveActiveLoan= Indicator.Y;
		}
		//如果loan loanreg都为空 直接赋值	ContraBizSituation		
		if(contract.getContraBizSituation() == null){
			contract.setContraBizSituation(constractStatusUtil.getContraBizSituation(acct, acctO, null, null));			
		}

		contract.setLoanList(contractSubloanList);
		//判断账户是否有效 循环贷 根据 有效期与余额来判断
		//			      非循环贷根据 loan是否终止与余额来判断
		Indicator isContractActive = Indicator.Y;// y 有效 n 无效
		if(loanPlan.loanMold == LoanMold.C){
			if(acct.getAcctExpireDate().compareTo(req.getBizDate()) < 0 &&
				acct.getCurrBal().compareTo(BigDecimal.ZERO)==0){
				isContractActive = Indicator.N;
			}
		}
		else{
			if ( haveActiveLoan == Indicator.N 
			   && acct.getCurrBal().compareTo(BigDecimal.ZERO)==0){
				isContractActive = Indicator.N;
			}
		}
		
		Map<String,Object> returnMap = new HashMap<String, Object>();
		returnMap.put("subContract", contract);
		returnMap.put("isContractActive", isContractActive);

		return returnMap;
	}
	
	public STNQAAcctsbyCustUUIDRESPSubLoan setSubLoan(CcsAcct acct,CcsAcctO accto,STNQAAcctsbyCustUUIDRESPSubContract contract ,CcsLoan loan){
		STNQAAcctsbyCustUUIDRESPSubLoan subloan = new STNQAAcctsbyCustUUIDRESPSubLoan();
		
		subloan.setDueBillNo(loan.getDueBillNo()); //借据号
		subloan.setLoanCode(loan.getLoanCode()); //贷款产品代码
		subloan.setLoanType(loan.getLoanType());
		subloan.setLoanStatus(loan.getLoanStatus()); //借据状态
		subloan.setActiveDate(DateFormatUtil.format(loan.getActiveDate(), respDateFormat)); //激活日期
		subloan.setLoanInitTerm(loan.getLoanInitTerm()); //贷款总期数
		subloan.setLoanCurrTerm(loan.getCurrTerm()); //当前期数
		subloan.setLoanRemainTerm(loan.getRemainTerm()); //剩余期数
		subloan.setLoanInitPrin(loan.getLoanInitPrin()); //贷款总本金
		subloan.setLoanExpireDate(DateFormatUtil.format(loan.getLoanExpireDate(), respDateFormat)); //贷款到期日期
		subloan.setLoanOverdueDate(DateFormatUtil.format(loan.getOverdueDate(), respDateFormat)); //逾期起始日期
		if( constractStatusUtil.isContractOverDue( acct,  accto) == Indicator.Y){
			subloan.setOverdueAmt(loan.getLoanBalXfrin()); //逾期欠款总额 
		}else {
			subloan.setOverdueAmt(BigDecimal.ZERO);
		}
		//下一期期款（当期待还款金额） 取下个schedule 的总金额
		CcsRepaySchedule schedule ;
		CcsRepayScheduleHst scheduleHst ;
		QCcsRepaySchedule qSchedule = QCcsRepaySchedule.ccsRepaySchedule;
		QCcsRepayScheduleHst qScheduleHst = QCcsRepayScheduleHst.ccsRepayScheduleHst;
		schedule = new JPAQuery(em).from(qSchedule).where(qSchedule.loanId.eq(loan.getLoanId()).and(qSchedule.currTerm.eq(loan.getCurrTerm()+1))).singleResult(qSchedule);
		if(schedule != null){
			subloan.setNextTermAmt(BigDecimal.ZERO
					.add(schedule.getLoanInsuranceAmt()==null?BigDecimal.ZERO:schedule.getLoanInsuranceAmt())
					.add(schedule.getLoanLifeInsuAmt()==null?BigDecimal.ZERO:schedule.getLoanLifeInsuAmt())
					.add(schedule.getLoanPrepayPkgAmt()==null?BigDecimal.ZERO:schedule.getLoanPrepayPkgAmt())
					.add(schedule.getLoanStampdutyAmt()==null?BigDecimal.ZERO:schedule.getLoanStampdutyAmt())
					.add(schedule.getLoanTermFee()==null?BigDecimal.ZERO:schedule.getLoanTermFee())
					.add(schedule.getLoanTermInt()==null?BigDecimal.ZERO:schedule.getLoanTermInt())
					.add(schedule.getLoanTermPrin()==null?BigDecimal.ZERO:schedule.getLoanTermPrin())
					.add(schedule.getLoanReplaceSvcFee() == null ? BigDecimal.ZERO : schedule.getLoanReplaceSvcFee())
					.add(schedule.getLoanSvcFee()==null?BigDecimal.ZERO:schedule.getLoanSvcFee()));			
		}
		else{
			subloan.setNextTermAmt(BigDecimal.ZERO);
		}
		//等额本金跟等额本息才会进入下面方法
		if(LoanType.MCEP==loan.getLoanType() || LoanType.MCEI==loan.getLoanType()){
			//合同到期时，合同信息中当期应还款日期与下一应还款日期均为最后一个还款日
			if(loan.getCurrTerm() == loan.getLoanInitTerm()){
				schedule = new JPAQuery(em).from(qSchedule).where(qSchedule.loanId.eq(loan.getLoanId()).and(qSchedule.currTerm.eq(loan.getCurrTerm()))).singleResult(qSchedule);
				if(schedule==null){
					scheduleHst=new JPAQuery(em).from(qScheduleHst).where(qScheduleHst.loanId.eq(loan.getLoanId()).and(qScheduleHst.currTerm.eq(loan.getCurrTerm()))).singleResult(qScheduleHst);
					contract.setPmtDueDate(DateFormatUtil.format(scheduleHst.getLoanPmtDueDate(), respDateFormat));
					contract.setNextPmtDueDate(DateFormatUtil.format(scheduleHst.getLoanPmtDueDate(), respDateFormat));
				}else{
					contract.setPmtDueDate(DateFormatUtil.format(schedule.getLoanPmtDueDate(), respDateFormat));
					contract.setNextPmtDueDate(DateFormatUtil.format(schedule.getLoanPmtDueDate(), respDateFormat));
				}
			}
		}
		//把期数转化为BigDecimal,用于算月费率
		BigDecimal term=new BigDecimal(loan.getLoanInitTerm());
		
		//协议费率
		subloan.setAgreementRateInd(loan.getAgreementRateInd());
		//贷款服务费率
		subloan.setFeeRate(loan.getFeeRate()==null?null:loan.getFeeRate().divide(term, 8, RoundingMode.HALF_UP));
		//贷款服务费固定金额
		subloan.setFeeAmt(loan.getFeeAmt());
		//寿险费率
		subloan.setLifeInsuFeeRate(loan.getLifeInsuFeeRate()==null?null:loan.getLifeInsuFeeRate().divide(term, 8, RoundingMode.HALF_UP));
		//寿险固定金额
		subloan.setLifeInsuFeeAmt(loan.getLifeInsuFeeAmt());
		//保费月费率
		subloan.setInsuranceRate(loan.getInsuranceRate()==null?null:loan.getInsuranceRate().divide(term, 8, RoundingMode.HALF_UP));
		//保费月固定金额
		subloan.setInsAmt(loan.getInsAmt());
		//分期手续费率
		subloan.setInstallmentFeeRate(loan.getInstallmentFeeRate()==null?null:loan.getInstallmentFeeRate().divide(term, 8, RoundingMode.HALF_UP));
		//分期手续费固定金额
		subloan.setInstallmentFeeAmt(loan.getInstallmentFeeAmt());
		//提前还款包费率
		subloan.setPrepayPkgFeeRate(loan.getPrepayPkgFeeRate()== null? null:loan.getPrepayPkgFeeRate().divide(term, 8, RoundingMode.HALF_UP));
		//提前还款包固定金额
		subloan.setPrepayPkgFeeAmt(loan.getPrepayPkgFeeAmt());
		subloan.setPenaltyRate(loan.getPenaltyRate());
		subloan.setCompoundRate(loan.getCompoundRate());
		subloan.setInterestRate(loan.getInterestRate());
		subloan.setStampAmt(loan.getStampAmt());	
		subloan.setStampdutyRate(loan.getStampdutyRate());
		subloan.setAgentFeeRate(loan.getReplaceSvcFeeRate() == null?null:loan.getReplaceSvcFeeRate().divide(term,8,RoundingMode.HALF_UP));//代收服务费费率
		subloan.setAgentFeeMount(loan.getReplaceSvcFeeAmt());//代收服务费固定金额
		//趸交费金额
		subloan.setPremiumAmt(loan.getPremiumAmt());
		//是否代收趸交费
		subloan.setPremiumInd(loan.getPremiumInd());
		return subloan ;
	}
	
	public STNQAAcctsbyCustUUIDRESPSubLoan setSubLoan(CcsAcct acct,CcsLoanReg loanReg,STNQAAcctsbyCustUUIDReq req ){
		STNQAAcctsbyCustUUIDRESPSubLoan subloan = new STNQAAcctsbyCustUUIDRESPSubLoan();
		
		subloan.setDueBillNo(loanReg.getDueBillNo()); //借据号
		subloan.setLoanCode(loanReg.getLoanCode()); //贷款产品代码
		 if(loanReg.getLoanRegStatus().equals(LoanRegStatus.N) 
		  ||loanReg.getLoanRegStatus().equals(LoanRegStatus.C)){
			 subloan.setLoanStatus(LoanStatus.P); //借据状态			 
		 }
		 else{
			 subloan.setLoanStatus(LoanStatus.A); //借据状态
		 }
		subloan.setActiveDate(DateFormatUtil.format(req.getBizDate(), respDateFormat)); //激活日期
		subloan.setLoanInitTerm(loanReg.getLoanInitTerm()); //贷款总期数
		subloan.setLoanCurrTerm(0); //当前期数
		subloan.setLoanRemainTerm(loanReg.getLoanInitTerm()); //剩余期数
		subloan.setLoanInitPrin(loanReg.getLoanInitPrin()); //贷款总本金
		LoanFeeDef loanFeeDef;
		try{
			 if(StringUtils.isNotBlank(loanReg.getLoanFeeDefId())){
	    		loanFeeDef = unifiedParamFacilityProvide.loanFeeDefByKey(//20151127
	    				acct.getProductCd(), loanReg.getLoanType(), Integer.valueOf(loanReg.getLoanFeeDefId()));
	    	}else{
	    		loanFeeDef = unifiedParamFacilityProvide.loanFeeDef(loanReg.getLoanCode(), loanReg.getLoanInitTerm(), loanReg.getLoanInitPrin());
	    	}
		} catch (Exception e) {
			throw new ProcessException(MsRespCode.E_1052.getCode(), MsRespCode.E_1052.getMessage() + ",找不到贷款产品定价");
		}
		Date loanExpireDate = rescheduleUtils.getLoanPmtDueDate(acct.getNextStmtDate(), loanFeeDef, loanReg.getLoanInitTerm());
		subloan.setLoanExpireDate(DateFormatUtil.format(loanExpireDate, respDateFormat)); //贷款到期日期
		subloan.setLoanOverdueDate(null); //逾期起始日期
		subloan.setOverdueAmt(BigDecimal.ZERO); //逾期欠款总额 
		subloan.setNextTermAmt(BigDecimal.ZERO); //当期待还款金额 
		
		//把期数转化为BigDecimal,用于算月费率
		BigDecimal term=new BigDecimal(loanReg.getLoanInitTerm());
		
		//协议费率
		subloan.setAgreementRateInd(loanReg.getAgreementRateInd());
		//贷款服务费率
		subloan.setFeeRate(loanReg.getFeeRate()==null?null:loanReg.getFeeRate().divide(term, 8, RoundingMode.HALF_UP));
		//贷款服务费固定金额
		subloan.setFeeAmt(loanReg.getFeeAmt());
		//寿险费率
		subloan.setLifeInsuFeeRate(loanReg.getLifeInsuFeeRate()==null?null:loanReg.getLifeInsuFeeRate().divide(term, 8, RoundingMode.HALF_UP));
		//寿险固定金额
		subloan.setLifeInsuFeeAmt(loanReg.getLifeInsuFeeAmt());
		//保费月费率
		subloan.setInsuranceRate(loanReg.getInsuranceRate()==null?null:loanReg.getInsuranceRate().divide(term, 8, RoundingMode.HALF_UP));
		//保费月固定金额
		subloan.setInsAmt(loanReg.getInsAmt());
		//分期手续费率
		subloan.setInstallmentFeeRate(loanReg.getInstallmentFeeRate()==null?null:loanReg.getInstallmentFeeRate().divide(term, 8, RoundingMode.HALF_UP));
		//分期手续费固定金额
		subloan.setInstallmentFeeAmt(loanReg.getInstallmentFeeAmt());
		//提前还款包费率
		subloan.setPrepayPkgFeeRate(loanReg.getPrepayPkgFeeRate()== null? null:loanReg.getPrepayPkgFeeRate().divide(term, 8, RoundingMode.HALF_UP));
		//提前还款包固定金额
		subloan.setPrepayPkgFeeAmt(loanReg.getPrepayPkgFeeAmt());
		subloan.setPenaltyRate(loanReg.getPenaltyRate());
		subloan.setCompoundRate(loanReg.getCompoundRate());
		subloan.setInterestRate(loanReg.getInterestRate());
		subloan.setStampAmt(loanReg.getStampAmt());	
		subloan.setStampdutyRate(loanReg.getStampdutyRate());
		subloan.setAgentFeeRate(loanReg.getReplaceSvcFeeRate() == null?null:loanReg.getReplaceSvcFeeRate().divide(term,8,RoundingMode.HALF_UP));//代收服务费费率
		subloan.setAgentFeeMount(loanReg.getReplaceSvcFeeAmt());//代收服务费固定金额
		//趸交费金额
		subloan.setPremiumAmt(loanReg.getPremiumAmt());
		//是否代收趸交费
		subloan.setPremiumInd(loanReg.getPremiumInd());
	
		return subloan ;
	}
	
	

	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(STNQAAcctsbyCustUUIDResp resp, TxnInfo txnInfo) {
		
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
}

