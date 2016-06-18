package com.sunline.ccs.batch.cc9100;


import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.batch.common.DateUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.server.repos.RCcsEmployee;
import com.sunline.ccs.infrastructure.server.repos.RCcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsEmployee;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.QCcsEmployee;
import com.sunline.ccs.infrastructure.shared.model.QCcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.EmpType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.Relationship;
import com.sunline.ppy.dictionary.enums.TitleOfTechnicalType;
import com.sunline.ppy.dictionary.report.ccs.T9002ToPBOCRptItem;

/**
 * @see 类名：P9103PBOC
 * @see 描述：人行征信报送文件
 *
 * @see 创建日期：   2015-6-24下午2:46:59
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P9103PBOC implements ItemProcessor<CcsAcct, T9002ToPBOCRptItem> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	//默认地址字段长度为60
	private static final int DEFAULT_LEN =60;
	
	@Autowired
	private U9101PBOCUtil u9101PBOC;
	
	/**
	 * 参数获取组件
	 */
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Autowired
	private RCcsCustomer rTmCustomer;
	@Autowired
	private RCcsCustomerCrlmt rTmCustLimitO;
	@Autowired
	private RCcsLinkman rTmContact;
	@Autowired
	private RCcsEmployee rTmEmployee;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	@Override
	public T9002ToPBOCRptItem process(CcsAcct acct) throws Exception {
		
		try
		{	
			if ("P".equals(acct.getBlockCode()) && isCloseAndOverOneMonth(acct, batchStatusFacility.getBatchDate())) {
				return null;
			}else{
				//T+1 报送账户3种 开户>销户>账单日
				T9002ToPBOCRptItem pboc = new T9002ToPBOCRptItem();
				OrganizationContextHolder.setCurrentOrg(acct.getOrg());
				Calendar defaultDate = Calendar.getInstance();
				
				// 账户属性参数
				ProductCredit productCredit = parameterFacility.loadParameter(acct.getProductCd(), ProductCredit.class);
				AccountAttribute acctAttr = parameterFacility.loadParameter(productCredit.accountAttributeId, AccountAttribute.class);
				
				//获取客户
				CcsCustomer cust = rTmCustomer.findOne(acct.getCustId());
				
				//额度信息
				QCcsAcct qTmAccount = QCcsAcct.ccsAcct;
				JPAQuery custLimitQuery = new JPAQuery(em);
				CcsAcct account = custLimitQuery.from(qTmAccount).where(qTmAccount.custId.eq(acct.getCustId())).orderBy(qTmAccount.acctNbr.asc()).list(qTmAccount).get(0);
				BigDecimal custLimit = BigDecimal.ZERO;
				if(acct.getAcctNbr().equals(account.getAcctNbr()) && acct.getAcctType().equals(account.getAcctType())){
					QCcsCustomerCrlmt qTmCustLimitO = QCcsCustomerCrlmt.ccsCustomerCrlmt;
					custLimit = rTmCustLimitO.findOne(qTmCustLimitO.custLmtId.eq(account.getCustLmtId())).getCreditLmt();
				}
				
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
				
				//完整地址字段
				String acctAddress = "";
				String homeAddress = "";
				String compAddress = "";
				String regAddress = "";
				acctAddress=formatNull(acct.getStmtState())+formatNull(acct.getStmtCity())+formatNull(acct.getStmtDistrict())+formatNull(acct.getStmtAddress());
				if(homeAddr!=null){
					homeAddress=formatNull(homeAddr.getState())+formatNull(homeAddr.getCity())+formatNull(homeAddr.getDistrict())+formatNull(homeAddr.getAddress());
				}
				if(compAddr!=null){
					compAddress=formatNull(compAddr.getState())+formatNull(compAddr.getCity())+formatNull(compAddr.getDistrict())+formatNull(compAddr.getAddress());
				}
				if(regAddr!=null){
					regAddress=formatNull(regAddr.getState())+formatNull(regAddr.getCity())+formatNull(regAddr.getDistrict())+formatNull(regAddr.getAddress());
				}
				//联系人
				QCcsLinkman qTmContact = QCcsLinkman.ccsLinkman;
				CcsLinkman consortContact = rTmContact.findOne(qTmContact.org.eq(acct.getOrg()).and(qTmContact.custId.eq(cust.getCustId())).and(qTmContact.relationship.eq(Relationship.C)));
				
				//工作
				QCcsEmployee qTmEmployee = QCcsEmployee.ccsEmployee;
				CcsEmployee employee = rTmEmployee.findOne(qTmEmployee.org.eq(acct.getOrg()).and(qTmEmployee.custId.eq(acct.getCustId())));
				
				//卡片
				QCcsCard qTmCard = QCcsCard.ccsCard;
				JPAQuery cardQuery = new JPAQuery(em);
				List<CcsCard> cardList = cardQuery.from(qTmCard).where(qTmCard.acctNbr.eq(acct.getAcctNbr())).list(qTmCard);
				boolean isActivate = false;
				for(CcsCard card : cardList){
					if(card.getActiveInd() == Indicator.Y){
						isActivate = true;
						break;
					}
				}
				
				//账单
				//因为生产人行征信文件时，已生产账单。
				//当期实际还款额取值为上一期还款金额。
				//所以对应的当期应还款额，确实为上上期的最小还款额。 by dingxl
				QCcsStatement qTmStmtHst = QCcsStatement.ccsStatement;
				JPAQuery stmtQuery = new JPAQuery(em);
				List<CcsStatement> stmtList = stmtQuery.from(qTmStmtHst)
						.where(qTmStmtHst.acctNbr.eq(acct.getAcctNbr()).and(qTmStmtHst.acctType.eq(acct.getAcctType())))
						.orderBy(qTmStmtHst.stmtDate.desc())
						.list(qTmStmtHst);
				BigDecimal paymentAmt = BigDecimal.ZERO;
				BigDecimal totDueAmt = BigDecimal.ZERO;
				if(stmtList.size() != 0){
					CcsStatement stmt = stmtList.get(0);
					//获取该账期内账单分期贷方调整发生额
					BigDecimal sumAmtOfLoanB = BigDecimal.ZERO;
					sumAmtOfLoanB = u9101PBOC.getSumAmtOfLoanB(stmt);
					//实际还款金额=当期贷方发生额-账单分期贷方调整发生额(即冲销最小还款额的贷方金额)
					paymentAmt = stmt.getCtdAmtCr().subtract(sumAmtOfLoanB);
					if(stmt.getStmtCurrBal().compareTo(BigDecimal.ZERO) <0){
						paymentAmt = paymentAmt.add(stmt.getStmtCurrBal()); 
					}
				}
				if(stmtList.size() > 1){
					CcsStatement stmt1 = stmtList.get(1);
					//应还款金额需要减去宽限金额  mantis4472 by dingxl
					totDueAmt = stmt1.getTotDueAmt().subtract(acctAttr.delqTol);
					if (totDueAmt.compareTo(BigDecimal.ZERO)<0)
						totDueAmt = BigDecimal.ZERO;
				}
				
				//机构
				pboc.org = acct.getOrg();
				//处理余额
				BigDecimal pboc_dataItem04_1109;
				BigDecimal bigDecimal =acct.getCurrBal();
				if(bigDecimal.compareTo(BigDecimal.ZERO) < 0){
					pboc_dataItem04_1109 = BigDecimal.ZERO;
				}
				else{
					pboc_dataItem04_1109 = acct.getCurrBal();
				}
				
				//报文正文 - 基础段
				pboc.dataItem04_8103 = "0983";//账户记录长度; 基础段345+身份段371+职业段199+地址段68
				pboc.dataItem04_8105 = "A";//信息类别; A-基础段
				pboc.dataItem04_6101 = "";//金融机构代码(FTS填)
				pboc.dataItem04_7117 = "2";//业务种类; 2-信用卡
				pboc.dataItem04_7111 = "81";//业务种类细分; 81-贷记卡
				pboc.dataItem04_7101 = acct.getAcctNbr().toString();//业务号; 账号
				pboc.dataItem04_3141 = u9101PBOC.get3141(acct.getOwningBranch());//发生地点；OWNING_BRANCH转行政区划
				pboc.dataItem04_2101 = acct.getSetupDate();//开户日期
				pboc.dataItem04_2103 = u9101PBOC.get2103(defaultDate);//到期日期; 业务种类为信用卡时，用20991231填充
				pboc.dataItem04_1418 = u9101PBOC.get1418(acct.getAcctType().getCurrencyCode());//币种
				pboc.dataItem04_1101 = acct.getCreditLmt().intValue();//授信额度; 业务种类为信用卡时，此数据项是指信用额度 //不包括小数位
				pboc.dataItem04_1102 = custLimit.intValue();//共享授信额度;客户级额度，账户1为额度，账户2345为0 //不包括小数位
				pboc.dataItem04_1103 = acct.getLtdHighestBal();//最大负债额
				pboc.dataItem04_7115 = "4";//担保方式; 4-信用/免担保
				pboc.dataItem04_4111 = "C";//还款频率; 业务种类为信用卡时，用C填充
				pboc.dataItem04_4101 = "C";//还款月数; 业务种类为信用卡时，用C填充
				pboc.dataItem04_4105 = "C";//剩余还款月数; 业务种类为信用卡时用C填充
				pboc.dataItem04_2301 = u9101PBOC.getBatchDate();//结算/应还款日期 参照T+1规则，开户、关闭账户、账单日
				pboc.dataItem04_2107 = acct.getLastPmtDate()==null ? acct.getSetupDate() : acct.getLastPmtDate();//最近一次实际还款日期；自账户开立以来无还款历史的账户，其最近一次实际还款日期填报开户日期
				pboc.dataItem04_1105 = totDueAmt;//本月应还款金额；人行要求：贷记卡时，此数据元为上一结算周期计算出的最低还款额
				pboc.dataItem04_1107 = paymentAmt;//实际还款金额；上期还款金额(扣除溢缴款)
				pboc.dataItem04_1109 = pboc_dataItem04_1109;//余额
				pboc.dataItem04_4109 = (u9101PBOC.get1111(acct.getTotDueAmt(), acct.getCurrDueAmt(), acctAttr.delqTol, acct.getAgeCode())).toString()=="0"?"0":u9101PBOC.get4109(acct.getAgeCode());//当前逾期期数; 业务种类细分为贷记卡时，此数据项为当前连续未还最低还款额次数
				pboc.dataItem04_1111 = u9101PBOC.get1111(acct.getTotDueAmt(), acct.getCurrDueAmt(), acctAttr.delqTol, acct.getAgeCode());//当前逾期总额; 业务种类细分为贷记卡时，此数据项为当前连续未还最低还款额总额
				pboc.dataItem04_1113 = BigDecimal.ZERO;//逾期31-60天未归还贷款本金; 业务种类为信用卡时，用0填充
				pboc.dataItem04_1115 = BigDecimal.ZERO;//逾期61-90天未归还贷款本金; 业务种类为信用卡时，用0填充
				pboc.dataItem04_1117 = BigDecimal.ZERO;//逾期91-180天未归还贷款本金; 业务种类为信用卡时，用0填充
				pboc.dataItem04_1119 = BigDecimal.ZERO;//逾期180天以上未归还贷款本金; 业务种类为信用卡时，用0填充
				pboc.dataItem04_4312 = u9101PBOC.get4312(acct.getPaymentHst());//违约次数
				pboc.dataItem04_4107 = u9101PBOC.get4107(acct.getAgeHst());//最高逾期期数; 业务种类细分为贷记卡时，此数据项为12个月内连续未还最低还款额次数的最大值
				pboc.dataItem04_7105 = u9101PBOC.get7105(acct.getAgeCode());//五级分类状态;
				pboc.dataItem04_7109 = u9101PBOC.get7109(acct.getBlockCode(), isActivate);//账户状态
				pboc.dataItem04_7107 = u9101PBOC.get7107(acct.getAgeHst(), acct.getBlockCode(), totDueAmt, stmtList, acctAttr.delqTol);//24个月（账户）还款状态；
				pboc.dataItem04_1210 = BigDecimal.ZERO;//透支180天以上未付余额; 业务种类细分不为准贷记卡时，用0填充
				//dataItem04_7121 账户拥有者提示信息
				pboc.dataItem04_7121 = get7121(acct);//账户拥有者信息提示
				pboc.dataItem04_5101 = cust.getName();//姓名
				pboc.dataItem04_5107 = u9101PBOC.get5107(cust.getIdType());//证件类型
				pboc.dataItem04_5109 = cust.getIdNo();//证件号码
				pboc.dataItem04_8107 = null;//预留字段
	
				//报文正文 - 身份信息段
				pboc.dataItem05_8105 = "B";//信息类别; 如存在该段，则填“B”表示本信息段为身份信息段
				pboc.dataItem05_5105 = u9101PBOC.get5105(cust.getGender());//性别
				pboc.dataItem05_2408 = u9101PBOC.get2408(cust.getBirthday(), defaultDate);//出生日期; 无法填报时，统一填“19010101”
				pboc.dataItem05_5111 = u9101PBOC.get5111(cust.getMaritalStatus());//婚姻状况
				pboc.dataItem05_5113 = u9101PBOC.get5113(cust.getEducation());//最高学历
				pboc.dataItem05_5115 = u9101PBOC.get5115(cust.getEducation());//最高学位
				pboc.dataItem05_3115 = cust.getHomePhone();//住宅电话
				pboc.dataItem05_3117 = cust.getMobileNo();//手机号码
				pboc.dataItem05_3119 = compAddr == null ? "" : compAddr.getPhone();//单位电话
				pboc.dataItem05_3105 = cust.getEmail();//电子邮箱 
				pboc.dataItem05_3113 = u9101PBOC.substringByByte(acctAddress, DEFAULT_LEN);//通讯地址; 包含省市县（区）等详细信息。此处填报的是月结单地址
				pboc.dataItem05_3109 = acct.getStmtPostcode() == null ? "999999": acct.getStmtPostcode();//通讯地址邮政编码
				pboc.dataItem05_3101 = u9101PBOC.substringByByte(regAddress, DEFAULT_LEN);//户籍地址
				if(consortContact != null){
					pboc.dataItem05_5204 = consortContact.getName();//配偶姓名
					pboc.dataItem05_5208 = u9101PBOC.get5208(consortContact.getIdType());//配偶证件类型
					pboc.dataItem05_5210 = consortContact.getIdNo();//配偶证件号码
					pboc.dataItem05_5206 = consortContact.getCorpName();//配偶工作单位
					pboc.dataItem05_3111 = consortContact.getMobileNo();//配偶联系电话
				}
				
				//报文正文 - 职业信息段
				pboc.dataItem06_8105 = "C";//信息类别; 如存在该段，则填“C”表示本信息段为职业信息段
				pboc.dataItem06_5119 = u9101PBOC.get5119(cust.getOccupation());//职业
				pboc.dataItem06_5117 = cust.getCorpName();//单位名称
				pboc.dataItem06_6103 = u9101PBOC.get6103(employee == null ? EmpType.Z : employee.getCorpIndustryCategory());//单位所属行业
				pboc.dataItem06_3133 = u9101PBOC.substringByByte(compAddress, DEFAULT_LEN);//单位地址
				pboc.dataItem06_3129 = compAddr == null ? "" : compAddr.getPostcode();//单位地址邮政编码
				pboc.dataItem06_2109 = "";//本单位工作起始年份
				pboc.dataItem06_5121 = u9101PBOC.get5121(cust.getTitle());//职务
				pboc.dataItem06_5123 = u9101PBOC.get5123(employee == null ? TitleOfTechnicalType.D : employee.getCorpTechTitle());//职称
				pboc.dataItem06_5125 = employee == null ? BigDecimal.ZERO : employee.getIncomePy();//年收入
				pboc.dataItem06_7123 = "";//工资账号
				pboc.dataItem06_6105 = "";//工资账户开户银行
	
				//报文正文 - 居住地址段
				pboc.dataItem07_8105 = "D";//如存在该段，则填“D”表示本信息段为居住地址段
				pboc.dataItem07_3103 = homeAddr == null ? "暂缺" :u9101PBOC.substringByByte(homeAddress, DEFAULT_LEN);//居住地址
				pboc.dataItem07_3121 = homeAddr == null ? "999999" : homeAddr.getPostcode();//居住地址邮政编码
				pboc.dataItem07_5127 = u9101PBOC.get5127(cust.getHouseOwnership());//居住状况
				
				return pboc;
			}
		}catch (Exception e) {
			logger.error("人行征信报送文件生成异常, 账号{}, 账户类型{}", acct.getAcctNbr(), acct.getAcctType());
			throw e;
		}
	}
	/**
	 * @see 方法名：formatNull 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015-6-24下午2:47:31
	 * @author ChengChun
	 *  
	 * @param str
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private String formatNull(String str){
		return str == null ? "" : str;
	}
	/**
	 * @see 方法名：get7121 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015-6-24下午2:47:23
	 * @author ChengChun
	 *  
	 * @param acct
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private String get7121(CcsAcct acct){
		if(acct.getLastStmtDate() == null){
			return "2";
		}else{
			return "1";
		}
	}
	/**
	 * @see 方法名：isCloseAndOverOneMonth 
	 * @see 描述：TODO 方法描述
	 * @see 创建日期：2015-6-24下午2:47:15
	 * @author ChengChun
	 *  
	 * @param acct
	 * @param d
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private boolean isCloseAndOverOneMonth(CcsAcct acct,Date d){
		if (acct.getClosedDate() == null) {
			return false;
		}
		int i = DateUtils.getMonthInterval(acct.getClosedDate(), d);
		if (i >= 1) {
			return true;
		}
		return false;
	}
}
