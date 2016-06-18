package com.sunline.ccs.service.handler;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.YakMessage;
import com.sunline.ccs.facility.AuthCommUtils;
import com.sunline.ccs.facility.AuthCommUtils.MsgParameter;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.facility.CashLendingFacility;
import com.sunline.ccs.facility.CashLendingFacility.CashLendingMsg;
import com.sunline.ccs.facility.MsgUtils;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoanReg;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.LoanLendWay;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CashLoanLimitType;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S13084Req;
import com.sunline.ccs.service.protocol.S13084Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.api.AuthorizationService;
import com.sunline.ppy.api.BankClientService;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanAction;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ppy.dictionary.enums.LoanRegStatus;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/** 
 * @see 类名：CashLoanService
 * @see 描述：现金分期服务类
 *
 * @see 创建日期：   2015年6月24日 下午3:03:08
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class CashLoanService  {
	
	private static final String CALC_OPT = "0";
	
	private static final String ALLOW_OPT="01";
	
	private static final String CASH_CODE = "010000";
	
	private static final String CONSUME_CODE = "000000";
	
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	@Autowired
	private RCcsLoanReg rCcsLoanReg;
	
	@PersistenceContext
	private EntityManager em;
	
	@Resource(name="authorizationService")
	private AuthorizationService authorizationService;
	
	@Resource(name="bankClientService")
	private BankClientService bankClientService;
	
	@Autowired
	private AcctOTB accountOTB;
	
	@Resource(name="authCodeMap")
	private Map<String, String> authCodeMap;
	
	@Autowired
	private CashLendingFacility cashLoanLending;
	
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	
	/**
	 * 现金分期申请
	 * @param request
	 * @return
	 * @throws ProcessException
	 */
	@Transactional
	public String loan(S13084Req request, CcsCard card, CcsAcct acct, CcsLoanReg loanReg) throws ProcessException {
		String lendingReturn = CashLendingFacility.RPC_SUCCESS;
		if(!isCalcOnly(request)){
			YakMessage message = auth(loanReg, unifiedParameterFacilityProvide.productCredit(card.getProductCd()));
			
			loanReg.setLoanAction(LoanAction.A);
			loanReg.setOrigAuthCode(message.getBody(38));
			loanReg.setB007TxnTime(message.getBody(7));
			loanReg.setB011Trace(message.getBody(11));
			loanReg.setB032AcqInst(message.getBody(32));
			loanReg.setB033FwdIns(message.getBody(33));
			Organization cpsOrg = unifiedParameterFacilityProvide.organization();
			if (cpsOrg.cashLoanNeedAudit) {
				loanReg.setLoanRegStatus(LoanRegStatus.N);
				rCcsLoanReg.save(loanReg);
			} else {
				loanReg.setLoanRegStatus(LoanRegStatus.A);
				rCcsLoanReg.save(loanReg);
				//如果该机构为实时放款则在这里放款，以上先持久化，避免在放款被回滚
				if (LoanLendWay.O.equals(cpsOrg.cashLoanSendMode)) {
					
					CashLendingMsg returnMsg = cashLoanLending.lending(bankClientService, authorizationService, loanReg, acct, card.getProductCd(), "");
/*					downMsgFacility.sendMessage(cashLoanLending.getMsgCd(card.getProductCd(), loanReg, returnMsg.isSuccess()),
												loanReg.getCardNbr(),
												acct.getName(),
												acct.getGender(),
												acct.getMobileNo(),
												new Date(),
												cashLoanLending.getSendMessage(loanReg, returnMsg.isSuccess()));
*/					if (StringUtils.isNotBlank(returnMsg.getB039()) && null != authCodeMap.get(returnMsg.getB039())) {
						lendingReturn = new StringBuffer(authCodeMap.get(returnMsg.getB039())).append(returnMsg.getErrorMsg())
												.append("。【错误码：").append(returnMsg.getB039()).append("】")
												.toString();
								loanReg.setRemark(MsgUtils.substring(lendingReturn, 40));
					}
				}
			}
		}
		
		return lendingReturn;
	}
	
	/**
	 * 产生唯一流水号
	 * @return
	 */
	public String generateFlowNo(){
		DateFormat df = new SimpleDateFormat("yyDS");
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		StringBuffer sb = new StringBuffer(df.format(c.getTime()));
		sb.append(hour*60*60+minute*60+second);
		return sb.substring(1);
	}
	/**
	 * 现金分期撤销
	 * @param productCredit 
	 * @param request
	 * @throws ProcessException
	 */
	public void reverse(CcsLoanReg reg, ProductCredit productCredit)throws ProcessException{
		if(reg.getLoanAction() != LoanAction.A){
			throw new ProcessException(Constants.ERRL042_CODE,Constants.ERRL042_MES);
		}
		if(reg.getLoanRegStatus() != LoanRegStatus.N){
			throw new ProcessException(Constants.ERRL036_CODE,Constants.ERRL036_MES);
		}
		String mcc = "5999"; 
		
		String b003 = getTransType(productCredit);
		MsgParameter m = makeMsgParam(reg, mcc, b003,generateFlowNo());
		m.setB038(reg.getOrigAuthCode());
		m.setB090(AuthCommUtils.makeFeild90(AuthCommUtils.TPS_VAL_MTI_ADJUST, reg.getB011Trace(), reg.getB007TxnTime(), reg.getB032AcqInst(), reg.getB033FwdIns()));
		YakMessage request = AuthCommUtils.makeCashLoanReverseRequestMsg(m);
		
		YakMessage response = authorizationService.authorize(request);
		if(response == null){
			throw new ProcessException(Constants.ERRL033_CODE, Constants.ERRL033_MES);
		}
		String respCode = response.getBody(39);
		if(!StringUtils.equals(respCode,"00") && !StringUtils.equals(respCode, "11")){
			throw new ProcessException(Constants.ERRL034_CODE, Constants.ERRL034_MES+authCodeMap.get(respCode));
		}
	}
	/**
	 * 调用授权处理，调整账户状态
	 * @param reg
	 * @param CcsCard 
	 * @return
	 * @throws ProcessException
	 */
	public YakMessage auth(CcsLoanReg reg, ProductCredit productCredit)throws ProcessException {
		String mcc = "5999"; 
		
		String b003 = getTransType(productCredit);
		MsgParameter m = makeMsgParam(reg, mcc, b003,reg.getRefNbr());
		//组装金融交易请求报文
		YakMessage request = AuthCommUtils.makeCashLoanRequestMsg(m);
		
		YakMessage response = authorizationService.authorize(request);
		if(response == null){
			throw new ProcessException(Constants.ERRL033_CODE, Constants.ERRL033_MES);
		}
		String respCode = response.getBody(39);
		if(!StringUtils.equals(respCode,"00") && !StringUtils.equals(respCode, "11")){
			throw new ProcessException(Constants.ERRL034_CODE, Constants.ERRL034_MES + authCodeMap.get(respCode));
		}
		return response;
	}

	/**
	 * 根据使用额度类型获得交易类型
	 * @param CcsCard 
	 * @return
	 * @throws ProcessException
	 */
	private String getTransType(ProductCredit productCredit) throws ProcessException {
		if(productCredit == null)
			throw new ProcessException(Constants.ERRS001_CODE, Constants.ERRS001_MES); 
		String transType = CASH_CODE;//取现 
		if(productCredit.cashLoanLimitType == CashLoanLimitType.L){
			transType=CONSUME_CODE;//消费
		}
		return transType;
	}

	/**
	 * 生成yakmessage参数对象
	 * @param reg
	 * @param mcc
	 * @param b003
	 * @return
	 */
	private MsgParameter makeMsgParam(CcsLoanReg reg, String mcc, String b003,String b037) {
		MsgParameter m = new MsgParameter();
		m.setB003(b003);
		if(CONSUME_CODE.equals(b003)){
			m.setB025("00");
		}else{
			m.setB025("64");
		}
		m.setBusiDate(unifiedParameterFacilityProvide.BusinessDate());
		m.setCardNo(reg.getCardNbr());
		m.setCurrCd(reg.getAcctType().getCurrencyCode());
		m.setMcc(mcc);
		m.setTxnAmt(getAuthAmt(reg.getLoanInitPrin()));
		m.setB037(b037);
		return m;
	}
	
	/**
	 * 生成授权要用的金额值*100取整
	 * @param b
	 * @return
	 */
	private static String getAuthAmt(BigDecimal b){
		DecimalFormat df = new DecimalFormat("#0");
		return df.format(b.doubleValue()*100);
	}
	/**
	 * 生成返回报文
	 * @param request
	 * @param acct
	 * @param reg
	 * @return
	 */
	public S13084Resp makeResponse(CcsAcct acct,CcsLoanReg reg) {
		S13084Resp resp = new S13084Resp();
		resp.card_no = reg.getCardNbr();
		resp.curr_cd = reg.getAcctType().getCurrencyCode();
		resp.register_id = Integer.valueOf(reg.getRegisterId().toString());
		resp.dd_bank_acct_name = acct.getDdBankAcctName();
		resp.dd_bank_acct_no = acct.getDdBankAcctNbr();
		resp.dd_bank_branch = acct.getDdBankBranch();
		resp.dd_bank_name = acct.getDdBankName();
		resp.dd_ind = acct.getDdInd();
		resp.loan_fee_method = reg.getLoanFeeMethod();
		resp.loan_final_term_fee1 = reg.getLoanFinalTermFee();
		resp.loan_final_term_prin = reg.getLoanFinalTermPrin();
		resp.loan_first_term_fee1 = reg.getLoanFirstTermFee();
		resp.loan_first_term_prin = reg.getLoanFirstTermPrin();
		resp.loan_fixed_fee1 = reg.getLoanFixedFee();
		resp.loan_fixed_pmt_prin = reg.getLoanFixedPmtPrin();
		resp.loan_init_fee1 = reg.getLoanInitFee();
		resp.loan_init_prin = reg.getLoanInitPrin();
		resp.loan_init_term = reg.getLoanInitTerm();
		return resp;
	}

	/**
	 * @param request
	 */
	public void validateForApply(S13084Req request,CcsCard card,CcsAcct acct,LoanPlan loanPlan,LoanFeeDef loanFeeDef) throws ProcessException{
		// 判断账户上是否有锁定码存在，存在则无法分期
		if (!blockCodeUtils.isAllowedCashLoan(acct.getBlockCode())) {
			throw new ProcessException(Constants.ERRL008_CODE, Constants.ERRL008_MES);
		}

		// 判断卡片上是否有锁定码存在，存在则无法分期
		if (!blockCodeUtils.isAllowedCashLoan(card.getBlockCode())) {
			throw new ProcessException(Constants.ERRL008_CODE, Constants.ERRL008_MES);
		}
		//判断已有现金分期笔数是否超限
		QCcsLoanReg qCcsLoanReg = QCcsLoanReg.ccsLoanReg;
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		JPAQuery query = new JPAQuery(em);
		JPAQuery queryLoan = new JPAQuery(em);
		long cnt = 0;
		cnt+=query.from(qCcsLoanReg).where(qCcsLoanReg.cardNbr.eq(request.card_no).
				and(qCcsLoanReg.org.eq(OrganizationContextHolder.getCurrentOrg())).
				and(qCcsLoanReg.loanType.eq(LoanType.C)).
				and(qCcsLoanReg.loanAction.in(LoanAction.A,LoanAction.R)).
				and(qCcsLoanReg.loanRegStatus.in(LoanRegStatus.N,LoanRegStatus.A))).count();
		cnt+=queryLoan.from(qCcsLoan).where(qCcsLoan.cardNbr.eq(request.card_no).
				and(qCcsLoan.org.eq(OrganizationContextHolder.getCurrentOrg())).
				and(qCcsLoan.loanType.eq(LoanType.C)).
				and(qCcsLoan.loanStatus.notIn(LoanStatus.T,LoanStatus.F))).count();
		ProductCredit productCredit = unifiedParameterFacilityProvide.productCredit(card.getProductCd());
		if(productCredit.multiCashLoanInd == Indicator.Y){
			if(productCredit.maxCashLoanCnt-cnt<=0){
				throw new ProcessException(Constants.ERRL037_CODE,Constants.ERRL037_MES);
			}
		}else{
			if(cnt>0){
				throw new ProcessException(Constants.ERRL038_CODE,Constants.ERRL038_MES);
			}
		}
		if(request.getCash_amt().compareTo(loanFeeDef.maxAmount)>0 ){
			throw new ProcessException(Constants.ERRL018_CODE,Constants.ERRL018_MES);
		}
		if(request.getCash_amt().compareTo(loanFeeDef.minAmount)<0){
			throw new ProcessException(Constants.ERRL010_CODE,Constants.ERRL010_MES);
		}
		if(loanPlan.loanValidity.before(unifiedParameterFacilityProvide.BusinessDate())){
			throw new ProcessException(Constants.ERRL044_CODE, Constants.ERRL044_MES);
		}
		if(loanPlan.loanStaus != com.sunline.ccs.param.def.enums.LoanPlanStatus.A){
			throw new ProcessException(Constants.ERRL045_CODE,Constants.ERRL045_MES);
		}
	}

	/**
	 * 判断操作类型是否为试算
	 * @param request
	 * @return
	 */
	public boolean isCalcOnly(S13084Req request) {
		return StringUtils.equals(CALC_OPT, request.opt);
	}

	/**
	 * 对输入项和卡片信息做检查
	 * @param request
	 * @throws ProcessException
	 */
	public void validateInput(S13084Req request) throws ProcessException{
		CheckUtil.checkCardNo(request.card_no);
		CheckUtil.checkCurrCd(request.curr_cd, false);
		CheckUtil.rejectNull(request.loan_init_term, Constants.ERRL003_CODE, Constants.ERRL003_MES);
		CheckUtil.rejectNull(request.getCash_amt(), Constants.ERRL013_CODE, Constants.ERRL013_MES);
		if(!StringUtils.contains(ALLOW_OPT, request.getOpt())){
			throw new ProcessException(Constants.ERRS004_CODE,Constants.ERRS004_MES);
		}
	}
	
	/**
	 * 对系统信息进行检查
	 * @param card
	 * @param acct
	 * @param loanPlan
	 * @param loanFeeDef
	 * @throws ProcessException
	 */
	public void validateSysInfo(S13084Req req, CcsCard card,CcsAcct acct, LoanPlan loanPlan, LoanFeeDef loanFeeDef)throws ProcessException{
		CheckUtil.rejectNull(card, Constants.ERRS001_CODE, Constants.ERRS001_CODE);
		if (unifiedParameterFacilityProvide.BusinessDate().compareTo(card.getCardExpireDate()) > 0) {
			throw new ProcessException(Constants.ERRL011_CODE, Constants.ERRL011_MES);
		}
		CheckUtil.rejectNull(acct, Constants.ERRS001_CODE, Constants.ERRS001_CODE);
		if(loanPlan == null || loanFeeDef == null){
			throw new ProcessException(Constants.ERRS001_CODE,Constants.ERRS001_MES);
		}
		if(loanFeeDef.loanFeeMethod == LoanFeeMethod.C && req.loan_fee_method==null){
			throw new ProcessException(Constants.ERRL035_CODE,Constants.ERRL035_MES);
		}
		if(acct.getDdBankAcctNbr()==null || acct.getDdInd() == DdIndicator.N){
			throw new ProcessException(Constants.ERRL043_CODE,Constants.ERRL043_MES);
		}
		BigDecimal cashLoanOTB = accountOTB.acctCashLoanOTB(req.card_no, acct.getAcctType(), unifiedParameterFacilityProvide.BusinessDate());
		
		if(req.cash_amt.compareTo(cashLoanOTB)>0){
			throw new ProcessException(Constants.ERRL041_CODE, Constants.ERRL041_MES);
		}
		
	}

	
}
