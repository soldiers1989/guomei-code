package com.sunline.ccs.service.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunline.ppy.dictionary.entity.Check;
import com.sunline.ppy.dictionary.enums.EducationType;
import com.sunline.ppy.dictionary.enums.EmpPositionAttrType;
import com.sunline.ppy.dictionary.enums.EmpType;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.IncomeFlag;
import com.sunline.ppy.dictionary.enums.MaritalStatus;
import com.sunline.ppy.dictionary.enums.Relationship;
import com.sunline.ppy.dictionary.enums.TitleOfTechnicalType;

/**
 * 放款申请接收报文
 * @author jjb
 *
 */
public class S30001LoanReq extends SunshineRequestInfo implements Serializable {
	public static final long serialVersionUID = 1L;
	
	/**
	 * 姓名
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="CUSTOMERNAME")
	public String  customername;	
	/**
	 * 马上提供征信市回传的 ID
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="DATAID")
	public String dataid;
	/**
	 * 合同号
	 */
	@Check(lengths=18)
	@JsonProperty(value="CONTRACTNO")
	public String contractno;
	/**
	 * 性别
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="SEX")
	public Gender sex;
	/**
	 * 出生日期
	 * YYYYMMDD
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="BIRTHDAY")
	public Date birthday;
	/**
	 * 婚姻状况 
	 */
	@Check(lengths=4,notEmpty=true)
	@JsonProperty(value="MARRIAGE")
	public MaritalStatus marriage;
	/**
	 * 国籍
	 */
	@Check(lengths=3,notEmpty=true)
	@JsonProperty(value="COUNTRY")
	public String country;
	/**
	 * 证件号码
	 */
	@Check(lengths=18,notEmpty=true,fixed=true)
	@JsonProperty(value="CERTID")
	public String certid;
	/**
	 * 证件类型 
	 */
	@Check(lengths=5,notEmpty=true)
	@JsonProperty(value="CERTTYPE")
	public IdType certtype;
	/**
	 * 证件有效期
	 * YYYYMMDD
	 */
	@Check(lengths=8,notEmpty=true)
	@JsonProperty(value="DUEDATE")
	public Date duedate;
	/**
	 * 移动电话
	 */
	@Check(lengths=11,notEmpty=true,fixed=true,isNumber=true)
	@JsonProperty(value="MOBILE")
	public String mobile;
	/**
	 * 邮政编码
	 */
	@Check(lengths=6,notEmpty=true,fixed=true,isNumber=true)
	@JsonProperty(value="ZIPCODE")
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
	@JsonProperty(value="AREACODE")
	public String areacode;
	/**
	 * 家庭地址（街道）
	 */
	@Check(lengths=80,notEmpty=true)
	@JsonProperty(value="FAMILYADD")
	public String familyadd;
	/**
	 * 住宅性质 
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="FAMILYSTATUS")
	public String familystatus;
	/**
	 * 住宅电话
	 */
	@Check(lengths=20,notEmpty=true)
	@JsonProperty(value="FAMILYTEL")
	public String familytel;
	/**
	 * 最高学历
	 */
	@Check(lengths=4,notEmpty=true)
	@JsonProperty(value="EDUEXPERIENCE")
	public EducationType eduexperience;
	/**
	 * 最高学位
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="EDUDEGREE")
	public String edudegree;
	/**
	 * 通讯地址 
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="COMMADD")
	public String commadd;
	/**
	 * 单位地址
	 */
	@Check(lengths=80,notEmpty=true)
	@JsonProperty(value="WORKADD")
	public String workadd;
	/**
	 * 工作单位
	 */
	@Check(lengths=80,notEmpty=true)
	@JsonProperty(value="WORKCORP")
	public String workcorp;
	/**
	 * 单位邮编
	 */
	@Check(lengths=6,notEmpty=true)
	@JsonProperty(value="WORKZIP")
	public String workzip;
	/**
	 * 单位电话
	 */
	@Check(lengths=20,notEmpty=true)
	@JsonProperty(value="WORKTEL")
	public String worktel;
	/**
	 * 本单位工作时间
	 */
	@Check(lengths=4,notEmpty=true)
	@JsonProperty(value="WORKBEGINDATE")
	public String workbegindate;
	/**
	 * 职位 
	 */
	@Check(lengths=2,notEmpty=true)
	@JsonProperty(value="POSITIONLEVEL")
	public EmpPositionAttrType posionlevel;
	/**
	 * 职称 
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="POSITION")
	public TitleOfTechnicalType position;
	/**
	 * 单位性质
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="WORKNATURE")
	public String worknature;
	/**
	 * 行业 
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="UNITKIND")
	public EmpType unitkind;
	/**
	 * 紧急联系人1
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="LINK1")
	public String link1;
	/**
	 * 紧急联系人1关系
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="LINKRELATION1")
	public Relationship linkrelation1;
	/**
	 * 紧急联系人1联系移动电话
	 */
	@Check(lengths=20,notEmpty=true)
	@JsonProperty(value="LINKMOBILE1")
	public String linkmobile1;
	/**
	 * 紧急联系人2
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="LINK2")
	public String link2;
	/**
	 * 紧急联系人2关系
	 */
	@Check(lengths=1,notEmpty=true)
	@JsonProperty(value="LINKRELATION2")
	public Relationship linkrelation2;
	/**
	 * 紧急联系人2联系移动电话
	 */
	@Check(lengths=20,notEmpty=true)
	@JsonProperty(value="LINKMOBILE2")
	public String linkmobile2;
	/**
	 * 申请日期
	 */
	@Check(lengths=10)
	@JsonProperty(value="OPERATEDATE")
	public Date operatedate;
	/**
	 * 贷款种类
	 */
	@Check(lengths=32)
	@JsonProperty(value="BUSINESSTYPE")
	public String businesstype;
	/**
	 * 贷款总金额 
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="BUSINESSSUM")
	public BigDecimal businesssum;
	/**
	 * 贷款总期限 
	 */
	@Check(lengths=2,notEmpty=true,isNumber=true)
	@JsonProperty(value="LOANTERM")
	public String loanterm;
	/**
	 * 贷款用途
	 */
	@Check(lengths=4,notEmpty=false)
	@JsonProperty(value="PURPOSE")
	public String purpose;
	/**
	 * 保单号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="GUARANTYID")
	public String guarantyid;
	/**
	 * 个人收入标识 
	 */	
	@Check(lengths=3,notEmpty=true)
	@JsonProperty(value="INCOMEFLAG")
	public IncomeFlag incomeflag;
	/**
	 * 个人月收入
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="MONTHLYWAGES")
	public BigDecimal monthlywages;
	/**
	 * 保费率
	 */
	@Check(lengths=18,notEmpty=true)
	@JsonProperty(value="CONFIRMVALUEMONTHRATE")
	public BigDecimal confirmvaluemonthrate;
	/**
	 * 放款/还款卡号
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="PUTPAYCARDID")
	public String putpaycardid;
	/**
	 * 开户行名称
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANKNAME")
	public String bankname;
	/**
	 * 开户行code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="BANKCODE")
	public String bankcode;
	/**
	 * 开户行省
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANKPROVINCE")
	public String bankprovince;
	/**
	 * 开户行省code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="BANKPROVINCECODE")
	public String bankprovincecode;
	/**
	 * 开户行市
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANKCITY")
	public String bankcity;
	/**
	 * 开户行市code
	 */
	@Check(lengths=10,notEmpty=true)
	@JsonProperty(value="BANKCITYCODE")
	public String bankcitycode;
	/**
	 * 银行卡开户人 名称
	 */
	@Check(lengths=32,notEmpty=true)
	@JsonProperty(value="BANKCARDOWNER")
	public String bankcardowner;
	
	/**
	 * 产品编号
	 */
	@Check(lengths=101,notEmpty=true)
	@JsonProperty(value="PRODUCTCD")
	public String productcd;
	
	
	public String getCustomername() {
		return customername;
	}
	public void setCustomername(String customername) {
		this.customername = customername;
	}
	public String getDataid() {
		return dataid;
	}
	public void setDataid(String dataid) {
		this.dataid = dataid;
	}
	public String getContractno() {
		return contractno;
	}
	public void setContractno(String contractno) {
		this.contractno = contractno;
	}
	public Gender getSex() {
		return sex;
	}
	public void setSex(Gender sex) {
		this.sex = sex;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public MaritalStatus getMarriage() {
		return marriage;
	}
	public void setMarriage(MaritalStatus marriage) {
		this.marriage = marriage;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCertid() {
		return certid;
	}
	public void setCertid(String certid) {
		this.certid = certid;
	}
	public IdType getCerttype() {
		return certtype;
	}
	public void setCerttype(IdType certtype) {
		this.certtype = certtype;
	}
	
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
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
	public String getFamilyadd() {
		return familyadd;
	}
	public void setFamilyadd(String familyadd) {
		this.familyadd = familyadd;
	}
	public String getFamilystatus() {
		return familystatus;
	}
	public void setFamilystatus(String familystatus) {
		this.familystatus = familystatus;
	}
	public String getFamilytel() {
		return familytel;
	}
	public void setFamilytel(String familytel) {
		this.familytel = familytel;
	}
	public String getCommadd() {
		return commadd;
	}
	public void setCommadd(String commadd) {
		this.commadd = commadd;
	}
	public String getWorkadd() {
		return workadd;
	}
	public void setWorkadd(String workadd) {
		this.workadd = workadd;
	}
	public String getWorkcorp() {
		return workcorp;
	}
	public void setWorkcorp(String workcorp) {
		this.workcorp = workcorp;
	}
	public String getWorkzip() {
		return workzip;
	}
	public void setWorkzip(String workzip) {
		this.workzip = workzip;
	}
	public String getWorktel() {
		return worktel;
	}
	public void setWorktel(String worktel) {
		this.worktel = worktel;
	}
	public EmpPositionAttrType getPosionlevel() {
		return posionlevel;
	}
	public void setPosionlevel(EmpPositionAttrType posionlevel) {
		this.posionlevel = posionlevel;
	}
	public TitleOfTechnicalType getPosition() {
		return position;
	}
	public void setPosition(TitleOfTechnicalType position) {
		this.position = position;
	}
	public String getWorknature() {
		return worknature;
	}
	public void setWorknature(String worknature) {
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
	
	public String getBusinesstype() {
		return businesstype;
	}
	public void setBusinesstype(String businesstype) {
		this.businesstype = businesstype;
	}
	public BigDecimal getBusinesssum() {
		return businesssum;
	}
	public void setBusinesssum(BigDecimal businesssum) {
		this.businesssum = businesssum;
	}
	public String getLoanterm() {
		return loanterm;
	}
	public void setLoanterm(String loanterm) {
		this.loanterm = loanterm;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public String getGuarantyid() {
		return guarantyid;
	}
	public void setGuarantyid(String guarantyid) {
		this.guarantyid = guarantyid;
	}
	public IncomeFlag getIncomeflag() {
		return incomeflag;
	}
	public void setIncomeflag(IncomeFlag incomeflag) {
		this.incomeflag = incomeflag;
	}
	public BigDecimal getMonthlywages() {
		return monthlywages;
	}
	public void setMonthlywages(BigDecimal monthlywages) {
		this.monthlywages = monthlywages;
	}
	public BigDecimal getConfirmvaluemonthrate() {
		return confirmvaluemonthrate;
	}
	public void setConfirmvaluemonthrate(BigDecimal confirmvaluemonthrate) {
		this.confirmvaluemonthrate = confirmvaluemonthrate;
	}
	public String getPutpaycardid() {
		return putpaycardid;
	}
	public void setPutpaycardid(String putpaycardid) {
		this.putpaycardid = putpaycardid;
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
	public String getBankcardowner() {
		return bankcardowner;
	}
	public void setBankcardowner(String bankcardowner) {
		this.bankcardowner = bankcardowner;
	}
	public Date getDuedate() {
		return duedate;
	}
	public void setDuedate(Date duedate) {
		this.duedate = duedate;
	}
	public Date getOperatedate() {
		return operatedate;
	}
	public void setOperatedate(Date operatedate) {
		this.operatedate = operatedate;
	}
	public String getProductcd() {
		return productcd;
	}
	public void setProductcd(String productcd) {
		this.productcd = productcd;
	}
	public String getWorkbegindate() {
		return workbegindate;
	}
	public void setWorkbegindate(String workbegindate) {
		this.workbegindate = workbegindate;
	}
	public EducationType getEduexperience() {
		return eduexperience;
	}
	public void setEduexperience(EducationType eduexperience) {
		this.eduexperience = eduexperience;
	}
	public String getEdudegree() {
		return edudegree;
	}
	public void setEdudegree(String edudegree) {
		this.edudegree = edudegree;
	}
	
}
