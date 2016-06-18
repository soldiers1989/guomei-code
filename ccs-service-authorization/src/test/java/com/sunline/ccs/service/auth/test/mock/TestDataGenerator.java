package com.sunline.ccs.service.auth.test.mock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import com.sunline.ccs.test.CCSDataSet;
//import com.sunline.ccs.test.TestDataUtils;

/**
 * 自动化测试数据准备工具
* @author fanghj
 *
 */
@Service
@Transactional
public class TestDataGenerator {

//	/**
//	 * 参数获取组件
//	 */
//	@Autowired
//	private UnifiedParameterFacility unifiedParameter;
//	
//	@Autowired
//	private GlobalManagementService globalManagementService;
//
//	/**
//	 * 数据库访问组件
//	 */
//	@PersistenceContext
//	private EntityManager em;
//
//	@Autowired
//	private RCcsAcctNbr rCcsAcctNbr;
//	
//	
//	/**
//	 * 创建已激活主卡
//	 * @param productCode
//	 * @param nextStmtDate
//	 * @return
//	 * @throws Exception
//	 */
//	public CCSDataSet createActMainCard(String productCode, Date nextStmtDate) {
//		CCSDataSet cpsData = createMainCard(productCode);
//		// 更改账户上的下一账单日
//		for (CcsAcct acct : cpsData.getCcsAcct()){
//			acct.setNextStmtDate(nextStmtDate);
//		}
//		// 更改卡表上的激活标志
//		for (CcsCard card : cpsData.getCcsCard()){
//			card.setActivateDate(DateUtils.addDays(nextStmtDate, 35));
//			card.setActivateInd(Indicator.Y);
//		}
//		// 更改授权卡表
//		for (CcsCardO cardO : cpsData.getCcsCardO()){
//			cardO.setActivateInd(Indicator.Y);
//		}
//		return cpsData;
//	}
//	
//	/**
//	 * 建一张主卡（未激活）
//	 * @param productCode
//	 * @return
//	 */
//	public CCSDataSet createMainCard(String productCode)
//	{
//		CCSDataSet cpsData = new CCSDataSet();
//		//	判断贷记卡产品参数是否存在
//		ProductCredit productCredit = unifiedParameter.retrieveParameterObject(productCode, ProductCredit.class);
//		if ( productCredit == null){
//			throw new IllegalArgumentException("贷记卡产品参数ProductCredit不存在");
//		}
//		
//		//	判断卡产品参数是否存在
//		Product product = unifiedParameter.retrieveParameterObject(productCode, Product.class);
//		if ( product == null){
//			throw new IllegalArgumentException("卡产品参数Product不存在");
//		}
//		
//		
//		// 创建用户
//		CcsCustomer cust = createCcsCustomer();
//		
//		// 创建地址信息
//		List<CcsAddress> addressList = createCcsAddress(cust.getCustId());	
//		
//		// 创建联系人信息
//		List<CcsLinkman> contactList = createCcsLinkman(cust);
//		
//		// 创建用户信用额度
//		CcsCustomerCrlmt custLimitO = createCustomerLimitO(cust);
//		cust.setCustLmtId(custLimitO.getCustLmtId());
//		
//		// 创建逻辑卡
//		CcsCard card = createCcsCard(cust, product);
//		
//		// 创建逻辑卡统计表
//		CcsCardUsage cardStst = createCcsCardUsage(card);
//		
//		// 创建未匹配授权交易
//		CcsAuthmemoO unmatch =  createUnmatchO();
//		
//		// 创建授权卡表
//		CcsCardO cardO = createCcsCardO(card, custLimitO.getCustLmtId());
//		
//		// 创建物理卡和逻辑卡关联表
//		CcsCardLmMapping cardMediaMap = createCcsCardLmMapping(card, card.getLogicCardNbr());
//		
//		// 创建逻辑卡限额覆盖表
//		CcsCardThresholdCtrl cardLimitOverrideO = createCcsCardThresholdCtrl(card.getLogicCardNbr());
//		
//		// 创建账号
//		List<CcsAcct> ccsAcctList = createAccount(productCredit, cust, card.getLogicCardNbr());
//		card.setAcctNbr(ccsAcctList.get(0).getAcctNbr());
//		cardO.setAcctNbr(ccsAcctList.get(0).getAcctNbr());
//		// 创建账号授权表
//		List<CcsAcctO> ccsAcctOList = createAccountO(ccsAcctList);		
//		
//		for (CcsAcctO acctO : ccsAcctOList){
//			cpsData.getCcsAcctO().add(acctO);
//		}
//		int i=1000;
//		for (CcsAcct acct : ccsAcctList){
//			cpsData.getCcsAcct().add(acct);
//			createCcsTxnHst(i,acct);
//			i++;
//		
//		}
//		cpsData.getCcsCardLmMapping().add(cardMediaMap);
//		cpsData.getCcsCardO().add(cardO);
//		cpsData.getCcsCardUsage().add(cardStst);
//		cpsData.getCcsCard().add(card);
//		cpsData.getCcsCardThresholdCtrl().add(cardLimitOverrideO);
//		cpsData.getCcsCustomerCrlmt().add(custLimitO);
//		cpsData.getCcsAuthmemoO().add(unmatch);
//		
//		for (CcsLinkman contact : contactList){
//			cpsData.getCcsLinkman().add(contact);
//		}
//		for (CcsAddress address : addressList){
//			cpsData.getCcsAddress().add(address);
//		}			
//		cpsData.getCcsCustomer().add(cust);
//		return cpsData;
//	}
//	
//	public CCSDataSet createActMainCardwithAge(String productCode, Date nextStmtDate, String ageCd){
//		CCSDataSet cpsData = createActMainCard(productCode, nextStmtDate);
//		ProductCredit productCredit = unifiedParameter.loadParameter(productCode, ProductCredit.class);
//		AccountAttribute mainAcctAttr = unifiedParameter.loadParameter(productCredit.accountAttributeId.toString(), AccountAttribute.class);
//		AccountAttribute suppAcctAttr = unifiedParameter
//				.loadParameter(productCredit.dualAccountAttributeId.toString(), AccountAttribute.class);
//		// 更改账户上的账单日,首次账单日，宽限日
//		for (CcsAcct acct : cpsData.getCcsAcct()){
//			//	判断是哪个参数类型
//			AccountAttribute acctAttr = null;
//			if (mainAcctAttr.accountType == acct.getAcctType()){
//				acctAttr = mainAcctAttr;
//			}	else{
//				acctAttr = suppAcctAttr;
//			}
//			
//			//	设置账单日、首次账单日、
//			if (acctAttr.cycleBaseInd == CycleBaseInd.M){
//				acct.setFirstStmtDate(DateUtils.addMonths(nextStmtDate, - acctAttr.cycleBaseMult));
//				acct.setLastStmtDate(DateUtils.addMonths(nextStmtDate, - acctAttr.cycleBaseMult));
//			}else{
//				acct.setFirstStmtDate(DateUtils.addDays(nextStmtDate, - acctAttr.cycleBaseMult * 7));
//				acct.setLastStmtDate(DateUtils.addDays(nextStmtDate, - acctAttr.cycleBaseMult * 7));
//			}
//			
//			//	设置还款日、宽限日
//			switch (acctAttr.paymentDueDay){
//			case C :
//				acct.setGraceDate(nextStmtDate); 
//				acct.setPmtDueDate(nextStmtDate);
//				break;
//			case D :
//				acct.setGraceDate(DateUtils.addDays(acct.getLastStmtDate(), acctAttr.pmtDueDays + acctAttr.pmtGracePrd));
//				acct.setPmtDueDate(DateUtils.addDays(acct.getLastStmtDate(), acctAttr.pmtDueDays));
//				break;
//			case F :
//				if (acctAttr.pmtDueDate.compareTo(99) == 0){
//					acct.setGraceDate(DateUtils.addDays(DateUtils.setDays(nextStmtDate, 1), -1));
//					acct.setPmtDueDate(DateUtils.addDays(DateUtils.setDays(nextStmtDate, 1), -1));
//				} else{
//					Date d = DateUtils.setDays(nextStmtDate, acctAttr.pmtDueDate);
//					if (d.after(nextStmtDate)){
//						acct.setGraceDate(DateUtils.addMonths(d, -1));
//						acct.setPmtDueDate(DateUtils.addMonths(d, -1));
//					}	else{
//						acct.setGraceDate(d);
//						acct.setPmtDueDate(d);
//					}
//				}
//				break;
//			default : throw new IllegalArgumentException("账户类型参数取值有误");
//			}
//		}
//			
//		// 更新账龄、最小还款额
//		// TODO
//	
//		// 创建信用计划
//		List<PlanType> plist = new ArrayList<PlanType>();
//		plist.add(PlanType.R);
//		for (CcsAcct acct : cpsData.getCcsAcct()){
//			List<CcsPlan> plans = addPlans(acct, plist);
//			for (CcsPlan plan : plans){
//				cpsData.getCcsPlan().add(plan);
//			}
//		}
//		
//		// 更改卡表上的激活标志
//		for (CcsCard card : cpsData.getCcsCard()){
//			card.setActivateDate(DateUtils.addDays(nextStmtDate, 35));
//			card.setActivateInd(Indicator.Y);
//		}
//		// 更改授权卡表
//		for (CcsCardO cardO : cpsData.getCcsCardO()){
//			cardO.setActivateInd(Indicator.Y);
//		}
//		return cpsData;
//		
//	}
//	
//	/**
//	 * 创建消费信用计划
//	 * @param account
//	 * @return
//	 */
//	public List<CcsPlan> addPlans(CcsAcct account, List<PlanType> types){
//		return addPlans(account, types,true);
//	}
//	
//	/**
//	 * 根据needAmt判断是否需要对利息字段进行赋值
//	 * @param account
//	 * @param types
//	 * @param needAmt
//	 * @return
//	 */
//	public List<CcsPlan> addPlans(CcsAcct account, List<PlanType> types,boolean needAmt){
//		List<CcsPlan> plans = new ArrayList<CcsPlan>();
//		CcsPlan plan = null;
//		for (PlanType p : types){
//			plan = createPlan(p, getPlanTemplate(p), account,needAmt);
//			plans.add(plan);
//		}
//		return plans;
//	}
//	
//	/**
//	 * 生成账单数据
//	 * @param account
//	 * @param stmtDay
//	 * @param stmtFlag
//	 * @return
//	 */
//	public CcsStatement addStmtHsts(CcsAcct account, Date stmtDay, Indicator stmtFlag){
//		CcsStatement stmt = new CcsStatement();
//		stmt.setAcctNbr(account.getAcctNbr());
//		stmt.setAcctType(account.getAcctType());
//		stmt.setAgeCd(account.getAgeCd());
//		stmt.setBlockCode(account.getBlockCode());
//		stmt.setCashLmtRate(BigDecimal.valueOf(0.1));
//		stmt.setCreditLmt(BigDecimal.valueOf(9999));
//		stmt.setCtdAdjPoints(BigDecimal.valueOf(1));
//		stmt.setCtdAmtCr(BigDecimal.valueOf(2));
//		stmt.setCtdAmtDb(BigDecimal.valueOf(3));
//		stmt.setCtdCashAmt(BigDecimal.valueOf(4));
//		stmt.setCtdCashCnt(5);
//		stmt.setCtdCrAdjAmt(BigDecimal.valueOf(6));
//		stmt.setCtdCrAdjCnt(7);
//		stmt.setCtdDbAdjAmt(BigDecimal.valueOf(8));
//		stmt.setCtdDbAdjCnt(9);
//		stmt.setCtdDisbPoints(BigDecimal.valueOf(10));
//		stmt.setCtdEarnedPoints(BigDecimal.valueOf(11));
//		stmt.setCtdFeeAmt(BigDecimal.valueOf(12));
//		stmt.setCtdFeeCnt(13);
//		stmt.setCtdNbrCr(14);
//		stmt.setCtdNbrDb(15);
//		stmt.setCtdPaymentAmt(BigDecimal.valueOf(16));
//		stmt.setCtdPaymentCnt(17);
//		stmt.setCtdRefundAmt(BigDecimal.valueOf(18));
//		stmt.setCtdRefundCnt(19);
//		stmt.setCtdRetailAmt(BigDecimal.valueOf(20));
//		stmt.setCtdRetailCnt(21);
//		stmt.setCurrCd(account.getCurrCd());
//		stmt.setCurrDueAmt(BigDecimal.valueOf(22));
//		stmt.setDualBillingFlag(account.getDualBillingFlag());
//		stmt.setGraceDaysFullInd(account.getGraceDaysFullInd());
//		stmt.setLastStmtDate(account.getLastStmtDate());
//		stmt.setLoanLmtRate(BigDecimal.valueOf(23));
//		stmt.setName(account.getName());
//		stmt.setOrg(account.getOrg());
//		stmt.setOvrlmtDate(account.getOvrlmtDate());
//		stmt.setPastDueAmt1(BigDecimal.valueOf(24));
//		stmt.setPastDueAmt2(BigDecimal.valueOf(25));
//		stmt.setPastDueAmt3(BigDecimal.valueOf(26));
//		stmt.setPastDueAmt4(BigDecimal.valueOf(27));
//		stmt.setPastDueAmt5(BigDecimal.valueOf(28));
//		stmt.setPastDueAmt6(BigDecimal.valueOf(29));
//		stmt.setPastDueAmt7(BigDecimal.valueOf(30));
//		stmt.setPastDueAmt8(BigDecimal.valueOf(31));
//		stmt.setPmtDueDate(account.getPmtDueDate());
//		stmt.setPointBal(BigDecimal.valueOf(32));
//		stmt.setPointBeginBal(BigDecimal.valueOf(33));
//		stmt.setQualGraceBal(BigDecimal.valueOf(34));
//		stmt.setStmtBegBal(BigDecimal.valueOf(35));
//		stmt.setStmtCurrBal(BigDecimal.valueOf(36));
//		stmt.setStmtDate(stmtDay);
//		stmt.setStmtFlag(stmtFlag);
//		stmt.setTempLmt(account.getTempLmt());
//		stmt.setTempLmtBegDate(account.getTempLmtBegDate());
//		stmt.setTempLmtEndDate(account.getTempLmtEndDate());
//		stmt.setTotDueAmt(BigDecimal.valueOf(37));
//		stmt.setDefaultLogicCardNbr(account.getDefaultLogicCardNbr());
//		stmt.setEmail(account.getEmail());
//		stmt.setGender(account.getGender());
//		stmt.setMobileNo(account.getMobileNo());
//		stmt.setStmtAddress(account.getStmtAddress());
//		stmt.setStmtCity(account.getStmtCity());
//		stmt.setStmtCountryCd(account.getStmtCountryCd());
//		stmt.setStmtDistrict(account.getStmtDistrict());
//		stmt.setStmtMediaType(account.getStmtMediaType());
//		stmt.setStmtState(account.getStmtState());
//		stmt.setStmtZip(account.getStmtState());
//		
//		em.persist(stmt);
//		return stmt;
//	}
//	
//	/**
//	 * 增加分期注册表
//	 * @param account
//	 * @param regDate
//	 * @return
//	 */
//	public List<CcsLoanReg> addLoanReg(CcsAcct account, Date regDate){
//		List<CcsLoanReg> list = new ArrayList<CcsLoanReg>();
//		list.add(genCcsLoanReg(account, LoanType.R, regDate));
//		return list;
//	}
//	
//	/**
//	 * 产生单条分期注册
//	 * @param account
//	 * @param loanType
//	 * @param regDate
//	 * @return
//	 */
//	private CcsLoanReg genCcsLoanReg(CcsAcct account, LoanType loanType, Date regDate){
//		CcsLoanReg reg = new CcsLoanReg();
//		reg.setAcctNbr(account.getAcctNbr());
//		reg.setAcctType(account.getAcctType());
//		reg.setCardNo(account.getDefaultLogicCardNbr());
//		Map<String, LoanPlan> loanPlans = unifiedParameter.retrieveParameterObject(LoanPlan.class);
//		if (loanPlans.isEmpty())
//			throw new IllegalArgumentException("分期计划参数不存在");
//		for (String key : loanPlans.keySet()){
//			if (loanPlans.get(key).loanType == loanType){
//				reg.setLoanCode(loanPlans.get(key).loanCode);
//				break;
//			}
//		}
//		reg.setLoanFeeMethod(LoanFeeMethod.E);
//		reg.setLoanFinalTermFee1(BigDecimal.valueOf(20));
//		reg.setLoanFinalTermPrin(BigDecimal.valueOf(1000));
//		reg.setLoanFirstTermFee(BigDecimal.valueOf(10));
//		reg.setLoanFirstTermPrin(BigDecimal.valueOf(500));
//		reg.setLoanFixedFee(BigDecimal.valueOf(30));
//		reg.setLoanFixedPmtPrin(BigDecimal.valueOf(600));
//		reg.setLoanInitFee(BigDecimal.valueOf(130));
//		reg.setLoanInitPrin(BigDecimal.valueOf(6500));
//		reg.setLoanInitTerm(12);
//		reg.setLoanRegStatus(LoanRegStatus.N);
////		reg.setLoanStatus(LoanStatus.);
//		reg.setLoanType(loanType);
//		reg.setLogicCardNbr(account.getDefaultLogicCardNbr());
//		reg.setOrg(account.getOrg());
//		reg.setOrigAuthCode("123456");
//		reg.setOrigTransDate(DateUtils.addDays(regDate, -1));
//		reg.setOrigTxnAmt(BigDecimal.valueOf(7000));
//		reg.setRefNbr("1234567890");
//		reg.setRegisterDate(regDate);
//		reg.setRequestTime(regDate);
//		reg.setLoanAction(LoanAction.A);
//		em.persist(reg);
//		return reg;
//	}
//	/**
//	 * 增加分期注册表
//	 * @param account
//	 * @param regDate
//	 * @return
//	 */
//	public List<CcsLoan> addLoan(CcsAcct account, Date regDate){
//		List<CcsLoan> list = new ArrayList<CcsLoan>();
//		list.add(genCcsLoan(account, LoanType.MCEI, regDate));
//		list.add(genCcsLoan(account, LoanType.MCAT, regDate));
//		return list;
//	}
//	
//	/**
//	 * 产生单条分期注册
//	 * @param account
//	 * @param loanType
//	 * @param regDate
//	 * @return
//	 */
//	private CcsLoan genCcsLoan(CcsAcct account, LoanType loanType, Date regDate){
//		CcsLoan loan = new CcsLoan();
//	
//		loan.fillDefaultValues();
//		loan.setAcctNbr(account.getAcctNbr());
//		loan.setAcctType(account.getAcctType());
//		loan.setCardNo(account.getDefaultLogicCardNbr());
//		Map<String, LoanPlan> loanPlans = unifiedParameter.retrieveParameterObject(LoanPlan.class);
//		if (loanPlans.isEmpty())
//			throw new IllegalArgumentException("分期计划参数不存在");
//		for (String key : loanPlans.keySet()){
//			if (loanPlans.get(key).loanType == loanType){
//				loan.setLoanCode(loanPlans.get(key).loanCode);
//				break;
//			}
//		}
//		loan.setLoanFeeMethod(LoanFeeMethod.E);
//		loan.setLoanFinalTermFee1(BigDecimal.valueOf(20));
//		loan.setLoanFinalTermPrin(BigDecimal.valueOf(1000));
//		loan.setLoanFirstTermFee(BigDecimal.valueOf(10));
//		loan.setLoanFirstTermPrin(BigDecimal.valueOf(500));
//		loan.setLoanFixedFee(BigDecimal.valueOf(30));
//		loan.setLoanFixedPmtPrin(BigDecimal.valueOf(600));
//		loan.setLoanInitFee(BigDecimal.valueOf(130));
//		loan.setLoanInitPrin(BigDecimal.valueOf(6500));
//		loan.setLoanInitTerm(12);
//		
//		loan.setLoanStatus(LoanStatus.A);
//		loan.setLoanType(loanType);
//		loan.setLogicCardNbr(account.getDefaultLogicCardNbr());
//		loan.setOrg(account.getOrg());
//		loan.setOrigAuthCode("123456");
//		loan.setOrigTransDate(DateUtils.addDays(regDate, -1));
//		loan.setOrigTxnAmt(BigDecimal.valueOf(7000));
//		loan.setRefNbr("1234567890");
//		loan.setRegisterDate(regDate);
//		loan.setRequestTime(regDate);
//		if(loan.getLoanType() == LoanType.MCEI){
//			loan.setCurrTerm(3);
//			loan.setRemainTerm(9);
//		}
//		
//		em.persist(loan);
//		for(int i = 0;i<12;i++){
//			CcsRepaySchedule t = new CcsRepaySchedule();
//			t.fillDefaultValues();
//			t.setAcctNbr(loan.getAcctNbr());
//			t.setAcctType(loan.getAcctType());
//			t.setCardNo(loan.getCardNo());
//			t.setCurrTerm(i+1);
//			t.setLoanId(loan.getLoanId());
//			t.setLoanTermInterest(BigDecimal.valueOf(10));
//			t.setLoanTermPrin(BigDecimal.valueOf(50));
//			em.persist(t);
//		}
//		return loan;
//	}
//	/**
//	 * 产生积分调整通知记录
//	 * @param account
//	 * @param adjInds 调整类型列表
//	 * @param txnDate
//	 * @return
//	 */
//	public List<CcsPointsReg> addPointReg(CcsAcct account, List<PointAdjustIndicator> adjInds, Date txnDate){
//		List<CcsPointsReg> regList = new ArrayList<CcsPointsReg>();
//		for (PointAdjustIndicator adj : PointAdjustIndicator.values()){
//			CcsPointsReg reg = genCcsPointsReg(account, adj, txnDate);
//			regList.add(reg);
//		}
//		return regList;
//	}
//	
//	/**
//	 * 产生单条积分调整记录
//	 * @param account
//	 * @param adj
//	 * @param txnDate
//	 * @return
//	 */
//	private CcsPointsReg genCcsPointsReg(CcsAcct account, PointAdjustIndicator adj, Date txnDate){
//		CcsPointsReg reg = new CcsPointsReg();
//		reg.setAcctNbr(account.getAcctNbr());
//		reg.setAcctType(account.getAcctType());
//		reg.setAdjInd(adj);
//		reg.setCardNo(account.getDefaultLogicCardNbr());
//		reg.setOrg(account.getOrg());
//		reg.setPoint(BigDecimal.valueOf(100));
//		reg.setPostTxnType(PostTxnType.P);
//		reg.setTxnDate(txnDate);
//		reg.setRequestTime(new Date());
//		em.persist(reg);
//		return reg;
//	}
//	/**
//	 * 增加客服费用明细
//	 * @param card
//	 * @param serviceNbrs
//	 * @param batchDate
//	 * @return
//	 */
//	public List<CcsCssfeeReg> addCssFeeReg(CcsCard card, List<String> serviceNbrs, Date batchDate){
//		List<CcsCssfeeReg> list = new ArrayList<CcsCssfeeReg>();
//		CcsCssfeeReg reg = null;
//		for (String nbr : serviceNbrs){
//			reg = createCcsCssfeeReg(card.getOrg(), card.getLogicCardNbr(), nbr, batchDate);
//			list.add(reg);
//		}
//		return list;
//	}
//	
//	/**
//	 * 创建单条客服费用明细
//	 * @param org
//	 * @param cardNbr
//	 * @param serviceNbr
//	 * @param batchDate
//	 * @return
//	 */
//	private CcsCssfeeReg createCcsCssfeeReg(String org, String cardNbr, String serviceNbr, Date batchDate){
//		CcsCssfeeReg reg = new CcsCssfeeReg();
//		reg.setCardNo(cardNbr);
//		reg.setServiceNbr(serviceNbr);
//		reg.setOrg(org);
//		reg.setCssfeeTxnSeq(new Random().nextInt());
//		reg.setTxnDate(batchDate);
//		reg.setRequestTime(new Date());
//		em.persist(reg);
//		return reg;
//	}
//	
//	/**
//	 * 根据信用计划类型获取信用计划模板
//	 * @param planType
//	 * @return
//	 */
//	private PlanTemplate getPlanTemplate(PlanType planType){
//		PlanTemplate pt = null;
//		Map<String, PlanTemplate> maps = unifiedParameter.retrieveParameterObject(PlanTemplate.class);
//		
//		for (String key : maps.keySet()){
//			pt = maps.get(key);
//			if (pt.planType == planType){
//				return pt;
//			}
//		}
//		throw new IllegalArgumentException("信用计划模板不存在：PlanType:[" + planType.toString() + "]");
//	}
//	
//	/**
//	 * 创建信用计划
//	 * @param planType
//	 * @param planTemplate
//	 * @param account
//	 * @return
//	 */
//	private CcsPlan createPlan(PlanType planType, PlanTemplate planTemplate, CcsAcct account,boolean needAmt){
//		CcsPlan plan = new CcsPlan();
//		plan.fillDefaultValues();
//		plan.setOrg(OrganizationContextHolder.getCurrentOrg());
//		plan.setAcctNbr(account.getAcctNbr());
//		plan.setAcctType(account.getAcctType());
//		plan.setBeginBal(BigDecimal.valueOf(1000));
//		plan.setCurrBal(BigDecimal.valueOf(3000));
//		plan.setLogicCardNbr(account.getDefaultLogicCardNbr());
//		plan.setOrg(account.getOrg());
//		plan.setPastPrincipal(BigDecimal.valueOf(1000));
//		plan.setCtdPrincipal(BigDecimal.valueOf(2000));
//		plan.setPlanNbr(planTemplate.planNbr);
//		plan.setPlanType(planTemplate.planType);
//		plan.setProductCd(account.getProductCd());
//		plan.setInterestRate(new BigDecimal(0.1825));
//		plan.setCompoundRate(new BigDecimal(0.1825));
//		plan.setPenaltyRate(new BigDecimal(0.1825));
//		if(planType == PlanType.Q || planType == PlanType.P || planType == PlanType.J || planType == PlanType.L){
//			plan.setUsePlanRate(Indicator.Y);
//			if(needAmt){
//				plan.setCtdInterest(BigDecimal.valueOf(100));
//				plan.setCtdPenalty(BigDecimal.valueOf(100));
//				plan.setCtdComInt(BigDecimal.valueOf(100));
//			}
//		}else{
//			plan.setUsePlanRate(Indicator.N);
//		}
//		plan.setCompoundAcru(BigDecimal.ZERO);
//		plan.setPenaltyAcru(BigDecimal.ZERO);
//		
//		em.persist(plan);
//		return plan;
//	}
//	
//	
//	/**
//	 * 创建客户信息
//	 * @return
//	 */
//	private CcsCustomer createCcsCustomer()
//	{
//		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//		
//		CcsCustomer customer = new CcsCustomer();
//		customer.setOrg(OrganizationContextHolder.getCurrentOrg());
//		customer.setCustLmtId(0);
//		customer.setIdNo(TestDataUtils.genIdNo());
//		customer.setIdType(IdType.I);
//		customer.setSetupDate(getSetupDate());
//		customer.setUserAmt1(BigDecimal.ZERO);
//		customer.setUserAmt2(BigDecimal.ZERO);
//		customer.setUserAmt3(BigDecimal.ZERO);
//		customer.setUserAmt4(BigDecimal.ZERO);
//		customer.setUserAmt5(BigDecimal.ZERO);
//		customer.setUserAmt6(BigDecimal.ZERO);
//		customer.setUserNumber1(0);
//		customer.setUserNumber2(0);
//		customer.setUserNumber3(0);
//		customer.setUserNumber4(0);
//		customer.setUserNumber5(0);
//		customer.setUserNumber6(0);
//
//		customer.setName("姓名");
//		customer.setOncardName("OncardName");
//		customer.setGender(Gender.M);
//		try {
//			customer.setBirthday(df.parse("1980-01-01"));
//		} catch (ParseException e) {
//		}
//		customer.setPrOfCountry(Indicator.Y);
//		customer.setMaritalStatus(MaritalStatus.M);
//		customer.setQualification(EducationType.A);
//		customer.setSocialStatus(SocialStatus.N);
//		customer.setHouseOwnership(HouseOwnership.A);
//		customer.setHouseType(HouseType.A);
//		customer.setLiquidAsset(LiquidAsset.C);
//		customer.setMobileNo(TestDataUtils.genMobileNo());
//		customer.setEmpStatus(Indicator.Y);
//		customer.setNbrOfDependents(1);
//		customer.setSocialInsAmt(BigDecimal.valueOf(10));
//		customer.setEmpStability(EmpStability.A);
//		em.persist(customer);
//		
//		return customer;
//	}
//	
//	/**
//	 * 创建地址信息列表
//	 * @param custId
//	 * @return
//	 */
//	private List<CcsAddress> createCcsAddress(Integer custId){
//		List<CcsAddress> addressList = new ArrayList<CcsAddress>();
//		for (AddressType addressType : AddressType.values()){
//			addressList.add(createCcsAddress(custId, addressType));
//		}
//		return addressList;
//	}
//
//	/**
//	 * 创建单个地址信息
//	 * @param custId
//	 * @param addressType
//	 * @return
//	 */
//	private CcsAddress createCcsAddress(Integer custId, AddressType addressType){
//		CcsAddress addr = new CcsAddress();
//		addr.setOrg(OrganizationContextHolder.getCurrentOrg());
//		addr.setAddrType(addressType);
//		addr.setCustId(custId);
//		addr.setAddress("地址1234567890");
//		addr.setCity("上海");
//		addr.setCountryCd("CHN");
//		addr.setDistrict("浦东新区");
//		addr.setState("上海");
//		addr.setPhone(TestDataUtils.genMobileNo());
//		addr.setZip("200001");
//		em.persist(addr);
//		return addr;
//	}
//	/**
//	 * 创建联系人
//	 * @param custId
//	 * @return
//	 */
//	private List<CcsLinkman> createCcsLinkman(CcsCustomer cust){
//		List<CcsLinkman> contactList = new ArrayList<CcsLinkman>();
//		CcsLinkman contact = null;
//		contact = createCcsLinkman(cust, Relationship.M);
//		contactList.add(contact);
//		contact = createCcsLinkman(cust, Relationship.F);
//		contactList.add(contact);
//		return contactList;
//	}
//	
//	/**
//	 * 根据关系生成联系人
//	 * @param custId
//	 * @param relation
//	 * @return
//	 */
//	private CcsLinkman createCcsLinkman(CcsCustomer cust, Relationship relationship){
//		CcsLinkman contact = new CcsLinkman();
//		contact.setOrg(OrganizationContextHolder.getCurrentOrg());
//		contact.setBirthday(DateUtils.addDays(new Date(), -10000));
//		contact.setCorpFax("corpFax");
//		contact.setCorpName("corpName");
//		contact.setCorpPhone("4008123123");
//		contact.setCorpPost(EmpPositionAttrType.D);
//		contact.setCustId(cust.getCustId());
//		contact.setIdNo("311111111");
//		contact.setIdType(IdType.O);
//		contact.setMobileNo(TestDataUtils.genMobileNo());
//		contact.setName("联系人" + new Random().nextInt(100));
//		contact.setOrg(cust.getOrg());
//		contact.setRelationship(relationship);
//		switch (relationship){
//		case F :
//		case B :
//		case W :
//			contact.setGender(Gender.M); break;
//		case L :
//		case M :
//		case S : 
//			contact.setGender(Gender.F); break;
//		case C : 
//			if (cust.getGender() == null || cust.getGender() == Gender.M){
//				contact.setGender(Gender.F); 
//			}	else {
//				contact.setGender(Gender.M);
//			}
//			break;
//		default : throw new IllegalArgumentException("关系类型输入不正确");
//		}
//		return contact;
//	}
//	/**
//	 * 创建客户额度信息
//	 * @param customer
//	 * @return
//	 */
//	private CcsCustomerCrlmt createCustomerLimitO(CcsCustomer customer) {
//		
//		CcsCustomerCrlmt custLimit = new CcsCustomerCrlmt();
//		custLimit.setOrg(customer.getOrg());
//		custLimit.setLimitType(LimitType.H);
//		custLimit.setCreditLmt(BigDecimal.valueOf(10000));
//		custLimit.setLimitCategory(LimitCategory.CreditLmt);
//		
//		em.persist(custLimit);
//		return custLimit;
//	}
//	
//	/**
//	 * 创建逻辑卡
//	 * @param cust
//	 * @param acctNbr
//	 * @param productCredit
//	 * @param appNo
//	 * @return
//	 */
//	private CcsCard createCcsCard(CcsCustomer cust, Product product){
//		
//		CcsCard card = new CcsCard();
//		card.setOrg(cust.getOrg());
//		card.setLogicCardNbr(TestDataUtils.genCardNo(product));
//		card.setCustId(cust.getCustId());
//		card.setProductCd(product.productCode);
//		//	初始化时设置为0,建账完成后再更改
//		card.setAcctNbr(0);
//		card.setAppNo(TestDataUtils.genAppNo());
//		card.setBscSuppInd(BscSuppIndicator.B);
//		//	FIXME 明确主卡逻辑卡时，逻辑卡主卡卡号是否为null
//		card.setBscLogiccardNbr(card.getLogicCardNbr());
//		//	FIXME 从参数中获取分支机构号
//		card.setOwningBranch("111111");
//		card.setSetupDate(getSetupDate());
//		card.setActivateInd(Indicator.N);
//		card.setLatestCardNo(card.getLogicCardNbr());
//		card.setSalesInd(Indicator.N);
//		card.setPosPinVerifyInd(Indicator.N);
//		card.setRelationshipToBsc(Relationship.B);
//		card.setCardExpireDate(DateUtils.addYears(card.getSetupDate(), product.newCardValidPeriod));
//		//	FIXME 因为HSQL的类型，暂时由100 -> 10
//		card.setCardFeeRate(BigDecimal.valueOf(10));
//		card.setRenewInd(RenewInd.D);
//		card.setFirstUsageFlag(FirstUsageIndicator.A);
//		card.setWaiveCardfeeInd(Indicator.N);
//		card.setCardFetchMethod(CardFetchMethod.A);
//		card.setCardMailerInd(AddressType.C);
//		
//		// 测试vip添加
//		card.setBlockCode("V");
//		em.persist(card);
//		
//		return card;
//	}
//
//	/**
//	 * 创建逻辑卡统计信息表
//	 * @param card
//	 * @return
//	 */
//	private CcsCardUsage createCcsCardUsage(CcsCard card){
//		//建立对应的统计对象
//		CcsCardUsage cardSt = new CcsCardUsage();
//		
//		cardSt.setOrg(card.getOrg());
//		cardSt.setLogicCardNbr(card.getLogicCardNbr());
//		cardSt.setCtdRetailAmt(BigDecimal.ZERO);
//		cardSt.setCtdRetailCnt(0);
//		cardSt.setCtdCashAmt(BigDecimal.ZERO);
//		cardSt.setCtdCashCnt(0);
//		cardSt.setMtdRetailAmt(BigDecimal.ZERO);
//		cardSt.setMtdRetailCnt(0);
//		cardSt.setMtdCashAmt(BigDecimal.ZERO);
//		cardSt.setMtdCashCnt(0);
//		cardSt.setYtdRetailAmt(BigDecimal.ZERO);
//		cardSt.setYtdRetailCnt(0);
//		cardSt.setYtdCashAmt(BigDecimal.ZERO);
//		cardSt.setYtdCashCnt(0);
//		cardSt.setLtdRetailAmt(BigDecimal.ZERO);
//		cardSt.setLtdRetailCnt(0);
//		cardSt.setLtdCashAmt(BigDecimal.ZERO);
//		cardSt.setLtdCashCnt(0);
//		cardSt.setLastCycleRetailAmt(BigDecimal.ZERO);
//		cardSt.setLastCycleRetailCnt(0);
//		cardSt.setLastCycleCashAmt(BigDecimal.ZERO);
//		cardSt.setLastCycleCashCnt(0);
//		cardSt.setLastMthRetailAmt(BigDecimal.ZERO);
//		cardSt.setLastMthRetailCnt(0);
//		cardSt.setLastMthCashAmt(BigDecimal.ZERO);
//		cardSt.setLastMthCashCnt(0);
//		cardSt.setLastYearRetlAmt(BigDecimal.ZERO);
//		cardSt.setLastYearRetlCnt(0);
//		cardSt.setLastYearCashAmt(BigDecimal.ZERO);
//		cardSt.setLastYearCashCnt(0);	
//		em.persist(cardSt);
//		
//		return cardSt;
//	}
//
//	/**
//	 * 生成授权卡表
//	 * @param card
//	 * @return
//	 */
//	private CcsCardO createCcsCardO(CcsCard card, Integer custLmtId){
//		CcsCardO cardO = new CcsCardO();
//		cardO.setOrg(card.getOrg());
//		cardO.setAcctNbr(card.getAcctNbr());
//		cardO.setLogicCardNbr(card.getLogicCardNbr());
//		cardO.setCustId(card.getCustId());
//		cardO.setCustLmtId(custLmtId);
//		cardO.setProductCd(card.getProductCd());
//		cardO.setBscSuppInd(card.getBscSuppInd());
//		cardO.setBscLogiccardNbr(card.getBscLogiccardNbr());
//		cardO.setActivateInd(card.getActivateInd());
//		cardO.setPosPinVerifyInd(card.getPosPinVerifyInd());
//		cardO.setPinTries(0);
//		cardO.setBlockCode(card.getBlockCode());
//		cardO.setCycleRetailLmt(BigDecimal.valueOf(10000));
//		cardO.setCycleCashLmt(BigDecimal.valueOf(5000));
//		cardO.setCycleNetLmt(BigDecimal.valueOf(2000));
//		cardO.setTxnLimit(BigDecimal.valueOf(5000));
//		cardO.setTxnCashLimit(BigDecimal.valueOf(5000));
//		cardO.setTxnNetLimit(BigDecimal.valueOf(1000));
//		cardO.setDayUsedAtmNbr(0);
//		cardO.setDayUsedAtmAmt(BigDecimal.ZERO);
//		cardO.setDayUsedRetailNbr(0);
//		cardO.setDayUsedRetailAmt(BigDecimal.ZERO);
//		cardO.setDayUsedCashNbr(0);
//		cardO.setDayUsedCashAmt(BigDecimal.ZERO);
//		cardO.setDayUsedXfroutNbr(0);
//		cardO.setDayUsedXfroutAmt(BigDecimal.ZERO);
//		cardO.setCtdUsedAmt(BigDecimal.ZERO);
//		cardO.setCtdCashAmt(BigDecimal.ZERO);
//		cardO.setCtdNetAmt(BigDecimal.ZERO);
//		cardO.setInqPinTries(0);
//		cardO.setDayUsedAtmCupxbAmt(BigDecimal.valueOf(2000));
//		em.persist(cardO);
//		return cardO;
//	}
//	/**
//	 * 创建未匹配授权交易
//	 * @return
//	 */
//	private CcsAuthmemoO  createUnmatchO() {
//		CcsAuthmemoO unmatchO2 = new CcsAuthmemoO();
//		unmatchO2.setMti("0200");
//		unmatchO2.setB002CardNbr("6200480204984198");//request.getBody(2)主账号
//		unmatchO2.setAuthCode("600001");//request.getBody(38)授权标识应答码
//		unmatchO2.setB042MerId("10001");//request.getBody(42)受卡方标识码
//		unmatchO2.setB032AcqInst("10000000001");//受理机构标识码11
//		unmatchO2.setB033FwdIns("10000000002");//发送机构标识码11
//		unmatchO2.setB007TxnTime("0922124330");//传输日期
//		unmatchO2.setB011Trace("100002");//系统跟踪号
//		unmatchO2.setB004Amt(BigDecimal.valueOf(1000));
//		unmatchO2.setTxnAmt(BigDecimal.valueOf(1000));//getChbTransAmt
//		unmatchO2.setAuthTxnStatus(AuthTransStatus.N);
//		unmatchO2.setLogBizDate(getSetupDate());
//		unmatchO2.setFinalAction(AuthAction.A);
//		unmatchO2.setOrigTxnType(AuthTransType.Cash);
//		unmatchO2.setOrg("123123123123");
//		em.persist(unmatchO2);
//		
//		CcsAuthmemoO unmatchO = new CcsAuthmemoO();
//		unmatchO.setMti("0100");
//		unmatchO.setB003ProcCode("20");
//		unmatchO.setB002CardNbr("6200480204984198");//request.getBody(2)主账号
//		unmatchO.setAuthCode("600001");//request.getBody(38)授权标识应答码
//		unmatchO.setB042MerId("10001");//request.getBody(42)受卡方标识码
// 		unmatchO.setB032AcqInst("10000000001");//受理机构标识码11
// 		unmatchO.setB033FwdIns("10000000002");//发送机构标识码11
// 		unmatchO.setB007TxnTime("0922124320");//传输日期
// 		unmatchO.setB011Trace("100001");//系统跟踪号
//		unmatchO.setB004Amt(BigDecimal.valueOf(1000));
//		unmatchO.setTxnAmt(BigDecimal.valueOf(1000));//getChbTransAmt
//		unmatchO.setAuthTxnStatus(AuthTransStatus.N);
//		unmatchO.setLogBizDate(getSetupDate());
//		unmatchO.setFinalAction(AuthAction.A);
//		unmatchO.setOrigTxnType(AuthTransType.Cash);
//		unmatchO.setOrigLogKv(unmatchO2.getLogKv());
//		unmatchO.setOrg("123123123123");
//		em.persist(unmatchO);
//		
//		CcsAuthmemoO unmatchO3 = new CcsAuthmemoO();
//		unmatchO3.setMti("0100");
//		unmatchO3.setB003ProcCode("27");
//		unmatchO3.setB002CardNbr("6200480204984199");//request.getBody(2)主账号
//		unmatchO3.setAuthCode("600001");//request.getBody(38)授权标识应答码
//		unmatchO3.setB042MerId("10001");//request.getBody(42)受卡方标识码
// 		unmatchO3.setB032AcqInst("10000000001");//受理机构标识码11
// 		unmatchO3.setB033FwdIns("10000000002");//发送机构标识码11
// 		unmatchO3.setB007TxnTime("0922124321");//传输日期
// 		unmatchO3.setB011Trace("100001");//系统跟踪号
//		unmatchO3.setB004Amt(BigDecimal.valueOf(1000));
//		unmatchO3.setTxnAmt(BigDecimal.valueOf(1000));//getChbTransAmt
//		unmatchO3.setAuthTxnStatus(AuthTransStatus.N);
//		unmatchO3.setLogBizDate(getSetupDate());
//		unmatchO3.setFinalAction(AuthAction.A);
//		unmatchO3.setOrigTxnType(AuthTransType.Cash);
//		unmatchO3.setOrigLogKv(unmatchO.getLogKv());
//		unmatchO3.setOrg("123123123123");
//		em.persist(unmatchO3);
//		
//		CcsAuthmemoO unmatchO4 = new CcsAuthmemoO();
//		unmatchO4.setMti("0200");
//		unmatchO4.setB003ProcCode("21");
//		unmatchO4.setB002CardNbr("6200480204984177");//request.getBody(2)主账号
//		unmatchO4.setAuthCode("600001");//request.getBody(38)授权标识应答码
//		unmatchO4.setB042MerId("10001");//request.getBody(42)受卡方标识码
// 		unmatchO4.setB032AcqInst("10000000001");//受理机构标识码11
// 		unmatchO4.setB033FwdIns("10000000002");//发送机构标识码11
// 		unmatchO4.setB007TxnTime("0922124321");//传输日期
// 		unmatchO4.setB011Trace("100001");//系统跟踪号
//		unmatchO4.setB004Amt(BigDecimal.valueOf(1000));
//		unmatchO4.setTxnAmt(BigDecimal.valueOf(1000));//getChbTransAmt
//		unmatchO4.setAuthTxnStatus(AuthTransStatus.N);
//		unmatchO4.setLogBizDate(getSetupDate());
//		unmatchO4.setFinalAction(AuthAction.A);
//		unmatchO4.setOrigTxnType(AuthTransType.Cash);
//		unmatchO4.setOrigLogKv(unmatchO.getLogKv());
//		unmatchO4.setOrg("123123123123");
//		em.persist(unmatchO4);
//		
//		CcsAuthmemoO unmatchO5 = new CcsAuthmemoO();
//		unmatchO5.setMti("0200");
//		unmatchO5.setB003ProcCode("17");
//		unmatchO5.setB002CardNbr("6200480204984177");//request.getBody(2)主账号
//		unmatchO5.setAuthCode("600001");//request.getBody(38)授权标识应答码
//		unmatchO5.setB042MerId("10001");//request.getBody(42)受卡方标识码
// 		unmatchO5.setB032AcqInst("10000000001");//受理机构标识码11
// 		unmatchO5.setB033FwdIns("10000000002");//发送机构标识码11
// 		unmatchO5.setB007TxnTime("0922124321");//传输日期
// 		unmatchO5.setB011Trace("100001");//系统跟踪号
//		unmatchO5.setB004Amt(BigDecimal.valueOf(1000));
//		unmatchO5.setTxnAmt(BigDecimal.valueOf(1000));//getChbTransAmt
//		unmatchO5.setAuthTxnStatus(AuthTransStatus.N);
//		unmatchO5.setLogBizDate(getSetupDate());
//		unmatchO5.setFinalAction(AuthAction.A);
//		unmatchO5.setOrigTxnType(AuthTransType.Cash);
//		unmatchO5.setOrigLogKv(unmatchO4.getLogKv());
//		unmatchO5.setOrg("123123123123");
//		em.persist(unmatchO5);
//		
//		
//		CcsAuthmemoO unmatchO6 = new CcsAuthmemoO();
//		unmatchO6.setMti("0100");
//		unmatchO6.setB003ProcCode("03");
//		unmatchO6.setB002CardNbr("6200480204984167");//request.getBody(2)主账号
//		unmatchO6.setAuthCode("600001");//request.getBody(38)授权标识应答码
//		unmatchO6.setB042MerId("10001");//request.getBody(42)受卡方标识码
// 		unmatchO6.setB032AcqInst("10000000001");//受理机构标识码11
// 		unmatchO6.setB033FwdIns("10000000002");//发送机构标识码11
// 		unmatchO6.setB007TxnTime("0922124321");//传输日期
// 		unmatchO6.setB011Trace("100001");//系统跟踪号
//		unmatchO6.setB004Amt(BigDecimal.valueOf(1000));
//		unmatchO6.setTxnAmt(BigDecimal.valueOf(1000));//getChbTransAmt
//		unmatchO6.setAuthTxnStatus(AuthTransStatus.N);
//		unmatchO6.setLogBizDate(getSetupDate());
//		unmatchO6.setFinalAction(AuthAction.A);
//		unmatchO6.setChbTxnAmt(BigDecimal.valueOf(1000));
//		unmatchO6.setOrigTxnType(AuthTransType.Cash);
//		unmatchO6.setOrigLogKv(unmatchO4.getLogKv());
//		unmatchO6.setOrigChbTxnAmt(BigDecimal.valueOf(5000));
//		unmatchO6.setOrg("123123123123");
//		em.persist(unmatchO6);
//		
//		
//		CcsAuthmemoO unmatchO7 = new CcsAuthmemoO();
//		unmatchO7.setMti("0200");
//		unmatchO7.setB003ProcCode("00");
//		unmatchO7.setB002CardNbr("6200480204984167");//request.getBody(2)主账号
//		unmatchO7.setAuthCode("600001");//request.getBody(38)授权标识应答码
//		unmatchO7.setB042MerId("10001");//request.getBody(42)受卡方标识码
// 		unmatchO7.setB032AcqInst("10000000001");//受理机构标识码11
// 		unmatchO7.setB033FwdIns("10000000002");//发送机构标识码11
// 		unmatchO7.setB007TxnTime("0922124321");//传输日期
// 		unmatchO7.setB011Trace("100001");//系统跟踪号
//		unmatchO7.setB004Amt(BigDecimal.valueOf(1000));
//		unmatchO7.setTxnAmt(BigDecimal.valueOf(1000));//getChbTransAmt
//		unmatchO7.setAuthTxnStatus(AuthTransStatus.N);
//		unmatchO7.setLogBizDate(getSetupDate());
//		unmatchO7.setFinalAction(AuthAction.A);
//		unmatchO7.setChbTxnAmt(BigDecimal.valueOf(1000));
//		unmatchO7.setOrigTxnType(AuthTransType.Cash);
//		unmatchO7.setOrigLogKv(unmatchO6.getLogKv());
//		unmatchO7.setOrigChbTxnAmt(BigDecimal.valueOf(5000));
//		unmatchO7.setOrg("123123123123");
//		em.persist(unmatchO7);
//		
//		
//		CcsAuthmemoO unmatchO8 = new CcsAuthmemoO();
//		unmatchO8.setMti("0200");
//		unmatchO8.setB003ProcCode("20");
//		unmatchO8.setB002CardNbr("6200480204984167");//request.getBody(2)主账号
//		unmatchO8.setAuthCode("600001");//request.getBody(38)授权标识应答码
//		unmatchO8.setB042MerId("10001");//request.getBody(42)受卡方标识码
// 		unmatchO8.setB032AcqInst("10000000001");//受理机构标识码11
// 		unmatchO8.setB033FwdIns("10000000002");//发送机构标识码11
// 		unmatchO8.setB007TxnTime("0922124321");//传输日期
// 		unmatchO8.setB011Trace("100001");//系统跟踪号
//		unmatchO8.setB004Amt(BigDecimal.valueOf(1000));
//		unmatchO8.setTxnAmt(BigDecimal.valueOf(1000));//getChbTransAmt
//		unmatchO8.setAuthTxnStatus(AuthTransStatus.N);
//		unmatchO8.setLogBizDate(getSetupDate());
//		unmatchO8.setFinalAction(AuthAction.A);
//		unmatchO8.setChbTxnAmt(BigDecimal.valueOf(1000));
//		unmatchO8.setOrigTxnType(AuthTransType.Cash);
//		unmatchO8.setOrigLogKv(unmatchO7.getLogKv());
//		unmatchO8.setOrigChbTxnAmt(BigDecimal.valueOf(5000));
//		unmatchO8.setOrg("123123123123");
//		em.persist(unmatchO8);
//		
//		
//		return unmatchO;
//	}
//	
//	
//	/**
//	 *  创建逻辑卡限额覆盖表
//	 * @param newCardNo
//	 * @return
//	 */
//	private CcsCardThresholdCtrl createCcsCardThresholdCtrl(String newCardNo){
//		CcsCardThresholdCtrl ovr = new CcsCardThresholdCtrl();
//		//	TODO 
//		ovr.setLogicCardNbr(newCardNo);
//		ovr.setDayAtmOvriInd(Indicator.N);
//		ovr.setDayAtmAmtLmt(BigDecimal.valueOf(1000));
//		ovr.setDayAtmNbrLmt(0);
//		ovr.setDayCashOvriInd(Indicator.N);
//		ovr.setDayCashAmtLmt(BigDecimal.valueOf(1000));
//		ovr.setDayCashNbrLmt(0);
//		ovr.setDayRetlOvriInd(Indicator.N);
//		ovr.setDayRetailAmtLmt(BigDecimal.valueOf(1000));
//		ovr.setDayRetailNbrLmt(0);
//		ovr.setDayXfroutOvriInd(Indicator.N);
//		ovr.setDayXfroutAmtLmt(BigDecimal.valueOf(1000));
//		ovr.setDayXfroutNbrLmt(0);
//		ovr.setDayCupxbAtmAmtLmt(BigDecimal.valueOf(1000));
//		ovr.setDayCupxbAtmOvriInd(Indicator.N);
//		em.persist(ovr);
//		
//		return ovr ;
//		
//	}
//	/**
//	 * 生成物理卡和逻辑卡关联关系
//	 * @param card
//	 * @return
//	 */
//	private CcsCardLmMapping createCcsCardLmMapping(CcsCard card, String newCardNo){
//		CcsCardLmMapping tmCardLmMapping = new CcsCardLmMapping();
//		tmCardLmMapping.setCardNo(newCardNo);
//		tmCardLmMapping.setLogicCardNbr(card.getLogicCardNbr());
//		tmCardLmMapping.setOrg(card.getOrg());
//		em.persist(tmCardLmMapping);
//		return tmCardLmMapping;
//	}
//
//	/**
//	 * 产生卡产品对应所有账户
//	 * @return
//	 */
//	private List<CcsAcct> createAccount(ProductCredit product, CcsCustomer cust, String cardNbr){
//		List<CcsAcct> accountList = new ArrayList<CcsAcct>();
//		//	账户属性参数
//		AccountAttribute acctAttr = null;
//		
//		//	账户对象
//		CcsAcct account = null;
//		
//		//	建立本币账户
//		if (product.accountAttributeId != null){
//			acctAttr = unifiedParameter.retrieveParameterObject(product.accountAttributeId.toString(), AccountAttribute.class);
//			CcsAcctNbr nbr = new CcsAcctNbr();
//			nbr.setOrg(OrganizationContextHolder.getCurrentOrg());
//			//em.persist(nbr);
//			rCcsAcctNbr.save(nbr);
//			account = createCcsAcct(nbr.getAcctNbr(), acctAttr, cust, cardNbr, product);
//			accountList.add(account);
//		}
//		else
//		{
//			throw new IllegalArgumentException("产品参数缺少accountAttributeId");
//		}
//		
//		//	判断是否需要建立共享外币账户
//		if (acctAttr.accountType.isSharedCredit()){
//			acctAttr = unifiedParameter.retrieveParameterObject(product.dualAccountAttributeId.toString(), AccountAttribute.class);
//			if (!acctAttr.accountType.isSharedCredit()){
//				throw new IllegalArgumentException("产品参数中dualAccountAttributeId的账户属性非共享额度");
//			}
//			account = createCcsAcct(account.getAcctNbr(), acctAttr, cust, cardNbr, product);
//			accountList.add(account);
//		}
//		return accountList;
//	}
//	
//	/**
//	 * 产生单个账户信息
//	 * @param acctNbr
//	 * @param acctAttr
//	 * @return
//	 */
//	private CcsAcct createCcsAcct(Integer acctNbr, AccountAttribute acctAttr, CcsCustomer cust, String logicCardNbr, ProductCredit product){
//		CcsAcct acct = new CcsAcct();
//		acct.setOrg(cust.getOrg());
//		acct.setAcctNbr(acctNbr);
//		acct.setAcctType(acctAttr.accountType);
//		acct.setCustId(cust.getCustId());
//		acct.setCustLmtId(cust.getCustLmtId());	
//		acct.setProductCd(product.productCd);
//		acct.setDefaultLogicCardNbr(logicCardNbr);
//		acct.setCurrCd(acctAttr.accountType.getCurrencyCode());
//		// FIXME RMB 10000,外币进行折算
//		acct.setCreditLmt(BigDecimal.valueOf(10000));
//		acct.setGender(Gender.M);
//		acct.setTempLmt(BigDecimal.ZERO);
//		acct.setCashLmtRate(acctAttr.cashLimitRate);
//		acct.setOvrlmtRate(acctAttr.ovrlmtRate);
//		acct.setLoanLmtRate(acctAttr.loanLimitRate);
//		acct.setCurrBal(BigDecimal.ZERO);
//		acct.setCashBal(BigDecimal.ZERO);
//		acct.setPrincipalBal(BigDecimal.ZERO);
//		acct.setLoanBal(BigDecimal.ZERO);
//		acct.setDisputeAmt(BigDecimal.ZERO);
//		acct.setBeginBal(BigDecimal.ZERO);
//		acct.setPmtDueDayBal(BigDecimal.ZERO);
//		acct.setFirstPurchaseAmt(BigDecimal.ZERO);
//		acct.setQualGraceBal(BigDecimal.ZERO);
//		acct.setGraceDaysFullInd(Indicator.N);
//		acct.setPointBeginBal(BigDecimal.ZERO);
//		acct.setCtdEarnedPoints(BigDecimal.ZERO);
//		acct.setCtdDisbPoints(BigDecimal.ZERO);
//		acct.setCtdAdjPoints(BigDecimal.ZERO);
//		acct.setPointBal(BigDecimal.ZERO);
//		acct.setSetupDate(getSetupDate());
//		acct.setOvrlmtNbrOfCyc(0);
//		acct.setName(cust.getName());
//		//	FIXME 明确取分支机构号
//		acct.setOwningBranch("010101");
//		acct.setMobileNo(cust.getMobileNo());
//		acct.setCorpName(cust.getCorpName());
//		acct.setStmtAddress("账单地址1");
//		//	FIXME 明确按周循环的账户如何确定
//		acct.setCycleDay(product.dfltCycleDay.toString());
//		acct.setStmtFlag(Indicator.Y);
//		acct.setStmtMailAddrInd(AddressType.C);
//		acct.setStmtMediaType(StmtMediaType.P);
//		acct.setEmail(cust.getEmail());
//		acct.setAgeCd("0");
//		acct.setGlAgeCd("0");
//		acct.setAgeHst(StringUtils.rightPad("0", 24, "0"));
//		acct.setMemoDb(BigDecimal.ZERO);
//		acct.setMemoCash(BigDecimal.ZERO);
//		acct.setMemoCr(BigDecimal.ZERO);
//		acct.setDdInd(DdIndicator.N);
//		acct.setDdBankName("bankname");
//		acct.setDdBankBranch("branch");
//		acct.setDdBankAcctNbr("1001");
//		acct.setDdBankAcctName("姓名");
//		acct.setLastDdAmt(BigDecimal.ZERO);
//		acct.setDualBillingFlag(DualBillingInd.N);
//		acct.setLastPmtAmt(BigDecimal.ZERO);
//		//算账单日，暂时简单计算
//		Date nextStmtDate = DateUtils.setDays(getSetupDate(), product.dfltCycleDay);
//		if (nextStmtDate.before(getSetupDate())){
//			nextStmtDate = DateUtils.addMonths(nextStmtDate, 1);
//		}
//		acct.setNextStmtDate(nextStmtDate);
//		acct.setFirstStmtDate(nextStmtDate);
//		acct.setTotDueAmt(BigDecimal.ZERO);
//		acct.setCurrDueAmt(BigDecimal.ZERO);
//		acct.setPastDueAmt1(BigDecimal.ZERO);
//		acct.setPastDueAmt2(BigDecimal.ZERO);
//		acct.setPastDueAmt3(BigDecimal.ZERO);
//		acct.setPastDueAmt4(BigDecimal.ZERO);
//		acct.setPastDueAmt5(BigDecimal.ZERO);
//		acct.setPastDueAmt6(BigDecimal.ZERO);
//		acct.setPastDueAmt7(BigDecimal.ZERO);
//		acct.setPastDueAmt8(BigDecimal.ZERO);
//		acct.setCtdCashAmt(BigDecimal.ZERO);
//		acct.setCtdCashCnt(0);
//		acct.setCtdRetailAmt(BigDecimal.ZERO);
//		acct.setCtdRetailCnt(0);
//		acct.setCtdPaymentAmt(BigDecimal.ZERO);
//		acct.setCtdPaymentCnt(0);
//		acct.setCtdDbAdjAmt(BigDecimal.ZERO);
//		acct.setCtdDbAdjCnt(0);
//		acct.setCtdCrAdjAmt(BigDecimal.ZERO);
//		acct.setCtdCrAdjCnt(0);
//		acct.setCtdFeeAmt(BigDecimal.ZERO);
//		acct.setCtdFeeCnt(0);
//		acct.setCtdInterestAmt(BigDecimal.ZERO);
//		acct.setCtdInterestCnt(0);
//		acct.setCtdRefundAmt(BigDecimal.ZERO);
//		acct.setCtdRefundCnt(0);
//		acct.setCtdHiOvrlmtAmt(BigDecimal.ZERO);
//		acct.setMtdRetailAmt(BigDecimal.ZERO);
//		acct.setMtdRetailCnt(0);
//		acct.setMtdCashAmt(BigDecimal.ZERO);
//		acct.setMtdCashCnt(0);
//		acct.setMtdRefundAmt(BigDecimal.ZERO);
//		acct.setMtdRefundCnt(0);
//		acct.setMtdPaymentAmt(BigDecimal.ZERO);
//		acct.setMtdPaymentCnt(0);
//		acct.setYtdRetailAmt(BigDecimal.ZERO);
//		acct.setYtdRetailCnt(0);
//		acct.setYtdCashAmt(BigDecimal.ZERO);
//		acct.setYtdCashCnt(0);
//		acct.setYtdRefundAmt(BigDecimal.ZERO);
//		acct.setYtdRefundCnt(0);
//		acct.setYtdOvrlmtFeeAmt(BigDecimal.ZERO);
//		acct.setYtdOvrlmtFeeCnt(0);
//		acct.setYtdLpcAmt(BigDecimal.ZERO);
//		acct.setYtdLpcCnt(0);
//		acct.setYtdPaymentAmt(BigDecimal.ZERO);
//		acct.setYtdPaymentCnt(0);
//		acct.setLtdRetailAmt(BigDecimal.ZERO);
//		acct.setLtdRetailCnt(0);
//		acct.setLtdCashAmt(BigDecimal.ZERO);
//		acct.setLtdCashCnt(0);
//		acct.setLtdRefundAmt(BigDecimal.ZERO);
//		acct.setLtdRefundCnt(0);
//		acct.setLtdHighestPrincipal(BigDecimal.ZERO);
//		acct.setLtdHighestCrBal(BigDecimal.ZERO);
//		acct.setLtdHighestBal(BigDecimal.ZERO);
//		acct.setLtdPaymentAmt(BigDecimal.ZERO);
//		acct.setLtdPaymentCnt(0);
//		acct.setCollectTimes(0);
//		acct.setWaiveOvlfeeInd(Indicator.N);
//		acct.setWaiveCardfeeInd(Indicator.N);
//		acct.setWaiveLatefeeInd(Indicator.N);
//		acct.setWaiveSvcfeeInd(Indicator.N);
//		acct.setUserNumber1(0);
//		acct.setUserNumber2(0);
//		acct.setUserNumber3(0);
//		acct.setUserNumber4(0);
//		acct.setUserNumber5(0);
//		acct.setUserNumber6(0);
//		acct.setUserAmt1(BigDecimal.ZERO);
//		acct.setUserAmt2(BigDecimal.ZERO);
//		acct.setUserAmt3(BigDecimal.ZERO);
//		acct.setUserAmt4(BigDecimal.ZERO);
//		acct.setUserAmt5(BigDecimal.ZERO);
//		acct.setUserAmt6(BigDecimal.ZERO);
//		acct.setSmsInd(SmsInd.Y);
//		em.persist(acct);
//		return acct;
//	}
//
//	/**
//	 * 产生账号授权表
//	 * @param accountList
//	 * @return
//	 */
//	private List<CcsAcctO> createAccountO(List<CcsAcct> accountList){
//		List<CcsAcctO> accountOList = new ArrayList<CcsAcctO>();
//		for (CcsAcct acct : accountList){
//			accountOList.add(createAccountO(acct));
//		}
//		return accountOList;
//	}
//	
//	/**
//	 * 产生单条账号授权表
//	 * @param acct
//	 * @return
//	 */
//	private CcsAcctO createAccountO(CcsAcct acct){
//		CcsAcctO acctO = new CcsAcctO();
//		acctO.setOrg(OrganizationContextHolder.getCurrentOrg());
//		acctO.setAcctNbr(acct.getAcctNbr());
//		acctO.setAcctType(acct.getAcctType());
//		acctO.setCycleDay(acct.getCycleDay());
//		acctO.setBlockCode(acct.getBlockCode());
//		acctO.setCashBal(acct.getCashBal());
//		acctO.setCashLmtRate(acct.getCashLmtRate());
//		acctO.setCreditLmt(acct.getCreditLmt());
//		acctO.setCurrBal(acct.getCurrBal());
//		acctO.setCustId(acct.getCustId());
//		acctO.setCustLmtId(acct.getCustLmtId());
//		acctO.setDisputeAmt(acct.getDisputeAmt());
//		acctO.setLoanBal(acct.getLoanBal());
//		acctO.setLoanLmtRate(acct.getLoanLmtRate());
//		acctO.setOrg(acct.getOrg());
//		acctO.setOvrlmtRate(acct.getOvrlmtRate());
//		acctO.setOwningBranch(acct.getOwningBranch());
//		acctO.setProductCd(acct.getProductCd());
//		acctO.setTempLmt(acct.getTempLmt());
//		acctO.setTempLmtBegDate(acct.getTempLmtBegDate());
//		acctO.setTempLmtEndDate(acct.getTempLmtEndDate());
//		acctO.setMemoCash(acct.getMemoCash());
//		acctO.setMemoCr(acct.getMemoCr());
//		acctO.setMemoDb(acct.getMemoDb());
//		acctO.setSmsInd(SmsInd.N);
//		em.persist(acctO);
//		return acctO;
//	}
//	/**
//	 * 生成入账历史
//	 * @param account
//	 * @return
//	 */
//	public  CcsTxnHst createCcsTxnHst(int txnSeq, CcsAcct account)
//	{
//	CcsTxnHst hst=new CcsTxnHst();
//	hst.setOrg(account.getOrg());
//	hst.setTxnSeq(txnSeq);
//	hst.setAcctNbr(account.getAcctNbr());
//	hst.setAcctType(account.getAcctType());
//	hst.setCardNo(account.getDefaultLogicCardNbr());
//	hst.setLogicCardNbr(account.getDefaultLogicCardNbr());
//	hst.setBscLogiccardNbr(account.getDefaultLogicCardNbr());
//	hst.setProductCd(account.getProductCd());
//	hst.setTxnDate(account.getNextStmtDate());
//	hst.setTxnTime(account.getNextStmtDate());
//	hst.setPostTxnType(PostTxnType.M);
//	hst.setTxnCode("0102");
//	hst.setDbCrInd(DbCrInd.C);
//	hst.setTxnAmt(BigDecimal.valueOf(1000.00));
//	hst.setPostAmt(BigDecimal.valueOf(1000.00));
//	hst.setPostDate(account.getNextStmtDate());
//	hst.setAuthCode("456");
//	hst.setCardBlockCode("Z");
//	hst.setTxnCurrency("156");
//	hst.setPostCurrCd(account.getCurrCd());
//	hst.setOrigTransDate(account.getNextStmtDate());
//	hst.setPlanNbr("456");
//	hst.setRefNbr("3344");
//	hst.setTxnDesc("success");
//	hst.setTxnShortDesc("success");
//	hst.setPoint(BigDecimal.valueOf(10));
//	hst.setPostingFlag(PostingFlag.F00);
//	hst.setPrePostingFlag(PostingFlag.F00);
//	hst.setRelPmtAmt(BigDecimal.valueOf(0));
//	hst.setOrigPmtAmt(BigDecimal.valueOf(0));
//	hst.setAcqBranchIq("00005");
//	hst.setAcqTerminalId("00005");
//	hst.setAcqAcceptorId("00005");
//	hst.setAcqAddress(account.getStmtAddress());
//	hst.setMcc("34");
//	hst.setInputTxnCode("2245");
//	hst.setInputTxnAmt(BigDecimal.valueOf(0));
//	hst.setInputSettAmt(BigDecimal.valueOf(0));
//	hst.setInterchangeFee(BigDecimal.valueOf(0));
//	hst.setFeePayout(BigDecimal.valueOf(0));
//	hst.setFeeProfit(BigDecimal.valueOf(0));
//	hst.setLoanIssueProfit(BigDecimal.valueOf(0));
//	hst.setStmtDate(account.getNextStmtDate());
//	hst.setVoucherNo("897");
//	hst.setJpaVersion(1);
//	em.persist(hst);
//	return hst;
//	}
//	/**
//	 * 
//	 * 当日入账交易表
//	 *//*
//	public TtTxnPost createTttxnPost(int txnSeq,CcsAcct account)
//	{
//		TtTxnPost ttTxnPost =new TtTxnPost();
//		ttTxnPost.setOrg(account.getOrg());
//		ttTxnPost.setTxnSeq(txnSeq);
//		ttTxnPost.setAcctNbr(account.getAcctNbr());
//		ttTxnPost.setAcctType(account.getAcctType());
//		ttTxnPost.setCardNo(account.getDefaultLogicCardNbr());
//		ttTxnPost.setLogicCardNbr(account.getDefaultLogicCardNbr());
//		ttTxnPost.setBscLogiccardNbr(account.getDefaultLogicCardNbr());
//		ttTxnPost.setProductCd(account.getProductCd());
//		ttTxnPost.setTxnDate(account.getNextStmtDate());
//		ttTxnPost.setTxnTime(account.getNextStmtDate());
//		ttTxnPost.setPostTxnType(PostTxnType.M);
//		ttTxnPost.setTxnCode("0102");
//		ttTxnPost.setDbCrInd(DbCrInd.M);
//		ttTxnPost.setTxnAmt(BigDecimal.valueOf(1000));
//		ttTxnPost.setPostAmt(BigDecimal.valueOf(1000));
//		ttTxnPost.setPostDate(account.getNextStmtDate());
//		ttTxnPost.setAuthCode("123");
//		ttTxnPost.setCardBlockCode("Z");
//		ttTxnPost.setTxnCurrency("156");
//		ttTxnPost.setPostCurrCd("156");
//		ttTxnPost.setOrigTransDate(account.getLastStmtDate());
//		ttTxnPost.setPlanNbr("123");
//		ttTxnPost.setRefNbr("1234");
//		ttTxnPost.setTxnDesc("当日交易入账");
//		ttTxnPost.setTxnShortDesc("当日交易入账");
//		ttTxnPost.setPoint(BigDecimal.valueOf(10));
//		ttTxnPost.setPostingFlag(PostingFlag.F04);
//		ttTxnPost.setRelPmtAmt(BigDecimal.valueOf(0));
//		ttTxnPost.setOrigPmtAmt(BigDecimal.valueOf(0));
//		ttTxnPost.setAcqBranchIq("1111");
//		ttTxnPost.setAcqTerminalId("1111");
//		ttTxnPost.setAcqAcceptorId("1111");
//		ttTxnPost.setAcqAddress("某地址");
//		ttTxnPost.setMcc("123");
//		ttTxnPost.setInputTxnCode("1212");
//		ttTxnPost.setInputTxnAmt(BigDecimal.valueOf(1000));
//		ttTxnPost.setInputSettAmt(BigDecimal.valueOf(1000));
//		ttTxnPost.setInterchangeFee(BigDecimal.valueOf(0));
//		ttTxnPost.setFeePayout(BigDecimal.valueOf(0));
//		ttTxnPost.setFeeProfit(BigDecimal.valueOf(0));
//		ttTxnPost.setLoanIssueProfit(BigDecimal.valueOf(0));
//		ttTxnPost.setStmtDate(account.getNextStmtDate());
//		ttTxnPost.setVoucherNo("1123");
//		ttTxnPost.setJpaVersion(12);
//		em.persist(ttTxnPost);
//		return ttTxnPost;
//	}*/
//	private Date getSetupDate()
//	{
//		return globalManagementService.getSystemStatus().getProcessDate();
//	}
}
