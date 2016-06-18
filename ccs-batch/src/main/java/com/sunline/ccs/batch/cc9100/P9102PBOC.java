package com.sunline.ccs.batch.cc9100;


import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.MicroCreditRescheduleUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsEmployee;
import com.sunline.ccs.infrastructure.server.repos.RCcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsEmployee;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsEmployee;
import com.sunline.ccs.infrastructure.shared.model.QCcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.param.def.LoanFeeDef;
import com.sunline.ccs.param.def.enums.PaymentIntervalUnit;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.EmpType;
import com.sunline.ppy.dictionary.enums.Relationship;
import com.sunline.ppy.dictionary.enums.TitleOfTechnicalType;
import com.sunline.ppy.dictionary.report.ccs.T9002ToPBOCRptItem;

/**
 * @see 类名：P9102PBOC
 * @see 描述：人行征信报送文件-贷款
                                     贷款报送日期：
            1、开户日 open
            2、到期还款日 dueDate 已还清 close，否则不报此卡
            3、宽限日graceDate 已还清 close 否则为delay
            4、结清日paidOutDate close
 *
 * @see 创建日期：   2015-6-24下午2:44:28
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P9102PBOC implements ItemProcessor<S9102PBOC, T9002ToPBOCRptItem> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private U9101PBOCUtil util;
	
	@Autowired
	private RCcsCustomer rTmCustomer;
	@Autowired
	private RCcsLinkman rTmContact;
	@Autowired
	private RCcsEmployee rTmEmployee;
	@Autowired
	private RCcsAcct rTmAccount;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private LoanStepOpen open;
	@Autowired
	private LoanStepClose close;
	@Autowired
	private LoanStepDelay delay;
	@Autowired
	private LoanStepActive active;
	@Autowired
	private BatchStatusFacility batchFacility;
	@Autowired
	private MicroCreditRescheduleUtils microCreditUtils;
	@Override
	public T9002ToPBOCRptItem process(S9102PBOC item) throws Exception {
		CcsLoan loan = item.getTmLoan();
		LoanStep step = getLoanStep(item);
		if(step == null){ //非报送点 不做报送
			return null;
		}
		logger.debug("贷款报送，loanId={}，报送类型={}",loan.getLoanId(),step.getClass().getSimpleName());
		try
		{
			//T+1 报送账户3种 开户>到期还款日>结清日
			T9002ToPBOCRptItem pboc = new T9002ToPBOCRptItem();
			OrganizationContextHolder.setCurrentOrg(loan.getOrg());
			Calendar defaultDate = Calendar.getInstance();
			
			//获取客户
			CcsAcctKey key = new CcsAcctKey();
			key.setAcctNbr(loan.getAcctNbr());
			key.setAcctType(loan.getAcctType());
			CcsAcct acct = rTmAccount.findOne(key);
			CcsCustomer cust = rTmCustomer.findOne(acct.getCustId());
			
			//名址信息
			QCcsAddress qTmAddress = QCcsAddress.ccsAddress;
			JPAQuery addrQuery = new JPAQuery(em);
			List<CcsAddress> addrList = addrQuery.from(qTmAddress).where(qTmAddress.custId.eq(cust.getCustId())).list(qTmAddress);
			CcsAddress homeAddr = null;
			CcsAddress compAddr = null;
			CcsAddress regAddr = null;
			for(CcsAddress addr : addrList){
				if(addr.getAddrType() == AddressType.H){
					homeAddr = addr;
				}else if(addr.getAddrType() == AddressType.C){
					compAddr = addr;
				}else if(addr.getAddrType() == AddressType.S){
					regAddr = addr;
				}
			}
			
			//联系人
			QCcsLinkman qTmContact = QCcsLinkman.ccsLinkman;
			CcsLinkman consortContact = rTmContact.findOne(qTmContact.org.eq(acct.getOrg()).and(qTmContact.custId.eq(cust.getCustId())).and(qTmContact.relationship.eq(Relationship.C)));
			
			//工作
			QCcsEmployee qTmEmployee = QCcsEmployee.ccsEmployee;
			CcsEmployee employee = rTmEmployee.findOne(qTmEmployee.org.eq(acct.getOrg()).and(qTmEmployee.custId.eq(acct.getCustId())));
			
			//账单
			QCcsStatement qTmStmtHst = QCcsStatement.ccsStatement;
			JPAQuery stmtQuery = new JPAQuery(em);
			List<CcsStatement> stmtList = stmtQuery.from(qTmStmtHst)
					.where(qTmStmtHst.acctNbr.eq(acct.getAcctNbr()).and(qTmStmtHst.acctType.eq(acct.getAcctType())))
					.orderBy(qTmStmtHst.stmtDate.desc())
					.list(qTmStmtHst);
			BigDecimal paymentAmt = BigDecimal.ZERO;
			if(stmtList.size() != 0){
				CcsStatement stmt = stmtList.get(0);
				paymentAmt = stmt.getCtdRepayAmt();
				if(stmt.getStmtCurrBal().compareTo(BigDecimal.ZERO) <0){
					paymentAmt = paymentAmt.add(stmt.getStmtCurrBal()); 
				}
			}
			
			//机构
			pboc.org = acct.getOrg();
			
			//报文正文 - 基础段
			pboc.dataItem04_8103 = "0983";//账户记录长度; 基础段345+身份段371+职业段199+地址段68
			pboc.dataItem04_8105 = "A";//信息类别; A-基础段
			pboc.dataItem04_6101 = "";//金融机构代码(FTS填)
			pboc.dataItem04_7117 = "1";//业务种类; 1-贷款
			pboc.dataItem04_7111 = "99";//业务种类细分; 99-其它
			pboc.dataItem04_7101 =loan.getLoanId().toString();//业务号;分期流水号
			pboc.dataItem04_3141 = util.get3141(acct.getOwningBranch());//发生地点；OWNING_BRANCH转行政区划
			pboc.dataItem04_2101 = loan.getRegisterDate();//开户日期
			pboc.dataItem04_2103 = loan.getLoanExpireDate();// 到期日期; 业务种类为信用卡时，用20991231填充
			pboc.dataItem04_1418 = util.get1418(acct.getAcctType().getCurrencyCode());//币种
			pboc.dataItem04_1101 = loan.getLoanInitPrin().intValue();//分期总本金
			pboc.dataItem04_1102 = loan.getLoanInitPrin().intValue();//这里填写分期本金
			pboc.dataItem04_1103 = loan.getLoanInitPrin();//最大负债额
			pboc.dataItem04_7115 = "4";//担保方式; 4-信用/免担保
			
			PaymentIntervalUnit interval = getFrequence(loan);
			pboc.dataItem04_4111 = util.get4111(interval);//还款频率; 一次性
			pboc.dataItem04_4101 = util.get4101(interval,loan);//还款月数;还款频率为“一次性”，填写字母
			pboc.dataItem04_4105 = util.get4105(interval,loan);//剩余还款月数;还款频率为“一次性”，填写字母O
			
			step.setPboc(item, pboc);//针对不同报送时点，设置不同的值
			
			pboc.dataItem04_5101 = cust.getName();//姓名
			pboc.dataItem04_5107 = util.get5107(cust.getIdType());//证件类型
			pboc.dataItem04_5109 = cust.getIdNo();//证件号码
			pboc.dataItem04_8107 = null;//预留字段

			//报文正文 - 身份信息段
			pboc.dataItem05_8105 = "B";//信息类别; 如存在该段，则填“B”表示本信息段为身份信息段
			pboc.dataItem05_5105 = util.get5105(cust.getGender());//性别
			pboc.dataItem05_2408 = util.get2408(cust.getBirthday(), defaultDate);//出生日期; 无法填报时，统一填“19010101”
			pboc.dataItem05_5111 = util.get5111(cust.getMaritalStatus());//婚姻状况
			pboc.dataItem05_5113 = util.get5113(cust.getEducation());//最高学历
			pboc.dataItem05_5115 = util.get5115(cust.getEducation());//最高学位
			pboc.dataItem05_3115 = cust.getHomePhone();//住宅电话
			pboc.dataItem05_3117 = cust.getMobileNo();//手机号码
			pboc.dataItem05_3119 = compAddr == null ? "" : compAddr.getPhone();//单位电话
			pboc.dataItem05_3105 = cust.getEmail();//电子邮箱 
			pboc.dataItem05_3113 = acct.getStmtState()+acct.getStmtCity()+acct.getStmtDistrict()+acct.getStmtAddress();//通讯地址; 包含省市县（区）等详细信息。此处填报的是月结单地址
			pboc.dataItem05_3109 = acct.getStmtPostcode();//通讯地址邮政编码
			pboc.dataItem05_3101 = regAddr == null ? "" : regAddr.getState()+regAddr.getCity()+regAddr.getDistrict()+regAddr.getAddress();//户籍地址
			if(consortContact != null){
				pboc.dataItem05_5204 = consortContact.getName();//配偶姓名
				pboc.dataItem05_5208 = util.get5208(consortContact.getIdType());//配偶证件类型
				pboc.dataItem05_5210 = consortContact.getIdNo();//配偶证件号码
				pboc.dataItem05_5206 = consortContact.getCorpName();//配偶工作单位
				pboc.dataItem05_3111 = consortContact.getMobileNo();//配偶联系电话
			}
			
			//报文正文 - 职业信息段
			pboc.dataItem06_8105 = "C";//信息类别; 如存在该段，则填“C”表示本信息段为职业信息段
			pboc.dataItem06_5119 = util.get5119(cust.getOccupation());//职业
			pboc.dataItem06_5117 = cust.getCorpName();//单位名称
			pboc.dataItem06_6103 = util.get6103(employee == null ? EmpType.Z : employee.getCorpIndustryCategory());//单位所属行业
			pboc.dataItem06_3133 = compAddr == null ? "" : compAddr.getState()+compAddr.getCity()+compAddr.getDistrict()+compAddr.getAddress();//单位地址
			pboc.dataItem06_3129 = compAddr == null ? "" : compAddr.getPostcode();//单位地址邮政编码
			pboc.dataItem06_2109 = "";//本单位工作起始年份
			pboc.dataItem06_5121 = util.get5121(cust.getTitle());//职务
			pboc.dataItem06_5123 = util.get5123(employee == null ? TitleOfTechnicalType.D : employee.getCorpTechTitle());//职称
			pboc.dataItem06_5125 = employee == null ? BigDecimal.ZERO : employee.getIncomePy();//年收入
			pboc.dataItem06_7123 = "";//工资账号
			pboc.dataItem06_6105 = "";//工资账户开户银行

			//报文正文 - 居住地址段
			pboc.dataItem07_8105 = "D";//如存在该段，则填“D”表示本信息段为居住地址段
			pboc.dataItem07_3103 = homeAddr == null ? "暂缺" : homeAddr.getState()+homeAddr.getCity()+homeAddr.getDistrict()+homeAddr.getAddress();//居住地址
			pboc.dataItem07_3121 = homeAddr == null ? "999999" : homeAddr.getPostcode();//居住地址邮政编码
			pboc.dataItem07_5127 = util.get5127(cust.getHouseOwnership());//居住状况
			
			//特殊交易段
			if(step instanceof LoanStepActive && loan.getLastActionDate() != null){
				Date actDate = DateUtils.addMonths(loan.getLastActionDate(), 1);
				if(DateUtils.truncatedCompareTo(actDate, batchFacility.getBatchDate(), Calendar.DATE)>0){
					pboc.dataItem04_8103 = "1207";//983+224
					pboc.dataItem10_8105 = "G";
					pboc.dataItem10_7113 = get7113(loan);
					pboc.dataItem10_1416 = loan.getAdvPmtAmt();
					pboc.dataItem10_2410 = loan.getLastActionDate();
					pboc.dataItem10_4418 = String.valueOf(loan.getBefExtendInitTerm()-loan.getLoanInitTerm());
				}
			}
			
			return pboc;
		}catch (Exception e) {
			logger.error("人行征信报送文件生成异常, 记录ID={}", loan.getLoanId());
			logger.error("人行征信报送文件生成异常", e);
			throw e;
		}
	}
	/**
	 * @see 方法名：get7113 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015-6-24下午2:45:13
	 * @author ChengChun
	 *  
	 * @param loan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private String get7113(CcsLoan loan) {
		switch(loan.getLoanStatus()){
		case R:return "1";
		case S:return "4";
		case T:return "5";
		default: return null;
		}
	}
	/**
	 * @see 方法名：getFrequence 
	 * @see 描述：获取还款间隔单位
	 * @see 创建日期：2015-6-24下午2:45:29
	 * @author ChengChun
	 *  
	 * @param loan
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private PaymentIntervalUnit getFrequence(CcsLoan loan) {
		LoanFeeDef fee = microCreditUtils.getLoanFeeDef(loan.getLoanCode(), loan.getLoanInitTerm(), 
				loan.getLoanInitPrin(),loan.getLoanFeeDefId());
		return fee.paymentIntervalUnit;
	}
	
	/**
	 * @see 方法名：getLoanStep 
	 * @see 描述：根据tm_loan记录的状态，返回不同的loanStep
	 * @see 创建日期：2015-6-24下午2:45:46
	 * @author ChengChun
	 *  
	 * @param item
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private LoanStep getLoanStep(S9102PBOC item) {
		CcsLoan loan = item.getTmLoan();
		if(loan == null){
			return null;
		}
		if(batchFacility.shouldProcess(loan.getRegisterDate())){
			return open;	//开立日
		}
		if(loan.getPaidOutDate() != null){
			if(batchFacility.shouldProcess(loan.getPaidOutDate())){
				return close; //结清
			}
		}
		if(loan.getTerminalDate() != null){
			if(batchFacility.shouldProcess(loan.getTerminalDate())){
				return close; //提前结束
			}
		}
		if(loan.getLoanExpireDate()!=null && batchFacility.shouldProcess(loan.getLoanExpireDate())){
			if(loan.getPaidOutDate() == null){ //到期还款日未还款，认为是拖欠
				return delay;
			}else if(batchFacility.shouldProcess(loan.getPaidOutDate())){//如果还清日在批量之前，说明在paidOutDate已经报送过了
				return close; //否则认为是结清
			}
		}
		
		if(loan.getLoanExpireDate()!=null && org.apache.commons.lang.time.DateUtils.truncatedCompareTo(util.getBatchDate(), loan.getLoanExpireDate(), Calendar.DATE)>=0){
			//对于未结清的贷款，每个月底报送
			if(loan.getPaidOutDate() == null && util.isMonthEnd(util.getBatchDate())){
				return delay;
			}
		}else{
			if(loan.getPaidOutDate() == null){//未到期贷款且未还清的,宽限日报送
			
				CcsRepaySchedule s = null;
				for(CcsRepaySchedule t : item.getSchedules()){
					if(t.getCurrTerm().intValue() == loan.getCurrTerm().intValue()){
						s = t;
						break;
					}
				}
				if(s != null){
					Date graceDay = s.getLoanGraceDate();
					if(batchFacility.shouldProcess(graceDay)){
						return active;
					}
				}
			}
		}
		//其它时间点不报送
		return null;
	}
}
