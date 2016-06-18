package com.sunline.ccs.service.msentity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.CorpStructure;
import com.sunline.ppy.dictionary.enums.EducationType;
import com.sunline.ppy.dictionary.enums.EmpPositionAttrType;
import com.sunline.ppy.dictionary.enums.EmpType;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.HouseOwnership;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.IncomeFlag;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.MaritalStatus;
import com.sunline.ppy.dictionary.enums.Relationship;
import com.sunline.ppy.dictionary.enums.TitleOfTechnicalType;

/**
 * 循环现金贷开户请求接口
 * @author wangz
 *
 */
@SuppressWarnings("serial")
public class TFNAAcctSetupCycleReq extends MsRequestInfo {
	/**
	 * 申请单编号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="APPLY_NO")
	public String applyNo;
	/**
	 * 姓名
	 */
	@Check(lengths=40,notEmpty=true)
	@JsonProperty(value="CUSTOMER_NAME")
	public String  customername;
	/**
	 * 唯一客户号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="UUID")
	public String uuid;
	/**
	 * 性别
	 */
	@Check(lengths=1,notEmpty=true,fixed=true)
	@JsonProperty(value="SEX")
	public Gender gender;
	/**
	 * 出生日期
	 * YYYYMMDD
	 */
	@Check(lengths=8,notEmpty=true,fixed=true)
	@JsonProperty(value="BIRTHDAY")
	public Date birthday;
	/**
	 * 婚姻状况 
	 */
	@Check(lengths=4,notEmpty=false)
	@JsonProperty(value="MARRIAGE")
	public MaritalStatus maritalStatus;
	/**
	 * 国籍
	 */
	@Check(lengths=3,notEmpty=true,fixed=true)
	@JsonProperty(value="COUNTRY")
	public String country;
	/**
	 * 证件类型 
	 */
	@Check(lengths=5,notEmpty=true)
	@JsonProperty(value="CERT_TYPE")
	public IdType idType;
	/**
	 * 证件号码
	 */
	@Check(lengths=18,notEmpty=true,fixed=true)
	@JsonProperty(value="CERT_ID")
	public String idNo;
	/**
	 * 证件有效期
	 * YYYYMMDD
	 */
	@Check(lengths=8,fixed=true)
	@JsonProperty(value="DUE_DATE")
	public Date duedate;
	/**
	 * 移动电话
	 */
	@Check(lengths=11,notEmpty=true,fixed=true,isNumber=true)
	@JsonProperty(value="MOBILE")
	public String cellPhone;
	/**
	 * 邮政编码
	 */
	@Check(lengths=6,notEmpty=false,fixed=true,isNumber=true)
	@JsonProperty(value="ZIP_CODE")
	public String zipcode;
	/**
	 * 家庭地址（省）
	 */
	@Check(lengths=20,notEmpty=true)
	@JsonProperty(value="PROVINCE")
	public String province;
	/**
	 * 家庭地址（市）
	 */
	@Check(lengths=20,notEmpty=true)
	@JsonProperty(value="CITY")
	public String city;
	/**
	 * 家庭地址（区）
	 */
	@Check(lengths=20,notEmpty=true)
	@JsonProperty(value="AREA_CODE")
	public String areacode;
	/**
	 * 家庭地址（街道）
	 */
	@Check(lengths=80,notEmpty=true)
	@JsonProperty(value="FAMILY_ADDR")
	public String familyaddr;
	/**
	 * 住宅性质 
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="FAMILY_STATUS")
	public HouseOwnership familystatus;
	/**
	 * 住宅电话
	 */
	@Check(lengths=20,notEmpty=false)
	@JsonProperty(value="FAMILY_TEL")
	public String familytel;
	/**
	 * 最高学历
	 */
	@Check(lengths=4,notEmpty=false)
	@JsonProperty(value="EDU_EXPERIENCE")
	public EducationType eduExperience;
	/**
	 * 最高学位
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="EDU_DEGREE")
	public String eduDegree;
	/**
	 * 通讯地址 类型
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="COMM_ADDR")
	public AddressType commAddr;
	/**
	 * 单位地址
	 */
	@Check(lengths=80,notEmpty=false)
	@JsonProperty(value="WORK_ADDR")
	public String workAddr;
	/**
	 * 工作单位
	 */
	@Check(lengths=80,notEmpty=false)
	@JsonProperty(value="WORK_CORP")
	public String workCorp;
	/**
	 * 单位邮编
	 */
	@Check(lengths=6,notEmpty=false,fixed=true,isNumber=true)
	@JsonProperty(value="WORK_ZIP")
	public String workZip;
	/**
	 * 单位电话
	 */
	@Check(lengths=20,notEmpty=false)
	@JsonProperty(value="WORK_TEL")
	public String workTel;
	/**
	 * 本单位工作时间
	 */
	@Check(lengths=4,notEmpty=false)
	@JsonProperty(value="WORK_YEARS")
	public String workbegindate;
	/**
	 * 职位 
	 */
	@Check(lengths=2,notEmpty=false)
	@JsonProperty(value="POSITION_LEVEL")
	public EmpPositionAttrType positionLevel;
	/**
	 * 职称 
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="POSITION")
	public TitleOfTechnicalType position;
	/**
	 * 单位性质
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="WORKNATURE")
	public CorpStructure worknature;
	/**
	 * 行业 
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="UNITKIND")
	public EmpType unitkind;
	/**
	 * 紧急联系人1
	 */
	@Check(lengths=32)
	@JsonProperty(value="LINK1")
	public String link1;
	/**
	 * 紧急联系人1关系
	 */
	@Check(lengths=1)
	@JsonProperty(value="LINKRELATION1")
	public Relationship linkrelation1;
	/**
	 * 紧急联系人1联系移动电话
	 */
	@Check(lengths=20)
	@JsonProperty(value="LINKMOBILE1")
	public String linkmobile1;
	/**
	 * 紧急联系人2
	 */
	@Check(lengths=32,notEmpty=false)
	@JsonProperty(value="LINK2")
	public String link2;
	/**
	 * 紧急联系人2关系
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="LINKRELATION2")
	public Relationship linkrelation2;
	/**
	 * 紧急联系人2联系移动电话
	 */
	@Check(lengths=20,notEmpty=false)
	@JsonProperty(value="LINKMOBILE2")
	public String linkmobile2;
	/**
	 * 申请日期
	 */
	@Check(lengths=8,fixed=true)
	@JsonProperty(value="APPLY_DATE")
	public Date applyDate;
	
	/**
	 * 授信额度 
	 */
	@Check(lengths=18,notEmpty=true,regular="^(([1-9]\\d*)|0)$")
	@JsonProperty(value="CREDIT_LIMIT")
	public BigDecimal loanAmt;

	/**
	 * 合同到期日期
	 */
	@Check(lengths=8,fixed=true)
	@JsonProperty(value="CONTRA_EXPIRE_DATE")
	public Date contraExpireDate;

	/**
	 * 贷款种类
	 */
	@Check(lengths=32)
	@JsonProperty(value="BUSINESS_TYPE")
	public String businessType;
	/**
	 * 贷款产品代码
	 */
	@Check(lengths=4,notEmpty=true,fixed=true)
	@JsonProperty(value="LOAN_CODE")
	public String loanCode;
	/**
	 * 个人收入标识 
	 */	
	@Check(lengths=3,notEmpty=false)
	@JsonProperty(value="INCOME_FLAG")
	public IncomeFlag incomeFlag;
	/**
	 * 个人月收入
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="MONTHLY_WAGES")
	public BigDecimal monthlyWages;
	/**
	 * 放款/还款卡号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANK_CARD_NBR")
	public String putpaycardid;
	/**
	 * 银行卡开户人 名称
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANK_CARD_NAME")
	public String bankcardowner;
	/**
	 * 开户行名称
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANK_NAME")
	public String bankname;
	/**
	 * 开户行code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="BANK_CODE")
	public String bankcode;
	/**
	 * 开户行省
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANK_PROVINCE")
	public String bankprovince;
	/**
	 * 开户行省code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="BANK_PROV_CODE")
	public String bankprovincecode;
	/**
	 * 开户行市
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANK_CITY")
	public String bankcity;
	/**
	 * 开户行市code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="BANK_CITY_CODE")
	public String bankcitycode;
	/**
	 * 协议利率--下面的基础利率代替
	 */
//	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,6})?$")
//	@JsonProperty(value="AGREE_RATE")
//	public BigDecimal agreeRate;
	/**
	 * 协议利率有效期
	 * yyyyMMdd
	 */
	@Check(lengths=8,notEmpty=false,fixed=true)
	@JsonProperty(value="AGREE_RATE_EXPIRE_DATE")
	public Date agreeRateExpireDate;
	
	/**
	 * 合作方id
	 * yyyyMMdd
	 */
	@Check(lengths=8,notEmpty=true,fixed=true)
	@JsonProperty(value="COOPERATOR_ID")
	public String cooperatorId;
	
	/**
	 * 是否使用协议费率
	 * 
	 */
	@Check(lengths=1,notEmpty=true,fixed=true)
	@JsonProperty(value="AGREEMENT_RATE_IND")
	public Indicator agreeRateInd;
	
	/**
	 * 分期手续费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="INSTALLMENT_FEE_RATE")
	public BigDecimal feeRate;
	
	/**
	 * 分期手续费固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="INSTALLMENT_FEE_AMT")
	public BigDecimal feeAmount;
	

	/**
	 * 贷款服务费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="FEE_RATE")
	public BigDecimal installmentFeeRate;
	
	/**
	 * 贷款服务费固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="FEE_AMOUNT")
	public BigDecimal installmentFeeAmt;
	
	/**
	 * 寿险费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="LIFE_INSU_FEE_RATE")
	public BigDecimal lifeInsuFeeRate;
	
	/**
	 * 寿险固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="LIFE_INSU_FEE_AMT")
	public BigDecimal lifeInsuFeeAmt;
	
	/**
	 * 保费月费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="INS_RATE")
	public BigDecimal insRate;
	
	/**
	 * 保费月固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="INS_AMT")
	public BigDecimal insAmt;
	

	
	/**
	 * 提前还款包费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="PREPAYMENT_FEE_AMOUNT_RATE")
	public BigDecimal prepaymentFeeRate;
	
	/**
	 * 提前还款包固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
	@JsonProperty(value="PREPAYMENT_FEE_AMOUNT")
	public BigDecimal prepaymentFeeAmt;
	
	/**
	 * 罚息利率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="PENALTY_RATE")
	public BigDecimal penaltyRate;
	
	/**
	 * 复利利率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="COMPOUND_RATE")
	public BigDecimal compoundRate;
	
	/**
	 * 基础利率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="INTEREST_RATE")
	public BigDecimal interestRate;
	
	/**
	 * 税费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
//	@JsonProperty(value="STAMPDUTY_RATE")
	@JsonIgnore
	public BigDecimal stampRate;
	
	/**
	 * 印花税固定金额
	 * 
	 */
	@Check(lengths=18,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,2})?$")
//	@JsonProperty(value="STAMP_AMT")
	@JsonIgnore
	public BigDecimal stampAmt;
	
	/**
	 * 贷款子产品代码
	 * 
	 */
	@Check(lengths=8,notEmpty=false,isNumber=true)
	@JsonProperty(value="LOAN_FEE_DEF_ID")
	public String loanFeeDefId;
	
	/**
	 * 贷款用途
	 */
	@Check(lengths=4,notEmpty=false)
	@JsonProperty(value="PURPOSE")
	public String purpose;
	
	/**
	 * 是否加入寿险计划
	 */
	@Check(lengths=1,notEmpty=false)
	@JsonProperty(value="JOIN_LIFE_INSU_IND")
	public Indicator jionLifeInsuInd;
	
	/**
	 * 代收服务费率
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="AGENT_FEE_RATE")
	public BigDecimal agentFeeRate;
	
	/**
	 * 代收服务费固定金额
	 * 
	 */
	@Check(lengths=12,notEmpty=false,regular="^(([1-9]\\d*)|0)(\\.(\\d){1,8})?$")
	@JsonProperty(value="AGENT_FEE_AMOUNT")
	public BigDecimal agentFeeAmount;
	

	public String getCooperatorId() {
		return cooperatorId;
	}
	public void setCooperatorId(String cooperatorId) {
		this.cooperatorId = cooperatorId;
	}
	public String getApplyNo() {
		return applyNo;
	}
	public void setApplyNo(String applyNo) {
		this.applyNo = applyNo;
	}
	public String getCustomername() {
		return customername;
	}
	public void setCustomername(String customername) {
		this.customername = customername;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public MaritalStatus getMaritalStatus() {
		return maritalStatus;
	}
	public void setMaritalStatus(MaritalStatus maritalStatus) {
		this.maritalStatus = maritalStatus;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public IdType getIdType() {
		return idType;
	}
	public void setIdType(IdType idType) {
		this.idType = idType;
	}
	public String getIdNo() {
		return idNo;
	}
	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}
	public Date getDuedate() {
		return duedate;
	}
	public void setDuedate(Date duedate) {
		this.duedate = duedate;
	}
	public String getCellPhone() {
		return cellPhone;
	}
	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getAreacode() {
		return areacode;
	}
	public void setAreacode(String areacode) {
		this.areacode = areacode;
	}
	public String getFamilyaddr() {
		return familyaddr;
	}
	public void setFamilyaddr(String familyaddr) {
		this.familyaddr = familyaddr;
	}
	public HouseOwnership getFamilystatus() {
		return familystatus;
	}
	public void setFamilystatus(HouseOwnership familystatus) {
		this.familystatus = familystatus;
	}
	public String getFamilytel() {
		return familytel;
	}
	public void setFamilytel(String familytel) {
		this.familytel = familytel;
	}
	public EducationType getEduExperience() {
		return eduExperience;
	}
	public void setEduExperience(EducationType eduExperience) {
		this.eduExperience = eduExperience;
	}
	public String getEduDegree() {
		return eduDegree;
	}
	public void setEduDegree(String eduDegree) {
		this.eduDegree = eduDegree;
	}
	public AddressType getCommAddr() {
		return commAddr;
	}
	public void setCommAddr(AddressType commAddr) {
		this.commAddr = commAddr;
	}
	public String getWorkAddr() {
		return workAddr;
	}
	public void setWorkAddr(String workAddr) {
		this.workAddr = workAddr;
	}
	public String getWorkCorp() {
		return workCorp;
	}
	public void setWorkCorp(String workCorp) {
		this.workCorp = workCorp;
	}
	public String getWorkZip() {
		return workZip;
	}
	public void setWorkZip(String workZip) {
		this.workZip = workZip;
	}
	public String getWorkTel() {
		return workTel;
	}
	public void setWorkTel(String workTel) {
		this.workTel = workTel;
	}
	public String getWorkbegindate() {
		return workbegindate;
	}
	public void setWorkbegindate(String workbegindate) {
		this.workbegindate = workbegindate;
	}
	public EmpPositionAttrType getPositionLevel() {
		return positionLevel;
	}
	public void setPositionLevel(EmpPositionAttrType positionLevel) {
		this.positionLevel = positionLevel;
	}
	public TitleOfTechnicalType getPosition() {
		return position;
	}
	public void setPosition(TitleOfTechnicalType position) {
		this.position = position;
	}
	public CorpStructure getWorknature() {
		return worknature;
	}
	public void setWorknature(CorpStructure worknature) {
		this.worknature = worknature;
	}
	public EmpType getUnitkind() {
		return unitkind;
	}
	public void setUnitkind(EmpType unitkind) {
		this.unitkind = unitkind;
	}
	public String getLink1() {
		return link1;
	}
	public void setLink1(String link1) {
		this.link1 = link1;
	}
	public Relationship getLinkrelation1() {
		return linkrelation1;
	}
	public void setLinkrelation1(Relationship linkrelation1) {
		this.linkrelation1 = linkrelation1;
	}
	public String getLinkmobile1() {
		return linkmobile1;
	}
	public void setLinkmobile1(String linkmobile1) {
		this.linkmobile1 = linkmobile1;
	}
	public String getLink2() {
		return link2;
	}
	public void setLink2(String link2) {
		this.link2 = link2;
	}
	public Relationship getLinkrelation2() {
		return linkrelation2;
	}
	public void setLinkrelation2(Relationship linkrelation2) {
		this.linkrelation2 = linkrelation2;
	}
	public String getLinkmobile2() {
		return linkmobile2;
	}
	public void setLinkmobile2(String linkmobile2) {
		this.linkmobile2 = linkmobile2;
	}
	public Date getApplyDate() {
		return applyDate;
	}
	public void setApplyDate(Date applyDate) {
		this.applyDate = applyDate;
	}
	public BigDecimal getLoanAmt() {
		return loanAmt;
	}
	public void setLoanAmt(BigDecimal loanAmt) {
		this.loanAmt = loanAmt;
	}
	public Date getContraExpireDate() {
		return contraExpireDate;
	}
	public void setContraExpireDate(Date contraExpireDate) {
		this.contraExpireDate = contraExpireDate;
	}
	public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	public String getLoanCode() {
		return loanCode;
	}
	public void setLoanCode(String loanCode) {
		this.loanCode = loanCode;
	}
	public IncomeFlag getIncomeFlag() {
		return incomeFlag;
	}
	public void setIncomeFlag(IncomeFlag incomeFlag) {
		this.incomeFlag = incomeFlag;
	}
	public BigDecimal getMonthlyWages() {
		return monthlyWages;
	}
	public void setMonthlyWages(BigDecimal monthlyWages) {
		this.monthlyWages = monthlyWages;
	}
	public String getPutpaycardid() {
		return putpaycardid;
	}
	public void setPutpaycardid(String putpaycardid) {
		this.putpaycardid = putpaycardid;
	}
	public String getBankcardowner() {
		return bankcardowner;
	}
	public void setBankcardowner(String bankcardowner) {
		this.bankcardowner = bankcardowner;
	}
	public String getBankname() {
		return bankname;
	}
	public void setBankname(String bankname) {
		this.bankname = bankname;
	}
	public String getBankcode() {
		return bankcode;
	}
	public void setBankcode(String bankcode) {
		this.bankcode = bankcode;
	}
	public String getBankprovince() {
		return bankprovince;
	}
	public void setBankprovince(String bankprovince) {
		this.bankprovince = bankprovince;
	}
	public String getBankprovincecode() {
		return bankprovincecode;
	}
	public void setBankprovincecode(String bankprovincecode) {
		this.bankprovincecode = bankprovincecode;
	}
	public String getBankcity() {
		return bankcity;
	}
	public void setBankcity(String bankcity) {
		this.bankcity = bankcity;
	}
	public String getBankcitycode() {
		return bankcitycode;
	}
	public void setBankcitycode(String bankcitycode) {
		this.bankcitycode = bankcitycode;
	}
//	public BigDecimal getAgreeRate() {
//		return agreeRate;
//	}
//	public void setAgreeRate(BigDecimal agreeRate) {
//		this.agreeRate = agreeRate;
//	}
	public Date getAgreeRateExpireDate() {
		return agreeRateExpireDate;
	}
	public void setAgreeRateExpireDate(Date agreeRateExpireDate) {
		this.agreeRateExpireDate = agreeRateExpireDate;
	}
	public Indicator getAgreeRateInd() {
		return agreeRateInd;
	}
	public void setAgreeRateInd(Indicator agreeRateInd) {
		this.agreeRateInd = agreeRateInd;
	}
	public BigDecimal getFeeRate() {
		return feeRate;
	}
	public void setFeeRate(BigDecimal feeRate) {
		this.feeRate = feeRate;
	}
	public BigDecimal getFeeAmount() {
		return feeAmount;
	}
	public void setFeeAmount(BigDecimal feeAmount) {
		this.feeAmount = feeAmount;
	}
	public BigDecimal getLifeInsuFeeRate() {
		return lifeInsuFeeRate;
	}
	public void setLifeInsuFeeRate(BigDecimal lifeInsuFeeRate) {
		this.lifeInsuFeeRate = lifeInsuFeeRate;
	}
	public BigDecimal getLifeInsuFeeAmt() {
		return lifeInsuFeeAmt;
	}
	public void setLifeInsuFeeAmt(BigDecimal lifeInsuFeeAmt) {
		this.lifeInsuFeeAmt = lifeInsuFeeAmt;
	}
	public BigDecimal getInsRate() {
		return insRate;
	}
	public void setInsRate(BigDecimal insRate) {
		this.insRate = insRate;
	}
	public BigDecimal getInsAmt() {
		return insAmt;
	}
	public void setInsAmt(BigDecimal insAmt) {
		this.insAmt = insAmt;
	}
	public BigDecimal getInstallmentFeeRate() {
		return installmentFeeRate;
	}
	public void setInstallmentFeeRate(BigDecimal installmentFeeRate) {
		this.installmentFeeRate = installmentFeeRate;
	}
	public BigDecimal getInstallmentFeeAmt() {
		return installmentFeeAmt;
	}
	public void setInstallmentFeeAmt(BigDecimal installmentFeeAmt) {
		this.installmentFeeAmt = installmentFeeAmt;
	}
	public BigDecimal getPrepaymentFeeRate() {
		return prepaymentFeeRate;
	}
	public void setPrepaymentFeeRate(BigDecimal prepaymentFeeRate) {
		this.prepaymentFeeRate = prepaymentFeeRate;
	}
	public BigDecimal getPrepaymentFeeAmt() {
		return prepaymentFeeAmt;
	}
	public void setPrepaymentFeeAmt(BigDecimal prepaymentFeeAmt) {
		this.prepaymentFeeAmt = prepaymentFeeAmt;
	}
	public BigDecimal getPenaltyRate() {
		return penaltyRate;
	}
	public void setPenaltyRate(BigDecimal penaltyRate) {
		this.penaltyRate = penaltyRate;
	}
	public BigDecimal getCompoundRate() {
		return compoundRate;
	}
	public void setCompoundRate(BigDecimal compoundRate) {
		this.compoundRate = compoundRate;
	}
	public BigDecimal getInterestRate() {
		return interestRate;
	}
	public void setInterestRate(BigDecimal interestRate) {
		this.interestRate = interestRate;
	}
	public BigDecimal getStampRate() {
		return stampRate;
	}
	public void setStampRate(BigDecimal stampRate) {
		this.stampRate = stampRate;
	}
	public BigDecimal getStampAmt() {
		return stampAmt;
	}
	public void setStampAmt(BigDecimal stampAmt) {
		this.stampAmt = stampAmt;
	}
	public String getLoanFeeDefId() {
		return loanFeeDefId;
	}
	public void setLoanFeeDefId(String loanFeeDefId) {
		this.loanFeeDefId = loanFeeDefId;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public Indicator getJionLifeInsuInd() {
		return jionLifeInsuInd;
	}
	public void setJionLifeInsuInd(Indicator jionLifeInsuInd) {
		this.jionLifeInsuInd = jionLifeInsuInd;
	}
	public BigDecimal getAgentFeeRate() {
		return agentFeeRate;
	}
	public void setAgentFeeRate(BigDecimal agentFeeRate) {
		this.agentFeeRate = agentFeeRate;
	}
	public BigDecimal getAgentFeeAmount() {
		return agentFeeAmount;
	}
	public void setAgentFeeAmount(BigDecimal agentFeeAmount) {
		this.agentFeeAmount = agentFeeAmount;
	}
	
	
}
