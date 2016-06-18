package com.sunline.ccs.service.auth.test.verifier;
//package com.sunline.ccs.service.auth.test.verifier;
//
//import java.math.BigDecimal;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import junit.framework.Assert;
//
//import org.apache.commons.lang.time.DateUtils;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.annotation.DirtiesContext.ClassMode;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.sunline.ppy.api.AuthorizationService;
//import com.sunline.ppy.api.CustomAttributesKey;
//import com.sunline.ppy.api.MediumInfo;
//import com.sunline.ppy.dictionary.enums.ExpiryDateFlag;
//import com.sunline.ppy.dictionary.enums.Indicator;
//import com.sunline.ppy.dictionary.enums.InputSource;
//import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
//import com.sunline.pcm.param.def.Product;
//import com.sunline.pcm.param.test.BmpParamGenerator;
//import com.sunline.pcm.service.sdk.ParameterServiceMock;
//import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
//import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
//import com.sunline.ccs.param.def.AuthProduct;
//import com.sunline.ccs.param.def.CountryCtrl;
//import com.sunline.ccs.param.def.enums.CheckType;
//import com.sunline.ccs.service.auth.test.mock.MediumServiceMock;
//import com.sunline.ccs.test.CPSDataSet;
//import com.sunline.ccs.test.DefaultParamGenerator;
//import com.sunline.ccs.test.TestDataGenerator;
//import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
//import com.sunline.ark.support.OrganizationContextHolder;
//import com.sunline.ark.support.service.YakMessage;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("/test-service.xml")
//@Transactional
//@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
//public class AuthOtbCheckTest {
//
//	@Autowired
//	private AuthorizationService authService;
//
//	@Autowired
//	private MediumServiceMock mediumMock;
//
//	@Autowired
//	private GlobalManagementServiceMock globalMock;
//
//	@Autowired
//	private TestDataGenerator dataGenerator;
//
//	@Autowired
//	private ParameterServiceMock parameterMock;
//
//	@Autowired
//	private UnifiedParameterFacility parameterFacility;
//	/**
//	 * 默认参数生成工具
//	 */
//	@Autowired
//	private DefaultParamGenerator defaultParamGenerator;
//
//	/**
//	 * bmp参数生成工具
//	 */
//	@Autowired
//	private BmpParamGenerator bmpParamGenerator;
//
//	private CPSDataSet cps;
//
//	private Product product;
//
//	@Before
//	public void setup() throws ParseException {
//		OrganizationContextHolder.setCurrentOrg("123456789012");
//		Date today = new SimpleDateFormat("yyyyMMdd").parse("20120919");
//		globalMock.setupBatchDate(today, DateUtils.addDays(today, -1));
//
//		product = bmpParamGenerator.loadParameters(parameterMock, null);
//		defaultParamGenerator.loadParameters(parameterMock, product.productCode);
//
//		cps = dataGenerator.createActMainCard(product.productCode, today);
//
//		MediumInfo info = new MediumInfo();
//		info.setArqcVerifyResult(PasswordVerifyResult.Approve);
//		info.setLogicCardNbr(cps.getCcsCardO().get(0).getLogicCardNbr());
//		mediumMock.setMediumInfo(info);
//
//	}
//	
//	/**
//	 * 初始化YakMsg
//	 * @return
//	 */
//	private YakMessage initYakMessage() {
//		YakMessage msg = new YakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		
//		msg.getBodyAttributes().put(2, "6200480204984198");
//		msg.getBodyAttributes().put(3, "00");
//		// 设置交易金额
//		msg.getBodyAttributes().put(4, "500000");
//		msg.getBodyAttributes().put(6, "1");
//		msg.getBodyAttributes().put(14, "1234");
//		msg.getBodyAttributes().put(18, "1001");
//		msg.getBodyAttributes().put(19, "156");
//		msg.getBodyAttributes().put(22, "661");
//		msg.getBodyAttributes().put(23, "");
//		msg.getBodyAttributes().put(35,"6200480204984198=12345678901234567890");
//		msg.getBodyAttributes().put(38, "600001");
//		// 特殊交易商户控制表(Key)
//		msg.getBodyAttributes().put(42, "10001");
//		msg.getBodyAttributes().put(45, "6012323232323232");
//		msg.getBodyAttributes().put(48, "ASIN0101234567890");
//		msg.getBodyAttributes().put(49, "156");
//		msg.getBodyAttributes().put(52, "10");
//		msg.getBodyAttributes().put(60, "1234577512555901234567890");
//		msg.getBodyAttributes().put(61, "1168461641684964494984949849489497479468497940");
//
//		msg.getCustomAttributes().put("cardActiveFlag", Indicator.Y);
//		msg.getCustomAttributes().put("blockCodes", "1234ABCD");
//		msg.getCustomAttributes().put("expiryDateFlag", ExpiryDateFlag.Space);
//		msg.getCustomAttributes().put("passwordVerifyResult",	PasswordVerifyResult.Approve);
//		msg.getCustomAttributes().put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME,new Date());
//		msg.getCustomAttributes().put(CustomAttributesKey.INPUT_SOURCE,InputSource.CUP);
//		msg.getCustomAttributes().put(CustomAttributesKey.MTI, "0100");
//		
//		return msg;
//	}
//
//	/**
//	 * 初始化AuthProduct
//	 * @return
//	 */
//	public AuthProduct initAuthParameter(){
//		AuthProduct authProduct = parameterFacility.loadParameter(product.productCode, AuthProduct.class);
//		// [Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put(CheckType.MccCodeCheckFlag, false);
//
//		// [Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put(CheckType.CountryCodeCheckFlag, true);	
//		CountryCtrl countryCtrl = parameterFacility.loadParameter("156", CountryCtrl.class);
//		countryCtrl.validInd = false;	
//		
//		return authProduct;
//	}
//	
//	/**
//	 * 测试目的 : [Otb_001]  账户可用额验证
//	 * 测试组装的基本要素 : chbTransAmt > accountOTB
//	 * 测试判断标准: 返回Reanso: B001 [51]
//	 * 测试控制台输出：1. 命中原因[B001]，对应行为[D]；2. 排序后reasoncd =B001优先级= 6000
//	 */
//	public String doAuthOtb001CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		
//		// 命中规则条件的数据
//		for(CcsAcctO a : cps.getCcsAcctO()){
//			a.setCreditLmt(BigDecimal.valueOf(2000));
//		}
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Otb_002]  客户可用额验证
//	 * 测试组装的基本要素 : chbTransAmt > customerOTB
//	 * 测试判断标准: 返回Reanso: B003ProcCode [51]
//	 * 测试控制台输出：1. 命中原因[B003ProcCode]，对应行为[D]；2. 排序后reasoncd =B003ProcCode优先级= 6000
//	 */
//	public String doAuthOtb002CardNbrCheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		for(CcsCustomerCrlmt c : cps.getCcsCustomerCrlmt()){
//			c.setCreditLmt(BigDecimal.valueOf(2000));
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Otb_003]  逻辑卡消费限额验证
//	 * 测试组装的基本要素 : chbTransAmt > CcsCardO.txnLmt
//	 * 测试判断标准: 返回Reanso: B005 [51]
//	 * 测试控制台输出：1. 命中原因[B005]，对应行为[D]；2. 排序后reasoncd =B005优先级= 6000
//	 */
//	public String doAuthOtb003ProcCodeCheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		for(CcsCardO c : cps.getCcsCardO()){
//			c.setTxnLimit(BigDecimal.valueOf(2000));
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Otb_004] [取现]账户取现可用额验证
//	 * 测试组装的基本要素 : transType == AuthTransType.Cash, chbTransAmt > cashOTB
//	 * 测试判断标准: 返回Reanso: B002CardNbr [51]
//	 * 测试控制台输出：1. 命中原因[B002CardNbr]，对应行为[D]；2. 排序后reasoncd =B002CardNbr优先级= 6000
//	 */
//	public String doAuthOtb004AmtCheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		msg.getBodyAttributes().put(3, "01");
//		msg.getBodyAttributes().put(4, "600000");
//		msg.getBodyAttributes().put(18, "6011");
//		for(CcsCardO c : cps.getCcsCardO()){
//			c.setTxnLimit(BigDecimal.valueOf(20000));
//			c.setTxnCashLimit(BigDecimal.valueOf(20000));
//			c.setCycleCashLmt(BigDecimal.valueOf(20000));
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Otb_005] [取现]逻辑卡单笔取现限额验证
//	 * 测试组装的基本要素 : transType == AuthTransType.Cash, chbTransAmt > $txnCashLmt
//	 * 测试判断标准: 返回Reanso: B006ChbAmt [51]
//	 * 测试控制台输出：1. 命中原因[B006ChbAmt]，对应行为[D]；2. 排序后reasoncd =B006ChbAmt优先级= 6000
//	 */
//	public String doAuthOtb005CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		msg.getBodyAttributes().put(3, "01");
//		msg.getBodyAttributes().put(4, "510000");
//		msg.getBodyAttributes().put(18, "6011");
//		for(CcsCardO c : cps.getCcsCardO()){
//			c.setTxnLimit(BigDecimal.valueOf(20000));
//			c.setTxnCashLimit(BigDecimal.valueOf(2000));
//			c.setCycleCashLmt(BigDecimal.valueOf(20000));
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 :  [Otb_001-003]  通过的交易
//	 * 测试组装的基本要素 : chbTransAmt <= accountOTB 、chbTransAmt <= customerOTB 、chbTransAmt <= $txnLmt
//	 * 测试判断标准: 返回responsCode = 00 通过
//	 */
//	public String doAuthOtb123CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 :  [Otb_004-005]  通过的交易
//	 * 测试组装的基本要素 : transType == AuthTransType.Cash , chbTransAmt <= cashOTB ,  chbTransAmt <= $txnCashLmt
//	 * 测试判断标准: 返回responsCode = 00 通过
//	 */
//	public String doAuthOtb45CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		msg.getBodyAttributes().put(3, "01");
//		msg.getBodyAttributes().put(4, "500000");
//		msg.getBodyAttributes().put(18, "6011");
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//
//	/**
//	 * [Otb_001-005]  正向通过的交易
//	 */
//	@Test
//	public void authOtbAllPassCheckTest() {
//		Assert.assertEquals("00", doAuthOtb123CheckTest());
//		Assert.assertEquals("00", doAuthOtb45CheckTest());
//	}
//
//	/**
//	 * [Otb_001]  账户可用额验证
//	 */
//	@Test
//	public void authOtb001CheckTest() {
//		Assert.assertEquals("51", doAuthOtb001CheckTest());
//	}
//	
//	/**
//	 * [Otb_002]  客户可用额验证
//	 */
//	@Test
//	public void authOtb002CardNbrCheckTest() {
//		Assert.assertEquals("51", doAuthOtb002CardNbrCheckTest());
//	}
//	
//	/**
//	 * [Otb_003]  逻辑卡消费限额验证
//	 */
//	@Test
//	public void authOtb003ProcCodeCheckTest() {
//		Assert.assertEquals("51", doAuthOtb003ProcCodeCheckTest());
//	}
//	
//	/**
//	 * [Otb_004] [取现]账户取现可用额验证
//	 */
//	@Test
//	public void authOtb004AmtCheckTest() {
//		Assert.assertEquals("51", doAuthOtb004AmtCheckTest());
//	}
//	
//	/**
//	 * [Otb_005] [取现]账户取现可用额验证
//	 */
//	@Test
//	public void authOtb005CheckTest() {
//		Assert.assertEquals("51", doAuthOtb005CheckTest());
//	}
//}
