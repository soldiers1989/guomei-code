package com.sunline.ccs.ui.client.pages.loaninfo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sunline.ccs.infrastructure.client.ui.UCcsCustomer;
import com.sunline.ccs.infrastructure.client.ui.UCcsLoan;
import com.sunline.ccs.infrastructure.client.ui.UCcsRepaySchedule;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.ui.client.commons.CommonUiUtils;
import com.sunline.kylin.web.ark.client.helper.DecimalColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.IntegerColumnHelper;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinButton;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.res.Page;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.EventObjectHandler;
import com.sunline.ui.event.client.IClickEventListener;
import com.sunline.ui.grid.client.listener.ISelectRowEventListener;
import com.sunline.ui.tab.client.Tab;
import com.sunline.ui.tab.client.TabItemSetting;
import com.sunline.ui.tab.client.TabSetting;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * 贷款信息查询
 *
 */
@Singleton
public class QueryLoanInfo extends Page {
	@JsonProperty(value="MAP")
	private MapData map;   //赋值用的map

	public static final String PAGE_ID = "acct-3003";

	@Inject
	private QueryLoanInfoConstants constants;

	@Inject
	private ClientUtils clientUtils;

	@Inject
	private UCcsLoan uTmLoan;
	
	@Inject
	private UCcsRepaySchedule uTmSchedule;

	@Inject
	private UCcsCustomer uTmCustomer;

	private KylinGrid loanKylinGrid; // 贷款列表
	private KylinGrid scheduleKylinGrid; // 还款计划列表

	private KylinForm loanDetailForm; // 贷款详细信息form
	private KylinForm scheduleDetailForm; // 还款计划明细form

	private KylinButton fetchPlanListBtn;

	private TextColumnHelper guarantyIdItem;
	private EnumColumnHelper<IdType> idTypeItem;
	private TextColumnHelper idNoItem;
	private TextColumnHelper loanReceiptNbrItem;
	private TextColumnHelper contrNbrItem;

	private String submitGuarantyId;
	private String submitIdType;
	private String submitIdNo;
	private String loanReceiptNbr;
	private String contrNbr;
	private Integer loanId;

	private Tab detailTabSet;
	private TabItemSetting tmLoanDetailTab;
	private TabItemSetting planDetailTab;
	
	private TabItemSetting applyDetailTab;
	//申请单的form
	private KylinForm applyForm = new KylinForm();  //唯一不用先object的
	private TextColumnHelper APP_NO = new TextColumnHelper("APP_NO", "申请件编号", 70).readonly(true);
	private TextColumnHelper APP_SERIAL_NO = new TextColumnHelper("APP_SERIAL_NO" , "APP流水号" , 70).readonly(true);
	private TextColumnHelper UNIQUE_ID = new TextColumnHelper("UNIQUE_ID", "唯一标识" , 70).readonly(true);
	private TextColumnHelper CHANNEL = new TextColumnHelper( "CHANNEL","渠道" , 70).readonly(true);
	private TextColumnHelper COOPERATOR_ID = new TextColumnHelper("COOPERATOR_ID" ,"客户来源（合作方）" , 70).readonly(true);
	private TextColumnHelper PRODUCT_CD = new TextColumnHelper("PRODUCT_CD" ,"产品代码" , 70).readonly(true);
	private TextColumnHelper WORKFLOW_FLAG = new TextColumnHelper( "WORKFLOW_FLAG", "流程规则标识", 70).readonly(true);
	private TextColumnHelper NAME = new TextColumnHelper( "NAME", "姓名" , 70).readonly(true);
	private TextColumnHelper GENDER = new TextColumnHelper( "GENDER", "性别", 70).readonly(true);
	private TextColumnHelper ID_TYPE = new TextColumnHelper( "ID_TYPE", "证件类型", 70).readonly(true);
	private TextColumnHelper ID_NO = new TextColumnHelper( "ID_NO", "证件号码", 70).readonly(true);
	private TextColumnHelper ID_LAST_DATE = new TextColumnHelper( "ID_LAST_DATE", "证件到期日", 70).readonly(true);
	private TextColumnHelper ID_LONG_EFFECTIVE = new TextColumnHelper( "ID_LONG_EFFECTIVE", "证件长期有效", 70).readonly(true);
	private TextColumnHelper CELLPHONE = new TextColumnHelper( "CELLPHONE", "移动电话", 70).readonly(true);
	private DecimalColumnHelper MONTH_INCOME = new DecimalColumnHelper( "MONTH_INCOME", "月收入", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper OTHER_INCOME = new DecimalColumnHelper( "OTHER_INCOME", "其他收入", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper YEAR_INCOME = new DecimalColumnHelper( "YEAR_INCOME", "年收入", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper OTHER_LOAN = new DecimalColumnHelper( "OTHER_LOAN", "其他贷款", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private TextColumnHelper BANK_CODE = new TextColumnHelper( "BANK_CODE", "银行行号", 70).readonly(true);
	private TextColumnHelper BANK_CARD_NO = new TextColumnHelper( "BANK_CARD_NO", "银行卡号", 70).readonly(true);
	private TextColumnHelper BANK_PROVINCE_CODE = new TextColumnHelper( "BANK_PROVINCE_CODE", "银行分行省code", 70).readonly(true);
	private TextColumnHelper BANK_CITY_CODE = new TextColumnHelper( "BANK_CITY_CODE", "银行分行市code", 70).readonly(true);
	private TextColumnHelper HOME_PHONE = new TextColumnHelper( "HOME_PHONE", "住宅电话", 70).readonly(true);
	private TextColumnHelper EMAIL = new TextColumnHelper( "EMAIL", "电子邮箱", 70).readonly(true);
	private TextColumnHelper ABODE_STATE = new TextColumnHelper( "ABODE_STATE", "居住地址（省code）", 70).readonly(true);
	private TextColumnHelper ABODE_CITY = new TextColumnHelper( "ABODE_CITY", "居住地址（市code）", 70).readonly(true);
	private TextColumnHelper ABODE_ZONE = new TextColumnHelper( "ABODE_ZONE", "居住地址（区/县）", 70).readonly(true);
	private TextColumnHelper ABODE_DETAIL = new TextColumnHelper( "ABODE_DETAIL", "详细地址", 70).readonly(true);
	private TextColumnHelper QUALIFICATION = new TextColumnHelper( "QUALIFICATION", "教育状况", 70).readonly(true);
	private TextColumnHelper SOCIAL_IDENTITY = new TextColumnHelper( "SOCIAL_IDENTITY", "社会身份", 70).readonly(true);
	private TextColumnHelper WORK_STAND_FROM = new TextColumnHelper( "WORK_STAND_FROM", "参加工作日期", 70).readonly(true);
	private TextColumnHelper LENGTH_OF_SCHOOLING = new TextColumnHelper( "LENGTH_OF_SCHOOLING", "学制", 70).readonly(true);
	private IntegerColumnHelper YEARS_OF_WORK = new IntegerColumnHelper( "YEARS_OF_WORK", "工作年限", 70, -999999999, 999999999).readonly(true);
	private TextColumnHelper EMP_STAND_FROM = new TextColumnHelper( "EMP_STAND_FROM", "入学时间/入职日期", 70).readonly(true);
	private TextColumnHelper UNIT_NAME = new TextColumnHelper( "UNIT_NAME", "学校名称/工作单位", 70).readonly(true);
	private TextColumnHelper EMP_DEPAPMENT = new TextColumnHelper( "EMP_DEPAPMENT", "任职部门", 70).readonly(true);
	private TextColumnHelper EMP_POST = new TextColumnHelper( "EMP_POST", "职务", 70).readonly(true);
	private TextColumnHelper EMP_TYPE = new TextColumnHelper( "EMP_TYPE", "单位行业类别", 70).readonly(true);
	private TextColumnHelper EMP_STRUCTURE = new TextColumnHelper( "EMP_STRUCTURE", "单位性质", 70).readonly(true);
	private TextColumnHelper EMP_PHONE = new TextColumnHelper( "EMP_PHONE", "单位电话", 70).readonly(true);
	private TextColumnHelper EMP_PHONE_EXT_NUM = new TextColumnHelper( "EMP_PHONE_EXT_NUM", "单位电话分机号", 70).readonly(true);
	private TextColumnHelper EMP_PROVINCE = new TextColumnHelper( "EMP_PROVINCE", "单位所在省", 70).readonly(true);
	private TextColumnHelper EMP_CITY = new TextColumnHelper( "EMP_CITY", "单位所在市", 70).readonly(true);
	private TextColumnHelper EMP_ZONE = new TextColumnHelper( "EMP_ZONE", "单位所在区/县", 70).readonly(true);
	private TextColumnHelper EMP_ADD = new TextColumnHelper( "EMP_ADD", "单位地址（详细地址）", 70).readonly(true);
	private TextColumnHelper MARITAL_STATUS = new TextColumnHelper( "MARITAL_STATUS", "婚姻状况", 70).readonly(true);
	private TextColumnHelper HOUSE_CONDITION = new TextColumnHelper( "HOUSE_CONDITION", "住房状况", 70).readonly(true);
	private DecimalColumnHelper APP_LMT = new DecimalColumnHelper( "APP_LMT", "申请额度", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private IntegerColumnHelper LONG_TERM = new IntegerColumnHelper( "LONG_TERM", "贷款期数", 70, -999999999, 999999999).readonly(true);
	private TextColumnHelper SUB_PRODUCT_CD = new TextColumnHelper( "SUB_PRODUCT_CD", "信贷子产品编号", 70).readonly(true);
	private DecimalColumnHelper LOAN_RATE = new DecimalColumnHelper( "LOAN_RATE", "利率", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper FEE_RATE = new DecimalColumnHelper( "FEE_RATE", "贷款服务费率", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper FEE_AMOUNT = new DecimalColumnHelper( "FEE_AMOUNT", "贷款服务费", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper INSTALLMENT_FEE_RATE = new DecimalColumnHelper( "INSTALLMENT_FEE_RATE", "分期手续费率", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper INSTALLMENT_FEE_AMT = new DecimalColumnHelper( "INSTALLMENT_FEE_AMT", "分期手续费固定金额", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper INS_RATE = new DecimalColumnHelper( "INS_RATE", "保费月费率", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper INS_AMT = new DecimalColumnHelper( "INS_AMT", "保费月固定金额", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper AGENT_FEE_RATE = new DecimalColumnHelper( "AGENT_FEE_RATE", "月代收服务费率", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper AGENT_FEE_AMOUNT = new DecimalColumnHelper( "AGENT_FEE_AMOUNT", "代收服务费", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper LIFE_INSU_FEE_RATE = new DecimalColumnHelper( "LIFE_INSU_FEE_RATE", "月增值服务费率(寿险费率)", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper LIFE_INSURANCE_AMT = new DecimalColumnHelper( "LIFE_INSURANCE_AMT", "增值服务费(寿险金额)", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private TextColumnHelper JION_LIFE_INSURANCE = new TextColumnHelper( "JION_LIFE_INSURANCE", "增值服务项(是否加入寿险计划)", 70).readonly(true);
	private TextColumnHelper LOAN_PURPOSE = new TextColumnHelper( "LOAN_PURPOSE", "贷款用途", 70).readonly(true);
	private DecimalColumnHelper LOAN_FIXED_AMT = new DecimalColumnHelper( "LOAN_FIXED_AMT", "预计每期还款金额", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private TextColumnHelper SERIAL_NO = new TextColumnHelper( "SERIAL_NO", "影像上传的请求流水", 70).readonly(true);
	private IntegerColumnHelper RESIZE = new IntegerColumnHelper( "RESIZE", "影像尺寸", 70, -999999999, 999999999).readonly(true);
	
	private KylinForm insuranceForm = new KylinForm();   //INSURANCE_INFO	申请人参保信息	Object	N	二期新增，社保贷申请单要素
	private TextColumnHelper EXIST_EMP_EDW = new TextColumnHelper( "EXIST_EMP_EDW", "职工养老保险当前是否参保", 70).readonly(true);
	private TextColumnHelper EMP_EDW_BASE = new TextColumnHelper( "EMP_EDW_BASE", "职工养老保险的缴费基数", 70).readonly(true);
	private TextColumnHelper EMP_EDW_START_DT = new TextColumnHelper( "EMP_EDW_START_DT", "职工养老保险首次缴费日期", 70).readonly(true);
	private IntegerColumnHelper EMP_EDW_MONTHS = new IntegerColumnHelper( "EMP_EDW_MONTHS", "职工养老保险实际缴费月数", 70, -999999999, 999999999).readonly(true);
	private TextColumnHelper EXIST_EMP_MDC_INSU = new TextColumnHelper( "EXIST_EMP_MDC_INSU", "职工医疗保险当前是否参保", 70).readonly(true);
	private TextColumnHelper EMP_MDC_INSU_BASE = new TextColumnHelper( "EMP_MDC_INSU_BASE", "职工医疗保险的缴费基数", 70).readonly(true);
	private TextColumnHelper EMP_MDC_INSU_START_DT = new TextColumnHelper( "EMP_MDC_INSU_START_DT", "职工医疗保险首次缴费日期", 70).readonly(true);
	private IntegerColumnHelper EMP_MDC_INSU_MONTHS = new IntegerColumnHelper( "EMP_MDC_INSU_MONTHS", "职工医疗保险实际缴费月数", 70, -999999999, 999999999).readonly(true);
	private TextColumnHelper EXIST_INJU_INSU = new TextColumnHelper( "EXIST_INJU_INSU", "工伤保险当前是否参保", 70).readonly(true);
	private TextColumnHelper EXIST_UNEMP_INSU = new TextColumnHelper( "EXIST_UNEMP_INSU", "失业保险当前是否参保", 70).readonly(true);
	private TextColumnHelper EXIST_BIRTH_INSU = new TextColumnHelper( "EXIST_BIRTH_INSU", "生育保险当前是否参保", 70).readonly(true);
	private TextColumnHelper EXIST_RSDT_OLD_INSU = new TextColumnHelper( "EXIST_RSDT_OLD_INSU", "居民养老保险当前是否参保", 70).readonly(true);
	private TextColumnHelper RSDT_OLD_INSU_LVL = new TextColumnHelper( "RSDT_OLD_INSU_LVL", "居民养老保险的缴费档次", 70).readonly(true);
	private TextColumnHelper RSDT_OLD_INSU_START_DT = new TextColumnHelper( "RSDT_OLD_INSU_START_DT", "居民养老保险首次缴费日期", 70).readonly(true);
	private IntegerColumnHelper RSDT_OLD_INSU_YEARS = new IntegerColumnHelper( "RSDT_OLD_INSU_YEARS", "居民养老保险缴费年数", 70, -999999999, 999999999).readonly(true);
	private TextColumnHelper EXIST_RSDT_MDC_INSU = new TextColumnHelper( "EXIST_RSDT_MDC_INSU", "居民医疗保险当前是否参保", 70).readonly(true);
	private TextColumnHelper RSDT_MDC_INSU_LVL = new TextColumnHelper( "RSDT_MDC_INSU_LVL", "居民医疗保险的缴费档次", 70).readonly(true);
	private TextColumnHelper RSDT_MDC_INSU_START_DT = new TextColumnHelper( "RSDT_MDC_INSU_START_DT", "	居民医疗保险首次缴费日期", 70).readonly(true);
	private IntegerColumnHelper RSDT_MDC_INSU_YEARS = new IntegerColumnHelper( "RSDT_MDC_INSU_YEARS", "居民医疗保险缴费年数", 70, -999999999, 999999999).readonly(true);
	
	private KylinGrid socialGrid = new KylinGrid();   //SOCIAL_ACCOUNT_INFO	社交账号附加信息	Object[]	
	private TextColumnHelper SOCIAL_ACCOUNT_TYPE = new TextColumnHelper( "SOCIAL_ACCOUNT_TYPE", "社交账号类型", 70).readonly(true);
	private TextColumnHelper SOCIAL_ACCOUNT_NO = new TextColumnHelper( "SOCIAL_ACCOUNT_NO", "社交账号", 70).readonly(true);
	
	private KylinGrid contactGrid = new KylinGrid();   //1.2.3.48	CONTACT_INFO	联系人信息	Object[]	Y	
	private TextColumnHelper CONTACT_NAME = new TextColumnHelper( "CONTACT_NAME", "联系人中文姓名", 70).readonly(true);
	private TextColumnHelper CONTACT_GENDER = new TextColumnHelper( "CONTACT_GENDER", "联系人性别", 70).readonly(true);
	private TextColumnHelper CONTACT_RELATION = new TextColumnHelper( "CONTACT_RELATION", "联系人与申请人关系", 70).readonly(true);
	private TextColumnHelper CONTACT_MOBILE = new TextColumnHelper( "CONTACT_MOBILE", "联系人移动电话", 70).readonly(true);
	private TextColumnHelper IF_SAME_ADDRESS = new TextColumnHelper( "IF_SAME_ADDRESS", "是否与申请人同居住地", 70).readonly(true);
	private TextColumnHelper CONTACT_ADDRESS = new TextColumnHelper( "CONTACT_ADDRESS", "联系人地址", 70).readonly(true);
	
	private KylinGrid accessoryGrid = new KylinGrid();   //ACCESSORY_INFO	附加资料信息	Object[]	Y	
	private TextColumnHelper ACCESSORY_TYPE = new TextColumnHelper( "ACCESSORY_TYPE", "资料类型", 70).readonly(true);
	private IntegerColumnHelper ACCESSORY_COUNT = new IntegerColumnHelper( "ACCESSORY_COUNT", "资料数量", 70, -999999999, 999999999).readonly(true);
	private TextColumnHelper REQUIRED_FLAG = new TextColumnHelper( "REQUIRED_FLAG", "资料必传选传标识", 70).readonly(true);
	
	private TabItemSetting productTab;
	//商品的form
	private KylinForm orderForm = new KylinForm(); //ORDER_INFO	订单表	Object	N	三期新增，商品贷申请单要素
	private TextColumnHelper ORDER_ID = new TextColumnHelper( "ORDER_ID", "订单编号", 70).readonly(true);
	private TextColumnHelper REFERENCE_ID = new TextColumnHelper( "REFERENCE_ID", "商户订单号", 70).readonly(true);
	private TextColumnHelper COMPANY_ID = new TextColumnHelper( "COMPANY_ID", "商户编号", 70).readonly(true);
	private TextColumnHelper TERMINAL_ID = new TextColumnHelper( "TERMINAL_ID", "终端编号", 70).readonly(true);
	private TextColumnHelper RA_ID = new TextColumnHelper( "RA_ID", "销售人员编号", 70).readonly(true);
	private TextColumnHelper CUSTOMER_ID = new TextColumnHelper( "CUSTOMER_ID", "客户编号", 70).readonly(true);
	private TextColumnHelper PROMOTION_ID = new TextColumnHelper( "PROMOTION_ID", "营销活动编号", 70).readonly(true);
	private DecimalColumnHelper TOTAL_AMOUNT = new DecimalColumnHelper( "TOTAL_AMOUNT", "商品总金额", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private IntegerColumnHelper TOTAL_QUANTITY = new IntegerColumnHelper( "TOTAL_QUANTITY", "商品总件数", 70, -999999999, 999999999).readonly(true);
	private TextColumnHelper CONSIGNEE = new TextColumnHelper( "CONSIGNEE", "收件人", 70).readonly(true);
	private TextColumnHelper CONSIGNEE_PHONE = new TextColumnHelper( "CONSIGNEE_PHONE", "收件人电话", 70).readonly(true);
	private TextColumnHelper CONSIGNEE_ADDRESS = new TextColumnHelper( "CONSIGNEE_ADDRESS", "配送地址", 70).readonly(true);
	private TextColumnHelper INTERNAL_CODE = new TextColumnHelper( "INTERNAL_CODE", "内部代码", 70).readonly(true);
	private TextColumnHelper COLLECTION_DOWNPAYMENT = new TextColumnHelper( "COLLECTION_DOWNPAYMENT", "是否代收首付", 70).readonly(true);
	private DecimalColumnHelper DOWN_PAYMENT = new DecimalColumnHelper( "DOWN_PAYMENT", "首付金额", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper SUBSIDY_RATIO = new DecimalColumnHelper( "SUBSIDY_RATIO", "补贴比例", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private TextColumnHelper PAYMENT_TYPE = new TextColumnHelper( "PAYMENT_TYPE", "支付方式", 70).readonly(true);
	private TextColumnHelper ORDER_CREATE_TIME = new TextColumnHelper( "ORDER_CREATE_TIME", "下单时间", 70).readonly(true);
	private TextColumnHelper TRANSACTION_TIME = new TextColumnHelper( "TRANSACTION_TIME", "交易时间", 70).readonly(true);
	private KylinGrid orderProductGrid = new KylinGrid();   //ORDER_PRODUCT_INFO	订单商品信息	Object[]	N	多个商品
	private TextColumnHelper COMMODITY_ID = new TextColumnHelper( "COMMODITY_ID", "商品编号", 70).readonly(true);
	private TextColumnHelper CATEGORY_LEVEL_1_ID = new TextColumnHelper( "CATEGORY_LEVEL_1_ID", "一级品类", 70).readonly(true);
	private TextColumnHelper CATEGORY_LEVEL_1_NAME = new TextColumnHelper( "CATEGORY_LEVEL_1_NAME", "一级品类名称", 70).readonly(true);
	private TextColumnHelper CATEGORY_LEVEL_2_ID = new TextColumnHelper( "CATEGORY_LEVEL_2_ID", "二级品类", 70).readonly(true);
	private TextColumnHelper CATEGORY_LEVEL_2_NAME = new TextColumnHelper( "CATEGORY_LEVEL_2_NAME", "二级品类名称", 70).readonly(true);
	private TextColumnHelper CATEGORY_LEVEL_3_ID = new TextColumnHelper( "CATEGORY_LEVEL_3_ID", "三级品类", 70).readonly(true);
	private TextColumnHelper CATEGORY_LEVEL_3_NAME = new TextColumnHelper( "CATEGORY_LEVEL_3_NAME", "三级品类名称", 70).readonly(true);
	private TextColumnHelper BRAND = new TextColumnHelper( "BRAND", "品牌名称", 70).readonly(true);
	private TextColumnHelper COMMODITY_NAME = new TextColumnHelper( "COMMODITY_NAME", "商品名称", 70).readonly(true);
	private DecimalColumnHelper PRICE = new DecimalColumnHelper( "PRICE", "单价", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private IntegerColumnHelper QUANTITY = new IntegerColumnHelper( "QUANTITY", "件数", 70, -999999999, 999999999).readonly(true);
	
	//旅游 芒果中旅版本新增
	private TabItemSetting travelTab;
	private KylinForm travelOrderInfoForm = new KylinForm();    //TRAVEL_ORDER_INFO	旅游订单信息	Object	N	
	private TextColumnHelper RETAILER_ID = new TextColumnHelper("RETAILER_ID", "商户ID", 70).readonly(true);
	private TextColumnHelper RETAILER_NAME = new TextColumnHelper("RETAILER_NAME", "商户名", 70).readonly(true);
	private TextColumnHelper RA_ID1 = new TextColumnHelper("RA_ID","RA_ID",70).readonly(true);
	private TextColumnHelper ORDER_ID1 = new TextColumnHelper("ORDER_ID","商户订单号",70).readonly(true);
	private TextColumnHelper INTERNAL_CODE1 = new TextColumnHelper("INTERNAL_CODE","内部代码",70).readonly(true);
	private IntegerColumnHelper TOTAL_GOODS_NUM = new IntegerColumnHelper( "TOTAL_GOODS_NUM", "商品总件数", 70, -999999999, 999999999).readonly(true);
	private DecimalColumnHelper TOTAL_GOODS_PRICE = new DecimalColumnHelper( "TOTAL_GOODS_PRICE", "商品总金额", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private TextColumnHelper GOODS_NAME = new TextColumnHelper("GOODS_NAME","商品名",70).readonly(true);
	private TextColumnHelper GOODS_CATEGORY_CODE = new TextColumnHelper("GOODS_CATEGORY_CODE","商品范畴编码",70).readonly(true);
	private TextColumnHelper GOODS_CATEGORY = new TextColumnHelper("GOODS_CATEGORY","商品范畴",70).readonly(true);
	private TextColumnHelper GOODS_TYPE_CODE = new TextColumnHelper("GOODS_TYPE_CODE","商品类别编码",70).readonly(true);
	private TextColumnHelper GOODS_TYPE = new TextColumnHelper("GOODS_TYPE","商品类别",70).readonly(true);
	private DecimalColumnHelper DOWN_PAYMENT1 = new DecimalColumnHelper( "DOWN_PAYMENT", "首付金额", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private DecimalColumnHelper DOWN_PAYMENT_PERCENT = new DecimalColumnHelper( "DOWN_PAYMENT_PERCENT", "首付比例", 70, new BigDecimal(-999999999), new BigDecimal(999999999) ,  20).readonly(true);
	private TextColumnHelper ORIGIN = new TextColumnHelper("ORIGIN","出发地",70).readonly(true);
	private TextColumnHelper DESTINATION = new TextColumnHelper("DESTINATION","目的地",70).readonly(true);
	private TextColumnHelper DEPARTURE_TIME = new TextColumnHelper("DEPARTURE_TIME","出发时间",70).readonly(true);
	private TextColumnHelper RETURN_TIME = new TextColumnHelper("RETURN_TIME","返回时间",70).readonly(true);
	private IntegerColumnHelper TRAVEL_NUM = new IntegerColumnHelper( "TRAVEL_NUM", "出行人数", 70, -999999999, 999999999).readonly(true);
	private IntegerColumnHelper TRAVEL_KIDS_NUM = new IntegerColumnHelper( "TRAVEL_KIDS_NUM", "出行小孩个数", 70, -999999999, 999999999).readonly(true);
	private TextColumnHelper TRAVEL_TYPE = new TextColumnHelper("TRAVEL_TYPE","旅行方式",70).readonly(true);
	private TextColumnHelper IS_NEED_VISA = new TextColumnHelper("IS_NEED_VISA","是否需要签证",70).readonly(true);
	
	private KylinGrid travelPeerInfoGrid = new KylinGrid();   //TRAVEL_PEER_INFO	同行人信息（0个或多个）	Object[]	N	
	private TextColumnHelper PEER_USER_NAME = new TextColumnHelper("PEER_USER_NAME","同行人姓名",70).readonly(true);
	private TextColumnHelper PEER_ID_NO = new TextColumnHelper("PEER_ID_NO","同行人身份证号",70).readonly(true);
	private TextColumnHelper PEER_PHONE = new TextColumnHelper("PEER_PHONE","同行人手机号",70).readonly(true);
	private TextColumnHelper RELATIONSHIP = new TextColumnHelper("RELATIONSHIP","同行人与申请人关系",70).readonly(true);

	// 保存贷款的所有还款计划记录
	private Map<Integer, CcsRepaySchedule> tmPlanMap = new HashMap<Integer, CcsRepaySchedule>();

	private Tab mainTabSet;

	protected Integer selectedAcctNo;

	protected AccountType selectedAcctType;

	private KylinForm telSearchForm;

	private KylinForm searchForm;

	private KylinForm receiptnbrSearchForm;

	private KylinForm contrNbrForm;

	@Override
	public void refresh() {
		searchForm.getUi().clear();
		telSearchForm.getUi().clear();
		receiptnbrSearchForm.getUi().clear();
		loanDetailForm.getUi().clear();
		contrNbrForm.getUi().clear();

		scheduleDetailForm.getUi().clear();
		loanKylinGrid.clearData();
		scheduleKylinGrid.clearData();
		
		clearApplyDetailLayoutValues();
		clearProductLayoutValues();
		clearTravelLayoutValues();
	}

	@Override
	public IsWidget createPage() {
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("100%");
		vp.setHeight("100%");
		
		// ScrollPanel sp = new ScrollPanel();
		// sp.setHeight("100%");
		// 信用计划列表查询区域
		HorizontalPanel hLayout = new HorizontalPanel();
		hLayout.setWidth("98%");
		hLayout.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);

		loanKylinGrid = createLoanKylinGrid();
		hLayout.setHeight("350px");
		hLayout.add(loanKylinGrid);

		fetchPlanListBtn = ClientUtils.createSearchButton(new IClickEventListener() {
			@Override
			public void onClick() {
				if (loanKylinGrid.getUi().getSelectedRow().toMap().size() == 0) {
					Dialog.alert(constants.msgSelectNoAcct());
					return;
				}
				scheduleDetailForm.getUi().clear();
				loadPlanListFromServer();
			}
		});
		hLayout.add(fetchPlanListBtn);

		scheduleKylinGrid = createPlanKylinGrid();
		hLayout.add(scheduleKylinGrid);

		// 信用计划详细信息的显示区域
		vp.add(createSearchForm());
		vp.add(createSearchFormToReceiptNbr());
		vp.add(createSearchFormToTel());
		vp.add(createSearchFormToContrNbr());
		detailTabSet = createDetailInfoTabSet();
		vp.add(hLayout);
		vp.add(detailTabSet);
		return vp;
	}

	/**
	 * 创建保单号查询表单
	 * 
	 * @return
	 */
	@SuppressWarnings("all")
	private HorizontalPanel createSearchForm() {
		HorizontalPanel hPanel;
		searchForm = new KylinForm();
		searchForm.setWidth("98%");
		searchForm.setCol(3);
		guarantyIdItem = uTmLoan.GuarantyId();
		final KylinButton searchBtn = clientUtils.createSearchButton(new IClickEventListener() {
			@Override
			public void onClick() {
				receiptnbrSearchForm.setFieldValue(uTmLoan.DueBillNo().getName(), "");
				telSearchForm.setFieldValue(uTmCustomer.IdType().getName(), "");
				telSearchForm.setFieldValue(uTmCustomer.IdNo().getName(), "");
				contrNbrForm.setFieldValue(uTmLoan.ContrNbr().getName(), "");
				contrNbrForm.setFieldRequired(uTmLoan.GuarantyId().getName(), true);
				submitGuarantyId = searchForm.getFieldValue(uTmLoan.GuarantyId().getName());
				RPC.ajax("rpc/t3003Server/getLoanList", new RpcCallback<Data>() {
					@Override
					public void onSuccess(Data result) {
						updateLoanKylinGrid(result);
						scheduleDetailForm.getUi().clear();
						loanDetailForm.getUi().clear();
						scheduleKylinGrid.clearData();
						clearApplyDetailLayoutValues();
						clearProductLayoutValues();
						clearTravelLayoutValues();
					}
				}, submitGuarantyId, null, null, null, null);
			}
		});

		searchForm.setField(guarantyIdItem);
		hPanel = CommonUiUtils.lineLayoutForm(searchForm, searchBtn, null, null);
		return hPanel;
	}

	/**
	 * 
	 * @see 方法名：createSearchFormToTel
	 * @see 描述：创建证件类型/证件号码查询表单
	 * @see 创建日期：Jun 29, 20152:16:19 PM
	 * @author Liming.Feng
	 * 
	 * @return {@link com.sunline.kylin.web.ark.client.ui.KylinForm}
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@SuppressWarnings("static-access")
	private HorizontalPanel createSearchFormToTel() {
		HorizontalPanel hPanel;
		telSearchForm = new KylinForm();

		telSearchForm.setWidth("98%");
		telSearchForm.setCol(3);

		idTypeItem = uTmCustomer.IdType();
		idNoItem = uTmCustomer.IdNo();

		final KylinButton searchBtn = clientUtils.createSearchButton(new IClickEventListener() {
			@Override
			public void onClick() {
				receiptnbrSearchForm.setFieldValue(uTmLoan.DueBillNo().getName(), "");
				searchForm.setFieldValue(uTmLoan.GuarantyId().getName(), "");
				contrNbrForm.setFieldValue(uTmLoan.ContrNbr().getName(), "");
				telSearchForm.setFieldRequired(uTmCustomer.IdType().getName(), true);
				telSearchForm.setFieldRequired(uTmCustomer.IdNo().getName(), true);
				submitIdNo = telSearchForm.getFieldValue(idNoItem.getName());
				submitIdType = telSearchForm.getFieldValue(idTypeItem.getName());
				RPC.ajax("rpc/t3003Server/getLoanList", new RpcCallback<Data>() {
					@Override
					public void onSuccess(Data result) {
						updateLoanKylinGrid(result);
						scheduleDetailForm.getUi().clear();
						loanDetailForm.getUi().clear();
						scheduleKylinGrid.clearData();
						clearApplyDetailLayoutValues();
						clearProductLayoutValues();
						clearTravelLayoutValues();
					}
				}, null, submitIdType, submitIdNo, null, null);
			}
		});
		telSearchForm.setField(idTypeItem, idNoItem);

		hPanel = CommonUiUtils.lineLayoutForm(telSearchForm, searchBtn, null, null);

		return hPanel;

	}

	/**
	 * 
	 * @see 方法名：createSearchFormToReceiptNbr
	 * @see 描述：
	 * @see 创建日期：Jun 29, 20152:16:06 PM
	 * @author Liming.Feng
	 * 
	 * @return
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@SuppressWarnings("static-access")
	private HorizontalPanel createSearchFormToReceiptNbr() {
		HorizontalPanel hPanel;

		receiptnbrSearchForm = new KylinForm();
		receiptnbrSearchForm.setWidth("98%");
		receiptnbrSearchForm.setCol(3);
		loanReceiptNbrItem = uTmLoan.DueBillNo();

		final KylinButton searchBtn = clientUtils.createSearchButton(new IClickEventListener() {

			@Override
			public void onClick() {
				receiptnbrSearchForm.setFieldRequired(uTmLoan.DueBillNo().getName(), true);
				loanReceiptNbr = receiptnbrSearchForm.getFieldValue(loanReceiptNbrItem.getName());
				telSearchForm.setFieldValue(uTmCustomer.IdType().getName(), "");
				telSearchForm.setFieldValue(uTmCustomer.IdNo().getName(), "");
				searchForm.setFieldValue(uTmLoan.GuarantyId().getName(), "");
				RPC.ajax("rpc/t3003Server/getLoanList", new RpcCallback<Data>() {
					@Override
					public void onSuccess(Data result) {
						updateLoanKylinGrid(result);
						scheduleDetailForm.getUi().clear();
						loanDetailForm.getUi().clear();
						scheduleKylinGrid.clearData();
						clearApplyDetailLayoutValues();
						clearProductLayoutValues();
						clearTravelLayoutValues();
					}
				}, null, null, null, loanReceiptNbr, null);
			}
		});

		receiptnbrSearchForm.setField(loanReceiptNbrItem);
		hPanel = CommonUiUtils.lineLayoutForm(receiptnbrSearchForm, searchBtn, null, null);
		return hPanel;
	}

	private HorizontalPanel createSearchFormToContrNbr() {
		HorizontalPanel hPanel;

		contrNbrForm = new KylinForm();
		contrNbrForm.setWidth("98%");
		contrNbrForm.setCol(3);
		contrNbrItem = uTmLoan.ContrNbr();

		final KylinButton searchBtn = ClientUtils.createSearchButton(new IClickEventListener() {

			@Override
			public void onClick() {
				contrNbrForm.setFieldRequired(uTmLoan.ContrNbr().getName(), true);
				contrNbr = contrNbrForm.getFieldValue(contrNbrItem.getName());
				receiptnbrSearchForm.setFieldValue(uTmLoan.DueBillNo().getName(), "");
				telSearchForm.setFieldValue(uTmCustomer.IdType().getName(), "");
				telSearchForm.setFieldValue(uTmCustomer.IdNo().getName(), "");
				searchForm.setFieldValue(uTmLoan.GuarantyId().getName(), "");
				RPC.ajax("rpc/t3003Server/getLoanList", new RpcCallback<Data>() {
					@Override
					public void onSuccess(Data result) {
						updateLoanKylinGrid(result);
						scheduleDetailForm.getUi().clear();
						loanDetailForm.getUi().clear();
						scheduleKylinGrid.clearData();
						clearApplyDetailLayoutValues();
						clearProductLayoutValues();
						clearTravelLayoutValues();
					}
				}, null, null, null, null, contrNbr);
			}
		});

		contrNbrForm.setField(contrNbrItem);
		hPanel = CommonUiUtils.lineLayoutForm(contrNbrForm, searchBtn, null, null);
		return hPanel;
	}

	/**
	 * 贷款列表
	 * 
	 * @return
	 */
	private KylinGrid createLoanKylinGrid() {
		final KylinGrid KylinGrid = new KylinGrid();

		KylinGrid.setHeight("350px");
		KylinGrid.setWidth("100%");
		DecimalColumnHelper loanIdItem = uTmLoan.LoanId();

		TextColumnHelper LoanCodeItem = uTmLoan.LoanCode();

		KylinGrid.setColumns(loanIdItem, uTmLoan.AcctNbr(),uTmLoan.AcctType(), LoanCodeItem, uTmLoan.LoanType());
		KylinGrid.loadData();
		KylinGrid.getSetting().onSelectRow(new ISelectRowEventListener() {

			@Override
			public void selectRow(MapData rowdata, String rowid, EventObjectHandler rowobj) {

				if (null == rowdata) {
					clearLoanInfo();
					return;
				}
				RPC.ajax("rpc/t3003Server/getApplyDetail", new RpcCallback<Data>() {
								@Override
								public void onSuccess(Data result) {
									if(result.toString().equals("{}")){
										Dialog.tipNotice("在审批系统查询不到该合同对应的申请单商品数据及旅游订单信息");
										return ;
									}
									updateApplyDetailLayoutView(result);
									updateProductLayoutView(result);
									updateTravelView(result);
								}
							}, rowdata.getString(uTmLoan.AcctNbr().getName()) , rowdata.getString(uTmLoan.AcctType().getName()));
				selectedAcctNo = rowdata.getInteger(CcsAcct.P_AcctNbr);
				selectedAcctType = AccountType.valueOf(rowdata.getString(CcsAcct.P_AcctType));
				loanId = rowdata.getInteger(uTmLoan.LoanId().getName());
				String selectedTab = detailTabSet.getSelectedTabItemID();
				if (!"tmLoanDetailTab".equals(selectedTab)) {
					detailTabSet.selectTabItem("tmLoanDetailTab");
				}
				loanDetailForm.setFormData(rowdata);
			}
		});

		return KylinGrid;
	}

	/**
	 * 还款计划列表
	 * 
	 * @return
	 */
	private KylinGrid createPlanKylinGrid() {
		final KylinGrid kylinGrid = new KylinGrid();
		kylinGrid.setHeight("350px");
		kylinGrid.setWidth("100%");
		kylinGrid.setColumns(
				uTmSchedule.ScheduleId().setColumnWidth(150),
				uTmSchedule.CurrTerm()	.setColumnWidth(100),
				uTmSchedule.LoanTermPrin().setColumnWidth(100),
				uTmSchedule.LoanTermInt().setColumnWidth(100),
				uTmSchedule.LoanTermFee().setColumnWidth(100),
				uTmSchedule.LoanInsuranceAmt().setColumnWidth(100),
				uTmSchedule.LoanLifeInsuAmt().setColumnWidth(100),
				uTmSchedule.LoanSvcFee().setColumnWidth(100),          //新增服务费  2015.12.02  chenpy
				uTmSchedule.LoanPrepayPkgAmt().setColumnWidth(100),
				uTmSchedule.LoanReplaceSvcFee().setColumnWidth(100)    //新增应还代收服务费  20151224  chenpy
				);
		
		kylinGrid.getSetting().onSelectRow(new ISelectRowEventListener() {
			
			@Override
			public void selectRow(MapData rowdata, String rowid, EventObjectHandler rowobj) {
				if (null == rowdata) {
					scheduleDetailForm.getUi().clear();
					return;
				}
				detailTabSet.selectTabItem("planDetailTab");
				scheduleDetailForm.setFormData(rowdata);
			}
		});
		
		return kylinGrid;
	}

	/**
	 * 创建贷款明细和还款计划详细显示的TAB
	 * 
	 * @return
	 */
	private Tab createDetailInfoTabSet() {
		TabSetting tabSetting = new TabSetting().dblClickToClose(true).dragToMove(true)
				.showSwitch(true);
		mainTabSet = new Tab(tabSetting);
		mainTabSet.setWidth("100%");
		mainTabSet.setHeight("270px");

		loanDetailForm = createTmLoanForm();
		ScrollPanel loanDetailPanel = new ScrollPanel();
		loanDetailPanel.add(loanDetailForm);
		loanDetailPanel.setHeight("270px");
		loanDetailPanel.setWidth("98%");
		tmLoanDetailTab = new TabItemSetting("tmLoanDetailTab", constants.loanBatchInfoTitle());
		mainTabSet.addItem(tmLoanDetailTab, loanDetailPanel);
		
		scheduleDetailForm = createPlanForm();
		ScrollPanel scheduleDetailPanel = new ScrollPanel();
		scheduleDetailPanel.add(scheduleDetailForm);
		scheduleDetailPanel.setHeight("270px");
		scheduleDetailPanel.setWidth("98%");
		planDetailTab = new TabItemSetting("planDetailTab", constants.planInfoTitle());
		mainTabSet.addItem(planDetailTab, scheduleDetailPanel);
		
		ScrollPanel applyDetailPanel = new ScrollPanel();
		applyDetailPanel.add(createApplyDetailLayout());
		applyDetailPanel.setHeight("270px");
		applyDetailPanel.setWidth("98%");
		applyDetailTab = new TabItemSetting("applyDetailTab", constants.applyDetailTabTitle());
		mainTabSet.addItem(applyDetailTab, applyDetailPanel);
		
		ScrollPanel productPanel =  new ScrollPanel();
		productPanel.add(createProductLayoutCanvas());
		productPanel.setHeight("270px");
		productPanel.setWidth("98%");
		productTab = new TabItemSetting("productTab", constants.productTabTitle());
		mainTabSet.addItem(productTab, productPanel);
		
		ScrollPanel travelPanel = new ScrollPanel();
		travelPanel.add(createTravelLayout());
		travelPanel.setHeight("270px");
		travelPanel.setWidth("98%");
		travelTab = new TabItemSetting("travelTab", "旅游订单信息");
		mainTabSet.addItem(travelTab, travelPanel);
		
		return mainTabSet;
	}

	/**
	 * 创建还款计划明细form
	 * 
	 * @return
	 */
	private KylinForm createPlanForm() {
		KylinForm form = new KylinForm();
		form.setWidth("98%");
		form.setHeight("270px");
		DecimalColumnHelper loanIdItem = uTmLoan.LoanId().readonly(true);
		form.getSetting().labelWidth(140);
		form.setField(uTmSchedule.ScheduleId().readonly(true), 
				uTmSchedule.Org().readonly(true),
				loanIdItem.readonly(true), 
				uTmSchedule.AcctNbr().readonly(true), 
				uTmSchedule.AcctType().readonly(true), 
				uTmSchedule.LogicCardNbr().readonly(true),
				uTmSchedule.CardNbr().readonly(true), 
				uTmSchedule.CurrTerm().readonly(true),
				uTmSchedule.LoanTermPrin().readonly(true),
				uTmSchedule.LoanTermFee().readonly(true),
				uTmSchedule.LoanTermInt().readonly(true),
				uTmSchedule.LoanPmtDueDate().readonly(true),
				uTmSchedule.LoanGraceDate().readonly(true), 
				uTmSchedule.LoanInsuranceAmt().readonly(true),
				uTmSchedule.LoanStampdutyAmt().readonly(true), 
				uTmSchedule.LoanLifeInsuAmt().readonly(true),
				uTmSchedule.LoanPrepayPkgAmt().readonly(true),
				uTmSchedule.LoanReplaceSvcFee().readonly(true) 			//应还代收服务费
				);

		return form;
	}

	/**
	 * 创建贷款明细form
	 * 
	 * @return
	 */
	private KylinForm createTmLoanForm() {
		KylinForm form = new KylinForm();
		form.setWidth("98%");
		form.setHeight("270px");

		form.getSetting().labelWidth(140);
		form.setField(//基本信息
				uTmLoan.DueBillNo().readonly(true),
				uTmLoan.ContrNbr().readonly(true),
				uTmLoan.GuarantyId().readonly(true),
				uTmLoan.Org().readonly(true), 
				uTmLoan.LoanId().readonly(true), 
				uTmLoan.AcctNbr().readonly(true), 
				uTmLoan.AcctType().readonly(true),
				uTmLoan.RefNbr().readonly(true), 
				uTmLoan.LogicCardNbr().readonly(true), 
				uTmLoan.CardNbr().readonly(true), 
				uTmLoan.RegisterDate().readonly(true), 
				uTmLoan.RequestTime().readonly(true), 
				uTmLoan.LoanType().readonly(true), 
				uTmLoan.LoanStatus().readonly(true),
				uTmLoan.LastLoanStatus().readonly(true), 
				uTmLoan.LoanInitTerm().readonly(true),
				uTmLoan.CurrTerm().readonly(true), 
				uTmLoan.RemainTerm().readonly(true),
				uTmLoan.ActiveDate().readonly(true), 
				uTmLoan.PaidOutDate().readonly(true),
				uTmLoan.LoanCurrBal().readonly(true), 
				uTmLoan.OrigTxnAmt().readonly(true), 
				uTmLoan.OrigTransDate().readonly(true), 
				uTmLoan.OrigAuthCode().readonly(true),
				uTmLoan.LoanCode().readonly(true), 
				uTmLoan.RegisterId().readonly(true), 
				uTmLoan.ExtendDate().readonly(true),
				uTmLoan.OverdueDate().readonly(true), 
				uTmLoan.TerminalDate().readonly(true),
				uTmLoan.TerminalReasonCd().readonly(true), 
				uTmLoan.LoanExpireDate().readonly(true),
				uTmLoan.LoanCode().readonly(true), 
				uTmLoan.PaymentHst().readonly(true), 
				uTmLoan.PastShortenCnt().readonly(true), 
				uTmLoan.AdvPmtAmt().readonly(true),
				uTmLoan.LastActionDate().readonly(true), 
				uTmLoan.LastActionType().readonly(true),
				uTmLoan.PastExtendCnt().readonly(true),
				//CPD&DPD
				uTmLoan.CtdRepayAmt().readonly(true), 
				uTmLoan.CpdBeginDate().readonly(true),
				uTmLoan.MaxCpd().readonly(true),
				uTmLoan.MaxCpdDate().readonly(true),
				uTmLoan.MaxDpd().readonly(true),
				uTmLoan.MaxDpdDate().readonly(true),
				//本金
				uTmLoan.LoanInitPrin().readonly(true), 
				uTmLoan.UnstmtPrin().readonly(true),
				uTmLoan.PaidPrincipal().readonly(true),
				uTmLoan.LoanFixedPmtPrin().readonly(true), 
				uTmLoan.LoanFirstTermPrin().readonly(true),
				uTmLoan.LoanFinalTermPrin().readonly(true), 
				uTmLoan.LoanBalXfrout().readonly(true), 
				uTmLoan.LoanBalXfrin().readonly(true), 
				uTmLoan.LoanPrinXfrout().readonly(true),
				uTmLoan.LoanPrinXfrin().readonly(true), 
				uTmLoan.ExtendInitPrin().readonly(true), 
				uTmLoan.BefExtendFixedPmtPrin().readonly(true), 
				uTmLoan.BefExtendFirstTermPrin().readonly(true), 
				uTmLoan.BefExtendFinalTermPrin().readonly(true), 
				//利息&罚息&复利&
				uTmLoan.LastPenaltyDate().readonly(true),
				uTmLoan.PaidInterest().readonly(true),
				uTmLoan.InterestRate().readonly(true), 
				uTmLoan.PenaltyRate().readonly(true),
				uTmLoan.CompoundRate().readonly(true), 
				uTmLoan.FloatRate().readonly(true), 
				//分期手续费
				uTmLoan.LoanFeeMethod().readonly(true),
				uTmLoan.LoanInitFee().readonly(true),
				uTmLoan.UnstmtFee().readonly(true),
				uTmLoan.PaidFee().readonly(true),
				uTmLoan.LoanFixedFee().readonly(true), 
				uTmLoan.LoanFirstTermFee().readonly(true),
				uTmLoan.LoanFinalTermFee().readonly(true), 
				uTmLoan.FeeAmt().readonly(true),            
				uTmLoan.FeeRate().readonly(true),
				uTmLoan.LoanFeeXfrout().readonly(true),
				uTmLoan.LoanFeeXfrin().readonly(true), 
				uTmLoan.BefExtendInitFee().readonly(true),
				uTmLoan.BefExtendFixedFee().readonly(true), 
				uTmLoan.BefExtendFirstTermFee().readonly(true), 
				uTmLoan.BefExtendFinalTermFee().readonly(true),
				uTmLoan.ExtendFirstTermFee().readonly(true), 
				//手续费
				uTmLoan.PaidSvcFee().readonly(true),
				uTmLoan.UnstmtSvcFee().readonly(true),
				uTmLoan.PastSvcFee().readonly(true),
				uTmLoan.SvcfeeMethod().readonly(true),
				//印花税
				uTmLoan.StampdutyRate().readonly(true), 
				uTmLoan.StampdutyMethod().readonly(true),
				uTmLoan.StampAmt().readonly(true),
				uTmLoan.UnstmtStampdutyAmt().readonly(true),
				//保费
				uTmLoan.InsuranceRate().setDisplay("保险费率").readonly(true),       
				uTmLoan.InsuranceAmt().readonly(true), 
				uTmLoan.UnstmtInsuranceAmt().readonly(true),
				uTmLoan.LoanInsuranceAmt().readonly(true), 
				uTmLoan.PaidInsuranceAmt().readonly(true), 
				uTmLoan.LoanInsFeeMethod().readonly(true),
				uTmLoan.InsAmt().readonly(true),
				//寿险计划包
				uTmLoan.LifeInsuFeeAmt().readonly(true),
				uTmLoan.TotLifeInsuAmt().readonly(true),
				uTmLoan.UnstmtLifeInsuAmt().readonly(true),
				uTmLoan.PastLifeInsuAmt().readonly(true),
				uTmLoan.PaidLifeInsuAmt().readonly(true),
				uTmLoan.LifeInsuFeeRate().readonly(true),
				uTmLoan.LifeInsuFeeMethod().readonly(true),
				//提前还款计划包
				uTmLoan.UnstmtPrepayPkgAmt().readonly(true),
				uTmLoan.PrepayPkgFeeMethod().readonly(true),
				uTmLoan.PrepayPkgFeeRate().readonly(true),
				uTmLoan.PaidPrepayPkgAmt().readonly(true),
				uTmLoan.PastPrepayPkgAmt().readonly(true),
				uTmLoan.PrepayPkgFeeAmt().readonly(true),
				uTmLoan.TotPrepayPkgAmt().readonly(true),
				//服务费
				uTmLoan.InstallmentFeeAmt().readonly(true),
				uTmLoan.InstallmentFeeRate().readonly(true),
				//代收服务费
				uTmLoan.ReplaceSvcFeeRate().readonly(true),
				uTmLoan.ReplaceSvcFeeAmt().readonly(true),
				uTmLoan.ReplaceSvcFeeMethod().readonly(true),
				uTmLoan.TotReplaceSvcFee().readonly(true),
				uTmLoan.UnstmtReplaceSvcFee().readonly(true),
				uTmLoan.PastReplaceSvcFee().readonly(true),
				uTmLoan.PaidReplaceSvcFee().readonly(true),
				uTmLoan.PremiumAmt().readonly(true),	//添加趸交费、是否退还趸交费
				uTmLoan.PremiumInd().readonly(true),
				uTmLoan.ReplacePenaltyRate().readonly(true), 		//代收罚息利率
				uTmLoan.PrepayPkgInd().readonly(true),
				uTmLoan.ApplyTerm().readonly(true),
				uTmLoan.ApplyDelayTerm().readonly(true),
				uTmLoan.AccuApplyCount().readonly(true),
				uTmLoan.AccuDelayCount().readonly(true),
				uTmLoan.PayDateTerm().readonly(true),
				uTmLoan.PayDateBegDate().readonly(true),
				uTmLoan.PayDateAccu().readonly(true),
				uTmLoan.CompensateAmtSum().readonly(true),
				uTmLoan.CompensateCount().readonly(true),
				uTmLoan.CompensateRefundAmtSum().readonly(true),
				uTmLoan.CompensateRefundCount().readonly(true)
				);
		
		return form;
	}

	/**
	 * 加载还款计划列表
	 */
	private void loadPlanListFromServer() {

		RPC.ajax("rpc/t3003Server/getScheduleList", new RpcCallback<Data>() {
			@SuppressWarnings("all")
			@Override
			public void onSuccess(Data result) {
				// 更新信用计划列表
				QueryLoanInfo.this.updatePlanKylinGrid(result);

				tmPlanMap.clear();
				List<CcsPlan> tresult = (List<CcsPlan>) result.asListData().toList();
			}
		}, loanId);
	}

	protected void updateLoanKylinGrid(Data data) {
		MapData result = new MapData();
		result.put("rows", data);
		result.put("total", data.asListData().size());
		loanKylinGrid.loadData(result);
	}

	protected void updatePlanKylinGrid(Data data) {
		MapData result = new MapData();
		result.put("rows", data);
		result.put("total", data.asListData().size());
		scheduleKylinGrid.loadData(result);
	}

	/**
	 * 清除与账户相关的信息
	 */
	private void clearLoanInfo() {
		loanDetailForm.getUi().clear();

		// selectedAcctNo = null;
		loanId = null;
		scheduleKylinGrid.loadData();
	}
	
	//申请单的Tab
	private  IsWidget createApplyDetailLayout() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("98%");
		panel.setHeight("270px");
		
		applyForm.setField(
				APP_NO.setGroup("申请单信息"),
				APP_SERIAL_NO,
				UNIQUE_ID,
				CHANNEL,
				COOPERATOR_ID,
				PRODUCT_CD,
				WORKFLOW_FLAG,
				NAME,
				GENDER,
				ID_TYPE,
				ID_NO,
				ID_LAST_DATE,
				ID_LONG_EFFECTIVE,
				CELLPHONE,
				MONTH_INCOME,
				OTHER_INCOME,
				YEAR_INCOME,
				OTHER_LOAN,
				BANK_CODE,
				BANK_CARD_NO,
				BANK_PROVINCE_CODE,
				BANK_CITY_CODE,
				HOME_PHONE,
				EMAIL,
				ABODE_STATE,
				ABODE_CITY,
				ABODE_ZONE,
				ABODE_DETAIL,
				QUALIFICATION,
				SOCIAL_IDENTITY,
				WORK_STAND_FROM,
				LENGTH_OF_SCHOOLING,
				YEARS_OF_WORK,
				EMP_STAND_FROM,
				UNIT_NAME,
				EMP_DEPAPMENT,
				EMP_POST,
				EMP_TYPE,
				EMP_STRUCTURE,
				EMP_PHONE,
				EMP_PHONE_EXT_NUM,
				EMP_PROVINCE,
				EMP_CITY,
				EMP_ZONE,
				EMP_ADD,
				MARITAL_STATUS,
				HOUSE_CONDITION,
				APP_LMT,
				LONG_TERM,
				SUB_PRODUCT_CD,
				LOAN_RATE,
				FEE_RATE,
				FEE_AMOUNT,
				INSTALLMENT_FEE_RATE,
				INSTALLMENT_FEE_AMT,
				INS_RATE,
				INS_AMT,
				AGENT_FEE_RATE,
				AGENT_FEE_AMOUNT,
				LIFE_INSU_FEE_RATE,
				LIFE_INSURANCE_AMT,
				JION_LIFE_INSURANCE,
				LOAN_PURPOSE,
				LOAN_FIXED_AMT,
				SERIAL_NO,
				RESIZE
				);
		applyForm.getSetting().labelWidth(140);
		
		insuranceForm.setField(
				EXIST_EMP_EDW.setGroup("申请人参保信息"),
				EMP_EDW_BASE,
				EMP_EDW_START_DT,
				EMP_EDW_MONTHS,
				EXIST_EMP_MDC_INSU,
				EMP_MDC_INSU_BASE,
				EMP_MDC_INSU_START_DT,
				EMP_MDC_INSU_MONTHS,
				EXIST_INJU_INSU,
				EXIST_UNEMP_INSU,
				EXIST_BIRTH_INSU,
				EXIST_RSDT_OLD_INSU,
				RSDT_OLD_INSU_LVL,
				RSDT_OLD_INSU_START_DT,
				RSDT_OLD_INSU_YEARS,
				EXIST_RSDT_MDC_INSU,
				RSDT_MDC_INSU_LVL,
				RSDT_MDC_INSU_START_DT,
				RSDT_MDC_INSU_YEARS
				);
		insuranceForm.getSetting().labelWidth(140);
		
		socialGrid.getSetting().usePager(false);
		socialGrid.getSetting().checkbox(false);
		socialGrid.setWidth(250);
		socialGrid.setHeight(150);
		socialGrid.setTitle("社交账号附加信息");
		
		socialGrid.setColumns(
				SOCIAL_ACCOUNT_TYPE.setColumnWidth(100),
				SOCIAL_ACCOUNT_NO.setColumnWidth(100)
				);
		
		contactGrid.getSetting().usePager(false);
		contactGrid.getSetting().checkbox(false);
		contactGrid.setWidth(650);
		contactGrid.setHeight(150);
		contactGrid.setTitle("联系人信息");
		contactGrid.setColumns(
				CONTACT_NAME.setColumnWidth(100),
				CONTACT_GENDER.setColumnWidth(100),
				CONTACT_RELATION.setColumnWidth(100),
				CONTACT_MOBILE.setColumnWidth(100),
				IF_SAME_ADDRESS.setColumnWidth(100),
				CONTACT_ADDRESS.setColumnWidth(100)
				);
		
		accessoryGrid.getSetting().usePager(false);
		accessoryGrid.getSetting().checkbox(false);
		accessoryGrid.setWidth(350);
		accessoryGrid.setHeight(150);
		accessoryGrid.setTitle("附加资料信息");
		accessoryGrid.setColumns(
				ACCESSORY_TYPE.setColumnWidth(100),
				ACCESSORY_COUNT.setColumnWidth(100),
				REQUIRED_FLAG.setColumnWidth(100)
				);
		
		panel.add(applyForm);
		panel.add(insuranceForm);
		panel.add(socialGrid);
		panel.add(contactGrid);
		panel.add(accessoryGrid);
		
		return panel;
	}
	
	public void updateApplyDetailLayoutView(Data result) {
		if(result == null){
			return ;
		}
		this.map = result.asMapData();
		applyForm.setFieldValue("APP_NO", map.getString("APP_NO"));
		applyForm.setFieldValue("UNIQUE_ID", map.getString("UNIQUE_ID"));
		applyForm.setFieldValue("CHANNEL", map.getString("CHANNEL"));
		applyForm.setFieldValue("COOPERATOR_ID", map.getString("COOPERATOR_ID"));
		applyForm.setFieldValue("PRODUCT_CD", map.getString("PRODUCT_CD"));
		applyForm.setFieldValue("WORKFLOW_FLAG", map.getString("WORKFLOW_FLAG"));
		applyForm.setFieldValue("NAME", map.getString("NAME"));
		applyForm.setFieldValue("GENDER", map.getString("GENDER"));
		applyForm.setFieldValue("ID_TYPE", map.getString("ID_TYPE"));
		applyForm.setFieldValue("ID_NO", map.getString("ID_NO"));
		applyForm.setFieldValue("ID_LAST_DATE", map.getString("ID_LAST_DATE"));
		applyForm.setFieldValue("ID_LONG_EFFECTIVE", map.getString("ID_LONG_EFFECTIVE"));
		applyForm.setFieldValue("CELLPHONE", map.getString("CELLPHONE"));
		applyForm.setFieldValue("MONTH_INCOME", map.getString("MONTH_INCOME"));
		applyForm.setFieldValue("OTHER_INCOME", map.getString("OTHER_INCOME"));
		applyForm.setFieldValue("YEAR_INCOME", map.getString("YEAR_INCOME"));
		applyForm.setFieldValue("OTHER_LOAN", map.getString("OTHER_LOAN"));
		applyForm.setFieldValue("BANK_CODE", map.getString("BANK_CODE"));
		applyForm.setFieldValue("BANK_CARD_NO", map.getString("BANK_CARD_NO"));
		applyForm.setFieldValue("BANK_PROVINCE_CODE", map.getString("BANK_PROVINCE_CODE"));
		applyForm.setFieldValue("BANK_CITY_CODE", map.getString("BANK_CITY_CODE"));
		applyForm.setFieldValue("HOME_PHONE", map.getString("HOME_PHONE"));
		applyForm.setFieldValue("EMAIL", map.getString("EMAIL"));
		applyForm.setFieldValue("ABODE_STATE", map.getString("ABODE_STATE"));
		applyForm.setFieldValue("ABODE_CITY", map.getString("ABODE_CITY"));
		applyForm.setFieldValue("ABODE_ZONE", map.getString("ABODE_ZONE"));
		applyForm.setFieldValue("ABODE_DETAIL", map.getString("ABODE_DETAIL"));
		applyForm.setFieldValue("QUALIFICATION", map.getString("QUALIFICATION"));
		applyForm.setFieldValue("SOCIAL_IDENTITY", map.getString(""));
		applyForm.setFieldValue("WORK_STAND_FROM", map.getString("WORK_STAND_FROM"));
		applyForm.setFieldValue("LENGTH_OF_SCHOOLING", map.getString("LENGTH_OF_SCHOOLING"));
		applyForm.setFieldValue("YEARS_OF_WORK", map.getString("YEARS_OF_WORK"));
		applyForm.setFieldValue("EMP_STAND_FROM", map.getString("EMP_STAND_FROM"));
		applyForm.setFieldValue("UNIT_NAME", map.getString("UNIT_NAME"));
		applyForm.setFieldValue("EMP_DEPAPMENT", map.getString("EMP_DEPAPMENT"));
		applyForm.setFieldValue("EMP_POST", map.getString("EMP_POST"));
		applyForm.setFieldValue("EMP_TYPE", map.getString("EMP_TYPE"));
		applyForm.setFieldValue("EMP_STRUCTURE", map.getString("EMP_STRUCTURE"));
		applyForm.setFieldValue("EMP_PHONE", map.getString("EMP_PHONE"));
		applyForm.setFieldValue("EMP_PHONE_EXT_NUM", map.getString("EMP_PHONE_EXT_NUM"));
		applyForm.setFieldValue("EMP_PROVINCE", map.getString("EMP_PROVINCE"));
		applyForm.setFieldValue("EMP_CITY", map.getString("EMP_CITY"));
		applyForm.setFieldValue("EMP_ZONE", map.getString("EMP_ZONE"));
		applyForm.setFieldValue("EMP_ADD", map.getString("EMP_ADD"));
		applyForm.setFieldValue("MARITAL_STATUS", map.getString("MARITAL_STATUS"));
		applyForm.setFieldValue("HOUSE_CONDITION", map.getString("HOUSE_CONDITION"));
		applyForm.setFieldValue("APP_LMT", map.getString("APP_LMT"));
		applyForm.setFieldValue("LONG_TERM", map.getString("LONG_TERM"));
		applyForm.setFieldValue("SUB_PRODUCT_CD", map.getString("SUB_PRODUCT_CD"));
		applyForm.setFieldValue("LOAN_RATE", map.getString("LOAN_RATE"));
		applyForm.setFieldValue("FEE_RATE", map.getString("FEE_RATE"));
		applyForm.setFieldValue("FEE_AMOUNT", map.getString("FEE_AMOUNT"));
		applyForm.setFieldValue("INSTALLMENT_FEE_RATE", map.getString("INSTALLMENT_FEE_RATE"));
		applyForm.setFieldValue("INSTALLMENT_FEE_AMT", map.getString("INSTALLMENT_FEE_AMT"));
		applyForm.setFieldValue("INS_RATE", map.getString("INS_RATE"));
		applyForm.setFieldValue("INS_AMT", map.getString("INS_AMT"));
		applyForm.setFieldValue("AGENT_FEE_RATE", map.getString("AGENT_FEE_RATE"));
		applyForm.setFieldValue("AGENT_FEE_AMOUNT", map.getString("AGENT_FEE_AMOUNT"));
		applyForm.setFieldValue("LIFE_INSU_FEE_RATE", map.getString("LIFE_INSU_FEE_RATE"));
		applyForm.setFieldValue("LIFE_INSURANCE_AMT", map.getString("LIFE_INSURANCE_AMT"));
		applyForm.setFieldValue("JION_LIFE_INSURANCE", map.getString("JION_LIFE_INSURANCE"));
		applyForm.setFieldValue("LOAN_PURPOSE", map.getString("LOAN_PURPOSE"));
		applyForm.setFieldValue("LOAN_FIXED_AMT", map.getString("LOAN_FIXED_AMT"));
		applyForm.setFieldValue("SERIAL_NO", map.getString("SERIAL_NO"));
		applyForm.setFieldValue("RESIZE", map.getString("RESIZE"));
		
		if(map.getData("INSURANCE_INFO") != null && ! "null".equals(map.getData("INSURANCE_INFO").toString())){
			MapData map5 = map.getData("INSURANCE_INFO").asMapData();
			insuranceForm.setFieldValue("EXIST_EMP_EDW", map5.getString("EXIST_EMP_EDW"));
			insuranceForm.setFieldValue("EMP_EDW_BASE", map5.getString("EMP_EDW_BASE"));
			insuranceForm.setFieldValue("EMP_EDW_START_DT", map5.getString("EMP_EDW_START_DT"));
			insuranceForm.setFieldValue("EMP_EDW_MONTHS", map5.getString("EMP_EDW_MONTHS"));
			insuranceForm.setFieldValue("EXIST_EMP_MDC_INSU", map5.getString("EXIST_EMP_MDC_INSU"));
			insuranceForm.setFieldValue("EMP_MDC_INSU_BASE", map5.getString("EMP_MDC_INSU_BASE"));
			insuranceForm.setFieldValue("EMP_MDC_INSU_START_DT", map5.getString("EMP_MDC_INSU_START_DT"));
			insuranceForm.setFieldValue("EMP_MDC_INSU_MONTHS", map5.getString("EMP_MDC_INSU_MONTHS"));
			insuranceForm.setFieldValue("EXIST_INJU_INSU", map5.getString("EXIST_INJU_INSU"));
			insuranceForm.setFieldValue("EXIST_UNEMP_INSU", map5.getString("EXIST_UNEMP_INSU"));
			insuranceForm.setFieldValue("EXIST_BIRTH_INSU", map5.getString("EXIST_BIRTH_INSU"));
			insuranceForm.setFieldValue("EXIST_RSDT_OLD_INSU", map5.getString("EXIST_RSDT_OLD_INSU"));
			insuranceForm.setFieldValue("RSDT_OLD_INSU_LVL", map5.getString("RSDT_OLD_INSU_LVL"));
			insuranceForm.setFieldValue("RSDT_OLD_INSU_START_DT", map5.getString("RSDT_OLD_INSU_START_DT"));
			insuranceForm.setFieldValue("RSDT_OLD_INSU_YEARS", map5.getString("RSDT_OLD_INSU_YEARS"));
			insuranceForm.setFieldValue("EXIST_RSDT_MDC_INSU", map5.getString("EXIST_RSDT_MDC_INSU"));
			insuranceForm.setFieldValue("RSDT_MDC_INSU_LVL", map5.getString("RSDT_MDC_INSU_LVL"));
			insuranceForm.setFieldValue("RSDT_MDC_INSU_START_DT", map5.getString("RSDT_MDC_INSU_START_DT"));
			insuranceForm.setFieldValue("RSDT_MDC_INSU_YEARS", map5.getString("RSDT_MDC_INSU_YEARS"));
		}
		
		if(map.getData("SOCIAL_ACCOUNT_INFO") != null && ! "null".equals(map.getData("SOCIAL_ACCOUNT_INFO").toString())){
			ListData map2 = map.getData("SOCIAL_ACCOUNT_INFO").asListData();
			socialGrid.loadData(map2);
		}
		
		if(map.getData("CONTACT_INFO") != null && ! "null".equals(map.getData("CONTACT_INFO").toString())){
			ListData map3 = map.getData("CONTACT_INFO").asListData();
			contactGrid.loadData(map3);
		}
		
		if(map.getData("ACCESSORY_INFO") != null && ! "null".equals(map.getData("ACCESSORY_INFO").toString())){
			ListData map4 = map.getData("ACCESSORY_INFO").asListData();
			accessoryGrid.loadData(map4);
		}
	}
	
	//商品form
	private  IsWidget createProductLayoutCanvas() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("98%");
		panel.setHeight("270px");
		
		orderForm.setField(
				ORDER_ID.setGroup("订单信息"),
				REFERENCE_ID,
				COMPANY_ID,
				TERMINAL_ID,
				RA_ID,
				CUSTOMER_ID,
				PROMOTION_ID,
				TOTAL_AMOUNT,
				TOTAL_QUANTITY,
				CONSIGNEE,
				CONSIGNEE_PHONE,
				CONSIGNEE_ADDRESS,
				INTERNAL_CODE,
				COLLECTION_DOWNPAYMENT,
				DOWN_PAYMENT,
				SUBSIDY_RATIO,
				PAYMENT_TYPE,
				ORDER_CREATE_TIME,
				TRANSACTION_TIME
				);
		orderForm.getSetting().labelWidth(140);
		
		orderProductGrid.getSetting().usePager(false);
		orderProductGrid.getSetting().checkbox(false);
		orderProductGrid.setWidth(1150);
		orderProductGrid.setHeight(150);
		orderProductGrid.setTitle("订单商品信息");
		orderProductGrid.setColumns(
				COMMODITY_ID.setColumnWidth(100),
				CATEGORY_LEVEL_1_ID.setColumnWidth(100),
				CATEGORY_LEVEL_1_NAME.setColumnWidth(100),
				CATEGORY_LEVEL_2_ID.setColumnWidth(100),
				CATEGORY_LEVEL_2_NAME.setColumnWidth(100),
				CATEGORY_LEVEL_3_ID.setColumnWidth(100),
				CATEGORY_LEVEL_3_NAME.setColumnWidth(100),
				BRAND.setColumnWidth(100),
				COMMODITY_NAME.setColumnWidth(100),
				PRICE.setColumnWidth(100),
				QUANTITY.setColumnWidth(100)
				);
		
		panel.add(orderForm);
		panel.add(orderProductGrid);
		
		return panel;
	}
	
	public void updateProductLayoutView(Data result) {
		if(result == null){
			return ;
		}
		this.map = result.asMapData();
		if(map.getData("ORDER_INFO") != null && ! "null".equals(map.getData("ORDER_INFO").toString())){
			MapData map2 = map.getData("ORDER_INFO").asMapData();
			orderForm.setFieldValue("ORDER_ID", map2.getString("ORDER_ID"));
			orderForm.setFieldValue("REFERENCE_ID", map2.getString("REFERENCE_ID"));
			orderForm.setFieldValue("COMPANY_ID", map2.getString("COMPANY_ID"));
			orderForm.setFieldValue("TERMINAL_ID", map2.getString("TERMINAL_ID"));
			orderForm.setFieldValue("RA_ID", map2.getString("RA_ID"));
			orderForm.setFieldValue("CUSTOMER_ID", map2.getString("CUSTOMER_ID"));
			orderForm.setFieldValue("PROMOTION_ID", map2.getString("PROMOTION_ID"));
			orderForm.setFieldValue("TOTAL_AMOUNT", map2.getString("TOTAL_AMOUNT"));
			orderForm.setFieldValue("TOTAL_QUANTITY", map2.getString("TOTAL_QUANTITY"));
			orderForm.setFieldValue("CONSIGNEE", map2.getString("CONSIGNEE"));
			orderForm.setFieldValue("CONSIGNEE_PHONE", map2.getString("CONSIGNEE_PHONE"));
			orderForm.setFieldValue("CONSIGNEE_ADDRESS", map2.getString("CONSIGNEE_ADDRESS"));
			orderForm.setFieldValue("INTERNAL_CODE", map2.getString("INTERNAL_CODE"));
			orderForm.setFieldValue("COLLECTION_DOWNPAYMENT", map2.getString("COLLECTION_DOWNPAYMENT"));
			orderForm.setFieldValue("DOWN_PAYMENT", map2.getString("DOWN_PAYMENT"));
			orderForm.setFieldValue("SUBSIDY_RATIO", map2.getString("SUBSIDY_RATIO"));
			orderForm.setFieldValue("PAYMENT_TYPE", map2.getString("PAYMENT_TYPE"));
			orderForm.setFieldValue("ORDER_CREATE_TIME", map2.getString("ORDER_CREATE_TIME"));
			orderForm.setFieldValue("TRANSACTION_TIME", map2.getString("TRANSACTION_TIME"));
			//接口格式改变
			if(map2.getData("ORDER_PRODUCT_INFO") != null && ! "null".equals(map2.getData("ORDER_PRODUCT_INFO").toString())){
				ListData map3 = map2.getData("ORDER_PRODUCT_INFO").asListData();
				orderProductGrid.loadData(map3);
			}
		}
	}
	
	//商品form
	private  IsWidget createTravelLayout() {
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("98%");
		panel.setHeight("270px");
		
		travelOrderInfoForm.setField(
				RETAILER_ID.setGroup("旅游订单信息"),
				RETAILER_NAME,
				RA_ID1,
				ORDER_ID1,
				INTERNAL_CODE1,
				TOTAL_GOODS_NUM,
				TOTAL_GOODS_PRICE,
				GOODS_NAME,
				GOODS_CATEGORY_CODE,
				GOODS_CATEGORY,
				GOODS_TYPE_CODE,
				GOODS_TYPE,
				DOWN_PAYMENT1,
				DOWN_PAYMENT_PERCENT,
				ORIGIN,
				DESTINATION,
				DEPARTURE_TIME,
				RETURN_TIME,
				TRAVEL_NUM,
				TRAVEL_KIDS_NUM,
				TRAVEL_TYPE,
				IS_NEED_VISA
				);
		travelOrderInfoForm.getSetting().labelWidth(140);
		
		travelPeerInfoGrid.getSetting().usePager(false);
		travelPeerInfoGrid.getSetting().checkbox(false);
		travelPeerInfoGrid.setWidth(450);
		travelPeerInfoGrid.setHeight(150);
		travelPeerInfoGrid.setTitle("同行人信息");
		travelPeerInfoGrid.setColumns(
				PEER_USER_NAME.setColumnWidth(100),
				PEER_ID_NO.setColumnWidth(100),
				PEER_PHONE.setColumnWidth(100),
				RELATIONSHIP.setColumnWidth(100)
				);
		
		panel.add(travelOrderInfoForm);
		panel.add(travelPeerInfoGrid);
		
		return panel;
	}
	
	public void updateTravelView(Data result) {
		if(result == null){
			return ;
		}
		this.map = result.asMapData();
		
		if(map.getData("TRAVEL_ORDER_INFO") != null && ! "null".equals(map.getData("TRAVEL_ORDER_INFO").toString())){
			MapData map1 = map.getData("TRAVEL_ORDER_INFO").asMapData();
			travelOrderInfoForm.setFieldValue("RETAILER_ID", map1.getString("RETAILER_ID"));
			travelOrderInfoForm.setFieldValue("RETAILER_NAME", map1.getString("RETAILER_NAME"));
			travelOrderInfoForm.setFieldValue("RA_ID1", map1.getString("RA_ID"));
			travelOrderInfoForm.setFieldValue("ORDER_ID1", map1.getString("ORDER_ID"));
			travelOrderInfoForm.setFieldValue("INTERNAL_CODE1", map1.getString("INTERNAL_CODE"));
			travelOrderInfoForm.setFieldValue("TOTAL_GOODS_NUM", map1.getString("TOTAL_GOODS_NUM"));
			travelOrderInfoForm.setFieldValue("TOTAL_GOODS_PRICE", map1.getString("TOTAL_GOODS_PRICE"));
			travelOrderInfoForm.setFieldValue("GOODS_NAME", map1.getString("GOODS_NAME"));
			travelOrderInfoForm.setFieldValue("GOODS_CATEGORY_CODE", map1.getString("GOODS_CATEGORY_CODE"));
			travelOrderInfoForm.setFieldValue("GOODS_CATEGORY", map1.getString("GOODS_CATEGORY"));
			travelOrderInfoForm.setFieldValue("GOODS_TYPE_CODE", map1.getString("GOODS_TYPE_CODE"));
			travelOrderInfoForm.setFieldValue("GOODS_TYPE", map1.getString("GOODS_TYPE"));
			travelOrderInfoForm.setFieldValue("DOWN_PAYMENT1", map1.getString("DOWN_PAYMENT"));
			travelOrderInfoForm.setFieldValue("DOWN_PAYMENT_PERCENT", map1.getString("DOWN_PAYMENT_PERCENT"));
			travelOrderInfoForm.setFieldValue("ORIGIN", map1.getString("ORIGIN"));
			travelOrderInfoForm.setFieldValue("DESTINATION", map1.getString("DESTINATION"));
			travelOrderInfoForm.setFieldValue("DEPARTURE_TIME", map1.getString("DEPARTURE_TIME"));
			travelOrderInfoForm.setFieldValue("RETURN_TIME", map1.getString("RETURN_TIME"));
			travelOrderInfoForm.setFieldValue("TRAVEL_NUM", map1.getString("TRAVEL_NUM"));
			travelOrderInfoForm.setFieldValue("TRAVEL_KIDS_NUM", map1.getString("TRAVEL_KIDS_NUM"));
			travelOrderInfoForm.setFieldValue("TRAVEL_TYPE", map1.getString("TRAVEL_TYPE"));
			travelOrderInfoForm.setFieldValue("IS_NEED_VISA", map1.getString("IS_NEED_VISA"));
			
			if(map1.getData("TRAVEL_PEER_INFO") != null && ! "null".equals(map1.getData("TRAVEL_PEER_INFO").toString())){
				ListData map2 = map1.getData("TRAVEL_PEER_INFO").asListData();
				travelPeerInfoGrid.loadData(map2);
			}
		}
	}
	
	public void clearApplyDetailLayoutValues(){
		applyForm.getUi().clear();
		insuranceForm.getUi().clear();
		socialGrid.clearData();
		contactGrid.clearData();
		accessoryGrid.clearData();
	}
	
	public void clearProductLayoutValues(){
		orderForm.getUi().clear();
		orderProductGrid.clearData();
	}
	
	public void clearTravelLayoutValues(){
		travelOrderInfoForm.getUi().clear();
		travelPeerInfoGrid.clearData();
	}
	
}
