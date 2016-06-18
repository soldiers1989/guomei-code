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
//import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
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
//public class AuthVelocityCheckTest {
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
//		msg.getBodyAttributes().put(2, "6200480204984198");
//		msg.getBodyAttributes().put(3, "01");
//		msg.getBodyAttributes().put(4, "1000");
//		msg.getBodyAttributes().put(6, "1000");
//		msg.getBodyAttributes().put(14, "1234");
//		msg.getBodyAttributes().put(18, "6011");
//		msg.getBodyAttributes().put(19, "156");
//		msg.getBodyAttributes().put(22, "021");
//		msg.getBodyAttributes().put(23, "");
//		msg.getBodyAttributes().put(35,"6200480204984198=12345678901234567890");
//		msg.getBodyAttributes().put(38, "600001");
//		msg.getBodyAttributes().put(42, "10001");
//		msg.getBodyAttributes().put(45, "6012323232323232");
//		msg.getBodyAttributes().put(48, "ASIN0101234567890");
//		msg.getBodyAttributes().put(49, "156");
//		msg.getBodyAttributes().put(52, "10");
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
//		// [Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put(CheckType.CountryCodeCheckFlag, true);	
//		CountryCtrl countryCtrl = parameterFacility.loadParameter("156", CountryCtrl.class);
//		countryCtrl.validInd = false;	
//		
//		return authProduct;
//	}
//	
//	/**
//	 * 测试目的 : [Vel_001] [消费]当期消费金额检查
//	 * 测试组装的基本要素 : transAmt + $ctdUsedAmt > $cycleRetailLmt
//	 * 测试判断标准: 返回Reanso: F006 [65]
//	 * 测试控制台输出：1. 命中原因[F006]，对应行为[D]；2. 排序后reasoncd = F006优先级= 6000
//	 */
//	public String doAuthVelocity001CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		msg.getBodyAttributes().put(3, "00");
//		msg.getBodyAttributes().put(18, "1001");
//		for(CcsCardO c  : cps.getCcsCardO()){
//			c.setCtdUsedAmt(BigDecimal.valueOf(1000));
//			c.setCycleRetailLmt(BigDecimal.valueOf(1000));
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Vel_002] [取现]当期取现金额检查
//	 * 测试组装的基本要素 : transAmt + $ctdCashAmt > $cycleCashLmt
//	 * 测试判断标准: 返回Reanso: F022 [65]
//	 * 测试控制台输出：1. 命中原因[F022]，对应行为[D]；2. 排序后reasoncd = F022优先级= 6000
//	 */
//	public String doAuthVelocity002CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		for(CcsCardO c  : cps.getCcsCardO()){
//			c.setCtdCashAmt(BigDecimal.valueOf(1000));
//			c.setCycleCashLmt(BigDecimal.valueOf(1000));
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Vel_003] [消费]当期网银交易金额检查
//	 * 测试组装的基本要素 : $ctdNetAmt + transAmt > $cycleNetLmt , AuthTransType == (Auth,PreAuth,AgentDebit), AuthTransTerminal == EB
//	 * 测试判断标准: 返回Reanso: F034 [65]
//	 * 测试控制台输出：1. 命中原因[F034]，对应行为[D]；2. 排序后reasoncd = F034优先级= 6000
//	 */
//	public String doAuthVelocity003CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		msg.getBodyAttributes().put(3, "00");
//		msg.getBodyAttributes().put(18, "1001");
//		msg.getBodyAttributes().put(40, "150");
//		msg.getCustomAttributes().put(CustomAttributesKey.INPUT_SOURCE,InputSource.BANK);
//		for(CcsCardO c  : cps.getCcsCardO()){
//			c.setCtdNetAmt(BigDecimal.valueOf(1000));
//			c.setCycleNetLmt(BigDecimal.valueOf(1000));
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Vel_004] 单日ATM取现限额检查
//	 * 测试组装的基本要素 : AuthTransType == Cash , AuthTransTerminal == ATM , $dayUsedAtmAmt + chbTransAmt > $dayAtmLimit 
//	 * 测试判断标准: 返回Reanso: F014 [65]
//	 * 测试控制台输出：1. 命中原因[F014]，对应行为[D]；2. 排序后reasoncd = F014优先级= 6000
//	 */
//	public String doAuthVelocity004CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		for(CcsCardO c  : cps.getCcsCardO()){
//			c.setDayUsedAtmAmt(BigDecimal.valueOf(2000));
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Vel_005] 单日ATM取现限笔检查
//	 * 测试组装的基本要素 : AuthTransType == Cash , AuthTransTerminal == ATM , $dayUsedAtmNbr +1 > $dayAtmNbr
//	 * 测试判断标准: 返回Reanso: F013 [61]
//	 * 测试控制台输出：1. 命中原因[F013]，对应行为[D]；2. 排序后reasoncd = F013优先级= 6000
//	 */
//	public String doAuthVelocity005CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		for(CcsCardO c  : cps.getCcsCardO()){
//			c.setDayUsedAtmNbr(999);
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Vel_006] 单日取现限额检查
//	 * 测试组装的基本要素 : AuthTransType == Cash , $dayUsedCashAmt + chbTransAmt > $dayCashAmtLimit
//	 * 测试判断标准: 返回Reanso: F018 [65]
//	 * 测试控制台输出：1. 命中原因[F018]，对应行为[D]；2. 排序后reasoncd = F018优先级= 6000
//	 */
//	public String doAuthVelocity006CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		for(CcsCardO c  : cps.getCcsCardO()){
//			c.setDayUsedCashAmt(BigDecimal.valueOf(2000));
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Vel_007] 单日取现限笔检查
//	 * 测试组装的基本要素 : AuthTransType == Cash , $dayUsedCashNbr + 1 > $dayCshNbrLimit
//	 * 测试判断标准: 返回Reanso: F017 [61]
//	 * 测试控制台输出：1. 命中原因[F017]，对应行为[D]；2. 排序后reasoncd = F017优先级= 6000
//	 */
//	public String doAuthVelocity007CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		for(CcsCardO c  : cps.getCcsCardO()){
//			c.setDayUsedCashNbr(999);
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Vel_008] 单日消费限额检查
//	 * 测试组装的基本要素 : AuthTransType == (Auth , AgentDebit , PreAuth) ,  $dayUsedRetailAmt + chbTransAmt > $dayRetailAmtLimit
//	 * 测试判断标准: 返回Reanso: F043 [61]
//	 * 测试控制台输出：1. 命中原因[F043]，对应行为[D]；2. 排序后reasoncd = F043优先级= 6000
//	 */
//	public String doAuthVelocity008CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		msg.getBodyAttributes().put(3, "00");
//		msg.getBodyAttributes().put(18, "1001");
//		for(CcsCardO c  : cps.getCcsCardO()){
//			c.setDayUsedRetailAmt(BigDecimal.valueOf(10000));
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Vel_009] 单日消费限笔检查
//	 * 测试组装的基本要素 : AuthTransType == (Auth , AgentDebit , PreAuth) ,  $dayUsedRetailNbr +1 > $dayRetailNbrLimit
//	 * 测试判断标准: 返回Reanso: F043 [61]
//	 * 测试控制台输出：1. 命中原因[F043]，对应行为[D]；2. 排序后reasoncd = F043优先级= 6000
//	 */
//	public String doAuthVelocity009CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		// 命中规则条件的数据
//		msg.getBodyAttributes().put(3, "00");
//		msg.getBodyAttributes().put(18, "1001");
//		for(CcsCardO c  : cps.getCcsCardO()){
//			c.setDayUsedRetailNbr(999);
//		}
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * [Vel_001] [消费]当期消费金额检查
//	 */
//	@Test
//	public void authVelocity001CheckTest() {
//		Assert.assertEquals("65", doAuthVelocity001CheckTest());
//	}
//	
//	/**
//	 * [Vel_002] [取现]当期取现金额检查
//	 */
//	@Test
//	public void authVelocity002CheckTest() {
//		Assert.assertEquals("65", doAuthVelocity002CheckTest());
//	}
//	
//	/**
//	 * [Vel_003] [消费]当期网银交易金额检查
//	 */
//	@Test
//	public void authVelocity003CheckTest() {
//		Assert.assertEquals("65", doAuthVelocity003CheckTest());
//	}
//	
//	/**
//	 * [Vel_004] 单日ATM取现限额检查
//	 */
//	@Test
//	public void authVelocity004CheckTest() {
//		Assert.assertEquals("65", doAuthVelocity004CheckTest());
//	}
//	
//	/**
//	 * [Vel_005] 单日ATM取现限笔检查
//	 */
//	@Test
//	public void authVelocity005CheckTest() {
//		Assert.assertEquals("61", doAuthVelocity005CheckTest());
//	}
//	
//	/**
//	 * [Vel_006] 单日取现限额检查
//	 */
//	@Test
//	public void authVelocity006CheckTest() {
//		Assert.assertEquals("65", doAuthVelocity006CheckTest());
//	}
//	
//	/**
//	 * [Vel_007] 单日取现限笔检查
//	 */
//	@Test
//	public void authVelocity007CheckTest() {
//		Assert.assertEquals("61", doAuthVelocity007CheckTest());
//	}
//	
//	/**
//	 * [Vel_008] 单日消费限额检查
//	 */
//	@Test
//	public void authVelocity008CheckTest() {
//		Assert.assertEquals("61", doAuthVelocity008CheckTest());
//	}
//	
//	/**
//	 * [Vel_009] 单日消费限笔检查
//	 */
//	@Test
//	public void authVelocity009CheckTest() {
//		Assert.assertEquals("65", doAuthVelocity009CheckTest());
//	}
//}
