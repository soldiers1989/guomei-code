package com.sunline.ccs.service.auth.common;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.loan.AuthLoanProvideImpl;
import com.sunline.ccs.otb.AcctOTB;
import com.sunline.ccs.otb.CustOTB;
import com.sunline.ccs.param.def.LoanMerchant;
import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.Program;
import com.sunline.ccs.param.def.ProgramFeeDef;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.service.auth.context.AuthContext;
import com.sunline.ccs.service.auth.context.CupMsg;
import com.sunline.ccs.service.auth.context.LoanInfo;
import com.sunline.ccs.service.auth.context.TxnInfo;
import com.sunline.ccs.service.auth.frame.AuthException;
import com.sunline.ccs.service.auth.frame.HandlerCommService;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;

/**
 * 
 * @see 类名：AuthLoanService
 * @see 描述：分期公共业务处理
 *
 * @see 创建日期：   2015年6月24日下午3:13:42
 * @author liruilin
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class AuthLoanService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private HandlerCommService pcs;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private AuthCommService authCommonService;
	@Autowired
	private CustOTB customerOTB;
	@Autowired
	private UnifiedParamFacilityProvide unifieldParServer;
	@Autowired
	private AcctOTB accountOTB;
	@Autowired
	private CustAcctCardFacility custAcctCardFacility;
	
	/**
	 * [分期第一步]获取分期信息
	 * 
	 * @param message
	 * @return
	 * @throws AuthException
	 */
	public void loanProcessor(AuthContext context) throws AuthException {
		TxnInfo txnInfo = context.getTxnInfo();
		CcsAcctO account = context.getAccount();
		CupMsg message = context.getMessage();
		ProductCredit productCredit = context.getProductCredit();
		if (txnInfo.getTransType() == AuthTransType.Loan && txnInfo.getTransDirection() == AuthTransDirection.Normal) {
			logger.debug("# [分期第一步]获取分期信息 #");
			// 获取分期信息
			LoanInfo loan = message.getLoanforMessage();
			if (loan == null) {
				// TODO reason暂时没有
				authCommonService.throwAuthException(AuthReason.TF02, "报文上送的分期数据获取失败");
			}
			// TODO 获取分期项目 , 获取发卡分行列表 , 获取卡产品列表
			Program program = parameterFacility.loadParameter(loan.getProgramId(), Program.class);
			loan.setLoanType(program.loanType);
			loan.setProgram(program);
			// 分期期数判断
			ProgramFeeDef programFeeDef = program.programFeeDef.get(loan.getLoanInitTerm());
			if (programFeeDef == null) {
				// TODO reason暂时没有
				authCommonService.throwAuthException(AuthReason.TL51, "报文上送的分期期数不在项目活动分期期数列表中");
			} else {
				loan.setProgramFeeDef(programFeeDef);
			}
			// 获取商户信息
			loan.setLoanMerchant(parameterFacility.loadParameter(loan.getMerchantId(), LoanMerchant.class));
			// 手续费收取方式：商户不允许自定义收取方式时，使用商户覆盖报文中上送的收取方式
			if (programFeeDef.loanFeeMethod != LoanFeeMethod.C) {
				loan.setLoanFeeMethod(programFeeDef.loanFeeMethod);
			}

			/**
			 * 在申请pos分期的时候，根据分期产品参数定义 “临时额度是否参与分期” 如果为true ,则使用上送的金额
			 * 如果为false,需要将分期的金额与永久额度做比较 如果分期的金额 >永久金额，则在规则文件中拒绝此交易
			 */
			BigDecimal loanOtb = BigDecimal.ZERO;
			// 获取分期计划
			LoanPlan loanPlan = parameterFacility.loadParameter(program.loanPlanId, LoanPlan.class);
			if (loanPlan.useTemplimit != null && loanPlan.useTemplimit) {
				// 分期otb，包含临额
				loanOtb = loanOTB(account, productCredit, txnInfo.getBizDate(), true);
				logger.debug("# 当前分期产品参数useTemplimit(临时额度是否参与分期)== true,分期额度包含临时额度");
			} else {
				// 调整分期otb，不包含临额
				loanOtb = loanOTB(account, productCredit, txnInfo.getBizDate(), false);
				logger.debug("# 当前分期产品参数useTemplimit(临时额度是否参与分期)== false,分期额度不包含临时额度");
			}
			// 将loanPlan存在LoanInfo中
			loan.setLoanPlan(loanPlan);
			// 账户分期可用额度
			txnInfo.setLoanOTB(loanOtb);
			logger.debug("# LoanOTB == " + txnInfo.getLoanOTB());

			// 返回分期信息，程序后面执行规则校验
			context.setLoanInfo(loan);
			/**
			 * 获得分期注册表信息、暂存在context中
			 */
			buildLoanReg(context);
		}
	}

	/**
	 * 获取分期额度
	 * 
	 * @param accountO
	 * @param productCredit
	 * @param bizDate
	 * @param isAddTempLmt
	 *            是否包含临时额度
	 * @return
	 */
	public BigDecimal loanOTB(CcsAcctO accountO, ProductCredit productCredit, Date bizDate, boolean isAddTempLmt) {
		BigDecimal sumloanInitPrin = authCommonService.sumloanInitPrin(accountO);
		BigDecimal sumUnmatchLoanChbTxnAmt = authCommonService.sumUnmatchLoanChbTxnAmt(accountO);
		if (isAddTempLmt) {
			return accountOTB.loanOTB(accountO, productCredit, sumloanInitPrin, sumUnmatchLoanChbTxnAmt, bizDate);
		} else {
			return accountOTB.loanInqOTB(accountO, productCredit, sumloanInitPrin, sumUnmatchLoanChbTxnAmt, bizDate);
		}
	}

	/**
	 * 获得分期注册表信息-暂存在context中
	 * @param context
	 * @throws AuthException 
	 */
	private void buildLoanReg(AuthContext context) throws AuthException{
		LoanInfo loanInfo = context.getLoanInfo();
		TxnInfo txnInfo = context.getTxnInfo();
		CcsCardO card = context.getCard();
		CcsAcctO accountO = context.getAccount();
		CcsAcct account = custAcctCardFacility.getAcctByAcctNbr(accountO.getAcctType(),accountO.getAcctNbr());
		try {
			//增加分期
			AuthLoanProvideImpl authLoan = new AuthLoanProvideImpl(loanInfo.getLoanType(), loanInfo.getProgramFeeDef(), loanInfo.getLoanFeeMethod());
			CcsLoanReg ccsLoanReg = authLoan.genLoanReg(loanInfo.getLoanInitTerm(), txnInfo.getChbTransAmt()
									, loanInfo.getRefNbr()
									, account.getDefaultLogicCardNbr() , card.getLogicCardNbr()
									, loanInfo.getLoanFeeMethod(), accountO.getAcctNbr(), accountO.getAcctType()
									, loanInfo.getProgram().loanPlanId, unifieldParServer.BusinessDate());
			// 将分期注册表放入loanInfo
			context.getLoanInfo().setCcsLoanReg(ccsLoanReg);
		} catch (Exception e) {
			//TODO 获取分期注册表信息时出现异常、reason没有确定
			authCommonService.throwAuthException(AuthReason.S009, "生成分期注册表信息时出现异常");
		}
	}
	
}
