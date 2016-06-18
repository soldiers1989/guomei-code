package com.sunline.ccs.batch.cc9100;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.batch.common.EnumUtils;
import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsChinaDivision;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsChinaDivision;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.ccs.param.def.enums.LogicMod;
import com.sunline.ccs.param.def.enums.PaymentIntervalUnit;
import com.sunline.pcm.param.def.Branch;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.EducationType;
import com.sunline.ppy.dictionary.enums.EmpPositionAttrType;
import com.sunline.ppy.dictionary.enums.EmpType;
import com.sunline.ppy.dictionary.enums.Gender;
import com.sunline.ppy.dictionary.enums.HouseOwnership;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.MaritalStatus;
import com.sunline.ppy.dictionary.enums.OccupationType;
import com.sunline.ppy.dictionary.enums.PostingFlag;
import com.sunline.ppy.dictionary.enums.TitleOfTechnicalType;

/**
 * @see 类名：U9101PBOCUtil
 * @see 描述：人行征信报送文件Util类
 * 
 * @see 创建日期： 2015-6-24下午2:48:39
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class U9101PBOCUtil {

    @Autowired
    private BatchStatusFacility batchFacility;
    @Autowired
    private BlockCodeUtils blockCodeUtils;
    @Autowired
    private UnifiedParameterFacility parameterFacility;
    @PersistenceContext
    private EntityManager em;

    /**
     * @see 方法名：getBatchDate
     * @see 描述：获取批量日期
     * @see 创建日期：2015-6-24下午2:49:31
     * @author ChengChun
     * 
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public Date getBatchDate() {
	return batchFacility.getBatchDate();
    }

    /**
     * @see 方法名：get1111
     * @see 描述：1111 当前逾期总额，小于1元时 逾期填1 非逾期填0；逾期2<=ageCD<=9
     * @see 创建日期：2015-6-24下午2:50:01
     * @author ChengChun
     * 
     * @param totDueAmt
     * @param currDueAmt
     * @param delqTol
     * @param ageCd
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public BigDecimal get1111(BigDecimal totDueAmt, BigDecimal currDueAmt, BigDecimal delqTol, String ageCd) {
	// 需要减去宽限金额，因为宽限金额内的逾期最小还款额不增长账龄 mantis4472 by dingxl
	BigDecimal amt = totDueAmt.subtract(currDueAmt).subtract(delqTol);
	if (amt.compareTo(BigDecimal.ONE) < 0) {
	    if (ageCd.compareTo("2") >= 0 && ageCd.compareTo("9") <= 0) {
		amt = BigDecimal.ONE;
	    } else {
		amt = BigDecimal.ZERO;
	    }
	}
	return amt;
    }

    /**
     * @see 方法名：get1418
     * @see 描述：1418 币种 编码方法：此处填报的是账户开立时所使用的币种。 采用根据GB/T
     *      12406-1996《表示货币和资金的代码》和ISO/FDIS 4217:2000《货币和资金的代码》编制的三位字母型代码。
     * @see 创建日期：2015-6-24下午2:50:30
     * @author ChengChun
     * 
     * @param curr
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get1418(String curr) {
	Map<String, String> map = new HashMap<String, String>();
	map.put("156", "CNY"); // 人民币
	map.put("840", "USD"); // 美元

	return map.get(curr);
    }

    /**
     * @see 方法名：get2103
     * @see 描述：2103 到期日期 业务种类为信用卡时，用20991231填充
     * @see 创建日期：2015-6-24下午2:51:00
     * @author ChengChun
     * 
     * @param defaultDate
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public Date get2103(Calendar defaultDate) {
	defaultDate.set(2099, 11, 31);
	return defaultDate.getTime();
    }

    /**
     * @see 方法名：get2408
     * @see 描述：2408 出生日期 格式为YYYYMMDD; 无法填报时，统一填“19010101”
     * @see 创建日期：2015-6-24下午2:51:24
     * @author ChengChun
     * 
     * @param birthday
     * @param defaultDate
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public Date get2408(Date birthday, Calendar defaultDate) {
	if (birthday != null) {
	    return birthday;
	} else {
	    defaultDate.set(1901, 00, 01);
	    return defaultDate.getTime();
	}
    }

    /**
     * @see 方法名：get4107
     * @see 描述：4107 最高逾期期数 AGE_HIST中最近12个月的最大值，AGE_CD-1
     * @see 创建日期：2015-6-24下午2:51:47
     * @author ChengChun
     * 
     * @param ageHist
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get4107(String ageHist) {
	String age = "0";
	if (ageHist != null) {
	    if (ageHist.length() > 12) {
		ageHist = ageHist.substring(0, 12);
	    }

	    for (char _age : "987654321".toCharArray()) {
		if (ageHist.contains(String.valueOf(_age))) {
		    age = String.valueOf(Integer.valueOf(String.valueOf(_age)) - 1);
		    break;
		}
	    }
	}
	return age;
    }

    /**
     * @see 方法名：get4109
     * @see 描述：4109 当前逾期期数。 说明：AGE_CD-1
     * @see 创建日期：2015-6-24下午2:52:13
     * @author ChengChun
     * 
     * @param ageCd
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get4109(String ageCd) {
	if ("C".equals(ageCd) || "0".equals(ageCd)) {
	    return "0";
	} else {
	    return String.valueOf(Integer.valueOf(ageCd) - 1);
	}
    }

    /**
     * @see 方法名：get4312
     * @see 描述：4312 违约次数。 PAYMENT_HIST中最近12月（正数）中U+N的次数
     * @see 创建日期：2015-6-24下午2:52:36
     * @author ChengChun
     * 
     * @param paymentHist
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get4312(String paymentHist) {
	if (paymentHist == null) {
	    return String.valueOf(0);
	} else if (paymentHist.length() > 12) {
	    paymentHist = paymentHist.substring(0, 12);
	}
	char[] a = paymentHist.toCharArray();
	int i = 0;
	for (char _a : a) {
	    if (_a == 'U' || _a == 'N') {
		i++;
	    }
	}
	return String.valueOf(i);
    }

    /**
     * @see 方法名：get5105
     * @see 描述： 5105 性别 采用GB/T 2261.1-2003 《个人基本信息分类与代码 第1部分：人的性别代码》
     *      代码表：0-未知的性别；1-男性；2-女性；9-未说明性别
     * @see 创建日期：2015-6-24下午2:53:00
     * @author ChengChun
     * 
     * @param gender
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get5105(Gender gender) {
	Map<Gender, String> map = new HashMap<Gender, String>();
	map.put(Gender.M, "1");// 男性
	map.put(Gender.F, "2");// 女性

	return map.get(gender);
    }

    /**
     * @see 方法名：get5107
     * @see 描述：5107 证件类型 代码表：0-身份证；1-户口簿；2-护照；3-军官证；4-士兵证；5-港澳居民来往内地通行证；
     *      6-台湾同胞来往内地通行证；7-临时身份证；8-外国人居留证；9-警官证；X-其他证件
     * @see 创建日期：2015-6-24下午2:53:36
     * @author ChengChun
     * 
     * @param idType
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get5107(IdType idType) {
	Map<IdType, String> map = new HashMap<IdType, String>();
	map.put(IdType.C, "9");// 警官证
	map.put(IdType.F, "8");// 外国人居留证
	map.put(IdType.H, "5");// 港澳居民来往内地通行证
	map.put(IdType.I, "0");// 身份证
	map.put(IdType.L, "X");// 营业执照
	map.put(IdType.O, "X");// 其他有效证件
	map.put(IdType.P, "2");// 护照
	map.put(IdType.R, "1");// 户口簿
	map.put(IdType.S, "4");// 军官证
	map.put(IdType.T, "7");// 临时身份证
	map.put(IdType.W, "6");// 台湾同胞来往内地通行证

	return map.get(idType);
    }

    /**
     * @see 方法名：get5111
     * @see 描述：5111 婚姻状况 采用GB/T 2261.2-2003 《个人基本信息分类与代码 第2部分：婚姻状况代码》
     *      代码表：10-未婚；20-已婚；21-初婚；22-再婚；23-复婚；30-丧偶；40-离婚；90-未说明的婚姻状况。
     * @see 创建日期：2015-6-24下午2:54:11
     * @author ChengChun
     * 
     * @param maritalStatus
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get5111(MaritalStatus maritalStatus) {
	switch (maritalStatus) {
	case S:
	    return "10";// 未婚
	case M:
	case C:
	    return "20";// 已婚
	case W:
	    return "30";// 丧偶
	case D:
	    return "40";// 离婚
	case O:
	    return "90";// 未说明的婚姻状况
	default:
	    return "90";// 未说明的婚姻状况
	}
    }

    /**
     * @see 方法名：get5113
     * @see 描述：5113 最高学历 采用GB/T 4658-1984《文化程度代码》的大类代码
     *      代码表：10-研究生；20-大学本科（简称“大学”）；30-大学专科和专科学校（简称“大专”）；
     *      40-中等专业学校或中等技术学校；50-技术学校；60-高中；70-初中；80-小学；90-文盲或半文盲；99-未知。
     * @see 创建日期：2015-6-24下午2:55:16
     * @author ChengChun
     * 
     * @param educationType
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get5113(EducationType educationType) {
	Map<EducationType, String> map = new HashMap<EducationType, String>();
	map.put(EducationType.A, "10");// 研究生
	map.put(EducationType.B, "10");// 研究生
	map.put(EducationType.C, "20");// 大学本科
	map.put(EducationType.D, "30");// 大学专科和专科学校
	map.put(EducationType.E, "40");// 中等专业学校或中等技术学校
	map.put(EducationType.F, "60");// 高中
	map.put(EducationType.G, "70");// 初中

	return map.get(educationType);
    }

    /**
     * @see 方法名：get5115
     * @see 描述：5115 最高学位 0-其他；1-名誉博士；2-博士；3-硕士；4-学士；9-未知。
     * @see 创建日期：2015-6-24下午2:55:56
     * @author ChengChun
     * 
     * @param qualification
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get5115(EducationType qualification) {
	Map<EducationType, String> map = new HashMap<EducationType, String>();
	map.put(EducationType.A, "2");// 博士
	map.put(EducationType.B, "3");// 硕士
	map.put(EducationType.C, "4");// 学士
	map.put(EducationType.D, "9");// 未知
	map.put(EducationType.E, "9");
	map.put(EducationType.F, "9");
	map.put(EducationType.G, "9");

	return map.get(qualification);
    }

    /**
     * @see 方法名：get5119
     * @see 描述：5119 职业 采用GB/T 6565-1999《职业分类与代码》中的大类代码
     *      代码表：0-国家机关、党群组织、企业、事业单位负责人；1-专业技术人员；3-办事人员和有关人员；4-商业、服务业人员；
     *      5-农、林、牧、渔、水利业生产人员；6-生产、运输设备操作人员及有关人员；X-军人；Y-不便分类的其他从业人员；Z-未知。
     * @see 创建日期：2015-6-24下午2:56:21
     * @author ChengChun
     * 
     * @param occupationType
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get5119(OccupationType occupationType) {
	Map<OccupationType, String> map = new HashMap<OccupationType, String>();
	map.put(OccupationType.A, "0");// 国家机关、党群组织、企业、事业单位负责人
	map.put(OccupationType.B, "1");// 专业技术人员
	map.put(OccupationType.C, "3");// 办事人员和有关人员
	map.put(OccupationType.D, "4");// 商业、服务业人员
	map.put(OccupationType.E, "5");// 农、林、牧、渔、水利业生产人员
	map.put(OccupationType.F, "6");// 生产、运输设备操作人员及有关人员
	map.put(OccupationType.G, "X");// 军人
	map.put(OccupationType.H, "Y");// 不便分类的其他从业人员

	return map.get(occupationType);
    }

    /**
     * @see 方法名：get5121
     * @see 描述：5121 职务
     *      代码表：1-高级领导（行政级别局级及局级以上领导或大公司高级管理人员）；2-中级领导（行政级别局级以下领导或大公司中级管理人员
     *      ）；3-一般员工；4-其他；9-未知
     * @see 创建日期：2015-6-24下午2:57:18
     * @author ChengChun
     * 
     * @param empPositionAttrType
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get5121(EmpPositionAttrType empPositionAttrType) {
	Map<EmpPositionAttrType, String> map = new HashMap<EmpPositionAttrType, String>();
	map.put(EmpPositionAttrType.A, "1");// 高级领导
	map.put(EmpPositionAttrType.B, "2");// 中级领导
	map.put(EmpPositionAttrType.C, "2");// 中级领导
	map.put(EmpPositionAttrType.D, "3");// 一般员工
	map.put(EmpPositionAttrType.E, "3");// 一般员工
	map.put(EmpPositionAttrType.F, "3");// 一般员工
	map.put(EmpPositionAttrType.G, "3");// 一般员工
	map.put(EmpPositionAttrType.H, "3");// 一般员工
	map.put(EmpPositionAttrType.I, "3");// 一般员工
	map.put(EmpPositionAttrType.Z, "4");// 其他

	return map.get(empPositionAttrType);
    }

    /**
     * @see 方法名：get5123
     * @see 描述：5123 职称 代码表：0-无；1-高级；2-中级；3-初级；9-未知
     * @see 创建日期：2015-6-24下午2:57:46
     * @author ChengChun
     * 
     * @param corpTitleOfTechnical
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get5123(TitleOfTechnicalType corpTitleOfTechnical) {
	Map<TitleOfTechnicalType, String> map = new HashMap<TitleOfTechnicalType, String>();
	map.put(TitleOfTechnicalType.A, "1");// 高级
	map.put(TitleOfTechnicalType.B, "2");// 中级
	map.put(TitleOfTechnicalType.C, "3");// 初级
	map.put(TitleOfTechnicalType.D, "9");// 未知

	return map.get(corpTitleOfTechnical);
    }

    /**
     * @see 方法名：get5127
     * @see 描述：5127 居住状况
     *      代码表：1-自置；2-按揭；3-亲属楼宇；4-集体宿舍；5-租房；6-共有住宅；7-其他；9-未知。无法填报中，统一选“未知”填充
     * @see 创建日期：2015-6-24下午2:58:09
     * @author ChengChun
     * 
     * @param houseOwnership
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get5127(HouseOwnership houseOwnership) {
	Map<HouseOwnership, String> map = new HashMap<HouseOwnership, String>();
	map.put(HouseOwnership.A, "1");// 自置
	map.put(HouseOwnership.B, "2");// 按揭
	map.put(HouseOwnership.C, "5");// 租房
	map.put(HouseOwnership.D, "3");// 亲属楼宇
	map.put(HouseOwnership.E, "4");// 集体宿舍
	map.put(HouseOwnership.Z, "7");// 其他

	return map.get(houseOwnership) == null ? "9" : map.get(houseOwnership);
    }

    /**
     * @see 方法名：get5208
     * @see 描述：5208 配偶证件类型,同5107 配偶证件类型
     * @see 创建日期：2015-6-24下午2:58:47
     * @author ChengChun
     * 
     * @param idType
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get5208(IdType idType) {
	return get5107(idType);
    }

    /**
     * @see 方法名：get6103
     * @see 描述：6103 单位所属行业 采用GB/T 4754-2002 《国民经济行业分类》的门类代码
     * @see 创建日期：2015-6-24下午2:59:10
     * @author ChengChun
     * 
     * @param corpIndustryCategory
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get6103(EmpType corpIndustryCategory) {
	Map<EmpType, String> map = new HashMap<EmpType, String>();
	map.put(EmpType.A, "A");// 农、林、牧、渔业
	map.put(EmpType.B, "B");// 采掘业
	map.put(EmpType.C, "C");// 制造业
	map.put(EmpType.D, "D");// 电力、燃气及水的生产和供应业
	map.put(EmpType.E, "E");// 建筑业
	map.put(EmpType.F, "F");// 交通运输、仓储和邮政业
	map.put(EmpType.G, "G");// 信息传输、计算机服务和软件业
	map.put(EmpType.H, "H");// 批发和零售业
	map.put(EmpType.I, "I");// 住宿和餐饮业
	map.put(EmpType.J, "J");// 金融业
	map.put(EmpType.K, "K");// 房地产业
	map.put(EmpType.L, "L");// 租赁和商务服务业
	map.put(EmpType.M, "M");// 科学研究、技术服务业和地质勘察业
	map.put(EmpType.N, "N");// 水利、环境和公共设施管理业
	map.put(EmpType.O, "O");// 居民服务和其他服务业
	map.put(EmpType.P, "P");// 教育
	map.put(EmpType.Q, "Q");// 卫生、社会保障和社会福利业
	map.put(EmpType.R, "R");// 文化、体育和娱乐业
	map.put(EmpType.S, "S");// 公共管理和社会组织
	map.put(EmpType.T, "T");// 国际组织
	map.put(EmpType.Z, "Z");// 未知

	return map.get(corpIndustryCategory);
    }

    /**
     * @see 方法名：get7105
     * @see 描述：7105 五级分类状态 AGE_CD 1-正常:C01;2-关注:23;3-次级:45;4-可疑:67;5-损失:89
     * @see 创建日期：2015-6-24下午2:59:37
     * @author ChengChun
     * 
     * @param ageCd
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get7105(String ageCd) {
	Map<String, String> map = new HashMap<String, String>();
	map.put("C", "1");// 正常
	map.put("0", "1");
	map.put("1", "1");
	map.put("2", "2");// 关注
	map.put("3", "2");
	map.put("4", "3");// 次级
	map.put("5", "3");
	map.put("6", "4");// 可疑
	map.put("7", "4");
	map.put("8", "5");// 损失
	map.put("9", "5");

	return map.get(ageCd);
    }

    /**
     * @see 方法名：get7107
     * @see 描述：7107 24个月（账户）还款状态 G= blockcode=P&&currBal!=0; C=
     *      blockcode=P&&currBal=0; *= currBal=0;
     *      其次ageCd:*=C;N=01;12345677=23456789;
     * @see 创建日期：2015-6-24下午3:00:06
     * @author ChengChun
     * 
     * @param ageHist
     * @param blockCode
     * @param currBal
     * @param stmtList
     * @param delqTol
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get7107(String ageHist, String blockCode, BigDecimal currBal, List<CcsStatement> stmtList,
	    BigDecimal delqTol) {
	String defaultAgeHist = "///////////////////////*";
	if (ageHist == null) {
	    return defaultAgeHist;
	} else {
	    String stateHist = "";
	    int i = 0;
	    for (char ageCd : ageHist.toCharArray()) {
		switch (ageCd) {
		case 'C':
		case '0':
		case '1':
		    // 还款后的账龄可能为C,0,1,所以账龄小于2时，需要通过上期最小还款额来判断
		    // 当最小还款额-宽限金额>0时，为'N',否则为'*'
		    // mantis4472 by dingxl
		    if (stmtList.size() > i + 1) {
			if (stmtList.get(i + 1).getTotDueAmt().subtract(delqTol).compareTo(BigDecimal.ZERO) > 0) {
			    stateHist += "N";
			} else {
			    stateHist += "*";
			}
		    } else {
			stateHist += "*";
		    }
		    break;
		case '2':
		    stateHist += "1";
		    break;
		case '3':
		    stateHist += "2";
		    break;
		case '4':
		    stateHist += "3";
		    break;
		case '5':
		    stateHist += "4";
		    break;
		case '6':
		    stateHist += "5";
		    break;
		case '7':
		    stateHist += "6";
		    break;
		case '8':
		    stateHist += "7";
		    break;
		case '9':
		    stateHist += "7";
		    break;
		default:
		    break;
		}
		i++;
	    }
	    if (blockCodeUtils.isExists(blockCode, "P")) {
		if (currBal.compareTo(BigDecimal.ZERO) == 0) {
		    stateHist = "C" + stateHist.substring(1);
		} else {
		    stateHist = "G" + stateHist.substring(1);
		}
	    }

	    StringBuffer stateHistBuffer = new StringBuffer((stateHist + defaultAgeHist).substring(0, 24));
	    return stateHistBuffer.reverse().toString();
	}

    }

    /**
     * @see 方法名：get7109
     * @see 描述： 7109 账户状态
     *      反映金融机构对该贷款或信用风险程度的评估。一个账户只要有应还未还的情况，账户状态就算逾期，而不是整个账户到期后未还清才算逾期
     *      。一次还本，按期还息。利息未还，也算逾期。 代码表： 业务种类为信用卡：1-正常；2-冻结；3-止付；4-销户；5-呆账；6-未激活
     *      优先级452361
     * @see 创建日期：2015-6-24下午3:00:30
     * @author ChengChun
     * 
     * @param blockCode
     * @param isActivate
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get7109(String blockCode, boolean isActivate) {
	// 按优先级顺序
	if (blockCodeUtils.isExists(blockCode, "P")) {
	    return "4";
	} else if (blockCodeUtils.isExists(blockCode, "W")) {
	    return "5";
	} else if (blockCodeUtils.isExists(blockCode, "T")) {
	    return "2";
	} else if (blockCodeUtils.isExists(blockCode, "R") || blockCodeUtils.isExists(blockCode, "S")
		|| blockCodeUtils.isExists(blockCode, "L")) {
	    return "3";
	} else if (!isActivate) {
	    return "6";
	} else {
	    return "1";
	}
    }

    /**
     * @see 方法名：get7121
     * @see 描述：7121 账户拥有者信息提示 表示一个新账户的开立或账户拥有者标识的更改
     *      代码表：1-已开立非更改；2-新账户开立；3-已开立并更改
     * @see 创建日期：2015-6-24下午3:01:54
     * @author ChengChun
     * 
     * @param date
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get7121(Date date) {
	Calendar setupDate = Calendar.getInstance();
	setupDate.setTime(date);
	Calendar batchDate = Calendar.getInstance();
	batchDate.setTime(batchFacility.getBatchDate());

	if (DateUtils.truncatedCompareTo(setupDate, batchDate, Calendar.DATE) == 0) {
	    return "2";
	} else {
	    return "1";
	}
    }

    /**
     * @see 方法名：get4111
     * @see 描述：获取还款周期值 01-日； 02-周； 03-月； 04-季； 05-半年； 06-年； 07-一次性；
     *      08-不定期（还款日之间的时间间隔不是固定周期）
     * @see 创建日期：2015-6-24下午3:02:27
     * @author ChengChun
     * 
     * @param unit
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get4111(PaymentIntervalUnit unit) {
	switch (unit) {
	case M:
	    return "03";
	case D:
	    return "01";
	case Q:
	    return "04";
	case W:
	    return "02";
	case Y:
	    return "06";
	default:
	    return "07";
	}
    }

    /**
     * @see 方法名：get3141
     * @see 描述：3141 发生地点 全国邮政编码表 映射 中国行政区划代码
     * @see 创建日期：2015-6-24下午3:02:46
     * @author ChengChun
     * 
     * @param owningBranch
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get3141(String owningBranch) {
	Branch branch = parameterFacility.retrieveParameterObject(owningBranch, Branch.class);
	if (branch == null || branch.zip == null) {
	    return "";
	}
	String zip = branch.zip;
	QCcsChinaDivision qTmArea = QCcsChinaDivision.ccsChinaDivision;
	CcsChinaDivision area = new JPAQuery(em).from(qTmArea).where(qTmArea.postcode.eq(zip)).singleResult(qTmArea);
	if (area != null) {
	    return area.getDivisionCode();
	}

	while (zip.length() >= 2) {
	    zip = zip.substring(0, zip.length() - 1);// 退位匹配
	    area = new JPAQuery(em).from(qTmArea).where(qTmArea.postcode.like(zip + "%")).singleResult(qTmArea);
	    if (area != null) {
		return area.getParentAreaCode();
	    }
	}
	return "";
    }

    /**
     * @see 方法名：get4101
     * @see 描述： 对于固定还款周期的贷款： 此数据项填写该笔贷款的总还款期数折合的月数，折合规则如下： 还款频率为天：除以30.42
     *      还款频率为周：除以4.33 还款频率为月：实际总还款期数 还款频率为季：乘以3 还款频率为半年：乘以6 还款频率为年：乘以12
     *      还款频率为
     *      “其他”，且周期固定的贷款，参照还款频率为日、周、月、季、半年、年的情况计算确定。例如还款频率为“双周”时，还款月数=总还款期数
     *      ÷4.33×2 对于非固定还款周期的贷款： 还款频率为“不定期”，填写字母U 还款频率为“一次性”，填写字母O
     *      还款频率为“其他”，填写字母X 对于信用卡，填写C。
     * @see 创建日期：2015-6-24下午3:03:15
     * @author ChengChun
     * 
     * @param unit
     * @param loan
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get4101(PaymentIntervalUnit unit, CcsLoan loan) {
	switch (unit) {
	case M:
	    return String.valueOf(loan.getLoanInitTerm());
	case D:
	    return String.valueOf((int)loan.getLoanInitTerm() / 30.42);
	case Q:
	    return String.valueOf(loan.getLoanInitTerm() * 3);
	case W:
	    return String.valueOf((int)loan.getLoanInitTerm() / 4.33);
	case Y:
	    return String.valueOf(loan.getLoanInitTerm() * 12);
	default:
	    return "O";
	}
    }

    /**
     * @see 方法名：get4105
     * @see 描述：对于固定还款周期的贷款： 此数据项填写按照还款计划表，借款人应该在数据提取日到到期日之间偿还的贷款期数折合的月数，折合规则如下：
     *      还款频率为天：除以30.42 还款频率为周：除以4.33 还款频率为月：实际剩余还款期数 还款频率为季：乘以3 还款频率为半年：乘以6
     *      还款频率为年：乘以12 还款频率为“其他”且周期固定的贷款，参照还款频率为日、周、月、季、半年、年的情况计算确定。
     *      对于非固定还款周期的贷款，无论贷款是否结清，统一按照下列规则填写： 还款频率为“不定期”，填写字母U 还款频率为“一次性”，填写字母O
     *      还款频率为“其他”，填写字母X 对于信用卡，填写C。
     * @see 创建日期：2015-6-24下午3:03:42
     * @author ChengChun
     * 
     * @param unit
     * @param loan
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String get4105(PaymentIntervalUnit unit, CcsLoan loan) {
	switch (unit) {
	case M:
	    return String.valueOf(loan.getRemainTerm());
	case D:
	    return String.valueOf((int)loan.getRemainTerm() / 30.42);
	case Q:
	    return String.valueOf(loan.getRemainTerm() * 3);
	case W:
	    return String.valueOf((int)loan.getRemainTerm() / 4.33);
	case Y:
	    return String.valueOf(loan.getRemainTerm() * 12);
	default:
	    return "O";
	}
    }

    /**
     * @see 方法名：getLastDayOfMonth
     * @see 描述：获得月底最后一天的日期
     * @see 创建日期：2015-6-24下午3:03:56
     * @author ChengChun
     * 
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public Date getLastDayOfMonth() {
	Date d = getBatchDate();
	Calendar c = Calendar.getInstance();
	c.setTime(d);
	c.add(Calendar.MONTH, 1);
	c.set(Calendar.DATE, 1);
	c.add(Calendar.DATE, -1);
	return c.getTime();
    }

    /**
     * @see 方法名：getMonthInterval
     * @see 描述：获得给定两个日期之间相隔的月数，算到日
     * @see 创建日期：2015-6-24下午3:04:07
     * @author ChengChun
     * 
     * @param from
     * @param to
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public int getMonthInterval(Date from, Date to) {
	Calendar f = Calendar.getInstance();
	f.setTime(from);
	Calendar t = Calendar.getInstance();
	t.setTime(to);
	int year = t.get(Calendar.YEAR) - f.get(Calendar.YEAR);
	int month = t.get(Calendar.MONTH) - f.get(Calendar.MONTH);
	return year * 12 + month;
    }

    /**
     * @see 方法名：getIntervalDays
     * @see 描述：获得两个日期间隔天数
     * @see 创建日期：2015-6-24下午3:04:25
     * @author ChengChun
     * 
     * @param from
     * @param to
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public int getIntervalDays(Date from, Date to) {
	long interval = to.getTime() - from.getTime();
	int result = (int)(interval / (1000 * 3600 * 24));
	return result;
    }

    /**
     * @see 方法名：isMonthEnd
     * @see 描述：判断给定日期是否月底
     * @see 创建日期：2015-6-24下午3:04:40
     * @author ChengChun
     * 
     * @param date
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public boolean isMonthEnd(Date date) {
	Calendar c = Calendar.getInstance();
	c.setTime(date);
	c.add(Calendar.DATE, 1);
	return c.get(Calendar.DATE) == 1;
    }

    /**
     * @see 方法名：substringByByte
     * @see 描述：截取对应字节长截取字符串,如果最后一个字节为半个汉字则截取到前一个字节
     * @see 创建日期：2015-6-24下午3:04:58
     * @author ChengChun
     * 
     * @param buff
     * @param lenOfByte
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public String substringByByte(String buff, int lenOfByte) {
	if (buff == null) {
	    return "";
	}
	byte[] buf = buff.getBytes();
	if (buf.length <= lenOfByte) {
	    return buff;
	}

	int lenOfChar = 0;
	boolean bChineseFirstHalf = false;
	int add = 0;
	for (int i = 0; i < lenOfByte; i++) {
	    if (buf[i] < 0 && !bChineseFirstHalf) {
		 bChineseFirstHalf = true;
	    } else {
		if (add % 3 == 0 && buf[i] < 0) {
		    add++;
		    lenOfChar++;
		    add = 0;
		}
		bChineseFirstHalf = false;
	    }

	}
	return buff.substring(0, lenOfChar-1);
    }

    /**
     * @see 方法名：getSumAmtOfLoanB
     * @see 描述：统计该账单中账单分期转出贷方发生额之和
     * @see 创建日期：2015-6-24下午3:05:12
     * @author ChengChun
     * 
     * @param stmtHst
     * @return
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    public BigDecimal getSumAmtOfLoanB(CcsStatement stmtHst) {
	BigDecimal sumAmtOfLoanB = BigDecimal.ZERO;
	QCcsTxnHst qTmTxnHst = QCcsTxnHst.ccsTxnHst;
	List<CcsTxnHst> txnHsts =
		new JPAQuery(em)
			.from(qTmTxnHst)
			.where(qTmTxnHst.acctNbr.eq(stmtHst.getAcctNbr())
				       .and(qTmTxnHst.acctType.eq(stmtHst.getAcctType()))
				       .and(qTmTxnHst.stmtDate.eq(stmtHst.getStmtDate()))).list(qTmTxnHst);

	for (CcsTxnHst txn : txnHsts) {
	    // 交易码参数
	    TxnCd txnCd = parameterFacility.loadParameter(txn.getTxnCode(), TxnCd.class);
	    if (EnumUtils.in(txnCd.logicMod, LogicMod.A26, LogicMod.L26) && txn.getPostingFlag() == PostingFlag.F00) {
		sumAmtOfLoanB = sumAmtOfLoanB.add(txn.getPostAmt().abs());
	    }
	}

	return sumAmtOfLoanB;
    }

}
