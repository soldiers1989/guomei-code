package com.sunline.ccs.service.handler.query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.loan.McLoanProvideImpl;
import com.sunline.ccs.loan.TrialResp;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ccs.service.msentity.TNTAdvanceRepayReq;
import com.sunline.ccs.service.msentity.TNTAdvanceRepayResp;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 兜底 提前还款试算/申请
 * @author zhengjf
 *
 */
@Service
public class TNTAdvanceRepay {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SunshineCommService sunshineCommService;
	@Autowired
	private TxnUtils txnUtils;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
	@Autowired
	private McLoanProvideImpl loanProvideImpl;
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	
	//预约类型 1-试算  2-申请
	private static final String BOOKING_TYPE_1 ="1";
	
	private static final String BOOKING_TYPE_2 ="2";
	
	public TNTAdvanceRepayResp handler(TNTAdvanceRepayReq req) throws ProcessException {
		LogTools.printLogger(log, "TNTAdvanceRepayReq", "兜底提前还款试算/预约", req, true);
		LogTools.printObj(log, req, "请求参数TNTAdvanceRepayReq");
		if(log.isDebugEnabled())
			log.debug("[TNTAdvanceRepay]:借据号[{}],操作类型[{}],预约还款日期[{}]",req.getDueBillNo(),req.getType(),req.getCalDate());
		
		TNTAdvanceRepayResp resp = new TNTAdvanceRepayResp();
		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		TrialResp trialResp = null;
		
		context.setTxnInfo(txnInfo);
//		context.setSunshineRequestInfo(req);
		
		try{
			this.initTxnInfo(context,req);
			sunshineCommService.loadLoan(context, true);		
			sunshineCommService.loadLoanReg(context,null,LoanRegStatus.A,LoanAction.O, false);
			sunshineCommService.loadAcct(context, true);
			sunshineCommService.loadAcctO(context, true);
			
			//检查贷款
			sunshineCommService.checkLoan(context);
			
			//检查业务逻辑
			this.check(context);
			
			//试算预约结清金额
			trialResp = this.computBookingAmt(context);
			
			//预约结清申请
			this.bookingApply(context, trialResp);
			
		}catch (ProcessException pe){
			if(log.isErrorEnabled())
				log.error(pe.getMessage(), pe);
			
			this.sunshineCommService.preException(pe, pe, txnInfo);
		}catch (Exception e) {
			if(log.isErrorEnabled())
				log.error(e.getMessage(), e);
			ProcessException pe = new ProcessException();
			
			this.sunshineCommService.preException(pe, pe, txnInfo);
		}finally{
			LogTools.printLogger(log, "TNTAdvanceRepayResp", "提前还款试算/预约", resp, false);
		}
		
		this.setResponse(resp,req,txnInfo, trialResp);
		LogTools.printObj(log, resp, "响应报文TNTAdvanceRepayResp");
		
		return resp;
	}
	
	/**
	 * 初始化交易中间信息
	 * @param context
	 * @param req
	 */
	public void initTxnInfo(TxnContext context,TNTAdvanceRepayReq req){
		TxnInfo txnInfo = context.getTxnInfo();
		txnInfo.setBizDate(req.getBizDate());
		txnInfo.setDueBillNo(req.getDueBillNo());
		txnInfo.setSysOlTime(new Date());
		txnInfo.setRequestTime(req.getRequestTime());
		txnInfo.setServiceSn(req.getServiceSn());
		txnInfo.setRefNbr(txnUtils.genRefnbr(req.getBizDate(), req.getServiceSn()));
		txnInfo.setBookingDate(req.getCalDate());//预约日期
		txnInfo.setBookingType(req.getType());//预约类型 1-试算 2-申请
		txnInfo.setInputSource(req.getInputSource());
		txnInfo.setAcqId(req.getAcqId());
		txnInfo.setSubTerminalType(req.getSubTerminalType());
//		txnInfo.setDueBillNo(req.getDueBillNo());
		
		if(log.isDebugEnabled())
			log.debug("业务日期：[{}]",txnInfo.getBizDate());
		LogTools.printObj(log, txnInfo, "交易中间信息txnInfo");
		
		if(!StringUtils.equals(BOOKING_TYPE_1, req.getType()) 
				&& !StringUtils.equals(BOOKING_TYPE_2, req.getType())){
			throw new ProcessException(MsRespCode.E_1043.getCode(),MsRespCode.E_1043 .getMessage()+ ",字段名称{" +req.getType()+ "} 1-试算  2-申请");
		}
	}
	
	/**
	 * 检查业务逻辑
	 * @param context
	 */
	private void check(TxnContext context) {
		if(log.isDebugEnabled())
			log.debug("检查业务逻辑--check");
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcctO accounto = context.getAccounto();
		CcsAcct ccsAcct =context.getAccount();
		CcsLoan loan = context.getLoan();
		LoanFeeDef lfd = unifiedParamFacilityProvide.loanFeeDef(ccsAcct.getProductCd(), LoanType.MCEI, Integer.valueOf(loan.getLoanInitTerm()));
		
		LogTools.printObj(log, accounto.getBlockCode(), "账户锁定码");
		// 账户锁定码包含"P"
		if (StringUtils.isNotEmpty(accounto.getBlockCode()) && accounto.getBlockCode().toUpperCase().indexOf("P") > -1) {
			throw new ProcessException(MsRespCode.E_1014.getCode(), MsRespCode.E_1014.getMessage());
		}
		//该产品不支持提前还款
		if(!Indicator.Y.equals(lfd.appointEarlySettleEnable)){
			throw new ProcessException(MsRespCode.E_1069.getCode(), MsRespCode.E_1069.getMessage());
		}
		if(log.isDebugEnabled())
			log.debug("当前日期为[{}]，预约还款日[{}],预约提前结清提前天数[{}]",txnInfo.getBizDate(),txnInfo.getBookingDate(),lfd.appointEarlySettleDate.intValue());
        //预约还款日>今天
		if(DateUtils.getIntervalDays(txnInfo.getBizDate(),txnInfo.getBookingDate())<1){
			 throw new ProcessException(MsRespCode.E_1021.getCode(), MsRespCode.E_1021.getMessage().replace("N",1+""));
		}
		//申请日须在账单日前N天 
		if(DateUtils.getIntervalDays(txnInfo.getBizDate(),ccsAcct.getNextStmtDate())<lfd.appointEarlySettleDate.intValue()){
			throw new ProcessException(MsRespCode.E_1067.getCode(), MsRespCode.E_1067.getMessage().replace("N",lfd.appointEarlySettleDate.intValue()+""));
		}
		//判断是否逾期
		if(log.isDebugEnabled())
			log.debug("逾期开始日期为[{}]",loan.getOverdueDate());
		if(null != loan.getOverdueDate()){
			throw new ProcessException(MsRespCode.E_1025.getCode(), MsRespCode.E_1025.getMessage());
		}
		//预约还款日<还款日
		if(log.isDebugEnabled())
			log.debug("下一账单日期[{}]",ccsAcct.getNextStmtDate());
		if(DateUtils.getIntervalDays(txnInfo.getBookingDate(),ccsAcct.getNextStmtDate()) <= 0){
			 throw new ProcessException(MsRespCode.E_1022.getCode(),MsRespCode.E_1022.getMessage()+ccsAcct.getNextStmtDate());
		}
	}
	
	/**
	 * 计算预约结清金额
	 * @param context
	 * @return
	 */
	private TrialResp computBookingAmt(TxnContext context) {
		if(log.isDebugEnabled())
			log.debug("计算预约结清金额--computBookingAmt");
		TxnInfo txnInfo = context.getTxnInfo();
		
		//查询信用计划列表
		List<CcsPlan> ccsPlans = sunshineCommService.loadPlans(context, true);
		//调用试算方法得到试算金额
		TrialResp trialResp = new TrialResp();
		try {
			trialResp=loanProvideImpl.mCLoanTodaySettlement(context.getLoan(),txnInfo.getBizDate(), txnInfo.getBookingDate(), LoanUsage.M, trialResp,ccsPlans,null);
		} catch (Exception e) {
			if(log.isErrorEnabled())
				log.error(e.getMessage(), e);
			throw new ProcessException(MsRespCode.E_1026.getCode(), MsRespCode.E_1026.getMessage());
		}
		
		LogTools.printObj(log, trialResp, "试算金额trialResp");
		//提前还款金额不能<=0
		if(trialResp.getTotalAMT().compareTo(BigDecimal.ZERO)<=0){
			throw new ProcessException(MsRespCode.E_1026.getCode(), MsRespCode.E_1026.getMessage());
		}
		return trialResp;
	}
	
	/**
	 * 预约结清申请处理
	 * @param context
	 * @param trialResp
	 */
	@Transactional
	private void bookingApply(TxnContext context,TrialResp trialResp) {
		if(log.isDebugEnabled())
			log.debug("预约结清申请处理--bookingApply");
		TxnInfo txnInfo = context.getTxnInfo();
		CcsLoan loan = context.getLoan();
		CcsLoanReg ccsLoanReg = context.getLoanReg();
		
		if(BOOKING_TYPE_2.equals(txnInfo.getBookingType())){
			//查询贷款信息注册表  有则更新 无则新建
			if(ccsLoanReg==null){
				try {
					//初始化预约注册信息
					ccsLoanReg=sunshineCommService.genLoanReg(0,BigDecimal.ZERO, 
							loan.getRefNbr(), loan.getCardNbr(), 
							loan.getCardNbr(), loan.getAcctNbr(), loan.getAcctType(),
							null,txnInfo.getBizDate());
					ccsLoanReg.setPreAdDate(txnInfo.getBookingDate());
					ccsLoanReg.setPreAdAmt(trialResp.getTotalAMT());
					//赋值账户信息	
					setCcsLoanReg(ccsLoanReg,context.getLoan());
					//保存信息
//					rCcsLoanReg.save(ccsloanReg);
				} catch (ProcessException e) {
					throw new ProcessException(MsRespCode.E_1044.getCode(), MsRespCode.E_1044.getMessage());
				}
			}else{
				ccsLoanReg.setRegisterDate(txnInfo.getBizDate());
				ccsLoanReg.setPreAdDate(txnInfo.getBookingDate());
				ccsLoanReg.setPreAdAmt(trialResp.getTotalAMT());
			}
			context.setLoanReg(ccsLoanReg);
			rCcsLoanReg.save(ccsLoanReg);
		}
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TNTAdvanceRepayResp resp, TNTAdvanceRepayReq req, TxnInfo txnInfo,TrialResp trialResp) {
		
		if(null != trialResp){
			resp.setAmount(String.valueOf(trialResp.getTotalAMT()));//总金额 totalAMT
			resp.setInterest(String.valueOf(trialResp.getCtdInterestAMT()));//利息 CtdInterestAMT
			resp.setPoundage(String.valueOf(trialResp.getCtdInitFee()));//手续费 initFee
			resp.setPremium(String.valueOf(trialResp.getCtdInsuranceAMT()));//保费 insuranceAMT
			resp.setPrincipal(String.valueOf(trialResp.getCtdPricinpalAMT()));//本金  CrdPricinpalAMT
		}
		resp.setDueBillNo(req.getDueBillNo());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
	
	/**
	 * 设置预约注册信息
	 * @param ccsloanReg
	 * @param loan
	 */
	public void setCcsLoanReg(CcsLoanReg ccsloanReg,CcsLoan loan){
		ccsloanReg.setOrg(loan.getOrg());
		ccsloanReg.setAcctNbr(loan.getAcctNbr());
		ccsloanReg.setAcctType(loan.getAcctType());
		ccsloanReg.setRequestTime(new Date());
		ccsloanReg.setLogicCardNbr(loan.getLogicCardNbr());//LoanRegStatus
		ccsloanReg.setCardNbr(loan.getCardNbr());
		ccsloanReg.setLoanRegStatus(LoanRegStatus.A);
		ccsloanReg.setLoanAction(LoanAction.O);//GUARANTY_ID
		ccsloanReg.setDueBillNo(loan.getDueBillNo());
		ccsloanReg.setContrNbr(loan.getContrNbr());
		ccsloanReg.setGuarantyId(loan.getGuarantyId());		
		ccsloanReg.setLoanCode(loan.getLoanCode());
	}
}
